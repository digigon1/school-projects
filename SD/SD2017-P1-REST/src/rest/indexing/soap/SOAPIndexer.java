package rest.indexing.soap;

import api.Document;
import api.ServerConfig;
import api.soap.IndexerService;
import com.google.gson.Gson;
import com.mongodb.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import sys.storage.LocalVolatileStorage;
import sys.storage.Storage;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Created by goncalo on 11-04-2017.
 */
@WebService(
		serviceName = IndexerService.NAME,
		targetNamespace = IndexerService.NAMESPACE,
		endpointInterface = IndexerService.INTERFACE)
public class SOAPIndexer implements IndexerService {

	private Gson gson;
	private Properties properties;
	private DB mongoDB;
	private Storage s;
	private String secret;

	public SOAPIndexer(){
		this.gson = new Gson();
		this.s = new LocalVolatileStorage();
		try {
			MongoClientURI mongoClientURI = new MongoClientURI("mongodb://mongo1,mongo2,mongo3/maria?w=2&readPreference=secondary");
			MongoClient mongo = new MongoClient(mongoClientURI);
			mongoDB = mongo.getDB("indexer");
		} catch (Exception e){
			e.printStackTrace();
			System.exit(1);
		}
		try{
			InetAddress address = InetAddress.getByName("230.229.213.230");

			int castPort = 9999;

			if (!address.isMulticastAddress())
				System.out.println("Use range: 224.0.0.0 -- 239.255.255.255");

			MulticastSocket socket = new MulticastSocket();
			socket.joinGroup(address);
			String data = "rendezvous";
			DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length());
			packet.setAddress(address);
			packet.setPort(castPort);
			socket.send(packet);

			//receive packet
			byte[] request = new byte[65536];
			DatagramPacket requestPacket = new DatagramPacket(request, request.length);
			socket.receive(requestPacket);

			socket.leaveGroup(address);

			properties = new Properties();
			properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka1:9092,kafka2:9092,kafka3:9092");
			properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
			properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

			new Thread(() -> {
				Properties props = new Properties();
				props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka1:9092,kafka2:9092,kafka3:9092");
				props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
				props.put(ConsumerConfig.GROUP_ID_CONFIG, "ran" + System.nanoTime());
				props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
				props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
				try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
					consumer.subscribe(Arrays.asList("add", "remove"));
					while (true) {
						ConsumerRecords<String, String> records = consumer.poll(1000);
						records.forEach(r -> {
							switch (r.topic()){
								case "add":
									addSpecial(r.key(), gson.fromJson(r.value(), Document.class));
									break;
								case "remove":
									s.remove(r.key());
									break;
								default:
									break;
							}
						});
					}
				}
			}).start();
		} catch (IOException e){
			System.out.println("Socket fail");
		}
	}

	public SOAPIndexer(String secret){
		this();
		this.secret = secret;
	}

	@Override
	public List<String> search(String keywords) throws InvalidArgumentException {
		if(keywords == null)
			throw new InvalidArgumentException("keywords can't be null");

		List<String> words = Arrays.asList(keywords.split("\\+"));
		DBCollection table = mongoDB.getCollection("documents");
		DBCursor c = table.find();
		List<Document> r = new ArrayList<>();
		while (c.hasNext()) {
			r.add(gson.fromJson((String) c.next().get("document"), Document.class));
		}
		return r.stream().filter(document -> {
			for(String s : words)
				if(!document.getKeywords().contains(s))
					return false;

			return true;
		}).map(Document::getUrl).collect(Collectors.toList());
	}

    @Override
    public void configure(String secret, ServerConfig config) throws InvalidArgumentException, SecurityException {
		if(secret == null || config == null)
			throw new InvalidArgumentException("Invalid argument");

		if(!this.secret.equals(secret))
			throw new SecurityException();
    }

    @Override
	public boolean add(Document doc, String secret) throws InvalidArgumentException, SecurityException {
		if (!this.secret.equals(secret))
			throw new SecurityException("secret is wrong");

		if (doc == null)
			throw new InvalidArgumentException("doc can't be null");

		String id = doc.id();

		DBCollection table = mongoDB.getCollection("documents");
		BasicDBObject o = new BasicDBObject();
		o.put("id", id);
		if (table.find(o).hasNext())
			return false;

		mongoDB.requestStart();
		o.put("document", gson.toJson(doc));
		table.insert(o);
		mongoDB.requestDone();

		try (Producer<String, String> producer = new KafkaProducer<>(properties)) {
			producer.send(new ProducerRecord<>("add", id, gson.toJson(doc)));
		}

		return true;
	}

	private void addSpecial(String id, Document doc){
		System.out.println("add "+id);
		s.store(id, doc);
	}

	@Override
	public boolean remove(String id, String secret) throws InvalidArgumentException, SecurityException {
        if(!this.secret.equals(secret))
            throw new SecurityException("secret is wrong");

        if(id == null)
			throw new InvalidArgumentException("id can't be null");

		DBCollection table = mongoDB.getCollection("documents");
		BasicDBObject o = new BasicDBObject();
		o.put("id", id);
		if (!table.find(o).hasNext())
			return false;

		mongoDB.requestStart();
		BasicDBObject end = new BasicDBObject();
		end.put("id", id);
		table.remove(end);
		mongoDB.requestDone();

		try (Producer<String, Document> producer = new KafkaProducer<>(properties)) {
			producer.send(new ProducerRecord<>("remove", id, null));
		}

		return true;
	}

	@WebMethod()
	public String ping(){
		return "pong";
	}
}

package rest.indexing.rest;

import api.Document;
import api.ServerConfig;
import api.rest.IndexerService;
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

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.*;

/**
 * Created by goncalo on 14-03-2017.
 */
public class IndexerServiceImplementation implements IndexerService {

	private final String name;
	private DB mongoDB;
	private Properties properties;
    private String uri;
	private Storage s;
	private String secret;
	private final Gson gson;
	//private long ran;

	IndexerServiceImplementation(String secret, String name){
		//this.ran = 0;
		this.name = name;

        this.secret = secret;
        this.gson = new Gson();
        this.s = new LocalVolatileStorage();
		this.uri = "";
		try {
			MongoClientURI uri = new MongoClientURI("mongodb://mongo1,mongo2,mongo3/maria?w=2&readPreference=secondary");
			MongoClient mongo = new MongoClient(uri);
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

			uri = new String(requestPacket.getData(), 0, requestPacket.getLength());
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
                        	//ran++;
                            switch (r.topic()){
                                case "add":
                                    addSpecial(r.key(), this.secret, gson.fromJson(r.value(), Document.class));
                                    break;
                                case "remove":
                                    removeSpecial(r.key(), this.secret);
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

	@Override
	public List<String> search(String keywords) {
		/* CAUSALITY ATTEMPT, DOES NOT WORK
		System.out.println("SEARCH "+meta);
		if(meta.equals(name+"-"+ran)){
			List<String> words = Arrays.asList(keywords.split("[ \\+]"));

			List<Document> docs = s.search(words);

			return docs.stream().map(Document::getUrl).collect(Collectors.toList());
		}
		*/

		List<String> words = Arrays.asList(keywords.split("[ \\+]"));
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
    public void configure(String secret, ServerConfig config) {
        if(!this.secret.equals(secret))
            throw new WebApplicationException(FORBIDDEN);

        throw new WebApplicationException(NOT_IMPLEMENTED);
    }

    @POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void add(@PathParam("id") String id, @QueryParam("secret") String secret, Document doc) {
        if(!this.secret.equals(secret)) {
			throw new WebApplicationException(FORBIDDEN);
		}

		if(!s.store(id, doc)) {
			throw new WebApplicationException(CONFLICT);
		}

		DBCollection table = mongoDB.getCollection("documents");
		BasicDBObject o = new BasicDBObject();
		o.put("id", id);
		if (table.find(o).hasNext())
			throw new WebApplicationException(CONFLICT);

		mongoDB.requestStart();
		o.put("document", gson.toJson(doc));
		table.insert(o);
		mongoDB.requestDone();

        try (Producer<String, String> producer = new KafkaProducer<>(properties)) {
            producer.send(new ProducerRecord<>("add", id, gson.toJson(doc)));
        }

        // CAUSALITY ATTEMPT, DOES NOT WORK
        //return Response.status(OK).header("X-indexer-metadata", name+"-"+(++ran)).build();
	}

    private void addSpecial(String id, String secret, Document doc) {
        if(!this.secret.equals(secret))
            throw new WebApplicationException(FORBIDDEN);

        s.store(id, doc);
    }

	@Override
	public void remove(String id, String secret) {
	    if(!this.secret.equals(secret))
	        throw new WebApplicationException(FORBIDDEN);

		DBCollection table = mongoDB.getCollection("documents");
		BasicDBObject o = new BasicDBObject();
		o.put("id", id);
		if (!table.find(o).hasNext())
			throw new WebApplicationException(NOT_FOUND);

		mongoDB.requestStart();
		BasicDBObject end = new BasicDBObject();
		end.put("id", id);
		table.remove(end);
		mongoDB.requestDone();

        try (Producer<String, Document> producer = new KafkaProducer<>(properties)) {
            producer.send(new ProducerRecord<>("remove", id, null));
        }

        // CAUSALITY ATTEMPT, DOES NOT WORK
		//return Response.ok().header("X-indexer-metadata", name+"-"+(++ran)).build();
	}

	private boolean removeSpecial(String id, String secret){
        if(!this.secret.equals(secret))
            throw new WebApplicationException(FORBIDDEN);

		return s.remove(id);
	}

	@GET
    @Path("/ping")
    @Produces(MediaType.APPLICATION_JSON)
    public String ping(){
		return "pong";
    }
}

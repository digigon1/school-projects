package rest.server;

import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import api.Endpoint;
import api.soap.IndexerService;
import com.google.gson.Gson;
import com.mongodb.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.glassfish.jersey.client.ClientProperties;

import static api.soap.IndexerService.*;
import static javax.ws.rs.core.Response.Status.*;

/**
 * Implementacao do servidor de rendezvous em REST 
 */
@Path("/contacts")
public class RendezVousResources {

	//private final Properties props;
	private DB mongoDB;
	//private KafkaProducer<String, String> producer;
	//private Properties properties;
	private Map<String, Endpoint> db = new ConcurrentHashMap<>();
    private String secret;
    private final Gson gson;

    public RendezVousResources(String secret){
    	try {
			MongoClientURI uri = new MongoClientURI("mongodb://mongo1,mongo2,mongo3/maria?w=2&readPreference=secondary");
			MongoClient mongo = new MongoClient(uri);
			mongoDB = mongo.getDB("rendezvous");
		} catch (Exception e){
			e.printStackTrace();
			System.exit(1);
		}

        this.secret = secret;
        this.gson = new Gson();

        /* KAFKA IMPLEMENTATION WAS REPLACED BY MONGO SOLUTION DUE TO APPARENT INTERNAL KAFKA BUG
        	OTHERWISE, IT IS COMPLETELY IMPLEMENTED
		properties = new Properties();
		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka1:9092,kafka2:9092,kafka3:9092");
		properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

		//TEST
		properties.put(ProducerConfig.CLIENT_ID_CONFIG, "test_user");

		props = new Properties();
		new Thread(() -> {
			props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka1:9092,kafka2:9092,kafka3:9092");
			props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
			props.put(ConsumerConfig.GROUP_ID_CONFIG, "Rendezvous-Group");
			props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
			props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

			//TEST
			props.put(ConsumerConfig.CLIENT_ID_CONFIG, "test_user");

			try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
				System.out.println("Subscribing");
				consumer.subscribe(Collections.singletonList("register_topic"));
				//consumer.subscribe(Arrays.asList("register_topic", "update", "unregister"));
				System.out.println("Subbed");
				while (true) {
					ConsumerRecords<String, String> records = consumer.poll(1000);
					//System.out.println("on loop: "+records.count());
					records.forEach(r -> {
						switch (r.topic()){
							case "register_topic":
								registerSpecial(r.key(), gson.fromJson(r.value(), Endpoint.class));
								break;
							case "update":
								updateSpecial(r.key(), gson.fromJson(r.value(), Endpoint.class));
								break;
							case "unregister":
								unregisterSpecial(r.key());
								break;
							default:
								break;
						}
						System.out.printf("topic = %s, key = %s, value = %s%n", r.topic(), r.key(), r.value());
					});
				}
			} catch (Exception e){
				e.printStackTrace();
			}
			System.out.println("Exiting thread");
		}).start();
		*/
    }

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Endpoint[] endpoints() {
    	try {
			DBCollection table = mongoDB.getCollection("endpoints");
			DBCursor c = table.find();
			List<Endpoint> r = new ArrayList<>();
			while (c.hasNext()) {
				r.add(gson.fromJson((String) c.next().get("endpoint"), Endpoint.class));
			}
			Endpoint[] result = new Endpoint[r.size()];
			return r.toArray(result);
		} catch (Exception e){
    		e.printStackTrace();
		}
		return null;
	}

	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void register(@PathParam("id") String id, @QueryParam("secret") String secret, Endpoint endpoint){
		System.err.println("register: "+id);

		if(this.secret.equals(secret)) {

			DBCollection table = mongoDB.getCollection("endpoints");
			BasicDBObject o = new BasicDBObject();
			o.put("id", id);
			if (table.find(o).hasNext())
				throw new WebApplicationException(CONFLICT);

			mongoDB.requestStart();
			BasicDBObject end = new BasicDBObject();
			end.put("id", id);
			end.put("endpoint", gson.toJson(endpoint));
			table.insert(end);
			mongoDB.requestDone();

			startPinging(id, endpoint);

			/* KAFKA IMPLEMENTATION FAILED WITH (SEEMINGLY) INTERNAL BUG, REPLACED WITH MONGODB SOLUTION
			try (Producer<String, String> producer = new KafkaProducer<>(properties)) {
				System.out.println("sending " + id);
				producer.send(new ProducerRecord<>("register_topic", id, gson.toJson(endpoint)));
				producer.flush();
				System.out.println("sent " + id);
				producer.close();
				System.out.println("closed");
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/

		} else {
			throw new WebApplicationException(FORBIDDEN);
		}
	}

	private void startPinging(String id, Endpoint endpoint){
		new Thread(()->{
			for(;;){
				try {
					Thread.sleep(1000);
					//TODO test!
					boolean executed = false;
					for (int i = 0; !executed && i < 3; i++) {
						try {
							if(endpoint.getAttributes().get("type").equals("rest")) {
								Client client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier()).build();

								client.property(ClientProperties.CONNECT_TIMEOUT, 5000);
								client.property(ClientProperties.READ_TIMEOUT, 5000);

								URI baseURI = UriBuilder.fromUri(endpoint.getUrl()).build();

								WebTarget target = client.target( baseURI );
								target.path("/indexer/ping").request().accept(MediaType.APPLICATION_JSON).get(String.class);
							} else {
								//SOAP
								System.out.println("TRYING TO PING SOAP");
								System.out.println(endpoint.getUrl());
								URL wsURL = new URL(endpoint.getUrl() + "/indexer");
								HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
								System.out.println("QName");
								QName qname = new QName(NAMESPACE, NAME);
								System.out.println("Service");
								Service service = Service.create(wsURL, qname);
								System.out.println("Indexer");
								IndexerService contacts = service.getPort(IndexerService.class);
								System.out.println("Ping");
								contacts.ping();
								System.out.println("SOAP PING DONE");
							}
						} catch (ProcessingException ex){
							System.out.println("Retrying");
							Thread.sleep(5000);
							continue;
						} catch (Exception ex){
							ex.printStackTrace();
							Thread.sleep(5000);
							continue;
						}
						executed = true;
					}
					if(!executed) {
						try {
							unregister(id, this.secret);
						} catch (Exception ignored){}

						Thread.currentThread().interrupt();
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}).start();
	}

	private void registerSpecial(String id, Endpoint endpoint) {
		System.err.printf("register: %s <%s>\n", id, endpoint);
		
		if (db.containsKey(id)) {
			System.err.println(id+" is in DB");
			throw new WebApplicationException(CONFLICT);
		} else {
			startPinging(id, endpoint);
			db.put(id, endpoint);
		}
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void update(@PathParam("id") String id, @QueryParam("secret") String secret, Endpoint endpoint) {
		if(this.secret.equals(secret)) {

			DBCollection table = mongoDB.getCollection("endpoints");
			BasicDBObject o = new BasicDBObject();
			o.put("id", id);
			if (!table.find(o).hasNext())
				throw new WebApplicationException(NOT_FOUND);

			mongoDB.requestStart();
			BasicDBObject n = new BasicDBObject();
			n.put("endpoint", gson.toJson(endpoint));
			table.update(o, n);
			mongoDB.requestDone();

			/*
			try (Producer<String, String> producer = new KafkaProducer<>(properties)) {
				producer.send(new ProducerRecord<>("update", id, gson.toJson(endpoint)));
			} catch (Exception e){
				e.printStackTrace();
			} finally {
				producer.close();
			}
			*/
		} else {
			throw new WebApplicationException(FORBIDDEN);
		}
	}

	private void updateSpecial(String id, Endpoint endpoint){
		System.err.printf("update: %s <%s>\n", id, endpoint);
		
		if (!db.containsKey(id))
			throw new WebApplicationException( NOT_FOUND );
		else
			db.put(id, endpoint);
	}

	@DELETE
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void unregister(@PathParam("id") String id, @QueryParam("secret") String secret) {
		if (this.secret.equals(secret)) {

			DBCollection table = mongoDB.getCollection("endpoints");
			BasicDBObject o = new BasicDBObject();
			o.put("id", id);
			if (!table.find(o).hasNext())
				throw new WebApplicationException(NOT_FOUND);

			mongoDB.requestStart();
			table.remove(o);
			mongoDB.requestDone();

			/*
			try (Producer<String, String> producer = new KafkaProducer<>(properties)) {
				producer.send(new ProducerRecord<>("unregister", id, null));
			} catch (Exception e){
				e.printStackTrace();
			} finally {
				producer.close();
			}
			*/
		} else {
			throw new WebApplicationException(FORBIDDEN);
		}
	}

	private void unregisterSpecial(String id) {
		System.err.printf("delete: %s \n", id);
		// TODO: para completar
		if(!db.containsKey(id))
			throw new WebApplicationException(NOT_FOUND);
		else
			db.remove(id);
	}

	/*
	@DELETE
	@Path("/document/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public boolean delete(@PathParam("id") String id, @QueryParam("secret") String secret){
        if(!this.secret.equals(secret))
            throw new WebApplicationException(FORBIDDEN);

		boolean[] result = {false};
		Thread[] threads = new Thread[db.size()];

		int t = 0;
		for (Endpoint e: db.values()) {
			System.out.println("removing "+id+" from "+e.getUrl());
			threads[t] = new Thread(()->{
				try {
					//TODO test!
					boolean executed = false;
					for (int i = 0; !executed && i < 3; i++) {
						try {
							if(e.getAttributes().get("type").equals("rest")) {
								ClientConfig config = new ClientConfig();
								Client client = ClientBuilder.newClient(config);

								client.property(ClientProperties.CONNECT_TIMEOUT, 5000);
								client.property(ClientProperties.READ_TIMEOUT, 5000);

								URI baseURI = UriBuilder.fromUri(e.getUrl()).build();

								WebTarget target = client.target( baseURI );
								synchronized (this) {
									result[0] |= target.path("/indexer/special/" + id + "?secret=" + secret).request().delete(Boolean.class);
								}
							} else {
								try {
									URL wsURL = new URL(e.getUrl() + "?wsdl");

									QName qname = new QName(NAMESPACE, NAME);

									Service service = Service.create(wsURL, qname);

									IndexerService contacts = service.getPort(IndexerService.class);

									synchronized (this) {
										result[0] |= contacts.special(id, secret);
									}
								} catch (Exception ex){
									ex.printStackTrace();
								}
								//SOAP
							}
						} catch (ProcessingException ex){
							System.out.println("Retrying delete");
							Thread.sleep(5000);
							continue;
						}
						executed = true;
					}
					if(!executed) {
						db.remove(id);
						Thread.currentThread().interrupt();
					}

				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			});
			threads[t++].start();
		}
		for (Thread thread: threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("result "+result[0]);
		return result[0];
	}
	*/

	static public class InsecureHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}
}



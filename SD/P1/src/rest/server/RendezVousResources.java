package rest.server;

import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import api.Endpoint;
import api.soap.IndexerAPI;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import static api.soap.IndexerAPI.*;
import static javax.ws.rs.core.Response.Status.*;

/**
 * Implementacao do servidor de rendezvous em REST 
 */
@Path("/contacts")
public class RendezVousResources {

	private Map<String, Endpoint> db = new ConcurrentHashMap<>();

	public RendezVousResources(){

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Endpoint[] endpoints() {
		return db.values().toArray( new Endpoint[ db.size() ]);
	}

	/*
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Endpoint endpoints( @PathParam("id") String id){
		
		if(db.containsKey(id))
			return db.get(id);
		else
			throw new WebApplicationException(NOT_FOUND);
	}
	*/

	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void register( @PathParam("id") String id, Endpoint endpoint) {
		System.err.printf("register: %s <%s>\n", id, endpoint);
		
		if (db.containsKey(id))
			throw new WebApplicationException( CONFLICT );
		else {
			new Thread(()->{
				for(;;){
					try {
						Thread.sleep(1000);
						//TODO test!
						boolean executed = false;
						for (int i = 0; !executed && i < 3; i++) {
							try {
								if(endpoint.getAttributes().get("type").equals("rest")) {
									ClientConfig config = new ClientConfig();
									Client client = ClientBuilder.newClient(config);

									client.property(ClientProperties.CONNECT_TIMEOUT, 5000);
									client.property(ClientProperties.READ_TIMEOUT, 5000);

									URI baseURI = UriBuilder.fromUri(endpoint.getUrl()).build();

									WebTarget target = client.target( baseURI );
									target.path("/indexer/ping").request().accept(MediaType.APPLICATION_JSON).get(String.class);
								} else {
									//SOAP
									URL wsURL = new URL(endpoint.getUrl() + "?wsdl");

									QName qname = new QName(NAMESPACE, NAME);

									Service service = Service.create(wsURL, qname);

									IndexerAPI contacts = service.getPort(IndexerAPI.class);

									contacts.ping();
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
							db.remove(id);
							Thread.currentThread().interrupt();
						}


					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}).start();
			db.put(id, endpoint);
		}
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void update(@PathParam("id") String id, Endpoint endpoint) {
		System.err.printf("update: %s <%s>\n", id, endpoint);
		
		if ( ! db.containsKey(id))
			throw new WebApplicationException( NOT_FOUND );
		else
			db.put(id, endpoint);
	}

	@DELETE
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void unregister(@PathParam("id") String id) {
		System.err.printf("delete: %s \n", id);
		// TODO: para completar
		if(!db.containsKey(id))
			throw new WebApplicationException(NOT_FOUND);
		else
			db.remove(id);
	}

	@DELETE
	@Path("/document/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public boolean delete(@PathParam("id") String id){
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
									result[0] |= target.path("/indexer/special/" + id).request().delete(Boolean.class);
								}
							} else {
								try {
									URL wsURL = new URL(e.getUrl() + "?wsdl");

									QName qname = new QName(NAMESPACE, NAME);

									Service service = Service.create(wsURL, qname);

									IndexerAPI contacts = service.getPort(IndexerAPI.class);

									synchronized (this) {
										result[0] |= contacts.special(id);
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
}

package rest;

import api.Endpoint;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Collections;

/**
 * Created by goncalo on 07-03-2017.
 */
public class IndexerServer {
	public static void main(String[] args) {
		int port = 8080;
		String url = "";
		if( args.length == 0) {
			System.out.println("Usage: java <name> <RendezVousURL> [<port>]");
			return;
		} else if(args.length == 1){
			url = args[0];
		}

		if(args.length > 1)
			port = Integer.parseInt(args[1]);

		URI baseUri = UriBuilder.fromUri("http://0.0.0.0/").port(port).build();

		ResourceConfig config = new ResourceConfig();
		config.register( new PrelimIndexerService() );

		JdkHttpServerFactory.createHttpServer(baseUri, config);

		ClientConfig clientConfig = new ClientConfig();
		Client client = ClientBuilder.newClient(clientConfig);

		URI baseURI = UriBuilder.fromUri("http://localhost:8080/").build();

		WebTarget target = client.target( baseURI );

		Endpoint endpoint = new Endpoint(baseUri.toASCIIString(), Collections.emptyMap());

		Response response = target.path("/contacts/" + endpoint.generateId())
				.request()
				.post( Entity.entity( endpoint, MediaType.APPLICATION_JSON));

	}
}

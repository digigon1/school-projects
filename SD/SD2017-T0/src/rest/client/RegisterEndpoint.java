package rest.client;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;

import api.Endpoint;

public class RegisterEndpoint {

	public static void main(String[] args) throws IOException {
	    ClientConfig config = new ClientConfig();
	    Client client = ClientBuilder.newClient(config);

	    URI baseURI = UriBuilder.fromUri("http://localhost:8080/").build();
	    
	    WebTarget target = client.target( baseURI );

	    Endpoint endpoint = new Endpoint("http://some-server-endpoint-url", Collections.emptyMap());
	    
	    Response response = target.path("/contacts/" + endpoint.generateId())
	    					.request()
	    					.post( Entity.entity( endpoint, MediaType.APPLICATION_JSON));

	    System.out.println(response.getStatus() );
	    
	}
}

package rest.client;

import api.Endpoint;
import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

/**
 * Created by goncalo on 07-03-2017.
 */
public class DeleteEndpoint {
	public static void main(String[] args) throws IOException {
		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);

		URI baseURI = UriBuilder.fromUri("http://localhost:8080/").build();

		WebTarget target = client.target( baseURI );

		String id = "B24ACF03C702953CEC75015D0A970CCE";

		Response response = target.path("/contacts/" + id)
				.request()
				.delete();
				//.post( Entity.entity( endpoint, MediaType.APPLICATION_JSON));

		System.out.println(response.getStatus());

	}
}

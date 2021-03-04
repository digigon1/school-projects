package rest.client;

import api.Endpoint;
import api.ServerConfig;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import rest.server.RendezVousResources;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by goncalo on 07-03-2017.
 */
public class SearchThirdParty {
	public static void main(String[] args) throws IOException {

		Client client = ClientBuilder.newBuilder()
				.hostnameVerifier(new InsecureHostnameVerifier())
				.build();
		URI baseURI = UriBuilder.fromUri("https://127.0.1.1:40309/").build();
		WebTarget target = client.target( baseURI );

		/*
		target.path("/indexer/configure")
				.queryParam("secret", "secret")
				.request()
				.put(Entity.entity(new ServerConfig("vkKDo4tmffKMT4O14FBFvzzmm", "2zCCeHdlmmFleuoI4PB9rwe1iPIB1zFrqHtyq6tBcfAsNv7ivF", "EqexkwAAAAAA0wcAAAABXDXDLsk", "HNsM4pgSA7VBrhVHi25yNMcMzJUNcSqS"), MediaType.APPLICATION_JSON));
                //.put(Entity.entity(new ServerConfig("vkKDo4tmffKMT4O14FBFvzzmm", "2zCCeHdlmmFleuoI4PB9rwe1iPIB1zFrqHtyq6tBcfAsNv7ivF", null, null), MediaType.APPLICATION_JSON));
        */

        System.out.println("Search terms:");
        List<String> response = Arrays.asList(target.path("/indexer/search")
				.queryParam("query", new Scanner(System.in).next())
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get(String[].class));

		System.out.println(Arrays.toString(response.toArray()));
	}

	static public class InsecureHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}
}

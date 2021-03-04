package rest.client;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import api.ServerConfig;
import org.glassfish.jersey.client.ClientConfig;

import api.Endpoint;

public class ConfigureThirdParty {

	public static void main(String[] args) throws IOException {
        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new SearchThirdParty.InsecureHostnameVerifier())
                .build();
        URI baseURI = UriBuilder.fromUri("https://127.0.1.1:40309/").build();
        WebTarget target = client.target( baseURI );


		target.path("/indexer/configure")
				.queryParam("secret", "secret")
				.request()
				.put(Entity.entity(new ServerConfig("vkKDo4tmffKMT4O14FBFvzzmm", "2zCCeHdlmmFleuoI4PB9rwe1iPIB1zFrqHtyq6tBcfAsNv7ivF", "142088125-oPF1dItl1Fvn7MlJbT3EPvkl9TA6z27gpUqVePFH", "oJmPQBxsMHik8I4TJzIK6pvJ9BlmoOlhe6UWtOJn2khCv"), MediaType.APPLICATION_JSON));
                //.put(Entity.entity(new ServerConfig("vkKDo4tmffKMT4O14FBFvzzmm", "2zCCeHdlmmFleuoI4PB9rwe1iPIB1zFrqHtyq6tBcfAsNv7ivF", null, null), MediaType.APPLICATION_JSON));

	}
	
	static public class InsecureHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}
}

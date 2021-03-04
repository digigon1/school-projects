package rest.indexing.soap;

import api.Endpoint;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import org.glassfish.jersey.client.ClientConfig;
import rest.indexing.rest.IndexerServer;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by goncalo on 11-04-2017.
 */
public class SOAPIndexerServer {
	public static void main(String[] args) throws Exception {
		String secret;
		if(args.length > 0)
			secret = args[0];
		else {
			System.err.println("SECRET WAS NOT SEND");
			System.exit(1);
			return;
		}

		Random r = new Random();
		int port = 30000 + r.nextInt(30000);

		URI baseUri = UriBuilder.fromUri("https://" + InetAddress.getLocalHost().getHostAddress()).path("/").port(port).build();
		//javax.xml.ws.Endpoint.publish(baseUri.toString(), new SOAPIndexer(secret));


		HttpsConfigurator configurator = new HttpsConfigurator(SSLContext.getDefault());
		HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), port), -1);
		httpsServer.setHttpsConfigurator(configurator);
		HttpContext httpContext = httpsServer.createContext("/indexer");
		httpsServer.start();

		javax.xml.ws.Endpoint ep = javax.xml.ws.Endpoint.create(new SOAPIndexer(secret));
		ep.publish(httpContext);


		String uri;
		if (args.length > 1) {
			uri = args[1];
		} else {

			//send multicast request
			//use request to register
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
		}

		//register indexer
		Client client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier()).build();


		String epString = baseUri.toString();
		epString = epString.substring(0, epString.length() - 1);
		Map<String, Object> attr = new HashMap<>();
		attr.put("type", "soap");
		Endpoint endpoint = new Endpoint(epString, attr);

		URI baseURI = UriBuilder.fromUri(uri).build();
		WebTarget target = client.target(baseURI);
		Response response = target.path(endpoint.generateId())
				.queryParam("secret", secret)
				.request()
				.post(Entity.entity(endpoint, MediaType.APPLICATION_JSON));

		System.out.println(response.getStatus());

		System.err.println("SOAP Indexer Server ready @ " + baseUri);
	}

	static public class InsecureHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}
}

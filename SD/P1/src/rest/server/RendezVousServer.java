package rest.server;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URI;
import java.util.Arrays;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class RendezVousServer {

	public static void main(String[] args) throws Exception {
		int port = 8080;
		if( args.length > 0)
			port = Integer.parseInt(args[0]);

		URI baseUri = UriBuilder.fromUri("http://0.0.0.0/").port(port).build();

		ResourceConfig config = new ResourceConfig();
		config.register( new RendezVousResources() );

		JdkHttpServerFactory.createHttpServer(baseUri, config);

		System.err.println("REST RendezVous Server ready @ " + baseUri);


		//MULICAST
		String multicastAddress = "230.229.213.230";
		final InetAddress group = InetAddress.getByName(multicastAddress);
		if(!group.isMulticastAddress()) {
			System.out.println("Use range : 224.0.0.0 -- 239.255.255.255");
			System.exit(1);
		}

		MulticastSocket socket = new MulticastSocket(9999);
		socket.joinGroup(group);

		URI uri = UriBuilder.fromUri("http://"+InetAddress.getLocalHost().getHostAddress()).path("/contacts").port(port).build();
		String url = uri.toString();
		byte[] reply = url.getBytes();

		while(true) {
			// WAIT for a discovery request
			byte[] request = new byte[65536];
			DatagramPacket requestPacket = new DatagramPacket( request, request.length );
			socket.receive( requestPacket );
			System.out.write( requestPacket.getData(), 0, requestPacket.getLength() );
			System.out.println();
			// REPLY with the URL of the service
            if(new String(requestPacket.getData(), 0, requestPacket.getLength()).equals("rendezvous")) {
                int clientPort = requestPacket.getPort();

                DatagramPacket replyPacket = new DatagramPacket(reply, reply.length);
                replyPacket.setAddress(requestPacket.getAddress());
				System.out.println(clientPort);
				replyPacket.setPort(clientPort);
                socket.send(replyPacket);
			}
		}
	}
}

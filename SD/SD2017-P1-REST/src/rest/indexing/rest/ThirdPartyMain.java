package rest.indexing.rest;

import api.Endpoint;
import api.ServerConfig;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

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
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Goncalo on 20/05/2017.
 */
public class ThirdPartyMain {
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
        int port = 30000+r.nextInt(30000);

        URI baseUri = UriBuilder.fromUri("https://"+ InetAddress.getLocalHost().getHostAddress()).path("/").port(port).build();

        ResourceConfig config = new ResourceConfig();
        config.register(new ThirdPartyProxy(secret));

        JdkHttpServerFactory.createHttpServer(baseUri, config, SSLContext.getDefault());


        String uri;
        if(args.length > 1) {
            uri = args[1];
        } else {

            //TODO
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

        String ep = baseUri.toString();
        ep = ep.substring(0, ep.length()-1);
        Map<String, Object> attr = new HashMap<>();
        attr.put("type", "rest");
        Endpoint endpoint = new Endpoint(ep, attr);

        URI baseURI = UriBuilder.fromUri(uri).build();
        WebTarget target = client.target(baseURI);
        Response response = target.path(endpoint.generateId())
                .queryParam("secret", secret)
                .request()
                .post(Entity.entity(endpoint, MediaType.APPLICATION_JSON));

        System.out.println(response.getStatus() );

        System.err.println("Third-Party REST Indexer Server ready @ " + baseUri);
    }

    static public class InsecureHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}

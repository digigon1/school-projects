package rest.indexing.rest;

import api.Document;
import api.Endpoint;
import api.rest.IndexerService;
import com.google.common.collect.Lists;
import org.glassfish.jersey.client.ClientConfig;
import sys.storage.LocalVolatileStorage;
import sys.storage.Storage;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * Created by goncalo on 14-03-2017.
 */
public class IndexerServiceImplementation implements IndexerService {

	private String uri;
	private Storage s;

	IndexerServiceImplementation(){
		this.s = new LocalVolatileStorage();
		this.uri = "";
		try{
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
			socket.leaveGroup(address);
		} catch (IOException e){
			System.out.println("Socket fail");
		}
	}

	@Override
	public List<String> search(String keywords) {
		System.out.println("search");
		List<String> words = Arrays.asList(keywords.split("\\+"));

		List<Document> docs = s.search(words);

		return docs.stream().map(Document::getUrl).collect(Collectors.toList());
	}

	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void add(@PathParam("id") String id, Document doc) {
		//TODO just this? check
		System.out.println("add");
		if(!s.store(id, doc)){
			throw new WebApplicationException( CONFLICT );
		}
	}

	@Override
	public void remove(String id) {
		System.out.println("remove "+id);
		//TODO just this? check
		//Send request to rendezvousserver to send removeSpecial(id) to all

		WebTarget target = ClientBuilder.newClient(new ClientConfig()).target(UriBuilder.fromUri(uri).build());
		//Obtém os servidores registados sob a forma de um array

		if(!target.path("/document/" + id).request().delete(Boolean.class)){
			System.out.println("remove failed");
			throw new WebApplicationException(NOT_FOUND);
		}
		/*
		if(!s.remove(id)) {
			System.out.println("remove failed");
			throw new WebApplicationException(NOT_FOUND);
		}
		*/

	}

	@DELETE
	@Path("/special/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public boolean removeSpecial(@PathParam("id") String id){
		System.out.println("special "+id);
		/*
		if(!s.remove(id)){
			System.out.println("special failed");
			throw new WebApplicationException(NOT_FOUND);
		}
		*/

		return s.remove(id);
	}

	@GET
    @Path("/ping")
    @Produces(MediaType.APPLICATION_JSON)
    public String ping(){
		return "pong";
    }
}

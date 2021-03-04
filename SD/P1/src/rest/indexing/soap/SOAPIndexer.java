package rest.indexing.soap;

import api.Document;
import api.soap.IndexerAPI;
import org.glassfish.jersey.client.ClientConfig;
import sys.storage.LocalVolatileStorage;
import sys.storage.Storage;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by goncalo on 11-04-2017.
 */
@WebService(
		serviceName = IndexerAPI.NAME,
		targetNamespace = IndexerAPI.NAMESPACE,
		endpointInterface = IndexerAPI.INTERFACE)
public class SOAPIndexer implements IndexerAPI {

	private String uri;
	private Storage s;

	public SOAPIndexer(){
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
	public List<String> search(String keywords) throws InvalidArgumentException {
		if(keywords == null)
			throw new InvalidArgumentException("keywords can't be null");

		return s.search(Arrays.asList(keywords.split("\\+"))).stream().map(Document::getUrl).collect(Collectors.toList());
	}

	@Override
	public boolean add(Document doc) throws InvalidArgumentException {
		if(doc == null)
			throw new InvalidArgumentException("doc can't be null");

		return s.store(doc.id(), doc);
	}

	@Override
	public boolean remove(String id) throws InvalidArgumentException {
		if(id == null)
			throw new InvalidArgumentException("id can't be null");

		/*
		WebTarget target = ClientBuilder.newClient(new ClientConfig()).target(UriBuilder.fromUri(uri).build());
		target.path("/document/"+id).request().delete();

		return s.remove(id);
		*/

		System.out.println("Sending request for "+id);
		WebTarget target = ClientBuilder.newClient(new ClientConfig()).target(UriBuilder.fromUri(uri).build());
		return target.path("/document/" + id).request().delete(Boolean.class);
	}

	@WebMethod()
	public boolean special(String id) throws InvalidArgumentException{
		System.out.println("special for "+id);
		if(id == null)
			throw new InvalidArgumentException("id can't be null");

		return s.remove(id);
	}

	@WebMethod()
	public String ping(){
		return "pong";
	}
}

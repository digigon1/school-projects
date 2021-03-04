package rest;

import api.Document;
import api.IndexerService;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by goncalo on 07-03-2017.
 */
public class PrelimIndexerService implements IndexerService {

	private Map<String, Document> db = new ConcurrentHashMap<>();

	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void add(@PathParam("id") String documentId, Document doc) {
		if(db.containsKey(documentId))
			throw new WebApplicationException(Response.Status.CONFLICT);
		else
			db.put(documentId, doc);

	}


	@DELETE
	@Path("/{id}")
	public void remove(@PathParam("id") String documentId) {
		if(!db.containsKey(documentId))
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		else
			db.remove(documentId);
	}
}

package api.rest;

import api.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by goncalo on 14-03-2017.
 */
@Path("/indexer")
public interface IndexerService {

	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	List<String> search(@QueryParam("query") String keywords );

	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	void add( @PathParam("id") String id, Document doc );

	@DELETE
	@Path("/{id}")
	void remove( @PathParam("id") String id );
}
package api.rest;

import api.Document;
import api.ServerConfig;

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
    List<String> search(@QueryParam("query") String keywords);

    /**
     * Configure server for accessing third-party service. Servers that do not act as a proxy should do nothing and return 200.
     *
     * secret: for protecting access to this function passed as a query parameter
     * config: configuration for remote access
     * Should return HTTP code 403 on security issue.
     */
    @PUT
    @Path("/configure")
    @Consumes(MediaType.APPLICATION_JSON)
    void configure( @QueryParam("secret") String secret, ServerConfig config);

    /**
     * Secret for protecting access to this function passed as a query parameter
     * Should return HTTP code 403 on security issue.
     */
    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    void add( @PathParam("id") String id, @QueryParam("secret") String secret, Document doc );

    /**
     * Secret for protecting access to this function passed as a query parameter
     * Should return HTTP code 403 on security issue.
     */
    @DELETE
    @Path("/{id}")
    void remove( @PathParam("id") String id, @QueryParam("secret") String secret );

    @GET
    @Path("/ping")
    @Produces(MediaType.APPLICATION_JSON)
    String ping();
}
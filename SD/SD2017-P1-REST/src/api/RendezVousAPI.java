package api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Interface do servidor que mantem lista de servidores.
 */
public interface RendezVousAPI {

    /**
     * Devolve array com a lista de servidores registados.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Endpoint[] endpoints();

    /**
     * Regista novo servidor.
     */
    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void register( @PathParam("id") String id, @QueryParam("secret") String secret, Endpoint endpoint);

    /**
     * De-regista servidor, dado o seu id.
     */
    @DELETE
    @Path("/{id}")
    public void unregister(@PathParam("id") String id, @QueryParam("secret") String secret);
}
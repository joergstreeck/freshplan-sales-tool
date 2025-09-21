package de.freshplan.performance.resources;

import de.freshplan.performance.http.EtagSupport;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
public class CustomerResource {

  @Inject EtagSupport etag;

  @GET
  public Response list(@HeaderParam("If-None-Match") String inm) {
    // Hot-Projection payload (IDs, name, status, updatedAt)
    String payload = "[/* json from projection */]";
    String e = EtagSupport.sha256Hex(payload); // or use projection version hash
    return EtagSupport.conditional(e, inm, () -> Response.ok(payload).build());
  }
}

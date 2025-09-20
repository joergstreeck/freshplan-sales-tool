package de.freshplan.admin.user;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;

@Path("/api/admin/users")
@Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
public class AdminUserResource {

  @Inject AdminUserService svc;

  @GET @RolesAllowed({"admin","security"})
  public Response list(@QueryParam("q") String q,
                       @QueryParam("orgId") String orgId,
                       @QueryParam("territory") String territory,
                       @QueryParam("limit") @DefaultValue("50") int limit){
    var out = svc.list(q, orgId==null?null:java.util.UUID.fromString(orgId), territory, limit);
    return Response.ok(java.util.Map.of("items", out)).build();
  }

  public static class Create { public String email; public String name; public java.util.List<String> roles; }
  @POST @RolesAllowed({"admin","security"})
  public Response create(Create body, @Context UriInfo uri){
    var u = svc.createUser(body.email, body.name, body.roles==null?java.util.List.of():body.roles);
    return Response.created(uri.getAbsolutePathBuilder().path(u.get("id").toString()).build()).entity(u).build();
  }

  public static class Patch { public String name; public java.util.List<String> roles; public Boolean disabled; }
  @PATCH @Path("{id}") @RolesAllowed({"admin","security"})
  public Response patch(@PathParam("id") String id, Patch body){
    var u = svc.patchUser(java.util.UUID.fromString(id), body.name, body.roles, body.disabled);
    return Response.ok(u).build();
  }

  public static class Claims { public String orgId; public java.util.List<String> territories; public java.util.List<String> channels; public java.util.List<String> scopes; }
  @PUT @Path("{id}/claims") @RolesAllowed({"admin","security"})
  public Response claims(@PathParam("id") String id, Claims body){
    svc.replaceClaims(java.util.UUID.fromString(id),
      body.orgId==null?null:java.util.UUID.fromString(body.orgId), body.territories, body.channels, body.scopes);
    return Response.status(Response.Status.NO_CONTENT).build();
  }

  @POST @Path("{id}/sync") @RolesAllowed({"admin","security"})
  public Response sync(@PathParam("id") String id){
    // sync job already enqueued in claims; this endpoint can enqueue explicitly
    return Response.accepted().build();
  }
}

package de.freshplan.admin.policy;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.UUID;

@Path("/api/admin/security")
@Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
public class AdminPolicyResource {

  @Inject AdminPolicyService svc;

  public static class PolicyChange { public String key; public Object value; public String riskTier; public String justification; public boolean emergency; }

  @POST @Path("/policies") @RolesAllowed({"admin","security"})
  public Response change(PolicyChange body){
    String json = body.value==null? "null" : new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(body.value).toString();
    svc.requestChange(body.key, json, body.riskTier==null?"TIER3":body.riskTier, body.justification, body.emergency);
    return Response.accepted().build();
  }

  @POST @Path("/approvals/{id}/approve") @RolesAllowed({"admin","security"})
  public Response approve(@PathParam("id") String id){ svc.approve(java.util.UUID.fromString(id)); return Response.status(Response.Status.NO_CONTENT).build(); }

  @POST @Path("/approvals/{id}/reject") @RolesAllowed({"admin","security"})
  public Response reject(@PathParam("id") String id){ svc.reject(java.util.UUID.fromString(id)); return Response.status(Response.Status.NO_CONTENT).build(); }
}

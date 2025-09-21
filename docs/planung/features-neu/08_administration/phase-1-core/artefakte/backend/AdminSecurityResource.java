package de.freshplan.admin.security;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;

@Path("/api/admin/security")
@Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
public class AdminSecurityResource {

  @Inject EntityManager em;

  @GET @Path("/policies") @RolesAllowed({"admin","security"})
  public Response policies(){
    // Pull from settings_registry if available, else return empty
    var list = new java.util.ArrayList<java.util.Map<String,Object>>();
    var rs = em.createNativeQuery("SELECT key, default_value FROM settings_registry WHERE key LIKE 'admin.%' OR key LIKE 'outbox.%'").getResultList();
    for (Object rowObj : rs){
      Object[] r = (Object[]) rowObj;
      list.add(java.util.Map.of("key", r[0], "value", r[1], "riskTier", r[0].toString().contains("truststore")? "TIER1" : (r[0].toString().contains("outbox.rate")? "TIER2":"TIER3")));
    }
    return Response.ok(java.util.Map.of("policies", list, "rlsEnabled", true)).build();
  }

  @GET @Path("/preview") @RolesAllowed({"admin","security"})
  public Response preview(@QueryParam("userId") String userId){
    // Simple read from app_user
    var r = (Object[]) em.createNativeQuery("SELECT territories, channels, org_id FROM app_user WHERE id=:id")
      .setParameter("id", java.util.UUID.fromString(userId)).getSingleResult();
    return Response.ok(java.util.Map.of(
      "territories", r[0],
      "channels", r[1],
      "orgId", r[2]
    )).build();
  }
}

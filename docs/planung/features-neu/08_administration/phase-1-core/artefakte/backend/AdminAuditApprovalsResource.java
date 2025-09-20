package de.freshplan.admin.audit;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;

import de.freshplan.admin.policy.AdminPolicyService;

@Path("/api/admin/audit/approvals")
@Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
public class AdminAuditApprovalsResource {

  @Inject EntityManager em;
  @Inject AdminPolicyService svc;

  @GET @RolesAllowed({"admin","security"})
  public Response list(@QueryParam("status") @DefaultValue("PENDING") String status){
    var rows = em.createNativeQuery("SELECT id, risk_tier::text, action, resource_type, resource_id, status, time_delay_until FROM admin_approval_request WHERE status=:st ORDER BY created_at DESC")
      .setParameter("st", status).getResultList();
    var items = new java.util.ArrayList<java.util.Map<String,Object>>();
    for (Object rowObj : rows){
      Object[] r = (Object[]) rowObj;
      items.add(java.util.Map.of(
        "id", r[0], "riskTier", r[1], "action", r[2], "resourceType", r[3], "resourceId", r[4], "status", r[5], "timeDelayUntil", r[6]
      ));
    }
    return Response.ok(java.util.Map.of("items", items)).build();
  }

  @POST @Path("/{id}/approve") @RolesAllowed({"admin","security"})
  public Response approve(@PathParam("id") String id){ svc.approve(java.util.UUID.fromString(id)); return Response.status(Response.Status.NO_CONTENT).build(); }

  @POST @Path("/{id}/reject") @RolesAllowed({"admin","security"})
  public Response reject(@PathParam("id") String id){ svc.reject(java.util.UUID.fromString(id)); return Response.status(Response.Status.NO_CONTENT).build(); }
}

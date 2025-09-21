package de.freshplan.admin.audit;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.time.OffsetDateTime;

@Path("/api/admin/audit")
@Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
public class AdminAuditResource {

  @Inject AdminAuditService svc;

  @GET @Path("/events") @RolesAllowed({"admin","security"})
  public Response events(@QueryParam("q") String q,
                         @QueryParam("resourceType") String rt,
                         @QueryParam("from") String from,
                         @QueryParam("to") String to,
                         @QueryParam("limit") @DefaultValue("50") int limit){
    var list = svc.search(q, rt,
      from==null?null:OffsetDateTime.parse(from),
      to==null?null:OffsetDateTime.parse(to),
      limit);
    return Response.ok(java.util.Map.of("items", list)).build();
  }

  @GET @Path("/export") @Produces("application/jsonlines") @RolesAllowed({"admin","security","auditor"})
  public Response export(@QueryParam("from") String from, @QueryParam("to") String to){
    // For brevity, reuse events search and join with \\n – production should stream via cursor
    var list = svc.search(null, null, from==null?null:OffsetDateTime.parse(from), to==null?null:OffsetDateTime.parse(to), 1000);
    StringBuilder sb = new StringBuilder();
    for (var m : list){ sb.append(new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(m).toString()).append("\\n"); }
    return Response.ok(sb.toString()).build();
  }
}

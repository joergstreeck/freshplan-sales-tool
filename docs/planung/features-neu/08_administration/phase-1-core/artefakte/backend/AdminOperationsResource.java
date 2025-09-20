package de.freshplan.admin.ops;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.Map;
import java.util.UUID;

@Path("/api/admin/ops")
@Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
public class AdminOperationsResource {

  @Inject AdminOperationsService svc;

  @GET @Path("/smtp") @RolesAllowed({"admin","ops","security"})
  public Response getSmtp(){ return Response.ok(svc.getSmtp()).build(); }

  public static class Smtp { public String host; public int port; public String username; public String passwordRef; public String tlsMode; public String truststoreRef; }
  @PUT @Path("/smtp") @RolesAllowed({"admin","security"})
  public Response putSmtp(Smtp b){ svc.updateSmtp(b.host,b.port,b.username,b.passwordRef,b.tlsMode,b.truststoreRef); return Response.accepted().build(); }

  @GET @Path("/outbox") @RolesAllowed({"admin","ops","security"})
  public Response outbox(){ return Response.ok(svc.getOutbox()).build(); }

  @POST @Path("/outbox/pause") @RolesAllowed({"admin","ops"})
  public Response pause(){ svc.pauseOutbox(); return Response.status(Response.Status.NO_CONTENT).build(); }

  @POST @Path("/outbox/resume") @RolesAllowed({"admin","ops"})
  public Response resume(){ svc.resumeOutbox(); return Response.status(Response.Status.NO_CONTENT).build(); }

  public static class Rate { public int ratePerMin; }
  @POST @Path("/outbox/rate") @RolesAllowed({"admin","security"})
  public Response rate(Rate r){ svc.setRate(r.ratePerMin); return Response.accepted().build(); }

  public static class Dsar { public String subjectId; public String justification; }
  @POST @Path("/dsar/export") @RolesAllowed({"admin","security"})
  public Response dsarExport(Dsar b){ svc.queueDsar("EXPORT", java.util.UUID.fromString(b.subjectId), b.justification); return Response.accepted().build(); }

  @POST @Path("/dsar/delete") @RolesAllowed({"admin","security"})
  public Response dsarDelete(Dsar b){ svc.queueDsar("DELETE", java.util.UUID.fromString(b.subjectId), b.justification); return Response.accepted().build(); }
}

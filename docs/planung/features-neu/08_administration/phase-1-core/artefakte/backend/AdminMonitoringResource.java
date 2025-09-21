package de.freshplan.admin.monitoring;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import io.micrometer.core.instrument.MeterRegistry;

@Path("/api/admin/monitoring")
@Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
public class AdminMonitoringResource {

  @Inject MeterRegistry meter;

  @GET @Path("/overview") @RolesAllowed({"admin","ops","security","manager"})
  public Response overview(){
    double apiP95 = meter.find("http.server.requests").timer() == null ? 0 : meter.find("http.server.requests").timer().takeSnapshot().percentileValues()[3].value();
    double abacDeny = meter.counter("abac_denied_total").count();
    double audit = meter.counter("admin_audit_total").count();
    // OutboxLag/ErrorRate would be sourced from custom meters; simplified snapshot here
    return Response.ok(java.util.Map.of("apiP95Ms", apiP95*1000, "outboxLag", 0, "errorRate", 0, "abacDenyRate", abacDeny, "auditEventsLastHour", (int)audit)).build();
  }

  @GET @Path("/health") @RolesAllowed({"admin","ops","security","manager"})
  public Response health(){ return Response.ok(java.util.Map.of("status","UP")).build(); }
}

package com.freshplan.reports;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.freshplan.cockpit.SalesCockpitService;
import com.freshplan.costs.CostStatistics;
import java.util.Map;

@Path("/api/reports")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class ReportsResource {

  @Inject SalesCockpitService sales;
  @Inject CostStatistics costs;

  @GET
  @Path("/sales-summary")
  @RolesAllowed({"user","manager","admin"})
  public Response salesSummary(@QueryParam("range") @DefaultValue("30d") String range) {
    // Delegation an SalesCockpitService (bestehende Logik)
    var dash = sales.calculateStatistics(range); // Annahme: Overload vorhanden / Adapter bauen
    var body = Map.of(
      "sampleSuccessRate", dash.getSampleSuccessRate(),
      "roiPipeline", dash.getRoiPipeline(),
      "partnerSharePct", dash.getPartnerSharePct(),
      "atRiskCustomers", dash.getCustomersAtRisk()
    );
    return Response.ok(body).build();
  }

  @GET
  @Path("/customer-analytics")
  @RolesAllowed({"user","manager","admin"})
  public Response customerAnalytics(@QueryParam("segment") String segment,
                                    @QueryParam("territory") String territory,
                                    @QueryParam("seasonFrom") String seasonFrom,
                                    @QueryParam("seasonTo") String seasonTo,
                                    @QueryParam("renewalBefore") String renewalBefore,
                                    @QueryParam("limit") @DefaultValue("100") int limit,
                                    @QueryParam("cursor") String cursor) {
    // Thin wrapper: Query an Service/Repository (oder Views), Paging via cursor
    var result = ReportsQuery.fetchCustomerAnalytics(segment, territory, seasonFrom, seasonTo, renewalBefore, limit, cursor);
    return Response.ok(result).build();
  }

  @GET
  @Path("/activity-stats")
  @RolesAllowed({"user","manager","admin"})
  public Response activityStats(@QueryParam("kind") String kind,
                                @QueryParam("from") String from,
                                @QueryParam("to") String to) {
    var result = ReportsQuery.fetchActivityStats(kind, from, to);
    return Response.ok(result).build();
  }

  @GET
  @Path("/cost-overview")
  @RolesAllowed({"manager","admin"})
  public Response costOverview(@QueryParam("range") @DefaultValue("month") String range) {
    var summary = ReportsQuery.fetchCostOverview(costs, range); // nutzt headless CostStatistics
    return Response.ok(summary).build();
  }

  @GET
  @Path("/export")
  @RolesAllowed({"user","manager","admin"})
  @Produces({MediaType.APPLICATION_OCTET_STREAM, "application/x-ndjson", MediaType.APPLICATION_JSON})
  public Response export(@QueryParam("type") String type,
                         @QueryParam("format") String format,
                         @QueryParam("range") String range,
                         @QueryParam("segment") String segment,
                         @QueryParam("territory") String territory) {
    // Re-use UniversalExportService (bereits implementiert)
    var stream = UniversalExportAdapter.export(type, format, range, segment, territory);
    return Response.ok(stream).header("Content-Disposition", "attachment; filename=report." + format).build();
  }
}

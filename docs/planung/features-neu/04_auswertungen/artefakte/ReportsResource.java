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

/**
 * Reports REST API Controller - Thin wrapper for existing Analytics Services
 *
 * @see ../../grundlagen/API_STANDARDS.md - Jakarta EE REST API Standards
 * @see ../../grundlagen/SECURITY_GUIDELINES.md - RBAC Security Implementation
 * @see ../../grundlagen/PERFORMANCE_STANDARDS.md - P95 <200ms SLO Requirements
 *
 * This controller provides a unified API layer for Reports & Analytics
 * functionality, wrapping existing SalesCockpitService and CostStatistics
 * without duplicating business logic. All endpoints enforce RBAC security
 * and follow OpenAPI 3.1 specifications.
 *
 * @author Backend Team
 * @version 1.1
 * @since 2025-09-19
 */
@Path("/api/reports")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class ReportsResource {

  /** Existing Sales Cockpit Service - 559 LOC production-ready analytics */
  @Inject SalesCockpitService sales;

  /** Existing Cost Statistics Service - performance-optimized aggregations */
  @Inject CostStatistics costs;

  /**
   * Get Sales Summary with B2B-Food-specific KPIs
   *
   * @param range Time range filter (7d, 30d, 90d) - defaults to 30d
   * @return JSON response with Sample-Success-Rate, ROI-Pipeline,
   *         Partner-Share and At-Risk-Customers
   * @see ../../grundlagen/API_STANDARDS.md - Response Format Standards
   */
  @GET
  @Path("/sales-summary")
  @RolesAllowed({"user","manager","admin"})
  public Response salesSummary(@QueryParam("range") @DefaultValue("30d") String range) {
    // Delegation to existing SalesCockpitService (no logic duplication)
    var dashboard = sales.calculateStatistics(range);
    var body = Map.of(
      "sampleSuccessRate", dashboard.getSampleSuccessRate(),
      "roiPipeline", dashboard.getRoiPipeline(),
      "partnerSharePct", dashboard.getPartnerSharePct(),
      "atRiskCustomers", dashboard.getCustomersAtRisk()
    );
    return Response.ok(body).build();
  }

  /**
   * Get Customer Analytics with Territory and Seasonal Filtering
   *
   * @param segment Customer segment filter (gastronomiebetriebe, direktkunden)
   * @param territory Geographic territory filter (ABAC-secured)
   * @param seasonFrom Seasonal analysis start date (YYYY-MM-DD)
   * @param seasonTo Seasonal analysis end date (YYYY-MM-DD)
   * @param renewalBefore Filter customers with renewals before date
   * @param limit Maximum records to return (default: 100, max: 1000)
   * @param cursor Pagination cursor for large result sets
   * @return JSON response with customer analytics and pagination metadata
   * @see ../../grundlagen/SECURITY_GUIDELINES.md - ABAC Territory Scoping
   */
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
    var result = ReportsQuery.fetchCustomerAnalytics(segment, territory,
        seasonFrom, seasonTo, renewalBefore, limit, cursor);
    return Response.ok(result).build();
  }

  /**
   * Get Activity Statistics with Time-Range Filtering
   *
   * @param kind Activity type filter (calls, meetings, samples, follow_ups)
   * @param from Start date for statistics (YYYY-MM-DD)
   * @param to End date for statistics (YYYY-MM-DD)
   * @return JSON response with activity counts and success rates
   * @see ../../grundlagen/PERFORMANCE_STANDARDS.md - Query Optimization
   */
  @GET
  @Path("/activity-stats")
  @RolesAllowed({"user","manager","admin"})
  public Response activityStats(@QueryParam("kind") String kind,
                                @QueryParam("from") String from,
                                @QueryParam("to") String to) {
    var result = ReportsQuery.fetchActivityStats(kind, from, to);
    return Response.ok(result).build();
  }

  /**
   * Get Cost Overview - Manager/Admin only access
   *
   * @param range Time range for cost analysis (week, month, quarter, year)
   * @return JSON response with cost breakdowns and budget comparisons
   * @see ../../grundlagen/SECURITY_GUIDELINES.md - Role-Based Access Control
   */
  @GET
  @Path("/cost-overview")
  @RolesAllowed({"manager","admin"})
  public Response costOverview(@QueryParam("range") @DefaultValue("month") String range) {
    var summary = ReportsQuery.fetchCostOverview(costs, range);
    return Response.ok(summary).build();
  }

  /**
   * Universal Export Endpoint - Supports all Report Types and Formats
   *
   * @param type Report type (sales-summary, customer-analytics, activity-stats)
   * @param format Export format (csv, xlsx, pdf, json, html, jsonl)
   * @param range Time range filter for data selection
   * @param segment Customer segment filter
   * @param territory Geographic territory filter (ABAC-secured)
   * @return File download response with appropriate Content-Type and filename
   * @see ../../grundlagen/PERFORMANCE_STANDARDS.md - Streaming for Large Exports
   */
  @GET
  @Path("/export")
  @RolesAllowed({"user","manager","admin"})
  @Produces({MediaType.APPLICATION_OCTET_STREAM, "application/x-ndjson",
             MediaType.APPLICATION_JSON})
  public Response export(@QueryParam("type") String type,
                         @QueryParam("format") String format,
                         @QueryParam("range") String range,
                         @QueryParam("segment") String segment,
                         @QueryParam("territory") String territory) {
    var stream = UniversalExportAdapter.export(type, format, range,
        segment, territory);
    return Response.ok(stream)
        .header("Content-Disposition", "attachment; filename=report." + format)
        .build();
  }
}

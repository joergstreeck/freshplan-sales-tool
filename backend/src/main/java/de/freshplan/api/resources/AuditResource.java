package de.freshplan.api.resources;

import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.entity.AuditSource;
import de.freshplan.domain.audit.repository.AuditRepository;
import de.freshplan.domain.audit.service.AuditService;
import de.freshplan.domain.audit.service.dto.AuditContext;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * REST API for audit trail access and management
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/audit")
@ApplicationScoped
@Authenticated
@Tag(name = "Audit", description = "Audit trail management and compliance reporting")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuditResource {

  private static final Logger log = Logger.getLogger(AuditResource.class);

  @Inject AuditRepository auditRepository;

  @Inject AuditService auditService;

  @GET
  @Path("/entity/{entityType}/{entityId}")
  @RolesAllowed({"admin", "manager", "auditor"})
  @Operation(summary = "Get audit trail for an entity")
  @APIResponse(
      responseCode = "200",
      description = "Audit entries for the entity",
      content =
          @Content(schema = @Schema(implementation = AuditEntry.class, type = SchemaType.ARRAY)))
  public Response getEntityAuditTrail(
      @PathParam("entityType") String entityType,
      @PathParam("entityId") UUID entityId,
      @QueryParam("page") @DefaultValue("0") @Min(0) int page,
      @QueryParam("size") @DefaultValue("50") @Min(1) @Max(100) int size) {

    log.infof("Fetching audit trail for %s/%s", entityType, entityId);

    List<AuditEntry> entries = auditRepository.findByEntity(entityType, entityId, page, size);

    // Log the audit access itself
    auditService.logAsync(
        AuditContext.of(AuditEventType.DATA_EXPORT_STARTED, "audit_trail", UUID.randomUUID()));

    return Response.ok(entries).build();
  }

  @GET
  @Path("/search")
  @RolesAllowed({"admin", "auditor"})
  @Operation(summary = "Search audit entries with filters")
  public Response searchAuditTrail(
      @QueryParam("entityType") String entityType,
      @QueryParam("entityId") UUID entityId,
      @QueryParam("userId") UUID userId,
      @QueryParam("eventType") List<AuditEventType> eventTypes,
      @QueryParam("source") List<AuditSource> sources,
      @QueryParam("from") @Parameter(description = "ISO date-time") String fromStr,
      @QueryParam("to") @Parameter(description = "ISO date-time") String toStr,
      @QueryParam("searchText") String searchText,
      @QueryParam("page") @DefaultValue("0") @Min(0) int page,
      @QueryParam("size") @DefaultValue("50") @Min(1) @Max(100) int size) {

    Instant from =
        fromStr != null
            ? Instant.parse(fromStr)
            : Instant.now().minusSeconds(86400 * 7); // Default 7 days
    Instant to = toStr != null ? Instant.parse(toStr) : Instant.now();

    var criteria =
        AuditRepository.AuditSearchCriteria.builder()
            .entityType(entityType)
            .entityId(entityId)
            .userId(userId)
            .eventTypes(eventTypes)
            .sources(sources)
            .from(from)
            .to(to)
            .searchText(searchText)
            .page(page)
            .size(size)
            .build();

    List<AuditEntry> entries = auditRepository.search(criteria);

    return Response.ok(entries).build();
  }

  /**
   * @deprecated Use /api/v2/export/audit/{format} endpoint with Universal Export Framework instead
   */
  @GET
  @Path("/export")
  @RolesAllowed({"admin", "auditor"})
  @Operation(summary = "Export audit trail data - DEPRECATED", deprecated = true)
  @Deprecated(since = "2.0", forRemoval = true)
  public Response exportAuditTrail(
      @QueryParam("format") @DefaultValue("csv") String format,
      @QueryParam("from") String fromStr,
      @QueryParam("to") String toStr,
      @QueryParam("entityType") String entityType,
      @QueryParam("eventType") List<AuditEventType> eventTypes) {

    // Redirect to new Universal Export Framework endpoint, forwarding query parameters
    StringBuilder redirectUrlBuilder =
        new StringBuilder(String.format("/api/v2/export/audit/%s", format));
    boolean firstParam = true;

    if (fromStr != null) {
      redirectUrlBuilder.append(firstParam ? "?" : "&").append("from=").append(fromStr);
      firstParam = false;
    }
    if (toStr != null) {
      redirectUrlBuilder.append(firstParam ? "?" : "&").append("to=").append(toStr);
      firstParam = false;
    }
    if (entityType != null) {
      redirectUrlBuilder.append(firstParam ? "?" : "&").append("entityType=").append(entityType);
      firstParam = false;
    }
    if (eventTypes != null && !eventTypes.isEmpty()) {
      for (AuditEventType eventType : eventTypes) {
        redirectUrlBuilder
            .append(firstParam ? "?" : "&")
            .append("eventType=")
            .append(eventType.name());
        firstParam = false;
      }
    }

    String redirectUrl = redirectUrlBuilder.toString();
    return Response.status(Response.Status.MOVED_PERMANENTLY)
        .header("Location", redirectUrl)
        .entity("This endpoint is deprecated. Please use " + redirectUrl)
        .build();
  }

  @GET
  @Path("/statistics")
  @RolesAllowed({"admin", "auditor"})
  @Operation(summary = "Get audit statistics for a time period")
  public Response getStatistics(
      @QueryParam("from") @Parameter(description = "ISO date-time") String fromStr,
      @QueryParam("to") @Parameter(description = "ISO date-time") String toStr) {

    Instant from = fromStr != null ? Instant.parse(fromStr) : Instant.now().minusSeconds(86400 * 7);
    Instant to = toStr != null ? Instant.parse(toStr) : Instant.now();

    var stats = auditRepository.getStatistics(from, to);

    return Response.ok(stats).build();
  }

  @GET
  @Path("/security-events")
  @RolesAllowed({"admin", "security"})
  @Operation(summary = "Get security-relevant audit events")
  public Response getSecurityEvents(@QueryParam("hours") @DefaultValue("24") int hours) {

    Instant from = Instant.now().minusSeconds(hours * 3600L);
    List<AuditEntry> entries = auditRepository.findSecurityEvents(from, Instant.now());

    return Response.ok(entries).build();
  }

  @GET
  @Path("/failures")
  @RolesAllowed({"admin", "manager"})
  @Operation(summary = "Get failed operations")
  public Response getFailures(@QueryParam("hours") @DefaultValue("24") int hours) {

    Instant from = Instant.now().minusSeconds(hours * 3600L);
    List<AuditEntry> entries = auditRepository.findFailures(from, Instant.now());

    return Response.ok(entries).build();
  }

  @POST
  @Path("/verify-integrity")
  @RolesAllowed({"admin", "auditor"})
  @Operation(summary = "Verify audit trail integrity")
  public Response verifyIntegrity(
      @QueryParam("from") @Parameter(description = "ISO date-time") String fromStr,
      @QueryParam("to") @Parameter(description = "ISO date-time") String toStr) {

    Instant from = fromStr != null ? Instant.parse(fromStr) : Instant.now().minusSeconds(86400);
    Instant to = toStr != null ? Instant.parse(toStr) : Instant.now();

    var issues = auditRepository.verifyIntegrity(from, to);

    if (issues.isEmpty()) {
      return Response.ok(Map.of("status", "valid", "message", "Audit trail integrity verified"))
          .build();
    } else {
      return Response.status(Response.Status.CONFLICT)
          .entity(Map.of("status", "compromised", "issues", issues))
          .build();
    }
  }

  @GET
  @Path("/dashboard/metrics")
  @RolesAllowed({"admin", "auditor"})
  @Operation(summary = "Get audit dashboard metrics")
  public Response getDashboardMetrics() {
    log.info("Fetching real dashboard metrics from database");

    try {
      // Get real metrics from repository
      var dashboardMetrics = auditRepository.getDashboardMetrics();

      // Convert to response format
      var metrics =
          Map.of(
              "coverage", dashboardMetrics.coverage,
              "integrityStatus", dashboardMetrics.integrityStatus,
              "retentionCompliance", dashboardMetrics.retentionCompliance,
              "lastAudit", dashboardMetrics.lastAudit,
              "criticalEventsToday", dashboardMetrics.criticalEventsToday,
              "activeUsers", dashboardMetrics.activeUsers,
              "totalEventsToday", dashboardMetrics.totalEventsToday,
              "topEventTypes", dashboardMetrics.topEventTypes);

      return Response.ok(metrics).build();
    } catch (Exception e) {
      log.errorf(e, "Failed to fetch dashboard metrics");
      // Fallback to basic metrics on error
      var fallbackMetrics =
          Map.of(
              "coverage",
              0.0,
              "integrityStatus",
              "error",
              "retentionCompliance",
              0,
              "lastAudit",
              Instant.now().toString(),
              "criticalEventsToday",
              0L,
              "activeUsers",
              0L,
              "totalEventsToday",
              0L,
              "topEventTypes",
              List.of());
      return Response.ok(fallbackMetrics).build();
    }
  }

  @GET
  @Path("/dashboard/activity-chart")
  @RolesAllowed({"admin", "auditor"})
  @Operation(summary = "Get activity data for chart visualization")
  public Response getActivityChartData(
      @QueryParam("days") @DefaultValue("7") int days,
      @QueryParam("groupBy") @DefaultValue("hour") String groupBy) {

    log.infof("Fetching activity chart data for %d days, grouped by %s", days, groupBy);

    try {
      // Get real activity data from repository
      var data = auditRepository.getActivityChartData(days, groupBy);

      if (data.isEmpty()) {
        // Return default data if no activity
        data =
            List.of(
                Map.of("time", "00:00", "value", 0L),
                Map.of("time", "06:00", "value", 0L),
                Map.of("time", "12:00", "value", 0L),
                Map.of("time", "18:00", "value", 0L));
      }

      return Response.ok(data).build();
    } catch (Exception e) {
      log.errorf(e, "Failed to fetch activity chart data");
      // Return empty data on error
      return Response.ok(List.of()).build();
    }
  }

  @GET
  @Path("/dashboard/critical-events")
  @RolesAllowed({"admin", "auditor"})
  @Operation(summary = "Get recent critical events")
  public Response getCriticalEvents(
      @QueryParam("limit") @DefaultValue("10") @Min(1) @Max(50) int limit) {

    log.infof("Fetching critical events with limit %d", limit);

    try {
      // Get real critical events from repository
      List<AuditEntry> events = auditRepository.getCriticalEvents(limit);

      return Response.ok(events).build();
    } catch (Exception e) {
      log.errorf(e, "Failed to fetch critical events");
      return Response.ok(List.of()).build();
    }
  }

  @GET
  @Path("/dashboard/compliance-alerts")
  @RolesAllowed({"admin", "auditor"})
  @Operation(summary = "Get compliance alerts and warnings")
  public Response getComplianceAlerts() {
    log.info("Fetching compliance alerts");

    try {
      // Get real compliance alerts from repository
      var alerts = auditRepository.getComplianceAlerts();

      return Response.ok(alerts).build();
    } catch (Exception e) {
      log.errorf(e, "Failed to fetch compliance alerts");
      // Return empty list on error
      return Response.ok(List.of()).build();
    }
  }

  /** Escape CSV special characters */
  private String csvEscape(String value) {
    if (value == null) {
      return "";
    }

    // Escape quotes and wrap in quotes if contains comma, newline, or quote
    if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    return value;
  }
}

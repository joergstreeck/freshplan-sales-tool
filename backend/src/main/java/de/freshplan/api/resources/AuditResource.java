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
import jakarta.ws.rs.core.StreamingOutput;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
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

  @GET
  @Path("/export")
  @RolesAllowed({"admin", "auditor"})
  @Operation(summary = "Export audit trail data")
  @Produces({"text/csv", "application/json"})
  public Response exportAuditTrail(
      @QueryParam("format") @DefaultValue("csv") String format,
      @QueryParam("from") @Parameter(description = "ISO date") String fromStr,
      @QueryParam("to") @Parameter(description = "ISO date") String toStr,
      @QueryParam("entityType") String entityType,
      @QueryParam("eventType") List<AuditEventType> eventTypes) {

    LocalDate from = fromStr != null ? LocalDate.parse(fromStr) : LocalDate.now().minusDays(30);
    LocalDate to = toStr != null ? LocalDate.parse(toStr) : LocalDate.now();

    // Build search criteria
    var criteria =
        AuditRepository.AuditSearchCriteria.builder()
            .entityType(entityType)
            .eventTypes(eventTypes)
            .from(from.atStartOfDay(ZoneOffset.UTC).toInstant())
            .to(to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant())
            .build();

    // Log the export request
    auditService.logSync(
        AuditContext.builder()
            .eventType(AuditEventType.DATA_EXPORT_STARTED)
            .entityType("audit_trail")
            .entityId(UUID.randomUUID())
            .newValue(
                Map.of(
                    "format", format,
                    "from", from,
                    "to", to,
                    "filters", criteria))
            .build());

    if ("json".equalsIgnoreCase(format)) {
      List<AuditEntry> entries = auditRepository.search(criteria);
      return Response.ok(entries)
          .type(MediaType.APPLICATION_JSON)
          .header(
              "Content-Disposition",
              "attachment; filename=audit_trail_" + LocalDate.now() + ".json")
          .build();
    } else {
      // Stream CSV for memory efficiency
      StreamingOutput stream =
          output -> {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
                Stream<AuditEntry> entries = auditRepository.streamForExport(criteria)) {

              // Write CSV header
              writer.write(
                  "Timestamp,Event Type,Entity Type,Entity ID,User,User Role,Change Reason,IP Address,Source\n");

              // Write entries
              entries.forEach(
                  entry -> {
                    try {
                      writer.write(
                          String.format(
                              "%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                              entry.getTimestamp(),
                              entry.getEventType(),
                              entry.getEntityType(),
                              entry.getEntityId(),
                              entry.getUserName(),
                              entry.getUserRole(),
                              csvEscape(entry.getChangeReason()),
                              entry.getIpAddress(),
                              entry.getSource()));
                    } catch (Exception e) {
                      log.errorf(e, "Error writing audit entry to CSV");
                    }
                  });

              writer.flush();
            }
          };

      return Response.ok(stream)
          .type("text/csv")
          .header(
              "Content-Disposition", "attachment; filename=audit_trail_" + LocalDate.now() + ".csv")
          .build();
    }
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

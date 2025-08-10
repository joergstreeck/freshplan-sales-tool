package de.freshplan.api.resources;

import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.entity.AuditSource;
import de.freshplan.domain.audit.repository.AuditRepository;
import de.freshplan.domain.audit.service.AuditService;
import de.freshplan.domain.audit.service.dto.AuditContext;
import de.freshplan.infrastructure.export.*;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.HashMap;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * REST API for audit trail export using Universal Export Framework
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/v2/export/audit")
@ApplicationScoped
@Authenticated
@Tag(name = "Audit Export", description = "Export audit trail data in various formats")
@Produces(MediaType.APPLICATION_JSON)
public class AuditExportResource {

  private static final Logger log = Logger.getLogger(AuditExportResource.class);

  @Inject AuditRepository auditRepository;

  @Inject AuditService auditService;

  @Inject UniversalExportService exportService;

  @GET
  @Path("/{format}")
  @RolesAllowed({"admin", "auditor"})
  @Operation(summary = "Export audit data in specified format")
  @Produces({
    "text/csv",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "application/json",
    "text/html",
    "application/pdf"
  })
  public Response exportAudit(
      @PathParam("format") String format,
      @QueryParam("from") String fromStr,
      @QueryParam("to") String toStr,
      @QueryParam("entityType") String entityType,
      @QueryParam("entityId") String entityId,
      @QueryParam("userId") String userId,
      @QueryParam("eventType") List<AuditEventType> eventTypes,
      @QueryParam("source") List<AuditSource> sources,
      @QueryParam("searchText") String searchText) {

    log.infof("Exporting audit data as %s", format);

    try {
      // Parse export format
      ExportFormat exportFormat = ExportFormat.fromString(format.toUpperCase());

      // Build date range
      LocalDate from = fromStr != null ? LocalDate.parse(fromStr) : LocalDate.now().minusDays(30);
      LocalDate to = toStr != null ? LocalDate.parse(toStr) : LocalDate.now();

      // Build search criteria
      var criteria =
          AuditRepository.AuditSearchCriteria.builder()
              .entityType(entityType)
              .entityId(entityId != null ? UUID.fromString(entityId) : null)
              .userId(userId != null ? UUID.fromString(userId) : null)
              .eventTypes(eventTypes)
              .sources(sources)
              .from(from.atStartOfDay(ZoneOffset.UTC).toInstant())
              .to(to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant())
              .searchText(searchText)
              .build();

      // Fetch audit entries
      List<AuditEntry> entries = auditRepository.search(criteria);

      // Convert to export format
      List<Map<String, Object>> exportData = new ArrayList<>();
      for (AuditEntry entry : entries) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("timestamp", entry.getTimestamp());
        row.put("eventType", entry.getEventType() != null ? entry.getEventType().toString() : "");
        row.put("entityType", entry.getEntityType());
        row.put("entityId", entry.getEntityId() != null ? entry.getEntityId().toString() : "");
        row.put("userId", entry.getUserId() != null ? entry.getUserId().toString() : "");
        row.put("userName", entry.getUserName());
        row.put("userRole", entry.getUserRole());
        row.put("source", entry.getSource() != null ? entry.getSource().toString() : "");
        row.put("ipAddress", entry.getIpAddress());
        row.put("userAgent", entry.getUserAgent());
        row.put("changeReason", entry.getChangeReason());
        row.put("oldValue", entry.getOldValue());
        row.put("newValue", entry.getNewValue());
        row.put("userComment", entry.getUserComment());
        exportData.add(row);
      }

      // Build export configuration with field configurations
      ExportConfig.Builder configBuilder =
          ExportConfig.builder()
              .title("Audit Trail Export")
              .subtitle(
                  String.format(
                      "Von %s bis %s",
                      from.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                      to.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))))
              .generatedBy(getCurrentUser());

      // Add field configurations using builder pattern
      configBuilder.addField(
          ExportConfig.FieldConfig.builder()
              .key("timestamp")
              .label("Zeitstempel")
              .type(ExportConfig.FieldType.DATETIME)
              .build());
      configBuilder.addField(
          ExportConfig.FieldConfig.builder()
              .key("eventType")
              .label("Ereignistyp")
              .type(ExportConfig.FieldType.STRING)
              .build());
      configBuilder.addField(
          ExportConfig.FieldConfig.builder()
              .key("entityType")
              .label("Entitätstyp")
              .type(ExportConfig.FieldType.STRING)
              .build());
      configBuilder.addField(
          ExportConfig.FieldConfig.builder()
              .key("entityId")
              .label("Entitäts-ID")
              .type(ExportConfig.FieldType.STRING)
              .build());
      configBuilder.addField(
          ExportConfig.FieldConfig.builder()
              .key("userId")
              .label("Benutzer-ID")
              .type(ExportConfig.FieldType.STRING)
              .build());
      configBuilder.addField(
          ExportConfig.FieldConfig.builder()
              .key("userName")
              .label("Benutzer")
              .type(ExportConfig.FieldType.STRING)
              .build());
      configBuilder.addField(
          ExportConfig.FieldConfig.builder()
              .key("userRole")
              .label("Rolle")
              .type(ExportConfig.FieldType.STRING)
              .build());
      configBuilder.addField(
          ExportConfig.FieldConfig.builder()
              .key("source")
              .label("Quelle")
              .type(ExportConfig.FieldType.STRING)
              .build());
      configBuilder.addField(
          ExportConfig.FieldConfig.builder()
              .key("ipAddress")
              .label("IP-Adresse")
              .type(ExportConfig.FieldType.STRING)
              .build());
      configBuilder.addField(
          ExportConfig.FieldConfig.builder()
              .key("userAgent")
              .label("User Agent")
              .type(ExportConfig.FieldType.STRING)
              .build());
      configBuilder.addField(
          ExportConfig.FieldConfig.builder()
              .key("changeReason")
              .label("Änderungsgrund")
              .type(ExportConfig.FieldType.STRING)
              .build());
      configBuilder.addField(
          ExportConfig.FieldConfig.builder()
              .key("oldValue")
              .label("Alter Wert")
              .type(ExportConfig.FieldType.STRING)
              .build());
      configBuilder.addField(
          ExportConfig.FieldConfig.builder()
              .key("newValue")
              .label("Neuer Wert")
              .type(ExportConfig.FieldType.STRING)
              .build());
      configBuilder.addField(
          ExportConfig.FieldConfig.builder()
              .key("userComment")
              .label("Kommentar")
              .type(ExportConfig.FieldType.STRING)
              .build());

      // Add filters
      Map<String, Object> filters = new HashMap<>();
      if (entityType != null) filters.put("entityType", entityType);
      if (userId != null) filters.put("userId", userId);
      if (eventTypes != null && !eventTypes.isEmpty()) {
        filters.put(
            "eventTypes", String.join(", ", eventTypes.stream().map(Enum::toString).toList()));
      }
      if (!filters.isEmpty()) {
        configBuilder.filters(filters);
      }

      ExportConfig config = configBuilder.build();

      // Log the export
      auditService.logAsync(
          AuditContext.builder()
              .eventType(AuditEventType.DATA_EXPORT_STARTED)
              .entityType("audit_trail")
              .entityId(UUID.randomUUID())
              .newValue(
                  Map.of(
                      "format", exportFormat.toString(),
                      "records", exportData.size(),
                      "from", from.toString(),
                      "to", to.toString()))
              .build());

      // Export using Universal Export Service
      return exportService.exportAsResponse(exportData, config, exportFormat);

    } catch (IllegalArgumentException e) {
      log.error("Invalid export format: " + format, e);
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(Map.of("error", "Invalid export format: " + format))
          .build();
    } catch (Exception e) {
      log.error("Export failed", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(Map.of("error", "Export failed: " + e.getMessage()))
          .build();
    }
  }

  private String getCurrentUser() {
    // TODO: Get from security context
    return "System";
  }
}

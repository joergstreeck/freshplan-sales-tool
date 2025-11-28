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

      // Parse UUIDs (PMD Complexity Refactoring - Issue #146)
      UUID parsedEntityId = parseUuidParam(entityId, "entityId");
      UUID parsedUserId = parseUuidParam(userId, "userId");

      // Build search criteria
      var criteria =
          AuditRepository.AuditSearchCriteria.builder()
              .entityType(entityType)
              .entityId(parsedEntityId)
              .userId(parsedUserId)
              .eventTypes(eventTypes)
              .sources(sources)
              .from(from.atStartOfDay(ZoneOffset.UTC).toInstant())
              .to(to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant())
              .searchText(searchText)
              .build();

      // Fetch audit entries
      List<AuditEntry> entries = auditRepository.search(criteria);

      // Convert to export format (PMD Complexity Refactoring - Issue #146)
      List<Map<String, Object>> exportData = convertEntriesToExportData(entries);

      // Build export configuration (PMD Complexity Refactoring - Issue #146)
      ExportConfig config = buildExportConfig(from, to, entityType, userId, eventTypes);

      // Log the export
      logExportEvent(exportFormat, exportData.size(), from, to);

      // Export using Universal Export Service
      return exportService.exportAsResponse(exportData, config, exportFormat);

    } catch (IllegalArgumentException e) {
      log.error("Invalid export format: " + format, e);
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(Map.of("error", "Invalid export format: " + format))
          .build();
    } catch (UuidParseException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(Map.of("error", e.getMessage()))
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

  // ============================================================================
  // PMD Complexity Refactoring (Issue #146) - Helper methods for exportAudit()
  // ============================================================================

  /** Custom exception for UUID parsing errors. */
  private static class UuidParseException extends RuntimeException {
    UuidParseException(String message) {
      super(message);
    }
  }

  /**
   * Parse UUID parameter with proper error handling.
   *
   * @param value the string value to parse
   * @param paramName the parameter name for error messages
   * @return parsed UUID or null if value is null
   * @throws UuidParseException if value is invalid UUID format
   */
  private UUID parseUuidParam(String value, String paramName) {
    if (value == null) {
      return null;
    }
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException e) {
      throw new UuidParseException("Invalid " + paramName + " format: " + value);
    }
  }

  /**
   * Convert audit entries to export data format.
   *
   * @param entries the audit entries to convert
   * @return list of maps with export data
   */
  private List<Map<String, Object>> convertEntriesToExportData(List<AuditEntry> entries) {
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
    return exportData;
  }

  /**
   * Build export configuration with field definitions.
   *
   * @param from start date
   * @param to end date
   * @param entityType entity type filter
   * @param userId user ID filter
   * @param eventTypes event type filter
   * @return configured ExportConfig
   */
  private ExportConfig buildExportConfig(
      LocalDate from,
      LocalDate to,
      String entityType,
      String userId,
      List<AuditEventType> eventTypes) {

    ExportConfig.Builder configBuilder =
        ExportConfig.builder()
            .title("Audit Trail Export")
            .subtitle(
                String.format(
                    "Von %s bis %s",
                    from.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    to.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))))
            .generatedBy(getCurrentUser());

    // Add field configurations
    addAuditFieldConfigs(configBuilder);

    // Add filters
    addFilters(configBuilder, entityType, userId, eventTypes);

    return configBuilder.build();
  }

  /**
   * Add field configurations to export config builder.
   *
   * @param configBuilder the builder to add fields to
   */
  private void addAuditFieldConfigs(ExportConfig.Builder configBuilder) {
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
  }

  /**
   * Add filter information to export config.
   *
   * @param configBuilder the builder to add filters to
   * @param entityType entity type filter
   * @param userId user ID filter
   * @param eventTypes event type filter
   */
  private void addFilters(
      ExportConfig.Builder configBuilder,
      String entityType,
      String userId,
      List<AuditEventType> eventTypes) {
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
  }

  /**
   * Log the export event for audit trail.
   *
   * @param exportFormat the export format used
   * @param recordCount number of records exported
   * @param from start date
   * @param to end date
   */
  private void logExportEvent(
      ExportFormat exportFormat, int recordCount, LocalDate from, LocalDate to) {
    auditService.logAsync(
        AuditContext.builder()
            .eventType(AuditEventType.DATA_EXPORT_STARTED)
            .entityType("audit_trail")
            .entityId(UUID.randomUUID())
            .newValue(
                Map.of(
                    "format", exportFormat.toString(),
                    "records", recordCount,
                    "from", from.toString(),
                    "to", to.toString()))
            .build());
  }
}

package de.freshplan.api.resources;

import static de.freshplan.infrastructure.export.ExportConfig.FieldType.*;
import static de.freshplan.infrastructure.export.UniversalExportService.field;

import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.repository.AuditRepository;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.infrastructure.export.*;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.time.LocalDateTime;
import java.util.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * Generic export REST endpoint using Universal Export Framework. This replaces the old
 * ExportResource with a unified implementation.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/v2/export")
@ApplicationScoped
@Authenticated
@Tag(
    name = "Universal Export",
    description = "Unified export endpoints using Universal Export Framework")
public class GenericExportResource {

  private static final Logger log = Logger.getLogger(GenericExportResource.class);

  @Inject UniversalExportService exportService;

  @Inject AuditRepository auditRepository;

  @Inject CustomerRepository customerRepository;

  /** Export audit entries in any format */
  @GET
  @Path("/audit/{format}")
  @RolesAllowed({"admin", "auditor", "manager"})
  @Operation(summary = "Export audit trail in specified format")
  public Response exportAudit(
      @PathParam("format") String formatStr,
      @QueryParam("entityType") String entityType,
      @QueryParam("entityId") UUID entityId,
      @QueryParam("from") String from,
      @QueryParam("to") String to,
      @QueryParam("userId") String userId,
      @QueryParam("eventType") String eventType,
      @Context UriInfo uriInfo) {

    log.infof("Exporting audit data as %s", formatStr);

    try {
      // Parse format
      ExportFormat format = ExportFormat.fromString(formatStr);

      // Fetch data
      List<AuditEntry> entries = fetchAuditData(entityType, entityId, from, to, userId, eventType);

      // Build configuration
      ExportConfig config =
          ExportConfig.builder()
              .title("Audit Trail Report")
              .subtitle(String.format("%d Einträge gefunden", entries.size()))
              .generatedBy("FreshPlan System")
              .fields(
                  Arrays.asList(
                      field("timestamp", "Zeitstempel", DATETIME, "dd.MM.yyyy HH:mm:ss"),
                      field("eventType", "Event", STRING),
                      field("entityType", "Entity Typ", STRING),
                      field("entityId", "Entity ID", STRING),
                      field("userName", "Benutzer", STRING),
                      field("ipAddress", "IP-Adresse", STRING),
                      field("source", "Quelle", STRING),
                      field("dataHash", "Hash", STRING)))
              .styles(ExportConfig.ExportStyles.defaultStyles())
              .build();

      // Export using Universal Export Service
      return exportService.exportAsResponse(entries, config, format);

    } catch (IllegalArgumentException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(Map.of("error", "Unsupported format: " + formatStr))
          .build();
    } catch (Exception e) {
      log.error("Export failed", e);
      return Response.serverError().entity(Map.of("error", e.getMessage())).build();
    }
  }

  /** Export customers in any format */
  @GET
  @Path("/customers/{format}")
  @RolesAllowed({"admin", "manager", "sales"})
  @Operation(summary = "Export customers in specified format")
  public Response exportCustomers(
      @PathParam("format") String formatStr,
      @QueryParam("status") List<String> status,
      @QueryParam("industry") String industry,
      @QueryParam("includeContacts") @DefaultValue("true") boolean includeContacts,
      @Context UriInfo uriInfo) {

    log.infof("Exporting customers as %s", formatStr);

    try {
      // Parse format
      ExportFormat format = ExportFormat.fromString(formatStr);

      // Fetch data
      List<Customer> customers = customerRepository.findByFilters(status, industry);

      // Prepare data based on includeContacts flag
      List<?> exportData;
      ExportConfig config;

      if (includeContacts && format != ExportFormat.JSON) {
        // Flatten customers with contacts for tabular formats
        exportData = flattenCustomersWithContacts(customers);
        config = buildCustomerContactConfig();
      } else {
        // Export customers as-is for JSON or without contacts
        exportData = customers;
        config = buildCustomerConfig(includeContacts);
      }

      // Export using Universal Export Service
      return exportService.exportAsResponse(exportData, config, format);

    } catch (IllegalArgumentException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(Map.of("error", "Unsupported format: " + formatStr))
          .build();
    } catch (Exception e) {
      log.error("Export failed", e);
      return Response.serverError().entity(Map.of("error", e.getMessage())).build();
    }
  }

  /** Generic export endpoint for any entity */
  @GET
  @Path("/{entity}/{format}")
  @RolesAllowed({"admin", "manager"})
  @Operation(summary = "Export any entity in specified format")
  public Response exportEntity(
      @PathParam("entity") String entity,
      @PathParam("format") String formatStr,
      @Context UriInfo uriInfo) {

    log.infof("Generic export: entity=%s, format=%s", entity, formatStr);

    try {
      // Parse format
      ExportFormat format = ExportFormat.fromString(formatStr);

      // Route to appropriate handler based on entity
      switch (entity.toLowerCase()) {
        case "audit":
          return exportAudit(formatStr, null, null, null, null, null, null, uriInfo);
        case "customers":
          return exportCustomers(formatStr, null, null, true, uriInfo);
        default:
          return Response.status(Response.Status.NOT_FOUND)
              .entity(Map.of("error", "Unknown entity: " + entity))
              .build();
      }

    } catch (Exception e) {
      log.error("Export failed", e);
      return Response.serverError().entity(Map.of("error", e.getMessage())).build();
    }
  }

  /** Get supported export formats */
  @GET
  @Path("/formats")
  @Operation(summary = "Get list of supported export formats")
  public Response getSupportedFormats() {
    List<ExportFormat> supportedFormats = exportService.getSupportedFormats();
    List<Map<String, String>> formats = new ArrayList<>();

    for (ExportFormat format : supportedFormats) {
      Map<String, String> formatInfo = new HashMap<>();
      formatInfo.put("format", format.name().toLowerCase());
      formatInfo.put("mimeType", format.getMimeType());
      formatInfo.put("extension", format.getExtension());
      formatInfo.put("displayName", format.getDisplayName());
      formats.add(formatInfo);
    }

    return Response.ok(formats).build();
  }

  // Helper methods

  private List<AuditEntry> fetchAuditData(
      String entityType, UUID entityId, String from, String to, String userId, String eventType) {
    // PMD Complexity Refactoring (Issue #146) - Extracted filter methods
    var query = new StringBuilder("1=1");
    var params = new HashMap<String, Object>();

    addStringFilter(query, params, "entityType", entityType);
    addUuidFilter(query, params, "entityId", entityId);
    addStringFilter(query, params, "userId", userId);
    addStringFilter(query, params, "eventType", eventType);
    addDateTimeFilter(query, params, "from", "timestamp >=", from);
    addDateTimeFilter(query, params, "to", "timestamp <=", to);

    return auditRepository.find(query.toString(), params).list();
  }

  // ============================================================================
  // PMD Complexity Refactoring (Issue #146) - Helper methods for fetchAuditData()
  // ============================================================================

  private void addStringFilter(
      StringBuilder query, Map<String, Object> params, String paramName, String value) {
    if (value != null && !value.isEmpty()) {
      query.append(" AND ").append(paramName).append(" = :").append(paramName);
      params.put(paramName, value);
    }
  }

  private void addUuidFilter(
      StringBuilder query, Map<String, Object> params, String paramName, UUID value) {
    if (value != null) {
      query.append(" AND ").append(paramName).append(" = :").append(paramName);
      params.put(paramName, value);
    }
  }

  private void addDateTimeFilter(
      StringBuilder query,
      Map<String, Object> params,
      String paramName,
      String condition,
      String value) {
    if (value != null && !value.isEmpty()) {
      query.append(" AND ").append(condition).append(" :").append(paramName);
      params.put(paramName, LocalDateTime.parse(value));
    }
  }

  private ExportConfig buildCustomerConfig(boolean includeContacts) {
    List<ExportConfig.FieldConfig> fields = new ArrayList<>();

    // Basic customer fields
    fields.add(field("customerNumber", "Kundennummer", STRING));
    fields.add(field("companyName", "Firma", STRING));
    fields.add(field("status", "Status", STRING));
    fields.add(field("industry", "Branche", STRING));
    fields.add(field("createdAt", "Erstellt", DATE, "dd.MM.yyyy"));

    if (!includeContacts) {
      fields.add(field("contactCount", "Anzahl Kontakte", NUMBER));
    }

    return ExportConfig.builder()
        .title("Kundenliste")
        .subtitle(String.format("Exportiert am %s", LocalDateTime.now()))
        .generatedBy("FreshPlan System")
        .fields(fields)
        .styles(ExportConfig.ExportStyles.defaultStyles())
        .build();
  }

  private ExportConfig buildCustomerContactConfig() {
    return ExportConfig.builder()
        .title("Kunden mit Kontakten")
        .subtitle(String.format("Exportiert am %s", LocalDateTime.now()))
        .generatedBy("FreshPlan System")
        .fields(
            Arrays.asList(
                field("customerNumber", "Kundennummer", STRING),
                field("companyName", "Firma", STRING),
                field("status", "Status", STRING),
                field("industry", "Branche", STRING),
                field("contactName", "Kontakt Name", STRING),
                field("contactEmail", "E-Mail", STRING),
                field("contactPhone", "Telefon", STRING),
                field("contactPosition", "Position", STRING),
                field("contactDepartment", "Abteilung", STRING),
                field("isPrimary", "Hauptkontakt", BOOLEAN)))
        .styles(ExportConfig.ExportStyles.defaultStyles())
        .build();
  }

  private List<Map<String, Object>> flattenCustomersWithContacts(List<Customer> customers) {
    List<Map<String, Object>> flattened = new ArrayList<>();

    for (Customer customer : customers) {
      if (customer.getContacts() != null && !customer.getContacts().isEmpty()) {
        customer
            .getContacts()
            .forEach(
                contact -> {
                  Map<String, Object> row = new HashMap<>();

                  // Customer fields
                  row.put("customerNumber", customer.getCustomerNumber());
                  row.put("companyName", customer.getCompanyName());
                  row.put("status", customer.getStatus());
                  row.put("industry", customer.getIndustry());

                  // Contact fields - handle the generic Object type
                  if (contact instanceof de.freshplan.domain.customer.entity.CustomerContact) {
                    var cc = (de.freshplan.domain.customer.entity.CustomerContact) contact;
                    row.put(
                        "contactName",
                        (cc.getFirstName() != null ? cc.getFirstName() : "")
                            + " "
                            + (cc.getLastName() != null ? cc.getLastName() : ""));
                    row.put("contactEmail", cc.getEmail());
                    row.put("contactPhone", cc.getPhone());
                    row.put("contactPosition", cc.getPosition());
                    row.put("contactDepartment", cc.getDepartment());
                    row.put("isPrimary", cc.getIsPrimary());
                  }

                  flattened.add(row);
                });
      } else {
        // Customer without contacts
        Map<String, Object> row = new HashMap<>();
        row.put("customerNumber", customer.getCustomerNumber());
        row.put("companyName", customer.getCompanyName());
        row.put("status", customer.getStatus());
        row.put("industry", customer.getIndustry());
        row.put("contactName", "");
        row.put("contactEmail", "");
        row.put("contactPhone", "");
        row.put("contactPosition", "");
        row.put("contactDepartment", "");
        row.put("isPrimary", false);
        flattened.add(row);
      }
    }

    return flattened;
  }
}

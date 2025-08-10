package de.freshplan.api.resources;

import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.repository.AuditRepository;
import de.freshplan.domain.audit.service.AuditService;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.export.service.ExportService;
import de.freshplan.domain.export.service.HtmlExportService;
import de.freshplan.domain.export.service.dto.ExportRequest;
import de.freshplan.domain.export.service.dto.ExportOptions;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * REST API for data export in multiple formats
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/export")
@ApplicationScoped
@Authenticated
@Tag(name = "Export", description = "Multi-format data export endpoints")
public class ExportResource {
    
    private static final Logger log = Logger.getLogger(ExportResource.class);
    
    @Inject
    ExportService exportService;
    
    @Inject
    HtmlExportService htmlExportService;
    
    @Inject
    AuditRepository auditRepository;
    
    @Inject
    CustomerRepository customerRepository;
    
    @Inject
    AuditService auditService;
    
    /**
     * Export audit data as CSV
     */
    @GET
    @Path("/audit/csv")
    @RolesAllowed({"admin", "auditor"})
    @Produces("text/csv")
    @Operation(summary = "Export audit trail as CSV")
    public Response exportAuditCsv(
            @QueryParam("entityType") String entityType,
            @QueryParam("entityId") UUID entityId,
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("userId") String userId,
            @QueryParam("eventType") String eventType) {
        
        log.infof("Exporting audit CSV for entityType=%s, entityId=%s", entityType, entityId);
        
        // Build filter criteria
        ExportRequest request = ExportRequest.builder()
                .entityType(entityType)
                .entityId(entityId)
                .dateFrom(from != null ? LocalDateTime.parse(from) : null)
                .dateTo(to != null ? LocalDateTime.parse(to) : null)
                .userId(userId)
                .eventType(eventType)
                .build();
        
        // Log export action
        auditService.logExport("AUDIT_CSV", request.toMap());
        
        // Generate CSV
        StreamingOutput stream = output -> {
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
                
                // Write BOM for Excel compatibility
                writer.write('\ufeff');
                
                // Write CSV header
                writer.write("ID,Timestamp,Event Type,Entity Type,Entity ID,User,User Name,");
                writer.write("IP Address,User Agent,Source,Changes,Hash\n");
                
                // Fetch and write data
                List<AuditEntry> entries = auditRepository.findByFilters(request);
                
                for (AuditEntry entry : entries) {
                    writer.write(formatCsvLine(entry));
                    writer.write("\n");
                }
                
                writer.flush();
            }
        };
        
        String filename = String.format("audit_export_%s.csv", 
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return Response.ok(stream)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .build();
    }
    
    /**
     * Export audit data as PDF
     */
    @GET
    @Path("/audit/pdf")
    @RolesAllowed({"admin", "auditor", "manager"})
    @Produces("application/pdf")
    @Operation(summary = "Export audit trail as PDF report")
    public Response exportAuditPdf(
            @QueryParam("entityType") String entityType,
            @QueryParam("entityId") UUID entityId,
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("includeDetails") @DefaultValue("true") boolean includeDetails) {
        
        log.infof("Exporting audit PDF for entityType=%s, entityId=%s", entityType, entityId);
        
        ExportRequest request = ExportRequest.builder()
                .entityType(entityType)
                .entityId(entityId)
                .dateFrom(from != null ? LocalDateTime.parse(from) : null)
                .dateTo(to != null ? LocalDateTime.parse(to) : null)
                .includeDetails(includeDetails)
                .build();
        
        // Log export action
        auditService.logExport("AUDIT_PDF", request.toMap());
        
        // Generate PDF using export service
        byte[] pdfBytes = exportService.generateAuditPdf(request);
        
        String filename = String.format("audit_report_%s.pdf",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        return Response.ok(pdfBytes)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", "application/pdf")
                .header("Content-Length", pdfBytes.length)
                .build();
    }
    
    /**
     * Export audit data as Excel
     */
    @GET
    @Path("/audit/excel")
    @RolesAllowed({"admin", "auditor"})
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "Export audit trail as Excel workbook")
    public Response exportAuditExcel(
            @QueryParam("entityType") String entityType,
            @QueryParam("entityId") UUID entityId,
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("groupBy") @DefaultValue("day") String groupBy) {
        
        log.infof("Exporting audit Excel for entityType=%s", entityType);
        
        ExportRequest request = ExportRequest.builder()
                .entityType(entityType)
                .entityId(entityId)
                .dateFrom(from != null ? LocalDateTime.parse(from) : null)
                .dateTo(to != null ? LocalDateTime.parse(to) : null)
                .groupBy(groupBy)
                .build();
        
        // Log export action
        auditService.logExport("AUDIT_EXCEL", request.toMap());
        
        // Generate Excel using Apache POI
        byte[] excelBytes = exportService.generateAuditExcel(request);
        
        String filename = String.format("audit_analysis_%s.xlsx",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        return Response.ok(excelBytes)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .build();
    }
    
    /**
     * Export audit data as JSON
     */
    @GET
    @Path("/audit/json")
    @RolesAllowed({"admin", "auditor"})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Export audit trail as JSON")
    public Response exportAuditJson(
            @QueryParam("entityType") String entityType,
            @QueryParam("entityId") UUID entityId,
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            @QueryParam("size") @DefaultValue("1000") @Min(1) @Max(10000) int size) {
        
        ExportRequest request = ExportRequest.builder()
                .entityType(entityType)
                .entityId(entityId)
                .dateFrom(from != null ? LocalDateTime.parse(from) : null)
                .dateTo(to != null ? LocalDateTime.parse(to) : null)
                .page(page)
                .size(size)
                .build();
        
        // Log export action
        auditService.logExport("AUDIT_JSON", request.toMap());
        
        List<AuditEntry> entries = auditRepository.findByFilters(request);
        
        return Response.ok(entries)
                .header("X-Total-Count", auditRepository.countByFilters(request))
                .build();
    }
    
    /**
     * Export customer data with contacts as CSV
     */
    @GET
    @Path("/customers/csv")
    @RolesAllowed({"admin", "manager", "sales"})
    @Produces("text/csv")
    @Operation(summary = "Export customers and contacts as CSV")
    public Response exportCustomersCsv(
            @QueryParam("status") List<String> status,
            @QueryParam("industry") String industry,
            @QueryParam("includeContacts") @DefaultValue("true") boolean includeContacts) {
        
        log.info("Exporting customers CSV");
        
        // Log export action
        auditService.logExport("CUSTOMERS_CSV", Map.of(
                "status", status != null ? status : List.of(),
                "industry", industry != null ? industry : "all",
                "includeContacts", includeContacts
        ));
        
        StreamingOutput stream = output -> {
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
                
                // Write BOM
                writer.write('\ufeff');
                
                // Write header
                if (includeContacts) {
                    writer.write("Customer Number,Company Name,Status,Industry,Location,");
                    writer.write("Contact Name,Contact Email,Contact Phone,Contact Role,");
                    writer.write("Contact Department,Is Primary,Last Contact\n");
                } else {
                    writer.write("Customer Number,Company Name,Status,Industry,Location,");
                    writer.write("Contact Count,Last Contact,Created At\n");
                }
                
                // Fetch and write data
                List<Customer> customers = customerRepository.findByFilters(status, industry);
                
                for (Customer customer : customers) {
                    if (includeContacts && customer.getContacts() != null && !customer.getContacts().isEmpty()) {
                        // Write one line per contact
                        customer.getContacts().forEach(contact -> {
                            try {
                                writer.write(formatCustomerContactCsvLine(customer, contact));
                                writer.write("\n");
                            } catch (Exception e) {
                                log.error("Error writing contact CSV line", e);
                            }
                        });
                    } else {
                        // Write customer summary line
                        writer.write(formatCustomerCsvLine(customer));
                        writer.write("\n");
                    }
                }
                
                writer.flush();
            }
        };
        
        String filename = String.format("customers_export_%s.csv",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        return Response.ok(stream)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .build();
    }
    
    /**
     * Export customers as Excel
     */
    @GET
    @Path("/customers/excel")
    @RolesAllowed({"admin", "manager", "sales"})
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "Export customers as Excel workbook")
    public Response exportCustomersExcel(
            @QueryParam("status") List<String> status,
            @QueryParam("industry") String industry,
            @QueryParam("includeContacts") @DefaultValue("true") boolean includeContacts,
            @QueryParam("includeStats") @DefaultValue("true") boolean includeStats) {
        
        log.info("Exporting customers Excel");
        
        ExportRequest request = ExportRequest.builder()
                .status(status)
                .industry(industry)
                .includeContacts(includeContacts)
                .includeStats(includeStats)
                .build();
        
        // Log export action
        auditService.logExport("CUSTOMERS_EXCEL", request.toMap());
        
        // Generate Excel
        byte[] excelBytes = exportService.generateCustomersExcel(request);
        
        String filename = String.format("customers_%s.xlsx",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        return Response.ok(excelBytes)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .build();
    }
    
    /**
     * Export customers as HTML (for PDF printing)
     * 
     * This is a robust solution that generates HTML which can be:
     * 1. Displayed in the browser
     * 2. Printed to PDF using browser's print function
     * 3. Saved as HTML for further processing
     * 
     * This approach avoids PDF library issues and provides more flexibility
     */
    @GET
    @Path("/customers/pdf")
    @RolesAllowed({"admin", "manager", "sales"})
    @Produces("text/html")
    @Operation(summary = "Export customers as HTML report (printable to PDF)")
    public Response exportCustomersPdf(
            @QueryParam("status") List<String> status,
            @QueryParam("industry") String industry,
            @QueryParam("format") @DefaultValue("list") String format) {
        
        log.info("Exporting customers as HTML for PDF printing");
        
        ExportRequest request = ExportRequest.builder()
                .status(status)
                .industry(industry)
                .format(format)
                .build();
        
        // Log export action
        auditService.logExport("CUSTOMERS_PDF", request.toMap());
        
        // Generate HTML that can be printed to PDF
        String htmlContent = htmlExportService.generateCustomersHtml(request);
        
        // Return HTML with appropriate headers
        // The browser will handle printing to PDF
        return Response.ok(htmlContent)
                .header("Content-Type", "text/html; charset=UTF-8")
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .build();
    }
    
    /**
     * Export customers as JSON
     */
    @GET
    @Path("/customers/json")
    @RolesAllowed({"admin", "manager", "sales"})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Export customers as JSON")
    public Response exportCustomersJson(
            @QueryParam("status") List<String> status,
            @QueryParam("industry") String industry,
            @QueryParam("includeContacts") @DefaultValue("true") boolean includeContacts,
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            @QueryParam("size") @DefaultValue("1000") @Min(1) @Max(10000) int size) {
        
        ExportRequest request = ExportRequest.builder()
                .status(status)
                .industry(industry)
                .includeContacts(includeContacts)
                .page(page)
                .size(size)
                .build();
        
        // Log export action
        auditService.logExport("CUSTOMERS_JSON", request.toMap());
        
        List<Customer> customers = customerRepository.findByFilters(status, industry, page, size);
        
        return Response.ok(customers)
                .header("X-Total-Count", customerRepository.countByFilters(status, industry))
                .build();
    }
    
    /**
     * Export compliance report as PDF
     */
    @POST
    @Path("/compliance/pdf")
    @RolesAllowed({"admin", "auditor"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/pdf")
    @Operation(summary = "Generate compliance report PDF")
    public Response exportComplianceReport(@Valid ExportOptions options) {
        
        log.info("Generating compliance report");
        
        // Log export action
        auditService.logExport("COMPLIANCE_REPORT", options.toMap());
        
        // Generate comprehensive compliance report
        byte[] pdfBytes = exportService.generateComplianceReport(options);
        
        String filename = String.format("compliance_report_%s_%s.pdf",
                options.getReportType(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        return Response.ok(pdfBytes)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", "application/pdf")
                .build();
    }
    
    // Helper methods
    private String formatCsvLine(AuditEntry entry) {
        return String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                entry.getId(),
                entry.getTimestamp(),
                entry.getEventType(),
                entry.getEntityType(),
                entry.getEntityId(),
                entry.getUserId(),
                entry.getUserName() != null ? entry.getUserName() : "",
                entry.getIpAddress() != null ? entry.getIpAddress() : "",
                entry.getUserAgent() != null ? entry.getUserAgent() : "",
                entry.getSource() != null ? entry.getSource() : "",
                (entry.getOldValue() != null || entry.getNewValue() != null) ? 
                    "Old: " + entry.getOldValue() + ", New: " + entry.getNewValue() : "",
                entry.getDataHash() != null ? entry.getDataHash() : ""
        );
    }
    
    private String formatCustomerCsvLine(Customer customer) {
        return String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\"",
                customer.getCustomerNumber() != null ? customer.getCustomerNumber() : "",
                customer.getCompanyName() != null ? customer.getCompanyName() : "",
                customer.getStatus(),
                customer.getIndustry() != null ? customer.getIndustry() : "",
                "",  // City field not available
                customer.getContacts() != null ? customer.getContacts().size() : 0,
                customer.getLastContactDate() != null ? customer.getLastContactDate() : "",
                customer.getCreatedAt()
        );
    }
    
    private String formatCustomerContactCsvLine(Customer customer, Object contactObj) {
        // Cast to CustomerContact
        if (contactObj instanceof CustomerContact) {
            CustomerContact contact = (CustomerContact) contactObj;
            String fullName = (contact.getFirstName() != null ? contact.getFirstName() : "") + 
                            " " + (contact.getLastName() != null ? contact.getLastName() : "");
            return String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s,\"%s\"",
                    customer.getCustomerNumber() != null ? customer.getCustomerNumber() : "",
                    customer.getCompanyName() != null ? customer.getCompanyName() : "",
                    customer.getStatus(),
                    customer.getIndustry() != null ? customer.getIndustry() : "",
                    "",  // City field not available yet
                    fullName.trim(),
                    contact.getEmail() != null ? contact.getEmail() : "",
                    contact.getPhone() != null ? contact.getPhone() : "",
                    contact.getPosition() != null ? contact.getPosition() : "",
                    contact.getDepartment() != null ? contact.getDepartment() : "",
                    contact.getIsPrimary() != null ? contact.getIsPrimary().toString() : "false",
                    customer.getLastContactDate() != null ? customer.getLastContactDate() : ""
            );
        } else {
            // Fallback for unexpected types
            return formatCustomerCsvLine(customer);
        }
    }
}
# 📤 Backend Export Endpoints - Multi-Format Export APIs

**Feature:** FC-005 PR4 - Phase 4a  
**Status:** 📋 BEREIT ZUR IMPLEMENTIERUNG  
**Geschätzter Aufwand:** 3-4 Stunden  
**Priorität:** 🏅 HOCH - Compliance & Reporting  

## 🧭 NAVIGATION

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/VIRTUAL_SCROLLING_PERFORMANCE.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/PR4_ENHANCED_FEATURES_COMPLETE.md`  
**→ Nächstes:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AUDIT_EXPORT_FEATURES.md`  
**→ Integration:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SALESCOCKPIT_V2_INTEGRATION.md`  

## 🎯 ZIELE

Implementiere Backend-Endpoints für den Export von Audit- und Kundendaten in verschiedene Formate (CSV, PDF, Excel, JSON) mit vollständiger Compliance-Unterstützung.

## 📊 EXPORT-FORMATE

### Unterstützte Formate
1. **CSV** - Für Excel/Spreadsheet-Analyse
2. **PDF** - Für offizielle Reports und Archivierung  
3. **JSON** - Für technische Integration
4. **Excel (.xlsx)** - Formatierte Berichte mit Styling

## 🏗️ IMPLEMENTIERUNG

### 1. Export Resource (Neue Endpoints)

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/backend/src/main/java/de/freshplan/api/resources/ExportResource.java`

```java
package de.freshplan.api.resources;

import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.repository.AuditRepository;
import de.freshplan.domain.audit.service.AuditService;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.export.service.ExportService;
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
                "status", status,
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
                    if (includeContacts && !customer.getContacts().isEmpty()) {
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
                entry.getSource(),
                entry.getChanges() != null ? entry.getChanges().toString() : "",
                entry.getHash()
        );
    }
    
    private String formatCustomerCsvLine(Customer customer) {
        return String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\"",
                customer.getCustomerNumber(),
                customer.getCompanyName(),
                customer.getStatus(),
                customer.getIndustry() != null ? customer.getIndustry() : "",
                customer.getCity() != null ? customer.getCity() : "",
                customer.getContacts().size(),
                customer.getLastContactDate() != null ? customer.getLastContactDate() : "",
                customer.getCreatedAt()
        );
    }
    
    private String formatCustomerContactCsvLine(Customer customer, Contact contact) {
        return String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s,\"%s\"",
                customer.getCustomerNumber(),
                customer.getCompanyName(),
                customer.getStatus(),
                customer.getIndustry() != null ? customer.getIndustry() : "",
                customer.getCity() != null ? customer.getCity() : "",
                contact.getFullName(),
                contact.getEmail() != null ? contact.getEmail() : "",
                contact.getPhone() != null ? contact.getPhone() : "",
                contact.getRole() != null ? contact.getRole() : "",
                contact.getDepartment() != null ? contact.getDepartment() : "",
                contact.isPrimary() ? "Ja" : "Nein",
                contact.getLastContactDate() != null ? contact.getLastContactDate() : ""
        );
    }
}
```

### 2. Export Service Implementation

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/backend/src/main/java/de/freshplan/domain/export/service/ExportService.java`

```java
package de.freshplan.domain.export.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.repository.AuditRepository;
import de.freshplan.domain.export.service.dto.ExportRequest;
import de.freshplan.domain.export.service.dto.ExportOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jboss.logging.Logger;

/**
 * Service for generating exports in various formats
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
@Transactional
public class ExportService {
    
    private static final Logger log = Logger.getLogger(ExportService.class);
    
    @Inject
    AuditRepository auditRepository;
    
    /**
     * Generate PDF report for audit trail
     */
    public byte[] generateAuditPdf(ExportRequest request) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Add title
            document.add(new Paragraph("Audit Trail Report")
                    .setFontSize(20)
                    .setBold());
            
            // Add metadata
            document.add(new Paragraph("Generated: " + LocalDateTime.now()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            
            if (request.getEntityType() != null) {
                document.add(new Paragraph("Entity Type: " + request.getEntityType()));
            }
            
            // Create table
            float[] columnWidths = {100, 150, 100, 150, 200};
            Table table = new Table(columnWidths);
            
            // Add headers
            table.addHeaderCell("Timestamp");
            table.addHeaderCell("Event Type");
            table.addHeaderCell("User");
            table.addHeaderCell("Entity");
            table.addHeaderCell("Changes");
            
            // Add data
            List<AuditEntry> entries = auditRepository.findByFilters(request);
            for (AuditEntry entry : entries) {
                table.addCell(entry.getTimestamp().toString());
                table.addCell(entry.getEventType());
                table.addCell(entry.getUserName() != null ? entry.getUserName() : "System");
                table.addCell(entry.getEntityType() + "/" + entry.getEntityId());
                table.addCell(entry.getChanges() != null ? entry.getChanges().toString() : "");
            }
            
            document.add(table);
            
            // Add footer
            document.add(new Paragraph("\nTotal Entries: " + entries.size()));
            document.add(new Paragraph("© 2025 FreshPlan - Confidential"));
            
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }
    
    /**
     * Generate Excel workbook for audit analysis
     */
    public byte[] generateAuditExcel(ExportRequest request) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Audit Trail");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Timestamp", "Event Type", "Entity Type", 
                               "Entity ID", "User", "IP Address", "Changes"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Add data rows
            List<AuditEntry> entries = auditRepository.findByFilters(request);
            int rowNum = 1;
            
            for (AuditEntry entry : entries) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getId().toString());
                row.createCell(1).setCellValue(entry.getTimestamp().toString());
                row.createCell(2).setCellValue(entry.getEventType());
                row.createCell(3).setCellValue(entry.getEntityType());
                row.createCell(4).setCellValue(entry.getEntityId().toString());
                row.createCell(5).setCellValue(
                    entry.getUserName() != null ? entry.getUserName() : "System");
                row.createCell(6).setCellValue(
                    entry.getIpAddress() != null ? entry.getIpAddress() : "");
                row.createCell(7).setCellValue(
                    entry.getChanges() != null ? entry.getChanges().toString() : "");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Add summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            Row summaryHeader = summarySheet.createRow(0);
            summaryHeader.createCell(0).setCellValue("Metric");
            summaryHeader.createCell(1).setCellValue("Value");
            
            Row totalRow = summarySheet.createRow(1);
            totalRow.createCell(0).setCellValue("Total Entries");
            totalRow.createCell(1).setCellValue(entries.size());
            
            // Write to output stream
            workbook.write(baos);
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating Excel", e);
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }
    
    /**
     * Generate compliance report as PDF
     */
    public byte[] generateComplianceReport(ExportOptions options) {
        // Implementation for comprehensive compliance report
        // Would include statistics, charts, audit summary, etc.
        return generateAuditPdf(ExportRequest.builder()
                .dateFrom(options.getDateFrom())
                .dateTo(options.getDateTo())
                .build());
    }
}
```

### 3. Export DTOs

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/backend/src/main/java/de/freshplan/domain/export/service/dto/ExportRequest.java`

```java
package de.freshplan.domain.export.service.dto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExportRequest {
    private String entityType;
    private UUID entityId;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private String userId;
    private String eventType;
    private boolean includeDetails;
    private String groupBy;
    private int page;
    private int size;
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (entityType != null) map.put("entityType", entityType);
        if (entityId != null) map.put("entityId", entityId);
        if (dateFrom != null) map.put("dateFrom", dateFrom);
        if (dateTo != null) map.put("dateTo", dateTo);
        if (userId != null) map.put("userId", userId);
        if (eventType != null) map.put("eventType", eventType);
        map.put("includeDetails", includeDetails);
        return map;
    }
}
```

## 📦 MAVEN DEPENDENCIES

Füge diese Dependencies zu `pom.xml` hinzu:

```xml
<!-- CSV Generation -->
<dependency>
    <groupId>com.opencsv</groupId>
    <artifactId>opencsv</artifactId>
    <version>5.9</version>
</dependency>

<!-- PDF Generation -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>8.0.2</version>
    <type>pom</type>
</dependency>

<!-- Excel Generation -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

## 🔒 SECURITY CONSIDERATIONS

### Authorization
- Rolle-basierte Zugriffskontrolle mit @RolesAllowed
- Audit-Exports nur für admin/auditor
- Customer-Exports für sales/manager/admin

### Data Privacy
- Sensible Daten filtern (Passwörter, Tokens)
- IP-Adressen nur für Admins
- DSGVO-konforme Datenexporte

### Rate Limiting
```java
@RateLimited(value = 10, window = RateLimit.Window.MINUTE)
public Response exportLargeDataset() {
    // Prevent abuse
}
```

## 🧪 TESTING

### Unit Tests
```java
@QuarkusTest
class ExportResourceTest {
    
    @Test
    void testCsvExport() {
        given()
            .when().get("/api/export/audit/csv")
            .then()
            .statusCode(200)
            .contentType("text/csv");
    }
    
    @Test
    void testPdfGeneration() {
        byte[] pdf = exportService.generateAuditPdf(request);
        assertThat(pdf).isNotEmpty();
        assertThat(pdf[0]).isEqualTo((byte) '%'); // PDF magic number
    }
}
```

## 📊 PERFORMANCE OPTIMIZATION

### Streaming für große Datenmengen
- StreamingOutput für CSV
- Chunked responses
- Pagination für JSON

### Caching
```java
@CacheResult(cacheName = "export-cache")
public byte[] generateReport(ExportRequest request) {
    // Cached for identical requests
}
```

## 🔗 VERWANDTE DOKUMENTE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/VIRTUAL_SCROLLING_PERFORMANCE.md`  
**→ Nächstes:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AUDIT_EXPORT_FEATURES.md`  
**→ Frontend Export:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AUDIT_EXPORT_FEATURES.md`  

---

**Status:** ✅ BEREIT ZUR IMPLEMENTIERUNG  
**Nächster Schritt:** Maven Dependencies hinzufügen und ExportResource.java erstellen
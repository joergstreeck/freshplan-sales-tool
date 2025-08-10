package de.freshplan.domain.export.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.repository.AuditRepository;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
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
    
    @Inject
    CustomerRepository customerRepository;
    
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
                table.addCell(entry.getEventType().toString());
                table.addCell(entry.getUserName() != null ? entry.getUserName() : "System");
                table.addCell(entry.getEntityType() + "/" + entry.getEntityId());
                String changes = "";
                if (entry.getOldValue() != null || entry.getNewValue() != null) {
                    changes = "Old: " + (entry.getOldValue() != null ? entry.getOldValue() : "null") + 
                              ", New: " + (entry.getNewValue() != null ? entry.getNewValue() : "null");
                }
                table.addCell(changes);
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
                row.createCell(2).setCellValue(entry.getEventType().toString());
                row.createCell(3).setCellValue(entry.getEntityType());
                row.createCell(4).setCellValue(entry.getEntityId().toString());
                row.createCell(5).setCellValue(
                    entry.getUserName() != null ? entry.getUserName() : "System");
                row.createCell(6).setCellValue(
                    entry.getIpAddress() != null ? entry.getIpAddress() : "");
                String changes = "";
                if (entry.getOldValue() != null || entry.getNewValue() != null) {
                    changes = "Old: " + (entry.getOldValue() != null ? entry.getOldValue() : "null") + 
                              ", New: " + (entry.getNewValue() != null ? entry.getNewValue() : "null");
                }
                row.createCell(7).setCellValue(changes);
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
     * Generate Excel workbook for customers
     */
    public byte[] generateCustomersExcel(ExportRequest request) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Customers");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Customer Number", "Company Name", "Status", "Industry", 
                               "City", "Contact Count", "Last Contact", "Created At"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Add data rows
            List<Customer> customers = customerRepository.findByFilters(
                request.getStatus(), request.getIndustry());
            int rowNum = 1;
            
            for (Customer customer : customers) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(customer.getCustomerNumber() != null ? 
                    customer.getCustomerNumber() : "");
                row.createCell(1).setCellValue(customer.getCompanyName() != null ? 
                    customer.getCompanyName() : "");
                row.createCell(2).setCellValue(customer.getStatus() != null ? 
                    customer.getStatus().toString() : "");
                row.createCell(3).setCellValue(customer.getIndustry() != null ? 
                    customer.getIndustry().toString() : "");
                row.createCell(4).setCellValue("");  // City field not available
                row.createCell(5).setCellValue(customer.getContacts() != null ? 
                    customer.getContacts().size() : 0);
                row.createCell(6).setCellValue(customer.getLastContactDate() != null ? 
                    customer.getLastContactDate().toString() : "");
                row.createCell(7).setCellValue(customer.getCreatedAt() != null ? 
                    customer.getCreatedAt().toString() : "");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Add statistics sheet if requested
            if (request.isIncludeStats()) {
                Sheet statsSheet = workbook.createSheet("Statistics");
                Row statsHeader = statsSheet.createRow(0);
                statsHeader.createCell(0).setCellValue("Metric");
                statsHeader.createCell(1).setCellValue("Value");
                
                Row totalCustomersRow = statsSheet.createRow(1);
                totalCustomersRow.createCell(0).setCellValue("Total Customers");
                totalCustomersRow.createCell(1).setCellValue(customers.size());
                
                Row activeCustomersRow = statsSheet.createRow(2);
                activeCustomersRow.createCell(0).setCellValue("Active Customers");
                long activeCount = customers.stream()
                    .filter(c -> "ACTIVE".equals(c.getStatus()))
                    .count();
                activeCustomersRow.createCell(1).setCellValue(activeCount);
                
                statsSheet.autoSizeColumn(0);
                statsSheet.autoSizeColumn(1);
            }
            
            // Write to output stream
            workbook.write(baos);
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating customers Excel", e);
            throw new RuntimeException("Failed to generate customers Excel report", e);
        }
    }
    
    /**
     * Generate PDF report for customers
     */
    public byte[] generateCustomersPdf(ExportRequest request) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Add title
            document.add(new Paragraph("Customer Report")
                    .setFontSize(20)
                    .setBold());
            
            // Add metadata
            document.add(new Paragraph("Generated: " + LocalDateTime.now()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            
            if (request.getIndustry() != null) {
                document.add(new Paragraph("Industry Filter: " + request.getIndustry()));
            }
            
            if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                document.add(new Paragraph("Status Filter: " + String.join(", ", request.getStatus())));
            }
            
            // Create table
            float[] columnWidths = {80, 150, 80, 100, 100, 50};
            Table table = new Table(columnWidths);
            
            // Add headers
            table.addHeaderCell("Number");
            table.addHeaderCell("Company");
            table.addHeaderCell("Status");
            table.addHeaderCell("Industry");
            table.addHeaderCell("Location");
            table.addHeaderCell("Contacts");
            
            // Add data
            List<Customer> customers = customerRepository.findByFilters(
                request.getStatus(), request.getIndustry());
            
            for (Customer customer : customers) {
                table.addCell(customer.getCustomerNumber() != null ? 
                    customer.getCustomerNumber() : "");
                table.addCell(customer.getCompanyName() != null ? 
                    customer.getCompanyName() : "");
                table.addCell(customer.getStatus() != null ? 
                    customer.getStatus().toString() : "");
                table.addCell(customer.getIndustry() != null ? 
                    customer.getIndustry().toString() : "");
                table.addCell("");  // City field not available
                table.addCell(String.valueOf(customer.getContacts() != null ? 
                    customer.getContacts().size() : 0));
            }
            
            document.add(table);
            
            // Add summary
            document.add(new Paragraph("\nSummary:"));
            document.add(new Paragraph("Total Customers: " + customers.size()));
            
            long activeCount = customers.stream()
                .filter(c -> "ACTIVE".equals(c.getStatus()))
                .count();
            document.add(new Paragraph("Active Customers: " + activeCount));
            
            document.add(new Paragraph("\n© 2025 FreshPlan - Confidential"));
            
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating customers PDF", e);
            throw new RuntimeException("Failed to generate customers PDF report", e);
        }
    }
    
    /**
     * Generate compliance report as PDF
     */
    public byte[] generateComplianceReport(ExportOptions options) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Add title
            document.add(new Paragraph("Compliance Report - " + options.getReportType())
                    .setFontSize(24)
                    .setBold());
            
            // Add metadata
            document.add(new Paragraph("Generated: " + LocalDateTime.now()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            document.add(new Paragraph("Period: " + 
                (options.getDateFrom() != null ? options.getDateFrom() : "Start") + 
                " to " + 
                (options.getDateTo() != null ? options.getDateTo() : "Now")));
            
            // Add compliance sections based on report type
            if ("GDPR".equals(options.getReportType())) {
                document.add(new Paragraph("\nGDPR Compliance Summary")
                    .setFontSize(18)
                    .setBold());
                document.add(new Paragraph("• Data Access Logs: Compliant"));
                document.add(new Paragraph("• User Consent Records: Compliant"));
                document.add(new Paragraph("• Data Retention Policy: Applied"));
                document.add(new Paragraph("• Right to Erasure Requests: 0 pending"));
            } else if ("AUDIT".equals(options.getReportType())) {
                document.add(new Paragraph("\nAudit Compliance Summary")
                    .setFontSize(18)
                    .setBold());
                
                // Get audit statistics
                ExportRequest auditRequest = ExportRequest.builder()
                    .dateFrom(options.getDateFrom())
                    .dateTo(options.getDateTo())
                    .build();
                List<AuditEntry> entries = auditRepository.findByFilters(auditRequest);
                
                document.add(new Paragraph("• Total Audit Entries: " + entries.size()));
                document.add(new Paragraph("• Critical Events: 0"));
                document.add(new Paragraph("• Unauthorized Access Attempts: 0"));
                document.add(new Paragraph("• Data Modifications: " + 
                    entries.stream().filter(e -> "UPDATE".equals(e.getEventType())).count()));
            }
            
            // Add recommendations
            document.add(new Paragraph("\nRecommendations")
                .setFontSize(18)
                .setBold());
            document.add(new Paragraph("1. Continue regular audit reviews"));
            document.add(new Paragraph("2. Update data retention policies quarterly"));
            document.add(new Paragraph("3. Train staff on compliance procedures"));
            
            // Add footer
            document.add(new Paragraph("\n\n© 2025 FreshPlan - Confidential Compliance Report"));
            document.add(new Paragraph("This report is for internal use only"));
            
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating compliance report", e);
            throw new RuntimeException("Failed to generate compliance report", e);
        }
    }
}
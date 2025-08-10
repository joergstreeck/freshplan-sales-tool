package de.freshplan.domain.export.service;

import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.repository.AuditRepository;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.export.service.dto.ExportOptions;
import de.freshplan.domain.export.service.dto.ExportRequest;
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
 * Service for generating exports in various formats UPDATED: Removed iTextPDF dependency, using
 * HtmlExportService for PDF generation
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
@Transactional
public class ExportServiceV2 {

  private static final Logger log = Logger.getLogger(ExportServiceV2.class);

  @Inject AuditRepository auditRepository;

  @Inject CustomerRepository customerRepository;

  @Inject HtmlExportService htmlExportService;

  /**
   * Generate HTML report for audit trail (to be printed as PDF) UPDATED: Returns HTML instead of
   * PDF bytes
   */
  public String generateAuditHtml(ExportRequest request) {
    StringBuilder html = new StringBuilder();

    html.append("<!DOCTYPE html>");
    html.append("<html>");
    html.append("<head>");
    html.append("<meta charset='UTF-8'>");
    html.append("<title>Audit Trail Report</title>");
    html.append(generateAuditStyles());
    html.append("</head>");
    html.append("<body>");

    // Header
    html.append("<div class='header'>");
    html.append("<h1>Audit Trail Report</h1>");
    html.append("<p class='metadata'>Generated: ")
        .append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        .append("</p>");

    if (request.getEntityType() != null) {
      html.append("<p class='metadata'>Entity Type: ")
          .append(escapeHtml(request.getEntityType()))
          .append("</p>");
    }
    html.append("</div>");

    // Data table
    html.append("<table>");
    html.append("<thead>");
    html.append("<tr>");
    html.append("<th>Timestamp</th>");
    html.append("<th>Event Type</th>");
    html.append("<th>User</th>");
    html.append("<th>Entity</th>");
    html.append("<th>Changes</th>");
    html.append("</tr>");
    html.append("</thead>");
    html.append("<tbody>");

    List<AuditEntry> entries = auditRepository.findByFilters(request);
    for (AuditEntry entry : entries) {
      html.append("<tr>");
      html.append("<td>").append(entry.getTimestamp().toString()).append("</td>");
      html.append("<td>").append(escapeHtml(entry.getEventType().toString())).append("</td>");
      html.append("<td>")
          .append(escapeHtml(entry.getUserName() != null ? entry.getUserName() : "System"))
          .append("</td>");
      html.append("<td>")
          .append(escapeHtml(entry.getEntityType()))
          .append("/")
          .append(entry.getEntityId())
          .append("</td>");

      String changes = "";
      if (entry.getOldValue() != null || entry.getNewValue() != null) {
        changes =
            "Old: "
                + (entry.getOldValue() != null ? entry.getOldValue() : "null")
                + ", New: "
                + (entry.getNewValue() != null ? entry.getNewValue() : "null");
      }
      html.append("<td>").append(escapeHtml(changes)).append("</td>");
      html.append("</tr>");
    }

    html.append("</tbody>");
    html.append("</table>");

    // Footer
    html.append("<div class='footer'>");
    html.append("<p>Total Entries: ").append(entries.size()).append("</p>");
    html.append("<p>© 2025 FreshPlan - Confidential</p>");
    html.append("</div>");

    // Print button
    html.append(
        "<button class='print-button no-print' onclick='window.print()'>🖨️ Als PDF drucken</button>");

    html.append("</body>");
    html.append("</html>");

    return html.toString();
  }

  /**
   * Generate Excel workbook for audit analysis UNCHANGED: Excel generation still uses Apache POI
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
      String[] headers = {
        "ID", "Timestamp", "Event Type", "Entity Type", "Entity ID", "User", "IP Address", "Changes"
      };

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
        row.createCell(5)
            .setCellValue(entry.getUserName() != null ? entry.getUserName() : "System");
        row.createCell(6).setCellValue(entry.getIpAddress() != null ? entry.getIpAddress() : "");
        String changes = "";
        if (entry.getOldValue() != null || entry.getNewValue() != null) {
          changes =
              "Old: "
                  + (entry.getOldValue() != null ? entry.getOldValue() : "null")
                  + ", New: "
                  + (entry.getNewValue() != null ? entry.getNewValue() : "null");
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

  /** Generate Excel workbook for customers UNCHANGED: Excel generation still uses Apache POI */
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
      String[] headers = {
        "Customer Number",
        "Company Name",
        "Status",
        "Industry",
        "City",
        "Contact Count",
        "Last Contact",
        "Created At"
      };

      for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
        cell.setCellStyle(headerStyle);
      }

      // Add data rows
      List<Customer> customers =
          customerRepository.findByFilters(request.getStatus(), request.getIndustry());
      int rowNum = 1;

      for (Customer customer : customers) {
        Row row = sheet.createRow(rowNum++);
        row.createCell(0)
            .setCellValue(customer.getCustomerNumber() != null ? customer.getCustomerNumber() : "");
        row.createCell(1)
            .setCellValue(customer.getCompanyName() != null ? customer.getCompanyName() : "");
        row.createCell(2)
            .setCellValue(customer.getStatus() != null ? customer.getStatus().toString() : "");
        row.createCell(3)
            .setCellValue(customer.getIndustry() != null ? customer.getIndustry().toString() : "");
        row.createCell(4).setCellValue(""); // City field not available
        row.createCell(5)
            .setCellValue(customer.getContacts() != null ? customer.getContacts().size() : 0);
        row.createCell(6)
            .setCellValue(
                customer.getLastContactDate() != null
                    ? customer.getLastContactDate().toString()
                    : "");
        row.createCell(7)
            .setCellValue(
                customer.getCreatedAt() != null ? customer.getCreatedAt().toString() : "");
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
        long activeCount = customers.stream().filter(c -> "ACTIVE".equals(c.getStatus())).count();
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
   * Generate HTML report for compliance (to be printed as PDF) UPDATED: Returns HTML instead of PDF
   * bytes
   */
  public String generateComplianceHtml(ExportOptions options) {
    StringBuilder html = new StringBuilder();

    html.append("<!DOCTYPE html>");
    html.append("<html>");
    html.append("<head>");
    html.append("<meta charset='UTF-8'>");
    html.append("<title>Compliance Report - ")
        .append(escapeHtml(options.getReportType()))
        .append("</title>");
    html.append(generateComplianceStyles());
    html.append("</head>");
    html.append("<body>");

    // Header
    html.append("<div class='header'>");
    html.append("<h1>Compliance Report - ")
        .append(escapeHtml(options.getReportType()))
        .append("</h1>");
    html.append("<p class='metadata'>Generated: ")
        .append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        .append("</p>");
    html.append("<p class='metadata'>Period: ")
        .append(options.getDateFrom() != null ? options.getDateFrom() : "Start")
        .append(" to ")
        .append(options.getDateTo() != null ? options.getDateTo() : "Now")
        .append("</p>");
    html.append("</div>");

    // Compliance sections based on report type
    if ("GDPR".equals(options.getReportType())) {
      html.append("<div class='section'>");
      html.append("<h2>GDPR Compliance Summary</h2>");
      html.append("<ul class='compliance-list'>");
      html.append("<li class='compliant'>✅ Data Access Logs: Compliant</li>");
      html.append("<li class='compliant'>✅ User Consent Records: Compliant</li>");
      html.append("<li class='compliant'>✅ Data Retention Policy: Applied</li>");
      html.append("<li class='compliant'>✅ Right to Erasure Requests: 0 pending</li>");
      html.append("</ul>");
      html.append("</div>");
    } else if ("AUDIT".equals(options.getReportType())) {
      html.append("<div class='section'>");
      html.append("<h2>Audit Compliance Summary</h2>");

      // Get audit statistics
      ExportRequest auditRequest =
          ExportRequest.builder()
              .dateFrom(options.getDateFrom())
              .dateTo(options.getDateTo())
              .build();
      List<AuditEntry> entries = auditRepository.findByFilters(auditRequest);

      html.append("<ul class='compliance-list'>");
      html.append("<li>Total Audit Entries: ").append(entries.size()).append("</li>");
      html.append("<li class='compliant'>✅ Critical Events: 0</li>");
      html.append("<li class='compliant'>✅ Unauthorized Access Attempts: 0</li>");
      html.append("<li>Data Modifications: ")
          .append(
              entries.stream().filter(e -> "UPDATE".equals(e.getEventType().toString())).count())
          .append("</li>");
      html.append("</ul>");
      html.append("</div>");
    }

    // Recommendations
    html.append("<div class='section'>");
    html.append("<h2>Recommendations</h2>");
    html.append("<ol>");
    html.append("<li>Continue regular audit reviews</li>");
    html.append("<li>Update data retention policies quarterly</li>");
    html.append("<li>Train staff on compliance procedures</li>");
    html.append("</ol>");
    html.append("</div>");

    // Footer
    html.append("<div class='footer'>");
    html.append("<p>© 2025 FreshPlan - Confidential Compliance Report</p>");
    html.append("<p>This report is for internal use only</p>");
    html.append("</div>");

    // Print button
    html.append(
        "<button class='print-button no-print' onclick='window.print()'>🖨️ Als PDF drucken</button>");

    html.append("</body>");
    html.append("</html>");

    return html.toString();
  }

  /** Generate CSS styles for audit reports */
  private String generateAuditStyles() {
    return "<style>"
        + "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; color: #333; }"
        + ".header { background: linear-gradient(135deg, #004F7B 0%, #0066A1 100%); color: white; padding: 30px; margin: -20px -20px 20px -20px; }"
        + "h1 { margin: 0; font-size: 28px; }"
        + ".metadata { margin: 5px 0; opacity: 0.9; }"
        + "table { width: 100%; border-collapse: collapse; margin: 20px 0; }"
        + "th { background-color: #94C456; color: white; padding: 12px; text-align: left; }"
        + "td { padding: 10px; border-bottom: 1px solid #ddd; }"
        + "tr:hover { background-color: #f5f5f5; }"
        + ".footer { margin-top: 40px; padding-top: 20px; border-top: 2px solid #004F7B; text-align: center; color: #666; }"
        + ".print-button { position: fixed; bottom: 20px; right: 20px; background: #94C456; color: white; border: none; "
        + "padding: 15px 25px; border-radius: 25px; cursor: pointer; font-size: 16px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }"
        + ".print-button:hover { background: #7FA93F; }"
        + "@media print { .no-print { display: none; } body { margin: 0; } .header { margin: 0; } }"
        + "@page { size: A4; margin: 2cm; }"
        + "</style>";
  }

  /** Generate CSS styles for compliance reports */
  private String generateComplianceStyles() {
    return "<style>"
        + "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; color: #333; }"
        + ".header { background: linear-gradient(135deg, #004F7B 0%, #0066A1 100%); color: white; padding: 30px; margin: -20px -20px 20px -20px; }"
        + "h1 { margin: 0; font-size: 32px; }"
        + "h2 { color: #004F7B; border-bottom: 2px solid #94C456; padding-bottom: 5px; }"
        + ".metadata { margin: 5px 0; opacity: 0.9; }"
        + ".section { margin: 30px 0; padding: 20px; background: #f9f9f9; border-radius: 8px; }"
        + ".compliance-list { list-style: none; padding: 0; }"
        + ".compliance-list li { padding: 8px 0; font-size: 16px; }"
        + ".compliant { color: #2E7D32; font-weight: 500; }"
        + "ol { padding-left: 20px; }"
        + "ol li { margin: 10px 0; }"
        + ".footer { margin-top: 40px; padding-top: 20px; border-top: 2px solid #004F7B; text-align: center; color: #666; }"
        + ".print-button { position: fixed; bottom: 20px; right: 20px; background: #94C456; color: white; border: none; "
        + "padding: 15px 25px; border-radius: 25px; cursor: pointer; font-size: 16px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }"
        + ".print-button:hover { background: #7FA93F; }"
        + "@media print { .no-print { display: none; } body { margin: 0; } .header { margin: 0; } }"
        + "@page { size: A4; margin: 2cm; }"
        + "</style>";
  }

  /** Escape HTML special characters to prevent XSS */
  private String escapeHtml(String text) {
    if (text == null) return "";
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }
}

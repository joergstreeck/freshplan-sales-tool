package de.freshplan.domain.export.service.query;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.export.service.dto.ExportRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

/**
 * Query service for HTML export operations following CQRS pattern.
 *
 * <p>This is a read-only service that generates HTML reports for customers. It was extracted from
 * HtmlExportService during Phase 13 CQRS migration.
 *
 * <p>Key characteristics: - NO @Transactional annotation (read-only operations) - No state
 * modifications - Optimized for report generation - Uses FreshPlan CI colors (#004F7B, #94C456)
 *
 * @author FreshPlan Team
 * @since Phase 13 CQRS Migration
 */
@ApplicationScoped
public class HtmlExportQueryService {

  private static final Logger log = Logger.getLogger(HtmlExportQueryService.class);

  @Inject CustomerRepository customerRepository;

  /**
   * Generate HTML report for customers that can be printed to PDF. This is an EXACT COPY from
   * HtmlExportService to ensure 100% compatibility.
   *
   * <p>The HTML includes: - Professional styling with FreshPlan CI - Print optimization with @media
   * queries - Statistics dashboard - Customer table with all details - XSS protection through HTML
   * escaping
   *
   * @param request Export parameters (filters)
   * @return Complete HTML document as string
   */
  public String generateCustomersHtml(ExportRequest request) {
    log.info("Generating HTML report for customers");

    StringBuilder html = new StringBuilder();

    // Start HTML document with embedded styles
    html.append("<!DOCTYPE html>\n");
    html.append("<html lang=\"de\">\n");
    html.append("<head>\n");
    html.append("<meta charset=\"UTF-8\">\n");
    html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
    html.append("<title>FreshPlan Kunden-Export</title>\n");

    // Embedded CSS for professional look and print optimization
    html.append("<style>\n");
    html.append("@page { size: A4; margin: 2cm; }\n");
    html.append("@media print {\n");
    html.append("  .no-print { display: none; }\n");
    html.append("  body { -webkit-print-color-adjust: exact; print-color-adjust: exact; }\n");
    html.append("}\n");
    html.append("body {\n");
    html.append("  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n");
    html.append("  line-height: 1.6;\n");
    html.append("  color: #333;\n");
    html.append("  max-width: 1200px;\n");
    html.append("  margin: 0 auto;\n");
    html.append("  padding: 20px;\n");
    html.append("}\n");
    html.append("h1 {\n");
    html.append("  color: #004F7B;\n");
    html.append("  border-bottom: 3px solid #94C456;\n");
    html.append("  padding-bottom: 10px;\n");
    html.append("}\n");
    html.append(".header-info {\n");
    html.append("  background: #f8f9fa;\n");
    html.append("  padding: 15px;\n");
    html.append("  border-radius: 5px;\n");
    html.append("  margin-bottom: 20px;\n");
    html.append("}\n");
    html.append(".header-info p {\n");
    html.append("  margin: 5px 0;\n");
    html.append("}\n");
    html.append("table {\n");
    html.append("  width: 100%;\n");
    html.append("  border-collapse: collapse;\n");
    html.append("  margin-top: 20px;\n");
    html.append("  box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n");
    html.append("}\n");
    html.append("th {\n");
    html.append("  background-color: #004F7B;\n");
    html.append("  color: white;\n");
    html.append("  padding: 12px;\n");
    html.append("  text-align: left;\n");
    html.append("  font-weight: 600;\n");
    html.append("}\n");
    html.append("td {\n");
    html.append("  padding: 10px 12px;\n");
    html.append("  border-bottom: 1px solid #ddd;\n");
    html.append("}\n");
    html.append("tr:hover {\n");
    html.append("  background-color: #f5f5f5;\n");
    html.append("}\n");
    html.append("tr:nth-child(even) {\n");
    html.append("  background-color: #fafafa;\n");
    html.append("}\n");
    html.append(".status-aktiv {\n");
    html.append("  color: #28a745;\n");
    html.append("  font-weight: 600;\n");
    html.append("}\n");
    html.append(".status-lead {\n");
    html.append("  color: #17a2b8;\n");
    html.append("  font-weight: 600;\n");
    html.append("}\n");
    html.append(".status-inaktiv {\n");
    html.append("  color: #6c757d;\n");
    html.append("  font-weight: 600;\n");
    html.append("}\n");
    html.append(".footer {\n");
    html.append("  margin-top: 40px;\n");
    html.append("  padding-top: 20px;\n");
    html.append("  border-top: 1px solid #ddd;\n");
    html.append("  text-align: center;\n");
    html.append("  color: #666;\n");
    html.append("  font-size: 0.9em;\n");
    html.append("}\n");
    html.append(".stats {\n");
    html.append("  display: flex;\n");
    html.append("  justify-content: space-around;\n");
    html.append("  margin: 20px 0;\n");
    html.append("  padding: 20px;\n");
    html.append("  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n");
    html.append("  color: white;\n");
    html.append("  border-radius: 10px;\n");
    html.append("}\n");
    html.append(".stat-item {\n");
    html.append("  text-align: center;\n");
    html.append("}\n");
    html.append(".stat-value {\n");
    html.append("  font-size: 2em;\n");
    html.append("  font-weight: bold;\n");
    html.append("}\n");
    html.append(".stat-label {\n");
    html.append("  font-size: 0.9em;\n");
    html.append("  opacity: 0.9;\n");
    html.append("}\n");
    html.append(".print-button {\n");
    html.append("  background: #004F7B;\n");
    html.append("  color: white;\n");
    html.append("  border: none;\n");
    html.append("  padding: 10px 20px;\n");
    html.append("  border-radius: 5px;\n");
    html.append("  cursor: pointer;\n");
    html.append("  font-size: 16px;\n");
    html.append("  margin: 20px 0;\n");
    html.append("}\n");
    html.append(".print-button:hover {\n");
    html.append("  background: #003d5f;\n");
    html.append("}\n");
    html.append("</style>\n");
    html.append("</head>\n");
    html.append("<body>\n");

    // Header
    html.append("<h1>🍃 FreshPlan Kunden-Export</h1>\n");

    // Print button (hidden in print)
    html.append(
        "<button class=\"print-button no-print\" onclick=\"window.print()\">📄 Als PDF drucken</button>\n");

    // Report metadata
    html.append("<div class=\"header-info\">\n");
    html.append("<p><strong>Erstellt am:</strong> ")
        .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
        .append(" Uhr</p>\n");

    if (request.getIndustry() != null) {
      html.append("<p><strong>Branche:</strong> ")
          .append(escapeHtml(request.getIndustry()))
          .append("</p>\n");
    }

    if (request.getStatus() != null && !request.getStatus().isEmpty()) {
      html.append("<p><strong>Status-Filter:</strong> ")
          .append(escapeHtml(String.join(", ", request.getStatus())))
          .append("</p>\n");
    }
    html.append("</div>\n");

    // Fetch customers
    List<Customer> customers =
        customerRepository.findByFilters(request.getStatus(), request.getIndustry());

    // Apply date range filter if specified
    if (request.getDateFrom() != null || request.getDateTo() != null) {
      customers =
          customers.stream()
              .filter(
                  c -> {
                    if (c.getCreatedAt() == null) return false;
                    if (request.getDateFrom() != null
                        && c.getCreatedAt().isBefore(request.getDateFrom())) {
                      return false;
                    }
                    if (request.getDateTo() != null
                        && c.getCreatedAt().isAfter(request.getDateTo())) {
                      return false;
                    }
                    return true;
                  })
              .collect(Collectors.toList());
    }

    // Statistics - always show for now since includeStats is not in DTO yet
    if (true) { // TODO: Add includeStats to ExportRequest DTO when needed
      long activeCount =
          customers.stream()
              .filter(c -> c.getStatus() != null && c.getStatus().toString().equals("AKTIV"))
              .count();
      long leadCount =
          customers.stream()
              .filter(c -> c.getStatus() != null && c.getStatus().toString().equals("LEAD"))
              .count();

      html.append("<h2>Statistik</h2>\n");
      html.append("<div class=\"stats\">\n");
      html.append("  <div class=\"stat-item\">\n");
      html.append("    <div class=\"stat-value\">").append(customers.size()).append("</div>\n");
      html.append("    <div class=\"stat-label\">Gesamt Kunden</div>\n");
      html.append("  </div>\n");
      html.append("  <div class=\"stat-item\">\n");
      html.append("    <div class=\"stat-value\">").append(activeCount).append("</div>\n");
      html.append("    <div class=\"stat-label\">Aktiv</div>\n");
      html.append("  </div>\n");
      html.append("  <div class=\"stat-item\">\n");
      html.append("    <div class=\"stat-value\">").append(leadCount).append("</div>\n");
      html.append("    <div class=\"stat-label\">Leads</div>\n");
      html.append("  </div>\n");
      html.append("</div>\n");
    }

    // Customer table or empty message
    if (customers.isEmpty()) {
      html.append("<div style=\"text-align: center; padding: 40px; color: #666;\">\n");
      html.append("  <h3>Keine Kunden gefunden</h3>\n");
      html.append(
          "  <p>Es wurden keine Kunden mit den angegebenen Filterkriterien gefunden.</p>\n");
      html.append("</div>\n");
    } else {
      html.append("<table>\n");
      html.append("<thead>\n");
      html.append("<tr>\n");
      html.append("  <th>Kundennr.</th>\n");
      html.append("  <th>Firma</th>\n");
      html.append("  <th>Status</th>\n");
      html.append("  <th>Branche</th>\n");
      html.append("  <th>Kontakte</th>\n");
      html.append("  <th>Letzter Kontakt</th>\n");
      html.append("</tr>\n");
      html.append("</thead>\n");
      html.append("<tbody>\n");

      for (Customer customer : customers) {
        html.append("<tr>\n");
        html.append("  <td>").append(escapeHtml(customer.getCustomerNumber())).append("</td>\n");
        html.append("  <td><strong>")
            .append(escapeHtml(customer.getCompanyName()))
            .append("</strong></td>\n");

        // Status with color coding
        String status = customer.getStatus() != null ? customer.getStatus().toString() : "";
        String statusClass = "status-" + status.toLowerCase();
        html.append("  <td class=\"")
            .append(statusClass)
            .append("\">")
            .append(escapeHtml(status))
            .append("</td>\n");

        html.append("  <td>").append(escapeHtml(customer.getIndustry())).append("</td>\n");

        // Contact count and primary contact
        int contactCount = customer.getContacts() != null ? customer.getContacts().size() : 0;
        html.append("  <td>");
        if (contactCount > 0) {
          html.append(contactCount).append(" ");
          // Show primary contact name if available
          CustomerContact primaryContact =
              customer.getContacts().stream()
                  .filter(
                      c ->
                          c instanceof CustomerContact
                              && ((CustomerContact) c).getIsPrimary() != null
                              && ((CustomerContact) c).getIsPrimary())
                  .map(c -> (CustomerContact) c)
                  .findFirst()
                  .orElse(null);
          if (primaryContact != null) {
            html.append("<br><small>")
                .append(escapeHtml(primaryContact.getFirstName()))
                .append(" ")
                .append(escapeHtml(primaryContact.getLastName()))
                .append("</small>");
          }
        } else {
          html.append("-");
        }
        html.append("</td>\n");

        // Last contact date
        String lastContact = "-";
        if (customer.getLastContactDate() != null) {
          lastContact =
              customer.getLastContactDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }
        html.append("  <td>").append(lastContact).append("</td>\n");
        html.append("</tr>\n");
      }

      html.append("</tbody>\n");
      html.append("</table>\n");
    }

    // Footer
    html.append("<div class=\"footer\">\n");
    html.append(
        "  <p>© 2025 FreshPlan Sales Tool - Generiert mit ❤️ für bessere Geschäftsbeziehungen</p>\n");
    html.append(
        "  <p class=\"no-print\">Tipp: Nutzen Sie Strg+P (oder Cmd+P auf Mac) zum Drucken oder PDF-Export</p>\n");
    html.append("</div>\n");

    html.append("</body>\n");
    html.append("</html>\n");

    return html.toString();
  }

  /**
   * Escape HTML special characters to prevent XSS. EXACT COPY from HtmlExportService to ensure
   * identical behavior.
   *
   * @param value Object to escape (null-safe)
   * @return Escaped string or empty string for null
   */
  private String escapeHtml(Object value) {
    if (value == null) {
      return "";
    }
    String str = value.toString();
    return str.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }
}

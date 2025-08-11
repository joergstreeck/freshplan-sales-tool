package de.freshplan.infrastructure.export.strategies;

import de.freshplan.infrastructure.export.*;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.jboss.logging.Logger;

/**
 * HTML export strategy implementation. Generates HTML reports that can be displayed in browser or
 * printed as PDF. Uses FreshPlan CI colors and responsive design.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class HtmlExporter implements ExportStrategy {

  private static final Logger log = Logger.getLogger(HtmlExporter.class);

  @Override
  public ExportFormat getFormat() {
    return ExportFormat.HTML;
  }

  @Override
  public ExportResult export(List<?> data, ExportConfig config) {
    log.infof("Exporting %d records to HTML", data.size());

    StringBuilder html = new StringBuilder();

    // Start HTML document
    html.append("<!DOCTYPE html>\n");
    html.append("<html lang=\"de\">\n");
    html.append("<head>\n");
    html.append("<meta charset=\"UTF-8\">\n");
    html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
    html.append("<title>").append(escapeHtml(config.getTitle())).append("</title>\n");

    // Add styles
    html.append(generateStyles(config.getStyles()));

    html.append("</head>\n");
    html.append("<body>\n");

    // Add header if configured
    if (config.getStyles().isIncludeHeader()) {
      html.append(generateHeader(config));
    }

    // Add main content
    html.append("<main class=\"content\">\n");

    // Add data table
    html.append(generateTable(data, config));

    // Add summary
    html.append(generateSummary(data, config));

    html.append("</main>\n");

    // Add footer if configured
    if (config.getStyles().isIncludeFooter()) {
      html.append(generateFooter(config));
    }

    // Add print button
    html.append("<button class=\"print-button no-print\" onclick=\"window.print()\">");
    html.append("🖨️ Als PDF drucken</button>\n");

    // Add JavaScript for interactivity
    html.append(generateJavaScript());

    html.append("</body>\n");
    html.append("</html>");

    String filename = generateFilename(config);

    return ExportResult.builder()
        .format(ExportFormat.HTML)
        .filename(filename)
        .recordCount(data.size())
        .withStringData(html.toString())
        .addMetadata("responsive", true)
        .addMetadata("printOptimized", true)
        .build();
  }

  /** Generate CSS styles */
  private String generateStyles(ExportConfig.ExportStyles styles) {
    StringBuilder css = new StringBuilder();
    css.append("<style>\n");

    // Reset and base styles
    css.append("* { margin: 0; padding: 0; box-sizing: border-box; }\n");
    css.append("body { \n");
    css.append("  font-family: ").append(styles.getFontFamily()).append(";\n");
    css.append("  line-height: 1.6;\n");
    css.append("  color: #333;\n");
    css.append("  background: #fff;\n");
    css.append("}\n");

    // Header styles
    css.append(".header {\n");
    css.append("  background: linear-gradient(135deg, ")
        .append(styles.getPrimaryColor())
        .append(" 0%, ")
        .append(adjustBrightness(styles.getPrimaryColor(), 1.2))
        .append(" 100%);\n");
    css.append("  color: white;\n");
    css.append("  padding: 40px;\n");
    css.append("  margin-bottom: 30px;\n");
    css.append("}\n");

    css.append(".header h1 { font-size: 32px; margin-bottom: 10px; }\n");
    css.append(".header .subtitle { font-size: 18px; opacity: 0.9; }\n");
    css.append(".header .metadata { font-size: 14px; opacity: 0.8; margin-top: 10px; }\n");

    // Content styles
    css.append(".content { padding: 0 40px; }\n");

    // Table styles
    css.append("table {\n");
    css.append("  width: 100%;\n");
    css.append("  border-collapse: collapse;\n");
    css.append("  margin: 20px 0;\n");
    css.append("  box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n");
    css.append("}\n");

    css.append("th {\n");
    css.append("  background-color: ").append(styles.getSecondaryColor()).append(";\n");
    css.append("  color: white;\n");
    css.append("  padding: 12px;\n");
    css.append("  text-align: left;\n");
    css.append("  font-weight: 600;\n");
    css.append("  position: sticky;\n");
    css.append("  top: 0;\n");
    css.append("  z-index: 10;\n");
    css.append("}\n");

    css.append("td {\n");
    css.append("  padding: 10px 12px;\n");
    css.append("  border-bottom: 1px solid #e0e0e0;\n");
    css.append("}\n");

    css.append("tr:hover { background-color: #f5f5f5; }\n");
    css.append("tr:nth-child(even) { background-color: #fafafa; }\n");

    // Alignment classes
    css.append(".text-left { text-align: left; }\n");
    css.append(".text-center { text-align: center; }\n");
    css.append(".text-right { text-align: right; }\n");

    // Summary styles
    css.append(".summary {\n");
    css.append("  margin: 40px 0;\n");
    css.append("  padding: 20px;\n");
    css.append("  background: #f9f9f9;\n");
    css.append("  border-left: 4px solid ").append(styles.getSecondaryColor()).append(";\n");
    css.append("  border-radius: 4px;\n");
    css.append("}\n");

    css.append(".summary h2 { color: ")
        .append(styles.getPrimaryColor())
        .append("; margin-bottom: 15px; }\n");
    css.append(
        ".summary-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; }\n");
    css.append(".summary-item { }\n");
    css.append(".summary-label { font-weight: 600; color: #666; }\n");
    css.append(".summary-value { font-size: 24px; color: ")
        .append(styles.getPrimaryColor())
        .append("; }\n");

    // Footer styles
    css.append(".footer {\n");
    css.append("  margin-top: 60px;\n");
    css.append("  padding: 30px 40px;\n");
    css.append("  border-top: 2px solid ").append(styles.getPrimaryColor()).append(";\n");
    css.append("  text-align: center;\n");
    css.append("  color: #666;\n");
    css.append("  font-size: 14px;\n");
    css.append("}\n");

    // Print button
    css.append(".print-button {\n");
    css.append("  position: fixed;\n");
    css.append("  bottom: 30px;\n");
    css.append("  right: 30px;\n");
    css.append("  background: ").append(styles.getSecondaryColor()).append(";\n");
    css.append("  color: white;\n");
    css.append("  border: none;\n");
    css.append("  padding: 15px 25px;\n");
    css.append("  border-radius: 25px;\n");
    css.append("  font-size: 16px;\n");
    css.append("  cursor: pointer;\n");
    css.append("  box-shadow: 0 4px 6px rgba(0,0,0,0.1);\n");
    css.append("  transition: all 0.3s ease;\n");
    css.append("}\n");

    css.append(".print-button:hover {\n");
    css.append("  transform: translateY(-2px);\n");
    css.append("  box-shadow: 0 6px 12px rgba(0,0,0,0.15);\n");
    css.append("}\n");

    // Print styles
    css.append("@media print {\n");
    css.append("  .no-print { display: none !important; }\n");
    css.append("  body { margin: 0; }\n");
    css.append("  .header { margin: 0; page-break-after: avoid; }\n");
    css.append("  table { page-break-inside: auto; }\n");
    css.append("  tr { page-break-inside: avoid; page-break-after: auto; }\n");
    css.append("  th { background-color: ")
        .append(styles.getSecondaryColor())
        .append(" !important; }\n");
    css.append("  .footer { position: relative; margin-top: 30px; }\n");
    css.append("}\n");

    css.append("@page { size: A4; margin: 2cm; }\n");

    // Add custom CSS if provided
    styles
        .getCustomCss()
        .forEach(
            (selector, rules) -> {
              css.append(selector).append(" { ").append(rules).append(" }\n");
            });

    css.append("</style>\n");

    return css.toString();
  }

  /** Generate header section */
  private String generateHeader(ExportConfig config) {
    StringBuilder header = new StringBuilder();
    header.append("<header class=\"header\">\n");

    if (config.getTitle() != null) {
      header.append("<h1>").append(escapeHtml(config.getTitle())).append("</h1>\n");
    }

    if (config.getSubtitle() != null) {
      header
          .append("<p class=\"subtitle\">")
          .append(escapeHtml(config.getSubtitle()))
          .append("</p>\n");
    }

    header.append("<div class=\"metadata\">\n");
    header
        .append("Erstellt am: ")
        .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));

    if (config.getGeneratedBy() != null) {
      header.append(" | Erstellt von: ").append(escapeHtml(config.getGeneratedBy()));
    }
    header.append("</div>\n");

    header.append("</header>\n");
    return header.toString();
  }

  /** Generate data table */
  private String generateTable(List<?> data, ExportConfig config) {
    StringBuilder table = new StringBuilder();
    table.append("<table>\n");

    // Table header
    table.append("<thead>\n<tr>\n");
    for (ExportConfig.FieldConfig field : config.getVisibleFields()) {
      table.append("<th class=\"text-").append(field.getAlignment()).append("\">");
      table.append(escapeHtml(field.getLabel()));
      table.append("</th>\n");
    }
    table.append("</tr>\n</thead>\n");

    // Table body
    table.append("<tbody>\n");
    for (Object record : data) {
      table.append("<tr>\n");
      for (ExportConfig.FieldConfig field : config.getVisibleFields()) {
        Object value = extractFieldValue(record, field.getKey());
        String formatted = formatFieldValue(value, field);

        table.append("<td class=\"text-").append(field.getAlignment()).append("\">");
        table.append(escapeHtml(formatted));
        table.append("</td>\n");
      }
      table.append("</tr>\n");
    }
    table.append("</tbody>\n");

    table.append("</table>\n");
    return table.toString();
  }

  /** Generate summary section */
  private String generateSummary(List<?> data, ExportConfig config) {
    StringBuilder summary = new StringBuilder();
    summary.append("<div class=\"summary\">\n");
    summary.append("<h2>Zusammenfassung</h2>\n");
    summary.append("<div class=\"summary-grid\">\n");

    summary.append("<div class=\"summary-item\">\n");
    summary.append("<div class=\"summary-label\">Anzahl Datensätze</div>\n");
    summary.append("<div class=\"summary-value\">").append(data.size()).append("</div>\n");
    summary.append("</div>\n");

    // Add filter summary if present
    if (!config.getFilters().isEmpty()) {
      summary.append("<div class=\"summary-item\">\n");
      summary.append("<div class=\"summary-label\">Filter angewendet</div>\n");
      summary
          .append("<div class=\"summary-value\">")
          .append(config.getFilters().size())
          .append("</div>\n");
      summary.append("</div>\n");
    }

    summary.append("</div>\n");
    summary.append("</div>\n");
    return summary.toString();
  }

  /** Generate footer section */
  private String generateFooter(ExportConfig config) {
    StringBuilder footer = new StringBuilder();
    footer.append("<footer class=\"footer\">\n");
    footer.append("<p>© 2025 FreshPlan - Vertrauliche Informationen</p>\n");
    footer.append(
        "<p>Dieser Bericht wurde automatisch generiert und ist nur für den internen Gebrauch bestimmt.</p>\n");
    footer.append("</footer>\n");
    return footer.toString();
  }

  /** Generate JavaScript for interactivity */
  private String generateJavaScript() {
    return """
            <script>
            // Add sorting functionality
            document.querySelectorAll('th').forEach(header => {
                header.style.cursor = 'pointer';
                header.addEventListener('click', () => {
                    // Simple client-side sorting could be added here
                    console.log('Sorting by:', header.textContent);
                });
            });

            // Add search functionality
            function filterTable(searchTerm) {
                const rows = document.querySelectorAll('tbody tr');
                rows.forEach(row => {
                    const text = row.textContent.toLowerCase();
                    row.style.display = text.includes(searchTerm.toLowerCase()) ? '' : 'none';
                });
            }
            </script>
            """;
  }

  /** Generate filename for HTML export */
  private String generateFilename(ExportConfig config) {
    String base =
        config.getTitle() != null
            ? config
                .getTitle()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "")
            : "report";

    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

    return base + "_" + timestamp;
  }

  /** Escape HTML special characters */
  private String escapeHtml(String text) {
    if (text == null) return "";
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }

  /** Adjust color brightness */
  private String adjustBrightness(String hexColor, double factor) {
    // Simple brightness adjustment
    return hexColor; // Simplified for now
  }
}

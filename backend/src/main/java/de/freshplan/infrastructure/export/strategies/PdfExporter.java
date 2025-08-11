package de.freshplan.infrastructure.export.strategies;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import de.freshplan.infrastructure.export.ExportConfig;
import de.freshplan.infrastructure.export.ExportFormat;
import de.freshplan.infrastructure.export.ExportResult;
import de.freshplan.infrastructure.export.ExportStrategy;
import jakarta.enterprise.context.ApplicationScoped;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.jboss.logging.Logger;

/**
 * PDF export strategy using OpenPDF library. Creates professional PDF documents with proper
 * formatting.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class PdfExporter implements ExportStrategy {

  private static final Logger log = Logger.getLogger(PdfExporter.class);

  // FreshPlan Colors
  private static final Color FRESHPLAN_GREEN = new Color(148, 196, 86);
  private static final Color FRESHPLAN_BLUE = new Color(0, 79, 123);
  private static final Color LIGHT_GRAY = new Color(245, 245, 245);

  @Override
  public ExportFormat getFormat() {
    return ExportFormat.PDF;
  }

  @Override
  public ExportResult export(List<?> data, ExportConfig config) {
    log.infof("Exporting %d records to PDF", data.size());

    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Document document = new Document(PageSize.A4.rotate()); // Landscape for tables
      PdfWriter.getInstance(document, baos);

      document.open();

      // Add metadata
      document.addTitle(config.getTitle());
      document.addAuthor(
          config.getGeneratedBy() != null ? config.getGeneratedBy() : "FreshPlan System");
      document.addCreationDate();
      document.addCreator("FreshPlan Export System");

      // Add header
      addHeader(document, config);

      // Add data table
      if (!data.isEmpty()) {
        addDataTable(document, data, config);
      } else {
        addEmptyMessage(document);
      }

      // Add footer
      addFooter(document, config);

      document.close();

      String filename = generateFilename(config);

      return ExportResult.builder()
          .format(ExportFormat.PDF)
          .filename(filename)
          .recordCount(data.size())
          .withByteData(baos.toByteArray())
          .addMetadata("library", "OpenPDF")
          .addMetadata("pageSize", "A4 Landscape")
          .build();

    } catch (Exception e) {
      log.error("PDF export failed", e);
      throw new RuntimeException("Failed to generate PDF", e);
    }
  }

  private void addHeader(Document document, ExportConfig config) throws DocumentException {
    // Title
    Font titleFont = new Font(Font.HELVETICA, 24, Font.BOLD, FRESHPLAN_BLUE);
    Paragraph title = new Paragraph(config.getTitle(), titleFont);
    title.setAlignment(Element.ALIGN_CENTER);
    title.setSpacingAfter(10);
    document.add(title);

    // Subtitle
    if (config.getSubtitle() != null) {
      Font subtitleFont = new Font(Font.HELVETICA, 14, Font.NORMAL, Color.GRAY);
      Paragraph subtitle = new Paragraph(config.getSubtitle(), subtitleFont);
      subtitle.setAlignment(Element.ALIGN_CENTER);
      subtitle.setSpacingAfter(20);
      document.add(subtitle);
    }

    // Metadata bar
    Font metaFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);
    Paragraph metadata = new Paragraph();
    metadata.setAlignment(Element.ALIGN_CENTER);
    metadata.add(new Chunk("Erstellt am: ", metaFont));
    metadata.add(
        new Chunk(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")), metaFont));
    if (config.getGeneratedBy() != null) {
      metadata.add(new Chunk(" | Erstellt von: ", metaFont));
      metadata.add(new Chunk(config.getGeneratedBy(), metaFont));
    }
    metadata.setSpacingAfter(20);
    document.add(metadata);
  }

  private void addDataTable(Document document, List<?> data, ExportConfig config)
      throws DocumentException {
    List<ExportConfig.FieldConfig> fields = config.getVisibleFields();

    // Create table with column count
    PdfPTable table = new PdfPTable(fields.size());
    table.setWidthPercentage(100);
    table.setSpacingBefore(10);

    // Calculate column widths based on field types
    float[] widths = calculateColumnWidths(fields);
    table.setWidths(widths);

    // Add header row
    Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    for (ExportConfig.FieldConfig field : fields) {
      PdfPCell cell = new PdfPCell(new Phrase(field.getLabel(), headerFont));
      cell.setBackgroundColor(FRESHPLAN_BLUE);
      cell.setPadding(8);
      cell.setHorizontalAlignment(getAlignment(field));
      table.addCell(cell);
    }

    // Add data rows
    Font dataFont = new Font(Font.HELVETICA, 9, Font.NORMAL);
    int rowIndex = 0;

    for (Object record : data) {
      Color bgColor = (rowIndex % 2 == 0) ? Color.WHITE : LIGHT_GRAY;

      for (ExportConfig.FieldConfig field : fields) {
        String value = formatFieldValue(extractFieldValue(record, field.getKey()), field);
        PdfPCell cell = new PdfPCell(new Phrase(value, dataFont));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setHorizontalAlignment(getAlignment(field));
        table.addCell(cell);
      }
      rowIndex++;
    }

    document.add(table);
  }

  private void addEmptyMessage(Document document) throws DocumentException {
    Font emptyFont = new Font(Font.HELVETICA, 14, Font.ITALIC, Color.GRAY);
    Paragraph empty = new Paragraph("Keine Daten vorhanden", emptyFont);
    empty.setAlignment(Element.ALIGN_CENTER);
    empty.setSpacingBefore(50);
    document.add(empty);
  }

  private void addFooter(Document document, ExportConfig config) throws DocumentException {
    // Add summary
    Font summaryFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
    Paragraph summary = new Paragraph();
    summary.setAlignment(Element.ALIGN_LEFT);
    summary.setSpacingBefore(30);

    if (config.getFilters() != null && !config.getFilters().isEmpty()) {
      summary.add(new Chunk("Angewendete Filter: ", new Font(Font.HELVETICA, 10, Font.BOLD)));
      for (Map.Entry<String, Object> filter : config.getFilters().entrySet()) {
        summary.add(
            new Chunk(String.format("%s=%s ", filter.getKey(), filter.getValue()), summaryFont));
      }
    }

    document.add(summary);

    // FreshPlan branding
    Font brandFont = new Font(Font.HELVETICA, 8, Font.ITALIC, FRESHPLAN_GREEN);
    Paragraph brand = new Paragraph("Erstellt mit FreshPlan Sales Tool 2.0", brandFont);
    brand.setAlignment(Element.ALIGN_CENTER);
    brand.setSpacingBefore(20);
    document.add(brand);
  }

  private float[] calculateColumnWidths(List<ExportConfig.FieldConfig> fields) {
    float[] widths = new float[fields.size()];
    float totalWidth = 0;

    for (int i = 0; i < fields.size(); i++) {
      ExportConfig.FieldConfig field = fields.get(i);
      // Assign width based on field type
      float width =
          switch (field.getType()) {
            case DATE, BOOLEAN -> 10f;
            case NUMBER, CURRENCY, PERCENTAGE -> 12f;
            case EMAIL, PHONE -> 20f;
            case URL -> 25f;
            default -> 15f; // STRING and others
          };

      // Override with configured width if available
      if (field.getWidth() != null) {
        width = field.getWidth().floatValue();
      }

      widths[i] = width;
      totalWidth += width;
    }

    // Normalize widths
    for (int i = 0; i < widths.length; i++) {
      widths[i] = widths[i] / totalWidth * 100;
    }

    return widths;
  }

  private int getAlignment(ExportConfig.FieldConfig field) {
    return switch (field.getAlignment()) {
      case "center" -> Element.ALIGN_CENTER;
      case "right" -> Element.ALIGN_RIGHT;
      default -> Element.ALIGN_LEFT;
    };
  }

  // Use default implementations from ExportStrategy interface
  // The extractFieldValue and formatFieldValue methods are already provided
  // by the interface with the exact same logic

  private String generateFilename(ExportConfig config) {
    String base =
        config.getTitle() != null
            ? config
                .getTitle()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "")
            : "export";

    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

    return base + "_" + timestamp;
  }
}

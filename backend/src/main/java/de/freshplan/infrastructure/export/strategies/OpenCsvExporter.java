package de.freshplan.infrastructure.export.strategies;

import com.opencsv.CSVWriter;
import de.freshplan.infrastructure.export.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.jboss.logging.Logger;

/**
 * CSV export strategy using OpenCSV library. Combines the power of OpenCSV with our Universal
 * Export Framework.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class OpenCsvExporter implements ExportStrategy {

  private static final Logger log = Logger.getLogger(OpenCsvExporter.class);

  @Override
  public ExportFormat getFormat() {
    return ExportFormat.CSV;
  }

  @Override
  public ExportResult export(List<?> data, ExportConfig config) {
    log.infof("Exporting %d records to CSV using OpenCSV", data.size());

    // Create streaming output for efficient memory usage
    StreamingOutput stream =
        output -> {
          try (OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
              CSVWriter csvWriter =
                  new CSVWriter(
                      writer,
                      CSVWriter.DEFAULT_SEPARATOR,
                      CSVWriter.DEFAULT_QUOTE_CHARACTER,
                      CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                      CSVWriter.DEFAULT_LINE_END)) {

            // Write UTF-8 BOM for Excel compatibility
            output.write(0xef);
            output.write(0xbb);
            output.write(0xbf);

            // Build header
            String[] header =
                config.getVisibleFields().stream()
                    .map(ExportConfig.FieldConfig::getLabel)
                    .toArray(String[]::new);
            csvWriter.writeNext(header);

            // Write data rows
            for (Object record : data) {
              String[] row =
                  config.getVisibleFields().stream()
                      .map(
                          field -> {
                            Object value = extractFieldValue(record, field.getKey());
                            return formatFieldValue(value, field);
                          })
                      .toArray(String[]::new);
              csvWriter.writeNext(row);
            }

            csvWriter.flush();
          }
        };

    // Generate filename
    String filename = generateFilename(config);

    return ExportResult.builder()
        .format(ExportFormat.CSV)
        .filename(filename)
        .recordCount(data.size())
        .withStreamData(stream)
        .addMetadata("library", "OpenCSV")
        .addMetadata("version", "5.9")
        .build();
  }

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

  @Override
  public String formatDate(Object value, String pattern) {
    if (value instanceof LocalDateTime dateTime) {
      String format = pattern != null ? pattern : "dd.MM.yyyy";
      return dateTime.format(DateTimeFormatter.ofPattern(format));
    }
    return value != null ? value.toString() : "";
  }

  @Override
  public String formatDateTime(Object value, String pattern) {
    if (value instanceof LocalDateTime dateTime) {
      String format = pattern != null ? pattern : "dd.MM.yyyy HH:mm:ss";
      return dateTime.format(DateTimeFormatter.ofPattern(format));
    }
    return value != null ? value.toString() : "";
  }

  @Override
  public String formatCurrency(Object value, String currency) {
    if (value instanceof Number number) {
      String curr = currency != null ? currency : "EUR";
      return String.format("%.2f %s", number.doubleValue(), curr);
    }
    return value != null ? value.toString() : "";
  }
}

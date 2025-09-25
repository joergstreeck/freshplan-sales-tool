package de.freshplan.infrastructure.export;

import de.freshplan.infrastructure.export.strategies.*;
import de.freshplan.infrastructure.security.RlsContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.logging.Logger;

/**
 * Central export service that coordinates export operations. Uses strategy pattern to delegate to
 * format-specific exporters.
 *
 * <p>This is the main entry point for all export operations in the application.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
@Transactional
public class UniversalExportService {

  private static final Logger log = Logger.getLogger(UniversalExportService.class);

  // Strategy implementations
  @Inject OpenCsvExporter openCsvExporter; // OpenCSV Library

  @Inject ApachePoiExcelExporter excelExporter; // Apache POI Library

  @Inject HtmlExporter htmlExporter; // Our HTML solution

  @Inject JsonExporter jsonExporter; // Jackson Library

  @Inject PdfExporter pdfExporter; // OpenPDF Library

  private final Map<ExportFormat, ExportStrategy> strategies = new HashMap<>();

  /** Initialize strategies */
  @jakarta.annotation.PostConstruct
  void init() {
    strategies.put(ExportFormat.CSV, openCsvExporter); // OpenCSV
    strategies.put(ExportFormat.EXCEL, excelExporter); // Apache POI
    strategies.put(ExportFormat.HTML, htmlExporter); // Our solution
    strategies.put(ExportFormat.PDF, pdfExporter); // OpenPDF
    strategies.put(ExportFormat.JSON, jsonExporter); // Jackson

    log.infof("Universal Export Service initialized with %d strategies", strategies.size());
  }

  /**
   * Export data in the specified format.
   *
   * @param data List of objects to export
   * @param config Export configuration
   * @param format Target export format
   * @return Export result
   */
  @Transactional
  @RlsContext
  public ExportResult export(List<?> data, ExportConfig config, ExportFormat format) {
    log.infof(
        "Starting export: format=%s, records=%d, title=%s", format, data.size(), config.getTitle());

    // Validate input
    if (data == null) {
      throw new IllegalArgumentException("Data cannot be null");
    }
    if (config == null) {
      throw new IllegalArgumentException("Config cannot be null");
    }
    if (format == null) {
      throw new IllegalArgumentException("Format cannot be null");
    }

    // Get appropriate strategy
    ExportStrategy strategy = strategies.get(format);
    if (strategy == null) {
      throw new UnsupportedOperationException("Export format not yet implemented: " + format);
    }

    // Perform export
    try {
      ExportResult result = strategy.export(data, config);

      log.infof(
          "Export completed successfully: %d records exported to %s",
          result.getRecordCount(), result.getFullFilename());

      // TODO: Audit log the export operation
      logExportOperation(config, format, result);

      return result;

    } catch (Exception e) {
      log.error("Export failed", e);
      throw new ExportException("Export failed: " + e.getMessage(), e);
    }
  }

  /**
   * Export data and create HTTP Response. Convenience method that handles Response creation.
   *
   * @param data List of objects to export
   * @param config Export configuration
   * @param format Target export format
   * @return JAX-RS Response ready to send to client
   */
  @Transactional
  @RlsContext
  public Response exportAsResponse(List<?> data, ExportConfig config, ExportFormat format) {
    ExportResult result = export(data, config, format);

    Response.ResponseBuilder responseBuilder;

    // Build response based on data type
    if (result.getStreamData() != null) {
      responseBuilder = Response.ok(result.getStreamData());
    } else if (result.getByteData() != null) {
      responseBuilder = Response.ok(result.getByteData());
    } else if (result.getStringData() != null) {
      responseBuilder = Response.ok(result.getStringData());
    } else {
      throw new IllegalStateException("Export result has no data");
    }

    // Add headers
    responseBuilder
        .header("Content-Type", result.getContentType())
        .header("Content-Disposition", result.getContentDisposition());

    // Add cache control for exports
    responseBuilder.header("Cache-Control", "no-cache, no-store, must-revalidate");
    responseBuilder.header("Pragma", "no-cache");
    responseBuilder.header("Expires", "0");

    // Add custom metadata headers if needed
    result
        .getMetadata()
        .forEach(
            (key, value) -> {
              responseBuilder.header("X-Export-" + key, value.toString());
            });

    return responseBuilder.build();
  }

  /**
   * Create a basic export configuration for an entity. This is a convenience method for simple
   * exports.
   *
   * @param title Export title
   * @param fields Field configurations
   * @return Basic export configuration
   */
  public ExportConfig createBasicConfig(String title, List<ExportConfig.FieldConfig> fields) {
    return ExportConfig.builder()
        .title(title)
        .generatedBy("FreshPlan System")
        .fields(fields)
        .styles(ExportConfig.ExportStyles.defaultStyles())
        .build();
  }

  /**
   * Create a field configuration helper.
   *
   * @param key Field key
   * @param label Display label
   * @param type Field type
   * @return Field configuration
   */
  public static ExportConfig.FieldConfig field(
      String key, String label, ExportConfig.FieldType type) {
    return ExportConfig.FieldConfig.builder().key(key).label(label).type(type).build();
  }

  /**
   * Create a field configuration with format.
   *
   * @param key Field key
   * @param label Display label
   * @param type Field type
   * @param format Format pattern
   * @return Field configuration
   */
  public static ExportConfig.FieldConfig field(
      String key, String label, ExportConfig.FieldType type, String format) {
    return ExportConfig.FieldConfig.builder()
        .key(key)
        .label(label)
        .type(type)
        .format(format)
        .build();
  }

  /**
   * Check if a format is supported.
   *
   * @param format Export format to check
   * @return true if the format is supported
   */
  public boolean isFormatSupported(ExportFormat format) {
    return strategies.containsKey(format);
  }

  /**
   * Get list of supported formats.
   *
   * @return List of supported export formats
   */
  public List<ExportFormat> getSupportedFormats() {
    return strategies.keySet().stream().toList();
  }

  /** Log export operation for audit trail. */
  private void logExportOperation(ExportConfig config, ExportFormat format, ExportResult result) {
    // TODO: Integrate with audit service
    log.infof(
        "AUDIT: Export performed - Title: %s, Format: %s, Records: %d, User: %s",
        config.getTitle(), format, result.getRecordCount(), config.getGeneratedBy());
  }

  /** Exception for export operations */
  public static class ExportException extends RuntimeException {
    public ExportException(String message) {
      super(message);
    }

    public ExportException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}

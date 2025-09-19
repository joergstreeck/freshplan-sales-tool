package com.freshplan.reports;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.StreamingOutput;
import com.freshplan.export.UniversalExportService;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * Universal Export Adapter for Reports - Memory-Efficient Streaming
 *
 * @see ../../grundlagen/PERFORMANCE_STANDARDS.md - Streaming for Large Datasets
 * @see ../../grundlagen/SECURITY_GUIDELINES.md - ABAC Security in Export Operations
 * @see ../../grundlagen/API_STANDARDS.md - Export Format Standards
 *
 * This adapter extends the existing UniversalExportService (fully implemented
 * in Kundenliste) with Reports-specific export capabilities. It provides
 * memory-efficient JSONL streaming for Data Science teams while maintaining
 * compatibility with standard formats (CSV, Excel, PDF, HTML).
 *
 * Key features:
 * - JSONL streaming for large datasets (>100k records)
 * - Cursor-based pagination to prevent memory issues
 * - ABAC security enforcement through ReportsQuery
 * - Delegation to existing UniversalExportService for standard formats
 *
 * @author Backend Team
 * @version 1.1
 * @since 2025-09-19
 */
@ApplicationScoped
public class UniversalExportAdapter {

  /** Existing Universal Export Service (fully implemented in Kundenliste) */
  @Inject UniversalExportService exporter;

  /** ABAC-secured query service for data access */
  @Inject ReportsQuery queries;

  /** Jackson ObjectMapper for JSON serialization */
  @Inject ObjectMapper mapper;

  /**
   * Export Reports Data in multiple formats with ABAC security
   *
   * @param type Report type (sales-summary, customer-analytics, activity-stats)
   * @param format Export format (csv, xlsx, pdf, json, html, jsonl)
   * @param range Time range filter for data selection
   * @param segment Customer segment filter (gastronomiebetriebe, direktkunden)
   * @param territory Geographic territory filter (ABAC-enforced)
   * @return StreamingOutput for memory-efficient large file downloads
   * @see ../../grundlagen/PERFORMANCE_STANDARDS.md - Memory Budget Guidelines
   */
  public StreamingOutput export(String type, String format, String range,
                                String segment, String territory) {
    switch (format) {
      case "jsonl":
        return streamJsonl(type, range, segment, territory);
      case "json":
        return out -> {
          Map<String,Object> payload = buildPayload(type, range, segment, territory);
          mapper.writeValue(out, payload);
        };
      default:
        // Delegate to existing UniversalExportService for standard formats
        return exporter.export(type, format, range, segment, territory);
    }
  }

  /**
   * Create JSONL Streaming Output for Data Science Teams
   *
   * @param type Report type to stream
   * @param range Time range filter
   * @param segment Segment filter
   * @param territory Territory filter (ABAC-secured)
   * @return StreamingOutput that writes JSONL line-by-line
   * @see ../../grundlagen/PERFORMANCE_STANDARDS.md - Memory-Efficient Streaming
   */
  private StreamingOutput streamJsonl(String type, String range,
                                      String segment, String territory) {
    return new StreamingOutput() {
      @Override
      public void write(OutputStream out) {
        try {
          JsonGenerator generator = mapper.getFactory().createGenerator(out);
          Iterator<Map<String,Object>> iterator = iterateRows(type, range,
              segment, territory);

          while (iterator.hasNext()) {
            mapper.writeValue(generator, iterator.next());
            generator.writeRaw('\n'); // JSONL format: one JSON per line
          }
          generator.flush();
        } catch (Exception e) {
          throw new RuntimeException("JSONL streaming failed", e);
        }
      }
    };
  }

  /**
   * Create Cursor-Based Iterator for Large Dataset Streaming
   *
   * @param type Report type to iterate
   * @param range Time range filter
   * @param segment Segment filter
   * @param territory Territory filter
   * @return Iterator that fetches data in batches using cursor pagination
   * @see ../../grundlagen/PERFORMANCE_STANDARDS.md - Cursor Pagination
   */
  private Iterator<Map<String,Object>> iterateRows(String type, String range,
                                                   String segment, String territory) {
    return new Iterator<>() {
      String cursor = null;
      Iterator<Map<String,Object>> currentBatch = null;

      @Override
      public boolean hasNext() {
        if (currentBatch != null && currentBatch.hasNext()) {
          return true;
        }

        // Fetch next batch using cursor pagination (500 records per batch)
        Map<String,Object> page = queries.fetchCustomerAnalytics(segment,
            territory, null, null, null, 500, cursor);

        @SuppressWarnings("unchecked")
        var items = (java.util.List<Map<String,Object>>) page.get("items");
        cursor = (String) page.get("nextCursor");
        currentBatch = items.iterator();

        return currentBatch.hasNext();
      }

      @Override
      public Map<String,Object> next() {
        return currentBatch.next();
      }
    };
  }

  /**
   * Build JSON Payload for non-streaming exports
   *
   * @param type Report type
   * @param range Time range
   * @param segment Segment filter
   * @param territory Territory filter
   * @return Map with report data for JSON serialization
   * @see ../../grundlagen/API_STANDARDS.md - Response Format Standards
   */
  private Map<String,Object> buildPayload(String type, String range,
                                          String segment, String territory) {
    if ("sales-summary".equals(type)) {
      // TODO: Replace with real delegation to SalesCockpitService
      return Map.of(
          "range", range,
          "sampleSuccessRatePct", 0,
          "roiPipelineValue", 0,
          "partnerSharePct", 0,
          "atRiskCustomers", 0,
          "updatedAt", java.time.OffsetDateTime.now().toString()
      );
    }
    return Map.of("status", "ok", "message", "Export completed");
  }
}

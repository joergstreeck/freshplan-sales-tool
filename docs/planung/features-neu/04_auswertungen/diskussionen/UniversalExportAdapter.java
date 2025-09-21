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

@ApplicationScoped
public class UniversalExportAdapter {

  @Inject UniversalExportService exporter;
  @Inject ReportsQuery queries;
  @Inject ObjectMapper mapper;

  public StreamingOutput export(String type, String format, String range, String segment, String territory) {
    switch (format) {
      case "jsonl":
        return streamJsonl(type, range, segment, territory);
      case "json":
        return out -> {
          Map<String,Object> payload = buildPayload(type, range, segment, territory);
          mapper.writeValue(out, payload);
        };
      default:
        // CSV/XLSX/PDF/HTML – Übergabe an bestehenden Exporter
        return exporter.export(type, format, range, segment, territory);
    }
  }

  private StreamingOutput streamJsonl(String type, String range, String segment, String territory) {
    return new StreamingOutput() {
      @Override public void write(OutputStream out) {
        try {
          JsonGenerator g = mapper.getFactory().createGenerator(out);
          Iterator<Map<String,Object>> it = iterateRows(type, range, segment, territory);
          while (it.hasNext()) {
            mapper.writeValue(g, it.next());
            g.writeRaw('\n');
          }
          g.flush();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  private Iterator<Map<String,Object>> iterateRows(String type, String range, String segment, String territory) {
    // Minimalbeispiel: für customer-analytics paginiert abfragen
    return new Iterator<>() {
      String cursor = null;
      Iterator<Map<String,Object>> batch = null;
      @Override public boolean hasNext() {
        if (batch != null && batch.hasNext()) return true;
        Map<String,Object> page = queries.fetchCustomerAnalytics(segment, territory, null, null, null, 500, cursor);
        @SuppressWarnings("unchecked")
        var items = (java.util.List<Map<String,Object>>) page.get("items");
        cursor = (String) page.get("nextCursor");
        batch = items.iterator();
        return batch.hasNext();
      }
      @Override public Map<String,Object> next() { return batch.next(); }
    };
  }

  private Map<String,Object> buildPayload(String type, String range, String segment, String territory) {
    // Für einfache JSON-Exports
    if ("sales-summary".equals(type)) {
      // Placeholder – in Realität Delegation an Service/DTO
      return Map.of("range", range, "sampleSuccessRatePct", 0, "roiPipelineValue", 0, "partnerSharePct", 0, "atRiskCustomers", 0, "updatedAt", java.time.OffsetDateTime.now().toString());
    }
    return Map.of("status","ok");
  }
}

package com.freshplan.leads;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.StreamingOutput;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.OutputStream;
import java.util.*;

@ApplicationScoped
public class LeadExportAdapter {

  @Inject ObjectMapper mapper;
  @Inject LeadRepository repo; // imagined repository

  public StreamingOutput export(String format, Map<String,String> filters) {
    if ("jsonl".equals(format)) return jsonl(filters);
    // else delegate to Universal Exporter (csv/xlsx/pdf/json/html)
    return out -> mapper.writeValue(out, Map.of("status","ok"));
  }

  private StreamingOutput jsonl(Map<String,String> filters) {
    return (OutputStream out) -> {
      JsonGenerator g = mapper.getFactory().createGenerator(out);
      String cursor = null;
      do {
        var page = repo.page(filters, 500, cursor); // ABAC inside repo
        @SuppressWarnings("unchecked")
        var items = (List<Map<String,Object>>)page.get("items");
        for (var row: items) { mapper.writeValue(g, row); g.writeRaw('\n'); }
        cursor = (String)page.get("nextCursor");
      } while (cursor != null);
      g.flush();
    };
  }
}

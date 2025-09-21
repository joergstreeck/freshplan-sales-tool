package de.freshplan.integration.sync;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

public class AuditLog {
  private final String path;
  public AuditLog(String path){ this.path = path; }
  public synchronized void write(String action, boolean success, Map<String,Object> detail){
    try {
      java.io.File f = new java.io.File(path);
      f.getParentFile().mkdirs();
      try (FileWriter fw = new FileWriter(f, true)){
        String json = String.format("{"ts":"%s","action":"%s","success":%s,"detail":%s}
",
            Instant.now().toString(), action, success, toJson(detail));
        fw.write(json);
      }
    } catch(IOException e){
      e.printStackTrace();
    }
  }
  private static String toJson(Map<String,Object> m){
    StringBuilder sb = new StringBuilder("{");
    boolean first = true;
    for (var e : m.entrySet()){
      if (!first) sb.append(",");
      first = false;
      sb.append(""").append(e.getKey()).append("":"").append(String.valueOf(e.getValue()).replace(""","\"")).append(""");
    }
    sb.append("}");
    return sb.toString();
  }
}

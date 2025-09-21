package de.freshplan.settings.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.freshplan.settings.repo.SettingsRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.*;
/** SettingsMergeEngine – merge by precedence + strategy (scalar/object/list). */
@ApplicationScoped
public class SettingsMergeEngine {

  public static final List<String> PRECEDENCE = List.of("global","tenant","territory","account","contact_role","contact","user");

  @Inject SettingsRepository repo;
  @Inject ObjectMapper om;

  public record Scope(UUID tenantId, String territory, UUID accountId, String contactRole, UUID contactId, UUID userId) {}

  public static String computeScopeSig(Scope s){
    String raw = String.valueOf(s.tenantId)+"|"+String.valueOf(s.territory)+"|"+String.valueOf(s.accountId)+"|"+String.valueOf(s.contactRole)+"|"+String.valueOf(s.contactId)+"|"+String.valueOf(s.userId);
    return md5(raw);
  }
  private static String md5(String x){
    try{
      var md = MessageDigest.getInstance("MD5");
      byte[] d = md.digest(x.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : d) sb.append(String.format("%02x", b));
      return sb.toString();
    }catch(Exception e){ throw new RuntimeException(e); }
  }

  public Result computeEffective(Scope s){
    var reg = repo.loadRegistry(); // key -> RegistryEntry
    var store = repo.loadStoreForScope(s); // key -> List<ScopedValue> sorted by precedence
    ObjectNode eff = om.createObjectNode();
    reg.forEach((key, r) -> { if (r.defaultValue()!=null) eff.set(key, r.defaultValue().deepCopy()); });
    store.forEach((key, list) -> {
      var entry = reg.get(key); if (entry==null) return;
      for (de.freshplan.settings.repo.SettingsRepository.ScopedValue sv : list){
        switch(entry.mergeStrategy()){
          case "scalar" -> eff.set(key, sv.value().deepCopy());
          case "object" -> {
            var base = eff.has(key) && eff.get(key).isObject() ? (ObjectNode) eff.get(key) : om.createObjectNode();
            eff.set(key, deepMergeObject(base, (ObjectNode) sv.value()));
          }
          case "list" -> {
            var base = eff.has(key) && eff.get(key).isArray() ? (ArrayNode) eff.get(key) : om.createArrayNode();
            eff.set(key, mergeList(base, (ArrayNode) sv.value()));
          }
        }
      }
    });
    String etag = sha256Hex(eff.toString());
    return new Result(eff, etag, OffsetDateTime.now());
  }

  private static String sha256Hex(String s){
    try{
      var md = MessageDigest.getInstance("SHA-256");
      byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : d) sb.append(String.format("%02x", b));
      return sb.toString();
    }catch(Exception e){ throw new RuntimeException(e); }
  }

  private ObjectNode deepMergeObject(ObjectNode base, ObjectNode patch){
    var it = patch.fields();
    while(it.hasNext()){
      var e = it.next();
      var key = e.getKey();
      var val = e.getValue();
      if (val.isNull()){ base.remove(key); continue; }
      if (base.has(key) && base.get(key).isObject() && val.isObject()){
        base.set(key, deepMergeObject((ObjectNode) base.get(key), (ObjectNode) val));
      } else { base.set(key, val.deepCopy()); }
    }
    return base;
  }

  private ArrayNode mergeList(ArrayNode base, ArrayNode add){
    java.util.Set<String> seen = new java.util.LinkedHashSet<>();
    ArrayNode out = om.createArrayNode();
    for (JsonNode n : base){ String k=n.toString(); if (seen.add(k)) out.add(n.deepCopy()); }
    for (JsonNode n : add){ String k=n.toString(); if (seen.add(k)) out.add(n.deepCopy()); }
    return out;
  }

  public record Result(ObjectNode blob, String etag, OffsetDateTime computedAt) {}
}

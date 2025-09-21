package de.freshplan.governance.settings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class SettingsService {

  @Inject DataSource ds;
  @Inject ObjectMapper mapper;
  @Inject MeterRegistry metrics;

  @Inject JsonSchemaValidator validator;

  private final Map<String, CacheEntry> l1 = new java.util.concurrent.ConcurrentHashMap<>();
  private final Logger log = Logger.getLogger(SettingsService.class);

  private record CacheEntry(String etag, long expiresAtMs, ObjectNode payload){}

  private Timer fetchTimer(){ return metrics.timer("settings_fetch_ms"); }
  private Counter etagHitCounter(){ return metrics.counter("settings_etag_hits"); }
  private Counter lookupSourceRegistry(String module){ return metrics.counter("settings_lookup_source", "module", module, "source","registry"); }
  private Counter lookupSourceConfig(String module){ return metrics.counter("settings_lookup_source", "module", module, "source","config"); }

  public record EffectiveResult(String etag, ObjectNode payload){}

  public Duration cacheTtl(){ return Duration.ofMinutes(15); }

  /** GET effective values for a set of keys (may be empty -> all visible). */
  public EffectiveResult getEffective(SettingsPrincipal p, Set<String> keys){
    long start = System.nanoTime();
    try (Connection c = ds.getConnection()){
      setSession(c, p);
      ObjectNode result = mapper.createObjectNode();

      // Build cache key
      String cacheKey = p.cacheKeyFor(keys.isEmpty()? Set.of("*"): keys);
      CacheEntry ce = l1.get(cacheKey);
      if (ce != null && System.currentTimeMillis() < ce.expiresAtMs){
        etagHitCounter().increment();
        return new EffectiveResult(ce.etag, ce.payload.deepCopy());
      }

      // Query path
      String baseSql = "SELECT e.key, e.value, e.etag FROM settings_effective e " +
          "WHERE (e.tenant_id IS NOT DISTINCT FROM ?) AND (e.org_id IS NOT DISTINCT FROM ?) AND (e.user_id IS NOT DISTINCT FROM ?)";
      String sql = keys.isEmpty() ? baseSql : baseSql + " AND e.key = ANY (?)";

      try (PreparedStatement ps = c.prepareStatement(sql)){
        ps.setObject(1, p.tenantId().orElse(null));
        ps.setObject(2, p.orgId().orElse(null));
        ps.setObject(3, p.userId());
        if (!keys.isEmpty()){
          ps.setArray(4, c.createArrayOf("text", keys.toArray()));
        }
        try (ResultSet rs = ps.executeQuery()){
          while (rs.next()){
            String k = rs.getString(1);
            String v = rs.getString(2);
            result.set(k, mapper.readTree(v));
          }
        }
      }

      // Compute missing on the fly
      if (!keys.isEmpty()){
        Set<String> missing = new HashSet<>(keys);
        Iterator<String> it = result.fieldNames();
        while (it.hasNext()) missing.remove(it.next());
        for (String mk : missing){
          JsonNode merged = mergeChain(c, p, mk);
          if (merged != null){
            upsertEffective(c, p, mk, merged);
            result.set(mk, merged);
          }
        }
      }

      // ETag from payload
      String etag = sha256Hex(result.toString());
      CacheEntry entry = new CacheEntry(etag, System.currentTimeMillis()+cacheTtl().toMillis(), result.deepCopy());
      l1.put(cacheKey, entry);

      return new EffectiveResult(etag, result);
    } catch (Exception e){
      throw new RuntimeException(e);
    } finally {
      fetchTimer().record(Duration.ofNanos(System.nanoTime()-start));
    }
  }

  @Transactional
  public void patch(SettingsPrincipal p, PatchRequest req){
    try (Connection c = ds.getConnection()){
      setSession(c, p);
      // Load registry meta
      RegistryMeta meta = loadRegistryMeta(c, req.key());
      if (meta == null) throw new IllegalArgumentException("Unknown key: "+req.key());
      // Validate scope
      if (!meta.allowsScope(req.scope())){
        throw new IllegalArgumentException("Key "+req.key()+" does not allow scope "+req.scope());
      }
      // Validate value
      validator.validate(meta.schema(), req.value());

      // Upsert store
      String upSql = "INSERT INTO settings_store(key, tenant_id, org_id, user_id, value, updated_by) " +
          "VALUES (?,?,?,?,?,?) " +
          "ON CONFLICT (key, tenant_id, org_id, user_id) DO UPDATE SET value = EXCLUDED.value, updated_by = EXCLUDED.updated_by, updated_at = now()";
      try (PreparedStatement ps = c.prepareStatement(upSql)){
        ps.setString(1, req.key());
        ps.setObject(2, req.tenantId().orElse(null));
        ps.setObject(3, req.orgId().orElse(null));
        ps.setObject(4, req.userId().orElse(null));
        ps.setObject(5, req.value().toString(), Types.OTHER);
        ps.setObject(6, p.userId());
        ps.executeUpdate();
      }

      // Recompute effective for this principal
      JsonNode merged = mergeChain(c, p, req.key());
      if (merged != null){
        upsertEffective(c, p, req.key(), merged);
      }
      // Bust local caches referencing this key
      bustLocalCaches(req.key());
    } catch (Exception e){
      throw new RuntimeException(e);
    }
  }

  private void bustLocalCaches(String key){
    l1.keySet().removeIf(k -> k.endsWith(key) || k.contains(","+key) || k.contains("*"));
  }

  private void setSession(Connection c, SettingsPrincipal p) throws SQLException{
    try (Statement st = c.createStatement()){
      st.execute("SELECT set_config('app.user_id', '"+p.userId()+"', true)");
      st.execute("SELECT set_config('app.tenant_id', '"+p.tenantId().map(java.util.UUID::toString).orElse("null")+"', true)");
      st.execute("SELECT set_config('app.org_id', '"+p.orgId().map(java.util.UUID::toString).orElse("null")+"', true)");
      st.execute("SELECT set_config('app.roles', '"+String.join(",", p.roles())+"', true)");
    }
  }

  private static String sha256Hex(String s){
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : d) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (Exception e){ throw new RuntimeException(e); }
  }

  private record RegistryMeta(String key, String type, Set<String> scopes, String mergeStrategy, JsonNode schema){
    boolean allowsScope(String s){ return scopes.contains(s); }
  }

  private RegistryMeta loadRegistryMeta(Connection c, String key) throws Exception {
    try (PreparedStatement ps = c.prepareStatement("SELECT type, scope, merge_strategy, schema FROM settings_registry WHERE key=?")){
      ps.setString(1, key);
      try (ResultSet rs = ps.executeQuery()){
        if (!rs.next()) return null;
        String type = rs.getString(1);
        String scopeArr = rs.getString(2);
        String merge = rs.getString(3);
        String schema = rs.getString(4);
        Set<String> scopes = new HashSet<>();
        if (scopeArr != null){
          for (JsonNode n : mapper.readTree(scopeArr)) scopes.add(n.asText());
        }
        JsonNode schemaNode = (schema == null) ? null : mapper.readTree(schema);
        return new RegistryMeta(key, type, scopes, merge, schemaNode);
      }
    }
  }

  private JsonNode mergeChain(Connection c, SettingsPrincipal p, String key) throws Exception {
    // Order: global -> tenant -> org -> user
    List<Row> rows = new ArrayList<>();
    String sql = ""
        + "SELECT key, tenant_id, org_id, user_id, value "
        + "FROM settings_store WHERE key=? AND ("
        + " (tenant_id IS NULL AND org_id IS NULL AND user_id IS NULL) "
        + " OR (tenant_id IS NOT NULL AND org_id IS NULL  AND user_id IS NULL AND tenant_id IS NOT DISTINCT FROM ?) "
        + " OR (tenant_id IS NOT NULL AND org_id IS NOT NULL AND user_id IS NULL AND tenant_id IS NOT DISTINCT FROM ? AND org_id IS NOT DISTINCT FROM ?) "
        + " OR (tenant_id IS NOT NULL AND org_id IS NOT NULL AND user_id IS NOT NULL AND tenant_id IS NOT DISTINCT FROM ? AND org_id IS NOT DISTINCT FROM ? AND user_id IS NOT DISTINCT FROM ?) "
        + ") "
        + "ORDER BY "
        + " (tenant_id IS NULL AND org_id IS NULL AND user_id IS NULL) ASC, "
        + " (tenant_id IS NOT NULL AND org_id IS NULL  AND user_id IS NULL) ASC, "
        + " (tenant_id IS NOT NULL AND org_id IS NOT NULL AND user_id IS NULL) ASC, "
        + " (tenant_id IS NOT NULL AND org_id IS NOT NULL AND user_id IS NOT NULL) ASC";
    try (PreparedStatement ps = c.prepareStatement(sql)){
      ps.setString(1, key);
      ps.setObject(2, p.tenantId().orElse(null));
      ps.setObject(3, p.tenantId().orElse(null));
      ps.setObject(4, p.orgId().orElse(null));
      ps.setObject(5, p.tenantId().orElse(null));
      ps.setObject(6, p.orgId().orElse(null));
      ps.setObject(7, p.userId());
      try (ResultSet rs = ps.executeQuery()){
        while (rs.next()){
          rows.add(new Row(rs.getString(1), (java.util.UUID) rs.getObject(2), (java.util.UUID) rs.getObject(3), (java.util.UUID) rs.getObject(4), rs.getString(5)));
        }
      }
    }

    // Load merge strategy + default
    String mergeStrategy = "replace";
    JsonNode defaultVal = null;
    try (PreparedStatement ps = c.prepareStatement("SELECT default_value, merge_strategy FROM settings_registry WHERE key=?")){
      ps.setString(1, key);
      try (ResultSet rs = ps.executeQuery()){
        if (rs.next()){
          String def = rs.getString(1);
          mergeStrategy = rs.getString(2);
          defaultVal = (def==null) ? null : mapper.readTree(def);
        }
      }
    }
    JsonNode merged = defaultVal;
    for (Row r : rows){
      JsonNode v = mapper.readTree(r.valueJson);
      merged = JsonMerge.merge(mergeStrategy, mapper, merged, v);
    }
    return merged;
  }

  private void upsertEffective(Connection c, SettingsPrincipal p, String key, JsonNode merged) throws Exception {
    String etag = sha256Hex(merged.toString());
    String sql = "INSERT INTO settings_effective(key, tenant_id, org_id, user_id, value, etag, computed_at) " +
        "VALUES (?,?,?,?,?, ?, now()) " +
        "ON CONFLICT (key, tenant_id, org_id, user_id) DO UPDATE SET value = EXCLUDED.value, etag = EXCLUDED.etag, computed_at = now()";
    try (PreparedStatement ps = c.prepareStatement(sql)){
      ps.setString(1, key);
      ps.setObject(2, p.tenantId().orElse(null));
      ps.setObject(3, p.orgId().orElse(null));
      ps.setObject(4, p.userId());
      ps.setObject(5, merged.toString(), Types.OTHER);
      ps.setString(6, etag);
      ps.executeUpdate();
    }
  }

  public record PatchRequest(
      String key,
      String scope, // "global" | "tenant" | "org" | "user"
      Optional<java.util.UUID> tenantId,
      Optional<java.util.UUID> orgId,
      Optional<java.util.UUID> userId,
      JsonNode value
  ) {}
  private record Row(String key, java.util.UUID tenant, java.util.UUID org, java.util.UUID user, String valueJson){}

  public void clearAllLocalCaches(){
    l1.clear();
  }
}

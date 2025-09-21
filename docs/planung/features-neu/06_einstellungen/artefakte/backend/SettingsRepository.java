package de.freshplan.settings.repo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.*;
import de.freshplan.settings.service.SettingsMergeEngine.Scope;

/**
 * OPTIMIZED: Best of both worlds - comprehensive registry + clean queries + write operations
 * - Enhanced RegistryEntry with schema validation support (from NEW)
 * - Clean text block queries for readability (from OLD)
 * - Contextual error handling (from OLD)
 * - Complete CRUD operations (from NEW)
 */
@ApplicationScoped
public class SettingsRepository {

  @Inject EntityManager em;
  @Inject ObjectMapper om;

  /** Enhanced Registry Entry with schema validation support */
  public static record RegistryEntry(
    String key,
    String type,
    String mergeStrategy,
    JsonNode defaultValue,
    JsonNode jsonSchema,     // NEW: For runtime validation
    List<String> scope,      // NEW: Allowed scopes
    int version              // NEW: Schema versioning
  ) {}

  public static record ScopedValue(String key, String level, JsonNode value) {}

  /** Load comprehensive registry with schema validation data */
  public Map<String, RegistryEntry> loadRegistry() {
    Query q = em.createNativeQuery("""
      SELECT key, type, merge_strategy, default_value, json_schema, scope, version
      FROM settings_registry
    """);

    @SuppressWarnings("unchecked")
    List<Object[]> rows = q.getResultList();
    Map<String, RegistryEntry> m = new HashMap<>();

    for (Object[] r : rows) {
      String key = (String) r[0];
      String type = (String) r[1];
      String merge = (String) r[2];
      JsonNode def = parseJson("default_value for " + key, r[3]);
      JsonNode schema = parseJson("json_schema for " + key, r[4]);
      List<String> scope = parseScope("scope for " + key, r[5]);
      int version = r[6] == null ? 1 : ((Number) r[6]).intValue();

      m.put(key, new RegistryEntry(key, type, merge, def, schema, scope, version));
    }
    return m;
  }

  /** Enhanced JSON parsing with contextual error messages */
  private JsonNode parseJson(String context, Object raw) {
    try {
      if (raw == null) return null;
      if (raw instanceof String s) return om.readTree(s);
      return om.readTree(raw.toString());
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse JSON for " + context + ": " + e.getMessage(), e);
    }
  }

  /** Parse scope array with error handling */
  private List<String> parseScope(String context, Object raw) {
    try {
      if (raw == null) return List.of();
      JsonNode n = parseJson(context, raw);
      List<String> out = new ArrayList<>();
      n.forEach(j -> out.add(j.asText()));
      return out;
    } catch (Exception e) {
      // Log warning but don't fail - fallback to empty scope
      System.err.println("Warning: Failed to parse scope for " + context + ", using empty scope");
      return List.of();
    }
  }

  /** Load store values with clean text block query (from OLD) */
  public Map<String, List<ScopedValue>> loadStoreForScope(Scope s) {
    String sql = """
      SELECT key,
             CASE
               WHEN user_id       IS NOT NULL THEN 'user'
               WHEN contact_id    IS NOT NULL THEN 'contact'
               WHEN contact_role  IS NOT NULL THEN 'contact_role'
               WHEN account_id    IS NOT NULL THEN 'account'
               WHEN territory     IS NOT NULL THEN 'territory'
               WHEN tenant_id     IS NOT NULL THEN 'tenant'
               ELSE 'global'
             END AS level,
             value
        FROM settings_store
       WHERE (tenant_id   IS NULL OR tenant_id   = :tenant)
         AND (territory   IS NULL OR territory   = :territory)
         AND (account_id  IS NULL OR account_id  = :account)
         AND (contact_role IS NULL OR contact_role = CAST(:crole AS contact_role_enum))
         AND (contact_id  IS NULL OR contact_id  = :contact)
         AND (user_id     IS NULL OR user_id     = :user)
      """;

    var q = em.createNativeQuery(sql)
      .setParameter("tenant", s.tenantId())
      .setParameter("territory", s.territory())
      .setParameter("account", s.accountId())
      .setParameter("crole", s.contactRole())
      .setParameter("contact", s.contactId())
      .setParameter("user", s.userId());

    @SuppressWarnings("unchecked")
    List<Object[]> rows = q.getResultList();
    Map<String, List<ScopedValue>> grouped = new HashMap<>();

    for (Object[] r : rows) {
      String key = (String) r[0];
      String level = (String) r[1];
      String json = r[2].toString();
      var value = parseJson("store value for key " + key, json);
      grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(new ScopedValue(key, level, value));
    }

    // Sort by precedence (lowest to highest priority)
    grouped.values().forEach(list ->
      list.sort(Comparator.comparingInt(v -> precedenceRank(v.level())))
    );
    return grouped;
  }

  private int precedenceRank(String level) {
    return switch (level) {
      case "global" -> 0;
      case "tenant" -> 1;
      case "territory" -> 2;
      case "account" -> 3;
      case "contact_role" -> 4;
      case "contact" -> 5;
      case "user" -> 6;
      default -> 100;
    };
  }

  /** NEW: Upsert setting with proper conflict handling */
  public void upsertSetting(
    UUID tenant, String territory, UUID account, String crole,
    UUID contact, UUID user, String key, JsonNode value, UUID updatedBy
  ) {
    String sql = """
      INSERT INTO settings_store (
        tenant_id, territory, account_id, contact_role, contact_id, user_id,
        key, value, updated_by
      ) VALUES (
        :tenant, :territory, :account,
        CAST(:crole AS contact_role_enum),
        :contact, :user, :key,
        CAST(:value AS jsonb), :updatedBy
      )
      ON CONFLICT (key, scope_sig)
      DO UPDATE SET
        value = EXCLUDED.value,
        updated_at = now(),
        updated_by = EXCLUDED.updated_by
    """;

    try {
      em.createNativeQuery(sql)
        .setParameter("tenant", tenant)
        .setParameter("territory", territory)
        .setParameter("account", account)
        .setParameter("crole", crole)
        .setParameter("contact", contact)
        .setParameter("user", user)
        .setParameter("key", key)
        .setParameter("value", value == null ? "null" : value.toString())
        .setParameter("updatedBy", updatedBy)
        .executeUpdate();
    } catch (Exception e) {
      throw new RuntimeException("Failed to upsert setting " + key + " for scope: " + e.getMessage(), e);
    }
  }

  /** NEW: Delete setting with null-safe comparison */
  public void deleteSetting(
    UUID tenant, String territory, UUID account, String crole,
    UUID contact, UUID user, String key
  ) {
    String sql = """
      DELETE FROM settings_store
       WHERE key = :key
         AND tenant_id   IS NOT DISTINCT FROM :tenant
         AND territory   IS NOT DISTINCT FROM :territory
         AND account_id  IS NOT DISTINCT FROM :account
         AND contact_role IS NOT DISTINCT FROM CAST(:crole AS contact_role_enum)
         AND contact_id  IS NOT DISTINCT FROM :contact
         AND user_id     IS NOT DISTINCT FROM :user
    """;

    try {
      int deleted = em.createNativeQuery(sql)
        .setParameter("key", key)
        .setParameter("tenant", tenant)
        .setParameter("territory", territory)
        .setParameter("account", account)
        .setParameter("crole", crole)
        .setParameter("contact", contact)
        .setParameter("user", user)
        .executeUpdate();

      if (deleted == 0) {
        System.out.println("Warning: No setting found to delete for key " + key + " in specified scope");
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to delete setting " + key + " for scope: " + e.getMessage(), e);
    }
  }
}
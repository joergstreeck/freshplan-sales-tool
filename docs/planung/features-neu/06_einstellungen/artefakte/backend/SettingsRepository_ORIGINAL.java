package de.freshplan.settings.repo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.*;
import de.freshplan.settings.service.SettingsMergeEngine.Scope;

/** Repository – uses named parameters and RLS is enforced in DB via session settings. */
@ApplicationScoped
public class SettingsRepository {

  @Inject EntityManager em;
  @Inject ObjectMapper om;

  public static record RegistryEntry(String key, String type, String mergeStrategy, JsonNode defaultValue){}
  public static record ScopedValue(String key, String level, JsonNode value){}

  public Map<String,RegistryEntry> loadRegistry(){
    Query q = em.createNativeQuery("SELECT key, type, merge_strategy, default_value FROM settings_registry");
    @SuppressWarnings("unchecked") List<Object[]> rows = q.getResultList();
    Map<String,RegistryEntry> m = new HashMap<>();
    for (Object[] r : rows){
      m.put((String)r[0], new RegistryEntry((String)r[0], (String)r[1], (String)r[2], parse((String)r[0], r[3])));
    }
    return m;
  }

  private JsonNode parse(String key, Object raw){
    if (raw==null) return null;
    try {
      if (raw instanceof String s) return om.readTree(s);
      return om.readTree(raw.toString());
    } catch (Exception e){ throw new RuntimeException("parse json for "+key, e); }
  }

  /** Load store values relevant for given scope ordered by precedence. */
  public Map<String,List<ScopedValue>> loadStoreForScope(Scope s){
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
         AND (contact_role IS NULL OR contact_role = :crole)
         AND (contact_id  IS NULL OR contact_id  = :contact)
         AND (user_id     IS NULL OR user_id     = :user)
    """;

    var q = em.createNativeQuery(sql)
      .setParameter("tenant",  s.tenantId())
      .setParameter("territory", s.territory())
      .setParameter("account", s.accountId())
      .setParameter("crole",   s.contactRole())
      .setParameter("contact", s.contactId())
      .setParameter("user",    s.userId());

    @SuppressWarnings("unchecked") List<Object[]> rows = q.getResultList();
    Map<String,List<ScopedValue>> grouped = new HashMap<>();
    for (Object[] r : rows){
      String key = (String) r[0];
      String level = (String) r[1];
      String json = r[2].toString();
      var value = parse(key, json);
      grouped.computeIfAbsent(key, k-> new ArrayList<>()).add(new ScopedValue(key, level, value));
    }
    grouped.values().forEach(list -> list.sort(Comparator.comparingInt(v -> precedenceRank(v.level()))));
    return grouped;
  }

  private int precedenceRank(String level){
    return switch(level){
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
}

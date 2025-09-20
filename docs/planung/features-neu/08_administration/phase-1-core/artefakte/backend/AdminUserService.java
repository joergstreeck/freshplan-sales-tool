package de.freshplan.admin.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.*;

import de.freshplan.admin.security.AdminSecurityService;
import de.freshplan.security.ScopeContext;

@ApplicationScoped
public class AdminUserService {

  @Inject EntityManager em;
  @Inject AdminSecurityService sec;
  @Inject ScopeContext scope;

  public Map<String,Object> createUser(String email, String name, List<String> roles){
    sec.enforce("user:create", "user", null, null);
    UUID id = UUID.randomUUID();
    em.createNativeQuery("INSERT INTO app_user(id,email,name,roles,disabled) VALUES (:id,:email,:name,:roles,false)")
      .setParameter("id", id).setParameter("email", email).setParameter("name", name)
      .setParameter("roles", roles.toArray(new String[0])).executeUpdate();
    return getUser(id);
  }

  public Map<String,Object> getUser(UUID id){
    Query q = em.createNativeQuery("SELECT id, email, name, roles, org_id, territories, channels, scopes, disabled FROM app_user WHERE id=:id")
      .setParameter("id", id);
    Object[] r = (Object[]) q.getSingleResult();
    Map<String,Object> m = new LinkedHashMap<>();
    m.put("id", r[0]); m.put("email", r[1]); m.put("name", r[2]); m.put("roles", r[3]);
    m.put("orgId", r[4]); m.put("territories", r[5]); m.put("channels", r[6]); m.put("scopes", r[7]); m.put("disabled", r[8]);
    return m;
  }

  public List<Map<String,Object>> list(String qstr, UUID orgId, String territory, int limit){
    String sql = "SELECT id,email,name,roles,org_id,territories,channels,scopes,disabled FROM app_user WHERE 1=1";
    if (qstr != null && !qstr.isBlank()) sql += " AND (email ILIKE :q OR name ILIKE :q)";
    if (orgId != null) sql += " AND org_id = :org";
    if (territory != null && !territory.isBlank()) sql += " AND :terr = ANY(territories)";
    sql += " ORDER BY name LIMIT :limit";
    Query q = em.createNativeQuery(sql);
    if (qstr != null && !qstr.isBlank()) q.setParameter("q", "%"+qstr+"%");
    if (orgId != null) q.setParameter("org", orgId);
    if (territory != null && !territory.isBlank()) q.setParameter("terr", territory);
    q.setParameter("limit", Math.max(1, Math.min(limit, 200)));
    @SuppressWarnings("unchecked") List<Object[]> rows = q.getResultList();
    List<Map<String,Object>> out = new ArrayList<>();
    for (Object[] r: rows){
      Map<String,Object> m = new LinkedHashMap<>();
      m.put("id", r[0]); m.put("email", r[1]); m.put("name", r[2]); m.put("roles", r[3]);
      m.put("orgId", r[4]); m.put("territories", r[5]); m.put("channels", r[6]); m.put("scopes", r[7]); m.put("disabled", r[8]);
      out.add(m);
    }
    return out;
  }

  public Map<String,Object> patchUser(UUID id, String name, List<String> roles, Boolean disabled){
    sec.enforce("user:patch", "user", null, null);
    String sql = "UPDATE app_user SET updated_at=now()";
    if (name != null) sql += ", name=:name";
    if (roles != null) sql += ", roles=:roles";
    if (disabled != null) sql += ", disabled=:disabled";
    sql += " WHERE id=:id";
    var q = em.createNativeQuery(sql).setParameter("id", id);
    if (name != null) q.setParameter("name", name);
    if (roles != null) q.setParameter("roles", roles.toArray(new String[0]));
    if (disabled != null) q.setParameter("disabled", disabled);
    q.executeUpdate();
    return getUser(id);
  }

  public void replaceClaims(UUID id, UUID orgId, List<String> territories, List<String> channels, List<String> scopesList){
    sec.enforce("user:claims", "user", null, orgId==null?null:orgId.toString());
    em.createNativeQuery("UPDATE app_user SET org_id=:org, territories=:terr, channels=:ch, scopes=:sc WHERE id=:id")
      .setParameter("id", id)
      .setParameter("org", orgId)
      .setParameter("terr", territories==null? new String[]{} : territories.toArray(new String[0]))
      .setParameter("ch", channels==null? new String[]{} : channels.toArray(new String[0]))
      .setParameter("sc", scopesList==null? new String[]{} : scopesList.toArray(new String[0]))
      .executeUpdate();
    // enqueue Keycloak sync job
    em.createNativeQuery("INSERT INTO admin_kc_sync_job(user_id, requested_by) VALUES (:u,:rb)")
      .setParameter("u", id).setParameter("rb", scope.getUserId()).executeUpdate();
  }
}

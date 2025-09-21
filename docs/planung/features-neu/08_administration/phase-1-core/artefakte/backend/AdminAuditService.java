package de.freshplan.admin.audit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.*;

import de.freshplan.security.ScopeContext;

@ApplicationScoped
public class AdminAuditService {

  @Inject EntityManager em;
  @Inject ScopeContext scope;

  public void log(String action, String resourceType, String resourceId, String riskTier, String reason, Object before, Object after){
    em.createNativeQuery("INSERT INTO admin_audit(tenant_id, org_id, actor_user_id, actor_roles, actor_territories, action, resource_type, resource_id, risk_tier, reason, before_json, after_json, correlation_id) " +
      "VALUES (:t,:o,:u,:roles,:terr,:a,:rt,:rid, CAST(:tier AS risk_tier_enum), :rea, CAST(:bef AS jsonb), CAST(:aft AS jsonb), :cid)")
      .setParameter("t", scope.getTenantId())
      .setParameter("o", scope.getOrgId())
      .setParameter("u", scope.getUserId())
      .setParameter("roles", scope.getRoles()==null? new String[]{}: scope.getRoles().toArray(new String[0]))
      .setParameter("terr", scope.getTerritories()==null? new String[]{}: scope.getTerritories().toArray(new String[0]))
      .setParameter("a", action).setParameter("rt", resourceType).setParameter("rid", resourceId)
      .setParameter("tier", riskTier).setParameter("rea", reason)
      .setParameter("bef", before==null? "null" : before.toString())
      .setParameter("aft", after==null? "null" : after.toString())
      .setParameter("cid", java.util.UUID.randomUUID())
      .executeUpdate();
  }

  public List<Map<String,Object>> search(String q, String resourceType, java.time.OffsetDateTime from, java.time.OffsetDateTime to, int limit){
    String sql = "SELECT id, action, resource_type, resource_id, risk_tier::text, created_at FROM admin_audit WHERE 1=1";
    if (q != null && !q.isBlank()) sql += " AND (action ILIKE :q OR resource_id ILIKE :q)";
    if (resourceType != null && !resourceType.isBlank()) sql += " AND resource_type = :rt";
    if (from != null) sql += " AND created_at >= :f";
    if (to != null) sql += " AND created_at <= :t";
    sql += " ORDER BY created_at DESC LIMIT :limit";
    Query query = em.createNativeQuery(sql);
    if (q != null && !q.isBlank()) query.setParameter("q", "%"+q+"%");
    if (resourceType != null && !resourceType.isBlank()) query.setParameter("rt", resourceType);
    if (from != null) query.setParameter("f", java.sql.Timestamp.from(from.toInstant()));
    if (to != null) query.setParameter("t", java.sql.Timestamp.from(to.toInstant()));
    query.setParameter("limit", Math.max(1, Math.min(limit, 200)));
    @SuppressWarnings("unchecked") List<Object[]> rows = query.getResultList();
    List<Map<String,Object>> out = new ArrayList<>();
    for (Object[] r : rows){
      Map<String,Object> m = new LinkedHashMap<>();
      m.put("id", r[0]); m.put("action", r[1]); m.put("resourceType", r[2]); m.put("resourceId", r[3]); m.put("riskTier", r[4]); m.put("createdAt", r[5]);
      out.add(m);
    }
    return out;
  }
}

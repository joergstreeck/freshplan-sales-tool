package de.freshplan.leads.repo;

import de.freshplan.leads.domain.LeadEntity;
import de.freshplan.leads.domain.LeadStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.*;

@ApplicationScoped
public class LeadRepository {

  @Inject EntityManager em;

  public void persist(LeadEntity e) { em.persist(e); }

  public List<LeadEntity> findPage(String q, LeadStatus status, List<String> territories, String territoryFilter,
                                   UUID cursor, int limit) {
    StringBuilder jpql = new StringBuilder();
    jpql.append("SELECT l FROM LeadEntity l WHERE 1=1");
    Map<String,Object> params = new HashMap<>();
    if (q != null && !q.isBlank()) { jpql.append(" AND lower(l.name) LIKE :q"); params.put("q", "%"+q.toLowerCase()+"%"); }
    if (status != null) { jpql.append(" AND l.status = :status"); params.put("status", status); }
    if (territories != null && !territories.isEmpty()) {
      jpql.append(" AND l.territory IN :scoped"); params.put("scoped", territories);
    }
    if (territoryFilter != null && !territoryFilter.isBlank()) {
      jpql.append(" AND l.territory = :territory"); params.put("territory", territoryFilter);
    }
    if (cursor != null) {
      jpql.append(" AND l.id > :cursor"); params.put("cursor", cursor);
    }
    jpql.append(" ORDER BY l.id ASC");
    TypedQuery<LeadEntity> tq = em.createQuery(jpql.toString(), LeadEntity.class);
    for (var e : params.entrySet()) tq.setParameter(e.getKey(), e.getValue());
    tq.setMaxResults(Math.max(1, Math.min(200, limit)));
    return tq.getResultList();
  }
}

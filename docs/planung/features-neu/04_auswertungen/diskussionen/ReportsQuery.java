package com.freshplan.reports;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.time.LocalDate;
import java.util.*;

@ApplicationScoped
public class ReportsQuery {

  @Inject EntityManager em;
  @Inject ScopeContext scope;

  public Map<String, Object> fetchCustomerAnalytics(String segment, String territory,
                                                    String seasonFrom, String seasonTo,
                                                    String renewalBefore, int limit, String cursor) {

    StringBuilder sql = new StringBuilder();
    sql.append("""
      SELECT c.id as customer_id, c.name, c.territory,
             hp.sample_status, hp.roi_bucket, hp.roi_value,
             hp.decision_maker_count, hp.has_exec_alignment,
             hp.season_start, hp.season_end, hp.renewal_date, hp.exclusivity
      FROM customers c
      LEFT JOIN cm_customer_hot_proj hp ON hp.customer_id = c.id
      WHERE 1=1
    """);

    Map<String,Object> params = new HashMap<>();

    // ABAC Scoping (territory / chain) from ScopeContext
    if (!scope.getTerritories().isEmpty()) {
      sql.append(" AND c.territory = ANY(:territories)");
      params.put("territories", scope.getTerritories().toArray(new String[0]));
    }
    if (scope.getChainId() != null) {
      sql.append(" AND c.chain_id = :chainId");
      params.put("chainId", scope.getChainId());
    }

    if (territory != null && !territory.isBlank()) {
      sql.append(" AND c.territory = :territory"); params.put("territory", territory);
    }
    if (seasonFrom != null) { sql.append(" AND hp.season_start >= :seasonFrom"); params.put("seasonFrom", LocalDate.parse(seasonFrom)); }
    if (seasonTo != null)   { sql.append(" AND hp.season_end   <= :seasonTo");   params.put("seasonTo", LocalDate.parse(seasonTo)); }
    if (renewalBefore != null) { sql.append(" AND hp.renewal_date <= :renewalBefore"); params.put("renewalBefore", LocalDate.parse(renewalBefore)); }

    // rudimentary cursor (created_at or id)
    if (cursor != null && !cursor.isBlank()) {
      sql.append(" AND c.id > :cursor"); params.put("cursor", UUID.fromString(cursor));
    }

    sql.append(" ORDER BY c.id ASC LIMIT :limit");
    params.put("limit", Math.max(1, Math.min(200, limit)));

    Query q = em.createNativeQuery(sql.toString());
    params.forEach(q::setParameter);
    @SuppressWarnings("unchecked")
    List<Object[]> rows = q.getResultList();

    List<Map<String,Object>> items = new ArrayList<>();
    String nextCursor = null;
    for (Object[] r : rows) {
      Map<String,Object> m = new LinkedHashMap<>();
      m.put("customerId", r[0]);
      m.put("name", r[1]);
      m.put("territory", r[2]);
      m.put("sampleStatus", r[3]);
      m.put("roiBucket", r[4]);
      m.put("roiValue", r[5]);
      m.put("decisionMakerCount", r[6]);
      m.put("hasExecAlignment", r[7]);
      m.put("seasonStart", r[8]);
      m.put("seasonEnd", r[9]);
      m.put("renewalDate", r[10]);
      m.put("exclusivity", r[11]);
      items.add(m);
      nextCursor = r[0].toString();
    }
    Map<String,Object> page = new HashMap<>();
    page.put("items", items);
    page.put("nextCursor", rows.size() == params.get("limit") ? nextCursor : null);
    return page;
  }

  public Map<String,Object> fetchActivityStats(String kind, String from, String to) {
    StringBuilder sql = new StringBuilder();
    sql.append("""
      SELECT DATE(a.occurred_at) AS day, COUNT(*) AS cnt
      FROM activities a
      WHERE 1=1
    """);
    Map<String,Object> params = new HashMap<>();

    // ABAC by territory via customer join if territories present
    if (!scope.getTerritories().isEmpty()) {
      sql.append(" AND EXISTS (SELECT 1 FROM customers c WHERE c.id=a.customer_id AND c.territory = ANY(:territories))");
      params.put("territories", scope.getTerritories().toArray(new String[0]));
    }
    if (kind != null && !kind.isBlank()) { sql.append(" AND a.kind = :kind"); params.put("kind", kind); }
    if (from != null) { sql.append(" AND a.occurred_at >= :from"); params.put("from", java.sql.Date.valueOf(from)); }
    if (to   != null) { sql.append(" AND a.occurred_at <= :to");   params.put("to",   java.sql.Date.valueOf(to)); }

    sql.append(" GROUP BY 1 ORDER BY 1 ASC");
    Query q = em.createNativeQuery(sql.toString());
    params.forEach(q::setParameter);

    @SuppressWarnings("unchecked")
    List<Object[]> rows = q.getResultList();
    List<Map<String,Object>> byDay = new ArrayList<>();
    int total = 0;
    for (Object[] r : rows) {
      int cnt = ((Number)r[1]).intValue();
      byDay.add(Map.of("day", String.valueOf(r[0]), "count", cnt));
      total += cnt;
    }
    return Map.of("kind", kind, "from", from, "to", to, "total", total, "byDay", byDay);
  }

  public Map<String,Object> fetchCostOverview(Object costsService, String range) {
    // Adapter zu bestehendem CostStatistics (hier als Object, konkreter Typ im Projekt)
    // Pseudocode: totals & breakdown ziehen
    Map<String,Double> totals = new LinkedHashMap<>();
    List<Map<String,Object>> breakdown = new ArrayList<>();
    // TODO: reale Delegation an CostStatistics
    totals.put("total", 0.0);
    breakdown.add(Map.of("category","n/a","amount",0.0));
    return Map.of("range", range, "totals", totals, "breakdown", breakdown);
  }
}

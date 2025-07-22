package de.freshplan.domain.opportunity.repository;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.user.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Opportunity Repository
 *
 * <p>Data Access Layer für Opportunities mit erweiterten Pipeline-spezifischen Queries. Unterstützt
 * Filterung, Sortierung und Business Intelligence Queries.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class OpportunityRepository implements PanacheRepositoryBase<Opportunity, UUID> {

  // =====================================
  // BASIC CRUD OPERATIONS
  // =====================================

  /** Findet alle aktiven Opportunities (nicht abgeschlossen) mit Paginierung */
  public List<Opportunity> findAllActive(Page page) {
    return find(
            "stage NOT IN (?1, ?2)",
            Sort.by("stageChangedAt").descending(),
            OpportunityStage.CLOSED_WON,
            OpportunityStage.CLOSED_LOST)
        .page(page)
        .list();
  }

  /** Findet alle Opportunities eines bestimmten Verkäufers */
  public List<Opportunity> findByAssignedTo(User assignedTo) {
    return find("assignedTo", Sort.by("stageChangedAt").descending(), assignedTo).list();
  }

  /** Findet Opportunities nach Stage */
  public List<Opportunity> findByStage(OpportunityStage stage) {
    return find("stage", Sort.by("stageChangedAt").descending(), stage).list();
  }

  // =====================================
  // PIPELINE-SPECIFIC QUERIES
  // =====================================

  /** Pipeline-Übersicht: Gruppiert Opportunities nach Stage */
  public List<Object[]> getPipelineOverview() {
    return getEntityManager()
        .createQuery(
            """
                    SELECT o.stage, COUNT(o), COALESCE(SUM(o.expectedValue), 0)
                    FROM Opportunity o
                    WHERE o.stage IN (:activeStages)
                    GROUP BY o.stage
                    ORDER BY
                        CASE o.stage
                            WHEN 'NEW_LEAD' THEN 1
                            WHEN 'QUALIFICATION' THEN 2
                            WHEN 'NEEDS_ANALYSIS' THEN 3
                            WHEN 'PROPOSAL' THEN 4
                            WHEN 'NEGOTIATION' THEN 5
                            ELSE 6
                        END
                    """,
            Object[].class)
        .setParameter(
            "activeStages",
            List.of(
                OpportunityStage.NEW_LEAD,
                OpportunityStage.QUALIFICATION,
                OpportunityStage.NEEDS_ANALYSIS,
                OpportunityStage.PROPOSAL,
                OpportunityStage.NEGOTIATION))
        .getResultList();
  }

  /** Findet Opportunities mit hoher Priorität (hoher Wert, baldiges Close-Datum) */
  public List<Opportunity> findHighPriorityOpportunities(int limit) {
    return getEntityManager()
        .createQuery(
            """
                    SELECT o FROM Opportunity o
                    WHERE o.stage IN (:activeStages)
                    AND o.expectedValue > :minValue
                    AND (o.expectedCloseDate IS NULL OR o.expectedCloseDate <= :nextWeek)
                    ORDER BY o.expectedValue DESC, o.expectedCloseDate ASC
                    """,
            Opportunity.class)
        .setParameter(
            "activeStages", List.of(OpportunityStage.PROPOSAL, OpportunityStage.NEGOTIATION))
        .setParameter("minValue", new BigDecimal("1000"))
        .setParameter("nextWeek", LocalDate.now().plusWeeks(1))
        .setMaxResults(limit)
        .getResultList();
  }

  /** Findet überfällige Opportunities (Close-Datum überschritten) */
  public List<Opportunity> findOverdueOpportunities() {
    return find(
            """
                stage IN (?1, ?2, ?3, ?4, ?5)
                AND expectedCloseDate IS NOT NULL
                AND expectedCloseDate < ?6
                """,
            Sort.by("expectedCloseDate").ascending(),
            OpportunityStage.NEW_LEAD,
            OpportunityStage.QUALIFICATION,
            OpportunityStage.NEEDS_ANALYSIS,
            OpportunityStage.PROPOSAL,
            OpportunityStage.NEGOTIATION,
            LocalDate.now())
        .list();
  }

  // =====================================
  // BUSINESS INTELLIGENCE QUERIES
  // =====================================

  /** Sales Forecast: Berechnet erwarteten Umsatz basierend auf Wahrscheinlichkeit */
  public BigDecimal calculateForecast() {
    Double result =
        getEntityManager()
            .createQuery(
                """
                    SELECT COALESCE(SUM(o.expectedValue * o.probability / 100.0), 0.0)
                    FROM Opportunity o
                    WHERE o.stage IN (:activeStages)
                    """,
                Double.class)
            .setParameter(
                "activeStages",
                List.of(
                    OpportunityStage.NEW_LEAD,
                    OpportunityStage.QUALIFICATION,
                    OpportunityStage.NEEDS_ANALYSIS,
                    OpportunityStage.PROPOSAL,
                    OpportunityStage.NEGOTIATION))
            .getSingleResult();

    return BigDecimal.valueOf(result);
  }

  /** Conversion Rate: Prozentsatz gewonnener Opportunities */
  public Double getConversionRate() {
    Object[] result =
        getEntityManager()
            .createQuery(
                """
                    SELECT
                        COUNT(CASE WHEN o.stage = :won THEN 1 END) as won,
                        COUNT(CASE WHEN o.stage IN (:closed) THEN 1 END) as total
                    FROM Opportunity o
                    WHERE o.stage IN (:closed)
                    """,
                Object[].class)
            .setParameter("won", OpportunityStage.CLOSED_WON)
            .setParameter(
                "closed", List.of(OpportunityStage.CLOSED_WON, OpportunityStage.CLOSED_LOST))
            .getSingleResult();

    Long won = (Long) result[0];
    Long total = (Long) result[1];

    return total > 0 ? (won.doubleValue() / total.doubleValue() * 100) : 0.0;
  }

  /** Findet Opportunities die lange in einer Stage stehen (Bottleneck Analysis) */
  public List<Opportunity> findStagnantOpportunities(int daysInStage) {
    return getEntityManager()
        .createQuery(
            """
                    SELECT o FROM Opportunity o
                    WHERE o.stage IN (:activeStages)
                    AND o.stageChangedAt < :thresholdDate
                    ORDER BY o.stageChangedAt ASC
                    """,
            Opportunity.class)
        .setParameter(
            "activeStages",
            List.of(
                OpportunityStage.NEW_LEAD,
                OpportunityStage.QUALIFICATION,
                OpportunityStage.NEEDS_ANALYSIS,
                OpportunityStage.PROPOSAL,
                OpportunityStage.NEGOTIATION))
        .setParameter("thresholdDate", LocalDate.now().minusDays(daysInStage).atStartOfDay())
        .getResultList();
  }

  // =====================================
  // SEARCH AND FILTER
  // =====================================

  /** Erweiterte Suche mit mehreren Filtern */
  public List<Opportunity> findWithFilters(
      String searchTerm,
      OpportunityStage stage,
      User assignedTo,
      LocalDate fromDate,
      LocalDate toDate,
      Page page) {
    StringBuilder query = new StringBuilder("SELECT o FROM Opportunity o WHERE 1=1");

    if (searchTerm != null && !searchTerm.trim().isEmpty()) {
      query.append(" AND (LOWER(o.name) LIKE LOWER(?1) OR LOWER(o.description) LIKE LOWER(?1))");
    }
    if (stage != null) {
      query.append(" AND o.stage = ?2");
    }
    if (assignedTo != null) {
      query.append(" AND o.assignedTo = ?3");
    }
    if (fromDate != null) {
      query.append(" AND o.createdAt >= ?4");
    }
    if (toDate != null) {
      query.append(" AND o.createdAt <= ?5");
    }

    query.append(" ORDER BY o.stageChangedAt DESC");

    var typedQuery = getEntityManager().createQuery(query.toString(), Opportunity.class);

    int paramIndex = 1;
    if (searchTerm != null && !searchTerm.trim().isEmpty()) {
      typedQuery.setParameter(paramIndex++, "%" + searchTerm.trim() + "%");
    }
    if (stage != null) {
      typedQuery.setParameter(paramIndex++, stage);
    }
    if (assignedTo != null) {
      typedQuery.setParameter(paramIndex++, assignedTo);
    }
    if (fromDate != null) {
      typedQuery.setParameter(paramIndex++, fromDate.atStartOfDay());
    }
    if (toDate != null) {
      typedQuery.setParameter(paramIndex++, toDate.plusDays(1).atStartOfDay());
    }

    return typedQuery
        .setFirstResult(page.index * page.size)
        .setMaxResults(page.size)
        .getResultList();
  }

  /** Get performance metrics for a specific user */
  public Map<String, Object> getUserPerformanceMetrics(User user) {
    Map<String, Object> metrics = new HashMap<>();

    // Total opportunities
    Long totalOpportunities = count("assignedTo", user);
    metrics.put("totalOpportunities", totalOpportunities);

    // Won opportunities
    Long wonOpportunities =
        count("assignedTo = ?1 and stage = ?2", user, OpportunityStage.CLOSED_WON);
    metrics.put("wonOpportunities", wonOpportunities);

    // Total value
    BigDecimal totalValue =
        getEntityManager()
            .createQuery(
                "SELECT COALESCE(SUM(o.expectedValue), 0) FROM Opportunity o WHERE o.assignedTo = :user",
                BigDecimal.class)
            .setParameter("user", user)
            .getSingleResult();
    metrics.put("totalValue", totalValue);

    // Won value
    BigDecimal wonValue =
        getEntityManager()
            .createQuery(
                "SELECT COALESCE(SUM(o.expectedValue), 0) FROM Opportunity o WHERE o.assignedTo = :user AND o.stage = :stage",
                BigDecimal.class)
            .setParameter("user", user)
            .setParameter("stage", OpportunityStage.CLOSED_WON)
            .getSingleResult();
    metrics.put("wonValue", wonValue);

    return metrics;
  }

  /** Calculate win rate for a user */
  public Double getWinRateForUser(User user) {
    Long totalClosed =
        count(
            "assignedTo = ?1 and (stage = ?2 or stage = ?3)",
            user,
            OpportunityStage.CLOSED_WON,
            OpportunityStage.CLOSED_LOST);

    if (totalClosed == 0) {
      return 0.0;
    }

    Long won = count("assignedTo = ?1 and stage = ?2", user, OpportunityStage.CLOSED_WON);
    return (double) won / totalClosed * 100;
  }

  /** Find top opportunities by value */
  public List<Opportunity> findTopOpportunitiesByValue(int limit) {
    return getEntityManager()
        .createQuery(
            """
                    SELECT o FROM Opportunity o
                    WHERE o.stage NOT IN (:closedStages)
                    ORDER BY o.expectedValue DESC NULLS LAST
                    """,
            Opportunity.class)
        .setParameter(
            "closedStages", List.of(OpportunityStage.CLOSED_WON, OpportunityStage.CLOSED_LOST))
        .setMaxResults(limit)
        .getResultList();
  }

  /** Find opportunities requiring attention (stale opportunities) */
  public List<Opportunity> findOpportunitiesRequiringAttention() {
    LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

    return getEntityManager()
        .createQuery(
            """
                    SELECT o FROM Opportunity o
                    WHERE o.stage NOT IN (:closedStages)
                    AND o.stageChangedAt < :cutoffDate
                    ORDER BY o.stageChangedAt ASC
                    """,
            Opportunity.class)
        .setParameter(
            "closedStages", List.of(OpportunityStage.CLOSED_WON, OpportunityStage.CLOSED_LOST))
        .setParameter("cutoffDate", thirtyDaysAgo)
        .getResultList();
  }

  /** Find opportunities by customer */
  public List<Opportunity> findByCustomer(Customer customer) {
    return list("customer", customer);
  }

  /** Get stage distribution */
  public Map<OpportunityStage, Long> getStageDistribution() {
    List<Object[]> results =
        getEntityManager()
            .createQuery(
                "SELECT o.stage, COUNT(o) FROM Opportunity o GROUP BY o.stage", Object[].class)
            .getResultList();

    Map<OpportunityStage, Long> distribution = new HashMap<>();
    for (Object[] result : results) {
      distribution.put((OpportunityStage) result[0], (Long) result[1]);
    }
    return distribution;
  }

  /** Find opportunities by expected close date range */
  public List<Opportunity> findByExpectedCloseDateBetween(LocalDate startDate, LocalDate endDate) {
    return find("expectedCloseDate >= ?1 and expectedCloseDate <= ?2", startDate, endDate).list();
  }

  /** Find opportunities by created at range */
  public List<Opportunity> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
    return find("createdAt >= ?1 and createdAt <= ?2", startDate, endDate).list();
  }
}

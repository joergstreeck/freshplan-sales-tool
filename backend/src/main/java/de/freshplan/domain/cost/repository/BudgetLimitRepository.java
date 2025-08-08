package de.freshplan.domain.cost.repository;

import de.freshplan.domain.cost.entity.BudgetLimit;
import de.freshplan.domain.cost.entity.BudgetPeriod;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repository für Budget Limits mit spezifischen Abfragen */
@ApplicationScoped
public class BudgetLimitRepository implements PanacheRepositoryBase<BudgetLimit, UUID> {

  /** Findet alle aktiven Budget Limits */
  public List<BudgetLimit> findActive() {
    return find("active = true").list();
  }

  /** Findet Budget Limit für globalen Scope und Period */
  public Optional<BudgetLimit> findGlobalLimit(BudgetPeriod period) {
    return find("scope = 'global' AND period = ?1 AND active = true", period).firstResultOptional();
  }

  /** Findet Budget Limit für einen Service */
  public Optional<BudgetLimit> findServiceLimit(String service, BudgetPeriod period) {
    return find(
            "scope = 'service' AND scopeValue = ?1 AND period = ?2 AND active = true",
            service,
            period)
        .firstResultOptional();
  }

  /** Findet Budget Limit für ein Feature */
  public Optional<BudgetLimit> findFeatureLimit(String feature, BudgetPeriod period) {
    return find(
            "scope = 'feature' AND scopeValue = ?1 AND period = ?2 AND active = true",
            feature,
            period)
        .firstResultOptional();
  }

  /** Findet Budget Limit für einen User */
  public Optional<BudgetLimit> findUserLimit(String userId, BudgetPeriod period) {
    return find(
            "scope = 'user' AND scopeValue = ?1 AND period = ?2 AND active = true", userId, period)
        .firstResultOptional();
  }

  /** Findet alle relevanten Limits für einen Scope und Wert */
  public List<BudgetLimit> findRelevantLimits(String scope, String scopeValue) {
    if (scopeValue == null) {
      return find("scope = ?1 AND active = true", scope).list();
    }
    return find("scope = ?1 AND scopeValue = ?2 AND active = true", scope, scopeValue).list();
  }

  /** Findet Limits nach Period */
  public List<BudgetLimit> findByPeriod(BudgetPeriod period) {
    return find("period = ?1 AND active = true", period).list();
  }

  /** Deaktiviert ein Budget Limit */
  public void deactivate(UUID limitId) {
    update("active = false WHERE id = ?1", limitId);
  }

  /** Aktiviert ein Budget Limit */
  public void activate(UUID limitId) {
    update("active = true WHERE id = ?1", limitId);
  }

  /** Findet Limits, die eine Alert-Schwelle überschreiten könnten */
  public List<BudgetLimit> findLimitsForAlert() {
    return find("active = true AND alertThreshold < 1.0").list();
  }

  /** Zählt aktive Limits pro Scope */
  @SuppressWarnings("unchecked")
  public List<Object[]> countLimitsByScope() {
    return getEntityManager()
        .createQuery("SELECT scope, COUNT(*) FROM BudgetLimit WHERE active = true GROUP BY scope")
        .getResultList();
  }
}

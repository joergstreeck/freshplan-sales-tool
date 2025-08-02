package de.freshplan.domain.cost.repository;

import de.freshplan.domain.cost.entity.CostTransaction;
import de.freshplan.domain.cost.entity.TransactionStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repository für Cost Transactions mit spezifischen Abfragen für Cost Management
 */
@ApplicationScoped
public class CostTransactionRepository implements PanacheRepositoryBase<CostTransaction, UUID> {

    /**
     * Findet alle Transaktionen in einem Zeitraum
     */
    public List<CostTransaction> findInPeriod(LocalDateTime start, LocalDateTime end) {
        return find("startTime >= ?1 AND startTime < ?2", start, end).list();
    }

    /**
     * Berechnet Gesamtkosten für einen Service in einem Zeitraum
     */
    public BigDecimal getTotalCostByService(String service, LocalDateTime start, LocalDateTime end) {
        return find("SELECT COALESCE(SUM(actualCost), 0) FROM CostTransaction t " +
                   "WHERE t.service = ?1 AND t.startTime >= ?2 AND t.startTime < ?3 AND t.status = ?4",
                   service, start, end, TransactionStatus.COMPLETED)
                   .project(BigDecimal.class)
                   .firstResult();
    }

    /**
     * Berechnet Gesamtkosten für ein Feature in einem Zeitraum
     */
    public BigDecimal getTotalCostByFeature(String feature, LocalDateTime start, LocalDateTime end) {
        return find("SELECT COALESCE(SUM(actualCost), 0) FROM CostTransaction t " +
                   "WHERE t.feature = ?1 AND t.startTime >= ?2 AND t.startTime < ?3 AND t.status = ?4",
                   feature, start, end, TransactionStatus.COMPLETED)
                   .project(BigDecimal.class)
                   .firstResult();
    }

    /**
     * Berechnet Gesamtkosten für einen User in einem Zeitraum
     */
    public BigDecimal getTotalCostByUser(String userId, LocalDateTime start, LocalDateTime end) {
        return find("SELECT COALESCE(SUM(actualCost), 0) FROM CostTransaction t " +
                   "WHERE t.userId = ?1 AND t.startTime >= ?2 AND t.startTime < ?3 AND t.status = ?4",
                   userId, start, end, TransactionStatus.COMPLETED)
                   .project(BigDecimal.class)
                   .firstResult();
    }

    /**
     * Berechnet Gesamtkosten in einem Zeitraum
     */
    public BigDecimal getTotalCost(LocalDateTime start, LocalDateTime end) {
        return find("SELECT COALESCE(SUM(actualCost), 0) FROM CostTransaction t " +
                   "WHERE t.startTime >= ?1 AND t.startTime < ?2 AND t.status = ?3",
                   start, end, TransactionStatus.COMPLETED)
                   .project(BigDecimal.class)
                   .firstResult();
    }

    /**
     * Findet die Top Cost Drivers (Services mit höchsten Kosten)
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> getTopCostDriversByService(LocalDateTime start, LocalDateTime end, int limit) {
        return getEntityManager()
            .createQuery("SELECT t.service, SUM(t.actualCost), COUNT(t) " +
                        "FROM CostTransaction t " +
                        "WHERE t.startTime >= :start AND t.startTime < :end AND t.status = :status " +
                        "GROUP BY t.service " +
                        "ORDER BY SUM(t.actualCost) DESC")
            .setParameter("start", start)
            .setParameter("end", end)
            .setParameter("status", TransactionStatus.COMPLETED)
            .setMaxResults(limit)
            .getResultList();
    }

    /**
     * Findet die Top Cost Drivers (Features mit höchsten Kosten)
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> getTopCostDriversByFeature(LocalDateTime start, LocalDateTime end, int limit) {
        return getEntityManager()
            .createQuery("SELECT t.feature, SUM(t.actualCost), COUNT(t) " +
                        "FROM CostTransaction t " +
                        "WHERE t.startTime >= :start AND t.startTime < :end AND t.status = :status " +
                        "GROUP BY t.feature " +
                        "ORDER BY SUM(t.actualCost) DESC")
            .setParameter("start", start)
            .setParameter("end", end)
            .setParameter("status", TransactionStatus.COMPLETED)
            .setMaxResults(limit)
            .getResultList();
    }

    /**
     * Findet Transaktionen, die über dem Budget liegen
     */
    public List<CostTransaction> findOverBudgetTransactions(LocalDateTime start, LocalDateTime end) {
        return find("startTime >= ?1 AND startTime < ?2 AND actualCost > estimatedCost", start, end).list();
    }

    /**
     * Berechnet durchschnittliche Token-Kosten pro Service
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> getAverageTokenCostsByService(LocalDateTime start, LocalDateTime end) {
        return getEntityManager()
            .createQuery("SELECT t.service, t.model, AVG(t.actualCost / NULLIF(t.tokensUsed, 0)) " +
                        "FROM CostTransaction t " +
                        "WHERE t.startTime >= :start AND t.startTime < :end AND t.status = :status " +
                        "AND t.tokensUsed > 0 " +
                        "GROUP BY t.service, t.model " +
                        "ORDER BY t.service, t.model")
            .setParameter("start", start)
            .setParameter("end", end)
            .setParameter("status", TransactionStatus.COMPLETED)
            .getResultList();
    }

    /**
     * Findet laufende Transaktionen (für Monitoring)
     */
    public List<CostTransaction> findRunningTransactions() {
        return find("status = ?1", TransactionStatus.STARTED).list();
    }

    /**
     * Findet fehlgeschlagene Transaktionen in einem Zeitraum
     */
    public List<CostTransaction> findFailedTransactions(LocalDateTime start, LocalDateTime end) {
        return find("startTime >= ?1 AND startTime < ?2 AND status = ?3", start, end, TransactionStatus.FAILED).list();
    }

    /**
     * Zählt Transaktionen pro Status in einem Zeitraum
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> getTransactionCountsByStatus(LocalDateTime start, LocalDateTime end) {
        return getEntityManager()
            .createQuery("SELECT t.status, COUNT(t) " +
                        "FROM CostTransaction t " +
                        "WHERE t.startTime >= :start AND t.startTime < :end " +
                        "GROUP BY t.status")
            .setParameter("start", start)
            .setParameter("end", end)
            .getResultList();
    }
}
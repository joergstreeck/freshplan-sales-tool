package de.freshplan.domain.cost.service;

import de.freshplan.domain.cost.entity.BudgetLimit;
import de.freshplan.domain.cost.entity.CostTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service für Cost-basierte Alerts und Notifications
 */
@ApplicationScoped
public class CostAlertService {
    
    private static final Logger LOG = LoggerFactory.getLogger(CostAlertService.class);

    /**
     * Sendet Budget Alert bei Überschreitung von Schwellenwerten
     */
    public void sendBudgetAlert(BudgetLimit limit, BigDecimal currentUsage) {
        BigDecimal percentage = currentUsage
            .divide(limit.limitAmount, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

        String alertLevel = determineAlertLevel(limit, currentUsage);
        
        LOG.warn("BUDGET ALERT [{}] - {}: {}% (€{}/€{}) - Aktueller Verbrauch: €{}", 
            alertLevel,
            limit.getDisplayName(),
            percentage.setScale(1, RoundingMode.HALF_UP),
            currentUsage.setScale(2, RoundingMode.HALF_UP),
            limit.limitAmount.setScale(2, RoundingMode.HALF_UP),
            currentUsage.setScale(2, RoundingMode.HALF_UP)
        );

        // TODO: Integration mit Notification Service
        // notificationService.send(createBudgetAlert(limit, currentUsage, alertLevel));
    }

    /**
     * Sendet Alert bei Over-Budget Transaktionen
     */
    public void sendOverBudgetAlert(CostTransaction transaction) {
        BigDecimal difference = transaction.getCostDifference();
        BigDecimal percentageOver = difference
            .divide(transaction.estimatedCost, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

        LOG.warn("COST OVERRUN - Transaction {}: {} {} kostet €{} statt geschätzt €{} (+{}%)",
            transaction.id,
            transaction.service,
            transaction.feature,
            transaction.actualCost.setScale(2, RoundingMode.HALF_UP),
            transaction.estimatedCost.setScale(2, RoundingMode.HALF_UP),
            percentageOver.setScale(1, RoundingMode.HALF_UP)
        );

        // TODO: Integration mit Notification Service für Over-Budget Alerts
    }

    /**
     * Sendet Daily Cost Summary
     */
    public void sendDailySmary(BigDecimal totalCost, BigDecimal budgetLimit, int transactionCount) {
        if (budgetLimit != null) {
            BigDecimal percentage = totalCost
                .divide(budgetLimit, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

            LOG.info("DAILY COST SUMMARY - Gesamt: €{} / €{} ({}%) - {} Transaktionen",
                totalCost.setScale(2, RoundingMode.HALF_UP),
                budgetLimit.setScale(2, RoundingMode.HALF_UP),
                percentage.setScale(1, RoundingMode.HALF_UP),
                transactionCount
            );
        } else {
            LOG.info("DAILY COST SUMMARY - Gesamt: €{} - {} Transaktionen (kein Budget-Limit gesetzt)",
                totalCost.setScale(2, RoundingMode.HALF_UP),
                transactionCount
            );
        }

        // TODO: Integration mit Email Service für Daily Summary
    }

    /**
     * Sendet Service-Degradation Alert
     */
    public void sendServiceDegradationAlert(String service, String feature, String reason) {
        LOG.info("SERVICE DEGRADATION - {} {} wurde degradiert: {}", service, feature, reason);
        
        // TODO: Integration mit User Notification Service
        // Zeigt user-freundliche Meldung im Frontend an
    }

    /**
     * Sendet Feature-Deaktivierung Alert
     */
    public void sendFeatureDisabledAlert(String feature, String reason) {
        LOG.warn("FEATURE DISABLED - {} wurde deaktiviert: {}", feature, reason);
        
        // TODO: Integration mit Admin Alert System
    }

    // Private Helper Methods

    private String determineAlertLevel(BudgetLimit limit, BigDecimal currentUsage) {
        if (limit.shouldHardStop(currentUsage)) {
            return "CRITICAL";
        } else if (currentUsage.compareTo(limit.limitAmount.multiply(BigDecimal.valueOf(0.9))) >= 0) {
            return "HIGH";
        } else if (limit.shouldAlert(currentUsage)) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    // TODO: Weitere Alert-Typen implementieren:
    // - sendHighUsageAlert() für ungewöhnlich hohe Nutzung
    // - sendCostOptimizationSuggestion() für Optimierungsvorschläge
    // - sendServiceHealthAlert() für Service-Performance-Probleme
}
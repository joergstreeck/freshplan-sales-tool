package de.freshplan.domain.cost.service;

import de.freshplan.domain.cost.entity.*;
import de.freshplan.domain.cost.repository.BudgetLimitRepository;
import de.freshplan.domain.cost.repository.CostTransactionRepository;
import de.freshplan.domain.cost.service.dto.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Zentraler Cost Tracking Service für das Cost Management System
 * 
 * Implementiert die Kern-Funktionalitäten:
 * - Transaction Tracking
 * - Budget Checking
 * - Cost Analysis
 * - Alert Management
 */
@ApplicationScoped
@Transactional
public class CostTrackingService {

    @Inject
    CostTransactionRepository transactionRepository;

    @Inject
    BudgetLimitRepository budgetRepository;

    @Inject
    CostAlertService alertService;

    /**
     * Startet eine neue Cost Transaction
     */
    public CostTransactionContext startTransaction(
            String service, 
            String feature, 
            String model, 
            BigDecimal estimatedCost,
            String userId) {
        
        // Budget-Check vor Transaction-Start
        BudgetCheckResult budgetCheck = checkBudget(service, feature, estimatedCost, userId);
        if (!budgetCheck.canAfford()) {
            throw new BudgetExceededException(budgetCheck.getReason());
        }

        CostTransaction transaction = CostTransaction.startTransaction(service, feature, model, estimatedCost);
        transaction.userId = userId;
        transaction.persist();

        return new CostTransactionContext(transaction, this);
    }

    /**
     * Komplettiert eine Transaction
     */
    public void completeTransaction(UUID transactionId, BigDecimal actualCost, Integer tokensUsed) {
        CostTransaction transaction = transactionRepository.findById(transactionId);
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction not found: " + transactionId);
        }

        transaction.complete(actualCost, tokensUsed);
        
        // Prüfe auf Budget-Überschreitungen nach tatsächlichen Kosten
        checkAndSendAlerts(transaction);
    }

    /**
     * Markiert eine Transaction als fehlgeschlagen
     */
    public void failTransaction(UUID transactionId, String errorMessage) {
        CostTransaction transaction = transactionRepository.findById(transactionId);
        if (transaction != null) {
            transaction.fail(errorMessage);
        }
    }

    /**
     * Prüft Budget-Verfügbarkeit für eine geplante Transaktion
     */
    public BudgetCheckResult checkBudget(String service, String feature, BigDecimal estimatedCost, String userId) {
        LocalDateTime now = LocalDateTime.now();
        
        // Prüfe verschiedene Budget-Ebenen (hierarchisch)
        BudgetCheckResult result = new BudgetCheckResult();

        // 1. Global Daily Limit
        Optional<BudgetLimit> globalDaily = budgetRepository.findGlobalLimit(BudgetPeriod.DAILY);
        if (globalDaily.isPresent()) {
            BudgetLimit limit = globalDaily.get();
            LocalDateTime dayStart = BudgetPeriod.DAILY.getPeriodStart(now);
            LocalDateTime dayEnd = BudgetPeriod.DAILY.getPeriodEnd(now);
            BigDecimal usedToday = transactionRepository.getTotalCost(dayStart, dayEnd);
            
            if (limit.shouldHardStop(usedToday.add(estimatedCost))) {
                result.deny("Globales Tagesbudget erreicht");
                return result;
            }
            
            if (limit.shouldAlert(usedToday.add(estimatedCost))) {
                result.addWarning("Globales Tagesbudget bei 80%");
            }
        }

        // 2. Service-spezifisches Limit
        Optional<BudgetLimit> serviceLimit = budgetRepository.findServiceLimit(service, BudgetPeriod.DAILY);
        if (serviceLimit.isPresent()) {
            BudgetLimit limit = serviceLimit.get();
            LocalDateTime dayStart = BudgetPeriod.DAILY.getPeriodStart(now);
            LocalDateTime dayEnd = BudgetPeriod.DAILY.getPeriodEnd(now);
            BigDecimal usedToday = transactionRepository.getTotalCostByService(service, dayStart, dayEnd);
            
            if (limit.shouldHardStop(usedToday.add(estimatedCost))) {
                result.deny("Service-Budget für " + service + " erreicht");
                return result;
            }
        }

        // 3. Feature-spezifisches Limit
        Optional<BudgetLimit> featureLimit = budgetRepository.findFeatureLimit(feature, BudgetPeriod.DAILY);
        if (featureLimit.isPresent()) {
            BudgetLimit limit = featureLimit.get();
            LocalDateTime dayStart = BudgetPeriod.DAILY.getPeriodStart(now);
            LocalDateTime dayEnd = BudgetPeriod.DAILY.getPeriodEnd(now);
            BigDecimal usedToday = transactionRepository.getTotalCostByFeature(feature, dayStart, dayEnd);
            
            if (limit.shouldHardStop(usedToday.add(estimatedCost))) {
                result.deny("Feature-Budget für " + feature + " erreicht");
                return result;
            }
        }

        result.approve();
        return result;
    }

    /**
     * Holt Cost Statistics für einen Zeitraum
     */
    public CostStatistics getCostStatistics(LocalDateTime start, LocalDateTime end) {
        CostStatistics stats = new CostStatistics();
        
        stats.totalCost = transactionRepository.getTotalCost(start, end);
        stats.transactionCount = transactionRepository.findInPeriod(start, end).size();
        
        // Top Cost Drivers
        List<Object[]> topServices = transactionRepository.getTopCostDriversByService(start, end, 5);
        for (Object[] row : topServices) {
            String service = (String) row[0];
            BigDecimal cost = (BigDecimal) row[1];
            Long count = (Long) row[2];
            stats.addServiceCost(service, cost, count.intValue());
        }

        List<Object[]> topFeatures = transactionRepository.getTopCostDriversByFeature(start, end, 5);
        for (Object[] row : topFeatures) {
            String feature = (String) row[0];
            BigDecimal cost = (BigDecimal) row[1];
            Long count = (Long) row[2];
            stats.addFeatureCost(feature, cost, count.intValue());
        }

        // Fehlgeschlagene Transaktionen
        stats.failedTransactions = transactionRepository.findFailedTransactions(start, end).size();
        
        // Over-Budget Transaktionen
        stats.overBudgetTransactions = transactionRepository.findOverBudgetTransactions(start, end).size();

        return stats;
    }

    /**
     * Holt Cost Dashboard Data für heute
     */
    public CostDashboardData getTodayDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = BudgetPeriod.DAILY.getPeriodStart(now);
        LocalDateTime todayEnd = BudgetPeriod.DAILY.getPeriodEnd(now);

        CostDashboardData dashboard = new CostDashboardData();
        dashboard.period = "Heute";
        dashboard.periodStart = todayStart;
        dashboard.periodEnd = todayEnd;

        // Aktuelle Kosten
        dashboard.totalCost = transactionRepository.getTotalCost(todayStart, todayEnd);
        
        // Budget-Informationen
        Optional<BudgetLimit> dailyLimit = budgetRepository.findGlobalLimit(BudgetPeriod.DAILY);
        if (dailyLimit.isPresent()) {
            dashboard.budgetLimit = dailyLimit.get().limitAmount;
            dashboard.budgetUsedPercentage = dashboard.totalCost
                .divide(dashboard.budgetLimit, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            dashboard.remainingBudget = dashboard.budgetLimit.subtract(dashboard.totalCost);
        }

        // Top Cost Drivers
        List<Object[]> topServices = transactionRepository.getTopCostDriversByService(todayStart, todayEnd, 3);
        for (Object[] row : topServices) {
            String service = (String) row[0];
            BigDecimal cost = (BigDecimal) row[1];
            Long usage = (Long) row[2];
            dashboard.addTopCostDriver(service, cost, usage.intValue());
        }

        // Laufende Transaktionen
        dashboard.runningTransactions = transactionRepository.findRunningTransactions().size();

        return dashboard;
    }

    /**
     * Erstellt oder aktualisiert ein Budget Limit
     */
    public BudgetLimit createOrUpdateBudgetLimit(
            String scope, 
            String scopeValue, 
            BudgetPeriod period, 
            BigDecimal limitAmount,
            BigDecimal alertThreshold,
            BigDecimal hardStopThreshold) {
        
        // Prüfe ob bereits existiert
        Optional<BudgetLimit> existing = findExistingLimit(scope, scopeValue, period);
        
        BudgetLimit limit;
        if (existing.isPresent()) {
            limit = existing.get();
            limit.limitAmount = limitAmount;
            limit.alertThreshold = alertThreshold;
            limit.hardStopThreshold = hardStopThreshold;
        } else {
            limit = new BudgetLimit(scope, scopeValue, period, limitAmount);
            limit.alertThreshold = alertThreshold;
            limit.hardStopThreshold = hardStopThreshold;
        }
        
        limit.persist();
        return limit;
    }

    /**
     * Triggert manuelle Budget-Prüfung und Alerts
     */
    public void triggerBudgetCheck() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = BudgetPeriod.DAILY.getPeriodStart(now);
        LocalDateTime todayEnd = BudgetPeriod.DAILY.getPeriodEnd(now);

        // Prüfe alle aktiven Limits
        List<BudgetLimit> limits = budgetRepository.findActive();
        
        for (BudgetLimit limit : limits) {
            LocalDateTime periodStart = limit.period.getPeriodStart(now);
            LocalDateTime periodEnd = limit.period.getPeriodEnd(now);
            
            BigDecimal currentUsage = getCurrentUsageForLimit(limit, periodStart, periodEnd);
            
            if (limit.shouldAlert(currentUsage)) {
                alertService.sendBudgetAlert(limit, currentUsage);
            }
        }
    }

    // Private Helper Methods

    private void checkAndSendAlerts(CostTransaction transaction) {
        // Implementierung für Alert-Checks nach Transaction
        if (transaction.isOverBudget()) {
            alertService.sendOverBudgetAlert(transaction);
        }
    }

    private Optional<BudgetLimit> findExistingLimit(String scope, String scopeValue, BudgetPeriod period) {
        return switch (scope) {
            case "global" -> budgetRepository.findGlobalLimit(period);
            case "service" -> budgetRepository.findServiceLimit(scopeValue, period);
            case "feature" -> budgetRepository.findFeatureLimit(scopeValue, period);
            case "user" -> budgetRepository.findUserLimit(scopeValue, period);
            default -> Optional.empty();
        };
    }

    private BigDecimal getCurrentUsageForLimit(BudgetLimit limit, LocalDateTime start, LocalDateTime end) {
        return switch (limit.scope) {
            case "global" -> transactionRepository.getTotalCost(start, end);
            case "service" -> transactionRepository.getTotalCostByService(limit.scopeValue, start, end);
            case "feature" -> transactionRepository.getTotalCostByFeature(limit.scopeValue, start, end);
            case "user" -> transactionRepository.getTotalCostByUser(limit.scopeValue, start, end);
            default -> BigDecimal.ZERO;
        };
    }
}
package de.freshplan.domain.cost.service.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Cost Statistics DTO für Analytics und Reporting
 */
public class CostStatistics {
    public BigDecimal totalCost = BigDecimal.ZERO;
    public int transactionCount = 0;
    public int failedTransactions = 0;
    public int overBudgetTransactions = 0;
    
    public Map<String, ServiceCostInfo> serviceCosts = new HashMap<>();
    public Map<String, FeatureCostInfo> featureCosts = new HashMap<>();

    public void addServiceCost(String service, BigDecimal cost, int usage) {
        serviceCosts.put(service, new ServiceCostInfo(service, cost, usage));
    }

    public void addFeatureCost(String feature, BigDecimal cost, int usage) {
        featureCosts.put(feature, new FeatureCostInfo(feature, cost, usage));
    }

    public BigDecimal getAverageCostPerTransaction() {
        if (transactionCount == 0) return BigDecimal.ZERO;
        return totalCost.divide(BigDecimal.valueOf(transactionCount), 4, BigDecimal.ROUND_HALF_UP);
    }

    public double getFailureRate() {
        if (transactionCount == 0) return 0.0;
        return (double) failedTransactions / transactionCount * 100;
    }

    public double getOverBudgetRate() {
        if (transactionCount == 0) return 0.0;
        return (double) overBudgetTransactions / transactionCount * 100;
    }

    public static class ServiceCostInfo {
        public String service;
        public BigDecimal cost;
        public int usage;

        public ServiceCostInfo(String service, BigDecimal cost, int usage) {
            this.service = service;
            this.cost = cost;
            this.usage = usage;
        }
    }

    public static class FeatureCostInfo {
        public String feature;
        public BigDecimal cost;
        public int usage;

        public FeatureCostInfo(String feature, BigDecimal cost, int usage) {
            this.feature = feature;
            this.cost = cost;
            this.usage = usage;
        }
    }
}
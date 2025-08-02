package de.freshplan.domain.cost.service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard Data für Cost Management UI
 */
public class CostDashboardData {
    public String period;
    public LocalDateTime periodStart;
    public LocalDateTime periodEnd;
    
    public BigDecimal totalCost = BigDecimal.ZERO;
    public BigDecimal budgetLimit;
    public BigDecimal budgetUsedPercentage = BigDecimal.ZERO;
    public BigDecimal remainingBudget = BigDecimal.ZERO;
    
    public List<TopCostDriver> topCostDrivers = new ArrayList<>();
    public int runningTransactions = 0;

    public void addTopCostDriver(String name, BigDecimal cost, int usage) {
        topCostDrivers.add(new TopCostDriver(name, cost, usage));
    }

    public boolean isOverBudget() {
        return budgetLimit != null && totalCost.compareTo(budgetLimit) > 0;
    }

    public boolean isNearBudgetLimit(double threshold) {
        if (budgetLimit == null || budgetLimit.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        
        double percentage = budgetUsedPercentage.doubleValue() / 100.0;
        return percentage >= threshold;
    }

    public String getBudgetStatus() {
        if (budgetLimit == null) {
            return "Kein Limit gesetzt";
        }
        
        if (isOverBudget()) {
            return "Budget überschritten";
        } else if (isNearBudgetLimit(0.9)) {
            return "Budget fast erreicht";
        } else if (isNearBudgetLimit(0.8)) {
            return "Budget zu 80% genutzt";
        } else {
            return "Budget OK";
        }
    }

    public static class TopCostDriver {
        public String feature;
        public BigDecimal cost;
        public int usage;

        public TopCostDriver(String feature, BigDecimal cost, int usage) {
            this.feature = feature;
            this.cost = cost;
            this.usage = usage;
        }
    }
}
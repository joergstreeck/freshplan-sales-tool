package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.dto.RevenueMetrics;
import java.util.UUID;

/**
 * Interface for revenue metrics operations.
 *
 * <p>Extracted during Sprint 2.1.7.7 Cycle 2 fix to break circular dependency between
 * customer.service and xentral.service. Follows Dependency Inversion Principle (SOLID).
 *
 * <p>This interface allows customer.service (CustomerHealthScoreService) to retrieve revenue
 * metrics without depending on the xentral.service implementation, breaking the circular
 * dependency.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public interface RevenueMetricsProvider {

  /**
   * Get revenue metrics for a customer.
   *
   * <p>Retrieves revenue metrics including data from Xentral ERP if available. Combines local
   * database data with Xentral API data.
   *
   * @param customerId Customer UUID
   * @return RevenueMetrics object with aggregated data
   */
  RevenueMetrics getRevenueMetrics(UUID customerId);
}

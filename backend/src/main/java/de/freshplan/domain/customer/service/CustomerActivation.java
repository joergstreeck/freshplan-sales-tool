package de.freshplan.domain.customer.service;

import java.util.UUID;

/**
 * Interface for customer activation operations.
 *
 * <p>Extracted during Sprint 2.1.7.7 Cycle 2 fix to break circular dependency between
 * customer.service and xentral.service. Follows Dependency Inversion Principle (SOLID).
 *
 * <p>This interface allows xentral.service (XentralOrderEventHandlerImpl) to activate customers
 * without depending on the full CustomerService class, breaking the circular dependency.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public interface CustomerActivation {

  /**
   * Activate a PROSPECT customer to AKTIV status (first order received).
   *
   * <p>Sprint 2.1.7.4: PROSPECT → AKTIV Transition
   *
   * <p>Business Logic:
   *
   * <ul>
   *   <li>Customer must be in PROSPECT status
   *   <li>Transition to AKTIV status
   *   <li>Create timeline event
   *   <li>Set firstOrderDate if not set
   * </ul>
   *
   * @param customerId Customer UUID
   * @param orderReference Order reference/number that triggered activation
   * @throws IllegalStateException if customer is not PROSPECT
   */
  void activateCustomer(UUID customerId, String orderReference);
}

package de.freshplan.modules.xentral.service;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.CustomerActivation;
import de.freshplan.infrastructure.security.RlsContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Real Implementation for Xentral Order Event Handler
 *
 * <p>Sprint 2.1.7.2: Xentral Webhook Integration
 *
 * <p>This implementation handles "Order Delivered" events from Xentral ERP and automatically
 * activates PROSPECT customers on their first order.
 *
 * <p>Business Logic:
 *
 * <ul>
 *   <li>Find customer by xentralCustomerId
 *   <li>If customer status = PROSPECT → call customerService.activateCustomer()
 *   <li>If customer status = AKTIV → update lastOrderDate
 *   <li>Log the order event for audit trail
 * </ul>
 *
 * <p>Security: Uses @RlsContext for proper Row-Level Security context
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
@Priority(1) // Highest priority - selected over Mock (which has Priority=Integer.MAX_VALUE)
public class XentralOrderEventHandlerImpl implements XentralOrderEventHandler {

  private static final Logger logger = LoggerFactory.getLogger(XentralOrderEventHandlerImpl.class);

  @Inject CustomerRepository customerRepository;

  @Inject CustomerActivation customerActivation;

  /**
   * Handle "Order Delivered" event from Xentral
   *
   * <p>This method is called when Xentral sends a webhook notification that an order has been
   * delivered. It automatically activates PROSPECT customers on their first order.
   *
   * @param xentralCustomerId Customer ID in Xentral ERP system
   * @param orderNumber Order number from Xentral
   * @param deliveryDate Delivery date of the order
   * @throws IllegalArgumentException if xentralCustomerId is null or empty
   */
  @Override
  @Transactional
  @RlsContext // Sprint 1.5: RLS Security Context
  public void handleOrderDelivered(
      String xentralCustomerId, String orderNumber, LocalDate deliveryDate) {

    // Validation
    if (xentralCustomerId == null || xentralCustomerId.isBlank()) {
      logger.error("Xentral Webhook Error: xentralCustomerId is null or empty");
      throw new IllegalArgumentException("xentralCustomerId cannot be null or empty");
    }

    if (orderNumber == null || orderNumber.isBlank()) {
      logger.error("Xentral Webhook Error: orderNumber is null or empty");
      throw new IllegalArgumentException("orderNumber cannot be null or empty");
    }

    if (deliveryDate == null) {
      logger.error("Xentral Webhook Error: deliveryDate is null");
      throw new IllegalArgumentException("deliveryDate cannot be null");
    }

    logger.info(
        "Processing Xentral Order Delivered Event: customer={}, order={}, date={}",
        xentralCustomerId,
        orderNumber,
        deliveryDate);

    // Find customer by xentralCustomerId
    Customer customer =
        customerRepository
            .findByXentralCustomerId(xentralCustomerId)
            .orElseThrow(
                () -> {
                  logger.warn("Customer not found for xentralCustomerId: {}", xentralCustomerId);
                  return new IllegalArgumentException("Customer not found: " + xentralCustomerId);
                });

    // Business Logic: Auto-Activation for PROSPECT customers
    if (customer.getStatus() == CustomerStatus.PROSPECT) {
      logger.info(
          "Auto-activating PROSPECT customer: {} (xentralId: {})",
          customer.getCompanyName(),
          xentralCustomerId);

      // Sprint 2.1.7.4: Use CustomerActivation.activateCustomer() (Cycle 2 fix: Interface
      // abstraction)
      customerActivation.activateCustomer(customer.getId(), orderNumber);

      logger.info(
          "Customer activated successfully: {} → AKTIV (order: {})",
          customer.getCompanyName(),
          orderNumber);

    } else if (customer.getStatus() == CustomerStatus.AKTIV) {
      // Update lastOrderDate for active customers
      customer.setLastOrderDate(deliveryDate);
      customerRepository.persist(customer);

      logger.info(
          "Updated lastOrderDate for AKTIV customer: {} (order: {})",
          customer.getCompanyName(),
          orderNumber);

    } else {
      logger.warn(
          "Unexpected customer status: {} for customer: {} (order: {})",
          customer.getStatus(),
          customer.getCompanyName(),
          orderNumber);
    }
  }
}

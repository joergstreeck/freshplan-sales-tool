package de.freshplan.modules.xentral.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock Implementation for Xentral Order Event Handler
 *
 * <p>Sprint 2.1.7.4: Mock for testing Sprint 2.1.7.2: Replace with real Xentral Webhook
 *
 * <p>This is a temporary mock implementation that only logs events. It will be replaced with a real
 * Xentral Webhook integration in Sprint 2.1.7.2.
 *
 * <p>IMPORTANT: This mock does NOT trigger customer activation. It only logs the event for testing
 * purposes. The real implementation will call CustomerService.activateCustomer().
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class MockXentralOrderEventHandler implements XentralOrderEventHandler {

  private static final Logger logger = LoggerFactory.getLogger(MockXentralOrderEventHandler.class);

  /**
   * Mock implementation that only logs the event
   *
   * <p>TODO: Sprint 2.1.7.2 - Implement real Xentral Webhook integration
   *
   * @param xentralCustomerId Customer ID in Xentral ERP system
   * @param orderNumber Order number from Xentral
   * @param deliveryDate Delivery date of the order
   */
  @Override
  public void handleOrderDelivered(
      String xentralCustomerId, String orderNumber, LocalDate deliveryDate) {
    // TODO: Sprint 2.1.7.2 - Implement real Xentral Webhook
    // Real implementation will:
    // 1. Find customer by xentralCustomerId
    // 2. If PROSPECT → call customerService.activateCustomer(customerId, orderNumber)
    // 3. If AKTIV → update lastOrderDate
    // 4. Log the order event

    logger.info(
        "Mock Xentral Event: Order delivered - customer={}, order={}, date={}",
        xentralCustomerId,
        orderNumber,
        deliveryDate);
  }
}

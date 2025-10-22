package de.freshplan.modules.xentral.service;

import java.time.LocalDate;

/**
 * Handles Xentral Order Events
 *
 * <p>Sprint 2.1.7.4: Interface definition Sprint 2.1.7.2: Full implementation with Webhook
 *
 * <p>This interface defines the contract for handling order events from Xentral ERP. The
 * implementation will automatically activate PROSPECT customers when they receive their first
 * order.
 *
 * <p>IMPORTANT: This is currently a mock interface. The real Xentral Webhook integration will be
 * implemented in Sprint 2.1.7.2.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public interface XentralOrderEventHandler {

  /**
   * Handle "Order Delivered" event from Xentral
   *
   * <p>This method will be called when Xentral sends a webhook notification that an order has been
   * delivered. It automatically activates PROSPECT customers on their first order.
   *
   * <p>Business Logic: - Find customer by xentralCustomerId - If customer status = PROSPECT →
   * activate customer - If customer status = AKTIV → update last order date - Log the order event
   *
   * @param xentralCustomerId Customer ID in Xentral ERP system
   * @param orderNumber Order number from Xentral
   * @param deliveryDate Delivery date of the order
   */
  void handleOrderDelivered(String xentralCustomerId, String orderNumber, LocalDate deliveryDate);
}

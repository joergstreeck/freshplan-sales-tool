package de.freshplan.modules.xentral.service;

import de.freshplan.domain.customer.dto.PaymentBehavior;
import de.freshplan.domain.customer.dto.RevenueMetrics;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.service.RevenueMetricsProvider;
import de.freshplan.modules.xentral.dto.XentralInvoiceDTO;
import de.freshplan.modules.xentral.service.XentralApiService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Revenue Metrics Service
 *
 * <p>Sprint 2.1.7.2: Customer Dashboard - Revenue Metrics from Xentral
 *
 * <p>Sprint 2.1.7.7 Cycle 2 fix: Implements RevenueMetricsProvider interface to break circular
 * dependency between customer.service and xentral.service (Dependency Inversion Principle).
 *
 * <p>Calculates revenue metrics (30/90/365 days) and payment behavior from Xentral invoices.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class RevenueMetricsService implements RevenueMetricsProvider {

  private static final Logger logger = LoggerFactory.getLogger(RevenueMetricsService.class);

  @Inject XentralApiService xentralApiService;

  /**
   * Get revenue metrics for customer
   *
   * @param customerId Customer UUID
   * @return Revenue metrics (30/90/365 days + payment behavior)
   * @throws NotFoundException if customer not found
   */
  public RevenueMetrics getRevenueMetrics(UUID customerId) {
    logger.debug("Getting revenue metrics for customer: {}", customerId);

    // 1. Find customer
    Customer customer = Customer.findById(customerId);
    if (customer == null) {
      throw new NotFoundException("Customer not found: " + customerId);
    }

    // 2. If no Xentral-ID → return empty metrics
    if (customer.getXentralCustomerId() == null) {
      logger.debug("Customer {} has no Xentral-ID - returning empty metrics", customerId);
      return RevenueMetrics.empty();
    }

    // 3. Get invoices from Xentral
    List<XentralInvoiceDTO> invoices =
        xentralApiService.getInvoicesByCustomer(customer.getXentralCustomerId());

    if (invoices.isEmpty()) {
      logger.debug(
          "Customer {} (Xentral-ID: {}) has no invoices - returning empty metrics",
          customerId,
          customer.getXentralCustomerId());
      return RevenueMetrics.empty();
    }

    // 4. Calculate Revenue for different time periods
    LocalDate now = LocalDate.now();
    BigDecimal revenue30Days = calculateRevenueForPeriod(invoices, now.minusDays(30), now);
    BigDecimal revenue90Days = calculateRevenueForPeriod(invoices, now.minusDays(90), now);
    BigDecimal revenue365Days = calculateRevenueForPeriod(invoices, now.minusDays(365), now);

    // 5. Calculate average days to pay (simplified)
    Integer averageDaysToPay = calculateAverageDaysToPay(invoices);

    // 6. Get last order date
    LocalDate lastOrderDate =
        invoices.stream()
            .map(XentralInvoiceDTO::invoiceDate)
            .filter(date -> date != null)
            .max(LocalDate::compareTo)
            .orElse(null);

    // 7. Determine payment behavior
    PaymentBehavior paymentBehavior = PaymentBehavior.fromAverageDaysToPay(averageDaysToPay);

    logger.info(
        "Revenue metrics for customer {}: 30d={}, 90d={}, 365d={}, payment={}",
        customerId,
        revenue30Days,
        revenue90Days,
        revenue365Days,
        paymentBehavior);

    return new RevenueMetrics(
        revenue30Days,
        revenue90Days,
        revenue365Days,
        paymentBehavior,
        averageDaysToPay,
        lastOrderDate);
  }

  /**
   * Calculate total revenue for a specific time period
   *
   * @param invoices All invoices
   * @param startDate Start date (inclusive)
   * @param endDate End date (inclusive)
   * @return Total revenue for period
   */
  private BigDecimal calculateRevenueForPeriod(
      List<XentralInvoiceDTO> invoices, LocalDate startDate, LocalDate endDate) {

    return invoices.stream()
        .filter(inv -> inv.invoiceDate() != null)
        .filter(inv -> !inv.invoiceDate().isBefore(startDate))
        .filter(inv -> !inv.invoiceDate().isAfter(endDate))
        .map(XentralInvoiceDTO::amount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Calculate average days to pay (simplified)
   *
   * <p>Assumes payment date = invoice date + 14 days (standard payment term) for Mock-Mode For
   * Real-Mode: Would use actual payment dates from Xentral
   *
   * @param invoices All invoices
   * @return Average days to pay, or null if no invoices
   */
  private Integer calculateAverageDaysToPay(List<XentralInvoiceDTO> invoices) {
    if (invoices.isEmpty()) {
      return null;
    }

    // Simplified calculation for Mock-Mode
    // In Real-Mode: Use actual payment dates from XentralV1InvoiceBalance
    // For now: Assume 14 days (standard Zahlungsziel)
    return 14; // TODO: Implement real calculation when payment data is available
  }
}

package de.freshplan.modules.xentral.service;

import de.freshplan.modules.xentral.dto.v1.XentralV1Invoice;
import de.freshplan.modules.xentral.dto.v1.XentralV1InvoiceBalance;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.jboss.logging.Logger;

/**
 * Calculates financial metrics from Xentral invoice data.
 *
 * <p>Used by XentralV1V2ApiAdapter to compute: - totalRevenue = SUM(invoice.balance.paid) -
 * averageDaysToPay = AVG(DAYS.between(invoice.date, balance.paymentDate)) - lastOrderDate =
 * MAX(invoice.date)
 *
 * <p>Sprint: 2.1.7.2 - D2c Financial Calculations
 */
@ApplicationScoped
public class FinancialMetricsCalculator {

  private static final Logger LOG = Logger.getLogger(FinancialMetricsCalculator.class);

  /**
   * Calculates all financial metrics for a customer from their invoices and balance data.
   *
   * @param invoices list of invoices for the customer
   * @param balances map of invoice ID to balance data
   * @return financial metrics (totalRevenue, avgDaysToPay, lastOrderDate)
   */
  public FinancialMetrics calculate(
      List<XentralV1Invoice> invoices, Map<String, XentralV1InvoiceBalance> balances) {

    if (invoices == null || invoices.isEmpty()) {
      LOG.debugf("No invoices provided for financial metrics calculation");
      return new FinancialMetrics(BigDecimal.ZERO, null, null);
    }

    BigDecimal totalRevenue = calculateTotalRevenue(invoices, balances);
    Integer averageDaysToPay = calculateAverageDaysToPay(invoices, balances);
    LocalDate lastOrderDate = calculateLastOrderDate(invoices);

    return new FinancialMetrics(totalRevenue, averageDaysToPay, lastOrderDate);
  }

  /**
   * Calculates total revenue = SUM(balance.paid).
   *
   * <p>Only counts invoices with available balance data.
   *
   * @param invoices list of invoices
   * @param balances map of invoice ID to balance data
   * @return total revenue (sum of paid amounts)
   */
  private BigDecimal calculateTotalRevenue(
      List<XentralV1Invoice> invoices, Map<String, XentralV1InvoiceBalance> balances) {

    return invoices.stream()
        .filter(invoice -> balances.containsKey(invoice.id()))
        .map(invoice -> balances.get(invoice.id()).getPaidAmount())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Calculates average days to pay = AVG(DAYS.between(invoiceDate, paymentDate)).
   *
   * <p>Only counts invoices that are paid (balance.paymentDate != null).
   *
   * @param invoices list of invoices
   * @param balances map of invoice ID to balance data
   * @return average days to pay or null if no paid invoices
   */
  private Integer calculateAverageDaysToPay(
      List<XentralV1Invoice> invoices, Map<String, XentralV1InvoiceBalance> balances) {

    List<Long> daysToPay =
        invoices.stream()
            .filter(invoice -> balances.containsKey(invoice.id()))
            .filter(invoice -> invoice.invoiceDate() != null)
            .filter(invoice -> balances.get(invoice.id()).paymentDate() != null)
            .map(
                invoice -> {
                  LocalDate invoiceDate = invoice.invoiceDate();
                  LocalDate paymentDate = balances.get(invoice.id()).paymentDate();
                  return ChronoUnit.DAYS.between(invoiceDate, paymentDate);
                })
            .toList();

    if (daysToPay.isEmpty()) {
      return null;
    }

    double average = daysToPay.stream().mapToLong(Long::longValue).average().orElse(0.0);

    return BigDecimal.valueOf(average).setScale(0, RoundingMode.HALF_UP).intValue();
  }

  /**
   * Calculates last order date = MAX(invoice.invoiceDate).
   *
   * @param invoices list of invoices
   * @return most recent invoice date or null if no invoices
   */
  private LocalDate calculateLastOrderDate(List<XentralV1Invoice> invoices) {
    return invoices.stream()
        .map(XentralV1Invoice::invoiceDate)
        .filter(date -> date != null)
        .max(LocalDate::compareTo)
        .orElse(null);
  }

  /** Result record containing all financial metrics. */
  public record FinancialMetrics(
      BigDecimal totalRevenue, Integer averageDaysToPay, LocalDate lastOrderDate) {}
}

package de.freshplan.modules.xentral.service;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.modules.xentral.dto.v1.XentralV1Invoice;
import de.freshplan.modules.xentral.dto.v1.XentralV1InvoiceBalance;
import de.freshplan.modules.xentral.service.FinancialMetricsCalculator.FinancialMetrics;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for FinancialMetricsCalculator.
 *
 * <p>Sprint: 2.1.7.2 - D2e Coverage Improvement
 */
@QuarkusTest
class FinancialMetricsCalculatorTest {

  @Inject FinancialMetricsCalculator calculator;

  @Test
  @DisplayName("calculate() - returns zeros/nulls when invoice list is null")
  void testCalculate_NullInvoices() {
    // When
    FinancialMetrics metrics = calculator.calculate(null, Map.of());

    // Then
    assertThat(metrics.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(metrics.averageDaysToPay()).isNull();
    assertThat(metrics.lastOrderDate()).isNull();
  }

  @Test
  @DisplayName("calculate() - returns zeros/nulls when invoice list is empty")
  void testCalculate_EmptyInvoices() {
    // When
    FinancialMetrics metrics = calculator.calculate(List.of(), Map.of());

    // Then
    assertThat(metrics.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(metrics.averageDaysToPay()).isNull();
    assertThat(metrics.lastOrderDate()).isNull();
  }

  @Test
  @DisplayName("calculate() - returns zero revenue but lastOrderDate when no balance data")
  void testCalculate_InvoicesWithoutBalances() {
    // Given: Invoices exist, but no balance data available
    var invoice1 =
        new XentralV1Invoice(
            "INV-001",
            "RE-001",
            "CUST-123",
            new BigDecimal("1000.00"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            "open");

    var invoice2 =
        new XentralV1Invoice(
            "INV-002",
            "RE-002",
            "CUST-123",
            new BigDecimal("2000.00"),
            LocalDate.of(2024, 10, 1),
            LocalDate.of(2024, 11, 1),
            "open");

    // When: No balances provided
    FinancialMetrics metrics = calculator.calculate(List.of(invoice1, invoice2), Map.of());

    // Then: Revenue = 0 (no balance data), but lastOrderDate exists
    assertThat(metrics.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(metrics.averageDaysToPay()).isNull();
    assertThat(metrics.lastOrderDate()).isEqualTo(LocalDate.of(2024, 10, 1));
  }

  @Test
  @DisplayName("calculate() - calculates total revenue from paid balances")
  void testCalculate_TotalRevenue_MultiplePaidInvoices() {
    // Given: 3 invoices with different paid amounts
    var invoice1 =
        new XentralV1Invoice(
            "INV-001",
            "RE-001",
            "CUST-123",
            new BigDecimal("1000.00"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            "paid");

    var invoice2 =
        new XentralV1Invoice(
            "INV-002",
            "RE-002",
            "CUST-123",
            new BigDecimal("2500.00"),
            LocalDate.of(2024, 9, 15),
            LocalDate.of(2024, 10, 15),
            "paid");

    var invoice3 =
        new XentralV1Invoice(
            "INV-003",
            "RE-003",
            "CUST-123",
            new BigDecimal("750.50"),
            LocalDate.of(2024, 10, 1),
            LocalDate.of(2024, 11, 1),
            "paid");

    var balances =
        Map.of(
            "INV-001",
                new XentralV1InvoiceBalance(
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    BigDecimal.ZERO,
                    LocalDate.of(2024, 9, 20)),
            "INV-002",
                new XentralV1InvoiceBalance(
                    new BigDecimal("2500.00"),
                    new BigDecimal("2500.00"),
                    BigDecimal.ZERO,
                    LocalDate.of(2024, 10, 1)),
            "INV-003",
                new XentralV1InvoiceBalance(
                    new BigDecimal("750.50"),
                    new BigDecimal("750.50"),
                    BigDecimal.ZERO,
                    LocalDate.of(2024, 10, 15)));

    // When
    FinancialMetrics metrics =
        calculator.calculate(List.of(invoice1, invoice2, invoice3), balances);

    // Then: totalRevenue = 1000.00 + 2500.00 + 750.50 = 4250.50
    assertThat(metrics.totalRevenue()).isEqualByComparingTo(new BigDecimal("4250.50"));
  }

  @Test
  @DisplayName("calculate() - calculates total revenue from partially paid invoices")
  void testCalculate_TotalRevenue_PartiallyPaidInvoices() {
    // Given: Invoices with partial payments
    var invoice1 =
        new XentralV1Invoice(
            "INV-001",
            "RE-001",
            "CUST-123",
            new BigDecimal("1000.00"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            "partial");

    var invoice2 =
        new XentralV1Invoice(
            "INV-002",
            "RE-002",
            "CUST-123",
            new BigDecimal("2000.00"),
            LocalDate.of(2024, 9, 15),
            LocalDate.of(2024, 10, 15),
            "partial");

    var balances =
        Map.of(
            "INV-001",
                new XentralV1InvoiceBalance(
                    new BigDecimal("1000.00"),
                    new BigDecimal("500.00"), // Only 500 paid
                    new BigDecimal("500.00"),
                    null),
            "INV-002",
                new XentralV1InvoiceBalance(
                    new BigDecimal("2000.00"),
                    new BigDecimal("1200.00"), // Only 1200 paid
                    new BigDecimal("800.00"),
                    null));

    // When
    FinancialMetrics metrics = calculator.calculate(List.of(invoice1, invoice2), balances);

    // Then: totalRevenue = 500 + 1200 = 1700
    assertThat(metrics.totalRevenue()).isEqualByComparingTo(new BigDecimal("1700.00"));
  }

  @Test
  @DisplayName("calculate() - calculates averageDaysToPay correctly")
  void testCalculate_AverageDaysToPay_MultipleInvoices() {
    // Given: 3 paid invoices with different payment times
    var invoice1 =
        new XentralV1Invoice(
            "INV-001",
            "RE-001",
            "CUST-123",
            new BigDecimal("1000.00"),
            LocalDate.of(2024, 9, 1), // Paid after 10 days
            LocalDate.of(2024, 10, 1),
            "paid");

    var invoice2 =
        new XentralV1Invoice(
            "INV-002",
            "RE-002",
            "CUST-123",
            new BigDecimal("2000.00"),
            LocalDate.of(2024, 9, 1), // Paid after 20 days
            LocalDate.of(2024, 10, 1),
            "paid");

    var invoice3 =
        new XentralV1Invoice(
            "INV-003",
            "RE-003",
            "CUST-123",
            new BigDecimal("1500.00"),
            LocalDate.of(2024, 9, 1), // Paid after 30 days
            LocalDate.of(2024, 10, 1),
            "paid");

    var balances =
        Map.of(
            "INV-001",
                new XentralV1InvoiceBalance(
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    BigDecimal.ZERO,
                    LocalDate.of(2024, 9, 11)), // 10 days
            "INV-002",
                new XentralV1InvoiceBalance(
                    new BigDecimal("2000.00"),
                    new BigDecimal("2000.00"),
                    BigDecimal.ZERO,
                    LocalDate.of(2024, 9, 21)), // 20 days
            "INV-003",
                new XentralV1InvoiceBalance(
                    new BigDecimal("1500.00"),
                    new BigDecimal("1500.00"),
                    BigDecimal.ZERO,
                    LocalDate.of(2024, 10, 1))); // 30 days

    // When
    FinancialMetrics metrics =
        calculator.calculate(List.of(invoice1, invoice2, invoice3), balances);

    // Then: averageDaysToPay = (10 + 20 + 30) / 3 = 20
    assertThat(metrics.averageDaysToPay()).isEqualTo(20);
  }

  @Test
  @DisplayName("calculate() - returns null averageDaysToPay when no payment dates")
  void testCalculate_AverageDaysToPay_NoPaymentDates() {
    // Given: Invoices with balances but no payment dates (still open)
    var invoice1 =
        new XentralV1Invoice(
            "INV-001",
            "RE-001",
            "CUST-123",
            new BigDecimal("1000.00"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            "open");

    var balances =
        Map.of(
            "INV-001",
            new XentralV1InvoiceBalance(
                new BigDecimal("1000.00"),
                BigDecimal.ZERO,
                new BigDecimal("1000.00"),
                null)); // No payment date

    // When
    FinancialMetrics metrics = calculator.calculate(List.of(invoice1), balances);

    // Then: averageDaysToPay should be null
    assertThat(metrics.averageDaysToPay()).isNull();
  }

  @Test
  @DisplayName("calculate() - handles mixed paid and unpaid invoices")
  void testCalculate_MixedPaidAndUnpaidInvoices() {
    // Given: 3 invoices, only 2 are paid
    var invoice1 =
        new XentralV1Invoice(
            "INV-001",
            "RE-001",
            "CUST-123",
            new BigDecimal("1000.00"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            "paid");

    var invoice2 =
        new XentralV1Invoice(
            "INV-002",
            "RE-002",
            "CUST-123",
            new BigDecimal("2000.00"),
            LocalDate.of(2024, 9, 15),
            LocalDate.of(2024, 10, 15),
            "open");

    var invoice3 =
        new XentralV1Invoice(
            "INV-003",
            "RE-003",
            "CUST-123",
            new BigDecimal("1500.00"),
            LocalDate.of(2024, 10, 1),
            LocalDate.of(2024, 11, 1),
            "paid");

    var balances =
        Map.of(
            "INV-001",
                new XentralV1InvoiceBalance(
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    BigDecimal.ZERO,
                    LocalDate.of(2024, 9, 15)), // 14 days
            "INV-002",
                new XentralV1InvoiceBalance(
                    new BigDecimal("2000.00"),
                    BigDecimal.ZERO,
                    new BigDecimal("2000.00"),
                    null), // Not paid yet
            "INV-003",
                new XentralV1InvoiceBalance(
                    new BigDecimal("1500.00"),
                    new BigDecimal("1500.00"),
                    BigDecimal.ZERO,
                    LocalDate.of(2024, 10, 7))); // 6 days

    // When
    FinancialMetrics metrics =
        calculator.calculate(List.of(invoice1, invoice2, invoice3), balances);

    // Then
    assertThat(metrics.totalRevenue())
        .isEqualByComparingTo(new BigDecimal("2500.00")); // 1000 + 1500
    assertThat(metrics.averageDaysToPay()).isEqualTo(10); // (14 + 6) / 2 = 10
    assertThat(metrics.lastOrderDate()).isEqualTo(LocalDate.of(2024, 10, 1));
  }

  @Test
  @DisplayName("calculate() - calculates lastOrderDate correctly")
  void testCalculate_LastOrderDate_FindsMaxDate() {
    // Given: Invoices with different dates
    var invoice1 =
        new XentralV1Invoice(
            "INV-001",
            "RE-001",
            "CUST-123",
            new BigDecimal("1000.00"),
            LocalDate.of(2024, 8, 15),
            LocalDate.of(2024, 9, 15),
            "paid");

    var invoice2 =
        new XentralV1Invoice(
            "INV-002",
            "RE-002",
            "CUST-123",
            new BigDecimal("2000.00"),
            LocalDate.of(2024, 10, 1), // Latest date
            LocalDate.of(2024, 11, 1),
            "open");

    var invoice3 =
        new XentralV1Invoice(
            "INV-003",
            "RE-003",
            "CUST-123",
            new BigDecimal("1500.00"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            "paid");

    // When
    FinancialMetrics metrics =
        calculator.calculate(List.of(invoice1, invoice2, invoice3), Map.of());

    // Then: lastOrderDate should be 2024-10-01
    assertThat(metrics.lastOrderDate()).isEqualTo(LocalDate.of(2024, 10, 1));
  }

  @Test
  @DisplayName("calculate() - returns null lastOrderDate when all invoice dates are null")
  void testCalculate_LastOrderDate_AllDatesNull() {
    // Given: Invoices without dates
    var invoice1 =
        new XentralV1Invoice(
            "INV-001", "RE-001", "CUST-123", new BigDecimal("1000.00"), null, null, "paid");

    var invoice2 =
        new XentralV1Invoice(
            "INV-002", "RE-002", "CUST-123", new BigDecimal("2000.00"), null, null, "open");

    // When
    FinancialMetrics metrics = calculator.calculate(List.of(invoice1, invoice2), Map.of());

    // Then: lastOrderDate should be null
    assertThat(metrics.lastOrderDate()).isNull();
  }

  @Test
  @DisplayName("calculate() - handles invoices with null invoice dates gracefully")
  void testCalculate_HandlesNullInvoiceDates() {
    // Given: Mixed invoices (some with dates, some without)
    var invoice1 =
        new XentralV1Invoice(
            "INV-001",
            "RE-001",
            "CUST-123",
            new BigDecimal("1000.00"),
            null, // No invoice date
            LocalDate.of(2024, 10, 1),
            "paid");

    var invoice2 =
        new XentralV1Invoice(
            "INV-002",
            "RE-002",
            "CUST-123",
            new BigDecimal("2000.00"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            "paid");

    var balances =
        Map.of(
            "INV-001",
                new XentralV1InvoiceBalance(
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    BigDecimal.ZERO,
                    LocalDate.of(2024, 9, 15)),
            "INV-002",
                new XentralV1InvoiceBalance(
                    new BigDecimal("2000.00"),
                    new BigDecimal("2000.00"),
                    BigDecimal.ZERO,
                    LocalDate.of(2024, 9, 15)));

    // When
    FinancialMetrics metrics = calculator.calculate(List.of(invoice1, invoice2), balances);

    // Then: Should only calculate averageDaysToPay for invoice2 (has invoiceDate)
    assertThat(metrics.totalRevenue()).isEqualByComparingTo(new BigDecimal("3000.00"));
    assertThat(metrics.averageDaysToPay()).isEqualTo(14); // Only invoice2: 14 days
    assertThat(metrics.lastOrderDate()).isEqualTo(LocalDate.of(2024, 9, 1)); // Only invoice2
  }

  @Test
  @DisplayName("calculate() - handles single invoice correctly")
  void testCalculate_SingleInvoice() {
    // Given: Only one invoice
    var invoice =
        new XentralV1Invoice(
            "INV-001",
            "RE-001",
            "CUST-123",
            new BigDecimal("1500.00"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            "paid");

    var balance =
        Map.of(
            "INV-001",
            new XentralV1InvoiceBalance(
                new BigDecimal("1500.00"),
                new BigDecimal("1500.00"),
                BigDecimal.ZERO,
                LocalDate.of(2024, 9, 15))); // 14 days

    // When
    FinancialMetrics metrics = calculator.calculate(List.of(invoice), balance);

    // Then
    assertThat(metrics.totalRevenue()).isEqualByComparingTo(new BigDecimal("1500.00"));
    assertThat(metrics.averageDaysToPay()).isEqualTo(14);
    assertThat(metrics.lastOrderDate()).isEqualTo(LocalDate.of(2024, 9, 1));
  }

  @Test
  @DisplayName("calculate() - handles rounding in averageDaysToPay")
  void testCalculate_AverageDaysToPay_Rounding() {
    // Given: Invoices that produce non-integer average (15, 16, 17 days → avg 16.0)
    var invoice1 =
        new XentralV1Invoice(
            "INV-001",
            "RE-001",
            "CUST-123",
            new BigDecimal("1000.00"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            "paid");

    var invoice2 =
        new XentralV1Invoice(
            "INV-002",
            "RE-002",
            "CUST-123",
            new BigDecimal("2000.00"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            "paid");

    var invoice3 =
        new XentralV1Invoice(
            "INV-003",
            "RE-003",
            "CUST-123",
            new BigDecimal("1500.00"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            "paid");

    var balances =
        Map.of(
            "INV-001",
                new XentralV1InvoiceBalance(
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    BigDecimal.ZERO,
                    LocalDate.of(2024, 9, 16)), // 15 days
            "INV-002",
                new XentralV1InvoiceBalance(
                    new BigDecimal("2000.00"),
                    new BigDecimal("2000.00"),
                    BigDecimal.ZERO,
                    LocalDate.of(2024, 9, 17)), // 16 days
            "INV-003",
                new XentralV1InvoiceBalance(
                    new BigDecimal("1500.00"),
                    new BigDecimal("1500.00"),
                    BigDecimal.ZERO,
                    LocalDate.of(2024, 9, 18))); // 17 days

    // When
    FinancialMetrics metrics =
        calculator.calculate(List.of(invoice1, invoice2, invoice3), balances);

    // Then: (15 + 16 + 17) / 3 = 16.0 (exact)
    assertThat(metrics.averageDaysToPay()).isEqualTo(16);
  }

  @Test
  @DisplayName("calculate() - handles rounding with half-up for averageDaysToPay")
  void testCalculate_AverageDaysToPay_RoundingHalfUp() {
    // Given: Invoices that produce average of 14.5 days → should round to 15
    var invoice1 =
        new XentralV1Invoice(
            "INV-001",
            "RE-001",
            "CUST-123",
            new BigDecimal("1000.00"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            "paid");

    var invoice2 =
        new XentralV1Invoice(
            "INV-002",
            "RE-002",
            "CUST-123",
            new BigDecimal("2000.00"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            "paid");

    var balances =
        Map.of(
            "INV-001",
                new XentralV1InvoiceBalance(
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    BigDecimal.ZERO,
                    LocalDate.of(2024, 9, 15)), // 14 days
            "INV-002",
                new XentralV1InvoiceBalance(
                    new BigDecimal("2000.00"),
                    new BigDecimal("2000.00"),
                    BigDecimal.ZERO,
                    LocalDate.of(2024, 9, 16))); // 15 days

    // When
    FinancialMetrics metrics = calculator.calculate(List.of(invoice1, invoice2), balances);

    // Then: (14 + 15) / 2 = 14.5 → rounds to 15
    assertThat(metrics.averageDaysToPay()).isEqualTo(15);
  }

  @Test
  @DisplayName("calculate() - handles large invoice amounts correctly")
  void testCalculate_LargeAmounts() {
    // Given: Very large invoice amounts
    var invoice1 =
        new XentralV1Invoice(
            "INV-001",
            "RE-001",
            "CUST-123",
            new BigDecimal("999999.99"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            "paid");

    var invoice2 =
        new XentralV1Invoice(
            "INV-002",
            "RE-002",
            "CUST-123",
            new BigDecimal("1234567.89"),
            LocalDate.of(2024, 9, 15),
            LocalDate.of(2024, 10, 15),
            "paid");

    var balances =
        Map.of(
            "INV-001",
                new XentralV1InvoiceBalance(
                    new BigDecimal("999999.99"),
                    new BigDecimal("999999.99"),
                    BigDecimal.ZERO,
                    LocalDate.of(2024, 9, 10)),
            "INV-002",
                new XentralV1InvoiceBalance(
                    new BigDecimal("1234567.89"),
                    new BigDecimal("1234567.89"),
                    BigDecimal.ZERO,
                    LocalDate.of(2024, 9, 25)));

    // When
    FinancialMetrics metrics = calculator.calculate(List.of(invoice1, invoice2), balances);

    // Then: Should handle large amounts correctly
    assertThat(metrics.totalRevenue()).isEqualByComparingTo(new BigDecimal("2234567.88"));
  }

  @Test
  @DisplayName("calculate() - handles payment before invoice date (negative days)")
  void testCalculate_PaymentBeforeInvoiceDate() {
    // Given: Payment date is BEFORE invoice date (unusual but possible)
    var invoice =
        new XentralV1Invoice(
            "INV-001",
            "RE-001",
            "CUST-123",
            new BigDecimal("1000.00"),
            LocalDate.of(2024, 9, 15), // Invoice date
            LocalDate.of(2024, 10, 15),
            "paid");

    var balance =
        Map.of(
            "INV-001",
            new XentralV1InvoiceBalance(
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                BigDecimal.ZERO,
                LocalDate.of(2024, 9, 10))); // Payment BEFORE invoice date

    // When
    FinancialMetrics metrics = calculator.calculate(List.of(invoice), balance);

    // Then: Should handle negative days (-5 days)
    assertThat(metrics.averageDaysToPay()).isEqualTo(-5);
  }

  @Test
  @DisplayName("calculate() - ignores invoices without matching balances")
  void testCalculate_IgnoresInvoicesWithoutBalances() {
    // Given: 2 invoices, but only 1 has balance data
    var invoice1 =
        new XentralV1Invoice(
            "INV-001",
            "RE-001",
            "CUST-123",
            new BigDecimal("1000.00"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            "paid");

    var invoice2 =
        new XentralV1Invoice(
            "INV-002",
            "RE-002",
            "CUST-123",
            new BigDecimal("2000.00"),
            LocalDate.of(2024, 9, 15),
            LocalDate.of(2024, 10, 15),
            "paid");

    var balances =
        Map.of(
            "INV-001",
            new XentralV1InvoiceBalance(
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                BigDecimal.ZERO,
                LocalDate.of(2024, 9, 10)));
    // INV-002 has NO balance data

    // When
    FinancialMetrics metrics = calculator.calculate(List.of(invoice1, invoice2), balances);

    // Then: Should only count invoice1 for revenue and payment days
    assertThat(metrics.totalRevenue()).isEqualByComparingTo(new BigDecimal("1000.00"));
    assertThat(metrics.averageDaysToPay()).isEqualTo(9); // Only invoice1
    assertThat(metrics.lastOrderDate()).isEqualTo(LocalDate.of(2024, 9, 15)); // Both invoices
  }
}

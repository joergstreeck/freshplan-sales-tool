package de.freshplan.modules.xentral.dto.v1;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.modules.xentral.dto.XentralInvoiceDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for XentralV1InvoiceMapper.
 *
 * <p>Sprint: 2.1.7.2 - D2e Real API Adapter Tests
 */
@QuarkusTest
class XentralV1InvoiceMapperTest {

  @Inject XentralV1InvoiceMapper mapper;

  @Test
  @DisplayName("toDTO() - maps invoice with fully paid balance")
  void testToDTO_WithFullyPaidBalance() {
    // Given
    var invoice =
        new XentralV1Invoice(
            "INV-001",
            "RE-2024-00123",
            "CUST-456",
            new BigDecimal("1500.00"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            "paid");

    var balance =
        new XentralV1InvoiceBalance(
            new BigDecimal("1500.00"), // total
            new BigDecimal("1500.00"), // paid
            BigDecimal.ZERO, // open
            LocalDate.of(2024, 9, 15) // paymentDate
            );

    // When
    XentralInvoiceDTO dto = mapper.toDTO(invoice, balance);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.invoiceId()).isEqualTo("INV-001");
    assertThat(dto.invoiceNumber()).isEqualTo("RE-2024-00123");
    assertThat(dto.customerId()).isEqualTo("CUST-456");
    assertThat(dto.amount()).isEqualByComparingTo("1500.00");
    assertThat(dto.invoiceDate()).isEqualTo(LocalDate.of(2024, 9, 1));
    assertThat(dto.dueDate()).isEqualTo(LocalDate.of(2024, 10, 1));
    assertThat(dto.paymentDate()).isEqualTo(LocalDate.of(2024, 9, 15));
    assertThat(dto.status()).isEqualTo("paid"); // balance.isFullyPaid() → "paid"
  }

  @Test
  @DisplayName("toDTO() - maps invoice with partially paid balance")
  void testToDTO_WithPartiallyPaidBalance() {
    // Given
    var invoice =
        new XentralV1Invoice(
            "INV-002",
            "RE-2024-00124",
            "CUST-789",
            new BigDecimal("2000.00"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            "open");

    var balance =
        new XentralV1InvoiceBalance(
            new BigDecimal("2000.00"), // total
            new BigDecimal("800.00"), // paid (40%)
            new BigDecimal("1200.00"), // open
            null // no full payment yet
            );

    // When
    XentralInvoiceDTO dto = mapper.toDTO(invoice, balance);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.invoiceId()).isEqualTo("INV-002");
    assertThat(dto.amount()).isEqualByComparingTo("2000.00");
    assertThat(dto.paymentDate()).isNull(); // not fully paid
    assertThat(dto.status()).isEqualTo("partial"); // balance.isPartiallyPaid() → "partial"
  }

  @Test
  @DisplayName("toDTO() - maps invoice without balance data")
  void testToDTO_WithoutBalance() {
    // Given: invoice only (no balance endpoint available)
    var invoice =
        new XentralV1Invoice(
            "INV-003",
            "RE-2024-00125",
            "CUST-111",
            new BigDecimal("750.00"),
            LocalDate.of(2024, 9, 15),
            LocalDate.of(2024, 10, 15),
            "open");

    // When: map without balance data
    XentralInvoiceDTO dto = mapper.toDTO(invoice, null);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.invoiceId()).isEqualTo("INV-003");
    assertThat(dto.invoiceNumber()).isEqualTo("RE-2024-00125");
    assertThat(dto.customerId()).isEqualTo("CUST-111");
    assertThat(dto.amount()).isEqualByComparingTo("750.00");
    assertThat(dto.invoiceDate()).isEqualTo(LocalDate.of(2024, 9, 15));
    assertThat(dto.dueDate()).isEqualTo(LocalDate.of(2024, 10, 15));
    assertThat(dto.paymentDate()).isNull(); // no balance data
    assertThat(dto.status()).isEqualTo("open"); // fallback to invoice.status
  }

  @Test
  @DisplayName("toDTO() - convenience method without balance")
  void testToDTO_ConvenienceMethod() {
    // Given
    var invoice =
        new XentralV1Invoice(
            "INV-004",
            "RE-2024-00126",
            "CUST-222",
            new BigDecimal("500.00"),
            LocalDate.of(2024, 10, 1),
            LocalDate.of(2024, 11, 1),
            "paid");

    // When: use convenience method
    XentralInvoiceDTO dto = mapper.toDTO(invoice);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.invoiceId()).isEqualTo("INV-004");
    assertThat(dto.status()).isEqualTo("paid"); // fallback to invoice.status
    assertThat(dto.paymentDate()).isNull();
  }

  @Test
  @DisplayName("toDTO() - returns null for null invoice")
  void testToDTO_NullInvoice() {
    // When
    XentralInvoiceDTO dto = mapper.toDTO(null, null);

    // Then
    assertThat(dto).isNull();
  }

  @Test
  @DisplayName("mapStatus() - balance fully paid overrides invoice status")
  void testMapStatus_BalanceFullyPaid() {
    // Given: invoice.status = "open", but balance says fully paid
    var invoice =
        new XentralV1Invoice(
            "INV-005",
            "RE-2024-00127",
            "CUST-333",
            new BigDecimal("1000.00"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            "open" // invoice status not updated yet
            );

    var balance =
        new XentralV1InvoiceBalance(
            new BigDecimal("1000.00"),
            new BigDecimal("1000.00"), // fully paid
            BigDecimal.ZERO,
            LocalDate.of(2024, 9, 20));

    // When
    XentralInvoiceDTO dto = mapper.toDTO(invoice, balance);

    // Then: balance data wins
    assertThat(dto.status()).isEqualTo("paid");
  }

  @Test
  @DisplayName("mapStatus() - handles various invoice status strings")
  void testMapStatus_VariousStatuses() {
    // Test different status strings
    assertThat(
            mapper
                .toDTO(
                    createInvoice("completed"), // English variant
                    null)
                .status())
        .isEqualTo("paid");

    assertThat(mapper.toDTO(createInvoice("overdue"), null).status()).isEqualTo("overdue");

    assertThat(mapper.toDTO(createInvoice("cancelled"), null).status()).isEqualTo("cancelled");

    assertThat(
            mapper
                .toDTO(
                    createInvoice("canceled"), // US spelling
                    null)
                .status())
        .isEqualTo("cancelled");

    assertThat(mapper.toDTO(createInvoice("unknown_status"), null).status()).isEqualTo("open");

    assertThat(mapper.toDTO(createInvoice(null), null).status()).isEqualTo("open");
  }

  @Test
  @DisplayName("mapStatus() - balance partially paid overrides invoice status")
  void testMapStatus_BalancePartiallyPaid() {
    // Given: invoice.status = "paid", but balance shows partial payment
    var invoice =
        new XentralV1Invoice(
            "INV-006",
            "RE-2024-00128",
            "CUST-444",
            new BigDecimal("3000.00"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            "paid" // incorrect status
            );

    var balance =
        new XentralV1InvoiceBalance(
            new BigDecimal("3000.00"),
            new BigDecimal("1500.00"), // only 50% paid
            new BigDecimal("1500.00"),
            null);

    // When
    XentralInvoiceDTO dto = mapper.toDTO(invoice, balance);

    // Then: balance data wins
    assertThat(dto.status()).isEqualTo("partial");
  }

  @Test
  @DisplayName("toDTO() - maps all fields correctly for complex scenario")
  void testToDTO_ComplexScenario() {
    // Given: realistic invoice + balance scenario
    var invoice =
        new XentralV1Invoice(
            "INV-789",
            "RE-2024-12345",
            "CUST-ACME-001",
            new BigDecimal("15750.99"),
            LocalDate.of(2024, 10, 1),
            LocalDate.of(2024, 10, 31),
            "paid");

    var balance =
        new XentralV1InvoiceBalance(
            new BigDecimal("15750.99"),
            new BigDecimal("15750.99"),
            BigDecimal.ZERO,
            LocalDate.of(2024, 10, 20));

    // When
    XentralInvoiceDTO dto = mapper.toDTO(invoice, balance);

    // Then: all fields mapped correctly
    assertThat(dto.invoiceId()).isEqualTo("INV-789");
    assertThat(dto.invoiceNumber()).isEqualTo("RE-2024-12345");
    assertThat(dto.customerId()).isEqualTo("CUST-ACME-001");
    assertThat(dto.amount()).isEqualByComparingTo("15750.99");
    assertThat(dto.invoiceDate()).isEqualTo(LocalDate.of(2024, 10, 1));
    assertThat(dto.dueDate()).isEqualTo(LocalDate.of(2024, 10, 31));
    assertThat(dto.paymentDate()).isEqualTo(LocalDate.of(2024, 10, 20));
    assertThat(dto.status()).isEqualTo("paid");
  }

  // --- Helper Methods ---

  private XentralV1Invoice createInvoice(String status) {
    return new XentralV1Invoice(
        "INV-TEST",
        "RE-TEST",
        "CUST-TEST",
        BigDecimal.TEN,
        LocalDate.now(),
        LocalDate.now(),
        status);
  }
}

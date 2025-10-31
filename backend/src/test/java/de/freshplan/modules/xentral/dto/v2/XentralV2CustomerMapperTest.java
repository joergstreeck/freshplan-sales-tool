package de.freshplan.modules.xentral.dto.v2;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.modules.xentral.dto.XentralCustomerDTO;
import de.freshplan.modules.xentral.dto.v2.XentralV2Customer.General;
import de.freshplan.modules.xentral.dto.v2.XentralV2Customer.SalesRep;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for XentralV2CustomerMapper.
 *
 * <p>Sprint: 2.1.7.2 - D2e Real API Adapter Tests
 */
@QuarkusTest
class XentralV2CustomerMapperTest {

  @Inject XentralV2CustomerMapper mapper;

  @Test
  @DisplayName("toDTO() - maps v2 customer to DTO with null financial fields")
  void testToDTO_CompleteData() {
    // Given
    var customer =
        new XentralV2Customer(
            "123",
            new General("ACME Corp", "info@acme.com", "+49-123-456789", "https://acme.com"),
            null, // project
            "active",
            new SalesRep("SALES-001", "John Doe", "john@freshplan.com"));

    // When
    XentralCustomerDTO dto = mapper.toDTO(customer);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.xentralId()).isEqualTo("123");
    assertThat(dto.companyName()).isEqualTo("ACME Corp");
    assertThat(dto.email()).isEqualTo("info@acme.com");
    assertThat(dto.phone()).isEqualTo("+49-123-456789");
    assertThat(dto.salesRepId()).isEqualTo("SALES-001");

    // Financial fields should be null (enriched later)
    assertThat(dto.totalRevenue()).isNull();
    assertThat(dto.averageDaysToPay()).isNull();
    assertThat(dto.lastOrderDate()).isNull();
  }

  @Test
  @DisplayName("toDTO() - handles missing optional fields gracefully")
  void testToDTO_MissingOptionalFields() {
    // Given: customer without sales rep, phone, email
    var customer =
        new XentralV2Customer(
            "456",
            new General("TechStart GmbH", null, null, null), // no email, phone
            null,
            "active",
            null); // no sales rep

    // When
    XentralCustomerDTO dto = mapper.toDTO(customer);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.xentralId()).isEqualTo("456");
    assertThat(dto.companyName()).isEqualTo("TechStart GmbH");
    assertThat(dto.email()).isEmpty(); // empty string, not null
    assertThat(dto.phone()).isEmpty();
    assertThat(dto.salesRepId()).isNull();
  }

  @Test
  @DisplayName("toDTO() - returns null for null customer")
  void testToDTO_NullCustomer() {
    // When
    XentralCustomerDTO dto = mapper.toDTO(null);

    // Then
    assertThat(dto).isNull();
  }

  @Test
  @DisplayName("enrichWithFinancialData() - adds financial metrics to existing DTO")
  void testEnrichWithFinancialData() {
    // Given: base DTO without financial data
    var baseDTO =
        new XentralCustomerDTO(
            "123",
            "ACME Corp",
            "info@acme.com",
            "+49-123-456789",
            null, // no financial data
            null,
            null,
            "SALES-001");

    // When: enrich with financial metrics
    BigDecimal totalRevenue = new BigDecimal("125000.50");
    Integer averageDaysToPay = 28;
    LocalDate lastOrderDate = LocalDate.of(2024, 10, 15);

    XentralCustomerDTO enrichedDTO =
        mapper.enrichWithFinancialData(baseDTO, totalRevenue, averageDaysToPay, lastOrderDate);

    // Then
    assertThat(enrichedDTO).isNotNull();

    // Original fields preserved
    assertThat(enrichedDTO.xentralId()).isEqualTo("123");
    assertThat(enrichedDTO.companyName()).isEqualTo("ACME Corp");
    assertThat(enrichedDTO.email()).isEqualTo("info@acme.com");
    assertThat(enrichedDTO.phone()).isEqualTo("+49-123-456789");
    assertThat(enrichedDTO.salesRepId()).isEqualTo("SALES-001");

    // Financial fields added
    assertThat(enrichedDTO.totalRevenue()).isEqualByComparingTo(totalRevenue);
    assertThat(enrichedDTO.averageDaysToPay()).isEqualTo(28);
    assertThat(enrichedDTO.lastOrderDate()).isEqualTo(lastOrderDate);
  }

  @Test
  @DisplayName("enrichWithFinancialData() - handles null financial metrics gracefully")
  void testEnrichWithFinancialData_NullMetrics() {
    // Given: base DTO
    var baseDTO =
        new XentralCustomerDTO(
            "789", "NoInvoices Inc", "test@example.com", null, null, null, null, null);

    // When: enrich with null financial metrics (customer has no invoices)
    XentralCustomerDTO enrichedDTO = mapper.enrichWithFinancialData(baseDTO, null, null, null);

    // Then
    assertThat(enrichedDTO).isNotNull();
    assertThat(enrichedDTO.xentralId()).isEqualTo("789");
    assertThat(enrichedDTO.companyName()).isEqualTo("NoInvoices Inc");

    // Financial fields remain null
    assertThat(enrichedDTO.totalRevenue()).isNull();
    assertThat(enrichedDTO.averageDaysToPay()).isNull();
    assertThat(enrichedDTO.lastOrderDate()).isNull();
  }

  @Test
  @DisplayName("enrichWithFinancialData() - returns null for null DTO")
  void testEnrichWithFinancialData_NullDTO() {
    // When
    XentralCustomerDTO enrichedDTO =
        mapper.enrichWithFinancialData(null, BigDecimal.TEN, 30, LocalDate.now());

    // Then
    assertThat(enrichedDTO).isNull();
  }
}

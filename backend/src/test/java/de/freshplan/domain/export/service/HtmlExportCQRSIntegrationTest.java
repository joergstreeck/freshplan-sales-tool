package de.freshplan.domain.export.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.CustomerType;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.export.service.dto.ExportRequest;
import de.freshplan.test.builders.CustomerTestDataFactory;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * FIXED Integration Test for HtmlExportService CQRS Implementation.
 *
 * <p>WICHTIG: Verwendet @TestTransaction für ALLE Tests um Daten-Pollution zu vermeiden! Alle Daten
 * werden nach jedem Test automatisch zurückgerollt.
 *
 * @author Claude (Fixed Version)
 * @since Test-Pollution Fix
 */
@QuarkusTest
@Tag("integration")
@TestProfile(HtmlExportCQRSTestProfile.class)
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager"})
@DisplayName("HtmlExport Service CQRS Integration Test")
class HtmlExportCQRSIntegrationTest {

  private static final Logger LOG = Logger.getLogger(HtmlExportCQRSIntegrationTest.class);

  @Inject HtmlExportService htmlExportService;
  @Inject CustomerRepository customerRepository;

  @ConfigProperty(name = "features.cqrs.enabled")
  boolean cqrsEnabled;

  private String testRunId;

  @BeforeEach
  void setUp() {
    testRunId = UUID.randomUUID().toString().substring(0, 8);
    LOG.infof("Starting test run: %s (with automatic rollback)", testRunId);
  }

  @AfterEach
  @TestTransaction
  void cleanup() {
    // Delete test customers
    customerRepository.delete("customerNumber LIKE 'TEST-%'");
  }

  @Test
  @TestTransaction
  @DisplayName("CQRS mode should be enabled for this test")
  void testCQRSModeIsEnabled() {
    assertThat(cqrsEnabled)
        .as("CQRS mode should be enabled via HtmlExportCQRSTestProfile")
        .isTrue();
  }

  @Test
  @TestTransaction
  @DisplayName("Generate HTML export with all customers")
  void generateCustomersHtml_allCustomers_shouldReturnHtml() {
    // Given - Create test customers
    Customer hotel =
        createTestCustomer(
            "EXP-H1",
            "[TEST] Export Hotel",
            CustomerType.UNTERNEHMEN,
            CustomerStatus.AKTIV,
            Industry.HOTEL);
    Customer restaurant =
        createTestCustomer(
            "EXP-R1",
            "[TEST] Export Restaurant",
            CustomerType.NEUKUNDE,
            CustomerStatus.PROSPECT,
            Industry.RESTAURANT);

    customerRepository.flush();

    // Given - Basic export request
    ExportRequest request =
        ExportRequest.builder()
            .entityType("customer")
            .includeDetails(true)
            .includeStats(false)
            .includeContacts(false)
            .page(0)
            .size(100)
            .build();

    // When - Generate HTML
    String html = htmlExportService.generateCustomersHtml(request);

    // Then - Validate HTML structure
    assertThat(html).isNotNull();
    assertThat(html).contains("<!DOCTYPE html>");
    assertThat(html).contains("<html lang=\"de\">");
    assertThat(html).contains("FreshPlan Kunden-Export");
    assertThat(html).contains("[TEST] Export Hotel");
    assertThat(html).contains("[TEST] Export Restaurant");
  }

  @Test
  @TestTransaction
  @DisplayName("Export with status filter should only include matching customers")
  void generateCustomersHtml_withStatusFilter_shouldFilterResults() {
    // Given - Create test customers
    Customer active =
        createTestCustomer(
            "EXP-A1",
            "[TEST] Active Customer",
            CustomerType.UNTERNEHMEN,
            CustomerStatus.AKTIV,
            Industry.HOTEL);
    Customer lead =
        createTestCustomer(
            "EXP-L1",
            "[TEST] Lead Customer",
            CustomerType.NEUKUNDE,
            CustomerStatus.PROSPECT,
            Industry.RESTAURANT);

    customerRepository.flush();

    // Given - Filter for AKTIV status only
    ExportRequest request =
        ExportRequest.builder()
            .entityType("customer")
            .status(Arrays.asList("AKTIV"))
            .includeDetails(true)
            .page(0)
            .size(100)
            .build();

    // When - Generate HTML
    String html = htmlExportService.generateCustomersHtml(request);

    // Then - Should only include active customer
    assertThat(html).contains("[TEST] Active Customer");
    assertThat(html).doesNotContain("[TEST] Lead Customer");
  }

  @Test
  @TestTransaction
  @DisplayName("Export with industry filter should only include matching customers")
  void generateCustomersHtml_withIndustryFilter_shouldFilterResults() {
    // Given - Create test customers
    Customer hotel =
        createTestCustomer(
            "EXP-H2",
            "[TEST] Hotel Export",
            CustomerType.UNTERNEHMEN,
            CustomerStatus.AKTIV,
            Industry.HOTEL);
    Customer restaurant =
        createTestCustomer(
            "EXP-R2",
            "[TEST] Restaurant Export",
            CustomerType.UNTERNEHMEN,
            CustomerStatus.AKTIV,
            Industry.RESTAURANT);

    customerRepository.flush();

    // Given - Filter for HOTEL industry
    ExportRequest request =
        ExportRequest.builder()
            .entityType("customer")
            .businessType("HOTEL")
            .includeDetails(true)
            .page(0)
            .size(100)
            .build();

    // When - Generate HTML
    String html = htmlExportService.generateCustomersHtml(request);

    // Then - Should include hotel but not restaurant
    assertThat(html).contains("[TEST] Hotel Export");
    assertThat(html).contains("HOTEL");
    assertThat(html).doesNotContain("[TEST] Restaurant Export");
    assertThat(html).doesNotContain("RESTAURANT");
  }

  @Test
  @TestTransaction
  @DisplayName("Export with date range should filter by creation date")
  void generateCustomersHtml_withDateRange_shouldFilterByDate() {
    // Given - Create customers with different creation dates
    Customer recent =
        createTestCustomer(
            "EXP-NEW",
            "[TEST] Recent Customer",
            CustomerType.UNTERNEHMEN,
            CustomerStatus.AKTIV,
            Industry.HOTEL);
    recent.setCreatedAt(LocalDateTime.now());

    Customer old =
        createTestCustomer(
            "EXP-OLD",
            "[TEST] Old Customer",
            CustomerType.UNTERNEHMEN,
            CustomerStatus.AKTIV,
            Industry.HOTEL);
    old.setCreatedAt(LocalDateTime.now().minusDays(30));

    customerRepository.flush();

    // Given - Date range that excludes older customer
    LocalDateTime fromDate = LocalDateTime.now().minusDays(7);
    LocalDateTime toDate = LocalDateTime.now().plusDays(1);

    ExportRequest request =
        ExportRequest.builder()
            .entityType("customer")
            .dateFrom(fromDate)
            .dateTo(toDate)
            .includeDetails(true)
            .page(0)
            .size(100)
            .build();

    // When - Generate HTML
    String html = htmlExportService.generateCustomersHtml(request);

    // Then - Should only include recent customer
    assertThat(html).contains("[TEST] Recent Customer");
    assertThat(html).doesNotContain("[TEST] Old Customer");
  }

  @Test
  @TestTransaction
  @DisplayName("Export with includeStats should add statistics section")
  void generateCustomersHtml_withIncludeStats_shouldAddStatistics() {
    // Given - Create test data
    createTestCustomer(
        "EXP-S1",
        "[TEST] Stats Customer 1",
        CustomerType.UNTERNEHMEN,
        CustomerStatus.AKTIV,
        Industry.HOTEL);
    createTestCustomer(
        "EXP-S2",
        "[TEST] Stats Customer 2",
        CustomerType.NEUKUNDE,
        CustomerStatus.PROSPECT,
        Industry.RESTAURANT);

    customerRepository.flush();

    // Given - Request with stats
    ExportRequest request =
        ExportRequest.builder()
            .entityType("customer")
            .includeDetails(true)
            .includeStats(true)
            .page(0)
            .size(100)
            .build();

    // When - Generate HTML
    String html = htmlExportService.generateCustomersHtml(request);

    // Then - Should include statistics section
    assertThat(html).contains("Statistik");
    assertThat(html).containsPattern("Gesamt.*Kunden|Total.*Customer");
  }

  @Test
  @TestTransaction
  @DisplayName("Export with pagination should respect page and size")
  void generateCustomersHtml_withPagination_shouldRespectLimits() {
    // Given - Create 10 test customers
    for (int i = 0; i < 10; i++) {
      createTestCustomer(
          "PG" + i,
          "[TEST] Pagination Test " + i,
          CustomerType.UNTERNEHMEN,
          CustomerStatus.AKTIV,
          Industry.SONSTIGE);
    }
    customerRepository.flush();

    // When - Request first page with size 5
    ExportRequest request =
        ExportRequest.builder().entityType("customer").includeDetails(true).page(0).size(5).build();

    String html = htmlExportService.generateCustomersHtml(request);

    // Then - Should have limited results
    assertThat(html).isNotNull();
    // Count occurrences of customer markers
    int customerCount =
        html.split("<tr class=\"customer-row\"|<div class=\"customer\"|Kunde:|Customer:").length
            - 1;
    assertThat(customerCount).isLessThanOrEqualTo(5);

    // All data will be rolled back automatically - no pollution!
  }

  @Test
  @TestTransaction
  @DisplayName("Generated HTML should have proper structure for PDF conversion")
  void generateCustomersHtml_shouldHaveProperStructure() {
    // Given - Create test data
    createTestCustomer(
        "EXP-PDF",
        "[TEST] PDF Export",
        CustomerType.UNTERNEHMEN,
        CustomerStatus.AKTIV,
        Industry.HOTEL);
    customerRepository.flush();

    // Given - Basic request
    ExportRequest request =
        ExportRequest.builder()
            .entityType("customer")
            .includeDetails(true)
            .page(0)
            .size(100)
            .build();

    // When - Generate HTML
    String html = htmlExportService.generateCustomersHtml(request);

    // Then - Validate HTML structure
    assertThat(html)
        .contains("<!DOCTYPE html>")
        .contains("<html")
        .contains("<head>")
        .contains("<meta charset=\"UTF-8\">")
        .contains("<style>")
        .contains("@page") // Print styles
        .contains("@media print") // Print media query
        .contains("<body>")
        .contains("</body>")
        .contains("</html>");
  }

  @Test
  @TestTransaction
  @DisplayName("Export with no matching customers should return empty report")
  void generateCustomersHtml_noMatches_shouldReturnEmptyReport() {
    // Given - Create test data
    createTestCustomer(
        "EXP-NOMATCH",
        "[TEST] No Match",
        CustomerType.UNTERNEHMEN,
        CustomerStatus.AKTIV,
        Industry.HOTEL);
    customerRepository.flush();

    // Given - Filter that matches nothing
    ExportRequest request =
        ExportRequest.builder()
            .entityType("customer")
            .businessType("XXXNONEXISTENTXXX")
            .includeDetails(true)
            .page(0)
            .size(100)
            .build();

    // When - Generate HTML
    String html = htmlExportService.generateCustomersHtml(request);

    // Then - Should return HTML but without customer data
    assertThat(html).isNotNull();
    assertThat(html).contains("<!DOCTYPE html>");
    assertThat(html).doesNotContain("[TEST] No Match");
    // Should indicate no results
    assertThat(html.toLowerCase())
        .containsAnyOf(
            "keine kunden", "no customers", "0 ergebnis", "keine treffer", "keine daten");
  }

  @Test
  @TestTransaction
  @DisplayName("CQRS mode should properly delegate export operations")
  void cqrsMode_shouldProperlyDelegateOperations() {
    // Given - Create test data
    createTestCustomer(
        "EXP-CQRS",
        "[TEST] CQRS Export",
        CustomerType.UNTERNEHMEN,
        CustomerStatus.AKTIV,
        Industry.HOTEL);
    customerRepository.flush();

    // Create various export requests
    ExportRequest basicRequest =
        ExportRequest.builder().entityType("customer").includeDetails(true).build();

    ExportRequest filteredRequest =
        ExportRequest.builder()
            .entityType("customer")
            .status(Arrays.asList("AKTIV", "PROSPECT"))
            .businessType("HOTEL")
            .includeDetails(true)
            .includeStats(true)
            .build();

    ExportRequest paginatedRequest =
        ExportRequest.builder().entityType("customer").page(0).size(10).build();

    // All should work in CQRS mode
    String html1 = htmlExportService.generateCustomersHtml(basicRequest);
    String html2 = htmlExportService.generateCustomersHtml(filteredRequest);
    String html3 = htmlExportService.generateCustomersHtml(paginatedRequest);

    assertThat(html1).isNotNull().contains("<!DOCTYPE html>");
    assertThat(html2).isNotNull().contains("<!DOCTYPE html>");
    assertThat(html3).isNotNull().contains("<!DOCTYPE html>");

    // Verify CQRS is enabled
    assertThat(cqrsEnabled).isTrue();
  }

  // Helper method
  private Customer createTestCustomer(
      String number, String name, CustomerType type, CustomerStatus status, Industry industry) {
    Customer customer =
        CustomerTestDataFactory.builder()
            .withCompanyName(name)
            .withStatus(status)
            .withIndustry(industry)
            .withExpectedAnnualVolume(new BigDecimal("100000"))
            .build();
    customer.setCustomerNumber(number);
    customer.setCompanyName(name); // Override to remove [TEST-xxx] prefix
    customer.setIsTestData(true);
    customerRepository.persist(customer);
    return customer;
  }
}

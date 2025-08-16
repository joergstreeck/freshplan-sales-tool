package de.freshplan.domain.search.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.CustomerType;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.search.service.dto.CustomerSearchDto;
import de.freshplan.domain.search.service.dto.SearchResult;
import de.freshplan.domain.search.service.dto.SearchResults;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * FIXED Integration Test for SearchService CQRS Implementation.
 *
 * <p>WICHTIG: Verwendet @TestTransaction für ALLE Tests um Daten-Pollution zu vermeiden! Alle Daten
 * werden nach jedem Test automatisch zurückgerollt.
 *
 * @author Claude (Fixed Version)
 * @since Test-Pollution Fix
 */
@QuarkusTest
@TestProfile(SearchCQRSTestProfile.class)
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
@DisplayName("Search Service CQRS Integration Test")
class SearchCQRSIntegrationTest {

  private static final Logger LOG = Logger.getLogger(SearchCQRSIntegrationTest.class);

  @Inject SearchService searchService;
  @Inject CustomerRepository customerRepository;

  @ConfigProperty(name = "features.cqrs.enabled")
  boolean cqrsEnabled;

  private String testRunId;

  @BeforeEach
  void setUp() {
    testRunId = UUID.randomUUID().toString().substring(0, 8);
    LOG.infof("Starting test run: %s (with automatic rollback)", testRunId);
  }

  @Test
  @TestTransaction
  @DisplayName("CQRS mode should be enabled for this test")
  void testCQRSModeIsEnabled() {
    assertThat(cqrsEnabled).as("CQRS mode should be enabled via SearchCQRSTestProfile").isTrue();
  }

  @Test
  @TestTransaction
  @DisplayName("Universal search should find customers by various fields")
  void universalSearch_shouldFindByVariousFields() {
    // Given - Create test customers
    Customer hotel =
        createTestCustomer(
            "SRCH-H1",
            "[TEST] Hotel Search",
            CustomerType.UNTERNEHMEN,
            CustomerStatus.AKTIV,
            Industry.HOTEL);
    Customer restaurant =
        createTestCustomer(
            "SRCH-R1",
            "[TEST] Restaurant Search",
            CustomerType.NEUKUNDE,
            CustomerStatus.LEAD,
            Industry.RESTAURANT);

    customerRepository.flush();

    // When - Search by company name
    SearchResults results = searchService.universalSearch("Hotel Search", false, false, 10);

    // Then
    assertThat(results).isNotNull();
    assertThat(results.getCustomers()).hasSize(1);
    SearchResult firstResult = results.getCustomers().get(0);
    assertThat(firstResult.getData()).isInstanceOf(CustomerSearchDto.class);
    CustomerSearchDto customerData = (CustomerSearchDto) firstResult.getData();
    assertThat(customerData.getCompanyName()).contains("Hotel Search");
  }

  @Test
  @TestTransaction
  @DisplayName("Search should respect inactive filter")
  void universalSearch_withInactiveFilter_shouldFilterCorrectly() {
    // Given - Create customers with unique test identifier
    String uniqueTestId = "SRCH-" + testRunId;
    Customer active =
        createTestCustomer(
            "SRCH-A1",
            uniqueTestId + " Active Customer",
            CustomerType.UNTERNEHMEN,
            CustomerStatus.AKTIV,
            Industry.HOTEL);
    Customer inactive =
        createTestCustomer(
            "SRCH-I1",
            uniqueTestId + " Inactive Customer",
            CustomerType.UNTERNEHMEN,
            CustomerStatus.INAKTIV,
            Industry.HOTEL);

    customerRepository.flush();

    // When - Search with unique identifier to avoid conflicts with existing test data
    SearchResults withoutInactive = searchService.universalSearch(uniqueTestId, false, false, 10);

    // When - Search with inactive
    SearchResults withInactive = searchService.universalSearch(uniqueTestId, false, true, 10);

    // Then
    assertThat(withoutInactive.getCustomers())
        .extracting(sr -> ((CustomerSearchDto) sr.getData()).getCompanyName())
        .contains(uniqueTestId + " Active Customer")
        .doesNotContain(uniqueTestId + " Inactive Customer");

    assertThat(withInactive.getCustomers())
        .extracting(sr -> ((CustomerSearchDto) sr.getData()).getCompanyName())
        .contains(uniqueTestId + " Active Customer", uniqueTestId + " Inactive Customer");
  }

  @Test
  @TestTransaction
  @DisplayName("Quick search should respect limit")
  void quickSearch_shouldRespectLimit() {
    // Given - Create 10 test customers
    for (int i = 0; i < 10; i++) {
      createTestCustomer(
          "SRCH-Q" + i,
          "[TEST] Quick " + i,
          CustomerType.UNTERNEHMEN,
          CustomerStatus.AKTIV,
          Industry.HOTEL);
    }
    customerRepository.flush();

    // When - Quick search with limit 5
    SearchResults results = searchService.quickSearch("[TEST] Quick", 5);

    // Then
    assertThat(results).isNotNull();
    assertThat(results.getCustomers()).hasSize(5);
  }

  @Test
  @TestTransaction
  @DisplayName("Search should handle large datasets efficiently")
  void search_withLargeDataset_shouldPerformWell() {
    // Given - Create 100 test customers
    for (int i = 0; i < 100; i++) {
      Customer c = new Customer();
      c.setCustomerNumber("PERF" + i);
      c.setCompanyName("[TEST] Performance Test " + i);
      c.setCustomerType(CustomerType.UNTERNEHMEN);
      c.setStatus(i % 3 == 0 ? CustomerStatus.AKTIV : CustomerStatus.LEAD);
      c.setIndustry(Industry.values()[i % Industry.values().length]);
      c.setExpectedAnnualVolume(new BigDecimal(100000 + i * 1000));
      c.setIsTestData(true);
      c.setCreatedBy("testuser");
      c.setCreatedAt(LocalDateTime.now());
      customerRepository.persist(c);
    }
    customerRepository.flush();

    // When - Perform search
    long startTime = System.currentTimeMillis();
    SearchResults results =
        searchService.universalSearch(
            "[TEST] Performance",
            false, // includeContacts
            false, // includeInactive
            20 // limit
            );
    long executionTime = System.currentTimeMillis() - startTime;

    // Then - Should complete quickly
    assertThat(results).isNotNull();
    assertThat(results.getCustomers()).hasSize(20); // Should respect limit
    assertThat(executionTime).isLessThan(1000L); // Should complete within 1 second

    // All data will be rolled back automatically - no pollution!
  }

  @Test
  @TestTransaction
  @DisplayName("Search relevance should rank exact matches higher")
  void searchRelevance_shouldRankCorrectly() {
    // Given
    Customer exact =
        createTestCustomer(
            "REL-1",
            "Relevance Test",
            CustomerType.UNTERNEHMEN,
            CustomerStatus.AKTIV,
            Industry.HOTEL);
    Customer partial =
        createTestCustomer(
            "REL-2",
            "Test of Relevance",
            CustomerType.UNTERNEHMEN,
            CustomerStatus.AKTIV,
            Industry.HOTEL);
    Customer distant =
        createTestCustomer(
            "REL-3",
            "Something with Relevance in it",
            CustomerType.UNTERNEHMEN,
            CustomerStatus.AKTIV,
            Industry.HOTEL);

    customerRepository.flush();

    // When
    SearchResults results = searchService.universalSearch("Relevance Test", false, false, 10);

    // Then - Exact match should be first
    assertThat(results.getCustomers()).isNotEmpty();
    SearchResult firstResult = results.getCustomers().get(0);
    CustomerSearchDto customerData = (CustomerSearchDto) firstResult.getData();
    assertThat(customerData.getCompanyName()).isEqualTo("Relevance Test");
  }

  @Test
  @TestTransaction
  @DisplayName("Search for non-existent term should return empty results")
  void universalSearch_nonExistentTerm_shouldReturnEmpty() {
    // Given - Some test data
    createTestCustomer(
        "EMPTY-1",
        "[TEST] Existing Customer",
        CustomerType.UNTERNEHMEN,
        CustomerStatus.AKTIV,
        Industry.HOTEL);
    customerRepository.flush();

    // When
    SearchResults results = searchService.universalSearch("DOES_NOT_EXIST_12345", false, false, 10);

    // Then
    assertThat(results).isNotNull();
    assertThat(results.getCustomers()).isEmpty();
    assertThat(results.getTotalCount()).isEqualTo(0);
  }

  // Helper method
  private Customer createTestCustomer(
      String number, String name, CustomerType type, CustomerStatus status, Industry industry) {
    Customer customer = new Customer();
    customer.setCustomerNumber(number);
    customer.setCompanyName(name);
    customer.setCustomerType(type);
    customer.setStatus(status);
    customer.setIndustry(industry);
    customer.setIsTestData(true);
    customer.setCreatedBy("testuser");
    customer.setCreatedAt(LocalDateTime.now());
    customerRepository.persist(customer);
    return customer;
  }
}

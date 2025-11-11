package de.freshplan.domain.customer.service.query;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerLifecycleStage;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.CustomerService;
import de.freshplan.domain.customer.service.dto.CustomerDashboardResponse;
import de.freshplan.domain.customer.service.dto.CustomerListResponse;
import de.freshplan.domain.customer.service.dto.CustomerResponse;
import de.freshplan.domain.customer.service.exception.CustomerNotFoundException;
import de.freshplan.test.builders.CustomerTestDataFactory;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for CustomerQueryService.
 *
 * <p>These tests prove that CustomerQueryService behaves IDENTICALLY to CustomerService for all
 * read operations. This is critical for the CQRS migration to ensure no breaking changes when
 * switching via feature flag.
 */
@QuarkusTest
@Tag("integration")
class CustomerQueryServiceIntegrationTest {

  @Inject CustomerQueryService queryService;

  @Inject CustomerService originalService;

  @Inject CustomerRepository customerRepository;

  @Inject jakarta.persistence.EntityManager em;

  private UUID testCustomerId;
  private String testCustomerNumber;

  @AfterEach
  @Transactional
  void cleanup() {
    // Delete test customers using pattern matching
    // Pattern: KD-TEST-* or any customer with isTestData=true
    em.createNativeQuery("DELETE FROM customer_timeline_events WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'KD-%' OR is_test_data = true)").executeUpdate();
    em.createNativeQuery("DELETE FROM customer_contacts WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'KD-%' OR is_test_data = true)").executeUpdate();
    em.createNativeQuery("DELETE FROM opportunities WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'KD-%' OR is_test_data = true)").executeUpdate();
    em.createNativeQuery("DELETE FROM customers WHERE customer_number LIKE 'KD-%' OR is_test_data = true").executeUpdate();
  }

  private void setupTestData() {
    // Create a test customer directly in DB for read tests using CustomerBuilder
    Customer testCustomer =
        CustomerTestDataFactory.builder()
            .withCompanyName("Test Company GmbH")
            .withStatus(CustomerStatus.AKTIV)
            .withIndustry(Industry.HOTEL)
            .withExpectedAnnualVolume(new BigDecimal("100000.00"))
            .build();

    // Override specific fields to maintain test requirements
    // Use unique customer number to avoid constraint violations
    testCustomer.setCustomerNumber("KD-TEST-" + UUID.randomUUID().toString().substring(0, 8));
    testCustomer.setCompanyName("Test Company GmbH");
    testCustomer.setLegalForm("GMBH");
    testCustomer.setLifecycleStage(CustomerLifecycleStage.GROWTH);
    testCustomer.setRiskScore(50);
    testCustomer.setActualAnnualVolume(new BigDecimal("80000.00"));
    testCustomer.setLastContactDate(LocalDateTime.now().minusDays(5));
    testCustomer.setCreatedBy("test-user");
    testCustomer.setUpdatedBy("test-user");
    testCustomer.setIsDeleted(false);

    customerRepository.persist(testCustomer);
    customerRepository.flush();

    testCustomerId = testCustomer.getId();
    testCustomerNumber = testCustomer.getCustomerNumber();
  }

  // ========== TEST: getCustomer() ==========

  @Test
  @TestTransaction
  void getCustomer_shouldReturnIdenticalResults() {
    setupTestData();
    // When: Call both services
    CustomerResponse fromOriginal = originalService.getCustomer(testCustomerId);
    CustomerResponse fromQuery = queryService.getCustomer(testCustomerId);

    // Then: Results must be identical
    assertThat(fromQuery).usingRecursiveComparison().isEqualTo(fromOriginal);

    // Verify that we got the right customer
    assertThat(fromQuery.companyName()).isEqualTo("Test Company GmbH");
    assertThat(fromQuery.status()).isEqualTo(CustomerStatus.AKTIV);
  }

  @Test
  @TestTransaction
  void getCustomer_withNullId_shouldThrowSameException() {
    // No test data needed for null ID test
    // When/Then: Both should throw IllegalArgumentException
    assertThatThrownBy(() -> originalService.getCustomer(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Customer ID cannot be null");

    assertThatThrownBy(() -> queryService.getCustomer(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Customer ID cannot be null");
  }

  @Test
  @TestTransaction
  void getCustomer_withNonExistentId_shouldThrowSameException() {
    // No test data needed for non-existent ID test
    UUID nonExistentId = UUID.randomUUID();

    // When/Then: Both should throw CustomerNotFoundException
    assertThatThrownBy(() -> originalService.getCustomer(nonExistentId))
        .isInstanceOf(CustomerNotFoundException.class);

    assertThatThrownBy(() -> queryService.getCustomer(nonExistentId))
        .isInstanceOf(CustomerNotFoundException.class);
  }

  // ========== TEST: getAllCustomers() ==========

  @Test
  @TestTransaction
  void getAllCustomers_shouldReturnIdenticalResults() {
    setupTestData();
    // Given: Add more test customers
    createAdditionalTestCustomers(5);

    // When: Call both services
    CustomerListResponse fromOriginal = originalService.getAllCustomers(0, 10);
    CustomerListResponse fromQuery = queryService.getAllCustomers(0, 10);

    // Then: Results must be identical
    assertThat(fromQuery.content()).hasSize(fromOriginal.content().size());
    assertThat(fromQuery.totalElements()).isEqualTo(fromOriginal.totalElements());
    assertThat(fromQuery.totalPages()).isEqualTo(fromOriginal.totalPages());
    assertThat(fromQuery.page()).isEqualTo(fromOriginal.page());

    // Compare actual customer data
    assertThat(fromQuery.content())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrderElementsOf(fromOriginal.content());
  }

  @Test
  @TestTransaction
  void getAllCustomers_withPagination_shouldReturnIdenticalResults() {
    setupTestData();
    // Given: Add more test customers for pagination
    createAdditionalTestCustomers(15);

    // When: Get page 1 (second page)
    CustomerListResponse fromOriginal = originalService.getAllCustomers(1, 5);
    CustomerListResponse fromQuery = queryService.getAllCustomers(1, 5);

    // Then: Pagination must work identically
    assertThat(fromQuery.content()).hasSize(5);
    assertThat(fromQuery.page()).isEqualTo(1);
    assertThat(fromQuery.content())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrderElementsOf(fromOriginal.content());
  }

  // ========== TEST: getCustomersByStatus() ==========

  @Test
  @TestTransaction
  void getCustomersByStatus_shouldReturnIdenticalResults() {
    setupTestData();
    // Given: Create customers with different statuses
    createCustomerWithStatus(CustomerStatus.AKTIV, "Active Company 1");
    createCustomerWithStatus(CustomerStatus.AKTIV, "Active Company 2");
    createCustomerWithStatus(CustomerStatus.INAKTIV, "Inactive Company");
    createCustomerWithStatus(CustomerStatus.ARCHIVIERT, "Archived Company");

    // When: Query for AKTIV status
    CustomerListResponse fromOriginal =
        originalService.getCustomersByStatus(CustomerStatus.AKTIV, 0, 20);
    CustomerListResponse fromQuery = queryService.getCustomersByStatus(CustomerStatus.AKTIV, 0, 20);

    // Then: Must return same customers - including seed data
    // Services should return identical results
    assertThat(fromQuery.totalElements()).isEqualTo(fromOriginal.totalElements());
    assertThat(fromQuery.content()).hasSize(fromOriginal.content().size());

    // Verify our test customers are included
    assertThat(fromQuery.content())
        .extracting(CustomerResponse::companyName)
        .contains("Test Company GmbH", "Active Company 1", "Active Company 2");

    // Compare that both services return the same results
    assertThat(fromQuery.content())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrderElementsOf(fromOriginal.content());
  }

  // ========== TEST: getCustomersByIndustry() ==========
  // DISABLED: getCustomersByIndustry() methods removed - use findByFilters() instead

  // @Test
  // @TestTransaction
  // void getCustomersByIndustry_shouldReturnIdenticalResults() {
  //   // Test disabled - getCustomersByIndustry() method removed from services
  //   // Use findByFilters() with businessType parameter instead
  // }

  // ========== TEST: getCustomerHierarchy() ==========

  @Test
  @TestTransaction
  void getCustomerHierarchy_shouldReturnIdenticalResults() {
    setupTestData();
    // When: Get hierarchy for test customer
    CustomerResponse fromOriginal = originalService.getCustomerHierarchy(testCustomerId);
    CustomerResponse fromQuery = queryService.getCustomerHierarchy(testCustomerId);

    // Then: Results must be identical
    assertThat(fromQuery).usingRecursiveComparison().isEqualTo(fromOriginal);
  }

  @Test
  @TestTransaction
  void getCustomerHierarchy_withNonExistentId_shouldThrowSameException() {
    // No test data needed for non-existent ID test
    UUID nonExistentId = UUID.randomUUID();

    // When/Then: Both should throw CustomerNotFoundException
    assertThatThrownBy(() -> originalService.getCustomerHierarchy(nonExistentId))
        .isInstanceOf(CustomerNotFoundException.class);

    assertThatThrownBy(() -> queryService.getCustomerHierarchy(nonExistentId))
        .isInstanceOf(CustomerNotFoundException.class);
  }

  // ========== TEST: getCustomersAtRisk() ==========

  @Test
  @TestTransaction
  void getCustomersAtRisk_shouldReturnIdenticalResults() {
    setupTestData();
    // Given: Create customers with different risk scores
    createCustomerWithRiskScore(80, "High Risk Company 1");
    createCustomerWithRiskScore(75, "High Risk Company 2");
    createCustomerWithRiskScore(30, "Low Risk Company");

    // When: Query for risk score >= 70
    CustomerListResponse fromOriginal = originalService.getCustomersAtRisk(70, 0, 20);
    CustomerListResponse fromQuery = queryService.getCustomersAtRisk(70, 0, 20);

    // Then: Must return same high-risk customers - including any seed data with high risk
    // Services should return identical results
    assertThat(fromQuery.totalElements()).isEqualTo(fromOriginal.totalElements());
    assertThat(fromQuery.content()).hasSize(fromOriginal.content().size());

    // Verify our test high-risk customers are included
    assertThat(fromQuery.content())
        .extracting(CustomerResponse::companyName)
        .contains("High Risk Company 1", "High Risk Company 2");

    // Verify low risk customer is NOT included
    assertThat(fromQuery.content())
        .extracting(CustomerResponse::companyName)
        .doesNotContain("Low Risk Company");

    // Compare that both services return the same results
    assertThat(fromQuery.content())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrderElementsOf(fromOriginal.content());
  }

  // ========== TEST: getOverdueFollowUps() ==========

  @Test
  @TestTransaction
  void getOverdueFollowUps_shouldReturnIdenticalResults() {
    setupTestData();
    // Given: Create customers with overdue follow-ups
    createCustomerWithLastContact(LocalDateTime.now().minusDays(35), "Overdue Company 1");
    createCustomerWithLastContact(LocalDateTime.now().minusDays(40), "Overdue Company 2");
    createCustomerWithLastContact(LocalDateTime.now().minusDays(5), "Recent Contact Company");

    // When: Query for overdue follow-ups
    CustomerListResponse fromOriginal = originalService.getOverdueFollowUps(0, 10);
    CustomerListResponse fromQuery = queryService.getOverdueFollowUps(0, 10);

    // Then: Results must be identical
    assertThat(fromQuery.totalElements()).isEqualTo(fromOriginal.totalElements());
    assertThat(fromQuery.content())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrderElementsOf(fromOriginal.content());
  }

  // ========== TEST: checkDuplicates() ==========

  @Test
  @TestTransaction
  void checkDuplicates_shouldReturnIdenticalResults() {
    setupTestData();
    // Given: Create similar company names
    createCustomerWithName("ACME Corporation");
    createCustomerWithName("ACME Corp");
    createCustomerWithName("Acme Corp.");
    createCustomerWithName("Different Company");

    // When: Check for duplicates
    List<CustomerResponse> fromOriginal = originalService.checkDuplicates("ACME");
    List<CustomerResponse> fromQuery = queryService.checkDuplicates("ACME");

    // Then: Must find same duplicates
    assertThat(fromQuery).hasSize(fromOriginal.size());
    assertThat(fromQuery)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrderElementsOf(fromOriginal);
  }

  // ========== TEST: getDashboardData() ==========

  @Test
  @TestTransaction
  void getDashboardData_shouldReturnIdenticalResults() {
    setupTestData();
    // Given: Create diverse customer data for dashboard
    createCustomerWithStatus(CustomerStatus.AKTIV, "Active 1");
    createCustomerWithStatus(CustomerStatus.AKTIV, "Active 2");
    createCustomerWithStatus(CustomerStatus.INAKTIV, "Inactive 1");
    createCustomerWithLifecycleStage(CustomerLifecycleStage.ACQUISITION, "Acquisition 1");
    createCustomerWithLifecycleStage(CustomerLifecycleStage.ONBOARDING, "Onboarding 1");
    createCustomerWithRiskScore(80, "At Risk 1");

    // When: Get dashboard data
    CustomerDashboardResponse fromOriginal = originalService.getDashboardData();
    CustomerDashboardResponse fromQuery = queryService.getDashboardData();

    // Then: All metrics must be identical
    assertThat(fromQuery.totalCustomers()).isEqualTo(fromOriginal.totalCustomers());
    assertThat(fromQuery.activeCustomers()).isEqualTo(fromOriginal.activeCustomers());
    assertThat(fromQuery.newThisMonth()).isEqualTo(fromOriginal.newThisMonth());
    assertThat(fromQuery.atRiskCount()).isEqualTo(fromOriginal.atRiskCount());
    assertThat(fromQuery.upcomingFollowUps()).isEqualTo(fromOriginal.upcomingFollowUps());

    // Check status distribution
    assertThat(fromQuery.customersByStatus())
        .containsExactlyInAnyOrderEntriesOf(fromOriginal.customersByStatus());

    // Check lifecycle distribution
    assertThat(fromQuery.customersByLifecycle())
        .containsExactlyInAnyOrderEntriesOf(fromOriginal.customersByLifecycle());
  }

  // ========== HELPER METHODS ==========

  @TestTransaction
  void createAdditionalTestCustomers(int count) {
    for (int i = 1; i <= count; i++) {
      Customer customer =
          CustomerTestDataFactory.builder()
              .withCompanyName("Test Company " + i)
              .withStatus(CustomerStatus.AKTIV)
              .withIndustry(Industry.HOTEL)
              .build();

      // Override specific fields to maintain test requirements
      customer.setCustomerNumber(de.freshplan.testsupport.UniqueData.customerNumber("KD", i + 1));
      customer.setCompanyName("Test Company " + i);
      customer.setLegalForm("GMBH");
      customer.setLifecycleStage(CustomerLifecycleStage.GROWTH);
      customer.setRiskScore(40 + i);
      customer.setCreatedBy("test-user");
      customer.setUpdatedBy("test-user");
      customer.setIsDeleted(false);
      customer.setIsTestData(true);
      customerRepository.persist(customer);
    }
    customerRepository.flush();
  }

  @TestTransaction
  void createCustomerWithStatus(CustomerStatus status, String name) {
    Customer customer =
        CustomerTestDataFactory.builder()
            .withCompanyName(name)
            .withStatus(status)
            .withIndustry(Industry.HOTEL)
            .build();

    customer.setCustomerNumber(
        de.freshplan.testsupport.UniqueData.customerNumber("KD", (int) (Math.random() * 10000)));
    customer.setCompanyName(name);
    customer.setLegalForm("GMBH");
    customer.setLifecycleStage(CustomerLifecycleStage.GROWTH);
    customer.setRiskScore(50);
    customer.setCreatedBy("test-user");
    customer.setUpdatedBy("test-user");
    customer.setIsDeleted(false);
    customer.setIsTestData(true);
    customerRepository.persist(customer);
    customerRepository.flush();
  }

  @TestTransaction
  void createCustomerWithIndustry(Industry industry, String name) {
    Customer customer =
        CustomerTestDataFactory.builder()
            .withCompanyName(name)
            .withStatus(CustomerStatus.AKTIV)
            .withIndustry(industry)
            .build();

    customer.setCustomerNumber(
        de.freshplan.testsupport.UniqueData.customerNumber("KD", (int) (Math.random() * 10000)));
    customer.setCompanyName(name);
    customer.setLegalForm("GMBH");
    customer.setLifecycleStage(CustomerLifecycleStage.GROWTH);
    customer.setRiskScore(50);
    customer.setCreatedBy("test-user");
    customer.setUpdatedBy("test-user");
    customer.setIsDeleted(false);
    customer.setIsTestData(true);
    customerRepository.persist(customer);
    customerRepository.flush();
  }

  @TestTransaction
  void createCustomerWithRiskScore(int riskScore, String name) {
    Customer customer =
        CustomerTestDataFactory.builder()
            .withCompanyName(name)
            .withStatus(CustomerStatus.AKTIV)
            .withIndustry(Industry.HOTEL)
            .build();

    customer.setCustomerNumber(
        de.freshplan.testsupport.UniqueData.customerNumber("KD", (int) (Math.random() * 10000)));
    customer.setCompanyName(name);
    customer.setLegalForm("GMBH");
    customer.setLifecycleStage(CustomerLifecycleStage.GROWTH);
    customer.setRiskScore(riskScore);
    customer.setCreatedBy("test-user");
    customer.setUpdatedBy("test-user");
    customer.setIsDeleted(false);
    customer.setIsTestData(true);
    customerRepository.persist(customer);
    customerRepository.flush();
  }

  @TestTransaction
  void createCustomerWithLastContact(LocalDateTime lastContact, String name) {
    Customer customer =
        CustomerTestDataFactory.builder()
            .withCompanyName(name)
            .withStatus(CustomerStatus.AKTIV)
            .withIndustry(Industry.HOTEL)
            .build();

    customer.setCustomerNumber(
        de.freshplan.testsupport.UniqueData.customerNumber("KD", (int) (Math.random() * 10000)));
    customer.setCompanyName(name);
    customer.setLegalForm("GMBH");
    customer.setLifecycleStage(CustomerLifecycleStage.GROWTH);
    customer.setRiskScore(50);
    customer.setLastContactDate(lastContact);
    customer.setNextFollowUpDate(lastContact.plusDays(30));
    customer.setCreatedBy("test-user");
    customer.setUpdatedBy("test-user");
    customer.setIsDeleted(false);
    customer.setIsTestData(true);
    customerRepository.persist(customer);
    customerRepository.flush();
  }

  @TestTransaction
  void createCustomerWithName(String name) {
    Customer customer =
        CustomerTestDataFactory.builder()
            .withCompanyName(name)
            .withStatus(CustomerStatus.AKTIV)
            .withIndustry(Industry.HOTEL)
            .build();

    customer.setCustomerNumber(
        de.freshplan.testsupport.UniqueData.customerNumber("KD", (int) (Math.random() * 10000)));
    customer.setCompanyName(name);
    customer.setLegalForm("GMBH");
    customer.setLifecycleStage(CustomerLifecycleStage.GROWTH);
    customer.setRiskScore(50);
    customer.setCreatedBy("test-user");
    customer.setUpdatedBy("test-user");
    customer.setIsDeleted(false);
    customer.setIsTestData(true);
    customerRepository.persist(customer);
    customerRepository.flush();
  }

  @TestTransaction
  void createCustomerWithLifecycleStage(CustomerLifecycleStage stage, String name) {
    Customer customer =
        CustomerTestDataFactory.builder()
            .withCompanyName(name)
            .withStatus(CustomerStatus.AKTIV)
            .withIndustry(Industry.HOTEL)
            .build();

    customer.setCustomerNumber(
        de.freshplan.testsupport.UniqueData.customerNumber("KD", (int) (Math.random() * 10000)));
    customer.setCompanyName(name);
    customer.setLegalForm("GMBH");
    customer.setLifecycleStage(stage);
    customer.setRiskScore(50);
    customer.setCreatedBy("test-user");
    customer.setUpdatedBy("test-user");
    customer.setIsDeleted(false);
    customer.setIsTestData(true);
    customerRepository.persist(customer);
    customerRepository.flush();
  }
}

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
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for CustomerQueryService.
 *
 * <p>These tests prove that CustomerQueryService behaves IDENTICALLY to CustomerService for all
 * read operations. This is critical for the CQRS migration to ensure no breaking changes when
 * switching via feature flag.
 */
@QuarkusTest
class CustomerQueryServiceIntegrationTest {

  @Inject CustomerQueryService queryService;

  @Inject CustomerService originalService;

  @Inject CustomerRepository customerRepository;

  private UUID testCustomerId;
  private String testCustomerNumber;

  @BeforeEach
  @TestTransaction
  void setUp() {
    // Clean up any test data - skip foreign key constraints by using query
    customerRepository
        .getEntityManager()
        .createQuery("DELETE FROM CustomerTimelineEvent")
        .executeUpdate();
    customerRepository.getEntityManager().createQuery("DELETE FROM Customer").executeUpdate();
    customerRepository.flush();

    // Create a test customer directly in DB for read tests
    Customer testCustomer = new Customer();
    testCustomer.setCustomerNumber("KD-2025-00001");
    testCustomer.setCompanyName("Test Company GmbH");
    testCustomer.setLegalForm("GmbH");
    testCustomer.setStatus(CustomerStatus.AKTIV);
    testCustomer.setIndustry(Industry.HOTEL);
    testCustomer.setLifecycleStage(CustomerLifecycleStage.GROWTH);
    testCustomer.setRiskScore(50);
    testCustomer.setExpectedAnnualVolume(new BigDecimal("100000.00"));
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
  void getCustomer_shouldReturnIdenticalResults() {
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
  void getCustomer_withNullId_shouldThrowSameException() {
    // When/Then: Both should throw IllegalArgumentException
    assertThatThrownBy(() -> originalService.getCustomer(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Customer ID cannot be null");

    assertThatThrownBy(() -> queryService.getCustomer(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Customer ID cannot be null");
  }

  @Test
  void getCustomer_withNonExistentId_shouldThrowSameException() {
    UUID nonExistentId = UUID.randomUUID();

    // When/Then: Both should throw CustomerNotFoundException
    assertThatThrownBy(() -> originalService.getCustomer(nonExistentId))
        .isInstanceOf(CustomerNotFoundException.class);

    assertThatThrownBy(() -> queryService.getCustomer(nonExistentId))
        .isInstanceOf(CustomerNotFoundException.class);
  }

  // ========== TEST: getAllCustomers() ==========

  @Test
  void getAllCustomers_shouldReturnIdenticalResults() {
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
  void getAllCustomers_withPagination_shouldReturnIdenticalResults() {
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
  void getCustomersByStatus_shouldReturnIdenticalResults() {
    // Given: Create customers with different statuses
    createCustomerWithStatus(CustomerStatus.AKTIV, "Active Company 1");
    createCustomerWithStatus(CustomerStatus.AKTIV, "Active Company 2");
    createCustomerWithStatus(CustomerStatus.INAKTIV, "Inactive Company");
    createCustomerWithStatus(CustomerStatus.ARCHIVIERT, "Archived Company");

    // When: Query for AKTIV status
    CustomerListResponse fromOriginal =
        originalService.getCustomersByStatus(CustomerStatus.AKTIV, 0, 10);
    CustomerListResponse fromQuery = queryService.getCustomersByStatus(CustomerStatus.AKTIV, 0, 10);

    // Then: Must return same customers
    assertThat(fromQuery.content()).hasSize(3); // Original test customer + 2 new active
    assertThat(fromQuery.totalElements()).isEqualTo(fromOriginal.totalElements());
    assertThat(fromQuery.content())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrderElementsOf(fromOriginal.content());
  }

  // ========== TEST: getCustomersByIndustry() ==========

  @Test
  void getCustomersByIndustry_shouldReturnIdenticalResults() {
    // Given: Create customers with different industries
    createCustomerWithIndustry(Industry.HOTEL, "Hotel Company 1");
    createCustomerWithIndustry(Industry.HOTEL, "Hotel Company 2");
    createCustomerWithIndustry(Industry.RESTAURANT, "Restaurant Company");

    // When: Query for HOTEL industry
    CustomerListResponse fromOriginal =
        originalService.getCustomersByIndustry(Industry.HOTEL, 0, 10);
    CustomerListResponse fromQuery = queryService.getCustomersByIndustry(Industry.HOTEL, 0, 10);

    // Then: Must return same customers
    assertThat(fromQuery.content()).hasSize(3); // Original + 2 new hotel companies
    assertThat(fromQuery.content())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrderElementsOf(fromOriginal.content());
  }

  // ========== TEST: getCustomerHierarchy() ==========

  @Test
  void getCustomerHierarchy_shouldReturnIdenticalResults() {
    // When: Get hierarchy for test customer
    CustomerResponse fromOriginal = originalService.getCustomerHierarchy(testCustomerId);
    CustomerResponse fromQuery = queryService.getCustomerHierarchy(testCustomerId);

    // Then: Results must be identical
    assertThat(fromQuery).usingRecursiveComparison().isEqualTo(fromOriginal);
  }

  @Test
  void getCustomerHierarchy_withNonExistentId_shouldThrowSameException() {
    UUID nonExistentId = UUID.randomUUID();

    // When/Then: Both should throw CustomerNotFoundException
    assertThatThrownBy(() -> originalService.getCustomerHierarchy(nonExistentId))
        .isInstanceOf(CustomerNotFoundException.class);

    assertThatThrownBy(() -> queryService.getCustomerHierarchy(nonExistentId))
        .isInstanceOf(CustomerNotFoundException.class);
  }

  // ========== TEST: getCustomersAtRisk() ==========

  @Test
  void getCustomersAtRisk_shouldReturnIdenticalResults() {
    // Given: Create customers with different risk scores
    createCustomerWithRiskScore(80, "High Risk Company 1");
    createCustomerWithRiskScore(75, "High Risk Company 2");
    createCustomerWithRiskScore(30, "Low Risk Company");

    // When: Query for risk score >= 70
    CustomerListResponse fromOriginal = originalService.getCustomersAtRisk(70, 0, 10);
    CustomerListResponse fromQuery = queryService.getCustomersAtRisk(70, 0, 10);

    // Then: Must return same high-risk customers
    assertThat(fromQuery.content()).hasSize(2);
    assertThat(fromQuery.totalElements()).isEqualTo(fromOriginal.totalElements());
    assertThat(fromQuery.content())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrderElementsOf(fromOriginal.content());
  }

  // ========== TEST: getOverdueFollowUps() ==========

  @Test
  void getOverdueFollowUps_shouldReturnIdenticalResults() {
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
  void checkDuplicates_shouldReturnIdenticalResults() {
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
  void getDashboardData_shouldReturnIdenticalResults() {
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
      Customer customer = new Customer();
      customer.setCustomerNumber(de.freshplan.testsupport.UniqueData.customerNumber("KD", i + 1));
      customer.setCompanyName("Test Company " + i);
      customer.setLegalForm("GmbH");
      customer.setStatus(CustomerStatus.AKTIV);
      customer.setIndustry(Industry.HOTEL);
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
    Customer customer = new Customer();
    customer.setCustomerNumber(de.freshplan.testsupport.UniqueData.customerNumber("KD", 
        (int)(Math.random() * 10000)));
    customer.setCompanyName(name);
    customer.setLegalForm("GmbH");
    customer.setStatus(status);
    customer.setIndustry(Industry.HOTEL);
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
    Customer customer = new Customer();
    customer.setCustomerNumber(de.freshplan.testsupport.UniqueData.customerNumber("KD", (int)(Math.random() * 10000)));
    customer.setCompanyName(name);
    customer.setLegalForm("GmbH");
    customer.setStatus(CustomerStatus.AKTIV);
    customer.setIndustry(industry);
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
    Customer customer = new Customer();
    customer.setCustomerNumber(de.freshplan.testsupport.UniqueData.customerNumber("KD", (int)(Math.random() * 10000)));
    customer.setCompanyName(name);
    customer.setLegalForm("GmbH");
    customer.setStatus(CustomerStatus.AKTIV);
    customer.setIndustry(Industry.HOTEL);
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
    Customer customer = new Customer();
    customer.setCustomerNumber(de.freshplan.testsupport.UniqueData.customerNumber("KD", (int)(Math.random() * 10000)));
    customer.setCompanyName(name);
    customer.setLegalForm("GmbH");
    customer.setStatus(CustomerStatus.AKTIV);
    customer.setIndustry(Industry.HOTEL);
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
    Customer customer = new Customer();
    customer.setCustomerNumber(de.freshplan.testsupport.UniqueData.customerNumber("KD", (int)(Math.random() * 10000)));
    customer.setCompanyName(name);
    customer.setLegalForm("GmbH");
    customer.setStatus(CustomerStatus.AKTIV);
    customer.setIndustry(Industry.HOTEL);
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
    Customer customer = new Customer();
    customer.setCustomerNumber(de.freshplan.testsupport.UniqueData.customerNumber("KD", (int)(Math.random() * 10000)));
    customer.setCompanyName(name);
    customer.setLegalForm("GmbH");
    customer.setStatus(CustomerStatus.AKTIV);
    customer.setIndustry(Industry.HOTEL);
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

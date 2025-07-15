package de.freshplan.domain.customer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.customer.entity.*;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive test suite for CustomerRepository. Tests all repository methods including soft
 * delete support, search functionality, hierarchy management, and specialized queries.
 */
@QuarkusTest
class CustomerRepositoryTest {

  @Inject CustomerRepository repository;

  @Inject EntityManager em;

  // Counter for unique customer numbers
  private static final AtomicInteger customerCounter = new AtomicInteger(1);

  // Test data will be created in each test method for proper isolation

  @AfterEach
  @Transactional
  void cleanupTestData() {
    // Delete only test customers (those with KD-TEST- prefix)
    em.createQuery("DELETE FROM Customer c WHERE c.customerNumber LIKE 'KD-TEST-%'")
        .executeUpdate();
    em.flush();
  }

  /**
   * Creates standard test data set for tests that need multiple customers. Returns a TestDataSet
   * with active, deleted, parent and child customers.
   */
  private TestDataSet createStandardTestData() {
    Customer testCustomer = createTestCustomer("Test Company");
    testCustomer.setStatus(CustomerStatus.AKTIV);
    testCustomer.setLifecycleStage(CustomerLifecycleStage.GROWTH);
    testCustomer.setIndustry(Industry.GESUNDHEITSWESEN);
    testCustomer.setExpectedAnnualVolume(new BigDecimal("50000.00"));
    repository.persist(testCustomer);

    Customer deletedCustomer = createTestCustomer("Deleted Company");
    deletedCustomer.setStatus(CustomerStatus.INAKTIV);
    deletedCustomer.setIsDeleted(true);
    repository.persist(deletedCustomer);

    Customer parentCustomer = createTestCustomer("Parent Company");
    parentCustomer.setStatus(CustomerStatus.AKTIV);
    parentCustomer.setExpectedAnnualVolume(new BigDecimal("30000.00"));
    repository.persist(parentCustomer);

    Customer childCustomer = createTestCustomer("Child Company");
    childCustomer.setStatus(CustomerStatus.AKTIV);
    childCustomer.setParentCustomer(parentCustomer);
    childCustomer.setExpectedAnnualVolume(new BigDecimal("20000.00"));
    repository.persist(childCustomer);

    em.flush();

    return new TestDataSet(testCustomer, deletedCustomer, parentCustomer, childCustomer);
  }

  /** Creates a single customer with specific status for simple tests. */
  private Customer createSingleCustomer(String name, CustomerStatus status) {
    Customer customer = createTestCustomer(name);
    customer.setStatus(status);
    repository.persist(customer);
    em.flush();
    return customer;
  }

  /** Creates a customer with specific industry for industry-based tests. */
  private Customer createCustomerWithIndustry(String name, Industry industry) {
    Customer customer = createTestCustomer(name);
    customer.setStatus(CustomerStatus.AKTIV);
    customer.setIndustry(industry);
    repository.persist(customer);
    em.flush();
    return customer;
  }

  /** Creates customers with specific expected volumes for volume-based tests. */
  private Customer createCustomerWithVolume(String name, BigDecimal expectedVolume) {
    Customer customer = createTestCustomer(name);
    customer.setStatus(CustomerStatus.AKTIV);
    customer.setExpectedAnnualVolume(expectedVolume);
    repository.persist(customer);
    em.flush();
    return customer;
  }

  /** Helper class to hold test data */
  private static class TestDataSet {
    final Customer testCustomer;
    final Customer deletedCustomer;
    final Customer parentCustomer;
    final Customer childCustomer;

    TestDataSet(
        Customer testCustomer,
        Customer deletedCustomer,
        Customer parentCustomer,
        Customer childCustomer) {
      this.testCustomer = testCustomer;
      this.deletedCustomer = deletedCustomer;
      this.parentCustomer = parentCustomer;
      this.childCustomer = childCustomer;
    }
  }

  // ========== SOFT DELETE TESTS ==========

  @Test
  @TestTransaction
  void findByIdActive_shouldReturnActiveCustomer() {
    // Create test customer
    Customer testCustomer = createTestCustomer("Test Company");
    testCustomer.setStatus(CustomerStatus.AKTIV);
    repository.persist(testCustomer);
    em.flush();

    var result = repository.findByIdActive(testCustomer.getId());

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(testCustomer.getId());
    assertThat(result.get().getIsDeleted()).isFalse();
  }

  @Test
  @TestTransaction
  void findByIdActive_shouldNotReturnDeletedCustomer() {
    TestDataSet data = createStandardTestData();

    var result = repository.findByIdActive(data.deletedCustomer.getId());

    assertThat(result).isEmpty();
  }

  @Test
  @TestTransaction
  void findAllActive_shouldOnlyReturnActiveCustomers() {
    TestDataSet data = createStandardTestData();

    var result = repository.findAllActive(null);

    assertThat(result).hasSize(3); // testCustomer, parentCustomer, childCustomer
    assertThat(result).noneMatch(Customer::getIsDeleted);
    assertThat(result)
        .extracting(Customer::getCompanyName)
        .containsExactlyInAnyOrder("Test Company", "Parent Company", "Child Company");
  }

  @Test
  @TestTransaction
  void countActive_shouldOnlyCountActiveCustomers() {
    TestDataSet data = createStandardTestData();

    long count = repository.countActive();

    assertThat(count).isEqualTo(3); // testCustomer, parentCustomer, childCustomer
  }

  @Test
  @TestTransaction
  void findDeleted_shouldOnlyReturnDeletedCustomers() {
    TestDataSet data = createStandardTestData();

    var result = repository.findDeleted(null);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(data.deletedCustomer.getId());
    assertThat(result.get(0).getIsDeleted()).isTrue();
  }

  // ========== RISK CUSTOMER QUERIES ==========

  @Test
  @TestTransaction
  void findActiveCustomersWithoutRecentContact_shouldReturnCustomersWithOldContact() {
    // Create customer with no contact
    Customer noContactCustomer = createTestCustomer("No Contact Company");
    noContactCustomer.setStatus(CustomerStatus.AKTIV);
    noContactCustomer.setLastContactDate(null);
    repository.persist(noContactCustomer);

    // Create customer with old contact
    Customer oldContactCustomer = createTestCustomer("Old Contact Company");
    oldContactCustomer.setStatus(CustomerStatus.AKTIV);
    oldContactCustomer.setLastContactDate(LocalDateTime.now().minusDays(60));
    repository.persist(oldContactCustomer);

    em.flush();

    LocalDateTime thresholdDate = LocalDateTime.now().minusDays(30);
    var result = repository.findActiveCustomersWithoutRecentContact(thresholdDate);

    assertThat(result).hasSize(2);
    assertThat(result)
        .extracting(Customer::getCompanyName)
        .containsExactlyInAnyOrder("No Contact Company", "Old Contact Company");
  }

  @Test
  void countActiveCustomersWithoutRecentContact_shouldCountCorrectly() {
    LocalDateTime thresholdDate = LocalDateTime.now().minusDays(30);
    long count = repository.countActiveCustomersWithoutRecentContact(thresholdDate);

    assertThat(count).isEqualTo(0); // All test customers have recent contact
  }

  // ========== UNIQUE CONSTRAINTS ==========

  @Test
  @TestTransaction
  void findByCustomerNumber_shouldReturnCorrectCustomer() {
    TestDataSet data = createStandardTestData();

    var result = repository.findByCustomerNumber(data.testCustomer.getCustomerNumber());

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(data.testCustomer.getId());
  }

  @Test
  void findByCustomerNumber_shouldReturnEmptyForNull() {
    var result = repository.findByCustomerNumber(null);

    assertThat(result).isEmpty();
  }

  @Test
  void findByCustomerNumber_shouldReturnEmptyForBlank() {
    var result = repository.findByCustomerNumber("   ");

    assertThat(result).isEmpty();
  }

  @Test
  @TestTransaction
  void findByCustomerNumber_shouldNotReturnDeletedCustomer() {
    TestDataSet data = createStandardTestData();

    var result = repository.findByCustomerNumber(data.deletedCustomer.getCustomerNumber());

    assertThat(result).isEmpty();
  }

  @Test
  @TestTransaction
  void existsByCustomerNumber_shouldReturnTrueForExisting() {
    TestDataSet data = createStandardTestData();

    boolean exists = repository.existsByCustomerNumber(data.testCustomer.getCustomerNumber());

    assertThat(exists).isTrue();
  }

  @Test
  void existsByCustomerNumber_shouldReturnFalseForNonExisting() {
    boolean exists = repository.existsByCustomerNumber("KD-2025-99999");

    assertThat(exists).isFalse();
  }

  @Test
  void existsByCustomerNumber_shouldReturnFalseForNull() {
    boolean exists = repository.existsByCustomerNumber(null);

    assertThat(exists).isFalse();
  }

  // ========== SEARCH & FILTERING ==========

  @Test
  @TestTransaction
  void findByStatus_shouldReturnCustomersWithStatus() {
    TestDataSet data = createStandardTestData();

    var result = repository.findByStatus(CustomerStatus.AKTIV, null);

    assertThat(result).hasSize(3); // testCustomer, parentCustomer, childCustomer
    assertThat(result).extracting(Customer::getId).contains(data.testCustomer.getId());
  }

  @Test
  @TestTransaction
  void findByStatusIn_shouldReturnCustomersWithAnyStatus() {
    // Create one AKTIV and one INAKTIV customer
    Customer activeCustomer = createTestCustomer("Active Company");
    activeCustomer.setStatus(CustomerStatus.AKTIV);
    repository.persist(activeCustomer);

    Customer inactiveCustomer = createTestCustomer("Inactive Company");
    inactiveCustomer.setStatus(CustomerStatus.INAKTIV);
    repository.persist(inactiveCustomer);
    em.flush();

    var statuses = List.of(CustomerStatus.AKTIV, CustomerStatus.INAKTIV);
    var result = repository.findByStatusIn(statuses, null);

    assertThat(result).hasSize(2);
    assertThat(result)
        .extracting(Customer::getStatus)
        .containsExactlyInAnyOrder(CustomerStatus.AKTIV, CustomerStatus.INAKTIV);
  }

  @Test
  void findByStatusIn_shouldReturnEmptyForNullList() {
    var result = repository.findByStatusIn(null, null);

    assertThat(result).isEmpty();
  }

  @Test
  void findByStatusIn_shouldReturnEmptyForEmptyList() {
    var result = repository.findByStatusIn(List.of(), null);

    assertThat(result).isEmpty();
  }

  @Test
  @TestTransaction
  void findByLifecycleStage_shouldReturnCorrectCustomers() {
    TestDataSet data = createStandardTestData();

    var result = repository.findByLifecycleStage(CustomerLifecycleStage.GROWTH, null);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(data.testCustomer.getId());
  }

  @Test
  @TestTransaction
  void findByIndustry_shouldReturnCorrectCustomers() {
    TestDataSet data = createStandardTestData();

    var result = repository.findByIndustry(Industry.GESUNDHEITSWESEN, null);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(data.testCustomer.getId());
  }

  // ========== HIERARCHY SUPPORT ==========

  @Test
  @TestTransaction
  void findChildren_shouldReturnAllChildren() {
    TestDataSet data = createStandardTestData();

    var children = repository.findChildren(data.parentCustomer.getId());

    assertThat(children).hasSize(1);
    assertThat(children.get(0).getId()).isEqualTo(data.childCustomer.getId());
  }

  @Test
  @TestTransaction
  void findRootCustomers_shouldReturnCustomersWithoutParent() {
    TestDataSet data = createStandardTestData();

    var roots = repository.findRootCustomers(null);

    assertThat(roots).hasSize(2); // testCustomer and parentCustomer
    assertThat(roots).noneMatch(c -> c.getParentCustomer() != null);
  }

  @Test
  @TestTransaction
  void hasChildren_shouldReturnTrueForParentWithChildren() {
    TestDataSet data = createStandardTestData();

    boolean hasChildren = repository.hasChildren(data.parentCustomer.getId());

    assertThat(hasChildren).isTrue();
  }

  @Test
  @TestTransaction
  void hasChildren_shouldReturnFalseForCustomerWithoutChildren() {
    TestDataSet data = createStandardTestData();

    boolean hasChildren = repository.hasChildren(data.testCustomer.getId());

    assertThat(hasChildren).isFalse();
  }

  // ========== RISK MANAGEMENT ==========

  @Test
  @TestTransaction
  void findAtRisk_shouldReturnHighRiskCustomers() {
    // Create high risk customer
    Customer highRiskCustomer = createTestCustomer("High Risk Company");
    highRiskCustomer.setRiskScore(80);
    repository.persist(highRiskCustomer);
    em.flush();

    var result = repository.findAtRisk(70, null);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getRiskScore()).isGreaterThanOrEqualTo(70);
  }

  @Test
  @TestTransaction
  void findOverdueFollowUps_shouldReturnOverdueCustomers() {
    // Create customer with overdue follow-up
    Customer overdueCustomer = createTestCustomer("Overdue Company");
    overdueCustomer.setNextFollowUpDate(LocalDateTime.now().minusDays(5));
    repository.persist(overdueCustomer);
    em.flush();

    var result = repository.findOverdueFollowUps(null);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(overdueCustomer.getId());
  }

  @Test
  @TestTransaction
  void findNotContactedSince_shouldReturnCustomersNotContacted() {
    // Create customer not contacted for long time
    Customer notContactedCustomer = createTestCustomer("Not Contacted Company");
    notContactedCustomer.setLastContactDate(LocalDateTime.now().minusDays(100));
    repository.persist(notContactedCustomer);
    em.flush();

    var result = repository.findNotContactedSince(90, null);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(notContactedCustomer.getId());
  }

  // ========== FINANCIAL QUERIES ==========

  @Test
  @TestTransaction
  void findByExpectedVolumeRange_shouldReturnCustomersInRange() {
    TestDataSet data = createStandardTestData();

    // Create customers with different volumes
    Customer lowVolumeCustomer = createTestCustomer("Low Volume Company");
    lowVolumeCustomer.setExpectedAnnualVolume(new BigDecimal("10000.00"));
    repository.persist(lowVolumeCustomer);

    Customer highVolumeCustomer = createTestCustomer("High Volume Company");
    highVolumeCustomer.setExpectedAnnualVolume(new BigDecimal("100000.00"));
    repository.persist(highVolumeCustomer);
    em.flush();

    var result =
        repository.findByExpectedVolumeRange(
            new BigDecimal("30000.00"), new BigDecimal("70000.00"), null);

    assertThat(result).hasSize(2); // testCustomer (50k) and parentCustomer (30k) both in range
    // Both testCustomer and parentCustomer should be in the range
    assertThat(result)
        .extracting(Customer::getId)
        .containsExactlyInAnyOrder(data.testCustomer.getId(), data.parentCustomer.getId());
    // Verify all returned customers are in the expected range
    assertThat(result)
        .allSatisfy(
            customer ->
                assertThat(customer.getExpectedAnnualVolume())
                    .isBetween(new BigDecimal("30000.00"), new BigDecimal("70000.00")));
  }

  @Test
  @TestTransaction
  void findByExpectedVolumeRange_withOnlyMinVolume_shouldReturnCustomersAboveMin() {
    TestDataSet data = createStandardTestData();

    var result = repository.findByExpectedVolumeRange(new BigDecimal("40000.00"), null, null);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getExpectedAnnualVolume())
        .isGreaterThanOrEqualTo(new BigDecimal("40000.00"));
  }

  @Test
  @TestTransaction
  void findByExpectedVolumeRange_withOnlyMaxVolume_shouldReturnCustomersBelowMax() {
    TestDataSet data = createStandardTestData();

    var result = repository.findByExpectedVolumeRange(null, new BigDecimal("60000.00"), null);

    assertThat(result).hasSize(3); // test, parent, child customers
  }

  @Test
  @TestTransaction
  void findByExpectedVolumeRange_withNoBounds_shouldReturnAllActive() {
    TestDataSet data = createStandardTestData();

    var result = repository.findByExpectedVolumeRange(null, null, null);

    assertThat(result).hasSize(3); // All active customers
  }

  // ========== DUPLICATE DETECTION ==========

  @Test
  @TestTransaction
  void findPotentialDuplicates_shouldFindSimilarNames() {
    // Create similar named companies
    Customer similarCustomer1 = createTestCustomer("Test Company GmbH");
    repository.persist(similarCustomer1);

    Customer similarCustomer2 = createTestCustomer("Test Company AG");
    repository.persist(similarCustomer2);
    em.flush();

    var duplicates = repository.findPotentialDuplicates("Test Company");

    assertThat(duplicates).hasSize(2); // GmbH and AG variants
    assertThat(duplicates)
        .extracting(Customer::getCompanyName)
        .containsExactlyInAnyOrder("Test Company GmbH", "Test Company AG");
  }

  @Test
  void findPotentialDuplicates_shouldReturnEmptyForNull() {
    var duplicates = repository.findPotentialDuplicates(null);

    assertThat(duplicates).isEmpty();
  }

  @Test
  void findPotentialDuplicates_shouldReturnEmptyForBlank() {
    var duplicates = repository.findPotentialDuplicates("   ");

    assertThat(duplicates).isEmpty();
  }

  // ========== DASHBOARD QUERIES ==========

  @Test
  @TestTransaction
  void countByStatus_shouldCountCorrectly() {
    TestDataSet data = createStandardTestData();

    long count = repository.countByStatus(CustomerStatus.AKTIV);

    assertThat(count).isEqualTo(3); // testCustomer, parentCustomer, childCustomer
  }

  @Test
  @TestTransaction
  void countByLifecycleStage_shouldCountCorrectly() {
    TestDataSet data = createStandardTestData();

    long count = repository.countByLifecycleStage(CustomerLifecycleStage.GROWTH);

    assertThat(count).isEqualTo(1); // nur testCustomer hat GROWTH stage
  }

  @Test
  @TestTransaction
  void countNewThisMonth_shouldCountRecentlyCreated() {
    // Create customer created today
    Customer newCustomer = createTestCustomer("New This Month Company");
    newCustomer.setCreatedAt(LocalDateTime.now());
    repository.persist(newCustomer);
    em.flush();

    long count = repository.countNewThisMonth();

    assertThat(count).isGreaterThanOrEqualTo(1); // At least the new customer
  }

  @Test
  @TestTransaction
  void countAtRisk_shouldCountHighRiskCustomers() {
    // Create high risk customer
    Customer highRiskCustomer = createTestCustomer("High Risk Company");
    highRiskCustomer.setRiskScore(85);
    repository.persist(highRiskCustomer);
    em.flush();

    long count = repository.countAtRisk(80);

    assertThat(count).isEqualTo(1);
  }

  @Test
  @TestTransaction
  void countOverdueFollowUps_shouldCountOverdue() {
    // Create overdue customer
    Customer overdueCustomer = createTestCustomer("Overdue Company");
    overdueCustomer.setNextFollowUpDate(LocalDateTime.now().minusDays(1));
    repository.persist(overdueCustomer);
    em.flush();

    long count = repository.countOverdueFollowUps();

    assertThat(count).isEqualTo(1);
  }

  // ========== RECENT ACTIVITY ==========

  @Test
  @TestTransaction
  void findRecentlyCreated_shouldReturnRecentCustomers() {
    // Create recent customer
    Customer recentCustomer = createTestCustomer("Recent Company");
    recentCustomer.setCreatedAt(LocalDateTime.now().minusDays(2));
    repository.persist(recentCustomer);
    em.flush();

    var result = repository.findRecentlyCreated(7, null);

    assertThat(result).isNotEmpty();
    assertThat(result).anyMatch(c -> c.getCompanyName().equals("Recent Company"));
  }

  @Test
  @TestTransaction
  void findRecentlyUpdated_shouldReturnRecentlyUpdatedCustomers() {
    TestDataSet data = createStandardTestData();

    // Update test customer
    data.testCustomer.setCompanyName("Updated Test Company");
    data.testCustomer.setUpdatedAt(LocalDateTime.now());
    repository.persist(data.testCustomer);
    em.flush();

    var result = repository.findRecentlyUpdated(1, null);

    assertThat(result).isNotEmpty();
    assertThat(result).anyMatch(c -> c.getCompanyName().equals("Updated Test Company"));
  }

  // ========== UTILITY METHODS ==========

  @Test
  @TestTransaction
  void getMaxCustomerNumberForYear_shouldReturnMaxNumber() {
    // Create customer with higher number
    Customer highNumberCustomer =
        createTestCustomerWithNumber("High Number Company", "KD-2025-00099");
    repository.persist(highNumberCustomer);
    em.flush();

    Integer maxNumber = repository.getMaxCustomerNumberForYear(2025);

    assertThat(maxNumber).isEqualTo(99);
  }

  @Test
  @TestTransaction
  void getMaxCustomerNumberForYear_shouldReturnNullForNoCustomers() {
    // Delete all customers for year 2026
    em.createQuery("DELETE FROM Customer WHERE customerNumber LIKE 'KD-2026-%'").executeUpdate();
    em.flush();

    Integer maxNumber = repository.getMaxCustomerNumberForYear(2026);

    assertThat(maxNumber).isNull();
  }

  // ========== HELPER METHODS ==========

  private Customer createTestCustomer(String companyName) {
    Customer customer = new Customer();
    // Don't set ID manually - let JPA generate it
    customer.setCompanyName(companyName);
    // Generate unique customer number
    customer.setCustomerNumber(
        "KD-TEST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    customer.setIsDeleted(false);
    // Set required audit fields
    customer.setCreatedBy("test-user");
    customer.setUpdatedBy("test-user");
    customer.setCreatedAt(LocalDateTime.now());
    customer.setUpdatedAt(LocalDateTime.now());
    return customer;
  }

  // For special cases where customer number matters
  private Customer createTestCustomerWithNumber(String companyName, String customerNumber) {
    Customer customer = new Customer();
    customer.setCompanyName(companyName);
    customer.setCustomerNumber(customerNumber);
    customer.setIsDeleted(false);
    customer.setCreatedBy("test-user");
    customer.setUpdatedBy("test-user");
    customer.setCreatedAt(LocalDateTime.now());
    customer.setUpdatedAt(LocalDateTime.now());
    return customer;
  }
}

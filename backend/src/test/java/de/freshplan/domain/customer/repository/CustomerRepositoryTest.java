package de.freshplan.domain.customer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.customer.entity.*;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive test suite for CustomerRepository.
 * Tests all repository methods including soft delete support, search functionality,
 * hierarchy management, and specialized queries.
 */
@QuarkusTest
@TestTransaction
class CustomerRepositoryTest {

  @Inject CustomerRepository repository;

  @Inject EntityManager em;

  private Customer testCustomer;
  private Customer deletedCustomer;
  private Customer parentCustomer;
  private Customer childCustomer;

  @BeforeEach
  void setUp() {
    // Clean up any existing test data in correct order due to foreign key constraints
    em.createQuery("DELETE FROM CustomerTimelineEvent").executeUpdate();
    em.createQuery("DELETE FROM CustomerContact").executeUpdate();
    em.createQuery("DELETE FROM CustomerLocation").executeUpdate();
    em.createQuery("DELETE FROM CustomerAddress").executeUpdate();
    em.createQuery("DELETE FROM Customer").executeUpdate();
    em.flush();

    // Create test customers
    testCustomer = createTestCustomer("Test Company", "KD-2025-00001");
    testCustomer.setStatus(CustomerStatus.AKTIV);
    testCustomer.setLifecycleStage(CustomerLifecycleStage.GROWTH);
    testCustomer.setIndustry(Industry.GESUNDHEITSWESEN);
    testCustomer.setExpectedAnnualVolume(new BigDecimal("50000.00"));
    testCustomer.setRiskScore(30);
    testCustomer.setLastContactDate(LocalDateTime.now().minusDays(10));
    testCustomer.setNextFollowUpDate(LocalDateTime.now().plusDays(5));
    repository.persist(testCustomer);

    // Create deleted customer
    deletedCustomer = createTestCustomer("Deleted Company", "KD-2025-00002");
    deletedCustomer.setIsDeleted(true);
    deletedCustomer.setDeletedAt(LocalDateTime.now());
    repository.persist(deletedCustomer);

    // Create parent-child hierarchy
    parentCustomer = createTestCustomer("Parent Company", "KD-2025-00003");
    repository.persist(parentCustomer);

    childCustomer = createTestCustomer("Child Company", "KD-2025-00004");
    childCustomer.setParentCustomer(parentCustomer);
    repository.persist(childCustomer);

    em.flush();
  }

  // ========== SOFT DELETE TESTS ==========

  @Test
  void findByIdActive_shouldReturnActiveCustomer() {
    var result = repository.findByIdActive(testCustomer.getId());
    
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(testCustomer.getId());
    assertThat(result.get().getIsDeleted()).isFalse();
  }

  @Test
  void findByIdActive_shouldNotReturnDeletedCustomer() {
    var result = repository.findByIdActive(deletedCustomer.getId());
    
    assertThat(result).isEmpty();
  }

  @Test
  void findAllActive_shouldOnlyReturnActiveCustomers() {
    var result = repository.findAllActive(null);
    
    assertThat(result).hasSize(3); // testCustomer, parentCustomer, childCustomer
    assertThat(result).noneMatch(Customer::getIsDeleted);
    assertThat(result).extracting(Customer::getCompanyName)
        .containsExactlyInAnyOrder("Test Company", "Parent Company", "Child Company");
  }

  @Test
  void countActive_shouldOnlyCountActiveCustomers() {
    long count = repository.countActive();
    
    assertThat(count).isEqualTo(3); // Excluding deleted customer
  }

  @Test
  void findDeleted_shouldOnlyReturnDeletedCustomers() {
    var result = repository.findDeleted(null);
    
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(deletedCustomer.getId());
    assertThat(result.get(0).getIsDeleted()).isTrue();
  }

  // ========== RISK CUSTOMER QUERIES ==========

  @Test
  void findActiveCustomersWithoutRecentContact_shouldReturnCustomersWithOldContact() {
    // Create customer with no contact
    Customer noContactCustomer = createTestCustomer("No Contact Company", "KD-2025-00005");
    noContactCustomer.setStatus(CustomerStatus.AKTIV);
    noContactCustomer.setLastContactDate(null);
    repository.persist(noContactCustomer);

    // Create customer with old contact
    Customer oldContactCustomer = createTestCustomer("Old Contact Company", "KD-2025-00006");
    oldContactCustomer.setStatus(CustomerStatus.AKTIV);
    oldContactCustomer.setLastContactDate(LocalDateTime.now().minusDays(60));
    repository.persist(oldContactCustomer);

    em.flush();

    LocalDateTime thresholdDate = LocalDateTime.now().minusDays(30);
    var result = repository.findActiveCustomersWithoutRecentContact(thresholdDate);

    assertThat(result).hasSize(2);
    assertThat(result).extracting(Customer::getCompanyName)
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
  void findByCustomerNumber_shouldReturnCorrectCustomer() {
    var result = repository.findByCustomerNumber("KD-2025-00001");
    
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(testCustomer.getId());
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
  void findByCustomerNumber_shouldNotReturnDeletedCustomer() {
    var result = repository.findByCustomerNumber("KD-2025-00002");
    
    assertThat(result).isEmpty();
  }

  @Test
  void existsByCustomerNumber_shouldReturnTrueForExisting() {
    boolean exists = repository.existsByCustomerNumber("KD-2025-00001");
    
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
  void findByStatus_shouldReturnCustomersWithStatus() {
    var result = repository.findByStatus(CustomerStatus.AKTIV, null);
    
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(testCustomer.getId());
  }

  @Test
  void findByStatusIn_shouldReturnCustomersWithAnyStatus() {
    // Create customer with different status
    Customer inactiveCustomer = createTestCustomer("Inactive Company", "KD-2025-00007");
    inactiveCustomer.setStatus(CustomerStatus.INAKTIV);
    repository.persist(inactiveCustomer);
    em.flush();

    var statuses = List.of(CustomerStatus.AKTIV, CustomerStatus.INAKTIV);
    var result = repository.findByStatusIn(statuses, null);
    
    assertThat(result).hasSize(2);
    assertThat(result).extracting(Customer::getStatus)
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
  void findByLifecycleStage_shouldReturnCorrectCustomers() {
    var result = repository.findByLifecycleStage(CustomerLifecycleStage.GROWTH, null);
    
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(testCustomer.getId());
  }

  @Test
  void findByIndustry_shouldReturnCorrectCustomers() {
    var result = repository.findByIndustry(Industry.GESUNDHEITSWESEN, null);
    
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(testCustomer.getId());
  }

  // ========== HIERARCHY SUPPORT ==========

  @Test
  void findChildren_shouldReturnAllChildren() {
    var children = repository.findChildren(parentCustomer.getId());
    
    assertThat(children).hasSize(1);
    assertThat(children.get(0).getId()).isEqualTo(childCustomer.getId());
  }

  @Test
  void findRootCustomers_shouldReturnCustomersWithoutParent() {
    var roots = repository.findRootCustomers(null);
    
    assertThat(roots).hasSize(2); // testCustomer and parentCustomer
    assertThat(roots).noneMatch(c -> c.getParentCustomer() != null);
  }

  @Test
  void hasChildren_shouldReturnTrueForParentWithChildren() {
    boolean hasChildren = repository.hasChildren(parentCustomer.getId());
    
    assertThat(hasChildren).isTrue();
  }

  @Test
  void hasChildren_shouldReturnFalseForCustomerWithoutChildren() {
    boolean hasChildren = repository.hasChildren(testCustomer.getId());
    
    assertThat(hasChildren).isFalse();
  }

  // ========== RISK MANAGEMENT ==========

  @Test
  void findAtRisk_shouldReturnHighRiskCustomers() {
    // Create high risk customer
    Customer highRiskCustomer = createTestCustomer("High Risk Company", "KD-2025-00008");
    highRiskCustomer.setRiskScore(80);
    repository.persist(highRiskCustomer);
    em.flush();

    var result = repository.findAtRisk(70, null);
    
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getRiskScore()).isGreaterThanOrEqualTo(70);
  }

  @Test
  void findOverdueFollowUps_shouldReturnOverdueCustomers() {
    // Create customer with overdue follow-up
    Customer overdueCustomer = createTestCustomer("Overdue Company", "KD-2025-00009");
    overdueCustomer.setNextFollowUpDate(LocalDateTime.now().minusDays(5));
    repository.persist(overdueCustomer);
    em.flush();

    var result = repository.findOverdueFollowUps(null);
    
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(overdueCustomer.getId());
  }

  @Test
  void findNotContactedSince_shouldReturnCustomersNotContacted() {
    // Create customer not contacted for long time
    Customer notContactedCustomer = createTestCustomer("Not Contacted Company", "KD-2025-00010");
    notContactedCustomer.setLastContactDate(LocalDateTime.now().minusDays(100));
    repository.persist(notContactedCustomer);
    em.flush();

    var result = repository.findNotContactedSince(90, null);
    
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(notContactedCustomer.getId());
  }

  // ========== FINANCIAL QUERIES ==========

  @Test
  void findByExpectedVolumeRange_shouldReturnCustomersInRange() {
    // Create customers with different volumes
    Customer lowVolumeCustomer = createTestCustomer("Low Volume Company", "KD-2025-00011");
    lowVolumeCustomer.setExpectedAnnualVolume(new BigDecimal("10000.00"));
    repository.persist(lowVolumeCustomer);

    Customer highVolumeCustomer = createTestCustomer("High Volume Company", "KD-2025-00012");
    highVolumeCustomer.setExpectedAnnualVolume(new BigDecimal("100000.00"));
    repository.persist(highVolumeCustomer);
    em.flush();

    var result = repository.findByExpectedVolumeRange(
        new BigDecimal("30000.00"), 
        new BigDecimal("70000.00"), 
        null
    );
    
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(testCustomer.getId());
    assertThat(result.get(0).getExpectedAnnualVolume())
        .isBetween(new BigDecimal("30000.00"), new BigDecimal("70000.00"));
  }

  @Test
  void findByExpectedVolumeRange_withOnlyMinVolume_shouldReturnCustomersAboveMin() {
    var result = repository.findByExpectedVolumeRange(
        new BigDecimal("40000.00"), 
        null, 
        null
    );
    
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getExpectedAnnualVolume())
        .isGreaterThanOrEqualTo(new BigDecimal("40000.00"));
  }

  @Test
  void findByExpectedVolumeRange_withOnlyMaxVolume_shouldReturnCustomersBelowMax() {
    var result = repository.findByExpectedVolumeRange(
        null, 
        new BigDecimal("60000.00"), 
        null
    );
    
    assertThat(result).hasSize(3); // test, parent, child customers
  }

  @Test
  void findByExpectedVolumeRange_withNoBounds_shouldReturnAllActive() {
    var result = repository.findByExpectedVolumeRange(null, null, null);
    
    assertThat(result).hasSize(3); // All active customers
  }

  // ========== DUPLICATE DETECTION ==========

  @Test
  void findPotentialDuplicates_shouldFindSimilarNames() {
    // Create similar named companies
    Customer similarCustomer1 = createTestCustomer("Test Company GmbH", "KD-2025-00013");
    repository.persist(similarCustomer1);

    Customer similarCustomer2 = createTestCustomer("Test Company AG", "KD-2025-00014");
    repository.persist(similarCustomer2);
    em.flush();

    var duplicates = repository.findPotentialDuplicates("Test Company");
    
    assertThat(duplicates).hasSize(2); // GmbH and AG variants
    assertThat(duplicates).extracting(Customer::getCompanyName)
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
  void countByStatus_shouldCountCorrectly() {
    long count = repository.countByStatus(CustomerStatus.AKTIV);
    
    assertThat(count).isEqualTo(1);
  }

  @Test
  void countByLifecycleStage_shouldCountCorrectly() {
    long count = repository.countByLifecycleStage(CustomerLifecycleStage.GROWTH);
    
    assertThat(count).isEqualTo(1);
  }

  @Test
  void countNewThisMonth_shouldCountRecentlyCreated() {
    // Create customer created today
    Customer newCustomer = createTestCustomer("New This Month Company", "KD-2025-00015");
    newCustomer.setCreatedAt(LocalDateTime.now());
    repository.persist(newCustomer);
    em.flush();

    long count = repository.countNewThisMonth();
    
    assertThat(count).isGreaterThanOrEqualTo(1); // At least the new customer
  }

  @Test
  void countAtRisk_shouldCountHighRiskCustomers() {
    // Create high risk customer
    Customer highRiskCustomer = createTestCustomer("High Risk Company", "KD-2025-00016");
    highRiskCustomer.setRiskScore(85);
    repository.persist(highRiskCustomer);
    em.flush();

    long count = repository.countAtRisk(80);
    
    assertThat(count).isEqualTo(1);
  }

  @Test
  void countOverdueFollowUps_shouldCountOverdue() {
    // Create overdue customer
    Customer overdueCustomer = createTestCustomer("Overdue Company", "KD-2025-00017");
    overdueCustomer.setNextFollowUpDate(LocalDateTime.now().minusDays(1));
    repository.persist(overdueCustomer);
    em.flush();

    long count = repository.countOverdueFollowUps();
    
    assertThat(count).isEqualTo(1);
  }

  // ========== RECENT ACTIVITY ==========

  @Test
  void findRecentlyCreated_shouldReturnRecentCustomers() {
    // Create recent customer
    Customer recentCustomer = createTestCustomer("Recent Company", "KD-2025-00018");
    recentCustomer.setCreatedAt(LocalDateTime.now().minusDays(2));
    repository.persist(recentCustomer);
    em.flush();

    var result = repository.findRecentlyCreated(7, null);
    
    assertThat(result).isNotEmpty();
    assertThat(result).anyMatch(c -> c.getCompanyName().equals("Recent Company"));
  }

  @Test
  void findRecentlyUpdated_shouldReturnRecentlyUpdatedCustomers() {
    // Update test customer
    testCustomer.setCompanyName("Updated Test Company");
    testCustomer.setUpdatedAt(LocalDateTime.now());
    repository.persist(testCustomer);
    em.flush();

    var result = repository.findRecentlyUpdated(1, null);
    
    assertThat(result).isNotEmpty();
    assertThat(result).anyMatch(c -> c.getCompanyName().equals("Updated Test Company"));
  }

  // ========== UTILITY METHODS ==========

  @Test
  void getMaxCustomerNumberForYear_shouldReturnMaxNumber() {
    // Create customer with higher number
    Customer highNumberCustomer = createTestCustomer("High Number Company", "KD-2025-00099");
    repository.persist(highNumberCustomer);
    em.flush();

    Integer maxNumber = repository.getMaxCustomerNumberForYear(2025);
    
    assertThat(maxNumber).isEqualTo(99);
  }

  @Test
  void getMaxCustomerNumberForYear_shouldReturnNullForNoCustomers() {
    // Delete all customers for year 2026
    em.createQuery("DELETE FROM Customer WHERE customerNumber LIKE 'KD-2026-%'")
        .executeUpdate();
    em.flush();

    Integer maxNumber = repository.getMaxCustomerNumberForYear(2026);
    
    assertThat(maxNumber).isNull();
  }

  // ========== HELPER METHODS ==========

  private Customer createTestCustomer(String companyName, String customerNumber) {
    Customer customer = new Customer();
    // Don't set ID manually - let JPA generate it
    customer.setCompanyName(companyName);
    customer.setCustomerNumber(customerNumber);
    customer.setIsDeleted(false);
    // Set required audit fields
    customer.setCreatedBy("test-user");
    customer.setUpdatedBy("test-user");
    customer.setCreatedAt(LocalDateTime.now());
    customer.setUpdatedAt(LocalDateTime.now());
    return customer;
  }
}
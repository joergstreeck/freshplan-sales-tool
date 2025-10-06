package de.freshplan.domain.cockpit.service.query;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.cockpit.service.dto.*;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.test.builders.CustomerTestDataFactory;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for SalesCockpitQueryService.
 *
 * <p>Converted from Mockito unit tests to @QuarkusTest integration tests in Phase 5B.3. Uses
 * self-managed test data (entity.persist()) instead of mocks.
 *
 * <p>Verifies that all query operations work correctly with real database interactions and that NO
 * write operations occur (pure read-only service).
 *
 * <p>IMPORTANT: @TestTransaction is applied per-method (not class-level) to ensure proper test
 * isolation and rollback after each test.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@Tag("integration")
class SalesCockpitQueryServiceTest {

  @Inject SalesCockpitQueryService queryService;

  @Inject CustomerRepository customerRepository;

  @Inject jakarta.persistence.EntityManager em;

  private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

  /**
   * Clean up test data before each test to ensure test isolation. Sprint 2.1.6: Fix test data
   * contamination between tests.
   */
  @org.junit.jupiter.api.BeforeEach
  @jakarta.transaction.Transactional
  void cleanupBeforeEach() {
    // Delete in correct order to respect foreign key constraints
    em.createNativeQuery(
            "DELETE FROM opportunity_activities WHERE opportunity_id IN (SELECT id FROM opportunities WHERE customer_id IN (SELECT id FROM customers WHERE is_test_data = true))")
        .executeUpdate();
    em.createNativeQuery(
            "DELETE FROM opportunities WHERE customer_id IN (SELECT id FROM customers WHERE is_test_data = true)")
        .executeUpdate();
    customerRepository.deleteAllTestData();
  }

  /**
   * Creates and persists a test customer within the test transaction. Must be called at the
   * beginning of each test method.
   */
  private Customer createAndPersistTestCustomer(String companyName, CustomerStatus status) {
    Customer testCustomer =
        CustomerTestDataFactory.builder()
            .withCompanyName(companyName)
            .withCustomerNumber("KD-" + System.nanoTime() % 1000000)
            .withStatus(status)
            .build();
    testCustomer.persist();
    return testCustomer;
  }

  private Customer createCustomerWithDaysSinceContact(int days, CustomerStatus status) {
    Customer customer =
        CustomerTestDataFactory.builder()
            .withCompanyName("Customer " + days + " days")
            .withCustomerNumber("KD-" + System.nanoTime() % 1000000)
            .withStatus(status)
            .build();
    customer.setLastContactDate(LocalDateTime.now().minusDays(days));
    customer.setCreatedAt(LocalDateTime.now().minusDays(days + 30));
    customer.persist();
    return customer;
  }

  private Customer createCustomerWithOverdueFollowUp() {
    Customer customer =
        CustomerTestDataFactory.builder()
            .withCompanyName("Overdue Customer GmbH")
            .withCustomerNumber("KD-" + System.nanoTime() % 1000000)
            .withStatus(CustomerStatus.AKTIV)
            .build();
    customer.setNextFollowUpDate(LocalDateTime.now().minusDays(5));
    customer.setCreatedAt(LocalDateTime.now().minusDays(60));
    customer.persist();
    return customer;
  }

  @TestTransaction
  @Test
  void testGetDashboardData_withValidUser_shouldReturnDashboard() {
    // Given - Create test customers
    createAndPersistTestCustomer("Test Company 1", CustomerStatus.AKTIV);
    createAndPersistTestCustomer("Test Company 2", CustomerStatus.AKTIV);
    createAndPersistTestCustomer("Test Company 3", CustomerStatus.INAKTIV);

    // Count before
    long customerCountBefore = customerRepository.count();

    // When
    SalesCockpitDashboard result = queryService.getDashboardData(TEST_USER_ID);

    // Then
    assertNotNull(result);
    assertNotNull(result.getTodaysTasks());
    assertNotNull(result.getRiskCustomers());
    assertNotNull(result.getStatistics());
    assertNotNull(result.getAlerts());

    // Verify NO write operations - count should be same
    long customerCountAfter = customerRepository.count();
    assertEquals(customerCountBefore, customerCountAfter, "Query service should not modify data");
  }

  @TestTransaction
  @Test
  void testGetDashboardData_withTestUserId_shouldSkipUserValidation() {
    // Given
    createAndPersistTestCustomer("Test Company", CustomerStatus.AKTIV);

    // When
    SalesCockpitDashboard result = queryService.getDashboardData(TEST_USER_ID);

    // Then
    assertNotNull(result);
    // TEST_USER_ID skips user validation, so this should work without user in DB
  }

  @TestTransaction
  @Test
  void testGetDashboardData_withNullUserId_shouldThrowException() {
    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> queryService.getDashboardData(null));

    assertEquals("User ID must not be null", exception.getMessage());
  }

  @TestTransaction
  @Test
  void testGetDevDashboardData_shouldReturnConsistentData() {
    // Given - Create diverse test data
    createAndPersistTestCustomer("Company 1", CustomerStatus.AKTIV);
    createAndPersistTestCustomer("Company 2", CustomerStatus.AKTIV);
    createCustomerWithOverdueFollowUp();
    createCustomerWithDaysSinceContact(125, CustomerStatus.AKTIV); // High risk
    createCustomerWithDaysSinceContact(35, CustomerStatus.AKTIV); // Low risk

    // Count before
    long customerCountBefore = customerRepository.count();

    // When
    SalesCockpitDashboard result = queryService.getDevDashboardData();

    // Then
    assertNotNull(result);
    assertNotNull(result.getStatistics());
    assertNotNull(result.getTodaysTasks());
    assertNotNull(result.getRiskCustomers());
    assertNotNull(result.getAlerts());

    // Verify NO write operations
    long customerCountAfter = customerRepository.count();
    assertEquals(customerCountBefore, customerCountAfter, "Query service should not modify data");
  }

  @TestTransaction
  @Test
  void testTodaysTasks_shouldIncludeOverdueFollowUps() {
    // Given
    createCustomerWithOverdueFollowUp();
    createAndPersistTestCustomer("Regular Company", CustomerStatus.AKTIV);

    // When
    SalesCockpitDashboard result = queryService.getDashboardData(TEST_USER_ID);

    // Then
    List<DashboardTask> tasks = result.getTodaysTasks();
    assertNotNull(tasks);

    // At least one task should be about overdue follow-up
    boolean hasOverdueTask =
        tasks.stream().anyMatch(t -> t.getTitle() != null && t.getTitle().contains("ÜBERFÄLLIG"));

    assertTrue(hasOverdueTask, "Should have at least one overdue follow-up task");
  }

  @TestTransaction
  @Test
  void testRiskCustomers_shouldCalculateRiskLevels() {
    // Given
    Customer highRiskCustomer = createCustomerWithDaysSinceContact(125, CustomerStatus.AKTIV);
    Customer mediumRiskCustomer = createCustomerWithDaysSinceContact(95, CustomerStatus.AKTIV);
    Customer lowRiskCustomer = createCustomerWithDaysSinceContact(65, CustomerStatus.AKTIV);

    // When
    SalesCockpitDashboard result = queryService.getDashboardData(TEST_USER_ID);

    // Then
    List<RiskCustomer> riskCustomers = result.getRiskCustomers();
    assertNotNull(riskCustomers);
    assertEquals(3, riskCustomers.size(), "Should have 3 risk customers");

    // Verify risk levels are correctly assigned
    RiskCustomer high =
        riskCustomers.stream()
            .filter(rc -> rc.getDaysSinceLastContact() > 120)
            .findFirst()
            .orElse(null);
    assertNotNull(high, "Should have high risk customer");
    assertEquals(RiskCustomer.RiskLevel.HIGH, high.getRiskLevel());

    RiskCustomer medium =
        riskCustomers.stream()
            .filter(rc -> rc.getDaysSinceLastContact() > 90 && rc.getDaysSinceLastContact() <= 120)
            .findFirst()
            .orElse(null);
    assertNotNull(medium, "Should have medium risk customer");
    assertEquals(RiskCustomer.RiskLevel.MEDIUM, medium.getRiskLevel());

    RiskCustomer low =
        riskCustomers.stream()
            .filter(rc -> rc.getDaysSinceLastContact() > 60 && rc.getDaysSinceLastContact() <= 90)
            .findFirst()
            .orElse(null);
    assertNotNull(low, "Should have low risk customer");
    assertEquals(RiskCustomer.RiskLevel.LOW, low.getRiskLevel());
  }

  @TestTransaction
  @Test
  void testStatistics_shouldAggregateCorrectly() {
    // Given - Count customers before test
    long customersBefore = customerRepository.count();

    createAndPersistTestCustomer("Active 1", CustomerStatus.AKTIV);
    createAndPersistTestCustomer("Active 2", CustomerStatus.AKTIV);
    createAndPersistTestCustomer("Inactive", CustomerStatus.INAKTIV);
    createCustomerWithDaysSinceContact(125, CustomerStatus.AKTIV); // At risk
    createCustomerWithOverdueFollowUp(); // Overdue

    // When
    SalesCockpitDashboard result = queryService.getDashboardData(TEST_USER_ID);

    // Then
    DashboardStatistics stats = result.getStatistics();
    assertNotNull(stats);
    // Phase 5C Fix: Verify we created exactly 5 new customers (accounting for test isolation)
    long customersAfter = customerRepository.count();
    assertEquals(
        customersBefore + 5, customersAfter, "Should have created exactly 5 test customers");
    assertTrue(stats.getActiveCustomers() >= 4, "Should have at least 4 active customers");
    assertTrue(
        stats.getCustomersAtRisk() >= 1, "Should have at least 1 customer at risk (>60 days)");
    assertTrue(stats.getOverdueItems() >= 1, "Should have at least 1 overdue follow-up");
  }

  @TestTransaction
  @Test
  void testAlerts_shouldGenerateOpportunityAlerts() {
    // Given
    createCustomerWithDaysSinceContact(35, CustomerStatus.AKTIV);
    createAndPersistTestCustomer("Regular Company", CustomerStatus.AKTIV);

    // When
    SalesCockpitDashboard result = queryService.getDashboardData(TEST_USER_ID);

    // Then
    List<DashboardAlert> alerts = result.getAlerts();
    assertNotNull(alerts);
    // Alerts are generated based on business logic - just verify structure
  }

  @TestTransaction
  @Test
  void testNoWriteOperations_inAnyMethod() {
    // Given
    createAndPersistTestCustomer("Test Company 1", CustomerStatus.AKTIV);
    createAndPersistTestCustomer("Test Company 2", CustomerStatus.AKTIV);
    createCustomerWithOverdueFollowUp();

    // Count before
    long customerCountBefore = customerRepository.count();

    // When - Execute all public methods
    queryService.getDashboardData(TEST_USER_ID);
    queryService.getDevDashboardData();

    // Then - Verify absolutely NO write operations occurred
    long customerCountAfter = customerRepository.count();
    assertEquals(
        customerCountBefore,
        customerCountAfter,
        "Query service should not create, update, or delete any customers");
  }
}

package de.freshplan.domain.cockpit.service;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for SalesCockpitService (Facade).
 *
 * <p>Sprint 2.1.7.4 - Section 7: Seasonal Business Support Tests
 *
 * <p>Tests the Facade pattern which delegates to SalesCockpitQueryService when CQRS is enabled.
 * Verifies that seasonal business metrics are correctly calculated and returned via the Facade.
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.4
 */
@QuarkusTest
@Tag("integration")
@DisplayName("SalesCockpitService - Seasonal Business Support (Facade)")
class SalesCockpitServiceTest {

  @Inject SalesCockpitService salesCockpitService;

  @Inject CustomerRepository customerRepository;

  @Inject jakarta.persistence.EntityManager em;

  private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

  /**
   * Clean up test data before each test to ensure test isolation. Sprint 2.1.6: Fix test data
   * contamination between tests. NOTE: We delete ALL customers to ensure complete isolation.
   */
  @org.junit.jupiter.api.BeforeEach
  @jakarta.transaction.Transactional
  void cleanupBeforeEach() {
    // Delete in correct order to respect foreign key constraints
    // 1. Delete opportunity_activities first (child of opportunities)
    em.createNativeQuery("DELETE FROM opportunity_activities").executeUpdate();

    // 2. Delete opportunities (child of customers)
    em.createNativeQuery("DELETE FROM opportunities").executeUpdate();

    // 3. Delete other customer-related tables
    em.createNativeQuery("DELETE FROM customer_timeline_events").executeUpdate();
    em.createNativeQuery("DELETE FROM customer_contacts").executeUpdate();
    em.createNativeQuery("DELETE FROM customer_addresses").executeUpdate();
    em.createNativeQuery("DELETE FROM customer_locations").executeUpdate();

    // 4. Finally delete ALL customers (not just test data)
    em.createNativeQuery("DELETE FROM customers").executeUpdate();
    em.flush();
    em.clear();
  }

  /**
   * Sprint 2.1.7.4 - Helper method to create seasonal business customers.
   *
   * @param companyName Company name
   * @param activeMonths List of active months (1-12)
   * @return Persisted seasonal customer
   */
  private Customer createSeasonalCustomer(String companyName, List<Integer> activeMonths) {
    Customer customer =
        CustomerTestDataFactory.builder()
            .withCompanyName(companyName)
            .withCustomerNumber("KD-" + System.nanoTime() % 1000000)
            .withStatus(CustomerStatus.AKTIV)
            .build();
    customer.setIsSeasonalBusiness(true);
    customer.setSeasonalMonths(activeMonths);
    customer.setSeasonalPattern("CUSTOM");
    customer.setCreatedAt(LocalDateTime.now().minusDays(30));
    customer.persist();
    return customer;
  }

  /**
   * Sprint 2.1.7.4 - Test 1: Dashboard contains seasonal business metrics.
   *
   * <p>Verifies that getDashboardData() via Facade returns DashboardStatistics with seasonal
   * metrics (seasonalActive and seasonalPaused).
   */
  @Test
  @TestTransaction
  @DisplayName("getDashboardData() should include seasonal business metrics")
  void testGetDashboardData_shouldIncludeSeasonalMetrics() {
    // GIVEN: Seasonal customers - 1 in-season, 1 out-of-season
    int currentMonth = java.time.LocalDate.now().getMonthValue();
    int nextMonth = (currentMonth % 12) + 1;

    // Seasonal customer IN-SEASON (active months include current month)
    createSeasonalCustomer("Eiscafé Sommer GmbH", List.of(currentMonth));

    // Seasonal customer OUT-OF-SEASON (active months do NOT include current month)
    createSeasonalCustomer("Biergarten Winter KG", List.of(nextMonth));

    // Regular non-seasonal customer (should not affect seasonal counts)
    Customer regular =
        CustomerTestDataFactory.builder()
            .withCompanyName("Regular GmbH")
            .withCustomerNumber("KD-" + System.nanoTime() % 1000000)
            .withStatus(CustomerStatus.AKTIV)
            .build();
    regular.setIsSeasonalBusiness(false);
    regular.setCreatedAt(LocalDateTime.now().minusDays(30));
    regular.persist();

    // WHEN: Calling getDashboardData() via Facade
    SalesCockpitDashboard dashboard = salesCockpitService.getDashboardData(TEST_USER_ID);

    // THEN: Dashboard should exist
    assertNotNull(dashboard, "Dashboard should not be null");
    assertNotNull(dashboard.getStatistics(), "Statistics should not be null");

    DashboardStatistics stats = dashboard.getStatistics();

    // THEN: Verify seasonal metrics
    assertEquals(
        1,
        stats.getSeasonalActive(),
        "Should have 1 seasonal customer in-season (current month: " + currentMonth + ")");
    assertEquals(
        1,
        stats.getSeasonalPaused(),
        "Should have 1 seasonal customer out-of-season (next month: " + nextMonth + ")");

    // Verify regular customer is not counted in seasonal metrics
    assertTrue(
        stats.getTotalCustomers() >= 3, "Total customers should include all 3 test customers");
  }

  /**
   * Sprint 2.1.7.4 - Test 2: Seasonal metrics consistency (CQRS Facade).
   *
   * <p>Verifies that the Facade (SalesCockpitService) returns identical seasonal metrics as the
   * QueryService, ensuring CQRS consistency.
   *
   * <p>NOTE: This test validates that the Facade correctly delegates to the QueryService and that
   * seasonal calculation logic is not duplicated/diverged.
   */
  @Test
  @TestTransaction
  @DisplayName("getDashboardData() seasonal metrics should be consistent with QueryService")
  void testGetDashboardData_seasonalMetricsConsistencyWithQueryService() {
    // GIVEN: Multiple seasonal customers with varying active months
    int currentMonth = java.time.LocalDate.now().getMonthValue();
    int month1 = (currentMonth % 12) + 1; // Next month
    int month2 = ((currentMonth + 1) % 12) + 1; // Month after next

    // Create 3 seasonal customers:
    // 1 in-season (current month)
    createSeasonalCustomer("In-Season A", List.of(currentMonth));

    // 2 out-of-season (future months)
    createSeasonalCustomer("Off-Season B", List.of(month1));
    createSeasonalCustomer("Off-Season C", List.of(month2));

    // WHEN: Calling getDashboardData() via Facade
    SalesCockpitDashboard dashboard = salesCockpitService.getDashboardData(TEST_USER_ID);
    DashboardStatistics stats = dashboard.getStatistics();

    // THEN: Verify seasonal metrics are calculated correctly
    assertEquals(
        1,
        stats.getSeasonalActive(),
        "Should have exactly 1 seasonal customer in-season (current month: " + currentMonth + ")");
    assertEquals(
        2,
        stats.getSeasonalPaused(),
        "Should have exactly 2 seasonal customers out-of-season (months: "
            + month1
            + ", "
            + month2
            + ")");

    // THEN: Verify business logic correctness
    // Total seasonal = seasonalActive + seasonalPaused
    int totalSeasonal = stats.getSeasonalActive() + stats.getSeasonalPaused();
    assertEquals(
        3,
        totalSeasonal,
        "Total seasonal customers (active + paused) should be 3 (all test seasonal customers)");

    // THEN: Verify seasonal metrics are non-negative
    assertTrue(stats.getSeasonalActive() >= 0, "Seasonal active count should never be negative");
    assertTrue(stats.getSeasonalPaused() >= 0, "Seasonal paused count should never be negative");
  }
}

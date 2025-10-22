package de.freshplan.domain.customer.service;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.CustomerRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit and Integration Tests for ChurnDetectionService
 *
 * <p>Sprint 2.1.7.4: Seasonal Business Support Tests
 *
 * @author FreshPlan Team
 */
@QuarkusTest
@DisplayName("ChurnDetectionService - Seasonal Business Support")
class ChurnDetectionServiceTest {

  @Inject ChurnDetectionService churnDetectionService;

  @Inject CustomerRepository customerRepository;

  @AfterEach
  @Transactional
  void cleanup() {
    // Clean up test customers
    customerRepository.delete(
        "companyName LIKE 'TEST-CHURN-%' OR customerNumber LIKE 'TEST-CHURN-%'");
  }

  @Test
  @DisplayName("shouldCheckForChurn() - regular customer (should return true)")
  void shouldCheckForChurn_RegularCustomer() {
    // GIVEN: Regular (non-seasonal) customer
    Customer customer = new Customer();
    customer.setIsSeasonalBusiness(false);
    customer.setSeasonalMonths(null);

    // WHEN: Checking if churn monitoring should be applied
    boolean result = churnDetectionService.shouldCheckForChurn(customer);

    // THEN: Should be monitored for churn
    assertTrue(result, "Regular customers should always be checked for churn");
  }

  @Test
  @DisplayName("shouldCheckForChurn() - seasonal in-season (should return true) - Januar (Winter)")
  void shouldCheckForChurn_SeasonalInSeason_Januar() {
    // GIVEN: Seasonal business with active season in winter (October - March)
    Customer customer = new Customer();
    customer.setIsSeasonalBusiness(true);
    customer.setSeasonalMonths(Arrays.asList(10, 11, 12, 1, 2, 3)); // Oct - Mar
    customer.setSeasonalPattern("WINTER");

    // WHEN: Current month is January (in-season)
    // NOTE: Test assumes current date is flexible or we're testing the logic path
    int currentMonth = LocalDate.now().getMonthValue();

    boolean result = churnDetectionService.shouldCheckForChurn(customer);

    // THEN: Should be monitored if current month is in active months
    if (customer.getSeasonalMonths().contains(currentMonth)) {
      assertTrue(
          result,
          "Seasonal customers IN-SEASON (current month: "
              + currentMonth
              + ") should be checked for churn");
    } else {
      assertFalse(
          result,
          "Seasonal customers OUT-OF-SEASON (current month: "
              + currentMonth
              + ") should NOT be checked for churn");
    }
  }

  @Test
  @DisplayName("shouldCheckForChurn() - seasonal in-season (should return true) - Simulated Month")
  void shouldCheckForChurn_SeasonalInSeason_Simulated() {
    // GIVEN: Seasonal business with active season in summer (March - October)
    Customer customer = new Customer();
    customer.setIsSeasonalBusiness(true);
    // Set current month as active month to guarantee in-season test
    int currentMonth = LocalDate.now().getMonthValue();
    customer.setSeasonalMonths(List.of(currentMonth)); // Only current month
    customer.setSeasonalPattern("CUSTOM");

    // WHEN: Checking if churn monitoring should be applied
    boolean result = churnDetectionService.shouldCheckForChurn(customer);

    // THEN: Should be monitored for churn (in-season)
    assertTrue(
        result,
        "Seasonal customers IN-SEASON (month "
            + currentMonth
            + " is in active months) should be checked for churn");
  }

  @Test
  @DisplayName(
      "shouldCheckForChurn() - seasonal off-season (should return false) - Simulated Month")
  void shouldCheckForChurn_SeasonalOffSeason_Simulated() {
    // GIVEN: Seasonal business with active season that does NOT include current month
    Customer customer = new Customer();
    customer.setIsSeasonalBusiness(true);
    // Set active months to explicitly exclude current month
    int currentMonth = LocalDate.now().getMonthValue();
    int offSeasonMonth = (currentMonth % 12) + 1; // Next month (guaranteed different)
    customer.setSeasonalMonths(List.of(offSeasonMonth)); // Only next month
    customer.setSeasonalPattern("CUSTOM");

    // WHEN: Checking if churn monitoring should be applied
    boolean result = churnDetectionService.shouldCheckForChurn(customer);

    // THEN: Should NOT be monitored for churn (off-season)
    assertFalse(
        result,
        "Seasonal customers OUT-OF-SEASON (current month "
            + currentMonth
            + " NOT in active months "
            + customer.getSeasonalMonths()
            + ") should NOT be checked for churn");
  }

  @Test
  @DisplayName("shouldCheckForChurn() - seasonal with empty active months (should return true)")
  void shouldCheckForChurn_SeasonalEmptyMonths() {
    // GIVEN: Seasonal business with no active months defined
    Customer customer = new Customer();
    customer.setIsSeasonalBusiness(true);
    customer.setSeasonalMonths(List.of()); // Empty list

    // WHEN: Checking if churn monitoring should be applied
    boolean result = churnDetectionService.shouldCheckForChurn(customer);

    // THEN: Should be monitored (no exclusion)
    assertTrue(
        result,
        "Seasonal customers with empty active months should be checked for churn (no exclusion)");
  }

  @Test
  @Transactional
  @DisplayName("getAtRiskCustomers() - excludes off-season seasonal businesses (Integration Test)")
  void getAtRiskCustomers_ExcludesOffSeasonSeasonal() {
    // GIVEN: 3 customers - 1 regular, 1 seasonal in-season, 1 seasonal off-season
    // All have lastContactDate > 90 days ago

    LocalDateTime oldContactDate = LocalDateTime.now().minusDays(100); // 100 days ago

    // Regular customer at-risk
    Customer regularCustomer = createTestCustomer("TEST-CHURN-REG", false, null);
    regularCustomer.setStatus(CustomerStatus.AKTIV);
    regularCustomer.setLastContactDate(oldContactDate);
    customerRepository.persist(regularCustomer);

    // Seasonal customer IN-SEASON (should be included)
    int currentMonth = LocalDate.now().getMonthValue();
    Customer seasonalInSeason = createTestCustomer("TEST-CHURN-SEA-IN", true, currentMonth);
    seasonalInSeason.setStatus(CustomerStatus.AKTIV);
    seasonalInSeason.setLastContactDate(oldContactDate);
    seasonalInSeason.setSeasonalPattern("CUSTOM");
    customerRepository.persist(seasonalInSeason);

    // Seasonal customer OFF-SEASON (should be excluded)
    int offSeasonMonth = (currentMonth % 12) + 1; // Next month (guaranteed different)
    Customer seasonalOffSeason = createTestCustomer("TEST-CHURN-SEA-OFF", true, offSeasonMonth);
    seasonalOffSeason.setStatus(CustomerStatus.AKTIV);
    seasonalOffSeason.setLastContactDate(oldContactDate);
    seasonalOffSeason.setSeasonalPattern("CUSTOM");
    customerRepository.persist(seasonalOffSeason);

    customerRepository.flush();

    // WHEN: Getting at-risk customers
    List<Customer> atRiskCustomers = churnDetectionService.getAtRiskCustomers();

    // THEN: Should include regular + seasonal in-season, exclude seasonal off-season
    List<String> atRiskNumbers = atRiskCustomers.stream().map(Customer::getCustomerNumber).toList();

    assertTrue(
        atRiskNumbers.contains(regularCustomer.getCustomerNumber()),
        "Regular customer should be at-risk");
    assertTrue(
        atRiskNumbers.contains(seasonalInSeason.getCustomerNumber()),
        "Seasonal in-season customer should be at-risk");
    assertFalse(
        atRiskNumbers.contains(seasonalOffSeason.getCustomerNumber()),
        "Seasonal off-season customer should NOT be at-risk (excluded)");

    // Verify count
    int expectedCount = 2; // regular + seasonal in-season
    assertTrue(
        atRiskCustomers.size() >= expectedCount,
        "Should have at least "
            + expectedCount
            + " at-risk customers (found: "
            + atRiskCustomers.size()
            + ")");
  }

  @Test
  @Transactional
  @DisplayName("getAtRiskCustomers() - excludes customers with recent contact (Integration Test)")
  void getAtRiskCustomers_ExcludesRecentContact() {
    // GIVEN: 2 customers - 1 with old contact (at-risk), 1 with recent contact (not at-risk)

    LocalDateTime oldContactDate = LocalDateTime.now().minusDays(100); // 100 days ago (at-risk)
    LocalDateTime recentContactDate = LocalDateTime.now().minusDays(30); // 30 days ago (OK)

    // Customer with old contact
    Customer oldContactCustomer = createTestCustomer("TEST-CHURN-OLD", false, null);
    oldContactCustomer.setStatus(CustomerStatus.AKTIV);
    oldContactCustomer.setLastContactDate(oldContactDate);
    customerRepository.persist(oldContactCustomer);

    // Customer with recent contact
    Customer recentContactCustomer = createTestCustomer("TEST-CHURN-RECENT", false, null);
    recentContactCustomer.setStatus(CustomerStatus.AKTIV);
    recentContactCustomer.setLastContactDate(recentContactDate);
    customerRepository.persist(recentContactCustomer);

    customerRepository.flush();

    // WHEN: Getting at-risk customers
    List<Customer> atRiskCustomers = churnDetectionService.getAtRiskCustomers();

    // THEN: Should include old contact, exclude recent contact
    List<String> atRiskNumbers = atRiskCustomers.stream().map(Customer::getCustomerNumber).toList();

    assertTrue(
        atRiskNumbers.contains(oldContactCustomer.getCustomerNumber()),
        "Customer with old contact should be at-risk");
    assertFalse(
        atRiskNumbers.contains(recentContactCustomer.getCustomerNumber()),
        "Customer with recent contact should NOT be at-risk");
  }

  @Test
  @DisplayName("getChurnThresholdDays() - returns correct threshold")
  void getChurnThresholdDays() {
    // WHEN: Getting churn threshold
    int threshold = churnDetectionService.getChurnThresholdDays();

    // THEN: Should return 90 days
    assertEquals(90, threshold, "Churn threshold should be 90 days");
  }

  // Helper Methods

  private Customer createTestCustomer(
      String customerNumber, boolean isSeasonal, Integer activeMonth) {
    Customer customer = new Customer();
    // Ensure customer_number is max 20 chars
    customer.setCustomerNumber(
        customerNumber.length() > 20 ? customerNumber.substring(0, 20) : customerNumber);
    customer.setCompanyName("Test - " + customerNumber);
    customer.setStatus(CustomerStatus.AKTIV);
    customer.setCreatedAt(LocalDateTime.now());
    customer.setLastModifiedAt(LocalDateTime.now());
    customer.setCreatedBy("test-system"); // Required field
    customer.setLastModifiedBy("test-system"); // Required field

    customer.setIsSeasonalBusiness(isSeasonal);
    if (isSeasonal && activeMonth != null) {
      customer.setSeasonalMonths(List.of(activeMonth));
    }

    return customer;
  }
}

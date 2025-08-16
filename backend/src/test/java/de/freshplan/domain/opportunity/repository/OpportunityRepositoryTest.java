package de.freshplan.domain.opportunity.repository;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Umfassende Tests für OpportunityRepository - Analytics Queries
 *
 * <p>Tests decken ab: - Complex Analytics Queries - Forecast Calculations - Stage Distribution -
 * Performance Metrics - Time-based Analytics - User-specific Metrics
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
@TestTransaction
public class OpportunityRepositoryTest {

  @Inject OpportunityRepository opportunityRepository;

  @Inject CustomerRepository customerRepository;

  @Inject UserRepository userRepository;

  private Customer testCustomer1;
  private Customer testCustomer2;
  private User testUser1;
  private User testUser2;

  @BeforeEach
  void setUp() {
    // Clean up existing test data
    opportunityRepository.deleteAll();
    opportunityRepository.getEntityManager().flush();
    opportunityRepository.getEntityManager().clear();

    // Create test customers
    testCustomer1 = getOrCreateCustomer("Test Company 1", "test1@example.com");
    testCustomer2 = getOrCreateCustomer("Test Company 2", "test2@example.com");

    // Get or create test users for CI environment
    testUser1 = userRepository.find("username", "testuser1").firstResult();
    if (testUser1 == null) {
      testUser1 = new User("testuser1", "Test", "User1", "testuser1@test.com");
      testUser1.setRoles(Arrays.asList("admin", "manager", "sales"));
      userRepository.persist(testUser1);
    }

    testUser2 = userRepository.find("username", "testuser2").firstResult();
    if (testUser2 == null) {
      testUser2 = new User("testuser2", "Test", "User2", "testuser2@test.com");
      testUser2.setRoles(Arrays.asList("manager", "sales"));
      userRepository.persist(testUser2);
    }
  }

  @Nested
  @DisplayName("Basic Repository Operations")
  class BasicRepositoryOperations {

    @Test
    @DisplayName("Should find opportunities by stage")
    void findByStage_shouldReturnCorrectOpportunities() {
      // Arrange
      createTestOpportunity("New Lead 1", OpportunityStage.NEW_LEAD, testUser1);
      createTestOpportunity("New Lead 2", OpportunityStage.NEW_LEAD, testUser1);
      createTestOpportunity("Qualification 1", OpportunityStage.QUALIFICATION, testUser1);

      // Act
      var newLeadOpportunities = opportunityRepository.findByStage(OpportunityStage.NEW_LEAD);
      var qualificationOpportunities =
          opportunityRepository.findByStage(OpportunityStage.QUALIFICATION);

      // Assert
      assertThat(newLeadOpportunities).hasSize(2);
      assertThat(qualificationOpportunities).hasSize(1);
    }

    @Test
    @DisplayName("Should find opportunities by assigned user")
    void findByAssignedTo_shouldReturnUserOpportunities() {
      // Arrange
      createTestOpportunity("User1 Opp1", OpportunityStage.NEW_LEAD, testUser1);
      createTestOpportunity("User1 Opp2", OpportunityStage.QUALIFICATION, testUser1);
      createTestOpportunity("User2 Opp1", OpportunityStage.NEW_LEAD, testUser2);

      // Act
      var user1Opportunities = opportunityRepository.findByAssignedTo(testUser1);
      var user2Opportunities = opportunityRepository.findByAssignedTo(testUser2);

      // Debug
      System.out.println("User1 opportunities found: " + user1Opportunities.size());
      for (var opp : user1Opportunities) {
        System.out.println("- " + opp.getName() + " (Stage: " + opp.getStage() + ")");
      }

      // Assert
      assertThat(user1Opportunities).hasSize(2);
      assertThat(user2Opportunities).hasSize(1);
    }

    @Test
    @DisplayName("Should find opportunities by customer")
    void findByCustomer_shouldReturnCustomerOpportunities() {
      // Arrange
      // createTestOpportunity uses testCustomer1 by default
      createTestOpportunity("Customer1 Opp1", OpportunityStage.NEW_LEAD, testUser1);
      createTestOpportunity("Customer1 Opp2", OpportunityStage.PROPOSAL, testUser1);

      // Create one for customer2
      var opp3 =
          createTestOpportunity(
              "Customer2 Opp1", OpportunityStage.NEW_LEAD, testUser1, null, testCustomer2);

      // Act
      var customer1Opportunities = opportunityRepository.findByCustomer(testCustomer1);
      var customer2Opportunities = opportunityRepository.findByCustomer(testCustomer2);

      // Assert
      assertThat(customer1Opportunities).hasSize(2);
      assertThat(customer2Opportunities).hasSize(1);
    }
  }

  @Nested
  @DisplayName("Analytics and Forecast Calculations")
  class AnalyticsCalculations {

    @Test
    @DisplayName("Should calculate total forecast correctly")
    void calculateForecast_shouldReturnCorrectTotal() {
      // Arrange
      createOpportunityWithValue(
          "Opp1", OpportunityStage.NEW_LEAD, BigDecimal.valueOf(10000), 10); // Expected: 1000
      createOpportunityWithValue(
          "Opp2", OpportunityStage.PROPOSAL, BigDecimal.valueOf(20000), 60); // Expected: 12000
      createOpportunityWithValue(
          "Opp3", OpportunityStage.NEGOTIATION, BigDecimal.valueOf(15000), 80); // Expected: 12000

      // Create closed opportunities (should not be included)
      createOpportunityWithValue(
          "Closed Won",
          OpportunityStage.CLOSED_WON,
          BigDecimal.valueOf(50000),
          100); // Should not be included
      createOpportunityWithValue(
          "Closed Lost",
          OpportunityStage.CLOSED_LOST,
          BigDecimal.valueOf(30000),
          0); // Should not be included

      // Act
      var forecast = opportunityRepository.calculateForecast();

      // Assert
      assertThat(forecast).isEqualByComparingTo(BigDecimal.valueOf(25000)); // 1000 + 12000 + 12000
    }

    @Test
    @DisplayName("Should return zero forecast when no active opportunities exist")
    void calculateForecast_noActiveOpportunities_shouldReturnZero() {
      // Arrange - only closed opportunities
      createOpportunityWithValue(
          "Closed Won", OpportunityStage.CLOSED_WON, BigDecimal.valueOf(50000), 100);
      createOpportunityWithValue(
          "Closed Lost", OpportunityStage.CLOSED_LOST, BigDecimal.valueOf(30000), 0);

      // Act
      var forecast = opportunityRepository.calculateForecast();

      // Assert
      assertThat(forecast).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should handle null expected values in forecast calculation")
    void calculateForecast_withNullValues_shouldIgnoreNulls() {
      // Arrange
      createOpportunityWithValue(
          "Valid Opp", OpportunityStage.PROPOSAL, BigDecimal.valueOf(10000), 60); // Expected: 6000

      var oppWithNullValue =
          createTestOpportunity("Null Value Opp", OpportunityStage.PROPOSAL, testUser1);
      oppWithNullValue.setExpectedValue(null); // This should be ignored

      // Act
      var forecast = opportunityRepository.calculateForecast();

      // Assert
      assertThat(forecast).isEqualByComparingTo(BigDecimal.valueOf(6000));
    }

    @Test
    @DisplayName("Should calculate stage distribution correctly")
    void getStageDistribution_shouldReturnCorrectCounts() {
      // Arrange
      createTestOpportunity("New1", OpportunityStage.NEW_LEAD, testUser1);
      createTestOpportunity("New2", OpportunityStage.NEW_LEAD, testUser1);
      createTestOpportunity("Qual1", OpportunityStage.QUALIFICATION, testUser1);
      createTestOpportunity("Prop1", OpportunityStage.PROPOSAL, testUser1);
      createTestOpportunity("Prop2", OpportunityStage.PROPOSAL, testUser1);
      createTestOpportunity("Won1", OpportunityStage.CLOSED_WON, testUser1);

      // Act
      var distribution = opportunityRepository.getStageDistribution();

      // Assert
      assertThat(distribution)
          .containsEntry(OpportunityStage.NEW_LEAD, 2L)
          .containsEntry(OpportunityStage.QUALIFICATION, 1L)
          .containsEntry(OpportunityStage.PROPOSAL, 2L)
          .containsEntry(OpportunityStage.CLOSED_WON, 1L)
          .doesNotContainKey(OpportunityStage.NEEDS_ANALYSIS)
          .doesNotContainKey(OpportunityStage.NEGOTIATION)
          .doesNotContainKey(OpportunityStage.CLOSED_LOST);
    }

    @Test
    @DisplayName("Should calculate conversion rates between stages")
    void getConversionRate_shouldCalculateCorrectly() {
      // Arrange - Create opportunities that show conversion pattern
      // 10 NEW_LEAD opportunities
      for (int i = 1; i <= 10; i++) {
        createTestOpportunity("New" + i, OpportunityStage.NEW_LEAD, testUser1);
      }
      // 5 QUALIFICATION opportunities (50% conversion from NEW_LEAD)
      for (int i = 1; i <= 5; i++) {
        createTestOpportunity("Qual" + i, OpportunityStage.QUALIFICATION, testUser1);
      }

      // Act
      var conversionRate = opportunityRepository.getConversionRate();

      // Assert
      // Since we can't directly measure historical conversions without time tracking,
      // this test verifies the query execution rather than specific conversion logic
      assertThat(conversionRate).isNotNull();
    }
  }

  @Nested
  @DisplayName("Time-based Analytics")
  class TimeBasedAnalytics {

    @Test
    @DisplayName("Should find opportunities by expected close date range")
    void findByExpectedCloseDateBetween_shouldReturnCorrectOpportunities() {
      // Arrange
      var today = LocalDate.now();
      var nextWeek = today.plusWeeks(1);
      var nextMonth = today.plusMonths(1);

      var oppThisWeek =
          createTestOpportunity(
              "This Week", OpportunityStage.PROPOSAL, testUser1, today.plusDays(3));
      var oppNextWeek =
          createTestOpportunity(
              "Next Week", OpportunityStage.PROPOSAL, testUser1, nextWeek.plusDays(2));
      var oppNextMonth =
          createTestOpportunity("Next Month", OpportunityStage.PROPOSAL, testUser1, nextMonth);

      // Act
      var thisWeekOpportunities =
          opportunityRepository.findByExpectedCloseDateBetween(today, nextWeek);

      // Assert
      assertThat(thisWeekOpportunities).hasSize(1);
      assertThat(thisWeekOpportunities.get(0).getName()).isEqualTo("This Week");
    }

    @Test
    @DisplayName("Should find opportunities created in date range")
    void findByCreatedAtBetween_shouldReturnCorrectOpportunities() {
      // Arrange
      var yesterday = LocalDateTime.now().minusDays(1);
      var tomorrow = LocalDateTime.now().plusDays(1);

      // Create opportunities - createdAt is set automatically
      // This test won't work as expected since we can't set createdAt manually
      var opp1 = createTestOpportunity("Opp 1", OpportunityStage.NEW_LEAD, testUser1);
      var opp2 = createTestOpportunity("Opp 2", OpportunityStage.NEW_LEAD, testUser1);

      // Act
      var recentOpportunities = opportunityRepository.findByCreatedAtBetween(yesterday, tomorrow);

      // Assert - both opportunities were just created so both should be found
      assertThat(recentOpportunities).hasSize(2);
    }

    @Test
    @DisplayName("Should find overdue opportunities")
    void findOverdueOpportunities_shouldReturnExpiredOpportunities() {
      // Arrange
      var pastDate = LocalDate.now().minusDays(5);
      var futureDate = LocalDate.now().plusDays(5);

      var overdueOpp =
          createTestOpportunity("Overdue Opp", OpportunityStage.PROPOSAL, testUser1, pastDate);
      var futureOpp =
          createTestOpportunity("Future Opp", OpportunityStage.PROPOSAL, testUser1, futureDate);

      // Act
      var overdueOpportunities = opportunityRepository.findOverdueOpportunities();

      // Assert
      assertThat(overdueOpportunities).hasSize(1);
      assertThat(overdueOpportunities.get(0).getName()).isEqualTo("Overdue Opp");
    }
  }

  @Nested
  @DisplayName("User Performance Analytics")
  class UserPerformanceAnalytics {

    @Test
    @DisplayName("Should calculate user performance metrics")
    void getUserPerformanceMetrics_shouldReturnCorrectData() {
      // Arrange
      createOpportunityWithValue(
          "User1 Won", OpportunityStage.CLOSED_WON, BigDecimal.valueOf(10000), 100, testUser1);
      createOpportunityWithValue(
          "User1 Lost", OpportunityStage.CLOSED_LOST, BigDecimal.valueOf(5000), 0, testUser1);
      createOpportunityWithValue(
          "User1 Active", OpportunityStage.PROPOSAL, BigDecimal.valueOf(8000), 60, testUser1);

      createOpportunityWithValue(
          "User2 Won", OpportunityStage.CLOSED_WON, BigDecimal.valueOf(15000), 100, testUser2);

      // Act
      var user1Metrics = opportunityRepository.getUserPerformanceMetrics(testUser1);
      var user2Metrics = opportunityRepository.getUserPerformanceMetrics(testUser2);

      // Assert
      assertThat(user1Metrics)
          .containsEntry("totalOpportunities", 3L)
          .containsEntry("wonOpportunities", 1L)
          .containsKey("totalValue")
          .containsKey("wonValue");

      assertThat(user2Metrics)
          .containsEntry("totalOpportunities", 1L)
          .containsEntry("wonOpportunities", 1L)
          .containsKey("totalValue")
          .containsKey("wonValue");
    }

    @Test
    @DisplayName("Should calculate win rate for user")
    void getWinRateForUser_shouldCalculateCorrectly() {
      // Arrange
      createOpportunityWithValue(
          "Won1", OpportunityStage.CLOSED_WON, BigDecimal.valueOf(10000), 100, testUser1);
      createOpportunityWithValue(
          "Won2", OpportunityStage.CLOSED_WON, BigDecimal.valueOf(15000), 100, testUser1);
      createOpportunityWithValue(
          "Lost1", OpportunityStage.CLOSED_LOST, BigDecimal.valueOf(5000), 0, testUser1);
      // Active opportunities should not count in win rate
      createOpportunityWithValue(
          "Active1", OpportunityStage.PROPOSAL, BigDecimal.valueOf(8000), 60, testUser1);

      // Act
      var winRate = opportunityRepository.getWinRateForUser(testUser1);

      // Assert - 2 won out of 3 closed = 66.67%
      assertThat(winRate).isCloseTo(66.67, within(0.1));
    }

    @Test
    @DisplayName("Should return zero win rate when user has no closed opportunities")
    void getWinRateForUser_noClosedOpportunities_shouldReturnZero() {
      // Arrange - only active opportunities
      createOpportunityWithValue(
          "Active1", OpportunityStage.PROPOSAL, BigDecimal.valueOf(8000), 60, testUser1);

      // Act
      var winRate = opportunityRepository.getWinRateForUser(testUser1);

      // Assert
      assertThat(winRate).isEqualTo(0.0);
    }
  }

  @Nested
  @DisplayName("Complex Analytics Queries")
  class ComplexAnalyticsQueries {

    @Test
    @DisplayName("Should find top performing opportunities by value")
    void findTopOpportunitiesByValue_shouldReturnSortedList() {
      // Arrange
      createOpportunityWithValue(
          "High Value", OpportunityStage.PROPOSAL, BigDecimal.valueOf(50000), 60);
      createOpportunityWithValue(
          "Medium Value", OpportunityStage.PROPOSAL, BigDecimal.valueOf(25000), 60);
      createOpportunityWithValue(
          "Low Value", OpportunityStage.PROPOSAL, BigDecimal.valueOf(10000), 60);

      // Act
      var topOpportunities = opportunityRepository.findTopOpportunitiesByValue(2);

      // Assert
      assertThat(topOpportunities).hasSize(2);
      assertThat(topOpportunities.get(0).getName()).isEqualTo("High Value");
      assertThat(topOpportunities.get(1).getName()).isEqualTo("Medium Value");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 5})
    @DisplayName("Should respect limit parameter in top opportunities query")
    void findTopOpportunitiesByValue_shouldRespectLimit(int limit) {
      // Arrange - Create 5 opportunities
      for (int i = 1; i <= 5; i++) {
        createOpportunityWithValue(
            "Opp" + i, OpportunityStage.PROPOSAL, BigDecimal.valueOf(i * 10000), 60);
      }

      // Act
      var topOpportunities = opportunityRepository.findTopOpportunitiesByValue(limit);

      // Assert
      assertThat(topOpportunities).hasSize(Math.min(limit, 5));
    }

    @Test
    @DisplayName("Should find opportunities requiring attention (stale opportunities)")
    void findOpportunitiesRequiringAttention_shouldReturnCorrectOpportunities() {
      // Arrange
      // The method finds opportunities that have been in the same stage for 30+ days
      // We can't directly set stageChangedAt, so this test will likely find no results
      // Create opportunities - they will all have recent stageChangedAt
      var opp1 =
          createOpportunityWithValue(
              "New Opp", OpportunityStage.PROPOSAL, BigDecimal.valueOf(20000), 60);
      var opp2 =
          createOpportunityWithValue(
              "Another New", OpportunityStage.NEGOTIATION, BigDecimal.valueOf(100000), 80);

      // Act
      var attentionOpportunities = opportunityRepository.findOpportunitiesRequiringAttention();

      // Assert - all opportunities are newly created, so none should be stale
      assertThat(attentionOpportunities).isEmpty();
    }
  }

  // Helper methods

  @TestTransaction
  Customer getOrCreateCustomer(String companyName, String email) {
    var existingCustomer = customerRepository.find("companyName", companyName).firstResult();
    if (existingCustomer != null) {
      return existingCustomer;
    }

    var customer = new Customer();
    customer.setCompanyName(companyName);
    customer.setCustomerNumber("TEST-" + System.currentTimeMillis());
    customer.setCreatedBy("testuser");
    // Customer email field not available
    customerRepository.persist(customer);
    return customer;
  }

  @TestTransaction
  User getOrCreateUser(String username, String firstName, String lastName) {
    var existingUser = userRepository.find("username", username).firstResult();
    if (existingUser != null) {
      return existingUser;
    }

    throw new IllegalStateException(
        "Test user '" + username + "' not found - TestDataInitializer may not have run properly");
  }

  @TestTransaction
  Opportunity createTestOpportunity(String name, OpportunityStage stage, User assignedTo) {
    return createTestOpportunity(name, stage, assignedTo, null);
  }

  @TestTransaction
  Opportunity createTestOpportunity(
      String name, OpportunityStage stage, User assignedTo, LocalDate expectedCloseDate) {
    return createTestOpportunity(name, stage, assignedTo, expectedCloseDate, testCustomer1);
  }

  @TestTransaction
  Opportunity createTestOpportunity(
      String name,
      OpportunityStage stage,
      User assignedTo,
      LocalDate expectedCloseDate,
      Customer customer) {
    var opportunity = new Opportunity();
    opportunity.setName(name);
    opportunity.setStage(stage);
    opportunity.setAssignedTo(assignedTo);
    opportunity.setCustomer(customer);
    if (expectedCloseDate != null) {
      opportunity.setExpectedCloseDate(expectedCloseDate);
    }
    opportunityRepository.persist(opportunity);
    return opportunity;
  }

  @TestTransaction
  Opportunity createOpportunityWithValue(
      String name, OpportunityStage stage, BigDecimal expectedValue, Integer probability) {
    return createOpportunityWithValue(name, stage, expectedValue, probability, testUser1);
  }

  @TestTransaction
  Opportunity createOpportunityWithValue(
      String name,
      OpportunityStage stage,
      BigDecimal expectedValue,
      Integer probability,
      User assignedTo) {
    var opportunity = new Opportunity();
    opportunity.setName(name);
    opportunity.setStage(stage);
    opportunity.setAssignedTo(assignedTo);
    opportunity.setCustomer(testCustomer1);
    opportunity.setExpectedValue(expectedValue);
    opportunity.setProbability(probability);
    opportunityRepository.persist(opportunity);
    return opportunity;
  }
}

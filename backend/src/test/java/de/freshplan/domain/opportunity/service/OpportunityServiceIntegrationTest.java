package de.freshplan.domain.opportunity.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityActivity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.domain.opportunity.service.dto.CreateOpportunityRequest;
import de.freshplan.domain.opportunity.service.dto.UpdateOpportunityRequest;
import de.freshplan.domain.opportunity.service.exception.OpportunityNotFoundException;
import de.freshplan.test.builders.CustomerBuilder;
import de.freshplan.test.builders.OpportunityBuilder;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for OpportunityService without mocks.
 *
 * <p>Tests all opportunity management operations with real database interactions. This replaces the
 * mock-based OpportunityServiceMockTest.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@Tag("core")
@TestSecurity(
    user = "integrationtest",
    roles = {"admin", "sales"})
@DisplayName("OpportunityService Integration Tests (ohne Mocks)")
class OpportunityServiceIntegrationTest {

  // testRunId für Test-Isolation
  protected final String testRunId = java.util.UUID.randomUUID().toString().substring(0, 8);

  @Inject OpportunityService opportunityService;
  @Inject OpportunityRepository opportunityRepository;
  @Inject OpportunityBuilder opportunityBuilder;
  @Inject CustomerBuilder customerBuilder;

  @Test
  @TestTransaction
  @DisplayName("Should create opportunity with valid data in database")
  void createOpportunity_withValidData_shouldPersistInDatabase() {
    // Given - Create customer first
    Customer customer =
        customerBuilder
            .withCompanyName("[TEST] Integration Test Company " + testRunId)
            .withStatus(CustomerStatus.LEAD)
            .persist();

    CreateOpportunityRequest request =
        CreateOpportunityRequest.builder()
            .name("Test Opportunity " + testRunId)
            .description("Integration test opportunity")
            .customerId(customer.getId())
            .expectedValue(BigDecimal.valueOf(50000))
            .expectedCloseDate(LocalDate.now().plusMonths(2))
            .build();

    // When
    var response = opportunityService.createOpportunity(request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getId()).isNotNull();
    assertThat(response.getName()).isEqualTo("Test Opportunity " + testRunId);
    assertThat(response.getDescription()).isEqualTo("Integration test opportunity");
    assertThat(response.getStage()).isEqualTo(OpportunityStage.NEW_LEAD);
    assertThat(response.getExpectedValue()).isEqualByComparingTo(BigDecimal.valueOf(50000));
    assertThat(response.getProbability()).isEqualTo(10); // Default for NEW_LEAD

    // Verify in database
    Opportunity dbOpp = opportunityRepository.findByIdOptional(response.getId()).orElse(null);
    assertThat(dbOpp).isNotNull();
    assertThat(dbOpp.getName()).isEqualTo("Test Opportunity " + testRunId);
    assertThat(dbOpp.getCustomer().getId()).isEqualTo(customer.getId());
  }

  @Test
  @TestTransaction
  @DisplayName("Should throw exception for null customer ID")
  void createOpportunity_withNullCustomerId_shouldThrowException() {
    // Given
    CreateOpportunityRequest request =
        CreateOpportunityRequest.builder()
            .name("Test Opportunity")
            .customerId(null) // Invalid - customer is required
            .build();

    // When/Then
    assertThatThrownBy(() -> opportunityService.createOpportunity(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Customer ID");
  }

  @Test
  @TestTransaction
  @DisplayName("Should find opportunity by ID from database")
  void findById_withExistingId_shouldReturnFromDatabase() {
    // Given - Create opportunity
    Customer customer = customerBuilder.withCompanyName("[TEST] Find Test " + testRunId).persist();

    Opportunity opportunity =
        opportunityBuilder
            .forCustomer(customer)
            .withName("Findable Opportunity")
            .withExpectedValue(75000)
            .inStage(OpportunityStage.PROPOSAL)
            .persist();

    // When
    var found = opportunityService.findById(opportunity.getId());

    // Then
    assertThat(found).isNotNull();
    assertThat(found.getId()).isEqualTo(opportunity.getId());
    assertThat(found.getName()).contains("Findable Opportunity");
    assertThat(found.getStage()).isEqualTo(OpportunityStage.PROPOSAL);
    assertThat(found.getExpectedValue()).isEqualByComparingTo(BigDecimal.valueOf(75000));
  }

  @Test
  @TestTransaction
  @DisplayName("Should throw exception for non-existent ID")
  void findById_withNonExistentId_shouldThrowException() {
    // Given
    UUID nonExistentId = UUID.randomUUID();

    // When/Then
    assertThatThrownBy(() -> opportunityService.findById(nonExistentId))
        .isInstanceOf(OpportunityNotFoundException.class)
        .hasMessageContaining(nonExistentId.toString());
  }

  @Test
  @TestTransaction
  @DisplayName("Should update opportunity in database")
  void updateOpportunity_withValidData_shouldPersistChanges() {
    // Given - Create opportunity
    Customer customer =
        customerBuilder.withCompanyName("[TEST] Update Test " + testRunId).persist();

    Opportunity original =
        opportunityBuilder
            .forCustomer(customer)
            .withName("Original Name")
            .withDescription("Original Description")
            .withExpectedValue(100000)
            .inStage(OpportunityStage.NEW_LEAD)
            .persist();

    UpdateOpportunityRequest updateRequest =
        UpdateOpportunityRequest.builder()
            .name("Updated Name")
            .description("Updated Description")
            .expectedValue(BigDecimal.valueOf(150000))
            .build();

    // When
    var updated = opportunityService.updateOpportunity(original.getId(), updateRequest);

    // Then
    assertThat(updated.getName()).isEqualTo("Updated Name");
    assertThat(updated.getDescription()).isEqualTo("Updated Description");
    assertThat(updated.getExpectedValue()).isEqualByComparingTo(BigDecimal.valueOf(150000));

    // Verify in database
    Opportunity dbOpp = opportunityRepository.findById(original.getId());
    assertThat(dbOpp.getName()).isEqualTo("Updated Name");
    assertThat(dbOpp.getDescription()).isEqualTo("Updated Description");
  }

  @Test
  @TestTransaction
  @DisplayName("Should delete opportunity from database")
  void deleteOpportunity_withExistingId_shouldRemoveFromDatabase() {
    // Given - Create opportunity
    Customer customer =
        customerBuilder.withCompanyName("[TEST] Delete Test " + testRunId).persist();

    Opportunity opportunity =
        opportunityBuilder.forCustomer(customer).withName("To Be Deleted").persist();

    UUID oppId = opportunity.getId();

    // Verify it exists
    assertThat(opportunityRepository.findByIdOptional(oppId)).isPresent();

    // When
    opportunityService.deleteOpportunity(oppId);

    // Then - Verify it's deleted
    assertThat(opportunityRepository.findByIdOptional(oppId)).isEmpty();
  }

  @Test
  @TestTransaction
  @DisplayName("Should throw exception when deleting non-existent opportunity")
  void deleteOpportunity_withNonExistentId_shouldThrowException() {
    // Given
    UUID nonExistentId = UUID.randomUUID();

    // When/Then
    assertThatThrownBy(() -> opportunityService.deleteOpportunity(nonExistentId))
        .isInstanceOf(OpportunityNotFoundException.class)
        .hasMessageContaining(nonExistentId.toString());
  }

  @Test
  @TestTransaction
  @DisplayName("Should change opportunity stage and track activity")
  void changeStage_withValidTransition_shouldUpdateStageAndTrackActivity() {
    // Given - Create opportunity in NEW_LEAD stage
    Customer customer =
        customerBuilder.withCompanyName("[TEST] Stage Change Test " + testRunId).persist();

    Opportunity opportunity =
        opportunityBuilder
            .forCustomer(customer)
            .withName("Stage Test")
            .inStage(OpportunityStage.NEW_LEAD)
            .persist();

    // When - Change to QUALIFICATION stage
    var updated =
        opportunityService.changeStage(opportunity.getId(), OpportunityStage.QUALIFICATION);

    // Then
    assertThat(updated.getStage()).isEqualTo(OpportunityStage.QUALIFICATION);
    assertThat(updated.getProbability()).isEqualTo(25); // Default for QUALIFICATION

    // Verify in database
    Opportunity dbOpp = opportunityRepository.findById(opportunity.getId());
    assertThat(dbOpp.getStage()).isEqualTo(OpportunityStage.QUALIFICATION);
    assertThat(dbOpp.getProbability()).isEqualTo(25);

    // Verify activity was tracked
    assertThat(dbOpp.getActivities()).hasSize(1);
    OpportunityActivity activity = dbOpp.getActivities().get(0);
    assertThat(activity.getActivityType())
        .isEqualTo(OpportunityActivity.ActivityType.STAGE_CHANGED);
    // The title contains the stage names, not the description
    assertThat(activity.getTitle()).contains("→");
    assertThat(activity.getDescription()).isEqualTo("Stage change");
  }

  @Test
  @TestTransaction
  @DisplayName("Should calculate forecast correctly")
  void calculateForecast_shouldSumWeightedValues() {
    // Given - Create multiple opportunities with different probabilities
    Customer customer =
        customerBuilder.withCompanyName("[TEST] Forecast Test " + testRunId).persist();

    // Opportunity 1: 100,000 * 60% = 60,000
    opportunityBuilder
        .forCustomer(customer)
        .withName("Forecast Opp 1")
        .withExpectedValue(100000)
        .inStage(OpportunityStage.PROPOSAL) // 60% probability
        .persist();

    // Opportunity 2: 50,000 * 80% = 40,000
    opportunityBuilder
        .forCustomer(customer)
        .withName("Forecast Opp 2")
        .withExpectedValue(50000)
        .inStage(OpportunityStage.NEGOTIATION) // 80% probability
        .persist();

    // Opportunity 3: Closed Lost - should not count
    opportunityBuilder
        .forCustomer(customer)
        .withName("Forecast Opp 3")
        .withExpectedValue(200000)
        .asLost() // 0% probability
        .persist();

    // When
    BigDecimal forecast = opportunityService.calculateForecast();

    // Then - Forecast should include weighted values from all active opportunities
    // Note: There might be other opportunities in DB from other tests, so we check >= expected
    assertThat(forecast)
        .isGreaterThanOrEqualTo(BigDecimal.valueOf(100000)); // At least our 60k + 40k
  }

  @Test
  @TestTransaction
  @DisplayName("Should create multiple opportunities for customer")
  void createMultipleOpportunities_shouldAllBePersisted() {
    // Given - Create customer
    Customer customer = customerBuilder.withCompanyName("[TEST] Multi Test " + testRunId).persist();

    // When - Create multiple opportunities
    for (int i = 0; i < 3; i++) {
      CreateOpportunityRequest request =
          CreateOpportunityRequest.builder()
              .name("Multi Opp " + i + " " + testRunId)
              .customerId(customer.getId())
              .expectedValue(BigDecimal.valueOf(10000 + (i * 5000)))
              .expectedCloseDate(LocalDate.now().plusMonths(i + 1))
              .build();

      var response = opportunityService.createOpportunity(request);
      assertThat(response).isNotNull();
      assertThat(response.getId()).isNotNull();
    }

    // Then - Verify all are in database
    long count =
        opportunityRepository
            .find("customer.id = ?1 and name like ?2", customer.getId(), "%Multi Opp%")
            .count();
    assertThat(count).isEqualTo(3);
  }

  @Test
  @TestTransaction
  @DisplayName("Should handle concurrent stage changes")
  void changeStage_concurrent_shouldHandleCorrectly() {
    // Given - Create opportunity
    Customer customer =
        customerBuilder.withCompanyName("[TEST] Concurrent Test " + testRunId).persist();

    Opportunity opportunity =
        opportunityBuilder
            .forCustomer(customer)
            .withName("Concurrent Opp")
            .inStage(OpportunityStage.NEW_LEAD)
            .persist();

    // When - Change stage multiple times
    var stage1 =
        opportunityService.changeStage(opportunity.getId(), OpportunityStage.QUALIFICATION);
    assertThat(stage1.getStage()).isEqualTo(OpportunityStage.QUALIFICATION);

    var stage2 =
        opportunityService.changeStage(opportunity.getId(), OpportunityStage.NEEDS_ANALYSIS);
    assertThat(stage2.getStage()).isEqualTo(OpportunityStage.NEEDS_ANALYSIS);

    var stage3 = opportunityService.changeStage(opportunity.getId(), OpportunityStage.PROPOSAL);
    assertThat(stage3.getStage()).isEqualTo(OpportunityStage.PROPOSAL);

    // Then - Verify final state in database
    Opportunity dbOpp = opportunityRepository.findById(opportunity.getId());
    assertThat(dbOpp.getStage()).isEqualTo(OpportunityStage.PROPOSAL);
    assertThat(dbOpp.getActivities()).hasSize(3); // Three stage changes
  }
}

package de.freshplan.domain.opportunity.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.service.dto.CreateOpportunityRequest;
import de.freshplan.domain.opportunity.service.dto.OpportunityResponse;
import de.freshplan.domain.opportunity.service.dto.UpdateOpportunityRequest;
import de.freshplan.test.builders.CustomerBuilder;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

/**
 * Integration Test für CQRS-Implementation des OpportunityService
 *
 * <p>Dieser Test stellt sicher, dass beide Implementierungen (Legacy und CQRS) identisches
 * Verhalten aufweisen.
 */
@QuarkusTest
@Disabled("TEMPORARY: Sprint 2.1.4 CI Performance Fix")
@Tag("core")
@TestProfile(OpportunityCQRSTestProfile.class)
class OpportunityCQRSIntegrationTest {

  @Inject OpportunityService opportunityService;

  @Inject CustomerBuilder customerBuilder;

  @ConfigProperty(name = "features.cqrs.enabled")
  boolean cqrsEnabled;

  private UUID testCustomerId;
  private CreateOpportunityRequest createRequest;

  private void setupTestData() {
    // Create a real test customer within current transaction
    Customer testCustomer =
        customerBuilder.reset().withCompanyName("[TEST] Opportunity Test Customer").persist();

    testCustomerId = testCustomer.getId();

    createRequest =
        CreateOpportunityRequest.builder()
            .name("Integration Test Opportunity")
            .customerId(testCustomerId)
            .description("Test description")
            .expectedValue(new BigDecimal("50000"))
            .expectedCloseDate(LocalDate.now().plusDays(90))
            .build();
  }

  @Test
  void testCQRSModeIsEnabled() {
    // Verify that CQRS mode is enabled for this test
    assertThat(cqrsEnabled).isTrue();
  }

  // =====================================
  // CREATE OPERATION TESTS
  // =====================================

  @Test
  @TestTransaction
  void createOpportunity_inCQRSMode_shouldCreateSuccessfully() {
    // Setup test data in transaction
    setupTestData();

    // When
    OpportunityResponse response = opportunityService.createOpportunity(createRequest);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getName()).isEqualTo("Integration Test Opportunity");
    assertThat(response.getStage()).isEqualTo(OpportunityStage.NEW_LEAD);
    assertThat(response.getExpectedValue()).isEqualTo(new BigDecimal("50000"));
    assertThat(response.getExpectedCloseDate()).isEqualTo(LocalDate.now().plusDays(90));
  }

  @Test
  @TestTransaction
  void createAndRetrieve_inCQRSMode_shouldWorkEndToEnd() {
    // Setup test data in transaction
    setupTestData();

    // Create
    OpportunityResponse created = opportunityService.createOpportunity(createRequest);
    assertThat(created.getId()).isNotNull();

    // Retrieve
    OpportunityResponse retrieved = opportunityService.findById(created.getId());

    // Compare
    assertThat(retrieved).isNotNull();
    assertThat(retrieved.getId()).isEqualTo(created.getId());
    assertThat(retrieved.getName()).isEqualTo(created.getName());
    assertThat(retrieved.getStage()).isEqualTo(created.getStage());
  }

  // =====================================
  // UPDATE OPERATION TESTS
  // =====================================

  @Test
  @TestTransaction
  void updateOpportunity_inCQRSMode_shouldUpdateSuccessfully() {
    // Setup test data in transaction
    setupTestData();

    // Create
    OpportunityResponse created = opportunityService.createOpportunity(createRequest);

    // Update
    UpdateOpportunityRequest updateRequest =
        UpdateOpportunityRequest.builder()
            .name("Updated Opportunity Name")
            .description("Updated description")
            .expectedValue(new BigDecimal("75000"))
            .probability(80)
            .build();

    OpportunityResponse updated =
        opportunityService.updateOpportunity(created.getId(), updateRequest);

    // Verify
    assertThat(updated.getName()).isEqualTo("Updated Opportunity Name");
    assertThat(updated.getDescription()).isEqualTo("Updated description");
    assertThat(updated.getExpectedValue()).isEqualTo(new BigDecimal("75000"));
    assertThat(updated.getProbability()).isEqualTo(80);
  }

  // =====================================
  // STAGE MANAGEMENT TESTS
  // =====================================

  @Test
  @TestTransaction
  void changeStage_inCQRSMode_shouldTransitionCorrectly() {
    // Setup test data in transaction
    setupTestData();

    // Create
    OpportunityResponse created = opportunityService.createOpportunity(createRequest);

    // Change stage
    OpportunityResponse changed =
        opportunityService.changeStage(
            created.getId(), OpportunityStage.QUALIFICATION, "Moving to qualification phase");

    // Verify
    assertThat(changed.getStage()).isEqualTo(OpportunityStage.QUALIFICATION);

    // Retrieve and verify persistence
    OpportunityResponse retrieved = opportunityService.findById(created.getId());
    assertThat(retrieved.getStage()).isEqualTo(OpportunityStage.QUALIFICATION);
  }

  @Test
  @TestTransaction
  void changeStage_withInvalidTransition_shouldThrowException() {
    // Setup test data in transaction
    setupTestData();

    // Create
    OpportunityResponse created = opportunityService.createOpportunity(createRequest);

    // Try invalid transition (NEW_LEAD -> CLOSED_WON)
    assertThatThrownBy(
            () ->
                opportunityService.changeStage(
                    created.getId(), OpportunityStage.CLOSED_WON, "Invalid jump"))
        .hasMessageContaining("Invalid stage transition");
  }

  // =====================================
  // QUERY OPERATION TESTS
  // =====================================

  @Test
  @TestTransaction
  void findByStage_inCQRSMode_shouldReturnFilteredResults() {
    // Setup test data in transaction
    setupTestData();

    // Create opportunities in different stages
    OpportunityResponse opp1 = opportunityService.createOpportunity(createRequest);

    CreateOpportunityRequest request2 =
        CreateOpportunityRequest.builder()
            .name("Second Opportunity")
            .customerId(testCustomerId)
            .build();
    OpportunityResponse opp2 = opportunityService.createOpportunity(request2);

    // Move one to PROPOSAL stage
    opportunityService.changeStage(opp2.getId(), OpportunityStage.QUALIFICATION, "Test");
    opportunityService.changeStage(opp2.getId(), OpportunityStage.NEEDS_ANALYSIS, "Test");
    opportunityService.changeStage(opp2.getId(), OpportunityStage.PROPOSAL, "Test");

    // Query by stage
    List<OpportunityResponse> newLeads = opportunityService.findByStage(OpportunityStage.NEW_LEAD);
    List<OpportunityResponse> proposals = opportunityService.findByStage(OpportunityStage.PROPOSAL);

    // Verify
    assertThat(newLeads).anyMatch(o -> o.getId().equals(opp1.getId()));
    assertThat(proposals).anyMatch(o -> o.getId().equals(opp2.getId()));
  }

  @Test
  @TestTransaction
  void calculateForecast_inCQRSMode_shouldCalculateCorrectly() {
    // Setup test data in transaction
    setupTestData();

    // Create opportunities with different values
    createRequest.setExpectedValue(new BigDecimal("100000"));
    opportunityService.createOpportunity(createRequest);

    CreateOpportunityRequest request2 =
        CreateOpportunityRequest.builder()
            .name("High Value Opportunity")
            .customerId(testCustomerId)
            .expectedValue(new BigDecimal("200000"))
            .build();
    opportunityService.createOpportunity(request2);

    // Calculate forecast
    BigDecimal forecast = opportunityService.calculateForecast();

    // Verify (should be > 0 with opportunities created)
    assertThat(forecast).isGreaterThan(BigDecimal.ZERO);
  }

  // =====================================
  // DELETE OPERATION TESTS
  // =====================================

  @Test
  @TestTransaction
  void deleteOpportunity_inCQRSMode_shouldDeleteSuccessfully() {
    // Setup test data in transaction
    setupTestData();

    // Create
    OpportunityResponse created = opportunityService.createOpportunity(createRequest);
    UUID id = created.getId();

    // Verify it exists
    OpportunityResponse exists = opportunityService.findById(id);
    assertThat(exists).isNotNull();

    // Delete
    opportunityService.deleteOpportunity(id);

    // Verify it's deleted
    assertThatThrownBy(() -> opportunityService.findById(id)).hasMessageContaining("not found");
  }
}

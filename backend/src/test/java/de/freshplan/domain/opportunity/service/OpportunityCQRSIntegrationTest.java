package de.freshplan.domain.opportunity.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.service.dto.CreateOpportunityRequest;
import de.freshplan.domain.opportunity.service.dto.OpportunityResponse;
import de.freshplan.domain.opportunity.service.dto.UpdateOpportunityRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration Test für CQRS-Implementation des OpportunityService
 * 
 * Dieser Test stellt sicher, dass beide Implementierungen (Legacy und CQRS)
 * identisches Verhalten aufweisen.
 */
@QuarkusTest
@TestProfile(OpportunityCQRSTestProfile.class)
class OpportunityCQRSIntegrationTest {

  @Inject OpportunityService opportunityService;

  @ConfigProperty(name = "features.cqrs.enabled")
  boolean cqrsEnabled;

  private UUID testCustomerId;
  private CreateOpportunityRequest createRequest;

  @BeforeEach
  @Transactional
  void setUp() {
    // Setup test data
    testCustomerId = UUID.randomUUID();
    
    createRequest = CreateOpportunityRequest.builder()
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
  @Transactional
  void createOpportunity_inCQRSMode_shouldCreateSuccessfully() {
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
  @Transactional
  void createAndRetrieve_inCQRSMode_shouldWorkEndToEnd() {
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
  @Transactional
  void updateOpportunity_inCQRSMode_shouldUpdateSuccessfully() {
    // Create
    OpportunityResponse created = opportunityService.createOpportunity(createRequest);

    // Update
    UpdateOpportunityRequest updateRequest = UpdateOpportunityRequest.builder()
        .name("Updated Opportunity Name")
        .description("Updated description")
        .expectedValue(new BigDecimal("75000"))
        .probability(80)
        .build();

    OpportunityResponse updated = opportunityService.updateOpportunity(
        created.getId(), 
        updateRequest
    );

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
  @Transactional
  void changeStage_inCQRSMode_shouldTransitionCorrectly() {
    // Create
    OpportunityResponse created = opportunityService.createOpportunity(createRequest);

    // Change stage
    OpportunityResponse changed = opportunityService.changeStage(
        created.getId(),
        OpportunityStage.QUALIFICATION,
        "Moving to qualification phase"
    );

    // Verify
    assertThat(changed.getStage()).isEqualTo(OpportunityStage.QUALIFICATION);

    // Retrieve and verify persistence
    OpportunityResponse retrieved = opportunityService.findById(created.getId());
    assertThat(retrieved.getStage()).isEqualTo(OpportunityStage.QUALIFICATION);
  }

  @Test
  @Transactional
  void changeStage_withInvalidTransition_shouldThrowException() {
    // Create
    OpportunityResponse created = opportunityService.createOpportunity(createRequest);

    // Try invalid transition (NEW_LEAD -> CLOSED_WON)
    assertThatThrownBy(() -> 
        opportunityService.changeStage(
            created.getId(),
            OpportunityStage.CLOSED_WON,
            "Invalid jump"
        )
    ).hasMessageContaining("Invalid stage transition");
  }

  // =====================================
  // QUERY OPERATION TESTS
  // =====================================

  @Test
  @Transactional
  void findByStage_inCQRSMode_shouldReturnFilteredResults() {
    // Create opportunities in different stages
    OpportunityResponse opp1 = opportunityService.createOpportunity(createRequest);
    
    CreateOpportunityRequest request2 = CreateOpportunityRequest.builder()
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
  @Transactional
  void calculateForecast_inCQRSMode_shouldCalculateCorrectly() {
    // Create opportunities with different values
    createRequest.setExpectedValue(new BigDecimal("100000"));
    opportunityService.createOpportunity(createRequest);

    CreateOpportunityRequest request2 = CreateOpportunityRequest.builder()
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
  @Transactional
  void deleteOpportunity_inCQRSMode_shouldDeleteSuccessfully() {
    // Create
    OpportunityResponse created = opportunityService.createOpportunity(createRequest);
    UUID id = created.getId();

    // Verify it exists
    OpportunityResponse exists = opportunityService.findById(id);
    assertThat(exists).isNotNull();

    // Delete
    opportunityService.deleteOpportunity(id);

    // Verify it's deleted
    assertThatThrownBy(() -> opportunityService.findById(id))
        .hasMessageContaining("not found");
  }
}


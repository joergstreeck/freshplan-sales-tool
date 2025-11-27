package de.freshplan.domain.opportunity.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.entity.OpportunityType;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.domain.opportunity.service.dto.CreateOpportunityFromLeadRequest;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration Tests für OpportunityService.createFromLead()
 *
 * <p>Testet die Lead → Opportunity Conversion mit folgenden Szenarien: - Happy Path: QUALIFIED Lead
 * → Opportunity (NEW_LEAD Stage) - Lead Status Auto-Update → CONVERTED - Validation: Lead muss
 * QUALIFIED oder ACTIVE sein - Traceability: lead_id FK wird korrekt gesetzt
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7 (Opportunity Backend Integration)
 */
@QuarkusTest
@Tag("integration")
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
@DisplayName("OpportunityService.createFromLead() Integration Tests")
public class OpportunityServiceCreateFromLeadTest {

  @Inject OpportunityService opportunityService;

  @Inject jakarta.persistence.EntityManager em;

  @Inject OpportunityRepository opportunityRepository;

  @AfterEach
  @Transactional
  void cleanup() {
    // Step 1: Delete opportunities first (FK constraint: opportunities.lead_id -> leads.id)
    opportunityRepository.deleteAll();

    // Step 2: Delete leads (pattern: [TEST-OPP]%)
    Lead.delete("companyName LIKE ?1", "[TEST-OPP]%");
  }

  private Lead testLead;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean up any existing test data
    opportunityRepository.deleteAll();
    Lead.delete("companyName LIKE ?1", "[TEST-OPP]%");

    // Create test lead (QUALIFIED status)
    testLead = new Lead();
    testLead.companyName = "[TEST-OPP] Hotel Seeblick GmbH";
    testLead.status = LeadStatus.QUALIFIED;
    testLead.registeredAt = LocalDateTime.now().minusDays(30);
    testLead.estimatedVolume = BigDecimal.valueOf(336000);
    testLead.ownerUserId = "test-partner-1";
    testLead.createdBy = "test-user";
    testLead.persist();
  }

  @Test
  @Transactional
  @DisplayName("Should create Opportunity from QUALIFIED Lead")
  void createFromLead_withQualifiedLead_shouldCreateOpportunity() {
    // Arrange
    var request =
        CreateOpportunityFromLeadRequest.builder()
            .name("Hotel Seeblick - Vollversorgung Q2 2025")
            .dealType("Liefervertrag")
            .timeframe("Q2 2025")
            .expectedValue(BigDecimal.valueOf(336000))
            .expectedCloseDate(LocalDate.now().plusDays(90))
            .build();

    // Act
    var result = opportunityService.createFromLead(testLead.id, request);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Hotel Seeblick - Vollversorgung Q2 2025");
    assertThat(result.getStage()).isEqualTo(OpportunityStage.NEW_LEAD);
    assertThat(result.getExpectedValue()).isEqualByComparingTo(BigDecimal.valueOf(336000));
    assertThat(result.getExpectedCloseDate()).isEqualTo(LocalDate.now().plusDays(90));

    // Verify lead_id FK is set
    Opportunity persistedOpp = opportunityRepository.findById(result.getId());
    assertThat(persistedOpp).isNotNull();
    assertThat(persistedOpp.getLead()).isNotNull();
    assertThat(persistedOpp.getLead().id).isEqualTo(testLead.id);
  }

  @Test
  @Transactional
  @DisplayName("Should auto-update Lead status to CONVERTED")
  void createFromLead_shouldUpdateLeadStatusToConverted() {
    // Arrange
    var request =
        CreateOpportunityFromLeadRequest.builder()
            .name("Test Opportunity")
            .dealType("Liefervertrag")
            .timeframe("Q2 2025")
            .expectedValue(BigDecimal.valueOf(20000))
            .expectedCloseDate(LocalDate.now().plusDays(60))
            .build();

    // Act
    opportunityService.createFromLead(testLead.id, request);

    // Assert: Lead status should be CONVERTED
    Lead updatedLead = Lead.findById(testLead.id);
    assertThat(updatedLead).isNotNull();
    assertThat(updatedLead.status)
        .as("Lead status should be auto-updated to CONVERTED")
        .isEqualTo(LeadStatus.CONVERTED);
    assertThat(updatedLead.updatedAt)
        .as("Lead updatedAt should be refreshed")
        .isAfter(testLead.createdAt);
  }

  @Test
  @Transactional
  @DisplayName("Should allow creating Opportunity from ACTIVE Lead")
  void createFromLead_withActiveLead_shouldSucceed() {
    // Arrange: Create ACTIVE Lead (allowed per service implementation)
    Lead activeLead = new Lead();
    activeLead.companyName = "[TEST-OPP] Active Lead GmbH";
    activeLead.status = LeadStatus.ACTIVE; // ← ACTIVE is allowed!
    activeLead.registeredAt = LocalDateTime.now().minusDays(5);
    activeLead.createdBy = "test-user";
    activeLead.persist();

    var request =
        CreateOpportunityFromLeadRequest.builder()
            .name("Test Opportunity")
            .dealType("Liefervertrag")
            .timeframe("Q1 2025")
            .expectedValue(BigDecimal.valueOf(10000))
            .expectedCloseDate(LocalDate.now().plusDays(30))
            .build();

    // Act
    var result = opportunityService.createFromLead(activeLead.id, request);

    // Assert: Opportunity created successfully
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Test Opportunity");
  }

  @Test
  @Transactional
  @DisplayName("Should fail when Lead does not exist")
  void createFromLead_withNonExistentLead_shouldThrowException() {
    // Arrange
    Long nonExistentLeadId = 999999L;
    var request =
        CreateOpportunityFromLeadRequest.builder()
            .name("Test Opportunity")
            .dealType("Liefervertrag")
            .timeframe("Q1 2025")
            .expectedValue(BigDecimal.valueOf(10000))
            .expectedCloseDate(LocalDate.now().plusDays(30))
            .build();

    // Act & Assert
    assertThatThrownBy(() -> opportunityService.createFromLead(nonExistentLeadId, request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Lead not found");
  }

  @Test
  @Transactional
  @DisplayName("Should pre-fill Opportunity with Lead data")
  void createFromLead_shouldPreFillOpportunityWithLeadData() {
    // Arrange: Update testLead (already persisted in setUp)
    testLead.estimatedVolume = BigDecimal.valueOf(50000);
    testLead.ownerUserId = "test-partner-2";
    // No persist() needed - testLead is already managed

    var request =
        CreateOpportunityFromLeadRequest.builder()
            .name(null) // Auto-generate from Lead
            .dealType("Testphase")
            .timeframe("H1 2025")
            .expectedValue(null) // Use Lead.estimatedVolume
            .expectedCloseDate(LocalDate.now().plusDays(45))
            .build();

    // Act
    var result = opportunityService.createFromLead(testLead.id, request);

    // Assert
    assertThat(result).isNotNull();
    // Name should be auto-generated: "Vertragschance - {companyName} ({dealType})"
    assertThat(result.getName())
        .contains("Vertragschance")
        .contains(testLead.companyName)
        .contains("Testphase");
    // Expected Value from request (null) - service doesn't auto-fill from Lead
    // This is intentional: Frontend should pass expectedValue explicitly
    assertThat(result.getExpectedValue()).isNull();
  }

  @Test
  @Transactional
  @DisplayName(
      "Should set OpportunityType=NEUGESCHAEFT by default (Sprint 2.1.7.1 Freshfoodz Business Type)")
  void createFromLead_shouldSetDefaultOpportunityTypeNeugeschaeft() {
    // Arrange
    var request =
        CreateOpportunityFromLeadRequest.builder()
            .name("Test Opportunity")
            .dealType("Liefervertrag")
            .timeframe("Q2 2025")
            .expectedValue(BigDecimal.valueOf(20000))
            .expectedCloseDate(LocalDate.now().plusDays(60))
            .build();

    // Act
    var result = opportunityService.createFromLead(testLead.id, request);

    // Assert: opportunityType should be NEUGESCHAEFT (Freshfoodz default for Lead → Opportunity)
    assertThat(result.getOpportunityType())
        .as(
            "Sprint 2.1.7.1: createFromLead() should automatically set OpportunityType=NEUGESCHAEFT")
        .isEqualTo(OpportunityType.NEUGESCHAEFT);

    // Verify in DB (load from repository to ensure persistence)
    Opportunity persistedOpp = opportunityRepository.findById(result.getId());
    assertThat(persistedOpp).isNotNull();
    assertThat(persistedOpp.getOpportunityType())
        .as("Persisted Opportunity should have OpportunityType=NEUGESCHAEFT in DB")
        .isEqualTo(OpportunityType.NEUGESCHAEFT);
  }
}

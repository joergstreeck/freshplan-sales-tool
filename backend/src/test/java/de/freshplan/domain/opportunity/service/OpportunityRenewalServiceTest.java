package de.freshplan.domain.opportunity.service;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for RENEWAL stage business logic (Enum validation).
 *
 * <p>Tests contract renewal workflows, stage transitions, and business rules for the 7th Kanban
 * column (RENEWAL stage).
 *
 * <p><b>Migration:</b> Converted from @QuarkusTest to plain JUnit (saves ~15s per test run)
 *
 * <p>This test validates ENUM logic and entity properties without requiring Quarkus boot or
 * database operations.
 */
@Tag("unit")
@DisplayName("OpportunityService - RENEWAL Stage")
class OpportunityRenewalServiceTest {

  // ==================== RENEWAL STAGE BUSINESS LOGIC ====================

  @Test
  @DisplayName("Should identify RENEWAL stage as active and renewal")
  void shouldIdentifyRenewalStageAsActiveAndRenewal() {
    // Act & Assert
    assertThat(OpportunityStage.RENEWAL.isActive()).isTrue();
    assertThat(OpportunityStage.RENEWAL.isRenewal()).isTrue();
    assertThat(OpportunityStage.RENEWAL.isClosed()).isFalse();
  }

  @Test
  @DisplayName("Should have correct default probability for RENEWAL stage")
  void shouldHaveCorrectDefaultProbabilityForRenewal() {
    // Act & Assert
    assertThat(OpportunityStage.RENEWAL.getDefaultProbability()).isEqualTo(75);
  }

  @Test
  @DisplayName("Should allow correct stage transitions to/from RENEWAL")
  void shouldAllowCorrectStageTransitionsToFromRenewal() {
    // Assert transitions TO RENEWAL
    assertThat(OpportunityStage.CLOSED_WON.canTransitionTo(OpportunityStage.RENEWAL))
        .as("CLOSED_WON should transition to RENEWAL")
        .isTrue();

    // Assert transitions FROM RENEWAL
    assertThat(OpportunityStage.RENEWAL.canTransitionTo(OpportunityStage.CLOSED_WON))
        .as("RENEWAL should transition to CLOSED_WON")
        .isTrue();
    assertThat(OpportunityStage.RENEWAL.canTransitionTo(OpportunityStage.CLOSED_LOST))
        .as("RENEWAL should transition to CLOSED_LOST")
        .isTrue();

    // Assert invalid transitions TO RENEWAL
    assertThat(OpportunityStage.NEW_LEAD.canTransitionTo(OpportunityStage.RENEWAL))
        .as("NEW_LEAD should NOT transition to RENEWAL")
        .isFalse();
    assertThat(OpportunityStage.PROPOSAL.canTransitionTo(OpportunityStage.RENEWAL))
        .as("PROPOSAL should NOT transition to RENEWAL")
        .isFalse();

    // Assert invalid transitions FROM RENEWAL
    assertThat(OpportunityStage.RENEWAL.canTransitionTo(OpportunityStage.NEW_LEAD))
        .as("RENEWAL should NOT transition back to NEW_LEAD")
        .isFalse();
    assertThat(OpportunityStage.RENEWAL.canTransitionTo(OpportunityStage.PROPOSAL))
        .as("RENEWAL should NOT transition back to PROPOSAL")
        .isFalse();
  }

  @Test
  @DisplayName("Should include RENEWAL in opportunity stage values")
  void shouldIncludeRenewalInOpportunityStageValues() {
    // Act
    OpportunityStage[] stages = OpportunityStage.values();

    // Assert
    assertThat(stages).contains(OpportunityStage.RENEWAL);
    assertThat(OpportunityStage.valueOf("RENEWAL")).isEqualTo(OpportunityStage.RENEWAL);
  }

  @Test
  @DisplayName("Should have correct RENEWAL stage properties")
  void shouldHaveCorrectRenewalStageProperties() {
    // Act & Assert
    assertThat(OpportunityStage.RENEWAL.name()).isEqualTo("RENEWAL");
    assertThat(OpportunityStage.RENEWAL.getDisplayName()).isEqualTo("Verlängerung");
    assertThat(OpportunityStage.RENEWAL.getColor()).isEqualTo("#ff9800");
    assertThat(OpportunityStage.RENEWAL.getDefaultProbability()).isEqualTo(75);
  }

  // ==================== RENEWAL STAGE VALIDATION ====================

  @Test
  @DisplayName("Should validate RENEWAL stage ordering")
  void shouldValidateRenewalStageOrdering() {
    // RENEWAL sollte nach CLOSED_WON in der Enum-Reihenfolge stehen
    OpportunityStage[] stages = OpportunityStage.values();

    int closedWonIndex = -1;
    int renewalIndex = -1;

    for (int i = 0; i < stages.length; i++) {
      if (stages[i] == OpportunityStage.CLOSED_WON) {
        closedWonIndex = i;
      }
      if (stages[i] == OpportunityStage.RENEWAL) {
        renewalIndex = i;
      }
    }

    assertThat(renewalIndex)
        .isGreaterThan(closedWonIndex)
        .as("RENEWAL should come after CLOSED_WON in enum ordering");
  }

  @Test
  @DisplayName("Should have consistent stage properties")
  void shouldHaveConsistentStageProperties() {
    // RENEWAL sollte konsistente Eigenschaften haben
    assertThat(OpportunityStage.RENEWAL.isActive()).isTrue();
    assertThat(OpportunityStage.RENEWAL.isClosed()).isFalse();
    assertThat(OpportunityStage.RENEWAL.isRenewal()).isTrue();

    // Kein anderer Stage sollte isRenewal() = true haben
    for (OpportunityStage stage : OpportunityStage.values()) {
      if (stage != OpportunityStage.RENEWAL) {
        assertThat(stage.isRenewal())
            .as("Only RENEWAL stage should return true for isRenewal()")
            .isFalse();
      }
    }
  }

  // ==================== RENEWAL STAGE INTEGRATION ====================

  @Test
  @DisplayName("Should support RENEWAL opportunities with correct entity properties")
  void shouldSupportRenewalOpportunitiesWithCorrectProperties() {
    // Given: Create RENEWAL opportunity entity (no DB)
    Opportunity renewalOpp = new Opportunity();
    renewalOpp.setStage(OpportunityStage.RENEWAL);
    renewalOpp.setProbability(75);

    // Then: RENEWAL opportunities have correct properties
    assertThat(renewalOpp.getStage()).isEqualTo(OpportunityStage.RENEWAL);
    assertThat(renewalOpp.getProbability()).isEqualTo(75);
    assertThat(renewalOpp.getStage().isRenewal()).isTrue();
    assertThat(renewalOpp.getStage().isActive()).isTrue();
  }

  @Test
  @DisplayName("Should support RENEWAL in stage enum collection")
  void shouldSupportRenewalInStageEnumCollection() {
    // Test dass RENEWAL Stage in Enum-Collection vorhanden ist

    // Alle Stages durchgehen und prüfen, dass RENEWAL dabei ist
    boolean renewalFound = false;
    for (OpportunityStage stage : OpportunityStage.values()) {
      if (stage == OpportunityStage.RENEWAL) {
        renewalFound = true;
        break;
      }
    }

    assertThat(renewalFound)
        .isTrue()
        .as("RENEWAL stage should be included in OpportunityStage enum");
  }
}

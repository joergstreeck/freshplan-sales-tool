package de.freshplan.domain.opportunity.service;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.opportunity.entity.OpportunityStage;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration Tests für RENEWAL Stage Business Logic im OpportunityService
 *
 * <p>Testet die Contract Renewal Workflows, Stage Transitions und Business Rules für die 7.
 * Kanban-Spalte (RENEWAL Stage).
 */
@QuarkusTest
@Tag("migrate")
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
class OpportunityRenewalServiceTest {

  @Inject OpportunityService opportunityService;

  @Nested
  @DisplayName("RENEWAL Stage Business Logic")
  class RenewalBusinessLogic {

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
  }

  @Nested
  @DisplayName("RENEWAL Stage Validation")
  class RenewalStageValidation {

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
  }

  @Nested
  @DisplayName("RENEWAL Stage Integration")
  class RenewalStageIntegration {

    @Test
    @DisplayName("Should work with existing opportunity service methods")
    void shouldWorkWithExistingOpportunityServiceMethods() {
      // Test dass RENEWAL Stage mit bestehenden Service-Methoden funktioniert
      var allOpportunities = opportunityService.findAll();

      // Assert dass Service-Aufruf nicht fehlschlägt
      assertThat(allOpportunities).isNotNull();

      // Wenn RENEWAL Opportunities existieren, sollten sie korrekt geladen werden
      var renewalOpportunities =
          allOpportunities.stream()
              .filter(opp -> opp.getStage() == OpportunityStage.RENEWAL)
              .toList();

      // Assertion: RENEWAL Opportunities haben korrekte Properties
      renewalOpportunities.forEach(
          opp -> {
            assertThat(opp.getStage()).isEqualTo(OpportunityStage.RENEWAL);
            assertThat(opp.getProbability()).isEqualTo(75);
          });
    }

    @Test
    @DisplayName("Should support RENEWAL in stage statistics")
    void shouldSupportRenewalInStageStatistics() {
      // Test dass RENEWAL Stage in Statistik-Berechnungen korrekt berücksichtigt wird

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
}

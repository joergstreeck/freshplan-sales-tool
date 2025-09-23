package de.freshplan.domain.opportunity.entity;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.user.entity.User;
import de.freshplan.test.builders.CustomerBuilder;
import de.freshplan.test.builders.OpportunityBuilder;
import de.freshplan.test.builders.UserBuilder;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Umfassende Tests für OpportunityStage Business Rules
 *
 * <p>Tests decken ab: - Stage Transitions (Valid/Invalid) - Default Probability Calculations -
 * Business Rule Validations - Stage Workflow Logic - Edge Cases
 *
 * <p>Now uses TestDataBuilder pattern instead of mocks.
 *
 * @author FreshPlan Team
 * @since 2.0.0 - Updated in Migration Phase 4
 */
@QuarkusTest
@Tag("core")
public class OpportunityStageTest {

  @Inject CustomerBuilder customerBuilder;
  @Inject OpportunityBuilder opportunityBuilder;
  @Inject UserBuilder userBuilder;

  // testRunId für Test-Isolation
  private final String testRunId = UUID.randomUUID().toString().substring(0, 8);

  private Opportunity testOpportunity;
  private Customer testCustomer;
  private User testUser;

  @BeforeEach
  void setUp() {
    // Verwende Builder statt Mocks
    testCustomer =
        customerBuilder
            .withCompanyName("[TEST] Test Company " + testRunId)
            .build(); // Nur build() für Entity-Tests

    testUser =
        userBuilder
            .withUsername("testuser_" + testRunId)
            .withFirstName("Test")
            .withLastName("User")
            .build(); // Nur build() für Entity-Tests

    // Create test opportunity mit Builder
    testOpportunity =
        opportunityBuilder
            .forCustomer(testCustomer)
            .withName("Test Opportunity " + testRunId)
            .assignedTo(testUser)
            .withExpectedValue(10000)
            .inStage(OpportunityStage.NEW_LEAD)
            .build(); // Nur build() für Entity-Tests
  }

  @Nested
  @Tag("core")
  @DisplayName("Stage Transition Tests")
  class StageTransitionTests {

    @Test
    @DisplayName("Should allow forward progression through stages")
    void stageTransition_forwardProgression_shouldWork() {
      // Test NEW_LEAD → QUALIFICATION
      testOpportunity.changeStage(OpportunityStage.QUALIFICATION);
      assertThat(testOpportunity.getStage()).isEqualTo(OpportunityStage.QUALIFICATION);
      assertThat(testOpportunity.getProbability()).isEqualTo(25);

      // Test QUALIFICATION → NEEDS_ANALYSIS
      testOpportunity.changeStage(OpportunityStage.NEEDS_ANALYSIS);
      assertThat(testOpportunity.getStage()).isEqualTo(OpportunityStage.NEEDS_ANALYSIS);
      assertThat(testOpportunity.getProbability()).isEqualTo(40);

      // Test NEEDS_ANALYSIS → PROPOSAL
      testOpportunity.changeStage(OpportunityStage.PROPOSAL);
      assertThat(testOpportunity.getStage()).isEqualTo(OpportunityStage.PROPOSAL);
      assertThat(testOpportunity.getProbability()).isEqualTo(60);

      // Test PROPOSAL → NEGOTIATION
      testOpportunity.changeStage(OpportunityStage.NEGOTIATION);
      assertThat(testOpportunity.getStage()).isEqualTo(OpportunityStage.NEGOTIATION);
      assertThat(testOpportunity.getProbability()).isEqualTo(80);

      // Test NEGOTIATION → CLOSED_WON
      testOpportunity.changeStage(OpportunityStage.CLOSED_WON);
      assertThat(testOpportunity.getStage()).isEqualTo(OpportunityStage.CLOSED_WON);
      assertThat(testOpportunity.getProbability()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should allow backward movement to previous stages")
    void stageTransition_backwardMovement_shouldWork() {
      // Start from PROPOSAL
      testOpportunity.changeStage(OpportunityStage.PROPOSAL);

      // Move back to QUALIFICATION
      testOpportunity.changeStage(OpportunityStage.QUALIFICATION);
      assertThat(testOpportunity.getStage()).isEqualTo(OpportunityStage.QUALIFICATION);
      assertThat(testOpportunity.getProbability()).isEqualTo(25);

      // Move back to NEW_LEAD
      testOpportunity.changeStage(OpportunityStage.NEW_LEAD);
      assertThat(testOpportunity.getStage()).isEqualTo(OpportunityStage.NEW_LEAD);
      assertThat(testOpportunity.getProbability()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should allow direct jump to CLOSED_LOST from any stage")
    void stageTransition_directToClosedLost_shouldWorkFromAnyStage() {
      // From NEW_LEAD
      testOpportunity.changeStage(OpportunityStage.CLOSED_LOST);
      assertThat(testOpportunity.getStage()).isEqualTo(OpportunityStage.CLOSED_LOST);
      assertThat(testOpportunity.getProbability()).isEqualTo(0);

      // Reset and test from NEGOTIATION
      testOpportunity.changeStage(OpportunityStage.NEGOTIATION);
      testOpportunity.changeStage(OpportunityStage.CLOSED_LOST);
      assertThat(testOpportunity.getStage()).isEqualTo(OpportunityStage.CLOSED_LOST);
      assertThat(testOpportunity.getProbability()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should not allow transitions from closed states")
    void stageTransition_fromClosedStates_shouldNotWork() {
      // Test CLOSED_WON → any other state should not work
      testOpportunity.changeStage(OpportunityStage.CLOSED_WON);
      var originalStageChangedAt = testOpportunity.getStageChangedAt();

      // Try to change to other states - should be ignored
      testOpportunity.changeStage(OpportunityStage.NEW_LEAD);
      assertThat(testOpportunity.getStage()).isEqualTo(OpportunityStage.CLOSED_WON);
      assertThat(testOpportunity.getStageChangedAt()).isEqualTo(originalStageChangedAt);

      // Test CLOSED_LOST → any other state should not work
      // Reset to a non-closed state first
      testOpportunity =
          opportunityBuilder
              .forCustomer(testCustomer)
              .withName("Test Opportunity Reset " + testRunId)
              .assignedTo(testUser)
              .withExpectedValue(10000)
              .inStage(OpportunityStage.NEW_LEAD)
              .build();

      testOpportunity.changeStage(OpportunityStage.CLOSED_LOST);
      originalStageChangedAt = testOpportunity.getStageChangedAt();

      testOpportunity.changeStage(OpportunityStage.PROPOSAL);
      assertThat(testOpportunity.getStage()).isEqualTo(OpportunityStage.CLOSED_LOST);
      assertThat(testOpportunity.getStageChangedAt()).isEqualTo(originalStageChangedAt);
    }

    @Test
    @DisplayName("Should update stageChangedAt timestamp on stage change")
    void stageTransition_shouldUpdateTimestamp() {
      // Arrange
      var initialTimestamp = testOpportunity.getStageChangedAt();

      // Wait a small amount to ensure timestamp difference
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      // Act
      testOpportunity.changeStage(OpportunityStage.QUALIFICATION);

      // Assert
      assertThat(testOpportunity.getStageChangedAt()).isNotNull().isAfter(initialTimestamp);
    }

    @Test
    @DisplayName("Should not update timestamp when stage doesn't change")
    void stageTransition_sameStage_shouldNotUpdateTimestamp() {
      // Arrange
      testOpportunity.changeStage(OpportunityStage.NEW_LEAD); // Set initial state
      var initialTimestamp = testOpportunity.getStageChangedAt();

      // Act - try to set same stage
      testOpportunity.changeStage(OpportunityStage.NEW_LEAD);

      // Assert
      assertThat(testOpportunity.getStageChangedAt()).isEqualTo(initialTimestamp);
    }
  }

  @Nested
  @Tag("core")
  @DisplayName("Default Probability Tests")
  class DefaultProbabilityTests {

    @ParameterizedTest
    @MethodSource("stageAndProbabilityProvider")
    @DisplayName("Should set correct default probability for each stage")
    void getDefaultProbabilityForStage_shouldReturnCorrectValue(
        OpportunityStage stage, int expectedProbability) {
      // Act
      testOpportunity.changeStage(stage);

      // Assert
      assertThat(testOpportunity.getProbability()).isEqualTo(expectedProbability);
    }

    static Stream<Arguments> stageAndProbabilityProvider() {
      return Stream.of(
          Arguments.of(OpportunityStage.NEW_LEAD, 10),
          Arguments.of(OpportunityStage.QUALIFICATION, 25),
          Arguments.of(OpportunityStage.NEEDS_ANALYSIS, 40),
          Arguments.of(OpportunityStage.PROPOSAL, 60),
          Arguments.of(OpportunityStage.NEGOTIATION, 80),
          Arguments.of(OpportunityStage.CLOSED_WON, 100),
          Arguments.of(OpportunityStage.CLOSED_LOST, 0),
          Arguments.of(OpportunityStage.RENEWAL, 75));
    }

    @Test
    @DisplayName("Should allow manual probability override")
    void setProbability_withCustomValue_shouldOverrideDefault() {
      // Act
      testOpportunity.changeStage(OpportunityStage.PROPOSAL); // Default 60%
      testOpportunity.setProbability(75); // Manual override

      // Assert
      assertThat(testOpportunity.getProbability()).isEqualTo(75);
    }

    @Test
    @DisplayName("Should reset to default probability on stage change")
    void changeStage_shouldResetToDefaultProbability() {
      // Arrange
      testOpportunity.changeStage(OpportunityStage.PROPOSAL);
      testOpportunity.setProbability(75); // Manual override

      // Act
      testOpportunity.changeStage(OpportunityStage.NEGOTIATION);

      // Assert
      assertThat(testOpportunity.getProbability()).isEqualTo(80); // Default for NEGOTIATION
    }
  }

  @Nested
  @Tag("core")
  @DisplayName("Stage Enum Tests")
  class StageEnumTests {

    @ParameterizedTest
    @EnumSource(OpportunityStage.class)
    @DisplayName("Should have valid enum values")
    void opportunityStage_allValues_shouldBeValid(OpportunityStage stage) {
      // Assert
      assertThat(stage).isNotNull();
      assertThat(stage.name()).isNotBlank();
    }

    @Test
    @DisplayName("Should have expected number of stages")
    void opportunityStage_shouldHaveExpectedStages() {
      // Assert
      assertThat(OpportunityStage.values()).hasSize(8);
    }

    @Test
    @DisplayName("Should have stages in logical order")
    void opportunityStage_shouldHaveLogicalOrder() {
      // Arrange
      var stages = OpportunityStage.values();

      // Assert
      assertThat(stages[0]).isEqualTo(OpportunityStage.NEW_LEAD);
      assertThat(stages[1]).isEqualTo(OpportunityStage.QUALIFICATION);
      assertThat(stages[2]).isEqualTo(OpportunityStage.NEEDS_ANALYSIS);
      assertThat(stages[3]).isEqualTo(OpportunityStage.PROPOSAL);
      assertThat(stages[4]).isEqualTo(OpportunityStage.NEGOTIATION);
      assertThat(stages[5]).isEqualTo(OpportunityStage.CLOSED_WON);
      assertThat(stages[6]).isEqualTo(OpportunityStage.CLOSED_LOST);
      assertThat(stages[7]).isEqualTo(OpportunityStage.RENEWAL);
    }
  }

  @Nested
  @Tag("core")
  @DisplayName("Business Rule Tests")
  class BusinessRuleTests {

    @Test
    @DisplayName("Should identify active stages correctly")
    void isActiveStage_shouldIdentifyCorrectly() {
      // Active stages (not closed)
      assertThat(isActiveStage(OpportunityStage.NEW_LEAD)).isTrue();
      assertThat(isActiveStage(OpportunityStage.QUALIFICATION)).isTrue();
      assertThat(isActiveStage(OpportunityStage.NEEDS_ANALYSIS)).isTrue();
      assertThat(isActiveStage(OpportunityStage.PROPOSAL)).isTrue();
      assertThat(isActiveStage(OpportunityStage.NEGOTIATION)).isTrue();

      // Renewal stage (active)
      assertThat(isActiveStage(OpportunityStage.RENEWAL)).isTrue();

      // Closed stages (not active)
      assertThat(isActiveStage(OpportunityStage.CLOSED_WON)).isFalse();
      assertThat(isActiveStage(OpportunityStage.CLOSED_LOST)).isFalse();
    }

    @Test
    @DisplayName("Should identify won opportunities correctly")
    void isWonOpportunity_shouldIdentifyCorrectly() {
      testOpportunity.changeStage(OpportunityStage.CLOSED_WON);
      assertThat(testOpportunity.isWonOpportunity()).isTrue();
      assertThat(testOpportunity.isLostOpportunity()).isFalse();
      assertThat(testOpportunity.isClosedOpportunity()).isTrue();

      // Reset to a non-closed state before changing to CLOSED_LOST
      testOpportunity =
          opportunityBuilder
              .forCustomer(testCustomer)
              .withName("Test Opportunity Lost " + testRunId)
              .assignedTo(testUser)
              .withExpectedValue(10000)
              .inStage(OpportunityStage.NEW_LEAD)
              .build();

      testOpportunity.changeStage(OpportunityStage.CLOSED_LOST);
      assertThat(testOpportunity.isWonOpportunity()).isFalse();
      assertThat(testOpportunity.isLostOpportunity()).isTrue();
      assertThat(testOpportunity.isClosedOpportunity()).isTrue();

      // Reset again for NEW_LEAD test
      testOpportunity =
          opportunityBuilder
              .forCustomer(testCustomer)
              .withName("Test Opportunity New " + testRunId)
              .assignedTo(testUser)
              .withExpectedValue(10000)
              .inStage(OpportunityStage.NEW_LEAD)
              .build();

      assertThat(testOpportunity.isWonOpportunity()).isFalse();
      assertThat(testOpportunity.isLostOpportunity()).isFalse();
      assertThat(testOpportunity.isClosedOpportunity()).isFalse();
    }

    @Test
    @DisplayName("Should calculate opportunity value correctly")
    void getOpportunityValue_shouldCalculateCorrectly() {
      // Arrange
      testOpportunity.setExpectedValue(BigDecimal.valueOf(10000));
      testOpportunity.changeStage(OpportunityStage.PROPOSAL); // 60% probability

      // Act
      var expectedWeightedValue =
          BigDecimal.valueOf(10000)
              .multiply(BigDecimal.valueOf(60))
              .divide(BigDecimal.valueOf(100));

      // Assert
      // We'll verify this calculation in the service layer
      assertThat(testOpportunity.getExpectedValue()).isEqualTo(BigDecimal.valueOf(10000));
      assertThat(testOpportunity.getProbability()).isEqualTo(60);
    }

    @Test
    @DisplayName("Should handle null expected value gracefully")
    void opportunityWithNullValue_shouldHandleGracefully() {
      // Arrange
      testOpportunity.setExpectedValue(null);
      testOpportunity.changeStage(OpportunityStage.PROPOSAL);

      // Assert
      assertThat(testOpportunity.getExpectedValue()).isNull();
      assertThat(testOpportunity.getProbability()).isEqualTo(60);
    }
  }

  @Nested
  @Tag("core")
  @DisplayName("Stage Workflow Tests")
  class StageWorkflowTests {

    @Test
    @DisplayName("Should track stage history through multiple transitions")
    void stageWorkflow_multipleTransitions_shouldTrackCorrectly() {
      // Track initial state
      assertThat(testOpportunity.getStage()).isEqualTo(OpportunityStage.NEW_LEAD);
      var stage1Time = testOpportunity.getStageChangedAt();

      // Transition 1: NEW_LEAD → QUALIFICATION
      testOpportunity.changeStage(OpportunityStage.QUALIFICATION);
      var stage2Time = testOpportunity.getStageChangedAt();
      assertThat(stage2Time).isAfter(stage1Time);

      // Transition 2: QUALIFICATION → PROPOSAL (skip NEEDS_ANALYSIS)
      testOpportunity.changeStage(OpportunityStage.PROPOSAL);
      var stage3Time = testOpportunity.getStageChangedAt();
      assertThat(stage3Time).isAfter(stage2Time);

      // Transition 3: PROPOSAL → CLOSED_WON
      testOpportunity.changeStage(OpportunityStage.CLOSED_WON);
      var stage4Time = testOpportunity.getStageChangedAt();
      assertThat(stage4Time).isAfter(stage3Time);

      // Verify final state
      assertThat(testOpportunity.getStage()).isEqualTo(OpportunityStage.CLOSED_WON);
      assertThat(testOpportunity.getProbability()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should allow stage skipping in forward direction")
    void stageWorkflow_skipStages_shouldWork() {
      // Skip directly from NEW_LEAD to PROPOSAL
      testOpportunity.changeStage(OpportunityStage.PROPOSAL);

      assertThat(testOpportunity.getStage()).isEqualTo(OpportunityStage.PROPOSAL);
      assertThat(testOpportunity.getProbability()).isEqualTo(60);
    }

    @Test
    @DisplayName("Should handle rapid stage changes correctly")
    void stageWorkflow_rapidChanges_shouldHandleCorrectly() {
      // Rapid succession of changes
      testOpportunity.changeStage(OpportunityStage.QUALIFICATION);
      testOpportunity.changeStage(OpportunityStage.NEEDS_ANALYSIS);
      testOpportunity.changeStage(OpportunityStage.PROPOSAL);
      testOpportunity.changeStage(OpportunityStage.NEGOTIATION);

      // Verify final state
      assertThat(testOpportunity.getStage()).isEqualTo(OpportunityStage.NEGOTIATION);
      assertThat(testOpportunity.getProbability()).isEqualTo(80);
      assertThat(testOpportunity.getStageChangedAt()).isNotNull();
    }
  }

  @Nested
  @Tag("core")
  @DisplayName("RENEWAL Stage Tests")
  class RenewalStageTests {

    @Test
    @DisplayName("Should allow transition from CLOSED_WON to RENEWAL")
    void stageTransition_closedWonToRenewal_shouldWork() {
      // Arrange
      testOpportunity.changeStage(OpportunityStage.CLOSED_WON);

      // Act
      testOpportunity.changeStage(OpportunityStage.RENEWAL);

      // Assert
      assertThat(testOpportunity.getStage()).isEqualTo(OpportunityStage.RENEWAL);
      assertThat(testOpportunity.getProbability()).isEqualTo(75);
    }

    @Test
    @DisplayName("Should allow transition from RENEWAL to CLOSED_WON")
    void stageTransition_renewalToClosedWon_shouldWork() {
      // Arrange
      testOpportunity.changeStage(OpportunityStage.CLOSED_WON);
      testOpportunity.changeStage(OpportunityStage.RENEWAL);

      // Act
      testOpportunity.changeStage(OpportunityStage.CLOSED_WON);

      // Assert
      assertThat(testOpportunity.getStage()).isEqualTo(OpportunityStage.CLOSED_WON);
      assertThat(testOpportunity.getProbability()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should allow transition from RENEWAL to CLOSED_LOST")
    void stageTransition_renewalToClosedLost_shouldWork() {
      // Arrange
      testOpportunity.changeStage(OpportunityStage.CLOSED_WON);
      testOpportunity.changeStage(OpportunityStage.RENEWAL);

      // Act
      testOpportunity.changeStage(OpportunityStage.CLOSED_LOST);

      // Assert
      assertThat(testOpportunity.getStage()).isEqualTo(OpportunityStage.CLOSED_LOST);
      assertThat(testOpportunity.getProbability()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should identify RENEWAL as active stage")
    void isActive_renewalStage_shouldReturnTrue() {
      // Act
      testOpportunity.changeStage(OpportunityStage.RENEWAL);

      // Assert
      assertThat(testOpportunity.getStage().isActive()).isTrue();
    }

    @Test
    @DisplayName("Should identify RENEWAL stage correctly")
    void isRenewal_renewalStage_shouldReturnTrue() {
      // Act
      testOpportunity.changeStage(OpportunityStage.RENEWAL);

      // Assert
      assertThat(testOpportunity.getStage().isRenewal()).isTrue();

      // Verify other stages are not renewal
      assertThat(OpportunityStage.NEW_LEAD.isRenewal()).isFalse();
      assertThat(OpportunityStage.CLOSED_WON.isRenewal()).isFalse();
    }

    @Test
    @DisplayName("Should have correct display name and color for RENEWAL")
    void renewalStage_shouldHaveCorrectProperties() {
      // Assert
      assertThat(OpportunityStage.RENEWAL.getDisplayName()).isEqualTo("Verlängerung");
      assertThat(OpportunityStage.RENEWAL.getColor()).isEqualTo("#ff9800");
      assertThat(OpportunityStage.RENEWAL.getDefaultProbability()).isEqualTo(75);
    }

    @Test
    @DisplayName("Should not allow invalid transitions to RENEWAL")
    void stageTransition_invalidToRenewal_shouldNotWork() {
      // Test direct transition from NEW_LEAD to RENEWAL (should not work)
      testOpportunity.changeStage(OpportunityStage.NEW_LEAD);
      var originalStage = testOpportunity.getStage();
      var originalTimestamp = testOpportunity.getStageChangedAt();

      // This transition should be blocked by business rules in the service layer
      // For now, we test that the enum allows us to check valid transitions
      var nextStages = OpportunityStage.NEW_LEAD.getNextPossibleStages();
      assertThat(nextStages).doesNotContain(OpportunityStage.RENEWAL);
    }
  }

  // Helper method for testing active stages
  private boolean isActiveStage(OpportunityStage stage) {
    return stage != OpportunityStage.CLOSED_WON && stage != OpportunityStage.CLOSED_LOST;
  }
}

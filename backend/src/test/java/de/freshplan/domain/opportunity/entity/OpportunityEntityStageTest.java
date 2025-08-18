package de.freshplan.domain.opportunity.entity;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.user.entity.User;
import de.freshplan.test.builders.CustomerTestDataFactory;
import de.freshplan.test.builders.OpportunityTestDataFactory;
import de.freshplan.test.builders.UserTestDataFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit Tests für Opportunity Entity Stage Transition Business Logic
 *
 * <p>Tests decken ab: - Entity.changeStage() Business Rules - Stage Validation Rules - Transition
 * History Tracking - Invalid Transition Handling - Edge Cases in Stage Changes - Business Rule
 * Enforcement
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@DisplayName("Opportunity Entity Stage Transition Tests")
public class OpportunityEntityStageTest {

  private Customer testCustomer;
  private User testUser;

  @BeforeEach
  void setUp() {
    // Create test customer using TestDataFactory
    testCustomer = CustomerTestDataFactory.builder().withCompanyName("[TEST] Test Company").build();
    testCustomer.setId(UUID.randomUUID());
    testCustomer.setIsTestData(true); // Mark as test data

    // Create test user using TestDataFactory
    testUser =
        UserTestDataFactory.builder()
            .withUsername("testuser")
            .withFirstName("Test")
            .withLastName("User")
            .withEmail("test@example.com")
            .build();
  }

  @Nested
  @DisplayName("Valid Stage Transition Tests")
  class ValidStageTransitionTests {

    @ParameterizedTest
    @MethodSource("validForwardTransitions")
    @DisplayName("Should allow valid forward stage transitions")
    void changeStage_validForwardTransition_shouldSucceed(
        OpportunityStage fromStage, OpportunityStage toStage, int expectedProbability) {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", fromStage);
      var originalStageChangedAt = opportunity.getStageChangedAt();

      // Act
      opportunity.changeStage(toStage);

      // Assert
      assertThat(opportunity.getStage()).isEqualTo(toStage);
      assertThat(opportunity.getProbability()).isEqualTo(expectedProbability);
      assertThat(opportunity.getStageChangedAt()).isAfter(originalStageChangedAt);
      assertThat(opportunity.getUpdatedAt()).isNotNull();
    }

    static Stream<Arguments> validForwardTransitions() {
      return Stream.of(
          Arguments.of(OpportunityStage.NEW_LEAD, OpportunityStage.QUALIFICATION, 25),
          Arguments.of(OpportunityStage.QUALIFICATION, OpportunityStage.NEEDS_ANALYSIS, 40),
          Arguments.of(OpportunityStage.NEEDS_ANALYSIS, OpportunityStage.PROPOSAL, 60),
          Arguments.of(OpportunityStage.PROPOSAL, OpportunityStage.NEGOTIATION, 80),
          Arguments.of(OpportunityStage.NEGOTIATION, OpportunityStage.CLOSED_WON, 100),
          Arguments.of(OpportunityStage.NEW_LEAD, OpportunityStage.CLOSED_WON, 100),
          Arguments.of(OpportunityStage.QUALIFICATION, OpportunityStage.CLOSED_WON, 100),
          Arguments.of(OpportunityStage.NEEDS_ANALYSIS, OpportunityStage.CLOSED_WON, 100));
    }

    @ParameterizedTest
    @MethodSource("validBackwardTransitions")
    @DisplayName("Should allow valid backward stage transitions")
    void changeStage_validBackwardTransition_shouldSucceed(
        OpportunityStage fromStage, OpportunityStage toStage, int expectedProbability) {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", fromStage);

      // Act
      opportunity.changeStage(toStage);

      // Assert
      assertThat(opportunity.getStage()).isEqualTo(toStage);
      assertThat(opportunity.getProbability()).isEqualTo(expectedProbability);
    }

    static Stream<Arguments> validBackwardTransitions() {
      return Stream.of(
          Arguments.of(OpportunityStage.NEGOTIATION, OpportunityStage.PROPOSAL, 60),
          Arguments.of(OpportunityStage.PROPOSAL, OpportunityStage.NEEDS_ANALYSIS, 40),
          Arguments.of(OpportunityStage.NEEDS_ANALYSIS, OpportunityStage.QUALIFICATION, 25),
          Arguments.of(OpportunityStage.QUALIFICATION, OpportunityStage.NEW_LEAD, 10),
          Arguments.of(OpportunityStage.NEGOTIATION, OpportunityStage.NEW_LEAD, 10),
          Arguments.of(OpportunityStage.PROPOSAL, OpportunityStage.NEW_LEAD, 10));
    }

    @ParameterizedTest
    @MethodSource("closedLostFromActiveStages")
    @DisplayName("Should allow transition to CLOSED_LOST from any active stage")
    void changeStage_toClosedLostFromAnyActiveStage_shouldSucceed(OpportunityStage fromStage) {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", fromStage);

      // Act
      opportunity.changeStage(OpportunityStage.CLOSED_LOST);

      // Assert
      assertThat(opportunity.getStage()).isEqualTo(OpportunityStage.CLOSED_LOST);
      assertThat(opportunity.getProbability()).isEqualTo(0);
    }

    static Stream<Arguments> closedLostFromActiveStages() {
      return Stream.of(
          Arguments.of(OpportunityStage.NEW_LEAD),
          Arguments.of(OpportunityStage.QUALIFICATION),
          Arguments.of(OpportunityStage.NEEDS_ANALYSIS),
          Arguments.of(OpportunityStage.PROPOSAL),
          Arguments.of(OpportunityStage.NEGOTIATION));
    }

    @Test
    @DisplayName("Should be no-op when changing to same stage")
    void changeStage_sameStage_shouldBeNoOp() {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", OpportunityStage.PROPOSAL);
      var originalStageChangedAt = opportunity.getStageChangedAt();
      var originalUpdatedAt = opportunity.getUpdatedAt();

      // Act
      opportunity.changeStage(OpportunityStage.PROPOSAL);

      // Assert
      assertThat(opportunity.getStage()).isEqualTo(OpportunityStage.PROPOSAL);
      assertThat(opportunity.getStageChangedAt()).isEqualTo(originalStageChangedAt);
      // updatedAt might change due to setter behavior, but stage logic should not change timestamps
    }
  }

  @Nested
  @DisplayName("Invalid Stage Transition Tests")
  class InvalidStageTransitionTests {

    @Test
    @DisplayName("Should ignore transitions from CLOSED_WON")
    void changeStage_fromClosedWon_shouldBeIgnored() {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", OpportunityStage.CLOSED_WON);
      var originalStage = opportunity.getStage();
      var originalProbability = opportunity.getProbability();

      // Act - Try to change from CLOSED_WON to any other stage
      opportunity.changeStage(OpportunityStage.NEW_LEAD);

      // Assert - Should remain unchanged
      assertThat(opportunity.getStage()).isEqualTo(originalStage);
      assertThat(opportunity.getProbability()).isEqualTo(originalProbability);
    }

    @Test
    @DisplayName("Should ignore transitions from CLOSED_LOST")
    void changeStage_fromClosedLost_shouldBeIgnored() {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", OpportunityStage.CLOSED_LOST);
      var originalStage = opportunity.getStage();
      var originalProbability = opportunity.getProbability();

      // Act - Try to change from CLOSED_LOST to any other stage
      opportunity.changeStage(OpportunityStage.PROPOSAL);

      // Assert - Should remain unchanged
      assertThat(opportunity.getStage()).isEqualTo(originalStage);
      assertThat(opportunity.getProbability()).isEqualTo(originalProbability);
    }
  }

  @Nested
  @DisplayName("Stage Transition Business Rules")
  class StageTransitionBusinessRules {

    @Test
    @DisplayName("Should update probability to stage default")
    void changeStage_shouldUpdateProbabilityToStageDefault() {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", OpportunityStage.NEW_LEAD);
      opportunity.setProbability(99); // Set custom probability

      // Act
      opportunity.changeStage(OpportunityStage.PROPOSAL);

      // Assert
      assertThat(opportunity.getProbability()).isEqualTo(60); // Default for PROPOSAL
    }

    @Test
    @DisplayName("Should track timestamp accurately")
    void changeStage_shouldTrackTimestampAccurately() {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", OpportunityStage.NEW_LEAD);
      var beforeChange = LocalDateTime.now();

      // Act
      opportunity.changeStage(OpportunityStage.QUALIFICATION);

      // Assert
      var afterChange = LocalDateTime.now();
      assertThat(opportunity.getStageChangedAt())
          .isAfter(beforeChange.minusSeconds(1))
          .isBefore(afterChange.plusSeconds(1));
    }

    @Test
    @DisplayName("Should update updatedAt timestamp")
    void changeStage_shouldUpdateUpdatedAtTimestamp() {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", OpportunityStage.NEW_LEAD);
      var originalUpdatedAt = opportunity.getUpdatedAt();

      // Act
      opportunity.changeStage(OpportunityStage.QUALIFICATION);

      // Assert
      if (originalUpdatedAt != null) {
        assertThat(opportunity.getUpdatedAt()).isAfter(originalUpdatedAt);
      } else {
        assertThat(opportunity.getUpdatedAt()).isNotNull();
      }
    }
  }

  @Nested
  @DisplayName("Edge Cases and Boundary Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle null stage gracefully")
    void changeStage_nullStage_shouldBeIgnored() {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", OpportunityStage.PROPOSAL);
      var originalStage = opportunity.getStage();

      // Act
      opportunity.changeStage(null);

      // Assert - Should remain unchanged
      assertThat(opportunity.getStage()).isEqualTo(originalStage);
    }

    @Test
    @DisplayName("Should work with newly created opportunity")
    void changeStage_newOpportunity_shouldWork() {
      // Arrange
      var opportunity =
          OpportunityTestDataFactory.builder()
              .withName("New Opp")
              .inStage(OpportunityStage.NEW_LEAD)
              .assignedTo(testUser)
              .forCustomer(testCustomer)
              .withExpectedValue(BigDecimal.valueOf(10000))
              .build();

      // Act
      opportunity.changeStage(OpportunityStage.QUALIFICATION);

      // Assert
      assertThat(opportunity.getStage()).isEqualTo(OpportunityStage.QUALIFICATION);
      assertThat(opportunity.getProbability()).isEqualTo(25);
    }
  }

  // Helper methods
  private Opportunity createTestOpportunity(String name, OpportunityStage stage) {
    var opportunity =
        OpportunityTestDataFactory.builder()
            .withName(name)
            .inStage(OpportunityStage.NEW_LEAD)
            .assignedTo(testUser)
            .forCustomer(testCustomer)
            .withExpectedValue(BigDecimal.valueOf(10000))
            .build();
    opportunity.setId(UUID.randomUUID());

    // Use reflection to set stage directly if not NEW_LEAD to avoid business logic
    if (stage != OpportunityStage.NEW_LEAD) {
      try {
        var stageField = Opportunity.class.getDeclaredField("stage");
        stageField.setAccessible(true);
        stageField.set(opportunity, stage);

        var probabilityField = Opportunity.class.getDeclaredField("probability");
        probabilityField.setAccessible(true);
        int defaultProbability =
            switch (stage) {
              case NEW_LEAD -> 10;
              case QUALIFICATION -> 25;
              case NEEDS_ANALYSIS -> 40;
              case PROPOSAL -> 60;
              case NEGOTIATION -> 80;
              case CLOSED_WON -> 100;
              case CLOSED_LOST -> 0;
              case RENEWAL -> 75;
            };
        probabilityField.set(opportunity, defaultProbability);
      } catch (Exception e) {
        throw new RuntimeException("Failed to set stage via reflection", e);
      }
    }

    return opportunity;
  }
}

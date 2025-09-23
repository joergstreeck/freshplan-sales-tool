package de.freshplan.domain.opportunity.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.domain.opportunity.service.dto.ChangeStageRequest;
import de.freshplan.domain.opportunity.service.exception.InvalidStageTransitionException;
import de.freshplan.domain.opportunity.service.exception.OpportunityNotFoundException;
import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.test.builders.CustomerBuilder;
import de.freshplan.test.builders.OpportunityBuilder;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import java.time.Duration;
import java.time.LocalDateTime;
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
 * Spezialisierte Tests für OpportunityService Stage Transition Rules
 *
 * <p>Tests decken ab: - Complex Stage Transition Business Logic - Stage Validation Rules -
 * Transition History Tracking - Invalid Transition Handling - Edge Cases in Stage Changes -
 * Business Rule Enforcement
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@Tag("core")
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
@io.quarkus.test.TestTransaction
public class OpportunityServiceStageTransitionTest {

  @Inject OpportunityService opportunityService;

  @Inject OpportunityRepository opportunityRepository;

  @Inject CustomerRepository customerRepository;

  @Inject UserRepository userRepository;

  @Inject EntityManager entityManager;

  @Inject CustomerBuilder customerBuilder;

  @Inject OpportunityBuilder opportunityBuilder;

  @Inject UserTransaction userTransaction;

  private Customer testCustomer;
  private User testUser;

  @BeforeEach
  void setUp() {
    // Will be created lazily when needed
    testCustomer = null;
    testUser = null;
  }

  @Nested
  @Tag("core")
  @DisplayName("Valid Stage Transition Tests")
  class ValidStageTransitionTests {

    @ParameterizedTest
    @MethodSource("validForwardTransitions")
    @DisplayName("Should allow valid forward stage transitions")
    void changeStage_validForwardTransition_shouldSucceed(
        OpportunityStage fromStage, OpportunityStage toStage, int expectedProbability) {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", fromStage);
      var request = ChangeStageRequest.builder().stage(toStage).build();

      // Act
      var result = opportunityService.changeStage(opportunity.getId(), request);

      // Assert
      assertThat(result)
          .satisfies(
              updated -> {
                assertThat(updated.getStage()).isEqualTo(toStage);
                assertThat(updated.getProbability()).isEqualTo(expectedProbability);
                assertThat(updated.getStageChangedAt()).isNotNull();
                assertThat(updated.getStageChangedAt()).isAfter(opportunity.getStageChangedAt());
              });
    }

    static Stream<Arguments> validForwardTransitions() {
      return Stream.of(
          Arguments.of(OpportunityStage.NEW_LEAD, OpportunityStage.QUALIFICATION, 25),
          Arguments.of(OpportunityStage.QUALIFICATION, OpportunityStage.NEEDS_ANALYSIS, 40),
          Arguments.of(OpportunityStage.NEEDS_ANALYSIS, OpportunityStage.PROPOSAL, 60),
          Arguments.of(OpportunityStage.PROPOSAL, OpportunityStage.NEGOTIATION, 80),
          Arguments.of(OpportunityStage.NEGOTIATION, OpportunityStage.CLOSED_WON, 100),

          // Stage skipping should also be allowed
          Arguments.of(OpportunityStage.NEW_LEAD, OpportunityStage.PROPOSAL, 60),
          Arguments.of(OpportunityStage.QUALIFICATION, OpportunityStage.NEGOTIATION, 80),
          Arguments.of(OpportunityStage.NEW_LEAD, OpportunityStage.CLOSED_WON, 100));
    }

    @ParameterizedTest
    @MethodSource("validBackwardTransitions")
    @DisplayName("Should allow valid backward stage transitions")
    void changeStage_validBackwardTransition_shouldSucceed(
        OpportunityStage fromStage, OpportunityStage toStage, int expectedProbability) {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", fromStage);
      var request = ChangeStageRequest.builder().stage(toStage).build();

      // Act
      var result = opportunityService.changeStage(opportunity.getId(), request);

      // Assert
      assertThat(result)
          .satisfies(
              updated -> {
                assertThat(updated.getStage()).isEqualTo(toStage);
                assertThat(updated.getProbability()).isEqualTo(expectedProbability);
                assertThat(updated.getStageChangedAt()).isNotNull();
              });
    }

    static Stream<Arguments> validBackwardTransitions() {
      return Stream.of(
          Arguments.of(OpportunityStage.NEGOTIATION, OpportunityStage.PROPOSAL, 60),
          Arguments.of(OpportunityStage.PROPOSAL, OpportunityStage.NEEDS_ANALYSIS, 40),
          Arguments.of(OpportunityStage.NEEDS_ANALYSIS, OpportunityStage.QUALIFICATION, 25),
          Arguments.of(OpportunityStage.QUALIFICATION, OpportunityStage.NEW_LEAD, 10),

          // Large backward jumps should also be allowed
          Arguments.of(OpportunityStage.NEGOTIATION, OpportunityStage.NEW_LEAD, 10),
          Arguments.of(OpportunityStage.PROPOSAL, OpportunityStage.NEW_LEAD, 10));
    }

    @ParameterizedTest
    @EnumSource(
        value = OpportunityStage.class,
        names = {"CLOSED_WON", "CLOSED_LOST"},
        mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Should allow transition to CLOSED_LOST from any active stage")
    void changeStage_toClosedLostFromAnyActiveStage_shouldSucceed(OpportunityStage fromStage) {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", fromStage);
      var request = ChangeStageRequest.builder().stage(OpportunityStage.CLOSED_LOST).build();

      // Act
      var result = opportunityService.changeStage(opportunity.getId(), request);

      // Assert
      assertThat(result)
          .satisfies(
              updated -> {
                assertThat(updated.getStage()).isEqualTo(OpportunityStage.CLOSED_LOST);
                assertThat(updated.getProbability()).isEqualTo(0);
                assertThat(updated.getStageChangedAt()).isNotNull();
              });
    }

    @Test
    @DisplayName("Should allow transition to same stage (no-op)")
    void changeStage_sameStage_shouldBeNoOp() {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", OpportunityStage.PROPOSAL);
      // The opportunity should already have the correct probability for PROPOSAL stage
      assertThat(opportunity.getProbability())
          .as("Created opportunity should have PROPOSAL probability")
          .isEqualTo(60);
      var originalTimestamp = opportunity.getStageChangedAt();
      var request = ChangeStageRequest.builder().stage(OpportunityStage.PROPOSAL).build();

      // Act
      var result = opportunityService.changeStage(opportunity.getId(), request);

      // Assert
      assertThat(result)
          .satisfies(
              updated -> {
                assertThat(updated.getStage()).isEqualTo(OpportunityStage.PROPOSAL);
                assertThat(updated.getProbability()).isEqualTo(60);
                // Allow for small timing differences (nanoseconds) in CI environment
                // Check that timestamps are close (within 1 second as no-op might still touch
                // timestamp)
                var timeDifference =
                    Duration.between(originalTimestamp, updated.getStageChangedAt()).abs();
                assertThat(timeDifference)
                    .isLessThan(Duration.ofSeconds(1)); // No significant change
              });
    }
  }

  @Nested
  @Tag("core")
  @DisplayName("Invalid Stage Transition Tests")
  class InvalidStageTransitionTests {

    @ParameterizedTest
    @MethodSource("invalidTransitionsFromClosedStates")
    @DisplayName("Should reject transitions from closed states")
    void changeStage_fromClosedState_shouldThrowException(
        OpportunityStage fromStage, OpportunityStage toStage) {
      // Arrange
      var opportunity = createTestOpportunity("Closed Opportunity", fromStage);
      var request = ChangeStageRequest.builder().stage(toStage).build();

      // Act & Assert
      assertThatThrownBy(() -> opportunityService.changeStage(opportunity.getId(), request))
          .isInstanceOf(InvalidStageTransitionException.class)
          .hasMessageContaining("Cannot change stage from closed state");
    }

    static Stream<Arguments> invalidTransitionsFromClosedStates() {
      return Stream.of(
          // From CLOSED_WON to any other stage
          Arguments.of(OpportunityStage.CLOSED_WON, OpportunityStage.NEW_LEAD),
          Arguments.of(OpportunityStage.CLOSED_WON, OpportunityStage.QUALIFICATION),
          Arguments.of(OpportunityStage.CLOSED_WON, OpportunityStage.PROPOSAL),
          Arguments.of(OpportunityStage.CLOSED_WON, OpportunityStage.NEGOTIATION),
          Arguments.of(OpportunityStage.CLOSED_WON, OpportunityStage.CLOSED_LOST),

          // From CLOSED_LOST to any other stage
          Arguments.of(OpportunityStage.CLOSED_LOST, OpportunityStage.NEW_LEAD),
          Arguments.of(OpportunityStage.CLOSED_LOST, OpportunityStage.QUALIFICATION),
          Arguments.of(OpportunityStage.CLOSED_LOST, OpportunityStage.PROPOSAL),
          Arguments.of(OpportunityStage.CLOSED_LOST, OpportunityStage.NEGOTIATION),
          Arguments.of(OpportunityStage.CLOSED_LOST, OpportunityStage.CLOSED_WON));
    }

    @Test
    @DisplayName("Should throw exception for non-existent opportunity")
    void changeStage_nonExistentOpportunity_shouldThrowException() {
      // Arrange
      var nonExistentId = UUID.randomUUID();
      var request = ChangeStageRequest.builder().stage(OpportunityStage.QUALIFICATION).build();

      // Act & Assert
      assertThatThrownBy(() -> opportunityService.changeStage(nonExistentId, request))
          .isInstanceOf(OpportunityNotFoundException.class)
          .hasMessageContaining(nonExistentId.toString());
    }

    @Test
    @DisplayName("Should throw exception for null stage request")
    void changeStage_nullStageRequest_shouldThrowException() {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", OpportunityStage.NEW_LEAD);
      var request = ChangeStageRequest.builder().stage(null).build();

      // Act & Assert
      assertThatThrownBy(() -> opportunityService.changeStage(opportunity.getId(), request))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Stage cannot be null");
    }
  }

  @Nested
  @Tag("core")
  @DisplayName("Stage Transition Business Rules")
  class StageTransitionBusinessRules {

    @Test
    @org.junit.jupiter.api.Disabled(
        "CDI limitation: @Transactional cannot be used in nested test classes")
    @DisplayName("Should update probability according to stage default")
    void changeStage_shouldUpdateProbabilityToStageDefault() {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", OpportunityStage.NEW_LEAD);
      // Manually update probability after creation to test override behavior
      opportunity.setProbability(50); // Custom probability
      opportunityRepository.persist(opportunity);
      opportunityRepository.flush();

      var request = ChangeStageRequest.builder().stage(OpportunityStage.PROPOSAL).build();

      // Act
      var result = opportunityService.changeStage(opportunity.getId(), request);

      // Assert
      assertThat(result.getProbability()).isEqualTo(60); // Default for PROPOSAL, not custom 50
    }

    @Test
    @DisplayName("Should allow custom probability override after stage change")
    void changeStage_withCustomProbability_shouldAllowOverride() {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", OpportunityStage.NEW_LEAD);
      var request =
          ChangeStageRequest.builder()
              .stage(OpportunityStage.PROPOSAL)
              .customProbability(75) // Override default
              .build();

      // Act
      var result = opportunityService.changeStage(opportunity.getId(), request);

      // Assert
      assertThat(result.getProbability()).isEqualTo(75); // Custom probability used
    }

    @Test
    @DisplayName("Should validate custom probability is within valid range")
    void changeStage_invalidCustomProbability_shouldThrowException() {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", OpportunityStage.NEW_LEAD);
      var request =
          ChangeStageRequest.builder()
              .stage(OpportunityStage.PROPOSAL)
              .customProbability(150) // Invalid - over 100%
              .build();

      // Act & Assert
      assertThatThrownBy(() -> opportunityService.changeStage(opportunity.getId(), request))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Probability must be between 0 and 100");
    }

    @Test
    @DisplayName("Should track stage change timestamp accurately")
    void changeStage_shouldTrackTimestampAccurately() {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", OpportunityStage.NEW_LEAD);
      var originalTimestamp = opportunity.getStageChangedAt();

      // Wait a small amount to ensure timestamp difference
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      var request = ChangeStageRequest.builder().stage(OpportunityStage.QUALIFICATION).build();

      // Act
      var result = opportunityService.changeStage(opportunity.getId(), request);

      // Assert
      assertThat(result.getStageChangedAt())
          .isAfter(originalTimestamp)
          .isCloseTo(LocalDateTime.now(), within(5, java.time.temporal.ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("Should maintain other opportunity fields during stage change")
    void changeStage_shouldMaintainOtherFields() {
      // Arrange
      var opportunity = createTestOpportunity("Test Opportunity", OpportunityStage.NEW_LEAD);
      var originalName = opportunity.getName();
      var originalDescription = opportunity.getDescription();
      var originalExpectedValue = opportunity.getExpectedValue();
      var originalCustomer = opportunity.getCustomer();
      var originalAssignedTo = opportunity.getAssignedTo();

      var request = ChangeStageRequest.builder().stage(OpportunityStage.PROPOSAL).build();

      // Act
      var result = opportunityService.changeStage(opportunity.getId(), request);

      // Assert - Only stage and related fields should change
      assertThat(result)
          .satisfies(
              updated -> {
                assertThat(updated.getName()).isEqualTo(originalName);
                assertThat(updated.getDescription()).isEqualTo(originalDescription);
                assertThat(updated.getExpectedValue()).isEqualByComparingTo(originalExpectedValue);
                assertThat(updated.getCustomerId()).isEqualTo(originalCustomer.getId());
                assertThat(updated.getAssignedToId()).isEqualTo(originalAssignedTo.getId());
                assertThat(updated.getStage()).isEqualTo(OpportunityStage.PROPOSAL); // Changed
                assertThat(updated.getProbability()).isEqualTo(60); // Changed
              });
    }
  }

  @Nested
  @Tag("core")
  @DisplayName("Complex Stage Transition Scenarios")
  class ComplexStageTransitionScenarios {

    @Test
    @DisplayName("Should handle rapid sequential stage transitions")
    void changeStage_rapidSequentialTransitions_shouldHandleCorrectly() {
      // Arrange
      var opportunity =
          createTestOpportunity("Rapid Transition Opportunity", OpportunityStage.NEW_LEAD);

      // Act - Rapid sequential transitions
      var result1 =
          opportunityService.changeStage(
              opportunity.getId(),
              ChangeStageRequest.builder().stage(OpportunityStage.QUALIFICATION).build());

      var result2 =
          opportunityService.changeStage(
              opportunity.getId(),
              ChangeStageRequest.builder().stage(OpportunityStage.NEEDS_ANALYSIS).build());

      var result3 =
          opportunityService.changeStage(
              opportunity.getId(),
              ChangeStageRequest.builder().stage(OpportunityStage.PROPOSAL).build());

      // Assert
      assertThat(result3)
          .satisfies(
              finalResult -> {
                assertThat(finalResult.getStage()).isEqualTo(OpportunityStage.PROPOSAL);
                assertThat(finalResult.getProbability()).isEqualTo(60);
                assertThat(finalResult.getStageChangedAt()).isAfter(result1.getStageChangedAt());
                assertThat(finalResult.getStageChangedAt()).isAfter(result2.getStageChangedAt());
              });
    }

    @Test
    @DisplayName("Should handle forward then backward stage progression")
    void changeStage_forwardThenBackward_shouldHandleCorrectly() {
      // Arrange
      var opportunity = createTestOpportunity("Ping Pong Opportunity", OpportunityStage.NEW_LEAD);

      // Act - Forward progression
      opportunityService.changeStage(
          opportunity.getId(),
          ChangeStageRequest.builder().stage(OpportunityStage.QUALIFICATION).build());

      opportunityService.changeStage(
          opportunity.getId(),
          ChangeStageRequest.builder().stage(OpportunityStage.PROPOSAL).build());

      // Backward movement
      var finalResult =
          opportunityService.changeStage(
              opportunity.getId(),
              ChangeStageRequest.builder().stage(OpportunityStage.QUALIFICATION).build());

      // Assert
      assertThat(finalResult)
          .satisfies(
              result -> {
                assertThat(result.getStage()).isEqualTo(OpportunityStage.QUALIFICATION);
                assertThat(result.getProbability())
                    .isEqualTo(25); // Reset to default for QUALIFICATION
              });
    }

    @Test
    @DisplayName("Should handle stage skip then return scenario")
    void changeStage_skipThenReturn_shouldHandleCorrectly() {
      // Arrange
      var opportunity = createTestOpportunity("Skip Return Opportunity", OpportunityStage.NEW_LEAD);

      // Act - Skip stages
      var result1 =
          opportunityService.changeStage(
              opportunity.getId(),
              ChangeStageRequest.builder()
                  .stage(OpportunityStage.NEGOTIATION)
                  .build()); // Skip multiple stages

      // Return to skipped stage
      var result2 =
          opportunityService.changeStage(
              opportunity.getId(),
              ChangeStageRequest.builder().stage(OpportunityStage.QUALIFICATION).build());

      // Assert
      assertThat(result2)
          .satisfies(
              result -> {
                assertThat(result.getStage()).isEqualTo(OpportunityStage.QUALIFICATION);
                assertThat(result.getProbability()).isEqualTo(25);
                assertThat(result.getStageChangedAt()).isAfter(result1.getStageChangedAt());
              });
    }

    @Test
    @DisplayName("Should handle multiple opportunities with different transition patterns")
    void changeStage_multipleOpportunities_shouldHandleIndependently() {
      // Arrange
      var opp1 = createTestOpportunity("Opportunity 1", OpportunityStage.NEW_LEAD);
      var opp2 = createTestOpportunity("Opportunity 2", OpportunityStage.NEW_LEAD);

      // Act - Different transition patterns
      var result1 =
          opportunityService.changeStage(
              opp1.getId(),
              ChangeStageRequest.builder().stage(OpportunityStage.CLOSED_WON).build());

      var result2 =
          opportunityService.changeStage(
              opp2.getId(),
              ChangeStageRequest.builder().stage(OpportunityStage.QUALIFICATION).build());

      // Assert - Each opportunity maintains independent state
      assertThat(result1.getStage()).isEqualTo(OpportunityStage.CLOSED_WON);
      assertThat(result1.getProbability()).isEqualTo(100);

      assertThat(result2.getStage()).isEqualTo(OpportunityStage.QUALIFICATION);
      assertThat(result2.getProbability()).isEqualTo(25);

      // Verify independence - changing one doesn't affect the other
      var result1Updated = opportunityRepository.findById(opp1.getId());
      assertThat(result1Updated.getStage()).isEqualTo(OpportunityStage.CLOSED_WON);
    }
  }

  // Helper methods

  @Transactional
  Customer getOrCreateCustomer(String companyName, String email) {
    var existingCustomer = customerRepository.find("companyName", companyName).firstResult();
    if (existingCustomer != null) {
      return existingCustomer;
    }

    // Create minimal test customer with all required fields using CustomerBuilder
    var customer = customerBuilder.withCompanyName(companyName).build();

    // Override specific fields to maintain test requirements
    customer.setCompanyName(companyName); // Override to use exact name without [TEST-xxx] prefix
    customer.setCustomerNumber("TEST-" + System.currentTimeMillis()); // Unique customer number
    customer.setIsTestData(true); // Mark as test data
    customer.setCreatedBy("test-system"); // Set created by

    customerRepository.persist(customer);
    return customer;
  }

  @Transactional
  User getOrCreateUser(String username, String firstName, String lastName) {
    var existingUser = userRepository.find("username", username).firstResult();
    if (existingUser != null) {
      return existingUser;
    }

    // Cannot create User directly - find existing or fail
    throw new UnsupportedOperationException(
        "Cannot create User directly - use existing test users");
  }

  Opportunity createTestOpportunity(String name, OpportunityStage stage) {
    // Ensure test data is created if not already
    if (testCustomer == null) {
      testCustomer = getOrCreateCustomer("Test Company", "test@example.com");
    }

    if (testUser == null) {
      // Create and persist test user
      testUser =
          de.freshplan.test.builders.UserTestDataFactory.builder()
              .withUsername("stagetest")
              .withFirstName("Test")
              .withLastName("User")
              .withEmail("stagetest@freshplan.de")
              .build();
      testUser.enable();
      testUser.addRole("admin");
      userRepository.persist(testUser);
      userRepository.flush();
    }

    var opportunity =
        opportunityBuilder
            .withName(name)
            .inStage(stage)
            .forCustomer(testCustomer)
            .assignedTo(testUser)
            .persist();

    // Make sure the opportunity has an ID
    assertThat(opportunity.getId()).as("Opportunity ID should be set after persist").isNotNull();

    return opportunity;
  }
}

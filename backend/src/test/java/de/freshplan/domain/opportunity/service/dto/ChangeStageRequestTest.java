package de.freshplan.domain.opportunity.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.opportunity.entity.OpportunityStage;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit Tests für ChangeStageRequest DTO.
 *
 * <p>Sprint 2.1.4: Neu erstellt als Plain JUnit Test.
 *
 * <p>Testet Validation-Constraints und Builder-Logik.
 *
 * @see TEST_DEBUGGING_GUIDE.md
 */
@Tag("unit")
@DisplayName("ChangeStageRequest DTO Unit Tests")
class ChangeStageRequestTest {

  private static ValidatorFactory validatorFactory;
  private Validator validator;

  @BeforeAll
  static void setUpFactory() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
  }

  @BeforeEach
  void setUp() {
    validator = validatorFactory.getValidator();
  }

  @AfterAll
  static void tearDownFactory() {
    if (validatorFactory != null) {
      validatorFactory.close();
    }
  }

  @Test
  @DisplayName("Should validate successfully with valid values")
  void validation_withValidValues_shouldPass() {
    // Given
    ChangeStageRequest request =
        ChangeStageRequest.builder()
            .stage(OpportunityStage.QUALIFICATION)
            .customProbability(50)
            .reason("Customer showed strong interest")
            .build();

    // When
    Set<ConstraintViolation<ChangeStageRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should fail validation when stage is null")
  void validation_withNullStage_shouldFail() {
    // Given
    ChangeStageRequest request = new ChangeStageRequest();
    request.setStage(null);

    // When
    Set<ConstraintViolation<ChangeStageRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).contains("Stage cannot be null");
  }

  @Test
  @DisplayName("Should fail validation when customProbability is negative")
  void validation_withNegativeProbability_shouldFail() {
    // Given
    ChangeStageRequest request = new ChangeStageRequest();
    request.setStage(OpportunityStage.PROPOSAL);
    request.setCustomProbability(-1);

    // When
    Set<ConstraintViolation<ChangeStageRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Probability must be between 0 and 100");
  }

  @Test
  @DisplayName("Should fail validation when customProbability exceeds 100")
  void validation_withTooHighProbability_shouldFail() {
    // Given
    ChangeStageRequest request = new ChangeStageRequest();
    request.setStage(OpportunityStage.NEGOTIATION);
    request.setCustomProbability(101);

    // When
    Set<ConstraintViolation<ChangeStageRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Probability must be between 0 and 100");
  }

  @Test
  @DisplayName("Should accept customProbability of 0")
  void validation_withMinProbability_shouldPass() {
    // Given
    ChangeStageRequest request = new ChangeStageRequest();
    request.setStage(OpportunityStage.NEW_LEAD);
    request.setCustomProbability(0);

    // When
    Set<ConstraintViolation<ChangeStageRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should accept customProbability of 100")
  void validation_withMaxProbability_shouldPass() {
    // Given
    ChangeStageRequest request = new ChangeStageRequest();
    request.setStage(OpportunityStage.CLOSED_WON);
    request.setCustomProbability(100);

    // When
    Set<ConstraintViolation<ChangeStageRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should accept null customProbability")
  void validation_withNullProbability_shouldPass() {
    // Given
    ChangeStageRequest request = new ChangeStageRequest();
    request.setStage(OpportunityStage.PROPOSAL);
    request.setCustomProbability(null);

    // When
    Set<ConstraintViolation<ChangeStageRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should accept null reason")
  void validation_withNullReason_shouldPass() {
    // Given
    ChangeStageRequest request = new ChangeStageRequest();
    request.setStage(OpportunityStage.QUALIFICATION);
    request.setReason(null);

    // When
    Set<ConstraintViolation<ChangeStageRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should use builder correctly")
  void builder_shouldCreateRequestWithAllFields() {
    // When
    ChangeStageRequest request =
        ChangeStageRequest.builder()
            .stage(OpportunityStage.NEGOTIATION)
            .customProbability(75)
            .reason("Advanced to negotiation stage")
            .build();

    // Then
    assertThat(request.getStage()).isEqualTo(OpportunityStage.NEGOTIATION);
    assertThat(request.getCustomProbability()).isEqualTo(75);
    assertThat(request.getReason()).isEqualTo("Advanced to negotiation stage");
  }

  @Test
  @DisplayName("Should use setters correctly")
  void setters_shouldUpdateFields() {
    // Given
    ChangeStageRequest request = new ChangeStageRequest();

    // When
    request.setStage(OpportunityStage.CLOSED_LOST);
    request.setCustomProbability(0);
    request.setReason("Customer went with competitor");

    // Then
    assertThat(request.getStage()).isEqualTo(OpportunityStage.CLOSED_LOST);
    assertThat(request.getCustomProbability()).isEqualTo(0);
    assertThat(request.getReason()).isEqualTo("Customer went with competitor");
  }

  @Test
  @DisplayName("Should handle all OpportunityStage enum values")
  void validation_withAllStageValues_shouldPass() {
    // Test all valid stage transitions (7 stages after RENEWAL removal)
    OpportunityStage[] stages = {
      OpportunityStage.NEW_LEAD,
      OpportunityStage.QUALIFICATION,
      OpportunityStage.NEEDS_ANALYSIS,
      OpportunityStage.PROPOSAL,
      OpportunityStage.NEGOTIATION,
      OpportunityStage.CLOSED_WON,
      OpportunityStage.CLOSED_LOST
    };

    for (OpportunityStage stage : stages) {
      // Given
      ChangeStageRequest request = new ChangeStageRequest();
      request.setStage(stage);

      // When
      Set<ConstraintViolation<ChangeStageRequest>> violations = validator.validate(request);

      // Then
      assertThat(violations).as("Stage " + stage + " should be valid").isEmpty();
    }
  }

  @Test
  @DisplayName("Should handle edge case probability values")
  void validation_withEdgeCaseProbabilityValues_shouldWork() {
    // Given - minimum valid value
    ChangeStageRequest request1 = new ChangeStageRequest();
    request1.setStage(OpportunityStage.NEW_LEAD);
    request1.setCustomProbability(0);

    // When
    Set<ConstraintViolation<ChangeStageRequest>> violations1 = validator.validate(request1);

    // Then
    assertThat(violations1).isEmpty();

    // Given - maximum valid value
    ChangeStageRequest request2 = new ChangeStageRequest();
    request2.setStage(OpportunityStage.CLOSED_WON);
    request2.setCustomProbability(100);

    // When
    Set<ConstraintViolation<ChangeStageRequest>> violations2 = validator.validate(request2);

    // Then
    assertThat(violations2).isEmpty();
  }

  @Test
  @DisplayName("Should allow empty reason string")
  void validation_withEmptyReason_shouldPass() {
    // Given
    ChangeStageRequest request = new ChangeStageRequest();
    request.setStage(OpportunityStage.PROPOSAL);
    request.setReason("");

    // When
    Set<ConstraintViolation<ChangeStageRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should allow long reason text")
  void validation_withLongReason_shouldPass() {
    // Given
    ChangeStageRequest request = new ChangeStageRequest();
    request.setStage(OpportunityStage.NEGOTIATION);
    request.setReason("a".repeat(1000));

    // When
    Set<ConstraintViolation<ChangeStageRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should create request with builder pattern fluently")
  void builder_shouldSupportFluentAPI() {
    // When
    ChangeStageRequest request =
        ChangeStageRequest.builder()
            .stage(OpportunityStage.PROPOSAL)
            .customProbability(60)
            .reason("Proposal submitted")
            .build();

    // Then
    assertThat(request.getStage()).isEqualTo(OpportunityStage.PROPOSAL);
    assertThat(request.getCustomProbability()).isEqualTo(60);
    assertThat(request.getReason()).isEqualTo("Proposal submitted");
  }

  @Test
  @DisplayName("Should allow builder with only required field")
  void builder_withOnlyRequiredField_shouldWork() {
    // When
    ChangeStageRequest request =
        ChangeStageRequest.builder().stage(OpportunityStage.QUALIFICATION).build();

    // Then
    assertThat(request.getStage()).isEqualTo(OpportunityStage.QUALIFICATION);
    assertThat(request.getCustomProbability()).isNull();
    assertThat(request.getReason()).isNull();
  }
}

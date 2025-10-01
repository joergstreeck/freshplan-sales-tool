package de.freshplan.domain.opportunity.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit Tests für UpdateOpportunityRequest DTO.
 *
 * <p>Sprint 2.1.4: Neu erstellt als Plain JUnit Test.
 *
 * <p>Testet Validation-Constraints und Builder-Logik.
 *
 * @see TEST_DEBUGGING_GUIDE.md
 */
@Tag("unit")
@DisplayName("UpdateOpportunityRequest DTO Unit Tests")
class UpdateOpportunityRequestTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  @DisplayName("Should validate successfully with valid values")
  void validation_withValidValues_shouldPass() {
    // Given
    UpdateOpportunityRequest request =
        UpdateOpportunityRequest.builder()
            .name("Updated Opportunity")
            .description("Updated Description")
            .expectedValue(new BigDecimal("30000.00"))
            .probability(75)
            .build();

    // When
    Set<ConstraintViolation<UpdateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should fail validation when name exceeds max length")
  void validation_withTooLongName_shouldFail() {
    // Given
    UpdateOpportunityRequest request = new UpdateOpportunityRequest();
    request.setName("a".repeat(256));

    // When
    Set<ConstraintViolation<UpdateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Name darf maximal 255 Zeichen lang sein");
  }

  @Test
  @DisplayName("Should accept max length name of 255 characters")
  void validation_withMaxLengthName_shouldPass() {
    // Given
    UpdateOpportunityRequest request = new UpdateOpportunityRequest();
    request.setName("a".repeat(255));

    // When
    Set<ConstraintViolation<UpdateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should fail validation when description exceeds max length")
  void validation_withTooLongDescription_shouldFail() {
    // Given
    UpdateOpportunityRequest request = new UpdateOpportunityRequest();
    request.setDescription("a".repeat(2001));

    // When
    Set<ConstraintViolation<UpdateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Beschreibung darf maximal 2000 Zeichen lang sein");
  }

  @Test
  @DisplayName("Should accept max length description of 2000 characters")
  void validation_withMaxLengthDescription_shouldPass() {
    // Given
    UpdateOpportunityRequest request = new UpdateOpportunityRequest();
    request.setDescription("a".repeat(2000));

    // When
    Set<ConstraintViolation<UpdateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should fail validation when expectedValue is negative")
  void validation_withNegativeExpectedValue_shouldFail() {
    // Given
    UpdateOpportunityRequest request = new UpdateOpportunityRequest();
    request.setExpectedValue(new BigDecimal("-500.00"));

    // When
    Set<ConstraintViolation<UpdateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Erwarteter Wert muss positiv sein");
  }

  @Test
  @DisplayName("Should accept zero expectedValue")
  void validation_withZeroExpectedValue_shouldPass() {
    // Given
    UpdateOpportunityRequest request = new UpdateOpportunityRequest();
    request.setExpectedValue(BigDecimal.ZERO);

    // When
    Set<ConstraintViolation<UpdateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should fail validation when probability is negative")
  void validation_withNegativeProbability_shouldFail() {
    // Given
    UpdateOpportunityRequest request = new UpdateOpportunityRequest();
    request.setProbability(-1);

    // When
    Set<ConstraintViolation<UpdateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Wahrscheinlichkeit muss zwischen 0 und 100 liegen");
  }

  @Test
  @DisplayName("Should fail validation when probability exceeds 100")
  void validation_withTooHighProbability_shouldFail() {
    // Given
    UpdateOpportunityRequest request = new UpdateOpportunityRequest();
    request.setProbability(101);

    // When
    Set<ConstraintViolation<UpdateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Wahrscheinlichkeit muss zwischen 0 und 100 liegen");
  }

  @Test
  @DisplayName("Should accept probability of 0")
  void validation_withMinProbability_shouldPass() {
    // Given
    UpdateOpportunityRequest request = new UpdateOpportunityRequest();
    request.setProbability(0);

    // When
    Set<ConstraintViolation<UpdateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should accept probability of 100")
  void validation_withMaxProbability_shouldPass() {
    // Given
    UpdateOpportunityRequest request = new UpdateOpportunityRequest();
    request.setProbability(100);

    // When
    Set<ConstraintViolation<UpdateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should use builder correctly")
  void builder_shouldCreateRequestWithAllFields() {
    // Given
    UUID customerId = UUID.randomUUID();
    BigDecimal expectedValue = new BigDecimal("50000.00");
    LocalDate closeDate = LocalDate.now().plusMonths(6);

    // When
    UpdateOpportunityRequest request =
        UpdateOpportunityRequest.builder()
            .name("Builder Test")
            .description("Builder Description")
            .customerId(customerId)
            .expectedValue(expectedValue)
            .expectedCloseDate(closeDate)
            .probability(85)
            .build();

    // Then
    assertThat(request.getName()).isEqualTo("Builder Test");
    assertThat(request.getDescription()).isEqualTo("Builder Description");
    assertThat(request.getCustomerId()).isEqualTo(customerId);
    assertThat(request.getExpectedValue()).isEqualByComparingTo(expectedValue);
    assertThat(request.getExpectedCloseDate()).isEqualTo(closeDate);
    assertThat(request.getProbability()).isEqualTo(85);
  }

  @Test
  @DisplayName("Should handle null fields correctly")
  void validation_withNullFields_shouldPass() {
    // Given - all fields are optional in UpdateRequest
    UpdateOpportunityRequest request = new UpdateOpportunityRequest();

    // When
    Set<ConstraintViolation<UpdateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should use setters correctly")
  void setters_shouldUpdateFields() {
    // Given
    UpdateOpportunityRequest request = new UpdateOpportunityRequest();
    UUID customerId = UUID.randomUUID();
    BigDecimal expectedValue = new BigDecimal("75000.00");
    LocalDate closeDate = LocalDate.now().plusMonths(9);

    // When
    request.setName("Updated Name");
    request.setDescription("Updated Description");
    request.setCustomerId(customerId);
    request.setExpectedValue(expectedValue);
    request.setExpectedCloseDate(closeDate);
    request.setProbability(90);

    // Then
    assertThat(request.getName()).isEqualTo("Updated Name");
    assertThat(request.getDescription()).isEqualTo("Updated Description");
    assertThat(request.getCustomerId()).isEqualTo(customerId);
    assertThat(request.getExpectedValue()).isEqualByComparingTo(expectedValue);
    assertThat(request.getExpectedCloseDate()).isEqualTo(closeDate);
    assertThat(request.getProbability()).isEqualTo(90);
  }

  @Test
  @DisplayName("Should have meaningful toString")
  void toString_shouldContainAllFields() {
    // Given
    UUID customerId = UUID.randomUUID();
    UpdateOpportunityRequest request =
        UpdateOpportunityRequest.builder()
            .name("Test Opportunity")
            .description("Test Description")
            .customerId(customerId)
            .probability(50)
            .build();

    // When
    String result = request.toString();

    // Then
    assertThat(result)
        .contains("UpdateOpportunityRequest")
        .contains("Test Opportunity")
        .contains("Test Description")
        .contains(customerId.toString())
        .contains("probability=50");
  }

  @Test
  @DisplayName("Should handle edge case probability values")
  void validation_withEdgeCaseProbabilityValues_shouldWork() {
    // Given - minimum valid value
    UpdateOpportunityRequest request1 = new UpdateOpportunityRequest();
    request1.setProbability(0);

    // When
    Set<ConstraintViolation<UpdateOpportunityRequest>> violations1 = validator.validate(request1);

    // Then
    assertThat(violations1).isEmpty();

    // Given - maximum valid value
    UpdateOpportunityRequest request2 = new UpdateOpportunityRequest();
    request2.setProbability(100);

    // When
    Set<ConstraintViolation<UpdateOpportunityRequest>> violations2 = validator.validate(request2);

    // Then
    assertThat(violations2).isEmpty();
  }
}

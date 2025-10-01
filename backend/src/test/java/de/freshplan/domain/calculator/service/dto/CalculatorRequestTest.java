package de.freshplan.domain.calculator.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

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
 * Unit Tests für CalculatorRequest DTO.
 *
 * <p>Sprint 2.1.4: Neu erstellt als Plain JUnit Test.
 *
 * <p>Testet Validation-Constraints und Bean-Logik.
 *
 * @see TEST_DEBUGGING_GUIDE.md
 */
@Tag("unit")
@DisplayName("CalculatorRequest DTO Unit Tests")
class CalculatorRequestTest {

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
  @DisplayName("Should create valid request with all fields")
  void constructor_withValidFields_shouldCreateRequest() {
    // When
    CalculatorRequest request = new CalculatorRequest(10000.0, 15, true, false);

    // Then
    assertThat(request.getOrderValue()).isEqualTo(10000.0);
    assertThat(request.getLeadTime()).isEqualTo(15);
    assertThat(request.getPickup()).isTrue();
    assertThat(request.getChain()).isFalse();
  }

  @Test
  @DisplayName("Should validate successfully with valid values")
  void validation_withValidValues_shouldPass() {
    // Given
    CalculatorRequest request = new CalculatorRequest(5000.0, 10, false, false);

    // When
    Set<ConstraintViolation<CalculatorRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should fail validation when orderValue is null")
  void validation_withNullOrderValue_shouldFail() {
    // Given
    CalculatorRequest request = new CalculatorRequest(null, 10, true, false);

    // When
    Set<ConstraintViolation<CalculatorRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).contains("Bestellwert ist erforderlich");
  }

  @Test
  @DisplayName("Should fail validation when orderValue is negative")
  void validation_withNegativeOrderValue_shouldFail() {
    // Given
    CalculatorRequest request = new CalculatorRequest(-100.0, 10, true, false);

    // When
    Set<ConstraintViolation<CalculatorRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Bestellwert muss mindestens 0€ sein");
  }

  @Test
  @DisplayName("Should fail validation when orderValue exceeds maximum")
  void validation_withTooHighOrderValue_shouldFail() {
    // Given
    CalculatorRequest request = new CalculatorRequest(1000001.0, 10, true, false);

    // When
    Set<ConstraintViolation<CalculatorRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Bestellwert darf maximal 1.000.000€ sein");
  }

  @Test
  @DisplayName("Should accept maximum orderValue of 1000000")
  void validation_withMaxOrderValue_shouldPass() {
    // Given
    CalculatorRequest request = new CalculatorRequest(1000000.0, 10, true, false);

    // When
    Set<ConstraintViolation<CalculatorRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should fail validation when leadTime is null")
  void validation_withNullLeadTime_shouldFail() {
    // Given
    CalculatorRequest request = new CalculatorRequest(5000.0, null, true, false);

    // When
    Set<ConstraintViolation<CalculatorRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).contains("Vorlaufzeit ist erforderlich");
  }

  @Test
  @DisplayName("Should fail validation when leadTime is negative")
  void validation_withNegativeLeadTime_shouldFail() {
    // Given
    CalculatorRequest request = new CalculatorRequest(5000.0, -5, true, false);

    // When
    Set<ConstraintViolation<CalculatorRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Vorlaufzeit muss mindestens 0 Tage sein");
  }

  @Test
  @DisplayName("Should fail validation when leadTime exceeds 365 days")
  void validation_withTooHighLeadTime_shouldFail() {
    // Given
    CalculatorRequest request = new CalculatorRequest(5000.0, 366, true, false);

    // When
    Set<ConstraintViolation<CalculatorRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Vorlaufzeit darf maximal 365 Tage sein");
  }

  @Test
  @DisplayName("Should fail validation when pickup is null")
  void validation_withNullPickup_shouldFail() {
    // Given
    CalculatorRequest request = new CalculatorRequest(5000.0, 10, null, false);

    // When
    Set<ConstraintViolation<CalculatorRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).contains("Abholung ist erforderlich");
  }

  @Test
  @DisplayName("Should fail validation when chain is null")
  void validation_withNullChain_shouldFail() {
    // Given
    CalculatorRequest request = new CalculatorRequest(5000.0, 10, true, null);

    // When
    Set<ConstraintViolation<CalculatorRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).contains("Kette ist erforderlich");
  }

  @Test
  @DisplayName("Should use setters correctly")
  void setters_shouldUpdateFields() {
    // Given
    CalculatorRequest request = new CalculatorRequest();

    // When
    request.setOrderValue(25000.0);
    request.setLeadTime(20);
    request.setPickup(true);
    request.setChain(true);

    // Then
    assertThat(request.getOrderValue()).isEqualTo(25000.0);
    assertThat(request.getLeadTime()).isEqualTo(20);
    assertThat(request.getPickup()).isTrue();
    assertThat(request.getChain()).isTrue();
  }

  @Test
  @DisplayName("Should handle edge case values")
  void validation_withEdgeCaseValues_shouldWork() {
    // Given - minimum valid values
    CalculatorRequest request1 = new CalculatorRequest(0.0, 0, false, false);

    // When
    Set<ConstraintViolation<CalculatorRequest>> violations1 = validator.validate(request1);

    // Then
    assertThat(violations1).isEmpty();

    // Given - maximum valid values
    CalculatorRequest request2 = new CalculatorRequest(1000000.0, 365, true, true);

    // When
    Set<ConstraintViolation<CalculatorRequest>> violations2 = validator.validate(request2);

    // Then
    assertThat(violations2).isEmpty();
  }
}

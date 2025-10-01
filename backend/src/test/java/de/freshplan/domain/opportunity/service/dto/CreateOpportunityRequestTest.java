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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit Tests für CreateOpportunityRequest DTO.
 *
 * <p>Sprint 2.1.4: Neu erstellt als Plain JUnit Test.
 *
 * <p>Testet Validation-Constraints und Builder-Logik.
 *
 * @see TEST_DEBUGGING_GUIDE.md
 */
@Tag("unit")
@DisplayName("CreateOpportunityRequest DTO Unit Tests")
class CreateOpportunityRequestTest {

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
    // Given
    UUID customerId = UUID.randomUUID();
    UUID assignedTo = UUID.randomUUID();
    BigDecimal expectedValue = new BigDecimal("50000.00");
    LocalDate closeDate = LocalDate.now().plusMonths(3);

    // When
    CreateOpportunityRequest request =
        new CreateOpportunityRequest(
            "New Deal", "Description", customerId, assignedTo, expectedValue, closeDate);

    // Then
    assertThat(request.getName()).isEqualTo("New Deal");
    assertThat(request.getDescription()).isEqualTo("Description");
    assertThat(request.getCustomerId()).isEqualTo(customerId);
    assertThat(request.getAssignedTo()).isEqualTo(assignedTo);
    assertThat(request.getExpectedValue()).isEqualByComparingTo(expectedValue);
    assertThat(request.getExpectedCloseDate()).isEqualTo(closeDate);
  }

  @Test
  @DisplayName("Should validate successfully with valid values")
  void validation_withValidValues_shouldPass() {
    // Given
    CreateOpportunityRequest request =
        CreateOpportunityRequest.builder()
            .name("Test Opportunity")
            .description("Test Description")
            .expectedValue(new BigDecimal("25000.00"))
            .build();

    // When
    Set<ConstraintViolation<CreateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should fail validation when name is blank")
  void validation_withBlankName_shouldFail() {
    // Given
    CreateOpportunityRequest request = new CreateOpportunityRequest();
    request.setName("");

    // When
    Set<ConstraintViolation<CreateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Opportunity Name ist erforderlich");
  }

  @Test
  @DisplayName("Should fail validation when name is null")
  void validation_withNullName_shouldFail() {
    // Given
    CreateOpportunityRequest request = new CreateOpportunityRequest();
    request.setName(null);

    // When
    Set<ConstraintViolation<CreateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Opportunity Name ist erforderlich");
  }

  @Test
  @DisplayName("Should fail validation when name exceeds max length")
  void validation_withTooLongName_shouldFail() {
    // Given
    CreateOpportunityRequest request = new CreateOpportunityRequest();
    request.setName("a".repeat(256));

    // When
    Set<ConstraintViolation<CreateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Name darf maximal 255 Zeichen lang sein");
  }

  @Test
  @DisplayName("Should accept max length name of 255 characters")
  void validation_withMaxLengthName_shouldPass() {
    // Given
    CreateOpportunityRequest request = new CreateOpportunityRequest();
    request.setName("a".repeat(255));

    // When
    Set<ConstraintViolation<CreateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should fail validation when description exceeds max length")
  void validation_withTooLongDescription_shouldFail() {
    // Given
    CreateOpportunityRequest request = new CreateOpportunityRequest();
    request.setName("Valid Name");
    request.setDescription("a".repeat(2001));

    // When
    Set<ConstraintViolation<CreateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Beschreibung darf maximal 2000 Zeichen lang sein");
  }

  @Test
  @DisplayName("Should fail validation when expectedValue is negative")
  void validation_withNegativeExpectedValue_shouldFail() {
    // Given
    CreateOpportunityRequest request = new CreateOpportunityRequest();
    request.setName("Valid Name");
    request.setExpectedValue(new BigDecimal("-100.00"));

    // When
    Set<ConstraintViolation<CreateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Erwarteter Wert muss positiv sein");
  }

  @Test
  @DisplayName("Should accept zero expectedValue")
  void validation_withZeroExpectedValue_shouldPass() {
    // Given
    CreateOpportunityRequest request = new CreateOpportunityRequest();
    request.setName("Valid Name");
    request.setExpectedValue(BigDecimal.ZERO);

    // When
    Set<ConstraintViolation<CreateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should use builder correctly")
  void builder_shouldCreateRequestWithAllFields() {
    // Given
    UUID customerId = UUID.randomUUID();
    UUID assignedTo = UUID.randomUUID();
    BigDecimal expectedValue = new BigDecimal("75000.00");
    LocalDate closeDate = LocalDate.now().plusMonths(6);

    // When
    CreateOpportunityRequest request =
        CreateOpportunityRequest.builder()
            .name("Builder Test")
            .description("Builder Description")
            .customerId(customerId)
            .assignedTo(assignedTo)
            .expectedValue(expectedValue)
            .expectedCloseDate(closeDate)
            .build();

    // Then
    assertThat(request.getName()).isEqualTo("Builder Test");
    assertThat(request.getDescription()).isEqualTo("Builder Description");
    assertThat(request.getCustomerId()).isEqualTo(customerId);
    assertThat(request.getAssignedTo()).isEqualTo(assignedTo);
    assertThat(request.getExpectedValue()).isEqualByComparingTo(expectedValue);
    assertThat(request.getExpectedCloseDate()).isEqualTo(closeDate);
  }

  @Test
  @DisplayName("Should handle null optional fields")
  void validation_withNullOptionalFields_shouldPass() {
    // Given
    CreateOpportunityRequest request = new CreateOpportunityRequest();
    request.setName("Valid Name");
    request.setDescription(null);
    request.setCustomerId(null);
    request.setAssignedTo(null);
    request.setExpectedValue(null);
    request.setExpectedCloseDate(null);

    // When
    Set<ConstraintViolation<CreateOpportunityRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should use setters correctly")
  void setters_shouldUpdateFields() {
    // Given
    CreateOpportunityRequest request = new CreateOpportunityRequest();
    UUID customerId = UUID.randomUUID();
    UUID assignedTo = UUID.randomUUID();
    BigDecimal expectedValue = new BigDecimal("100000.00");
    LocalDate closeDate = LocalDate.now().plusMonths(12);

    // When
    request.setName("Updated Name");
    request.setDescription("Updated Description");
    request.setCustomerId(customerId);
    request.setAssignedTo(assignedTo);
    request.setExpectedValue(expectedValue);
    request.setExpectedCloseDate(closeDate);

    // Then
    assertThat(request.getName()).isEqualTo("Updated Name");
    assertThat(request.getDescription()).isEqualTo("Updated Description");
    assertThat(request.getCustomerId()).isEqualTo(customerId);
    assertThat(request.getAssignedTo()).isEqualTo(assignedTo);
    assertThat(request.getExpectedValue()).isEqualByComparingTo(expectedValue);
    assertThat(request.getExpectedCloseDate()).isEqualTo(closeDate);
  }

  @Test
  @DisplayName("Should have meaningful toString")
  void toString_shouldContainAllFields() {
    // Given
    UUID customerId = UUID.randomUUID();
    CreateOpportunityRequest request =
        CreateOpportunityRequest.builder()
            .name("Test Opportunity")
            .description("Test Description")
            .customerId(customerId)
            .build();

    // When
    String result = request.toString();

    // Then
    assertThat(result)
        .contains("CreateOpportunityRequest")
        .contains("Test Opportunity")
        .contains("Test Description")
        .contains(customerId.toString());
  }
}

package de.freshplan.domain.user.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit Tests für CreateUserRequest DTO.
 *
 * <p>Sprint 2.1.4: Neu erstellt als Plain JUnit Test.
 *
 * <p>Testet Validation-Constraints für User-Erstellung.
 *
 * @see TEST_DEBUGGING_GUIDE.md
 */
@Tag("unit")
@DisplayName("CreateUserRequest DTO Unit Tests")
class CreateUserRequestTest {

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
    CreateUserRequest request =
        CreateUserRequest.builder()
            .username("testuser")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

    // When
    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should fail validation when username is blank")
  void validation_withBlankUsername_shouldFail() {
    // Given
    CreateUserRequest request = new CreateUserRequest("", "John", "Doe", "john.doe@example.com");

    // When
    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

    // Then - blank triggers multiple constraints: @NotBlank, @Size, @Pattern
    assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
    assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("Username is required")))
        .isTrue();
  }

  @Test
  @DisplayName("Should fail validation when username is too short")
  void validation_withTooShortUsername_shouldFail() {
    // Given
    CreateUserRequest request = new CreateUserRequest("ab", "John", "Doe", "john.doe@example.com");

    // When
    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Username must be between 3 and 60 characters");
  }

  @Test
  @DisplayName("Should accept minimum username length of 3 characters")
  void validation_withMinLengthUsername_shouldPass() {
    // Given
    CreateUserRequest request = new CreateUserRequest("abc", "John", "Doe", "john.doe@example.com");

    // When
    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should fail validation when username exceeds 60 characters")
  void validation_withTooLongUsername_shouldFail() {
    // Given
    CreateUserRequest request =
        new CreateUserRequest("a".repeat(61), "John", "Doe", "john.doe@example.com");

    // When
    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Username must be between 3 and 60 characters");
  }

  @Test
  @DisplayName("Should accept maximum username length of 60 characters")
  void validation_withMaxLengthUsername_shouldPass() {
    // Given
    CreateUserRequest request =
        new CreateUserRequest("a".repeat(60), "John", "Doe", "john.doe@example.com");

    // When
    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should fail validation when username contains invalid characters")
  void validation_withInvalidUsernameCharacters_shouldFail() {
    // Given
    CreateUserRequest request =
        new CreateUserRequest("user@name", "John", "Doe", "john.doe@example.com");

    // When
    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Username can only contain letters, numbers, dots, underscores and hyphens");
  }

  @Test
  @DisplayName("Should accept username with valid special characters")
  void validation_withValidSpecialCharactersInUsername_shouldPass() {
    // Given
    CreateUserRequest request =
        new CreateUserRequest("user.name_123-test", "John", "Doe", "john.doe@example.com");

    // When
    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should fail validation when firstName is blank")
  void validation_withBlankFirstName_shouldFail() {
    // Given
    CreateUserRequest request =
        new CreateUserRequest("testuser", "", "Doe", "john.doe@example.com");

    // When
    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).contains("First name is required");
  }

  @Test
  @DisplayName("Should fail validation when firstName exceeds 60 characters")
  void validation_withTooLongFirstName_shouldFail() {
    // Given
    CreateUserRequest request =
        new CreateUserRequest("testuser", "a".repeat(61), "Doe", "john.doe@example.com");

    // When
    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("First name must not exceed 60 characters");
  }

  @Test
  @DisplayName("Should accept maximum firstName length of 60 characters")
  void validation_withMaxLengthFirstName_shouldPass() {
    // Given
    CreateUserRequest request =
        new CreateUserRequest("testuser", "a".repeat(60), "Doe", "john.doe@example.com");

    // When
    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should fail validation when lastName is blank")
  void validation_withBlankLastName_shouldFail() {
    // Given
    CreateUserRequest request =
        new CreateUserRequest("testuser", "John", "", "john.doe@example.com");

    // When
    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).contains("Last name is required");
  }

  @Test
  @DisplayName("Should fail validation when lastName exceeds 60 characters")
  void validation_withTooLongLastName_shouldFail() {
    // Given
    CreateUserRequest request =
        new CreateUserRequest("testuser", "John", "a".repeat(61), "john.doe@example.com");

    // When
    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Last name must not exceed 60 characters");
  }

  @Test
  @DisplayName("Should accept maximum lastName length of 60 characters")
  void validation_withMaxLengthLastName_shouldPass() {
    // Given
    CreateUserRequest request =
        new CreateUserRequest("testuser", "John", "a".repeat(60), "john.doe@example.com");

    // When
    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should fail validation when email is blank")
  void validation_withBlankEmail_shouldFail() {
    // Given
    CreateUserRequest request = new CreateUserRequest("testuser", "John", "Doe", "");

    // When
    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
    assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("Email is required")))
        .isTrue();
  }

  @Test
  @DisplayName("Should fail validation when email format is invalid")
  void validation_withInvalidEmailFormat_shouldFail() {
    // Given
    CreateUserRequest request = new CreateUserRequest("testuser", "John", "Doe", "invalid-email");

    // When
    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).contains("Email must be valid");
  }

  @Test
  @DisplayName("Should fail validation when email exceeds 120 characters")
  void validation_withTooLongEmail_shouldFail() {
    // Given
    String longEmail = "a".repeat(110) + "@example.com"; // 121 characters total
    CreateUserRequest request = new CreateUserRequest("testuser", "John", "Doe", longEmail);

    // When
    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

    // Then - triggers both @Size and @Email constraints
    assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
    assertThat(
            violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email must not exceed 120 characters")))
        .isTrue();
  }

  @Test
  @DisplayName("Should accept email near maximum length")
  void validation_withNearMaxLengthEmail_shouldPass() {
    // Given - use valid email format under 120 chars (64 char limit for local-part)
    String localPart = "test." + "a".repeat(58); // 63 chars (max local-part is 64)
    String maxEmail = localPart + "@example.com"; // 76 chars total (valid)
    CreateUserRequest request = new CreateUserRequest("testuser", "John", "Doe", maxEmail);

    // When
    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Should accept valid email formats")
  void validation_withValidEmailFormats_shouldPass() {
    String[] validEmails = {
      "test@example.com",
      "user.name@example.com",
      "user+tag@example.co.uk",
      "user_name@example.org",
      "123@example.com"
    };

    for (String email : validEmails) {
      // Given
      CreateUserRequest request = new CreateUserRequest("testuser", "John", "Doe", email);

      // When
      Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

      // Then
      assertThat(violations).as("Email " + email + " should be valid").isEmpty();
    }
  }

  @Test
  @DisplayName("Should use builder correctly")
  void builder_shouldCreateRequestWithAllFields() {
    // When
    CreateUserRequest request =
        CreateUserRequest.builder()
            .username("john.doe")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

    // Then
    assertThat(request.getUsername()).isEqualTo("john.doe");
    assertThat(request.getFirstName()).isEqualTo("John");
    assertThat(request.getLastName()).isEqualTo("Doe");
    assertThat(request.getEmail()).isEqualTo("john.doe@example.com");
  }

  @Test
  @DisplayName("Should be immutable")
  void request_shouldBeImmutable() {
    // Given
    CreateUserRequest request =
        CreateUserRequest.builder()
            .username("testuser")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

    // When - getters should return same values
    String username1 = request.getUsername();
    String username2 = request.getUsername();

    // Then
    assertThat(username1).isSameAs(username2);
    assertThat(request.getUsername()).isEqualTo("testuser");
    assertThat(request.getFirstName()).isEqualTo("John");
    assertThat(request.getLastName()).isEqualTo("Doe");
    assertThat(request.getEmail()).isEqualTo("john.doe@example.com");
  }

  @Test
  @DisplayName("Should fail with multiple validation errors")
  void validation_withMultipleErrors_shouldReportAll() {
    // Given - blank username, blank firstName, invalid email
    CreateUserRequest request = new CreateUserRequest("", "", "Doe", "invalid");

    // When
    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

    // Then - should have 3 violations
    assertThat(violations).hasSizeGreaterThanOrEqualTo(3);
  }
}

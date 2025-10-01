package de.freshplan.domain.user.service.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit Tests für InvalidRoleException.
 *
 * <p>Sprint 2.1.4: Neu erstellt als Plain JUnit Test.
 *
 * <p>Testet Exception-Message-Formatierung.
 *
 * @see TEST_DEBUGGING_GUIDE.md
 */
@Tag("unit")
@DisplayName("InvalidRoleException Unit Tests")
class InvalidRoleExceptionTest {

  @Test
  @DisplayName("Should create exception with invalid role message")
  void constructor_withInvalidRole_shouldCreateMessage() {
    // When
    InvalidRoleException exception = new InvalidRoleException("superuser");

    // Then
    assertThat(exception.getMessage())
        .contains("Invalid role: 'superuser'")
        .contains("Allowed roles are:")
        .contains("admin")
        .contains("manager")
        .contains("sales");
  }

  @Test
  @DisplayName("Should handle null role in message")
  void constructor_withNullRole_shouldCreateMessage() {
    // When
    InvalidRoleException exception = new InvalidRoleException(null);

    // Then
    assertThat(exception.getMessage())
        .contains("Invalid role: 'null'")
        .contains("Allowed roles are:");
  }

  @Test
  @DisplayName("Should handle empty role in message")
  void constructor_withEmptyRole_shouldCreateMessage() {
    // When
    InvalidRoleException exception = new InvalidRoleException("");

    // Then
    assertThat(exception.getMessage()).contains("Invalid role: ''").contains("Allowed roles are:");
  }

  @Test
  @DisplayName("Should be a RuntimeException")
  void exception_shouldBeRuntimeException() {
    // When
    InvalidRoleException exception = new InvalidRoleException("invalid");

    // Then
    assertThat(exception).isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("Should list all allowed roles in message")
  void constructor_shouldListAllAllowedRoles() {
    // When
    InvalidRoleException exception = new InvalidRoleException("moderator");

    // Then
    String message = exception.getMessage();
    assertThat(message)
        .contains("admin")
        .contains("manager")
        .contains("sales")
        .contains("[")
        .contains("]");
  }

  @Test
  @DisplayName("Should have consistent message format")
  void constructor_shouldHaveConsistentMessageFormat() {
    // Given
    String[] invalidRoles = {"user", "guest", "viewer", "editor"};

    // When & Then
    for (String role : invalidRoles) {
      InvalidRoleException exception = new InvalidRoleException(role);
      assertThat(exception.getMessage())
          .startsWith("Invalid role:")
          .contains(role)
          .contains("Allowed roles are:");
    }
  }

  @Test
  @DisplayName("Should handle special characters in role")
  void constructor_withSpecialCharacters_shouldCreateMessage() {
    // When
    InvalidRoleException exception = new InvalidRoleException("admin@test");

    // Then
    assertThat(exception.getMessage()).contains("Invalid role: 'admin@test'");
  }

  @Test
  @DisplayName("Should handle long role names")
  void constructor_withLongRole_shouldCreateMessage() {
    // Given
    String longRole = "a".repeat(100);

    // When
    InvalidRoleException exception = new InvalidRoleException(longRole);

    // Then
    assertThat(exception.getMessage()).contains("Invalid role: '" + longRole + "'");
  }
}

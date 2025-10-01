package de.freshplan.domain.user.service.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for RoleValidator.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Tag("unit")
class RoleValidatorTest {

  @Test
  void areValidRoles_withValidRoles_shouldReturnTrue() {
    // Given
    List<String> roles = List.of("admin", "manager", "sales");

    // When
    boolean result = RoleValidator.areValidRoles(roles);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void areValidRoles_withInvalidRole_shouldReturnFalse() {
    // Given
    List<String> roles = List.of("admin", "invalid_role");

    // When
    boolean result = RoleValidator.areValidRoles(roles);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void areValidRoles_withEmptyList_shouldReturnFalse() {
    // When
    boolean result = RoleValidator.areValidRoles(List.of());

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void areValidRoles_withNull_shouldReturnFalse() {
    // When
    boolean result = RoleValidator.areValidRoles(null);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void areValidRoles_withNullInList_shouldReturnFalse() {
    // Given
    List<String> roles = Arrays.asList("admin", null, "sales");

    // When
    boolean result = RoleValidator.areValidRoles(roles);

    // Then
    assertThat(result).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"admin", "manager", "sales", "ADMIN", "Manager", "SALES"})
  void isValidRole_withValidRoles_shouldReturnTrue(String role) {
    // When
    boolean result = RoleValidator.isValidRole(role);

    // Then
    assertThat(result).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"invalid", "superuser", "moderator", "Admin ", " admin", ""})
  void isValidRole_withInvalidRoles_shouldReturnFalse(String role) {
    // When
    boolean result = RoleValidator.isValidRole(role);

    // Then
    assertThat(result).isFalse();
  }

  @ParameterizedTest
  @NullSource
  void isValidRole_withNull_shouldReturnFalse(String role) {
    // When
    boolean result = RoleValidator.isValidRole(role);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void getAllowedRoles_shouldReturnCorrectSet() {
    // When
    var allowedRoles = RoleValidator.getAllowedRoles();

    // Then
    assertThat(allowedRoles).hasSize(3).containsExactlyInAnyOrder("admin", "manager", "sales");
  }

  @Test
  void normalizeRole_shouldConvertToLowercase() {
    // Given & When & Then
    assertThat(RoleValidator.normalizeRole("ADMIN")).isEqualTo("admin");
    assertThat(RoleValidator.normalizeRole("Manager")).isEqualTo("manager");
    assertThat(RoleValidator.normalizeRole("sales")).isEqualTo("sales");
    assertThat(RoleValidator.normalizeRole("")).isEqualTo("");
  }

  @Test
  void normalizeRole_withNull_shouldReturnNull() {
    // When
    String result = RoleValidator.normalizeRole(null);

    // Then
    assertThat(result).isNull();
  }

  @Test
  void normalizeRoles_shouldNormalizeAllRoles() {
    // Given
    List<String> roles = List.of("ADMIN", "Manager", "SALES");

    // When
    List<String> normalized = RoleValidator.normalizeRoles(roles);

    // Then
    assertThat(normalized).hasSize(3).containsExactly("admin", "manager", "sales");
  }

  @Test
  void normalizeRoles_withNullList_shouldReturnNull() {
    // When
    List<String> result = RoleValidator.normalizeRoles(null);

    // Then
    assertThat(result).isNull();
  }

  @Test
  void normalizeRoles_withNullValues_shouldNormalizeToNull() {
    // Given
    List<String> roles = Arrays.asList("ADMIN", null, "sales");

    // When
    List<String> normalized = RoleValidator.normalizeRoles(roles);

    // Then
    assertThat(normalized).hasSize(3).containsExactly("admin", null, "sales");
  }
}

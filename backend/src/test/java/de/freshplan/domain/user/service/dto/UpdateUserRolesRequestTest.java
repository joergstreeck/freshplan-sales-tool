package de.freshplan.domain.user.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for UpdateUserRolesRequest DTO.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
class UpdateUserRolesRequestTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void validation_withValidRoles_shouldPass() {
    // Given
    UpdateUserRolesRequest request =
        UpdateUserRolesRequest.builder().roles(List.of("admin", "manager")).build();

    // When
    Set<ConstraintViolation<UpdateUserRolesRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  void validation_withNullRoles_shouldFail() {
    // Given
    UpdateUserRolesRequest request = UpdateUserRolesRequest.builder().roles(null).build();

    // When
    Set<ConstraintViolation<UpdateUserRolesRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Roles list cannot be null");
  }

  @Test
  void validation_withEmptyRoles_shouldFail() {
    // Given
    UpdateUserRolesRequest request = UpdateUserRolesRequest.builder().roles(List.of()).build();

    // When
    Set<ConstraintViolation<UpdateUserRolesRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .isEqualTo("At least one role must be specified");
  }

  @Test
  void builder_shouldCreateValidRequest() {
    // Given
    List<String> roles = List.of("admin", "sales");

    // When
    UpdateUserRolesRequest request = UpdateUserRolesRequest.builder().roles(roles).build();

    // Then
    assertThat(request.getRoles()).isEqualTo(roles);
  }

  @Test
  void constructor_withRoles_shouldSetRoles() {
    // Given
    List<String> roles = List.of("manager");

    // When
    UpdateUserRolesRequest request = new UpdateUserRolesRequest(roles);

    // Then
    assertThat(request.getRoles()).isEqualTo(roles);
  }

  @Test
  void getRoles_shouldReturnUnmodifiableList() {
    // Given
    List<String> originalRoles = List.of("admin", "manager");
    UpdateUserRolesRequest request = UpdateUserRolesRequest.builder().roles(originalRoles).build();

    // When
    List<String> retrievedRoles = request.getRoles();

    // Then
    assertThat(retrievedRoles).isEqualTo(originalRoles);
    assertThat(retrievedRoles).isUnmodifiable();
  }
}

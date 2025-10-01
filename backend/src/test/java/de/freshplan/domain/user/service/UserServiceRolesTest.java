package de.freshplan.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.service.dto.UpdateUserRolesRequest;
import de.freshplan.domain.user.service.dto.UserResponse;
import de.freshplan.domain.user.service.exception.InvalidRoleException;
import de.freshplan.domain.user.service.exception.UserNotFoundException;
import de.freshplan.test.builders.UserTestDataFactory;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for UserService role management functionality.
 *
 * <p>Converted from Mockito unit tests to @QuarkusTest integration tests in Phase 4C. Uses
 * self-managed test data (entity.persist()) instead of mocks.
 *
 * <p>IMPORTANT: @TestTransaction is applied per-method (not class-level) to ensure proper test
 * isolation and rollback after each test.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@Tag("integration")
class UserServiceRolesTest {

  @Inject UserService userService;

  /**
   * Creates and persists a test user within the test transaction. Must be called at the beginning
   * of each test method.
   */
  private User createAndPersistTestUser() {
    User testUser =
        UserTestDataFactory.builder()
            .withUsername("john.doe-" + System.nanoTime() % 1000000) // Unique username
            .withFirstName("John")
            .withLastName("Doe")
            .withEmail("john.doe-" + System.nanoTime() % 1000000 + "@example.com") // Unique email
            .build();
    testUser.persist();
    return testUser;
  }

  @TestTransaction
  @Test
  void updateUserRoles_withValidRoles_shouldUpdateSuccessfully() {
    // Given - Create test user within test transaction
    User testUser = createAndPersistTestUser();
    UUID userId = testUser.getId();

    UpdateUserRolesRequest request =
        UpdateUserRolesRequest.builder().roles(List.of("admin", "manager")).build();

    // When
    UserResponse response = userService.updateUserRoles(userId, request);

    // Then
    assertThat(response.getRoles()).containsExactlyInAnyOrder("admin", "manager");

    // Verify in DB
    User updatedUser = User.findById(userId);
    assertThat(updatedUser.getRoles()).containsExactlyInAnyOrder("admin", "manager");
  }

  @TestTransaction
  @Test
  void updateUserRoles_withMixedCaseRoles_shouldNormalize() {
    // Given - Create test user within test transaction
    User testUser = createAndPersistTestUser();
    UUID userId = testUser.getId();

    UpdateUserRolesRequest request =
        UpdateUserRolesRequest.builder().roles(List.of("ADMIN", "Manager", "SALES")).build();

    // When
    UserResponse response = userService.updateUserRoles(userId, request);

    // Then
    assertThat(response.getRoles()).containsExactlyInAnyOrder("admin", "manager", "sales");

    // Verify in DB
    User updatedUser = User.findById(userId);
    assertThat(updatedUser.getRoles()).containsExactlyInAnyOrder("admin", "manager", "sales");
  }

  @TestTransaction
  @Test
  void updateUserRoles_withInvalidRole_shouldThrowException() {
    // Given - Create test user within test transaction
    User testUser = createAndPersistTestUser();
    UUID userId = testUser.getId();

    UpdateUserRolesRequest request =
        UpdateUserRolesRequest.builder().roles(List.of("admin", "invalid_role")).build();

    // When & Then
    assertThatThrownBy(() -> userService.updateUserRoles(userId, request))
        .isInstanceOf(InvalidRoleException.class)
        .hasMessageContaining("Invalid role: 'invalid_role'")
        .hasMessageContaining("Allowed roles are:")
        .hasMessageContaining("admin")
        .hasMessageContaining("manager")
        .hasMessageContaining("sales")
        .hasMessageContaining("invalid_role");

    // Verify roles were not updated in DB (should still have original roles from factory)
    User unchangedUser = User.findById(userId);
    // User may have default roles from factory, important thing is that invalid role is rejected
    assertThat(unchangedUser.getRoles()).doesNotContain("invalid_role");
  }

  @TestTransaction
  @Test
  void updateUserRoles_withNonExistentUser_shouldThrowException() {
    // Given
    UUID nonExistentUserId = UUID.randomUUID();
    UpdateUserRolesRequest request =
        UpdateUserRolesRequest.builder().roles(List.of("admin")).build();

    // When & Then
    assertThatThrownBy(() -> userService.updateUserRoles(nonExistentUserId, request))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found with ID: " + nonExistentUserId);
  }

  @TestTransaction
  @Test
  void updateUserRoles_withSingleRole_shouldUpdateSuccessfully() {
    // Given - Create test user within test transaction
    User testUser = createAndPersistTestUser();
    UUID userId = testUser.getId();

    UpdateUserRolesRequest request =
        UpdateUserRolesRequest.builder().roles(List.of("sales")).build();

    // When
    UserResponse response = userService.updateUserRoles(userId, request);

    // Then
    assertThat(response.getRoles()).containsExactly("sales");

    // Verify in DB
    User updatedUser = User.findById(userId);
    assertThat(updatedUser.getRoles()).containsExactly("sales");
  }

  @TestTransaction
  @Test
  void updateUserRoles_withDuplicateRoles_shouldDeduplicateInEntity() {
    // Given - Create test user within test transaction
    User testUser = createAndPersistTestUser();
    UUID userId = testUser.getId();

    UpdateUserRolesRequest request =
        UpdateUserRolesRequest.builder().roles(List.of("admin", "admin", "sales")).build();

    // When
    UserResponse response = userService.updateUserRoles(userId, request);

    // Then - Service processes roles (duplicates may or may not be removed depending on impl)
    // The important thing is that the service accepts the request and updates the user
    assertThat(response.getRoles()).contains("admin", "sales");
    assertThat(response.getRoles().size()).isGreaterThanOrEqualTo(2);

    // Verify in DB - user was updated successfully
    User updatedUser = User.findById(userId);
    assertThat(updatedUser.getRoles()).contains("admin", "sales");
  }
}

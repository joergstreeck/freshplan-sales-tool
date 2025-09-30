package de.freshplan.domain.user.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.domain.user.service.dto.CreateUserRequest;
import de.freshplan.domain.user.service.dto.UpdateUserRequest;
import de.freshplan.domain.user.service.dto.UpdateUserRolesRequest;
import de.freshplan.domain.user.service.dto.UserResponse;
import de.freshplan.domain.user.service.exception.DuplicateEmailException;
import de.freshplan.domain.user.service.exception.DuplicateUsernameException;
import de.freshplan.domain.user.service.exception.UserNotFoundException;
import de.freshplan.domain.user.service.mapper.UserMapper;
import de.freshplan.domain.user.service.validation.RoleValidator;
import de.freshplan.test.builders.UserTestDataFactory;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/** Unit tests for UserCommandService. Tests all command operations with mocked dependencies. */
@QuarkusTest
@Tag("core")
class UserCommandServiceTest {

  @Inject UserCommandService commandService;

  @InjectMock UserRepository userRepository;

  @InjectMock UserMapper userMapper;

  private UUID testUserId;
  private User testUser;
  private UserResponse testUserResponse;
  private CreateUserRequest createRequest;
  private UpdateUserRequest updateRequest;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();

    // Setup test user using UserTestDataFactory
    testUser =
        UserTestDataFactory.builder()
            .withUsername("testuser")
            .withFirstName("Test")
            .withLastName("User")
            .withEmail("test@example.com")
            .withRoles(Arrays.asList("sales", "manager"))
            .build();
    setFieldValue(testUser, "id", testUserId);

    // Setup response using constructor
    testUserResponse =
        new UserResponse(
            testUserId,
            "testuser",
            "Test",
            "User",
            "test@example.com",
            true,
            Arrays.asList("sales", "manager"),
            Instant.now(),
            Instant.now());

    // Setup create request using constructor
    createRequest = new CreateUserRequest("newuser", "New", "User", "new@example.com");

    // Setup update request using constructor (username, firstName, lastName, email, enabled)
    updateRequest =
        new UpdateUserRequest("updateduser", "Updated", "User", "updated@example.com", false);

    // Reset mocks
    reset(userRepository, userMapper);
  }

  // ========== CREATE USER TESTS ==========

  @Test
  void createUser_withValidRequest_shouldCreateSuccessfully() {
    // Given
    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(userMapper.toEntity(createRequest)).thenReturn(testUser);
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse result = commandService.createUser(createRequest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getUsername())
        .isEqualTo(testUserResponse.getUsername()); // Use response username
    verify(userRepository).persist(testUser);
  }

  @Test
  void createUser_withDuplicateUsername_shouldThrowException() {
    // Given
    when(userRepository.existsByUsername("newuser")).thenReturn(true);

    // When/Then
    assertThatThrownBy(() -> commandService.createUser(createRequest))
        .isInstanceOf(DuplicateUsernameException.class)
        .hasMessage("Username already exists: newuser");

    verify(userRepository, never()).persist(any(User.class));
  }

  @Test
  void createUser_withDuplicateEmail_shouldThrowException() {
    // Given
    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

    // When/Then
    assertThatThrownBy(() -> commandService.createUser(createRequest))
        .isInstanceOf(DuplicateEmailException.class)
        .hasMessage("Email already exists: new@example.com");

    verify(userRepository, never()).persist(any(User.class));
  }

  @Test
  void createUser_withNullRequest_shouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> commandService.createUser(null))
        .isInstanceOf(jakarta.validation.ConstraintViolationException.class);
  }

  // ========== UPDATE USER TESTS ==========

  @Test
  void updateUser_withValidData_shouldUpdateSuccessfully() {
    // Given
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(userRepository.existsByUsernameExcluding("updateduser", testUserId)).thenReturn(false);
    when(userRepository.existsByEmailExcluding("updated@example.com", testUserId))
        .thenReturn(false);
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse result = commandService.updateUser(testUserId, updateRequest);

    // Then
    assertThat(result).isNotNull();
    verify(userMapper).updateEntity(testUser, updateRequest);
    verify(userRepository, never()).persist(any(User.class)); // Update doesn't call persist
  }

  @Test
  void updateUser_withNonExistentUser_shouldThrowException() {
    // Given
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> commandService.updateUser(testUserId, updateRequest))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessage("User not found with ID: " + testUserId);
  }

  @Test
  void updateUser_withNoChanges_shouldNotUpdate() {
    // Given
    String actualUsername = testUser.getUsername(); // Get the actual username from the builder
    String actualEmail = testUser.getEmail(); // Get the actual email from the builder

    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(userRepository.existsByUsernameExcluding(actualUsername, testUserId)).thenReturn(false);
    when(userRepository.existsByEmailExcluding(actualEmail, testUserId)).thenReturn(false);
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // Create an update request with same values (username, firstName, lastName, email, enabled)
    UpdateUserRequest sameRequest =
        new UpdateUserRequest(actualUsername, "Test", "User", actualEmail, true);

    // When
    UserResponse result = commandService.updateUser(testUserId, sameRequest);

    // Then
    assertThat(result).isNotNull();
    // When no changes are detected, updateEntity should NOT be called
    verify(userMapper, never()).updateEntity(any(), any());
    // But toResponse should be called to return the unchanged user
    verify(userMapper).toResponse(testUser);
  }

  // ========== DELETE USER TESTS ==========

  @Test
  void deleteUser_withExistingUser_shouldDeleteSuccessfully() {
    // Given
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));

    // When
    commandService.deleteUser(testUserId);

    // Then
    verify(userRepository).delete(testUser);
  }

  @Test
  void deleteUser_withNonExistentUser_shouldThrowException() {
    // Given
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> commandService.deleteUser(testUserId))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessage("User not found with ID: " + testUserId);

    verify(userRepository, never()).delete(any());
  }

  @Test
  void deleteUser_withNullId_shouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> commandService.deleteUser(null))
        .isInstanceOf(jakarta.validation.ConstraintViolationException.class);
  }

  // ========== ENABLE USER TESTS ==========

  @Test
  void enableUser_withDisabledUser_shouldEnable() {
    // Given
    setFieldValue(testUser, "enabled", false);
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse result = commandService.enableUser(testUserId);

    // Then
    assertThat(result).isNotNull();
    assertThat(testUser.isEnabled()).isTrue();
    verify(userRepository).flush();
  }

  @Test
  void enableUser_withAlreadyEnabledUser_shouldRemainEnabled() {
    // Given
    setFieldValue(testUser, "enabled", true);
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse result = commandService.enableUser(testUserId);

    // Then
    assertThat(result).isNotNull();
    assertThat(testUser.isEnabled()).isTrue();
    verify(userRepository).flush();
  }

  // ========== DISABLE USER TESTS ==========

  @Test
  void disableUser_withEnabledUser_shouldDisable() {
    // Given
    setFieldValue(testUser, "enabled", true);
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse result = commandService.disableUser(testUserId);

    // Then
    assertThat(result).isNotNull();
    assertThat(testUser.isEnabled()).isFalse();
    verify(userRepository).flush();
  }

  @Test
  void disableUser_withAlreadyDisabledUser_shouldRemainDisabled() {
    // Given
    setFieldValue(testUser, "enabled", false);
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse result = commandService.disableUser(testUserId);

    // Then
    assertThat(result).isNotNull();
    assertThat(testUser.isEnabled()).isFalse();
    verify(userRepository).flush();
  }

  // ========== UPDATE USER ROLES TESTS ==========

  @Test
  @org.junit.jupiter.api.Disabled("Requires mockito-inline for static mocking")
  void updateUserRoles_withValidRoles_shouldUpdate() {
    // Given
    UpdateUserRolesRequest rolesRequest =
        new UpdateUserRolesRequest(Arrays.asList("admin", "sales"));

    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // Mock static RoleValidator
    try (MockedStatic<RoleValidator> roleValidator = mockStatic(RoleValidator.class)) {
      roleValidator
          .when(() -> RoleValidator.normalizeAndValidateRoles(rolesRequest.getRoles()))
          .thenReturn(Arrays.asList("admin", "sales"));

      // When
      UserResponse result = commandService.updateUserRoles(testUserId, rolesRequest);

      // Then
      assertThat(result).isNotNull();
      assertThat(testUser.getRoles()).containsExactly("admin", "sales");
      verify(userRepository).flush();
    }
  }

  @Test
  void updateUserRoles_withNullRequest_shouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> commandService.updateUserRoles(testUserId, null))
        .isInstanceOf(jakarta.validation.ConstraintViolationException.class);
  }

  @Test
  @org.junit.jupiter.api.Disabled("Requires mockito-inline for static mocking")
  void updateUserRoles_withRoleNormalization_shouldNormalizeRoles() {
    // Given
    List<String> inputRoles = Arrays.asList("ADMIN", "Sales", "manager");
    List<String> normalizedRoles = Arrays.asList("admin", "sales", "manager");

    UpdateUserRolesRequest rolesRequest = new UpdateUserRolesRequest(inputRoles);

    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // Mock static RoleValidator to normalize roles
    try (MockedStatic<RoleValidator> roleValidator = mockStatic(RoleValidator.class)) {
      roleValidator
          .when(() -> RoleValidator.normalizeAndValidateRoles(inputRoles))
          .thenReturn(normalizedRoles);

      // When
      UserResponse result = commandService.updateUserRoles(testUserId, rolesRequest);

      // Then
      assertThat(result).isNotNull();
      assertThat(testUser.getRoles()).containsExactly("admin", "sales", "manager");
    }
  }

  @Test
  @org.junit.jupiter.api.Disabled("Requires mockito-inline for static mocking")
  void updateUserRoles_withInvalidRole_shouldThrowException() {
    // Given
    UpdateUserRolesRequest rolesRequest = new UpdateUserRolesRequest(Arrays.asList("invalid_role"));

    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));

    // Mock static RoleValidator to throw exception
    try (MockedStatic<RoleValidator> roleValidator = mockStatic(RoleValidator.class)) {
      roleValidator
          .when(() -> RoleValidator.normalizeAndValidateRoles(rolesRequest.getRoles()))
          .thenThrow(new IllegalArgumentException("Invalid role: invalid_role"));

      // When/Then
      assertThatThrownBy(() -> commandService.updateUserRoles(testUserId, rolesRequest))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Invalid role: invalid_role");
    }
  }

  // Helper method to set private fields via reflection
  private void setFieldValue(Object obj, String fieldName, Object value) {
    try {
      java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(obj, value);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set field " + fieldName, e);
    }
  }
}

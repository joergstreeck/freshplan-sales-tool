package de.freshplan.domain.user.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

/**
 * Unit tests for UserCommandService.
 * Tests all command operations with mocked dependencies.
 */
@QuarkusTest
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
    
    // Setup test user
    testUser = new User();
    testUser.setId(testUserId);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setFirstName("Test");
    testUser.setLastName("User");
    testUser.setEnabled(true);
    testUser.setRoles(Arrays.asList("sales", "manager"));

    // Setup response
    testUserResponse = new UserResponse();
    testUserResponse.setId(testUserId);
    testUserResponse.setUsername("testuser");
    testUserResponse.setEmail("test@example.com");
    testUserResponse.setFirstName("Test");
    testUserResponse.setLastName("User");
    testUserResponse.setEnabled(true);
    testUserResponse.setRoles(Arrays.asList("sales", "manager"));

    // Setup create request
    createRequest = new CreateUserRequest();
    createRequest.setUsername("newuser");
    createRequest.setEmail("new@example.com");
    createRequest.setFirstName("New");
    createRequest.setLastName("User");
    createRequest.setEnabled(true);
    createRequest.setRoles(Arrays.asList("sales"));

    // Setup update request
    updateRequest = new UpdateUserRequest();
    updateRequest.setUsername("updateduser");
    updateRequest.setEmail("updated@example.com");
    updateRequest.setFirstName("Updated");
    updateRequest.setLastName("User");
    updateRequest.setEnabled(false);

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
    assertThat(result.getUsername()).isEqualTo("testuser");
    verify(userRepository).persist(testUser);
    verify(userRepository).existsByUsername("newuser");
    verify(userRepository).existsByEmail("new@example.com");
  }

  @Test
  void createUser_withNullRequest_shouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> commandService.createUser(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("CreateUserRequest cannot be null");
  }

  @Test
  void createUser_withNullUsername_shouldThrowException() {
    // Given
    createRequest.setUsername(null);

    // When/Then
    assertThatThrownBy(() -> commandService.createUser(createRequest))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username cannot be null or empty");
  }

  @Test
  void createUser_withEmptyUsername_shouldThrowException() {
    // Given
    createRequest.setUsername("  ");

    // When/Then
    assertThatThrownBy(() -> commandService.createUser(createRequest))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username cannot be null or empty");
  }

  @Test
  void createUser_withDuplicateUsername_shouldThrowException() {
    // Given
    when(userRepository.existsByUsername("newuser")).thenReturn(true);

    // When/Then
    assertThatThrownBy(() -> commandService.createUser(createRequest))
        .isInstanceOf(DuplicateUsernameException.class);
    
    verify(userRepository, never()).persist(any());
  }

  @Test
  void createUser_withDuplicateEmail_shouldThrowException() {
    // Given
    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

    // When/Then
    assertThatThrownBy(() -> commandService.createUser(createRequest))
        .isInstanceOf(DuplicateEmailException.class);
    
    verify(userRepository, never()).persist(any());
  }

  // ========== UPDATE USER TESTS ==========

  @Test
  void updateUser_withValidChanges_shouldUpdateSuccessfully() {
    // Given
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(userRepository.existsByUsernameExcluding("updateduser", testUserId)).thenReturn(false);
    when(userRepository.existsByEmailExcluding("updated@example.com", testUserId)).thenReturn(false);
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse result = commandService.updateUser(testUserId, updateRequest);

    // Then
    assertThat(result).isNotNull();
    verify(userMapper).updateEntity(testUser, updateRequest);
    verify(userRepository, never()).persist(any()); // User is managed, no persist needed
  }

  @Test
  void updateUser_withNoChanges_shouldSkipUpdate() {
    // Given
    updateRequest.setUsername("testuser");
    updateRequest.setEmail("test@example.com");
    updateRequest.setFirstName("Test");
    updateRequest.setLastName("User");
    updateRequest.setEnabled(true);
    
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse result = commandService.updateUser(testUserId, updateRequest);

    // Then
    assertThat(result).isNotNull();
    verify(userMapper, never()).updateEntity(any(), any());
    verify(userRepository, never()).existsByUsernameExcluding(any(), any());
    verify(userRepository, never()).existsByEmailExcluding(any(), any());
  }

  @Test
  void updateUser_withNonExistentUser_shouldThrowException() {
    // Given
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> commandService.updateUser(testUserId, updateRequest))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found with ID: " + testUserId);
  }

  @Test
  void updateUser_withDuplicateUsername_shouldThrowException() {
    // Given
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(userRepository.existsByUsernameExcluding("updateduser", testUserId)).thenReturn(true);

    // When/Then
    assertThatThrownBy(() -> commandService.updateUser(testUserId, updateRequest))
        .isInstanceOf(DuplicateUsernameException.class);
    
    verify(userMapper, never()).updateEntity(any(), any());
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
  void deleteUser_withNullId_shouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> commandService.deleteUser(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User ID cannot be null");
  }

  @Test
  void deleteUser_withNonExistentUser_shouldThrowException() {
    // Given
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> commandService.deleteUser(testUserId))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found with ID: " + testUserId);
  }

  // ========== ENABLE USER TESTS ==========

  @Test
  void enableUser_withExistingUser_shouldEnableSuccessfully() {
    // Given
    testUser.setEnabled(false);
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse result = commandService.enableUser(testUserId);

    // Then
    assertThat(result).isNotNull();
    verify(testUser).enable();
    verify(userRepository).flush();
  }

  @Test
  void enableUser_withNullId_shouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> commandService.enableUser(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User ID cannot be null");
  }

  // ========== DISABLE USER TESTS ==========

  @Test
  void disableUser_withExistingUser_shouldDisableSuccessfully() {
    // Given
    testUser.setEnabled(true);
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse result = commandService.disableUser(testUserId);

    // Then
    assertThat(result).isNotNull();
    verify(testUser).disable();
    verify(userRepository).flush();
  }

  @Test
  void disableUser_withNonExistentUser_shouldThrowException() {
    // Given
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> commandService.disableUser(testUserId))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found with ID: " + testUserId);
  }

  // ========== UPDATE USER ROLES TESTS ==========

  @Test
  void updateUserRoles_withValidRoles_shouldUpdateSuccessfully() {
    // Given
    UpdateUserRolesRequest rolesRequest = new UpdateUserRolesRequest();
    rolesRequest.setRoles(Arrays.asList("admin", "SALES", "manager")); // Mixed case
    
    List<String> normalizedRoles = Arrays.asList("admin", "sales", "manager");
    
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);
    
    // Mock static RoleValidator
    try (MockedStatic<RoleValidator> mockedValidator = mockStatic(RoleValidator.class)) {
      mockedValidator.when(() -> RoleValidator.normalizeAndValidateRoles(rolesRequest.getRoles()))
          .thenReturn(normalizedRoles);
      
      // When
      UserResponse result = commandService.updateUserRoles(testUserId, rolesRequest);
      
      // Then
      assertThat(result).isNotNull();
      verify(testUser).setRoles(normalizedRoles);
      verify(userRepository).flush();
    }
  }

  @Test
  void updateUserRoles_withNullRequest_shouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> commandService.updateUserRoles(testUserId, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("UpdateUserRolesRequest cannot be null");
  }

  @Test
  void updateUserRoles_withNonExistentUser_shouldThrowException() {
    // Given
    UpdateUserRolesRequest rolesRequest = new UpdateUserRolesRequest();
    rolesRequest.setRoles(Arrays.asList("admin"));
    
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> commandService.updateUserRoles(testUserId, rolesRequest))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found with ID: " + testUserId);
  }

  // ========== HARD DELETE VERIFICATION TEST ==========

  @Test
  void deleteUser_shouldPerformHardDelete_notSoftDelete() {
    // This test verifies that deleteUser performs a HARD DELETE
    // and not a soft delete (no isActive flag is set)
    
    // Given
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));

    // When
    commandService.deleteUser(testUserId);

    // Then
    verify(userRepository).delete(testUser); // Hard delete
    verify(testUser, never()).setEnabled(false); // Not a soft delete
    // No isActive field is modified (User entity doesn't have isActive)
  }
}
package de.freshplan.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.domain.user.service.dto.CreateUserRequest;
import de.freshplan.domain.user.service.dto.UpdateUserRequest;
import de.freshplan.domain.user.service.dto.UserResponse;
import de.freshplan.domain.user.service.exception.DuplicateEmailException;
import de.freshplan.domain.user.service.exception.DuplicateUsernameException;
import de.freshplan.domain.user.service.exception.UserNotFoundException;
import de.freshplan.domain.user.service.mapper.UserMapper;
import de.freshplan.test.builders.UserTestDataFactory;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for UserService.
 *
 * <p>Tests business logic with mocked dependencies. Ensures code readability with short lines and
 * clear structure.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@Tag("core")
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
class UserServiceTest {

  @InjectMock UserRepository userRepository;

  @InjectMock UserMapper userMapper;

  @Inject UserService userService;

  private User testUser;
  private UserResponse testUserResponse;
  private CreateUserRequest createRequest;
  private UpdateUserRequest updateRequest;

  @BeforeEach
  void setUp() {
    testUser = createTestUser();
    testUserResponse = createTestUserResponse();
    createRequest = createTestCreateRequest();
    updateRequest = createTestUpdateRequest();
  }

  @Test
  void testCreateUser_Success() {
    // Given
    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(userMapper.toEntity(any(CreateUserRequest.class))).thenReturn(testUser);
    when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

    // When
    UserResponse response = userService.createUser(createRequest);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getUsername()).isEqualTo(testUserResponse.getUsername());

    verify(userRepository).persist(any(User.class));
  }

  @Test
  void testCreateUser_DuplicateUsername_ShouldThrow() {
    // Given
    when(userRepository.existsByUsername(anyString())).thenReturn(true);

    // When/Then
    assertThatThrownBy(() -> userService.createUser(createRequest))
        .isInstanceOf(DuplicateUsernameException.class)
        .hasMessageContaining("john.doe");

    verify(userRepository, never()).persist(any(User.class));
  }

  @Test
  void testCreateUser_DuplicateEmail_ShouldThrow() {
    // Given
    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(true);

    // When/Then
    assertThatThrownBy(() -> userService.createUser(createRequest))
        .isInstanceOf(DuplicateEmailException.class)
        .hasMessageContaining("john.doe@freshplan.de");

    verify(userRepository, never()).persist(any(User.class));
  }

  @Test
  void testGetUserById_Success() {
    // Given
    UUID userId = UUID.randomUUID();
    when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse response = userService.getUserById(userId);

    // Then
    assertThat(response).isEqualTo(testUserResponse);
  }

  @Test
  void testGetUserById_NotFound_ShouldThrow() {
    // Given
    UUID userId = UUID.randomUUID();
    when(userRepository.findByIdOptional(userId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> userService.getUserById(userId))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining(userId.toString());
  }

  @Test
  void testGetAllUsers() {
    // Given
    User anotherUser = createAnotherTestUser();
    List<User> users = List.of(testUser, anotherUser);
    UserResponse anotherResponse = createAnotherTestUserResponse();

    when(userRepository.listAll()).thenReturn(users);

    // Use Answer to return different responses based on the input user
    when(userMapper.toResponse(any(User.class)))
        .thenAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              if (user.getUsername().equals(testUser.getUsername())) {
                return testUserResponse;
              } else {
                return anotherResponse;
              }
            });

    // When
    List<UserResponse> responses = userService.getAllUsers();

    // Then
    assertThat(responses).hasSize(2);
    assertThat(responses).containsExactlyInAnyOrder(testUserResponse, anotherResponse);
  }

  @Test
  void testFindByEmail_Success() {
    // Given
    String email = "john.doe@freshplan.de";
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    Optional<UserResponse> response = userService.findByEmail(email);

    // Then
    assertThat(response).isPresent();
    assertThat(response.get()).isEqualTo(testUserResponse);
  }

  @Test
  void testFindByEmail_NotFound() {
    // Given
    String email = "nonexistent@freshplan.de";
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // When
    Optional<UserResponse> response = userService.findByEmail(email);

    // Then
    assertThat(response).isEmpty();
  }

  @Test
  void testUpdateUser_Success() {
    // Given
    UUID userId = UUID.randomUUID();
    when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));

    // Capture what gets passed to updateEntity
    doAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              UpdateUserRequest req = invocation.getArgument(1);
              user.setUsername(req.getUsername());
              user.setEmail(req.getEmail());
              return null;
            })
        .when(userMapper)
        .updateEntity(any(), any());

    when(userMapper.toResponse(any())).thenReturn(testUserResponse);

    // When
    UserResponse response = userService.updateUser(userId, updateRequest);

    // Then
    assertThat(response).isNotNull();
    verify(userMapper).updateEntity(testUser, updateRequest);
  }

  @Test
  void testUpdateUser_NotFound_ShouldThrow() {
    // Given
    UUID userId = UUID.randomUUID();
    when(userRepository.findByIdOptional(userId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> userService.updateUser(userId, updateRequest))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining(userId.toString());
  }

  @Test
  void testUpdateUser_DuplicateUsername_ShouldThrow() {
    // Given
    UUID userId = UUID.randomUUID();
    User existingUser = createTestUser();
    setPrivateField(existingUser, "id", userId);

    when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(existingUser));
    when(userRepository.existsByUsernameExcluding(updateRequest.getUsername(), userId))
        .thenReturn(true);

    // When/Then
    assertThatThrownBy(() -> userService.updateUser(userId, updateRequest))
        .isInstanceOf(DuplicateUsernameException.class);
  }

  @Test
  void testDeleteUser_Success() {
    // Given
    UUID userId = UUID.randomUUID();
    when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));

    // When
    userService.deleteUser(userId);

    // Then
    verify(userRepository).delete(testUser);
  }

  @Test
  void testDeleteUser_NotFound_ShouldThrow() {
    // Given
    UUID userId = UUID.randomUUID();
    when(userRepository.findByIdOptional(userId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> userService.deleteUser(userId))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void testEnableUser_Success() {
    // Given
    UUID userId = UUID.randomUUID();
    testUser.disable(); // Start with disabled user

    when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse response = userService.enableUser(userId);

    // Then
    assertThat(testUser.isEnabled()).isTrue();
    verify(userRepository).flush();
  }

  @Test
  void testDisableUser_Success() {
    // Given
    UUID userId = UUID.randomUUID();
    testUser.enable(); // Start with enabled user

    when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse response = userService.disableUser(userId);

    // Then
    assertThat(testUser.isEnabled()).isFalse();
    verify(userRepository).flush();
  }

  // Helper methods

  private User createTestUser() {
    return UserTestDataFactory.builder()
        .withUsername("john.doe")
        .withFirstName("John")
        .withLastName("Doe")
        .withEmail("john.doe@freshplan.de")
        .build();
  }

  private User createAnotherTestUser() {
    return UserTestDataFactory.builder()
        .withUsername("jane.smith")
        .withFirstName("Jane")
        .withLastName("Smith")
        .withEmail("jane.smith@freshplan.de")
        .build();
  }

  private UserResponse createTestUserResponse() {
    return UserResponse.builder()
        .id(UUID.randomUUID())
        .username("john.doe")
        .firstName("John")
        .lastName("Doe")
        .email("john.doe@freshplan.de")
        .enabled(true)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }

  private UserResponse createAnotherTestUserResponse() {
    return UserResponse.builder()
        .id(UUID.randomUUID())
        .username("jane.smith")
        .firstName("Jane")
        .lastName("Smith")
        .email("jane.smith@freshplan.de")
        .enabled(true)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }

  private CreateUserRequest createTestCreateRequest() {
    return CreateUserRequest.builder()
        .username("john.doe")
        .firstName("John")
        .lastName("Doe")
        .email("john.doe@freshplan.de")
        .build();
  }

  private UpdateUserRequest createTestUpdateRequest() {
    return UpdateUserRequest.builder()
        .username("john.updated")
        .firstName("John")
        .lastName("Updated")
        .email("john.updated@freshplan.de")
        .enabled(true)
        .build();
  }

  private void setPrivateField(Object target, String fieldName, Object value) {
    try {
      var field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (NoSuchFieldException e) {
      // Try superclass if field not found in current class
      try {
        var field = target.getClass().getSuperclass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
      } catch (Exception ex) {
        throw new RuntimeException("Failed to set field: " + fieldName, ex);
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to set field: " + fieldName, e);
    }
  }
}

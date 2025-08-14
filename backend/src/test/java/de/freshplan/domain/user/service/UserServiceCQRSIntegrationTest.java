package de.freshplan.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.domain.user.service.command.UserCommandService;
import de.freshplan.domain.user.service.query.UserQueryService;
import de.freshplan.domain.user.service.dto.CreateUserRequest;
import de.freshplan.domain.user.service.dto.UpdateUserRequest;
import de.freshplan.domain.user.service.dto.UpdateUserRolesRequest;
import de.freshplan.domain.user.service.dto.UserResponse;
import de.freshplan.domain.user.service.mapper.UserMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for UserService with CQRS pattern enabled.
 * Tests the complete flow through the facade to command and query services.
 */
@QuarkusTest
@TestProfile(UserServiceCQRSTestProfile.class)
class UserServiceCQRSIntegrationTest {

  @Inject UserService userService;

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

    // Setup response
    testUserResponse = new UserResponse();
    testUserResponse.setId(testUserId);
    testUserResponse.setUsername("testuser");
    testUserResponse.setEmail("test@example.com");
    testUserResponse.setFirstName("Test");
    testUserResponse.setLastName("User");
    testUserResponse.setEnabled(true);

    // Setup requests
    createRequest = new CreateUserRequest();
    createRequest.setUsername("newuser");
    createRequest.setEmail("new@example.com");
    createRequest.setFirstName("New");
    createRequest.setLastName("User");
    createRequest.setEnabled(true);
    createRequest.setRoles(Arrays.asList("sales"));

    updateRequest = new UpdateUserRequest();
    updateRequest.setUsername("updateduser");
    updateRequest.setEmail("updated@example.com");
    updateRequest.setFirstName("Updated");
    updateRequest.setLastName("User");
    updateRequest.setEnabled(false);

    // Reset mocks
    reset(userRepository, userMapper);
  }

  // ========== COMMAND FLOW TESTS ==========

  @Test
  void createUser_withCQRSEnabled_shouldDelegateToCommandService() {
    // Given
    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(userMapper.toEntity(createRequest)).thenReturn(testUser);
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse result = userService.createUser(createRequest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("testuser");
    verify(userRepository).persist(testUser);
  }

  @Test
  void updateUser_withCQRSEnabled_shouldDelegateToCommandService() {
    // Given
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(userRepository.existsByUsernameExcluding("updateduser", testUserId)).thenReturn(false);
    when(userRepository.existsByEmailExcluding("updated@example.com", testUserId)).thenReturn(false);
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse result = userService.updateUser(testUserId, updateRequest);

    // Then
    assertThat(result).isNotNull();
    verify(userMapper).updateEntity(testUser, updateRequest);
  }

  @Test
  void deleteUser_withCQRSEnabled_shouldDelegateToCommandService() {
    // Given
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));

    // When
    userService.deleteUser(testUserId);

    // Then
    verify(userRepository).delete(testUser);
  }

  @Test
  void enableUser_withCQRSEnabled_shouldDelegateToCommandService() {
    // Given
    testUser.setEnabled(false);
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse result = userService.enableUser(testUserId);

    // Then
    assertThat(result).isNotNull();
    verify(userRepository).flush();
  }

  @Test
  void disableUser_withCQRSEnabled_shouldDelegateToCommandService() {
    // Given
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse result = userService.disableUser(testUserId);

    // Then
    assertThat(result).isNotNull();
    verify(userRepository).flush();
  }

  // ========== QUERY FLOW TESTS ==========

  @Test
  void getUser_withCQRSEnabled_shouldDelegateToQueryService() {
    // Given
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse result = userService.getUser(testUserId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("testuser");
    verifyNoWriteOperations();
  }

  @Test
  void getUserByUsername_withCQRSEnabled_shouldDelegateToQueryService() {
    // Given
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse result = userService.getUserByUsername("testuser");

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("testuser");
    verifyNoWriteOperations();
  }

  @Test
  void getAllUsers_withCQRSEnabled_shouldDelegateToQueryService() {
    // Given
    List<User> users = Arrays.asList(testUser);
    when(userRepository.listAll()).thenReturn(users);
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    List<UserResponse> result = userService.getAllUsers();

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getUsername()).isEqualTo("testuser");
    verifyNoWriteOperations();
  }

  @Test
  void countUsers_withCQRSEnabled_shouldDelegateToQueryService() {
    // Given
    when(userRepository.count()).thenReturn(42L);

    // When
    long result = userService.countUsers();

    // Then
    assertThat(result).isEqualTo(42L);
    verifyNoWriteOperations();
  }

  // ========== COMPLETE LIFECYCLE TEST ==========

  @Test
  void completeUserLifecycle_withCQRSEnabled_shouldWorkEndToEnd() {
    // This test simulates a complete user lifecycle through CQRS

    // 1. Create user
    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(userMapper.toEntity(createRequest)).thenReturn(testUser);
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);
    
    UserResponse createdUser = userService.createUser(createRequest);
    assertThat(createdUser).isNotNull();
    verify(userRepository).persist(testUser);

    // 2. Query user
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    UserResponse queriedUser = userService.getUser(testUserId);
    assertThat(queriedUser.getUsername()).isEqualTo("testuser");

    // 3. Update user
    when(userRepository.existsByUsernameExcluding("updateduser", testUserId)).thenReturn(false);
    when(userRepository.existsByEmailExcluding("updated@example.com", testUserId)).thenReturn(false);
    UserResponse updatedUser = userService.updateUser(testUserId, updateRequest);
    assertThat(updatedUser).isNotNull();

    // 4. Disable user
    UserResponse disabledUser = userService.disableUser(testUserId);
    assertThat(disabledUser).isNotNull();

    // 5. Delete user
    userService.deleteUser(testUserId);
    verify(userRepository).delete(testUser);
  }

  // ========== HELPER METHOD ==========

  private void verifyNoWriteOperations() {
    verify(userRepository, never()).persist(any());
    verify(userRepository, never()).delete(any());
    verify(userRepository, never()).flush();
  }
}

/**
 * Test profile to enable CQRS for integration tests.
 */
class UserServiceCQRSTestProfile implements io.quarkus.test.junit.QuarkusTestProfile {
  @Override
  public java.util.Map<String, String> getConfigOverrides() {
    return java.util.Map.of("features.cqrs.enabled", "true");
  }
}
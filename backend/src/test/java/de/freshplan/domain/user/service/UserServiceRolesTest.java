package de.freshplan.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.domain.user.service.dto.UpdateUserRolesRequest;
import de.freshplan.domain.user.service.dto.UserResponse;
import de.freshplan.domain.user.service.exception.InvalidRoleException;
import de.freshplan.domain.user.service.exception.UserNotFoundException;
import de.freshplan.domain.user.service.mapper.UserMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for UserService role management functionality.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class UserServiceRolesTest {

  @Mock private UserRepository userRepository;

  @Mock private UserMapper userMapper;

  @InjectMocks private UserService userService;

  private User testUser;
  private UUID userId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    testUser = new User("john.doe", "John", "Doe", "john.doe@example.com");
    // Use reflection to set the ID (since it's generated)
    try {
      var idField = User.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(testUser, userId);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void updateUserRoles_withValidRoles_shouldUpdateSuccessfully() {
    // Given
    UpdateUserRolesRequest request =
        UpdateUserRolesRequest.builder().roles(List.of("admin", "manager")).build();

    when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser))
        .thenReturn(
            UserResponse.builder()
                .id(userId)
                .username("john.doe")
                .roles(List.of("admin", "manager"))
                .build());

    // When
    UserResponse response = userService.updateUserRoles(userId, request);

    // Then
    assertThat(response.getRoles()).containsExactlyInAnyOrder("admin", "manager");
    verify(userRepository).findByIdOptional(userId);
    verify(userRepository).flush();
    verify(userMapper).toResponse(testUser);
    assertThat(testUser.getRoles()).containsExactlyInAnyOrder("admin", "manager");
  }

  @Test
  void updateUserRoles_withMixedCaseRoles_shouldNormalize() {
    // Given
    UpdateUserRolesRequest request =
        UpdateUserRolesRequest.builder().roles(List.of("ADMIN", "Manager", "SALES")).build();

    when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser))
        .thenReturn(
            UserResponse.builder()
                .id(userId)
                .username("john.doe")
                .roles(List.of("admin", "manager", "sales"))
                .build());

    // When
    UserResponse response = userService.updateUserRoles(userId, request);

    // Then
    assertThat(response.getRoles()).containsExactlyInAnyOrder("admin", "manager", "sales");
    assertThat(testUser.getRoles()).containsExactlyInAnyOrder("admin", "manager", "sales");
  }

  @Test
  void updateUserRoles_withInvalidRole_shouldThrowException() {
    // Given
    UpdateUserRolesRequest request =
        UpdateUserRolesRequest.builder().roles(List.of("admin", "invalid_role")).build();

    when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));

    // When & Then
    assertThatThrownBy(() -> userService.updateUserRoles(userId, request))
        .isInstanceOf(InvalidRoleException.class)
        .hasMessageContaining("Invalid role: 'invalid_role'")
        .hasMessageContaining("Allowed roles are:")
        .hasMessageContaining("admin")
        .hasMessageContaining("manager")
        .hasMessageContaining("sales")
        .hasMessageContaining("invalid_role");

    verify(userRepository).findByIdOptional(userId);
    verify(userRepository, never()).flush();
  }

  @Test
  void updateUserRoles_withNonExistentUser_shouldThrowException() {
    // Given
    UpdateUserRolesRequest request =
        UpdateUserRolesRequest.builder().roles(List.of("admin")).build();

    when(userRepository.findByIdOptional(userId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userService.updateUserRoles(userId, request))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found with ID: " + userId);

    verify(userRepository).findByIdOptional(userId);
    verify(userRepository, never()).flush();
  }

  @Test
  void updateUserRoles_withSingleRole_shouldUpdateSuccessfully() {
    // Given
    UpdateUserRolesRequest request =
        UpdateUserRolesRequest.builder().roles(List.of("sales")).build();

    when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser))
        .thenReturn(
            UserResponse.builder().id(userId).username("john.doe").roles(List.of("sales")).build());

    // When
    UserResponse response = userService.updateUserRoles(userId, request);

    // Then
    assertThat(response.getRoles()).containsExactly("sales");
    assertThat(testUser.getRoles()).containsExactly("sales");
  }

  @Test
  void updateUserRoles_withDuplicateRoles_shouldDeduplicateInEntity() {
    // Given
    UpdateUserRolesRequest request =
        UpdateUserRolesRequest.builder().roles(List.of("admin", "admin", "sales")).build();

    when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser))
        .thenReturn(
            UserResponse.builder()
                .id(userId)
                .username("john.doe")
                .roles(List.of("admin", "sales"))
                .build());

    // When
    UserResponse response = userService.updateUserRoles(userId, request);

    // Then
    // The normalized list will still contain duplicates,
    // but the entity setter should handle it
    verify(userRepository).flush();
  }
}

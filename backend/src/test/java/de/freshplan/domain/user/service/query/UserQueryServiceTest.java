package de.freshplan.domain.user.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.domain.user.service.dto.UserResponse;
import de.freshplan.domain.user.service.exception.UserNotFoundException;
import de.freshplan.domain.user.service.mapper.UserMapper;
import de.freshplan.test.builders.UserTestDataFactory;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for UserQueryService. Tests all query operations with mocked dependencies. IMPORTANT:
 * Verifies that NO write operations are performed.
 */
@QuarkusTest
@Tag("core")
class UserQueryServiceTest {

  @Inject UserQueryService queryService;

  @InjectMock UserRepository userRepository;

  @InjectMock UserMapper userMapper;

  private UUID testUserId;
  private User testUser;
  private User testUser2;
  private UserResponse testUserResponse;
  private UserResponse testUserResponse2;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    UUID userId2 = UUID.randomUUID();

    // Setup test users using UserTestDataFactory
    testUser =
        UserTestDataFactory.builder()
            .withUsername("testuser")
            .withFirstName("Test")
            .withLastName("User")
            .withEmail("test@example.com")
            .build();
    // Use reflection to set the ID since it's private and set by JPA
    setFieldValue(testUser, "id", testUserId);

    testUser2 =
        UserTestDataFactory.builder()
            .withUsername("testuser2")
            .withFirstName("Test2")
            .withLastName("User2")
            .withEmail("test2@example.com")
            .asDisabled()
            .build();
    setFieldValue(testUser2, "id", userId2);

    // Setup responses - UserResponse has all-args constructor
    testUserResponse =
        new UserResponse(
            testUserId,
            "testuser",
            "Test",
            "User",
            "test@example.com",
            true,
            Arrays.asList("sales"),
            null,
            null);

    testUserResponse2 =
        new UserResponse(
            userId2,
            "testuser2",
            "Test2",
            "User2",
            "test2@example.com",
            false,
            Arrays.asList("sales"),
            null,
            null);

    // Reset mocks
    reset(userRepository, userMapper);
  }

  // ========== GET USER TESTS ==========

  @Test
  void getUser_withExistingUser_shouldReturnUser() {
    // Given
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse result = queryService.getUser(testUserId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("testuser");
    verifyNoWriteOperations();
  }

  @Test
  void getUser_withNullId_shouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> queryService.getUser(null))
        .isInstanceOf(jakarta.validation.ConstraintViolationException.class);
  }

  @Test
  void getUser_withNonExistentUser_shouldThrowException() {
    // Given
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> queryService.getUser(testUserId))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found with ID: " + testUserId);
  }

  // ========== GET USER BY USERNAME TESTS ==========

  @Test
  void getUserByUsername_withExistingUser_shouldReturnUser() {
    // Given
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse result = queryService.getUserByUsername("testuser");

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("testuser");
    verifyNoWriteOperations();
  }

  @Test
  void getUserByUsername_withNullUsername_shouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> queryService.getUserByUsername(null))
        .isInstanceOf(jakarta.validation.ConstraintViolationException.class);
  }

  @Test
  void getUserByUsername_withEmptyUsername_shouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> queryService.getUserByUsername("  "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username cannot be null or empty");
  }

  @Test
  void getUserByUsername_withNonExistentUser_shouldThrowException() {
    // Given
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> queryService.getUserByUsername("nonexistent"))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found with username: nonexistent");
  }

  // ========== LIST USERS TESTS ==========

  @Test
  void listUsers_shouldReturnPaginatedUsers() {
    // Given
    List<User> users = Arrays.asList(testUser, testUser2);
    PanacheQuery<User> query = mock(PanacheQuery.class);
    when(userRepository.findAll()).thenReturn(query);
    when(query.page(any(Page.class))).thenReturn(query);
    when(query.list()).thenReturn(users);
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);
    when(userMapper.toResponse(testUser2)).thenReturn(testUserResponse2);

    // When
    List<UserResponse> result = queryService.listUsers(0, 10);

    // Then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getUsername()).isEqualTo("testuser");
    assertThat(result.get(1).getUsername()).isEqualTo("testuser2");
    verify(query).page(any(Page.class));
    verifyNoWriteOperations();
  }

  // ========== LIST ENABLED USERS TESTS ==========

  @Test
  void listEnabledUsers_shouldReturnOnlyEnabledUsers() {
    // Given
    List<User> enabledUsers = Arrays.asList(testUser); // Only testUser is enabled
    when(userRepository.findEnabledUsers(any(Page.class))).thenReturn(enabledUsers);
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    List<UserResponse> result = queryService.listEnabledUsers(0, 10);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getUsername()).isEqualTo("testuser");
    assertThat(result.get(0).isEnabled()).isTrue();
    verify(userRepository).findEnabledUsers(any(Page.class));
    verifyNoWriteOperations();
  }

  // ========== SEARCH USERS TESTS ==========

  @Test
  void searchUsers_withSearchTerm_shouldReturnMatchingUsers() {
    // Given
    String searchTerm = "test";
    List<User> searchResults = Arrays.asList(testUser, testUser2);
    when(userRepository.search(eq(searchTerm), any(Page.class))).thenReturn(searchResults);
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);
    when(userMapper.toResponse(testUser2)).thenReturn(testUserResponse2);

    // When
    List<UserResponse> result = queryService.searchUsers(searchTerm, 0, 10);

    // Then
    assertThat(result).hasSize(2);
    verify(userRepository).search(eq(searchTerm), any(Page.class));
    verifyNoWriteOperations();
  }

  // ========== GET USER BY ID (ALIAS) TEST ==========

  @Test
  void getUserById_shouldDelegateToGetUser() {
    // Given
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    UserResponse result = queryService.getUserById(testUserId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("testuser");
    verify(userRepository).findByIdOptional(testUserId);
    verifyNoWriteOperations();
  }

  // ========== GET ALL USERS TEST ==========

  @Test
  void getAllUsers_shouldReturnAllUsers() {
    // Given
    List<User> allUsers = Arrays.asList(testUser, testUser2);
    when(userRepository.listAll()).thenReturn(allUsers);
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);
    when(userMapper.toResponse(testUser2)).thenReturn(testUserResponse2);

    // When
    List<UserResponse> result = queryService.getAllUsers();

    // Then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getUsername()).isEqualTo("testuser");
    assertThat(result.get(1).getUsername()).isEqualTo("testuser2");
    verifyNoWriteOperations();
  }

  // ========== FIND BY EMAIL TEST ==========

  @Test
  void findByEmail_withExistingEmail_shouldReturnUser() {
    // Given
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

    // When
    Optional<UserResponse> result = queryService.findByEmail("test@example.com");

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    verifyNoWriteOperations();
  }

  @Test
  void findByEmail_withNonExistentEmail_shouldReturnEmpty() {
    // Given
    when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    // When
    Optional<UserResponse> result = queryService.findByEmail("nonexistent@example.com");

    // Then
    assertThat(result).isEmpty();
    verifyNoWriteOperations();
  }

  // ========== COUNT USERS TEST ==========

  @Test
  void countUsers_shouldReturnTotalCount() {
    // Given
    when(userRepository.count()).thenReturn(42L);

    // When
    long result = queryService.countUsers();

    // Then
    assertThat(result).isEqualTo(42L);
    verifyNoWriteOperations();
  }

  // ========== COUNT ENABLED USERS TEST ==========

  @Test
  void countEnabledUsers_shouldReturnEnabledCount() {
    // Given
    when(userRepository.countEnabled()).thenReturn(25L);

    // When
    long result = queryService.countEnabledUsers();

    // Then
    assertThat(result).isEqualTo(25L);
    verifyNoWriteOperations();
  }

  // ========== HELPER METHOD ==========

  /**
   * Verifies that no write operations (persist, delete, flush) are performed. This is critical for
   * CQRS - Query services must be read-only!
   */
  private void verifyNoWriteOperations() {
    verify(userRepository, never()).persist(any(User.class));
    verify(userRepository, never()).delete(any());
    verify(userRepository, never()).flush();
    verify(userRepository, never()).getEntityManager();
    // Verify no @Transactional operations
    verify(userMapper, never()).updateEntity(any(), any());
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

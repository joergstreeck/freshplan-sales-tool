package de.freshplan.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import de.freshplan.domain.permission.entity.Permission;
import de.freshplan.domain.permission.service.PermissionService;
import de.freshplan.infrastructure.security.SecurityContextProvider;
import jakarta.ws.rs.core.Response;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Mock-basierte Unit Tests für PermissionResource.
 *
 * <p>Sprint 2.1.4: Migriert von @QuarkusTest zu Mockito für Performance (~15s Ersparnis).
 *
 * <p>Testet REST-Delegation ohne echten HTTP-Stack.
 *
 * @see TEST_DEBUGGING_GUIDE.md
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("PermissionResource Unit Tests")
class PermissionResourceMockTest {

  @Mock PermissionService permissionService;

  @Mock SecurityContextProvider securityProvider;

  @InjectMocks PermissionResource resource;

  @BeforeEach
  void setUp() {
    // Mocks werden automatisch zurückgesetzt durch MockitoExtension
  }

  // ===== GET /me Tests =====

  @Test
  @DisplayName("getCurrentUserPermissions should delegate to service")
  void getCurrentUserPermissions_shouldDelegateToService() {
    // Given
    List<String> permissions = Arrays.asList("customers:read", "customers:write");
    when(permissionService.getCurrentUserPermissions()).thenReturn(permissions);

    // When
    Response response = resource.getCurrentUserPermissions();

    // Then
    assertThat(response.getStatus()).isEqualTo(200);

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getEntity();
    @SuppressWarnings("unchecked")
    List<String> returnedPermissions = (List<String>) body.get("permissions");

    assertThat(returnedPermissions).containsExactlyInAnyOrder("customers:read", "customers:write");
    verify(permissionService, times(1)).getCurrentUserPermissions();
  }

  @Test
  @DisplayName("getCurrentUserPermissions should return empty list when no permissions")
  void getCurrentUserPermissions_withNoPermissions_shouldReturnEmptyList() {
    // Given
    when(permissionService.getCurrentUserPermissions()).thenReturn(Collections.emptyList());

    // When
    Response response = resource.getCurrentUserPermissions();

    // Then
    assertThat(response.getStatus()).isEqualTo(200);

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getEntity();
    @SuppressWarnings("unchecked")
    List<String> permissions = (List<String>) body.get("permissions");

    assertThat(permissions).isEmpty();
  }

  // ===== GET /check/{permissionCode} Tests =====

  @Test
  @DisplayName("checkPermission should return true when user has permission")
  void checkPermission_whenUserHasPermission_shouldReturnTrue() {
    // Given
    when(permissionService.hasPermission("customers:write")).thenReturn(true);

    // When
    Response response = resource.checkPermission("customers:write");

    // Then
    assertThat(response.getStatus()).isEqualTo(200);

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getEntity();

    assertThat(body.get("hasPermission")).isEqualTo(true);
    verify(permissionService, times(1)).hasPermission("customers:write");
  }

  @Test
  @DisplayName("checkPermission should return false when user lacks permission")
  void checkPermission_whenUserLacksPermission_shouldReturnFalse() {
    // Given
    when(permissionService.hasPermission("customers:delete")).thenReturn(false);

    // When
    Response response = resource.checkPermission("customers:delete");

    // Then
    assertThat(response.getStatus()).isEqualTo(200);

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getEntity();

    assertThat(body.get("hasPermission")).isEqualTo(false);
  }

  // ===== POST /grant Tests =====

  @Test
  @DisplayName("grantPermission should return 501 Not Implemented")
  void grantPermission_shouldReturn501() {
    // Given
    UUID userId = UUID.randomUUID();
    var request = new de.freshplan.api.PermissionResource.GrantPermissionRequest();
    request.userId = userId;
    request.permissionCode = "customers:read";
    request.reason = "Test grant";

    // When
    Response response = resource.grantPermission(request);

    // Then
    assertThat(response.getStatus()).isEqualTo(501);

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getEntity();

    assertThat(body.get("message")).isEqualTo("Permission management not yet implemented");
    verifyNoInteractions(permissionService);
  }

  // ===== POST /revoke Tests =====

  @Test
  @DisplayName("revokePermission should return 501 Not Implemented")
  void revokePermission_shouldReturn501() {
    // Given
    UUID userId = UUID.randomUUID();
    var request = new de.freshplan.api.PermissionResource.RevokePermissionRequest();
    request.userId = userId;
    request.permissionCode = "customers:read";
    request.reason = "Test revoke";

    // When
    Response response = resource.revokePermission(request);

    // Then
    assertThat(response.getStatus()).isEqualTo(501);

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getEntity();

    assertThat(body.get("message")).isEqualTo("Permission management not yet implemented");
    verifyNoInteractions(permissionService);
  }

  // ===== GET /user/{userId} Tests =====

  @Test
  @DisplayName("getUserPermissions should return empty list for now")
  void getUserPermissions_shouldReturnEmptyList() {
    // Given
    UUID userId = UUID.randomUUID();

    // When
    Response response = resource.getUserPermissions(userId);

    // Then
    assertThat(response.getStatus()).isEqualTo(200);

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getEntity();
    @SuppressWarnings("unchecked")
    List<Permission> permissions = (List<Permission>) body.get("permissions");

    assertThat(permissions).isEmpty();
  }

  // ===== Edge Cases =====

  @Test
  @DisplayName("checkPermission with empty code should delegate to service")
  void checkPermission_withEmptyCode_shouldDelegateToService() {
    // Given
    when(permissionService.hasPermission("")).thenReturn(false);

    // When
    Response response = resource.checkPermission("");

    // Then
    assertThat(response.getStatus()).isEqualTo(200);

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getEntity();

    assertThat(body.get("hasPermission")).isEqualTo(false);
    verify(permissionService, times(1)).hasPermission("");
  }

  @Test
  @DisplayName("checkPermission with special characters should work")
  void checkPermission_withSpecialCharacters_shouldWork() {
    // Given
    String permissionCode = "admin:permissions";
    when(permissionService.hasPermission(permissionCode)).thenReturn(true);

    // When
    Response response = resource.checkPermission(permissionCode);

    // Then
    assertThat(response.getStatus()).isEqualTo(200);

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getEntity();

    assertThat(body.get("hasPermission")).isEqualTo(true);
  }
}

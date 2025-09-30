package de.freshplan.domain.permission.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.freshplan.infrastructure.security.SecurityContextProvider;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;import jakarta.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("migrate")
@DisplayName("PermissionService Tests")
class PermissionServiceTest {

  @Inject PermissionService permissionService;

  @InjectMock SecurityContextProvider securityProvider;

  @BeforeEach
  void setUp() {
    reset(securityProvider);
  }

  // ===== hasPermission Tests =====

  @Test
  @DisplayName("Admin should have all permissions")
  void hasPermission_adminRole_shouldReturnTrue() {
    // Arrange
    when(securityProvider.getRoles()).thenReturn(Set.of("admin"));

    // Act & Assert
    assertThat(permissionService.hasPermission("customers:read")).isTrue();
    assertThat(permissionService.hasPermission("customers:write")).isTrue();
    assertThat(permissionService.hasPermission("customers:delete")).isTrue();
    assertThat(permissionService.hasPermission("admin:permissions")).isTrue();
    assertThat(permissionService.hasPermission("any:permission")).isTrue();
  }

  @Test
  @DisplayName("Manager should have customer permissions")
  void hasPermission_managerRole_shouldHaveCustomerPermissions() {
    // Arrange
    when(securityProvider.getRoles()).thenReturn(Set.of("manager"));

    // Act & Assert
    assertThat(permissionService.hasPermission("customers:read")).isTrue();
    assertThat(permissionService.hasPermission("customers:write")).isTrue();
    assertThat(permissionService.hasPermission("customers:delete")).isTrue();
    assertThat(permissionService.hasPermission("admin:permissions")).isFalse();
  }

  @Test
  @DisplayName("Sales should only have read permission")
  void hasPermission_salesRole_shouldOnlyHaveReadPermission() {
    // Arrange
    when(securityProvider.getRoles()).thenReturn(Set.of("sales"));

    // Act & Assert
    assertThat(permissionService.hasPermission("customers:read")).isTrue();
    assertThat(permissionService.hasPermission("customers:write")).isFalse();
    assertThat(permissionService.hasPermission("customers:delete")).isFalse();
    assertThat(permissionService.hasPermission("admin:permissions")).isFalse();
  }

  @Test
  @DisplayName("Unknown role should have no permissions")
  void hasPermission_unknownRole_shouldReturnFalse() {
    // Arrange
    when(securityProvider.getRoles()).thenReturn(Set.of("unknown"));

    // Act & Assert
    assertThat(permissionService.hasPermission("customers:read")).isFalse();
    assertThat(permissionService.hasPermission("customers:write")).isFalse();
  }

  @Test
  @DisplayName("Should handle security provider exceptions gracefully")
  void hasPermission_securityProviderThrowsException_shouldReturnFalse() {
    // Arrange
    when(securityProvider.getRoles()).thenThrow(new RuntimeException("Security error"));

    // Act & Assert
    assertThat(permissionService.hasPermission("customers:read")).isFalse();
  }

  // ===== getCurrentUserPermissions Tests =====

  @Test
  @DisplayName("Admin should get all permissions")
  void getCurrentUserPermissions_adminRole_shouldReturnAllPermissions() {
    // Arrange
    when(securityProvider.getRoles()).thenReturn(Set.of("admin"));

    // Act
    List<String> permissions = permissionService.getCurrentUserPermissions();

    // Assert
    assertThat(permissions)
        .containsExactlyInAnyOrder(
            "customers:read",
            "customers:write",
            "customers:delete",
            "admin:permissions",
            "admin:users");
  }

  @Test
  @DisplayName("Manager should get manager permissions")
  void getCurrentUserPermissions_managerRole_shouldReturnManagerPermissions() {
    // Arrange
    when(securityProvider.getRoles()).thenReturn(Set.of("manager"));

    // Act
    List<String> permissions = permissionService.getCurrentUserPermissions();

    // Assert
    assertThat(permissions)
        .containsExactlyInAnyOrder("customers:read", "customers:write", "customers:delete");
  }

  @Test
  @DisplayName("Sales should get sales permissions")
  void getCurrentUserPermissions_salesRole_shouldReturnSalesPermissions() {
    // Arrange
    when(securityProvider.getRoles()).thenReturn(Set.of("sales"));

    // Act
    List<String> permissions = permissionService.getCurrentUserPermissions();

    // Assert
    assertThat(permissions).containsExactly("customers:read");
  }

  @Test
  @DisplayName("Unknown role should get empty permissions")
  void getCurrentUserPermissions_unknownRole_shouldReturnEmptyList() {
    // Arrange
    when(securityProvider.getRoles()).thenReturn(Set.of("unknown"));

    // Act
    List<String> permissions = permissionService.getCurrentUserPermissions();

    // Assert
    assertThat(permissions).isEmpty();
  }

  @Test
  @DisplayName("Should handle security provider exceptions")
  void getCurrentUserPermissions_securityProviderThrowsException_shouldReturnEmptyList() {
    // Arrange
    when(securityProvider.getRoles()).thenThrow(new RuntimeException("Security error"));

    // Act
    List<String> permissions = permissionService.getCurrentUserPermissions();

    // Assert
    assertThat(permissions).isEmpty();
  }

  // ===== grantUserPermission Tests =====

  @Test
  @DisplayName("grantUserPermission should throw UnsupportedOperationException")
  void grantUserPermission_shouldThrowUnsupportedOperationException() {
    // Arrange
    UUID userId = UUID.randomUUID();
    String permissionCode = "customers:read";
    String reason = "Test reason";

    // Act & Assert
    assertThatThrownBy(() -> permissionService.grantUserPermission(userId, permissionCode, reason))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessage("Permission management not yet implemented");
  }

  // ===== revokeUserPermission Tests =====

  @Test
  @DisplayName("revokeUserPermission should throw UnsupportedOperationException")
  void revokeUserPermission_shouldThrowUnsupportedOperationException() {
    // Arrange
    UUID userId = UUID.randomUUID();
    String permissionCode = "customers:read";
    String reason = "Test reason";

    // Act & Assert
    assertThatThrownBy(() -> permissionService.revokeUserPermission(userId, permissionCode, reason))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessage("Permission management not yet implemented");
  }

  // ===== Multiple Roles Tests =====

  @Test
  @DisplayName("User with multiple roles should have combined permissions")
  void hasPermission_multipleRoles_shouldHaveCombinedPermissions() {
    // Arrange
    when(securityProvider.getRoles()).thenReturn(Set.of("manager", "sales"));

    // Act & Assert
    assertThat(permissionService.hasPermission("customers:read")).isTrue();
    assertThat(permissionService.hasPermission("customers:write")).isTrue();
  }

  @Test
  @DisplayName("Admin role should override other roles")
  void getCurrentUserPermissions_adminWithOtherRoles_shouldReturnAdminPermissions() {
    // Arrange
    when(securityProvider.getRoles()).thenReturn(Set.of("admin", "manager", "sales"));

    // Act
    List<String> permissions = permissionService.getCurrentUserPermissions();

    // Assert - Should return admin permissions, not combined
    assertThat(permissions)
        .containsExactlyInAnyOrder(
            "customers:read",
            "customers:write",
            "customers:delete",
            "admin:permissions",
            "admin:users");
  }
}

package de.freshplan.domain.permission.entity;

import static org.assertj.core.api.Assertions.*;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
@QuarkusTest
@Tag("migrate")@TestTransaction
@DisplayName("Role Entity Tests")
class RoleTest {

  // ===== Constructor Tests =====

  @Test
  @DisplayName("Should create role with valid parameters")
  void constructor_withValidParams_shouldCreateRole() {
    // Act
    Role role = new Role("admin", "Administrator role");

    // Assert
    assertThat(role.getName()).isEqualTo("admin");
    assertThat(role.getDescription()).isEqualTo("Administrator role");
    assertThat(role.isActive()).isTrue();
    assertThat(role.getRolePermissions()).isEmpty();
  }

  @Test
  @DisplayName("Should throw exception for null name")
  void constructor_withNullName_shouldThrow() {
    // Act & Assert
    assertThatThrownBy(() -> new Role(null, "Description"))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Name cannot be null");
  }

  @Test
  @DisplayName("Should accept null description")
  void constructor_withNullDescription_shouldSucceed() {
    // Act
    Role role = new Role("admin", null);

    // Assert
    assertThat(role.getDescription()).isNull();
  }

  // ===== Setter Tests =====

  @Test
  @DisplayName("Should update name")
  void setName_withValidName_shouldUpdate() {
    // Arrange
    Role role = new Role("admin", "Description");

    // Act
    role.setName("manager");

    // Assert
    assertThat(role.getName()).isEqualTo("manager");
  }

  @Test
  @DisplayName("Should throw exception when setting null name")
  void setName_withNull_shouldThrow() {
    // Arrange
    Role role = new Role("admin", "Description");

    // Act & Assert
    assertThatThrownBy(() -> role.setName(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Name cannot be null");
  }

  @Test
  @DisplayName("Should update description")
  void setDescription_shouldUpdate() {
    // Arrange
    Role role = new Role("admin", "Old description");

    // Act
    role.setDescription("New description");

    // Assert
    assertThat(role.getDescription()).isEqualTo("New description");
  }

  @Test
  @DisplayName("Should update active status")
  void setActive_shouldUpdate() {
    // Arrange
    Role role = new Role("admin", "Description");

    // Act
    role.setActive(false);

    // Assert
    assertThat(role.isActive()).isFalse();
  }

  // ===== Business Method Tests =====

  @Test
  @Transactional
  @DisplayName("Should grant permission to role")
  void grantPermission_shouldAddToRolePermissions() {
    // Arrange
    Role role = new Role("test-role", "Test Role");
    Permission permission = new Permission("test:read", "Test Read", null);
    permission.persist();
    UUID grantedBy = UUID.randomUUID();

    // Act
    role.grantPermission(permission, grantedBy);

    // Assert
    assertThat(role.getRolePermissions()).hasSize(1);
    RolePermission rp = role.getRolePermissions().iterator().next();
    assertThat(rp.getRole()).isEqualTo(role);
    assertThat(rp.getPermission()).isEqualTo(permission);
    assertThat(rp.isGranted()).isTrue();
    assertThat(rp.getGrantedBy()).isEqualTo(grantedBy);

    // Cleanup
    permission.delete();
  }

  @Test
  @Transactional
  @DisplayName("Should revoke permission from role")
  void revokePermission_shouldRemoveAndAddRevokeRecord() {
    // Arrange
    Role role = new Role("test-role", "Test Role");
    Permission permission = new Permission("test:read", "Test Read", null);
    permission.persist();
    UUID grantedBy = UUID.randomUUID();
    UUID revokedBy = UUID.randomUUID();

    // First grant
    role.grantPermission(permission, grantedBy);
    assertThat(role.getRolePermissions()).hasSize(1);

    // Act - Revoke
    role.revokePermission(permission, revokedBy);

    // Assert
    assertThat(role.getRolePermissions()).hasSize(1); // Old grant removed, revoke record added
    RolePermission rp = role.getRolePermissions().iterator().next();
    assertThat(rp.isGranted()).isFalse();
    assertThat(rp.getGrantedBy()).isEqualTo(revokedBy);

    // Cleanup
    permission.delete();
  }

  @Test
  @Transactional
  @DisplayName("hasPermission should return true for granted permission")
  void hasPermission_withGrantedPermission_shouldReturnTrue() {
    // Arrange
    Role role = new Role("test-role", "Test Role");
    Permission permission = new Permission("customers:read", "Read Customers", null);
    permission.persist();
    role.grantPermission(permission, UUID.randomUUID());

    // Act & Assert
    assertThat(role.hasPermission("customers:read")).isTrue();

    // Cleanup
    permission.delete();
  }

  @Test
  @Transactional
  @DisplayName("hasPermission should return false for non-granted permission")
  void hasPermission_withoutPermission_shouldReturnFalse() {
    // Arrange
    Role role = new Role("test-role", "Test Role");

    // Act & Assert
    assertThat(role.hasPermission("customers:write")).isFalse();
  }

  @Test
  @Transactional
  @DisplayName("hasPermission should match wildcard permissions")
  void hasPermission_withWildcardPermission_shouldMatch() {
    // Arrange
    Role role = new Role("test-role", "Test Role");
    Permission wildcard = new Permission("customers:*", "All Customer Actions", null);
    wildcard.persist();
    role.grantPermission(wildcard, UUID.randomUUID());

    // Act & Assert
    assertThat(role.hasPermission("customers:read")).isTrue();
    assertThat(role.hasPermission("customers:write")).isTrue();
    assertThat(role.hasPermission("orders:read")).isFalse();

    // Cleanup
    wildcard.delete();
  }

  @Test
  @Transactional
  @DisplayName("hasPermission should return false for revoked permission")
  void hasPermission_withRevokedPermission_shouldReturnFalse() {
    // Arrange
    Role role = new Role("test-role", "Test Role");
    Permission permission = new Permission("customers:read", "Read Customers", null);
    permission.persist();

    // Grant then revoke
    role.grantPermission(permission, UUID.randomUUID());
    role.revokePermission(permission, UUID.randomUUID());

    // Act & Assert
    assertThat(role.hasPermission("customers:read")).isFalse();

    // Cleanup
    permission.delete();
  }

  // ===== Persistence Tests =====

  @Test
  @Transactional
  @DisplayName("Should persist and retrieve role")
  void persistence_shouldSaveAndRetrieve() {
    // Arrange
    Role role = new Role("test-persist", "Test Persistence");

    // Act
    role.persist();
    Role found = Role.findById(role.getId());

    // Assert
    assertThat(found).isNotNull();
    assertThat(found.getName()).isEqualTo("test-persist");
    assertThat(found.getDescription()).isEqualTo("Test Persistence");
    assertThat(found.isActive()).isTrue();
    assertThat(found.getCreatedAt()).isNotNull();
    assertThat(found.getUpdatedAt()).isNotNull();
    assertThat(found.getCreatedAt()).isCloseTo(found.getUpdatedAt(), within(1, ChronoUnit.SECONDS));

    // Cleanup
    role.delete();
  }

  @Test
  @Transactional
  @DisplayName("Should update timestamps on modification")
  void persistence_shouldUpdateTimestamps() throws InterruptedException {
    // Arrange
    Role role = new Role("test-update", "Test Update");
    role.persist();

    Instant originalUpdatedAt = role.getUpdatedAt();
    Thread.sleep(100); // Ensure time difference

    // Act
    role.setDescription("Updated Description");
    role.persist();
    role.flush(); // Force flush to trigger @PreUpdate

    // Assert
    assertThat(role.getUpdatedAt()).isAfter(originalUpdatedAt);
    assertThat(role.getCreatedAt()).isEqualTo(role.getCreatedAt()); // Should not change

    // Cleanup
    role.delete();
  }

  @Test
  @Transactional
  @DisplayName("Should find role by name using named query")
  void namedQuery_findByName_shouldWork() {
    // Arrange
    Role role = new Role("test-findbyname", "Test Find By Name");
    role.persist();

    // Act
    Role found =
        Role.find(
                "#Role.findByName",
                io.quarkus.panache.common.Parameters.with("name", "test-findbyname"))
            .firstResult();

    // Assert
    assertThat(found).isNotNull();
    assertThat(found.getId()).isEqualTo(role.getId());

    // Cleanup
    role.delete();
  }

  @Test
  @Transactional
  @DisplayName("Should find active roles using named query")
  void namedQuery_findActive_shouldWork() {
    // Arrange
    Role activeRole1 = new Role("active1", "Active Role 1");
    Role activeRole2 = new Role("active2", "Active Role 2");
    Role inactiveRole = new Role("inactive", "Inactive Role");
    inactiveRole.setActive(false);

    activeRole1.persist();
    activeRole2.persist();
    inactiveRole.persist();

    // Act
    List<Role> found = Role.find("#Role.findActive").list();

    // Assert
    assertThat(found)
        .extracting(Role::getName)
        .contains("active1", "active2")
        .doesNotContain("inactive");

    // Cleanup
    activeRole1.delete();
    activeRole2.delete();
    inactiveRole.delete();
  }

  // ===== Equals and HashCode Tests =====

  @Test
  @DisplayName("equals() should return true for same instance")
  void equals_sameInstance_shouldReturnTrue() {
    // Arrange
    Role role = new Role("test", "Test");

    // Act & Assert
    assertThat(role).isEqualTo(role);
  }

  @Test
  @DisplayName("equals() should return false for different types")
  void equals_differentType_shouldReturnFalse() {
    // Arrange
    Role role = new Role("test", "Test");

    // Act & Assert
    assertThat(role).isNotEqualTo("not a role");
    assertThat(role).isNotEqualTo(null);
  }

  @Test
  @Transactional
  @DisplayName("equals() should compare by ID")
  void equals_shouldCompareById() {
    // Arrange
    Role role1 = new Role("role1", "Role 1");
    Role role2 = new Role("role2", "Role 2");

    role1.persist();
    role2.persist();

    Role role1Copy = Role.findById(role1.getId());

    // Act & Assert
    assertThat(role1).isEqualTo(role1Copy);
    assertThat(role1).isNotEqualTo(role2);
    assertThat(role1.hashCode()).isEqualTo(role1Copy.hashCode());
    assertThat(role1.hashCode()).isNotEqualTo(role2.hashCode());

    // Cleanup
    role1.delete();
    role2.delete();
  }

  // ===== toString Tests =====

  @Test
  @DisplayName("toString() should include key fields")
  void toString_shouldIncludeKeyFields() {
    // Arrange
    Role role = new Role("test-string", "Test ToString");

    // Act
    String result = role.toString();

    // Assert
    assertThat(result).contains("Role{");
    assertThat(result).contains("name='test-string'");
    assertThat(result).contains("active=true");
  }
}

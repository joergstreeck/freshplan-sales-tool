package de.freshplan.domain.permission.entity;

import static org.assertj.core.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("migrate")
@DisplayName("Permission Entity Tests")
class PermissionTest {

  // ===== Constructor Tests =====

  @Test
  @DisplayName("Should create permission with valid parameters")
  void constructor_withValidParams_shouldCreatePermission() {
    // Act
    Permission permission =
        new Permission("customers:read", "Read Customers", "Allows reading customer data");

    // Assert
    assertThat(permission.getPermissionCode()).isEqualTo("customers:read");
    assertThat(permission.getName()).isEqualTo("Read Customers");
    assertThat(permission.getDescription()).isEqualTo("Allows reading customer data");
    assertThat(permission.getResource()).isEqualTo("customers");
    assertThat(permission.getAction()).isEqualTo("read");
  }

  @Test
  @DisplayName("Should parse resource and action from permission code")
  void constructor_shouldParseResourceAndAction() {
    // Act
    Permission permission = new Permission("admin:permissions", "Manage Permissions", null);

    // Assert
    assertThat(permission.getResource()).isEqualTo("admin");
    assertThat(permission.getAction()).isEqualTo("permissions");
  }

  @Test
  @DisplayName("Should throw exception for null permission code")
  void constructor_withNullPermissionCode_shouldThrow() {
    // Act & Assert
    assertThatThrownBy(() -> new Permission(null, "Name", "Description"))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Permission code cannot be null");
  }

  @Test
  @DisplayName("Should throw exception for null name")
  void constructor_withNullName_shouldThrow() {
    // Act & Assert
    assertThatThrownBy(() -> new Permission("customers:read", null, "Description"))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Name cannot be null");
  }

  @Test
  @DisplayName("Should throw exception for invalid permission code format")
  void constructor_withInvalidFormat_shouldThrow() {
    // Act & Assert
    assertThatThrownBy(() -> new Permission("invalid-format", "Name", "Description"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Permission code must follow format 'resource:action'");
  }

  @Test
  @DisplayName("Should accept null description")
  void constructor_withNullDescription_shouldSucceed() {
    // Act
    Permission permission = new Permission("customers:read", "Read Customers", null);

    // Assert
    assertThat(permission.getDescription()).isNull();
  }

  // ===== Business Method Tests =====

  @Test
  @DisplayName("matches() should return true for exact match")
  void matches_withExactMatch_shouldReturnTrue() {
    // Arrange
    Permission permission = new Permission("customers:read", "Read Customers", null);

    // Act & Assert
    assertThat(permission.matches("customers", "read")).isTrue();
  }

  @Test
  @DisplayName("matches() should return false for different resource")
  void matches_withDifferentResource_shouldReturnFalse() {
    // Arrange
    Permission permission = new Permission("customers:read", "Read Customers", null);

    // Act & Assert
    assertThat(permission.matches("orders", "read")).isFalse();
  }

  @Test
  @DisplayName("matches() should return false for different action")
  void matches_withDifferentAction_shouldReturnFalse() {
    // Arrange
    Permission permission = new Permission("customers:read", "Read Customers", null);

    // Act & Assert
    assertThat(permission.matches("customers", "write")).isFalse();
  }

  @Test
  @DisplayName("matchesWildcard() should match with global wildcard")
  void matchesWildcard_withGlobalWildcard_shouldMatchAnything() {
    // Arrange
    Permission permission = new Permission("*:*", "Global Admin", null);

    // Act & Assert
    assertThat(permission.matchesWildcard("customers:read")).isTrue();
    assertThat(permission.matchesWildcard("admin:permissions")).isTrue();
    assertThat(permission.matchesWildcard("any:thing")).isTrue();
  }

  @Test
  @DisplayName("matchesWildcard() should match with resource wildcard")
  void matchesWildcard_withResourceWildcard_shouldMatchResource() {
    // Arrange
    Permission permission = new Permission("*:read", "Read All", null);

    // Act & Assert
    assertThat(permission.matchesWildcard("customers:read")).isTrue();
    assertThat(permission.matchesWildcard("orders:read")).isTrue();
    assertThat(permission.matchesWildcard("customers:write")).isFalse();
  }

  @Test
  @DisplayName("matchesWildcard() should match with action wildcard")
  void matchesWildcard_withActionWildcard_shouldMatchAction() {
    // Arrange
    Permission permission = new Permission("customers:*", "All Customer Actions", null);

    // Act & Assert
    assertThat(permission.matchesWildcard("customers:read")).isTrue();
    assertThat(permission.matchesWildcard("customers:write")).isTrue();
    assertThat(permission.matchesWildcard("orders:read")).isFalse();
  }

  @Test
  @DisplayName("matchesWildcard() should handle invalid format")
  void matchesWildcard_withInvalidFormat_shouldReturnFalse() {
    // Arrange
    Permission permission = new Permission("customers:read", "Read Customers", null);

    // Act & Assert
    assertThat(permission.matchesWildcard("invalid-format")).isFalse();
    assertThat(permission.matchesWildcard("")).isFalse();
  }

  // ===== Persistence Tests (Integration) =====

  @Test
  @Transactional
  @DisplayName("Should persist and retrieve permission")
  void persistence_shouldSaveAndRetrieve() {
    // Arrange
    Permission permission =
        new Permission("test:permission", "Test Permission", "Test Description");

    // Act
    permission.persist();
    Permission found = Permission.findById(permission.getId());

    // Assert
    assertThat(found).isNotNull();
    assertThat(found.getPermissionCode()).isEqualTo("test:permission");
    assertThat(found.getName()).isEqualTo("Test Permission");
    assertThat(found.getDescription()).isEqualTo("Test Description");
    assertThat(found.getResource()).isEqualTo("test");
    assertThat(found.getAction()).isEqualTo("permission");
    assertThat(found.getCreatedAt()).isNotNull();
    assertThat(found.getCreatedAt()).isBeforeOrEqualTo(Instant.now());

    // Cleanup
    permission.delete();
  }

  @Test
  @Transactional
  @DisplayName("Should find permission by code using named query")
  void namedQuery_findByCode_shouldWork() {
    // Arrange
    Permission permission = new Permission("test:findbycode", "Test Find By Code", null);
    permission.persist();

    // Act
    Permission found =
        (Permission)
            Permission.find(
                    "#Permission.findByCode",
                    io.quarkus.panache.common.Parameters.with("code", "test:findbycode"))
                .firstResult();

    // Assert
    assertThat(found).isNotNull();
    assertThat(found.getId()).isEqualTo(permission.getId());

    // Cleanup
    permission.delete();
  }

  @Test
  @Transactional
  @DisplayName("Should find permissions by resource using named query")
  void namedQuery_findByResource_shouldWork() {
    // Clean up any existing test permissions first to ensure isolation
    Permission.delete("permissionCode like ?1", "test:%");
    Permission.flush();

    // Arrange
    Permission perm1 = new Permission("test:read", "Test Read", null);
    Permission perm2 = new Permission("test:write", "Test Write", null);
    Permission perm3 = new Permission("other:read", "Other Read", null);

    perm1.persist();
    perm2.persist();
    perm3.persist();
    Permission.flush();

    // Act
    List<Permission> found =
        Permission.find(
                "#Permission.findByResource",
                io.quarkus.panache.common.Parameters.with("resource", "test"))
            .list();

    // Assert
    assertThat(found).hasSize(2);
    assertThat(found)
        .extracting(Permission::getPermissionCode)
        .containsExactlyInAnyOrder("test:read", "test:write");

    // Cleanup
    perm1.delete();
    perm2.delete();
    perm3.delete();
  }

  // ===== Equals and HashCode Tests =====

  @Test
  @DisplayName("equals() should return true for same instance")
  void equals_sameInstance_shouldReturnTrue() {
    // Arrange
    Permission permission = new Permission("test:equal", "Test", null);

    // Act & Assert
    assertThat(permission).isEqualTo(permission);
  }

  @Test
  @DisplayName("equals() should return false for different types")
  void equals_differentType_shouldReturnFalse() {
    // Arrange
    Permission permission = new Permission("test:equal", "Test", null);

    // Act & Assert
    assertThat(permission).isNotEqualTo("not a permission");
    assertThat(permission).isNotEqualTo(null);
  }

  @Test
  @Transactional
  @DisplayName("equals() should compare by ID")
  void equals_shouldCompareById() {
    // Arrange
    Permission perm1 = new Permission("test:equal1", "Test 1", null);
    Permission perm2 = new Permission("test:equal2", "Test 2", null);

    perm1.persist();
    perm2.persist();

    // Same ID
    Permission perm1Copy = Permission.findById(perm1.getId());

    // Act & Assert
    assertThat(perm1).isEqualTo(perm1Copy);
    assertThat(perm1).isNotEqualTo(perm2);
    assertThat(perm1.hashCode()).isEqualTo(perm1Copy.hashCode());
    assertThat(perm1.hashCode()).isNotEqualTo(perm2.hashCode());

    // Cleanup
    perm1.delete();
    perm2.delete();
  }

  // ===== toString Tests =====

  @Test
  @DisplayName("toString() should include key fields")
  void toString_shouldIncludeKeyFields() {
    // Arrange
    Permission permission = new Permission("test:string", "Test ToString", "Description");

    // Act
    String result = permission.toString();

    // Assert
    assertThat(result).contains("Permission{");
    assertThat(result).contains("permissionCode='test:string'");
    assertThat(result).contains("name='Test ToString'");
  }
}

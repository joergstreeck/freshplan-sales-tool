package de.freshplan.domain.permission.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit Tests für Permission Entity (ohne DB).
 *
 * <p>Sprint 2.1.4: Migriert von @QuarkusTest zu Plain JUnit (~15s Ersparnis).
 *
 * <p>Testet Business-Logik ohne Persistence.
 *
 * @see TEST_DEBUGGING_GUIDE.md
 */
@Tag("unit")
@DisplayName("Permission Entity Unit Tests")
class PermissionUnitTest {

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

  // ===== Equals and HashCode Tests (ohne DB) =====

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

  // ===== Edge Cases =====

  @Test
  @DisplayName("Should handle empty resource or action in wildcard matching")
  void matchesWildcard_withEmptyParts_shouldReturnFalse() {
    // Arrange
    Permission permission = new Permission("customers:read", "Read", null);

    // Act & Assert
    assertThat(permission.matchesWildcard(":")).isFalse();
    assertThat(permission.matchesWildcard("customers:")).isFalse();
    assertThat(permission.matchesWildcard(":read")).isFalse();
  }
}

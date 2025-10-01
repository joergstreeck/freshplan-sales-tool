package de.freshplan.domain.permission.entity;

import static org.assertj.core.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Persistence Tests für Permission Entity.
 *
 * <p>Sprint 2.1.4: Bereinigt - Business-Logik-Tests in PermissionUnitTest.
 *
 * <p>Testet nur DB-Operationen (persist, namedQueries, equals/hashCode).
 *
 * <p>Für Business-Logik-Tests siehe {@link PermissionUnitTest}.
 *
 * @see TEST_DEBUGGING_GUIDE.md
 */
@QuarkusTest
@Tag("integration")
@DisplayName("Permission Entity Persistence Tests")
class PermissionTest {

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

  // ===== Equals and HashCode Tests (with DB) =====

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
}

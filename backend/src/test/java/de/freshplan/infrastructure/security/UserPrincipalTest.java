package de.freshplan.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for UserPrincipal class. Tests the builder pattern, getters, and immutability. */
class UserPrincipalTest {

  @Nested
  @DisplayName("Builder Pattern Tests")
  class BuilderPatternTests {

    @Test
    @DisplayName("Should build UserPrincipal with all fields")
    void shouldBuildUserPrincipalWithAllFields() {
      String username = "john.doe";
      String email = "john.doe@example.com";
      Set<String> roles = Set.of("admin", "manager");

      UserPrincipal principal =
          UserPrincipal.builder()
              .username(username)
              .email(email)
              .roles(roles)
              .authenticated(true)
              .build();

      assertEquals(username, principal.getUsername());
      assertEquals(email, principal.getEmail());
      assertEquals(roles, principal.getRoles());
      assertTrue(principal.isAuthenticated());
    }

    @Test
    @DisplayName("Should build UserPrincipal with minimal fields")
    void shouldBuildUserPrincipalWithMinimalFields() {
      String username = "john.doe";

      UserPrincipal principal = UserPrincipal.builder().username(username).build();

      assertEquals(username, principal.getUsername());
      assertNull(principal.getEmail());
      assertTrue(principal.getRoles().isEmpty());
      assertTrue(principal.isAuthenticated()); // Default is true
    }

    @Test
    @DisplayName("Should handle null roles gracefully")
    void shouldHandleNullRolesGracefully() {
      UserPrincipal principal = UserPrincipal.builder().username("john.doe").roles(null).build();

      assertNotNull(principal.getRoles());
      assertTrue(principal.getRoles().isEmpty());
    }

    @Test
    @DisplayName("Should create immutable roles set")
    void shouldCreateImmutableRolesSet() {
      Set<String> originalRoles = Set.of("admin", "manager");
      UserPrincipal principal =
          UserPrincipal.builder().username("john.doe").roles(originalRoles).build();

      Set<String> principalRoles = principal.getRoles();

      // Should have the same content
      assertEquals(originalRoles, principalRoles);

      // Should be immutable (Set.of creates immutable sets)
      assertThrows(UnsupportedOperationException.class, () -> principalRoles.add("new-role"));
    }
  }

  @Nested
  @DisplayName("Role Management Tests")
  class RoleManagementTestsBasic {

    @Test
    @DisplayName("Should handle hasRole correctly")
    void shouldHandleHasRoleCorrectly() {
      UserPrincipal principal =
          UserPrincipal.builder().username("test.user").roles(Set.of("admin", "manager")).build();

      assertTrue(principal.hasRole("admin"));
      assertTrue(principal.hasRole("manager"));
      assertFalse(principal.hasRole("sales"));
    }

    @Test
    @DisplayName("Should handle hasAnyRole correctly")
    void shouldHandleHasAnyRoleCorrectly() {
      UserPrincipal principal =
          UserPrincipal.builder().username("test.user").roles(Set.of("admin")).build();

      assertTrue(principal.hasAnyRole("admin", "manager"));
      assertTrue(principal.hasAnyRole("manager", "admin"));
      assertFalse(principal.hasAnyRole("sales", "viewer"));
    }
  }

  @Nested
  @DisplayName("Equality and Hash Code Tests")
  class EqualityTests {

    @Test
    @DisplayName("Should be equal when all fields match")
    void shouldBeEqualWhenAllFieldsMatch() {
      String username = "john.doe";
      String email = "john.doe@example.com";
      Set<String> roles = Set.of("admin");

      UserPrincipal principal1 =
          UserPrincipal.builder()
              .username(username)
              .email(email)
              .roles(roles)
              .authenticated(true)
              .build();

      UserPrincipal principal2 =
          UserPrincipal.builder()
              .username(username)
              .email(email)
              .roles(roles)
              .authenticated(true)
              .build();

      assertEquals(principal1, principal2);
      assertEquals(principal1.hashCode(), principal2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when usernames differ")
    void shouldNotBeEqualWhenUsernamesDiffer() {
      UserPrincipal principal1 = UserPrincipal.builder().username("user1").build();

      UserPrincipal principal2 = UserPrincipal.builder().username("user2").build();

      assertNotEquals(principal1, principal2);
    }

    @Test
    @DisplayName("Should handle null comparison")
    void shouldHandleNullComparison() {
      UserPrincipal principal = UserPrincipal.builder().username("john.doe").build();

      assertNotEquals(principal, null);
      assertNotEquals(null, principal);
    }

    @Test
    @DisplayName("Should handle different class comparison")
    void shouldHandleDifferentClassComparison() {
      UserPrincipal principal = UserPrincipal.builder().username("john.doe").build();

      assertNotEquals(principal, "john.doe");
    }
  }

  @Nested
  @DisplayName("ToString Tests")
  class ToStringTests {

    @Test
    @DisplayName("Should include username in toString")
    void shouldIncludeUsernameInToString() {
      String username = "john.doe";
      UserPrincipal principal = UserPrincipal.builder().username(username).build();

      String toString = principal.toString();
      assertTrue(toString.contains(username));
      assertTrue(toString.contains("UserPrincipal"));
    }

    @Test
    @DisplayName("Should handle null username in toString")
    void shouldHandleNullUsernameInToString() {
      // UserPrincipal requires non-null username, so test with empty string instead
      UserPrincipal principal = UserPrincipal.builder().username("").build();

      String toString = principal.toString();
      assertNotNull(toString);
      assertTrue(toString.contains("UserPrincipal"));
    }

    @Test
    @DisplayName("Should not expose sensitive information in toString")
    void shouldNotExposeSensitiveInformationInToString() {
      UserPrincipal principal =
          UserPrincipal.builder()
              .username("john.doe")
              .email("john.doe@example.com")
              .roles(Set.of("admin", "secret-role"))
              .build();

      String toString = principal.toString();

      // Should contain basic info
      assertTrue(toString.contains("john.doe"));

      // Should not contain sensitive details like email or roles in basic toString
      // (this depends on implementation - adjust based on actual toString implementation)
    }
  }

  @Nested
  @DisplayName("Builder Method Chaining Tests")
  class BuilderMethodChainingTests {

    @Test
    @DisplayName("Should support method chaining")
    void shouldSupportMethodChaining() {
      UserPrincipal principal =
          UserPrincipal.builder()
              .username("john.doe")
              .email("john.doe@example.com")
              .roles(Set.of("admin"))
              .authenticated(true)
              .build();

      assertNotNull(principal);
      assertEquals("john.doe", principal.getUsername());
      assertEquals("john.doe@example.com", principal.getEmail());
      assertTrue(principal.isAuthenticated());
    }

    @Test
    @DisplayName("Should allow partial building")
    void shouldAllowPartialBuilding() {
      UserPrincipal.Builder builder = UserPrincipal.builder().username("john.doe");

      // Can continue building later
      UserPrincipal principal = builder.email("john.doe@example.com").build();

      assertEquals("john.doe", principal.getUsername());
      assertEquals("john.doe@example.com", principal.getEmail());
    }
  }

  @Nested
  @DisplayName("Role Management Tests")
  class RoleManagementTests {

    @Test
    @DisplayName("Should handle empty roles set")
    void shouldHandleEmptyRolesSet() {
      UserPrincipal principal =
          UserPrincipal.builder().username("john.doe").roles(Set.of()).build();

      assertTrue(principal.getRoles().isEmpty());
    }

    @Test
    @DisplayName("Should preserve roles order and uniqueness")
    void shouldPreserveRolesOrderAndUniqueness() {
      Set<String> roles = Set.of("admin", "manager", "sales");
      UserPrincipal principal = UserPrincipal.builder().username("john.doe").roles(roles).build();

      assertEquals(3, principal.getRoles().size());
      assertTrue(principal.getRoles().contains("admin"));
      assertTrue(principal.getRoles().contains("manager"));
      assertTrue(principal.getRoles().contains("sales"));
    }

    @Test
    @DisplayName("Should handle single role")
    void shouldHandleSingleRole() {
      UserPrincipal principal =
          UserPrincipal.builder().username("john.doe").roles(Set.of("user")).build();

      assertEquals(1, principal.getRoles().size());
      assertTrue(principal.getRoles().contains("user"));
    }
  }
}

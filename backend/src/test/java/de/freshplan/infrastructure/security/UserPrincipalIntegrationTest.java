package de.freshplan.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for UserPrincipal class.
 * These tests call the actual UserPrincipal methods to increase coverage.
 */
class UserPrincipalIntegrationTest {

  @Nested
  @DisplayName("Builder and Creation Tests")
  class BuilderAndCreationTests {

    @Test
    @DisplayName("Should build UserPrincipal with all fields")
    void shouldBuildUserPrincipalWithAllFields() {
      String username = "john.doe";
      String email = "john.doe@example.com";
      Set<String> roles = Set.of("admin", "manager");

      UserPrincipal principal = UserPrincipal.builder()
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

      UserPrincipal principal = UserPrincipal.builder()
          .username(username)
          .build();

      assertEquals(username, principal.getUsername());
      assertNull(principal.getEmail());
      assertTrue(principal.getRoles().isEmpty());
      assertTrue(principal.isAuthenticated()); // Default is true
    }

    @Test
    @DisplayName("Should create system user correctly")
    void shouldCreateSystemUserCorrectly() {
      UserPrincipal systemUser = UserPrincipal.system();
      
      assertNotNull(systemUser);
      assertEquals("system", systemUser.getUsername());
      assertEquals("system@freshplan.de", systemUser.getEmail());
      assertTrue(systemUser.getRoles().contains("system"));
      assertTrue(systemUser.isAuthenticated());
    }

    @Test
    @DisplayName("Should create anonymous user correctly")
    void shouldCreateAnonymousUserCorrectly() {
      UserPrincipal anonymousUser = UserPrincipal.anonymous();
      
      assertNotNull(anonymousUser);
      assertEquals("anonymous", anonymousUser.getUsername());
      assertNull(anonymousUser.getEmail());
      assertTrue(anonymousUser.getRoles().isEmpty());
      assertFalse(anonymousUser.isAuthenticated());
    }
  }

  @Nested
  @DisplayName("Role Management Tests")
  class RoleManagementTests {

    @Test
    @DisplayName("Should check hasRole correctly")
    void shouldCheckHasRoleCorrectly() {
      UserPrincipal principal = UserPrincipal.builder()
          .username("test.user")
          .roles(Set.of("admin", "manager"))
          .build();

      assertTrue(principal.hasRole("admin"));
      assertTrue(principal.hasRole("manager"));
      assertFalse(principal.hasRole("sales"));
      assertFalse(principal.hasRole("viewer"));
    }

    @Test
    @DisplayName("Should check hasAnyRole correctly")
    void shouldCheckHasAnyRoleCorrectly() {
      UserPrincipal principal = UserPrincipal.builder()
          .username("test.user")
          .roles(Set.of("admin"))
          .build();

      assertTrue(principal.hasAnyRole("admin", "manager"));
      assertTrue(principal.hasAnyRole("manager", "admin"));
      assertTrue(principal.hasAnyRole("admin"));
      assertFalse(principal.hasAnyRole("sales", "viewer"));
      assertFalse(principal.hasAnyRole());
    }

    @Test
    @DisplayName("Should handle empty roles")
    void shouldHandleEmptyRoles() {
      UserPrincipal principal = UserPrincipal.builder()
          .username("test.user")
          .roles(Set.of())
          .build();

      assertFalse(principal.hasRole("admin"));
      assertFalse(principal.hasRole(""));
      assertFalse(principal.hasAnyRole("admin", "manager"));
      assertTrue(principal.getRoles().isEmpty());
    }

    @Test
    @DisplayName("Should handle null roles gracefully")
    void shouldHandleNullRolesGracefully() {
      UserPrincipal principal = UserPrincipal.builder()
          .username("test.user")
          .roles(null)
          .build();

      assertFalse(principal.hasRole("admin"));
      assertFalse(principal.hasAnyRole("admin", "manager"));
      assertTrue(principal.getRoles().isEmpty());
    }
  }

  @Nested
  @DisplayName("Equality and HashCode Tests")
  class EqualityAndHashCodeTests {

    @Test
    @DisplayName("Should be equal when all fields match")
    void shouldBeEqualWhenAllFieldsMatch() {
      String username = "john.doe";
      String email = "john.doe@example.com";
      Set<String> roles = Set.of("admin");

      UserPrincipal principal1 = UserPrincipal.builder()
          .username(username)
          .email(email)
          .roles(roles)
          .authenticated(true)
          .build();

      UserPrincipal principal2 = UserPrincipal.builder()
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
      UserPrincipal principal1 = UserPrincipal.builder()
          .username("user1")
          .build();

      UserPrincipal principal2 = UserPrincipal.builder()
          .username("user2")
          .build();

      assertNotEquals(principal1, principal2);
    }

    @Test
    @DisplayName("Should not be equal when authentication differs")
    void shouldNotBeEqualWhenAuthenticationDiffers() {
      UserPrincipal principal1 = UserPrincipal.builder()
          .username("user1")
          .authenticated(true)
          .build();

      UserPrincipal principal2 = UserPrincipal.builder()
          .username("user1")
          .authenticated(false)
          .build();

      assertNotEquals(principal1, principal2);
    }

    @Test
    @DisplayName("Should handle null comparison")
    void shouldHandleNullComparison() {
      UserPrincipal principal = UserPrincipal.builder()
          .username("john.doe")
          .build();

      assertNotEquals(principal, null);
      assertNotEquals(null, principal);
    }

    @Test
    @DisplayName("Should handle different class comparison")
    void shouldHandleDifferentClassComparison() {
      UserPrincipal principal = UserPrincipal.builder()
          .username("john.doe")
          .build();

      assertNotEquals(principal, "john.doe");
      assertNotEquals(principal, 42);
    }
  }

  @Nested
  @DisplayName("ToString Tests")
  class ToStringTests {

    @Test
    @DisplayName("Should include username in toString")
    void shouldIncludeUsernameInToString() {
      String username = "john.doe";
      UserPrincipal principal = UserPrincipal.builder()
          .username(username)
          .build();

      String toString = principal.toString();
      assertTrue(toString.contains(username));
      assertTrue(toString.contains("UserPrincipal"));
    }

    @Test
    @DisplayName("Should include email in toString")
    void shouldIncludeEmailInToString() {
      String email = "john.doe@example.com";
      UserPrincipal principal = UserPrincipal.builder()
          .username("john.doe")
          .email(email)
          .build();

      String toString = principal.toString();
      assertTrue(toString.contains(email));
    }

    @Test
    @DisplayName("Should include roles in toString")
    void shouldIncludeRolesInToString() {
      Set<String> roles = Set.of("admin", "manager");
      UserPrincipal principal = UserPrincipal.builder()
          .username("john.doe")
          .roles(roles)
          .build();

      String toString = principal.toString();
      assertTrue(toString.contains("admin"));
      assertTrue(toString.contains("manager"));
    }

    @Test
    @DisplayName("Should include authentication status in toString")
    void shouldIncludeAuthenticationStatusInToString() {
      UserPrincipal principal = UserPrincipal.builder()
          .username("john.doe")
          .authenticated(false)
          .build();

      String toString = principal.toString();
      assertTrue(toString.contains("authenticated=false"));
    }
  }

  @Nested
  @DisplayName("Builder Method Chaining Tests")
  class BuilderMethodChainingTests {

    @Test
    @DisplayName("Should support method chaining")
    void shouldSupportMethodChaining() {
      UserPrincipal principal = UserPrincipal.builder()
          .username("john.doe")
          .email("john.doe@example.com")
          .roles(Set.of("admin"))
          .authenticated(true)
          .build();

      assertNotNull(principal);
      assertEquals("john.doe", principal.getUsername());
      assertEquals("john.doe@example.com", principal.getEmail());
      assertTrue(principal.isAuthenticated());
      assertTrue(principal.hasRole("admin"));
    }

    @Test
    @DisplayName("Should allow partial building")
    void shouldAllowPartialBuilding() {
      UserPrincipal.Builder builder = UserPrincipal.builder()
          .username("john.doe");

      // Can continue building later
      UserPrincipal principal = builder
          .email("john.doe@example.com")
          .build();

      assertEquals("john.doe", principal.getUsername());
      assertEquals("john.doe@example.com", principal.getEmail());
    }
  }

  @Nested
  @DisplayName("Edge Cases and Validation Tests")
  class EdgeCasesAndValidationTests {

    @Test
    @DisplayName("Should handle multiple role checks")
    void shouldHandleMultipleRoleChecks() {
      UserPrincipal principal = UserPrincipal.builder()
          .username("john.doe")
          .roles(Set.of("admin", "manager", "sales"))
          .build();

      assertTrue(principal.hasRole("admin"));
      assertTrue(principal.hasRole("manager"));
      assertTrue(principal.hasRole("sales"));
      assertFalse(principal.hasRole("viewer"));

      assertTrue(principal.hasAnyRole("admin"));
      assertTrue(principal.hasAnyRole("viewer", "admin"));
      assertTrue(principal.hasAnyRole("manager", "sales"));
      assertFalse(principal.hasAnyRole("viewer", "customer"));
    }

    @Test
    @DisplayName("Should create immutable roles set")
    void shouldCreateImmutableRolesSet() {
      Set<String> originalRoles = Set.of("admin", "manager");
      UserPrincipal principal = UserPrincipal.builder()
          .username("john.doe")
          .roles(originalRoles)
          .build();

      Set<String> principalRoles = principal.getRoles();
      
      // Should have the same content
      assertEquals(originalRoles, principalRoles);
      
      // Should be immutable
      assertThrows(UnsupportedOperationException.class, 
          () -> principalRoles.add("new-role"));
    }
  }
}
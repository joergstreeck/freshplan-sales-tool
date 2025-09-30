package de.freshplan.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.test.SecurityDisabledTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

/**
 * Integration tests for CurrentUserProducer. These tests use the real CDI container to test the
 * actual producer methods.
 */
@QuarkusTest
@Disabled("TEMPORARY: Sprint 2.1.4 CI Performance Fix")
@Tag("migrate")
@TestProfile(SecurityDisabledTestProfile.class)
class CurrentUserProducerIntegrationTest {

  @Inject CurrentUserProducer currentUserProducer;

  @Nested
  @DisplayName("Development Mode Tests")
  class DevelopmentModeTests {

    @Test
    @DisplayName("Should return user principal in development mode")
    void shouldReturnUserPrincipalInDevelopmentMode() {
      // In test/dev mode with security disabled, should return system or anonymous user
      UserPrincipal result = currentUserProducer.getCurrentUser();

      assertNotNull(result);
      assertNotNull(result.getUsername());

      // Should be either system user or anonymous user
      assertTrue(
          result.getUsername().equals("system") || result.getUsername().equals("anonymous"),
          "Expected system or anonymous user, got: " + result.getUsername());
    }

    @Test
    @DisplayName("Should handle multiple calls consistently")
    void shouldHandleMultipleCallsConsistently() {
      UserPrincipal result1 = currentUserProducer.getCurrentUser();
      UserPrincipal result2 = currentUserProducer.getCurrentUser();

      assertNotNull(result1);
      assertNotNull(result2);

      // Both should return users (not necessarily the same due to JWT/SecurityContext changes)
      assertNotNull(result1.getUsername());
      assertNotNull(result2.getUsername());
    }
  }

  @Nested
  @DisplayName("Static Factory Methods Tests")
  class StaticFactoryMethodsTests {

    @Test
    @DisplayName("Should create system user with correct properties")
    void shouldCreateSystemUserWithCorrectProperties() {
      UserPrincipal systemUser = UserPrincipal.system();

      assertNotNull(systemUser);
      assertEquals("system", systemUser.getUsername());
      assertEquals("system@freshplan.de", systemUser.getEmail());
      assertTrue(systemUser.getRoles().contains("system"));
      assertTrue(systemUser.isAuthenticated());

      // Test role checking
      assertTrue(systemUser.hasRole("system"));
      assertTrue(systemUser.hasAnyRole("system", "admin"));
      assertFalse(systemUser.hasRole("admin"));
    }

    @Test
    @DisplayName("Should create anonymous user with correct properties")
    void shouldCreateAnonymousUserWithCorrectProperties() {
      UserPrincipal anonymousUser = UserPrincipal.anonymous();

      assertNotNull(anonymousUser);
      assertEquals("anonymous", anonymousUser.getUsername());
      assertNull(anonymousUser.getEmail());
      assertTrue(anonymousUser.getRoles().isEmpty());
      assertFalse(anonymousUser.isAuthenticated());

      // Test role checking
      assertFalse(anonymousUser.hasRole("system"));
      assertFalse(anonymousUser.hasRole("admin"));
      assertFalse(anonymousUser.hasAnyRole("admin", "manager"));
    }
  }

  @Nested
  @DisplayName("UserPrincipal Builder Coverage Tests")
  class UserPrincipalBuilderCoverageTests {

    @Test
    @DisplayName("Should test all builder methods")
    void shouldTestAllBuilderMethods() {
      String username = "test.user";
      String email = "test@example.com";
      Set<String> roles = Set.of("admin", "manager");
      boolean authenticated = true;

      UserPrincipal principal =
          UserPrincipal.builder()
              .username(username)
              .email(email)
              .roles(roles)
              .authenticated(authenticated)
              .build();

      // Test all getters to increase coverage
      assertEquals(username, principal.getUsername());
      assertEquals(email, principal.getEmail());
      assertEquals(roles, principal.getRoles());
      assertEquals(authenticated, principal.isAuthenticated());

      // Test role methods
      assertTrue(principal.hasRole("admin"));
      assertTrue(principal.hasRole("manager"));
      assertFalse(principal.hasRole("sales"));

      assertTrue(principal.hasAnyRole("admin"));
      assertTrue(principal.hasAnyRole("admin", "sales"));
      assertFalse(principal.hasAnyRole("sales", "viewer"));
    }

    @Test
    @DisplayName("Should test builder with default values")
    void shouldTestBuilderWithDefaultValues() {
      UserPrincipal principal = UserPrincipal.builder().username("minimal.user").build();

      assertEquals("minimal.user", principal.getUsername());
      assertNull(principal.getEmail());
      assertTrue(principal.getRoles().isEmpty());
      assertTrue(principal.isAuthenticated()); // Default is true
    }

    @Test
    @DisplayName("Should test builder with null roles")
    void shouldTestBuilderWithNullRoles() {
      UserPrincipal principal =
          UserPrincipal.builder().username("null.roles.user").roles(null).build();

      assertEquals("null.roles.user", principal.getUsername());
      assertNotNull(principal.getRoles());
      assertTrue(principal.getRoles().isEmpty());
    }

    @Test
    @DisplayName("Should test builder with false authentication")
    void shouldTestBuilderWithFalseAuthentication() {
      UserPrincipal principal =
          UserPrincipal.builder().username("unauthenticated.user").authenticated(false).build();

      assertEquals("unauthenticated.user", principal.getUsername());
      assertFalse(principal.isAuthenticated());
    }
  }

  @Nested
  @DisplayName("Comprehensive Coverage Tests")
  class ComprehensiveCoverageTests {

    @Test
    @DisplayName("Should test equals with different objects")
    void shouldTestEqualsWithDifferentObjects() {
      UserPrincipal principal1 =
          UserPrincipal.builder()
              .username("user1")
              .email("user1@example.com")
              .roles(Set.of("admin"))
              .authenticated(true)
              .build();

      UserPrincipal principal2 =
          UserPrincipal.builder()
              .username("user1")
              .email("user1@example.com")
              .roles(Set.of("admin"))
              .authenticated(true)
              .build();

      UserPrincipal principal3 =
          UserPrincipal.builder()
              .username("user2")
              .email("user1@example.com")
              .roles(Set.of("admin"))
              .authenticated(true)
              .build();

      // Test equals
      assertEquals(principal1, principal2);
      assertNotEquals(principal1, principal3);
      assertNotEquals(principal1, null);
      assertNotEquals(principal1, "string");

      // Test hashCode
      assertEquals(principal1.hashCode(), principal2.hashCode());
    }

    @Test
    @DisplayName("Should test toString with various configurations")
    void shouldTestToStringWithVariousConfigurations() {
      UserPrincipal principal1 =
          UserPrincipal.builder()
              .username("user1")
              .email("user1@example.com")
              .roles(Set.of("admin", "manager"))
              .authenticated(true)
              .build();

      UserPrincipal principal2 =
          UserPrincipal.builder().username("user2").authenticated(false).build();

      String toString1 = principal1.toString();
      String toString2 = principal2.toString();

      // Both should contain UserPrincipal and username
      assertTrue(toString1.contains("UserPrincipal"));
      assertTrue(toString1.contains("user1"));
      assertTrue(toString1.contains("user1@example.com"));
      assertTrue(toString1.contains("admin"));
      assertTrue(toString1.contains("true"));

      assertTrue(toString2.contains("UserPrincipal"));
      assertTrue(toString2.contains("user2"));
      assertTrue(toString2.contains("false"));
    }

    @Test
    @DisplayName("Should test all hasAnyRole variations")
    void shouldTestAllHasAnyRoleVariations() {
      UserPrincipal principal =
          UserPrincipal.builder()
              .username("multi.role.user")
              .roles(Set.of("admin", "manager"))
              .build();

      // Test single role
      assertTrue(principal.hasAnyRole("admin"));
      assertTrue(principal.hasAnyRole("manager"));
      assertFalse(principal.hasAnyRole("sales"));

      // Test multiple roles
      assertTrue(principal.hasAnyRole("admin", "sales"));
      assertTrue(principal.hasAnyRole("sales", "admin"));
      assertTrue(principal.hasAnyRole("admin", "manager"));
      assertFalse(principal.hasAnyRole("sales", "viewer"));

      // Test with empty roles
      UserPrincipal emptyRolesPrincipal =
          UserPrincipal.builder().username("no.roles.user").roles(Set.of()).build();

      assertFalse(emptyRolesPrincipal.hasAnyRole("admin"));
      assertFalse(emptyRolesPrincipal.hasAnyRole("admin", "manager"));
    }
  }
}

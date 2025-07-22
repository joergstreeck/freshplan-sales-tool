package de.freshplan.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for SecurityContextProvider WITH security enabled.
 * These tests use @TestSecurity to simulate authenticated users.
 */
@QuarkusTest
@DisplayName("SecurityContextProvider Integration Tests (with Security)")
class SecurityContextProviderWithSecurityTest {

  @Inject SecurityContextProvider securityContextProvider;

  @Test
  @TestSecurity(user = "testuser", roles = {"admin", "manager"})
  @DisplayName("Should return authenticated user information")
  void shouldReturnAuthenticatedUserInfo() {
    // Given: User authenticated via @TestSecurity

    // When: Checking authentication status
    boolean isAuthenticated = securityContextProvider.isAuthenticated();
    String username = securityContextProvider.getUsername();
    Set<String> roles = securityContextProvider.getRoles();

    // Then: User should be authenticated with correct info
    assertTrue(isAuthenticated, "User should be authenticated");
    assertEquals("testuser", username, "Username should match");
    assertTrue(roles.contains("admin"), "Should have admin role");
    assertTrue(roles.contains("manager"), "Should have manager role");
  }

  @Test
  @TestSecurity(user = "salesuser", roles = {"sales"})
  @DisplayName("Should check roles correctly")
  void shouldCheckRolesCorrectly() {
    // Given: User with sales role only

    // When & Then: Role checks
    assertTrue(securityContextProvider.hasRole("sales"));
    assertFalse(securityContextProvider.hasRole("admin"));
    assertFalse(securityContextProvider.hasRole("manager"));
  }

  @Test
  @TestSecurity(user = "adminuser", roles = {"admin"})
  @DisplayName("Should require correct role")
  void shouldRequireCorrectRole() {
    // Given: User with admin role

    // When & Then: Should not throw for correct role
    assertDoesNotThrow(() -> securityContextProvider.requireRole("admin"));

    // When & Then: Should throw for missing role
    SecurityException exception = assertThrows(
        SecurityException.class,
        () -> securityContextProvider.requireRole("manager")
    );
    assertEquals("Role 'manager' required", exception.getMessage());
  }

  @Test
  @TestSecurity(user = "multiuser", roles = {"sales", "viewer"})
  @DisplayName("Should require any of multiple roles")
  void shouldRequireAnyOfMultipleRoles() {
    // Given: User with sales and viewer roles

    // When & Then: Should pass if user has at least one required role
    assertDoesNotThrow(() -> 
        securityContextProvider.requireAnyRole("admin", "sales", "manager")
    );

    // When & Then: Should fail if user has none of the required roles
    SecurityException exception = assertThrows(
        SecurityException.class,
        () -> securityContextProvider.requireAnyRole("admin", "manager")
    );
    assertTrue(exception.getMessage().contains("One of roles"));
  }

  @Test
  @DisplayName("Should handle unauthenticated access")
  void shouldHandleUnauthenticatedAccess() {
    // Given: No @TestSecurity annotation (anonymous user)

    // When & Then: Should not be authenticated
    assertFalse(securityContextProvider.isAuthenticated());
    assertNull(securityContextProvider.getUsername());
    assertNull(securityContextProvider.getUserId());
    assertTrue(securityContextProvider.getRoles().isEmpty());
  }

  @Test
  @DisplayName("Should throw on unauthorized access")
  void shouldThrowOnUnauthorizedAccess() {
    // Given: No @TestSecurity annotation (anonymous user)

    // When & Then: Should throw when authentication required
    SecurityException exception = assertThrows(
        SecurityException.class,
        () -> securityContextProvider.requireAuthentication()
    );
    assertEquals("Authentication required", exception.getMessage());
  }

  @Test
  @TestSecurity(
      user = "complexuser",
      roles = {"admin", "manager", "sales"}
  )
  @DisplayName("Should handle complex user with multiple roles")
  void shouldHandleComplexUserWithMultipleRoles() {
    // Given: User with multiple roles

    // When: Getting user details
    String username = securityContextProvider.getUsername();
    Set<String> roles = securityContextProvider.getRoles();

    // Then: All information should be available
    assertEquals("complexuser", username); // Falls back to principal name
    assertEquals(3, roles.size());
    assertTrue(roles.containsAll(Set.of("admin", "manager", "sales")));
  }

  @Test
  @TestSecurity(user = "testuser", roles = {"viewer"})
  @DisplayName("Should get authentication details")
  void shouldGetAuthenticationDetails() {
    // Given: Authenticated user

    // When: Getting authentication details
    var details = securityContextProvider.getAuthenticationDetails();

    // Then: Details should be populated
    assertTrue(details.isAuthenticated());
    assertEquals("testuser", details.getUsername());
    assertTrue(details.getRoles().contains("viewer"));
    // Note: Some fields may be null depending on JWT content
  }
}
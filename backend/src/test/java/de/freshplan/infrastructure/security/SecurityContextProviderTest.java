package de.freshplan.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.freshplan.test.SecurityDisabledTestProfile;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.TestTransaction;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Comprehensive test suite for SecurityContextProvider. Tests authentication, authorization, token
 * handling, and audit functionality.
 */
@QuarkusTest
@Tag("migrate")
@TestProfile(SecurityDisabledTestProfile.class)
@TestTransaction  // Sprint 2.1.4 Fix: Provide transaction context which includes RequestContext
class SecurityContextProviderTest {

  @Inject SecurityContextProvider securityContextProvider;

  @Mock private SecurityIdentity mockSecurityIdentity;
  @Mock private Instance<JsonWebToken> mockJwtInstance;
  @Mock private JsonWebToken mockJwt;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Nested
  @DisplayName("Authentication Tests")
  class AuthenticationTests {

    @Test
    @TestSecurity(
        user = "testuser",
        roles = {"admin", "manager"})
    @DisplayName("Should return true when user is authenticated")
    void shouldReturnTrueWhenAuthenticated() {
      assertTrue(securityContextProvider.isAuthenticated());
    }

    @Test
    @DisplayName("Should return false when user is anonymous")
    void shouldReturnFalseWhenAnonymous() {
      // In disabled security profile, user is anonymous by default
      assertFalse(securityContextProvider.isAuthenticated());
    }

    @Test
    @TestSecurity(user = "testuser")
    @DisplayName("Should require authentication and pass when authenticated")
    void shouldRequireAuthenticationAndPass() {
      assertDoesNotThrow(() -> securityContextProvider.requireAuthentication());
    }

    @Test
    @DisplayName("Should require authentication and throw when not authenticated")
    void shouldRequireAuthenticationAndThrow() {
      SecurityException exception =
          assertThrows(
              SecurityException.class, () -> securityContextProvider.requireAuthentication());
      assertEquals("Authentication required", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("Role-Based Access Control Tests")
  class RoleBasedAccessControlTests {

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Should return true when user has required role")
    void shouldReturnTrueWhenUserHasRole() {
      assertTrue(securityContextProvider.hasRole("admin"));
    }

    @Test
    @TestSecurity(
        user = "manager",
        roles = {"manager"})
    @DisplayName("Should return false when user lacks required role")
    void shouldReturnFalseWhenUserLacksRole() {
      assertFalse(securityContextProvider.hasRole("admin"));
    }

    @Test
    @TestSecurity(
        user = "admin",
        roles = {"admin"})
    @DisplayName("Should require role and pass when user has role")
    void shouldRequireRoleAndPass() {
      assertDoesNotThrow(() -> securityContextProvider.requireRole("admin"));
    }

    @Test
    @TestSecurity(
        user = "manager",
        roles = {"manager"})
    @DisplayName("Should require role and throw when user lacks role")
    void shouldRequireRoleAndThrow() {
      SecurityException exception =
          assertThrows(SecurityException.class, () -> securityContextProvider.requireRole("admin"));
      assertEquals("Role 'admin' required", exception.getMessage());
    }

    @Test
    @TestSecurity(
        user = "manager",
        roles = {"manager", "sales"})
    @DisplayName("Should require any role and pass when user has one of required roles")
    void shouldRequireAnyRoleAndPass() {
      assertDoesNotThrow(() -> securityContextProvider.requireAnyRole("admin", "manager"));
    }

    @Test
    @TestSecurity(
        user = "viewer",
        roles = {"viewer"})
    @DisplayName("Should require any role and throw when user lacks all roles")
    void shouldRequireAnyRoleAndThrow() {
      SecurityException exception =
          assertThrows(
              SecurityException.class,
              () -> securityContextProvider.requireAnyRole("admin", "manager"));
      assertTrue(exception.getMessage().contains("One of roles"));
      assertTrue(exception.getMessage().contains("required"));
    }

    @Test
    @TestSecurity(
        user = "manager",
        roles = {"admin", "manager", "sales"})
    @DisplayName("Should return correct roles set")
    void shouldReturnCorrectRoles() {
      Set<String> roles = securityContextProvider.getRoles();
      assertEquals(3, roles.size());
      assertTrue(roles.contains("admin"));
      assertTrue(roles.contains("manager"));
      assertTrue(roles.contains("sales"));
    }
  }

  @Nested
  @DisplayName("User Information Extraction Tests")
  class UserInformationTests {

    @Test
    @TestSecurity(user = "testuser")
    @DisplayName("Should return username from security identity")
    void shouldReturnUsernameFromSecurityIdentity() {
      assertEquals("testuser", securityContextProvider.getUsername());
    }

    @Test
    @DisplayName("Should return null username when not authenticated")
    void shouldReturnNullUsernameWhenNotAuthenticated() {
      assertNull(securityContextProvider.getUsername());
    }

    @Test
    @DisplayName("Should return null user ID when not authenticated")
    void shouldReturnNullUserIdWhenNotAuthenticated() {
      assertNull(securityContextProvider.getUserId());
    }

    @Test
    @DisplayName("Should return null email when not authenticated")
    void shouldReturnNullEmailWhenNotAuthenticated() {
      assertNull(securityContextProvider.getEmail());
    }
  }

  @Nested
  @DisplayName("JWT Token Handling Tests")
  class JwtTokenTests {

    @Test
    @DisplayName("Should return true for token expired when no JWT available")
    void shouldReturnTrueForTokenExpiredWhenNoJwt() {
      assertTrue(securityContextProvider.isTokenExpired(300));
    }
  }

  @Nested
  @DisplayName("JWT Mock Tests - Advanced Scenarios")
  class JwtMockTests {

    private SecurityContextProvider testProvider;

    @BeforeEach
    void setUpMocks() {
      testProvider = new SecurityContextProvider();
      testProvider.securityIdentity = mockSecurityIdentity;
      testProvider.jwtInstance = mockJwtInstance;
    }

    @Test
    @DisplayName("Should extract UUID from JWT subject")
    void shouldExtractUuidFromJwtSubject() {
      UUID testUuid = UUID.randomUUID();
      when(mockSecurityIdentity.isAnonymous()).thenReturn(false);
      when(mockJwtInstance.isUnsatisfied()).thenReturn(false);
      when(mockJwtInstance.get()).thenReturn(mockJwt);
      when(mockJwt.getSubject()).thenReturn(testUuid.toString());

      UUID result = testProvider.getUserId();
      assertEquals(testUuid, result);
    }

    @Test
    @DisplayName("Should generate UUID from non-UUID subject")
    void shouldGenerateUuidFromNonUuidSubject() {
      when(mockSecurityIdentity.isAnonymous()).thenReturn(false);
      when(mockJwtInstance.isUnsatisfied()).thenReturn(false);
      when(mockJwtInstance.get()).thenReturn(mockJwt);
      when(mockJwt.getSubject()).thenReturn("user123");

      UUID result = testProvider.getUserId();
      assertNotNull(result);
      // Should consistently generate the same UUID for the same string
      UUID expected = UUID.nameUUIDFromBytes("user123".getBytes());
      assertEquals(expected, result);
    }

    @Test
    @DisplayName("Should return null user ID for null subject")
    void shouldReturnNullUserIdForNullSubject() {
      when(mockSecurityIdentity.isAnonymous()).thenReturn(false);
      when(mockJwtInstance.isUnsatisfied()).thenReturn(false);
      when(mockJwtInstance.get()).thenReturn(mockJwt);
      when(mockJwt.getSubject()).thenReturn(null);

      UUID result = testProvider.getUserId();
      assertNull(result);
    }

    @Test
    @DisplayName("Should extract email from JWT")
    void shouldExtractEmailFromJwt() {
      when(mockSecurityIdentity.isAnonymous()).thenReturn(false);
      when(mockJwtInstance.isUnsatisfied()).thenReturn(false);
      when(mockJwtInstance.get()).thenReturn(mockJwt);
      when(mockJwt.getClaim("email")).thenReturn("test@example.com");

      String result = testProvider.getEmail();
      assertEquals("test@example.com", result);
    }

    @Test
    @DisplayName("Should extract preferred_username from JWT")
    void shouldExtractPreferredUsernameFromJwt() {
      when(mockSecurityIdentity.isAnonymous()).thenReturn(false);
      when(mockJwtInstance.isUnsatisfied()).thenReturn(false);
      when(mockJwtInstance.get()).thenReturn(mockJwt);
      when(mockJwt.getClaim("preferred_username")).thenReturn("john.doe");

      String result = testProvider.getUsername();
      assertEquals("john.doe", result);
    }

    @Test
    @DisplayName("Should fallback to username claim")
    void shouldFallbackToUsernameClaim() {
      when(mockSecurityIdentity.isAnonymous()).thenReturn(false);
      when(mockJwtInstance.isUnsatisfied()).thenReturn(false);
      when(mockJwtInstance.get()).thenReturn(mockJwt);
      when(mockJwt.getClaim("preferred_username")).thenReturn(null);
      when(mockJwt.getClaim("username")).thenReturn("john.doe");

      String result = testProvider.getUsername();
      assertEquals("john.doe", result);
    }

    @Test
    @DisplayName("Should fallback to name claim")
    void shouldFallbackToNameClaim() {
      when(mockSecurityIdentity.isAnonymous()).thenReturn(false);
      when(mockJwtInstance.isUnsatisfied()).thenReturn(false);
      when(mockJwtInstance.get()).thenReturn(mockJwt);
      when(mockJwt.getClaim("preferred_username")).thenReturn(null);
      when(mockJwt.getClaim("username")).thenReturn(null);
      when(mockJwt.getClaim("name")).thenReturn("John Doe");

      String result = testProvider.getUsername();
      assertEquals("John Doe", result);
    }

    @Test
    @DisplayName("Should extract token expiration from JWT")
    void shouldExtractTokenExpirationFromJwt() {
      long expTimestamp = 1672531200L; // 2023-01-01 00:00:00 UTC
      when(mockSecurityIdentity.isAnonymous()).thenReturn(false);
      when(mockJwtInstance.isUnsatisfied()).thenReturn(false);
      when(mockJwtInstance.get()).thenReturn(mockJwt);
      when(mockJwt.getClaim("exp")).thenReturn(expTimestamp);

      Instant result = testProvider.getTokenExpiration();
      assertEquals(Instant.ofEpochSecond(expTimestamp), result);
    }

    @Test
    @DisplayName("Should return null for missing exp claim")
    void shouldReturnNullForMissingExpClaim() {
      when(mockSecurityIdentity.isAnonymous()).thenReturn(false);
      when(mockJwtInstance.isUnsatisfied()).thenReturn(false);
      when(mockJwtInstance.get()).thenReturn(mockJwt);
      when(mockJwt.getClaim("exp")).thenReturn(null);

      Instant result = testProvider.getTokenExpiration();
      assertNull(result);
    }

    @Test
    @DisplayName("Should detect expired token")
    void shouldDetectExpiredToken() {
      long pastTimestamp = Instant.now().minusSeconds(3600).getEpochSecond(); // 1 hour ago
      when(mockSecurityIdentity.isAnonymous()).thenReturn(false);
      when(mockJwtInstance.isUnsatisfied()).thenReturn(false);
      when(mockJwtInstance.get()).thenReturn(mockJwt);
      when(mockJwt.getClaim("exp")).thenReturn(pastTimestamp);

      boolean result = testProvider.isTokenExpired(0);
      assertTrue(result);
    }

    @Test
    @DisplayName("Should detect token expiring soon")
    void shouldDetectTokenExpiringSoon() {
      long soonTimestamp = Instant.now().plusSeconds(30).getEpochSecond(); // 30 seconds from now
      when(mockSecurityIdentity.isAnonymous()).thenReturn(false);
      when(mockJwtInstance.isUnsatisfied()).thenReturn(false);
      when(mockJwtInstance.get()).thenReturn(mockJwt);
      when(mockJwt.getClaim("exp")).thenReturn(soonTimestamp);

      boolean result = testProvider.isTokenExpired(60); // Check if expires within 60 seconds
      assertTrue(result);
    }

    @Test
    @DisplayName("Should detect valid token")
    void shouldDetectValidToken() {
      long futureTimestamp = Instant.now().plusSeconds(3600).getEpochSecond(); // 1 hour from now
      when(mockSecurityIdentity.isAnonymous()).thenReturn(false);
      when(mockJwtInstance.isUnsatisfied()).thenReturn(false);
      when(mockJwtInstance.get()).thenReturn(mockJwt);
      when(mockJwt.getClaim("exp")).thenReturn(futureTimestamp);

      boolean result = testProvider.isTokenExpired(300); // Check if expires within 5 minutes
      assertFalse(result);
    }

    @Test
    @DisplayName("Should extract session ID from JWT")
    void shouldExtractSessionIdFromJwt() {
      when(mockSecurityIdentity.isAnonymous()).thenReturn(false);
      when(mockJwtInstance.isUnsatisfied()).thenReturn(false);
      when(mockJwtInstance.get()).thenReturn(mockJwt);
      when(mockJwt.getClaim("sid")).thenReturn("session_12345");

      String result = testProvider.getSessionId();
      assertEquals("session_12345", result);
    }

    @Test
    @DisplayName("Should return JWT when available")
    void shouldReturnJwtWhenAvailable() {
      when(mockJwtInstance.isUnsatisfied()).thenReturn(false);
      when(mockJwtInstance.get()).thenReturn(mockJwt);

      JsonWebToken result = testProvider.getJwt();
      assertEquals(mockJwt, result);
    }
  }

  @Nested
  @DisplayName("Authentication Details Tests")
  class AuthenticationDetailsTests {


    @Test
    @DisplayName("Should create anonymous details correctly")
    void shouldCreateAnonymousDetailsCorrectly() {
      SecurityContextProvider.AuthenticationDetails anonymous =
          SecurityContextProvider.AuthenticationDetails.anonymous();

      assertFalse(anonymous.isAuthenticated());
      assertNull(anonymous.getUserId());
      assertNull(anonymous.getUsername());
      assertNull(anonymous.getEmail());
      assertNull(anonymous.getSessionId());
      assertNull(anonymous.getTokenExpiration());
      assertTrue(anonymous.getRoles().isEmpty());
    }
  }

  @Nested
  @DisplayName("Builder Pattern Tests")
  class BuilderPatternTests {

    @Test
    @DisplayName("Should build authentication details with all fields")
    void shouldBuildAuthenticationDetailsWithAllFields() {
      String userId = UUID.randomUUID().toString();
      String username = "testuser";
      String email = "test@example.com";
      Set<String> roles = Set.of("admin", "manager");
      String sessionId = "session123";
      Instant tokenExpiration = Instant.now().plusSeconds(3600);

      SecurityContextProvider.AuthenticationDetails details =
          SecurityContextProvider.AuthenticationDetails.builder()
              .userId(userId)
              .username(username)
              .email(email)
              .roles(roles)
              .sessionId(sessionId)
              .tokenExpiration(tokenExpiration)
              .authenticated(true)
              .build();

      assertEquals(userId, details.getUserId());
      assertEquals(username, details.getUsername());
      assertEquals(email, details.getEmail());
      assertEquals(roles, details.getRoles());
      assertEquals(sessionId, details.getSessionId());
      assertEquals(tokenExpiration, details.getTokenExpiration());
      assertTrue(details.isAuthenticated());
    }

    @Test
    @DisplayName("Should handle null roles in builder")
    void shouldHandleNullRolesInBuilder() {
      SecurityContextProvider.AuthenticationDetails details =
          SecurityContextProvider.AuthenticationDetails.builder()
              .username("testuser")
              .roles(null)
              .authenticated(true)
              .build();

      assertTrue(details.getRoles().isEmpty());
    }
  }

  @Nested
  @DisplayName("Authentication Details Integration Tests")
  class AuthenticationDetailsIntegrationTests {

    private SecurityContextProvider testProvider;

    @BeforeEach
    void setUpMocks() {
      testProvider = new SecurityContextProvider();
      testProvider.securityIdentity = mockSecurityIdentity;
      testProvider.jwtInstance = mockJwtInstance;
    }

    @Test
    @DisplayName("Should create complete authentication details from JWT")
    void shouldCreateCompleteAuthenticationDetailsFromJwt() {
      UUID testUuid = UUID.randomUUID();
      String testUsername = "john.doe";
      String testEmail = "john.doe@example.com";
      String testSessionId = "session_123";
      long expTimestamp = Instant.now().plusSeconds(3600).getEpochSecond();
      Set<String> testRoles = Set.of("admin", "manager");

      when(mockSecurityIdentity.isAnonymous()).thenReturn(false);
      when(mockSecurityIdentity.getRoles()).thenReturn(testRoles);
      when(mockJwtInstance.isUnsatisfied()).thenReturn(false);
      when(mockJwtInstance.get()).thenReturn(mockJwt);
      when(mockJwt.getSubject()).thenReturn(testUuid.toString());
      when(mockJwt.getClaim("preferred_username")).thenReturn(testUsername);
      when(mockJwt.getClaim("email")).thenReturn(testEmail);
      when(mockJwt.getClaim("sid")).thenReturn(testSessionId);
      when(mockJwt.getClaim("exp")).thenReturn(expTimestamp);

      SecurityContextProvider.AuthenticationDetails details =
          testProvider.getAuthenticationDetails();

      assertTrue(details.isAuthenticated());
      assertEquals(testUuid.toString(), details.getUserId());
      assertEquals(testUsername, details.getUsername());
      assertEquals(testEmail, details.getEmail());
      assertEquals(testRoles, details.getRoles());
      assertEquals(testSessionId, details.getSessionId());
      assertEquals(Instant.ofEpochSecond(expTimestamp), details.getTokenExpiration());
    }

    @Test
    @DisplayName("Should handle partial JWT information gracefully")
    void shouldHandlePartialJwtInformationGracefully() {
      Set<String> testRoles = Set.of("user");

      when(mockSecurityIdentity.isAnonymous()).thenReturn(false);
      when(mockSecurityIdentity.getRoles()).thenReturn(testRoles);
      when(mockJwtInstance.isUnsatisfied()).thenReturn(false);
      when(mockJwtInstance.get()).thenReturn(mockJwt);
      when(mockJwt.getSubject()).thenReturn(null);
      when(mockJwt.getClaim("preferred_username")).thenReturn(null);
      when(mockJwt.getClaim("username")).thenReturn(null);
      when(mockJwt.getClaim("name")).thenReturn(null);
      when(mockJwt.getClaim("email")).thenReturn(null);
      when(mockJwt.getClaim("sid")).thenReturn(null);
      when(mockJwt.getClaim("exp")).thenReturn(null);

      // Fallback to security identity name
      when(mockSecurityIdentity.getPrincipal()).thenReturn(() -> "fallback_user");

      SecurityContextProvider.AuthenticationDetails details =
          testProvider.getAuthenticationDetails();

      assertTrue(details.isAuthenticated());
      assertNull(details.getUserId());
      assertEquals("fallback_user", details.getUsername());
      assertNull(details.getEmail());
      assertEquals(testRoles, details.getRoles());
      assertNull(details.getSessionId());
      assertNull(details.getTokenExpiration());
    }
  }

  @Nested
  @DisplayName("Edge Cases and Error Handling")
  class EdgeCasesTests {

  }

  // Moved methods from nested classes that need @ActivateRequestContext annotation
  // due to CDI limitation with interceptor bindings on nested class methods

  @Test
  @ActivateRequestContext
  @DisplayName("Should return null token expiration when JWT not available")
  void shouldReturnNullTokenExpirationWhenJwtNotAvailable() {
    assertNull(securityContextProvider.getTokenExpiration());
  }

  @Test
  @ActivateRequestContext
  @DisplayName("Should return null session ID when JWT not available")
  void shouldReturnNullSessionIdWhenJwtNotAvailable() {
    assertNull(securityContextProvider.getSessionId());
  }

  @Test
  @ActivateRequestContext
  @DisplayName("Should return null JWT when instance is unsatisfied")
  void shouldReturnNullJwtWhenInstanceUnsatisfied() {
    assertNull(securityContextProvider.getJwt());
  }

  @Test
  @ActivateRequestContext
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager"})
  @DisplayName("Should return authenticated details when user is authenticated")
  void shouldReturnAuthenticatedDetailsWhenAuthenticated() {
    SecurityContextProvider.AuthenticationDetails details =
        securityContextProvider.getAuthenticationDetails();

    assertTrue(details.isAuthenticated());
    assertEquals("testuser", details.getUsername());
    assertEquals(2, details.getRoles().size());
    assertTrue(details.getRoles().contains("admin"));
    assertTrue(details.getRoles().contains("manager"));
  }

  @Test
  @ActivateRequestContext
  @DisplayName("Should return anonymous details when user is not authenticated")
  void shouldReturnAnonymousDetailsWhenNotAuthenticated() {
    SecurityContextProvider.AuthenticationDetails details =
        securityContextProvider.getAuthenticationDetails();

    assertFalse(details.isAuthenticated());
    assertNull(details.getUsername());
    assertTrue(details.getRoles().isEmpty());
  }

  @Test
  @ActivateRequestContext
  @DisplayName("Should handle empty role name gracefully")
  void shouldHandleEmptyRoleNameGracefully() {
    assertFalse(securityContextProvider.hasRole(""));
    assertFalse(securityContextProvider.hasRole(null));
  }

  @Test
  @ActivateRequestContext
  @TestSecurity(
      user = "testuser",
      roles = {"admin"})
  @DisplayName("Should handle multiple role requirements correctly")
  void shouldHandleMultipleRoleRequirementsCorrectly() {
    // User has admin role
    assertDoesNotThrow(() -> securityContextProvider.requireAnyRole("admin", "manager", "sales"));

    // User doesn't have viewer role but has admin
    assertDoesNotThrow(() -> securityContextProvider.requireAnyRole("viewer", "admin"));

    // User doesn't have any of these roles
    assertThrows(
        SecurityException.class,
        () -> securityContextProvider.requireAnyRole("viewer", "customer"));
  }

  @Test
  @ActivateRequestContext
  @DisplayName("Should return empty set for roles when not authenticated")
  void shouldReturnEmptySetForRolesWhenNotAuthenticated() {
    Set<String> roles = securityContextProvider.getRoles();
    assertNotNull(roles);
    assertTrue(roles.isEmpty());
  }
}

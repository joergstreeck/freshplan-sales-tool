package de.freshplan.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.inject.Instance;
import java.security.Principal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for SecurityContextProvider WITHOUT Quarkus context.
 * These tests use mocks to test the logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityContextProvider Unit Tests")
class SecurityContextProviderUnitTest {

  @Mock private SecurityIdentity securityIdentity;
  @Mock private Instance<JsonWebToken> jwtInstance;
  @Mock private JsonWebToken jwt;
  @Mock private Principal principal;

  @InjectMocks private SecurityContextProvider securityContextProvider;

  @BeforeEach
  void setUp() {
    lenient().when(principal.getName()).thenReturn("testuser");
  }

  @Test
  @DisplayName("Should detect unauthenticated user")
  void shouldDetectUnauthenticatedUser() {
    // Given: Anonymous security identity
    when(securityIdentity.isAnonymous()).thenReturn(true);

    // When & Then
    assertFalse(securityContextProvider.isAuthenticated());
    assertNull(securityContextProvider.getUserId());
    assertNull(securityContextProvider.getUsername());
  }

  @Test
  @DisplayName("Should detect authenticated user")
  void shouldDetectAuthenticatedUser() {
    // Given: Authenticated security identity
    when(securityIdentity.isAnonymous()).thenReturn(false);

    // When & Then
    assertTrue(securityContextProvider.isAuthenticated());
  }

  @Test
  @DisplayName("Should get username from JWT preferred_username claim")
  void shouldGetUsernameFromJwtPreferredUsername() {
    // Given
    when(securityIdentity.isAnonymous()).thenReturn(false);
    when(jwtInstance.isUnsatisfied()).thenReturn(false);
    when(jwtInstance.get()).thenReturn(jwt);
    when(jwt.getClaim("preferred_username")).thenReturn("john.doe");

    // When
    String username = securityContextProvider.getUsername();

    // Then
    assertEquals("john.doe", username);
  }

  @Test
  @DisplayName("Should fall back to principal name when JWT not available")
  void shouldFallBackToPrincipalName() {
    // Given
    when(securityIdentity.isAnonymous()).thenReturn(false);
    when(securityIdentity.getPrincipal()).thenReturn(principal);
    when(jwtInstance.isUnsatisfied()).thenReturn(true);

    // When
    String username = securityContextProvider.getUsername();

    // Then
    assertEquals("testuser", username);
  }

  @Test
  @DisplayName("Should get user ID from JWT subject")
  void shouldGetUserIdFromJwtSubject() {
    // Given
    String userId = UUID.randomUUID().toString();
    when(securityIdentity.isAnonymous()).thenReturn(false);
    when(jwtInstance.isUnsatisfied()).thenReturn(false);
    when(jwtInstance.get()).thenReturn(jwt);
    when(jwt.getSubject()).thenReturn(userId);

    // When
    UUID result = securityContextProvider.getUserId();

    // Then
    assertEquals(UUID.fromString(userId), result);
  }

  @Test
  @DisplayName("Should handle non-UUID subject")
  void shouldHandleNonUuidSubject() {
    // Given
    when(securityIdentity.isAnonymous()).thenReturn(false);
    when(jwtInstance.isUnsatisfied()).thenReturn(false);
    when(jwtInstance.get()).thenReturn(jwt);
    when(jwt.getSubject()).thenReturn("non-uuid-subject");

    // When
    UUID result = securityContextProvider.getUserId();

    // Then
    assertNotNull(result); // Should generate deterministic UUID from string
  }

  @Test
  @DisplayName("Should get email from JWT")
  void shouldGetEmailFromJwt() {
    // Given
    when(securityIdentity.isAnonymous()).thenReturn(false);
    when(jwtInstance.isUnsatisfied()).thenReturn(false);
    when(jwtInstance.get()).thenReturn(jwt);
    when(jwt.getClaim("email")).thenReturn("test@example.com");

    // When
    String email = securityContextProvider.getEmail();

    // Then
    assertEquals("test@example.com", email);
  }

  @Test
  @DisplayName("Should check roles correctly")
  void shouldCheckRolesCorrectly() {
    // Given
    when(securityIdentity.getRoles()).thenReturn(Set.of("admin", "manager"));
    when(securityIdentity.hasRole("admin")).thenReturn(true);
    when(securityIdentity.hasRole("sales")).thenReturn(false);

    // When & Then
    assertTrue(securityContextProvider.hasRole("admin"));
    assertFalse(securityContextProvider.hasRole("sales"));
    assertEquals(Set.of("admin", "manager"), securityContextProvider.getRoles());
  }

  @Test
  @DisplayName("Should get token expiration")
  void shouldGetTokenExpiration() {
    // Given
    long expirationTime = Instant.now().plusSeconds(3600).getEpochSecond();
    when(securityIdentity.isAnonymous()).thenReturn(false);
    when(jwtInstance.isUnsatisfied()).thenReturn(false);
    when(jwtInstance.get()).thenReturn(jwt);
    when(jwt.getClaim("exp")).thenReturn(expirationTime);

    // When
    Instant expiration = securityContextProvider.getTokenExpiration();

    // Then
    assertNotNull(expiration);
    assertEquals(expirationTime, expiration.getEpochSecond());
  }

  @Test
  @DisplayName("Should check if token is expired")
  void shouldCheckIfTokenIsExpired() {
    // Given: Token expires in 30 seconds
    long expirationTime = Instant.now().plusSeconds(30).getEpochSecond();
    when(securityIdentity.isAnonymous()).thenReturn(false);
    when(jwtInstance.isUnsatisfied()).thenReturn(false);
    when(jwtInstance.get()).thenReturn(jwt);
    when(jwt.getClaim("exp")).thenReturn(expirationTime);

    // When & Then
    assertFalse(securityContextProvider.isTokenExpired(0)); // Not expired yet
    assertTrue(securityContextProvider.isTokenExpired(60)); // Expires within 60 seconds
  }

  @Test
  @DisplayName("Should throw SecurityException when authentication required")
  void shouldThrowWhenAuthenticationRequired() {
    // Given
    when(securityIdentity.isAnonymous()).thenReturn(true);

    // When & Then
    SecurityException exception = assertThrows(
        SecurityException.class,
        () -> securityContextProvider.requireAuthentication()
    );
    assertEquals("Authentication required", exception.getMessage());
  }

  @Test
  @DisplayName("Should throw SecurityException when role required")
  void shouldThrowWhenRoleRequired() {
    // Given
    when(securityIdentity.isAnonymous()).thenReturn(false);
    when(securityIdentity.getPrincipal()).thenReturn(principal);
    when(securityIdentity.hasRole("admin")).thenReturn(false);

    // When & Then
    SecurityException exception = assertThrows(
        SecurityException.class,
        () -> securityContextProvider.requireRole("admin")
    );
    assertEquals("Role 'admin' required", exception.getMessage());
  }

  @Test
  @DisplayName("Should require any of multiple roles")
  void shouldRequireAnyOfMultipleRoles() {
    // Given
    when(securityIdentity.isAnonymous()).thenReturn(false);
    when(securityIdentity.getPrincipal()).thenReturn(principal);
    when(securityIdentity.getRoles()).thenReturn(Set.of("sales", "viewer"));

    // When & Then: Should pass
    assertDoesNotThrow(() -> 
        securityContextProvider.requireAnyRole("admin", "sales", "manager")
    );

    // When & Then: Should fail
    assertThrows(
        SecurityException.class,
        () -> securityContextProvider.requireAnyRole("admin", "manager")
    );
  }

  @Test
  @DisplayName("Should get session ID from JWT")
  void shouldGetSessionIdFromJwt() {
    // Given
    when(securityIdentity.isAnonymous()).thenReturn(false);
    when(jwtInstance.isUnsatisfied()).thenReturn(false);
    when(jwtInstance.get()).thenReturn(jwt);
    when(jwt.getClaim("sid")).thenReturn("session-123");

    // When
    String sessionId = securityContextProvider.getSessionId();

    // Then
    assertEquals("session-123", sessionId);
  }

  @Test
  @DisplayName("Should build anonymous authentication details")
  void shouldBuildAnonymousAuthenticationDetails() {
    // Given
    when(securityIdentity.isAnonymous()).thenReturn(true);

    // When
    var details = securityContextProvider.getAuthenticationDetails();

    // Then
    assertFalse(details.isAuthenticated());
    assertNull(details.getUserId());
    assertNull(details.getUsername());
    assertTrue(details.getRoles().isEmpty());
  }

  @Test
  @DisplayName("Should build complete authentication details")
  void shouldBuildCompleteAuthenticationDetails() {
    // Given
    String userId = UUID.randomUUID().toString();
    when(securityIdentity.isAnonymous()).thenReturn(false);
    when(securityIdentity.getRoles()).thenReturn(Set.of("admin", "manager"));
    when(jwtInstance.isUnsatisfied()).thenReturn(false);
    when(jwtInstance.get()).thenReturn(jwt);
    when(jwt.getSubject()).thenReturn(userId);
    when(jwt.getClaim("preferred_username")).thenReturn("john.doe");
    when(jwt.getClaim("email")).thenReturn("john@example.com");
    when(jwt.getClaim("sid")).thenReturn("session-456");
    when(jwt.getClaim("exp")).thenReturn(null); // No expiration for simplicity

    // When
    var details = securityContextProvider.getAuthenticationDetails();

    // Then
    assertTrue(details.isAuthenticated());
    assertEquals(userId, details.getUserId());
    assertEquals("john.doe", details.getUsername());
    assertEquals("john@example.com", details.getEmail());
    assertEquals(Set.of("admin", "manager"), details.getRoles());
    assertEquals("session-456", details.getSessionId());
  }
}
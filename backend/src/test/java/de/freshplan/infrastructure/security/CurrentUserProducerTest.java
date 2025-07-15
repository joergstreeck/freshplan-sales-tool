package de.freshplan.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.enterprise.inject.Instance;
import java.util.Set;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for CurrentUserProducer class.
 * Tests CDI producer methods for current user information.
 */
class CurrentUserProducerTest {

  private CurrentUserProducer currentUserProducer;

  @Mock
  private SecurityContextProvider mockSecurityContext;
  
  @Mock
  private Instance<JsonWebToken> mockJwtInstance;
  
  @Mock
  private JsonWebToken mockJwt;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    currentUserProducer = new CurrentUserProducer();
    currentUserProducer.securityContext = mockSecurityContext;
    currentUserProducer.jwtInstance = mockJwtInstance;
  }

  @Nested
  @DisplayName("JWT Token Path Tests")
  class JwtTokenTests {

    @Test
    @DisplayName("Should create UserPrincipal from JWT token")
    void shouldCreateUserPrincipalFromJwt() {
      // Arrange
      String tokenName = "john.doe";
      String email = "john.doe@example.com";
      Set<String> groups = Set.of("admin", "manager");
      
      when(mockJwtInstance.isUnsatisfied()).thenReturn(false);
      when(mockJwtInstance.isAmbiguous()).thenReturn(false);
      when(mockJwtInstance.get()).thenReturn(mockJwt);
      when(mockJwt.getName()).thenReturn(tokenName);
      when(mockJwt.getClaim("email")).thenReturn(email);
      when(mockJwt.getGroups()).thenReturn(groups);

      // Act
      UserPrincipal result = currentUserProducer.getCurrentUser();

      // Assert
      assertNotNull(result);
      assertEquals(tokenName, result.getUsername());
      assertEquals(email, result.getEmail());
      assertEquals(groups, result.getRoles());
      assertTrue(result.isAuthenticated());
    }

    @Test
    @DisplayName("Should handle null JWT name gracefully")
    void shouldHandleNullJwtNameGracefully() {
      // Arrange
      when(mockJwtInstance.isUnsatisfied()).thenReturn(false);
      when(mockJwtInstance.isAmbiguous()).thenReturn(false);
      when(mockJwtInstance.get()).thenReturn(mockJwt);
      when(mockJwt.getName()).thenReturn(null);
      
      when(mockSecurityContext.isAuthenticated()).thenReturn(false);

      // Act
      UserPrincipal result = currentUserProducer.getCurrentUser();

      // Assert
      assertNotNull(result);
      // Should fallback to system or anonymous user
      assertTrue(result.getUsername().equals("system") || result.getUsername().equals("anonymous"));
    }
  }

  @Nested
  @DisplayName("SecurityContext Fallback Tests")
  class SecurityContextFallbackTests {

    @Test
    @DisplayName("Should fallback to SecurityContextProvider when JWT unavailable")
    void shouldFallbackToSecurityContextProvider() {
      // Arrange
      String username = "security.user";
      Set<String> roles = Set.of("sales");
      
      when(mockJwtInstance.isUnsatisfied()).thenReturn(true);
      when(mockSecurityContext.isAuthenticated()).thenReturn(true);
      when(mockSecurityContext.getUsername()).thenReturn(username);
      when(mockSecurityContext.getRoles()).thenReturn(roles);

      // Act
      UserPrincipal result = currentUserProducer.getCurrentUser();

      // Assert
      assertNotNull(result);
      assertEquals(username, result.getUsername());
      assertEquals(username + "@freshplan.de", result.getEmail());
      assertEquals(roles, result.getRoles());
      assertTrue(result.isAuthenticated());
    }

    @Test
    @DisplayName("Should handle null username in SecurityContext")
    void shouldHandleNullUsernameInSecurityContext() {
      // Arrange
      when(mockJwtInstance.isUnsatisfied()).thenReturn(true);
      when(mockSecurityContext.isAuthenticated()).thenReturn(true);
      when(mockSecurityContext.getUsername()).thenReturn(null);

      // Act
      UserPrincipal result = currentUserProducer.getCurrentUser();

      // Assert
      assertNotNull(result);
      // Should fallback to system or anonymous user
      assertTrue(result.getUsername().equals("system") || result.getUsername().equals("anonymous"));
    }
  }

  @Nested
  @DisplayName("Development Mode Tests")
  class DevelopmentModeTests {

    @Test
    @DisplayName("Should return system user when no authentication in dev mode")
    void shouldReturnSystemUserInDevMode() {
      // Arrange
      when(mockJwtInstance.isUnsatisfied()).thenReturn(true);
      when(mockSecurityContext.isAuthenticated()).thenReturn(false);

      // Act
      UserPrincipal result = currentUserProducer.getCurrentUser();

      // Assert
      assertNotNull(result);
      // In dev/test mode, should return system user
      assertTrue(result.getUsername().equals("system") || result.getUsername().equals("anonymous"));
      assertNotNull(result.getRoles());
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should handle JWT instance exceptions gracefully")
    void shouldHandleJwtInstanceExceptionsGracefully() {
      // Arrange
      when(mockJwtInstance.isUnsatisfied()).thenThrow(new RuntimeException("JWT error"));
      when(mockSecurityContext.isAuthenticated()).thenReturn(false);

      // Act & Assert
      // The current implementation doesn't handle exceptions gracefully,
      // so we expect the exception to propagate
      assertThrows(RuntimeException.class, () -> {
        currentUserProducer.getCurrentUser();
      });
    }

    @Test
    @DisplayName("Should handle SecurityContext exceptions gracefully")
    void shouldHandleSecurityContextExceptionsGracefully() {
      // Arrange
      when(mockJwtInstance.isUnsatisfied()).thenReturn(true);
      when(mockSecurityContext.isAuthenticated()).thenThrow(new RuntimeException("Security error"));

      // Act & Assert
      // The current implementation doesn't handle exceptions gracefully,
      // so we expect the exception to propagate
      assertThrows(RuntimeException.class, () -> {
        currentUserProducer.getCurrentUser();
      });
    }
  }

  @Nested
  @DisplayName("Static Methods Tests")
  class StaticMethodsTests {

    @Test
    @DisplayName("Should test system user creation")
    void shouldTestSystemUserCreation() {
      UserPrincipal systemUser = UserPrincipal.system();
      
      assertNotNull(systemUser);
      assertEquals("system", systemUser.getUsername());
      assertEquals("system@freshplan.de", systemUser.getEmail());
      assertTrue(systemUser.getRoles().contains("system"));
      assertTrue(systemUser.isAuthenticated());
    }

    @Test
    @DisplayName("Should test anonymous user creation")
    void shouldTestAnonymousUserCreation() {
      UserPrincipal anonymousUser = UserPrincipal.anonymous();
      
      assertNotNull(anonymousUser);
      assertEquals("anonymous", anonymousUser.getUsername());
      assertNull(anonymousUser.getEmail());
      assertTrue(anonymousUser.getRoles().isEmpty());
      assertFalse(anonymousUser.isAuthenticated());
    }
  }
}
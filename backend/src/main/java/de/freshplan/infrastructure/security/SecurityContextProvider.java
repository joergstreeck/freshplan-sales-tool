package de.freshplan.infrastructure.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.Set;
import java.util.UUID;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Provides security context information for the current request. This class extracts user
 * information from the JWT token.
 */
@RequestScoped
public class SecurityContextProvider {

  @Inject SecurityIdentity securityIdentity;

  @Inject Instance<JsonWebToken> jwtInstance;

  /**
   * Get the current user's ID from the JWT token.
   *
   * @return User ID or null if not authenticated
   */
  public UUID getUserId() {
    if (!isAuthenticated() || jwtInstance.isUnsatisfied()) {
      return null;
    }

    JsonWebToken jwt = jwtInstance.get();

    // Try to get user ID from 'sub' claim
    String subject = jwt.getSubject();
    if (subject != null) {
      try {
        return UUID.fromString(subject);
      } catch (IllegalArgumentException e) {
        // Subject is not a UUID, use it as string identifier
        // In a real scenario, you might want to map this to a database user
        return UUID.nameUUIDFromBytes(subject.getBytes());
      }
    }

    return null;
  }

  /**
   * Get the current user's username.
   *
   * @return Username or null if not authenticated
   */
  public String getUsername() {
    if (!isAuthenticated()) {
      return null;
    }

    if (jwtInstance.isUnsatisfied()) {
      // Fall back to principal name when no JWT available
      return securityIdentity.getPrincipal().getName();
    }

    JsonWebToken jwt = jwtInstance.get();

    // Try different claims that might contain the username
    String username = jwt.getClaim("preferred_username");
    if (username == null) {
      username = jwt.getClaim("username");
    }
    if (username == null) {
      username = jwt.getClaim("name");
    }
    if (username == null) {
      username = securityIdentity.getPrincipal().getName();
    }

    return username;
  }

  /**
   * Get the current user's email.
   *
   * @return Email or null if not authenticated
   */
  public String getEmail() {
    if (!isAuthenticated() || jwtInstance.isUnsatisfied()) {
      return null;
    }

    JsonWebToken jwt = jwtInstance.get();
    return jwt.getClaim("email");
  }

  /**
   * Get the current user's roles.
   *
   * @return Set of roles or empty set if not authenticated
   */
  public Set<String> getRoles() {
    return securityIdentity.getRoles();
  }

  /**
   * Check if a user has a specific role.
   *
   * @param role Role to check
   * @return true if user has the role, false otherwise
   */
  public boolean hasRole(String role) {
    return securityIdentity.hasRole(role);
  }

  /**
   * Check if the current request is authenticated.
   *
   * @return true if authenticated, false otherwise
   */
  public boolean isAuthenticated() {
    return !securityIdentity.isAnonymous();
  }

  /**
   * Get the full security identity for advanced use cases.
   *
   * @return SecurityIdentity
   */
  public SecurityIdentity getSecurityIdentity() {
    return securityIdentity;
  }

  /**
   * Get the full JWT token for advanced use cases.
   *
   * @return JsonWebToken or null if not available
   */
  public JsonWebToken getJwt() {
    if (jwtInstance.isUnsatisfied()) {
      return null;
    }
    return jwtInstance.get();
  }
}

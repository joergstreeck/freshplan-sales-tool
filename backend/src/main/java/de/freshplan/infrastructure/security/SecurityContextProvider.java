package de.freshplan.infrastructure.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

/**
 * Provides security context information for the current request. This class extracts user
 * information from the JWT token.
 */
@RequestScoped
public class SecurityContextProvider {

  private static final Logger LOG = Logger.getLogger(SecurityContextProvider.class);

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
        return UUID.nameUUIDFromBytes(subject.getBytes(java.nio.charset.StandardCharsets.UTF_8));
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

  /**
   * Get token expiration time.
   *
   * @return Instant when token expires, or null if not available
   */
  public Instant getTokenExpiration() {
    if (!isAuthenticated() || jwtInstance.isUnsatisfied()) {
      return null;
    }

    JsonWebToken jwt = jwtInstance.get();
    Long exp = jwt.getClaim("exp");
    return exp != null ? Instant.ofEpochSecond(exp) : null;
  }

  /**
   * Check if the token is expired or expires within the given seconds.
   *
   * @param withinSeconds Buffer time in seconds
   * @return true if token is expired or expires soon
   */
  public boolean isTokenExpired(int withinSeconds) {
    Instant expiration = getTokenExpiration();
    if (expiration == null) {
      return true;
    }
    return expiration.isBefore(Instant.now().plusSeconds(withinSeconds));
  }

  /**
   * Get the session ID from the JWT token.
   *
   * @return Session ID or null if not available
   */
  public String getSessionId() {
    if (!isAuthenticated() || jwtInstance.isUnsatisfied()) {
      return null;
    }

    JsonWebToken jwt = jwtInstance.get();
    return jwt.getClaim("sid");
  }

  /**
   * Get detailed authentication information for audit logging.
   *
   * @return Authentication details
   */
  public AuthenticationDetails getAuthenticationDetails() {
    if (!isAuthenticated()) {
      return AuthenticationDetails.anonymous();
    }

    String userId = getUserId() != null ? getUserId().toString() : null;
    String username = getUsername();
    String email = getEmail();
    Set<String> roles = getRoles();
    String sessionId = getSessionId();
    Instant tokenExpiration = getTokenExpiration();

    // Log authentication access for audit purposes
    LOG.debugf("Authentication details accessed for user: %s (%s)", username, userId);

    return AuthenticationDetails.builder()
        .userId(userId)
        .username(username)
        .email(email)
        .roles(roles)
        .sessionId(sessionId)
        .tokenExpiration(tokenExpiration)
        .authenticated(true)
        .build();
  }

  /**
   * Require authentication and throw exception if not authenticated.
   *
   * @throws SecurityException if not authenticated
   */
  public void requireAuthentication() {
    if (!isAuthenticated()) {
      LOG.warn("Unauthorized access attempt detected");
      throw new SecurityException("Authentication required");
    }
  }

  /**
   * Require specific role and throw exception if not authorized.
   *
   * @param role Required role
   * @throws SecurityException if role not present
   */
  public void requireRole(String role) {
    requireAuthentication();
    if (!hasRole(role)) {
      LOG.warnf(
          "Unauthorized role access attempt. User: %s, Required role: %s, Actual roles: %s",
          getUsername(), role, getRoles());
      throw new SecurityException("Role '" + role + "' required");
    }
  }

  /**
   * Require any of the specified roles.
   *
   * @param roles Required roles (at least one)
   * @throws SecurityException if none of the roles are present
   */
  public void requireAnyRole(String... roles) {
    requireAuthentication();
    Set<String> userRoles = getRoles();

    for (String role : roles) {
      if (userRoles.contains(role)) {
        return;
      }
    }

    LOG.warnf(
        "Unauthorized role access attempt. User: %s, Required roles: %s, Actual roles: %s",
        getUsername(), Set.of(roles), userRoles);
    throw new SecurityException("One of roles " + Set.of(roles) + " required");
  }

  /** Helper class for authentication details. */
  public static class AuthenticationDetails {
    private final String userId;
    private final String username;
    private final String email;
    private final Set<String> roles;
    private final String sessionId;
    private final Instant tokenExpiration;
    private final boolean authenticated;

    private AuthenticationDetails(Builder builder) {
      this.userId = builder.userId;
      this.username = builder.username;
      this.email = builder.email;
      this.roles = Set.copyOf(builder.roles != null ? builder.roles : Set.of());
      this.sessionId = builder.sessionId;
      this.tokenExpiration = builder.tokenExpiration;
      this.authenticated = builder.authenticated;
    }

    public static AuthenticationDetails anonymous() {
      return builder().authenticated(false).build();
    }

    public static Builder builder() {
      return new Builder();
    }

    // Getters
    public String getUserId() {
      return userId;
    }

    public String getUsername() {
      return username;
    }

    public String getEmail() {
      return email;
    }

    public Set<String> getRoles() {
      return roles;
    }

    public String getSessionId() {
      return sessionId;
    }

    public Instant getTokenExpiration() {
      return tokenExpiration;
    }

    public boolean isAuthenticated() {
      return authenticated;
    }

    public static class Builder {
      private String userId;
      private String username;
      private String email;
      private Set<String> roles;
      private String sessionId;
      private Instant tokenExpiration;
      private boolean authenticated = false;

      public Builder userId(String userId) {
        this.userId = userId;
        return this;
      }

      public Builder username(String username) {
        this.username = username;
        return this;
      }

      public Builder email(String email) {
        this.email = email;
        return this;
      }

      public Builder roles(Set<String> roles) {
        this.roles = roles;
        return this;
      }

      public Builder sessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
      }

      public Builder tokenExpiration(Instant tokenExpiration) {
        this.tokenExpiration = tokenExpiration;
        return this;
      }

      public Builder authenticated(boolean authenticated) {
        this.authenticated = authenticated;
        return this;
      }

      public AuthenticationDetails build() {
        return new AuthenticationDetails(this);
      }
    }
  }
}

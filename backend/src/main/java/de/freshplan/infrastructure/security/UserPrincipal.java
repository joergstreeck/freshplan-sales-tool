package de.freshplan.infrastructure.security;

import java.util.Objects;
import java.util.Set;

/**
 * Immutable representation of the current user. This provides a clean API for accessing user
 * information throughout the application.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public final class UserPrincipal {
  private final String username;
  private final String email;
  private final Set<String> roles;
  private final boolean authenticated;

  private UserPrincipal(Builder builder) {
    this.username = Objects.requireNonNull(builder.username, "username must not be null");
    this.email = builder.email;
    this.roles = Set.copyOf(builder.roles != null ? builder.roles : Set.of());
    this.authenticated = builder.authenticated;
  }

  /** Creates a system user for background processes and development mode. */
  public static UserPrincipal system() {
    return builder()
        .username("system")
        .email("system@freshplan.de")
        .roles(Set.of("system"))
        .authenticated(true)
        .build();
  }

  /** Creates an anonymous user for unauthenticated requests. */
  public static UserPrincipal anonymous() {
    return builder().username("anonymous").email(null).roles(Set.of()).authenticated(false).build();
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

  public boolean isAuthenticated() {
    return authenticated;
  }

  public boolean hasRole(String role) {
    return roles.contains(role);
  }

  public boolean hasAnyRole(String... rolesToCheck) {
    for (String role : rolesToCheck) {
      if (roles.contains(role)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserPrincipal that = (UserPrincipal) o;
    return authenticated == that.authenticated
        && Objects.equals(username, that.username)
        && Objects.equals(email, that.email)
        && Objects.equals(roles, that.roles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, email, roles, authenticated);
  }

  @Override
  public String toString() {
    return "UserPrincipal{"
        + "username='"
        + username
        + '\''
        + ", email='"
        + email
        + '\''
        + ", roles="
        + roles
        + ", authenticated="
        + authenticated
        + '}';
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String username;
    private String email;
    private Set<String> roles;
    private boolean authenticated = true;

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

    public Builder authenticated(boolean authenticated) {
      this.authenticated = authenticated;
      return this;
    }

    public UserPrincipal build() {
      return new UserPrincipal(this);
    }
  }
}

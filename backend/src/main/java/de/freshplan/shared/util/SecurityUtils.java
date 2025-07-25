package de.freshplan.shared.util;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

/**
 * Security utility class for accessing current user information
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class SecurityUtils {

  @Inject SecurityIdentity securityIdentity;

  /** Get current user ID */
  public UUID getCurrentUserId() {
    if (securityIdentity == null || securityIdentity.isAnonymous()) {
      return null;
    }

    // Try to get UUID from principal name
    String principal = securityIdentity.getPrincipal().getName();
    try {
      return UUID.fromString(principal);
    } catch (IllegalArgumentException e) {
      // If principal is not a UUID, generate one based on username
      // In production, this should map to actual user ID from database
      return UUID.nameUUIDFromBytes(principal.getBytes());
    }
  }

  /** Get current user name */
  public String getCurrentUserName() {
    if (securityIdentity == null || securityIdentity.isAnonymous()) {
      return "SYSTEM";
    }

    // Try to get display name from attributes
    Object displayName = securityIdentity.getAttributes().get("name");
    if (displayName != null) {
      return displayName.toString();
    }

    return securityIdentity.getPrincipal().getName();
  }

  /** Get current user role */
  public String getCurrentUserRole() {
    if (securityIdentity == null || securityIdentity.isAnonymous()) {
      return "ANONYMOUS";
    }

    // Get primary role
    return securityIdentity.getRoles().stream().findFirst().orElse("USER");
  }

  /** Get current session ID */
  public UUID getCurrentSessionId() {
    // In a real implementation, this would come from session management
    // For now, generate based on security identity
    if (securityIdentity == null || securityIdentity.isAnonymous()) {
      return null;
    }

    return UUID.nameUUIDFromBytes(
        (securityIdentity.getPrincipal().getName() + System.currentTimeMillis()).getBytes());
  }

  /** Check if user has a specific role */
  public boolean hasRole(String role) {
    return securityIdentity != null && securityIdentity.hasRole(role);
  }

  /** Check if user is authenticated */
  public boolean isAuthenticated() {
    return securityIdentity != null && !securityIdentity.isAnonymous();
  }
}

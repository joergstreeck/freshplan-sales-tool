package de.freshplan.domain.permission.service;

import de.freshplan.domain.permission.entity.UserPermission;
import de.freshplan.infrastructure.security.SecurityContextProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SIMPLIFIED Permission Service for backend startup.
 *
 * <p>TEMPORARILY SIMPLIFIED - Permission entities not ready yet. Returns default permissions based
 * on roles from Keycloak context.
 *
 * <p>Will be replaced with full FC-009 implementation once database schema is ready.
 */
@ApplicationScoped
@Transactional
public class PermissionService {

  private static final Logger logger = LoggerFactory.getLogger(PermissionService.class);

  @Inject SecurityContextProvider securityProvider;
  
  @Inject Clock clock; // Timezone-safe clock injection

  /** Simplified permission check - uses Keycloak roles as permissions for now. */
  public boolean hasPermission(String permissionCode) {
    try {
      Set<String> roles = securityProvider.getRoles();

      // Super simple mapping: admin has all permissions
      if (roles.contains("admin")) {
        return true;
      }

      // Basic role-based permissions
      if (permissionCode.startsWith("customers:") && roles.contains("manager")) {
        return true;
      }

      if (permissionCode.startsWith("customers:read") && roles.contains("sales")) {
        return true;
      }

      return false;

    } catch (Exception e) {
      logger.error("Error checking permission '{}': {}", permissionCode, e.getMessage());
      return false;
    }
  }

  /** Returns current user's permissions based on Keycloak roles. */
  public List<String> getCurrentUserPermissions() {
    try {
      Set<String> roles = securityProvider.getRoles();

      if (roles.contains("admin")) {
        return Arrays.asList(
            "customers:read",
            "customers:write",
            "customers:delete",
            "admin:permissions",
            "admin:users");
      } else if (roles.contains("manager")) {
        return Arrays.asList("customers:read", "customers:write", "customers:delete");
      } else if (roles.contains("sales")) {
        return Arrays.asList("customers:read");
      }

      return Arrays.asList(); // Empty for unknown roles

    } catch (Exception e) {
      logger.error("Error getting current user permissions: {}", e.getMessage());
      return Arrays.asList();
    }
  }
  
  /**
   * Timezone-safe method to check if a UserPermission is expired.
   * 
   * @param userPermission the permission to check
   * @return true if the permission is expired
   */
  public boolean isUserPermissionExpired(UserPermission userPermission) {
    return userPermission.getExpiresAt() != null 
        && LocalDateTime.now(clock).isAfter(userPermission.getExpiresAt());
  }
  
  /**
   * Timezone-safe method to check if a UserPermission is active.
   * 
   * @param userPermission the permission to check
   * @return true if the permission is active (not expired)
   */
  public boolean isUserPermissionActive(UserPermission userPermission) {
    return !isUserPermissionExpired(userPermission);
  }

  /** Placeholder methods for full permission management (not implemented yet). */
  public void grantUserPermission(UUID userId, String permissionCode, String reason) {
    logger.warn(
        "grantUserPermission not implemented yet - userId: {}, permission: {}",
        userId,
        permissionCode);
    throw new UnsupportedOperationException("Permission management not yet implemented");
  }

  public void revokeUserPermission(UUID userId, String permissionCode, String reason) {
    logger.warn(
        "revokeUserPermission not implemented yet - userId: {}, permission: {}",
        userId,
        permissionCode);
    throw new UnsupportedOperationException("Permission management not yet implemented");
  }
}

package de.freshplan.domain.user.service.validation;

import java.util.List;
import java.util.Set;

/**
 * Validator for user roles.
 *
 * <p>This class validates that roles conform to the allowed values and business rules.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class RoleValidator {

  /**
   * Set of allowed role values. Based on PHASE2_KICKOFF.md specifications. Matches Keycloak realm
   * configuration.
   */
  private static final Set<String> ALLOWED_ROLES = Set.of("admin", "manager", "sales");

  /** Private constructor to prevent instantiation. */
  private RoleValidator() {
    // Utility class
  }

  /**
   * Validates a list of roles.
   *
   * @param roles the roles to validate
   * @return true if all roles are valid, false otherwise
   */
  public static boolean areValidRoles(List<String> roles) {
    if (roles == null || roles.isEmpty()) {
      return false;
    }

    return roles.stream()
        .allMatch(role -> role != null && ALLOWED_ROLES.contains(role.toLowerCase()));
  }

  /**
   * Validates a single role.
   *
   * @param role the role to validate
   * @return true if the role is valid, false otherwise
   */
  public static boolean isValidRole(String role) {
    return role != null && ALLOWED_ROLES.contains(role.toLowerCase());
  }

  /**
   * Gets the set of allowed roles.
   *
   * @return immutable set of allowed roles
   */
  public static Set<String> getAllowedRoles() {
    return ALLOWED_ROLES;
  }

  /**
   * Normalizes a role to lowercase.
   *
   * @param role the role to normalize
   * @return the normalized role
   */
  public static String normalizeRole(String role) {
    return role != null ? role.toLowerCase() : null;
  }

  /**
   * Normalizes a list of roles to lowercase.
   *
   * @param roles the roles to normalize
   * @return list of normalized roles
   */
  public static List<String> normalizeRoles(List<String> roles) {
    if (roles == null) {
      return null;
    }

    return roles.stream().map(RoleValidator::normalizeRole).toList();
  }

  /**
   * Normalizes and validates a list of roles. This method combines normalization and validation in
   * one step.
   *
   * @param roles the roles to normalize and validate
   * @return list of normalized and validated roles
   * @throws de.freshplan.domain.user.service.exception.InvalidRoleException if any role is invalid
   */
  public static List<String> normalizeAndValidateRoles(List<String> roles) {
    if (roles == null || roles.isEmpty()) {
      throw new IllegalArgumentException("Roles list cannot be null or empty");
    }

    return roles.stream()
        .map(
            role -> {
              if (role == null) {
                throw new de.freshplan.domain.user.service.exception.InvalidRoleException("null");
              }
              String normalized = role.toLowerCase();
              if (!ALLOWED_ROLES.contains(normalized)) {
                throw new de.freshplan.domain.user.service.exception.InvalidRoleException(role);
              }
              return normalized;
            })
        .toList();
  }
}

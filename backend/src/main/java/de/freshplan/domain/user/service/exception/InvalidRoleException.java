package de.freshplan.domain.user.service.exception;

import de.freshplan.domain.user.service.validation.RoleValidator;

/**
 * Exception thrown when an invalid role is specified.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class InvalidRoleException extends RuntimeException {

  /**
   * Creates a new exception for an invalid role.
   *
   * @param role the invalid role
   */
  public InvalidRoleException(String role) {
    super(
        String.format(
            "Invalid role: '%s'. Allowed roles are: %s", role, RoleValidator.getAllowedRoles()));
  }
}

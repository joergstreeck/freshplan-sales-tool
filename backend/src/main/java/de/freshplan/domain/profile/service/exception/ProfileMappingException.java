package de.freshplan.domain.profile.service.exception;

/**
 * Exception thrown when profile data mapping fails.
 *
 * @author FreshPlan Team
 * @since 1.0.0
 */
public class ProfileMappingException extends RuntimeException {

  public ProfileMappingException(String message, Throwable cause) {
    super("Profile mapping failed: " + message, cause);
  }
}

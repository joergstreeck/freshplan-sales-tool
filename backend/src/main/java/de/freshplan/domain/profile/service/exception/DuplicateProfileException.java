package de.freshplan.domain.profile.service.exception;

/**
 * Exception thrown when attempting to create a duplicate profile.
 *
 * @author FreshPlan Team
 * @since 1.0.0
 */
public class DuplicateProfileException extends RuntimeException {

  public DuplicateProfileException(String customerId) {
    super("Profile already exists for customer ID: " + customerId);
  }
}

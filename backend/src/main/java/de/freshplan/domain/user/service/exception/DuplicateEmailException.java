package de.freshplan.domain.user.service.exception;

/**
 * Exception thrown when attempting to create or update a user with an email address that already
 * exists in the system.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class DuplicateEmailException extends RuntimeException {

  private final String email;

  /**
   * Creates a new duplicate email exception.
   *
   * @param email the duplicate email address
   */
  public DuplicateEmailException(String email) {
    super("Email already exists: " + email);
    this.email = email;
  }

  /**
   * Gets the duplicate email address.
   *
   * @return the email address
   */
  public String getEmail() {
    return email;
  }
}

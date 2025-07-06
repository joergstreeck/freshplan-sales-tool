package de.freshplan.domain.user.service.exception;

/**
 * Exception thrown when attempting to create or update a user with a username that already exists
 * in the system.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class DuplicateUsernameException extends RuntimeException {

  private final String username;

  /**
   * Creates a new duplicate username exception.
   *
   * @param username the duplicate username
   */
  public DuplicateUsernameException(String username) {
    super("Username already exists: " + username);
    this.username = username;
  }

  /**
   * Gets the duplicate username.
   *
   * @return the username
   */
  public String getUsername() {
    return username;
  }
}

package de.freshplan.domain.user.service.exception;

/**
 * Exception thrown when attempting to create or update a user with a username or email that already
 * exists.
 *
 * <p>This is a runtime exception that should result in a 409 Conflict HTTP response when thrown
 * from a REST endpoint.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class UserAlreadyExistsException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new user already exists exception with the specified detail message.
   *
   * @param message the detail message
   */
  public UserAlreadyExistsException(String message) {
    super(message);
  }

  /**
   * Constructs a new user already exists exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause
   */
  public UserAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }
}

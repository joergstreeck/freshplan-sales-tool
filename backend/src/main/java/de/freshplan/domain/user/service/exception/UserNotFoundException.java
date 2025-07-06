package de.freshplan.domain.user.service.exception;

/**
 * Exception thrown when a requested user is not found.
 *
 * <p>This is a runtime exception that should result in a 404 Not Found HTTP response when thrown
 * from a REST endpoint.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class UserNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new user not found exception with the specified detail message.
   *
   * @param message the detail message
   */
  public UserNotFoundException(String message) {
    super(message);
  }

  /**
   * Constructs a new user not found exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause
   */
  public UserNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}

package de.freshplan.infrastructure.ratelimit;

/**
 * Exception thrown when rate limit is exceeded.
 *
 * @author FreshPlan Team
 * @since 1.0.0
 */
public class RateLimitExceededException extends RuntimeException {

  public RateLimitExceededException(String message) {
    super(message);
  }
}

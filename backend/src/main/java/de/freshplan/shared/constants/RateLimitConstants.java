package de.freshplan.shared.constants;

/**
 * Constants for API rate limiting configuration.
 *
 * <p>These constants define default rate limiting behavior to prevent API abuse and ensure fair
 * usage across all clients.
 */
public final class RateLimitConstants {

  /** Default maximum number of requests allowed within the time window. */
  public static final int DEFAULT_MAX_REQUESTS = 10;

  /** Default time window in seconds for rate limiting. */
  public static final int DEFAULT_WINDOW_SECONDS = 60;

  /** Rate limit for authentication endpoints (stricter). */
  public static final int AUTH_MAX_REQUESTS = 5;

  public static final int AUTH_WINDOW_SECONDS = 300; // 5 minutes

  /** Rate limit for search/list endpoints (more permissive). */
  public static final int SEARCH_MAX_REQUESTS = 30;

  public static final int SEARCH_WINDOW_SECONDS = 60;

  /** Rate limit for write operations (moderate). */
  public static final int WRITE_MAX_REQUESTS = 20;

  public static final int WRITE_WINDOW_SECONDS = 60;

  // Private constructor to prevent instantiation
  private RateLimitConstants() {
    throw new AssertionError("Constants class should not be instantiated");
  }
}

package de.freshplan.shared.constants;

/**
 * Constants for time-related values used across the application.
 *
 * <p>These constants define standard time periods and durations for various business logic
 * operations.
 */
public final class TimeConstants {

  /** Default lookback period in days for recent activity queries. */
  public static final int DEFAULT_LOOKBACK_DAYS = 30;

  /** String representation of default lookback days for JAX-RS @DefaultValue. */
  public static final String DEFAULT_LOOKBACK_DAYS_STRING = "30";

  /** Standard week duration in days. */
  public static final int DAYS_IN_WEEK = 7;

  /** Quarter duration in days (approximate). */
  public static final int DAYS_IN_QUARTER = 90;

  /** Year duration in days (non-leap year). */
  public static final int DAYS_IN_YEAR = 365;

  /** Default cache expiration in seconds. */
  public static final int DEFAULT_CACHE_SECONDS = 300; // 5 minutes

  /** Session timeout in minutes. */
  public static final int SESSION_TIMEOUT_MINUTES = 30;

  /** Token refresh threshold in minutes. */
  public static final int TOKEN_REFRESH_THRESHOLD_MINUTES = 5;

  // Private constructor to prevent instantiation
  private TimeConstants() {
    throw new AssertionError("Constants class should not be instantiated");
  }
}

package de.freshplan.shared.constants;

/**
 * Constants for field length validations across DTOs and entities.
 *
 * <p>These constants ensure consistent field length constraints throughout the application, making
 * it easier to maintain and update size limits.
 */
public final class FieldLengthConstants {

  // Common field lengths
  /** Minimum length for user names and similar fields. */
  public static final int MIN_USERNAME_LENGTH = 3;

  /** Maximum length for short text fields like codes, usernames. */
  public static final int SHORT_TEXT_LENGTH = 30;

  /** Maximum length for medium text fields like titles, names. */
  public static final int MEDIUM_TEXT_LENGTH = 60;

  /** Maximum length for standard text fields. */
  public static final int STANDARD_TEXT_LENGTH = 100;

  /** Maximum length for email addresses. */
  public static final int EMAIL_LENGTH = 120;

  /** Maximum length for general text fields. */
  public static final int GENERAL_TEXT_LENGTH = 255;

  /** Maximum length for description fields. */
  public static final int DESCRIPTION_LENGTH = 500;

  /** Maximum length for large text fields. */
  public static final int LARGE_TEXT_LENGTH = 1000;

  /** Maximum length for extra large text fields like notes or comments. */
  public static final int EXTRA_LARGE_TEXT_LENGTH = 5000;

  // Database column specific lengths
  /** Standard length for database code columns. */
  public static final int DB_CODE_LENGTH = 20;

  /** Standard length for database name columns. */
  public static final int DB_NAME_LENGTH = 100;

  /** Standard length for database description columns. */
  public static final int DB_DESCRIPTION_LENGTH = 255;

  /** Standard length for database text columns. */
  public static final int DB_TEXT_LENGTH = 1000;

  /** Standard length for database large text columns. */
  public static final int DB_LARGE_TEXT_LENGTH = 5000;

  // Private constructor to prevent instantiation
  private FieldLengthConstants() {
    throw new AssertionError("Constants class should not be instantiated");
  }
}

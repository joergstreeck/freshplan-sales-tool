package de.freshplan.domain.customer.constants;

/**
 * Constants for the customer domain. Centralizes all magic numbers and configuration values.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public final class CustomerConstants {

  // Risk Management
  public static final int DEFAULT_RISK_THRESHOLD = 70;
  public static final int HIGH_RISK_THRESHOLD = 85;
  public static final int CRITICAL_RISK_THRESHOLD = 95;
  public static final int LOW_RISK_THRESHOLD = 30;

  // Pagination
  public static final int DEFAULT_PAGE_SIZE = 20;
  public static final int MAX_PAGE_SIZE = 100;
  public static final int MIN_PAGE_SIZE = 1;
  public static final int DEFAULT_PAGE_NUMBER = 0;

  // Business Rules
  public static final int DAYS_UNTIL_RISK = 90;
  public static final int DAYS_UNTIL_INACTIVE = 180;
  public static final int DAYS_UNTIL_HIGH_RISK = 120;
  public static final int NEW_CUSTOMER_DAYS = 30;

  // Customer Number Format
  public static final String CUSTOMER_NUMBER_FORMAT = "KD-%d-%05d";
  public static final String CUSTOMER_NUMBER_PATTERN = "KD-\\d{4}-\\d{5}";
  public static final int CUSTOMER_NUMBER_SEQUENCE_LENGTH = 5;

  // String Length Limits
  public static final int COMPANY_NAME_MAX_LENGTH = 255;
  public static final int COMPANY_NAME_MIN_LENGTH = 1;
  public static final int TRADING_NAME_MAX_LENGTH = 255;
  public static final int LEGAL_FORM_MAX_LENGTH = 100;
  public static final int CUSTOMER_NUMBER_MAX_LENGTH = 20;

  // Search
  public static final int GLOBAL_SEARCH_MAX_LENGTH = 100;
  public static final double DUPLICATE_SIMILARITY_THRESHOLD = 0.8;

  // Financial Limits
  public static final String MAX_ANNUAL_VOLUME = "9999999999.99";
  public static final String MIN_ANNUAL_VOLUME = "0.00";
  public static final int DECIMAL_PRECISION = 2;
  public static final int DECIMAL_SCALE = 10;

  // Test Data Prefixes
  public static final String TEST_DATA_PREFIX = "[TEST]";
  public static final String TEST_CUSTOMER_NUMBER_PREFIX = "9";

  private CustomerConstants() {
    throw new AssertionError("Utility class - do not instantiate");
  }
}

package de.freshplan.shared.constants;

/**
 * Constants for risk management and customer risk assessment.
 *
 * <p>These thresholds determine when customers are flagged as at-risk based on days since last
 * contact or other risk indicators.
 */
public final class RiskManagementConstants {

  /**
   * Low risk threshold in days. Customers not contacted within this period are considered low risk.
   */
  public static final int RISK_THRESHOLD_LOW_DAYS = 60;

  /**
   * Medium risk threshold in days. Customers not contacted within this period are considered medium
   * risk.
   */
  public static final int RISK_THRESHOLD_MEDIUM_DAYS = 90;

  /**
   * High risk threshold in days. Customers not contacted within this period are considered high
   * risk.
   */
  public static final int RISK_THRESHOLD_HIGH_DAYS = 120;

  /** Default risk score for new customers or when no score is calculated. */
  public static final int DEFAULT_RISK_SCORE = 70;

  /** String representation of default risk score for JAX-RS @DefaultValue. */
  public static final String DEFAULT_RISK_SCORE_STRING = "70";

  /** Risk score thresholds for categorization. */
  public static final int RISK_SCORE_HIGH_THRESHOLD = 80;

  public static final int RISK_SCORE_MEDIUM_THRESHOLD = 50;
  public static final int RISK_SCORE_LOW_THRESHOLD = 30;

  // Private constructor to prevent instantiation
  private RiskManagementConstants() {
    throw new AssertionError("Constants class should not be instantiated");
  }
}

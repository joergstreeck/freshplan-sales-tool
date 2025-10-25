package de.freshplan.domain.customer.dto;

/**
 * Payment Behavior Classification (Ampel-System)
 *
 * <p>Sprint 2.1.7.2: Customer Dashboard - Zahlungsverhalten-Ampel
 *
 * <p>Thresholds (based on averageDaysToPay): - EXCELLENT: ≤7 days (Zahlungsziel übertroffen) -
 * GOOD: 8-14 days (Innerhalb Zahlungsziel) - WARNING: 15-30 days (Verzögerte Zahlung) - CRITICAL:
 * >30 days (Kritische Zahlungsmoral) - N_A: No data available
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public enum PaymentBehavior {
  /** Excellent payment behavior (≤7 days) Color: Green */
  EXCELLENT,

  /** Good payment behavior (8-14 days) Color: Light Green / Blue */
  GOOD,

  /** Warning - delayed payment (15-30 days) Color: Orange */
  WARNING,

  /** Critical payment behavior (>30 days) Color: Red */
  CRITICAL,

  /** Not available - no payment data Color: Gray */
  N_A;

  /**
   * Classify payment behavior based on average days to pay
   *
   * @param averageDaysToPay Average days to pay (null = N/A)
   * @return Payment behavior classification
   */
  public static PaymentBehavior fromAverageDaysToPay(Integer averageDaysToPay) {
    if (averageDaysToPay == null || averageDaysToPay == 0) {
      return N_A;
    }

    if (averageDaysToPay <= 7) {
      return EXCELLENT;
    } else if (averageDaysToPay <= 14) {
      return GOOD;
    } else if (averageDaysToPay <= 30) {
      return WARNING;
    } else {
      return CRITICAL;
    }
  }
}

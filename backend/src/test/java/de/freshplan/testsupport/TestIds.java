package de.freshplan.testsupport;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility für eindeutige Test-IDs.
 *
 * <p>Verhindert Race-Conditions und Duplikate bei paralleler Test-Ausführung.
 *
 * <p>Sprint 2.1.4: Eingeführt zur Performance-Optimierung (Mock-basierte Tests).
 *
 * @see TEST_DEBUGGING_GUIDE.md
 */
public final class TestIds {

  private static final AtomicInteger COUNTER = new AtomicInteger(1000);

  private TestIds() {
    // Utility class
  }

  /**
   * Generiert eindeutige Customer-Nummer.
   *
   * <p>Format: TEST-CUST-{counter}-{uuid-prefix}
   *
   * <p>Beispiel: TEST-CUST-1001-a3f4b2c1
   */
  public static String uniqueCustomerNumber() {
    return "TEST-CUST-"
        + COUNTER.incrementAndGet()
        + "-"
        + UUID.randomUUID().toString().substring(0, 8);
  }

  /**
   * Generiert eindeutige Opportunity-Nummer.
   *
   * <p>Format: TEST-OPP-{counter}-{uuid-prefix}
   */
  public static String uniqueOpportunityNumber() {
    return "TEST-OPP-"
        + COUNTER.incrementAndGet()
        + "-"
        + UUID.randomUUID().toString().substring(0, 8);
  }

  /**
   * Generiert eindeutige Lead-Nummer.
   *
   * <p>Format: TEST-LEAD-{counter}-{uuid-prefix}
   */
  public static String uniqueLeadNumber() {
    return "TEST-LEAD-"
        + COUNTER.incrementAndGet()
        + "-"
        + UUID.randomUUID().toString().substring(0, 8);
  }

  /**
   * Generiert eindeutigen Company-Namen.
   *
   * <p>Format: Test Company {counter} {uuid-prefix}
   */
  public static String uniqueCompanyName() {
    return "Test Company "
        + COUNTER.incrementAndGet()
        + " "
        + UUID.randomUUID().toString().substring(0, 8);
  }

  /**
   * Generiert eindeutige Email-Adresse.
   *
   * <p>Format: test-{counter}-{uuid-prefix}@example.com
   */
  public static String uniqueEmail() {
    return "test-"
        + COUNTER.incrementAndGet()
        + "-"
        + UUID.randomUUID().toString().substring(0, 8)
        + "@example.com";
  }

  /**
   * Generiert eindeutige Idempotency-Key.
   *
   * <p>Format: idempotency-{uuid}
   */
  public static String uniqueIdempotencyKey() {
    return "idempotency-" + UUID.randomUUID();
  }

  /**
   * Generiert eindeutige Tenant-ID.
   *
   * <p>Format: tenant-{counter}
   */
  public static String uniqueTenantId() {
    return "tenant-" + COUNTER.incrementAndGet();
  }

  /**
   * Generiert eindeutige User-ID.
   *
   * <p>Format: test-user-{uuid}
   */
  public static String uniqueUserId() {
    return "test-user-" + UUID.randomUUID();
  }

  /**
   * Generiert komplett eindeutige UUID.
   *
   * <p>Für Fälle wo UUID als Primärschlüssel genutzt wird.
   */
  public static UUID uniqueUUID() {
    return UUID.randomUUID();
  }
}

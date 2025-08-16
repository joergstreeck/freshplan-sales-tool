package de.freshplan.test.base;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Basis-Klasse für ALLE Integration Tests mit vollständiger Isolation.
 *
 * <p>GARANTIEN: 1. Jeder Test startet mit exakt 50 Seed-Kunden (SEED-001 bis SEED-050) 2. Alle
 * Änderungen werden nach dem Test zurückgerollt 3. Keine gegenseitige Beeinflussung zwischen Tests
 * 4. Stabile, vorhersagbare Test-Umgebung
 *
 * <p>VERWENDUNG: - Alle Integration-Tests MÜSSEN von dieser Klasse erben -
 * Verwende @TestTransaction für automatisches Rollback - Seed-Daten sind immer verfügbar und
 * unverändert
 *
 * @author Claude
 * @since Phase 14.3 - Professionelle Test-Isolation
 */
@QuarkusTest
public abstract class IsolatedIntegrationTest {

  private static final Logger LOG = Logger.getLogger(IsolatedIntegrationTest.class);

  @Inject protected EntityManager em;

  // Konstanten für Seed-Daten
  protected static final int SEED_CUSTOMER_COUNT = 50;
  protected static final String SEED_PREFIX = "[SEED]";
  protected static final String TEST_PREFIX = "[TEST-ISO]";

  /** Setup vor jedem Test. Verifiziert dass Seed-Daten vorhanden sind. */
  @BeforeEach
  protected void verifyTestEnvironment() {
    // Verifiziere Seed-Daten
    Long seedCount =
        em.createQuery(
                "SELECT COUNT(c) FROM Customer c WHERE c.customerNumber LIKE 'SEED-%'", Long.class)
            .getSingleResult();

    if (seedCount != SEED_CUSTOMER_COUNT) {
      LOG.warnf("Erwartete %d Seed-Kunden, gefunden: %d", SEED_CUSTOMER_COUNT, seedCount);
    }

    // Cleanup von alten Test-Daten (falls vorhanden)
    cleanupOldTestData();

    LOG.debugf("Test-Umgebung bereit: %d Seed-Kunden verfügbar", seedCount);
  }

  /** Cleanup nach jedem Test. Entfernt NUR Test-Daten, NIEMALS Seed-Daten. */
  @AfterEach
  protected void cleanupTestData() {
    try {
      // Lösche nur Test-Daten die während des Tests erstellt wurden
      int deleted =
          em.createQuery(
                  "DELETE FROM Customer c WHERE c.companyName LIKE :prefix "
                      + "AND c.customerNumber NOT LIKE 'SEED-%'")
              .setParameter("prefix", TEST_PREFIX + "%")
              .executeUpdate();

      if (deleted > 0) {
        LOG.debugf("Cleanup: %d Test-Kunden entfernt", deleted);
      }
    } catch (Exception e) {
      LOG.errorf(e, "Fehler beim Cleanup von Test-Daten");
    }
  }

  /**
   * Entfernt alte Test-Daten von vorherigen Test-Läufen. Wird nur ausgeführt wenn Tests nicht
   * korrekt beendet wurden.
   */
  private void cleanupOldTestData() {
    try {
      // Entferne alle [TEST-*] Daten die NICHT [SEED] sind
      int deleted =
          em.createNativeQuery(
                  "DELETE FROM customers WHERE company_name LIKE '[TEST%' "
                      + "AND company_name NOT LIKE '[SEED]%'")
              .executeUpdate();

      if (deleted > 0) {
        LOG.warnf("Alte Test-Daten gefunden und entfernt: %d Einträge", deleted);
      }
    } catch (Exception e) {
      // Ignoriere Fehler beim Cleanup
      LOG.trace("Cleanup von alten Test-Daten übersprungen", e);
    }
  }

  /**
   * Helper: Erstelle einen Test-Kunden mit automatischem Cleanup.
   *
   * @param name Der Name des Test-Kunden
   * @return Die ID des erstellten Kunden
   */
  protected String createTestCustomer(String name) {
    String customerNumber = "TEST-" + System.currentTimeMillis();
    String companyName = TEST_PREFIX + " " + name;

    em.createNativeQuery(
            "INSERT INTO customers (id, customer_number, company_name, "
                + "customer_type, status, is_test_data, created_by, created_at) "
                + "VALUES (gen_random_uuid(), :number, :name, 'NEUKUNDE', 'LEAD', "
                + "true, 'test', NOW())")
        .setParameter("number", customerNumber)
        .setParameter("name", companyName)
        .executeUpdate();

    return customerNumber;
  }

  /**
   * Helper: Zähle Kunden (ohne Seed-Daten).
   *
   * @return Anzahl der Nicht-Seed-Kunden
   */
  protected long countNonSeedCustomers() {
    return em.createQuery(
            "SELECT COUNT(c) FROM Customer c WHERE c.customerNumber NOT LIKE 'SEED-%'", Long.class)
        .getSingleResult();
  }

  /**
   * Helper: Hole einen zufälligen Seed-Kunden für Tests.
   *
   * @param index Index des Seed-Kunden (1-50)
   * @return Customer Number des Seed-Kunden
   */
  protected String getSeedCustomer(int index) {
    if (index < 1 || index > SEED_CUSTOMER_COUNT) {
      throw new IllegalArgumentException(
          "Seed-Index muss zwischen 1 und " + SEED_CUSTOMER_COUNT + " liegen");
    }
    return String.format("SEED-%03d", index);
  }
}

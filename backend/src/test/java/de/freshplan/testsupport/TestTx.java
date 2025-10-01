package de.freshplan.testsupport;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import java.util.concurrent.Callable;

/**
 * Utility für Transaktions-Management in Tests.
 *
 * <p>Löst das Problem dass Service-Methoden in neuer Transaktion laufen und Test-Daten nicht sehen.
 *
 * <p>Sprint 2.1.4: Eingeführt zur Vermeidung von "Entity not found" in Integration-Tests.
 *
 * <p>Beispiel:
 *
 * <pre>{@code
 * @Inject TestTx testTx;
 *
 * @Test
 * void testServiceFindsData() throws Exception {
 *     UUID customerId = testTx.committed(() -> {
 *         Customer c = new Customer();
 *         em.persist(c);
 *         em.flush();
 *         return c.getId();
 *     });
 *
 *     // Service läuft in neuer TX und findet committed data
 *     Customer found = customerService.findById(customerId);
 *     assertNotNull(found);
 * }
 * }</pre>
 *
 * @see TEST_DEBUGGING_GUIDE.md
 */
@ApplicationScoped
public class TestTx {

  @Inject UserTransaction userTransaction;

  /**
   * Führt action in committed Transaction aus.
   *
   * <p>Nutze dies wenn:
   *
   * <ul>
   *   <li>Test Daten erzeugt die ein Service in neuer TX lesen soll
   *   <li>Commit-Hooks getestet werden sollen
   *   <li>Cross-Transaction-Verhalten getestet wird
   * </ul>
   *
   * @param action die auszuführende Aktion
   * @return Ergebnis der Aktion
   * @throws Exception bei Rollback oder Action-Fehler
   */
  public <T> T committed(Callable<T> action) throws Exception {
    userTransaction.begin();
    try {
      T result = action.call();
      userTransaction.commit();
      return result;
    } catch (Exception e) {
      try {
        userTransaction.rollback();
      } catch (Exception rollbackEx) {
        e.addSuppressed(rollbackEx);
      }
      throw e;
    }
  }

  /**
   * Führt action in committed Transaction aus (ohne Rückgabewert).
   *
   * @param action die auszuführende Aktion
   * @throws Exception bei Rollback oder Action-Fehler
   */
  public void committedVoid(RunnableWithException action) throws Exception {
    committed(
        () -> {
          action.run();
          return null;
        });
  }

  /** Functional interface für Runnable die Exceptions werfen kann. */
  @FunctionalInterface
  public interface RunnableWithException {
    void run() throws Exception;
  }
}

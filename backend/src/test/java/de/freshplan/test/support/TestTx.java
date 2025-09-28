package de.freshplan.test.support;

import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.EntityManager;
import java.util.function.Supplier;

/**
 * Test Transaction Utility for managing transaction boundaries in tests.
 * Ensures test data is committed and visible to service layers.
 */
public final class TestTx {

  private TestTx() {}

  /**
   * Executes work in a new transaction and commits it immediately.
   * Use this for test data setup that needs to be visible to services.
   */
  public static void committed(Runnable work) {
    QuarkusTransaction.requiringNew().run(() -> {
      work.run();
      em().flush();
    });
  }

  /**
   * Executes work in a new transaction, commits it, and returns a result.
   * Use this for test data setup that needs to return IDs or entities.
   */
  public static <T> T committed(Supplier<T> work) {
    return QuarkusTransaction.requiringNew().call(() -> {
      T t = work.get();
      em().flush();
      return t;
    });
  }

  private static EntityManager em() {
    return CDI.current().select(EntityManager.class).get();
  }
}
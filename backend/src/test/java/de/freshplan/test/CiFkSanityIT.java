package de.freshplan.test;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;

/**
 * Fail-fast sanity check for CI environment FK configurations.
 *
 * <p>This test verifies that V9000 migration has been applied correctly, setting up CASCADE deletes
 * for test cleanup.
 *
 * @author FreshPlan Team
 * @since 2025-08-16
 */
@QuarkusTest
@Tag("migrate")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CiFkSanityIT {

  @Inject DataSource ds;

  private static String actionName(char del) {
    switch (del) {
      case 'c':
        return "CASCADE";
      case 'n':
        return "SET NULL";
      case 'a':
        return "NO ACTION";
      case 'r':
        return "RESTRICT";
      case 'd':
        return "SET DEFAULT";
      default:
        return String.valueOf(del);
    }
  }

  /**
   * Verify that V9000 migration was actually applied. This should run first to ensure our
   * CI-specific migrations are active.
   */
  @Test
  @Order(0)
  void v9000MigrationWasApplied() throws Exception {
    try (var c = ds.getConnection();
        var ps = c.prepareStatement("SELECT 1 FROM flyway_schema_history WHERE version = '9000'");
        var rs = ps.executeQuery()) {
      assertTrue(
          rs.next(),
          "V9000 migration not found in flyway_schema_history! "
              + "CI-specific migrations are not being loaded.");
    }
  }

  /**
   * Verify FK actions for customers table are configured correctly: - Self-reference
   * (parent_customer_id) should be SET NULL - All other references should be CASCADE
   *
   * <p>Now uses OID-based self-reference detection for robustness.
   */
  @Test
  @Order(1)
  void fkActionsForCustomersAreAsExpected() throws Exception {
    int cascadeCount = 0;
    int setNullCount = 0;
    int unexpected = 0;

    try (var c = ds.getConnection();
        var ps =
            c.prepareStatement(
                """
           SELECT
             conrelid::regclass::text AS child,
             confrelid::regclass::text AS parent,
             (conrelid = confrelid)   AS self_ref,
             confdeltype
           FROM pg_constraint
           WHERE contype = 'f'
             AND confrelid = 'public.customers'::regclass
         """);
        ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        String child = rs.getString("child"); // e.g. "customers" or "public.customers"
        String parent = rs.getString("parent");
        boolean self = rs.getBoolean("self_ref");
        char del = rs.getString("confdeltype").charAt(0);

        // Debug log for CI diagnosis
        System.out.printf(
            "FK child=%s parent=%s self=%s action=%s%n", child, parent, self, actionName(del));

        if (self) {
          // Self-FK should be SET NULL
          if (del == 'n') {
            setNullCount++;
          } else {
            System.out.printf(
                "WARNING: Self-FK action should be SET NULL, but is %s%n", actionName(del));
            unexpected++;
          }
        } else {
          // Non-self FKs should be CASCADE
          if (del == 'c') {
            cascadeCount++;
          } else {
            System.out.printf(
                "WARNING: Non-self FK action should be CASCADE, but is %s (child=%s)%n",
                actionName(del), child);
            unexpected++;
          }
        }
      }
    }

    System.out.printf(
        "CI FK Sanity: %d CASCADE FKs, %d SET NULL FKs, %d unexpected%n",
        cascadeCount, setNullCount, unexpected);

    // Robust assertions
    assertTrue(
        cascadeCount >= 1,
        String.format("Expected at least one CASCADE FK to customers, found %d", cascadeCount));
    assertTrue(
        setNullCount >= 1,
        String.format(
            "Expected self-FK (customers->customers) to be SET NULL, found %d", setNullCount));
    assertEquals(
        0, unexpected, String.format("Found %d FKs with unexpected delete actions", unexpected));
  }
}

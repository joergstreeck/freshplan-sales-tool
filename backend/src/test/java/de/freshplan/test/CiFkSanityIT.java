package de.freshplan.test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fail-fast sanity check for CI environment FK configurations.
 * 
 * This test verifies that V9000 migration has been applied correctly,
 * setting up CASCADE deletes for test cleanup.
 * 
 * @author FreshPlan Team
 * @since 2025-08-16
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CiFkSanityIT {

  @Inject DataSource ds;

  /**
   * Verify that V9000 migration was actually applied.
   * This should run first to ensure our CI-specific migrations are active.
   */
  @Test
  @Order(0)
  void v9000MigrationWasApplied() throws Exception {
    try (var c = ds.getConnection();
         var ps = c.prepareStatement(
           "SELECT 1 FROM flyway_schema_history WHERE version = '9000'");
         var rs = ps.executeQuery()) {
      assertTrue(rs.next(), "V9000 migration not found in flyway_schema_history! " +
        "CI-specific migrations are not being loaded.");
    }
  }

  /**
   * Verify FK actions for customers table are configured correctly:
   * - Self-reference (parent_customer_id) should be SET NULL
   * - All other references should be CASCADE
   */
  @Test
  @Order(1)
  void fkActionsForCustomersAreAsExpected() throws Exception {
    int cascadeCount = 0;
    int setNullCount = 0;
    int otherCount = 0;

    try (var c = ds.getConnection();
         var ps = c.prepareStatement("""
           SELECT conrelid::regclass AS child, confrelid::regclass AS parent, confdeltype
           FROM pg_constraint
           WHERE contype='f' AND confrelid='public.customers'::regclass
         """);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        String child = rs.getString("child");
        char del = rs.getString("confdeltype").charAt(0); 
        // 'c'=CASCADE, 'n'=SET NULL, 'a'=NO ACTION, 'r'=RESTRICT, 'd'=SET DEFAULT

        if ("public.customers".equals(child)) {
          // Self-FK should be SET NULL
          if (del == 'n') {
            setNullCount++;
          } else {
            System.err.printf("WARNING: Self-FK has action '%c' instead of 'n' (SET NULL)%n", del);
            otherCount++;
          }
        } else {
          // All other FKs should be CASCADE
          if (del == 'c') {
            cascadeCount++;
          } else {
            System.err.printf("WARNING: FK from %s has action '%c' instead of 'c' (CASCADE)%n", child, del);
            otherCount++;
          }
        }
      }
    }

    System.out.printf("CI FK Sanity: %d CASCADE FKs, %d SET NULL FKs, %d unexpected%n", 
                      cascadeCount, setNullCount, otherCount);
    
    // Verify we found FKs at all
    assertFalse(cascadeCount == 0 && setNullCount == 0, 
      "No FK constraints found at all - V9000 migration probably didn't run!");
    
    // Verify expected configuration
    assertTrue(cascadeCount >= 1, 
      String.format("Expected at least one CASCADE FK to customers, found %d", cascadeCount));
    assertTrue(setNullCount >= 1, 
      String.format("Expected self-FK (customers->customers) to be SET NULL, found %d", setNullCount));
    assertEquals(0, otherCount, 
      String.format("Found %d FKs with unexpected delete actions", otherCount));
  }
}
package de.freshplan.test;

import static org.assertj.core.api.Assertions.assertThat;

import io.agroal.api.AgroalDataSource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Smoke test to verify SEED data is present after migrations. Runs early in test suite (A01) to
 * catch issues immediately.
 */
@QuarkusTest
@Tag("migrate")
public class A01_SeedSmokeTest {

  @Inject AgroalDataSource ds;

  @Test
  void seedsPresentAfterMigrate() throws Exception {
    try (var c = ds.getConnection();
        var st = c.createStatement()) {
      var rs =
          st.executeQuery("SELECT COUNT(*) FROM customers WHERE customer_number LIKE 'SEED-%'");
      rs.next();
      int seedCount = rs.getInt(1);

      assertThat(seedCount).as("SEED customers should be present after migrations").isEqualTo(20);

      // Also verify they're protected from cleanup
      var rs2 =
          st.executeQuery(
              "SELECT COUNT(*) FROM customers WHERE customer_number LIKE 'SEED-%' AND is_test_data = false");
      rs2.next();
      assertThat(rs2.getInt(1))
          .as("All SEED customers should have is_test_data=false for protection")
          .isEqualTo(20);
    }
  }
}

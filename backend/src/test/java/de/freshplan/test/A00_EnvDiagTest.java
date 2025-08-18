package de.freshplan.test;

import io.agroal.api.AgroalDataSource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

/**
 * Diagnostic test to identify database container issues.
 * Runs first alphabetically to capture environment state.
 */
@QuarkusTest
public class A00_EnvDiagTest {
  private static final Logger LOG = Logger.getLogger(A00_EnvDiagTest.class);

  @Inject AgroalDataSource ds;

  @Test
  void fingerprintDb() throws Exception {
    LOG.info("\n" + "=".repeat(80));
    LOG.info("=== DATABASE FINGERPRINT AT TEST START ===");
    LOG.info("Test class: " + this.getClass().getName());
    LOG.info("Test instance: " + this.hashCode());
    LOG.info("=".repeat(80));
    
    try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
      // Database connection details
      LOG.info("JDBC URL  : " + c.getMetaData().getURL());
      LOG.info("User      : " + c.getMetaData().getUserName());

      // Database server info
      ResultSet rs1 = st.executeQuery(
        "SELECT current_database(), current_schema, version(), " +
        "inet_server_addr()::text, inet_server_port()");
      if (rs1.next()) {
        LOG.info("DB=" + rs1.getString(1) + " schema=" + rs1.getString(2) +
                 " host=" + rs1.getString(4) + " port=" + rs1.getInt(5));
        LOG.info("Version: " + rs1.getString(3));
      }
      
      // Search path
      ResultSet rs2 = st.executeQuery("SHOW search_path");
      if (rs2.next()) {
        LOG.info("search_path=" + rs2.getString(1));
      }

      // Flyway migration history
      LOG.info("\n📜 Flyway Migration History (last 5):");
      ResultSet rs3 = st.executeQuery(
        "SELECT installed_rank, version, description, success " +
        "FROM flyway_schema_history " +
        "ORDER BY installed_rank DESC LIMIT 5");
      while (rs3.next()) {
        LOG.info(String.format("  Flyway #%d: V%s - %s (%s)", 
          rs3.getInt(1), 
          rs3.getString(2), 
          rs3.getString(3),
          rs3.getBoolean(4) ? "✅" : "❌"));
      }

      // SEED data count
      ResultSet rs4 = st.executeQuery(
        "SELECT COUNT(*) FROM customers WHERE customer_number LIKE 'SEED-%'");
      rs4.next();
      int seedCount = rs4.getInt(1);
      LOG.info("\n🌱 SEED count at test bootstrap = " + seedCount);
      
      if (seedCount == 0) {
        LOG.error("⚠️ NO SEED DATA FOUND! Expected 20 SEED customers.");
        
        // Check if they exist with wrong flag
        ResultSet rs5 = st.executeQuery(
          "SELECT COUNT(*), " +
          "SUM(CASE WHEN is_test_data = true THEN 1 ELSE 0 END) as with_true, " +
          "SUM(CASE WHEN is_test_data = false THEN 1 ELSE 0 END) as with_false " +
          "FROM customers WHERE company_name LIKE '[SEED]%'");
        if (rs5.next()) {
          LOG.info("SEED-like customers: total=" + rs5.getInt(1) + 
                   " with is_test_data=true:" + rs5.getInt(2) +
                   " with is_test_data=false:" + rs5.getInt(3));
        }
      } else {
        LOG.info("✅ SEED data present: " + seedCount + " customers");
      }
      
      // Total customer count
      ResultSet rs6 = st.executeQuery("SELECT COUNT(*) FROM customers");
      rs6.next();
      LOG.info("Total customers in DB: " + rs6.getInt(1));
      
      // CI flag check
      ResultSet rs7 = st.executeQuery("SELECT current_setting('ci.build', true)");
      rs7.next();
      String ciFlag = rs7.getString(1);
      LOG.info("CI flag (ci.build): " + ciFlag);
      
      LOG.info("=".repeat(80) + "\n");
    }
  }
}
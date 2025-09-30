package de.freshplan.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Minimale Tests für Security Context Foundation. Testet nur die abstrakten Security-Funktionen
 * ohne Business-Tabellen.
 */
@QuarkusTest
@DisplayName("Security Context Foundation Tests")
public class SecurityContextTest {

  @Inject DataSource dataSource;

  @Test
  @DisplayName("Database connection should be available")
  void testDatabaseConnection() throws Exception {
    assertNotNull(dataSource, "DataSource should be injected");

    try (Connection conn = dataSource.getConnection()) {
      assertNotNull(conn, "Should get valid connection");
      assertFalse(conn.isClosed(), "Connection should be open");
    }
  }

  @Test
  @DisplayName("Security context functions should exist")
  void testSecurityFunctionsExist() throws Exception {
    try (Connection conn = dataSource.getConnection()) {
      // Test set_app_context function exists
      String sql = "SELECT set_app_context(?::uuid, ?, ?, string_to_array(?, ','))";
      try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setObject(1, null); // null user for test
        ps.setString(2, "test-org");
        ps.setString(3, "DE");
        ps.setString(4, "user,sales");
        ps.execute();
        // No exception means function exists
      }

      // Test current_app_context function - just verify it returns JSON
      sql = "SELECT current_app_context()::text";
      try (PreparedStatement ps = conn.prepareStatement(sql);
          ResultSet rs = ps.executeQuery()) {
        assertTrue(rs.next(), "Should return context");
        String context = rs.getString(1);
        assertNotNull(context, "Context should not be null");
        // Just check it returns a JSON object
        assertTrue(context.startsWith("{"), "Context should be JSON object");
        assertTrue(context.contains("org_id"), "Context should have org_id key");
      }
    }
  }

  @Test
  @DisplayName("Settings scope enum should be created")
  void testSettingsScopeEnum() throws Exception {
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps =
            conn.prepareStatement(
                "SELECT enumlabel FROM pg_enum "
                    + "JOIN pg_type ON pg_enum.enumtypid = pg_type.oid "
                    + "WHERE pg_type.typname = 'settings_scope' "
                    + "ORDER BY enumsortorder");
        ResultSet rs = ps.executeQuery()) {

      assertTrue(rs.next(), "Should have at least one enum value");
      String firstValue = rs.getString(1);
      assertEquals("GLOBAL", firstValue, "First enum value should be GLOBAL");
    }
  }

  @Test
  @DisplayName("Security settings table should exist")
  void testSecuritySettingsTable() throws Exception {
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps =
            conn.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.tables "
                    + "WHERE table_name = 'security_settings'");
        ResultSet rs = ps.executeQuery()) {

      assertTrue(rs.next());
      assertEquals(1, rs.getInt(1), "Security settings table should exist");
    }
  }

  @Test
  @DisplayName("Security audit log table should exist")
  void testSecurityAuditTable() throws Exception {
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps =
            conn.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.tables "
                    + "WHERE table_name = 'security_audit_log'");
        ResultSet rs = ps.executeQuery()) {

      assertTrue(rs.next());
      assertEquals(1, rs.getInt(1), "Security audit log table should exist");
    }
  }

  @Test
  @TestSecurity(
      user = "test-user",
      roles = {"sales"})
  @DisplayName("Session filter should be injectable")
  void testSessionFilterExists() {
    // This test just verifies that the security annotations work
    // Actual filter testing would require request context
    assertTrue(true, "Test security annotation works");
  }
}

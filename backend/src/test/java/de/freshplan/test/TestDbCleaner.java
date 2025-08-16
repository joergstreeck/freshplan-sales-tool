package de.freshplan.test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import org.jboss.logging.Logger;

/**
 * Utility for cleaning up test data in FK-safe order.
 * 
 * <p>Deletes child records before parent records to avoid foreign key violations.
 * Used in integration tests to ensure clean state between tests.
 */
@ApplicationScoped
public class TestDbCleaner {
  
  private static final Logger LOG = Logger.getLogger(TestDbCleaner.class);
  
  @Inject DataSource dataSource;
  
  /**
   * Delete a customer and all its dependent data in FK-safe order.
   * 
   * @param customerId The customer ID to delete
   */
  @Transactional
  public void deleteCustomerWithChildren(UUID customerId) {
    try (Connection conn = dataSource.getConnection()) {
      // Order is important - delete children before parents
      
      // 1. Delete timeline events
      deleteFromTable(conn, "customer_timeline_events", "customer_id", customerId);
      
      // 2. Delete contacts
      deleteFromTable(conn, "customer_contacts", "customer_id", customerId);
      
      // 3. Delete opportunities
      deleteFromTable(conn, "opportunities", "customer_id", customerId);
      
      // 4. Delete audit entries
      deleteFromTable(conn, "audit_trail", "entity_id", customerId);
      
      // 5. Finally delete the customer
      deleteFromTable(conn, "customers", "id", customerId);
      
      LOG.debugf("Deleted customer %s with all children", customerId);
      
    } catch (SQLException e) {
      throw new RuntimeException("Failed to delete customer " + customerId, e);
    }
  }
  
  /**
   * Clean all test data marked with isTestData flag.
   */
  @Transactional
  public void cleanAllTestData() {
    try (Connection conn = dataSource.getConnection()) {
      // Delete in FK-safe order
      
      // 1. Delete dependent data
      try (PreparedStatement ps = conn.prepareStatement(
          "DELETE FROM customer_timeline_events WHERE customer_id IN (SELECT id FROM customers WHERE is_test_data = true)")) {
        int deleted = ps.executeUpdate();
        if (deleted > 0) LOG.debugf("Deleted %d test timeline events", deleted);
      }
      
      try (PreparedStatement ps = conn.prepareStatement(
          "DELETE FROM customer_contacts WHERE customer_id IN (SELECT id FROM customers WHERE is_test_data = true)")) {
        int deleted = ps.executeUpdate();
        if (deleted > 0) LOG.debugf("Deleted %d test contacts", deleted);
      }
      
      try (PreparedStatement ps = conn.prepareStatement(
          "DELETE FROM opportunities WHERE customer_id IN (SELECT id FROM customers WHERE is_test_data = true)")) {
        int deleted = ps.executeUpdate();
        if (deleted > 0) LOG.debugf("Deleted %d test opportunities", deleted);
      }
      
      // 2. Delete parent data
      try (PreparedStatement ps = conn.prepareStatement(
          "DELETE FROM customers WHERE is_test_data = true")) {
        int deleted = ps.executeUpdate();
        if (deleted > 0) LOG.debugf("Deleted %d test customers", deleted);
      }
      
    } catch (SQLException e) {
      throw new RuntimeException("Failed to clean test data", e);
    }
  }
  
  /**
   * Delete all data created by a specific test user.
   * 
   * @param testUsername The username used in tests
   */
  @Transactional
  public void cleanDataByUser(String testUsername) {
    try (Connection conn = dataSource.getConnection()) {
      // Delete in FK-safe order, based on created_by field
      
      String[] tables = {
        "customer_timeline_events",
        "customer_contacts", 
        "opportunities",
        "customers"
      };
      
      for (String table : tables) {
        try (PreparedStatement ps = conn.prepareStatement(
            "DELETE FROM " + table + " WHERE created_by = ?")) {
          ps.setString(1, testUsername);
          int deleted = ps.executeUpdate();
          if (deleted > 0) {
            LOG.debugf("Deleted %d rows from %s created by %s", deleted, table, testUsername);
          }
        }
      }
      
    } catch (SQLException e) {
      throw new RuntimeException("Failed to clean data for user " + testUsername, e);
    }
  }
  
  private void deleteFromTable(Connection conn, String table, String column, UUID value) throws SQLException {
    String sql = "DELETE FROM " + table + " WHERE " + column + " = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setObject(1, value);
      int deleted = ps.executeUpdate();
      if (deleted > 0) {
        LOG.debugf("Deleted %d rows from %s where %s = %s", deleted, table, column, value);
      }
    }
  }
}
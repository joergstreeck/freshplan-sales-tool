package de.freshplan.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import io.agroal.api.AgroalDataSource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

/**
 * Verifies that SEED data from V10005 migration is present and correct.
 * This test should run early to detect the Two-Container Problem.
 */
@QuarkusTest
class SeedDataVerificationTest {

  private static final Logger LOG = Logger.getLogger(SeedDataVerificationTest.class);

  @Inject CustomerRepository customerRepository;
  
  @Inject AgroalDataSource dataSource;

  @Test
  @Transactional
  void verify_seed_data_present_and_protected() {
    LOG.info("=".repeat(80));
    LOG.info("SEED DATA VERIFICATION TEST");
    LOG.info("=".repeat(80));
    
    // First check raw database
    try (Connection conn = dataSource.getConnection();
         Statement stmt = conn.createStatement()) {
      
      // Check Flyway history
      ResultSet rs = stmt.executeQuery(
        "SELECT version, description, success FROM flyway_schema_history " +
        "WHERE version = '10005' OR description LIKE '%seed%' " +
        "ORDER BY installed_rank DESC LIMIT 5");
      
      LOG.info("Flyway migrations containing 'seed':");
      while (rs.next()) {
        LOG.infof("  V%s: %s (success=%s)", 
                  rs.getString(1), rs.getString(2), rs.getBoolean(3));
      }
      
      // Count SEED customers directly
      ResultSet rs2 = stmt.executeQuery(
        "SELECT COUNT(*) FROM customers WHERE customer_number LIKE 'SEED-%'");
      rs2.next();
      int seedCountRaw = rs2.getInt(1);
      LOG.infof("Raw SQL count of SEED customers: %d", seedCountRaw);
      
      // Show first few SEED customers if any
      if (seedCountRaw > 0) {
        ResultSet rs3 = stmt.executeQuery(
          "SELECT customer_number, company_name, is_test_data " +
          "FROM customers WHERE customer_number LIKE 'SEED-%' " +
          "ORDER BY customer_number LIMIT 5");
        LOG.info("Sample SEED customers:");
        while (rs3.next()) {
          LOG.infof("  %s: %s (is_test_data=%s)",
                    rs3.getString(1), rs3.getString(2), rs3.getBoolean(3));
        }
      }
      
    } catch (Exception e) {
      LOG.error("Error checking database directly", e);
    }
    
    // Now use repository
    List<Customer> seedCustomers = customerRepository.list("customerNumber LIKE 'SEED-%'");
    
    LOG.infof("Repository found %d SEED customers", seedCustomers.size());
    
    if (seedCustomers.isEmpty()) {
      // Try alternative queries
      long byName = customerRepository.count("companyName LIKE '[SEED]%'");
      long total = customerRepository.count();
      
      LOG.errorf("NO SEED CUSTOMERS FOUND!");
      LOG.errorf("  Customers with [SEED] name: %d", byName);
      LOG.errorf("  Total customers: %d", total);
      
      // This is the core issue - SEED data should be present
      fail("SEED data from V10005 migration is missing! Two-Container Problem detected.");
    }
    
    // Verify we have exactly 20 SEED customers
    assertThat(seedCustomers)
        .as("Should have exactly 20 SEED customers from V10005")
        .hasSize(20);
    
    // Verify they are protected (is_test_data = false)
    assertThat(seedCustomers)
        .as("All SEED customers should be protected")
        .allMatch(c -> Boolean.FALSE.equals(c.getIsTestData()))
        .allMatch(c -> c.getCompanyName().startsWith("[SEED]"))
        .allMatch(c -> c.getCustomerNumber().matches("SEED-\\d{3}"));
    
    LOG.info("✅ All 20 SEED customers present and protected");
    LOG.info("=".repeat(80));
  }
}
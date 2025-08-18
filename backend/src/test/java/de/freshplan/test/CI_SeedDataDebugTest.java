package de.freshplan.test;

import de.freshplan.domain.customer.repository.CustomerRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Debug test to track SEED data throughout CI test execution.
 * This test runs at different points to identify where SEED data is lost.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CI_SeedDataDebugTest {

  @Inject CustomerRepository customerRepository;
  @Inject EntityManager em;

  @Test
  @Order(1)
  @Transactional
  public void AAA_checkSeedDataAtStart() {
    System.out.println("\n" + "=".repeat(80));
    System.out.println("=== CI SEED DATA DEBUG - START OF TEST RUN ===");
    System.out.println("=".repeat(80));
    
    debugSeedData("START");
  }

  @Test
  @Order(999)
  @Transactional
  public void ZZZ_checkSeedDataAtEnd() {
    System.out.println("\n" + "=".repeat(80));
    System.out.println("=== CI SEED DATA DEBUG - END OF TEST RUN ===");
    System.out.println("=".repeat(80));
    
    debugSeedData("END");
  }

  private void debugSeedData(String phase) {
    // Count total customers
    long totalCustomers = customerRepository.count();
    
    // Count SEED customers
    long seedCustomers = customerRepository.count("customerNumber LIKE ?1", "SEED-%");
    
    // Count SEED with is_test_data=true
    long seedWithTestTrue = customerRepository.count(
        "customerNumber LIKE ?1 AND isTestData = ?2", "SEED-%", true);
    
    // Count SEED with is_test_data=false
    long seedWithTestFalse = customerRepository.count(
        "customerNumber LIKE ?1 AND isTestData = ?2", "SEED-%", false);
    
    // Count SEED with is_test_data=null
    long seedWithTestNull = customerRepository.count(
        "customerNumber LIKE ?1 AND isTestData IS NULL", "SEED-%");
    
    // Count other test customers
    long testCustomers = customerRepository.count("companyName LIKE ?1", "[TEST]%");
    
    System.out.println("\n📊 SEED DATA STATUS AT " + phase + ":");
    System.out.println("├─ Total Customers: " + totalCustomers);
    System.out.println("├─ SEED Customers: " + seedCustomers);
    System.out.println("│  ├─ with is_test_data=true: " + seedWithTestTrue);
    System.out.println("│  ├─ with is_test_data=false: " + seedWithTestFalse);
    System.out.println("│  └─ with is_test_data=null: " + seedWithTestNull);
    System.out.println("└─ [TEST] Customers: " + testCustomers);
    
    if (seedCustomers < 20) {
      System.out.println("\n⚠️ WARNING: Expected 20 SEED customers, found only " + seedCustomers);
      
      // Check what's in the database
      System.out.println("\n🔍 Analyzing database content:");
      
      // Raw SQL to bypass any JPA issues
      var query = em.createNativeQuery(
          "SELECT customer_number, company_name, is_test_data " +
          "FROM customers " +
          "WHERE customer_number LIKE 'SEED-%' " +
          "ORDER BY customer_number");
      
      var results = query.getResultList();
      if (results.isEmpty()) {
        System.out.println("❌ NO SEED CUSTOMERS FOUND IN DATABASE!");
      } else {
        System.out.println("Found " + results.size() + " SEED customers:");
        for (Object row : results) {
          Object[] cols = (Object[]) row;
          System.out.println("  - " + cols[0] + ": " + cols[1] + " (is_test_data=" + cols[2] + ")");
        }
      }
      
      // Check if they were deleted
      System.out.println("\n🔍 Checking deletion patterns:");
      
      // Check for any DELETE operations in the current transaction
      var deletedCount = em.createNativeQuery(
          "SELECT COUNT(*) FROM customers WHERE is_deleted = true AND customer_number LIKE 'SEED-%'")
          .getSingleResult();
      System.out.println("Soft-deleted SEED customers: " + deletedCount);
      
      // Check Flyway history
      var flywayCheck = em.createNativeQuery(
          "SELECT version, description, success " +
          "FROM flyway_schema_history " +
          "WHERE version IN ('10004', '10005') " +
          "ORDER BY installed_rank DESC")
          .getResultList();
      
      System.out.println("\n📜 Flyway Migration Status:");
      for (Object row : flywayCheck) {
        Object[] cols = (Object[]) row;
        System.out.println("  V" + cols[0] + " - " + cols[1] + ": " + 
            (Boolean.TRUE.equals(cols[2]) ? "✅ SUCCESS" : "❌ FAILED"));
      }
    } else {
      System.out.println("\n✅ All 20 SEED customers present!");
    }
    
    System.out.println("\n" + "=".repeat(80));
  }
}
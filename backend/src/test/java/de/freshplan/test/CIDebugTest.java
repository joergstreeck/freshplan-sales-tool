package de.freshplan.test;

import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.repository.CustomerTimelineRepository;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * CI Debug Test - Sammelt präzise Informationen über den Zustand der CI-Datenbank
 *
 * <p>WICHTIG: Dieser Test läuft ZUERST (Order 1) um den initialen Zustand zu erfassen!
 */
@QuarkusTest
@Tag("quarantine")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CIDebugTest {

  @Inject CustomerRepository customerRepository;
  @Inject CustomerTimelineRepository timelineRepository;
  @Inject OpportunityRepository opportunityRepository;

  @Test
  @Order(1)
  void debugDatabaseState() {
    System.out.println("\n" + "=".repeat(100));
    System.out.println("=== CI DATABASE DEBUG INFORMATION - START ===");
    System.out.println("=".repeat(100));

    // 1. Customers Analysis
    System.out.println("\n### CUSTOMER ANALYSIS ###");
    long totalCustomers = customerRepository.count();
    System.out.println("Total Customers: " + totalCustomers);

    long testPrefixCustomers = customerRepository.count("companyName like ?1", "[TEST]%");
    System.out.println("Customers with [TEST] prefix: " + testPrefixCustomers);

    long isTestDataTrue = customerRepository.count("isTestData = ?1", true);
    System.out.println("Customers with isTestData=true: " + isTestDataTrue);

    long isTestDataFalse = customerRepository.count("isTestData = ?1", false);
    System.out.println("Customers with isTestData=false: " + isTestDataFalse);

    long isTestDataNull = customerRepository.count("isTestData is null");
    System.out.println("Customers with isTestData=null: " + isTestDataNull);

    // 2. Show first 10 customers
    System.out.println("\n### FIRST 10 CUSTOMERS ###");
    customerRepository
        .findAll()
        .page(0, 10)
        .list()
        .forEach(
            c -> {
              System.out.printf(
                  "ID: %s | Number: %s | Name: %s | isTestData: %s | CreatedBy: %s%n",
                  c.getId(),
                  c.getCustomerNumber(),
                  c.getCompanyName(),
                  c.getIsTestData(),
                  c.getCreatedBy());
            });

    // 3. Timeline Events Analysis
    System.out.println("\n### TIMELINE EVENTS ANALYSIS ###");
    long totalEvents = timelineRepository.count();
    System.out.println("Total Timeline Events: " + totalEvents);

    long testDataEvents = timelineRepository.count("isTestData = ?1", true);
    System.out.println("Events with isTestData=true: " + testDataEvents);

    // 4. Opportunities Analysis
    System.out.println("\n### OPPORTUNITIES ANALYSIS ###");
    long totalOpportunities = opportunityRepository.count();
    System.out.println("Total Opportunities: " + totalOpportunities);

    // 5. Check for specific test patterns
    System.out.println("\n### CUSTOMER NAME PATTERNS ###");
    long kdPattern = customerRepository.count("customerNumber like ?1", "KD%");
    System.out.println("Customers with KD* pattern: " + kdPattern);

    long testCompanyPattern = customerRepository.count("companyName like ?1", "%Test Company%");
    System.out.println("Customers with 'Test Company' in name: " + testCompanyPattern);

    long addPattern = customerRepository.count("customerNumber like ?1", "ADD-%");
    System.out.println("Customers with ADD-* pattern: " + addPattern);

    // 6. Check who created the data
    System.out.println("\n### CREATED BY ANALYSIS ###");
    customerRepository
        .getEntityManager()
        .createQuery("SELECT c.createdBy, COUNT(c) FROM Customer c GROUP BY c.createdBy")
        .getResultList()
        .forEach(
            row -> {
              Object[] data = (Object[]) row;
              System.out.printf("CreatedBy: %s | Count: %d%n", data[0], data[1]);
            });

    // 7. Environment Information
    System.out.println("\n### ENVIRONMENT INFO ###");
    System.out.println("Java Version: " + System.getProperty("java.version"));
    System.out.println(
        "OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
    System.out.println("User: " + System.getProperty("user.name"));
    System.out.println("CI Environment: " + System.getenv("CI"));
    System.out.println("GitHub Actions: " + System.getenv("GITHUB_ACTIONS"));
    System.out.println("Test Profile: " + System.getProperty("quarkus.profile"));

    // 8. Database Connection Info
    System.out.println("\n### DATABASE CONNECTION ###");
    try {
      var ds = customerRepository.getEntityManager().getEntityManagerFactory().getProperties();
      System.out.println("Hibernate Properties: " + ds);
    } catch (Exception e) {
      System.out.println("Could not get DB properties: " + e.getMessage());
    }

    System.out.println("\n" + "=".repeat(100));
    System.out.println("=== CI DATABASE DEBUG INFORMATION - END ===");
    System.out.println("=".repeat(100) + "\n");

    // This test always passes - it's just for information
    assert true;
  }

  @Test
  @Order(2)
  void debugAfterOtherTests() {
    // This runs AFTER other tests to see the difference
    System.out.println("\n" + "=".repeat(100));
    System.out.println("=== POST-TEST DATABASE STATE ===");
    System.out.println("=".repeat(100));

    long totalCustomers = customerRepository.count();
    System.out.println("Total Customers NOW: " + totalCustomers);

    long testPrefixCustomers = customerRepository.count("companyName like ?1", "[TEST]%");
    System.out.println("Customers with [TEST] prefix NOW: " + testPrefixCustomers);

    System.out.println("=".repeat(100) + "\n");
  }
}

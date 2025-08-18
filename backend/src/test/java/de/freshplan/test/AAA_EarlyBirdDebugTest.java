package de.freshplan.test;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * CRITICAL: This test MUST run FIRST to capture database pollution!
 *
 * <p>Named AAA_ to ensure alphabetical priority over all other tests. This test identifies which
 * tests are creating customers without proper test data markers.
 */
@QuarkusTest
public class AAA_EarlyBirdDebugTest {

  @Inject CustomerRepository customerRepository;

  @Test
  @TestTransaction
  void aaa_captureInitialDatabasePollution() {
    System.out.println("\n" + "!".repeat(120));
    System.out.println("!!! EARLY BIRD DEBUG TEST - RUNNING BEFORE ALL OTHER TESTS !!!");
    System.out.println("!!! This test runs FIRST alphabetically to catch database pollution !!!");
    System.out.println("!".repeat(120));

    // 1. Initial State
    long totalCustomers = customerRepository.count();
    System.out.println("\n### INITIAL DATABASE STATE (should be 0 or only seed data) ###");
    System.out.println("Total Customers at START: " + totalCustomers);

    // 2. Find ALL customers without proper test markers
    System.out.println(
        "\n### PROBLEMATIC CUSTOMERS (missing [TEST] prefix or isTestData flag) ###");

    List<Customer> problematicCustomers =
        customerRepository
            .find("(companyName NOT LIKE '[TEST]%' OR isTestData = false OR isTestData IS NULL)")
            .list();

    if (!problematicCustomers.isEmpty()) {
      System.out.println("FOUND " + problematicCustomers.size() + " PROBLEMATIC CUSTOMERS:");
      problematicCustomers.forEach(
          c -> {
            System.out.printf("\n  PROBLEM: %s%n", c.getCompanyName());
            System.out.printf("    - ID: %s%n", c.getId());
            System.out.printf("    - Number: %s%n", c.getCustomerNumber());
            System.out.printf("    - isTestData: %s (should be true!)%n", c.getIsTestData());
            System.out.printf("    - CreatedBy: %s%n", c.getCreatedBy());
            System.out.printf("    - CreatedAt: %s%n", c.getCreatedAt());

            // Try to identify which test created this
            identifyPotentialSource(c);
          });
    } else {
      System.out.println("✅ No problematic customers found at startup!");
    }

    // 3. Look for specific patterns we know are problematic
    System.out.println("\n### SEARCHING FOR KNOWN PROBLEMATIC PATTERNS ###");

    String[] problematicPatterns = {
      "Parent Company%",
      "Child Company%",
      "Source Company%",
      "Target Company%",
      "Test Company%",
      "Status Test Company%",
      "CUST-%",
      "ADD-%"
    };

    for (String pattern : problematicPatterns) {
      long count = customerRepository.count("companyName LIKE ?1", pattern);
      if (count > 0) {
        System.out.printf("⚠️  Found %d customers matching pattern: %s%n", count, pattern);

        // Show details
        customerRepository
            .find("companyName LIKE ?1", pattern)
            .page(0, 3)
            .list()
            .forEach(
                c -> {
                  System.out.printf(
                      "     - %s (isTestData=%s, createdBy=%s)%n",
                      c.getCompanyName(), c.getIsTestData(), c.getCreatedBy());
                });
      }
    }

    // 4. Timeline check
    System.out.println("\n### TIMELINE CHECK ###");
    if (totalCustomers > 0) {
      Customer oldest = customerRepository.find("ORDER BY createdAt ASC").firstResult();
      if (oldest != null) {
        System.out.printf(
            "Oldest customer: %s (created at %s)%n",
            oldest.getCompanyName(), oldest.getCreatedAt());
      }

      Customer newest = customerRepository.find("ORDER BY createdAt DESC").firstResult();
      if (newest != null) {
        System.out.printf(
            "Newest customer: %s (created at %s)%n",
            newest.getCompanyName(), newest.getCreatedAt());
      }
    }

    System.out.println("\n" + "!".repeat(120));
    System.out.println("!!! END OF EARLY BIRD DEBUG TEST !!!");
    System.out.println("!".repeat(120) + "\n");
  }

  private void identifyPotentialSource(Customer c) {
    String name = c.getCompanyName();

    // Known problematic test patterns
    if (name.contains("Parent Company") || name.contains("Child Company")) {
      System.out.println(
          "    ⚠️  LIKELY SOURCE: CustomerCQRSIntegrationTest.addChildCustomer_inCQRSMode_shouldCreateHierarchy()");
    } else if (name.contains("Source Company") || name.contains("Target Company")) {
      System.out.println(
          "    ⚠️  LIKELY SOURCE: CustomerCQRSIntegrationTest.mergeCustomers_inCQRSMode_shouldMergeData()");
    } else if (name.contains("Status Test Company")) {
      System.out.println("    ⚠️  LIKELY SOURCE: CustomerCommandServiceIntegrationTest");
    } else if (name.startsWith("CUST-")) {
      System.out.println("    ⚠️  LIKELY SOURCE: OpportunityDatabaseIntegrationTest");
    } else if (name.equals("Test Company")) {
      System.out.println(
          "    ⚠️  LIKELY SOURCE: OpportunityServiceStageTransitionTest or OpportunityResourceIntegrationTest");
    }
  }
}

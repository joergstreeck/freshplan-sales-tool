package de.freshplan.test;

import de.freshplan.domain.customer.repository.CustomerRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Mid-run check for SEED data to identify when they disappear. This test runs in the middle of the
 * test suite alphabetically.
 */
@QuarkusTest
@Tag("quarantine")
public class MID_SeedDataCheckTest {

  @Inject CustomerRepository customerRepository;
  @Inject EntityManager em;

  @Test
  @Transactional
  public void checkSeedDataMidRun() {
    System.out.println("\n" + "=".repeat(80));
    System.out.println("=== MID-RUN SEED DATA CHECK ===");
    System.out.println("=".repeat(80));

    // Count SEED customers
    long seedCount = customerRepository.count("customerNumber LIKE ?1", "SEED-%");
    long totalCount = customerRepository.count();

    System.out.println("Current Status:");
    System.out.println("├─ Total Customers: " + totalCount);
    System.out.println("└─ SEED Customers: " + seedCount);

    if (seedCount < 20) {
      System.out.println("\n⚠️ SEED DATA LOST BEFORE MID_SeedDataCheckTest!");

      // Try to identify which test classes have run before this
      System.out.println("\nThis test runs alphabetically after:");
      System.out.println("- All tests starting with A-L");
      System.out.println("- DatabaseCleanupTest");
      System.out.println("- DatabaseDeepCleanupTest");
      System.out.println("- BaseIntegrationTestWithCleanup subclasses");

      // Check if BaseIntegrationTestWithCleanup deleted them
      var deletedByTestData =
          em.createNativeQuery(
                  "SELECT COUNT(*) FROM customers "
                      + "WHERE customer_number LIKE 'SEED-%' "
                      + "AND is_test_data = true")
              .getSingleResult();

      System.out.println("\nSEED customers with is_test_data=true: " + deletedByTestData);
      System.out.println("(These would be deleted by BaseIntegrationTestWithCleanup!)");
    }

    System.out.println("=".repeat(80));
  }
}

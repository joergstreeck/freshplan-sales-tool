package de.freshplan.test;

import de.freshplan.domain.customer.repository.CustomerRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
/** Analyse und Cleanup der Test-Datenbank */
@QuarkusTest
@Tag("quarantine")@TestTransaction // CI-Fix: Rollback nach Test für Database Growth Check
public class DatabaseCleanupTest {

  @Inject CustomerRepository customerRepository;

  @Test
  public void analyzeTestData() {
    long totalCustomers = customerRepository.count();
    System.out.println("=== DATABASE ANALYSIS ===");
    System.out.println("Total Customers: " + totalCustomers);

    // Analyze test patterns
    long testPattern1 = customerRepository.count("companyName LIKE ?1", "[TEST]%");
    long testPattern2 = customerRepository.count("companyName LIKE ?1", "Test%");
    long testPattern3 = customerRepository.count("companyName LIKE ?1", "%Test%");
    long testPattern4 = customerRepository.count("customerNumber LIKE ?1", "KD-TEST%");
    long testPattern5 = customerRepository.count("customerNumber LIKE ?1", "KD-S%"); // Search tests
    long testPattern6 = customerRepository.count("customerNumber LIKE ?1", "KD-E%"); // Export tests

    System.out.println("Customers with [TEST] prefix: " + testPattern1);
    System.out.println("Customers starting with 'Test': " + testPattern2);
    System.out.println("Customers containing 'Test': " + testPattern3);
    System.out.println("Customer numbers with KD-TEST: " + testPattern4);
    System.out.println("Customer numbers with KD-S (Search tests): " + testPattern5);
    System.out.println("Customer numbers with KD-E (Export tests): " + testPattern6);

    // List some examples
    System.out.println("\n=== SAMPLE TEST CUSTOMERS ===");
    customerRepository
        .find("companyName LIKE ?1", "%Test%")
        .range(0, 10)
        .list()
        .forEach(
            c ->
                System.out.println(
                    String.format(
                        "ID: %s, Number: %s, Name: %s, Created: %s",
                        c.getId(), c.getCustomerNumber(), c.getCompanyName(), c.getCreatedAt())));
  }

  @Test
  @Transactional
  public void cleanupTestData() {
    System.out.println("\n=== CLEANUP ANALYSIS (DRY RUN) ===");

    // Count what would be deleted
    long toDelete1 = customerRepository.count("customerNumber LIKE ?1", "KD-S%");
    long toDelete2 = customerRepository.count("customerNumber LIKE ?1", "KD-E%");
    long toDelete3 = customerRepository.count("customerNumber LIKE ?1", "KD-EVT%");
    long toDelete4 = customerRepository.count("customerNumber LIKE ?1", "KD-CQRS%");
    long toDelete5 = customerRepository.count("companyName LIKE ?1", "%Search Solutions%");
    long toDelete6 = customerRepository.count("companyName LIKE ?1", "%Priority Customer%");
    long toDelete7 = customerRepository.count("companyName LIKE ?1", "%Relevance Test%");
    long toDelete8 = customerRepository.count("companyName LIKE ?1", "%Export%");

    long totalToDelete =
        toDelete1 + toDelete2 + toDelete3 + toDelete4 + toDelete5 + toDelete6 + toDelete7
            + toDelete8;

    System.out.println("Would delete KD-S* customers: " + toDelete1);
    System.out.println("Would delete KD-E* customers: " + toDelete2);
    System.out.println("Would delete KD-EVT* customers: " + toDelete3);
    System.out.println("Would delete KD-CQRS* customers: " + toDelete4);
    System.out.println("Would delete *Search Solutions* customers: " + toDelete5);
    System.out.println("Would delete *Priority Customer* customers: " + toDelete6);
    System.out.println("Would delete *Relevance Test* customers: " + toDelete7);
    System.out.println("Would delete *Export* customers: " + toDelete8);
    System.out.println("\nTOTAL TO DELETE: " + totalToDelete);

    // Actually perform cleanup - handle foreign key constraints
    System.out.println("\n=== PERFORMING CLEANUP ===");
    System.out.println("Note: Some deletes may fail due to foreign key constraints");

    long deleted = 0;
    try {
      // Delete by customer number patterns
      deleted += customerRepository.delete("customerNumber LIKE ?1", "KD-S%");
      System.out.println("Deleted KD-S*: " + deleted);
    } catch (Exception e) {
      System.out.println("Could not delete KD-S*: " + e.getMessage());
    }

    try {
      long del = customerRepository.delete("customerNumber LIKE ?1", "KD-E%");
      deleted += del;
      System.out.println("Deleted KD-E*: " + del);
    } catch (Exception e) {
      System.out.println("Could not delete KD-E*: " + e.getMessage());
    }

    try {
      long del = customerRepository.delete("customerNumber LIKE ?1", "KD-EVT%");
      deleted += del;
      System.out.println("Deleted KD-EVT*: " + del);
    } catch (Exception e) {
      System.out.println("Could not delete KD-EVT*: " + e.getMessage());
    }

    System.out.println("\nTOTAL DELETED: " + deleted + " test customers");
    System.out.println("Remaining customers: " + customerRepository.count());
  }
}

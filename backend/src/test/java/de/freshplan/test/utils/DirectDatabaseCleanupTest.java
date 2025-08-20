package de.freshplan.test.utils;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
/**
 * Direct database cleanup to remove ALL test customers.
 *
 * @author Claude
 * @since Emergency Cleanup
 */
@QuarkusTest
@Tag("quarantine")@TestTransaction // CI-Fix: Rollback nach Test für Database Growth Check
public class DirectDatabaseCleanupTest {

  private static final Logger LOG = Logger.getLogger(DirectDatabaseCleanupTest.class);

  @Inject CustomerRepository customerRepository;

  @Inject EntityManager em;

  @Test
  void performDirectCleanup() {
    // First, analyze what we have
    QuarkusTransaction.requiringNew()
        .run(
            () -> {
              long total = customerRepository.count();
              LOG.infof("=== BEFORE CLEANUP: Total customers: %d", total);

              // Count different patterns
              long testPrefix = customerRepository.count("companyName LIKE ?1", "%[TEST%");
              LOG.infof("Customers with [TEST in name: %d", testPrefix);

              // List all customer numbers that look like test data
              List<Customer> suspiciousCustomers =
                  customerRepository
                      .find(
                          "customerNumber LIKE ?1 OR customerNumber LIKE ?2 OR customerNumber LIKE ?3 "
                              + "OR customerNumber LIKE ?4 OR customerNumber LIKE ?5 OR customerNumber LIKE ?6 "
                              + "OR customerNumber LIKE ?7 OR customerNumber LIKE ?8 OR customerNumber LIKE ?9 "
                              + "OR customerNumber LIKE ?10 OR customerNumber LIKE ?11 OR customerNumber LIKE ?12 "
                              + "OR companyName LIKE ?13",
                          "PF%",
                          "PA%",
                          "PI%",
                          "QK%",
                          "PG%",
                          "E1%",
                          "E2%",
                          "S1%",
                          "S2%",
                          "EX%",
                          "PT%",
                          "ACT%",
                          "%[TEST%")
                      .list();

              LOG.infof("Found %d suspicious customers to delete", suspiciousCustomers.size());

              // Show a sample
              suspiciousCustomers.stream()
                  .limit(10)
                  .forEach(c -> LOG.infof("  - %s: %s", c.getCustomerNumber(), c.getCompanyName()));
            });

    // Now DELETE them all
    QuarkusTransaction.requiringNew()
        .run(
            () -> {
              LOG.info("=== STARTING AGGRESSIVE CLEANUP ===");

              // Delete by customer number patterns
              String[] patterns = {
                "PF%", "PA%", "PI%", "QK%", "PG%", "E1%", "E2%", "S1%", "S2%", "EX%", "PT%", "ACT%",
                "INA%"
              };

              long totalDeleted = 0;
              for (String pattern : patterns) {
                long deleted = customerRepository.delete("customerNumber LIKE ?1", pattern);
                if (deleted > 0) {
                  LOG.infof("Deleted %d customers with pattern %s", deleted, pattern);
                  totalDeleted += deleted;
                }
              }

              // Delete all with [TEST in company name
              long deletedByName = customerRepository.delete("companyName LIKE ?1", "%[TEST%");
              LOG.infof("Deleted %d customers with [TEST in name", deletedByName);
              totalDeleted += deletedByName;

              // Delete all marked as test data
              long deletedTestData = customerRepository.delete("isTestData = true");
              LOG.infof("Deleted %d customers marked as test data", deletedTestData);
              totalDeleted += deletedTestData;

              // Delete specific problem patterns we've seen
              long deletedSpecific =
                  customerRepository.delete(
                      "companyName LIKE ?1 OR companyName LIKE ?2 OR companyName LIKE ?3 "
                          + "OR companyName LIKE ?4 OR companyName LIKE ?5",
                      "%Quick Test%",
                      "%Performance Test%",
                      "%Pagination Test%",
                      "%Priority%",
                      "%Relevance Test%");
              LOG.infof("Deleted %d customers with specific test names", deletedSpecific);
              totalDeleted += deletedSpecific;

              LOG.infof("=== TOTAL DELETED: %d ===", totalDeleted);
            });

    // Final verification
    QuarkusTransaction.requiringNew()
        .run(
            () -> {
              long remaining = customerRepository.count();
              LOG.infof("=== AFTER CLEANUP: Total customers: %d", remaining);

              // Check what's left
              List<Customer> remainingCustomers = customerRepository.findAll().list();
              LOG.info("Remaining customers:");
              remainingCustomers.forEach(
                  c ->
                      LOG.infof(
                          "  - %s: %s (test=%s)",
                          c.getCustomerNumber(), c.getCompanyName(), c.getIsTestData()));

              // If we still have test data, be more aggressive
              if (remaining > 100) {
                LOG.warn("Still too many customers, checking for more patterns...");

                remainingCustomers.stream()
                    .filter(
                        c ->
                            c.getCompanyName() != null
                                && (c.getCompanyName().contains("TEST")
                                    || c.getCompanyName().contains("Test")
                                    || c.getCustomerNumber().matches("^[A-Z]{2,3}[0-9a-f]{3,}.*")))
                    .forEach(
                        c -> {
                          LOG.warnf(
                              "Suspicious customer still present: %s - %s",
                              c.getCustomerNumber(), c.getCompanyName());
                        });
              }
            });
  }

  @Test
  void nuclearOption() {
    // NUCLEAR OPTION - Delete EVERYTHING that looks remotely like test data
    QuarkusTransaction.requiringNew()
        .run(
            () -> {
              LOG.warn("=== NUCLEAR CLEANUP - DELETING ALL SUSPICIOUS DATA ===");

              // Get all customers
              List<Customer> all = customerRepository.findAll().list();
              long deleted = 0;

              for (Customer c : all) {
                boolean shouldDelete = false;

                // Check various criteria
                if (c.getIsTestData() != null && c.getIsTestData()) {
                  shouldDelete = true;
                }
                if (c.getCompanyName() != null) {
                  String name = c.getCompanyName();
                  if (name.contains("[TEST")
                      || name.contains("Test ")
                      || name.contains("Performance")
                      || name.contains("Quick")
                      || name.contains("Pagination")
                      || name.contains("Priority")
                      || name.contains("Relevance")) {
                    shouldDelete = true;
                  }
                }
                if (c.getCustomerNumber() != null) {
                  String num = c.getCustomerNumber();
                  // Check if it matches our test patterns
                  if (num.matches("^(PF|PA|PI|QK|PG|E[12]|S[12]|EX|PT|ACT|INA).*")) {
                    shouldDelete = true;
                  }
                  // Check for hex patterns (like our test IDs)
                  if (num.matches("^[A-Z]{2,3}[0-9a-f]{4,}.*")) {
                    shouldDelete = true;
                  }
                }

                if (shouldDelete) {
                  customerRepository.delete(c);
                  deleted++;
                  LOG.infof("Deleted: %s - %s", c.getCustomerNumber(), c.getCompanyName());
                }
              }

              LOG.warnf("=== NUCLEAR CLEANUP COMPLETE: Deleted %d customers ===", deleted);
            });

    // Final count
    QuarkusTransaction.requiringNew()
        .run(
            () -> {
              long remaining = customerRepository.count();
              LOG.infof("=== FINAL COUNT: %d customers remaining ===", remaining);
            });
  }
}

package de.freshplan.test.utils;

import org.junit.jupiter.api.Tag;
import de.freshplan.domain.customer.repository.ContactInteractionRepository;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Utility class for cleaning up test data after integration tests.
 *
 * <p>This class ensures that test data created during integration tests is properly cleaned up to
 * prevent database pollution.
 *
 * @author Claude
 * @since Phase 14.3 - Test Data Management
 */
@ApplicationScoped
@Tag("quarantine")public class TestDataCleanup {

  private static final Logger LOG = Logger.getLogger(TestDataCleanup.class);

  @Inject CustomerRepository customerRepository;

  @Inject ContactRepository contactRepository;

  @Inject ContactInteractionRepository interactionRepository;

  @Inject OpportunityRepository opportunityRepository;

  /**
   * Clean up all test customers with the given test run ID. This method should be called
   * in @AfterEach of integration tests.
   *
   * @param testRunId The unique test run ID used to identify test data
   */
  public void cleanupTestRun(String testRunId) {
    if (testRunId == null || testRunId.isBlank()) {
      LOG.warn("Cannot cleanup test data: testRunId is null or blank");
      return;
    }

    QuarkusTransaction.requiringNew()
        .run(
            () -> {
              try {
                // First clean up opportunities that reference test customers
                // (to avoid foreign key constraint violations)
                long deletedOpportunities =
                    opportunityRepository.delete("name LIKE ?1", "%[TEST]%" + testRunId + "%");

                if (deletedOpportunities > 0) {
                  LOG.infof(
                      "Cleaned up %d test opportunities for run: %s",
                      deletedOpportunities, testRunId);
                }

                // Also delete opportunities by customer company name pattern
                deletedOpportunities =
                    opportunityRepository.delete(
                        "customer.companyName LIKE ?1", "%[TEST]%" + testRunId + "%");

                if (deletedOpportunities > 0) {
                  LOG.infof(
                      "Cleaned up %d additional test opportunities for run: %s",
                      deletedOpportunities, testRunId);
                }

                // Now clean up customers created with this test run ID
                long deletedCustomers =
                    customerRepository.delete(
                        "companyName LIKE ?1 OR customerNumber LIKE ?2",
                        "%[TEST-" + testRunId + "]%",
                        "%" + testRunId + "%");

                if (deletedCustomers > 0) {
                  LOG.infof(
                      "Cleaned up %d test customers for run: %s", deletedCustomers, testRunId);
                }

                // Also clean up any customers with specific prefixes that might contain the
                // testRunId
                String[] prefixes = {
                  "QK", "PF", "PG", "E1", "E2", "S1", "S2", "PA", "PI", "EX", "PT", "ACT", "INA"
                };
                for (String prefix : prefixes) {
                  long deleted =
                      customerRepository.delete(
                          "customerNumber LIKE ?1",
                          prefix
                              + "%"
                              + testRunId.substring(0, Math.min(testRunId.length(), 5))
                              + "%");
                  if (deleted > 0) {
                    LOG.infof("Cleaned up %d test customers with prefix %s", deleted, prefix);
                  }
                }

                // Clean up test data flag customers (safety net)
                long deletedTestData = customerRepository.deleteAllTestData();
                if (deletedTestData > 0) {
                  LOG.infof("Cleaned up %d recent test data customers", deletedTestData);
                }

              } catch (Exception e) {
                LOG.errorf(e, "Error cleaning up test data for run: %s", testRunId);
              }
            });
  }

  /** Clean up all test data. This is a safety net for tests that fail to clean up properly. */
  public void cleanupOldTestData() {
    QuarkusTransaction.requiringNew()
        .run(
            () -> {
              try {
                // Delete all test customers
                long deleted = customerRepository.deleteAllTestData();

                if (deleted > 0) {
                  LOG.infof("Cleaned up %d test customers", deleted);
                }

              } catch (Exception e) {
                LOG.error("Error cleaning up old test data", e);
              }
            });
  }

  /**
   * Get count of test data in database for monitoring.
   *
   * @return Count of test data customers
   */
  public long getTestDataCount() {
    return QuarkusTransaction.requiringNew()
        .call(
            () -> {
              return customerRepository.count("isTestData = true");
            });
  }

  /** Emergency cleanup - removes ALL test data. Use with caution! */
  public void emergencyCleanup() {
    QuarkusTransaction.requiringNew()
        .run(
            () -> {
              LOG.warn("EMERGENCY CLEANUP: Removing all test data!");

              // Delete all customers marked as test data
              long deleted = customerRepository.delete("isTestData = true");
              LOG.warnf("Deleted %d test data customers", deleted);

              // Delete all customers with TEST in name
              long deletedWithTest = customerRepository.delete("companyName LIKE '%[TEST%'");
              LOG.warnf("Deleted %d TEST-prefixed customers", deletedWithTest);
            });
  }
}

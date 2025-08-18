package de.freshplan.test.utils;

import de.freshplan.domain.customer.repository.ContactInteractionRepository;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

/**
 * Emergency test to analyze and clean up test data pollution.
 *
 * <p>Run this test to see the current state of test data and clean it up.
 *
 * @author Claude
 * @since Phase 14.3 - Test Data Management Emergency
 */
@QuarkusTest
@TestTransaction // CI-Fix: Rollback nach Test für Database Growth Check
public class EmergencyTestDataCleanupTest {

  private static final Logger LOG = Logger.getLogger(EmergencyTestDataCleanupTest.class);

  @Inject CustomerRepository customerRepository;

  @Inject ContactRepository contactRepository;

  @Inject ContactInteractionRepository interactionRepository;

  @Inject OpportunityRepository opportunityRepository;

  @Inject EntityManager em;

  @Inject TestDataCleanup testDataCleanup;

  @Test
  void performCompleteCleanupWithDependencies() {
    // Umfassende Bereinigung mit Berücksichtigung von Foreign Keys
    QuarkusTransaction.requiringNew()
        .run(
            () -> {
              LOG.warn("========================================");
              LOG.warn("STARTING COMPLETE CLEANUP WITH DEPENDENCIES");
              LOG.warn("========================================");

              // 1. Analysiere Ausgangslage
              long totalCustomers = customerRepository.count();
              LOG.infof("Total customers before cleanup: %d", totalCustomers);

              // 2. Lösche zuerst abhängige Daten mit Native SQL

              // Zuerst customer_contacts löschen
              int deletedCustomerContacts =
                  em.createNativeQuery(
                          "DELETE FROM customer_contacts WHERE customer_id IN "
                              + "(SELECT id FROM customers WHERE "
                              + "customer_number LIKE 'PF%' OR customer_number LIKE 'PA%' OR "
                              + "customer_number LIKE 'PI%' OR customer_number LIKE 'QK%' OR "
                              + "customer_number LIKE 'PG%' OR customer_number LIKE 'E1%' OR "
                              + "customer_number LIKE 'E2%' OR customer_number LIKE 'S1%' OR "
                              + "customer_number LIKE 'S2%' OR customer_number LIKE 'EX%' OR "
                              + "customer_number LIKE 'PT%' OR customer_number LIKE 'ACT%' OR "
                              + "customer_number LIKE 'INA%' OR company_name LIKE '%[TEST%' OR "
                              + "is_test_data = true)")
                      .executeUpdate();

              if (deletedCustomerContacts > 0) {
                LOG.infof("Deleted %d customer_contacts", deletedCustomerContacts);
              }

              // Dann contact_interactions löschen
              int deletedInteractions =
                  em.createNativeQuery(
                          "DELETE FROM contact_interactions WHERE contact_id IN "
                              + "(SELECT id FROM customer_contacts WHERE customer_id IN "
                              + "(SELECT id FROM customers WHERE "
                              + "customer_number LIKE 'PF%' OR customer_number LIKE 'PA%' OR "
                              + "customer_number LIKE 'PI%' OR customer_number LIKE 'QK%' OR "
                              + "customer_number LIKE 'PG%' OR customer_number LIKE 'E1%' OR "
                              + "customer_number LIKE 'E2%' OR customer_number LIKE 'S1%' OR "
                              + "customer_number LIKE 'S2%' OR customer_number LIKE 'EX%' OR "
                              + "customer_number LIKE 'PT%' OR customer_number LIKE 'ACT%' OR "
                              + "customer_number LIKE 'INA%' OR company_name LIKE '%[TEST%' OR "
                              + "is_test_data = true))")
                      .executeUpdate();

              if (deletedInteractions > 0) {
                LOG.infof("Deleted %d contact_interactions", deletedInteractions);
              }

              // Dann customer_contacts nochmal löschen (nach interactions)
              int deletedContacts =
                  em.createNativeQuery(
                          "DELETE FROM customer_contacts WHERE customer_id IN "
                              + "(SELECT id FROM customers WHERE "
                              + "customer_number LIKE 'PF%' OR customer_number LIKE 'PA%' OR "
                              + "customer_number LIKE 'PI%' OR customer_number LIKE 'QK%' OR "
                              + "customer_number LIKE 'PG%' OR customer_number LIKE 'E1%' OR "
                              + "customer_number LIKE 'E2%' OR customer_number LIKE 'S1%' OR "
                              + "customer_number LIKE 'S2%' OR customer_number LIKE 'EX%' OR "
                              + "customer_number LIKE 'PT%' OR customer_number LIKE 'ACT%' OR "
                              + "customer_number LIKE 'INA%' OR company_name LIKE '%[TEST%' OR "
                              + "is_test_data = true)")
                      .executeUpdate();

              if (deletedContacts > 0) {
                LOG.infof("Deleted %d contacts", deletedContacts);
              }

              // Dann customer_timeline_events löschen
              int deletedTimeline =
                  em.createNativeQuery(
                          "DELETE FROM customer_timeline_events WHERE customer_id IN "
                              + "(SELECT id FROM customers WHERE "
                              + "customer_number LIKE 'PF%' OR customer_number LIKE 'PA%' OR "
                              + "customer_number LIKE 'PI%' OR customer_number LIKE 'QK%' OR "
                              + "customer_number LIKE 'PG%' OR customer_number LIKE 'E1%' OR "
                              + "customer_number LIKE 'E2%' OR customer_number LIKE 'S1%' OR "
                              + "customer_number LIKE 'S2%' OR customer_number LIKE 'EX%' OR "
                              + "customer_number LIKE 'PT%' OR customer_number LIKE 'ACT%' OR "
                              + "customer_number LIKE 'INA%' OR company_name LIKE '%[TEST%' OR "
                              + "is_test_data = true)")
                      .executeUpdate();

              if (deletedTimeline > 0) {
                LOG.infof("Deleted %d customer_timeline_events", deletedTimeline);
              }

              // Dann opportunities löschen
              int deletedOpportunities =
                  em.createNativeQuery(
                          "DELETE FROM opportunities WHERE customer_id IN "
                              + "(SELECT id FROM customers WHERE "
                              + "customer_number LIKE 'PF%' OR customer_number LIKE 'PA%' OR "
                              + "customer_number LIKE 'PI%' OR customer_number LIKE 'QK%' OR "
                              + "customer_number LIKE 'PG%' OR customer_number LIKE 'E1%' OR "
                              + "customer_number LIKE 'E2%' OR customer_number LIKE 'S1%' OR "
                              + "customer_number LIKE 'S2%' OR customer_number LIKE 'EX%' OR "
                              + "customer_number LIKE 'PT%' OR customer_number LIKE 'ACT%' OR "
                              + "customer_number LIKE 'INA%' OR company_name LIKE '%[TEST%' OR "
                              + "is_test_data = true)")
                      .executeUpdate();

              if (deletedOpportunities > 0) {
                LOG.infof("Deleted %d opportunities", deletedOpportunities);
              }

              // 3. Jetzt können wir die Customers löschen
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

              // Lösche alle mit [TEST im Namen
              long deletedByName = customerRepository.delete("companyName LIKE ?1", "%[TEST%");
              LOG.infof("Deleted %d customers with [TEST in name", deletedByName);
              totalDeleted += deletedByName;

              // Lösche alle markierten Testdaten
              long deletedTestData = customerRepository.delete("isTestData = true");
              LOG.infof("Deleted %d customers marked as test data", deletedTestData);
              totalDeleted += deletedTestData;

              LOG.warn("========================================");
              LOG.warnf("TOTAL DELETED: %d customers", totalDeleted);
              LOG.warn("========================================");

              // Finale Überprüfung
              long remaining = customerRepository.count();
              LOG.infof("Remaining customers after cleanup: %d", remaining);
            });
  }

  @Test
  void analyzeAndCleanupTestData() {
    // First, analyze the current situation
    QuarkusTransaction.requiringNew()
        .run(
            () -> {
              long totalCustomers = customerRepository.count();
              long testDataMarked = customerRepository.count("isTestData = true");
              long testPrefix = customerRepository.count("companyName LIKE '%[TEST%'");
              long qkPrefix = customerRepository.count("customerNumber LIKE 'QK%'");
              long pfPrefix = customerRepository.count("customerNumber LIKE 'PF%'");
              long pgPrefix = customerRepository.count("customerNumber LIKE 'PG%'");
              long e1Prefix = customerRepository.count("customerNumber LIKE 'E1%'");
              long e2Prefix = customerRepository.count("customerNumber LIKE 'E2%'");
              long s1Prefix = customerRepository.count("customerNumber LIKE 'S1%'");
              long s2Prefix = customerRepository.count("customerNumber LIKE 'S2%'");
              long paPrefix = customerRepository.count("customerNumber LIKE 'PA%'");
              long piPrefix = customerRepository.count("customerNumber LIKE 'PI%'");

              LOG.info("========================================");
              LOG.info("TEST DATA ANALYSIS REPORT");
              LOG.info("========================================");
              LOG.infof("Total customers in database: %d", totalCustomers);
              LOG.infof("Customers marked as test data: %d", testDataMarked);
              LOG.infof("Customers with [TEST prefix: %d", testPrefix);
              LOG.info("----------------------------------------");
              LOG.info("Test prefixes found:");
              if (qkPrefix > 0) LOG.infof("  QK prefix: %d", qkPrefix);
              if (pfPrefix > 0) LOG.infof("  PF prefix: %d", pfPrefix);
              if (pgPrefix > 0) LOG.infof("  PG prefix: %d", pgPrefix);
              if (e1Prefix > 0) LOG.infof("  E1 prefix: %d", e1Prefix);
              if (e2Prefix > 0) LOG.infof("  E2 prefix: %d", e2Prefix);
              if (s1Prefix > 0) LOG.infof("  S1 prefix: %d", s1Prefix);
              if (s2Prefix > 0) LOG.infof("  S2 prefix: %d", s2Prefix);
              if (paPrefix > 0) LOG.infof("  PA prefix: %d", paPrefix);
              if (piPrefix > 0) LOG.infof("  PI prefix: %d", piPrefix);
              LOG.info("========================================");
            });

    // Now perform cleanup
    LOG.info("Starting cleanup...");

    // Clean up old test data
    testDataCleanup.cleanupOldTestData();

    // Emergency cleanup if needed
    long remaining = testDataCleanup.getTestDataCount();
    if (remaining > 50) {
      LOG.warnf("Still %d test records remaining, performing emergency cleanup!", remaining);
      testDataCleanup.emergencyCleanup();
    }

    // Final check
    QuarkusTransaction.requiringNew()
        .run(
            () -> {
              long totalAfter = customerRepository.count();
              long testDataAfter = customerRepository.count("isTestData = true");

              LOG.info("========================================");
              LOG.info("CLEANUP COMPLETE");
              LOG.info("========================================");
              LOG.infof("Total customers after cleanup: %d", totalAfter);
              LOG.infof("Test data remaining: %d", testDataAfter);
              LOG.info("========================================");
            });
  }
}

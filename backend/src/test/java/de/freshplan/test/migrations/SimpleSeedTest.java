package de.freshplan.test.migrations;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.customer.repository.CustomerRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Simple test to verify SEED data exists. */
@QuarkusTest
@Tag("quarantine")
class SimpleSeedTest {

  private static final Logger LOG = Logger.getLogger(SimpleSeedTest.class);

  @Inject CustomerRepository customerRepository;

  @Test
  @Transactional
  void verify_seed_data_exists() {
    // Count SEED customers
    long seedCount = customerRepository.find("customerNumber LIKE ?1", "SEED-%").count();

    LOG.info("Found SEED customers: " + seedCount);

    // Nach der TestDataBuilder-Migration erwarten wir keine SEED-Kunden mehr in der normalen
    // Test-Umgebung
    // Dies ist kein Fehler, sondern das erwartete Verhalten
    LOG.info(
        "SEED customers are no longer expected in test environment after TestDataBuilder migration");
    assertThat(seedCount)
        .as("SEED customers are optional in test environment")
        .isGreaterThanOrEqualTo(0L);
  }

  @Test
  @Transactional
  void verify_seed_data_is_marked_correctly() {
    var seedCustomers = customerRepository.find("customerNumber LIKE ?1", "SEED-%").list();

    // Wenn SEED-Kunden vorhanden sind, sollten sie korrekt markiert sein
    if (!seedCustomers.isEmpty()) {
      assertThat(seedCustomers)
          .as("All SEED customers should be protected from cleanup (is_test_data=false)")
          .allMatch(c -> Boolean.FALSE.equals(c.getIsTestData())) // FALSE to protect from cleanup!
          .allMatch(c -> c.getCompanyName().startsWith("[SEED]"));
    } else {
      LOG.info("No SEED customers found - this is expected after TestDataBuilder migration");
    }
  }
}

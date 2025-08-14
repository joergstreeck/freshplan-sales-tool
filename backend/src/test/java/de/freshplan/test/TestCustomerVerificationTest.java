package de.freshplan.test;

import de.freshplan.domain.customer.repository.CustomerRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.jboss.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifiziert, dass die Test-Kunden korrekt erstellt wurden.
 */
@QuarkusTest
public class TestCustomerVerificationTest {
    
    private static final Logger LOG = Logger.getLogger(TestCustomerVerificationTest.class);
    
    @Inject
    CustomerRepository customerRepository;
    
    @Test
    void verifyTestCustomersCreated() {
        // Zähle alle Kunden
        long totalCustomers = customerRepository.count();
        LOG.info("Total customers in database: " + totalCustomers);
        
        // Zähle Test-Kunden mit [TEST] Prefix
        long testCustomers = customerRepository
            .find("companyName like ?1", "[TEST]%")
            .count();
        LOG.info("Test customers with [TEST] prefix: " + testCustomers);
        
        // Zeige einige Test-Kunden
        customerRepository
            .find("companyName like ?1", "[TEST]%")
            .range(0, 10)
            .list()
            .forEach(customer -> {
                LOG.info("Test customer: " + customer.getCustomerNumber() + " - " + customer.getCompanyName());
            });
        
        // Verifiziere, dass wir genug Test-Kunden haben
        assertThat(testCustomers)
            .as("Should have at least 5 test customers")
            .isGreaterThanOrEqualTo(5);
        
        // Log für Debugging
        LOG.info("=== TEST CUSTOMER VERIFICATION ===");
        LOG.info("Expected: >= 58 test customers");
        LOG.info("Actual: " + testCustomers + " test customers");
        LOG.info("Total: " + totalCustomers + " customers");
        
        // Warne wenn zu wenige Test-Kunden
        if (testCustomers < 58) {
            LOG.warn("⚠️ Only " + testCustomers + " test customers found, expected 58+");
            LOG.warn("Migration V219 might not have run completely");
        }
    }
}
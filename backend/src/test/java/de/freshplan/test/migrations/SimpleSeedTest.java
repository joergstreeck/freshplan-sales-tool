package de.freshplan.test.migrations;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.customer.repository.CustomerRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

/**
 * Simple test to verify SEED data exists.
 */
@QuarkusTest
class SimpleSeedTest {
    
    @Inject
    CustomerRepository customerRepository;
    
    @Test
    @Transactional
    void verify_seed_data_exists() {
        // Count SEED customers
        long seedCount = customerRepository
            .find("customerNumber LIKE ?1", "SEED-%")
            .count();
            
        System.out.println("Found SEED customers: " + seedCount);
            
        assertThat(seedCount)
            .as("Should have at least 20 SEED customers from V9998")
            .isGreaterThanOrEqualTo(20L);
    }
    
    @Test
    @Transactional
    void verify_seed_data_is_marked_correctly() {
        var seedCustomers = customerRepository
            .find("customerNumber LIKE ?1", "SEED-%")
            .list();
            
        assertThat(seedCustomers)
            .as("All SEED customers should be marked as test data")
            .allMatch(c -> Boolean.TRUE.equals(c.getIsTestData()))
            .allMatch(c -> c.getCompanyName().startsWith("[SEED]"));
    }
}
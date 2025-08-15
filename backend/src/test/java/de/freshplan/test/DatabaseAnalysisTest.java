package de.freshplan.test;

import de.freshplan.domain.customer.repository.CustomerRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * Simple test for CI/CD database growth monitoring
 */
@QuarkusTest
public class DatabaseAnalysisTest {

    @Inject
    CustomerRepository customerRepository;

    @Test
    public void countCustomers() {
        long count = customerRepository.count();
        System.out.println("Total Customers: " + count);
    }
}
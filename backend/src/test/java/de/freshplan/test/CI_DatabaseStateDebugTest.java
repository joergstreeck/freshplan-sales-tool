package de.freshplan.test;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * DEBUG TEST for CI - Shows exact database state
 * Named with CI_ prefix to run early alphabetically
 */
@QuarkusTest
public class CI_DatabaseStateDebugTest {
    
    @Inject CustomerRepository customerRepository;
    @Inject OpportunityRepository opportunityRepository;
    
    @Test
    @TestTransaction
    void ci_showExactDatabaseState() {
        System.out.println("\n" + "=".repeat(120));
        System.out.println("=== CI DATABASE STATE DEBUG ===");
        System.out.println("=".repeat(120));
        
        // 1. Customer Count
        long totalCustomers = customerRepository.count();
        System.out.println("\n### CUSTOMER COUNTS ###");
        System.out.println("Total Customers: " + totalCustomers);
        
        // 2. Count by patterns
        long withTestPrefix = customerRepository.count("companyName LIKE '[TEST]%'");
        long withSeedPrefix = customerRepository.count("companyName LIKE '[SEED]%'");
        long withIsTestData = customerRepository.count("isTestData = true");
        
        System.out.println("With [TEST] prefix: " + withTestPrefix);
        System.out.println("With [SEED] prefix: " + withSeedPrefix);
        System.out.println("With isTestData=true: " + withIsTestData);
        
        // 3. Show ALL customers
        System.out.println("\n### ALL CUSTOMERS IN DATABASE ###");
        customerRepository.findAll().list().forEach(c -> {
            System.out.printf("- %s | Number: %s | isTestData: %s | createdBy: %s | createdAt: %s%n",
                c.getCompanyName(),
                c.getCustomerNumber(),
                c.getIsTestData(),
                c.getCreatedBy(),
                c.getCreatedAt()
            );
        });
        
        // 4. Show customers WITHOUT proper marking
        System.out.println("\n### PROBLEMATIC CUSTOMERS (no [TEST] prefix or isTestData) ###");
        customerRepository
            .find("companyName NOT LIKE '[TEST]%' AND companyName NOT LIKE '[SEED]%'")
            .list()
            .forEach(c -> {
                System.out.printf("❌ PROBLEM: %s | isTestData: %s | createdBy: %s%n",
                    c.getCompanyName(),
                    c.getIsTestData(),
                    c.getCreatedBy()
                );
            });
        
        // 5. Opportunity Count
        long totalOpportunities = opportunityRepository.count();
        System.out.println("\n### OPPORTUNITY COUNTS ###");
        System.out.println("Total Opportunities: " + totalOpportunities);
        
        // 6. Show some opportunities
        if (totalOpportunities > 0) {
            System.out.println("\n### FIRST 5 OPPORTUNITIES ###");
            opportunityRepository.findAll().page(0, 5).list().forEach(o -> {
                Customer customer = o.getCustomer();
                String customerName = customer != null ? customer.getCompanyName() : "NO CUSTOMER";
                System.out.printf("- %s | Customer: %s | Value: %s%n",
                    o.getName(),
                    customerName,
                    o.getExpectedValue()
                );
            });
        }
        
        System.out.println("\n" + "=".repeat(120));
        System.out.println("=== END DEBUG ===");
        System.out.println("=".repeat(120) + "\n");
    }
}
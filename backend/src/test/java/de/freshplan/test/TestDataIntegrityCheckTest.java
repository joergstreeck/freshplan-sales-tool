package de.freshplan.test;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import java.util.List;

/**
 * Test Data Integrity Check - Findet inkonsistente Test-Daten
 */
@QuarkusTest
public class TestDataIntegrityCheckTest {
    
    @Inject CustomerRepository customerRepository;
    
    @Test
    void findInconsistentTestData() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("=== TEST DATA INTEGRITY CHECK ===");
        System.out.println("=".repeat(100));
        
        // 1. Find customers that look like test data but aren't marked as such
        System.out.println("\n### SUSPICIOUS TEST-LIKE DATA ###");
        
        List<Customer> suspiciousCustomers = customerRepository
            .find("(companyName like ?1 or companyName like ?2 or customerNumber like ?3) and (isTestData = false or isTestData is null)",
                  "%Test%", "%test%", "KD%")
            .list();
        
        if (!suspiciousCustomers.isEmpty()) {
            System.out.println("Found " + suspiciousCustomers.size() + " suspicious customers:");
            suspiciousCustomers.forEach(c -> {
                System.out.printf("  - ID: %s | Number: %s | Name: %s | isTestData: %s | CreatedBy: %s%n",
                    c.getId(),
                    c.getCustomerNumber(),
                    c.getCompanyName(),
                    c.getIsTestData(),
                    c.getCreatedBy()
                );
            });
        } else {
            System.out.println("No suspicious customers found.");
        }
        
        // 2. Find customers with [TEST] prefix but isTestData != true
        System.out.println("\n### INCONSISTENT [TEST] PREFIX ###");
        
        List<Customer> inconsistentTestPrefix = customerRepository
            .find("companyName like ?1 and (isTestData = false or isTestData is null)", "[TEST]%")
            .list();
        
        if (!inconsistentTestPrefix.isEmpty()) {
            System.out.println("Found " + inconsistentTestPrefix.size() + " customers with [TEST] prefix but wrong flag:");
            inconsistentTestPrefix.forEach(c -> {
                System.out.printf("  - %s (isTestData=%s)%n", c.getCompanyName(), c.getIsTestData());
            });
        } else {
            System.out.println("All [TEST] prefixed customers have correct isTestData flag.");
        }
        
        // 3. Find customers created by test users
        System.out.println("\n### CUSTOMERS CREATED BY TEST USERS ###");
        
        List<Customer> testUserCreated = customerRepository
            .find("createdBy in (?1)", List.of("testuser", "test-user", "test-data-seeder", "testuser1", "testuser2"))
            .list();
        
        System.out.println("Found " + testUserCreated.size() + " customers created by test users:");
        testUserCreated.stream()
            .limit(10)
            .forEach(c -> {
                System.out.printf("  - %s created by %s (isTestData=%s)%n", 
                    c.getCompanyName(), c.getCreatedBy(), c.getIsTestData());
            });
        
        if (testUserCreated.size() > 10) {
            System.out.println("  ... and " + (testUserCreated.size() - 10) + " more");
        }
        
        // 4. Summary
        System.out.println("\n### SUMMARY ###");
        long totalCustomers = customerRepository.count();
        long markedAsTest = customerRepository.count("isTestData = ?1", true);
        long hasTestPrefix = customerRepository.count("companyName like ?1", "[TEST]%");
        
        System.out.printf("Total Customers: %d%n", totalCustomers);
        System.out.printf("Marked as Test (isTestData=true): %d%n", markedAsTest);
        System.out.printf("Has [TEST] prefix: %d%n", hasTestPrefix);
        
        if (hasTestPrefix != markedAsTest) {
            System.out.println("⚠️ WARNING: Mismatch between [TEST] prefix and isTestData flag!");
        }
        
        if (testUserCreated.size() > markedAsTest) {
            System.out.println("⚠️ WARNING: More customers created by test users than marked as test data!");
        }
        
        System.out.println("=".repeat(100) + "\n");
    }
}
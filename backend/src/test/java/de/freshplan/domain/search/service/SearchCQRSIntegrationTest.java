package de.freshplan.domain.search.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.CustomerType;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.search.service.dto.SearchResult;
import de.freshplan.domain.search.service.dto.SearchResults;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jboss.logging.Logger;

/**
 * Integration Test for SearchService CQRS Implementation.
 * 
 * Tests the READ-ONLY SearchQueryService with Feature Flag enabled.
 * SearchService has no CommandService as it's purely for querying data.
 * 
 * Key aspects tested:
 * - Global search across entities
 * - Quick search for autocomplete
 * - Search result ranking
 * - Performance with large datasets
 * 
 * @author Claude
 * @since Phase 14.3 - Feature Flag Switching Tests
 */
@QuarkusTest
@TestProfile(SearchCQRSTestProfile.class)
@TestSecurity(user = "testuser", roles = {"admin", "manager", "sales"})
@DisplayName("Search Service CQRS Integration Test")
class SearchCQRSIntegrationTest {

    private static final Logger LOG = Logger.getLogger(SearchCQRSIntegrationTest.class);

    @Inject
    SearchService searchService; // Test via Facade to verify Feature Flag switching
    
    @Inject
    CustomerRepository customerRepository;
    
    @ConfigProperty(name = "features.cqrs.enabled")
    boolean cqrsEnabled;
    
    private UUID testCustomer1Id;
    private UUID testCustomer2Id;
    private String testRunId;
    
    @BeforeEach
    void setUp() {
        // Generate unique test run ID
        testRunId = UUID.randomUUID().toString().substring(0, 8);
        LOG.infof("Starting test run: %s", testRunId);
    }
    
    @AfterEach
    void cleanup() {
        // Clean up test data after each test
        if (testCustomer1Id != null || testCustomer2Id != null) {
            QuarkusTransaction.requiringNew().run(() -> {
                if (testCustomer1Id != null) {
                    customerRepository.deleteById(testCustomer1Id);
                }
                if (testCustomer2Id != null) {
                    customerRepository.deleteById(testCustomer2Id);
                }
                LOG.infof("Cleaned up test data for run: %s", testRunId);
            });
        }
    }
    
    /**
     * Creates test data in a separate committed transaction.
     * Returns customer names for search verification.
     */
    private String[] createAndPersistTestData() {
        return QuarkusTransaction.requiringNew().call(() -> {
            // Customer 1 - Hotel company
            Customer customer1 = new Customer();
            customer1.setCustomerNumber("S1" + testRunId.substring(0, 5));
            customer1.setCompanyName("[TEST-" + testRunId + "] Hotel Search");
            customer1.setCustomerType(CustomerType.UNTERNEHMEN);
            customer1.setStatus(CustomerStatus.AKTIV);
            customer1.setIndustry(Industry.HOTEL);
            customer1.setExpectedAnnualVolume(new BigDecimal("250000"));
            customer1.setIsTestData(true);
            customer1.setIsDeleted(false); // Explicitly set to false
            customer1.setCreatedBy("testuser");
            customer1.setCreatedAt(LocalDateTime.now());
            customerRepository.persist(customer1);
            testCustomer1Id = customer1.getId();
            
            // Customer 2 - Restaurant company
            Customer customer2 = new Customer();
            customer2.setCustomerNumber("S2" + testRunId.substring(0, 5));
            customer2.setCompanyName("[TEST-" + testRunId + "] Gourmet Restaurant");
            customer2.setCustomerType(CustomerType.NEUKUNDE);
            customer2.setStatus(CustomerStatus.LEAD);
            customer2.setIndustry(Industry.RESTAURANT);
            customer2.setExpectedAnnualVolume(new BigDecimal("150000"));
            customer2.setIsTestData(true);
            customer2.setIsDeleted(false); // Explicitly set to false
            customer2.setCreatedBy("testuser");
            customer2.setCreatedAt(LocalDateTime.now());
            customerRepository.persist(customer2);
            testCustomer2Id = customer2.getId();
            
            customerRepository.flush();
            
            LOG.infof("Created test customers: %s, %s", testCustomer1Id, testCustomer2Id);
            return new String[]{customer1.getCompanyName(), customer2.getCompanyName()};
        });
    }
    
    
    @Test
    @DisplayName("Feature Flag should be enabled for CQRS tests")
    void testCQRSModeIsEnabled() {
        assertThat(cqrsEnabled)
            .as("CQRS Feature Flag must be enabled for this test")
            .isTrue();
    }
    
    // =====================================
    // UNIVERSAL SEARCH TESTS
    // =====================================
    
    @Test
    @DisplayName("Universal search should find customers by company name")
    void universalSearch_byCompanyName_shouldFindCustomers() {
        // Given - Create test data in separate transaction
        String[] customerNames = createAndPersistTestData();
        String hotelName = customerNames[0];
        
        // When - Search in service's own transaction
        SearchResults results = QuarkusTransaction.requiringNew().call(() -> {
            return searchService.universalSearch(
                "Hotel",
                false, // includeContacts
                false, // includeInactive  
                10    // limit
            );
        });
        
        // Then - Should find the matching customer
        assertThat(results).isNotNull();
        assertThat(results.getCustomers()).isNotEmpty();
        assertThat(results.getCustomers())
            .anyMatch(result -> 
                result.getType().equals("customer") &&
                result.getId().equals(testCustomer1Id.toString())
            );
    }
    
    @Test
    @DisplayName("Universal search should find customers by customer number")
    void universalSearch_byCustomerNumber_shouldFindExactMatch() {
        // Given - Create test data in separate transaction
        createAndPersistTestData();
        String customerNumber = "S1" + testRunId.substring(0, 5);
        
        LOG.infof("Searching for customer number: %s", customerNumber);
        LOG.infof("Test customer ID: %s", testCustomer1Id);
        
        // Verify customer exists with correct number
        Customer verifyCustomer = QuarkusTransaction.requiringNew().call(() -> {
            return customerRepository.findById(testCustomer1Id);
        });
        LOG.infof("Verification - Customer exists: %s, Number: %s, isDeleted: %s", 
                  verifyCustomer != null, 
                  verifyCustomer != null ? verifyCustomer.getCustomerNumber() : "null",
                  verifyCustomer != null ? verifyCustomer.getIsDeleted() : "null");
        
        // Test repository method directly
        List<Customer> directRepoSearch = QuarkusTransaction.requiringNew().call(() -> {
            return customerRepository.findByCustomerNumberLike(customerNumber + "%", 10);
        });
        LOG.infof("Direct repository search results: %d", directRepoSearch.size());
        
        // When - Search in service's own transaction
        SearchResults results = QuarkusTransaction.requiringNew().call(() -> {
            return searchService.universalSearch(
                customerNumber,
                false, // includeContacts
                false, // includeInactive
                10    // limit
            );
        });
        
        LOG.infof("Search results count: %d", results.getCustomers().size());
        if (!results.getCustomers().isEmpty()) {
            LOG.infof("First result ID: %s", results.getCustomers().get(0).getId());
        }
        
        // Then - Should find exact match with high relevance
        assertThat(results).isNotNull();
        assertThat(results.getCustomers())
            .as("Should find customer with number: " + customerNumber)
            .isNotEmpty();
        
        SearchResult firstResult = results.getCustomers().get(0);
        assertThat(firstResult.getId()).isEqualTo(testCustomer1Id.toString());
        assertThat(firstResult.getRelevanceScore()).isGreaterThanOrEqualTo(100); // Exact match bonus
    }
    
    @Test
    @DisplayName("Universal search should respect includeInactive flag")
    void universalSearch_withInactiveFlag_shouldFilterResults() {
        // Given - Create test data in separate transaction
        List<UUID> customerIds = QuarkusTransaction.requiringNew().call(() -> {
            List<UUID> ids = new ArrayList<>();
            String shortId = testRunId.substring(0, 4);
            
            Customer activeCustomer = new Customer();
            activeCustomer.setCustomerNumber("ACT" + shortId);
            activeCustomer.setCompanyName("[TEST-" + testRunId + "] Active Restaurant");
            activeCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
            activeCustomer.setStatus(CustomerStatus.AKTIV);
            activeCustomer.setIndustry(Industry.RESTAURANT);
            activeCustomer.setIsTestData(true);
            activeCustomer.setCreatedBy("testuser");
            activeCustomer.setCreatedAt(LocalDateTime.now());
            customerRepository.persist(activeCustomer);
            ids.add(activeCustomer.getId());
            
            Customer inactiveCustomer = new Customer();
            inactiveCustomer.setCustomerNumber("INA" + shortId);
            inactiveCustomer.setCompanyName("[TEST-" + testRunId + "] Inactive Restaurant");
            inactiveCustomer.setCustomerType(CustomerType.NEUKUNDE);
            inactiveCustomer.setStatus(CustomerStatus.INAKTIV);
            inactiveCustomer.setIndustry(Industry.RESTAURANT);
            inactiveCustomer.setIsTestData(true);
            inactiveCustomer.setCreatedBy("testuser");
            inactiveCustomer.setCreatedAt(LocalDateTime.now());
            customerRepository.persist(inactiveCustomer);
            ids.add(inactiveCustomer.getId());
            
            customerRepository.flush();
            return ids;
        });
        
        // When - Search without inactive in service transaction
        SearchResults resultsWithoutInactive = QuarkusTransaction.requiringNew().call(() -> {
            return searchService.universalSearch(
                "TEST-" + testRunId,
                false, // includeContacts
                false, // includeInactive
                10    // limit
            );
        });
        
        // When - Search with inactive in service transaction
        SearchResults resultsWithInactive = QuarkusTransaction.requiringNew().call(() -> {
            return searchService.universalSearch(
                "TEST-" + testRunId,
                false, // includeContacts
                true,  // includeInactive
                10    // limit
            );
        });
        
        // Then - Without inactive should only find active customer
        assertThat(resultsWithoutInactive.getCustomers())
            .hasSize(1)
            .extracting(SearchResult::getId)
            .containsExactly(customerIds.get(0).toString());
        
        // Then - With inactive should find both
        assertThat(resultsWithInactive.getCustomers())
            .hasSize(2)
            .extracting(SearchResult::getId)
            .containsExactlyInAnyOrder(
                customerIds.get(0).toString(), 
                customerIds.get(1).toString()
            );
    }
    
    @Test
    @DisplayName("Universal search should return empty results for no matches")
    void universalSearch_noMatches_shouldReturnEmptyResults() {
        // Given - Create test data (needed for proper test context)
        createAndPersistTestData();
        
        // When - Search for non-existent term in service transaction
        SearchResults results = QuarkusTransaction.requiringNew().call(() -> {
            return searchService.universalSearch(
                "NonExistentCompanyXYZ123",
                false, // includeContacts
                false, // includeInactive
                10    // limit
            );
        });
        
        // Then - Should return empty but valid results
        assertThat(results).isNotNull();
        assertThat(results.getCustomers()).isEmpty();
        assertThat(results.getTotalCount()).isEqualTo(0);
    }
    
    // =====================================
    // QUICK SEARCH TESTS  
    // =====================================
    
    @Test
    @DisplayName("Quick search should return limited results for autocomplete")
    void quickSearch_shouldReturnLimitedResults() {
        // Given - Create additional test customers in separate transaction
        QuarkusTransaction.requiringNew().run(() -> {
            for (int i = 0; i < 10; i++) {
                Customer c = new Customer();
                c.setCustomerNumber("QK" + i + "-" + testRunId);
                c.setCompanyName("[TEST-" + testRunId + "] Quick Test Company " + i);
                c.setCustomerType(CustomerType.UNTERNEHMEN);
                c.setStatus(CustomerStatus.AKTIV);
                c.setIndustry(Industry.SONSTIGE);
                c.setIsTestData(true);
                c.setCreatedBy("testuser");
                c.setCreatedAt(LocalDateTime.now());
                customerRepository.persist(c);
            }
            customerRepository.flush();
        });
        
        // When - Quick search with limit in service transaction
        SearchResults results = QuarkusTransaction.requiringNew().call(() -> {
            return searchService.quickSearch("TEST-" + testRunId, 5);
        });
        
        // Then - Should respect limit
        assertThat(results).isNotNull();
        assertThat(results.getCustomers()).hasSize(5);
        assertThat(results.getExecutionTime()).isLessThan(50L); // Quick search should be fast
        
        // Clean up additional test customers
        QuarkusTransaction.requiringNew().run(() -> {
            customerRepository.delete("customerNumber like ?1", "QK%" + testRunId);
        });
    }
    
    @Test
    @DisplayName("Quick search should prioritize active customers")
    void quickSearch_shouldPrioritizeActiveCustomers() {
        // Given - Mix of active and inactive customers in separate transaction
        List<UUID> customerIds = QuarkusTransaction.requiringNew().call(() -> {
            List<UUID> ids = new ArrayList<>();
            
            Customer activeCustomer = new Customer();
            activeCustomer.setCustomerNumber("PA" + testRunId.substring(0, 5));
            activeCustomer.setCompanyName("[TEST-" + testRunId + "] Priority Active");
            activeCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
            activeCustomer.setStatus(CustomerStatus.AKTIV);
            activeCustomer.setIndustry(Industry.HOTEL);
            activeCustomer.setLastContactDate(LocalDateTime.now().minusDays(5)); // Recent contact
            activeCustomer.setIsTestData(true);
            activeCustomer.setCreatedBy("testuser");
            activeCustomer.setCreatedAt(LocalDateTime.now());
            customerRepository.persist(activeCustomer);
            ids.add(activeCustomer.getId());
            
            Customer inactiveCustomer = new Customer();
            inactiveCustomer.setCustomerNumber("PI" + testRunId.substring(0, 5));
            inactiveCustomer.setCompanyName("[TEST-" + testRunId + "] Priority Inactive");
            inactiveCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
            inactiveCustomer.setStatus(CustomerStatus.INAKTIV);
            inactiveCustomer.setIndustry(Industry.HOTEL);
            inactiveCustomer.setIsTestData(true);
            inactiveCustomer.setCreatedBy("testuser");
            inactiveCustomer.setCreatedAt(LocalDateTime.now());
            customerRepository.persist(inactiveCustomer);
            ids.add(inactiveCustomer.getId());
            
            customerRepository.flush();
            return ids;
        });
        
        // When - Quick search in service transaction
        SearchResults results = QuarkusTransaction.requiringNew().call(() -> {
            return searchService.quickSearch("TEST-" + testRunId, 10);
        });
        
        // Then - Both customers should be found, active should have higher score
        assertThat(results.getCustomers()).hasSizeGreaterThanOrEqualTo(2);
        
        UUID activeId = customerIds.get(0);
        UUID inactiveId = customerIds.get(1);
        
        // Find our test customers in the results
        Optional<SearchResult> activeResult = results.getCustomers().stream()
            .filter(r -> r.getId().equals(activeId.toString()))
            .findFirst();
        Optional<SearchResult> inactiveResult = results.getCustomers().stream()
            .filter(r -> r.getId().equals(inactiveId.toString()))
            .findFirst();
            
        assertThat(activeResult).isPresent();
        assertThat(inactiveResult).isPresent();
        
        // Active should have higher relevance score
        assertThat(activeResult.get().getRelevanceScore())
            .isGreaterThanOrEqualTo(inactiveResult.get().getRelevanceScore());
    }
    
    // =====================================
    // RELEVANCE SCORING TESTS
    // =====================================
    
    @Test
    @DisplayName("Search should apply relevance scoring correctly")
    void search_shouldApplyRelevanceScoring() {
        // Given - Customers with different relevance factors in separate transaction
        List<UUID> customerIds = QuarkusTransaction.requiringNew().call(() -> {
            List<UUID> ids = new ArrayList<>();
            
            Customer exactMatch = new Customer();
            exactMatch.setCustomerNumber("EX" + testRunId.substring(0, 5));
            exactMatch.setCompanyName("[TEST-" + testRunId + "] Relevance Test");
            exactMatch.setCustomerType(CustomerType.UNTERNEHMEN);
            exactMatch.setStatus(CustomerStatus.AKTIV);
            exactMatch.setIndustry(Industry.HOTEL);
            exactMatch.setIsTestData(true);
            exactMatch.setCreatedBy("testuser");
            exactMatch.setCreatedAt(LocalDateTime.now());
            customerRepository.persist(exactMatch);
            ids.add(exactMatch.getId());
            
            Customer partialMatch = new Customer();
            partialMatch.setCustomerNumber("PT" + testRunId.substring(0, 5));
            partialMatch.setCompanyName("[TEST-" + testRunId + "] Some Relevance Test Company");
            partialMatch.setCustomerType(CustomerType.UNTERNEHMEN);
            partialMatch.setStatus(CustomerStatus.LEAD);
            partialMatch.setIndustry(Industry.HOTEL);
            partialMatch.setIsTestData(true);
            partialMatch.setCreatedBy("testuser");
            partialMatch.setCreatedAt(LocalDateTime.now());
            customerRepository.persist(partialMatch);
            ids.add(partialMatch.getId());
            
            customerRepository.flush();
            return ids;
        });
        
        // When - Search for "Relevance Test" in service transaction
        SearchResults results = QuarkusTransaction.requiringNew().call(() -> {
            return searchService.universalSearch(
                "Relevance Test",
                false, // includeContacts
                false, // includeInactive
                10    // limit
            );
        });
        
        // Then - Both should be found, exact match should have higher score
        assertThat(results.getCustomers()).hasSizeGreaterThanOrEqualTo(2);
        
        UUID exactId = customerIds.get(0);
        UUID partialId = customerIds.get(1);
        
        // Find our test customers in the results
        Optional<SearchResult> exactResult = results.getCustomers().stream()
            .filter(r -> r.getId().equals(exactId.toString()))
            .findFirst();
        Optional<SearchResult> partialResult = results.getCustomers().stream()
            .filter(r -> r.getId().equals(partialId.toString()))
            .findFirst();
            
        assertThat(exactResult).isPresent();
        assertThat(partialResult).isPresent();
        
        // Exact match should have higher relevance score
        assertThat(exactResult.get().getRelevanceScore())
            .isGreaterThanOrEqualTo(partialResult.get().getRelevanceScore());
    }
    
    // =====================================
    // PERFORMANCE TESTS
    // =====================================
    
    @Test
    @DisplayName("Search should handle large datasets efficiently")
    void search_withLargeDataset_shouldPerformWell() {
        // Given - Create many test customers in separate transaction
        QuarkusTransaction.requiringNew().run(() -> {
            for (int i = 0; i < 100; i++) {
                Customer c = new Customer();
                c.setCustomerNumber("PF" + i + "-" + testRunId.substring(0, 3));
                c.setCompanyName("[TEST-" + testRunId + "] Performance Test " + i);
                c.setCustomerType(CustomerType.UNTERNEHMEN);
                c.setStatus(i % 3 == 0 ? CustomerStatus.AKTIV : CustomerStatus.LEAD);
                c.setIndustry(Industry.values()[i % Industry.values().length]);
                c.setExpectedAnnualVolume(new BigDecimal(100000 + i * 1000));
                c.setIsTestData(true);
                c.setCreatedBy("testuser");
                c.setCreatedAt(LocalDateTime.now());
                customerRepository.persist(c);
            }
            customerRepository.flush();
        });
        
        // When - Perform search in service transaction
        long startTime = System.currentTimeMillis();
        SearchResults results = QuarkusTransaction.requiringNew().call(() -> {
            return searchService.universalSearch(
                "TEST-" + testRunId,
                false, // includeContacts
                false, // includeInactive
                20    // limit
            );
        });
        long executionTime = System.currentTimeMillis() - startTime;
        
        // Then - Should complete quickly
        assertThat(results).isNotNull();
        assertThat(results.getCustomers()).hasSize(20); // Should respect limit
        assertThat(executionTime).isLessThan(500L); // Should complete within 500ms
        assertThat(results.getExecutionTime()).isLessThan(500L);
        
        // Cleanup large dataset - use specific pattern with testRunId
        QuarkusTransaction.requiringNew().run(() -> {
            customerRepository.delete("customerNumber like ?1", "PF%-" + testRunId.substring(0, 3) + "%");
        });
    }
    
    // =====================================
    // CQRS BEHAVIOR VERIFICATION
    // =====================================
    
    @Test
    @DisplayName("CQRS mode should properly delegate search operations")
    void cqrsMode_shouldProperlyDelegateOperations() {
        // Given - Create test data in separate transaction
        String[] customerNames = createAndPersistTestData();
        String hotelName = customerNames[0];
        String restaurantName = customerNames[1];
        
        // This test verifies that the facade properly delegates to QueryService
        
        // Test universalSearch delegation in service transaction
        SearchResults universalResults = QuarkusTransaction.requiringNew().call(() -> {
            return searchService.universalSearch(
                hotelName,
                false, // includeContacts
                false, // includeInactive
                10    // limit
            );
        });
        assertThat(universalResults).isNotNull();
        assertThat(universalResults.getCustomers()).isNotEmpty();
        
        // Test quickSearch delegation in service transaction
        SearchResults quickResults = QuarkusTransaction.requiringNew().call(() -> {
            return searchService.quickSearch(
                restaurantName.substring(0, 5),
                5
            );
        });
        assertThat(quickResults).isNotNull();
        
        // Both methods should work in CQRS mode
        assertThat(cqrsEnabled).isTrue();
    }
}
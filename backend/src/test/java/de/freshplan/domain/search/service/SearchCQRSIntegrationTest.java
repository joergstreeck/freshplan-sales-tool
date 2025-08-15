package de.freshplan.domain.search.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.CustomerType;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.search.service.dto.SearchResult;
import de.freshplan.domain.search.service.dto.SearchResults;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    @Inject
    SearchService searchService; // Test via Facade to verify Feature Flag switching
    
    @Inject
    CustomerRepository customerRepository;
    
    @ConfigProperty(name = "features.cqrs.enabled")
    boolean cqrsEnabled;
    
    private Customer testCustomer1;
    private Customer testCustomer2;
    
    @BeforeEach
    @TestTransaction
    void setUp() {
        // Create test data for searching
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        
        // Customer 1 - Hotel company
        testCustomer1 = new Customer();
        testCustomer1.setCustomerNumber("KD-S1-" + uniqueSuffix.substring(7));
        testCustomer1.setCompanyName("Hotel Search Solutions " + uniqueSuffix);
        testCustomer1.setCustomerType(CustomerType.UNTERNEHMEN);
        testCustomer1.setStatus(CustomerStatus.AKTIV);
        testCustomer1.setIndustry(Industry.HOTEL);
        testCustomer1.setExpectedAnnualVolume(new BigDecimal("250000"));
        testCustomer1.setCreatedBy("testuser");
        testCustomer1.setCreatedAt(LocalDateTime.now());
        customerRepository.persist(testCustomer1);
        
        // Customer 2 - Restaurant company
        testCustomer2 = new Customer();
        testCustomer2.setCustomerNumber("KD-S2-" + uniqueSuffix.substring(7));
        testCustomer2.setCompanyName("Gourmet Restaurant " + uniqueSuffix);
        testCustomer2.setCustomerType(CustomerType.NEUKUNDE);
        testCustomer2.setStatus(CustomerStatus.LEAD);
        testCustomer2.setIndustry(Industry.RESTAURANT);
        testCustomer2.setExpectedAnnualVolume(new BigDecimal("150000"));
        testCustomer2.setCreatedBy("testuser");
        testCustomer2.setCreatedAt(LocalDateTime.now());
        customerRepository.persist(testCustomer2);
        
        customerRepository.flush();
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
        // When - Search by partial company name (using part of the actual test customer name)
        SearchResults results = searchService.universalSearch(
            "Hotel Search Solutions",
            false, // includeContacts
            false, // includeInactive  
            10    // limit
        );
        
        // Then - Should find the matching customer
        assertThat(results).isNotNull();
        assertThat(results.getCustomers()).isNotEmpty();
        assertThat(results.getCustomers())
            .anyMatch(result -> 
                result.getType().equals("customer") &&
                result.getId().equals(testCustomer1.getId().toString())
            );
    }
    
    @Test
    @DisplayName("Universal search should find customers by customer number")
    void universalSearch_byCustomerNumber_shouldFindExactMatch() {
        // When - Search by customer number
        SearchResults results = searchService.universalSearch(
            testCustomer1.getCustomerNumber(),
            false, // includeContacts
            false, // includeInactive
            10    // limit
        );
        
        // Then - Should find exact match with high relevance
        assertThat(results).isNotNull();
        assertThat(results.getCustomers()).isNotEmpty();
        
        SearchResult firstResult = results.getCustomers().get(0);
        assertThat(firstResult.getId()).isEqualTo(testCustomer1.getId().toString());
        assertThat(firstResult.getRelevanceScore()).isGreaterThanOrEqualTo(100); // Exact match bonus
    }
    
    @Test
    @TestTransaction
    @DisplayName("Universal search should respect includeInactive flag")
    void universalSearch_withInactiveFlag_shouldFilterResults() {
        // Given - Make one customer inactive
        testCustomer2.setStatus(CustomerStatus.INAKTIV);
        testCustomer2 = customerRepository.getEntityManager().merge(testCustomer2);
        customerRepository.flush();
        
        // When - Search without inactive
        SearchResults resultsWithoutInactive = searchService.universalSearch(
            "Restaurant",
            false, // includeContacts
            false, // includeInactive
            10    // limit
        );
        
        // When - Search with inactive
        SearchResults resultsWithInactive = searchService.universalSearch(
            "Restaurant",
            false, // includeContacts
            true,  // includeInactive
            10    // limit
        );
        
        // Then - Results should differ
        assertThat(resultsWithoutInactive.getCustomers()).isEmpty();
        assertThat(resultsWithInactive.getCustomers()).isNotEmpty();
    }
    
    @Test
    @DisplayName("Universal search should return empty results for no matches")
    void universalSearch_noMatches_shouldReturnEmptyResults() {
        // When - Search for non-existent term
        SearchResults results = searchService.universalSearch(
            "NonExistentCompanyXYZ123",
            false, // includeContacts
            false, // includeInactive
            10    // limit
        );
        
        // Then - Should return empty but valid results
        assertThat(results).isNotNull();
        assertThat(results.getCustomers()).isEmpty();
        assertThat(results.getTotalCount()).isEqualTo(0);
    }
    
    // =====================================
    // QUICK SEARCH TESTS  
    // =====================================
    
    @Test
    @TestTransaction
    @DisplayName("Quick search should return limited results for autocomplete")
    void quickSearch_shouldReturnLimitedResults() {
        // Given - Create additional test customers
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        for (int i = 0; i < 10; i++) {
            Customer c = new Customer();
            c.setCustomerNumber("KD-QK-" + uniqueSuffix.substring(7) + "-" + i);
            c.setCompanyName("Quick Test Company " + i);
            c.setCustomerType(CustomerType.UNTERNEHMEN);
            c.setStatus(CustomerStatus.AKTIV);
            c.setIndustry(Industry.SONSTIGE);
            c.setCreatedBy("testuser");
            c.setCreatedAt(LocalDateTime.now());
            customerRepository.persist(c);
        }
        customerRepository.flush();
        
        // When - Quick search with limit
        SearchResults results = searchService.quickSearch("Quick", 5);
        
        // Then - Should respect limit
        assertThat(results).isNotNull();
        assertThat(results.getCustomers()).hasSize(5);
        assertThat(results.getExecutionTime()).isLessThan(50L); // Quick search should be fast
    }
    
    @Test
    @TestTransaction
    @DisplayName("Quick search should prioritize active customers")
    void quickSearch_shouldPrioritizeActiveCustomers() {
        // Given - Mix of active and inactive customers
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        Customer activeCustomer = new Customer();
        activeCustomer.setCustomerNumber("KD-ACT-" + uniqueSuffix.substring(7));
        activeCustomer.setCompanyName("Priority Test Active");
        activeCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
        activeCustomer.setStatus(CustomerStatus.AKTIV);
        activeCustomer.setIndustry(Industry.HOTEL);
        activeCustomer.setLastContactDate(LocalDateTime.now().minusDays(5)); // Recent contact
        activeCustomer.setCreatedBy("testuser");
        activeCustomer.setCreatedAt(LocalDateTime.now());
        customerRepository.persist(activeCustomer);
        
        Customer inactiveCustomer = new Customer();
        inactiveCustomer.setCustomerNumber("KD-INA-" + uniqueSuffix.substring(7));
        inactiveCustomer.setCompanyName("Priority Test Inactive");
        inactiveCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
        inactiveCustomer.setStatus(CustomerStatus.INAKTIV);
        inactiveCustomer.setIndustry(Industry.HOTEL);
        inactiveCustomer.setCreatedBy("testuser");
        inactiveCustomer.setCreatedAt(LocalDateTime.now());
        customerRepository.persist(inactiveCustomer);
        
        customerRepository.flush();
        
        // When - Quick search
        SearchResults results = searchService.quickSearch("Priority", 10);
        
        // Then - Both customers should be found, active should have higher score
        assertThat(results.getCustomers()).hasSizeGreaterThanOrEqualTo(2);
        
        // Find our test customers in the results
        Optional<SearchResult> activeResult = results.getCustomers().stream()
            .filter(r -> r.getId().equals(activeCustomer.getId().toString()))
            .findFirst();
        Optional<SearchResult> inactiveResult = results.getCustomers().stream()
            .filter(r -> r.getId().equals(inactiveCustomer.getId().toString()))
            .findFirst();
            
        assertThat(activeResult).isPresent();
        assertThat(inactiveResult).isPresent();
        
        // Active should have higher relevance score
        assertThat(activeResult.get().getRelevanceScore())
            .isGreaterThan(inactiveResult.get().getRelevanceScore());
    }
    
    // =====================================
    // RELEVANCE SCORING TESTS
    // =====================================
    
    @Test
    @TestTransaction
    @DisplayName("Search should apply relevance scoring correctly")
    void search_shouldApplyRelevanceScoring() {
        // Given - Customers with different relevance factors
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        Customer exactMatch = new Customer();
        exactMatch.setCustomerNumber("KD-EX-" + uniqueSuffix.substring(7));
        exactMatch.setCompanyName("Relevance Test");
        exactMatch.setCustomerType(CustomerType.UNTERNEHMEN);
        exactMatch.setStatus(CustomerStatus.AKTIV);
        exactMatch.setIndustry(Industry.HOTEL);
        exactMatch.setCreatedBy("testuser");
        exactMatch.setCreatedAt(LocalDateTime.now());
        customerRepository.persist(exactMatch);
        
        Customer partialMatch = new Customer();
        partialMatch.setCustomerNumber("KD-PT-" + uniqueSuffix.substring(7));
        partialMatch.setCompanyName("Some Relevance Test Company");
        partialMatch.setCustomerType(CustomerType.UNTERNEHMEN);
        partialMatch.setStatus(CustomerStatus.LEAD);
        partialMatch.setIndustry(Industry.HOTEL);
        partialMatch.setCreatedBy("testuser");
        partialMatch.setCreatedAt(LocalDateTime.now());
        customerRepository.persist(partialMatch);
        
        customerRepository.flush();
        
        // When - Search for "Relevance Test"
        SearchResults results = searchService.universalSearch(
            "Relevance Test",
            false, // includeContacts
            false, // includeInactive
            10    // limit
        );
        
        // Then - Both should be found, exact match should have higher score
        assertThat(results.getCustomers()).hasSizeGreaterThanOrEqualTo(2);
        
        // Find our test customers in the results
        Optional<SearchResult> exactResult = results.getCustomers().stream()
            .filter(r -> r.getId().equals(exactMatch.getId().toString()))
            .findFirst();
        Optional<SearchResult> partialResult = results.getCustomers().stream()
            .filter(r -> r.getId().equals(partialMatch.getId().toString()))
            .findFirst();
            
        assertThat(exactResult).isPresent();
        assertThat(partialResult).isPresent();
        
        // Exact match should have higher relevance score
        assertThat(exactResult.get().getRelevanceScore())
            .isGreaterThan(partialResult.get().getRelevanceScore());
    }
    
    // =====================================
    // PERFORMANCE TESTS
    // =====================================
    
    @Test
    @TestTransaction
    @DisplayName("Search should handle large datasets efficiently")
    void search_withLargeDataset_shouldPerformWell() {
        // Given - Create many test customers
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        for (int i = 0; i < 100; i++) {
            Customer c = new Customer();
            c.setCustomerNumber("KD-PF-" + uniqueSuffix.substring(8) + "-" + i);
            c.setCompanyName("Performance Test Company " + i);
            c.setCustomerType(CustomerType.UNTERNEHMEN);
            c.setStatus(i % 3 == 0 ? CustomerStatus.AKTIV : CustomerStatus.LEAD);
            c.setIndustry(Industry.values()[i % Industry.values().length]);
            c.setExpectedAnnualVolume(new BigDecimal(100000 + i * 1000));
            c.setCreatedBy("testuser");
            c.setCreatedAt(LocalDateTime.now());
            customerRepository.persist(c);
        }
        customerRepository.flush();
        
        // When - Perform search
        long startTime = System.currentTimeMillis();
        SearchResults results = searchService.universalSearch(
            "Performance Test",
            false, // includeContacts
            false, // includeInactive
            20    // limit
        );
        long executionTime = System.currentTimeMillis() - startTime;
        
        // Then - Should complete quickly
        assertThat(results).isNotNull();
        assertThat(results.getCustomers()).hasSize(20); // Should respect limit
        assertThat(executionTime).isLessThan(500L); // Should complete within 500ms
        assertThat(results.getExecutionTime()).isLessThan(500L);
    }
    
    // =====================================
    // CQRS BEHAVIOR VERIFICATION
    // =====================================
    
    @Test
    @DisplayName("CQRS mode should properly delegate search operations")
    void cqrsMode_shouldProperlyDelegateOperations() {
        // This test verifies that the facade properly delegates to QueryService
        
        // Test universalSearch delegation
        SearchResults universalResults = searchService.universalSearch(
            testCustomer1.getCompanyName(),
            false, // includeContacts
            false, // includeInactive
            10    // limit
        );
        assertThat(universalResults).isNotNull();
        assertThat(universalResults.getCustomers()).isNotEmpty();
        
        // Test quickSearch delegation
        SearchResults quickResults = searchService.quickSearch(
            testCustomer2.getCompanyName().substring(0, 5),
            5
        );
        assertThat(quickResults).isNotNull();
        
        // Both methods should work in CQRS mode
        assertThat(cqrsEnabled).isTrue();
    }
}
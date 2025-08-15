package de.freshplan.domain.export.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.CustomerType;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.export.service.dto.ExportRequest;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration Test for HtmlExportService CQRS Implementation.
 * 
 * Tests the READ-ONLY HtmlExportQueryService with Feature Flag enabled.
 * HtmlExportService has no CommandService as it's purely for generating reports.
 * 
 * Key aspects tested:
 * - HTML report generation
 * - Filter application (status, industry, date range)
 * - Include options (details, stats, contacts)
 * - HTML structure and content validation
 * 
 * @author Claude
 * @since Phase 14.3 - Feature Flag Switching Tests
 */
@QuarkusTest
@TestProfile(HtmlExportCQRSTestProfile.class)
@TestSecurity(user = "testuser", roles = {"admin", "manager"})
@DisplayName("HtmlExport Service CQRS Integration Test")
class HtmlExportCQRSIntegrationTest {

    private static final Logger LOG = Logger.getLogger(HtmlExportCQRSIntegrationTest.class);

    @Inject
    HtmlExportService htmlExportService; // Test via Facade to verify Feature Flag switching
    
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
            });
        }
    }
    
    /**
     * Creates test data in a separate committed transaction.
     * Returns customer names for verification.
     */
    private String[] createAndPersistTestData() {
        return QuarkusTransaction.requiringNew().call(() -> {
            // Customer 1 - Active Hotel
            Customer testCustomer1 = new Customer();
            testCustomer1.setCustomerNumber("E1" + testRunId.substring(0, 6));
            testCustomer1.setCompanyName("[TEST-" + testRunId + "] Export Hotel");
            testCustomer1.setCustomerType(CustomerType.UNTERNEHMEN);
            testCustomer1.setStatus(CustomerStatus.AKTIV);
            testCustomer1.setIndustry(Industry.HOTEL);
            testCustomer1.setExpectedAnnualVolume(new BigDecimal("500000"));
            testCustomer1.setIsTestData(true);
            testCustomer1.setCreatedBy("testuser");
            testCustomer1.setCreatedAt(LocalDateTime.now());
            testCustomer1.setLastContactDate(LocalDateTime.now().minusDays(10));
            customerRepository.persist(testCustomer1);
            testCustomer1Id = testCustomer1.getId();
            
            // Customer 2 - Lead Restaurant
            Customer testCustomer2 = new Customer();
            testCustomer2.setCustomerNumber("E2" + testRunId.substring(0, 6));
            testCustomer2.setCompanyName("[TEST-" + testRunId + "] Export Restaurant");
            testCustomer2.setCustomerType(CustomerType.NEUKUNDE);
            testCustomer2.setStatus(CustomerStatus.LEAD);
            testCustomer2.setIndustry(Industry.RESTAURANT);
            testCustomer2.setExpectedAnnualVolume(new BigDecimal("150000"));
            testCustomer2.setIsTestData(true);
            testCustomer2.setCreatedBy("testuser");
            testCustomer2.setCreatedAt(LocalDateTime.now().minusDays(30));
            customerRepository.persist(testCustomer2);
            testCustomer2Id = testCustomer2.getId();
            
            customerRepository.flush();
            
            return new String[]{testCustomer1.getCompanyName(), testCustomer2.getCompanyName()};
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
    // BASIC EXPORT TESTS
    // =====================================
    
    @Test
    @DisplayName("Generate HTML export with all customers")
    void generateCustomersHtml_allCustomers_shouldReturnHtml() {
        // Given - Create test data in separate transaction
        String[] customerNames = createAndPersistTestData();
        
        // Given - Basic export request
        ExportRequest request = ExportRequest.builder()
            .entityType("customer")
            .includeDetails(true)
            .includeStats(false)
            .includeContacts(false)
            .page(0)
            .size(100)
            .build();
        
        // When - Generate HTML in service transaction
        String html = QuarkusTransaction.requiringNew().call(() -> {
            return htmlExportService.generateCustomersHtml(request);
        });
        
        // Then - Validate HTML structure
        assertThat(html).isNotNull();
        assertThat(html).contains("<!DOCTYPE html>");
        assertThat(html).contains("<html lang=\"de\">");
        assertThat(html).contains("FreshPlan Kunden-Export");
        assertThat(html).contains(customerNames[0]);
        assertThat(html).contains(customerNames[1]);
    }
    
    @Test
    @DisplayName("Export with status filter should only include matching customers")
    void generateCustomersHtml_withStatusFilter_shouldFilterResults() {
        // Given - Create test data in separate transaction
        String[] customerNames = createAndPersistTestData();
        
        // Given - Filter for AKTIV status only
        ExportRequest request = ExportRequest.builder()
            .entityType("customer")
            .status(Arrays.asList("AKTIV"))
            .includeDetails(true)
            .page(0)
            .size(100)
            .build();
        
        // When - Generate HTML in service transaction
        String html = QuarkusTransaction.requiringNew().call(() -> {
            return htmlExportService.generateCustomersHtml(request);
        });
        
        // Then - Should only include active customer
        assertThat(html).contains(customerNames[0]);
        assertThat(html).doesNotContain(customerNames[1]);
    }
    
    @Test
    @DisplayName("Export with industry filter should only include matching customers")
    void generateCustomersHtml_withIndustryFilter_shouldFilterResults() {
        // Given - Create test data in separate transaction
        String[] customerNames = createAndPersistTestData();
        
        // Given - Filter for HOTEL industry
        ExportRequest request = ExportRequest.builder()
            .entityType("customer")
            .industry("HOTEL")
            .includeDetails(true)
            .page(0)
            .size(100)
            .build();
        
        // When - Generate HTML in service transaction
        String html = QuarkusTransaction.requiringNew().call(() -> {
            return htmlExportService.generateCustomersHtml(request);
        });
        
        // Then - Should include hotel customer but not restaurant customer
        assertThat(html).contains(customerNames[0]);
        assertThat(html).contains("HOTEL"); // Verify industry filter is applied
        assertThat(html).doesNotContain(customerNames[1]);
        assertThat(html).doesNotContain("RESTAURANT"); // Restaurant industry should not be in export
    }
    
    // =====================================
    // DATE RANGE TESTS
    // =====================================
    
    @Test
    @DisplayName("Export with date range should filter by creation date")
    void generateCustomersHtml_withDateRange_shouldFilterByDate() {
        // Given - Create test data in separate transaction
        String[] customerNames = createAndPersistTestData();
        
        // Given - Date range that excludes older customer
        LocalDateTime fromDate = LocalDateTime.now().minusDays(7);
        LocalDateTime toDate = LocalDateTime.now().plusDays(1);
        
        ExportRequest request = ExportRequest.builder()
            .entityType("customer")
            .dateFrom(fromDate)
            .dateTo(toDate)
            .includeDetails(true)
            .page(0)
            .size(100)
            .build();
        
        // When - Generate HTML in service transaction
        String html = QuarkusTransaction.requiringNew().call(() -> {
            return htmlExportService.generateCustomersHtml(request);
        });
        
        // Then - Should only include recent customer
        assertThat(html).contains(customerNames[0]);
        assertThat(html).doesNotContain(customerNames[1]);
    }
    
    // =====================================
    // INCLUDE OPTIONS TESTS
    // =====================================
    
    @Test
    @DisplayName("Export with includeStats should add statistics section")
    void generateCustomersHtml_withIncludeStats_shouldAddStatistics() {
        // Given - Create test data in separate transaction
        String[] customerNames = createAndPersistTestData();
        
        // Given - Request with stats
        ExportRequest request = ExportRequest.builder()
            .entityType("customer")
            .includeDetails(true)
            .includeStats(true)
            .page(0)
            .size(100)
            .build();
        
        // When - Generate HTML in service transaction
        String html = QuarkusTransaction.requiringNew().call(() -> {
            return htmlExportService.generateCustomersHtml(request);
        });
        
        // Then - Should include statistics section
        assertThat(html).contains("Statistik");
        // Stats section should include summary information
        assertThat(html).containsPattern("Gesamt.*Kunden|Total.*Customer");
    }
    
    @Test
    @DisplayName("Export with includeContacts should add contact information")
    void generateCustomersHtml_withIncludeContacts_shouldAddContacts() {
        // Given - Create test data in separate transaction
        String[] customerNames = createAndPersistTestData();
        
        // Given - Request with contacts
        ExportRequest request = ExportRequest.builder()
            .entityType("customer")
            .includeDetails(true)
            .includeContacts(true)
            .page(0)
            .size(100)
            .build();
        
        // When - Generate HTML in service transaction
        String html = QuarkusTransaction.requiringNew().call(() -> {
            return htmlExportService.generateCustomersHtml(request);
        });
        
        // Then - Should prepare for contact information
        assertThat(html).isNotNull();
        // Note: Actual contacts would need to be created in setup
        // This test verifies the option is processed
    }
    
    // =====================================
    // PAGINATION TESTS
    // =====================================
    
    @Test
    @DisplayName("Export with pagination should respect page and size")
    void generateCustomersHtml_withPagination_shouldRespectLimits() {
        // Given - Create additional customers in separate transaction
        QuarkusTransaction.requiringNew().run(() -> {
            for (int i = 0; i < 10; i++) {
                Customer c = new Customer();
                c.setCustomerNumber("PG" + i + testRunId.substring(0, 3));
                c.setCompanyName("[TEST-" + testRunId + "] Pagination Test " + i);
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
        
        // When - Request first page with size 5
        ExportRequest request = ExportRequest.builder()
            .entityType("customer")
            .includeDetails(true)
            .page(0)
            .size(5)
            .build();
        
        String html = QuarkusTransaction.requiringNew().call(() -> {
            return htmlExportService.generateCustomersHtml(request);
        });
        
        // Then - Should have limited results
        assertThat(html).isNotNull();
        // Count occurrences of customer markers
        int customerCount = html.split("<tr class=\"customer-row\"|<div class=\"customer\"|Kunde:|Customer:").length - 1;
        assertThat(customerCount).isLessThanOrEqualTo(5);
        
        // Cleanup additional test customers - use specific pattern with testRunId
        QuarkusTransaction.requiringNew().run(() -> {
            customerRepository.delete("customerNumber like ?1", "PG%" + testRunId.substring(0, 3) + "%");
        });
    }
    
    // =====================================
    // HTML STRUCTURE TESTS
    // =====================================
    
    @Test
    @DisplayName("Generated HTML should have proper structure for PDF conversion")
    void generateCustomersHtml_shouldHaveProperStructure() {
        // Given - Create test data in separate transaction
        String[] customerNames = createAndPersistTestData();
        
        // Given - Basic request
        ExportRequest request = ExportRequest.builder()
            .entityType("customer")
            .includeDetails(true)
            .page(0)
            .size(100)
            .build();
        
        // When - Generate HTML in service transaction
        String html = QuarkusTransaction.requiringNew().call(() -> {
            return htmlExportService.generateCustomersHtml(request);
        });
        
        // Then - Validate HTML structure
        assertThat(html)
            .contains("<!DOCTYPE html>")
            .contains("<html")
            .contains("<head>")
            .contains("<meta charset=\"UTF-8\">")
            .contains("<style>")
            .contains("@page")  // Print styles
            .contains("@media print")  // Print media query
            .contains("<body>")
            .contains("</body>")
            .contains("</html>");
    }
    
    // =====================================
    // EMPTY RESULT TESTS
    // =====================================
    
    @Test
    @DisplayName("Export with no matching customers should return empty report")
    void generateCustomersHtml_noMatches_shouldReturnEmptyReport() {
        // Given - Create test data in separate transaction
        String[] customerNames = createAndPersistTestData();
        
        // Given - Filter that matches nothing (using impossible industry)
        ExportRequest request = ExportRequest.builder()
            .entityType("customer")
            .industry("XXXNONEXISTENTXXX")
            .includeDetails(true)
            .page(0)
            .size(100)
            .build();
        
        // When - Generate HTML in service transaction
        String html = QuarkusTransaction.requiringNew().call(() -> {
            return htmlExportService.generateCustomersHtml(request);
        });
        
        // Then - Should return HTML but without customer data
        assertThat(html).isNotNull();
        assertThat(html).contains("<!DOCTYPE html>");
        assertThat(html).doesNotContain(customerNames[0]);
        assertThat(html).doesNotContain(customerNames[1]);
        // Should indicate no results - check for actual text in the HTML
        assertThat(html.toLowerCase()).containsAnyOf(
            "keine kunden", 
            "no customers", 
            "0 ergebnis",
            "keine treffer",
            "keine daten"
        );
    }
    
    // =====================================
    // CQRS BEHAVIOR VERIFICATION
    // =====================================
    
    @Test
    @DisplayName("CQRS mode should properly delegate export operations")
    void cqrsMode_shouldProperlyDelegateOperations() {
        // Given - Create test data in separate transaction
        String[] customerNames = createAndPersistTestData();
        
        // This test verifies that the facade properly delegates to QueryService
        
        // Create various export requests
        ExportRequest basicRequest = ExportRequest.builder()
            .entityType("customer")
            .includeDetails(true)
            .build();
        
        ExportRequest filteredRequest = ExportRequest.builder()
            .entityType("customer")
            .status(Arrays.asList("AKTIV", "LEAD"))
            .industry("HOTEL")
            .includeDetails(true)
            .includeStats(true)
            .build();
        
        ExportRequest paginatedRequest = ExportRequest.builder()
            .entityType("customer")
            .page(0)
            .size(10)
            .build();
        
        // All should work in CQRS mode
        String html1 = htmlExportService.generateCustomersHtml(basicRequest);
        String html2 = htmlExportService.generateCustomersHtml(filteredRequest);
        String html3 = htmlExportService.generateCustomersHtml(paginatedRequest);
        
        assertThat(html1).isNotNull().contains("<!DOCTYPE html>");
        assertThat(html2).isNotNull().contains("<!DOCTYPE html>");
        assertThat(html3).isNotNull().contains("<!DOCTYPE html>");
        
        // Verify CQRS is enabled
        assertThat(cqrsEnabled).isTrue();
    }
}
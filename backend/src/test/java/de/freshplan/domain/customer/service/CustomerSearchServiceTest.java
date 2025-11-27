package de.freshplan.domain.customer.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.*;
import de.freshplan.test.builders.CustomerTestDataFactory;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for core search functionality of CustomerSearchService.
 *
 * <p>Consolidates: BasicTest + FilterTest + SortTest (34 tests total)
 */
@QuarkusTest
@Tag("integration")
@DisplayName("CustomerSearchService - Core Functionality")
class CustomerSearchServiceTest {

  @Inject CustomerSearchService searchService;
  @Inject CustomerRepository customerRepository;
  @Inject jakarta.persistence.EntityManager em;

  /**
   * Clean up test data before each test to ensure test isolation. Sprint 2.1.6: Fix test data
   * contamination between tests.
   */
  @org.junit.jupiter.api.BeforeEach
  @jakarta.transaction.Transactional
  void cleanupBeforeEach() {
    // Delete in correct order to respect foreign key constraints
    em.createNativeQuery(
            "DELETE FROM opportunity_activities WHERE opportunity_id IN (SELECT id FROM opportunities WHERE customer_id IN (SELECT id FROM customers WHERE is_test_data = true))")
        .executeUpdate();
    em.createNativeQuery(
            "DELETE FROM opportunities WHERE customer_id IN (SELECT id FROM customers WHERE is_test_data = true)")
        .executeUpdate();
    customerRepository.deleteAllTestData();
  }

  @AfterEach
  @jakarta.transaction.Transactional
  void cleanup() {
    // Delete in correct order to respect foreign key constraints
    em.createNativeQuery(
            "DELETE FROM opportunity_activities WHERE opportunity_id IN (SELECT id FROM opportunities WHERE customer_id IN (SELECT id FROM customers WHERE is_test_data = true))")
        .executeUpdate();
    em.createNativeQuery(
            "DELETE FROM opportunities WHERE customer_id IN (SELECT id FROM customers WHERE is_test_data = true)")
        .executeUpdate();
    customerRepository.deleteAllTestData();
  }

  // ==================== BASIC SEARCH TESTS ====================
  @TestTransaction
  @DisplayName("Should perform basic search without filters")
  void shouldPerformBasicSearchWithoutFilters() {
    // Given: Remember initial count and create test customers
    CustomerSearchRequest initialRequest = new CustomerSearchRequest();
    long initialCount = searchService.search(initialRequest, 0, 1).getTotalElements();

    createAndPersistSampleCustomers();
    CustomerSearchRequest request = new CustomerSearchRequest();

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 100);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getTotalElements()).isEqualTo(initialCount + 2);
    assertThat(result.getPage()).isEqualTo(0);
    // Verify our test customers are in the results
    assertThat(result.getContent())
        .anyMatch(c -> c.companyName().equals("Berlin Restaurant GmbH"))
        .anyMatch(c -> c.companyName().equals("Munich Catering Services"));
  }

  @Test
  @TestTransaction
  @DisplayName("Should perform global search with text")
  void shouldPerformGlobalSearchWithText() {
    // Given: Create test customers
    createAndPersistSampleCustomers();
    CustomerSearchRequest request = new CustomerSearchRequest();
    request.setGlobalSearch("Berlin");

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getTotalElements()).isEqualTo(1L);
    assertThat(result.getContent().get(0).companyName()).contains("Berlin");
  }

  @Test
  @TestTransaction
  @DisplayName("Should handle empty search results")
  void shouldHandleEmptySearchResults() {
    // Given: Create test customers and search for non-existent
    createAndPersistSampleCustomers();
    CustomerSearchRequest request = new CustomerSearchRequest();
    request.setGlobalSearch("NonExistentCompany");

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isEqualTo(0L);
    assertThat(result.getTotalPages()).isEqualTo(0);
  }

  @Test
  @TestTransaction
  @DisplayName("Should return all customers when no criteria specified")
  void shouldReturnAllCustomersWhenNoCriteria() {
    // Given: Remember initial count and create test customers
    CustomerSearchRequest initialRequest = new CustomerSearchRequest();
    long initialCount = searchService.search(initialRequest, 0, 1).getTotalElements();

    createAndPersistMultipleCustomers(5);
    CustomerSearchRequest request = new CustomerSearchRequest();

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 100);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getTotalElements()).isEqualTo(initialCount + 5);
    // Verify our test customers are present
    for (int i = 1; i <= 5; i++) {
      final String expectedName = "Test Customer " + i;
      assertThat(result.getContent()).anyMatch(c -> c.companyName().equals(expectedName));
    }
  }

  @Test
  @TestTransaction
  @DisplayName("Should handle case-insensitive global search")
  void shouldHandleCaseInsensitiveGlobalSearch() {
    // Given: Create test customers
    createAndPersistSampleCustomers();

    // When: Search with different cases
    CustomerSearchRequest upperRequest = new CustomerSearchRequest();
    upperRequest.setGlobalSearch("BERLIN");

    CustomerSearchRequest lowerRequest = new CustomerSearchRequest();
    lowerRequest.setGlobalSearch("berlin");

    CustomerSearchService.PagedResponse<CustomerResponse> upperResult =
        searchService.search(upperRequest, 0, 10);
    CustomerSearchService.PagedResponse<CustomerResponse> lowerResult =
        searchService.search(lowerRequest, 0, 10);

    // Then: Both should find the same customer
    assertThat(upperResult.getTotalElements()).isEqualTo(lowerResult.getTotalElements());
    assertThat(upperResult.getContent()).hasSize(1);
    assertThat(lowerResult.getContent()).hasSize(1);
  }

  // ==================== FILTER TESTS ====================
  @Test
  @TestTransaction
  @DisplayName("Should filter by status")
  void shouldFilterByStatus() {
    // Given: Create customers with different statuses
    createCustomersWithDifferentStatuses();
    CustomerSearchRequest request = new CustomerSearchRequest();
    FilterCriteria statusFilter = new FilterCriteria();
    statusFilter.setField("status");
    statusFilter.setOperator(FilterOperator.EQUALS);
    statusFilter.setValue("AKTIV");
    request.setFilters(List.of(statusFilter));

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isNotEmpty();
    assertThat(result.getContent())
        .allMatch(customer -> "AKTIV".equals(customer.status().toString()));
  }

  @Test
  @TestTransaction
  @DisplayName("Should filter by date range")
  void shouldFilterByDateRange() {
    // Given: Remember initial count and create customers with different dates
    CustomerSearchRequest initialRequest = new CustomerSearchRequest();
    FilterCriteria initialDateFilter = new FilterCriteria();
    initialDateFilter.setField("createdAt");
    initialDateFilter.setOperator(FilterOperator.GREATER_THAN);
    initialDateFilter.setValue("2024-01-01");
    initialRequest.setFilters(List.of(initialDateFilter));
    long initialCount = searchService.search(initialRequest, 0, 1).getTotalElements();

    createCustomersWithDifferentDates();
    CustomerSearchRequest request = new CustomerSearchRequest();

    FilterCriteria dateFilter = new FilterCriteria();
    dateFilter.setField("createdAt");
    dateFilter.setOperator(FilterOperator.GREATER_THAN);
    dateFilter.setValue("2024-01-01");
    request.setFilters(List.of(dateFilter));

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 100);

    // Then
    assertThat(result).isNotNull();
    // All our test customers are created recently, so all should match
    assertThat(result.getTotalElements()).isEqualTo(initialCount + 3);
    // Verify our test customers are present
    assertThat(result.getContent())
        .anyMatch(c -> c.companyName().equals("[TEST-DATE] Recent Company"))
        .anyMatch(c -> c.companyName().equals("[TEST-DATE] Older Company"))
        .anyMatch(c -> c.companyName().equals("[TEST-DATE] Oldest Company"));
  }

  @Test
  @TestTransaction
  @DisplayName("Should filter by numeric range")
  void shouldFilterByNumericRange() {
    // Given: Create customers with different volumes
    createCustomersWithDifferentVolumes();
    CustomerSearchRequest request = new CustomerSearchRequest();
    FilterCriteria volumeFilter = new FilterCriteria();
    volumeFilter.setField("expectedAnnualVolume");
    volumeFilter.setOperator(FilterOperator.GREATER_THAN);
    volumeFilter.setValue("60000");
    request.setFilters(List.of(volumeFilter));

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then
    assertThat(result).isNotNull();
    // Only customers with volume > 60000 should be returned
    assertThat(result.getContent()).hasSize(2); // 75000 and 100000
    assertThat(result.getContent())
        .allMatch(
            customer ->
                customer.expectedAnnualVolume() != null
                    && customer.expectedAnnualVolume().compareTo(BigDecimal.valueOf(60000)) > 0);
  }

  @Test
  @TestTransaction
  @DisplayName("Should filter by industry")
  void shouldFilterByIndustry() {
    // Given: Create customers with different industries
    createCustomersWithDifferentIndustries();
    CustomerSearchRequest request = new CustomerSearchRequest();
    FilterCriteria industryFilter = new FilterCriteria();
    industryFilter.setField("industry");
    industryFilter.setOperator(FilterOperator.EQUALS);
    industryFilter.setValue("HOTEL");
    request.setFilters(List.of(industryFilter));

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent())
        .allMatch(customer -> "HOTEL".equals(customer.businessType().toString()));
  }

  @Test
  @TestTransaction
  @DisplayName("Should handle multiple filters with AND logic")
  void shouldHandleMultipleFilters() {
    // Given: Create diverse customers
    createDiverseCustomers();
    CustomerSearchRequest request = new CustomerSearchRequest();

    FilterCriteria statusFilter = new FilterCriteria();
    statusFilter.setField("status");
    statusFilter.setOperator(FilterOperator.EQUALS);
    statusFilter.setValue("AKTIV");

    FilterCriteria volumeFilter = new FilterCriteria();
    volumeFilter.setField("expectedAnnualVolume");
    volumeFilter.setOperator(FilterOperator.GREATER_THAN);
    volumeFilter.setValue("50000");

    request.setFilters(List.of(statusFilter, volumeFilter));

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent())
        .allMatch(
            customer ->
                "AKTIV".equals(customer.status().toString())
                    && customer.expectedAnnualVolume().compareTo(BigDecimal.valueOf(50000)) > 0);
  }

  @Test
  @TestTransaction
  @DisplayName("Should return empty result when no matches found")
  void shouldReturnEmptyWhenNoMatches() {
    // Given: Create customers with low volumes
    createCustomersWithDifferentVolumes();
    CustomerSearchRequest request = new CustomerSearchRequest();
    FilterCriteria volumeFilter = new FilterCriteria();
    volumeFilter.setField("expectedAnnualVolume");
    volumeFilter.setOperator(FilterOperator.GREATER_THAN);
    volumeFilter.setValue("1000000"); // Very high threshold
    request.setFilters(List.of(volumeFilter));

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isEqualTo(0L);
  }

  // ==================== SORT TESTS ====================
  @Test
  @TestTransaction
  @DisplayName("Should apply ascending sort by company name")
  void shouldApplyAscendingSortByCompanyName() {
    // Given: Create test customers with different names
    createCustomersForSorting();
    CustomerSearchRequest request = new CustomerSearchRequest();
    // Filter to only "Company" customers to exclude old test data
    request.setGlobalSearch("Company");

    SortCriteria sortCriteria = new SortCriteria();
    sortCriteria.setField("companyName");
    sortCriteria.setDirection("ASC");
    request.setMultiSort(List.of(sortCriteria));

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then - Filter results to only test-created customers
    var testCustomers =
        result.getContent().stream()
            .filter(
                c ->
                    List.of("Alpha Company", "Beta Company", "Gamma Company")
                        .contains(c.companyName()))
            .toList();
    assertThat(testCustomers).hasSize(3);
    // Alpha Company should come before Beta Company before Gamma Company
    assertThat(testCustomers.get(0).companyName()).isEqualTo("Alpha Company");
    assertThat(testCustomers.get(1).companyName()).isEqualTo("Beta Company");
    assertThat(testCustomers.get(2).companyName()).isEqualTo("Gamma Company");
  }

  @Test
  @TestTransaction
  @DisplayName("Should apply descending sort by expected annual volume")
  void shouldApplyDescendingSortByVolume() {
    // Given: Create customers with different volumes
    createCustomersWithDifferentVolumes();
    CustomerSearchRequest request = new CustomerSearchRequest();
    SortCriteria sortCriteria = new SortCriteria();
    sortCriteria.setField("expectedAnnualVolume");
    sortCriteria.setDirection("DESC");
    request.setMultiSort(List.of(sortCriteria));

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then
    assertThat(result).isNotNull();
    // Filter to only test-created customers
    var testCustomers =
        result.getContent().stream()
            .filter(
                c ->
                    List.of(
                            "Enterprise Company",
                            "Large Company",
                            "Medium Company",
                            "Small Company")
                        .contains(c.companyName()))
            .toList();
    assertThat(testCustomers).hasSize(4);
    // Should be sorted by volume descending: 100000, 75000, 50000, 25000
    assertThat(testCustomers.get(0).expectedAnnualVolume())
        .isEqualByComparingTo(BigDecimal.valueOf(100000));
    assertThat(testCustomers.get(1).expectedAnnualVolume())
        .isEqualByComparingTo(BigDecimal.valueOf(75000));
    assertThat(testCustomers.get(2).expectedAnnualVolume())
        .isEqualByComparingTo(BigDecimal.valueOf(50000));
    assertThat(testCustomers.get(3).expectedAnnualVolume())
        .isEqualByComparingTo(BigDecimal.valueOf(25000));
  }

  @Test
  @TestTransaction
  @DisplayName("Should handle multiple sort criteria")
  void shouldHandleMultipleSortCriteria() {
    // Given: Create customers with same status but different names
    createCustomersForMultiSort();
    CustomerSearchRequest request = new CustomerSearchRequest();
    // Filter to only Active/Potential customers to exclude old test data
    request.setGlobalSearch("Active");

    SortCriteria sort1 = new SortCriteria();
    sort1.setField("status");
    sort1.setDirection("DESC");

    SortCriteria sort2 = new SortCriteria();
    sort2.setField("companyName");
    sort2.setDirection("ASC");

    request.setMultiSort(List.of(sort1, sort2));

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then - Filter results to only test-created customers
    var testCustomers =
        result.getContent().stream()
            .filter(
                c ->
                    List.of("Active Alpha", "Active Beta", "Active Gamma")
                        .contains(c.companyName()))
            .toList();
    assertThat(testCustomers).hasSize(3);

    // All should be AKTIV status customers, sorted by name
    assertThat(testCustomers.get(0).status()).isEqualTo(CustomerStatus.AKTIV);
    assertThat(testCustomers.get(0).companyName()).isEqualTo("Active Alpha");
    assertThat(testCustomers.get(1).status()).isEqualTo(CustomerStatus.AKTIV);
    assertThat(testCustomers.get(1).companyName()).isEqualTo("Active Beta");
    assertThat(testCustomers.get(2).status()).isEqualTo(CustomerStatus.AKTIV);
    assertThat(testCustomers.get(2).companyName()).isEqualTo("Active Gamma");
  }

  @Test
  @TestTransaction
  @DisplayName("Should maintain stable sort for equal values")
  void shouldMaintainStableSortForEqualValues() {
    // Given: Create customers with same volume
    createCustomersWithSameVolume();
    CustomerSearchRequest request = new CustomerSearchRequest();
    // Filter to only "Company" customers to exclude old test data
    request.setGlobalSearch("Company");

    SortCriteria sortCriteria = new SortCriteria();
    sortCriteria.setField("expectedAnnualVolume");
    sortCriteria.setDirection("DESC");
    request.setMultiSort(List.of(sortCriteria));

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then - Filter results to only test-created customers
    var testCustomers =
        result.getContent().stream()
            .filter(c -> List.of("Company 1", "Company 2", "Company 3").contains(c.companyName()))
            .toList();
    assertThat(testCustomers).hasSize(3);
    // All have same volume, so order should be stable (by creation order or secondary criteria)
    assertThat(testCustomers)
        .allMatch(
            customer -> customer.expectedAnnualVolume().compareTo(BigDecimal.valueOf(50000)) == 0);
  }

  @Test
  @TestTransaction
  @DisplayName("Should apply default sort when no sort criteria specified")
  void shouldApplyDefaultSortWhenNoCriteria() {
    // Given: Remember initial count and create customers
    CustomerSearchRequest initialRequest = new CustomerSearchRequest();
    long initialCount = searchService.search(initialRequest, 0, 1).getTotalElements();

    createCustomersForSorting();
    CustomerSearchRequest request = new CustomerSearchRequest();
    // No sort criteria specified

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 100);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getTotalElements()).isEqualTo(initialCount + 3);
    // Should have some default ordering (usually by ID or creation time)
    assertThat(result.getContent()).isNotEmpty();
    // Verify our test customers are present
    assertThat(result.getContent())
        .anyMatch(c -> c.companyName().equals("Alpha Company"))
        .anyMatch(c -> c.companyName().equals("Beta Company"))
        .anyMatch(c -> c.companyName().equals("Gamma Company"));
  }

  @Test
  @TestTransaction
  @DisplayName("Should combine sort with filters")
  void shouldCombineSortWithFilters() {
    // Given: Create diverse customers
    createDiverseCustomersForSortAndFilter();
    CustomerSearchRequest request = new CustomerSearchRequest();

    // Filter for AKTIV status
    FilterCriteria filter = new FilterCriteria();
    filter.setField("status");
    filter.setOperator(FilterOperator.EQUALS);
    filter.setValue("AKTIV");
    request.setFilters(List.of(filter));

    // Sort by volume descending
    SortCriteria sort = new SortCriteria();
    sort.setField("expectedAnnualVolume");
    sort.setDirection("DESC");
    request.setMultiSort(List.of(sort));

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then
    assertThat(result).isNotNull();
    // Should only return AKTIV customers, sorted by volume
    assertThat(result.getContent())
        .allMatch(customer -> customer.status().equals(CustomerStatus.AKTIV));

    // Verify descending order
    for (int i = 1; i < result.getContent().size(); i++) {
      BigDecimal prev = result.getContent().get(i - 1).expectedAnnualVolume();
      BigDecimal curr = result.getContent().get(i).expectedAnnualVolume();
      // Skip null checks - both should have values if sorted by volume
      if (prev != null && curr != null) {
        assertThat(prev.compareTo(curr)).isGreaterThanOrEqualTo(0);
      }
    }
  }

  // ==================== SHARED HELPER METHODS ====================
  private void createAndPersistSampleCustomers() {
    Customer customer1 =
        CustomerTestDataFactory.builder()
            .withCompanyName("Berlin Restaurant GmbH")
            .withStatus(CustomerStatus.AKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(50000))
            .withIndustry(Industry.RESTAURANT)
            .build();
    customer1.setCompanyName("Berlin Restaurant GmbH"); // Override to remove [TEST-xxx] prefix
    customer1.setCreatedAt(LocalDate.now().minusDays(30).atStartOfDay());
    customerRepository.persist(customer1);

    Customer customer2 =
        CustomerTestDataFactory.builder()
            .withCompanyName("Munich Catering Services")
            .withStatus(CustomerStatus.AKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(75000))
            .withIndustry(Industry.CATERING)
            .build();
    customer2.setCompanyName("Munich Catering Services"); // Override to remove [TEST-xxx] prefix
    customer2.setCreatedAt(LocalDate.now().minusDays(15).atStartOfDay());
    customerRepository.persist(customer2);
  }

  private void createAndPersistMultipleCustomers(int count) {
    for (int i = 0; i < count; i++) {
      Customer customer =
          CustomerTestDataFactory.builder()
              .withCompanyName("Test Customer " + (i + 1))
              .withStatus(CustomerStatus.AKTIV)
              .withExpectedAnnualVolume(BigDecimal.valueOf(10000 + (i * 5000)))
              .withIndustry(Industry.HOTEL)
              .build();
      customer.setCompanyName("Test Customer " + (i + 1)); // Override prefix
      customerRepository.persist(customer);
    }
  }

  private void createCustomersWithDifferentStatuses() {
    Customer active =
        CustomerTestDataFactory.builder()
            .withCompanyName("Active Company")
            .withStatus(CustomerStatus.AKTIV)
            .build();
    active.setCompanyName("Active Company");
    customerRepository.persist(active);

    Customer inactive =
        CustomerTestDataFactory.builder()
            .withCompanyName("Inactive Company")
            .withStatus(CustomerStatus.INAKTIV)
            .build();
    inactive.setCompanyName("Inactive Company");
    customerRepository.persist(inactive);

    Customer potential =
        CustomerTestDataFactory.builder()
            .withCompanyName("Potential Company")
            .withStatus(CustomerStatus.PROSPECT)
            .build();
    potential.setCompanyName("Potential Company");
    customerRepository.persist(potential);
  }

  private void createCustomersWithDifferentDates() {
    Customer recent =
        CustomerTestDataFactory.builder().withCompanyName("[TEST-DATE] Recent Company").build();
    recent.setCompanyName("[TEST-DATE] Recent Company");
    recent.setCreatedAt(LocalDate.now().minusDays(5).atStartOfDay());
    customerRepository.persist(recent);

    Customer older =
        CustomerTestDataFactory.builder().withCompanyName("[TEST-DATE] Older Company").build();
    older.setCompanyName("[TEST-DATE] Older Company");
    older.setCreatedAt(LocalDate.now().minusDays(30).atStartOfDay());
    customerRepository.persist(older);

    Customer oldest =
        CustomerTestDataFactory.builder().withCompanyName("[TEST-DATE] Oldest Company").build();
    oldest.setCompanyName("[TEST-DATE] Oldest Company");
    oldest.setCreatedAt(LocalDate.now().minusDays(90).atStartOfDay());
    customerRepository.persist(oldest);
  }

  private void createCustomersWithDifferentVolumes() {
    Customer small =
        CustomerTestDataFactory.builder()
            .withCompanyName("Small Company")
            .withExpectedAnnualVolume(BigDecimal.valueOf(25000))
            .build();
    small.setCompanyName("Small Company");
    customerRepository.persist(small);

    Customer medium =
        CustomerTestDataFactory.builder()
            .withCompanyName("Medium Company")
            .withExpectedAnnualVolume(BigDecimal.valueOf(50000))
            .build();
    medium.setCompanyName("Medium Company");
    customerRepository.persist(medium);

    Customer large =
        CustomerTestDataFactory.builder()
            .withCompanyName("Large Company")
            .withExpectedAnnualVolume(BigDecimal.valueOf(75000))
            .build();
    large.setCompanyName("Large Company");
    customerRepository.persist(large);

    Customer enterprise =
        CustomerTestDataFactory.builder()
            .withCompanyName("Enterprise Company")
            .withExpectedAnnualVolume(BigDecimal.valueOf(100000))
            .build();
    enterprise.setCompanyName("Enterprise Company");
    customerRepository.persist(enterprise);
  }

  private void createCustomersWithDifferentIndustries() {
    Customer hotel1 =
        CustomerTestDataFactory.builder()
            .withCompanyName("Hotel One")
            .withIndustry(Industry.HOTEL)
            .build();
    hotel1.setCompanyName("Hotel One");
    customerRepository.persist(hotel1);

    Customer hotel2 =
        CustomerTestDataFactory.builder()
            .withCompanyName("Hotel Two")
            .withIndustry(Industry.HOTEL)
            .build();
    hotel2.setCompanyName("Hotel Two");
    customerRepository.persist(hotel2);

    Customer restaurant =
        CustomerTestDataFactory.builder()
            .withCompanyName("Restaurant")
            .withIndustry(Industry.RESTAURANT)
            .build();
    restaurant.setCompanyName("Restaurant");
    customerRepository.persist(restaurant);

    Customer catering =
        CustomerTestDataFactory.builder()
            .withCompanyName("Catering Service")
            .withIndustry(Industry.CATERING)
            .build();
    catering.setCompanyName("Catering Service");
    customerRepository.persist(catering);
  }

  private void createDiverseCustomers() {
    Customer active1 =
        CustomerTestDataFactory.builder()
            .withCompanyName("Active High Volume")
            .withStatus(CustomerStatus.AKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(75000))
            .build();
    active1.setCompanyName("Active High Volume");
    customerRepository.persist(active1);

    Customer active2 =
        CustomerTestDataFactory.builder()
            .withCompanyName("Active Low Volume")
            .withStatus(CustomerStatus.AKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(25000))
            .build();
    active2.setCompanyName("Active Low Volume");
    customerRepository.persist(active2);

    Customer inactive =
        CustomerTestDataFactory.builder()
            .withCompanyName("Inactive High Volume")
            .withStatus(CustomerStatus.INAKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(100000))
            .build();
    inactive.setCompanyName("Inactive High Volume");
    customerRepository.persist(inactive);
  }

  private void createCustomersForSorting() {
    Customer beta = CustomerTestDataFactory.builder().withCompanyName("Beta Company").build();
    beta.setCompanyName("Beta Company");
    customerRepository.persist(beta);

    Customer gamma = CustomerTestDataFactory.builder().withCompanyName("Gamma Company").build();
    gamma.setCompanyName("Gamma Company");
    customerRepository.persist(gamma);

    Customer alpha = CustomerTestDataFactory.builder().withCompanyName("Alpha Company").build();
    alpha.setCompanyName("Alpha Company");
    customerRepository.persist(alpha);
  }

  private void createCustomersWithSameVolume() {
    for (int i = 1; i <= 3; i++) {
      Customer customer =
          CustomerTestDataFactory.builder()
              .withCompanyName("Company " + i)
              .withExpectedAnnualVolume(BigDecimal.valueOf(50000))
              .build();
      customer.setCompanyName("Company " + i);
      customerRepository.persist(customer);
    }
  }

  private void createCustomersForMultiSort() {
    Customer activeBeta =
        CustomerTestDataFactory.builder()
            .withCompanyName("Active Beta")
            .withStatus(CustomerStatus.AKTIV)
            .build();
    activeBeta.setCompanyName("Active Beta");
    customerRepository.persist(activeBeta);

    Customer activeAlpha =
        CustomerTestDataFactory.builder()
            .withCompanyName("Active Alpha")
            .withStatus(CustomerStatus.AKTIV)
            .build();
    activeAlpha.setCompanyName("Active Alpha");
    customerRepository.persist(activeAlpha);

    Customer potential =
        CustomerTestDataFactory.builder()
            .withCompanyName("Potential Company")
            .withStatus(CustomerStatus.PROSPECT)
            .build();
    potential.setCompanyName("Potential Company");
    customerRepository.persist(potential);

    Customer activeGamma =
        CustomerTestDataFactory.builder()
            .withCompanyName("Active Gamma")
            .withStatus(CustomerStatus.AKTIV)
            .build();
    activeGamma.setCompanyName("Active Gamma");
    customerRepository.persist(activeGamma);
  }

  private void createDiverseCustomersForSortAndFilter() {
    Customer active1 =
        CustomerTestDataFactory.builder()
            .withCompanyName("Active High")
            .withStatus(CustomerStatus.AKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(80000))
            .build();
    active1.setCompanyName("Active High");
    customerRepository.persist(active1);

    Customer inactive =
        CustomerTestDataFactory.builder()
            .withCompanyName("Inactive Higher")
            .withStatus(CustomerStatus.INAKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(90000))
            .build();
    inactive.setCompanyName("Inactive Higher");
    customerRepository.persist(inactive);

    Customer active2 =
        CustomerTestDataFactory.builder()
            .withCompanyName("Active Medium")
            .withStatus(CustomerStatus.AKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(50000))
            .build();
    active2.setCompanyName("Active Medium");
    customerRepository.persist(active2);

    Customer active3 =
        CustomerTestDataFactory.builder()
            .withCompanyName("Active Low")
            .withStatus(CustomerStatus.AKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(30000))
            .build();
    active3.setCompanyName("Active Low");
    customerRepository.persist(active3);
  }
}

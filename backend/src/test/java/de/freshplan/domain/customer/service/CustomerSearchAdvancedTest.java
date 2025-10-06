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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for advanced search features of CustomerSearchService.
 *
 * <p>Consolidates: SmartSortTest + PaginationTest (38 tests total)
 */
@QuarkusTest
@Tag("integration")
@DisplayName("CustomerSearchService - Advanced Features")
class CustomerSearchAdvancedTest {

  @Inject CustomerSearchService searchService;
  @Inject CustomerRepository customerRepository;
  @Inject SmartSortService smartSortService;

  // ==================== SMART SORT TESTS ====================
  @Test
  @TestTransaction
  @DisplayName("Should apply SALES_PRIORITY smart sort strategy")
  void shouldApplySalesPriorityStrategy() {
    // Given: Create customers with different sales potential
    createCustomersForSalesPriority();
    CustomerSearchRequest request = new CustomerSearchRequest();
    List<SortCriteria> sortCriteria =
        smartSortService.createSmartSort(SmartSortService.SmartSortStrategy.SALES_PRIORITY);
    request.setMultiSort(sortCriteria);

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(3);
    // High volume active customer should come first
    assertThat(result.getContent().get(0).companyName()).isEqualTo("High Priority Customer");
    assertThat(result.getContent().get(0).expectedAnnualVolume())
        .isEqualByComparingTo(BigDecimal.valueOf(100000));
  }

  @Test
  @TestTransaction
  @DisplayName("Should apply RISK_MITIGATION smart sort strategy")
  void shouldApplyRiskMitigationStrategy() {
    // Given: Create customers with different risk scores
    createCustomersForRiskMitigation();
    CustomerSearchRequest request = new CustomerSearchRequest();
    List<SortCriteria> sortCriteria =
        smartSortService.createSmartSort(SmartSortService.SmartSortStrategy.RISK_MITIGATION);
    request.setMultiSort(sortCriteria);

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(3);
    // High risk customers should come first for mitigation
    assertThat(result.getContent().get(0).companyName()).contains("High Risk");
  }

  @Test
  @TestTransaction
  @DisplayName("Should apply ENGAGEMENT_FOCUS smart sort strategy")
  void shouldApplyEngagementFocusStrategy() {
    // Given: Create customers with different engagement levels
    createCustomersForEngagementFocus();
    CustomerSearchRequest request = new CustomerSearchRequest();
    List<SortCriteria> sortCriteria =
        smartSortService.createSmartSort(SmartSortService.SmartSortStrategy.ENGAGEMENT_FOCUS);
    request.setMultiSort(sortCriteria);

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(3);
    // Customers needing engagement should be prioritized
  }

  @Test
  @TestTransaction
  @DisplayName("Should apply REVENUE_POTENTIAL smart sort strategy")
  void shouldApplyRevenuePotentialStrategy() {
    // Given: Create customers with different revenue potential
    createCustomersForRevenuePotential();
    CustomerSearchRequest request = new CustomerSearchRequest();
    List<SortCriteria> sortCriteria =
        smartSortService.createSmartSort(SmartSortService.SmartSortStrategy.REVENUE_POTENTIAL);
    request.setMultiSort(sortCriteria);

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(3);
    // Highest revenue potential should come first
    BigDecimal firstVolume = result.getContent().get(0).expectedAnnualVolume();
    BigDecimal lastVolume =
        result.getContent().get(result.getContent().size() - 1).expectedAnnualVolume();
    assertThat(firstVolume).isGreaterThan(lastVolume);
  }

  @Test
  @TestTransaction
  @DisplayName("Should apply CONTACT_FREQUENCY smart sort strategy")
  void shouldApplyContactFrequencyStrategy() {
    // Given: Create customers with different contact dates
    createCustomersForContactFrequency();
    CustomerSearchRequest request = new CustomerSearchRequest();
    List<SortCriteria> sortCriteria =
        smartSortService.createSmartSort(SmartSortService.SmartSortStrategy.CONTACT_FREQUENCY);
    request.setMultiSort(sortCriteria);

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(3);
    // Customers with oldest contact should come first
    assertThat(result.getContent().get(0).companyName()).contains("Long Time No Contact");
  }

  @Test
  @TestTransaction
  @DisplayName("Should combine smart sort with global search")
  void shouldCombineSmartSortWithGlobalSearch() {
    // Given: Create diverse customers
    createDiverseCustomers();
    CustomerSearchRequest request = new CustomerSearchRequest();
    request.setGlobalSearch("Priority");
    List<SortCriteria> sortCriteria =
        smartSortService.createSmartSort(SmartSortService.SmartSortStrategy.SALES_PRIORITY);
    request.setMultiSort(sortCriteria);

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then
    assertThat(result).isNotNull();
    // Should only return customers matching "Priority"
    assertThat(result.getContent()).allMatch(c -> c.companyName().contains("Priority"));
    // And they should be sorted by sales priority
    if (result.getContent().size() > 1) {
      BigDecimal first = result.getContent().get(0).expectedAnnualVolume();
      BigDecimal last =
          result.getContent().get(result.getContent().size() - 1).expectedAnnualVolume();
      assertThat(first).isGreaterThanOrEqualTo(last);
    }
  }

  @Test
  @TestTransaction
  @DisplayName("Should combine smart sort with filters")
  void shouldCombineSmartSortWithFilters() {
    // Given: Create diverse customers
    createDiverseCustomers();
    CustomerSearchRequest request = new CustomerSearchRequest();

    // Filter for AKTIV status
    FilterCriteria filter = new FilterCriteria();
    filter.setField("status");
    filter.setOperator(FilterOperator.EQUALS);
    filter.setValue("AKTIV");
    request.setFilters(List.of(filter));

    // Apply smart sort
    List<SortCriteria> sortCriteria =
        smartSortService.createSmartSort(SmartSortService.SmartSortStrategy.SALES_PRIORITY);
    request.setMultiSort(sortCriteria);

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).allMatch(c -> c.status().equals(CustomerStatus.AKTIV));
  }

  @Test
  @TestTransaction
  @DisplayName("Should handle empty sort criteria list gracefully")
  void shouldHandleEmptySortCriteriaList() {
    // Given: Create customers
    createCustomersForSalesPriority();
    CustomerSearchRequest request = new CustomerSearchRequest();
    request.setMultiSort(List.of()); // Empty list

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then: Should return results without error
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(3);
  }

  // ==================== PAGINATION TESTS ====================
  @Test
  @TestTransaction
  @DisplayName("Should return first page with correct size")
  void shouldReturnFirstPageWithCorrectSize() {
    // Given: Create 10 customers
    createMultipleCustomers(10);
    CustomerSearchRequest request = new CustomerSearchRequest();

    // When: Request first page with size 5
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 5);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getPage()).isEqualTo(0);
    assertThat(result.getSize()).isEqualTo(5);
    assertThat(result.getContent()).hasSize(5);
    assertThat(result.getTotalElements()).isEqualTo(10L);
    assertThat(result.getTotalPages()).isEqualTo(2);
    assertThat(result.isFirst()).isTrue();
    assertThat(result.isLast()).isFalse();
  }

  @Test
  @TestTransaction
  @DisplayName("Should return second page with remaining elements")
  void shouldReturnSecondPageWithRemainingElements() {
    // Given: Create 12 customers
    createMultipleCustomers(12);
    CustomerSearchRequest request = new CustomerSearchRequest();

    // When: Request second page with size 5
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 1, 5);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getPage()).isEqualTo(1);
    assertThat(result.getSize()).isEqualTo(5);
    assertThat(result.getContent()).hasSize(5);
    assertThat(result.getTotalElements()).isEqualTo(12L);
    assertThat(result.getTotalPages()).isEqualTo(3);
    assertThat(result.isFirst()).isFalse();
    assertThat(result.isLast()).isFalse();
  }

  @Test
  @TestTransaction
  @DisplayName("Should return last page with partial content")
  void shouldReturnLastPageWithPartialContent() {
    // Given: Create 13 customers
    createMultipleCustomers(13);
    CustomerSearchRequest request = new CustomerSearchRequest();

    // When: Request third page with size 5 (should have only 3 elements)
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 2, 5);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getPage()).isEqualTo(2);
    assertThat(result.getSize()).isEqualTo(5);
    assertThat(result.getContent()).hasSize(3); // Only 3 remaining
    assertThat(result.getTotalElements()).isEqualTo(13L);
    assertThat(result.getTotalPages()).isEqualTo(3);
    assertThat(result.isFirst()).isFalse();
    assertThat(result.isLast()).isTrue();
  }

  @Test
  @TestTransaction
  @DisplayName("Should handle single page with all elements")
  void shouldHandleSinglePageWithAllElements() {
    // Given: Create 3 customers
    createMultipleCustomers(3);
    CustomerSearchRequest request = new CustomerSearchRequest();

    // When: Request with page size larger than total
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getPage()).isEqualTo(0);
    assertThat(result.getContent()).hasSize(3);
    assertThat(result.getTotalPages()).isEqualTo(1);
    assertThat(result.isFirst()).isTrue();
    assertThat(result.isLast()).isTrue();
  }

  @Test
  @TestTransaction
  @DisplayName("Should return empty page when beyond last page")
  void shouldReturnEmptyPageWhenBeyondLastPage() {
    // Given: Create 5 customers
    createMultipleCustomers(5);
    CustomerSearchRequest request = new CustomerSearchRequest();

    // When: Request page beyond available data
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 10, 5);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getPage()).isEqualTo(10);
    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isEqualTo(5L);
  }

  @Test
  @TestTransaction
  @DisplayName("Should handle large page size")
  void shouldHandleLargePageSize() {
    // Given: Create 50 customers
    createMultipleCustomers(50);
    CustomerSearchRequest request = new CustomerSearchRequest();

    // When: Request with large page size
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 100);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(50);
    assertThat(result.getTotalPages()).isEqualTo(1);
  }

  @Test
  @TestTransaction
  @DisplayName("Should validate negative page number")
  void shouldValidateNegativePageNumber() {
    // Given: Create customers
    createMultipleCustomers(5);
    CustomerSearchRequest request = new CustomerSearchRequest();

    // When & Then: Should throw exception for negative page
    assertThatThrownBy(() -> searchService.search(request, -1, 10))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Page index must be >= 0");
  }

  @Test
  @TestTransaction
  @DisplayName("Should validate zero page size")
  void shouldValidateZeroPageSize() {
    // Given: Create customers
    createMultipleCustomers(5);
    CustomerSearchRequest request = new CustomerSearchRequest();

    // When & Then: Should throw exception for zero page size
    assertThatThrownBy(() -> searchService.search(request, 0, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Page size must be > 0");
  }

  @Test
  @TestTransaction
  @DisplayName("Should validate negative page size")
  void shouldValidateNegativePageSize() {
    // Given: Create customers
    createMultipleCustomers(5);
    CustomerSearchRequest request = new CustomerSearchRequest();

    // When & Then: Should throw exception for negative page size
    assertThatThrownBy(() -> searchService.search(request, 0, -5))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Page size must be > 0");
  }

  @Test
  @TestTransaction
  @DisplayName("Should not have duplicate results across pages")
  void shouldNotHaveDuplicateResultsAcrossPages() {
    // Given: Create 15 customers
    createMultipleCustomers(15);
    CustomerSearchRequest request = new CustomerSearchRequest();

    // When: Get all pages
    CustomerSearchService.PagedResponse<CustomerResponse> page1 =
        searchService.search(request, 0, 5);
    CustomerSearchService.PagedResponse<CustomerResponse> page2 =
        searchService.search(request, 1, 5);
    CustomerSearchService.PagedResponse<CustomerResponse> page3 =
        searchService.search(request, 2, 5);

    // Then: Collect all IDs and check for uniqueness
    Set<String> allIds = new HashSet<>();
    page1.getContent().forEach(c -> allIds.add(c.id()));
    page2.getContent().forEach(c -> allIds.add(c.id()));
    page3.getContent().forEach(c -> allIds.add(c.id()));

    assertThat(allIds).hasSize(15); // All IDs should be unique
  }

  @Test
  @TestTransaction
  @DisplayName("Should maintain consistent pagination with filters")
  void shouldMaintainConsistentPaginationWithFilters() {
    // Given: Create customers with different statuses
    createCustomersWithMixedStatuses(20);
    CustomerSearchRequest request = new CustomerSearchRequest();
    FilterCriteria filter = new FilterCriteria();
    filter.setField("status");
    filter.setOperator(FilterOperator.EQUALS);
    filter.setValue("AKTIV");
    request.setFilters(List.of(filter));

    // When: Get paginated results with filter
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 5);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent())
        .hasSizeLessThanOrEqualTo(5)
        .allMatch(c -> c.status().equals(CustomerStatus.AKTIV));
    // Total should be only AKTIV customers (10 out of 20)
    assertThat(result.getTotalElements()).isEqualTo(10L);
    assertThat(result.getTotalPages()).isEqualTo(2);
  }

  // ==================== HELPER METHODS ====================
  private void createCustomersForSalesPriority() {
    Customer highPriority =
        CustomerTestDataFactory.builder()
            .withCompanyName("High Priority Customer")
            .withStatus(CustomerStatus.AKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(100000))
            .build();
    highPriority.setCompanyName("High Priority Customer");
    customerRepository.persist(highPriority);

    Customer mediumPriority =
        CustomerTestDataFactory.builder()
            .withCompanyName("Medium Priority Customer")
            .withStatus(CustomerStatus.AKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(50000))
            .build();
    mediumPriority.setCompanyName("Medium Priority Customer");
    customerRepository.persist(mediumPriority);

    Customer lowPriority =
        CustomerTestDataFactory.builder()
            .withCompanyName("Low Priority Customer")
            .withStatus(CustomerStatus.PROSPECT)
            .withExpectedAnnualVolume(BigDecimal.valueOf(10000))
            .build();
    lowPriority.setCompanyName("Low Priority Customer");
    customerRepository.persist(lowPriority);
  }

  private void createCustomersForRiskMitigation() {
    Customer highRisk =
        CustomerTestDataFactory.builder()
            .withCompanyName("High Risk Customer")
            .withRiskScore(85)
            .build();
    highRisk.setCompanyName("High Risk Customer");
    customerRepository.persist(highRisk);

    Customer mediumRisk =
        CustomerTestDataFactory.builder()
            .withCompanyName("Medium Risk Customer")
            .withRiskScore(50)
            .build();
    mediumRisk.setCompanyName("Medium Risk Customer");
    customerRepository.persist(mediumRisk);

    Customer lowRisk =
        CustomerTestDataFactory.builder()
            .withCompanyName("Low Risk Customer")
            .withRiskScore(15)
            .build();
    lowRisk.setCompanyName("Low Risk Customer");
    customerRepository.persist(lowRisk);
  }

  private void createCustomersForEngagementFocus() {
    Customer needsEngagement =
        CustomerTestDataFactory.builder()
            .withCompanyName("Needs Engagement")
            .withLastContactDate(LocalDateTime.now().minusDays(90))
            .build();
    needsEngagement.setCompanyName("Needs Engagement");
    customerRepository.persist(needsEngagement);

    Customer recentContact =
        CustomerTestDataFactory.builder()
            .withCompanyName("Recent Contact")
            .withLastContactDate(LocalDateTime.now().minusDays(5))
            .build();
    recentContact.setCompanyName("Recent Contact");
    customerRepository.persist(recentContact);

    Customer moderateContact =
        CustomerTestDataFactory.builder()
            .withCompanyName("Moderate Contact")
            .withLastContactDate(LocalDateTime.now().minusDays(30))
            .build();
    moderateContact.setCompanyName("Moderate Contact");
    customerRepository.persist(moderateContact);
  }

  private void createCustomersForRevenuePotential() {
    Customer highRevenue =
        CustomerTestDataFactory.builder()
            .withCompanyName("High Revenue Potential")
            .withExpectedAnnualVolume(BigDecimal.valueOf(150000))
            .build();
    highRevenue.setCompanyName("High Revenue Potential");
    customerRepository.persist(highRevenue);

    Customer mediumRevenue =
        CustomerTestDataFactory.builder()
            .withCompanyName("Medium Revenue Potential")
            .withExpectedAnnualVolume(BigDecimal.valueOf(75000))
            .build();
    mediumRevenue.setCompanyName("Medium Revenue Potential");
    customerRepository.persist(mediumRevenue);

    Customer lowRevenue =
        CustomerTestDataFactory.builder()
            .withCompanyName("Low Revenue Potential")
            .withExpectedAnnualVolume(BigDecimal.valueOf(25000))
            .build();
    lowRevenue.setCompanyName("Low Revenue Potential");
    customerRepository.persist(lowRevenue);
  }

  private void createCustomersForContactFrequency() {
    Customer oldContact =
        CustomerTestDataFactory.builder()
            .withCompanyName("Long Time No Contact")
            .withLastContactDate(LocalDateTime.now().minusDays(120))
            .build();
    oldContact.setCompanyName("Long Time No Contact");
    customerRepository.persist(oldContact);

    Customer recentContact =
        CustomerTestDataFactory.builder()
            .withCompanyName("Recently Contacted")
            .withLastContactDate(LocalDateTime.now().minusDays(2))
            .build();
    recentContact.setCompanyName("Recently Contacted");
    customerRepository.persist(recentContact);

    Customer moderateContact =
        CustomerTestDataFactory.builder()
            .withCompanyName("Moderate Contact Gap")
            .withLastContactDate(LocalDateTime.now().minusDays(45))
            .build();
    moderateContact.setCompanyName("Moderate Contact Gap");
    customerRepository.persist(moderateContact);
  }

  private void createDiverseCustomers() {
    Customer priority1 =
        CustomerTestDataFactory.builder()
            .withCompanyName("High Priority Active")
            .withStatus(CustomerStatus.AKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(120000))
            .build();
    priority1.setCompanyName("High Priority Active");
    customerRepository.persist(priority1);

    Customer priority2 =
        CustomerTestDataFactory.builder()
            .withCompanyName("Medium Priority Active")
            .withStatus(CustomerStatus.AKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(60000))
            .build();
    priority2.setCompanyName("Medium Priority Active");
    customerRepository.persist(priority2);

    Customer inactive =
        CustomerTestDataFactory.builder()
            .withCompanyName("High Volume Inactive")
            .withStatus(CustomerStatus.INAKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(200000))
            .build();
    inactive.setCompanyName("High Volume Inactive");
    customerRepository.persist(inactive);

    Customer noPriority =
        CustomerTestDataFactory.builder()
            .withCompanyName("No Match Customer")
            .withStatus(CustomerStatus.AKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(30000))
            .build();
    noPriority.setCompanyName("No Match Customer");
    customerRepository.persist(noPriority);
  }

  private void createMultipleCustomers(int count) {
    for (int i = 1; i <= count; i++) {
      Customer customer =
          CustomerTestDataFactory.builder()
              .withCompanyName("Customer " + String.format("%03d", i))
              .withStatus(CustomerStatus.AKTIV)
              .withExpectedAnnualVolume(BigDecimal.valueOf(10000 + (i * 1000)))
              .withIndustry(Industry.HOTEL)
              .build();
      customer.setCompanyName("Customer " + String.format("%03d", i));
      customerRepository.persist(customer);
    }
  }

  private void createCustomersWithMixedStatuses(int total) {
    for (int i = 1; i <= total; i++) {
      CustomerStatus status = (i % 2 == 0) ? CustomerStatus.AKTIV : CustomerStatus.INAKTIV;
      Customer customer =
          CustomerTestDataFactory.builder()
              .withCompanyName("Customer " + i)
              .withStatus(status)
              .build();
      customer.setCompanyName("Customer " + i);
      customerRepository.persist(customer);
    }
  }
}

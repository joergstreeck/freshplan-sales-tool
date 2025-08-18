package de.freshplan.domain.customer.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.*;
import de.freshplan.test.builders.CustomerBuilder;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for pagination functionality of CustomerSearchService.
 *
 * <p>Tests page navigation, page size handling, and pagination edge cases.
 */
@QuarkusTest
@DisplayName("CustomerSearchService - Pagination Tests")
class CustomerSearchPaginationTest {

  @Inject CustomerSearchService searchService;
  @Inject CustomerRepository customerRepository;
  @Inject CustomerBuilder customerBuilder;

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

  // Helper methods

  private void createMultipleCustomers(int count) {
    for (int i = 1; i <= count; i++) {
      Customer customer =
          customerBuilder
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
          customerBuilder.withCompanyName("Customer " + i).withStatus(status).build();
      customer.setCompanyName("Customer " + i);
      customerRepository.persist(customer);
    }
  }
}

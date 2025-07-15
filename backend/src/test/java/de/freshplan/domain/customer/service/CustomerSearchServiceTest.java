package de.freshplan.domain.customer.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.*;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for CustomerSearchService.
 *
 * <p>Tests the service layer logic for customer search functionality, including filter processing,
 * query building, and result transformation.
 */
@QuarkusTest
class CustomerSearchServiceTest {

  @Inject CustomerSearchService searchService;

  @InjectMock CustomerRepository customerRepository;

  @InjectMock SmartSortService smartSortService;

  private PanacheQuery<Customer> mockQuery;

  @BeforeEach
  void setUp() {
    mockQuery = mock(PanacheQuery.class);
  }

  @Nested
  @DisplayName("Basic Search Tests")
  class BasicSearchTests {

    @Test
    @DisplayName("Should perform basic search without filters")
    void testBasicSearch_withoutFilters() {
      // Given
      CustomerSearchRequest request = new CustomerSearchRequest();
      List<Customer> customers = createSampleCustomers();

      when(customerRepository.findAll(any(Sort.class))).thenReturn(mockQuery);
      when(mockQuery.page(any(Page.class))).thenReturn(mockQuery);
      when(mockQuery.list()).thenReturn(customers);
      when(mockQuery.count()).thenReturn(2L);

      // When
      CustomerSearchService.PagedResponse<CustomerResponse> result =
          searchService.search(request, 0, 10);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(2);
      assertThat(result.getTotalElements()).isEqualTo(2L);
      assertThat(result.getPage()).isEqualTo(0);
      assertThat(result.getSize()).isEqualTo(10);

      verify(customerRepository).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("Should perform global search with text")
    void testGlobalSearch_withText() {
      // Given
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("Berlin");
      List<Customer> customers = createSampleCustomers();

      when(customerRepository.find(anyString(), any(Object[].class))).thenReturn(mockQuery);
      when(mockQuery.page(any(Page.class))).thenReturn(mockQuery);
      when(mockQuery.list()).thenReturn(customers);
      when(mockQuery.count()).thenReturn(1L);

      // When
      CustomerSearchService.PagedResponse<CustomerResponse> result =
          searchService.search(request, 0, 10);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(2);
      assertThat(result.getTotalElements()).isEqualTo(1L);

      verify(customerRepository).find(contains("companyName"), any(Object[].class));
    }

    @Test
    @DisplayName("Should handle empty search results")
    void testSearch_withEmptyResults() {
      // Given
      CustomerSearchRequest request = new CustomerSearchRequest();
      request.setGlobalSearch("NonExistentCompany");

      when(customerRepository.find(anyString(), any(Object[].class))).thenReturn(mockQuery);
      when(mockQuery.page(any(Page.class))).thenReturn(mockQuery);
      when(mockQuery.list()).thenReturn(List.of());
      when(mockQuery.count()).thenReturn(0L);

      // When
      CustomerSearchService.PagedResponse<CustomerResponse> result =
          searchService.search(request, 0, 10);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isEqualTo(0L);
      assertThat(result.getTotalPages()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("Filter Tests")
  class FilterTests {

    @Test
    @DisplayName("Should filter by status")
    void testSearch_withStatusFilter() {
      // Given
      CustomerSearchRequest request = new CustomerSearchRequest();
      FilterCriteria statusFilter = new FilterCriteria();
      statusFilter.setField("status");
      statusFilter.setOperator(FilterOperator.EQUALS);
      statusFilter.setValue("AKTIV");
      request.setFilters(List.of(statusFilter));

      List<Customer> customers = createSampleCustomers();
      when(customerRepository.find(anyString(), any(Object[].class))).thenReturn(mockQuery);
      when(mockQuery.page(any(Page.class))).thenReturn(mockQuery);
      when(mockQuery.list()).thenReturn(customers);
      when(mockQuery.count()).thenReturn(1L);

      // When
      CustomerSearchService.PagedResponse<CustomerResponse> result =
          searchService.search(request, 0, 10);

      // Then
      assertThat(result).isNotNull();
      verify(customerRepository).find(contains("status"), any(Object[].class));
    }

    @Test
    @DisplayName("Should filter by date range")
    void testSearch_withDateRangeFilter() {
      // Given
      CustomerSearchRequest request = new CustomerSearchRequest();
      FilterCriteria dateFilter = new FilterCriteria();
      dateFilter.setField("createdAt");
      dateFilter.setOperator(FilterOperator.GREATER_THAN);
      dateFilter.setValue("2024-01-01");
      request.setFilters(List.of(dateFilter));

      List<Customer> customers = createSampleCustomers();
      when(customerRepository.find(anyString(), any(Object[].class))).thenReturn(mockQuery);
      when(mockQuery.page(any(Page.class))).thenReturn(mockQuery);
      when(mockQuery.list()).thenReturn(customers);
      when(mockQuery.count()).thenReturn(1L);

      // When
      CustomerSearchService.PagedResponse<CustomerResponse> result =
          searchService.search(request, 0, 10);

      // Then
      assertThat(result).isNotNull();
      verify(customerRepository).find(contains("createdAt"), any(Object[].class));
    }

    @Test
    @DisplayName("Should filter by numeric range")
    void testSearch_withNumericRangeFilter() {
      // Given
      CustomerSearchRequest request = new CustomerSearchRequest();
      FilterCriteria volumeFilter = new FilterCriteria();
      volumeFilter.setField("expectedAnnualVolume");
      volumeFilter.setOperator(FilterOperator.GREATER_THAN);
      volumeFilter.setValue("10000");
      request.setFilters(List.of(volumeFilter));

      List<Customer> customers = createSampleCustomers();
      when(customerRepository.find(anyString(), any(Object[].class))).thenReturn(mockQuery);
      when(mockQuery.page(any(Page.class))).thenReturn(mockQuery);
      when(mockQuery.list()).thenReturn(customers);
      when(mockQuery.count()).thenReturn(1L);

      // When
      CustomerSearchService.PagedResponse<CustomerResponse> result =
          searchService.search(request, 0, 10);

      // Then
      assertThat(result).isNotNull();
      verify(customerRepository).find(contains("expectedAnnualVolume"), any(Object[].class));
    }
  }

  @Nested
  @DisplayName("Smart Sort Tests")
  class SmartSortTests {

    @Test
    @DisplayName("Should apply smart sort strategy")
    void testSmartSearch_withStrategy() {
      // Given
      SmartSortService.SmartSortStrategy strategy =
          SmartSortService.SmartSortStrategy.SALES_PRIORITY;
      CustomerSearchRequest request = new CustomerSearchRequest();
      List<SortCriteria> sortCriteria = smartSortService.createSmartSort(strategy);
      request.setMultiSort(sortCriteria);

      List<Customer> customers = createSampleCustomers();
      Sort mockSort = Sort.by("expectedAnnualVolume").descending();

      when(smartSortService.createPanacheSort(strategy)).thenReturn(mockSort);
      when(customerRepository.findAll(mockSort)).thenReturn(mockQuery);
      when(mockQuery.page(any(Page.class))).thenReturn(mockQuery);
      when(mockQuery.list()).thenReturn(customers);
      when(mockQuery.count()).thenReturn(2L);

      // When
      CustomerSearchService.PagedResponse<CustomerResponse> result =
          searchService.search(request, 0, 10);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(2);
      verify(smartSortService).createPanacheSort(strategy);
      verify(customerRepository).findAll(mockSort);
    }

    @Test
    @DisplayName("Should handle invalid smart sort strategy")
    void testSmartSearch_withInvalidStrategy() {
      // Given
      CustomerSearchRequest request = new CustomerSearchRequest();
      // Test with empty sort criteria list
      request.setMultiSort(List.of());

      // When & Then - this will just return normal unsorted results
      CustomerSearchService.PagedResponse<CustomerResponse> result =
          searchService.search(request, 0, 10);
      assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should combine smart sort with global search")
    void testSmartSearch_withGlobalSearch() {
      // Given
      CustomerSearchRequest request = new CustomerSearchRequest();
      List<SortCriteria> sortCriteria =
          smartSortService.createSmartSort(SmartSortService.SmartSortStrategy.SALES_PRIORITY);
      request.setMultiSort(sortCriteria);
      request.setGlobalSearch("Berlin");

      List<Customer> customers = createSampleCustomers();
      Sort mockSort = Sort.by("expectedAnnualVolume").descending();

      when(smartSortService.createPanacheSort(any())).thenReturn(mockSort);
      when(customerRepository.find(anyString(), any(Object[].class))).thenReturn(mockQuery);
      when(mockQuery.page(any(Page.class))).thenReturn(mockQuery);
      when(mockQuery.list()).thenReturn(customers);
      when(mockQuery.count()).thenReturn(1L);

      // When
      CustomerSearchService.PagedResponse<CustomerResponse> result =
          searchService.search(request, 0, 10);

      // Then
      assertThat(result).isNotNull();
      verify(smartSortService).createPanacheSort(any());
      verify(customerRepository).find(contains("companyName"), any(Object[].class));
    }
  }

  @Nested
  @DisplayName("Pagination Tests")
  class PaginationTests {

    @Test
    @DisplayName("Should validate pagination parameters")
    void testSearch_withValidPagination() {
      // Given
      CustomerSearchRequest request = new CustomerSearchRequest();
      List<Customer> customers = createSampleCustomers();

      when(customerRepository.findAll(any(Sort.class))).thenReturn(mockQuery);
      when(mockQuery.page(any(Page.class))).thenReturn(mockQuery);
      when(mockQuery.list()).thenReturn(customers);
      when(mockQuery.count()).thenReturn(10L);

      // When
      CustomerSearchService.PagedResponse<CustomerResponse> result =
          searchService.search(request, 1, 5);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getPage()).isEqualTo(1);
      assertThat(result.getSize()).isEqualTo(5);
      assertThat(result.getTotalPages()).isEqualTo(2); // 10 elements / 5 per page

      verify(mockQuery).page(Page.of(1, 5));
    }

    @Test
    @DisplayName("Should handle negative page numbers")
    void testSearch_withNegativePage() {
      // Given
      CustomerSearchRequest request = new CustomerSearchRequest();

      // When & Then
      assertThatThrownBy(() -> searchService.search(request, -1, 10))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Page index must be >= 0");
    }

    @Test
    @DisplayName("Should handle zero page size")
    void testSearch_withZeroPageSize() {
      // Given
      CustomerSearchRequest request = new CustomerSearchRequest();

      // When & Then
      assertThatThrownBy(() -> searchService.search(request, 0, 0))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Page size must be > 0");
    }
  }

  @Nested
  @DisplayName("Sorting Tests")
  class SortingTests {

    @Test
    @DisplayName("Should apply custom sort criteria")
    void testSearch_withCustomSort() {
      // Given
      CustomerSearchRequest request = new CustomerSearchRequest();
      SortCriteria sortCriteria = new SortCriteria();
      sortCriteria.setField("companyName");
      sortCriteria.setDirection("ASC");
      request.setMultiSort(List.of(sortCriteria));

      List<Customer> customers = createSampleCustomers();
      when(customerRepository.findAll(any(Sort.class))).thenReturn(mockQuery);
      when(mockQuery.page(any(Page.class))).thenReturn(mockQuery);
      when(mockQuery.list()).thenReturn(customers);
      when(mockQuery.count()).thenReturn(2L);

      // When
      CustomerSearchService.PagedResponse<CustomerResponse> result =
          searchService.search(request, 0, 10);

      // Then
      assertThat(result).isNotNull();
      verify(customerRepository).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("Should handle multiple sort criteria")
    void testSearch_withMultipleSortCriteria() {
      // Given
      CustomerSearchRequest request = new CustomerSearchRequest();
      SortCriteria sort1 = new SortCriteria();
      sort1.setField("status");
      sort1.setDirection("DESC");

      SortCriteria sort2 = new SortCriteria();
      sort2.setField("companyName");
      sort2.setDirection("ASC");

      request.setMultiSort(List.of(sort1, sort2));

      List<Customer> customers = createSampleCustomers();
      when(customerRepository.findAll(any(Sort.class))).thenReturn(mockQuery);
      when(mockQuery.page(any(Page.class))).thenReturn(mockQuery);
      when(mockQuery.list()).thenReturn(customers);
      when(mockQuery.count()).thenReturn(2L);

      // When
      CustomerSearchService.PagedResponse<CustomerResponse> result =
          searchService.search(request, 0, 10);

      // Then
      assertThat(result).isNotNull();
      verify(customerRepository).findAll(any(Sort.class));
    }
  }

  /** Helper method to create sample customers for testing. */
  private List<Customer> createSampleCustomers() {
    Customer customer1 = new Customer();
    customer1.setCompanyName("Berlin Restaurant GmbH");
    customer1.setStatus(CustomerStatus.AKTIV);
    customer1.setExpectedAnnualVolume(BigDecimal.valueOf(50000));
    customer1.setCreatedAt(LocalDate.now().minusDays(30).atStartOfDay());

    Customer customer2 = new Customer();
    customer2.setCompanyName("Munich Catering Services");
    customer2.setStatus(CustomerStatus.AKTIV);
    customer2.setExpectedAnnualVolume(BigDecimal.valueOf(75000));
    customer2.setCreatedAt(LocalDate.now().minusDays(15).atStartOfDay());

    return Arrays.asList(customer1, customer2);
  }
}

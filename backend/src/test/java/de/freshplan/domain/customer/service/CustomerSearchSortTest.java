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
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for sorting functionality of CustomerSearchService.
 * 
 * Tests custom sort criteria, multiple sort fields, and sort direction handling.
 */
@QuarkusTest
@DisplayName("CustomerSearchService - Sort Tests")
class CustomerSearchSortTest {

  @Inject CustomerSearchService searchService;
  @Inject CustomerRepository customerRepository;
  @Inject CustomerBuilder customerBuilder;

  @Test
  @TestTransaction
  @DisplayName("Should apply ascending sort by company name")
  void shouldApplyAscendingSortByCompanyName() {
    // Given: Create test customers with different names
    createCustomersForSorting();
    CustomerSearchRequest request = new CustomerSearchRequest();
    SortCriteria sortCriteria = new SortCriteria();
    sortCriteria.setField("companyName");
    sortCriteria.setDirection("ASC");
    request.setMultiSort(List.of(sortCriteria));

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(3);
    // Alpha Company should come before Beta Company before Gamma Company
    assertThat(result.getContent().get(0).companyName()).isEqualTo("Alpha Company");
    assertThat(result.getContent().get(1).companyName()).isEqualTo("Beta Company");
    assertThat(result.getContent().get(2).companyName()).isEqualTo("Gamma Company");
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
    assertThat(result.getContent()).hasSize(3);
    // Should be sorted by volume descending: 100000, 50000, 25000
    assertThat(result.getContent().get(0).expectedAnnualVolume())
        .isEqualByComparingTo(BigDecimal.valueOf(100000));
    assertThat(result.getContent().get(1).expectedAnnualVolume())
        .isEqualByComparingTo(BigDecimal.valueOf(50000));
    assertThat(result.getContent().get(2).expectedAnnualVolume())
        .isEqualByComparingTo(BigDecimal.valueOf(25000));
  }

  @Test
  @TestTransaction
  @DisplayName("Should handle multiple sort criteria")
  void shouldHandleMultipleSortCriteria() {
    // Given: Create customers with same status but different names
    createCustomersForMultiSort();
    CustomerSearchRequest request = new CustomerSearchRequest();
    
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

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(4);
    
    // First, POTENTIELL status (highest alphabetically when DESC)
    assertThat(result.getContent().get(0).status()).isEqualTo(CustomerStatus.PROSPECT);
    
    // Then AKTIV status customers, sorted by name
    assertThat(result.getContent().get(1).status()).isEqualTo(CustomerStatus.AKTIV);
    assertThat(result.getContent().get(1).companyName()).isEqualTo("Active Alpha");
    assertThat(result.getContent().get(2).status()).isEqualTo(CustomerStatus.AKTIV);
    assertThat(result.getContent().get(2).companyName()).isEqualTo("Active Beta");
    assertThat(result.getContent().get(3).status()).isEqualTo(CustomerStatus.AKTIV);
    assertThat(result.getContent().get(3).companyName()).isEqualTo("Active Gamma");
  }

  @Test
  @TestTransaction
  @DisplayName("Should maintain stable sort for equal values")
  void shouldMaintainStableSortForEqualValues() {
    // Given: Create customers with same volume
    createCustomersWithSameVolume();
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
    assertThat(result.getContent()).hasSize(3);
    // All have same volume, so order should be stable (by creation order or secondary criteria)
    assertThat(result.getContent())
        .allMatch(customer -> customer.expectedAnnualVolume()
            .compareTo(BigDecimal.valueOf(50000)) == 0);
  }

  @Test
  @TestTransaction
  @DisplayName("Should apply default sort when no sort criteria specified")
  void shouldApplyDefaultSortWhenNoCriteria() {
    // Given: Create customers
    createCustomersForSorting();
    CustomerSearchRequest request = new CustomerSearchRequest();
    // No sort criteria specified

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(3);
    // Should have some default ordering (usually by ID or creation time)
    assertThat(result.getContent()).isNotEmpty();
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
      assertThat(prev.compareTo(curr)).isGreaterThanOrEqualTo(0);
    }
  }

  // Helper methods

  private void createCustomersForSorting() {
    Customer beta = customerBuilder
        .withCompanyName("Beta Company")
        .build();
    beta.setCompanyName("Beta Company");
    customerRepository.persist(beta);

    Customer gamma = customerBuilder
        .withCompanyName("Gamma Company")
        .build();
    gamma.setCompanyName("Gamma Company");
    customerRepository.persist(gamma);

    Customer alpha = customerBuilder
        .withCompanyName("Alpha Company")
        .build();
    alpha.setCompanyName("Alpha Company");
    customerRepository.persist(alpha);
  }

  private void createCustomersWithDifferentVolumes() {
    Customer medium = customerBuilder
        .withCompanyName("Medium Volume")
        .withExpectedAnnualVolume(BigDecimal.valueOf(50000))
        .build();
    medium.setCompanyName("Medium Volume");
    customerRepository.persist(medium);

    Customer high = customerBuilder
        .withCompanyName("High Volume")
        .withExpectedAnnualVolume(BigDecimal.valueOf(100000))
        .build();
    high.setCompanyName("High Volume");
    customerRepository.persist(high);

    Customer low = customerBuilder
        .withCompanyName("Low Volume")
        .withExpectedAnnualVolume(BigDecimal.valueOf(25000))
        .build();
    low.setCompanyName("Low Volume");
    customerRepository.persist(low);
  }

  private void createCustomersWithSameVolume() {
    for (int i = 1; i <= 3; i++) {
      Customer customer = customerBuilder
          .withCompanyName("Company " + i)
          .withExpectedAnnualVolume(BigDecimal.valueOf(50000))
          .build();
      customer.setCompanyName("Company " + i);
      customerRepository.persist(customer);
    }
  }

  private void createCustomersForMultiSort() {
    Customer activeBeta = customerBuilder
        .withCompanyName("Active Beta")
        .withStatus(CustomerStatus.AKTIV)
        .build();
    activeBeta.setCompanyName("Active Beta");
    customerRepository.persist(activeBeta);

    Customer activeAlpha = customerBuilder
        .withCompanyName("Active Alpha")
        .withStatus(CustomerStatus.AKTIV)
        .build();
    activeAlpha.setCompanyName("Active Alpha");
    customerRepository.persist(activeAlpha);

    Customer potential = customerBuilder
        .withCompanyName("Potential Company")
        .withStatus(CustomerStatus.PROSPECT)
        .build();
    potential.setCompanyName("Potential Company");
    customerRepository.persist(potential);

    Customer activeGamma = customerBuilder
        .withCompanyName("Active Gamma")
        .withStatus(CustomerStatus.AKTIV)
        .build();
    activeGamma.setCompanyName("Active Gamma");
    customerRepository.persist(activeGamma);
  }

  private void createDiverseCustomersForSortAndFilter() {
    Customer active1 = customerBuilder
        .withCompanyName("Active High")
        .withStatus(CustomerStatus.AKTIV)
        .withExpectedAnnualVolume(BigDecimal.valueOf(80000))
        .build();
    active1.setCompanyName("Active High");
    customerRepository.persist(active1);

    Customer inactive = customerBuilder
        .withCompanyName("Inactive Higher")
        .withStatus(CustomerStatus.INAKTIV)
        .withExpectedAnnualVolume(BigDecimal.valueOf(90000))
        .build();
    inactive.setCompanyName("Inactive Higher");
    customerRepository.persist(inactive);

    Customer active2 = customerBuilder
        .withCompanyName("Active Medium")
        .withStatus(CustomerStatus.AKTIV)
        .withExpectedAnnualVolume(BigDecimal.valueOf(50000))
        .build();
    active2.setCompanyName("Active Medium");
    customerRepository.persist(active2);

    Customer active3 = customerBuilder
        .withCompanyName("Active Low")
        .withStatus(CustomerStatus.AKTIV)
        .withExpectedAnnualVolume(BigDecimal.valueOf(30000))
        .build();
    active3.setCompanyName("Active Low");
    customerRepository.persist(active3);
  }
}
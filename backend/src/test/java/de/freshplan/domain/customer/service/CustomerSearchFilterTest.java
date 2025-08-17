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
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for filter functionality of CustomerSearchService.
 * 
 * Tests various filter operations including status, date range, numeric range,
 * and combined filters.
 */
@QuarkusTest
@DisplayName("CustomerSearchService - Filter Tests")
class CustomerSearchFilterTest {

  @Inject CustomerSearchService searchService;
  @Inject CustomerRepository customerRepository;
  @Inject CustomerBuilder customerBuilder;

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
    // Given: Create customers with different dates
    createCustomersWithDifferentDates();
    CustomerSearchRequest request = new CustomerSearchRequest();
    FilterCriteria dateFilter = new FilterCriteria();
    dateFilter.setField("createdAt");
    dateFilter.setOperator(FilterOperator.GREATER_THAN);
    dateFilter.setValue("2024-01-01");
    request.setFilters(List.of(dateFilter));

    // When
    CustomerSearchService.PagedResponse<CustomerResponse> result =
        searchService.search(request, 0, 10);

    // Then
    assertThat(result).isNotNull();
    // All our test customers are created recently, so all should match
    assertThat(result.getContent()).hasSize(3);
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
        .allMatch(customer -> customer.expectedAnnualVolume() != null 
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
        .allMatch(customer -> "HOTEL".equals(customer.industry().toString()));
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
        .allMatch(customer -> "AKTIV".equals(customer.status().toString()) 
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

  // Helper methods

  private void createCustomersWithDifferentStatuses() {
    Customer active = customerBuilder
        .withCompanyName("Active Company")
        .withStatus(CustomerStatus.AKTIV)
        .build();
    active.setCompanyName("Active Company");
    customerRepository.persist(active);

    Customer inactive = customerBuilder
        .withCompanyName("Inactive Company")
        .withStatus(CustomerStatus.INAKTIV)
        .build();
    inactive.setCompanyName("Inactive Company");
    customerRepository.persist(inactive);

    Customer potential = customerBuilder
        .withCompanyName("Potential Company")
        .withStatus(CustomerStatus.PROSPECT)
        .build();
    potential.setCompanyName("Potential Company");
    customerRepository.persist(potential);
  }

  private void createCustomersWithDifferentDates() {
    Customer recent = customerBuilder
        .withCompanyName("Recent Company")
        .build();
    recent.setCompanyName("Recent Company");
    recent.setCreatedAt(LocalDate.now().minusDays(5).atStartOfDay());
    customerRepository.persist(recent);

    Customer older = customerBuilder
        .withCompanyName("Older Company")
        .build();
    older.setCompanyName("Older Company");
    older.setCreatedAt(LocalDate.now().minusDays(30).atStartOfDay());
    customerRepository.persist(older);

    Customer oldest = customerBuilder
        .withCompanyName("Oldest Company")
        .build();
    oldest.setCompanyName("Oldest Company");
    oldest.setCreatedAt(LocalDate.now().minusDays(90).atStartOfDay());
    customerRepository.persist(oldest);
  }

  private void createCustomersWithDifferentVolumes() {
    Customer small = customerBuilder
        .withCompanyName("Small Company")
        .withExpectedAnnualVolume(BigDecimal.valueOf(25000))
        .build();
    small.setCompanyName("Small Company");
    customerRepository.persist(small);

    Customer medium = customerBuilder
        .withCompanyName("Medium Company")
        .withExpectedAnnualVolume(BigDecimal.valueOf(50000))
        .build();
    medium.setCompanyName("Medium Company");
    customerRepository.persist(medium);

    Customer large = customerBuilder
        .withCompanyName("Large Company")
        .withExpectedAnnualVolume(BigDecimal.valueOf(75000))
        .build();
    large.setCompanyName("Large Company");
    customerRepository.persist(large);

    Customer enterprise = customerBuilder
        .withCompanyName("Enterprise Company")
        .withExpectedAnnualVolume(BigDecimal.valueOf(100000))
        .build();
    enterprise.setCompanyName("Enterprise Company");
    customerRepository.persist(enterprise);
  }

  private void createCustomersWithDifferentIndustries() {
    Customer hotel1 = customerBuilder
        .withCompanyName("Hotel One")
        .withIndustry(Industry.HOTEL)
        .build();
    hotel1.setCompanyName("Hotel One");
    customerRepository.persist(hotel1);

    Customer hotel2 = customerBuilder
        .withCompanyName("Hotel Two")
        .withIndustry(Industry.HOTEL)
        .build();
    hotel2.setCompanyName("Hotel Two");
    customerRepository.persist(hotel2);

    Customer restaurant = customerBuilder
        .withCompanyName("Restaurant")
        .withIndustry(Industry.RESTAURANT)
        .build();
    restaurant.setCompanyName("Restaurant");
    customerRepository.persist(restaurant);

    Customer catering = customerBuilder
        .withCompanyName("Catering Service")
        .withIndustry(Industry.CATERING)
        .build();
    catering.setCompanyName("Catering Service");
    customerRepository.persist(catering);
  }

  private void createDiverseCustomers() {
    Customer active1 = customerBuilder
        .withCompanyName("Active High Volume")
        .withStatus(CustomerStatus.AKTIV)
        .withExpectedAnnualVolume(BigDecimal.valueOf(75000))
        .build();
    active1.setCompanyName("Active High Volume");
    customerRepository.persist(active1);

    Customer active2 = customerBuilder
        .withCompanyName("Active Low Volume")
        .withStatus(CustomerStatus.AKTIV)
        .withExpectedAnnualVolume(BigDecimal.valueOf(25000))
        .build();
    active2.setCompanyName("Active Low Volume");
    customerRepository.persist(active2);

    Customer inactive = customerBuilder
        .withCompanyName("Inactive High Volume")
        .withStatus(CustomerStatus.INAKTIV)
        .withExpectedAnnualVolume(BigDecimal.valueOf(100000))
        .build();
    inactive.setCompanyName("Inactive High Volume");
    customerRepository.persist(inactive);
  }
}
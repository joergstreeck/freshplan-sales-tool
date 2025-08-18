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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for basic search functionality of CustomerSearchService.
 *
 * <p>Tests basic search operations without filters, global search, and empty results.
 */
@QuarkusTest
@DisplayName("CustomerSearchService - Basic Search Tests")
class CustomerSearchBasicTest {

  @Inject CustomerSearchService searchService;
  @Inject CustomerRepository customerRepository;
  @Inject CustomerBuilder customerBuilder;

  @Test
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

  // Helper methods

  private void createAndPersistSampleCustomers() {
    Customer customer1 =
        customerBuilder
            .withCompanyName("Berlin Restaurant GmbH")
            .withStatus(CustomerStatus.AKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(50000))
            .withIndustry(Industry.RESTAURANT)
            .build();
    customer1.setCompanyName("Berlin Restaurant GmbH"); // Override to remove [TEST-xxx] prefix
    customer1.setCreatedAt(LocalDate.now().minusDays(30).atStartOfDay());
    customerRepository.persist(customer1);

    Customer customer2 =
        customerBuilder
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
          customerBuilder
              .withCompanyName("Test Customer " + (i + 1))
              .withStatus(CustomerStatus.AKTIV)
              .withExpectedAnnualVolume(BigDecimal.valueOf(10000 + (i * 5000)))
              .withIndustry(Industry.HOTEL)
              .build();
      customer.setCompanyName("Test Customer " + (i + 1)); // Override prefix
      customerRepository.persist(customer);
    }
  }
}

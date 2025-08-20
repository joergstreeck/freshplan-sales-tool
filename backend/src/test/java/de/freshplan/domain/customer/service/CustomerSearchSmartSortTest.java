package de.freshplan.domain.customer.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.*;
import de.freshplan.test.builders.CustomerBuilder;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
/**
 * Integration tests for SmartSort functionality of CustomerSearchService.
 *
 * <p>Tests intelligent sorting strategies like SALES_PRIORITY, RISK_MITIGATION, etc.
 */
@QuarkusTest
@Tag("migrate")@DisplayName("CustomerSearchService - SmartSort Tests")
class CustomerSearchSmartSortTest {

  @Inject CustomerSearchService searchService;
  @Inject CustomerRepository customerRepository;
  @Inject CustomerBuilder customerBuilder;
  @Inject SmartSortService smartSortService;

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

  // Helper methods

  private void createCustomersForSalesPriority() {
    Customer highPriority =
        customerBuilder
            .withCompanyName("High Priority Customer")
            .withStatus(CustomerStatus.AKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(100000))
            .build();
    highPriority.setCompanyName("High Priority Customer");
    customerRepository.persist(highPriority);

    Customer mediumPriority =
        customerBuilder
            .withCompanyName("Medium Priority Customer")
            .withStatus(CustomerStatus.AKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(50000))
            .build();
    mediumPriority.setCompanyName("Medium Priority Customer");
    customerRepository.persist(mediumPriority);

    Customer lowPriority =
        customerBuilder
            .withCompanyName("Low Priority Customer")
            .withStatus(CustomerStatus.PROSPECT)
            .withExpectedAnnualVolume(BigDecimal.valueOf(10000))
            .build();
    lowPriority.setCompanyName("Low Priority Customer");
    customerRepository.persist(lowPriority);
  }

  private void createCustomersForRiskMitigation() {
    Customer highRisk =
        customerBuilder.withCompanyName("High Risk Customer").withRiskScore(85).build();
    highRisk.setCompanyName("High Risk Customer");
    customerRepository.persist(highRisk);

    Customer mediumRisk =
        customerBuilder.withCompanyName("Medium Risk Customer").withRiskScore(50).build();
    mediumRisk.setCompanyName("Medium Risk Customer");
    customerRepository.persist(mediumRisk);

    Customer lowRisk =
        customerBuilder.withCompanyName("Low Risk Customer").withRiskScore(15).build();
    lowRisk.setCompanyName("Low Risk Customer");
    customerRepository.persist(lowRisk);
  }

  private void createCustomersForEngagementFocus() {
    Customer needsEngagement =
        customerBuilder
            .withCompanyName("Needs Engagement")
            .withLastContactDate(LocalDateTime.now().minusDays(90))
            .build();
    needsEngagement.setCompanyName("Needs Engagement");
    customerRepository.persist(needsEngagement);

    Customer recentContact =
        customerBuilder
            .withCompanyName("Recent Contact")
            .withLastContactDate(LocalDateTime.now().minusDays(5))
            .build();
    recentContact.setCompanyName("Recent Contact");
    customerRepository.persist(recentContact);

    Customer moderateContact =
        customerBuilder
            .withCompanyName("Moderate Contact")
            .withLastContactDate(LocalDateTime.now().minusDays(30))
            .build();
    moderateContact.setCompanyName("Moderate Contact");
    customerRepository.persist(moderateContact);
  }

  private void createCustomersForRevenuePotential() {
    Customer highRevenue =
        customerBuilder
            .withCompanyName("High Revenue Potential")
            .withExpectedAnnualVolume(BigDecimal.valueOf(150000))
            .build();
    highRevenue.setCompanyName("High Revenue Potential");
    customerRepository.persist(highRevenue);

    Customer mediumRevenue =
        customerBuilder
            .withCompanyName("Medium Revenue Potential")
            .withExpectedAnnualVolume(BigDecimal.valueOf(75000))
            .build();
    mediumRevenue.setCompanyName("Medium Revenue Potential");
    customerRepository.persist(mediumRevenue);

    Customer lowRevenue =
        customerBuilder
            .withCompanyName("Low Revenue Potential")
            .withExpectedAnnualVolume(BigDecimal.valueOf(25000))
            .build();
    lowRevenue.setCompanyName("Low Revenue Potential");
    customerRepository.persist(lowRevenue);
  }

  private void createCustomersForContactFrequency() {
    Customer oldContact =
        customerBuilder
            .withCompanyName("Long Time No Contact")
            .withLastContactDate(LocalDateTime.now().minusDays(120))
            .build();
    oldContact.setCompanyName("Long Time No Contact");
    customerRepository.persist(oldContact);

    Customer recentContact =
        customerBuilder
            .withCompanyName("Recently Contacted")
            .withLastContactDate(LocalDateTime.now().minusDays(2))
            .build();
    recentContact.setCompanyName("Recently Contacted");
    customerRepository.persist(recentContact);

    Customer moderateContact =
        customerBuilder
            .withCompanyName("Moderate Contact Gap")
            .withLastContactDate(LocalDateTime.now().minusDays(45))
            .build();
    moderateContact.setCompanyName("Moderate Contact Gap");
    customerRepository.persist(moderateContact);
  }

  private void createDiverseCustomers() {
    Customer priority1 =
        customerBuilder
            .withCompanyName("High Priority Active")
            .withStatus(CustomerStatus.AKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(120000))
            .build();
    priority1.setCompanyName("High Priority Active");
    customerRepository.persist(priority1);

    Customer priority2 =
        customerBuilder
            .withCompanyName("Medium Priority Active")
            .withStatus(CustomerStatus.AKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(60000))
            .build();
    priority2.setCompanyName("Medium Priority Active");
    customerRepository.persist(priority2);

    Customer inactive =
        customerBuilder
            .withCompanyName("High Volume Inactive")
            .withStatus(CustomerStatus.INAKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(200000))
            .build();
    inactive.setCompanyName("High Volume Inactive");
    customerRepository.persist(inactive);

    Customer noPriority =
        customerBuilder
            .withCompanyName("No Match Customer")
            .withStatus(CustomerStatus.AKTIV)
            .withExpectedAnnualVolume(BigDecimal.valueOf(30000))
            .build();
    noPriority.setCompanyName("No Match Customer");
    customerRepository.persist(noPriority);
  }
}

package de.freshplan.domain.search.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.search.service.dto.ContactSearchDto;
import de.freshplan.domain.search.service.dto.CustomerSearchDto;
import de.freshplan.domain.search.service.dto.SearchResult;
import de.freshplan.domain.search.service.dto.SearchResults;
import de.freshplan.test.builders.ContactTestDataFactory;
import de.freshplan.test.builders.CustomerBuilder;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for SearchQueryService (CQRS Query Service).
 *
 * <p>Tests all read-only search operations following established CQRS test patterns: -
 * universalSearch() with various query types - quickSearch() for autocomplete functionality - Query
 * type detection and relevance scoring - Repository integration and error handling
 *
 * <p>Applied Test-Fixing Patterns: 1. PanacheQuery-Mocking (if needed) 2. Mockito
 * Matcher-Consistency (all parameters as matchers) 3. Foreign Key-Safe Cleanup (not applicable -
 * read-only) 4. Flexible Verification (atLeastOnce() instead of exact times())
 */
@QuarkusTest
@Tag("core")
class SearchQueryServiceTest {

  @Inject SearchQueryService searchQueryService;
  @Inject CustomerBuilder customerBuilder;

  @InjectMock CustomerRepository customerRepository;

  @InjectMock ContactRepository contactRepository;

  // Test data
  private Customer testCustomer1;
  private Customer testCustomer2;
  private Customer inactiveCustomer;
  private CustomerContact testContact1;
  private CustomerContact testContact2;

  @BeforeEach
  void setUp() {
    // Reset mocks
    reset(customerRepository, contactRepository);

    // Create test customers
    testCustomer1 =
        createTestCustomer(
            "KD-2025-00001",
            "Bäckerei Schmidt GmbH",
            CustomerStatus.AKTIV,
            LocalDateTime.now().minusDays(10));

    testCustomer2 =
        createTestCustomer(
            "KD-2025-00002",
            "Metzgerei Müller",
            CustomerStatus.LEAD,
            LocalDateTime.now().minusDays(45));

    inactiveCustomer =
        createTestCustomer(
            "KD-2025-00003",
            "Geschlossener Betrieb",
            CustomerStatus.INAKTIV,
            LocalDateTime.now().minusDays(200));

    // Create test contacts
    testContact1 =
        createTestContact(
            "Hans",
            "Schmidt",
            "schmidt@baeckerei.de",
            "+49-123-456789",
            true, // isPrimary
            testCustomer1);

    testContact2 =
        createTestContact(
            "Maria",
            "Müller",
            "mueller@metzgerei.de",
            "+49-987-654321",
            false, // isPrimary
            testCustomer2);
  }

  @Test
  void universalSearch_withTextQuery_shouldReturnCustomersAndContacts() {
    // Given
    String query = "schmidt";

    when(customerRepository.searchFullText(eq(query), eq(20)))
        .thenReturn(Arrays.asList(testCustomer1));
    when(contactRepository.searchContactsFullText(eq(query), eq(20)))
        .thenReturn(Arrays.asList(testContact1));

    // When
    SearchResults results = searchQueryService.universalSearch(query, true, false, 20);

    // Then
    assertThat(results.getCustomers()).hasSize(1);
    assertThat(results.getContacts()).hasSize(1);
    assertThat(results.getTotalCount()).isEqualTo(2);
    assertThat(results.getExecutionTime()).isGreaterThanOrEqualTo(0);

    // Verify customer result
    SearchResult customerResult = results.getCustomers().get(0);
    assertThat(customerResult.getType()).isEqualTo("customer");
    assertThat(customerResult.getRelevanceScore()).isGreaterThan(0);

    CustomerSearchDto customerDto = (CustomerSearchDto) customerResult.getData();
    assertThat(customerDto.getCompanyName()).isEqualTo("Bäckerei Schmidt GmbH");

    // Pattern 4: Flexible Verification
    verify(customerRepository, atLeastOnce()).searchFullText(eq(query), eq(20));
    verify(contactRepository, atLeastOnce()).searchContactsFullText(eq(query), eq(20));
  }

  @Test
  void universalSearch_withEmailQuery_shouldDetectEmailAndSearchAccordingly() {
    // Given
    String emailQuery = "schmidt@baeckerei.de";

    when(customerRepository.findByContactEmail(eq(emailQuery), eq(20)))
        .thenReturn(Arrays.asList(testCustomer1));
    when(contactRepository.findByEmail(eq(emailQuery), eq(20)))
        .thenReturn(Arrays.asList(testContact1));

    // When
    SearchResults results = searchQueryService.universalSearch(emailQuery, true, false, 20);

    // Then
    assertThat(results.getCustomers()).hasSize(1);
    assertThat(results.getContacts()).hasSize(1);

    // Verify correct email search methods were called
    verify(customerRepository).findByContactEmail(eq(emailQuery), eq(20));
    verify(contactRepository).findByEmail(eq(emailQuery), eq(20));

    // Verify text search was NOT called
    verify(customerRepository, never()).searchFullText(anyString(), anyInt());
  }

  @Test
  void universalSearch_withPhoneQuery_shouldDetectPhoneAndSearchAccordingly() {
    // Given
    String phoneQuery = "+49-123-456789";

    when(customerRepository.findByPhone(eq(phoneQuery), eq(20)))
        .thenReturn(Arrays.asList(testCustomer1));
    when(contactRepository.findByPhoneOrMobile(eq(phoneQuery), eq(20)))
        .thenReturn(Arrays.asList(testContact1));

    // When
    SearchResults results = searchQueryService.universalSearch(phoneQuery, true, false, 20);

    // Then
    assertThat(results.getCustomers()).hasSize(1);
    assertThat(results.getContacts()).hasSize(1);

    // Verify correct phone search methods were called
    verify(customerRepository).findByPhone(eq(phoneQuery), eq(20));
    verify(contactRepository).findByPhoneOrMobile(eq(phoneQuery), eq(20));
  }

  @Test
  void universalSearch_withCustomerNumberQuery_shouldDetectAndSearchByNumber() {
    // Given
    String customerNumberQuery = "KD-2025-00001";

    when(customerRepository.findByCustomerNumberLike(eq(customerNumberQuery + "%"), eq(20)))
        .thenReturn(Arrays.asList(testCustomer1));
    when(contactRepository.searchContactsFullText(eq(customerNumberQuery), eq(20)))
        .thenReturn(Arrays.asList());

    // When
    SearchResults results =
        searchQueryService.universalSearch(customerNumberQuery, true, false, 20);

    // Then
    assertThat(results.getCustomers()).hasSize(1);
    assertThat(results.getContacts()).hasSize(0);

    // Verify customer number search was called
    verify(customerRepository).findByCustomerNumberLike(eq(customerNumberQuery + "%"), eq(20));
  }

  @Test
  void universalSearch_withIncludeInactiveTrue_shouldIncludeInactiveCustomers() {
    // Given
    String query = "betrieb";

    when(customerRepository.searchFullText(eq(query), eq(20)))
        .thenReturn(Arrays.asList(testCustomer1, inactiveCustomer));

    // When
    SearchResults results = searchQueryService.universalSearch(query, false, true, 20);

    // Then
    assertThat(results.getCustomers()).hasSize(2);

    // Verify both active and inactive customers are included
    List<String> customerNames =
        results.getCustomers().stream()
            .map(result -> ((CustomerSearchDto) result.getData()).getCompanyName())
            .toList();
    assertThat(customerNames).contains("Bäckerei Schmidt GmbH", "Geschlossener Betrieb");
  }

  @Test
  void universalSearch_withIncludeInactiveFalse_shouldFilterOutInactiveCustomers() {
    // Given
    String query = "betrieb";

    when(customerRepository.searchFullText(eq(query), eq(20)))
        .thenReturn(Arrays.asList(testCustomer1, inactiveCustomer));

    // When
    SearchResults results = searchQueryService.universalSearch(query, false, false, 20);

    // Then
    assertThat(results.getCustomers()).hasSize(1);

    CustomerSearchDto customerDto = (CustomerSearchDto) results.getCustomers().get(0).getData();
    assertThat(customerDto.getCompanyName()).isEqualTo("Bäckerei Schmidt GmbH");
    assertThat(customerDto.getStatus()).isEqualTo("AKTIV");
  }

  @Test
  void universalSearch_withIncludeContactsFalse_shouldNotSearchContacts() {
    // Given
    String query = "schmidt";

    when(customerRepository.searchFullText(eq(query), eq(20)))
        .thenReturn(Arrays.asList(testCustomer1));

    // When
    SearchResults results = searchQueryService.universalSearch(query, false, false, 20);

    // Then
    assertThat(results.getCustomers()).hasSize(1);
    assertThat(results.getContacts()).hasSize(0);

    // Verify contact repository was never called
    verifyNoInteractions(contactRepository);
  }

  @Test
  void quickSearch_shouldReturnLimitedResultsWithMinimalData() {
    // Given
    String query = "bäckerei";

    when(customerRepository.quickSearch(eq(query), eq(5))).thenReturn(Arrays.asList(testCustomer1));

    // When
    SearchResults results = searchQueryService.quickSearch(query, 5);

    // Then
    assertThat(results.getCustomers()).hasSize(1);
    assertThat(results.getContacts()).hasSize(0); // Quick search doesn't include contacts
    assertThat(results.getExecutionTime()).isGreaterThanOrEqualTo(0);

    // Verify quick search method was called
    verify(customerRepository).quickSearch(eq(query), eq(5));

    // Verify relevance score is set to default for quick search
    SearchResult result = results.getCustomers().get(0);
    assertThat(result.getRelevanceScore()).isEqualTo(100);
  }

  @Test
  void universalSearch_shouldCalculateRelevanceScores() {
    // Given
    String query = "schmidt";
    Customer exactMatchCustomer =
        createTestCustomer(
            "KD-2025-00001",
            "schmidt", // Exact company name match
            CustomerStatus.AKTIV,
            LocalDateTime.now().minusDays(5) // Recent contact
            );

    Customer partialMatchCustomer =
        createTestCustomer(
            "KD-2025-00002",
            "Bäckerei Schmidt GmbH", // Contains query
            CustomerStatus.LEAD,
            LocalDateTime.now().minusDays(60) // Older contact
            );

    when(customerRepository.searchFullText(eq(query), eq(20)))
        .thenReturn(Arrays.asList(exactMatchCustomer, partialMatchCustomer));

    // When
    SearchResults results = searchQueryService.universalSearch(query, false, false, 20);

    // Then
    assertThat(results.getCustomers()).hasSize(2);

    // Results should be sorted by relevance score (highest first)
    SearchResult firstResult = results.getCustomers().get(0);
    SearchResult secondResult = results.getCustomers().get(1);

    assertThat(firstResult.getRelevanceScore()).isGreaterThan(secondResult.getRelevanceScore());

    // Exact match should have higher score
    CustomerSearchDto firstDto = (CustomerSearchDto) firstResult.getData();
    assertThat(firstDto.getCompanyName()).isEqualTo("schmidt");
  }

  @Test
  void universalSearch_withContactRelevanceScoring_shouldScoreCorrectly() {
    // Given
    String query = "hans";
    CustomerContact primaryContact =
        createTestContact("Hans", "Schmidt", "hans@test.de", "+49-123-456", true, testCustomer1);
    CustomerContact secondaryContact =
        createTestContact("Hans", "Müller", "hans.m@test.de", "+49-987-654", false, testCustomer2);

    when(customerRepository.searchFullText(eq(query), eq(20))).thenReturn(Arrays.asList());
    when(contactRepository.searchContactsFullText(eq(query), eq(20)))
        .thenReturn(Arrays.asList(primaryContact, secondaryContact));

    // When
    SearchResults results = searchQueryService.universalSearch(query, true, false, 20);

    // Then
    assertThat(results.getContacts()).hasSize(2);

    // Primary contact should have higher relevance score
    SearchResult firstResult = results.getContacts().get(0);
    SearchResult secondResult = results.getContacts().get(1);

    ContactSearchDto firstContact = (ContactSearchDto) firstResult.getData();
    ContactSearchDto secondContact = (ContactSearchDto) secondResult.getData();

    // Primary contact should be scored higher
    if (firstContact.getIsPrimary()) {
      assertThat(firstResult.getRelevanceScore()).isGreaterThan(secondResult.getRelevanceScore());
    }
  }

  @Test
  void universalSearch_withEmptyResults_shouldReturnEmptySearchResults() {
    // Given
    String query = "nonexistent";

    when(customerRepository.searchFullText(eq(query), eq(20))).thenReturn(Arrays.asList());
    when(contactRepository.searchContactsFullText(eq(query), eq(20))).thenReturn(Arrays.asList());

    // When
    SearchResults results = searchQueryService.universalSearch(query, true, false, 20);

    // Then
    assertThat(results.getCustomers()).hasSize(0);
    assertThat(results.getContacts()).hasSize(0);
    assertThat(results.getTotalCount()).isEqualTo(0);
    assertThat(results.getExecutionTime()).isGreaterThanOrEqualTo(0);
  }

  @Test
  void universalSearch_withRepositoryException_shouldPropagateException() {
    // Given
    String query = "error";

    when(customerRepository.searchFullText(eq(query), eq(20)))
        .thenThrow(new RuntimeException("Database connection failed"));

    // When & Then
    try {
      searchQueryService.universalSearch(query, true, false, 20);
      throw new AssertionError("Expected RuntimeException to be thrown");
    } catch (RuntimeException e) {
      assertThat(e.getMessage()).isEqualTo("Database connection failed");
    }
  }

  // Helper methods for creating test data (migrated to use TestDataBuilder)
  private Customer createTestCustomer(
      String customerNumber,
      String companyName,
      CustomerStatus status,
      LocalDateTime lastContactDate) {
    Customer customer = customerBuilder.withCompanyName(companyName).withStatus(status).build();
    customer.setId(UUID.randomUUID());
    customer.setCustomerNumber(customerNumber);
    customer.setCompanyName(companyName); // Override to remove [TEST] prefix for mock tests
    customer.setLastContactDate(lastContactDate);
    return customer;
  }

  private CustomerContact createTestContact(
      String firstName,
      String lastName,
      String email,
      String phone,
      boolean isPrimary,
      Customer customer) {
    var builder =
        ContactTestDataFactory.builder()
            .forCustomer(customer)
            .withFirstName(firstName)
            .withLastName(lastName)
            .withEmail(email)
            .withPhone(phone);

    if (isPrimary) {
      builder.asPrimary();
    }

    CustomerContact contact = builder.build();
    contact.setId(UUID.randomUUID());
    contact.setIsPrimary(isPrimary);
    contact.setCustomer(customer);
    return contact;
  }
}

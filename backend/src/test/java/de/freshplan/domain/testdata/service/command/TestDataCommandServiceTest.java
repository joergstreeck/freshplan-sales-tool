package de.freshplan.domain.testdata.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.repository.CustomerTimelineRepository;
import de.freshplan.domain.testdata.service.TestDataService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Tests for TestDataCommandService using the established test patterns from previous CQRS phases.
 *
 * <p>Applied Test-Fixing Patterns: 1. PanacheQuery-Mocking (if needed) 2. Mockito
 * Matcher-Consistency (all parameters as matchers) 3. Foreign Key-Safe Cleanup (proper delete
 * order) 4. Flexible Verification (atLeastOnce() instead of exact times())
 */
@QuarkusTest
class TestDataCommandServiceTest {

  @Inject TestDataCommandService commandService;

  @InjectMock CustomerRepository customerRepository;

  @InjectMock CustomerTimelineRepository timelineRepository;

  @BeforeEach
  void setUp() {
    // Reset mocks for each test
    reset(customerRepository, timelineRepository);
  }

  @Test
  void seedTestData_shouldCreateFiveCustomersWithTimelineEvents() {
    // Given
    // Pattern 2: Mockito Matcher-Consistency - all parameters as matchers
    doAnswer(
            invocation -> {
              Customer customer = invocation.getArgument(0);
              customer.setId(java.util.UUID.randomUUID()); // Simulate DB generated ID
              return null;
            })
        .when(customerRepository)
        .persist((Customer) any());
    doNothing().when(timelineRepository).persist((CustomerTimelineEvent) any());

    // When
    TestDataService.SeedResult result = commandService.seedTestData();

    // Then
    assertThat(result.customersCreated()).isEqualTo(5);
    assertThat(result.eventsCreated()).isEqualTo(4);

    // Pattern 4: Flexible Verification - use atLeastOnce() for robustness
    verify(customerRepository, times(5)).persist((Customer) any());
    verify(timelineRepository, times(4)).persist((CustomerTimelineEvent) any());

    // Verify specific customer characteristics
    ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
    verify(customerRepository, atLeastOnce()).persist(customerCaptor.capture());

    List<Customer> capturedCustomers = customerCaptor.getAllValues();
    assertThat(capturedCustomers).hasSize(5);

    // Verify all customers have [TEST] prefix
    assertThat(capturedCustomers)
        .allSatisfy(customer -> assertThat(customer.getCompanyName()).startsWith("[TEST]"))
        .allSatisfy(customer -> assertThat(customer.getIsTestData()).isTrue())
        .allSatisfy(customer -> assertThat(customer.getCreatedBy()).isEqualTo("test-data-seeder"));
  }

  @Test
  void seedTestData_shouldCreateSpecificRiskCustomerFirst() {
    // Given
    doAnswer(
            invocation -> {
              Customer customer = invocation.getArgument(0);
              customer.setId(java.util.UUID.randomUUID());
              return null;
            })
        .when(customerRepository)
        .persist((Customer) any());
    doAnswer(
            invocation -> {
              return null;
            })
        .when(timelineRepository)
        .persist((CustomerTimelineEvent) any());

    // When
    commandService.seedTestData();

    // Then
    ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
    verify(customerRepository, atLeastOnce()).persist(customerCaptor.capture());

    Customer firstCustomer = customerCaptor.getAllValues().get(0);
    assertThat(firstCustomer.getCustomerNumber()).isEqualTo("90001");
    assertThat(firstCustomer.getCompanyName())
        .isEqualTo("[TEST] Bäckerei Schmidt - 90 Tage ohne Kontakt");
    assertThat(firstCustomer.getStatus()).isEqualTo(CustomerStatus.AKTIV);
    assertThat(firstCustomer.getRiskScore()).isEqualTo(85);
    assertThat(firstCustomer.getExpectedAnnualVolume())
        .isEqualByComparingTo(new BigDecimal("65000.00"));
  }

  @Test
  void seedTestData_withRepositoryException_shouldThrowRuntimeException() {
    // Given
    doThrow(new RuntimeException("Database connection failed"))
        .when(customerRepository)
        .persist((Customer) any());

    // When & Then
    assertThatThrownBy(() -> commandService.seedTestData())
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Failed to seed test data: Database connection failed");
  }

  @Test
  void cleanTestData_shouldDeleteEventsBeforeCustomers() {
    // Given - Pattern 3: Foreign Key-Safe Cleanup order simulation
    when(timelineRepository.delete(eq("isTestData"), eq(true))).thenReturn(10L);
    when(customerRepository.delete(eq("isTestData"), eq(true))).thenReturn(5L);

    // When
    TestDataService.CleanupResult result = commandService.cleanTestData();

    // Then
    assertThat(result.customersDeleted()).isEqualTo(5L);
    assertThat(result.eventsDeleted()).isEqualTo(10L);

    // Pattern 3: Verify FK-safe delete order (events first, then customers)
    var inOrder = inOrder(timelineRepository, customerRepository);
    inOrder.verify(timelineRepository).delete(eq("isTestData"), eq(true));
    inOrder.verify(customerRepository).delete(eq("isTestData"), eq(true));
  }

  @Test
  void cleanTestData_withRepositoryException_shouldThrowRuntimeException() {
    // Given
    doThrow(new RuntimeException("Database error"))
        .when(timelineRepository)
        .delete(eq("isTestData"), eq(true));

    // When & Then
    assertThatThrownBy(() -> commandService.cleanTestData())
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Failed to clean test data: Database error");
  }

  @Test
  void cleanOldTestData_shouldUseComplexDeleteQuery() {
    // Given
    String expectedEventsQuery =
        "(isTestData is null or isTestData = false) and customer.companyName not like '[TEST]%'";
    String expectedCustomersQuery =
        "(isTestData is null or isTestData = false) and companyName not like '[TEST]%'";

    when(timelineRepository.delete(expectedEventsQuery)).thenReturn(15L);
    when(customerRepository.delete(expectedCustomersQuery)).thenReturn(8L);

    // When
    TestDataService.CleanupResult result = commandService.cleanOldTestData();

    // Then
    assertThat(result.customersDeleted()).isEqualTo(8L);
    assertThat(result.eventsDeleted()).isEqualTo(15L);

    // Verify complex query strings are used exactly
    verify(timelineRepository).delete(expectedEventsQuery);
    verify(customerRepository).delete(expectedCustomersQuery);
  }

  @Test
  void seedAdditionalTestData_shouldCreate14Customers() {
    // Given
    doAnswer(
            invocation -> {
              Customer customer = invocation.getArgument(0);
              customer.setId(java.util.UUID.randomUUID());
              return null;
            })
        .when(customerRepository)
        .persist((Customer) any());

    // When
    TestDataService.SeedResult result = commandService.seedAdditionalTestData();

    // Then
    assertThat(result.customersCreated()).isEqualTo(14);
    assertThat(result.eventsCreated()).isEqualTo(0); // No events in additional seed

    verify(customerRepository, times(14)).persist((Customer) any());
    verify(timelineRepository, never()).persist((CustomerTimelineEvent) any());

    // Verify specific customer number pattern
    ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
    verify(customerRepository, atLeastOnce()).persist(customerCaptor.capture());

    List<Customer> customers = customerCaptor.getAllValues();
    assertThat(customers.get(0).getCustomerNumber()).isEqualTo("ADD-001");
    assertThat(customers.get(13).getCustomerNumber()).isEqualTo("ADD-014");
  }

  @Test
  void seedAdditionalTestData_shouldUseModuloLogicForStatusAndIndustry() {
    // Given
    doAnswer(
            invocation -> {
              Customer customer = invocation.getArgument(0);
              customer.setId(java.util.UUID.randomUUID());
              return null;
            })
        .when(customerRepository)
        .persist((Customer) any());

    // When
    commandService.seedAdditionalTestData();

    // Then
    ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
    verify(customerRepository, atLeastOnce()).persist(customerCaptor.capture());

    List<Customer> customers = customerCaptor.getAllValues();

    // Verify modulo logic: i=1 -> i%4!=0, i%3!=0, i%2!=0 -> should be LEAD
    Customer customer1 = customers.get(0); // i=1
    assertThat(customer1.getStatus()).isEqualTo(CustomerStatus.LEAD);

    // Verify modulo logic: i=4 -> i%4==0 -> should be AKTIV
    Customer customer4 = customers.get(3); // i=4
    assertThat(customer4.getStatus()).isEqualTo(CustomerStatus.AKTIV);
  }

  @Test
  void seedComprehensiveTestData_shouldCallAllHelperMethods() {
    // Given
    doAnswer(
            invocation -> {
              Customer customer = invocation.getArgument(0);
              customer.setId(java.util.UUID.randomUUID());
              return null;
            })
        .when(customerRepository)
        .persist((Customer) any());

    // When
    TestDataService.SeedResult result = commandService.seedComprehensiveTestData();

    // Then
    assertThat(result.customersCreated()).isGreaterThan(30); // Should create many test cases
    assertThat(result.eventsCreated()).isEqualTo(0); // Comprehensive seed doesn't create events

    // Verify substantial number of customers created (from all helper methods)
    verify(customerRepository, atLeast(30)).persist((Customer) any());
    verify(timelineRepository, never()).persist((CustomerTimelineEvent) any());
  }

  @Test
  void seedComprehensiveTestData_shouldCreateStringBoundaryTestCustomers() {
    // Given
    doAnswer(
            invocation -> {
              Customer customer = invocation.getArgument(0);
              customer.setId(java.util.UUID.randomUUID());
              return null;
            })
        .when(customerRepository)
        .persist((Customer) any());

    // When
    commandService.seedComprehensiveTestData();

    // Then
    ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
    verify(customerRepository, atLeastOnce()).persist(customerCaptor.capture());

    List<Customer> customers = customerCaptor.getAllValues();

    // Verify string boundary tests are included
    boolean hasMinimalName =
        customers.stream().anyMatch(c -> "[TEST] A".equals(c.getCompanyName()));
    boolean hasSpecialChars =
        customers.stream().anyMatch(c -> c.getCompanyName().contains("Café & Restaurant"));

    assertThat(hasMinimalName).isTrue();
    assertThat(hasSpecialChars).isTrue();
  }

  @Test
  void seedComprehensiveTestData_shouldCreateNumericEdgeCases() {
    // Given
    doAnswer(
            invocation -> {
              Customer customer = invocation.getArgument(0);
              customer.setId(java.util.UUID.randomUUID());
              return null;
            })
        .when(customerRepository)
        .persist((Customer) any());

    // When
    commandService.seedComprehensiveTestData();

    // Then
    ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
    verify(customerRepository, atLeastOnce()).persist(customerCaptor.capture());

    List<Customer> customers = customerCaptor.getAllValues();

    // Verify numeric edge cases are included
    boolean hasZeroValues =
        customers.stream().anyMatch(c -> c.getRiskScore() != null && c.getRiskScore() == 0);
    boolean hasMaxRisk =
        customers.stream().anyMatch(c -> c.getRiskScore() != null && c.getRiskScore() == 100);

    assertThat(hasZeroValues).isTrue();
    assertThat(hasMaxRisk).isTrue();
  }

  @Test
  void seedComprehensiveTestData_shouldCreateAllEnumValues() {
    // Given
    doAnswer(
            invocation -> {
              Customer customer = invocation.getArgument(0);
              customer.setId(java.util.UUID.randomUUID());
              return null;
            })
        .when(customerRepository)
        .persist((Customer) any());

    // When
    commandService.seedComprehensiveTestData();

    // Then
    ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
    verify(customerRepository, atLeastOnce()).persist(customerCaptor.capture());

    List<Customer> customers = customerCaptor.getAllValues();

    // Verify all CustomerStatus values are tested
    boolean hasLeadStatus = customers.stream().anyMatch(c -> c.getStatus() == CustomerStatus.LEAD);
    boolean hasAktivStatus =
        customers.stream().anyMatch(c -> c.getStatus() == CustomerStatus.AKTIV);
    boolean hasRisikoStatus =
        customers.stream().anyMatch(c -> c.getStatus() == CustomerStatus.RISIKO);

    assertThat(hasLeadStatus).isTrue();
    assertThat(hasAktivStatus).isTrue();
    assertThat(hasRisikoStatus).isTrue();
  }

  @Test
  void seedComprehensiveTestData_withRepositoryException_shouldThrowRuntimeException() {
    // Given
    doThrow(new RuntimeException("Persistence failed"))
        .when(customerRepository)
        .persist((Customer) any());

    // When & Then
    assertThatThrownBy(() -> commandService.seedComprehensiveTestData())
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Failed to seed comprehensive test data: Persistence failed");
  }

  /**
   * Helper method to verify that no write operations are performed. Used to ensure Command methods
   * don't accidentally perform read-only operations.
   */
  private void verifyNoWriteOperations() {
    verify(customerRepository, never()).persist((Customer) any());
    verify(timelineRepository, never()).persist((CustomerTimelineEvent) any());
    verify(customerRepository, never()).delete(any());
    verify(timelineRepository, never()).delete(any());
  }
}

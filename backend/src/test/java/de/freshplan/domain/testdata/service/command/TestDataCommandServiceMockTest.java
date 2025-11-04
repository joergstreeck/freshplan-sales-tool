package de.freshplan.domain.testdata.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.repository.CustomerTimelineRepository;
import de.freshplan.domain.testdata.service.TestDataService;
import de.freshplan.domain.testdata.service.provider.CleanupResult;
import de.freshplan.domain.testdata.service.provider.SeedResult;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Mock-based tests for TestDataCommandService (migrated from @QuarkusTest).
 *
 * <p>Sprint 2.1.4: Migriert von @QuarkusTest zu Mockito (~15s Ersparnis pro Run).
 *
 * <p>Testet CQRS Command-Operationen für TestData ohne DB-Zugriff.
 *
 * @see TEST_DEBUGGING_GUIDE.md
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("TestDataCommandService Mock Tests")
class TestDataCommandServiceMockTest {

  @Mock private CustomerRepository customerRepository;

  @Mock private CustomerTimelineRepository timelineRepository;

  @InjectMocks private TestDataCommandService commandService;

  @BeforeEach
  void setUp() {
    // Each test will configure its own specific mocks
  }

  @Test
  void seedTestData_shouldCreateFiveCustomersWithTimelineEvents() {
    // Given - Simulate DB ID generation
    doAnswer(
            invocation -> {
              Customer customer = invocation.getArgument(0);
              customer.setId(java.util.UUID.randomUUID());
              return null;
            })
        .when(customerRepository)
        .persist((Customer) any());
    doNothing().when(timelineRepository).persist((CustomerTimelineEvent) any());

    // When
    SeedResult result = commandService.seedTestData();

    // Then
    assertThat(result.customersCreated()).isEqualTo(5);
    assertThat(result.eventsCreated()).isEqualTo(4);

    verify(customerRepository, times(5)).persist((Customer) any());
    verify(timelineRepository, times(4)).persist((CustomerTimelineEvent) any());

    // Verify customer characteristics
    ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
    verify(customerRepository, atLeastOnce()).persist(customerCaptor.capture());

    List<Customer> capturedCustomers = customerCaptor.getAllValues();
    assertThat(capturedCustomers).hasSize(5);
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
    doAnswer(invocation -> null).when(timelineRepository).persist((CustomerTimelineEvent) any());

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
  @Disabled(
      "Cannot mock Panache static methods (Activity.delete, CustomerContact.delete, etc.) - requires Integration Test")
  void cleanTestData_shouldDeleteEventsBeforeCustomers() {
    // Given - FK-safe delete order
    when(timelineRepository.delete("isTestData", true)).thenReturn(10L);
    when(customerRepository.delete("isTestData", true)).thenReturn(5L);

    // When
    CleanupResult result = commandService.cleanTestData();

    // Then
    assertThat(result.customersDeleted()).isEqualTo(5L);
    assertThat(result.eventsDeleted()).isEqualTo(10L);

    // Verify FK-safe delete order (events first, then customers)
    var inOrder = inOrder(timelineRepository, customerRepository);
    inOrder.verify(timelineRepository).delete("isTestData", true);
    inOrder.verify(customerRepository).delete("isTestData", true);
  }

  @Test
  @Disabled(
      "Cannot mock Panache static methods (Activity.delete, CustomerContact.delete, etc.) - requires Integration Test")
  void cleanTestData_withRepositoryException_shouldThrowRuntimeException() {
    // Given
    doThrow(new RuntimeException("Database error"))
        .when(timelineRepository)
        .delete("isTestData", true);

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
    CleanupResult result = commandService.cleanOldTestData();

    // Then
    assertThat(result.customersDeleted()).isEqualTo(8L);
    assertThat(result.eventsDeleted()).isEqualTo(15L);

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
    SeedResult result = commandService.seedAdditionalTestData();

    // Then
    assertThat(result.customersCreated()).isEqualTo(14);
    assertThat(result.eventsCreated()).isEqualTo(0);

    verify(customerRepository, times(14)).persist((Customer) any());
    verify(timelineRepository, never()).persist((CustomerTimelineEvent) any());

    // Verify customer number pattern
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

    // Verify modulo logic: i=1 -> should be PROSPECT (Sprint 2.1.7.4: LEAD removed from lifecycle)
    Customer customer1 = customers.get(0);
    assertThat(customer1.getStatus()).isEqualTo(CustomerStatus.PROSPECT);

    // Verify modulo logic: i=4 -> should be AKTIV
    Customer customer4 = customers.get(3);
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
    SeedResult result = commandService.seedComprehensiveTestData();

    // Then
    assertThat(result.customersCreated()).isGreaterThan(30);
    assertThat(result.eventsCreated()).isEqualTo(0);

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

    boolean hasLeadStatus =
        customers.stream().anyMatch(c -> c.getStatus() == CustomerStatus.PROSPECT);
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
}

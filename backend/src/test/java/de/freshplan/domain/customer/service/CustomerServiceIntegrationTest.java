package de.freshplan.domain.customer.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.*;
import de.freshplan.domain.customer.service.exception.*;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for CustomerService to measure actual code coverage. Tests real service methods
 * with database interactions.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
@DisplayName("CustomerService Integration Tests")
class CustomerServiceIntegrationTest {

  @Inject CustomerService customerService;
  @Inject CustomerRepository customerRepository;
  @Inject EntityManager entityManager;

  private CreateCustomerRequest validCreateRequest;
  private UpdateCustomerRequest validUpdateRequest;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean database before each test using native queries for consistency
    entityManager.createNativeQuery("DELETE FROM customer_timeline_events").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM customer_contacts").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM customer_locations").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM customers").executeUpdate();
    entityManager.flush();

    // Create valid request DTOs for integration tests
    validCreateRequest = new CreateCustomerRequest("Test Hotel GmbH", CustomerType.UNTERNEHMEN);

    validUpdateRequest =
        new UpdateCustomerRequest(
            "Updated Hotel GmbH",
            "Updated Trading Name",
            "GmbH",
            CustomerType.UNTERNEHMEN,
            Industry.HOTEL,
            Classification.A_KUNDE,
            null, // parentCustomerId
            CustomerHierarchyType.STANDALONE,
            CustomerStatus.AKTIV,
            CustomerLifecycleStage.GROWTH,
            new BigDecimal("100000.00"),
            new BigDecimal("80000.00"),
            PaymentTerms.NETTO_30,
            new BigDecimal("15000.00"),
            DeliveryCondition.STANDARD,
            LocalDateTime.now().minusDays(3),
            LocalDateTime.now().plusDays(14));
  }

  @Test
  @TestTransaction
  @DisplayName("Should create customer with valid data")
  void createCustomer_withValidData_shouldCreateCustomer() {
    // When
    CustomerResponse result = customerService.createCustomer(validCreateRequest, "testuser");

    // Then
    assertThat(result).isNotNull();
    assertThat(result.companyName()).isEqualTo("Test Hotel GmbH");
    assertThat(result.customerType()).isEqualTo(CustomerType.UNTERNEHMEN);
    assertThat(result.status()).isEqualTo(CustomerStatus.LEAD);
    assertThat(result.lifecycleStage()).isEqualTo(CustomerLifecycleStage.ACQUISITION);
    assertThat(result.customerNumber()).startsWith("KD-");
  }

  @Test
  @TestTransaction
  @DisplayName("Should throw exception for duplicate customer")
  void createCustomer_withDuplicateCompanyName_shouldThrowException() {
    // Given - Create first customer with specific name
    CreateCustomerRequest firstRequest =
        new CreateCustomerRequest("Unique Hotel Name", CustomerType.UNTERNEHMEN);
    customerService.createCustomer(firstRequest, "testuser");

    // When & Then - Try to create similar company name (triggers LIKE duplicate detection)
    CreateCustomerRequest duplicateRequest =
        new CreateCustomerRequest("unique hotel name", CustomerType.UNTERNEHMEN);

    assertThatThrownBy(() -> customerService.createCustomer(duplicateRequest, "testuser"))
        .isInstanceOf(CustomerAlreadyExistsException.class)
        .hasMessageContaining("Unique Hotel Name");
  }

  @Test
  @TestTransaction
  @DisplayName("Should create customer with minimal data")
  void createCustomer_withMinimalData_shouldCreateCustomer() {
    // Given
    CreateCustomerRequest minimalRequest =
        new CreateCustomerRequest("Minimal Company", CustomerType.UNTERNEHMEN);

    // When
    CustomerResponse result = customerService.createCustomer(minimalRequest, "testuser");

    // Then
    assertThat(result).isNotNull();
    assertThat(result.companyName()).isEqualTo("Minimal Company");
    assertThat(result.customerType()).isEqualTo(CustomerType.UNTERNEHMEN);
    assertThat(result.status()).isEqualTo(CustomerStatus.LEAD);
    assertThat(result.lifecycleStage()).isEqualTo(CustomerLifecycleStage.ACQUISITION);
  }

  @Test
  @TestTransaction
  @DisplayName("Should retrieve existing customer")
  void getCustomer_withExistingId_shouldReturnCustomer() {
    // Given
    CustomerResponse created = customerService.createCustomer(validCreateRequest, "testuser");
    UUID customerId = UUID.fromString(created.id());

    // When
    CustomerResponse result = customerService.getCustomer(customerId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(created.id());
    assertThat(result.companyName()).isEqualTo("Test Hotel GmbH");
  }

  @Test
  @TestTransaction
  @DisplayName("Should throw exception for non-existing customer")
  void getCustomer_withNonExistingId_shouldThrowException() {
    // Given
    UUID nonExistingId = UUID.randomUUID();

    // When & Then
    assertThatThrownBy(() -> customerService.getCustomer(nonExistingId))
        .isInstanceOf(CustomerNotFoundException.class)
        .hasMessageContaining(nonExistingId.toString());
  }

  @Test
  @TestTransaction
  @DisplayName("Should retrieve all customers")
  void getAllCustomers_shouldReturnAllCustomers() {
    // Given
    customerService.createCustomer(validCreateRequest, "testuser");
    customerService.createCustomer(
        new CreateCustomerRequest("Second Hotel", CustomerType.UNTERNEHMEN), "testuser");

    // When
    CustomerListResponse result = customerService.getAllCustomers(0, 10);

    // Then
    assertThat(result.content()).hasSize(2);
    assertThat(result.totalElements()).isEqualTo(2);
    assertThat(result.totalPages()).isEqualTo(1);
  }

  @Test
  @TestTransaction
  @DisplayName("Should update existing customer")
  void updateCustomer_withValidData_shouldUpdateCustomer() {
    // Given
    CustomerResponse created = customerService.createCustomer(validCreateRequest, "testuser");
    UUID customerId = UUID.fromString(created.id());

    // When
    CustomerResponse result =
        customerService.updateCustomer(customerId, validUpdateRequest, "testuser");

    // Then
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(created.id());
    assertThat(result.companyName()).isEqualTo("Updated Hotel GmbH");
    assertThat(result.tradingName()).isEqualTo("Updated Trading Name");
    assertThat(result.status()).isEqualTo(CustomerStatus.AKTIV);
    assertThat(result.lifecycleStage()).isEqualTo(CustomerLifecycleStage.GROWTH);
  }

  @Test
  @TestTransaction
  @DisplayName("Should throw exception for non-existing customer")
  void updateCustomer_withNonExistingId_shouldThrowException() {
    // Given
    UUID nonExistingId = UUID.randomUUID();

    // When & Then
    assertThatThrownBy(
            () -> customerService.updateCustomer(nonExistingId, validUpdateRequest, "testuser"))
        .isInstanceOf(CustomerNotFoundException.class)
        .hasMessageContaining(nonExistingId.toString());
  }

  @Test
  @TestTransaction
  @DisplayName("Should delete existing customer")
  void deleteCustomer_withExistingId_shouldDeleteCustomer() {
    // Given
    CustomerResponse created = customerService.createCustomer(validCreateRequest, "testuser");
    UUID customerId = UUID.fromString(created.id());

    // When
    customerService.deleteCustomer(customerId, "testuser", "Test deletion");

    // Then
    assertThatThrownBy(() -> customerService.getCustomer(customerId))
        .isInstanceOf(CustomerNotFoundException.class);
  }

  @Test
  @TestTransaction
  @DisplayName("Should throw exception for non-existing customer")
  void deleteCustomer_withNonExistingId_shouldThrowException() {
    // Given
    UUID nonExistingId = UUID.randomUUID();

    // When & Then
    assertThatThrownBy(
            () -> customerService.deleteCustomer(nonExistingId, "testuser", "Test deletion"))
        .isInstanceOf(CustomerNotFoundException.class)
        .hasMessageContaining(nonExistingId.toString());
  }

  @Test
  @TestTransaction
  @DisplayName("Should change customer status")
  void changeStatus_withValidData_shouldChangeStatus() {
    // Given
    CustomerResponse created = customerService.createCustomer(validCreateRequest, "testuser");
    UUID customerId = UUID.fromString(created.id());
    // When
    CustomerResponse result =
        customerService.changeStatus(customerId, CustomerStatus.AKTIV, "testuser");

    // Then
    assertThat(result).isNotNull();
    assertThat(result.status()).isEqualTo(CustomerStatus.AKTIV);
  }

  @Test
  @TestTransaction
  @DisplayName("Should throw exception for non-existing customer")
  void changeStatus_withNonExistingId_shouldThrowException() {
    // Given
    UUID nonExistingId = UUID.randomUUID();

    // When & Then
    assertThatThrownBy(
            () -> customerService.changeStatus(nonExistingId, CustomerStatus.AKTIV, "testuser"))
        .isInstanceOf(CustomerNotFoundException.class)
        .hasMessageContaining(nonExistingId.toString());
  }

  @Test
  @TestTransaction
  @DisplayName("Should restore deleted customer")
  void restoreCustomer_withDeletedCustomer_shouldRestoreCustomer() {
    // Given
    CustomerResponse created = customerService.createCustomer(validCreateRequest, "testuser");
    UUID customerId = UUID.fromString(created.id());
    customerService.deleteCustomer(customerId, "testuser", "Test deletion");

    // When
    CustomerResponse result = customerService.restoreCustomer(customerId, "testuser");

    // Then
    assertThat(result).isNotNull();
    assertThat(result.isDeleted()).isFalse();
    assertThat(result.deletedAt()).isNull();
    assertThat(result.deletedBy()).isNull();
  }

  @Test
  @TestTransaction
  @DisplayName("Should throw exception for non-existing customer")
  void restoreCustomer_withNonExistingId_shouldThrowException() {
    // Given
    UUID nonExistingId = UUID.randomUUID();

    // When & Then
    assertThatThrownBy(() -> customerService.restoreCustomer(nonExistingId, "testuser"))
        .isInstanceOf(CustomerNotFoundException.class)
        .hasMessageContaining(nonExistingId.toString());
  }

  @Test
  @TestTransaction
  @DisplayName("Should retrieve all customers with correct content")
  void getAllCustomers_shouldReturnAllCreatedCustomers() {
    // Given
    customerService.createCustomer(validCreateRequest, "testuser");
    customerService.createCustomer(
        new CreateCustomerRequest("Other Company", CustomerType.UNTERNEHMEN), "testuser");

    // When
    CustomerListResponse result = customerService.getAllCustomers(0, 10);

    // Then
    assertThat(result.content()).hasSize(2);
    assertThat(result.content())
        .extracting(CustomerResponse::companyName)
        .containsExactlyInAnyOrder("Test Hotel GmbH", "Other Company");
  }

  @Test
  @TestTransaction
  @DisplayName("Should validate customer data")
  void createCustomer_withInvalidData_shouldThrowException() {
    // Given
    CreateCustomerRequest invalidRequest = new CreateCustomerRequest("", CustomerType.UNTERNEHMEN);

    // When & Then
    assertThatThrownBy(() -> customerService.createCustomer(invalidRequest, "testuser"))
        .isInstanceOf(jakarta.validation.ConstraintViolationException.class)
        .hasMessageContaining("Company name is required");
  }

  @Test
  @TestTransaction
  @DisplayName("Should validate customer type")
  void createCustomer_withNullType_shouldThrowException() {
    // Given
    CreateCustomerRequest invalidRequest = new CreateCustomerRequest("Test Company", null);

    // When & Then
    assertThatThrownBy(() -> customerService.createCustomer(invalidRequest, "testuser"))
        .isInstanceOf(jakarta.validation.ConstraintViolationException.class)
        .hasMessageContaining("Customer type is required");
  }

  @Test
  @TestTransaction
  @DisplayName("Should update risk scores for all customers")
  void updateRiskScores_shouldUpdateAllCustomers() {
    // Given
    CustomerResponse c1 =
        customerService.createCustomer(
            new CreateCustomerRequest("Low Risk", CustomerType.UNTERNEHMEN), "testuser");
    CustomerResponse c2 =
        customerService.createCustomer(
            new CreateCustomerRequest("High Risk", CustomerType.UNTERNEHMEN), "testuser");

    // Manually reset risk scores to a known state to test the update logic
    Customer customer1 = customerRepository.findByIdOptional(UUID.fromString(c1.id())).get();
    customer1.setRiskScore(0);
    Customer customer2 = customerRepository.findByIdOptional(UUID.fromString(c2.id())).get();
    customer2.setRiskScore(0);
    customerRepository.flush();

    // When
    customerService.updateAllRiskScores();
    customerRepository.flush(); // Ensure changes are persisted

    // Then
    Customer updatedCustomer1 = customerRepository.findByIdOptional(UUID.fromString(c1.id())).get();
    Customer updatedCustomer2 = customerRepository.findByIdOptional(UUID.fromString(c2.id())).get();

    assertThat(updatedCustomer1.getRiskScore()).isGreaterThan(0);
    assertThat(updatedCustomer2.getRiskScore()).isGreaterThan(0);
  }

  @Test
  @TestTransaction
  @DisplayName("Should find customers requiring follow-up")
  void findCustomersRequiringFollowUp_shouldReturnOverdueCustomers() {
    // Given
    CustomerResponse created =
        customerService.createCustomer(
            new CreateCustomerRequest("Test Corp", CustomerType.UNTERNEHMEN), "testuser");

    // Manually set the follow-up date to the past to create the test condition
    Customer customer = customerRepository.findByIdOptional(UUID.fromString(created.id())).get();
    customer.setNextFollowUpDate(LocalDateTime.now().minusDays(1));
    customerRepository.flush();

    // When
    CustomerListResponse result = customerService.getOverdueFollowUps(0, 10);

    // Then
    assertThat(result.content()).hasSize(1);
    assertThat(result.content().get(0).id()).isEqualTo(created.id());
  }

  @Test
  @TestTransaction
  @DisplayName("Should get customer dashboard data")
  void getDashboardData_shouldReturnDashboardData() {
    // Given
    customerService.createCustomer(validCreateRequest, "testuser");

    // When
    CustomerDashboardResponse result = customerService.getDashboardData();

    // Then
    assertThat(result).isNotNull();
    assertThat(result.totalCustomers()).isEqualTo(1);
    assertThat(result.activeCustomers()).isEqualTo(0); // LEAD status is not AKTIV
    assertThat(result.newThisMonth()).isEqualTo(1);
    // Basic validation - check that maps are not null
    assertThat(result.customersByStatus()).isNotNull();
    assertThat(result.customersByLifecycle()).isNotNull();
  }
}

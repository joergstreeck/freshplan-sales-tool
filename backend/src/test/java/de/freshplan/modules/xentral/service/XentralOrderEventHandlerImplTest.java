package de.freshplan.modules.xentral.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.test.builders.CustomerTestDataFactory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Enterprise-Level Integration Tests for XentralOrderEventHandlerImpl
 *
 * <p>Sprint 2.1.7.2 - D7: Xentral Webhook Integration
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>✅ Validation (null/blank parameters)
 *   <li>✅ PROSPECT auto-activation logic
 *   <li>✅ AKTIV customer lastOrderDate update
 *   <li>✅ Error handling (customer not found, unexpected status)
 *   <li>✅ Integration with real CustomerService
 * </ul>
 *
 * <p>NOTE: These are integration tests using real database transactions. Test data is cleaned up
 * after each test.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@Tag("integration")
class XentralOrderEventHandlerImplTest {

  @Inject XentralOrderEventHandler handler;

  @Inject CustomerRepository customerRepository;

  private Customer testProspectCustomer;
  private Customer testActiveCustomer;

  @BeforeEach
  @Transactional
  void setUp() {
    // Create PROSPECT customer for activation tests
    testProspectCustomer =
        CustomerTestDataFactory.builder()
            .withCompanyName("Test Prospect Webhook GmbH")
            .withCustomerNumber("KD-TEST-WEBHOOK-001")
            .asTestData(true)
            .build();
    testProspectCustomer.setStatus(CustomerStatus.PROSPECT);
    testProspectCustomer.setXentralCustomerId("XENT-CUST-WEBHOOK-001");
    customerRepository.persist(testProspectCustomer);

    // Create AKTIV customer for lastOrderDate update tests
    testActiveCustomer =
        CustomerTestDataFactory.builder()
            .withCompanyName("Test Active Webhook GmbH")
            .withCustomerNumber("KD-TEST-WEBHOOK-002")
            .asTestData(true)
            .build();
    testActiveCustomer.setStatus(CustomerStatus.AKTIV);
    testActiveCustomer.setXentralCustomerId("XENT-CUST-WEBHOOK-002");
    testActiveCustomer.setLastOrderDate(LocalDate.now().minusDays(30));
    customerRepository.persist(testActiveCustomer);
  }

  @AfterEach
  @Transactional
  void tearDown() {
    // Cleanup test data
    if (testProspectCustomer != null && testProspectCustomer.getId() != null) {
      customerRepository.delete(testProspectCustomer);
    }
    if (testActiveCustomer != null && testActiveCustomer.getId() != null) {
      customerRepository.delete(testActiveCustomer);
    }
  }

  // ========== VALIDATION TESTS ==========

  @Test
  void handleOrderDelivered_withNullXentralCustomerId_shouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> handler.handleOrderDelivered(null, "ORD-001", LocalDate.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("xentralCustomerId cannot be null or empty");
  }

  @Test
  void handleOrderDelivered_withBlankXentralCustomerId_shouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> handler.handleOrderDelivered("  ", "ORD-001", LocalDate.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("xentralCustomerId cannot be null or empty");
  }

  @Test
  void handleOrderDelivered_withNullOrderNumber_shouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> handler.handleOrderDelivered("XENT-CUST-001", null, LocalDate.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("orderNumber cannot be null or empty");
  }

  @Test
  void handleOrderDelivered_withBlankOrderNumber_shouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> handler.handleOrderDelivered("XENT-CUST-001", "  ", LocalDate.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("orderNumber cannot be null or empty");
  }

  @Test
  void handleOrderDelivered_withNullDeliveryDate_shouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> handler.handleOrderDelivered("XENT-CUST-001", "ORD-001", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("deliveryDate cannot be null");
  }

  // ========== CUSTOMER NOT FOUND TESTS ==========

  @Test
  void handleOrderDelivered_withNonExistentCustomer_shouldThrowException() {
    // When/Then
    assertThatThrownBy(
            () -> handler.handleOrderDelivered("XENT-CUST-NONEXISTENT", "ORD-001", LocalDate.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Customer not found");
  }

  // ========== PROSPECT ACTIVATION TESTS ==========

  @Test
  @Transactional
  void handleOrderDelivered_withProspectCustomer_shouldActivateCustomer() {
    // When
    handler.handleOrderDelivered("XENT-CUST-WEBHOOK-001", "ORD-2025-001", LocalDate.now());

    // Then - reload customer to see changes
    Customer reloaded =
        customerRepository
            .findByXentralCustomerId("XENT-CUST-WEBHOOK-001")
            .orElseThrow();

    assertThat(reloaded.getStatus()).isEqualTo(CustomerStatus.AKTIV);
  }

  // ========== AKTIV CUSTOMER TESTS ==========

  @Test
  @Transactional
  void handleOrderDelivered_withActiveCustomer_shouldUpdateLastOrderDate() {
    // Given
    LocalDate deliveryDate = LocalDate.now();
    LocalDate oldDate = testActiveCustomer.getLastOrderDate();

    // When
    handler.handleOrderDelivered("XENT-CUST-WEBHOOK-002", "ORD-2025-002", deliveryDate);

    // Then - reload customer to see changes
    Customer reloaded =
        customerRepository
            .findByXentralCustomerId("XENT-CUST-WEBHOOK-002")
            .orElseThrow();

    assertThat(reloaded.getLastOrderDate()).isEqualTo(deliveryDate);
    assertThat(reloaded.getLastOrderDate()).isNotEqualTo(oldDate);
    assertThat(reloaded.getStatus()).isEqualTo(CustomerStatus.AKTIV); // Still AKTIV
  }
}

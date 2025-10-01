package de.freshplan.api.resources;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.service.CustomerService;
import de.freshplan.domain.customer.service.command.CustomerCommandService;
import de.freshplan.domain.customer.service.dto.*;
import de.freshplan.domain.customer.service.query.CustomerQueryService;
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Tag;

/**
 * Pure unit test for CustomerResource delegation logic using reflection. Tests feature flag
 * behavior without Quarkus container.
 *
 * <p>This test validates that the correct service (legacy vs CQRS) is called based on feature flag
 * settings. It uses reflection to bypass security dependencies that cannot be mocked.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerResource Delegation Unit Tests")
@Tag("unit")
class CustomerResourceDelegationTest {

  @Mock private CustomerService customerService;

  @Mock private CustomerCommandService commandService;

  @Mock private CustomerQueryService queryService;

  private CustomerResource customerResource;
  private UUID testId;

  @BeforeEach
  void setUp() throws Exception {
    testId = UUID.randomUUID();

    // Create resource and inject services via reflection
    customerResource = new CustomerResource();
    injectField(customerResource, "customerService", customerService);
    injectField(customerResource, "commandService", commandService);
    injectField(customerResource, "queryService", queryService);
  }

  // ========== Query Delegation Tests ==========

  @Test
  @DisplayName("Should delegate getCustomer to legacy service when CQRS disabled")
  void testGetCustomer_LegacyMode() {
    // Given
    customerResource.cqrsEnabled = false;
    when(customerService.getCustomer(testId)).thenReturn(null);

    // When
    customerResource.getCustomer(testId);

    // Then
    verify(customerService).getCustomer(testId);
    verify(queryService, never()).getCustomer(any());
  }

  @Test
  @DisplayName("Should delegate getCustomer to QueryService when CQRS enabled")
  void testGetCustomer_CQRSMode() {
    // Given
    customerResource.cqrsEnabled = true;
    when(queryService.getCustomer(testId)).thenReturn(null);

    // When
    customerResource.getCustomer(testId);

    // Then
    verify(queryService).getCustomer(testId);
    verify(customerService, never()).getCustomer(any());
  }

  @Test
  @DisplayName("Should delegate dashboard to legacy service when CQRS disabled")
  void testDashboard_LegacyMode() {
    // Given
    customerResource.cqrsEnabled = false;
    when(customerService.getDashboardData()).thenReturn(null);

    // When
    customerResource.getDashboardData();

    // Then
    verify(customerService).getDashboardData();
    verify(queryService, never()).getDashboardData();
  }

  @Test
  @DisplayName("Should delegate dashboard to QueryService when CQRS enabled")
  void testDashboard_CQRSMode() {
    // Given
    customerResource.cqrsEnabled = true;
    when(queryService.getDashboardData()).thenReturn(null);

    // When
    customerResource.getDashboardData();

    // Then
    verify(queryService).getDashboardData();
    verify(customerService, never()).getDashboardData();
  }

  // ========== List Operations with Per-Use-Case Flags ==========

  @Test
  @DisplayName("Should use legacy service for list when both flags disabled")
  void testGetAllCustomers_BothFlagsDisabled() {
    // Given
    customerResource.cqrsEnabled = false;
    customerResource.customersListCqrsEnabled = false;
    when(customerService.getAllCustomers(anyInt(), anyInt())).thenReturn(null);

    // When
    customerResource.getAllCustomers(0, 20, null, null);

    // Then
    verify(customerService).getAllCustomers(0, 20);
    verify(queryService, never()).getAllCustomers(anyInt(), anyInt());
  }

  @Test
  @DisplayName("Should use legacy service when global CQRS disabled (list flag ignored)")
  void testGetAllCustomers_GlobalDisabled_ListEnabled() {
    // Given
    customerResource.cqrsEnabled = false;
    customerResource.customersListCqrsEnabled = true; // Ignored when global is disabled
    when(customerService.getAllCustomers(anyInt(), anyInt())).thenReturn(null);

    // When
    customerResource.getAllCustomers(0, 20, null, null);

    // Then
    verify(customerService).getAllCustomers(0, 20);
    verify(queryService, never()).getAllCustomers(anyInt(), anyInt());
  }

  @Test
  @DisplayName(
      "Should use legacy for list when per-use-case flag disabled (performance optimization)")
  void testGetAllCustomers_GlobalEnabled_ListDisabled() {
    // Given - Key configuration for performance mitigation
    customerResource.cqrsEnabled = true;
    customerResource.customersListCqrsEnabled = false;
    when(customerService.getAllCustomers(anyInt(), anyInt())).thenReturn(null);

    // When
    customerResource.getAllCustomers(0, 20, null, null);

    // Then - Uses legacy for list while other operations use CQRS
    verify(customerService).getAllCustomers(0, 20);
    verify(queryService, never()).getAllCustomers(anyInt(), anyInt());
  }

  @Test
  @DisplayName("Should use CQRS for list when both flags enabled")
  void testGetAllCustomers_BothFlagsEnabled() {
    // Given
    customerResource.cqrsEnabled = true;
    customerResource.customersListCqrsEnabled = true;
    when(queryService.getAllCustomers(anyInt(), anyInt())).thenReturn(null);

    // When
    customerResource.getAllCustomers(0, 20, null, null);

    // Then
    verify(queryService).getAllCustomers(0, 20);
    verify(customerService, never()).getAllCustomers(anyInt(), anyInt());
  }

  @Test
  @DisplayName("Should not affect single customer query with list flag")
  void testGetCustomer_NotAffectedByListFlag() {
    // Given - List flag should not affect single customer retrieval
    customerResource.cqrsEnabled = true;
    customerResource.customersListCqrsEnabled = false; // List disabled but CQRS enabled
    when(queryService.getCustomer(testId)).thenReturn(null);

    // When
    customerResource.getCustomer(testId);

    // Then - Single customer still uses CQRS
    verify(queryService).getCustomer(testId);
    verify(customerService, never()).getCustomer(any());
  }

  @Test
  @DisplayName("Should respect per-use-case flag for list with status filter")
  void testGetAllCustomersWithStatus_RespectsListFlag() {
    // Given
    customerResource.cqrsEnabled = true;
    customerResource.customersListCqrsEnabled = false;
    when(customerService.getCustomersByStatus(any(), anyInt(), anyInt())).thenReturn(null);

    // When
    customerResource.getAllCustomers(0, 20, CustomerStatus.AKTIV, null);

    // Then
    verify(customerService).getCustomersByStatus(CustomerStatus.AKTIV, 0, 20);
    verify(queryService, never()).getCustomersByStatus(any(), anyInt(), anyInt());
  }

  // ========== Helper Methods ==========

  private void injectField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}

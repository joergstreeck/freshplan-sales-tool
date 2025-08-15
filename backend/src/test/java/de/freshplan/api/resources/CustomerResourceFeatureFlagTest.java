package de.freshplan.api.resources;

import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.service.CustomerService;
import de.freshplan.domain.customer.service.command.CustomerCommandService;
import de.freshplan.domain.customer.service.query.CustomerQueryService;
import de.freshplan.domain.customer.service.dto.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Feature flag tests for CustomerResource.
 * Verifies that the correct service is called based on the feature flag setting.
 * 
 * This is a unit test that ensures the delegation logic works correctly.
 * Tests both global CQRS flag and per-use-case flags.
 */
@QuarkusTest
@TestSecurity(user = "testuser", roles = {"admin", "manager", "sales"})
@DisplayName("CustomerResource Feature Flag Tests")
class CustomerResourceFeatureFlagTest {

    @Inject
    CustomerResource customerResource;
    
    @InjectMock
    CustomerService customerService;
    
    @InjectMock
    CustomerCommandService commandService;
    
    @InjectMock
    CustomerQueryService queryService;
    
    private UUID testId;
    
    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        // Reset all mocks before each test
        Mockito.reset(customerService, commandService, queryService);
    }
    
    @Test
    @DisplayName("When CQRS disabled, should use legacy CustomerService for queries")
    void testLegacyMode_UsesCustomerService() {
        // Given
        customerResource.cqrsEnabled = false;
        CustomerResponse mockResponse = createMockCustomerResponse();
        when(customerService.getCustomer(testId)).thenReturn(mockResponse);
        
        // When
        customerResource.getCustomer(testId);
        
        // Then
        verify(customerService, times(1)).getCustomer(testId);
        verify(queryService, never()).getCustomer(any());
    }
    
    @Test
    @DisplayName("When CQRS enabled, should use QueryService for queries")
    void testCQRSMode_UsesQueryService() {
        // Given
        customerResource.cqrsEnabled = true;
        CustomerResponse mockResponse = createMockCustomerResponse();
        when(queryService.getCustomer(testId)).thenReturn(mockResponse);
        
        // When
        customerResource.getCustomer(testId);
        
        // Then
        verify(queryService, times(1)).getCustomer(testId);
        verify(customerService, never()).getCustomer(any());
    }
    
    @Test
    @DisplayName("When CQRS disabled, should use legacy CustomerService for commands")
    void testLegacyMode_UsesCustomerServiceForCommands() {
        // Given
        customerResource.cqrsEnabled = false;
        doNothing().when(customerService).deleteCustomer(any(), any(), any());
        
        // When
        customerResource.deleteCustomer(testId, "Test reason");
        
        // Then
        verify(customerService, times(1)).deleteCustomer(eq(testId), any(), eq("Test reason"));
        verify(commandService, never()).deleteCustomer(any(), any(), any());
    }
    
    @Test
    @DisplayName("When CQRS enabled, should use CommandService for commands")
    void testCQRSMode_UsesCommandService() {
        // Given
        customerResource.cqrsEnabled = true;
        doNothing().when(commandService).deleteCustomer(any(), any(), any());
        
        // When
        customerResource.deleteCustomer(testId, "Test reason");
        
        // Then
        verify(commandService, times(1)).deleteCustomer(eq(testId), any(), eq("Test reason"));
        verify(customerService, never()).deleteCustomer(any(), any(), any());
    }
    
    @Test
    @DisplayName("Dashboard should delegate to correct service based on flag")
    void testDashboard_DelegatesToCorrectService() {
        // Test with CQRS disabled
        customerResource.cqrsEnabled = false;
        CustomerDashboardResponse mockDashboard = createMockDashboardResponse();
        when(customerService.getDashboardData()).thenReturn(mockDashboard);
        
        customerResource.getDashboardData();
        verify(customerService, times(1)).getDashboardData();
        verify(queryService, never()).getDashboardData();
        
        // Reset and test with CQRS enabled
        Mockito.reset(customerService, queryService);
        customerResource.cqrsEnabled = true;
        when(queryService.getDashboardData()).thenReturn(mockDashboard);
        
        customerResource.getDashboardData();
        verify(queryService, times(1)).getDashboardData();
        verify(customerService, never()).getDashboardData();
    }
    
    @Test
    @DisplayName("Status change should delegate to correct service based on flag")
    void testStatusChange_DelegatesToCorrectService() {
        // Test with CQRS disabled
        customerResource.cqrsEnabled = false;
        CustomerResponse mockResponse = createMockCustomerResponse();
        ChangeStatusRequest request = new ChangeStatusRequest(CustomerStatus.INAKTIV);
        when(customerService.changeStatus(any(), any(), any())).thenReturn(mockResponse);
        
        customerResource.changeCustomerStatus(testId, request);
        verify(customerService, times(1)).changeStatus(eq(testId), eq(CustomerStatus.INAKTIV), any());
        verify(commandService, never()).changeStatus(any(), any(), any());
        
        // Reset and test with CQRS enabled
        Mockito.reset(customerService, commandService);
        customerResource.cqrsEnabled = true;
        when(commandService.changeStatus(any(), any(), any())).thenReturn(mockResponse);
        
        customerResource.changeCustomerStatus(testId, request);
        verify(commandService, times(1)).changeStatus(eq(testId), eq(CustomerStatus.INAKTIV), any());
        verify(customerService, never()).changeStatus(any(), any(), any());
    }
    
    // ========== Per-Use-Case Flag Tests for List Operations ==========
    
    @Test
    @DisplayName("When both flags disabled, getAllCustomers should use legacy service")
    void testGetAllCustomers_BothFlagsDisabled_UsesLegacy() {
        // Given
        customerResource.cqrsEnabled = false;
        customerResource.customersListCqrsEnabled = false;
        CustomerListResponse mockResponse = createMockListResponse();
        when(customerService.getAllCustomers(anyInt(), anyInt())).thenReturn(mockResponse);
        
        // When
        customerResource.getAllCustomers(0, 20, null, null);
        
        // Then
        verify(customerService, times(1)).getAllCustomers(0, 20);
        verify(queryService, never()).getAllCustomers(anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("When CQRS disabled but list enabled, getAllCustomers should still use legacy")
    void testGetAllCustomers_CqrsDisabledListEnabled_UsesLegacy() {
        // Given - list flag has no effect when global CQRS is disabled
        customerResource.cqrsEnabled = false;
        customerResource.customersListCqrsEnabled = true;
        CustomerListResponse mockResponse = createMockListResponse();
        when(customerService.getAllCustomers(anyInt(), anyInt())).thenReturn(mockResponse);
        
        // When
        customerResource.getAllCustomers(0, 20, null, null);
        
        // Then
        verify(customerService, times(1)).getAllCustomers(0, 20);
        verify(queryService, never()).getAllCustomers(anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("When CQRS enabled but list disabled, getAllCustomers should use legacy")
    void testGetAllCustomers_CqrsEnabledListDisabled_UsesLegacy() {
        // Given - this is the key configuration for performance mitigation
        customerResource.cqrsEnabled = true;
        customerResource.customersListCqrsEnabled = false;
        CustomerListResponse mockResponse = createMockListResponse();
        when(customerService.getAllCustomers(anyInt(), anyInt())).thenReturn(mockResponse);
        
        // When
        customerResource.getAllCustomers(0, 20, null, null);
        
        // Then - list uses legacy while other operations would use CQRS
        verify(customerService, times(1)).getAllCustomers(0, 20);
        verify(queryService, never()).getAllCustomers(anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("When both flags enabled, getAllCustomers should use CQRS")
    void testGetAllCustomers_BothFlagsEnabled_UsesCQRS() {
        // Given
        customerResource.cqrsEnabled = true;
        customerResource.customersListCqrsEnabled = true;
        CustomerListResponse mockResponse = createMockListResponse();
        when(queryService.getAllCustomers(anyInt(), anyInt())).thenReturn(mockResponse);
        
        // When
        customerResource.getAllCustomers(0, 20, null, null);
        
        // Then
        verify(queryService, times(1)).getAllCustomers(0, 20);
        verify(customerService, never()).getAllCustomers(anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("Per-use-case flag should not affect getCustomer (single)")
    void testGetCustomer_NotAffectedByListFlag() {
        // Given - list flag should not affect single customer retrieval
        customerResource.cqrsEnabled = true;
        customerResource.customersListCqrsEnabled = false; // List disabled but CQRS enabled
        CustomerResponse mockResponse = createMockCustomerResponse();
        when(queryService.getCustomer(testId)).thenReturn(mockResponse);
        
        // When
        customerResource.getCustomer(testId);
        
        // Then - single customer still uses CQRS
        verify(queryService, times(1)).getCustomer(testId);
        verify(customerService, never()).getCustomer(any());
    }
    
    @Test
    @DisplayName("List filtering with status should respect per-use-case flag")
    void testGetAllCustomersWithStatus_RespectsListFlag() {
        // Given
        customerResource.cqrsEnabled = true;
        customerResource.customersListCqrsEnabled = false;
        CustomerListResponse mockResponse = createMockListResponse();
        when(customerService.getCustomersByStatus(any(), anyInt(), anyInt())).thenReturn(mockResponse);
        
        // When
        customerResource.getAllCustomers(0, 20, CustomerStatus.AKTIV, null);
        
        // Then
        verify(customerService, times(1)).getCustomersByStatus(CustomerStatus.AKTIV, 0, 20);
        verify(queryService, never()).getCustomersByStatus(any(), anyInt(), anyInt());
    }
    
    // Helper methods to create mock responses
    private CustomerResponse createMockCustomerResponse() {
        // Use null for all parameters - we're only testing delegation, not data
        return null; // Mock will return this, but we don't verify the content
    }
    
    private CustomerDashboardResponse createMockDashboardResponse() {
        // Use null for simplicity - we're only testing delegation
        return null; // Mock will return this, but we don't verify the content
    }
    
    private CustomerListResponse createMockListResponse() {
        // Use null for simplicity - we're only testing delegation
        return null; // Mock will return this, but we don't verify the content
    }
}
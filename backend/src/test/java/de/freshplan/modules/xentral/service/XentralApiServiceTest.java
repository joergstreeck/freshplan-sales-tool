package de.freshplan.modules.xentral.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.freshplan.modules.xentral.client.MockXentralApiClient;
import de.freshplan.modules.xentral.dto.XentralCustomerDTO;
import de.freshplan.modules.xentral.dto.XentralEmployeeDTO;
import de.freshplan.modules.xentral.dto.XentralInvoiceDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for XentralApiService.
 *
 * <p>Sprint: 2.1.7.2 - D2e Real API Adapter Tests
 */
@QuarkusTest
class XentralApiServiceTest {

  @Inject XentralApiService service;

  @InjectMock MockXentralApiClient mockClient;

  @InjectMock XentralV1V2ApiAdapter realAdapter;

  @Nested
  @DisplayName("Mock Mode Tests")
  @TestProfile(MockModeProfile.class)
  class MockModeTests {

    @Test
    @DisplayName("getCustomersBySalesRep() - delegates to mock client in mock mode")
    void testGetCustomersBySalesRep_MockMode() {
      // Given
      var mockCustomer =
          new XentralCustomerDTO(
              "MOCK-001",
              "Mock Corp",
              "mock@example.com",
              "+49-123",
              new BigDecimal("10000.00"),
              30,
              LocalDate.now(),
              "SALES-001");

      when(mockClient.getCustomersBySalesRep("SALES-001")).thenReturn(List.of(mockCustomer));

      // When
      List<XentralCustomerDTO> customers = service.getCustomersBySalesRep("SALES-001");

      // Then
      assertThat(customers).hasSize(1);
      assertThat(customers.get(0).xentralId()).isEqualTo("MOCK-001");

      verify(mockClient, times(1)).getCustomersBySalesRep("SALES-001");
      verify(realAdapter, never()).getCustomersBySalesRep(anyString());
    }

    @Test
    @DisplayName("getCustomerById() - delegates to mock client in mock mode")
    void testGetCustomerById_MockMode() {
      // Given
      var mockCustomer =
          new XentralCustomerDTO(
              "MOCK-123",
              "Mock GmbH",
              "test@mock.com",
              null,
              null,
              null,
              null,
              null);

      when(mockClient.getCustomerById("MOCK-123")).thenReturn(mockCustomer);

      // When
      XentralCustomerDTO customer = service.getCustomerById("MOCK-123");

      // Then
      assertThat(customer).isNotNull();
      assertThat(customer.xentralId()).isEqualTo("MOCK-123");

      verify(mockClient, times(1)).getCustomerById("MOCK-123");
      verify(realAdapter, never()).getCustomerById(anyString());
    }

    @Test
    @DisplayName("getInvoicesByCustomer() - delegates to mock client in mock mode")
    void testGetInvoicesByCustomer_MockMode() {
      // Given
      var mockInvoice =
          new XentralInvoiceDTO(
              "INV-001",
              "RE-MOCK-001",
              "CUST-123",
              new BigDecimal("1000.00"),
              LocalDate.now(),
              LocalDate.now().plusDays(30),
              null,
              "open");

      when(mockClient.getInvoicesByCustomer("CUST-123")).thenReturn(List.of(mockInvoice));

      // When
      List<XentralInvoiceDTO> invoices = service.getInvoicesByCustomer("CUST-123");

      // Then
      assertThat(invoices).hasSize(1);
      assertThat(invoices.get(0).invoiceId()).isEqualTo("INV-001");

      verify(mockClient, times(1)).getInvoicesByCustomer("CUST-123");
      verify(realAdapter, never()).getInvoicesByCustomer(anyString());
    }

    @Test
    @DisplayName("getAllSalesReps() - delegates to mock client in mock mode")
    void testGetAllSalesReps_MockMode() {
      // Given
      var mockSalesRep =
          new XentralEmployeeDTO("SALES-001", "Mock", "User", "mock@example.com", "sales");

      when(mockClient.getAllSalesReps()).thenReturn(List.of(mockSalesRep));

      // When
      List<XentralEmployeeDTO> salesReps = service.getAllSalesReps();

      // Then
      assertThat(salesReps).hasSize(1);
      assertThat(salesReps.get(0).employeeId()).isEqualTo("SALES-001");

      verify(mockClient, times(1)).getAllSalesReps();
      verify(realAdapter, never()).getSalesReps();
    }

    @Test
    @DisplayName("testConnection() - returns true in mock mode")
    void testTestConnection_MockMode() {
      // Given
      when(mockClient.testConnection()).thenReturn(true);

      // When
      boolean connected = service.testConnection();

      // Then
      assertThat(connected).isTrue();

      verify(mockClient, times(1)).testConnection();
      verify(realAdapter, never()).testConnection();
    }

    @Test
    @DisplayName("isMockMode() - returns true in mock mode")
    void testIsMockMode_True() {
      // When/Then
      assertThat(service.isMockMode()).isTrue();
    }
  }

  @Nested
  @DisplayName("Real API Mode Tests")
  @TestProfile(RealApiModeProfile.class)
  class RealApiModeTests {

    @Test
    @DisplayName("getCustomersBySalesRep() - delegates to real adapter in real mode")
    void testGetCustomersBySalesRep_RealMode() {
      // Given
      var realCustomer =
          new XentralCustomerDTO(
              "REAL-001",
              "Real Corp",
              "real@example.com",
              "+49-987",
              new BigDecimal("50000.00"),
              25,
              LocalDate.now(),
              "SALES-002");

      when(realAdapter.getCustomersBySalesRep("SALES-002")).thenReturn(List.of(realCustomer));

      // When
      List<XentralCustomerDTO> customers = service.getCustomersBySalesRep("SALES-002");

      // Then
      assertThat(customers).hasSize(1);
      assertThat(customers.get(0).xentralId()).isEqualTo("REAL-001");

      verify(realAdapter, times(1)).getCustomersBySalesRep("SALES-002");
      verify(mockClient, never()).getCustomersBySalesRep(anyString());
    }

    @Test
    @DisplayName("getCustomerById() - delegates to real adapter in real mode")
    void testGetCustomerById_RealMode() {
      // Given
      var realCustomer =
          new XentralCustomerDTO(
              "REAL-123", "Real GmbH", "test@real.com", null, null, null, null, null);

      when(realAdapter.getCustomerById("REAL-123")).thenReturn(realCustomer);

      // When
      XentralCustomerDTO customer = service.getCustomerById("REAL-123");

      // Then
      assertThat(customer).isNotNull();
      assertThat(customer.xentralId()).isEqualTo("REAL-123");

      verify(realAdapter, times(1)).getCustomerById("REAL-123");
      verify(mockClient, never()).getCustomerById(anyString());
    }

    @Test
    @DisplayName("getInvoicesByCustomer() - delegates to real adapter in real mode")
    void testGetInvoicesByCustomer_RealMode() {
      // Given
      var realInvoice =
          new XentralInvoiceDTO(
              "INV-REAL-001",
              "RE-REAL-001",
              "CUST-456",
              new BigDecimal("5000.00"),
              LocalDate.now(),
              LocalDate.now().plusDays(14),
              LocalDate.now().plusDays(10),
              "paid");

      when(realAdapter.getInvoicesByCustomer("CUST-456")).thenReturn(List.of(realInvoice));

      // When
      List<XentralInvoiceDTO> invoices = service.getInvoicesByCustomer("CUST-456");

      // Then
      assertThat(invoices).hasSize(1);
      assertThat(invoices.get(0).invoiceId()).isEqualTo("INV-REAL-001");

      verify(realAdapter, times(1)).getInvoicesByCustomer("CUST-456");
      verify(mockClient, never()).getInvoicesByCustomer(anyString());
    }

    @Test
    @DisplayName("getAllSalesReps() - delegates to real adapter in real mode")
    void testGetAllSalesReps_RealMode() {
      // Given
      var realSalesRep =
          new XentralEmployeeDTO("SALES-REAL-001", "Real", "User", "real@example.com", "sales");

      when(realAdapter.getSalesReps()).thenReturn(List.of(realSalesRep));

      // When
      List<XentralEmployeeDTO> salesReps = service.getAllSalesReps();

      // Then
      assertThat(salesReps).hasSize(1);
      assertThat(salesReps.get(0).employeeId()).isEqualTo("SALES-REAL-001");

      verify(realAdapter, times(1)).getSalesReps();
      verify(mockClient, never()).getAllSalesReps();
    }

    @Test
    @DisplayName("testConnection() - delegates to real adapter in real mode")
    void testTestConnection_RealMode() {
      // Given
      when(realAdapter.testConnection()).thenReturn(true);

      // When
      boolean connected = service.testConnection();

      // Then
      assertThat(connected).isTrue();

      verify(realAdapter, times(1)).testConnection();
      verify(mockClient, never()).testConnection();
    }

    @Test
    @DisplayName(
        "getCustomersBySalesRep() - falls back to mock on error (graceful degradation)")
    void testGetCustomersBySalesRep_FallbackOnError() {
      // Given: real adapter throws exception
      when(realAdapter.getCustomersBySalesRep("SALES-ERROR"))
          .thenThrow(new RuntimeException("API connection failed"));

      // Mock fallback
      var fallbackCustomer =
          new XentralCustomerDTO(
              "FALLBACK-001",
              "Fallback Corp",
              "fallback@example.com",
              null,
              null,
              null,
              null,
              "SALES-ERROR");

      when(mockClient.getCustomersBySalesRep("SALES-ERROR"))
          .thenReturn(List.of(fallbackCustomer));

      // When
      List<XentralCustomerDTO> customers = service.getCustomersBySalesRep("SALES-ERROR");

      // Then: fallback to mock client
      assertThat(customers).hasSize(1);
      assertThat(customers.get(0).xentralId()).isEqualTo("FALLBACK-001");

      verify(realAdapter, times(1)).getCustomersBySalesRep("SALES-ERROR");
      verify(mockClient, times(1)).getCustomersBySalesRep("SALES-ERROR");
    }

    @Test
    @DisplayName("getCustomerById() - falls back to mock on error")
    void testGetCustomerById_FallbackOnError() {
      // Given
      when(realAdapter.getCustomerById("ERROR-123"))
          .thenThrow(new RuntimeException("Network timeout"));

      var fallbackCustomer =
          new XentralCustomerDTO(
              "ERROR-123", "Fallback GmbH", "test@fallback.com", null, null, null, null, null);

      when(mockClient.getCustomerById("ERROR-123")).thenReturn(fallbackCustomer);

      // When
      XentralCustomerDTO customer = service.getCustomerById("ERROR-123");

      // Then
      assertThat(customer).isNotNull();
      assertThat(customer.companyName()).isEqualTo("Fallback GmbH");

      verify(realAdapter, times(1)).getCustomerById("ERROR-123");
      verify(mockClient, times(1)).getCustomerById("ERROR-123");
    }

    @Test
    @DisplayName("isMockMode() - returns false in real mode")
    void testIsMockMode_False() {
      // When/Then
      assertThat(service.isMockMode()).isFalse();
    }
  }

  // --- Test Profiles ---

  /** Test profile with xentral.api.mock-mode=true */
  public static class MockModeProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of("xentral.api.mock-mode", "true");
    }
  }

  /** Test profile with xentral.api.mock-mode=false */
  public static class RealApiModeProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of("xentral.api.mock-mode", "false");
    }
  }
}

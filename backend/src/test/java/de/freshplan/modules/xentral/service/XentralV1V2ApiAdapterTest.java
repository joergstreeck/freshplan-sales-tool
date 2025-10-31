package de.freshplan.modules.xentral.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.freshplan.modules.xentral.client.XentralCustomersV2Client;
import de.freshplan.modules.xentral.client.XentralEmployeesV1Client;
import de.freshplan.modules.xentral.client.XentralInvoicesV1Client;
import de.freshplan.modules.xentral.dto.XentralCustomerDTO;
import de.freshplan.modules.xentral.dto.XentralEmployeeDTO;
import de.freshplan.modules.xentral.dto.XentralInvoiceDTO;
import de.freshplan.modules.xentral.dto.v1.XentralV1Invoice;
import de.freshplan.modules.xentral.dto.v1.XentralV1InvoiceBalance;
import de.freshplan.modules.xentral.dto.v1.XentralV1InvoiceMapper;
import de.freshplan.modules.xentral.dto.v1.XentralV1InvoiceResponse;
import de.freshplan.modules.xentral.dto.v2.XentralV2Customer;
import de.freshplan.modules.xentral.dto.v2.XentralV2Customer.General;
import de.freshplan.modules.xentral.dto.v2.XentralV2Customer.SalesRep;
import de.freshplan.modules.xentral.dto.v2.XentralV2CustomerMapper;
import de.freshplan.modules.xentral.dto.v2.XentralV2CustomerResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for XentralV1V2ApiAdapter.
 *
 * <p>Sprint: 2.1.7.2 - D2e Real API Adapter Tests
 */
@QuarkusTest
class XentralV1V2ApiAdapterTest {

  @Inject XentralV1V2ApiAdapter adapter;

  @InjectMock @RestClient XentralCustomersV2Client customersV2Client;

  @InjectMock @RestClient XentralInvoicesV1Client invoicesV1Client;

  @InjectMock @RestClient XentralEmployeesV1Client employeesV1Client;

  @Inject XentralV2CustomerMapper customerMapper;

  @Inject XentralV1InvoiceMapper invoiceMapper;

  @Inject FinancialMetricsCalculator financialCalculator;

  private static final String AUTH_HEADER_PREFIX = "Bearer ";

  @BeforeEach
  void setUp() {
    // Reset mocks before each test
  }

  @Test
  @DisplayName("getCustomers() - returns customers with financial metrics")
  void testGetCustomers_Success() {
    // Given: v2 API returns customers
    var v2Customer1 =
        new XentralV2Customer(
            "CUST-001",
            new General("ACME Corp", "info@acme.com", "+49-123-456", null),
            null,
            "active",
            new SalesRep("SALES-001", "John Doe", "john@example.com"));

    var v2Customer2 =
        new XentralV2Customer(
            "CUST-002",
            new General("TechStart GmbH", "contact@techstart.de", "+49-987-654", null),
            null,
            "active",
            new SalesRep("SALES-002", "Jane Smith", "jane@example.com"));

    var customersResponse =
        new XentralV2CustomerResponse(List.of(v2Customer1, v2Customer2), null, null);

    when(customersV2Client.getCustomers(anyString(), eq(1), eq(100))).thenReturn(customersResponse);

    // Mock invoices (empty = no financial data enrichment)
    when(invoicesV1Client.getInvoicesByCustomer(anyString(), eq("CUST-001"), eq(1), eq(100)))
        .thenReturn(new XentralV1InvoiceResponse(List.of(), null));
    when(invoicesV1Client.getInvoicesByCustomer(anyString(), eq("CUST-002"), eq(1), eq(100)))
        .thenReturn(new XentralV1InvoiceResponse(List.of(), null));

    // When
    List<XentralCustomerDTO> customers = adapter.getCustomers();

    // Then
    assertThat(customers).hasSize(2);
    assertThat(customers.get(0).xentralId()).isEqualTo("CUST-001");
    assertThat(customers.get(0).companyName()).isEqualTo("ACME Corp");
    assertThat(customers.get(0).totalRevenue()).isNull(); // No invoices = no revenue
    assertThat(customers.get(1).xentralId()).isEqualTo("CUST-002");
    assertThat(customers.get(1).companyName()).isEqualTo("TechStart GmbH");
    assertThat(customers.get(1).totalRevenue()).isNull(); // No invoices = no revenue

    verify(customersV2Client, times(1)).getCustomers(anyString(), eq(1), eq(100));
  }

  @Test
  @DisplayName("getCustomers() - returns empty list when no customers found")
  void testGetCustomers_EmptyResponse() {
    // Given: v2 API returns empty list
    var emptyResponse = new XentralV2CustomerResponse(List.of(), null, null);
    when(customersV2Client.getCustomers(anyString(), anyInt(), anyInt())).thenReturn(emptyResponse);

    // When
    List<XentralCustomerDTO> customers = adapter.getCustomers();

    // Then
    assertThat(customers).isEmpty();
  }

  @Test
  @DisplayName("getCustomers() - throws exception on API error")
  void testGetCustomers_ApiError() {
    // Given: v2 API throws exception
    when(customersV2Client.getCustomers(anyString(), anyInt(), anyInt()))
        .thenThrow(new RuntimeException("API connection failed"));

    // When/Then
    assertThatThrownBy(() -> adapter.getCustomers())
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to fetch customers from Xentral API");
  }

  @Test
  @DisplayName("getCustomerById() - returns customer with financial metrics")
  void testGetCustomerById_Found() {
    // Given
    var v2Customer =
        new XentralV2Customer(
            "CUST-123",
            new General("Test GmbH", "test@example.com", null, null),
            null,
            "active",
            null);

    var customerResponse = new XentralV2CustomerResponse(List.of(v2Customer), null, null);
    when(customersV2Client.getCustomerById(anyString(), eq("CUST-123")))
        .thenReturn(customerResponse);

    // Mock invoices (empty = no financial data)
    when(invoicesV1Client.getInvoicesByCustomer(anyString(), eq("CUST-123"), anyInt(), anyInt()))
        .thenReturn(new XentralV1InvoiceResponse(List.of(), null));

    // When
    XentralCustomerDTO customer = adapter.getCustomerById("CUST-123");

    // Then
    assertThat(customer).isNotNull();
    assertThat(customer.xentralId()).isEqualTo("CUST-123");
    assertThat(customer.companyName()).isEqualTo("Test GmbH");
    verify(customersV2Client, times(1)).getCustomerById(anyString(), eq("CUST-123"));
  }

  @Test
  @DisplayName("getCustomerById() - returns null when not found")
  void testGetCustomerById_NotFound() {
    // Given: v2 API returns empty list
    var emptyResponse = new XentralV2CustomerResponse(List.of(), null, null);
    when(customersV2Client.getCustomerById(anyString(), eq("CUST-999"))).thenReturn(emptyResponse);

    // When
    XentralCustomerDTO customer = adapter.getCustomerById("CUST-999");

    // Then
    assertThat(customer).isNull();
  }

  @Test
  @DisplayName("getCustomersBySalesRep() - filters customers by sales rep")
  void testGetCustomersBySalesRep() {
    // Given: 2 customers, 1 matches sales rep
    var v2Customer1 =
        new XentralV2Customer(
            "CUST-001",
            new General("ACME Corp", null, null, null),
            null,
            "active",
            new SalesRep("SALES-123", "John", "john@example.com"));

    var v2Customer2 =
        new XentralV2Customer(
            "CUST-002",
            new General("TechCorp", null, null, null),
            null,
            "active",
            new SalesRep("SALES-456", "Jane", "jane@example.com"));

    var customersResponse =
        new XentralV2CustomerResponse(List.of(v2Customer1, v2Customer2), null, null);
    when(customersV2Client.getCustomers(anyString(), anyInt(), anyInt()))
        .thenReturn(customersResponse);

    when(invoicesV1Client.getInvoicesByCustomer(anyString(), anyString(), anyInt(), anyInt()))
        .thenReturn(new XentralV1InvoiceResponse(List.of(), null));

    // When
    List<XentralCustomerDTO> customers = adapter.getCustomersBySalesRep("SALES-123");

    // Then: only CUST-001 matches
    assertThat(customers).hasSize(1);
    assertThat(customers.get(0).xentralId()).isEqualTo("CUST-001");
    assertThat(customers.get(0).companyName()).isEqualTo("ACME Corp");
    assertThat(customers.get(0).salesRepId()).isEqualTo("SALES-123");
  }

  @Test
  @DisplayName("getInvoicesByCustomer() - returns invoices with balance data")
  void testGetInvoicesByCustomer_WithInvoices() {
    // Given
    var invoice1 =
        new XentralV1Invoice(
            "INV-001",
            "RE-2024-001",
            "CUST-123",
            new BigDecimal("1000.00"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            "paid");

    var invoice2 =
        new XentralV1Invoice(
            "INV-002",
            "RE-2024-002",
            "CUST-123",
            new BigDecimal("2000.00"),
            LocalDate.of(2024, 10, 1),
            LocalDate.of(2024, 11, 1),
            "open");

    var invoiceResponse = new XentralV1InvoiceResponse(List.of(invoice1, invoice2), null);
    when(invoicesV1Client.getInvoicesByCustomer(anyString(), eq("CUST-123"), anyInt(), anyInt()))
        .thenReturn(invoiceResponse);

    // Mock balance fetching
    var balance1 =
        new XentralV1InvoiceBalance(
            new BigDecimal("1000.00"),
            new BigDecimal("1000.00"),
            BigDecimal.ZERO,
            LocalDate.of(2024, 9, 15));
    var balance2 =
        new XentralV1InvoiceBalance(
            new BigDecimal("2000.00"), BigDecimal.ZERO, new BigDecimal("2000.00"), null);

    when(invoicesV1Client.getInvoiceBalance(anyString(), eq("INV-001"))).thenReturn(balance1);
    when(invoicesV1Client.getInvoiceBalance(anyString(), eq("INV-002"))).thenReturn(balance2);

    // When
    List<XentralInvoiceDTO> invoices = adapter.getInvoicesByCustomer("CUST-123");

    // Then
    assertThat(invoices).hasSize(2);
    assertThat(invoices.get(0).invoiceId()).isEqualTo("INV-001");
    assertThat(invoices.get(0).invoiceNumber()).isEqualTo("RE-2024-001");
    assertThat(invoices.get(0).status()).isEqualTo("paid"); // balance.isFullyPaid() = true
    assertThat(invoices.get(1).invoiceId()).isEqualTo("INV-002");
    assertThat(invoices.get(1).invoiceNumber()).isEqualTo("RE-2024-002");
    assertThat(invoices.get(1).status()).isEqualTo("open"); // invoice.status fallback

    verify(invoicesV1Client, times(1))
        .getInvoicesByCustomer(anyString(), eq("CUST-123"), anyInt(), anyInt());
    verify(invoicesV1Client, times(1)).getInvoiceBalance(anyString(), eq("INV-001"));
    verify(invoicesV1Client, times(1)).getInvoiceBalance(anyString(), eq("INV-002"));
  }

  @Test
  @DisplayName("getInvoicesByCustomer() - returns empty list when no invoices found")
  void testGetInvoicesByCustomer_Empty() {
    // Given
    var emptyResponse = new XentralV1InvoiceResponse(List.of(), null);
    when(invoicesV1Client.getInvoicesByCustomer(anyString(), eq("CUST-999"), anyInt(), anyInt()))
        .thenReturn(emptyResponse);

    // When
    List<XentralInvoiceDTO> invoices = adapter.getInvoicesByCustomer("CUST-999");

    // Then
    assertThat(invoices).isEmpty();
  }

  @Test
  @DisplayName("getInvoicesByCustomer() - returns empty list on error (graceful degradation)")
  void testGetInvoicesByCustomer_ApiError() {
    // Given
    when(invoicesV1Client.getInvoicesByCustomer(anyString(), anyString(), anyInt(), anyInt()))
        .thenThrow(new RuntimeException("API error"));

    // When
    List<XentralInvoiceDTO> invoices = adapter.getInvoicesByCustomer("CUST-ERROR");

    // Then: graceful degradation
    assertThat(invoices).isEmpty();
  }

  @Test
  @DisplayName("getSalesReps() - returns list of employees")
  void testGetSalesReps_Success() {
    // Given
    var employee1 =
        new XentralEmployeesV1Client.XentralV1Employee(
            "EMP-001", "John", "Doe", "john@example.com", "sales", "Sales");
    var employee2 =
        new XentralEmployeesV1Client.XentralV1Employee(
            "EMP-002", "Jane", "Smith", "jane@example.com", "manager", "Management");

    var employeeResponse =
        new XentralEmployeesV1Client.XentralV1EmployeeResponse(List.of(employee1, employee2), null);
    when(employeesV1Client.getEmployees(anyString(), anyInt(), anyInt()))
        .thenReturn(employeeResponse);

    // When
    List<XentralEmployeeDTO> salesReps = adapter.getSalesReps();

    // Then
    assertThat(salesReps).hasSize(2);
    assertThat(salesReps.get(0).employeeId()).isEqualTo("EMP-001");
    assertThat(salesReps.get(0).firstName()).isEqualTo("John");
    assertThat(salesReps.get(1).employeeId()).isEqualTo("EMP-002");
    assertThat(salesReps.get(1).firstName()).isEqualTo("Jane");
  }

  @Test
  @DisplayName("getSalesReps() - returns empty list on error")
  void testGetSalesReps_ApiError() {
    // Given
    when(employeesV1Client.getEmployees(anyString(), anyInt(), anyInt()))
        .thenThrow(new RuntimeException("API error"));

    // When
    List<XentralEmployeeDTO> salesReps = adapter.getSalesReps();

    // Then: graceful degradation
    assertThat(salesReps).isEmpty();
  }

  @Test
  @DisplayName("testConnection() - returns true on successful connection")
  void testTestConnection_Success() {
    // Given
    var response = new XentralV2CustomerResponse(List.of(), null, null);
    when(customersV2Client.getCustomers(anyString(), eq(1), eq(1))).thenReturn(response);

    // When
    boolean connected = adapter.testConnection();

    // Then
    assertThat(connected).isTrue();
    verify(customersV2Client, times(1)).getCustomers(anyString(), eq(1), eq(1));
  }

  @Test
  @DisplayName("testConnection() - returns false on connection failure")
  void testTestConnection_Failure() {
    // Given
    when(customersV2Client.getCustomers(anyString(), anyInt(), anyInt()))
        .thenThrow(new RuntimeException("Connection refused"));

    // When
    boolean connected = adapter.testConnection();

    // Then
    assertThat(connected).isFalse();
  }
}

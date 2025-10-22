package de.freshplan.modules.xentral.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import de.freshplan.modules.xentral.service.FinancialMetricsCalculator.FinancialMetrics;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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

  @InjectMock XentralV2CustomerMapper customerMapper;

  @InjectMock XentralV1InvoiceMapper invoiceMapper;

  @InjectMock FinancialMetricsCalculator financialCalculator;

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

    var customersResponse = new XentralV2CustomerResponse(List.of(v2Customer1, v2Customer2), null, null);

    when(customersV2Client.getCustomers(anyString(), eq(1), eq(100)))
        .thenReturn(customersResponse);

    // Mock mapper: v2 customer → base DTO (without financial data)
    var baseDTO1 =
        new XentralCustomerDTO(
            "CUST-001", "ACME Corp", "info@acme.com", "+49-123-456", null, null, null, "SALES-001");
    var baseDTO2 =
        new XentralCustomerDTO(
            "CUST-002",
            "TechStart GmbH",
            "contact@techstart.de",
            "+49-987-654",
            null,
            null,
            null,
            "SALES-002");

    when(customerMapper.toDTO(v2Customer1)).thenReturn(baseDTO1);
    when(customerMapper.toDTO(v2Customer2)).thenReturn(baseDTO2);

    // Mock invoices + financial metrics
    var invoiceResponse1 = new XentralV1InvoiceResponse(List.of(), null);
    var invoiceResponse2 = new XentralV1InvoiceResponse(List.of(), null);

    when(invoicesV1Client.getInvoicesByCustomer(anyString(), eq("CUST-001"), eq(1), eq(100)))
        .thenReturn(invoiceResponse1);
    when(invoicesV1Client.getInvoicesByCustomer(anyString(), eq("CUST-002"), eq(1), eq(100)))
        .thenReturn(invoiceResponse2);

    var metrics1 =
        new FinancialMetrics(
            new BigDecimal("50000.00"), 25, LocalDate.of(2024, 10, 15));
    var metrics2 =
        new FinancialMetrics(
            new BigDecimal("30000.00"), 30, LocalDate.of(2024, 9, 20));

    when(financialCalculator.calculate(any(), any())).thenReturn(metrics1, metrics2);

    // Mock enrichment
    var enrichedDTO1 =
        new XentralCustomerDTO(
            "CUST-001",
            "ACME Corp",
            "info@acme.com",
            "+49-123-456",
            new BigDecimal("50000.00"),
            25,
            LocalDate.of(2024, 10, 15),
            "SALES-001");
    var enrichedDTO2 =
        new XentralCustomerDTO(
            "CUST-002",
            "TechStart GmbH",
            "contact@techstart.de",
            "+49-987-654",
            new BigDecimal("30000.00"),
            30,
            LocalDate.of(2024, 9, 20),
            "SALES-002");

    when(customerMapper.enrichWithFinancialData(
            eq(baseDTO1),
            eq(new BigDecimal("50000.00")),
            eq(25),
            eq(LocalDate.of(2024, 10, 15))))
        .thenReturn(enrichedDTO1);
    when(customerMapper.enrichWithFinancialData(
            eq(baseDTO2),
            eq(new BigDecimal("30000.00")),
            eq(30),
            eq(LocalDate.of(2024, 9, 20))))
        .thenReturn(enrichedDTO2);

    // When
    List<XentralCustomerDTO> customers = adapter.getCustomers();

    // Then
    assertThat(customers).hasSize(2);
    assertThat(customers.get(0).xentralId()).isEqualTo("CUST-001");
    assertThat(customers.get(0).totalRevenue()).isEqualByComparingTo("50000.00");
    assertThat(customers.get(1).xentralId()).isEqualTo("CUST-002");
    assertThat(customers.get(1).totalRevenue()).isEqualByComparingTo("30000.00");

    verify(customersV2Client, times(1)).getCustomers(anyString(), eq(1), eq(100));
  }

  @Test
  @DisplayName("getCustomers() - returns empty list when no customers found")
  void testGetCustomers_EmptyResponse() {
    // Given: v2 API returns empty list
    var emptyResponse = new XentralV2CustomerResponse(List.of(), null, null);
    when(customersV2Client.getCustomers(anyString(), anyInt(), anyInt()))
        .thenReturn(emptyResponse);

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

    var baseDTO =
        new XentralCustomerDTO(
            "CUST-123", "Test GmbH", "test@example.com", "", null, null, null, null);
    when(customerMapper.toDTO(v2Customer)).thenReturn(baseDTO);

    // Mock invoices
    when(invoicesV1Client.getInvoicesByCustomer(anyString(), eq("CUST-123"), anyInt(), anyInt()))
        .thenReturn(new XentralV1InvoiceResponse(List.of(), null));

    when(financialCalculator.calculate(any(), any()))
        .thenReturn(new FinancialMetrics(BigDecimal.ZERO, null, null));

    when(customerMapper.enrichWithFinancialData(any(), any(), any(), any())).thenReturn(baseDTO);

    // When
    XentralCustomerDTO customer = adapter.getCustomerById("CUST-123");

    // Then
    assertThat(customer).isNotNull();
    assertThat(customer.xentralId()).isEqualTo("CUST-123");
    verify(customersV2Client, times(1)).getCustomerById(anyString(), eq("CUST-123"));
  }

  @Test
  @DisplayName("getCustomerById() - returns null when not found")
  void testGetCustomerById_NotFound() {
    // Given: v2 API returns empty list
    var emptyResponse = new XentralV2CustomerResponse(List.of(), null, null);
    when(customersV2Client.getCustomerById(anyString(), eq("CUST-999")))
        .thenReturn(emptyResponse);

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

    var customersResponse = new XentralV2CustomerResponse(List.of(v2Customer1, v2Customer2), null, null);
    when(customersV2Client.getCustomers(anyString(), anyInt(), anyInt()))
        .thenReturn(customersResponse);

    var dto1 =
        new XentralCustomerDTO(
            "CUST-001", "ACME Corp", "", "", null, null, null, "SALES-123");
    var dto2 =
        new XentralCustomerDTO("CUST-002", "TechCorp", "", "", null, null, null, "SALES-456");

    when(customerMapper.toDTO(v2Customer1)).thenReturn(dto1);
    when(customerMapper.toDTO(v2Customer2)).thenReturn(dto2);

    when(invoicesV1Client.getInvoicesByCustomer(anyString(), anyString(), anyInt(), anyInt()))
        .thenReturn(new XentralV1InvoiceResponse(List.of(), null));

    when(financialCalculator.calculate(any(), any()))
        .thenReturn(new FinancialMetrics(BigDecimal.ZERO, null, null));

    when(customerMapper.enrichWithFinancialData(any(), any(), any(), any()))
        .thenAnswer(inv -> inv.getArgument(0));

    // When
    List<XentralCustomerDTO> customers = adapter.getCustomersBySalesRep("SALES-123");

    // Then: only CUST-001 matches
    assertThat(customers).hasSize(1);
    assertThat(customers.get(0).xentralId()).isEqualTo("CUST-001");
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
            new BigDecimal("2000.00"),
            BigDecimal.ZERO,
            new BigDecimal("2000.00"),
            null);

    when(invoicesV1Client.getInvoiceBalance(anyString(), eq("INV-001"))).thenReturn(balance1);
    when(invoicesV1Client.getInvoiceBalance(anyString(), eq("INV-002"))).thenReturn(balance2);

    // Mock mapper
    var invoiceDTO1 =
        new XentralInvoiceDTO(
            "INV-001",
            "RE-2024-001",
            "CUST-123",
            new BigDecimal("1000.00"),
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 10, 1),
            LocalDate.of(2024, 9, 15),
            "paid");

    var invoiceDTO2 =
        new XentralInvoiceDTO(
            "INV-002",
            "RE-2024-002",
            "CUST-123",
            new BigDecimal("2000.00"),
            LocalDate.of(2024, 10, 1),
            LocalDate.of(2024, 11, 1),
            null,
            "open");

    when(invoiceMapper.toDTO(invoice1, balance1)).thenReturn(invoiceDTO1);
    when(invoiceMapper.toDTO(invoice2, balance2)).thenReturn(invoiceDTO2);

    // When
    List<XentralInvoiceDTO> invoices = adapter.getInvoicesByCustomer("CUST-123");

    // Then
    assertThat(invoices).hasSize(2);
    assertThat(invoices.get(0).invoiceId()).isEqualTo("INV-001");
    assertThat(invoices.get(1).invoiceId()).isEqualTo("INV-002");

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
        new XentralEmployeesV1Client.XentralV1EmployeeResponse(
            List.of(employee1, employee2), null);
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

package de.freshplan.modules.xentral.client;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.modules.xentral.dto.XentralCustomerDTO;
import de.freshplan.modules.xentral.dto.XentralEmployeeDTO;
import de.freshplan.modules.xentral.dto.XentralInvoiceDTO;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit Tests for MockXentralApiClient
 *
 * <p>Sprint 2.1.7.2: Xentral Integration (TDD-Style)
 *
 * <p>Tests mock data behavior for development and testing.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
class MockXentralApiClientTest {

  private MockXentralApiClient mockClient;

  @BeforeEach
  void setUp() {
    mockClient = new MockXentralApiClient();
  }

  // ========== GET CUSTOMERS BY SALES REP ==========

  @Test
  void getCustomersBySalesRep_withValidSalesRepId_returnsFilteredCustomers() {
    // Given
    String salesRepId = "SALES-REP-001";

    // When
    List<XentralCustomerDTO> customers = mockClient.getCustomersBySalesRep(salesRepId);

    // Then
    assertNotNull(customers);
    assertTrue(customers.size() > 0, "Should return at least 1 customer for SALES-REP-001");

    // Verify all customers belong to this sales rep
    customers.forEach(
        customer ->
            assertEquals(
                salesRepId,
                customer.salesRepId(),
                "All customers should have salesRepId=" + salesRepId));

    // Verify expected customers are present
    assertTrue(
        customers.stream().anyMatch(c -> c.companyName().equals("Restaurant Silbertanne")),
        "Should contain Restaurant Silbertanne");
    assertTrue(
        customers.stream().anyMatch(c -> c.companyName().equals("Hotel Bergblick")),
        "Should contain Hotel Bergblick");
  }

  @Test
  void getCustomersBySalesRep_withDifferentSalesRepId_returnsDifferentCustomers() {
    // Given
    String salesRepId = "SALES-REP-002";

    // When
    List<XentralCustomerDTO> customers = mockClient.getCustomersBySalesRep(salesRepId);

    // Then
    assertNotNull(customers);
    assertTrue(customers.size() > 0, "Should return at least 1 customer for SALES-REP-002");

    // Verify expected customers
    assertTrue(
        customers.stream().anyMatch(c -> c.companyName().equals("Café Morgenrot")),
        "Should contain Café Morgenrot");
  }

  @Test
  void getCustomersBySalesRep_withNullSalesRepId_returnsEmptyList() {
    // When
    List<XentralCustomerDTO> customers = mockClient.getCustomersBySalesRep(null);

    // Then
    assertNotNull(customers);
    assertTrue(customers.isEmpty(), "Should return empty list for null salesRepId");
  }

  @Test
  void getCustomersBySalesRep_withBlankSalesRepId_returnsEmptyList() {
    // When
    List<XentralCustomerDTO> customers = mockClient.getCustomersBySalesRep("   ");

    // Then
    assertNotNull(customers);
    assertTrue(customers.isEmpty(), "Should return empty list for blank salesRepId");
  }

  @Test
  void getCustomersBySalesRep_withUnknownSalesRepId_returnsEmptyList() {
    // When
    List<XentralCustomerDTO> customers = mockClient.getCustomersBySalesRep("UNKNOWN-REP");

    // Then
    assertNotNull(customers);
    assertTrue(customers.isEmpty(), "Should return empty list for unknown salesRepId");
  }

  // ========== GET CUSTOMER BY ID ==========

  @Test
  void getCustomerById_withValidXentralId_returnsCustomer() {
    // Given
    String xentralId = "XENT-001";

    // When
    XentralCustomerDTO customer = mockClient.getCustomerById(xentralId);

    // Then
    assertNotNull(customer);
    assertEquals("XENT-001", customer.xentralId());
    assertEquals("Restaurant Silbertanne", customer.companyName());
    assertNotNull(customer.totalRevenue());
  }

  @Test
  void getCustomerById_withUnknownXentralId_returnsNull() {
    // When
    XentralCustomerDTO customer = mockClient.getCustomerById("UNKNOWN-ID");

    // Then
    assertNull(customer, "Should return null for unknown xentralId");
  }

  @Test
  void getCustomerById_withNullXentralId_returnsNull() {
    // When
    XentralCustomerDTO customer = mockClient.getCustomerById(null);

    // Then
    assertNull(customer, "Should return null for null xentralId");
  }

  // ========== GET INVOICES BY CUSTOMER ==========

  @Test
  void getInvoicesByCustomer_withValidCustomerId_returnsInvoices() {
    // Given
    String customerId = "XENT-001";

    // When
    List<XentralInvoiceDTO> invoices = mockClient.getInvoicesByCustomer(customerId);

    // Then
    assertNotNull(invoices);
    assertTrue(invoices.size() > 0, "Should return at least 1 invoice for XENT-001");

    // Verify all invoices belong to this customer
    invoices.forEach(
        invoice ->
            assertEquals(
                customerId,
                invoice.customerId(),
                "All invoices should have customerId=" + customerId));
  }

  @Test
  void getInvoicesByCustomer_withUnknownCustomerId_returnsEmptyList() {
    // When
    List<XentralInvoiceDTO> invoices = mockClient.getInvoicesByCustomer("UNKNOWN-ID");

    // Then
    assertNotNull(invoices);
    assertTrue(invoices.isEmpty(), "Should return empty list for unknown customerId");
  }

  @Test
  void getInvoicesByCustomer_withNullCustomerId_returnsEmptyList() {
    // When
    List<XentralInvoiceDTO> invoices = mockClient.getInvoicesByCustomer(null);

    // Then
    assertNotNull(invoices);
    assertTrue(invoices.isEmpty(), "Should return empty list for null customerId");
  }

  // ========== GET ALL SALES REPS ==========

  @Test
  void getAllSalesReps_returnsAllEmployees() {
    // When
    List<XentralEmployeeDTO> salesReps = mockClient.getAllSalesReps();

    // Then
    assertNotNull(salesReps);
    assertTrue(salesReps.size() >= 3, "Should return at least 3 sales reps");

    // Verify expected employees
    assertTrue(
        salesReps.stream().anyMatch(e -> e.email().equals("max.mustermann@freshplan.de")),
        "Should contain Max Mustermann");
    assertTrue(
        salesReps.stream().anyMatch(e -> e.email().equals("lisa.schmidt@freshplan.de")),
        "Should contain Lisa Schmidt");
  }

  // ========== TEST CONNECTION ==========

  @Test
  void testConnection_alwaysReturnsTrue() {
    // When
    boolean result = mockClient.testConnection();

    // Then
    assertTrue(result, "Mock connection test should always return true");
  }

  // ========== INVOICE DTO UTILITY METHODS ==========

  @Test
  void invoiceDTO_isPaid_returnsTrueWhenPaymentDateExists() {
    // Given
    List<XentralInvoiceDTO> invoices = mockClient.getInvoicesByCustomer("XENT-001");
    XentralInvoiceDTO paidInvoice =
        invoices.stream().filter(XentralInvoiceDTO::isPaid).findFirst().orElse(null);

    // Then
    assertNotNull(paidInvoice, "Should find at least one paid invoice");
    assertTrue(paidInvoice.isPaid());
    assertNotNull(paidInvoice.paymentDate());
  }

  @Test
  void invoiceDTO_getDaysToPay_calculatesCorrectly() {
    // Given
    List<XentralInvoiceDTO> invoices = mockClient.getInvoicesByCustomer("XENT-001");
    XentralInvoiceDTO paidInvoice =
        invoices.stream().filter(XentralInvoiceDTO::isPaid).findFirst().orElse(null);

    // Then
    assertNotNull(paidInvoice, "Should find at least one paid invoice");
    Integer daysToPay = paidInvoice.getDaysToPay();
    assertNotNull(daysToPay, "Days to pay should be calculated");
    assertTrue(daysToPay >= 0, "Days to pay should be >= 0");
  }
}

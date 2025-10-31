package de.freshplan.modules.xentral.client;

import de.freshplan.modules.xentral.dto.XentralCustomerDTO;
import de.freshplan.modules.xentral.dto.XentralEmployeeDTO;
import de.freshplan.modules.xentral.dto.XentralInvoiceDTO;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock Xentral API Client
 *
 * <p>Sprint 2.1.7.2: Xentral Integration (Mock-Mode Development)
 *
 * <p>Provides mock data for development and testing WITHOUT calling real Xentral API.
 *
 * <p>Mock Data Strategy: - Realistic test data (Food industry customers) - Deterministic (same
 * input = same output) - Sales-Rep filtered (respects salesRepId parameter) - Performance-friendly
 * (in-memory data)
 *
 * <p>Usage: - Development: xentral.api.mock-mode=true - Testing: Used in integration tests -
 * Production: xentral.api.mock-mode=false (switch to real API)
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class MockXentralApiClient {

  private static final Logger logger = LoggerFactory.getLogger(MockXentralApiClient.class);

  // Mock Data: Xentral Customers (Food industry)
  private static final List<XentralCustomerDTO> MOCK_CUSTOMERS =
      List.of(
          new XentralCustomerDTO(
              "XENT-001",
              "Restaurant Silbertanne",
              "info@silbertanne.de",
              "+49 89 12345678",
              new BigDecimal("142000.00"),
              18, // Average days to pay
              LocalDate.now().minusDays(15),
              "SALES-REP-001"),
          new XentralCustomerDTO(
              "XENT-002",
              "Hotel Bergblick",
              "rezeption@bergblick.de",
              "+49 8821 987654",
              new BigDecimal("285000.00"),
              25,
              LocalDate.now().minusDays(8),
              "SALES-REP-001"),
          new XentralCustomerDTO(
              "XENT-003",
              "Café Morgenrot",
              "kontakt@morgenrot-cafe.de",
              "+49 30 555666",
              new BigDecimal("38200.00"),
              12,
              LocalDate.now().minusDays(3),
              "SALES-REP-002"),
          new XentralCustomerDTO(
              "XENT-004",
              "Biergarten am See",
              "info@biergarten-see.de",
              "+49 89 777888",
              new BigDecimal("95000.00"),
              45, // WARNING Payment behavior (slow payer)
              LocalDate.now().minusDays(92), // At-risk (churn detection)
              "SALES-REP-001"),
          new XentralCustomerDTO(
              "XENT-005",
              "Eisdiele Venezia",
              "eis@venezia.de",
              "+49 89 222333",
              new BigDecimal("62000.00"),
              15,
              LocalDate.now().minusDays(180), // Seasonal business (winter pause OK!)
              "SALES-REP-002"));

  // Mock Data: Xentral Invoices
  private static final List<XentralInvoiceDTO> MOCK_INVOICES =
      List.of(
          // Restaurant Silbertanne - Recent activity
          new XentralInvoiceDTO(
              "INV-001",
              "RE-2025-001",
              "XENT-001",
              new BigDecimal("1250.00"),
              LocalDate.now().minusDays(20),
              LocalDate.now().minusDays(10),
              LocalDate.now().minusDays(5), // Paid on time
              "PAID"),
          new XentralInvoiceDTO(
              "INV-002",
              "RE-2025-002",
              "XENT-001",
              new BigDecimal("2800.50"),
              LocalDate.now().minusDays(50),
              LocalDate.now().minusDays(20),
              LocalDate.now().minusDays(15),
              "PAID"),
          // Hotel Bergblick - Large customer
          new XentralInvoiceDTO(
              "INV-003",
              "RE-2025-003",
              "XENT-002",
              new BigDecimal("8500.00"),
              LocalDate.now().minusDays(10),
              LocalDate.now().plusDays(20),
              null, // Not paid yet (OPEN)
              "OPEN"),
          new XentralInvoiceDTO(
              "INV-004",
              "RE-2025-004",
              "XENT-002",
              new BigDecimal("12000.00"),
              LocalDate.now().minusDays(35),
              LocalDate.now().minusDays(5),
              LocalDate.now().minusDays(7), // Paid 2 days late (OK)
              "PAID"),
          // Biergarten am See - Slow payer (WARNING)
          new XentralInvoiceDTO(
              "INV-005",
              "RE-2025-005",
              "XENT-004",
              new BigDecimal("3200.00"),
              LocalDate.now().minusDays(120),
              LocalDate.now().minusDays(90),
              LocalDate.now().minusDays(45), // Paid 45 days late (WARNING!)
              "PAID"),
          new XentralInvoiceDTO(
              "INV-006",
              "RE-2025-006",
              "XENT-004",
              new BigDecimal("1800.00"),
              LocalDate.now().minusDays(150),
              LocalDate.now().minusDays(120),
              LocalDate.now().minusDays(60), // Paid 60 days late (CRITICAL!)
              "PAID"));

  // Mock Data: Xentral Employees (Sales Reps)
  // Email domains match SEED data: @freshplan.example
  private static final List<XentralEmployeeDTO> MOCK_EMPLOYEES =
      List.of(
          new XentralEmployeeDTO(
              "SALES-REP-001", "Stefan", "Weber", "stefan.weber@freshplan.example", "sales"),
          new XentralEmployeeDTO(
              "SALES-REP-002", "Anna", "Schmidt", "anna.schmidt@freshplan.example", "sales"),
          new XentralEmployeeDTO(
              "SALES-REP-003", "Michael", "Becker", "michael.becker@freshplan.example", "sales"));

  /**
   * Get customers by sales rep ID
   *
   * <p>Mock implementation: Filters MOCK_CUSTOMERS by salesRepId
   *
   * @param salesRepId Xentral Employee ID (Sales Rep)
   * @return List of customers assigned to this sales rep
   */
  public List<XentralCustomerDTO> getCustomersBySalesRep(String salesRepId) {
    logger.debug("Mock: getCustomersBySalesRep(salesRepId={})", salesRepId);

    if (salesRepId == null || salesRepId.isBlank()) {
      logger.warn("Mock: salesRepId is null or blank - returning empty list");
      return List.of();
    }

    List<XentralCustomerDTO> filtered =
        MOCK_CUSTOMERS.stream()
            .filter(c -> salesRepId.equals(c.salesRepId()))
            .collect(Collectors.toList());

    logger.info("Mock: Found {} customers for salesRepId={}", filtered.size(), salesRepId);
    return filtered;
  }

  /**
   * Get single customer by Xentral ID
   *
   * <p>Mock implementation: Finds customer in MOCK_CUSTOMERS
   *
   * @param xentralCustomerId Xentral Customer ID
   * @return Customer or null if not found
   */
  public XentralCustomerDTO getCustomerById(String xentralCustomerId) {
    logger.debug("Mock: getCustomerById(xentralCustomerId={})", xentralCustomerId);

    if (xentralCustomerId == null || xentralCustomerId.isBlank()) {
      logger.warn("Mock: xentralCustomerId is null or blank - returning null");
      return null;
    }

    return MOCK_CUSTOMERS.stream()
        .filter(c -> xentralCustomerId.equals(c.xentralId()))
        .findFirst()
        .orElse(null);
  }

  /**
   * Get invoices by customer ID
   *
   * <p>Mock implementation: Filters MOCK_INVOICES by customerId
   *
   * @param xentralCustomerId Xentral Customer ID
   * @return List of invoices for this customer
   */
  public List<XentralInvoiceDTO> getInvoicesByCustomer(String xentralCustomerId) {
    logger.debug("Mock: getInvoicesByCustomer(xentralCustomerId={})", xentralCustomerId);

    if (xentralCustomerId == null || xentralCustomerId.isBlank()) {
      logger.warn("Mock: xentralCustomerId is null or blank - returning empty list");
      return List.of();
    }

    List<XentralInvoiceDTO> filtered =
        MOCK_INVOICES.stream()
            .filter(inv -> xentralCustomerId.equals(inv.customerId()))
            .collect(Collectors.toList());

    logger.info(
        "Mock: Found {} invoices for xentralCustomerId={}", filtered.size(), xentralCustomerId);
    return filtered;
  }

  /**
   * Get all sales reps (employees with role=sales)
   *
   * <p>Mock implementation: Returns MOCK_EMPLOYEES
   *
   * @return List of sales rep employees
   */
  public List<XentralEmployeeDTO> getAllSalesReps() {
    logger.debug("Mock: getAllSalesReps()");
    logger.info("Mock: Returning {} sales reps", MOCK_EMPLOYEES.size());
    return new ArrayList<>(MOCK_EMPLOYEES);
  }

  /**
   * Test connection to Xentral API
   *
   * <p>Mock implementation: Always returns true (mock is always "available")
   *
   * @return true (mock never fails)
   */
  public boolean testConnection() {
    logger.debug("Mock: testConnection()");
    logger.info("Mock: Connection test successful (mock mode)");
    return true;
  }
}

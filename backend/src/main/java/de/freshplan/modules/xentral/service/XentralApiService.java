package de.freshplan.modules.xentral.service;

import de.freshplan.modules.xentral.client.MockXentralApiClient;
import de.freshplan.modules.xentral.dto.XentralCustomerDTO;
import de.freshplan.modules.xentral.dto.XentralEmployeeDTO;
import de.freshplan.modules.xentral.dto.XentralInvoiceDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Xentral API Service (Facade Pattern)
 *
 * <p>Sprint 2.1.7.2: Xentral Integration
 *
 * <p>FACADE PATTERN: Provides unified API for Xentral integration with automatic Mock vs Real
 * switching.
 *
 * <p>Feature Flag Strategy: - Development: xentral.api.mock-mode=true (Mock-Mode) - Production:
 * xentral.api.mock-mode=false (Real API) - Fallback: If real API fails → fallback to mock data
 * (graceful degradation)
 *
 * <p>Benefits: - Start development immediately (no Xentral API dependency) - Switch to real API in
 * 1-2h (just change config) - Zero code changes (same interface) - Graceful degradation (fallback
 * on error)
 *
 * <p>Usage Example:
 *
 * <pre>{@code
 * @Inject XentralApiService xentralService;
 *
 * List<XentralCustomerDTO> customers =
 *     xentralService.getCustomersBySalesRep("SALES-REP-001");
 * }</pre>
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class XentralApiService {

  private static final Logger logger = LoggerFactory.getLogger(XentralApiService.class);

  @ConfigProperty(name = "xentral.api.mock-mode", defaultValue = "true")
  boolean mockMode;

  @Inject MockXentralApiClient mockClient;

  @Inject XentralV1V2ApiAdapter realAdapter;

  /**
   * Get customers by sales rep ID
   *
   * <p>Returns customers assigned to the specified sales rep in Xentral.
   *
   * @param salesRepId Xentral Employee ID (Sales Rep)
   * @return List of customers (empty if none found or error)
   */
  public List<XentralCustomerDTO> getCustomersBySalesRep(String salesRepId) {
    if (mockMode) {
      logger.debug("Mock-Mode: Delegating to MockXentralApiClient");
      return mockClient.getCustomersBySalesRep(salesRepId);
    }

    // Real API implementation
    try {
      logger.debug("Real-API-Mode: Calling Xentral v1/v2 API");
      List<XentralCustomerDTO> customers = realAdapter.getCustomersBySalesRep(salesRepId);
      logger.info("Real API: Found {} customers for salesRepId={}", customers.size(), salesRepId);
      return customers;

    } catch (Exception e) {
      logger.error("Xentral API failed - fallback to mock data", e);
      return mockClient.getCustomersBySalesRep(salesRepId);
    }
  }

  /**
   * Get single customer by Xentral ID
   *
   * @param xentralCustomerId Xentral Customer ID
   * @return Customer or null if not found
   */
  public XentralCustomerDTO getCustomerById(String xentralCustomerId) {
    if (mockMode) {
      logger.debug("Mock-Mode: Delegating to MockXentralApiClient");
      return mockClient.getCustomerById(xentralCustomerId);
    }

    // Real API implementation
    try {
      logger.debug("Real-API-Mode: Calling Xentral v1/v2 API");
      XentralCustomerDTO customer = realAdapter.getCustomerById(xentralCustomerId);
      logger.info("Real API: Found customer xentralCustomerId={}", xentralCustomerId);
      return customer;

    } catch (Exception e) {
      logger.error("Xentral API failed - fallback to mock data", e);
      return mockClient.getCustomerById(xentralCustomerId);
    }
  }

  /**
   * Get invoices by customer ID
   *
   * @param xentralCustomerId Xentral Customer ID
   * @return List of invoices (empty if none found or error)
   */
  public List<XentralInvoiceDTO> getInvoicesByCustomer(String xentralCustomerId) {
    if (mockMode) {
      logger.debug("Mock-Mode: Delegating to MockXentralApiClient");
      return mockClient.getInvoicesByCustomer(xentralCustomerId);
    }

    // Real API implementation
    try {
      logger.debug("Real-API-Mode: Calling Xentral v1 Invoices API");
      List<XentralInvoiceDTO> invoices = realAdapter.getInvoicesByCustomer(xentralCustomerId);
      logger.info(
          "Real API: Found {} invoices for xentralCustomerId={}",
          invoices.size(),
          xentralCustomerId);
      return invoices;

    } catch (Exception e) {
      logger.error("Xentral API failed - fallback to mock data", e);
      return mockClient.getInvoicesByCustomer(xentralCustomerId);
    }
  }

  /**
   * Get all sales reps (employees with role=sales)
   *
   * @return List of sales rep employees
   */
  public List<XentralEmployeeDTO> getAllSalesReps() {
    if (mockMode) {
      logger.debug("Mock-Mode: Delegating to MockXentralApiClient");
      return mockClient.getAllSalesReps();
    }

    // Real API implementation
    try {
      logger.debug("Real-API-Mode: Calling Xentral v1 Employees API");
      List<XentralEmployeeDTO> salesReps = realAdapter.getSalesReps();
      logger.info("Real API: Found {} sales reps", salesReps.size());
      return salesReps;

    } catch (Exception e) {
      logger.error("Xentral API failed - fallback to mock data", e);
      return mockClient.getAllSalesReps();
    }
  }

  /**
   * Test connection to Xentral API
   *
   * <p>Used by Admin-UI Connection Test Button
   *
   * @return true if connection successful, false otherwise
   */
  public boolean testConnection() {
    if (mockMode) {
      logger.debug("Mock-Mode: Connection test (always returns true)");
      return mockClient.testConnection();
    }

    // Real API implementation
    try {
      logger.debug("Real-API-Mode: Testing Xentral API connection");
      boolean success = realAdapter.testConnection();
      logger.info("Real API: Connection test {}", success ? "SUCCESSFUL" : "FAILED");
      return success;

    } catch (Exception e) {
      logger.error("Xentral API connection test failed", e);
      return false;
    }
  }

  /**
   * Check if mock mode is enabled
   *
   * @return true if mock mode, false if real API mode
   */
  public boolean isMockMode() {
    return mockMode;
  }
}

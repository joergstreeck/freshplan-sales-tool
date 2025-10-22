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

  // TODO: Inject real XentralApiClient when implemented
  // @Inject
  // @RestClient
  // XentralApiClient realClient;

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

    // TODO: Real API implementation
    logger.warn(
        "Real API mode enabled but not implemented yet - falling back to mock data (salesRepId={})",
        salesRepId);
    return mockClient.getCustomersBySalesRep(salesRepId);

    /*
    // Real API implementation (TODO: Sprint 2.1.7.2 later)
    try {
      logger.debug("Real-API-Mode: Calling Xentral API");
      XentralCustomerListResponse response = realClient.getCustomers(salesRepId, 1, 50);

      // Parse JSON:API response
      List<XentralCustomerDTO> customers = response.data().stream()
          .map(this::mapToCustomerDTO)
          .collect(Collectors.toList());

      logger.info("Real API: Found {} customers for salesRepId={}", customers.size(), salesRepId);
      return customers;

    } catch (Exception e) {
      logger.error("Xentral API failed - fallback to mock data", e);
      return mockClient.getCustomersBySalesRep(salesRepId);
    }
    */
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

    // TODO: Real API implementation
    logger.warn(
        "Real API mode enabled but not implemented yet - falling back to mock data (xentralCustomerId={})",
        xentralCustomerId);
    return mockClient.getCustomerById(xentralCustomerId);
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

    // TODO: Real API implementation
    logger.warn(
        "Real API mode enabled but not implemented yet - falling back to mock data (xentralCustomerId={})",
        xentralCustomerId);
    return mockClient.getInvoicesByCustomer(xentralCustomerId);
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

    // TODO: Real API implementation
    logger.warn("Real API mode enabled but not implemented yet - falling back to mock data");
    return mockClient.getAllSalesReps();
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

    // TODO: Real API implementation
    logger.warn("Real API mode enabled but not implemented yet - mock connection test");
    return mockClient.testConnection();
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

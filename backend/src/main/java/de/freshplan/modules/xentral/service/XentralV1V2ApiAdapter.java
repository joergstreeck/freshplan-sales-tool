package de.freshplan.modules.xentral.service;

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
import de.freshplan.modules.xentral.dto.v2.XentralV2CustomerMapper;
import de.freshplan.modules.xentral.dto.v2.XentralV2CustomerResponse;
import de.freshplan.modules.xentral.service.FinancialMetricsCalculator.FinancialMetrics;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

/**
 * Adapter for Xentral v1/v2 Hybrid API.
 *
 * <p>Aggregates data from multiple Xentral endpoints: - v2 API: /api/v2/customers (base customer
 * data) - v1 API: /api/v1/invoices (invoice list) - v1 API: /api/v1/invoices/{id}/balance
 * (financial details) - v1 API: /api/v1/employees (sales rep matching)
 *
 * <p>Financial metrics are calculated from invoice aggregation: - totalRevenue = SUM(balance.paid)
 * - averageDaysToPay = AVG(DAYS.between(invoice.date, balance.paymentDate)) - lastOrderDate =
 * MAX(invoice.date)
 *
 * <p>Sprint: 2.1.7.2 - D2c Real API Adapter
 *
 * @see XentralV1V2ApiAdapter
 * @see FinancialMetricsCalculator
 */
@ApplicationScoped
public class XentralV1V2ApiAdapter {

  private static final Logger LOG = Logger.getLogger(XentralV1V2ApiAdapter.class);

  @Inject @RestClient XentralCustomersV2Client customersV2Client;

  @Inject @RestClient XentralInvoicesV1Client invoicesV1Client;

  @Inject @RestClient XentralEmployeesV1Client employeesV1Client;

  @Inject XentralV2CustomerMapper customerMapper;

  @Inject XentralV1InvoiceMapper invoiceMapper;

  @Inject FinancialMetricsCalculator financialCalculator;

  @ConfigProperty(name = "xentral.api.token")
  String apiToken;

  private static final int DEFAULT_PAGE_SIZE = 100;

  /**
   * Get all customers with enriched financial data.
   *
   * @return list of customers with financial metrics
   */
  public List<XentralCustomerDTO> getCustomers() {
    LOG.info("Fetching customers from Xentral v2 API...");

    try {
      String authHeader = "Bearer " + apiToken;

      // Fetch customers from v2 API
      XentralV2CustomerResponse response =
          customersV2Client.getCustomers(authHeader, 1, DEFAULT_PAGE_SIZE);

      if (!response.hasData()) {
        LOG.warn("No customers found in Xentral v2 API");
        return List.of();
      }

      LOG.infof("Fetched %d customers from Xentral v2 API", response.data().size());

      // Map and enrich with financial data
      return response.data().stream()
          .map(this::enrichCustomerWithFinancialData)
          .collect(Collectors.toList());

    } catch (Exception e) {
      LOG.errorf(e, "Failed to fetch customers from Xentral API");
      throw new RuntimeException("Failed to fetch customers from Xentral API", e);
    }
  }

  /**
   * Get customers filtered by sales rep ID.
   *
   * @param salesRepId sales rep ID from Xentral
   * @return list of customers for this sales rep with financial metrics
   */
  public List<XentralCustomerDTO> getCustomersBySalesRep(String salesRepId) {
    LOG.infof("Fetching customers for sales rep: %s", salesRepId);

    List<XentralCustomerDTO> allCustomers = getCustomers();

    return allCustomers.stream()
        .filter(customer -> salesRepId.equals(customer.salesRepId()))
        .collect(Collectors.toList());
  }

  /**
   * Get a single customer by ID with enriched financial data.
   *
   * @param customerId customer ID from Xentral
   * @return customer with financial metrics or null if not found
   */
  public XentralCustomerDTO getCustomerById(String customerId) {
    LOG.infof("Fetching customer by ID: %s", customerId);

    try {
      String authHeader = "Bearer " + apiToken;

      XentralV2CustomerResponse response =
          customersV2Client.getCustomerById(authHeader, customerId);

      if (!response.hasData()) {
        LOG.warnf("Customer not found: %s", customerId);
        return null;
      }

      XentralV2Customer customer = response.data().get(0);
      return enrichCustomerWithFinancialData(customer);

    } catch (Exception e) {
      LOG.errorf(e, "Failed to fetch customer from Xentral API: %s", customerId);
      throw new RuntimeException("Failed to fetch customer from Xentral API", e);
    }
  }

  /**
   * Get invoices for a specific customer.
   *
   * @param customerId customer ID from Xentral
   * @return list of invoices with payment details
   */
  public List<XentralInvoiceDTO> getInvoicesByCustomer(String customerId) {
    LOG.infof("Fetching invoices for customer: %s", customerId);

    try {
      String authHeader = "Bearer " + apiToken;

      XentralV1InvoiceResponse response =
          invoicesV1Client.getInvoicesByCustomer(authHeader, customerId, 1, DEFAULT_PAGE_SIZE);

      if (!response.hasData()) {
        LOG.debugf("No invoices found for customer: %s", customerId);
        return List.of();
      }

      // Fetch balance data for each invoice
      Map<String, XentralV1InvoiceBalance> balances = fetchBalancesForInvoices(response.data());

      // Map to DTOs
      return response.data().stream()
          .map(invoice -> invoiceMapper.toDTO(invoice, balances.get(invoice.id())))
          .collect(Collectors.toList());

    } catch (Exception e) {
      LOG.errorf(e, "Failed to fetch invoices for customer: %s", customerId);
      return List.of(); // Return empty list on error (graceful degradation)
    }
  }

  /**
   * Get all sales reps (employees) from Xentral.
   *
   * @return list of employees mapped to XentralEmployeeDTO
   */
  public List<XentralEmployeeDTO> getSalesReps() {
    LOG.info("Fetching employees from Xentral v1 API...");

    try {
      String authHeader = "Bearer " + apiToken;

      XentralEmployeesV1Client.XentralV1EmployeeResponse response =
          employeesV1Client.getEmployees(authHeader, 1, DEFAULT_PAGE_SIZE);

      if (response.data() == null || response.data().isEmpty()) {
        LOG.warn("No employees found in Xentral v1 API");
        return List.of();
      }

      return response.data().stream()
          .map(
              emp ->
                  new XentralEmployeeDTO(
                      emp.id(), emp.firstName(), emp.lastName(), emp.email(), emp.role()))
          .collect(Collectors.toList());

    } catch (Exception e) {
      LOG.errorf(e, "Failed to fetch employees from Xentral API");
      return List.of();
    }
  }

  // --- Private Helper Methods ---

  /**
   * Enriches a v2 customer with financial metrics calculated from invoices.
   *
   * @param customer v2 customer from API
   * @return customer DTO with financial data
   */
  private XentralCustomerDTO enrichCustomerWithFinancialData(XentralV2Customer customer) {
    // Map customer to DTO (without financial data)
    XentralCustomerDTO baseCustomer = customerMapper.toDTO(customer);

    // Fetch invoices and calculate financial metrics
    try {
      String authHeader = "Bearer " + apiToken;

      XentralV1InvoiceResponse invoiceResponse =
          invoicesV1Client.getInvoicesByCustomer(authHeader, customer.id(), 1, DEFAULT_PAGE_SIZE);

      if (!invoiceResponse.hasData()) {
        LOG.debugf("No invoices found for customer: %s", customer.id());
        return baseCustomer; // Return customer without financial data
      }

      // Fetch balance data for all invoices
      Map<String, XentralV1InvoiceBalance> balances =
          fetchBalancesForInvoices(invoiceResponse.data());

      // Calculate financial metrics
      FinancialMetrics metrics = financialCalculator.calculate(invoiceResponse.data(), balances);

      // Enrich customer with financial data
      return customerMapper.enrichWithFinancialData(
          baseCustomer,
          metrics.totalRevenue(),
          metrics.averageDaysToPay(),
          metrics.lastOrderDate());

    } catch (Exception e) {
      LOG.errorf(e, "Failed to enrich customer with financial data: %s", customer.id());
      return baseCustomer; // Return customer without financial data on error
    }
  }

  /**
   * Fetches balance data for multiple invoices in parallel.
   *
   * <p>Note: In production, consider batching or caching to reduce API calls.
   *
   * @param invoices list of invoices
   * @return map of invoice ID to balance data
   */
  private Map<String, XentralV1InvoiceBalance> fetchBalancesForInvoices(
      List<XentralV1Invoice> invoices) {

    String authHeader = "Bearer " + apiToken;
    Map<String, XentralV1InvoiceBalance> balances = new HashMap<>();

    for (XentralV1Invoice invoice : invoices) {
      try {
        XentralV1InvoiceBalance balance =
            invoicesV1Client.getInvoiceBalance(authHeader, invoice.id());
        balances.put(invoice.id(), balance);
      } catch (Exception e) {
        LOG.warnf(e, "Failed to fetch balance for invoice: %s", invoice.id());
        // Continue without balance data for this invoice
      }
    }

    return balances;
  }

  /**
   * Test connection to Xentral API.
   *
   * @return true if connection successful, false otherwise
   */
  public boolean testConnection() {
    LOG.info("Testing connection to Xentral API...");

    try {
      String authHeader = "Bearer " + apiToken;
      XentralV2CustomerResponse response = customersV2Client.getCustomers(authHeader, 1, 1);
      LOG.info("Xentral API connection test successful");
      return true;
    } catch (Exception e) {
      LOG.errorf(e, "Xentral API connection test failed");
      return false;
    }
  }
}

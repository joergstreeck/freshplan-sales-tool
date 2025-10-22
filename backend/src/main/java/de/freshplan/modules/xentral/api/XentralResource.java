package de.freshplan.modules.xentral.api;

import de.freshplan.modules.xentral.dto.XentralCustomerDTO;
import de.freshplan.modules.xentral.dto.XentralEmployeeDTO;
import de.freshplan.modules.xentral.dto.XentralInvoiceDTO;
import de.freshplan.modules.xentral.service.XentralApiService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Xentral Integration REST Resource
 *
 * <p>Sprint 2.1.7.2: Xentral Integration
 *
 * <p>Provides REST API for Xentral ERP data access.
 *
 * <p>Endpoints: - GET /api/xentral/customers?salesRepId={id} - GET
 * /api/xentral/customers/{xentralId} - GET /api/xentral/invoices?customerId={id} - GET
 * /api/xentral/employees/sales-reps - GET /api/xentral/test-connection
 *
 * <p>Security: @RolesAllowed("user") - requires authentication
 *
 * <p>Note: This resource delegates to XentralApiService which handles Mock vs Real API switching.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/xentral")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class XentralResource {

  private static final Logger logger = LoggerFactory.getLogger(XentralResource.class);

  @Inject XentralApiService xentralService;

  /**
   * Get customers by sales rep ID
   *
   * <p>Returns customers assigned to the specified sales rep in Xentral.
   *
   * <p>Used by: ConvertToCustomerDialog (Autocomplete Dropdown)
   *
   * @param salesRepId Xentral Employee ID (Sales Rep)
   * @return List of customers (200 OK) or 400 if salesRepId is missing
   */
  @GET
  @Path("/customers")
  public Response getCustomers(@QueryParam("salesRepId") String salesRepId) {
    logger.debug("GET /api/xentral/customers?salesRepId={}", salesRepId);

    if (salesRepId == null || salesRepId.isBlank()) {
      logger.warn("Missing required query parameter: salesRepId");
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("{\"error\": \"Missing required parameter: salesRepId\"}")
          .build();
    }

    List<XentralCustomerDTO> customers = xentralService.getCustomersBySalesRep(salesRepId);

    logger.info("Returning {} customers for salesRepId={}", customers.size(), salesRepId);
    return Response.ok(customers).build();
  }

  /**
   * Get single customer by Xentral ID
   *
   * <p>Returns detailed customer data including financial metrics.
   *
   * <p>Used by: Customer-Dashboard (Revenue Metrics Widget)
   *
   * @param xentralId Xentral Customer ID
   * @return Customer (200 OK) or 404 if not found
   */
  @GET
  @Path("/customers/{xentralId}")
  public Response getCustomerById(@PathParam("xentralId") String xentralId) {
    logger.debug("GET /api/xentral/customers/{}", xentralId);

    XentralCustomerDTO customer = xentralService.getCustomerById(xentralId);

    if (customer == null) {
      logger.warn("Customer not found: xentralId={}", xentralId);
      return Response.status(Response.Status.NOT_FOUND)
          .entity("{\"error\": \"Customer not found: " + xentralId + "\"}")
          .build();
    }

    logger.info("Returning customer: xentralId={}", xentralId);
    return Response.ok(customer).build();
  }

  /**
   * Get invoices by customer ID
   *
   * <p>Returns invoices for the specified customer.
   *
   * <p>Used by: Customer-Dashboard (Payment Behavior Calculation)
   *
   * @param customerId Xentral Customer ID
   * @return List of invoices (200 OK) or 400 if customerId is missing
   */
  @GET
  @Path("/invoices")
  public Response getInvoices(@QueryParam("customerId") String customerId) {
    logger.debug("GET /api/xentral/invoices?customerId={}", customerId);

    if (customerId == null || customerId.isBlank()) {
      logger.warn("Missing required query parameter: customerId");
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("{\"error\": \"Missing required parameter: customerId\"}")
          .build();
    }

    List<XentralInvoiceDTO> invoices = xentralService.getInvoicesByCustomer(customerId);

    logger.info("Returning {} invoices for customerId={}", invoices.size(), customerId);
    return Response.ok(invoices).build();
  }

  /**
   * Get all sales reps
   *
   * <p>Returns employees with role=sales.
   *
   * <p>Used by: Sales-Rep Mapping Auto-Sync Job
   *
   * @return List of sales rep employees (200 OK)
   */
  @GET
  @Path("/employees/sales-reps")
  public Response getSalesReps() {
    logger.debug("GET /api/xentral/employees/sales-reps");

    List<XentralEmployeeDTO> salesReps = xentralService.getAllSalesReps();

    logger.info("Returning {} sales reps", salesReps.size());
    return Response.ok(salesReps).build();
  }

  /**
   * Test connection to Xentral API
   *
   * <p>Used by: Admin-UI (XentralSettingsPage Connection Test Button)
   *
   * @return Connection test result (200 OK with boolean) or 500 if test fails
   */
  @GET
  @Path("/test-connection")
  @RolesAllowed("admin")
  public Response testConnection() {
    logger.debug("GET /api/xentral/test-connection");

    boolean success = xentralService.testConnection();

    if (success) {
      logger.info("Xentral API connection test: SUCCESS");
      return Response.ok()
          .entity(
              "{\"success\": true, \"message\": \"Connection successful\", \"mockMode\": "
                  + xentralService.isMockMode()
                  + "}")
          .build();
    } else {
      logger.error("Xentral API connection test: FAILED");
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("{\"success\": false, \"message\": \"Connection failed\"}")
          .build();
    }
  }
}

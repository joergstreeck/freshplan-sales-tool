package de.freshplan.api.resources;

import de.freshplan.domain.customer.constants.CustomerConstants;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.service.CustomerService;
import de.freshplan.domain.customer.service.dto.*;
import de.freshplan.infrastructure.security.CurrentUser;
import de.freshplan.infrastructure.security.SecurityAudit;
import de.freshplan.infrastructure.security.SecurityContextProvider;
import de.freshplan.infrastructure.security.UserPrincipal;
import de.freshplan.shared.constants.PaginationConstants;
import de.freshplan.shared.constants.RiskManagementConstants;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

/**
 * REST API for Customer Management. Provides comprehensive CRUD operations, search, analytics, and
 * hierarchy management.
 *
 * <p>All endpoints follow RESTful conventions and return consistent JSON responses. Authentication
 * and authorization are handled at the application level.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "manager", "sales"})
@SecurityAudit
public class CustomerResource {

  @Inject CustomerService customerService;

  @Inject SecurityContextProvider securityContext;

  @Inject @CurrentUser UserPrincipal currentUser;

  // ========== CRUD OPERATIONS ==========

  /**
   * Creates a new customer. Requires admin or manager role for creating customers.
   *
   * @param request The customer creation request
   * @return 201 Created with customer response
   */
  @POST
  @RolesAllowed({"admin", "manager"})
  public Response createCustomer(@Valid CreateCustomerRequest request) {
    // Additional security: verify role programmatically for audit
    securityContext.requireAnyRole("admin", "manager");

    CustomerResponse customer = customerService.createCustomer(request, currentUser.getUsername());

    return Response.status(Response.Status.CREATED).entity(customer).build();
  }

  /**
   * Retrieves a customer by ID.
   *
   * @param id The customer ID
   * @return 200 OK with customer response, or 404 if not found
   */
  @GET
  @Path("/{id}")
  public Response getCustomer(@PathParam("id") UUID id) {
    CustomerResponse customer = customerService.getCustomer(id);
    return Response.ok(customer).build();
  }

  /**
   * Updates an existing customer. Requires admin or manager role for customer updates.
   *
   * @param id The customer ID
   * @param request The update request
   * @return 200 OK with updated customer response
   */
  @PUT
  @Path("/{id}")
  @RolesAllowed({"admin", "manager"})
  public Response updateCustomer(@PathParam("id") UUID id, @Valid UpdateCustomerRequest request) {
    securityContext.requireAnyRole("admin", "manager");

    CustomerResponse customer =
        customerService.updateCustomer(id, request, currentUser.getUsername());
    return Response.ok(customer).build();
  }

  /**
   * Soft deletes a customer. Requires admin role only - deletion is a critical operation.
   *
   * @param id The customer ID
   * @param reason The reason for deletion (query parameter)
   * @return 204 No Content
   */
  @DELETE
  @Path("/{id}")
  @RolesAllowed({"admin"})
  public Response deleteCustomer(
      @PathParam("id") UUID id,
      @QueryParam("reason") @DefaultValue("No reason provided") String reason) {
    securityContext.requireRole("admin");

    customerService.deleteCustomer(id, currentUser.getUsername(), reason);
    return Response.noContent().build();
  }

  /**
   * Restores a soft-deleted customer.
   *
   * @param id The customer ID
   * @return 200 OK with restored customer response
   */
  @PUT
  @Path("/{id}/restore")
  public Response restoreCustomer(@PathParam("id") UUID id) {
    CustomerResponse customer = customerService.restoreCustomer(id, currentUser.getUsername());
    return Response.ok(customer).build();
  }

  // ========== LIST & SEARCH OPERATIONS ==========

  /**
   * Gets all customers with pagination and optional filtering.
   *
   * @param page The page number (0-based, default 0)
   * @param size The page size (default 20, max 100)
   * @param status Optional status filter
   * @param industry Optional industry filter
   * @return 200 OK with paginated customer list
   */
  @GET
  public Response getAllCustomers(
      @QueryParam("page") @DefaultValue(PaginationConstants.DEFAULT_PAGE_NUMBER_STRING) int page,
      @QueryParam("size") @DefaultValue(PaginationConstants.DEFAULT_PAGE_SIZE_STRING) int size,
      @QueryParam("status") CustomerStatus status,
      @QueryParam("industry") Industry industry) {

    // Validate pagination parameters
    if (page < 0) page = 0;
    if (size > PaginationConstants.MAX_PAGE_SIZE) size = PaginationConstants.MAX_PAGE_SIZE;

    CustomerListResponse customers;

    if (status != null) {
      customers = customerService.getCustomersByStatus(status, page, size);
    } else if (industry != null) {
      customers = customerService.getCustomersByIndustry(industry, page, size);
    } else {
      customers = customerService.getAllCustomers(page, size);
    }

    return Response.ok(customers).build();
  }

  // Note: Search functionality has been moved to POST /api/customers/search
  // See CustomerSearchResource for the new implementation

  // ========== ANALYTICS & DASHBOARD ==========

  /**
   * Gets dashboard statistics and metrics.
   *
   * @return 200 OK with dashboard data
   */
  @GET
  @Path("/dashboard")
  public Response getDashboardData() {
    CustomerDashboardResponse dashboard = customerService.getDashboardData();
    return Response.ok(dashboard).build();
  }

  /**
   * Gets customers at risk based on risk score.
   *
   * @param minRiskScore Minimum risk score (default 70)
   * @param page The page number (0-based, default 0)
   * @param size The page size (default 20, max 100)
   * @return 200 OK with at-risk customers
   */
  @GET
  @Path("/analytics/risk-assessment")
  public Response getCustomersAtRisk(
      @QueryParam("minRiskScore") @DefaultValue(RiskManagementConstants.DEFAULT_RISK_SCORE_STRING)
          int minRiskScore,
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue(PaginationConstants.DEFAULT_PAGE_SIZE_STRING) int size) {

    // Validate parameters
    if (page < 0) page = CustomerConstants.DEFAULT_PAGE_NUMBER;
    if (size <= 0 || size > CustomerConstants.MAX_PAGE_SIZE) {
      size = CustomerConstants.DEFAULT_PAGE_SIZE;
    }
    if (minRiskScore < 0 || minRiskScore > 100) {
      minRiskScore = CustomerConstants.DEFAULT_RISK_THRESHOLD;
    }

    CustomerListResponse customers = customerService.getCustomersAtRisk(minRiskScore, page, size);
    return Response.ok(customers).build();
  }

  // ========== HIERARCHY MANAGEMENT ==========

  /**
   * Gets the hierarchy tree for a customer.
   *
   * @param id The customer ID
   * @return 200 OK with customer hierarchy
   */
  @GET
  @Path("/{id}/hierarchy")
  public Response getCustomerHierarchy(@PathParam("id") UUID id) {
    CustomerResponse hierarchy = customerService.getCustomerHierarchy(id);
    return Response.ok(hierarchy).build();
  }

  /**
   * Adds a child customer to a parent.
   *
   * @param parentId The parent customer ID
   * @param childId The child customer ID (from request body)
   * @return 200 OK with updated child customer
   */
  @POST
  @Path("/{parentId}/children")
  public Response addChildCustomer(
      @PathParam("parentId") UUID parentId, AddChildCustomerRequest request) {
    CustomerResponse child =
        customerService.addChildCustomer(parentId, request.childId(), currentUser.getUsername());
    return Response.ok(child).build();
  }

  // ========== UTILITY OPERATIONS ==========

  /**
   * Checks for potential duplicate customers.
   *
   * @param request The duplicate check request
   * @return 200 OK with list of potential duplicates
   */
  @POST
  @Path("/check-duplicates")
  public Response checkDuplicates(@Valid CheckDuplicatesRequest request) {
    List<CustomerResponse> duplicates = customerService.checkDuplicates(request.companyName());
    return Response.ok(duplicates).build();
  }

  /**
   * Merges two customers (keeps target, deletes source).
   *
   * @param targetId The target customer ID (to keep)
   * @param request The merge request containing source customer ID
   * @return 200 OK with merged customer
   */
  @POST
  @Path("/{targetId}/merge")
  public Response mergeCustomers(
      @PathParam("targetId") UUID targetId, @Valid MergeCustomersRequest request) {
    CustomerResponse customer =
        customerService.mergeCustomers(targetId, request.sourceId(), currentUser.getUsername());
    return Response.ok(customer).build();
  }

  /**
   * Changes customer status with business rule validation.
   *
   * @param id The customer ID
   * @param request The status change request
   * @return 200 OK with updated customer
   */
  @PUT
  @Path("/{id}/status")
  public Response changeCustomerStatus(
      @PathParam("id") UUID id, @Valid ChangeStatusRequest request) {
    CustomerResponse customer =
        customerService.changeStatus(id, request.newStatus(), currentUser.getUsername());
    return Response.ok(customer).build();
  }
}

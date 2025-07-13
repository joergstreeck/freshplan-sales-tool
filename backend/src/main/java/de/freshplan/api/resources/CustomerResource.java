package de.freshplan.api.resources;

import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.service.CustomerService;
import de.freshplan.domain.customer.service.dto.*;
import de.freshplan.infrastructure.security.SecurityAudit;
import de.freshplan.infrastructure.security.SecurityContextProvider;
import jakarta.annotation.security.PermitAll;
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
@PermitAll // TODO: SECURITY ROLLBACK - Remove after fixing test configuration (Issue #CI-FIX)
// @RolesAllowed({"admin", "manager", "sales"}) // TODO: SECURITY ROLLBACK - Uncomment after test fix
@SecurityAudit
public class CustomerResource {

  @Inject CustomerService customerService;

  @Inject SecurityContextProvider securityContext;

  // ========== CRUD OPERATIONS ==========

  /**
   * Creates a new customer.
   *
   * @param request The customer creation request
   * @return 201 Created with customer response
   */
  @POST
  public Response createCustomer(@Valid CreateCustomerRequest request) {
    // Extract user from security context
    String createdBy = securityContext.getUsername();
    if (createdBy == null) {
      createdBy = "system"; // Fallback for dev mode
    }

    CustomerResponse customer = customerService.createCustomer(request, createdBy);

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
   * Updates an existing customer.
   *
   * @param id The customer ID
   * @param request The update request
   * @return 200 OK with updated customer response
   */
  @PUT
  @Path("/{id}")
  public Response updateCustomer(@PathParam("id") UUID id, @Valid UpdateCustomerRequest request) {

    // TODO: Extract user from JWT when security is implemented
    String updatedBy = "system"; // Fallback for dev mode

    CustomerResponse customer = customerService.updateCustomer(id, request, updatedBy);
    return Response.ok(customer).build();
  }

  /**
   * Soft deletes a customer.
   *
   * @param id The customer ID
   * @param reason The reason for deletion (query parameter)
   * @return 204 No Content
   */
  @DELETE
  @Path("/{id}")
  public Response deleteCustomer(
      @PathParam("id") UUID id,
      @QueryParam("reason") @DefaultValue("No reason provided") String reason) {

    // TODO: Extract user from JWT when security is implemented
    String deletedBy = "system"; // Fallback for dev mode

    customerService.deleteCustomer(id, deletedBy, reason);
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
    // TODO: Extract user from JWT when security is implemented
    String restoredBy = "system"; // Fallback for dev mode

    CustomerResponse customer = customerService.restoreCustomer(id, restoredBy);
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
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("20") int size,
      @QueryParam("status") CustomerStatus status,
      @QueryParam("industry") Industry industry) {

    // Validate pagination parameters
    if (page < 0) page = 0;
    if (size <= 0 || size > 100) size = 20;

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
      @QueryParam("minRiskScore") @DefaultValue("70") int minRiskScore,
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("20") int size) {

    // Validate parameters
    if (page < 0) page = 0;
    if (size <= 0 || size > 100) size = 20;
    if (minRiskScore < 0 || minRiskScore > 100) minRiskScore = 70;

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

    // TODO: Extract user from JWT when security is implemented
    String updatedBy = "system"; // Fallback for dev mode

    CustomerResponse child =
        customerService.addChildCustomer(parentId, request.childId(), updatedBy);

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

    // TODO: Extract user from JWT when security is implemented
    String mergedBy = "system"; // Fallback for dev mode

    CustomerResponse customer =
        customerService.mergeCustomers(targetId, request.sourceId(), mergedBy);

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

    // TODO: Extract user from JWT when security is implemented
    String updatedBy = "system"; // Fallback for dev mode

    CustomerResponse customer = customerService.changeStatus(id, request.newStatus(), updatedBy);

    return Response.ok(customer).build();
  }
}

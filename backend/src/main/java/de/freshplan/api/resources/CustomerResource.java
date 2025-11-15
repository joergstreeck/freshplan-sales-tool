package de.freshplan.api.resources;

import de.freshplan.domain.customer.constants.CustomerConstants;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.service.CustomerService;
import de.freshplan.domain.customer.service.command.CustomerCommandService;
import de.freshplan.domain.customer.service.dto.*;
import de.freshplan.domain.customer.service.query.CustomerQueryService;
import de.freshplan.infrastructure.security.CurrentUser;
import de.freshplan.infrastructure.security.SecurityAudit;
import de.freshplan.infrastructure.security.SecurityContextProvider;
import de.freshplan.infrastructure.security.UserPrincipal;
import de.freshplan.shared.constants.PaginationConstants;
import de.freshplan.shared.constants.RiskManagementConstants;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST API for Customer Management. Provides comprehensive CRUD operations, search, analytics, and
 * hierarchy management.
 *
 * <p>All endpoints follow RESTful conventions and return consistent JSON responses. Authentication
 * and authorization are handled at the application level.
 *
 * <p>This resource acts as a Facade and can switch between legacy CustomerService and the new CQRS
 * services (CustomerCommandService/CustomerQueryService) via feature flag.
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

  private static final Logger log = LoggerFactory.getLogger(CustomerResource.class);

  @ConfigProperty(name = "features.cqrs.enabled", defaultValue = "false")
  boolean cqrsEnabled;

  @ConfigProperty(name = "features.cqrs.customers.list.enabled", defaultValue = "false")
  boolean customersListCqrsEnabled;

  @Inject CustomerService customerService; // Legacy service

  @Inject CustomerCommandService commandService; // New CQRS command service

  @Inject CustomerQueryService queryService; // New CQRS query service

  @Inject SecurityContextProvider securityContext;

  @Inject @CurrentUser UserPrincipal currentUser;

  @Inject
  de.freshplan.domain.opportunity.service.OpportunityService
      opportunityService; // For customer opportunities

  @Inject de.freshplan.modules.xentral.service.RevenueMetricsService revenueMetricsService;

  @Inject de.freshplan.domain.customer.service.BranchService branchService; // Sprint 2.1.7.7

  @Inject
  de.freshplan.domain.customer.service.HierarchyMetricsService
      hierarchyMetricsService; // Sprint 2.1.7.7 D3+D5

  @Inject Clock clock; // For audit timestamps (Sprint 2.1.7.2 D9.3)

  // ========== CRUD OPERATIONS ==========

  /**
   * Creates a new customer. Requires admin or manager role for creating customers.
   *
   * @param request The customer creation request
   * @return 201 Created with customer response
   */
  @POST
  @RolesAllowed({"admin", "manager"})
  @APIResponses({
    @APIResponse(
        responseCode = "201",
        description = "Customer created successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CustomerResponse.class))),
    @APIResponse(responseCode = "400", description = "Invalid request data"),
    @APIResponse(responseCode = "401", description = "Unauthorized - authentication required"),
    @APIResponse(responseCode = "403", description = "Forbidden - admin or manager role required")
  })
  public Response createCustomer(@Valid CreateCustomerRequest request) {
    // Additional security: verify role programmatically for audit
    securityContext.requireAnyRole("admin", "manager");

    CustomerResponse customer;
    if (cqrsEnabled) {
      log.debug("Using CQRS CommandService for createCustomer");
      customer = commandService.createCustomer(request, currentUser.getUsername());
    } else {
      log.debug("Using legacy CustomerService for createCustomer");
      customer = customerService.createCustomer(request, currentUser.getUsername());
    }

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
    CustomerResponse customer;
    if (cqrsEnabled) {
      log.debug("Using CQRS QueryService for getCustomer");
      customer = queryService.getCustomer(id);
    } else {
      log.debug("Using legacy CustomerService for getCustomer");
      customer = customerService.getCustomer(id);
    }
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

    CustomerResponse customer;
    if (cqrsEnabled) {
      log.debug("Using CQRS CommandService for updateCustomer");
      customer = commandService.updateCustomer(id, request, currentUser.getUsername());
    } else {
      log.debug("Using legacy CustomerService for updateCustomer");
      customer = customerService.updateCustomer(id, request, currentUser.getUsername());
    }
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

    if (cqrsEnabled) {
      log.debug("Using CQRS CommandService for deleteCustomer");
      commandService.deleteCustomer(id, currentUser.getUsername(), reason);
    } else {
      log.debug("Using legacy CustomerService for deleteCustomer");
      customerService.deleteCustomer(id, currentUser.getUsername(), reason);
    }
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
    CustomerResponse customer;
    if (cqrsEnabled) {
      log.debug("Using CQRS CommandService for restoreCustomer");
      customer = commandService.restoreCustomer(id, currentUser.getUsername());
    } else {
      log.debug("Using legacy CustomerService for restoreCustomer");
      customer = customerService.restoreCustomer(id, currentUser.getUsername());
    }
    return Response.ok(customer).build();
  }

  // ========== LIST & SEARCH OPERATIONS ==========

  /**
   * Gets all customers with pagination and optional filtering.
   *
   * @param page The page number (0-based, default 0)
   * @param size The page size (default 20, max 100)
   * @param status Optional status filter
   * @return 200 OK with paginated customer list
   */
  @GET
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Paginated list of customers",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CustomerListResponse.class))),
    @APIResponse(responseCode = "401", description = "Unauthorized - authentication required")
  })
  public Response getAllCustomers(
      @QueryParam("page") @DefaultValue(PaginationConstants.DEFAULT_PAGE_NUMBER_STRING) int page,
      @QueryParam("size") @DefaultValue(PaginationConstants.DEFAULT_PAGE_SIZE_STRING) int size,
      @QueryParam("status") CustomerStatus status) {

    // Validate pagination parameters
    if (page < 0) page = 0;
    if (size > PaginationConstants.MAX_PAGE_SIZE) size = PaginationConstants.MAX_PAGE_SIZE;

    CustomerListResponse customers;

    // Check if CQRS is enabled AND if list operations should use CQRS
    // This allows fine-grained control over performance-critical endpoints
    boolean useCqrsForList = cqrsEnabled && customersListCqrsEnabled;

    if (useCqrsForList) {
      log.debug("Using CQRS QueryService for getAllCustomers (both flags enabled)");
      if (status != null) {
        customers = queryService.getCustomersByStatus(status, page, size);
      } else {
        customers = queryService.getAllCustomers(page, size);
      }
    } else {
      log.debug(
          "Using legacy CustomerService for getAllCustomers (cqrs={}, list={})",
          cqrsEnabled,
          customersListCqrsEnabled);
      if (status != null) {
        customers = customerService.getCustomersByStatus(status, page, size);
      } else {
        customers = customerService.getAllCustomers(page, size);
      }
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
    CustomerDashboardResponse dashboard;
    if (cqrsEnabled) {
      log.debug("Using CQRS QueryService for getDashboardData");
      dashboard = queryService.getDashboardData();
    } else {
      log.debug("Using legacy CustomerService for getDashboardData");
      dashboard = customerService.getDashboardData();
    }
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

    CustomerListResponse customers;
    if (cqrsEnabled) {
      log.debug("Using CQRS QueryService for getCustomersAtRisk");
      customers = queryService.getCustomersAtRisk(minRiskScore, page, size);
    } else {
      log.debug("Using legacy CustomerService for getCustomersAtRisk");
      customers = customerService.getCustomersAtRisk(minRiskScore, page, size);
    }
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
    CustomerResponse hierarchy;
    if (cqrsEnabled) {
      log.debug("Using CQRS QueryService for getCustomerHierarchy");
      hierarchy = queryService.getCustomerHierarchy(id);
    } else {
      log.debug("Using legacy CustomerService for getCustomerHierarchy");
      hierarchy = customerService.getCustomerHierarchy(id);
    }
    return Response.ok(hierarchy).build();
  }

  /**
   * Gets hierarchy metrics for a HEADQUARTER customer.
   *
   * <p>Sprint 2.1.7.7 - D5: Multi-Location Management - Frontend Dashboard Integration
   *
   * <p>Returns aggregated metrics across all child branches (FILIALE customers): total revenue,
   * average revenue, branch count, open opportunities, and detailed branch breakdown.
   *
   * @param id The parent customer ID (must be HEADQUARTER)
   * @return 200 OK with hierarchy metrics
   * @throws IllegalArgumentException if customer not found
   * @throws InvalidHierarchyException if customer is not a HEADQUARTER
   */
  @GET
  @Path("/{id}/hierarchy/metrics")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Hierarchy metrics retrieved successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema =
                    @Schema(
                        implementation =
                            de.freshplan.domain.customer.service.HierarchyMetricsService
                                .HierarchyMetrics.class))),
    @APIResponse(
        responseCode = "400",
        description = "Customer is not a HEADQUARTER (InvalidHierarchyException)"),
    @APIResponse(responseCode = "404", description = "Customer not found"),
    @APIResponse(responseCode = "401", description = "Unauthorized - authentication required")
  })
  public Response getHierarchyMetrics(@PathParam("id") UUID id) {
    log.debug("Fetching hierarchy metrics for customer: {}", id);
    de.freshplan.domain.customer.service.HierarchyMetricsService.HierarchyMetrics metrics =
        hierarchyMetricsService.getHierarchyMetrics(id);
    return Response.ok(metrics).build();
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
    CustomerResponse child;
    if (cqrsEnabled) {
      log.debug("Using CQRS CommandService for addChildCustomer");
      child =
          commandService.addChildCustomer(parentId, request.childId(), currentUser.getUsername());
    } else {
      log.debug("Using legacy CustomerService for addChildCustomer");
      child =
          customerService.addChildCustomer(parentId, request.childId(), currentUser.getUsername());
    }
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
    List<CustomerResponse> duplicates;
    if (cqrsEnabled) {
      log.debug("Using CQRS QueryService for checkDuplicates");
      duplicates = queryService.checkDuplicates(request.companyName());
    } else {
      log.debug("Using legacy CustomerService for checkDuplicates");
      duplicates = customerService.checkDuplicates(request.companyName());
    }
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
    CustomerResponse customer;
    if (cqrsEnabled) {
      log.debug("Using CQRS CommandService for mergeCustomers");
      customer =
          commandService.mergeCustomers(targetId, request.sourceId(), currentUser.getUsername());
    } else {
      log.debug("Using legacy CustomerService for mergeCustomers");
      customer =
          customerService.mergeCustomers(targetId, request.sourceId(), currentUser.getUsername());
    }
    return Response.ok(customer).build();
  }

  /**
   * Changes customer status with role-based authorization.
   *
   * <p>Sprint 2.1.7.2 D11: 3-Tier Role-Based Status Update
   *
   * <p><strong>Authorization Rules:</strong>
   *
   * <ul>
   *   <li><strong>SALES (sales):</strong> Can only change AKTIV ↔ RISIKO
   *   <li><strong>MANAGER (manager):</strong> Can also set INAKTIV (+ AKTIV ↔ RISIKO)
   *   <li><strong>ADMIN (admin):</strong> Can set all statuses including ARCHIVIERT
   * </ul>
   *
   * <p><strong>Business Rules:</strong> System sets status automatically (Lead→AKTIV,
   * Churn→RISIKO), but users can manually override based on their role permissions.
   *
   * @param id The customer ID
   * @param request The status change request
   * @return 200 OK with updated customer, 403 if role lacks permission
   */
  @PUT
  @Path("/{id}/status")
  public Response changeCustomerStatus(
      @PathParam("id") UUID id, @Valid ChangeStatusRequest request) {

    log.info(
        "Status change request for customer {}: {} (user: {}, roles: {})",
        id,
        request.newStatus(),
        currentUser.getUsername(),
        currentUser.getRoles());

    // 1. Role-Based Authorization Check
    CustomerStatus targetStatus = request.newStatus();
    boolean isAdmin = securityContext.hasRole("admin");
    boolean isManager = securityContext.hasRole("manager");
    boolean isSales = securityContext.hasRole("sales");

    // ADMIN: Can set all statuses (no restrictions)
    if (isAdmin) {
      log.debug("Admin user - all status changes allowed");
    }
    // MANAGER: Can set AKTIV, RISIKO, INAKTIV (but NOT ARCHIVIERT)
    else if (isManager) {
      if (targetStatus == CustomerStatus.ARCHIVIERT) {
        log.warn(
            "Manager {} tried to set ARCHIVIERT status - requires ADMIN role",
            currentUser.getUsername());
        return Response.status(Response.Status.FORBIDDEN)
            .entity(
                new ErrorResponse(
                    "Only ADMIN can set ARCHIVIERT status. Contact your administrator.",
                    "INSUFFICIENT_PERMISSIONS"))
            .build();
      }
      log.debug("Manager user - can set AKTIV, RISIKO, INAKTIV");
    }
    // SALES: Can ONLY set AKTIV or RISIKO (no INAKTIV, no ARCHIVIERT)
    else if (isSales) {
      if (targetStatus != CustomerStatus.AKTIV && targetStatus != CustomerStatus.RISIKO) {
        log.warn(
            "Sales user {} tried to set {} status - only AKTIV/RISIKO allowed",
            currentUser.getUsername(),
            targetStatus);
        return Response.status(Response.Status.FORBIDDEN)
            .entity(
                new ErrorResponse(
                    "Sales users can only change between AKTIV and RISIKO. Contact your manager to"
                        + " set other statuses.",
                    "INSUFFICIENT_PERMISSIONS"))
            .build();
      }
      log.debug("Sales user - can only set AKTIV or RISIKO");
    }
    // Fallback: No valid role (should not happen due to @RolesAllowed on class level)
    else {
      log.error("User {} has no valid role for status changes", currentUser.getUsername());
      return Response.status(Response.Status.FORBIDDEN)
          .entity(
              new ErrorResponse(
                  "Insufficient permissions for status changes", "INSUFFICIENT_PERMISSIONS"))
          .build();
    }

    // 2. Execute Status Change (authorization passed)
    CustomerResponse customer;
    if (cqrsEnabled) {
      log.debug("Using CQRS CommandService for changeStatus");
      customer = commandService.changeStatus(id, request.newStatus(), currentUser.getUsername());
    } else {
      log.debug("Using legacy CustomerService for changeStatus");
      customer = customerService.changeStatus(id, request.newStatus(), currentUser.getUsername());
    }

    log.info(
        "Status changed successfully for customer {}: {} (by: {})",
        id,
        request.newStatus(),
        currentUser.getUsername());

    return Response.ok(customer).build();
  }

  // ========== OPPORTUNITY INTEGRATION ==========

  /**
   * Gets all opportunities for a specific customer.
   *
   * <p>Returns opportunities regardless of stage (Offen/Gewonnen/Verloren). Frontend groups them by
   * status. Results sorted by creation date descending (newest first).
   *
   * @param customerId The customer ID
   * @return 200 OK with list of opportunities
   * @since Sprint 2.1.7.3 - Customer → Opportunity Workflow
   */
  @GET
  @Path("/{customerId}/opportunities")
  public Response getCustomerOpportunities(@PathParam("customerId") UUID customerId) {
    log.debug("Fetching opportunities for customer: {}", customerId);

    // Verify customer exists (will throw if not found)
    if (cqrsEnabled) {
      queryService.getCustomer(customerId);
    } else {
      customerService.getCustomer(customerId);
    }

    // Fetch opportunities via OpportunityService
    List<de.freshplan.domain.opportunity.service.dto.OpportunityResponse> opportunities =
        opportunityService.findByCustomerId(customerId);

    log.info("Found {} opportunities for customer {}", opportunities.size(), customerId);

    return Response.ok(opportunities).build();
  }

  // ========== SPRINT 2.1.7.4: MANUAL ACTIVATION ==========

  /**
   * Activates a PROSPECT customer to AKTIV status.
   *
   * <p>Sprint 2.1.7.4: Manual Activation Button
   *
   * <p>Business Rule: PROSPECT → AKTIV when first order delivered
   *
   * <p>This endpoint allows manual activation until Xentral webhook integration is available
   * (Sprint 2.1.7.2).
   *
   * <p><strong>Authorization:</strong> Roles {@code admin}, {@code manager}, {@code sales} are
   * authorized. These are the actual role names used in the application (lowercase), not
   * placeholder constants. This was verified during code review to match the security
   * configuration.
   *
   * @param customerId Customer UUID
   * @param request Activation request with optional order number
   * @return 200 OK with updated customer, 400 if not PROSPECT, 404 if not found
   */
  @PUT
  @Path("/{id}/activate")
  @RolesAllowed({"admin", "manager", "sales"})
  public Response activateCustomer(
      @PathParam("id") UUID customerId, @Valid ActivateCustomerRequest request) {

    log.info("Activating customer {} (order: {})", customerId, request.orderNumber());

    // Validate: Customer must exist
    CustomerResponse customer = customerService.getCustomer(customerId);

    if (customer == null) {
      log.warn("Customer not found: {}", customerId);
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse("Customer not found", "CUSTOMER_NOT_FOUND"))
          .build();
    }

    // Validate: Customer must be PROSPECT
    if (customer.status() != CustomerStatus.PROSPECT) {
      log.warn("Customer {} is not PROSPECT (current status: {})", customerId, customer.status());
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(
              new ErrorResponse(
                  "Customer must be PROSPECT to activate. Current status: " + customer.status(),
                  "INVALID_STATUS"))
          .build();
    }

    // Activate: PROSPECT → AKTIV
    CustomerResponse updatedCustomer =
        customerService.changeStatus(customerId, CustomerStatus.AKTIV, currentUser.getUsername());

    log.info(
        "Customer {} activated: PROSPECT → AKTIV (order: {})", customerId, request.orderNumber());

    return Response.ok(updatedCustomer).build();
  }

  /**
   * Get revenue metrics for customer (Sprint 2.1.7.2)
   *
   * <p>Returns revenue metrics (30/90/365 days) and payment behavior from Xentral.
   *
   * <p><strong>Authorization:</strong> Roles {@code admin}, {@code manager}, {@code sales} are
   * authorized.
   *
   * @param customerId Customer UUID
   * @return 200 OK with revenue metrics, 404 if customer not found
   */
  @GET
  @Path("/{id}/revenue-metrics")
  @RolesAllowed({"admin", "manager", "sales"})
  public Response getRevenueMetrics(@PathParam("id") UUID customerId) {
    log.debug("Getting revenue metrics for customer: {}", customerId);

    try {
      de.freshplan.domain.customer.dto.RevenueMetrics metrics =
          revenueMetricsService.getRevenueMetrics(customerId);

      return Response.ok(metrics).build();

    } catch (NotFoundException e) {
      log.warn("Customer not found: {}", customerId);
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse("Customer not found", "CUSTOMER_NOT_FOUND"))
          .build();
    }
  }

  // ========== CONTACT MANAGEMENT (Sprint 2.1.7.2 D9.3) ==========

  /**
   * Creates a new contact for a customer.
   *
   * <p>Sprint 2.1.7.2 D9.3: Dashboard Contact CRUD
   *
   * @param customerId The customer ID
   * @param request The contact creation request
   * @return 201 Created with contact data
   */
  @POST
  @Path("/{id}/contacts")
  @Transactional
  @RolesAllowed({"admin", "manager", "sales"})
  public Response createContact(@PathParam("id") UUID customerId, @Valid ContactRequest request) {
    log.info("Creating contact for customer {}: {}", customerId, request.email());

    // 1. Verify customer exists
    Customer customer = Customer.findById(customerId);
    if (customer == null) {
      log.warn("Customer not found: {}", customerId);
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse("Customer not found", "CUSTOMER_NOT_FOUND"))
          .build();
    }

    // 2. Create contact
    CustomerContact contact = new CustomerContact();
    contact.setCustomer(customer);
    contact.setSalutation(request.salutation());
    contact.setTitle(request.title());
    contact.setFirstName(request.firstName());
    contact.setLastName(request.lastName());
    contact.setPosition(request.position());
    contact.setDecisionLevel(request.decisionLevel());
    contact.setEmail(request.email());
    contact.setPhone(request.phone());
    contact.setMobile(request.mobile());
    contact.setIsPrimary(request.isPrimary() != null ? request.isPrimary() : false);
    contact.setIsActive(true); // New contacts are active by default
    contact.setIsDecisionMaker(
        request.isDecisionMaker() != null ? request.isDecisionMaker() : false);

    // Audit fields
    contact.setCreatedBy(currentUser.getUsername());
    contact.setCreatedAt(LocalDateTime.now(clock));
    contact.setUpdatedBy(currentUser.getUsername());
    contact.setUpdatedAt(LocalDateTime.now(clock));

    contact.persist();

    log.info("Contact created: {} (ID: {})", contact.getEmail(), contact.getId());

    return Response.status(Response.Status.CREATED).entity(contact).build();
  }

  /**
   * Updates an existing contact.
   *
   * <p>Sprint 2.1.7.2 D9.3: Dashboard Contact CRUD
   *
   * @param customerId The customer ID
   * @param contactId The contact ID
   * @param request The contact update request
   * @return 200 OK with updated contact
   */
  @PUT
  @Path("/{id}/contacts/{contactId}")
  @Transactional
  @RolesAllowed({"admin", "manager", "sales"})
  public Response updateContact(
      @PathParam("id") UUID customerId,
      @PathParam("contactId") UUID contactId,
      @Valid ContactRequest request) {

    log.info("Updating contact {} for customer {}", contactId, customerId);

    // 1. Verify customer exists
    Customer customer = Customer.findById(customerId);
    if (customer == null) {
      log.warn("Customer not found: {}", customerId);
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse("Customer not found", "CUSTOMER_NOT_FOUND"))
          .build();
    }

    // 2. Verify contact exists and belongs to customer
    CustomerContact contact = CustomerContact.findById(contactId);
    if (contact == null) {
      log.warn("Contact not found: {}", contactId);
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse("Contact not found", "CONTACT_NOT_FOUND"))
          .build();
    }

    if (!contact.getCustomer().getId().equals(customerId)) {
      log.warn("Contact {} does not belong to customer {}", contactId, customerId);
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new ErrorResponse("Contact does not belong to this customer", "INVALID_CONTACT"))
          .build();
    }

    // 3. Update contact
    contact.setSalutation(request.salutation());
    contact.setTitle(request.title());
    contact.setFirstName(request.firstName());
    contact.setLastName(request.lastName());
    contact.setPosition(request.position());
    contact.setDecisionLevel(request.decisionLevel());
    contact.setEmail(request.email());
    contact.setPhone(request.phone());
    contact.setMobile(request.mobile());
    if (request.isPrimary() != null) {
      contact.setIsPrimary(request.isPrimary());
    }
    if (request.isDecisionMaker() != null) {
      contact.setIsDecisionMaker(request.isDecisionMaker());
    }

    // Audit fields
    contact.setUpdatedBy(currentUser.getUsername());
    contact.setUpdatedAt(LocalDateTime.now(clock));

    contact.persist();

    log.info("Contact updated: {} (ID: {})", contact.getEmail(), contact.getId());

    return Response.ok(contact).build();
  }

  // ========== LOCATION MANAGEMENT (Sprint 2.1.7.2 D11) ==========

  /**
   * Gets all locations for a customer.
   *
   * <p>Sprint 2.1.7.2 D11: Server-Driven Customer Cards - Card 1 (Unternehmensprofil)
   *
   * @param customerId The customer ID
   * @return 200 OK with list of locations
   */
  @GET
  @Path("/{id}/locations")
  @RolesAllowed({"admin", "manager", "sales"})
  public Response getCustomerLocations(@PathParam("id") UUID customerId) {
    log.debug("Fetching locations for customer: {}", customerId);

    // Verify customer exists
    Customer customer = Customer.findById(customerId);
    if (customer == null) {
      log.warn("Customer not found: {}", customerId);
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse("Customer not found", "CUSTOMER_NOT_FOUND"))
          .build();
    }

    // Fetch locations
    List<de.freshplan.domain.customer.entity.CustomerLocation> locations =
        de.freshplan.domain.customer.entity.CustomerLocation.find(
                "customer.id = ?1 and isDeleted = false ORDER BY isMainLocation DESC, createdAt ASC",
                customerId)
            .list();

    log.info("Found {} locations for customer {}", locations.size(), customerId);

    return Response.ok(locations).build();
  }

  // ========== BRANCH MANAGEMENT (Sprint 2.1.7.7) ==========

  /**
   * Creates a new branch (FILIALE) under a HEADQUARTER customer.
   *
   * <p>Sprint 2.1.7.7 D4: CreateBranchDialog - Branch Creation Endpoint
   *
   * <p><strong>Business Rules:</strong>
   *
   * <ul>
   *   <li>Parent customer must be a HEADQUARTER (hierarchyType = HEADQUARTER)
   *   <li>New branch will have hierarchyType = FILIALE
   *   <li>Branch inherits xentral_customer_id from parent
   *   <li>Branch starts with status = PROSPECT (unless specified otherwise)
   * </ul>
   *
   * <p><strong>Authorization:</strong> Roles {@code admin}, {@code manager} are authorized.
   *
   * @param headquarterId UUID of the parent HEADQUARTER customer
   * @param request CreateCustomerRequest with branch details (companyName, address, etc.)
   * @return 201 Created with branch CustomerResponse
   * @throws InvalidHierarchyException if parent is not a HEADQUARTER
   * @throws CustomerNotFoundException if parent customer not found
   */
  @POST
  @Path("/{id}/branches")
  @RolesAllowed({"admin", "manager"})
  @APIResponses({
    @APIResponse(
        responseCode = "201",
        description = "Branch created successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CustomerResponse.class))),
    @APIResponse(responseCode = "400", description = "Invalid request - parent is not HEADQUARTER"),
    @APIResponse(responseCode = "404", description = "Parent customer not found"),
    @APIResponse(responseCode = "403", description = "Forbidden - admin or manager role required")
  })
  public Response createBranch(
      @PathParam("id") UUID headquarterId, @Valid CreateCustomerRequest request) {

    log.info(
        "Creating branch for headquarter {} (user: {})", headquarterId, currentUser.getUsername());

    // Security check
    securityContext.requireAnyRole("admin", "manager");

    try {
      // Delegate to BranchService
      CustomerResponse branch =
          branchService.createBranch(headquarterId, request, currentUser.getUsername());

      log.info(
          "Branch created successfully: {} (ID: {}) under headquarter {}",
          branch.companyName(),
          branch.id(),
          headquarterId);

      return Response.status(Response.Status.CREATED).entity(branch).build();

    } catch (de.freshplan.domain.customer.service.exception.InvalidHierarchyException e) {
      log.warn("Invalid hierarchy: {}", e.getMessage());
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new ErrorResponse(e.getMessage(), "INVALID_HIERARCHY"))
          .build();
    } catch (de.freshplan.domain.customer.service.exception.CustomerNotFoundException e) {
      log.warn("Customer not found: {}", e.getMessage());
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse(e.getMessage(), "CUSTOMER_NOT_FOUND"))
          .build();
    }
  }

  /**
   * Lists all branches (FILIALE) for a HEADQUARTER customer.
   *
   * <p>Sprint 2.1.7.7 D4: CreateBranchDialog - Get Branches Endpoint
   *
   * @param headquarterId UUID of the parent HEADQUARTER customer
   * @return 200 OK with list of branches
   */
  @GET
  @Path("/{id}/branches")
  @RolesAllowed({"admin", "manager", "sales"})
  public Response getBranches(@PathParam("id") UUID headquarterId) {
    log.debug("Fetching branches for headquarter: {}", headquarterId);

    try {
      List<CustomerResponse> branches = branchService.getBranchesByHeadquarter(headquarterId);

      log.info("Found {} branches for headquarter {}", branches.size(), headquarterId);

      return Response.ok(branches).build();

    } catch (de.freshplan.domain.customer.service.exception.InvalidHierarchyException e) {
      log.warn("Invalid hierarchy: {}", e.getMessage());
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new ErrorResponse(e.getMessage(), "INVALID_HIERARCHY"))
          .build();
    } catch (de.freshplan.domain.customer.service.exception.CustomerNotFoundException e) {
      log.warn("Customer not found: {}", e.getMessage());
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse(e.getMessage(), "CUSTOMER_NOT_FOUND"))
          .build();
    }
  }

  /** Contact request DTO for create/update operations. */
  private record ContactRequest(
      String salutation,
      String title,
      String firstName,
      String lastName,
      String position,
      String decisionLevel,
      String email,
      String phone,
      String mobile,
      Boolean isPrimary,
      Boolean isDecisionMaker) {}

  /** Simple error response DTO for API errors. */
  private record ErrorResponse(String message, String errorCode) {}
}

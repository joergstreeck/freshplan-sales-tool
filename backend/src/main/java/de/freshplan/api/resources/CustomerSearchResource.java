package de.freshplan.api.resources;

import de.freshplan.domain.customer.service.CustomerSearchService;
import de.freshplan.domain.customer.service.CustomerSearchService.PagedResponse;
import de.freshplan.domain.customer.service.SmartSortService;
import de.freshplan.domain.customer.service.SmartSortService.SmartSortStrategy;
import de.freshplan.domain.customer.service.dto.CustomerResponse;
import de.freshplan.domain.customer.service.dto.CustomerSearchRequest;
import de.freshplan.domain.customer.service.dto.SmartSearchRequest;
import de.freshplan.infrastructure.security.SecurityAudit;
import de.freshplan.infrastructure.security.SecurityContextProvider;
import de.freshplan.shared.constants.PaginationConstants;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.executable.ExecutableType;
import jakarta.validation.executable.ValidateOnExecution;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * REST resource for advanced customer search functionality. Provides dynamic filtering, global
 * search, and sorting capabilities.
 */
@Path("/api/customers/search")
@RolesAllowed({"admin", "manager", "sales", "viewer"})
@SecurityAudit
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Customer Search", description = "Advanced customer search operations")
public class CustomerSearchResource {

  private static final Logger LOG = Logger.getLogger(CustomerSearchResource.class);

  @Inject CustomerSearchService searchService;

  @Inject SmartSortService smartSortService;

  @Inject SecurityContextProvider securityContext;

  @GET
  @Operation(
      summary = "Search customers with query parameter",
      description = "Simple search endpoint using query parameter")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Search completed successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = PagedCustomerResponse.class))),
    @APIResponse(responseCode = "400", description = "Invalid search parameters"),
    @APIResponse(responseCode = "401", description = "User not authenticated"),
    @APIResponse(responseCode = "500", description = "Internal server error")
  })
  public Response searchCustomersGet(
      @Parameter(description = "Search query", example = "Bella")
          @QueryParam("query")
          String query,
      @Parameter(description = "Page number (0-based)", example = "0")
          @QueryParam("page")
          @DefaultValue(PaginationConstants.DEFAULT_PAGE_NUMBER_STRING)
          @Min(value = 0, message = "Page must be >= 0")
          int page,
      @Parameter(description = "Page size", example = "20")
          @QueryParam("size")
          @DefaultValue(PaginationConstants.DEFAULT_PAGE_SIZE_STRING)
          @Min(value = 1, message = "Size must be >= 1")
          @Max(
              value = PaginationConstants.MAX_PAGE_SIZE,
              message = "Size must be <= " + PaginationConstants.MAX_PAGE_SIZE)
          int size) {
    LOG.infof("Customer GET search request: query=%s, page=%d, size=%d", query, page, size);

    try {
      // Create request object from query parameter
      CustomerSearchRequest request = new CustomerSearchRequest();
      if (query != null && !query.isBlank()) {
        request.setGlobalSearch(query);
      }
      
      PagedResponse<CustomerResponse> results = searchService.search(request, page, size);
      LOG.infof("Search completed: found %d customers", results.getTotalElements());
      return Response.ok(results).build();

    } catch (IllegalArgumentException e) {
      LOG.error("Invalid search parameters", e);
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new ErrorResponse("Invalid search parameters: " + e.getMessage()))
          .build();

    } catch (Exception e) {
      LOG.error("Error during customer search", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new ErrorResponse("An error occurred during search"))
          .build();
    }
  }

  @POST
  @Operation(
      summary = "Search customers with dynamic filters",
      description =
          "Performs an advanced search on customers with support for "
              + "global search, multiple filters, and custom sorting")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Search completed successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = PagedCustomerResponse.class))),
    @APIResponse(responseCode = "400", description = "Invalid search parameters"),
    @APIResponse(responseCode = "401", description = "User not authenticated"),
    @APIResponse(responseCode = "500", description = "Internal server error")
  })
  public Response searchCustomers(
      @RequestBody(
              description = "Search criteria including filters and sorting",
              required = true,
              content = @Content(schema = @Schema(implementation = CustomerSearchRequest.class)))
          @Valid
          CustomerSearchRequest request,
      @Parameter(description = "Page number (0-based)", example = "0")
          @QueryParam("page")
          @DefaultValue(PaginationConstants.DEFAULT_PAGE_NUMBER_STRING)
          @Min(value = 0, message = "Page must be >= 0")
          int page,
      @Parameter(description = "Page size", example = "20")
          @QueryParam("size")
          @DefaultValue(PaginationConstants.DEFAULT_PAGE_SIZE_STRING)
          @Min(value = 1, message = "Size must be >= 1")
          @Max(
              value = PaginationConstants.MAX_PAGE_SIZE,
              message = "Size must be <= " + PaginationConstants.MAX_PAGE_SIZE)
          int size) {
    LOG.infof("Customer search request received: page=%d, size=%d", page, size);

    try {
      PagedResponse<CustomerResponse> results = searchService.search(request, page, size);

      LOG.infof("Search completed: found %d customers", results.getTotalElements());

      return Response.ok(results).build();

    } catch (IllegalArgumentException e) {
      LOG.error("Invalid search parameters", e);
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new ErrorResponse("Invalid search parameters: " + e.getMessage()))
          .build();

    } catch (Exception e) {
      LOG.error("Error during customer search", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new ErrorResponse("An error occurred during search"))
          .build();
    }
  }

  @POST
  @Path("/smart")
  @ValidateOnExecution(type = ExecutableType.NONE)
  @Operation(
      summary = "Search customers with smart sorting strategies",
      description =
          "Performs customer search with predefined smart sorting strategies "
              + "optimized for sales, risk management, and engagement")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Smart search completed successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = PagedCustomerResponse.class))),
    @APIResponse(responseCode = "400", description = "Invalid search parameters"),
    @APIResponse(responseCode = "401", description = "User not authenticated"),
    @APIResponse(responseCode = "500", description = "Internal server error")
  })
  public Response smartSearch(
      @RequestBody(
              description = "Search criteria for smart sorting",
              required = true,
              content = @Content(schema = @Schema(implementation = SmartSearchRequest.class)))
          SmartSearchRequest request,
      @Parameter(description = "Page number (0-based)", example = "0")
          @QueryParam("page")
          @DefaultValue(PaginationConstants.DEFAULT_PAGE_NUMBER_STRING)
          int page,
      @Parameter(description = "Page size", example = "20")
          @QueryParam("size")
          @DefaultValue(PaginationConstants.DEFAULT_PAGE_SIZE_STRING)
          int size) {

    LOG.infof(
        "Smart search request received: strategy=%s, page=%d, size=%d",
        request.getStrategy(), page, size);

    try {
      // Convert smart search to regular search with smart sorting
      CustomerSearchRequest searchRequest = new CustomerSearchRequest();
      searchRequest.setGlobalSearch(request.getGlobalSearch());
      searchRequest.setFilters(request.getFilters());

      // Validate and apply smart sorting strategy
      String strategyName = request.getStrategy();
      if (strategyName == null || strategyName.isBlank()) {
        strategyName = "SALES_PRIORITY";
      }

      SmartSortStrategy strategy;
      try {
        strategy = SmartSortStrategy.valueOf(strategyName);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            "Invalid strategy: "
                + strategyName
                + ". Valid strategies are: SALES_PRIORITY, RISK_MITIGATION, ENGAGEMENT_FOCUS, REVENUE_POTENTIAL, CONTACT_FREQUENCY");
      }

      searchRequest.setMultiSort(smartSortService.createSmartSort(strategy));

      PagedResponse<CustomerResponse> results = searchService.search(searchRequest, page, size);

      LOG.infof(
          "Smart search completed: found %d customers using %s strategy",
          results.getTotalElements(), strategy);

      return Response.ok(results).build();

    } catch (IllegalArgumentException e) {
      LOG.error("Invalid smart search parameters", e);
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new ErrorResponse("Invalid smart search parameters: " + e.getMessage()))
          .build();

    } catch (Exception e) {
      LOG.error("Error during smart customer search", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new ErrorResponse("An error occurred during smart search"))
          .build();
    }
  }

  @GET
  @Path("/smart/strategies")
  @Operation(
      summary = "Get available smart sort strategies",
      description = "Returns all available smart sorting strategies with descriptions")
  @APIResponse(
      responseCode = "200",
      description = "Smart sort strategies retrieved successfully",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = SmartSortStrategyInfo.class)))
  public Response getSmartSortStrategies() {
    LOG.debug("Retrieving smart sort strategies");

    List<SmartSortStrategyInfo> strategies =
        List.of(
            new SmartSortStrategyInfo(
                "SALES_PRIORITY",
                smartSortService.getStrategyDescription(SmartSortStrategy.SALES_PRIORITY)),
            new SmartSortStrategyInfo(
                "RISK_MITIGATION",
                smartSortService.getStrategyDescription(SmartSortStrategy.RISK_MITIGATION)),
            new SmartSortStrategyInfo(
                "ENGAGEMENT_FOCUS",
                smartSortService.getStrategyDescription(SmartSortStrategy.ENGAGEMENT_FOCUS)),
            new SmartSortStrategyInfo(
                "REVENUE_POTENTIAL",
                smartSortService.getStrategyDescription(SmartSortStrategy.REVENUE_POTENTIAL)),
            new SmartSortStrategyInfo(
                "CONTACT_FREQUENCY",
                smartSortService.getStrategyDescription(SmartSortStrategy.CONTACT_FREQUENCY)));

    return Response.ok(strategies).build();
  }

  /** Schema class for OpenAPI documentation of paged response. */
  @Schema(description = "Paginated customer search results")
  public static class PagedCustomerResponse extends PagedResponse<CustomerResponse> {
    public PagedCustomerResponse(PagedResponse<CustomerResponse> response) {
      super(
          response.getContent(),
          response.getPage(),
          response.getSize(),
          response.getTotalElements(),
          response.getTotalPages(),
          response.isFirst(),
          response.isLast());
    }
  }

  /** Error response DTO. */
  @Schema(description = "Error response")
  public static class ErrorResponse {
    @Schema(description = "Error message", example = "Invalid search parameters")
    private final String error;

    public ErrorResponse(String error) {
      this.error = error;
    }

    public String getError() {
      return error;
    }
  }

  /** Smart sort strategy info DTO. */
  @Schema(description = "Information about a smart sort strategy")
  public static class SmartSortStrategyInfo {
    @Schema(description = "Strategy name", example = "SALES_PRIORITY")
    private final String name;

    @Schema(
        description = "Strategy description",
        example = "Prioritizes high-value sales opportunities with immediate potential")
    private final String description;

    public SmartSortStrategyInfo(String name, String description) {
      this.name = name;
      this.description = description;
    }

    public String getName() {
      return name;
    }

    public String getDescription() {
      return description;
    }
  }
}

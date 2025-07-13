package de.freshplan.api.resources;

import de.freshplan.domain.customer.service.CustomerSearchService;
import de.freshplan.domain.customer.service.CustomerSearchService.PagedResponse;
import de.freshplan.domain.customer.service.dto.CustomerResponse;
import de.freshplan.domain.customer.service.dto.CustomerSearchRequest;
import de.freshplan.infrastructure.security.SecurityAudit;
import de.freshplan.infrastructure.security.SecurityContextProvider;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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

  @Inject SecurityContextProvider securityContext;

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
          @DefaultValue("0")
          @Min(value = 0, message = "Page must be >= 0")
          int page,
      @Parameter(description = "Page size", example = "20")
          @QueryParam("size")
          @DefaultValue("20")
          @Min(value = 1, message = "Size must be >= 1")
          @Max(value = 100, message = "Size must be <= 100")
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
}

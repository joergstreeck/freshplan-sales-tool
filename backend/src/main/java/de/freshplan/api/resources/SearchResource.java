package de.freshplan.api.resources;

import de.freshplan.domain.search.service.SearchService;
import de.freshplan.domain.search.service.dto.SearchResults;
import de.freshplan.infrastructure.security.SecurityAudit;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * REST resource for universal search functionality across customers and contacts. Provides
 * intelligent query analysis and relevance-based results.
 *
 * @since FC-005 PR4
 */
@Path("/api/search")
@RolesAllowed({"admin", "manager", "sales", "viewer"})
@SecurityAudit
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Universal Search", description = "Universal search across entities")
public class SearchResource {

  private static final Logger LOG = Logger.getLogger(SearchResource.class);

  @Inject SearchService searchService;

  /**
   * Performs universal search across customers and contacts. Analyzes the query to determine search
   * type (email, phone, customer number, or text) and returns relevance-sorted results.
   *
   * @param query The search query (min 2 characters)
   * @param includeContacts Whether to include contacts in search results
   * @param includeInactive Whether to include inactive customers
   * @param limit Maximum number of results per entity type
   * @return Search results with customers and contacts
   */
  @GET
  @Path("/universal")
  @Operation(
      summary = "Universal Search",
      description = "Search across customers and contacts with intelligent query analysis")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Search results",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = SearchResults.class))),
    @APIResponse(responseCode = "400", description = "Invalid search parameters"),
    @APIResponse(responseCode = "401", description = "Unauthorized")
  })
  public Response universalSearch(
      @Parameter(
              description = "Search query (min 2 characters)",
              required = true,
              example = "schmidt")
          @QueryParam("query")
          @NotBlank
          @Size(min = 2, max = 100, message = "Query must be between 2 and 100 characters")
          String query,
      @Parameter(description = "Include contacts in results", example = "true")
          @QueryParam("includeContacts")
          @DefaultValue("true")
          boolean includeContacts,
      @Parameter(description = "Include inactive customers", example = "false")
          @QueryParam("includeInactive")
          @DefaultValue("false")
          boolean includeInactive,
      @Parameter(description = "Maximum results per entity type", example = "20")
          @QueryParam("limit")
          @DefaultValue("20")
          @Min(1)
          @Max(100)
          int limit) {

    LOG.infof(
        "Universal search request: query='%s', includeContacts=%s, includeInactive=%s, limit=%d",
        query, includeContacts, includeInactive, limit);

    try {
      SearchResults results =
          searchService.universalSearch(query, includeContacts, includeInactive, limit);

      LOG.infof(
          "Search completed: %d customers, %d contacts found",
          results.getCustomers().size(), results.getContacts().size());

      return Response.ok(results).build();
    } catch (Exception e) {
      LOG.errorf(e, "Error during universal search for query: %s", query);
      throw new WebApplicationException(
          "Search failed: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Quick search endpoint for autocomplete functionality. Returns only top matches with minimal
   * data.
   *
   * @param query The search query
   * @param limit Maximum number of results
   * @return Lightweight search results
   */
  @GET
  @Path("/quick")
  @Operation(
      summary = "Quick Search",
      description = "Lightweight search for autocomplete functionality")
  @APIResponses({@APIResponse(responseCode = "200", description = "Quick search results")})
  public Response quickSearch(
      @QueryParam("query") @NotBlank @Size(min = 1, max = 50) String query,
      @QueryParam("limit") @DefaultValue("5") @Min(1) @Max(20) int limit) {

    LOG.debugf("Quick search: query='%s', limit=%d", query, limit);

    try {
      SearchResults results = searchService.quickSearch(query, limit);
      return Response.ok(results).build();
    } catch (Exception e) {
      LOG.errorf(e, "Error during quick search for query: %s", query);
      throw new WebApplicationException(
          "Quick search failed", Response.Status.INTERNAL_SERVER_ERROR);
    }
  }
}

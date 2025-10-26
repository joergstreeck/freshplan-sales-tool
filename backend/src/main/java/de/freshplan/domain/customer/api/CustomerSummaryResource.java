package de.freshplan.domain.customer.api;

import de.freshplan.domain.customer.dto.CustomerSummaryDTO;
import de.freshplan.domain.customer.service.CustomerService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Customer Summary Resource for Compact View (Sprint 2.1.7.2 D11)
 *
 * <p>Provides condensed customer information for the compact overview shown when clicking a
 * customer in the list.
 *
 * <p><strong>Architecture:</strong>
 *
 * <ul>
 *   <li>Endpoint: GET /api/customers/{id}/summary
 *   <li>Returns: CustomerSummaryDTO with most important information
 *   <li>Use case: 80% of daily work (quick overview without full details)
 * </ul>
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/customers/{id}/summary")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Customer Summary", description = "Compact customer overview for daily work")
public class CustomerSummaryResource {

  @Inject CustomerService customerService;

  /**
   * Get customer summary for compact view
   *
   * <p>Returns condensed customer information including:
   *
   * <ul>
   *   <li>Company name, status, revenue
   *   <li>Location summary (count + top location names)
   *   <li>Primary contact information
   *   <li>Risk indicators
   *   <li>Next planned steps
   * </ul>
   *
   * @param customerId Customer ID
   * @return CustomerSummaryDTO with compact information
   */
  @GET
  @Operation(summary = "Get customer summary for compact view")
  @APIResponse(
      responseCode = "200",
      description = "Customer summary retrieved successfully",
      content = @Content(schema = @Schema(implementation = CustomerSummaryDTO.class)))
  @APIResponse(responseCode = "404", description = "Customer not found")
  public Response getCustomerSummary(
      @Parameter(description = "Customer ID", required = true) @PathParam("id") UUID customerId) {

    CustomerSummaryDTO summary = customerService.getCustomerSummary(customerId);

    if (summary == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity("Customer not found: " + customerId)
          .build();
    }

    return Response.ok(summary).build();
  }
}

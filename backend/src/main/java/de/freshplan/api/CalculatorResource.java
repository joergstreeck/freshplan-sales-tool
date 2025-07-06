package de.freshplan.api;

import de.freshplan.domain.calculator.service.CalculatorService;
import de.freshplan.domain.calculator.service.dto.CalculatorRequest;
import de.freshplan.domain.calculator.service.dto.CalculatorResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/** REST resource for discount calculator operations. */
@Path("/api/calculator")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Calculator", description = "Discount calculation operations")
@PermitAll
public class CalculatorResource {

  private final CalculatorService calculatorService;

  @Inject
  public CalculatorResource(CalculatorService calculatorService) {
    this.calculatorService = calculatorService;
  }

  /** Calculate discount based on input parameters. */
  @POST
  @Path("/calculate")
  @Operation(
      summary = "Calculate discount",
      description = "Calculates discount based on order value, lead time, and other parameters")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Calculation successful",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CalculatorResponse.class))),
    @APIResponse(responseCode = "400", description = "Invalid input parameters")
  })
  public Response calculate(
      @Valid @RequestBody(required = true, description = "Calculation parameters")
          CalculatorRequest request) {

    CalculatorResponse response = calculatorService.calculate(request);
    return Response.ok(response).build();
  }

  /** Get discount rules configuration. */
  @GET
  @Path("/rules")
  @Operation(
      summary = "Get discount rules",
      description = "Returns the current discount rules configuration")
  @APIResponses({@APIResponse(responseCode = "200", description = "Rules retrieved successfully")})
  public Response getRules() {
    // TODO: Implement rules endpoint if needed
    return Response.ok().entity("{\"message\": \"Rules endpoint not yet implemented\"}").build();
  }
}

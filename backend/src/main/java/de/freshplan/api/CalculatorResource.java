package de.freshplan.api;

import de.freshplan.domain.calculator.model.BuffetMenuType;
import de.freshplan.domain.calculator.service.CalculatorApplicationService;
import de.freshplan.domain.calculator.service.dto.BuffetQuoteResponse;
import de.freshplan.domain.calculator.service.dto.CalculateBuffetRequest;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.ApiResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST API for calculator operations.
 * 
 * Provides endpoints for buffet price calculations.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/calc")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Calculator", description = "Buffet price calculation operations")
public class CalculatorResource {
    
    private final CalculatorApplicationService calculatorService;
    
    @Inject
    public CalculatorResource(CalculatorApplicationService calculatorService) {
        this.calculatorService = calculatorService;
    }
    
    @POST
    @Path("/quote")
    @RolesAllowed({"admin", "manager", "sales"})
    @Operation(
        summary = "Calculate buffet quote",
        description = "Calculates pricing for buffet catering including volume discounts and tax"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Quote calculated successfully",
        content = @Content(schema = @Schema(implementation = BuffetQuoteResponse.class))
    )
    @ApiResponse(
        responseCode = "400", 
        description = "Invalid request parameters"
    )
    @ApiResponse(
        responseCode = "401",
        description = "Authentication required"
    )
    @ApiResponse(
        responseCode = "403",
        description = "Insufficient permissions (admin, manager, or sales role required)"
    )
    public Response calculateBuffetQuote(@Valid CalculateBuffetRequest request) {
        BuffetQuoteResponse quote = calculatorService.calculateBuffetQuote(request);
        return Response.ok(quote).build();
    }
    
    @GET
    @Path("/menu-types")
    @RolesAllowed({"admin", "manager", "sales", "viewer"})
    @Operation(
        summary = "Get available menu types",
        description = "Returns all available buffet menu types with their base prices"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Menu types retrieved successfully",
        content = @Content(schema = @Schema(implementation = BuffetMenuType[].class))
    )
    public Response getAvailableMenuTypes() {
        BuffetMenuType[] menuTypes = calculatorService.getAvailableMenuTypes();
        return Response.ok(menuTypes).build();
    }
}
package de.freshplan.api;

import de.freshplan.calculator.CalculatorService;
import de.freshplan.calculator.dto.CalculatorRequest;
import de.freshplan.calculator.dto.CalculatorResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST resource for discount calculations.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/calculator")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CalculatorResource {
    
    @Inject
    CalculatorService calculatorService;
    
    /**
     * Calculate discount based on FreshPlan business rules.
     * 
     * @param request the calculation parameters
     * @return the calculated discounts and final price
     */
    @POST
    @Path("/calculate")
    public Response calculate(@Valid CalculatorRequest request) {
        CalculatorResponse response = calculatorService.calculate(request);
        return Response.ok(response).build();
    }
    
    /**
     * Get discount rules for display in frontend.
     * 
     * @return the current discount rules
     */
    @GET
    @Path("/rules")
    public Response getRules() {
        return Response.ok()
            .entity("""
                {
                  "baseRules": [
                    {"min": 75000, "discount": 10, "description": "ab 75.000 EUR"},
                    {"min": 50000, "discount": 9, "description": "50.000 - 74.999 EUR"},
                    {"min": 30000, "discount": 8, "description": "30.000 - 49.999 EUR"},
                    {"min": 15000, "discount": 6, "description": "15.000 - 29.999 EUR"},
                    {"min": 5000, "discount": 3, "description": "5.000 - 14.999 EUR"},
                    {"min": 0, "discount": 0, "description": "unter 5.000 EUR"}
                  ],
                  "earlyBookingRules": [
                    {"days": 30, "discount": 3, "description": "30 - 44 Tage"},
                    {"days": 15, "discount": 2, "description": "15 - 29 Tage"},
                    {"days": 10, "discount": 1, "description": "10 - 14 Tage"},
                    {"days": 0, "discount": 0, "description": "unter 10 Tage"}
                  ],
                  "pickupDiscount": 2,
                  "pickupMinimum": 5000,
                  "maxTotalDiscount": 15
                }
                """)
            .build();
    }
}
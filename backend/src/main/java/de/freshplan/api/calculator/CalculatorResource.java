package de.freshplan.api.calculator;

import de.freshplan.api.calculator.dto.DiscountCalculationRequest;
import de.freshplan.api.calculator.dto.DiscountCalculationResponse;
import de.freshplan.api.calculator.mapper.CalculatorMapper;
import de.freshplan.domain.calculator.model.FreshPlanCalculationRequest;
import de.freshplan.domain.calculator.model.FreshPlanCalculationResult;
import de.freshplan.domain.calculator.service.FreshPlanCalculatorService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.freshplan.api.exception.mapper.ErrorResponse;

/**
 * REST API for FreshPlan discount calculations.
 * 
 * Provides endpoints for calculating B2B wholesale food discounts
 * according to Freshfoodz GmbH business rules.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/v1/calculator")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CalculatorResource {
    
    private static final Logger logger = LoggerFactory.getLogger(CalculatorResource.class);
    
    private final FreshPlanCalculatorService calculatorService;
    private final CalculatorMapper mapper;
    
    @Inject
    public CalculatorResource(FreshPlanCalculatorService calculatorService, 
                            CalculatorMapper mapper) {
        this.calculatorService = calculatorService;
        this.mapper = mapper;
    }
    
    /**
     * Calculates discount for a FreshPlan B2B order.
     * 
     * Applies volume discounts, lead time discounts, and self-pickup discounts
     * according to Freshfoodz GmbH business rules.
     * 
     * @param request the discount calculation request
     * @return the calculated discount and final pricing
     */
    @POST
    @Path("/discount")
    public Response calculateDiscount(@Valid DiscountCalculationRequest request) {
        logger.debug("Processing discount calculation: {}", request);
        
        // Map API DTO to domain model
        FreshPlanCalculationRequest domainRequest = mapper.toDomainRequest(request);
        
        // Perform calculation using domain service
        FreshPlanCalculationResult domainResult = 
            calculatorService.calculateDiscounts(domainRequest);
        
        // Map domain result to API response
        DiscountCalculationResponse response = 
            mapper.toApiResponse(domainResult, request);
        
        logger.debug("Discount calculation completed: {}", response);
        
        return Response.ok(response).build();
    }
    
}
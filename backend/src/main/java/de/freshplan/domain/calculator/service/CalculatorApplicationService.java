package de.freshplan.domain.calculator.service;

import de.freshplan.domain.calculator.model.BuffetCalculationRequest;
import de.freshplan.domain.calculator.model.BuffetCalculationResult;
import de.freshplan.domain.calculator.model.BuffetMenuType;
import de.freshplan.domain.calculator.service.dto.BuffetQuoteResponse;
import de.freshplan.domain.calculator.service.dto.CalculateBuffetRequest;
import de.freshplan.domain.calculator.service.mapper.CalculatorMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for calculator operations.
 * 
 * Orchestrates between REST layer and domain logic.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class CalculatorApplicationService {
    
    private static final Logger LOG = LoggerFactory.getLogger(CalculatorApplicationService.class);
    
    private final BuffetCalculatorService calculatorService;
    private final CalculatorMapper mapper;
    
    @Inject
    public CalculatorApplicationService(CalculatorMapper mapper) {
        this.calculatorService = new BuffetCalculatorService();
        this.mapper = mapper;
    }
    
    /**
     * Calculates buffet quote for given request.
     * 
     * @param request the calculation request
     * @return calculated quote response
     */
    public BuffetQuoteResponse calculateBuffetQuote(CalculateBuffetRequest request) {
        LOG.debug("Calculating buffet quote for {} persons, menu type: {}", 
                 request.getPersons(), request.getMenuType());
        
        // Map to domain model
        BuffetCalculationRequest domainRequest = mapper.toDomainRequest(request);
        
        // Execute business logic
        BuffetCalculationResult result = calculatorService.calculateBuffet(domainRequest);
        
        // Map back to DTO
        BuffetQuoteResponse response = mapper.toResponseDto(result);
        
        LOG.info("Buffet quote calculated: {} persons, gross total: €{}", 
                response.getPersons(), response.getGrossTotal());
        
        return response;
    }
    
    /**
     * Gets available buffet menu types.
     * 
     * @return array of available menu types
     */
    public BuffetMenuType[] getAvailableMenuTypes() {
        return calculatorService.getAvailableMenuTypes();
    }
}
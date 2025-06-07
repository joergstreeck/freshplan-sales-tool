package de.freshplan.api.calculator.mapper;

import de.freshplan.api.calculator.dto.DiscountCalculationRequest;
import de.freshplan.api.calculator.dto.DiscountCalculationResponse;
import de.freshplan.domain.calculator.model.FreshPlanCalculationRequest;
import de.freshplan.domain.calculator.model.FreshPlanCalculationResult;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;

/**
 * Mapper between Calculator API DTOs and Domain models.
 * 
 * Converts between API layer and domain layer objects.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class CalculatorMapper {
    
    /**
     * Maps API request DTO to domain request model.
     * 
     * @param dto the API request DTO
     * @return domain calculation request
     */
    public FreshPlanCalculationRequest toDomainRequest(DiscountCalculationRequest dto) {
        if (dto == null) {
            return null;
        }
        
        return new FreshPlanCalculationRequest(
            dto.getOrderValueNet(),
            dto.getLeadTimeDays(),
            dto.getSelfPickup()
        );
    }
    
    /**
     * Maps domain result to API response DTO.
     * 
     * @param domainResult the domain calculation result
     * @return API response DTO
     */
    public DiscountCalculationResponse toApiResponse(FreshPlanCalculationResult domainResult) {
        if (domainResult == null) {
            return null;
        }
        
        return new DiscountCalculationResponse(
            domainResult.getOrderValueNet(),
            null, // Lead time days not stored in result, would need to pass from request
            null, // Self pickup flag not stored in result, would need to pass from request
            domainResult.getVolumeDiscountTier(),
            domainResult.getLeadTimeDiscount(),
            domainResult.hasSelfPickupDiscount(),
            domainResult.getVolumeDiscountRate(),
            domainResult.getLeadTimeDiscountRate(),
            domainResult.getSelfPickupDiscountRate(),
            domainResult.getTotalDiscountRate(),
            domainResult.getVolumeDiscountAmount(),
            domainResult.getLeadTimeDiscountAmount(),
            domainResult.getSelfPickupDiscountAmount(),
            domainResult.getTotalDiscountAmount(),
            domainResult.getFinalNetValue(),
            LocalDateTime.now()
        );
    }
    
    /**
     * Maps domain result to API response DTO with request context.
     * 
     * @param domainResult the domain calculation result
     * @param originalRequest the original API request for context
     * @return API response DTO with complete information
     */
    public DiscountCalculationResponse toApiResponse(FreshPlanCalculationResult domainResult, 
                                                   DiscountCalculationRequest originalRequest) {
        if (domainResult == null) {
            return null;
        }
        
        return new DiscountCalculationResponse(
            domainResult.getOrderValueNet(),
            originalRequest != null ? originalRequest.getLeadTimeDays() : null,
            originalRequest != null ? originalRequest.getSelfPickup() : null,
            domainResult.getVolumeDiscountTier(),
            domainResult.getLeadTimeDiscount(),
            domainResult.hasSelfPickupDiscount(),
            domainResult.getVolumeDiscountRate(),
            domainResult.getLeadTimeDiscountRate(),
            domainResult.getSelfPickupDiscountRate(),
            domainResult.getTotalDiscountRate(),
            domainResult.getVolumeDiscountAmount(),
            domainResult.getLeadTimeDiscountAmount(),
            domainResult.getSelfPickupDiscountAmount(),
            domainResult.getTotalDiscountAmount(),
            domainResult.getFinalNetValue(),
            LocalDateTime.now()
        );
    }
}
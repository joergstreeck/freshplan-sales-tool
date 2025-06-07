package de.freshplan.domain.calculator.service.mapper;

import de.freshplan.domain.calculator.model.BuffetCalculationRequest;
import de.freshplan.domain.calculator.model.BuffetCalculationResult;
import de.freshplan.domain.calculator.service.dto.BuffetQuoteResponse;
import de.freshplan.domain.calculator.service.dto.CalculateBuffetRequest;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapper between calculator DTOs and domain models.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class CalculatorMapper {
    
    /**
     * Maps REST request DTO to domain model.
     * 
     * @param dto the REST request DTO
     * @return domain calculation request
     */
    public BuffetCalculationRequest toDomainRequest(CalculateBuffetRequest dto) {
        return new BuffetCalculationRequest(
            dto.getPersons(),
            dto.getMenuType()
        );
    }
    
    /**
     * Maps domain calculation result to REST response DTO.
     * 
     * @param result the domain calculation result
     * @return REST response DTO
     */
    public BuffetQuoteResponse toResponseDto(BuffetCalculationResult result) {
        return BuffetQuoteResponse.builder()
            .persons(result.getPersons())
            .menuType(result.getMenuType())
            .menuDisplayName(result.getMenuType().getDisplayName())
            .pricePerPerson(result.getPricePerPerson())
            .netTotal(result.getNetTotal())
            .taxAmount(result.getTaxAmount())
            .grossTotal(result.getGrossTotal())
            .taxRate(result.getTaxRate())
            .build();
    }
}
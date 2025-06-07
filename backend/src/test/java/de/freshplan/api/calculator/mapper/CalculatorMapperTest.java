package de.freshplan.api.calculator.mapper;

import de.freshplan.api.calculator.dto.DiscountCalculationRequest;
import de.freshplan.api.calculator.dto.DiscountCalculationResponse;
import de.freshplan.domain.calculator.model.DiscountTier;
import de.freshplan.domain.calculator.model.FreshPlanCalculationRequest;
import de.freshplan.domain.calculator.model.FreshPlanCalculationResult;
import de.freshplan.domain.calculator.model.LeadTimeDiscount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CalculatorMapper.
 * 
 * Tests mapping between API DTOs and domain models.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
class CalculatorMapperTest {
    
    private CalculatorMapper mapper;
    
    @BeforeEach
    void setUp() {
        mapper = new CalculatorMapper();
    }
    
    @Test
    void toDomainRequest_withValidDto_shouldMapCorrectly() {
        // Given
        DiscountCalculationRequest dto = new DiscountCalculationRequest(
            new BigDecimal("8000.00"), 15, true
        );
        
        // When
        FreshPlanCalculationRequest domainRequest = mapper.toDomainRequest(dto);
        
        // Then
        assertThat(domainRequest).isNotNull();
        assertThat(domainRequest.getOrderValueNet()).isEqualTo(new BigDecimal("8000.00"));
        assertThat(domainRequest.getLeadTimeDays()).isEqualTo(15);
        assertThat(domainRequest.getSelfPickup()).isTrue();
    }
    
    @Test
    void toDomainRequest_withNullDto_shouldReturnNull() {
        // When
        FreshPlanCalculationRequest domainRequest = mapper.toDomainRequest(null);
        
        // Then
        assertThat(domainRequest).isNull();
    }
    
    @Test
    void toApiResponse_withDomainResult_shouldMapCorrectly() {
        // Given
        FreshPlanCalculationRequest domainRequest = new FreshPlanCalculationRequest(
            new BigDecimal("8000.00"), 15, true
        );
        FreshPlanCalculationResult domainResult = new FreshPlanCalculationResult(domainRequest);
        
        // When
        DiscountCalculationResponse response = mapper.toApiResponse(domainResult);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderValueNet()).isEqualTo(new BigDecimal("8000.00"));
        assertThat(response.getVolumeDiscountTier()).isEqualTo(DiscountTier.TIER_1);
        assertThat(response.getLeadTimeDiscountCategory()).isEqualTo(LeadTimeDiscount.MEDIUM_LEAD);
        assertThat(response.getHasSelfPickupDiscount()).isTrue();
        assertThat(response.getVolumeDiscountRate()).isEqualTo(new BigDecimal("0.03"));
        assertThat(response.getLeadTimeDiscountRate()).isEqualTo(new BigDecimal("0.02"));
        assertThat(response.getSelfPickupDiscountRate()).isEqualTo(new BigDecimal("0.02"));
        assertThat(response.getTotalDiscountRate()).isEqualTo(new BigDecimal("0.07"));
        assertThat(response.getCalculatedAt()).isNotNull();
        
        // Note: leadTimeDays and selfPickup are null in simple mapping
        assertThat(response.getLeadTimeDays()).isNull();
        assertThat(response.getSelfPickup()).isNull();
    }
    
    @Test
    void toApiResponse_withDomainResultAndRequest_shouldMapCompleteInformation() {
        // Given
        DiscountCalculationRequest apiRequest = new DiscountCalculationRequest(
            new BigDecimal("8000.00"), 15, true
        );
        FreshPlanCalculationRequest domainRequest = mapper.toDomainRequest(apiRequest);
        FreshPlanCalculationResult domainResult = new FreshPlanCalculationResult(domainRequest);
        
        // When
        DiscountCalculationResponse response = mapper.toApiResponse(domainResult, apiRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderValueNet()).isEqualTo(new BigDecimal("8000.00"));
        assertThat(response.getLeadTimeDays()).isEqualTo(15);
        assertThat(response.getSelfPickup()).isTrue();
        assertThat(response.getVolumeDiscountTier()).isEqualTo(DiscountTier.TIER_1);
        assertThat(response.getLeadTimeDiscountCategory()).isEqualTo(LeadTimeDiscount.MEDIUM_LEAD);
        assertThat(response.getHasSelfPickupDiscount()).isTrue();
        assertThat(response.getCalculatedAt()).isNotNull();
    }
    
    @Test
    void toApiResponse_withNullDomainResult_shouldReturnNull() {
        // When
        DiscountCalculationResponse response = mapper.toApiResponse(null);
        
        // Then
        assertThat(response).isNull();
    }
    
    @Test
    void toApiResponse_withNullDomainResultAndRequest_shouldReturnNull() {
        // Given
        DiscountCalculationRequest apiRequest = new DiscountCalculationRequest(
            new BigDecimal("8000.00"), 15, true
        );
        
        // When
        DiscountCalculationResponse response = mapper.toApiResponse(null, apiRequest);
        
        // Then
        assertThat(response).isNull();
    }
    
    @Test
    void toApiResponse_withDomainResultAndNullRequest_shouldMapWithNullContextFields() {
        // Given
        FreshPlanCalculationRequest domainRequest = new FreshPlanCalculationRequest(
            new BigDecimal("8000.00"), 15, true
        );
        FreshPlanCalculationResult domainResult = new FreshPlanCalculationResult(domainRequest);
        
        // When
        DiscountCalculationResponse response = mapper.toApiResponse(domainResult, null);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderValueNet()).isEqualTo(new BigDecimal("8000.00"));
        assertThat(response.getLeadTimeDays()).isNull();
        assertThat(response.getSelfPickup()).isNull();
        assertThat(response.getVolumeDiscountTier()).isEqualTo(DiscountTier.TIER_1);
        assertThat(response.getCalculatedAt()).isNotNull();
    }
}
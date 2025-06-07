package de.freshplan.domain.calculator.service;

import de.freshplan.domain.calculator.model.DiscountTier;
import de.freshplan.domain.calculator.model.FreshPlanCalculationRequest;
import de.freshplan.domain.calculator.model.FreshPlanCalculationResult;
import de.freshplan.domain.calculator.model.LeadTimeDiscount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for FreshPlanCalculatorService.
 * 
 * Tests the real Freshfoodz GmbH business rules for discount calculation.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
class FreshPlanCalculatorServiceTest {
    
    private FreshPlanCalculatorService calculatorService;
    
    @BeforeEach
    void setUp() {
        calculatorService = new FreshPlanCalculatorService();
    }
    
    @Test
    void calculateDiscounts_withBasicOrder_shouldApplyCorrectVolumeDiscount() {
        // Given - Order of €8,000 (should get 3% volume discount)
        FreshPlanCalculationRequest request = new FreshPlanCalculationRequest(
            new BigDecimal("8000.00"), 5, false
        );
        
        // When
        FreshPlanCalculationResult result = calculatorService.calculateDiscounts(request);
        
        // Then
        assertThat(result.getVolumeDiscountTier()).isEqualTo(DiscountTier.TIER_1);
        assertThat(result.getVolumeDiscountRate()).isEqualTo(new BigDecimal("0.03")); // 3%
        assertThat(result.getVolumeDiscountAmount()).isEqualTo(new BigDecimal("240.00")); // 8000 * 0.03
        assertThat(result.getLeadTimeDiscountRate()).isEqualTo(new BigDecimal("0.00")); // No lead time discount
        assertThat(result.getSelfPickupDiscountRate()).isEqualTo(new BigDecimal("0.00")); // No pickup
        assertThat(result.getTotalDiscountRate()).isEqualTo(new BigDecimal("0.03"));
    }
    
    @Test
    void calculateDiscounts_withLargeOrderAndLeadTime_shouldApplyMultipleDiscounts() {
        // Given - Order of €60,000, 20 days lead time, self pickup
        FreshPlanCalculationRequest request = new FreshPlanCalculationRequest(
            new BigDecimal("60000.00"), 20, true
        );
        
        // When
        FreshPlanCalculationResult result = calculatorService.calculateDiscounts(request);
        
        // Then
        assertThat(result.getVolumeDiscountTier()).isEqualTo(DiscountTier.TIER_4);
        assertThat(result.getVolumeDiscountRate()).isEqualTo(new BigDecimal("0.09")); // 9%
        assertThat(result.getLeadTimeDiscount()).isEqualTo(LeadTimeDiscount.MEDIUM_LEAD);
        assertThat(result.getLeadTimeDiscountRate()).isEqualTo(new BigDecimal("0.02")); // 2%
        assertThat(result.getSelfPickupDiscountRate()).isEqualTo(new BigDecimal("0.02")); // 2%
        assertThat(result.getTotalDiscountRate()).isEqualTo(new BigDecimal("0.13")); // 9% + 2% + 2%
        
        // Check amounts
        assertThat(result.getVolumeDiscountAmount()).isEqualTo(new BigDecimal("5400.00")); // 60000 * 0.09
        assertThat(result.getLeadTimeDiscountAmount()).isEqualTo(new BigDecimal("1200.00")); // 60000 * 0.02
        assertThat(result.getSelfPickupDiscountAmount()).isEqualTo(new BigDecimal("1200.00")); // 60000 * 0.02
        assertThat(result.getTotalDiscountAmount()).isEqualTo(new BigDecimal("7800.00")); // Total discount
        
        // Final net value (B2B - no VAT)
        assertThat(result.getFinalNetValue()).isEqualTo(new BigDecimal("52200.00")); // 60000 - 7800
    }
    
    @Test
    void calculateDiscounts_withMaximumDiscountScenario_shouldCapAt15Percent() {
        // Given - Large order with all possible discounts (would exceed 15%)
        FreshPlanCalculationRequest request = new FreshPlanCalculationRequest(
            new BigDecimal("80000.00"), 35, true
        );
        
        // When
        FreshPlanCalculationResult result = calculatorService.calculateDiscounts(request);
        
        // Then - Should cap at 15% total discount
        assertThat(result.getVolumeDiscountRate()).isEqualTo(new BigDecimal("0.10")); // 10%
        assertThat(result.getLeadTimeDiscountRate()).isEqualTo(new BigDecimal("0.03")); // 3%
        assertThat(result.getSelfPickupDiscountRate()).isEqualTo(new BigDecimal("0.02")); // 2%
        // 10% + 3% + 2% = 15%, but should be capped at 15%
        assertThat(result.getTotalDiscountRate()).isEqualTo(new BigDecimal("0.15")); // Capped at 15%
        assertThat(result.getTotalDiscountAmount()).isEqualTo(new BigDecimal("12000.00")); // 80000 * 0.15
    }
    
    @ParameterizedTest
    @CsvSource({
        "3000.00, TIER_0, 0.00",
        "5000.00, TIER_1, 0.03",
        "12000.00, TIER_1, 0.03", 
        "15000.00, TIER_2, 0.06",
        "25000.00, TIER_2, 0.06",
        "30000.00, TIER_3, 0.08",
        "45000.00, TIER_3, 0.08",
        "50000.00, TIER_4, 0.09",
        "70000.00, TIER_4, 0.09",
        "75000.00, TIER_5, 0.10",
        "100000.00, TIER_5, 0.10"
    })
    void calculateDiscounts_volumeDiscountTiers_shouldApplyCorrectRates(
            String orderValue, DiscountTier expectedTier, String expectedRate) {
        // Given
        FreshPlanCalculationRequest request = new FreshPlanCalculationRequest(
            new BigDecimal(orderValue), 5, false
        );
        
        // When
        FreshPlanCalculationResult result = calculatorService.calculateDiscounts(request);
        
        // Then
        assertThat(result.getVolumeDiscountTier()).isEqualTo(expectedTier);
        assertThat(result.getVolumeDiscountRate()).isEqualTo(new BigDecimal(expectedRate));
    }
    
    @ParameterizedTest
    @CsvSource({
        "5, NO_DISCOUNT, 0.00",
        "10, SHORT_LEAD, 0.01",
        "14, SHORT_LEAD, 0.01", 
        "15, MEDIUM_LEAD, 0.02",
        "25, MEDIUM_LEAD, 0.02",
        "30, LONG_LEAD, 0.03",
        "45, LONG_LEAD, 0.03"
    })
    void calculateDiscounts_leadTimeDiscounts_shouldApplyCorrectRates(
            int leadTimeDays, LeadTimeDiscount expectedDiscount, String expectedRate) {
        // Given
        FreshPlanCalculationRequest request = new FreshPlanCalculationRequest(
            new BigDecimal("10000.00"), leadTimeDays, false
        );
        
        // When
        FreshPlanCalculationResult result = calculatorService.calculateDiscounts(request);
        
        // Then
        assertThat(result.getLeadTimeDiscount()).isEqualTo(expectedDiscount);
        assertThat(result.getLeadTimeDiscountRate()).isEqualTo(new BigDecimal(expectedRate));
    }
    
    @Test
    void calculateDiscounts_selfPickupUnderMinimum_shouldNotApplyPickupDiscount() {
        // Given - Order under €5,000 with self pickup
        FreshPlanCalculationRequest request = new FreshPlanCalculationRequest(
            new BigDecimal("3000.00"), 5, true
        );
        
        // When
        FreshPlanCalculationResult result = calculatorService.calculateDiscounts(request);
        
        // Then
        assertThat(result.hasSelfPickupDiscount()).isFalse();
        assertThat(result.getSelfPickupDiscountRate()).isEqualTo(new BigDecimal("0.00"));
    }
    
    @Test
    void calculateDiscounts_selfPickupOverMinimum_shouldApplyPickupDiscount() {
        // Given - Order over €5,000 with self pickup
        FreshPlanCalculationRequest request = new FreshPlanCalculationRequest(
            new BigDecimal("6000.00"), 5, true
        );
        
        // When
        FreshPlanCalculationResult result = calculatorService.calculateDiscounts(request);
        
        // Then
        assertThat(result.hasSelfPickupDiscount()).isTrue();
        assertThat(result.getSelfPickupDiscountRate()).isEqualTo(new BigDecimal("0.02")); // 2%
        assertThat(result.getSelfPickupDiscountAmount()).isEqualTo(new BigDecimal("120.00")); // 6000 * 0.02
    }
    
    @Test
    void calculateDiscounts_nullRequest_shouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> calculatorService.calculateDiscounts(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Calculation request cannot be null");
    }
    
    @Test
    void calculateDiscounts_orderUnderMinimum_shouldThrowException() {
        // Given
        FreshPlanCalculationRequest request = new FreshPlanCalculationRequest(
            new BigDecimal("50.00"), 5, false
        );
        
        // When/Then
        assertThatThrownBy(() -> calculatorService.calculateDiscounts(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Minimum order value is €100.00");
    }
    
    @Test
    void calculateDiscounts_orderOverMaximum_shouldThrowException() {
        // Given
        FreshPlanCalculationRequest request = new FreshPlanCalculationRequest(
            new BigDecimal("150000.00"), 5, false
        );
        
        // When/Then
        assertThatThrownBy(() -> calculatorService.calculateDiscounts(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Order value exceeds maximum");
    }
    
    @Test
    void calculateDiscounts_negativeLeadTime_shouldThrowException() {
        // Given
        FreshPlanCalculationRequest request = new FreshPlanCalculationRequest(
            new BigDecimal("5000.00"), -1, false
        );
        
        // When/Then
        assertThatThrownBy(() -> calculatorService.calculateDiscounts(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Lead time days must be non-negative");
    }
    
    @Test
    void isQualifyingOrder_withValidOrder_shouldReturnTrue() {
        // Given
        BigDecimal validOrderValue = new BigDecimal("500.00");
        
        // When
        boolean result = calculatorService.isQualifyingOrder(validOrderValue);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void isQualifyingOrder_withSmallOrder_shouldReturnFalse() {
        // Given
        BigDecimal smallOrderValue = new BigDecimal("50.00");
        
        // When
        boolean result = calculatorService.isQualifyingOrder(smallOrderValue);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void getMinimumOrderValue_shouldReturnCorrectValue() {
        // When
        BigDecimal minValue = calculatorService.getMinimumOrderValue();
        
        // Then
        assertThat(minValue).isEqualTo(new BigDecimal("100.00"));
    }
}
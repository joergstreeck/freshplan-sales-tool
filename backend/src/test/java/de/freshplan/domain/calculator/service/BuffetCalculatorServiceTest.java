package de.freshplan.domain.calculator.service;

import de.freshplan.domain.calculator.model.BuffetCalculationRequest;
import de.freshplan.domain.calculator.model.BuffetCalculationResult;
import de.freshplan.domain.calculator.model.BuffetMenuType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for BuffetCalculatorService.
 * 
 * Tests business logic with various scenarios including edge cases.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
class BuffetCalculatorServiceTest {
    
    private BuffetCalculatorService calculatorService;
    
    @BeforeEach
    void setUp() {
        calculatorService = new BuffetCalculatorService();
    }
    
    @Test
    void calculateBuffet_withValidBasicRequest_shouldReturnCorrectResult() {
        // Given
        BuffetCalculationRequest request = new BuffetCalculationRequest(25, BuffetMenuType.BASIC);
        
        // When
        BuffetCalculationResult result = calculatorService.calculateBuffet(request);
        
        // Then
        assertThat(result.getPersons()).isEqualTo(25);
        assertThat(result.getMenuType()).isEqualTo(BuffetMenuType.BASIC);
        assertThat(result.getPricePerPerson()).isEqualTo(new BigDecimal("12.50"));
        assertThat(result.getNetTotal()).isEqualTo(new BigDecimal("312.50")); // 25 * 12.50
        assertThat(result.getTaxRate()).isEqualTo(0.19);
        assertThat(result.getTaxAmount()).isEqualTo(new BigDecimal("59.38")); // 312.50 * 0.19
        assertThat(result.getGrossTotal()).isEqualTo(new BigDecimal("371.88")); // 312.50 + 59.38
    }
    
    @Test
    void calculateBuffet_withSmallGroupDiscount_shouldApply5PercentDiscount() {
        // Given - 60 persons qualifies for 5% discount
        BuffetCalculationRequest request = new BuffetCalculationRequest(60, BuffetMenuType.PREMIUM);
        
        // When
        BuffetCalculationResult result = calculatorService.calculateBuffet(request);
        
        // Then
        // Premium base price: €18.50, with 5% discount: €17.58
        assertThat(result.getPricePerPerson()).isEqualTo(new BigDecimal("17.58"));
        assertThat(result.getNetTotal()).isEqualTo(new BigDecimal("1054.80")); // 60 * 17.58
    }
    
    @Test
    void calculateBuffet_withLargeGroupDiscount_shouldApply10PercentDiscount() {
        // Given - 120 persons qualifies for 10% discount
        BuffetCalculationRequest request = new BuffetCalculationRequest(120, BuffetMenuType.DELUXE);
        
        // When
        BuffetCalculationResult result = calculatorService.calculateBuffet(request);
        
        // Then
        // Deluxe base price: €24.90, with 10% discount: €22.41
        assertThat(result.getPricePerPerson()).isEqualTo(new BigDecimal("22.41"));
        assertThat(result.getNetTotal()).isEqualTo(new BigDecimal("2689.20")); // 120 * 22.41
    }
    
    @ParameterizedTest
    @EnumSource(BuffetMenuType.class)
    void calculateBuffet_withAllMenuTypes_shouldReturnValidResults(BuffetMenuType menuType) {
        // Given
        BuffetCalculationRequest request = new BuffetCalculationRequest(30, menuType);
        
        // When
        BuffetCalculationResult result = calculatorService.calculateBuffet(request);
        
        // Then
        assertThat(result.getMenuType()).isEqualTo(menuType);
        assertThat(result.getPricePerPerson()).isEqualTo(
            new BigDecimal(String.valueOf(menuType.getBasePricePerPerson()))
        );
        assertThat(result.getNetTotal()).isPositive();
        assertThat(result.getGrossTotal()).isGreaterThan(result.getNetTotal());
    }
    
    @Test
    void calculateBuffet_withNullRequest_shouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> calculatorService.calculateBuffet(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Calculation request cannot be null");
    }
    
    @Test
    void calculateBuffet_withNullPersons_shouldThrowException() {
        // Given
        BuffetCalculationRequest request = new BuffetCalculationRequest(null, BuffetMenuType.BASIC);
        
        // When/Then
        assertThatThrownBy(() -> calculatorService.calculateBuffet(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Minimum 10 persons required");
    }
    
    @Test
    void calculateBuffet_withTooFewPersons_shouldThrowException() {
        // Given
        BuffetCalculationRequest request = new BuffetCalculationRequest(5, BuffetMenuType.BASIC);
        
        // When/Then
        assertThatThrownBy(() -> calculatorService.calculateBuffet(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Minimum 10 persons required");
    }
    
    @Test
    void calculateBuffet_withTooManyPersons_shouldThrowException() {
        // Given
        BuffetCalculationRequest request = new BuffetCalculationRequest(600, BuffetMenuType.BASIC);
        
        // When/Then
        assertThatThrownBy(() -> calculatorService.calculateBuffet(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Maximum 500 persons allowed");
    }
    
    @Test
    void calculateBuffet_withNullMenuType_shouldThrowException() {
        // Given
        BuffetCalculationRequest request = new BuffetCalculationRequest(25, null);
        
        // When/Then
        assertThatThrownBy(() -> calculatorService.calculateBuffet(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Menu type is required");
    }
    
    @Test
    void getAvailableMenuTypes_shouldReturnAllMenuTypes() {
        // When
        BuffetMenuType[] menuTypes = calculatorService.getAvailableMenuTypes();
        
        // Then
        assertThat(menuTypes).hasSize(3);
        assertThat(menuTypes).containsExactly(
            BuffetMenuType.BASIC,
            BuffetMenuType.PREMIUM, 
            BuffetMenuType.DELUXE
        );
    }
    
    @Test
    void calculateBuffet_edgeCaseExactly50Persons_shouldGetSmallGroupDiscount() {
        // Given - exactly 50 persons should get 5% discount
        BuffetCalculationRequest request = new BuffetCalculationRequest(50, BuffetMenuType.BASIC);
        
        // When
        BuffetCalculationResult result = calculatorService.calculateBuffet(request);
        
        // Then
        // Basic price €12.50 with 5% discount = €11.88
        assertThat(result.getPricePerPerson()).isEqualTo(new BigDecimal("11.88"));
    }
    
    @Test
    void calculateBuffet_edgeCaseExactly100Persons_shouldGetLargeGroupDiscount() {
        // Given - exactly 100 persons should get 10% discount
        BuffetCalculationRequest request = new BuffetCalculationRequest(100, BuffetMenuType.BASIC);
        
        // When
        BuffetCalculationResult result = calculatorService.calculateBuffet(request);
        
        // Then
        // Basic price €12.50 with 10% discount = €11.25
        assertThat(result.getPricePerPerson()).isEqualTo(new BigDecimal("11.25"));
    }
}
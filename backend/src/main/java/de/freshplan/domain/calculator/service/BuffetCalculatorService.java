package de.freshplan.domain.calculator.service;

import de.freshplan.domain.calculator.model.BuffetCalculationRequest;
import de.freshplan.domain.calculator.model.BuffetCalculationResult;
import de.freshplan.domain.calculator.model.BuffetMenuType;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Core business logic for buffet calculations.
 * 
 * Pure Java class without framework dependencies (Hexagonal Architecture).
 * Contains pricing logic and volume discounts.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class BuffetCalculatorService {
    
    /**
     * Standard German VAT rate for catering services.
     */
    private static final double VAT_RATE = 0.19;
    
    /**
     * Volume discount thresholds and rates.
     */
    private static final int SMALL_GROUP_THRESHOLD = 50;
    private static final int LARGE_GROUP_THRESHOLD = 100;
    private static final double SMALL_GROUP_DISCOUNT = 0.05; // 5%
    private static final double LARGE_GROUP_DISCOUNT = 0.10; // 10%
    
    /**
     * Calculates buffet pricing for given request.
     * 
     * Applies volume discounts:
     * - 50+ persons: 5% discount
     * - 100+ persons: 10% discount
     * 
     * @param request the calculation request
     * @return calculated pricing result
     * @throws IllegalArgumentException if request is invalid
     */
    public BuffetCalculationResult calculateBuffet(BuffetCalculationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Calculation request cannot be null");
        }
        
        if (request.getPersons() == null || request.getPersons() < 10) {
            throw new IllegalArgumentException("Minimum 10 persons required");
        }
        
        if (request.getPersons() > 500) {
            throw new IllegalArgumentException("Maximum 500 persons allowed");
        }
        
        if (request.getMenuType() == null) {
            throw new IllegalArgumentException("Menu type is required");
        }
        
        // Get base price for menu type
        double basePrice = request.getMenuType().getBasePricePerPerson();
        
        // Apply volume discount
        double discountRate = calculateVolumeDiscount(request.getPersons());
        double finalPricePerPerson = basePrice * (1.0 - discountRate);
        
        BigDecimal pricePerPerson = BigDecimal.valueOf(finalPricePerPerson)
                .setScale(2, RoundingMode.HALF_UP);
        
        return new BuffetCalculationResult(
            request.getPersons(),
            request.getMenuType(), 
            pricePerPerson,
            VAT_RATE
        );
    }
    
    /**
     * Calculates volume discount rate based on number of persons.
     * 
     * @param persons number of persons
     * @return discount rate (0.0 to 1.0)
     */
    private double calculateVolumeDiscount(int persons) {
        if (persons >= LARGE_GROUP_THRESHOLD) {
            return LARGE_GROUP_DISCOUNT;
        } else if (persons >= SMALL_GROUP_THRESHOLD) {
            return SMALL_GROUP_DISCOUNT;
        } else {
            return 0.0;
        }
    }
    
    /**
     * Gets available menu types with their base prices.
     * 
     * @return array of available buffet menu types
     */
    public BuffetMenuType[] getAvailableMenuTypes() {
        return BuffetMenuType.values();
    }
}
package de.freshplan.domain.calculator.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request for buffet calculation.
 * 
 * MVP Version: Only basic buffet calculation with hardcoded pricing.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public final class BuffetCalculationRequest {
    
    @NotNull(message = "Number of persons cannot be null")
    @Min(value = 10, message = "Minimum 10 persons for buffet")
    @Max(value = 500, message = "Maximum 500 persons for buffet")
    private final Integer persons;
    
    @NotNull(message = "Menu type cannot be null")
    private final BuffetMenuType menuType;
    
    /**
     * Creates a buffet calculation request.
     * 
     * @param persons number of persons (10-500)
     * @param menuType type of buffet menu
     */
    public BuffetCalculationRequest(Integer persons, BuffetMenuType menuType) {
        this.persons = persons;
        this.menuType = menuType;
    }
    
    public Integer getPersons() {
        return persons;
    }
    
    public BuffetMenuType getMenuType() {
        return menuType;
    }
    
    @Override
    public String toString() {
        return String.format("BuffetCalculationRequest{persons=%d, menuType=%s}", 
                           persons, menuType);
    }
}
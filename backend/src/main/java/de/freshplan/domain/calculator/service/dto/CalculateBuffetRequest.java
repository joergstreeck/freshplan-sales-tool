package de.freshplan.domain.calculator.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.freshplan.domain.calculator.model.BuffetMenuType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * REST API request DTO for buffet calculation.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public final class CalculateBuffetRequest {
    
    @NotNull(message = "Number of persons cannot be null")
    @Min(value = 10, message = "Minimum 10 persons for buffet")
    @Max(value = 500, message = "Maximum 500 persons for buffet")
    private final Integer persons;
    
    @NotNull(message = "Menu type cannot be null")
    private final BuffetMenuType menuType;
    
    @JsonCreator
    public CalculateBuffetRequest(
            @JsonProperty("persons") Integer persons,
            @JsonProperty("menuType") BuffetMenuType menuType) {
        this.persons = persons;
        this.menuType = menuType;
    }
    
    public Integer getPersons() {
        return persons;
    }
    
    public BuffetMenuType getMenuType() {
        return menuType;
    }
}
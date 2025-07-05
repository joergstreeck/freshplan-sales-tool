package de.freshplan.domain.customer.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for checking duplicate customers.
 * Used to find potential duplicate customers based on company name.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public record CheckDuplicatesRequest(
        
        @NotBlank(message = "Company name is required for duplicate check")
        @Size(min = 2, max = 255, message = "Company name must be between 2 and 255 characters")
        String companyName
        
) {}
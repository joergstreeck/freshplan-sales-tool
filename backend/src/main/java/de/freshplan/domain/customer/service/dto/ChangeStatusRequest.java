package de.freshplan.domain.customer.service.dto;

import de.freshplan.domain.customer.entity.CustomerStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for changing customer status.
 * Includes validation for business rule compliance.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public record ChangeStatusRequest(
        
        @NotNull(message = "New status is required")
        CustomerStatus newStatus
        
) {}
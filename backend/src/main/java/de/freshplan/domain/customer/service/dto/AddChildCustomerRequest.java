package de.freshplan.domain.customer.service.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request DTO for adding a child customer to a parent. Used by the hierarchy management endpoint.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public record AddChildCustomerRequest(
    @NotNull(message = "Child customer ID is required") UUID childId) {}

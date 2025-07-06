package de.freshplan.domain.customer.service.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request DTO for merging two customers. The target customer (from path parameter) will be kept,
 * the source customer (from this request) will be soft-deleted.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public record MergeCustomersRequest(
    @NotNull(message = "Source customer ID is required") UUID sourceId) {}

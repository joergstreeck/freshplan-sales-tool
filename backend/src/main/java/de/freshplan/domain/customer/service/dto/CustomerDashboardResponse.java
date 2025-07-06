package de.freshplan.domain.customer.service.dto;

import de.freshplan.domain.customer.entity.CustomerLifecycleStage;
import de.freshplan.domain.customer.entity.CustomerStatus;
import java.util.Map;

/**
 * Response DTO for customer dashboard data. Contains aggregated statistics for the dashboard view.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public record CustomerDashboardResponse(

    // Basic Statistics
    long totalCustomers,
    long activeCustomers,
    long newThisMonth,
    long atRiskCount,
    long upcomingFollowUps,

    // Distributions
    Map<CustomerStatus, Long> customersByStatus,
    Map<CustomerLifecycleStage, Long> customersByLifecycle) {}

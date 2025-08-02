package de.freshplan.api.resources;

import de.freshplan.domain.cost.entity.BudgetLimit;
import de.freshplan.domain.cost.entity.BudgetPeriod;
import de.freshplan.domain.cost.service.CostTrackingService;
import de.freshplan.domain.cost.service.dto.CostDashboardData;
import de.freshplan.domain.cost.service.dto.CostStatistics;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST API für Cost Management System
 * 
 * Bietet Endpoints für:
 * - Dashboard Data
 * - Cost Statistics  
 * - Budget Management
 * - Alert Triggering
 */
@Path("/api/cost-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CostManagementResource {

    private static final Logger LOG = LoggerFactory.getLogger(CostManagementResource.class);

    // Cost Management API endpoints

    @Inject
    CostTrackingService costService;

    /**
     * Holt Dashboard-Daten für heute
     */
    @GET
    @Path("/dashboard/today")
    public Response getTodayDashboard() {
        try {
            LOG.debug("Fetching today's cost dashboard data");
            CostDashboardData dashboard = costService.getTodayDashboard();
            return Response.ok(dashboard).build();
        } catch (Exception e) {
            LOG.error("Error fetching today's dashboard", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error fetching dashboard data: " + e.getMessage())
                .build();
        }
    }

    /**
     * Holt Cost Statistics für einen Zeitraum
     */
    @GET
    @Path("/statistics")
    public Response getCostStatistics(
            @QueryParam("start") String startStr,
            @QueryParam("end") String endStr) {
        
        try {
            LocalDateTime start = parseDateTime(startStr, LocalDateTime.now().minusDays(7));
            LocalDateTime end = parseDateTime(endStr, LocalDateTime.now());
            
            LOG.debug("Fetching cost statistics from {} to {}", start, end);
            CostStatistics stats = costService.getCostStatistics(start, end);
            return Response.ok(stats).build();
        } catch (Exception e) {
            LOG.error("Error fetching cost statistics", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error fetching statistics: " + e.getMessage())
                .build();
        }
    }

    /**
     * Erstellt oder aktualisiert ein Budget Limit
     */
    @POST
    @Path("/budget-limits")
    public Response createBudgetLimit(BudgetLimitRequest request) {
        try {
            LOG.info("Creating budget limit: {} {} for {} = €{}", 
                request.scope, request.period, request.scopeValue, request.limitAmount);
            
            BudgetLimit limit = costService.createOrUpdateBudgetLimit(
                request.scope,
                request.scopeValue,
                BudgetPeriod.valueOf(request.period.toUpperCase()),
                new BigDecimal(request.limitAmount),
                new BigDecimal(request.alertThreshold != null ? request.alertThreshold : "0.8"),
                new BigDecimal(request.hardStopThreshold != null ? request.hardStopThreshold : "0.95")
            );
            
            return Response.status(Response.Status.CREATED).entity(limit).build();
        } catch (Exception e) {
            LOG.error("Error creating budget limit", e);
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Error creating budget limit: " + e.getMessage())
                .build();
        }
    }

    /**
     * Triggert manuelle Budget-Prüfung
     */
    @POST
    @Path("/trigger-budget-check")
    public Response triggerBudgetCheck() {
        try {
            LOG.info("Manual budget check triggered");
            costService.triggerBudgetCheck();
            return Response.ok().entity("{\"message\": \"Budget check completed\"}").build();
        } catch (Exception e) {
            LOG.error("Error during budget check", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error during budget check: " + e.getMessage())
                .build();
        }
    }

    /**
     * Health Check für Cost Management System
     */
    @GET
    @Path("/health")
    public Response getHealthStatus() {
        try {
            // Einfacher Health Check
            CostDashboardData dashboard = costService.getTodayDashboard();
            
            return Response.ok()
                .entity(new HealthStatus(
                    "Cost Management System operational",
                    dashboard.totalCost,
                    dashboard.runningTransactions
                ))
                .build();
        } catch (Exception e) {
            LOG.error("Cost Management health check failed", e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(new HealthStatus("Cost Management System unavailable", null, 0))
                .build();
        }
    }

    /**
     * Simulate Cost Transaction (für Testing)
     */
    @POST
    @Path("/simulate-transaction")
    public Response simulateTransaction(SimulateTransactionRequest request) {
        try {
            LOG.info("Simulating cost transaction: {} {} for €{}", 
                request.service, request.feature, request.estimatedCost);
            
            // Simuliere eine erfolgreiche Transaktion
            try (var context = costService.startTransaction(
                    request.service,
                    request.feature,
                    request.model != null ? request.model : "test-model",
                    new BigDecimal(request.estimatedCost),
                    request.userId != null ? request.userId : "test-user")) {
                
                // Simuliere etwas Variation in den tatsächlichen Kosten
                BigDecimal actualCost = new BigDecimal(request.estimatedCost)
                    .multiply(BigDecimal.valueOf(0.9 + Math.random() * 0.2)); // ±10% Variation
                
                context.complete(actualCost, request.tokensUsed != null ? request.tokensUsed : 1000);
                
                return Response.ok()
                    .entity("{\"message\": \"Transaction simulated successfully\", \"transactionId\": \"" + 
                           context.getTransactionId() + "\"}")
                    .build();
            }
        } catch (Exception e) {
            LOG.error("Error simulating transaction", e);
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Error simulating transaction: " + e.getMessage())
                .build();
        }
    }

    // Private Helper Methods

    private LocalDateTime parseDateTime(String dateTimeStr, LocalDateTime defaultValue) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            LOG.warn("Could not parse date time: {}, using default", dateTimeStr);
            return defaultValue;
        }
    }

    // Request/Response DTOs

    public static class BudgetLimitRequest {
        public String scope; // "global", "service", "feature", "user"
        public String scopeValue; // optional
        public String period; // "daily", "weekly", "monthly"
        public String limitAmount;
        public String alertThreshold; // optional, default 0.8
        public String hardStopThreshold; // optional, default 0.95
    }

    public static class SimulateTransactionRequest {
        public String service;
        public String feature;
        public String model;
        public String estimatedCost;
        public String userId;
        public Integer tokensUsed;
    }

    public static class HealthStatus {
        public String status;
        public BigDecimal todaysTotalCost;
        public int runningTransactions;

        public HealthStatus(String status, BigDecimal todaysTotalCost, int runningTransactions) {
            this.status = status;
            this.todaysTotalCost = todaysTotalCost;
            this.runningTransactions = runningTransactions;
        }
    }
}
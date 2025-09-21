package de.freshplan.help.api;

import com.freshfoodz.crm.help.operations.CARResponse;
import com.freshfoodz.crm.help.operations.UserLeadOperationsGuide;
import de.freshplan.security.ScopeContext;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.Map;

/**
 * Operations-spezifische REST-Endpoints für User-Lead-Protection Guidance
 * Erweitert das CAR-Strategy Help-System um Operations-Excellence
 */
@Path("/api/help/operations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OperationsResource {

    @Inject
    UserLeadOperationsGuide operationsGuide;

    @Inject
    ScopeContext scope;

    @Inject
    MeterRegistry meter;

    /**
     * User-Lead-Protection Operations-Guidance
     * Bietet strukturierte Guidance für alle Operations-Anfragen
     */
    @POST
    @Path("/user-lead-guidance")
    @RolesAllowed({"user", "manager", "admin", "operations"})
    public Response getUserLeadGuidance(Map<String, Object> request) {
        String query = (String) request.get("query");
        if (query == null || query.isBlank()) {
            return Response.status(400)
                .entity(Map.of("error", "query parameter required"))
                .build();
        }

        double confidence = operationsGuide.getConfidenceForQuery(query);
        if (confidence < 0.5) {
            return Response.status(404)
                .entity(Map.of("error", "No suitable operations guidance found", "confidence", confidence))
                .build();
        }

        CARResponse guidance = operationsGuide.handleQuery(query);

        meter.counter("help_operations_guidance_requests_total",
                     "confidence_tier", getConfidenceTier(confidence),
                     "user_persona", scope.getPersona()).increment();

        return Response.ok(Map.of(
            "guidance", guidance,
            "confidence", confidence,
            "metadata", Map.of(
                "category", operationsGuide.getOperationCategory(),
                "timestamp", java.time.Instant.now(),
                "user_persona", scope.getPersona()
            )
        )).build();
    }

    /**
     * Quick-Check für Operations-Query-Confidence
     * Ermöglicht Frontend zu prüfen ob Operations-Guidance verfügbar ist
     */
    @GET
    @Path("/confidence-check")
    @RolesAllowed({"user", "manager", "admin", "operations"})
    public Response checkConfidence(@QueryParam("query") String query) {
        if (query == null || query.isBlank()) {
            return Response.status(400)
                .entity(Map.of("error", "query parameter required"))
                .build();
        }

        double confidence = operationsGuide.getConfidenceForQuery(query);
        String tier = getConfidenceTier(confidence);
        boolean hasGuidance = confidence >= 0.5;

        return Response.ok(Map.of(
            "confidence", confidence,
            "tier", tier,
            "has_guidance", hasGuidance,
            "category", operationsGuide.getOperationCategory()
        )).build();
    }

    /**
     * Operations-Health-Check für Monitoring
     * Überprüft ob Operations-Guide functionality verfügbar ist
     */
    @GET
    @Path("/health")
    @RolesAllowed({"admin", "operations"})
    public Response healthCheck() {
        try {
            // Test mit bekannter Operations-Query
            double testConfidence = operationsGuide.getConfidenceForQuery("lead protection status");
            boolean healthy = testConfidence > 0.0;

            return Response.ok(Map.of(
                "status", healthy ? "healthy" : "degraded",
                "test_confidence", testConfidence,
                "category", operationsGuide.getOperationCategory(),
                "timestamp", java.time.Instant.now()
            )).build();
        } catch (Exception e) {
            return Response.status(503)
                .entity(Map.of(
                    "status", "unhealthy",
                    "error", e.getMessage(),
                    "timestamp", java.time.Instant.now()
                ))
                .build();
        }
    }

    /**
     * Operations-Templates für häufige Queries
     * Bietet vordefinierte Templates für Standard-Operations-Anfragen
     */
    @GET
    @Path("/templates")
    @RolesAllowed({"user", "manager", "admin", "operations"})
    public Response getOperationsTemplates() {
        return Response.ok(Map.of(
            "templates", Map.of(
                "lead_protection_overview", Map.of(
                    "title", "Lead-Protection Übersicht",
                    "query", "lead protection system overview",
                    "description", "Vollständige Übersicht der User-Lead-Protection (6M+60T+10T)",
                    "confidence", 0.95
                ),
                "reminder_operations", Map.of(
                    "title", "Reminder-Pipeline Operations",
                    "query", "reminder pipeline management",
                    "description", "Reminder-Versendung und Pipeline-Management",
                    "confidence", 0.95
                ),
                "hold_management", Map.of(
                    "title", "Hold-Management (Stop-Clock)",
                    "query", "hold management stop clock",
                    "description", "Hold-Erstellung und Stop-Clock-Management",
                    "confidence", 0.95
                ),
                "state_machine", Map.of(
                    "title", "State-Machine Übersicht",
                    "query", "user lead state machine",
                    "description", "4 Haupt-States: PROTECTED → REMINDER_DUE → GRACE → EXPIRED",
                    "confidence", 0.95
                ),
                "qualified_activities", Map.of(
                    "title", "Qualifizierte Aktivitäten",
                    "query", "qualified activity types",
                    "description", "Aktivitäten die Lead-Protection verlängern",
                    "confidence", 0.95
                )
            ),
            "metadata", Map.of(
                "category", operationsGuide.getOperationCategory(),
                "total_templates", 5
            )
        )).build();
    }

    private String getConfidenceTier(double confidence) {
        if (confidence >= 0.9) return "high";
        if (confidence >= 0.7) return "medium";
        if (confidence >= 0.5) return "low";
        return "insufficient";
    }
}
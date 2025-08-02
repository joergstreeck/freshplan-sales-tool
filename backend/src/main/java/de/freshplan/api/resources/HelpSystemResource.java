package de.freshplan.api.resources;

import de.freshplan.domain.help.entity.HelpType;
import de.freshplan.domain.help.entity.UserLevel;
import de.freshplan.domain.help.service.HelpContentService;
import de.freshplan.domain.help.service.dto.HelpAnalytics;
import de.freshplan.domain.help.service.dto.HelpRequest;
import de.freshplan.domain.help.service.dto.HelpResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API für In-App Help System
 * 
 * Bietet Endpoints für:
 * - Kontextuelle Hilfe-Anfragen
 * - User Feedback Collection
 * - Help Content Search
 * - Analytics und Monitoring
 */
@Path("/api/help")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HelpSystemResource {

    private static final Logger LOG = LoggerFactory.getLogger(HelpSystemResource.class);

    @Inject
    HelpContentService helpService;

    /**
     * Holt kontextuelle Hilfe für ein Feature
     */
    @GET
    @Path("/content/{feature}")
    public Response getHelpForFeature(
            @PathParam("feature") String feature,
            @QueryParam("userId") String userId,
            @QueryParam("userLevel") @DefaultValue("BEGINNER") String userLevel,
            @QueryParam("userRoles") List<String> userRoles,
            @QueryParam("type") String preferredType,
            @QueryParam("firstTime") @DefaultValue("false") boolean firstTime,
            @QueryParam("sessionId") String sessionId) {
        
        try {
            LOG.debug("Help request for feature: {} by user: {} (level: {})", 
                     feature, userId, userLevel);

            HelpType type = null;
            if (preferredType != null && !preferredType.isEmpty()) {
                try {
                    type = HelpType.valueOf(preferredType.toUpperCase());
                } catch (IllegalArgumentException e) {
                    LOG.warn("Invalid help type requested: {}", preferredType);
                }
            }

            HelpRequest request = HelpRequest.builder()
                .userId(userId != null ? userId : "anonymous")
                .feature(feature)
                .userLevel(userLevel)
                .userRoles(userRoles != null ? userRoles : List.of())
                .preferredType(type)
                .isFirstTime(firstTime)
                .sessionId(sessionId)
                .context(Map.of(
                    "timestamp", System.currentTimeMillis(),
                    "endpoint", "getHelpForFeature"
                ))
                .build();

            HelpResponse helpResponse = helpService.getHelpForFeature(request);

            return Response.ok(helpResponse).build();

        } catch (Exception e) {
            LOG.error("Error getting help for feature: {}", feature, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error retrieving help content", e.getMessage()))
                .build();
        }
    }

    /**
     * Registriert User Feedback für Hilfe-Inhalt
     */
    @POST
    @Path("/feedback")
    public Response submitFeedback(FeedbackRequest request) {
        try {
            LOG.info("Feedback submitted for help {}: helpful={}", 
                    request.helpId, request.helpful);

            helpService.recordFeedback(
                request.helpId,
                request.userId,
                request.helpful,
                request.timeSpent,
                request.comment
            );

            return Response.ok(new SuccessResponse("Feedback recorded successfully")).build();

        } catch (Exception e) {
            LOG.error("Error recording feedback for help: {}", request.helpId, e);
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("Error recording feedback", e.getMessage()))
                .build();
        }
    }

    /**
     * Sucht in Hilfe-Inhalten
     */
    @GET
    @Path("/search")
    public Response searchHelp(
            @QueryParam("q") String searchTerm,
            @QueryParam("userId") String userId,
            @QueryParam("userLevel") @DefaultValue("BEGINNER") String userLevel,
            @QueryParam("userRoles") List<String> userRoles) {
        
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Search term is required", "Parameter 'q' cannot be empty"))
                    .build();
            }

            LOG.debug("Help search: '{}' by user: {} (level: {})", 
                     searchTerm, userId, userLevel);

            List<HelpResponse> results = helpService.searchHelp(
                searchTerm.trim(), 
                userLevel, 
                userRoles != null ? userRoles : List.of()
            );

            return Response.ok(new SearchResponse(searchTerm, results.size(), results)).build();

        } catch (Exception e) {
            LOG.error("Error searching help content", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error searching help content", e.getMessage()))
                .build();
        }
    }

    /**
     * Holt Help System Analytics
     */
    @GET
    @Path("/analytics")
    public Response getAnalytics() {
        try {
            LOG.debug("Fetching help system analytics");
            HelpAnalytics analytics = helpService.getAnalytics();
            return Response.ok(analytics).build();

        } catch (Exception e) {
            LOG.error("Error fetching help analytics", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error fetching analytics", e.getMessage()))
                .build();
        }
    }

    /**
     * Holt Feature-spezifische Analytics
     */
    @GET
    @Path("/analytics/{feature}")
    public Response getFeatureAnalytics(@PathParam("feature") String feature) {
        try {
            LOG.debug("Fetching analytics for feature: {}", feature);
            // Diese Methode müsste im Service implementiert werden
            // Für jetzt: Placeholder Response
            return Response.ok(Map.of(
                "feature", feature,
                "message", "Feature analytics not yet implemented"
            )).build();

        } catch (Exception e) {
            LOG.error("Error fetching feature analytics for: {}", feature, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error fetching feature analytics", e.getMessage()))
                .build();
        }
    }

    /**
     * Health Check für Help System
     */
    @GET
    @Path("/health")
    public Response getHealthStatus() {
        try {
            // Einfacher Health Check
            long totalViews = 0; // Würde aus Analytics kommen
            int activeContent = 0; // Würde aus Repository kommen

            return Response.ok(new HealthStatus(
                "Help System operational",
                totalViews,
                activeContent
            )).build();

        } catch (Exception e) {
            LOG.error("Help System health check failed", e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(new HealthStatus("Help System unavailable", 0L, 0))
                .build();
        }
    }

    /**
     * Erstellt neuen Hilfe-Inhalt (Admin-Funktion)
     */
    @POST
    @Path("/content")
    public Response createHelpContent(CreateHelpContentRequest request) {
        try {
            LOG.info("Creating help content: {} - {}", request.feature, request.title);

            UserLevel userLevel = UserLevel.valueOf(request.userLevel.toUpperCase());
            HelpType helpType = HelpType.valueOf(request.type.toUpperCase());

            var content = helpService.createOrUpdateHelpContent(
                request.feature,
                helpType,
                request.title,
                request.shortContent,
                request.mediumContent,
                request.detailedContent,
                userLevel,
                request.roles,
                request.createdBy != null ? request.createdBy : "system"
            );

            return Response.status(Response.Status.CREATED)
                .entity(Map.of(
                    "id", content.id,
                    "message", "Help content created successfully"
                ))
                .build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("Invalid input", e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.error("Error creating help content", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error creating help content", e.getMessage()))
                .build();
        }
    }

    /**
     * Aktiviert/Deaktiviert Hilfe-Inhalt
     */
    @PUT
    @Path("/content/{helpId}/toggle")
    public Response toggleHelpContent(
            @PathParam("helpId") UUID helpId,
            @QueryParam("active") boolean active,
            @QueryParam("updatedBy") String updatedBy) {
        
        try {
            helpService.toggleHelpContent(helpId, active, 
                updatedBy != null ? updatedBy : "system");

            return Response.ok(new SuccessResponse(
                String.format("Help content %s successfully", active ? "activated" : "deactivated")
            )).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Help content not found", e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.error("Error toggling help content: {}", helpId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error updating help content", e.getMessage()))
                .build();
        }
    }

    // Request/Response DTOs

    public static class FeedbackRequest {
        public UUID helpId;
        public String userId;
        public boolean helpful;
        public Integer timeSpent;
        public String comment;
    }

    public static class CreateHelpContentRequest {
        public String feature;
        public String type;
        public String title;
        public String shortContent;
        public String mediumContent;
        public String detailedContent;
        public String userLevel;
        public List<String> roles;
        public String createdBy;
    }

    public static class SearchResponse {
        public String searchTerm;
        public int resultCount;
        public List<HelpResponse> results;

        public SearchResponse(String searchTerm, int resultCount, List<HelpResponse> results) {
            this.searchTerm = searchTerm;
            this.resultCount = resultCount;
            this.results = results;
        }
    }

    public static class HealthStatus {
        public String status;
        public long totalViews;
        public int activeContent;

        public HealthStatus(String status, long totalViews, int activeContent) {
            this.status = status;
            this.totalViews = totalViews;
            this.activeContent = activeContent;
        }
    }

    public static class SuccessResponse {
        public String message;

        public SuccessResponse(String message) {
            this.message = message;
        }
    }

    public static class ErrorResponse {
        public String error;
        public String details;

        public ErrorResponse(String error, String details) {
            this.error = error;
            this.details = details;
        }
    }
}
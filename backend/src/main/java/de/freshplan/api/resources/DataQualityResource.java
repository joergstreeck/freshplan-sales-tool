package de.freshplan.api.resources;

import de.freshplan.api.exception.ErrorResponse;
import de.freshplan.domain.intelligence.dto.DataQualityMetricsDTO;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

@Path("/api/contact-interactions")
@RolesAllowed({"admin", "manager", "sales"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Data Quality", description = "Data quality and intelligence metrics")
public class DataQualityResource {

  private static final Logger LOG = Logger.getLogger(DataQualityResource.class);

  @GET
  @Path("/data-quality/metrics")
  @Operation(
      summary = "Get data quality metrics",
      description =
          "Returns comprehensive data quality metrics including freshness, completeness, and coverage")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Data quality metrics retrieved successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = DataQualityMetricsDTO.class))),
    @APIResponse(responseCode = "401", description = "User not authenticated"),
    @APIResponse(responseCode = "403", description = "User not authorized"),
    @APIResponse(responseCode = "500", description = "Internal server error")
  })
  public Response getDataQualityMetrics() {
    LOG.info("Fetching data quality metrics");

    try {
      // Mock data for now - replace with actual service implementation
      DataQualityMetricsDTO metrics = new DataQualityMetricsDTO();

      // Basic counts
      metrics.setTotalContacts(150L);
      metrics.setContactsWithInteractions(95L);
      metrics.setContactsWithoutInteractions(55L);
      metrics.setTotalInteractions(380L);

      // Freshness metrics
      metrics.setFreshContacts(45L); // < 90 days
      metrics.setAgingContacts(35L); // 90-180 days
      metrics.setStaleContacts(40L); // 180-365 days
      metrics.setCriticalContacts(30L); // > 365 days

      // Quality scores
      metrics.setDataCompletenessScore(72.5);
      metrics.setInteractionCoverage(63.3); // 95/150 * 100
      metrics.setAverageInteractionsPerContact(2.53); // 380/150
      metrics.setContactsWithWarmthScore(85L);

      // Overall quality assessment
      metrics.setOverallDataQuality("GOOD");
      metrics.setShowDataCollectionHints(true);

      // Critical gaps
      metrics.setCriticalDataGaps(
          Arrays.asList(
              "30 Kontakte wurden über 1 Jahr nicht aktualisiert",
              "55 Kontakte haben keine Interaktionen erfasst",
              "65 Kontakte fehlt die Warmth Score Bewertung"));

      // Improvement suggestions
      metrics.setImprovementSuggestions(
          Arrays.asList(
              "Erfassen Sie Interaktionen für die 55 Kontakte ohne Aktivität",
              "Aktualisieren Sie die 30 kritisch veralteten Kontakte",
              "Fügen Sie Warmth Scores für weitere 65 Kontakte hinzu"));

      // Timestamp
      metrics.setLastUpdated(LocalDateTime.now());

      LOG.infof(
          "Data quality metrics: Overall=%s, Completeness=%.1f%%, Coverage=%.1f%%",
          metrics.getOverallDataQuality(),
          metrics.getDataCompletenessScore(),
          metrics.getInteractionCoverage());

      return Response.ok(metrics).build();

    } catch (Exception e) {
      LOG.error("Error fetching data quality metrics", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(
              ErrorResponse.builder()
                  .type("/errors/internal")
                  .title("Internal Server Error")
                  .status(500)
                  .detail("Failed to fetch data quality metrics")
                  .build())
          .build();
    }
  }

  @GET
  @Path("/data-freshness/statistics")
  @Operation(
      summary = "Get data freshness statistics",
      description = "Returns statistics about data freshness levels")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Data freshness statistics retrieved successfully"),
    @APIResponse(responseCode = "401", description = "User not authenticated"),
    @APIResponse(responseCode = "500", description = "Internal server error")
  })
  public Response getDataFreshnessStatistics() {
    LOG.info("Fetching data freshness statistics");

    try {
      // Simple statistics object
      var statistics = new DataFreshnessStatistics();
      statistics.FRESH = 45;
      statistics.AGING = 35;
      statistics.STALE = 40;
      statistics.CRITICAL = 30;
      statistics.total = 150;

      return Response.ok(statistics).build();

    } catch (Exception e) {
      LOG.error("Error fetching data freshness statistics", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(
              ErrorResponse.builder()
                  .type("/errors/internal")
                  .title("Internal Server Error")
                  .status(500)
                  .detail("Failed to fetch data freshness statistics")
                  .build())
          .build();
    }
  }

  // Inner class for simple statistics
  public static class DataFreshnessStatistics {
    public Integer FRESH;
    public Integer AGING;
    public Integer STALE;
    public Integer CRITICAL;
    public Integer total;
  }
}

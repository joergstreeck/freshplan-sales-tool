package de.freshplan.api.resources;

import de.freshplan.domain.cockpit.service.SalesCockpitService;
import de.freshplan.domain.cockpit.service.dto.SalesCockpitDashboard;
import de.freshplan.domain.user.service.exception.UserNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * REST Resource für das Sales Cockpit Backend-for-Frontend (BFF).
 *
 * <p>Dieser Endpunkt aggregiert alle für das Sales Cockpit notwendigen Daten in einer einzigen,
 * optimierten Anfrage.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/sales-cockpit")
@Tag(name = "Sales Cockpit", description = "Backend-for-Frontend Endpunkte für das Sales Cockpit")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class SalesCockpitResource {

  private static final Logger LOG = Logger.getLogger(SalesCockpitResource.class);

  private final SalesCockpitService salesCockpitService;

  @Inject
  public SalesCockpitResource(SalesCockpitService salesCockpitService) {
    this.salesCockpitService = salesCockpitService;
  }

  /**
   * Lädt alle Dashboard-Daten für einen spezifischen Benutzer.
   *
   * <p>Dieser Endpunkt aggregiert: - Heutige Aufgaben - Risiko-Kunden (ohne Kontakt > 90 Tage) -
   * Dashboard-Statistiken - KI-gestützte Alerts
   *
   * @param userId Die ID des Benutzers
   * @return Aggregierte Dashboard-Daten
   */
  @GET
  @Path("/dashboard/{userId}")
  @Operation(
      summary = "Lädt Dashboard-Daten",
      description = "Aggregiert alle für die Cockpit-Startansicht notwendigen Daten")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Dashboard-Daten erfolgreich geladen",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = SalesCockpitDashboard.class))),
    @APIResponse(responseCode = "400", description = "Ungültige Benutzer-ID"),
    @APIResponse(responseCode = "404", description = "Benutzer nicht gefunden"),
    @APIResponse(responseCode = "500", description = "Interner Serverfehler")
  })
  public Response getDashboardData(
      @Parameter(
              description = "ID des Benutzers",
              required = true,
              example = "550e8400-e29b-41d4-a716-446655440000")
          @PathParam("userId")
          String userId) {

    try {
      // Validiere und parse UUID
      UUID userUuid;
      try {
        userUuid = UUID.fromString(userId);
      } catch (IllegalArgumentException e) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity("Invalid user ID format")
            .build();
      }

      // Lade Dashboard-Daten
      SalesCockpitDashboard dashboard = salesCockpitService.getDashboardData(userUuid);

      return Response.ok(dashboard).build();

    } catch (UserNotFoundException e) {
      // Benutzer nicht gefunden
      return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
    } catch (IllegalArgumentException e) {
      // Ungültige Parameter
      return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    } catch (RuntimeException e) {
      LOG.errorf(e, "Internal server error while fetching dashboard data for userId: %s", userId);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("Internal server error occurred")
          .build();
    }
  }

  /**
   * Lädt Dashboard-Daten für die Entwicklungsumgebung.
   *
   * <p>Dieser Endpunkt ist nur in der Entwicklungsumgebung verfügbar und gibt Mock-Daten zurück.
   *
   * @return Mock Dashboard-Daten für Entwicklung
   */
  @io.quarkus.arc.profile.IfBuildProfile("dev")
  @GET
  @Path("/dashboard/dev")
  @Operation(
      summary = "Lädt Development Dashboard-Daten",
      description = "Gibt Mock-Daten für die Entwicklungsumgebung zurück")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Development Dashboard-Daten erfolgreich geladen",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = SalesCockpitDashboard.class))),
    @APIResponse(responseCode = "500", description = "Interner Serverfehler")
  })
  public Response getDevDashboardData() {
    try {
      SalesCockpitDashboard dashboard = salesCockpitService.getDevDashboardData();
      return Response.ok(dashboard).build();
    } catch (RuntimeException e) {
      LOG.errorf(e, "Internal server error while fetching dev dashboard data");
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("Internal server error occurred")
          .build();
    }
  }

  /**
   * Health-Check Endpunkt für das Sales Cockpit.
   *
   * @return Status des Sales Cockpit Service
   */
  @GET
  @Path("/health")
  @Operation(summary = "Health Check", description = "Prüft den Status des Sales Cockpit Service")
  @APIResponse(responseCode = "200", description = "Service ist verfügbar")
  public Response health() {
    return Response.ok().entity("{\"status\":\"UP\",\"service\":\"sales-cockpit\"}").build();
  }
}

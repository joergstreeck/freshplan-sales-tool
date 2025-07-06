package de.freshplan.api.resources;

import de.freshplan.domain.cockpit.service.SalesCockpitService;
import de.freshplan.domain.cockpit.service.dto.SalesCockpitDashboard;
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

    } catch (IllegalArgumentException e) {
      // Benutzer nicht gefunden oder ungültige Parameter
      if (e.getMessage().contains("not found")) {
        return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
      } else {
        return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
      }
    } catch (RuntimeException e) {
      // Unerwarteter Laufzeitfehler
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

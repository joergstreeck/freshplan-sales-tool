package de.freshplan.modules.leads.api;

import de.freshplan.modules.leads.domain.GdprDataRequest;
import de.freshplan.modules.leads.domain.GdprDeletionLog;
import de.freshplan.modules.leads.service.GdprService;
import de.freshplan.modules.leads.service.GdprService.GdprDeletionBlockedException;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.time.LocalDateTime;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * REST Resource für DSGVO-Compliance Operationen.
 *
 * <p>Implementiert die gesetzlich vorgeschriebenen DSGVO-Endpoints:
 *
 * <ul>
 *   <li><strong>Art. 15 - Auskunftsrecht:</strong> GET /api/gdpr/leads/{id}/data-export
 *   <li><strong>Art. 17 - Löschrecht:</strong> DELETE /api/gdpr/leads/{id}
 *   <li><strong>Art. 7.3 - Einwilligungswiderruf:</strong> POST /api/gdpr/leads/{id}/revoke-consent
 * </ul>
 *
 * <p><strong>RBAC:</strong>
 *
 * <ul>
 *   <li>Art. 15 + Art. 17: Nur ADMIN und MANAGER (Datenschutzbeauftragter)
 *   <li>Art. 7.3: Alle authentifizierten Benutzer (Kunde hat Recht!)
 * </ul>
 *
 * @since Sprint 2.1.8
 */
@Path("/api/gdpr")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@SecurityScheme(securitySchemeName = "oidc", type = SecuritySchemeType.OPENIDCONNECT)
@SecurityRequirement(name = "oidc")
@Tag(name = "GDPR", description = "DSGVO-Compliance Operationen (Art. 15, 17, 7.3)")
public class GdprResource {

  private static final Logger LOG = Logger.getLogger(GdprResource.class);

  @Inject GdprService gdprService;

  // ============================================================================
  // Art. 15 - Auskunftsrecht (Datenexport)
  // ============================================================================

  /**
   * Generiert einen DSGVO-konformen Datenexport (Art. 15).
   *
   * <p>Antwortfrist: 1 Monat ab Anfrage (Art. 12 Abs. 3 DSGVO)
   *
   * @param leadId ID des Leads
   * @param securityContext Security Context für User-ID
   * @return PDF-Datei mit allen personenbezogenen Daten
   */
  @GET
  @Path("/leads/{id}/data-export")
  @Produces("application/pdf")
  @RolesAllowed({"ADMIN", "MANAGER"})
  @Operation(
      summary = "DSGVO Datenexport (Art. 15)",
      description =
          "Generiert einen PDF-Export mit allen personenbezogenen Daten eines Leads. "
              + "Nur für Manager und Admins (Datenschutzbeauftragte) zugänglich.")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "PDF erfolgreich generiert",
        content = @Content(mediaType = "application/pdf")),
    @APIResponse(responseCode = "404", description = "Lead nicht gefunden"),
    @APIResponse(responseCode = "403", description = "Keine Berechtigung")
  })
  public Response generateDataExport(
      @Parameter(description = "Lead ID") @PathParam("id") Long leadId,
      @Context SecurityContext securityContext) {

    String userId = securityContext.getUserPrincipal().getName();
    LOG.infof("Art. 15 Data Export requested for Lead %d by %s", leadId, userId);

    try {
      byte[] pdf = gdprService.generateDataExport(leadId, userId);

      String filename = "dsgvo-auskunft-lead-" + leadId + "-" + System.currentTimeMillis() + ".pdf";

      return Response.ok(pdf)
          .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
          .header("Content-Type", "application/pdf")
          .build();

    } catch (IllegalArgumentException e) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse("Lead nicht gefunden: " + leadId))
          .build();
    }
  }

  /**
   * Listet alle Datenexport-Anfragen für einen Lead.
   *
   * @param leadId ID des Leads
   * @return Liste der Anfragen
   */
  @GET
  @Path("/leads/{id}/data-requests")
  @RolesAllowed({"ADMIN", "MANAGER"})
  @Operation(
      summary = "Datenexport-Anfragen auflisten",
      description = "Listet alle Art. 15 Datenexport-Anfragen für einen Lead auf.")
  public List<GdprDataRequestDTO> getDataRequests(
      @Parameter(description = "Lead ID") @PathParam("id") Long leadId) {

    return gdprService.getDataRequestsForLead(leadId).stream()
        .map(GdprDataRequestDTO::fromEntity)
        .toList();
  }

  // ============================================================================
  // Art. 17 - Löschrecht (Soft-Delete + PII-Anonymisierung)
  // ============================================================================

  /**
   * DSGVO-konforme Löschung eines Leads (Art. 17).
   *
   * <p><strong>Implementierung:</strong> Soft-Delete + PII-Anonymisierung (kein Hard-Delete)
   *
   * @param leadId ID des Leads
   * @param request Löschgrund
   * @param securityContext Security Context für User-ID
   * @return 204 No Content bei Erfolg
   */
  @DELETE
  @Path("/leads/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({"ADMIN", "MANAGER"})
  @Operation(
      summary = "DSGVO-Löschung (Art. 17)",
      description =
          "Führt eine DSGVO-konforme Löschung durch (Soft-Delete + PII-Anonymisierung). "
              + "Die personenbezogenen Daten werden anonymisiert, der Audit-Trail bleibt erhalten.")
  @APIResponses({
    @APIResponse(responseCode = "204", description = "Erfolgreich gelöscht"),
    @APIResponse(responseCode = "400", description = "Ungültige Anfrage"),
    @APIResponse(responseCode = "404", description = "Lead nicht gefunden"),
    @APIResponse(
        responseCode = "409",
        description = "Löschung blockiert (z.B. offene Opportunities)")
  })
  public Response gdprDeleteLead(
      @Parameter(description = "Lead ID") @PathParam("id") Long leadId,
      GdprDeleteRequest request,
      @Context SecurityContext securityContext) {

    String userId = securityContext.getUserPrincipal().getName();
    LOG.infof(
        "Art. 17 GDPR Delete requested for Lead %d by %s. Reason: %s",
        leadId, userId, request.reason());

    try {
      gdprService.gdprDeleteLead(leadId, userId, request.reason());
      return Response.noContent().build();

    } catch (IllegalArgumentException e) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse("Lead nicht gefunden: " + leadId))
          .build();
    } catch (IllegalStateException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new ErrorResponse(e.getMessage()))
          .build();
    } catch (GdprDeletionBlockedException e) {
      return Response.status(Response.Status.CONFLICT)
          .entity(new ErrorResponse(e.getMessage()))
          .build();
    }
  }

  /**
   * Listet alle Löschprotokolle für einen Lead.
   *
   * @param leadId ID des Leads
   * @return Liste der Löschprotokolle
   */
  @GET
  @Path("/leads/{id}/deletion-logs")
  @RolesAllowed({"ADMIN", "MANAGER"})
  @Operation(
      summary = "Löschprotokolle auflisten",
      description = "Listet alle Art. 17 DSGVO-Löschprotokolle für einen Lead auf.")
  public List<GdprDeletionLogDTO> getDeletionLogs(
      @Parameter(description = "Lead ID") @PathParam("id") Long leadId) {

    return gdprService.getDeletionLogsForLead(leadId).stream()
        .map(GdprDeletionLogDTO::fromEntity)
        .toList();
  }

  // ============================================================================
  // Art. 7.3 - Einwilligungswiderruf
  // ============================================================================

  /**
   * Widerruft die Einwilligung eines Leads (Art. 7 Abs. 3).
   *
   * <p>Nach Widerruf ist keine Kontaktaufnahme mehr erlaubt.
   *
   * @param leadId ID des Leads
   * @param securityContext Security Context für User-ID
   * @return 204 No Content bei Erfolg
   */
  @POST
  @Path("/leads/{id}/revoke-consent")
  @Operation(
      summary = "Einwilligung widerrufen (Art. 7.3)",
      description =
          "Widerruft die Einwilligung des Leads. Nach Widerruf ist keine Kontaktaufnahme "
              + "mehr erlaubt. Alle Rollen können diese Aktion ausführen (Kunde hat Recht!).")
  @APIResponses({
    @APIResponse(responseCode = "204", description = "Erfolgreich widerrufen"),
    @APIResponse(responseCode = "400", description = "Bereits widerrufen"),
    @APIResponse(responseCode = "404", description = "Lead nicht gefunden")
  })
  public Response revokeConsent(
      @Parameter(description = "Lead ID") @PathParam("id") Long leadId,
      @Context SecurityContext securityContext) {

    String userId = securityContext.getUserPrincipal().getName();
    LOG.infof("Art. 7.3 Consent Revocation requested for Lead %d by %s", leadId, userId);

    try {
      gdprService.revokeConsent(leadId, userId);
      return Response.noContent().build();

    } catch (IllegalArgumentException e) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse("Lead nicht gefunden: " + leadId))
          .build();
    } catch (IllegalStateException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new ErrorResponse(e.getMessage()))
          .build();
    }
  }

  /**
   * Prüft ob ein Lead kontaktiert werden darf.
   *
   * @param leadId ID des Leads
   * @return true wenn Kontakt erlaubt, false wenn gesperrt
   */
  @GET
  @Path("/leads/{id}/contact-allowed")
  @Operation(
      summary = "Kontakt-Erlaubnis prüfen",
      description = "Prüft ob ein Lead kontaktiert werden darf (nicht gesperrt, nicht gelöscht).")
  public ContactAllowedResponse isContactAllowed(
      @Parameter(description = "Lead ID") @PathParam("id") Long leadId) {

    boolean allowed = gdprService.isContactAllowed(leadId);
    return new ContactAllowedResponse(leadId, allowed);
  }

  // ============================================================================
  // DTOs
  // ============================================================================

  /** Request für DSGVO-Löschung */
  public record GdprDeleteRequest(
      @NotBlank(message = "Löschgrund ist erforderlich")
          @Size(
              min = 10,
              max = 500,
              message = "Löschgrund muss zwischen 10 und 500 Zeichen lang sein")
          String reason) {}

  /** DTO für Datenexport-Anfragen */
  public record GdprDataRequestDTO(
      Long id,
      String entityType,
      Long entityId,
      String requestedBy,
      LocalDateTime requestedAt,
      Boolean pdfGenerated,
      LocalDateTime pdfGeneratedAt) {

    public static GdprDataRequestDTO fromEntity(GdprDataRequest entity) {
      return new GdprDataRequestDTO(
          entity.id,
          entity.entityType,
          entity.entityId,
          entity.requestedBy,
          entity.requestedAt,
          entity.pdfGenerated,
          entity.pdfGeneratedAt);
    }
  }

  /** DTO für Löschprotokolle */
  public record GdprDeletionLogDTO(
      Long id,
      String entityType,
      Long entityId,
      String deletedBy,
      LocalDateTime deletedAt,
      String deletionReason,
      String originalDataHash) {

    public static GdprDeletionLogDTO fromEntity(GdprDeletionLog entity) {
      return new GdprDeletionLogDTO(
          entity.id,
          entity.entityType,
          entity.entityId,
          entity.deletedBy,
          entity.deletedAt,
          entity.deletionReason,
          entity.originalDataHash);
    }
  }

  /** Response für Kontakt-Erlaubnis-Prüfung */
  public record ContactAllowedResponse(Long leadId, boolean contactAllowed) {}

  /** Standard Error Response */
  @Schema(description = "Error Response")
  public record ErrorResponse(String message) {}
}

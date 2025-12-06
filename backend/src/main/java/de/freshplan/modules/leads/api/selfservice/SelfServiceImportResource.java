package de.freshplan.modules.leads.api.selfservice;

import de.freshplan.modules.leads.api.selfservice.dto.*;
import de.freshplan.modules.leads.service.FileParserService.FileParseException;
import de.freshplan.modules.leads.service.ImportQuotaService;
import de.freshplan.modules.leads.service.ImportQuotaService.QuotaInfo;
import de.freshplan.modules.leads.service.ImportQuotaService.UserRole;
import de.freshplan.modules.leads.service.SelfServiceImportService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

/**
 * Self-Service Lead-Import REST API - Sprint 2.1.8 Phase 2
 *
 * <p>4-Schritt Import-Wizard API:
 *
 * <ol>
 *   <li>POST /upload - Datei hochladen
 *   <li>POST /{uploadId}/preview - Mapping + Validierung
 *   <li>POST /{uploadId}/execute - Import ausführen
 *   <li>GET /quota - Quota-Informationen abrufen
 * </ol>
 *
 * @since Sprint 2.1.8
 */
@Path("/api/leads/import")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"USER", "MANAGER", "ADMIN"})
public class SelfServiceImportResource {

  private static final Logger LOG = Logger.getLogger(SelfServiceImportResource.class);

  @Inject SelfServiceImportService importService;

  @Inject ImportQuotaService quotaService;

  @Inject SecurityIdentity securityIdentity;

  @ConfigProperty(name = "app.dev.fallback-user-id", defaultValue = "dev-admin-001")
  String fallbackUserId;

  // ============================================================================
  // Schritt 1: Upload
  // ============================================================================

  /**
   * Lädt eine CSV/Excel-Datei hoch und gibt Spalten + Mapping-Vorschläge zurück.
   *
   * @param file Hochgeladene Datei
   * @return Upload-Response mit Spalten und Auto-Mapping
   */
  @POST
  @Path("/upload")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response uploadFile(@RestForm("file") FileUpload file) {
    String userId = getCurrentUserId();
    LOG.infof("Upload request from user: %s", userId);

    if (file == null) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new ErrorResponse("Keine Datei hochgeladen"))
          .build();
    }

    try (InputStream inputStream = file.uploadedFile().toFile().toURI().toURL().openStream()) {
      ImportUploadResponse response =
          importService.uploadFile(inputStream, file.fileName(), file.size());

      return Response.ok(response).build();
    } catch (FileParseException e) {
      LOG.warnf("File parse error: %s", e.getMessage());
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new ErrorResponse(e.getMessage()))
          .build();
    } catch (Exception e) {
      LOG.errorf(e, "Upload failed");
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new ErrorResponse("Upload fehlgeschlagen: " + e.getMessage()))
          .build();
    }
  }

  // ============================================================================
  // Schritt 2/3: Preview
  // ============================================================================

  /**
   * Erstellt eine Vorschau mit Validierung und Duplikat-Check.
   *
   * @param uploadId Upload-ID aus Schritt 1
   * @param request Mapping-Request
   * @return Preview-Response mit Validierung
   */
  @POST
  @Path("/{uploadId}/preview")
  public Response preview(
      @PathParam("uploadId") String uploadId, @Valid ImportPreviewRequest request) {

    String userId = getCurrentUserId();
    UserRole role = getCurrentUserRole();

    LOG.infof("Preview request: uploadId=%s, user=%s, role=%s", uploadId, userId, role);

    try {
      ImportPreviewResponse response =
          importService.preview(uploadId, request.mapping(), userId, role);

      return Response.ok(response).build();
    } catch (IllegalArgumentException e) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse(e.getMessage()))
          .build();
    } catch (Exception e) {
      LOG.errorf(e, "Preview failed: uploadId=%s", uploadId);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new ErrorResponse("Vorschau fehlgeschlagen: " + e.getMessage()))
          .build();
    }
  }

  // ============================================================================
  // Schritt 4: Execute
  // ============================================================================

  /**
   * Führt den Import aus.
   *
   * @param uploadId Upload-ID aus Schritt 1
   * @param request Import-Request mit Mapping und Optionen
   * @return Import-Ergebnis
   */
  @POST
  @Path("/{uploadId}/execute")
  public Response execute(
      @PathParam("uploadId") String uploadId, @Valid ImportExecuteRequest request) {

    String userId = getCurrentUserId();
    UserRole role = getCurrentUserRole();

    LOG.infof(
        "Execute request: uploadId=%s, user=%s, role=%s, duplicateAction=%s",
        uploadId, userId, role, request.duplicateAction());

    try {
      ImportExecuteResponse response = importService.execute(uploadId, request, userId, role);

      if (response.success()) {
        return Response.status(Response.Status.CREATED).entity(response).build();
      } else if ("PENDING_APPROVAL".equals(response.status())) {
        return Response.status(Response.Status.ACCEPTED).entity(response).build();
      } else {
        return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
      }
    } catch (IllegalArgumentException e) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse(e.getMessage()))
          .build();
    } catch (Exception e) {
      LOG.errorf(e, "Execute failed: uploadId=%s", uploadId);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new ErrorResponse("Import fehlgeschlagen: " + e.getMessage()))
          .build();
    }
  }

  // ============================================================================
  // Error Report CSV-Download
  // ============================================================================

  /**
   * Generiert einen CSV-Download mit allen Validierungsfehlern aus dem Preview.
   *
   * <p>Das CSV enthält: Zeile, Spalte, Fehlertyp, Fehlermeldung, Originalwert
   *
   * @param uploadId Upload-ID aus Schritt 1
   * @param request Mapping für erneute Validierung
   * @return CSV-Datei mit Fehlern
   * @since Sprint 2.1.8
   */
  @POST
  @Path("/{uploadId}/errors.csv")
  @Produces("text/csv; charset=UTF-8")
  public Response downloadErrorReport(
      @PathParam("uploadId") String uploadId, @Valid ImportPreviewRequest request) {

    String userId = getCurrentUserId();
    UserRole role = getCurrentUserRole();

    LOG.infof("Error report download: uploadId=%s, user=%s", uploadId, userId);

    try {
      // Preview erneut ausführen um aktuelle Fehler zu bekommen
      ImportPreviewResponse preview =
          importService.preview(uploadId, request.mapping(), userId, role);

      // CSV generieren
      StringBuilder csv = new StringBuilder();

      // Header (BOM für Excel-Kompatibilität)
      csv.append("\uFEFF"); // UTF-8 BOM
      csv.append("Zeile;Spalte;Fehlertyp;Fehlermeldung;Originalwert\n");

      // Validierungsfehler
      for (var error : preview.errors()) {
        csv.append(error.row()).append(";");
        csv.append(escapeCsv(error.column())).append(";");
        csv.append("VALIDATION;");
        csv.append(escapeCsv(error.message())).append(";");
        csv.append(escapeCsv(error.value())).append("\n");
      }

      // Duplikate auch als "Warnung" ausgeben
      for (var dup : preview.duplicates()) {
        csv.append(dup.row()).append(";");
        csv.append("companyName;");
        csv.append(dup.type()).append(";");
        csv.append(
                escapeCsv(
                    "Mögliches Duplikat: "
                        + dup.existingCompanyName()
                        + " (ID: "
                        + dup.existingLeadId()
                        + ", Ähnlichkeit: "
                        + Math.round(dup.similarity() * 100)
                        + "%)"))
            .append(";");
        csv.append("-\n");
      }

      // Wenn keine Fehler, Info-Zeile hinzufügen
      if (preview.errors().isEmpty() && preview.duplicates().isEmpty()) {
        csv.append("-;-;INFO;Keine Validierungsfehler gefunden;-\n");
      }

      String fileName = "import-errors-" + uploadId.substring(0, 8) + ".csv";

      return Response.ok(csv.toString())
          .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
          .header("Content-Type", "text/csv; charset=UTF-8")
          .build();

    } catch (IllegalArgumentException e) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse(e.getMessage()))
          .build();
    } catch (Exception e) {
      LOG.errorf(e, "Error report generation failed: uploadId=%s", uploadId);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new ErrorResponse("Fehlerreport konnte nicht erstellt werden: " + e.getMessage()))
          .build();
    }
  }

  /**
   * Escaped einen Wert für CSV (Semikolon-getrennt). Wenn der Wert Semikolon, Anführungszeichen
   * oder Zeilenumbruch enthält, wird er in Anführungszeichen eingeschlossen.
   */
  private String escapeCsv(String value) {
    if (value == null) {
      return "";
    }
    if (value.contains(";")
        || value.contains("\"")
        || value.contains("\n")
        || value.contains("\r")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }

  // ============================================================================
  // Quota-Info
  // ============================================================================

  /**
   * Gibt die aktuellen Quota-Informationen für den User zurück.
   *
   * @return Quota-Info
   */
  @GET
  @Path("/quota")
  public Response getQuota() {
    String userId = getCurrentUserId();
    UserRole role = getCurrentUserRole();

    QuotaInfo quotaInfo = quotaService.getQuotaInfo(userId, role);

    return Response.ok(quotaInfo).build();
  }

  // ============================================================================
  // Helper Methods
  // ============================================================================

  /**
   * Get current user ID with dev mode fallback. In dev mode, auth is disabled and
   * SecurityContext.getUserPrincipal() returns null.
   */
  private String getCurrentUserId() {
    if (securityIdentity.getPrincipal() != null
        && securityIdentity.getPrincipal().getName() != null
        && !securityIdentity.getPrincipal().getName().isBlank()) {
      return securityIdentity.getPrincipal().getName();
    }
    return fallbackUserId; // Fallback for dev mode (configurable)
  }

  /** Get current user role with dev mode fallback. In dev mode returns ADMIN for full access. */
  private UserRole getCurrentUserRole() {
    if (securityIdentity.hasRole("ADMIN") || securityIdentity.hasRole("admin")) {
      return UserRole.ADMIN;
    } else if (securityIdentity.hasRole("MANAGER") || securityIdentity.hasRole("manager")) {
      return UserRole.MANAGER;
    } else if (securityIdentity.hasRole("USER") || securityIdentity.hasRole("sales")) {
      return UserRole.SALES;
    } else if (securityIdentity.hasRole("AUDITOR") || securityIdentity.hasRole("auditor")) {
      return UserRole.AUDITOR;
    }
    // Default: ADMIN in dev mode for full access to import features
    return UserRole.ADMIN;
  }

  // ============================================================================
  // DTOs
  // ============================================================================

  public record ErrorResponse(String message) {}
}

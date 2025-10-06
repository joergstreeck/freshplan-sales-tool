package de.freshplan.modules.leads.api.admin;

import de.freshplan.modules.leads.api.admin.dto.LeadImportRequest;
import de.freshplan.modules.leads.api.admin.dto.LeadImportResponse;
import de.freshplan.modules.leads.service.LeadImportService;
import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Bestandsleads-Migration Admin API (Modul 08).
 *
 * <p>Admin-only Endpoint für Batch-Import von historischen Leads.
 *
 * <p>Sprint 2.1.6 - User Story 1
 */
@Path("/api/admin/migration/leads")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ROLE_ADMIN"})
public class LeadImportResource {

  @Inject LeadImportService leadImportService;

  @Inject SecurityIdentity securityIdentity;

  /**
   * Import Bestandsleads (Batch).
   *
   * <p>Max. 1000 Leads/Batch. Dry-Run Mode PFLICHT vor Real-Import.
   *
   * @param request Import-Request mit dryRun flag und Lead-Daten
   * @return Detaillierte Response mit Erfolg/Fehler pro Lead
   */
  @POST
  @Path("/import")
  public Response importLeads(@Valid LeadImportRequest request) {

    // Security Check
    String currentUserId = securityIdentity.getPrincipal().getName();
    if (currentUserId == null || currentUserId.isBlank()) {
      Log.warn("Import attempt without valid user identity");
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity("User identity required for import")
          .build();
    }

    Log.infof(
        "Lead import request - dryRun=%s, leads=%d, user=%s",
        request.dryRun, request.leads.size(), currentUserId);

    // Batch size validation
    if (request.leads.size() > 1000) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Maximum 1000 leads per batch. Split into multiple requests.")
          .build();
    }

    // Process import
    try {
      LeadImportResponse response = leadImportService.importLeads(request, currentUserId);

      // Return appropriate status
      if (request.dryRun) {
        return Response.ok(response).build();
      }

      // Partial success: Some leads failed OR had validation errors
      if (response.statistics.failureCount > 0 || response.statistics.validationErrors > 0) {
        return Response.status(Response.Status.PARTIAL_CONTENT).entity(response).build();
      }

      return Response.status(Response.Status.CREATED).entity(response).build();

    } catch (Exception e) {
      Log.errorf(e, "Lead import failed - user=%s", currentUserId);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("Import failed: " + e.getMessage())
          .build();
    }
  }
}

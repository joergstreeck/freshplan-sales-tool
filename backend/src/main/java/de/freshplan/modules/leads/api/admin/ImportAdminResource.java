package de.freshplan.modules.leads.api.admin;

import de.freshplan.modules.leads.domain.ImportLog;
import de.freshplan.modules.leads.domain.ImportLog.ImportLogStatus;
import de.freshplan.modules.leads.service.ImportNotificationService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * Admin REST Resource für Import-Übersicht.
 *
 * <p>Bietet Admin-Endpoints für das Import-Dashboard:
 *
 * <ul>
 *   <li>Übersicht aller Imports
 *   <li>Pending Approvals (>10% Duplikate)
 *   <li>Approve/Reject Workflow
 *   <li>Import-Statistiken
 * </ul>
 *
 * @since Sprint 2.1.8 Phase 3
 */
@Path("/api/admin/imports")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "MANAGER"})
@SecurityScheme(securitySchemeName = "oidc", type = SecuritySchemeType.OPENIDCONNECT)
@SecurityRequirement(name = "oidc")
@Tag(name = "Import Admin", description = "Import-Admin-Übersicht für Manager und Admins")
public class ImportAdminResource {

  private static final Logger LOG = Logger.getLogger(ImportAdminResource.class);

  @Inject ImportNotificationService notificationService;

  // ============================================================================
  // Statistiken
  // ============================================================================

  @GET
  @Path("/stats")
  @Operation(
      summary = "Import-Statistiken",
      description = "Liefert Statistiken für das Import-Dashboard.")
  public ImportStatsResponse getStats() {
    long totalImports = ImportLog.count();
    long pendingApprovals = ImportLog.count("status", ImportLogStatus.PENDING_APPROVAL);
    long completedImports = ImportLog.count("status", ImportLogStatus.COMPLETED);
    long rejectedImports = ImportLog.count("status", ImportLogStatus.REJECTED);

    // Summen berechnen
    Long totalImported =
        ImportLog.find("SELECT COALESCE(SUM(importedCount), 0) FROM ImportLog")
            .project(Long.class)
            .firstResult();
    Long totalSkipped =
        ImportLog.find("SELECT COALESCE(SUM(skippedCount), 0) FROM ImportLog")
            .project(Long.class)
            .firstResult();
    Long totalErrors =
        ImportLog.find("SELECT COALESCE(SUM(errorCount), 0) FROM ImportLog")
            .project(Long.class)
            .firstResult();

    return new ImportStatsResponse(
        totalImports,
        pendingApprovals,
        completedImports,
        rejectedImports,
        totalImported,
        totalSkipped,
        totalErrors);
  }

  // ============================================================================
  // Import-Liste
  // ============================================================================

  @GET
  @Operation(summary = "Alle Imports", description = "Listet alle Lead-Imports auf.")
  public List<ImportLogDTO> getAllImports(
      @QueryParam("status") String status, @QueryParam("limit") @DefaultValue("100") int limit) {

    List<ImportLog> logs;
    if (status != null && !status.isEmpty()) {
      ImportLogStatus statusEnum;
      try {
        statusEnum = ImportLogStatus.valueOf(status.toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new BadRequestException("Ungültiger Status-Parameter: " + status);
      }
      logs = ImportLog.list("status = ?1 ORDER BY importedAt DESC", statusEnum);
    } else {
      logs = ImportLog.list("ORDER BY importedAt DESC");
    }

    return logs.stream().limit(limit).map(ImportLogDTO::fromEntity).toList();
  }

  @GET
  @Path("/pending")
  @Operation(
      summary = "Pending Approvals",
      description = "Listet alle Imports auf, die auf Genehmigung warten (>10% Duplikate).")
  public List<ImportLogDTO> getPendingApprovals() {
    return ImportLog.findPendingApprovals().stream().map(ImportLogDTO::fromEntity).toList();
  }

  // ============================================================================
  // Approval Workflow
  // ============================================================================

  @POST
  @Path("/{id}/approve")
  @Transactional
  @RolesAllowed({"ADMIN", "MANAGER"})
  @Operation(summary = "Import genehmigen", description = "Genehmigt einen wartenden Import.")
  public Response approveImport(
      @PathParam("id") UUID importId, @Context SecurityContext securityContext) {

    String userId = securityContext.getUserPrincipal().getName();
    LOG.infof("Import approval requested for %s by %s", importId, userId);

    ImportLog importLog = ImportLog.findById(importId);
    if (importLog == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse("Import nicht gefunden: " + importId))
          .build();
    }

    if (importLog.status != ImportLogStatus.PENDING_APPROVAL) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new ErrorResponse("Import ist nicht in Status PENDING_APPROVAL"))
          .build();
    }

    importLog.approve(userId);
    LOG.infof("Import %s approved by %s", importId, userId);

    // Notify user about approval (async, non-blocking)
    // Note: userEmail would need to be fetched from Keycloak/User service
    // For now we use userId which might be an email in some setups
    notificationService.notifyImportApproved(
        importLog.id, importLog.userId, importLog.fileName, importLog.importedCount);

    return Response.ok(ImportLogDTO.fromEntity(importLog)).build();
  }

  @POST
  @Path("/{id}/reject")
  @Transactional
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({"ADMIN", "MANAGER"})
  @Operation(summary = "Import ablehnen", description = "Lehnt einen wartenden Import ab.")
  public Response rejectImport(
      @PathParam("id") UUID importId,
      RejectRequest request,
      @Context SecurityContext securityContext) {

    String userId = securityContext.getUserPrincipal().getName();
    LOG.infof(
        "Import rejection requested for %s by %s. Reason: %s", importId, userId, request.reason());

    ImportLog importLog = ImportLog.findById(importId);
    if (importLog == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse("Import nicht gefunden: " + importId))
          .build();
    }

    if (importLog.status != ImportLogStatus.PENDING_APPROVAL) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new ErrorResponse("Import ist nicht in Status PENDING_APPROVAL"))
          .build();
    }

    importLog.reject(userId, request.reason());
    LOG.infof("Import %s rejected by %s", importId, userId);

    // Notify user about rejection (async, non-blocking)
    notificationService.notifyImportRejected(
        importLog.id, importLog.userId, importLog.fileName, request.reason());

    return Response.ok(ImportLogDTO.fromEntity(importLog)).build();
  }

  // ============================================================================
  // DTOs
  // ============================================================================

  public record ImportStatsResponse(
      long totalImports,
      long pendingApprovals,
      long completedImports,
      long rejectedImports,
      long totalImported,
      long totalSkipped,
      long totalErrors) {}

  public record ImportLogDTO(
      UUID id,
      String userId,
      LocalDateTime importedAt,
      Integer totalRows,
      Integer importedCount,
      Integer skippedCount,
      Integer errorCount,
      BigDecimal duplicateRate,
      String source,
      String fileName,
      Long fileSizeBytes,
      String fileType,
      String status,
      String approvedBy,
      LocalDateTime approvedAt,
      String rejectionReason) {

    public static ImportLogDTO fromEntity(ImportLog entity) {
      return new ImportLogDTO(
          entity.id,
          entity.userId,
          entity.importedAt,
          entity.totalRows,
          entity.importedCount,
          entity.skippedCount,
          entity.errorCount,
          entity.duplicateRate,
          entity.source,
          entity.fileName,
          entity.fileSizeBytes,
          entity.fileType,
          entity.status.name(),
          entity.approvedBy,
          entity.approvedAt,
          entity.rejectionReason);
    }
  }

  public record RejectRequest(String reason) {}

  public record ErrorResponse(String message) {}
}

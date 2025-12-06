package de.freshplan.modules.leads.api.admin;

import de.freshplan.modules.leads.domain.GdprDataRequest;
import de.freshplan.modules.leads.domain.GdprDeletionLog;
import de.freshplan.modules.leads.domain.Lead;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Admin REST Resource für DSGVO-Übersicht.
 *
 * <p>Bietet Admin-Endpoints für das DSGVO-Dashboard:
 *
 * <ul>
 *   <li>Übersicht aller Löschungen
 *   <li>Übersicht aller Datenexport-Anfragen
 *   <li>Statistiken für Compliance-Reporting
 * </ul>
 *
 * @since Sprint 2.1.8 Phase 3
 */
@Path("/api/admin/gdpr")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "MANAGER"})
@SecurityScheme(securitySchemeName = "oidc", type = SecuritySchemeType.OPENIDCONNECT)
@SecurityRequirement(name = "oidc")
@Tag(name = "GDPR Admin", description = "DSGVO-Admin-Übersicht für Manager und Admins")
public class GdprAdminResource {

  // ============================================================================
  // Statistiken
  // ============================================================================

  @GET
  @Path("/stats")
  @Operation(
      summary = "DSGVO-Statistiken",
      description = "Liefert Statistiken für das DSGVO-Dashboard (Löschungen, Anfragen etc.)")
  public GdprStatsResponse getStats() {
    long totalDeletions = GdprDeletionLog.count();
    long totalDataRequests = GdprDataRequest.count();
    long pendingRequests = GdprDataRequest.count("pdfGenerated = false");
    long deletedLeads = Lead.count("gdprDeleted = true");
    long blockedContacts = Lead.count("contactBlocked = true AND gdprDeleted = false");

    return new GdprStatsResponse(
        totalDeletions, totalDataRequests, pendingRequests, deletedLeads, blockedContacts);
  }

  // ============================================================================
  // Löschprotokolle
  // ============================================================================

  @GET
  @Path("/deletions")
  @Operation(
      summary = "Alle DSGVO-Löschungen",
      description = "Listet alle Art. 17 DSGVO-Löschprotokolle auf.")
  public List<GdprDeletionLogDTO> getAllDeletions(
      @QueryParam("from") String from,
      @QueryParam("to") String to,
      @QueryParam("limit") @DefaultValue("100") int limit) {

    List<GdprDeletionLog> logs;
    if (from != null && to != null) {
      LocalDateTime fromDate = LocalDateTime.parse(from + "T00:00:00");
      LocalDateTime toDate = LocalDateTime.parse(to + "T23:59:59");
      logs = GdprDeletionLog.findByDateRange(fromDate, toDate);
    } else {
      logs = GdprDeletionLog.list("ORDER BY deletedAt DESC");
    }

    return logs.stream().limit(limit).map(GdprDeletionLogDTO::fromEntity).toList();
  }

  // ============================================================================
  // Datenexport-Anfragen
  // ============================================================================

  @GET
  @Path("/data-requests")
  @Operation(
      summary = "Alle Datenexport-Anfragen",
      description = "Listet alle Art. 15 Datenauskunfts-Anfragen auf.")
  public List<GdprDataRequestDTO> getAllDataRequests(
      @QueryParam("pending") @DefaultValue("false") boolean pendingOnly,
      @QueryParam("limit") @DefaultValue("100") int limit) {

    List<GdprDataRequest> requests;
    if (pendingOnly) {
      requests = GdprDataRequest.findPendingRequests();
    } else {
      requests = GdprDataRequest.list("ORDER BY requestedAt DESC");
    }

    return requests.stream().limit(limit).map(GdprDataRequestDTO::fromEntity).toList();
  }

  // ============================================================================
  // Gelöschte Leads
  // ============================================================================

  @GET
  @Path("/deleted-leads")
  @Operation(
      summary = "Alle DSGVO-gelöschten Leads",
      description = "Listet alle Leads auf, die gemäß Art. 17 DSGVO anonymisiert wurden.")
  public List<DeletedLeadDTO> getDeletedLeads(@QueryParam("limit") @DefaultValue("100") int limit) {

    List<Lead> leads = Lead.list("gdprDeleted = true ORDER BY gdprDeletedAt DESC");
    return leads.stream().limit(limit).map(DeletedLeadDTO::fromEntity).toList();
  }

  // ============================================================================
  // DTOs
  // ============================================================================

  public record GdprStatsResponse(
      long totalDeletions,
      long totalDataRequests,
      long pendingRequests,
      long deletedLeads,
      long blockedContacts) {}

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

  public record DeletedLeadDTO(
      Long id,
      String companyName,
      LocalDateTime gdprDeletedAt,
      String gdprDeletedBy,
      String gdprDeletionReason) {

    public static DeletedLeadDTO fromEntity(Lead lead) {
      return new DeletedLeadDTO(
          lead.id,
          lead.companyName,
          lead.gdprDeletedAt,
          lead.gdprDeletedBy,
          lead.gdprDeletionReason);
    }
  }
}

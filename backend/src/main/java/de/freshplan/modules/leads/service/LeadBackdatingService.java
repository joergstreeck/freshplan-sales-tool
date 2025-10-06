package de.freshplan.modules.leads.service;

import de.freshplan.modules.leads.api.admin.dto.BackdatingRequest;
import de.freshplan.modules.leads.api.admin.dto.BackdatingResponse;
import de.freshplan.modules.leads.domain.Lead;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;

/**
 * Service for backdating Lead registeredAt timestamp.
 *
 * <p>Allows Admins/Managers to set historical registration dates for Bestandsleads-Migration.
 * Recalculates protection and progress deadlines based on the new registeredAt.
 *
 * <p>Sprint 2.1.6 - User Story 4
 */
@ApplicationScoped
public class LeadBackdatingService {

  @Inject LeadProtectionService protectionService;

  /**
   * Update registeredAt for a Lead and recalculate protection deadlines.
   *
   * @param leadId Lead ID
   * @param request Backdating request with new date and reason
   * @param currentUserId User performing the backdating
   * @return Response with old/new dates and recalculated deadlines
   */
  @Transactional
  public BackdatingResponse updateRegisteredAt(
      Long leadId, BackdatingRequest request, String currentUserId) {

    Log.infof(
        "Backdating lead %d: registeredAt=%s, reason=%s, user=%s",
        leadId, request.registeredAt, request.reason, currentUserId);

    // 1. Validate lead exists
    Lead lead = Lead.findById(leadId);
    if (lead == null) {
      throw new NotFoundException("Lead not found: " + leadId);
    }

    // 2. Validate registeredAt is not in the future
    if (request.registeredAt.isAfter(LocalDateTime.now())) {
      throw new IllegalArgumentException("registeredAt cannot be in the future");
    }

    // 3. Store old values for audit
    LocalDateTime oldRegisteredAt = lead.registeredAt;

    // 4. Update registeredAt + audit fields
    lead.registeredAt = request.registeredAt;
    lead.registeredAtOverrideReason = request.reason;
    lead.registeredAtSetBy = currentUserId;
    lead.registeredAtSetAt = LocalDateTime.now();
    lead.registeredAtSource = "backdated"; // lowercase - matches DB CHECK constraint

    // 5. Recalculate protection deadlines based on new registeredAt
    lead.protectionStartAt = request.registeredAt;

    // Protection remains 6 months (no change to protectionMonths field)
    // protectionEnd = protectionStartAt.plusMonths(protectionMonths)

    // Progress Deadline: registeredAt + 60 days (with Stop-the-Clock adjustments)
    LocalDateTime baseProgressDeadline = request.registeredAt.plusDays(lead.protectionDays60);

    // If clock is stopped, add pause duration
    if (lead.clockStoppedAt != null) {
      long pauseDurationDays =
          java.time.Duration.between(lead.clockStoppedAt, LocalDateTime.now()).toDays();
      lead.progressDeadline = baseProgressDeadline.plusDays(pauseDurationDays);
      Log.infof(
          "Clock stopped - adding %d days to progress deadline (new: %s)",
          pauseDurationDays, lead.progressDeadline);
    } else {
      lead.progressDeadline = baseProgressDeadline;
    }

    // 6. Update lastActivityAt if empty (for Pre-Claim detection)
    if (lead.lastActivityAt == null) {
      lead.lastActivityAt = request.registeredAt;
    }

    // 7. Persist changes
    lead.updatedAt = LocalDateTime.now();
    lead.updatedBy = currentUserId;
    lead.persist();

    // 8. Audit log
    Log.infof(
        "AUDIT: lead_registered_at_backdated - leadId=%d, old=%s, new=%s, reason=%s, user=%s",
        leadId, oldRegisteredAt, request.registeredAt, request.reason, currentUserId);

    // 9. Calculate protection end for response
    LocalDateTime protectionEnd = lead.protectionStartAt.plusMonths(lead.protectionMonths);

    // 10. Return response
    return BackdatingResponse.success(
        leadId, oldRegisteredAt, request.registeredAt, protectionEnd, lead.progressDeadline);
  }
}

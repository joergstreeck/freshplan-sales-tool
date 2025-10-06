package de.freshplan.modules.leads.api.admin.dto;

import java.time.LocalDateTime;

/**
 * Backdating Response für historisches registered_at Update.
 *
 * <p>Sprint 2.1.6 - User Story 4
 */
public class BackdatingResponse {

  public Long leadId;
  public LocalDateTime oldRegisteredAt;
  public LocalDateTime newRegisteredAt;
  public LocalDateTime newProtectionUntil;
  public LocalDateTime newProgressDeadline;
  public String message;

  public static BackdatingResponse success(
      Long leadId,
      LocalDateTime oldRegisteredAt,
      LocalDateTime newRegisteredAt,
      LocalDateTime newProtectionUntil,
      LocalDateTime newProgressDeadline) {
    BackdatingResponse response = new BackdatingResponse();
    response.leadId = leadId;
    response.oldRegisteredAt = oldRegisteredAt;
    response.newRegisteredAt = newRegisteredAt;
    response.newProtectionUntil = newProtectionUntil;
    response.newProgressDeadline = newProgressDeadline;
    response.message = "Registered date updated successfully. Protection deadlines recalculated.";
    return response;
  }
}

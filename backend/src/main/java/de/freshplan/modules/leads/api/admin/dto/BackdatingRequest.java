package de.freshplan.modules.leads.api.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Backdating Request für historisches registered_at Datum.
 *
 * <p>Sprint 2.1.6 - User Story 4
 */
public class BackdatingRequest {

  @NotNull(message = "registeredAt is required") public LocalDateTime registeredAt;

  @NotNull(message = "reason is required") @Size(min = 10, max = 250, message = "Reason must be between 10 and 250 characters")
  public String reason;

  @Size(max = 500, message = "Evidence URL max 500 characters")
  public String evidenceUrl;
}

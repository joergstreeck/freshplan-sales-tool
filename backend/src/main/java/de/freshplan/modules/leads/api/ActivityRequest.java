package de.freshplan.modules.leads.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for adding an activity to a lead. Activities update the lastActivityAt timestamp
 * which is crucial for the protection system.
 *
 * <p>Sprint 2.1.7 - Issue #126: Added ActivityOutcome field for standardized outcome tracking
 */
public class ActivityRequest {

  @NotNull(message = "Activity type is required")
  @Size(min = 1, max = 50, message = "Activity type must be between 1 and 50 characters")
  public String activityType; // CALL, EMAIL, MEETING, NOTE, SAMPLE_SENT, etc.

  @NotNull(message = "Description is required")
  @Size(min = 1, max = 1000, message = "Description must be between 1 and 1000 characters")
  public String description;

  /**
   * Activity outcome (optional) - Sprint 2.1.7 Issue #126
   *
   * <p>Valid values: SUCCESSFUL, UNSUCCESSFUL, NO_ANSWER, CALLBACK_REQUESTED, INFO_SENT,
   * QUALIFIED, DISQUALIFIED
   *
   * <p>Used for tracking activity success and follow-up needs
   */
  @Size(max = 50, message = "Outcome must be max 50 characters")
  public String outcome;
}

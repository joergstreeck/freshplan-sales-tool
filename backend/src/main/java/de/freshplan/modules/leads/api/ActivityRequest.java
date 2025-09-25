package de.freshplan.modules.leads.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for adding an activity to a lead. Activities update the lastActivityAt timestamp
 * which is crucial for the protection system.
 */
public class ActivityRequest {

  @NotNull(message = "Activity type is required")
  @Size(min = 1, max = 50, message = "Activity type must be between 1 and 50 characters")
  public String activityType; // CALL, EMAIL, MEETING, NOTE, SAMPLE_SENT, etc.

  @NotNull(message = "Description is required")
  @Size(min = 1, max = 1000, message = "Description must be between 1 and 1000 characters")
  public String description;
}
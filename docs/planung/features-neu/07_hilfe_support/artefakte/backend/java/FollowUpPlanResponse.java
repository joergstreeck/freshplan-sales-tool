package de.freshplan.help.domain;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FollowUpPlanResponse {
  public List<ActivityRef> createdActivities = new ArrayList<>();
  public static class ActivityRef {
    public UUID id;
    public OffsetDateTime dueDate;
    public String kind;
  }
}

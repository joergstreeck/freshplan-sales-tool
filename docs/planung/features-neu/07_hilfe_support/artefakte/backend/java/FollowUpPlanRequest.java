package de.freshplan.help.domain;

import java.util.List;
import java.util.UUID;

public class FollowUpPlanRequest {
  public UUID accountId;
  public List<String> contactRoles; // CHEF|BUYER|GF|REP
  public List<String> followupOffsets; // ISO-8601 'P3D', 'P7D'
  public String note;
}

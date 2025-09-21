package de.freshplan.help.domain;

import java.util.UUID;

public class RoiQuickCheckRequest {
  public UUID accountId;
  public double hoursSavedPerDay;
  public double hourlyRate;
  public double workingDaysPerMonth = 22;
  public double wasteReductionPerMonth = 0;
}

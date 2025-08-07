package de.freshplan.domain.cost.service.dto;

import java.util.ArrayList;
import java.util.List;

/** Ergebnis einer Budget-Prüfung */
public class BudgetCheckResult {
  private boolean approved = false;
  private String reason;
  private List<String> warnings = new ArrayList<>();

  public void approve() {
    this.approved = true;
    this.reason = null;
  }

  public void deny(String reason) {
    this.approved = false;
    this.reason = reason;
  }

  public void addWarning(String warning) {
    this.warnings.add(warning);
  }

  public boolean canAfford() {
    return approved;
  }

  public String getReason() {
    return reason;
  }

  public List<String> getWarnings() {
    return warnings;
  }

  public boolean hasWarnings() {
    return !warnings.isEmpty();
  }
}

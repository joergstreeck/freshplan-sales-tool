package de.freshplan.cockpit.dto;

import jakarta.validation.constraints.*;

/** DTOs for ROI Calculator (B2B-Convenience-Food) */
public class ROIDTO {

  public static class CalcRequest {
    @Min(1) public int mealsPerDay;
    @Min(1) @Max(31) public int daysPerMonth;
    @PositiveOrZero public double laborMinutesSavedPerMeal;
    @PositiveOrZero public double laborCostPerHour;
    @PositiveOrZero public double foodCostPerMeal;
    @Min(0) @Max(100) public double wasteReductionPct;
    public String channel;   // DIRECT or PARTNER (optional)
    public Double investment; // optional CAPEX for paybackMonths
  }

  public static class CalcResponse {
    public double monthlyLaborSavings;
    public double monthlyWasteSavings;
    public double totalMonthlySavings;
    public Double paybackMonths; // nullable; set if investment present
    public Assumptions assumptions = new Assumptions();
  }

  public static class Assumptions {
    public Double partnerDiscountPct;
    public Double directUpliftPct;
  }
}

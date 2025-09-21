package de.freshplan.cockpit.service;

import de.freshplan.cockpit.dto.ROIDTO;
import de.freshplan.security.ScopeContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ROIService {
  @Inject ScopeContext scope;

  public ROIDTO.CalcResponse calculate(ROIDTO.CalcRequest req) {
    // Labor savings
    double laborHoursSaved = (req.mealsPerDay * req.daysPerMonth * req.laborMinutesSavedPerMeal) / 60.0;
    double monthlyLaborSavings = laborHoursSaved * req.laborCostPerHour;

    // Waste savings
    double monthlyMeals = req.mealsPerDay * req.daysPerMonth;
    double monthlyWasteSavings = monthlyMeals * req.foodCostPerMeal * (req.wasteReductionPct / 100.0);

    // Channel assumptions
    ROIDTO.Assumptions asm = new ROIDTO.Assumptions();
    if ("PARTNER".equalsIgnoreCase(String.valueOf(req.channel))) {
      asm.partnerDiscountPct = 5.0; // example
    } else {
      asm.directUpliftPct = 3.0; // example
    }

    ROIDTO.CalcResponse resp = new ROIDTO.CalcResponse();
    resp.monthlyLaborSavings = round2(monthlyLaborSavings);
    resp.monthlyWasteSavings = round2(monthlyWasteSavings);
    resp.totalMonthlySavings = round2(resp.monthlyLaborSavings + resp.monthlyWasteSavings);

    // Calculate paybackMonths if investment provided
    if (req.investment != null && req.investment > 0 && resp.totalMonthlySavings > 0) {
      resp.paybackMonths = round2(req.investment / resp.totalMonthlySavings);
    } else {
      resp.paybackMonths = null;
    }

    resp.assumptions = asm;
    return resp;
  }

  private double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}

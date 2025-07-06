package de.freshplan.domain.cockpit.service.dto;

import java.util.List;

/**
 * DTO für die aggregierten Dashboard-Daten des Sales Cockpits.
 *
 * <p>Bündelt alle relevanten Informationen für die Startansicht des Sales Cockpits in einer
 * einzigen, optimierten Datenstruktur.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class SalesCockpitDashboard {

  private List<DashboardTask> todaysTasks;
  private List<RiskCustomer> riskCustomers;
  private DashboardStatistics statistics;
  private List<DashboardAlert> alerts;

  public SalesCockpitDashboard() {}

  public List<DashboardTask> getTodaysTasks() {
    return todaysTasks;
  }

  public void setTodaysTasks(List<DashboardTask> todaysTasks) {
    this.todaysTasks = todaysTasks;
  }

  public List<RiskCustomer> getRiskCustomers() {
    return riskCustomers;
  }

  public void setRiskCustomers(List<RiskCustomer> riskCustomers) {
    this.riskCustomers = riskCustomers;
  }

  public DashboardStatistics getStatistics() {
    return statistics;
  }

  public void setStatistics(DashboardStatistics statistics) {
    this.statistics = statistics;
  }

  public List<DashboardAlert> getAlerts() {
    return alerts;
  }

  public void setAlerts(List<DashboardAlert> alerts) {
    this.alerts = alerts;
  }
}

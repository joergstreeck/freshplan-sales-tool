package de.freshplan.domain.cockpit.service.dto;

/**
 * DTO für Dashboard-Statistiken im Sales Cockpit.
 *
 * <p>Sprint 2.1.7.4: Added prospects + conversionRate
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class DashboardStatistics {

  private int totalCustomers;
  private int activeCustomers;
  private int openTasks;
  private int overdueItems;
  private int customersAtRisk;
  // Sprint 2.1.7.4 - Customer Status Architecture
  private int prospects; // NEW: Status = PROSPECT
  private double conversionRate; // NEW: PROSPECT → AKTIV %

  public DashboardStatistics() {}

  public int getTotalCustomers() {
    return totalCustomers;
  }

  public void setTotalCustomers(int totalCustomers) {
    this.totalCustomers = totalCustomers;
  }

  public int getActiveCustomers() {
    return activeCustomers;
  }

  public void setActiveCustomers(int activeCustomers) {
    this.activeCustomers = activeCustomers;
  }

  public int getOpenTasks() {
    return openTasks;
  }

  public void setOpenTasks(int openTasks) {
    this.openTasks = openTasks;
  }

  public int getOverdueItems() {
    return overdueItems;
  }

  public void setOverdueItems(int overdueItems) {
    this.overdueItems = overdueItems;
  }

  public int getCustomersAtRisk() {
    return customersAtRisk;
  }

  public void setCustomersAtRisk(int customersAtRisk) {
    this.customersAtRisk = customersAtRisk;
  }

  // Sprint 2.1.7.4 - New Getters/Setters

  public int getProspects() {
    return prospects;
  }

  public void setProspects(int prospects) {
    this.prospects = prospects;
  }

  public double getConversionRate() {
    return conversionRate;
  }

  public void setConversionRate(double conversionRate) {
    this.conversionRate = conversionRate;
  }
}

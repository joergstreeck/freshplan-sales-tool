package de.freshplan.domain.cockpit.service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO für Risiko-Kunden im Sales Cockpit Dashboard.
 *
 * <p>Ein Risiko-Kunde ist ein Kunde, der länger als einen definierten Zeitraum (z.B. 90 Tage)
 * keinen Kontakt hatte.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class RiskCustomer {

  private UUID id;
  private String customerNumber;
  private String companyName;
  private LocalDateTime lastContactDate;
  private int daysSinceLastContact;
  private String riskReason;
  private RiskLevel riskLevel;
  private String recommendedAction;

  public enum RiskLevel {
    HIGH, // > 120 Tage ohne Kontakt
    MEDIUM, // 90-120 Tage ohne Kontakt
    LOW // 60-90 Tage ohne Kontakt
  }

  public RiskCustomer() {}

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getCustomerNumber() {
    return customerNumber;
  }

  public void setCustomerNumber(String customerNumber) {
    this.customerNumber = customerNumber;
  }

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public LocalDateTime getLastContactDate() {
    return lastContactDate;
  }

  public void setLastContactDate(LocalDateTime lastContactDate) {
    this.lastContactDate = lastContactDate;
  }

  public int getDaysSinceLastContact() {
    return daysSinceLastContact;
  }

  public void setDaysSinceLastContact(int daysSinceLastContact) {
    this.daysSinceLastContact = daysSinceLastContact;
  }

  public String getRiskReason() {
    return riskReason;
  }

  public void setRiskReason(String riskReason) {
    this.riskReason = riskReason;
  }

  public RiskLevel getRiskLevel() {
    return riskLevel;
  }

  public void setRiskLevel(RiskLevel riskLevel) {
    this.riskLevel = riskLevel;
  }

  public String getRecommendedAction() {
    return recommendedAction;
  }

  public void setRecommendedAction(String recommendedAction) {
    this.recommendedAction = recommendedAction;
  }
}

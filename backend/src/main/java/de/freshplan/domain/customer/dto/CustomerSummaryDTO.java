package de.freshplan.domain.customer.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Customer Summary DTO for compact view (Sprint 2.1.7.2 D11)
 *
 * <p>Provides condensed customer information for the compact overview displayed when clicking a
 * customer in the list.
 *
 * <p>This DTO contains the most important customer information needed for daily work (80% use
 * case):
 *
 * <ul>
 *   <li>Company basics (name, status, revenue)
 *   <li>Location summary (count + top location names)
 *   <li>Primary contact information
 *   <li>Risk indicators
 *   <li>Next steps (from activities)
 * </ul>
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class CustomerSummaryDTO {

  /** Company name */
  private String companyName;

  /** Customer status (AKTIV, INAKTIV, GESPERRT) */
  private String status;

  /** Expected annual revenue/volume */
  private BigDecimal expectedAnnualVolume;

  /** Total number of customer locations */
  private Integer locationCount;

  /** Names of top 3-5 locations (for quick overview) */
  private List<String> locationNames;

  /** Full name of primary contact person */
  private String primaryContactName;

  /** Email of primary contact person */
  private String primaryContactEmail;

  /** Risk score (0-100, where 100 = highest risk) */
  private Integer riskScore;

  /** Date of last contact/activity with this customer */
  private Instant lastContactDate;

  /** Next planned steps/activities (from CRM activities) */
  private List<String> nextSteps;

  // Constructors

  public CustomerSummaryDTO() {}

  public CustomerSummaryDTO(
      String companyName,
      String status,
      BigDecimal expectedAnnualVolume,
      Integer locationCount,
      List<String> locationNames,
      String primaryContactName,
      String primaryContactEmail,
      Integer riskScore,
      Instant lastContactDate,
      List<String> nextSteps) {
    this.companyName = companyName;
    this.status = status;
    this.expectedAnnualVolume = expectedAnnualVolume;
    this.locationCount = locationCount;
    this.locationNames = locationNames;
    this.primaryContactName = primaryContactName;
    this.primaryContactEmail = primaryContactEmail;
    this.riskScore = riskScore;
    this.lastContactDate = lastContactDate;
    this.nextSteps = nextSteps;
  }

  // Getters and Setters

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public BigDecimal getExpectedAnnualVolume() {
    return expectedAnnualVolume;
  }

  public void setExpectedAnnualVolume(BigDecimal expectedAnnualVolume) {
    this.expectedAnnualVolume = expectedAnnualVolume;
  }

  public Integer getLocationCount() {
    return locationCount;
  }

  public void setLocationCount(Integer locationCount) {
    this.locationCount = locationCount;
  }

  public List<String> getLocationNames() {
    return locationNames;
  }

  public void setLocationNames(List<String> locationNames) {
    this.locationNames = locationNames;
  }

  public String getPrimaryContactName() {
    return primaryContactName;
  }

  public void setPrimaryContactName(String primaryContactName) {
    this.primaryContactName = primaryContactName;
  }

  public String getPrimaryContactEmail() {
    return primaryContactEmail;
  }

  public void setPrimaryContactEmail(String primaryContactEmail) {
    this.primaryContactEmail = primaryContactEmail;
  }

  public Integer getRiskScore() {
    return riskScore;
  }

  public void setRiskScore(Integer riskScore) {
    this.riskScore = riskScore;
  }

  public Instant getLastContactDate() {
    return lastContactDate;
  }

  public void setLastContactDate(Instant lastContactDate) {
    this.lastContactDate = lastContactDate;
  }

  public List<String> getNextSteps() {
    return nextSteps;
  }

  public void setNextSteps(List<String> nextSteps) {
    this.nextSteps = nextSteps;
  }
}

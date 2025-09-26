package de.freshplan.modules.leads.events;

import de.freshplan.modules.leads.domain.LeadStatus;
import java.time.LocalDateTime;

/**
 * Event für Lead-Status-Änderungen. Wird über PostgreSQL LISTEN/NOTIFY verteilt.
 *
 * <p>Part of FP-236 Security Integration
 */
public class LeadStatusChangeEvent {

  private Long leadId;
  private String companyName;
  private LeadStatus oldStatus;
  private LeadStatus newStatus;
  private String changedBy;
  private LocalDateTime changedAt;
  private String territory;
  private String ownerUserId;
  private String idempotencyKey; // For deduplication

  // Constructors
  public LeadStatusChangeEvent() {
    this.changedAt = LocalDateTime.now();
  }

  public LeadStatusChangeEvent(
      Long leadId,
      String companyName,
      LeadStatus oldStatus,
      LeadStatus newStatus,
      String changedBy,
      String territory,
      String ownerUserId,
      String idempotencyKey,
      LocalDateTime changedAt) {
    this.leadId = leadId;
    this.companyName = companyName;
    this.oldStatus = oldStatus;
    this.newStatus = newStatus;
    this.changedBy = changedBy;
    this.changedAt = changedAt != null ? changedAt : LocalDateTime.now();
    this.territory = territory;
    this.ownerUserId = ownerUserId;
    this.idempotencyKey = idempotencyKey;
  }

  // Getters and Setters
  public Long getLeadId() {
    return leadId;
  }

  public void setLeadId(Long leadId) {
    this.leadId = leadId;
  }

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public LeadStatus getOldStatus() {
    return oldStatus;
  }

  public void setOldStatus(LeadStatus oldStatus) {
    this.oldStatus = oldStatus;
  }

  public LeadStatus getNewStatus() {
    return newStatus;
  }

  public void setNewStatus(LeadStatus newStatus) {
    this.newStatus = newStatus;
  }

  public String getChangedBy() {
    return changedBy;
  }

  public void setChangedBy(String changedBy) {
    this.changedBy = changedBy;
  }

  public LocalDateTime getChangedAt() {
    return changedAt;
  }

  public void setChangedAt(LocalDateTime changedAt) {
    this.changedAt = changedAt;
  }

  public String getTerritory() {
    return territory;
  }

  public void setTerritory(String territory) {
    this.territory = territory;
  }

  public String getOwnerUserId() {
    return ownerUserId;
  }

  public void setOwnerUserId(String ownerUserId) {
    this.ownerUserId = ownerUserId;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public void setIdempotencyKey(String idempotencyKey) {
    this.idempotencyKey = idempotencyKey;
  }

  @Override
  public String toString() {
    return "LeadStatusChangeEvent{"
        + "leadId="
        + leadId
        + ", companyName='"
        + companyName
        + '\''
        + ", oldStatus="
        + oldStatus
        + ", newStatus="
        + newStatus
        + ", changedBy='"
        + changedBy
        + '\''
        + ", changedAt="
        + changedAt
        + ", territory='"
        + territory
        + '\''
        + ", ownerUserId='"
        + ownerUserId
        + '\''
        + '}';
  }
}

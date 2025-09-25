package de.freshplan.modules.leads.api;

import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for Lead entity to avoid lazy loading issues. Sprint 2.1: Safe serialization without
 * lazy-loaded entities.
 */
public class LeadDTO {

  /** Nested DTO for Territory information to match test expectations. */
  public static class TerritoryInfo {
    public String id;
    public String name;
    public String countryCode;
    public String currencyCode;
  }

  public Long id;
  public Long version;
  public String companyName;
  public String contactPerson;
  public String email;
  public String phone;
  public String website;
  public String street;
  public String postalCode;
  public String city;
  public String countryCode;

  // Territory fields as nested object to match test expectations
  public TerritoryInfo territory;

  // Lead specific fields
  public String businessType;
  public String kitchenSize;
  public Integer employeeCount;
  public BigDecimal estimatedVolume;
  public String industry;
  public LeadStatus status;

  // Ownership and collaboration
  public String ownerUserId;
  public Set<String> collaboratorUserIds;

  // Source tracking
  public String source;
  public String sourceCampaign;

  // Protection system fields
  public LocalDateTime lastActivityAt;
  public LocalDateTime reminderSentAt;
  public LocalDateTime gracePeriodStartAt;
  public LocalDateTime expiredAt;

  // Stop-the-clock feature
  public LocalDateTime clockStoppedAt;
  public String stopReason;
  public String stopApprovedBy;

  // Metadata
  public LocalDateTime createdAt;
  public String createdBy;
  public LocalDateTime updatedAt;
  public String updatedBy;

  public static LeadDTO from(Lead lead) {
    LeadDTO dto = new LeadDTO();
    dto.id = lead.id;
    dto.version = lead.version;
    dto.companyName = lead.companyName;
    dto.contactPerson = lead.contactPerson;
    dto.email = lead.email;
    dto.phone = lead.phone;
    dto.website = lead.website;
    dto.street = lead.street;
    dto.postalCode = lead.postalCode;
    dto.city = lead.city;
    dto.countryCode = lead.countryCode;

    // Safely access territory fields (now safe within @Transactional)
    if (lead.territory != null) {
      dto.territory = new TerritoryInfo();
      dto.territory.id = lead.territory.id;
      dto.territory.name = lead.territory.name;
      dto.territory.countryCode = lead.territory.countryCode;
      dto.territory.currencyCode = lead.territory.currencyCode;
    }

    dto.businessType = lead.businessType;
    dto.kitchenSize = lead.kitchenSize;
    dto.employeeCount = lead.employeeCount;
    dto.estimatedVolume = lead.estimatedVolume;
    dto.industry = lead.industry;
    dto.status = lead.status;

    dto.ownerUserId = lead.ownerUserId;
    // Copy the Set to avoid lazy loading issues
    if (lead.collaboratorUserIds != null) {
      dto.collaboratorUserIds = new java.util.HashSet<>(lead.collaboratorUserIds);
    }

    dto.source = lead.source;
    dto.sourceCampaign = lead.sourceCampaign;

    dto.lastActivityAt = lead.lastActivityAt;
    dto.reminderSentAt = lead.reminderSentAt;
    dto.gracePeriodStartAt = lead.gracePeriodStartAt;
    dto.expiredAt = lead.expiredAt;

    dto.clockStoppedAt = lead.clockStoppedAt;
    dto.stopReason = lead.stopReason;
    dto.stopApprovedBy = lead.stopApprovedBy;

    dto.createdAt = lead.createdAt;
    dto.createdBy = lead.createdBy;
    dto.updatedAt = lead.updatedAt;
    dto.updatedBy = lead.updatedBy;

    return dto;
  }
}

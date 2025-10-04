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

  // Progressive Profiling (Sprint 2.1.5)
  public Short stage; // 0=Vormerkung, 1=Registrierung, 2=Qualifizierung

  // Protection system fields
  public LocalDateTime registeredAt; // Sprint 2.1.5: Pre-Claim Status Detection (null = Pre-Claim)
  public LocalDateTime lastActivityAt;
  public LocalDateTime reminderSentAt;
  public LocalDateTime gracePeriodStartAt;
  public LocalDateTime expiredAt;

  // Progress Tracking (Sprint 2.1.5)
  public LocalDateTime progressWarningSentAt;
  public LocalDateTime progressDeadline; // Sprint 2.1.5: Pre-Claim Badge (createdAt + 10 Tage)

  // Calculated Protection Fields (not persisted, calculated from protectionStartAt +
  // protectionMonths)
  public LocalDateTime protectionUntil; // Sprint 2.1.5: Pre-Claim Badge (registeredAt + 6 Monate)

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

    // Progressive Profiling (Sprint 2.1.5)
    dto.stage = lead.stage;

    // Sprint 2.1.5: Pre-Claim Status Detection (registeredAt === null → Pre-Claim)
    dto.registeredAt = lead.registeredAt;
    dto.lastActivityAt = lead.lastActivityAt;
    dto.reminderSentAt = lead.reminderSentAt;
    dto.gracePeriodStartAt = lead.gracePeriodStartAt;
    dto.expiredAt = lead.expiredAt;

    // Progress Tracking (Sprint 2.1.5)
    dto.progressWarningSentAt = lead.progressWarningSentAt;
    dto.progressDeadline = lead.progressDeadline;

    // Calculate protection_until from protectionStartAt + protectionMonths
    if (lead.protectionStartAt != null && lead.protectionMonths != null) {
      dto.protectionUntil = lead.protectionStartAt.plusMonths(lead.protectionMonths);
    }

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

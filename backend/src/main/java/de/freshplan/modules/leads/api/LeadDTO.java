package de.freshplan.modules.leads.api;

import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStage;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.UrgencyLevel;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

  // Progressive Profiling (Sprint 2.1.5, Sprint 2.1.6 - Issue #125 Enum)
  public LeadStage stage; // VORMERKUNG, REGISTRIERUNG, QUALIFIZIERT (serialized as 0, 1, 2)

  // Protection system fields
  public LocalDateTime registeredAt; // Variante B: IMMER gesetzt (Audit Trail)
  public LocalDateTime firstContactDocumentedAt; // Variante B: NULL = Pre-Claim aktiv (10 Tage Frist)
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

  // Stop-the-clock feature (Sprint 2.1.6 Phase 3)
  public LocalDateTime clockStoppedAt;
  public String stopReason;
  public String stopApprovedBy;
  public Long progressPauseTotalSeconds; // Cumulative pause duration

  // Lead Scoring (Sprint 2.1.6 Phase 4 - ADR-006 Phase 2)
  // Note: Spotless may reformat this line - keeping it compact for readability
  public Integer leadScore; // 0-100 (Umsatz 25% + Engagement 25% + Fit 25% + Dringlichkeit 25%)

  // Branch/Chain information (Sprint 2.1.6 Phase 5+)
  public Integer branchCount; // Anzahl Filialen/Standorte
  public Boolean isChain; // Kettenbetrieb ja/nein

  // Pain Scoring System V3 (Sprint 2.1.6 Phase 5+ - V278)
  // OPERATIONAL PAINS (35 Punkte max)
  public Boolean painStaffShortage; // Personalmangel (+10)
  public Boolean painHighCosts; // Hoher Kostendruck (+7)
  public Boolean painFoodWaste; // Food Waste/Überproduktion (+7)
  public Boolean painQualityInconsistency; // Interne Qualitätsinkonsistenz (+6, -4 mit Staff)
  public Boolean painTimePressure; // Zeitdruck/Effizienz (+5)

  // SWITCHING PAINS (21 Punkte max)
  public Boolean painSupplierQuality; // Qualitätsprobleme beim Lieferanten (+10)
  public Boolean painUnreliableDelivery; // Unzuverlässige Lieferzeiten (+8)
  public Boolean painPoorService; // Schlechter Service/Support (+3)

  public String painNotes; // Freitext für Details

  // Urgency Dimension (separate von Pain)
  public UrgencyLevel urgencyLevel; // NORMAL(0), MEDIUM(5), HIGH(10), EMERGENCY(25)
  public Integer multiPainBonus; // Auto-calculated: +10 bei 4+ Pains

  // Relationship Dimension (Sprint 2.1.6 Phase 5+ - V280)
  public String relationshipStatus; // COLD, CONTACTED, ENGAGED_SKEPTICAL, ENGAGED_POSITIVE, TRUSTED, ADVOCATE
  public String decisionMakerAccess; // UNKNOWN, BLOCKED, INDIRECT, DIRECT, IS_DECISION_MAKER
  public String competitorInUse; // Aktueller Wettbewerber (falls bekannt)
  public String internalChampionName; // Name des internen Champions (falls vorhanden)

  // Contacts (Sprint 2.1.6 Phase 5+ - ADR-007 Option C)
  public List<LeadContactDTO> contacts = new ArrayList<>();

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

    dto.businessType = lead.businessType != null ? lead.businessType.name() : null;
    dto.kitchenSize = lead.kitchenSize != null ? lead.kitchenSize.name() : null;
    dto.employeeCount = lead.employeeCount;
    dto.estimatedVolume = lead.estimatedVolume;
    dto.industry = lead.industry;
    dto.status = lead.status;

    dto.ownerUserId = lead.ownerUserId;
    // Copy the Set to avoid lazy loading issues
    if (lead.collaboratorUserIds != null) {
      dto.collaboratorUserIds = new java.util.HashSet<>(lead.collaboratorUserIds);
    }

    dto.source = lead.source != null ? lead.source.name() : null;
    dto.sourceCampaign = lead.sourceCampaign;

    // Progressive Profiling (Sprint 2.1.5)
    dto.stage = lead.stage;

    // Variante B: Pre-Claim Status Detection (firstContactDocumentedAt === null → Pre-Claim)
    dto.registeredAt = lead.registeredAt;
    dto.firstContactDocumentedAt = lead.firstContactDocumentedAt;
    dto.lastActivityAt = lead.lastActivityAt;
    dto.reminderSentAt = lead.reminderSentAt;
    dto.gracePeriodStartAt = lead.gracePeriodStartAt;
    dto.expiredAt = lead.expiredAt;

    // Progress Tracking (Sprint 2.1.5)
    dto.progressWarningSentAt = lead.progressWarningSentAt;
    dto.progressDeadline = lead.progressDeadline;

    // Calculate protection_until using Lead helper method (Single Source of Truth)
    dto.protectionUntil = lead.getProtectionUntil();

    dto.clockStoppedAt = lead.clockStoppedAt;
    dto.stopReason = lead.stopReason;
    dto.stopApprovedBy = lead.stopApprovedBy;
    dto.progressPauseTotalSeconds = lead.progressPauseTotalSeconds;

    // Lead Scoring (Sprint 2.1.6 Phase 4)
    dto.leadScore = lead.leadScore;

    // Branch/Chain information (Sprint 2.1.6 Phase 5+)
    dto.branchCount = lead.branchCount;
    dto.isChain = lead.isChain;

    // Pain Scoring System V3 (Sprint 2.1.6 Phase 5+ - V278)
    dto.painStaffShortage = lead.painStaffShortage;
    dto.painHighCosts = lead.painHighCosts;
    dto.painFoodWaste = lead.painFoodWaste;
    dto.painQualityInconsistency = lead.painQualityInconsistency;
    dto.painTimePressure = lead.painTimePressure;
    dto.painSupplierQuality = lead.painSupplierQuality;
    dto.painUnreliableDelivery = lead.painUnreliableDelivery;
    dto.painPoorService = lead.painPoorService;
    dto.painNotes = lead.painNotes;
    dto.urgencyLevel = lead.urgencyLevel;
    dto.multiPainBonus = lead.multiPainBonus;

    // Relationship Dimension (Sprint 2.1.6 Phase 5+ - V280)
    dto.relationshipStatus = lead.relationshipStatus != null ? lead.relationshipStatus.name() : null;
    dto.decisionMakerAccess = lead.decisionMakerAccess != null ? lead.decisionMakerAccess.name() : null;
    dto.competitorInUse = lead.competitorInUse;
    dto.internalChampionName = lead.internalChampionName;

    dto.createdAt = lead.createdAt;
    dto.createdBy = lead.createdBy;
    dto.updatedAt = lead.updatedAt;
    dto.updatedBy = lead.updatedBy;

    // Contacts (Sprint 2.1.6 Phase 5+ - ADR-007 Option C)
    // IMPORTANT: Always map contacts (even if empty) to avoid null in DTO
    if (lead.contacts != null) {
      dto.contacts =
          lead.contacts.stream().map(LeadDTO::toContactDTO).collect(Collectors.toList());
    }

    return dto;
  }

  /**
   * Converts LeadContact entity to LeadContactDTO.
   *
   * <p>Maps all fields including CRM Intelligence data (warmth_score, data_quality_score,
   * relationship data).
   */
  private static LeadContactDTO toContactDTO(de.freshplan.modules.leads.domain.LeadContact contact) {
    LeadContactDTO dto = new LeadContactDTO();
    dto.setId(contact.getId());
    dto.setLeadId(contact.getLead() != null ? contact.getLead().id : null);

    // Basic Info
    dto.setSalutation(contact.getSalutation());
    dto.setTitle(contact.getTitle());
    dto.setFirstName(contact.getFirstName());
    dto.setLastName(contact.getLastName());
    dto.setPosition(contact.getPosition());
    dto.setDecisionLevel(contact.getDecisionLevel());

    // Contact Info
    dto.setEmail(contact.getEmail());
    dto.setPhone(contact.getPhone());
    dto.setMobile(contact.getMobile());

    // Flags
    dto.setPrimary(contact.isPrimary());
    dto.setActive(contact.isActive());

    // Relationship Data
    dto.setBirthday(contact.getBirthday());
    dto.setHobbies(contact.getHobbies());
    dto.setFamilyStatus(contact.getFamilyStatus());
    dto.setChildrenCount(contact.getChildrenCount());
    dto.setPersonalNotes(contact.getPersonalNotes());

    // Intelligence Data
    dto.setWarmthScore(contact.getWarmthScore());
    dto.setWarmthConfidence(contact.getWarmthConfidence());
    dto.setLastInteractionDate(contact.getLastInteractionDate());
    dto.setInteractionCount(contact.getInteractionCount());

    // Data Quality
    dto.setDataQualityScore(contact.getDataQualityScore());
    dto.setDataQualityRecommendations(contact.getDataQualityRecommendations());

    // Legacy Compatibility
    dto.setIsDecisionMaker(contact.getIsDecisionMaker());
    dto.setIsDeleted(contact.getIsDeleted());

    // Audit Fields
    dto.setCreatedAt(contact.getCreatedAt());
    dto.setUpdatedAt(contact.getUpdatedAt());
    dto.setCreatedBy(contact.getCreatedBy());
    dto.setUpdatedBy(contact.getUpdatedBy());

    // Computed Fields
    dto.setFullName(contact.getFullName());
    dto.setDisplayName(contact.getDisplayName());

    return dto;
  }
}

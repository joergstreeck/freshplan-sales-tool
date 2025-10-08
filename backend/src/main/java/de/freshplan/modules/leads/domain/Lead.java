package de.freshplan.modules.leads.domain;

import de.freshplan.domain.shared.BusinessType;
import de.freshplan.domain.shared.KitchenSize;
import de.freshplan.domain.shared.LeadSource;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.vertx.core.json.JsonObject;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lead entity with user-based protection (NO geographical protection). Sprint 2.1: Leads are
 * available nationwide for all users, protection is based on user ownership.
 */
@Entity
@Table(name = "leads")
public class Lead extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  // Basic lead information
  @NotNull @Size(max = 255)
  @Column(name = "company_name", nullable = false)
  public String companyName;

  @Size(max = 255)
  @Column(name = "company_name_normalized")
  public String companyNameNormalized;

  @Size(max = 255)
  @Column(name = "contact_person")
  public String contactPerson;

  @Email
  @Size(max = 255)
  public String email;

  @Size(max = 320)
  @Column(name = "email_normalized")
  public String emailNormalized;

  @Size(max = 50)
  public String phone;

  @Size(max = 50)
  @Column(name = "phone_e164")
  public String phoneE164;

  @Size(max = 255)
  public String website;

  @Size(max = 255)
  @Column(name = "website_domain")
  public String websiteDomain;

  // Address and territory
  @Size(max = 255)
  public String street;

  @Size(max = 20)
  @Column(name = "postal_code")
  public String postalCode;

  @Size(max = 100)
  public String city;

  @NotNull @Size(max = 2)
  @Column(name = "country_code", nullable = false)
  public String countryCode = "DE";

  @NotNull @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "territory_id", nullable = false)
  public Territory territory;

  // Contacts (Sprint 2.1.6 Phase 5+ - ADR-007 Option C - 100% Parity with Customer)
  @OneToMany(
      mappedBy = "lead",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  public List<LeadContact> contacts = new ArrayList<>();

  // Industry specific
  /**
   * @deprecated Use {@link #businessType} instead. This field will be removed in next migration
   *     (V264). The businessType field is the Single Source of Truth for business classification.
   */
  @Deprecated(since = "2.1.6", forRemoval = true)
  @Size(max = 50)
  public String industry;

  @Enumerated(EnumType.STRING)
  @Column(name = "business_type", length = 50)
  public BusinessType businessType; // Restaurant/Hotel/Kantinen/Catering (Sprint 2.1.6 Phase 5: Enum)

  @Enumerated(EnumType.STRING)
  @Column(name = "kitchen_size", length = 20)
  public KitchenSize kitchenSize; // Klein/Mittel/Groß/Sehr Groß (Sprint 2.1.6 Phase 5: Enum)

  @Column(name = "employee_count")
  public Integer employeeCount;

  @Column(name = "estimated_volume", precision = 12, scale = 2)
  public BigDecimal estimatedVolume;

  // Lead status and ownership
  @NotNull @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  public LeadStatus status = LeadStatus.REGISTERED;

  @Size(max = 50)
  @Column(name = "owner_user_id") // Nullable since Sprint 2.1.6 Phase 3 (Protection Expiry)
  public String ownerUserId;

  @ElementCollection
  @CollectionTable(name = "lead_collaborators", joinColumns = @JoinColumn(name = "lead_id"))
  @Column(name = "user_id")
  public Set<String> collaboratorUserIds = new HashSet<>();

  // State machine timestamps
  // Variante B: registered_at IMMER gesetzt (Audit Trail)
  @Column(name = "registered_at", nullable = false)
  public LocalDateTime registeredAt;

  // Pre-Claim Logic Variante B: NULL = Pre-Claim aktiv (10 Tage Frist)
  @Column(name = "first_contact_documented_at", nullable = true)
  public LocalDateTime firstContactDocumentedAt;

  @Size(max = 250)
  @Column(name = "registered_at_override_reason")
  public String registeredAtOverrideReason;

  @Size(max = 100)
  @Column(name = "registered_at_set_by")
  public String registeredAtSetBy;

  @Column(name = "registered_at_set_at")
  public LocalDateTime registeredAtSetAt;

  @Size(max = 20)
  @Column(name = "registered_at_source")
  public String registeredAtSource = "system";

  @Column(name = "last_activity_at")
  public LocalDateTime lastActivityAt;

  @Column(name = "reminder_sent_at")
  public LocalDateTime reminderSentAt;

  @Column(name = "grace_period_start_at")
  public LocalDateTime gracePeriodStartAt;

  @Column(name = "expired_at")
  public LocalDateTime expiredAt;

  // Stop-the-clock system
  @Column(name = "clock_stopped_at")
  public LocalDateTime clockStoppedAt;

  @Column(name = "stop_reason", columnDefinition = "TEXT")
  public String stopReason;

  @Size(max = 50)
  @Column(name = "stop_approved_by")
  public String stopApprovedBy;

  // Cumulative pause duration (Sprint 2.1.6 - V262)
  @Column(name = "progress_pause_total_seconds", nullable = false)
  public Long progressPauseTotalSeconds = 0L;

  // Protection system (user-based, not territory-based)
  @Column(name = "protection_start_at", nullable = false)
  public LocalDateTime protectionStartAt = LocalDateTime.now();

  @Column(name = "protection_months", nullable = false)
  public Integer protectionMonths = 6;

  @Column(name = "protection_days_60", nullable = false)
  public Integer protectionDays60 = 60;

  @Column(name = "protection_days_10", nullable = false)
  public Integer protectionDays10 = 10;

  // Deduplication flag (Sprint 2.1.4)
  @Column(name = "is_canonical", nullable = false)
  public Boolean isCanonical = true;

  // Progress Tracking (Sprint 2.1.5 - V255)
  @Column(name = "progress_warning_sent_at")
  public LocalDateTime progressWarningSentAt;

  @Column(name = "progress_deadline")
  public LocalDateTime progressDeadline;

  // Progressive Profiling Stage (Sprint 2.1.5 - V255, Sprint 2.1.6 - Issue #125 Enum)
  @Enumerated(EnumType.ORDINAL)
  @Column(name = "stage", nullable = false)
  public LeadStage stage = LeadStage.VORMERKUNG; // 0=Vormerkung, 1=Registrierung, 2=Qualifiziert

  // DSGVO Pseudonymization (Sprint 2.1.6 Phase 3 - V265)
  @Column(name = "pseudonymized_at")
  public LocalDateTime pseudonymizedAt;

  // Lead Scoring (Sprint 2.1.6 Phase 4 - ADR-006 Phase 2 - V269)
  @Column(name = "lead_score")
  // Note: Spotless may reformat - keeping compact for readability
  public Integer leadScore; // 0-100 (Umsatz 25% + Engagement 25% + Fit 25% + Dringlichkeit 25%)

  // Metadata
  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  public LeadSource source; // MESSE/EMPFEHLUNG/TELEFON/WEB_FORMULAR/PARTNER/SONSTIGES (Sprint 2.1.6 Phase 5: Enum)

  @Size(max = 255)
  @Column(name = "source_campaign")
  public String sourceCampaign;

  @Column(name = "created_at", nullable = false)
  public LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at", nullable = false)
  public LocalDateTime updatedAt = LocalDateTime.now();

  @NotNull @Size(max = 50)
  @Column(name = "created_by", nullable = false)
  public String createdBy;

  @Size(max = 50)
  @Column(name = "updated_by")
  public String updatedBy;

  // Follow-up Tracking (Sprint 2.1 - FP-235)
  @Column(name = "last_followup_at")
  public LocalDateTime lastFollowupAt;

  @Column(name = "followup_count", nullable = false)
  public Integer followupCount = 0;

  @Column(name = "t3_followup_sent", nullable = false)
  public Boolean t3FollowupSent = false;

  @Column(name = "t7_followup_sent", nullable = false)
  public Boolean t7FollowupSent = false;

  // Flexible metadata storage (JSONB)
  @Column(columnDefinition = "jsonb")
  @Convert(converter = JsonObjectConverter.class)
  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
  public JsonObject metadata = new JsonObject();

  // Optimistic Locking
  @Version
  @Column(name = "version", nullable = false)
  public long version;

  // Relationships
  @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  public List<LeadActivity> activities = new ArrayList<>();

  // Business methods
  /**
   * Calculate protection end date (Sprint 2.1.6 Phase 4) Single Source of Truth for protectionUntil
   * calculation. Used by LeadDTO, LeadScoringService, LeadBackdatingService, LeadProtectionService.
   */
  public LocalDateTime getProtectionUntil() {
    if (protectionStartAt == null || protectionMonths == null) {
      return null;
    }
    return protectionStartAt.plusMonths(protectionMonths);
  }

  public boolean isProtectionActive() {
    if (clockStoppedAt != null) {
      return true; // Clock is stopped, protection remains active
    }

    LocalDateTime protectionEnd = getProtectionUntil();
    if (protectionEnd == null) {
      return false;
    }
    return LocalDateTime.now().isBefore(protectionEnd);
  }

  public boolean needsReminder() {
    // Pre-Claim leads (registeredAt = null) don't need reminders yet
    if (registeredAt == null) {
      return false;
    }
    if (lastActivityAt == null) {
      return registeredAt.plusDays(protectionDays60).isBefore(LocalDateTime.now());
    }
    return lastActivityAt.plusDays(protectionDays60).isBefore(LocalDateTime.now());
  }

  public boolean isInGracePeriod() {
    return status == LeadStatus.GRACE_PERIOD && gracePeriodStartAt != null;
  }

  public boolean canBeAccessedBy(String userId) {
    return (ownerUserId != null && ownerUserId.equals(userId))
        || collaboratorUserIds.contains(userId);
  }

  public void addCollaborator(String userId) {
    if (!collaboratorUserIds.contains(userId)) {
      collaboratorUserIds.add(userId);
    }
  }

  public void removeCollaborator(String userId) {
    collaboratorUserIds.remove(userId);
  }

  /** Normalize email for deduplication. Converts to lowercase and trims whitespace. */
  public static String normalizeEmail(String email) {
    if (email == null || email.isBlank()) {
      return null;
    }
    return email.toLowerCase().trim();
  }

  // Static finder methods
  public static List<Lead> findByOwner(String userId) {
    return list("ownerUserId", userId);
  }

  public static List<Lead> findByTerritory(String territoryId) {
    return list("territory.id", territoryId);
  }

  public static List<Lead> findByStatus(LeadStatus status) {
    return list("status", status);
  }

  public static Lead findByIdAndOwner(Long id, String userId) {
    return find("id = ?1 and ownerUserId = ?2", id, userId).firstResult();
  }

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    // Variante B: registeredAt wird explizit in LeadResource gesetzt
    if (protectionStartAt == null) {
      protectionStartAt = LocalDateTime.now();
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}

package de.freshplan.modules.leads.domain;

import de.freshplan.domain.shared.BusinessType;
import de.freshplan.domain.shared.DealSize;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "territory_id",
      nullable = true) // Nullable - territory is optional (only for Currency/Tax rules)
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
  public BusinessType
      businessType; // Restaurant/Hotel/Kantinen/Catering (Sprint 2.1.6 Phase 5: Enum)

  @Enumerated(EnumType.STRING)
  @Column(name = "kitchen_size", length = 20)
  public KitchenSize kitchenSize; // Klein/Mittel/Groß/Sehr Groß (Sprint 2.1.6 Phase 5: Enum)

  @Column(name = "employee_count")
  public Integer employeeCount;

  @Column(name = "estimated_volume", precision = 12, scale = 2)
  public BigDecimal estimatedVolume;

  // Branch/Chain information (Sprint 2.1.6 Phase 5+)
  @Column(name = "branch_count")
  public Integer branchCount = 1; // Anzahl Filialen/Standorte (Default: 1 Einzelstandort)

  @Column(name = "is_chain")
  public Boolean isChain = false; // Kettenbetrieb ja/nein

  // Pain Scoring System V3 (Sprint 2.1.6 Phase 5+ - V278)
  // OPERATIONAL PAINS (35 Punkte max) - Strukturelle Betriebsprobleme
  @Column(name = "pain_staff_shortage")
  public Boolean painStaffShortage = false; // +10 Punkte

  @Column(name = "pain_high_costs")
  public Boolean painHighCosts = false; // +7 Punkte

  @Column(name = "pain_food_waste")
  public Boolean painFoodWaste = false; // +7 Punkte

  @Column(name = "pain_quality_inconsistency")
  public Boolean painQualityInconsistency = false; // +6 Punkte (-4 wenn mit Staff kombiniert)

  @Column(name = "pain_time_pressure")
  public Boolean painTimePressure = false; // +5 Punkte

  // SWITCHING PAINS (21 Punkte max) - Probleme mit aktuellem Lieferanten
  @Column(name = "pain_supplier_quality")
  public Boolean painSupplierQuality = false; // +10 Punkte

  @Column(name = "pain_unreliable_delivery")
  public Boolean painUnreliableDelivery = false; // +8 Punkte

  @Column(name = "pain_poor_service")
  public Boolean painPoorService = false; // +3 Punkte

  @Column(name = "pain_notes", columnDefinition = "TEXT")
  public String painNotes;

  // Urgency Dimension (separate von Pain - Zeitdruck vs. Pain)
  @Enumerated(EnumType.STRING)
  @Column(name = "urgency_level", length = 20)
  public UrgencyLevel urgencyLevel =
      UrgencyLevel.NORMAL; // NORMAL(0), MEDIUM(5), HIGH(10), EMERGENCY(25)

  // Multi-Pain Bonus (auto-calculated)
  @Column(name = "multi_pain_bonus")
  public Integer multiPainBonus = 0; // +10 wenn 4+ Pains aktiv

  // Relationship Dimension (Sprint 2.1.6 Phase 5+ - V280)
  @Enumerated(EnumType.STRING)
  @Column(name = "relationship_status", length = 30)
  public RelationshipStatus relationshipStatus =
      RelationshipStatus.COLD; // 0-25 Punkte (40% Gewicht)

  @Enumerated(EnumType.STRING)
  @Column(name = "decision_maker_access", length = 30)
  public DecisionMakerAccess decisionMakerAccess =
      DecisionMakerAccess.UNKNOWN; // -3 bis +25 Punkte (60% Gewicht)

  @Size(max = 100)
  @Column(name = "competitor_in_use")
  public String competitorInUse; // Aktueller Wettbewerber (falls bekannt)

  @Size(max = 100)
  @Column(name = "internal_champion_name")
  public String internalChampionName; // Name des internen Champions (falls vorhanden)

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

  // ================================================================================
  // Sprint 2.1.8: DSGVO Compliance - Art. 7.3, 15, 17
  // ================================================================================

  // Art. 7.3: Einwilligungswiderruf (Consent Revocation)
  @Column(name = "consent_revoked_at")
  public LocalDateTime consentRevokedAt;

  @Size(max = 50)
  @Column(name = "consent_revoked_by")
  public String consentRevokedBy;

  @Column(name = "contact_blocked")
  public Boolean contactBlocked = false;

  // Art. 17: DSGVO-Löschung (Soft-Delete + PII-Anonymisierung)
  @Column(name = "gdpr_deleted")
  public Boolean gdprDeleted = false;

  @Column(name = "gdpr_deleted_at")
  public LocalDateTime gdprDeletedAt;

  @Size(max = 50)
  @Column(name = "gdpr_deleted_by")
  public String gdprDeletedBy;

  @Size(max = 500)
  @Column(name = "gdpr_deletion_reason")
  public String gdprDeletionReason;

  // Lead Scoring (Sprint 2.1.6 Phase 4 - ADR-006 Phase 2 - V269)
  @Column(name = "lead_score")
  // Note: Spotless may reformat - keeping compact for readability
  public Integer leadScore; // 0-100 (Umsatz 25% + Engagement 25% + Fit 25% + Dringlichkeit 25%)

  // ================================================================================
  // Sprint 2.1.6+: Lead Scoring System - 4 Dimensions
  // ================================================================================

  // Revenue Scoring Fields (user input)
  @Column(name = "budget_confirmed")
  public Boolean budgetConfirmed;

  @Column(name = "deal_size")
  @Enumerated(EnumType.STRING)
  public DealSize dealSize;

  // Score Cache (calculated by LeadScoringService for performance)
  @Column(name = "pain_score")
  public Integer painScore;

  @Column(name = "revenue_score")
  public Integer revenueScore;

  @Column(name = "fit_score")
  public Integer fitScore;

  @Column(name = "engagement_score")
  public Integer engagementScore;

  // Note: leadScore (total 0-100) already exists above

  // Metadata
  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  public LeadSource
      source; // MESSE/EMPFEHLUNG/TELEFON/WEB_FORMULAR/PARTNER/SONSTIGES (Sprint 2.1.6 Phase 5:

  // Enum)

  @Size(max = 255)
  @Column(name = "source_campaign")
  public String sourceCampaign;

  @Column(name = "created_at", nullable = false)
  public LocalDateTime createdAt = LocalDateTime.now();

  /**
   * Original Lead-Generierungsdatum (Sprint 2.1.8).
   *
   * <p>Für Import von Altdaten: Das tatsächliche Datum, an dem der Lead ursprünglich generiert
   * wurde. Falls NULL, gilt createdAt als Generierungsdatum.
   *
   * <p>Business Rule: Für Reports/Analysen sollte originalCreatedAt bevorzugt werden, falls
   * vorhanden.
   */
  @Column(name = "original_created_at")
  public LocalDateTime originalCreatedAt;

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

  // ============================================================================
  // PAIN SCORING LOGIC (Sprint 2.1.6 Phase 5+ - V278)
  // ============================================================================

  /**
   * Calculate Pain Score (0-62 Punkte).
   *
   * <p>Berechnung: - Base Pain: 56 Punkte max (8 Felder) - Cap für Staff + Quality: -4 (von 16 auf
   * 12, Doppel-Counting vermeiden) - Multi-Pain Bonus: +10 (wenn 4+ Pains aktiv)
   *
   * <p>Max. Score: 52 (alle Pains + Cap) + 10 (Bonus) = 62
   *
   * @return Pain-Score (0-62)
   */
  public int calculatePainScore() {
    // PMD Complexity Refactoring (Issue #146) - Extracted to helper methods
    PainScoreResult operationalResult = calculateOperationalPains();
    PainScoreResult switchingResult = calculateSwitchingPains();

    int score = operationalResult.score + switchingResult.score;
    int activePains = operationalResult.count + switchingResult.count;

    // ERST Cap anwenden (vor Multi-Pain-Bonus!)
    score += applyStaffQualityCap();

    // DANN Multi-Pain Bonus
    score += applyMultiPainBonus(activePains);

    return score;
  }

  // ============================================================================
  // PMD Complexity Refactoring (Issue #146) - Helper record and methods
  // ============================================================================

  /** Helper record for pain calculation results. */
  private record PainScoreResult(int score, int count) {}

  private PainScoreResult calculateOperationalPains() {
    int score = 0;
    int count = 0;
    if (Boolean.TRUE.equals(painStaffShortage)) {
      score += 10;
      count++;
    }
    if (Boolean.TRUE.equals(painHighCosts)) {
      score += 7;
      count++;
    }
    if (Boolean.TRUE.equals(painFoodWaste)) {
      score += 7;
      count++;
    }
    if (Boolean.TRUE.equals(painQualityInconsistency)) {
      score += 6;
      count++;
    }
    if (Boolean.TRUE.equals(painTimePressure)) {
      score += 5;
      count++;
    }
    return new PainScoreResult(score, count);
  }

  private PainScoreResult calculateSwitchingPains() {
    int score = 0;
    int count = 0;
    if (Boolean.TRUE.equals(painSupplierQuality)) {
      score += 10;
      count++;
    }
    if (Boolean.TRUE.equals(painUnreliableDelivery)) {
      score += 8;
      count++;
    }
    if (Boolean.TRUE.equals(painPoorService)) {
      score += 3;
      count++;
    }
    return new PainScoreResult(score, count);
  }

  private int applyStaffQualityCap() {
    if (Boolean.TRUE.equals(painStaffShortage) && Boolean.TRUE.equals(painQualityInconsistency)) {
      return -4; // Von 16 auf 12 reduzieren (Doppel-Counting vermeiden)
    }
    return 0;
  }

  private int applyMultiPainBonus(int activePains) {
    if (activePains >= 4) {
      multiPainBonus = 10;
      return 10;
    } else {
      multiPainBonus = 0;
      return 0;
    }
  }

  /**
   * Calculate Dringlichkeit (0-25 Punkte).
   *
   * <p>Formel: (Pain/62 × 60%) + (Urgency/25 × 40%)
   *
   * <p>Pain dominiert (60%), aber Urgency entscheidet über Sales Cycle (40%).
   *
   * <p><strong>Beispiele:</strong>
   *
   * <ul>
   *   <li>Hoher Pain + geringe Urgency = Nurturing-Lead (langfristiger Sales Cycle)
   *   <li>Hoher Pain + hohe Urgency = Hot Lead (sofort schließen)
   * </ul>
   *
   * @return Dringlichkeits-Score (0-25)
   */
  public int calculateUrgencyDimension() {
    int painScore = calculatePainScore();
    int urgencyScore = urgencyLevel != null ? urgencyLevel.getPoints() : 0;

    double painPart = (painScore / 62.0) * 15.0; // 60% von 25 = 15
    double urgencyPart = (urgencyScore / 25.0) * 10.0; // 40% von 25 = 10

    return (int) Math.round(painPart + urgencyPart);
  }

  // ============================================================================
  // ENGAGEMENT SCORING LOGIC (Sprint 2.1.6 Phase 5+ - V280)
  // ============================================================================

  /**
   * Calculate Engagement Score (0-25 Punkte).
   *
   * <p>Formel: (Relationship × 40%) + (DecisionMaker × 60%) + Recency Bonus + Touchpoint Bonus
   *
   * <p>Decision Maker Access ist kritischste Faktor (60% Gewicht):
   *
   * <ul>
   *   <li>IS_DECISION_MAKER: ~70-80% Win-Rate
   *   <li>DIRECT: ~50-60% Win-Rate
   *   <li>INDIRECT: ~25-35% Win-Rate
   *   <li>BLOCKED/UNKNOWN: ~10-15% Win-Rate
   * </ul>
   *
   * <p>Relationship Status (40% Gewicht) misst Beziehungsqualität unabhängig vom
   * Entscheider-Zugang.
   *
   * <p>Recency Bonus/Malus (-5 bis +5):
   *
   * <ul>
   *   <li>Letzte meaningful Interaktion <7 Tage: +5
   *   <li>7-30 Tage: +3
   *   <li>30-90 Tage: 0
   *   <li>90-180 Tage: -3
   *   <li>>180 Tage: -5
   * </ul>
   *
   * <p>Touchpoint Bonus (+0 bis +5):
   *
   * <ul>
   *   <li>>10 meaningful Touchpoints: +5
   *   <li>6-10 Touchpoints: +3
   *   <li>≤5 Touchpoints: 0
   * </ul>
   *
   * <p>Meaningful Interaktion: isMeaningfulContact = true ODER description > 100 Zeichen
   *
   * @return Engagement-Score (0-25, gecappt)
   */
  public int calculateEngagementScore() {
    int score = 0;

    // Relationship Status (0-25 Punkte, 40% Gewicht = max 10)
    int relationshipPoints = relationshipStatus != null ? relationshipStatus.getPoints() : 0;
    score += (int) (relationshipPoints * 0.4);

    // Decision Maker Access (-3 bis +25 Punkte, 60% Gewicht = max 15)
    int dmPoints = decisionMakerAccess != null ? decisionMakerAccess.getPoints() : 0;
    score += (int) (dmPoints * 0.6);

    // Recency Bonus/Malus (-5 bis +5)
    score += getInteractionRecencyScore();

    // Touchpoint Bonus (+0 bis +5)
    int touchpoints = getMeaningfulTouchpointCount();
    if (touchpoints > 10) {
      score += 5;
    } else if (touchpoints >= 6) {
      score += 3;
    }

    // Cap auf 0-25
    return Math.max(0, Math.min(25, score));
  }

  /**
   * Get recency score based on last meaningful interaction.
   *
   * <p>Meaningful = isMeaningfulContact = true OR description > 100 chars
   *
   * @return Recency score (-5 bis +5)
   */
  private int getInteractionRecencyScore() {
    LocalDateTime lastMeaningful = getLastMeaningfulInteraction();
    if (lastMeaningful == null) {
      return -5; // Keine meaningful Interaktion = schlechtester Score
    }

    long daysSince =
        java.time.temporal.ChronoUnit.DAYS.between(lastMeaningful, LocalDateTime.now());

    if (daysSince < 7) return +5;
    if (daysSince < 30) return +3;
    if (daysSince < 90) return 0;
    if (daysSince < 180) return -3;
    return -5;
  }

  /**
   * Get last meaningful interaction timestamp.
   *
   * <p>Meaningful = isMeaningfulContact = true OR description > 100 chars
   *
   * @return Timestamp oder null wenn keine meaningful Interaktion
   */
  private LocalDateTime getLastMeaningfulInteraction() {
    if (activities == null || activities.isEmpty()) {
      return null;
    }

    return activities.stream()
        .filter(this::isMeaningfulActivity)
        .map(activity -> activity.activityDate)
        .max(LocalDateTime::compareTo)
        .orElse(null);
  }

  /**
   * Count meaningful touchpoints.
   *
   * <p>Meaningful = isMeaningfulContact = true OR description > 100 chars
   *
   * @return Anzahl meaningful Touchpoints
   */
  private int getMeaningfulTouchpointCount() {
    if (activities == null || activities.isEmpty()) {
      return 0;
    }

    return (int) activities.stream().filter(this::isMeaningfulActivity).count();
  }

  /**
   * Check if activity is meaningful.
   *
   * <p>Heuristik: description > 100 Zeichen ODER isMeaningfulContact = true
   *
   * @param activity LeadActivity
   * @return true wenn meaningful
   */
  private boolean isMeaningfulActivity(LeadActivity activity) {
    // isMeaningfulContact Flag (z.B. MEETING, PHONE_CALL)
    if (activity.isMeaningfulContact) {
      return true;
    }

    // Description > 100 Zeichen (detaillierte Notizen = meaningful)
    if (activity.description != null && activity.description.length() > 100) {
      return true;
    }

    return false;
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

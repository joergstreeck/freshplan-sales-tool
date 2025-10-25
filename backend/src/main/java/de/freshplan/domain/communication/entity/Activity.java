package de.freshplan.domain.communication.entity;

import de.freshplan.modules.leads.domain.ActivityOutcome;
import de.freshplan.modules.leads.domain.ActivityType;
import de.freshplan.modules.leads.domain.JsonObjectConverter;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.vertx.core.json.JsonObject;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Unified Activity Entity
 *
 * <p>Sprint 2.1.7.2 (D8): Unified Communication System
 *
 * <p><b>WHY NOW?</b> Pre-Live Quality Investment
 *
 * <ul>
 *   <li>Current: ~50 DEV-SEED activities (5 sec migration)
 *   <li>Post-Live: Thousands of activities (16h+ migration risk)
 * </ul>
 *
 * <p><b>PROBLEM:</b>
 *
 * <ul>
 *   <li>LeadActivity exists for Leads only
 *   <li>Customer has NO activity tracking
 *   <li>Lead history LOST when converting to Customer
 *   <li>Vertriebsmitarbeiter cannot see complete customer journey
 * </ul>
 *
 * <p><b>SOLUTION:</b> Unified Activity System (CRM Best Practice)
 *
 * <ul>
 *   <li>Polymorphic entity_type + entity_id pattern (Salesforce, HubSpot, Dynamics)
 *   <li>Single activities table for BOTH Lead AND Customer
 *   <li>Unified timeline: CustomerDetailPage shows activities "Als Lead erfasst"
 *   <li>Future-proof for additional entities (Partner, Supplier, etc.)
 * </ul>
 *
 * <p><b>Database:</b> Migration V10039 (creates activities table)
 *
 * <p><b>Type Mismatch Handling:</b>
 *
 * <ul>
 *   <li>Lead.id = BIGINT (BIGSERIAL)
 *   <li>Customer.id = UUID
 *   <li>entity_id = TEXT (stores BOTH as String)
 * </ul>
 *
 * @see EntityType
 * @see ActivityType
 * @see ActivityOutcome
 */
@Entity
@Table(name = "activities")
public class Activity extends PanacheEntityBase {

  @Id
  @GeneratedValue
  @Column(name = "id", updatable = false, nullable = false)
  public UUID id;

  // ============================================================================
  // POLYMORPHIC ASSOCIATION
  // ============================================================================

  /**
   * Polymorphic Entity Type
   *
   * <p>Values: LEAD, CUSTOMER
   *
   * <p>Future: PARTNER, SUPPLIER, etc.
   */
  @NotNull @Enumerated(EnumType.STRING)
  @Column(name = "entity_type", nullable = false, length = 20)
  public EntityType entityType;

  /**
   * Polymorphic Entity ID (as TEXT)
   *
   * <p><b>TYPE MISMATCH:</b>
   *
   * <ul>
   *   <li>Lead.id = BIGINT → stored as TEXT (e.g., "123")
   *   <li>Customer.id = UUID → stored as TEXT (e.g., "550e8400-e29b-...")
   * </ul>
   *
   * <p>Future: Sprint 2.1.8+ may unify all entities to UUID
   */
  @NotNull @Column(name = "entity_id", nullable = false, columnDefinition = "TEXT")
  public String entityId;

  // ============================================================================
  // ACTIVITY CORE FIELDS
  // ============================================================================

  /**
   * Activity Type
   *
   * <p>Enum: QUALIFIED_CALL, MEETING, DEMO, ROI_PRESENTATION, SAMPLE_SENT, NOTE, FOLLOW_UP, EMAIL,
   * CALL, SAMPLE_FEEDBACK, FIRST_CONTACT_DOCUMENTED, EMAIL_RECEIVED, LEAD_ASSIGNED, ORDER,
   * STATUS_CHANGE, CREATED, DELETED, REMINDER_SENT, GRACE_PERIOD_STARTED, EXPIRED, REACTIVATED,
   * CLOCK_STOPPED, CLOCK_RESUMED
   */
  @NotNull @Enumerated(EnumType.STRING)
  @Column(name = "activity_type", nullable = false, length = 50)
  public ActivityType activityType;

  /**
   * Activity Date/Time
   *
   * <p>When did this activity occur?
   */
  @NotNull @Column(name = "activity_date", nullable = false)
  public LocalDateTime activityDate = LocalDateTime.now();

  /**
   * Description (Freetext)
   *
   * <p>Detailed notes about the activity.
   */
  @Column(columnDefinition = "TEXT")
  public String description;

  /**
   * Metadata (JSONB)
   *
   * <p>Flexible data for future extensibility.
   *
   * <p>Example: {"call_duration_seconds": 300, "email_subject": "Angebot Gemüse Q4 2025"}
   */
  @Column(columnDefinition = "jsonb")
  @Convert(converter = JsonObjectConverter.class)
  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
  public JsonObject metadata = new JsonObject();

  // ============================================================================
  // ACTIVITY FLAGS (from LeadActivity V229 + V256)
  // ============================================================================

  /**
   * Is Meaningful Contact?
   *
   * <p>Does this activity count as meaningful customer interaction?
   *
   * <p>Examples: CALL, EMAIL, MEETING → true | NOTE, STATUS_CHANGE → false
   */
  @Column(name = "is_meaningful_contact", nullable = false)
  public boolean isMeaningfulContact = false;

  /**
   * Resets Timer?
   *
   * <p>Does this activity reset the lead protection timer?
   *
   * <p>Example: Meaningful contacts reset 60-day timer.
   */
  @Column(name = "resets_timer", nullable = false)
  public boolean resetsTimer = false;

  /**
   * Counts As Progress?
   *
   * <p>Progress tracking for 60-day activity standard (§3.3 Handelsvertretervertrag)
   *
   * <p>TRUE triggers: leads.progress_deadline = NOW() + 60 days
   *
   * <p>Examples for Progress: QUALIFIED_CALL, MEETING, DEMO, ROI_PRESENTATION, SAMPLE_SENT
   */
  @Column(name = "counts_as_progress", nullable = false)
  public boolean countsAsProgress = false;

  // ============================================================================
  // VERTRIEBSDOKUMENTATION (V256 fields)
  // ============================================================================

  /**
   * Summary (Kurzzusammenfassung)
   *
   * <p>Short summary for dashboard/timeline display (max 500 chars)
   *
   * <p>Example: "Telefonat Küchenchef - Interesse an Bio-Gemüse-Abo"
   */
  @Size(max = 500)
  @Column(name = "summary", length = 500)
  public String summary;

  /**
   * Activity Outcome
   *
   * <p>Enum: SUCCESSFUL, UNSUCCESSFUL, NO_ANSWER, CALLBACK_REQUESTED, INFO_SENT, QUALIFIED,
   * DISQUALIFIED
   *
   * <p>Sprint 2.1.7 Issue #126: ActivityOutcome Enum
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "outcome", length = 50)
  public ActivityOutcome outcome;

  /**
   * Next Action (Geplante Folgeaktion)
   *
   * <p>Example: "Angebot schicken bis Fr", "Follow-up Call in 2 Wochen"
   */
  @Size(max = 200)
  @Column(name = "next_action", length = 200)
  public String nextAction;

  /**
   * Next Action Date
   *
   * <p>Datum für geplante Folgeaktion (for Reminder-System)
   */
  @Column(name = "next_action_date")
  public LocalDate nextActionDate;

  // ============================================================================
  // USER TRACKING
  // ============================================================================

  /**
   * User ID
   *
   * <p>User who logged the activity
   */
  @NotNull @Size(max = 50)
  @Column(name = "user_id", nullable = false, length = 50)
  public String userId;

  /**
   * Performed By (Actual Performer)
   *
   * <p>User ID des tatsächlichen Ausführenden (if different from user_id)
   *
   * <p>Example: Partner A erfasst Aktivität, die Partner B durchgeführt hat
   */
  @Size(max = 50)
  @Column(name = "performed_by", length = 50)
  public String performedBy;

  // ============================================================================
  // AUDIT FIELDS
  // ============================================================================

  /**
   * Created At (Timestamp)
   *
   * <p>When was this activity record created?
   */
  @NotNull @Column(name = "created_at", nullable = false)
  public LocalDateTime createdAt = LocalDateTime.now();

  // ============================================================================
  // LIFECYCLE CALLBACKS
  // ============================================================================

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
    if (activityDate == null) {
      activityDate = LocalDateTime.now();
    }
    // Backfill performed_by from user_id if not set
    if (performedBy == null && userId != null) {
      performedBy = userId;
    }
  }

  // ============================================================================
  // BUSINESS METHODS
  // ============================================================================

  /**
   * Should this activity update lead status?
   *
   * <p>Returns true if activity is meaningful contact or resets timer.
   *
   * @return true if lead status should be updated
   */
  public boolean shouldUpdateLeadStatus() {
    return isMeaningfulContact || resetsTimer;
  }

  /**
   * Factory Method: Create Activity for Lead
   *
   * @param leadId Lead ID (BIGINT as String)
   * @param userId User creating the activity
   * @param type Activity Type
   * @param description Description
   * @return New Activity instance
   */
  public static Activity forLead(
      Long leadId, String userId, ActivityType type, String description) {
    Activity activity = new Activity();
    activity.entityType = EntityType.LEAD;
    activity.entityId = leadId.toString(); // BIGINT → TEXT
    activity.userId = userId;
    activity.activityType = type;
    activity.description = description;
    activity.activityDate = LocalDateTime.now();

    // Auto-set flags from ActivityType enum
    activity.isMeaningfulContact = type.isMeaningfulContact();
    activity.resetsTimer = type.resetsTimer();
    activity.countsAsProgress = type.countsAsProgress();

    return activity;
  }

  /**
   * Factory Method: Create Activity for Customer
   *
   * @param customerId Customer ID (UUID as String)
   * @param userId User creating the activity
   * @param type Activity Type
   * @param description Description
   * @return New Activity instance
   */
  public static Activity forCustomer(
      UUID customerId, String userId, ActivityType type, String description) {
    Activity activity = new Activity();
    activity.entityType = EntityType.CUSTOMER;
    activity.entityId = customerId.toString(); // UUID → TEXT
    activity.userId = userId;
    activity.activityType = type;
    activity.description = description;
    activity.activityDate = LocalDateTime.now();

    // Auto-set flags from ActivityType enum
    activity.isMeaningfulContact = type.isMeaningfulContact();
    activity.resetsTimer = type.resetsTimer();
    activity.countsAsProgress = type.countsAsProgress();

    return activity;
  }
}

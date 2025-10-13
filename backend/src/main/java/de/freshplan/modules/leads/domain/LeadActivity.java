package de.freshplan.modules.leads.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.vertx.core.json.JsonObject;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Activity tracking for leads with meaningful contact detection. Sprint 2.1: Tracks all
 * interactions to determine lead status changes.
 */
@Entity
@Table(name = "lead_activities")
public class LeadActivity extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @NotNull @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lead_id", nullable = false)
  public Lead lead;

  @NotNull @Size(max = 50)
  @Column(name = "user_id", nullable = false)
  public String userId;

  @NotNull @Enumerated(EnumType.STRING)
  @Column(name = "activity_type", nullable = false, length = 50)
  public ActivityType activityType;

  // Alternative field names for compatibility
  @Transient public ActivityType type;

  @Column(name = "activity_date", nullable = false)
  public LocalDateTime activityDate = LocalDateTime.now();

  // Alternative field name for compatibility
  @Transient public LocalDateTime occurredAt;

  @Column(columnDefinition = "TEXT")
  public String description;

  @Column(columnDefinition = "jsonb")
  @Convert(converter = JsonObjectConverter.class)
  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
  public JsonObject metadata = new JsonObject();

  @Column(name = "is_meaningful_contact")
  public boolean isMeaningfulContact = false;

  @Column(name = "resets_timer")
  public boolean resetsTimer = false;

  // Progress Tracking (Sprint 2.1.5 - V256)
  @Column(name = "counts_as_progress", nullable = false)
  public boolean countsAsProgress = false;

  @Size(max = 500)
  @Column(name = "summary")
  public String summary;

  // Sprint 2.1.7 Issue #126: ActivityOutcome Enum
  @Enumerated(EnumType.STRING)
  @Column(name = "outcome", length = 50)
  public ActivityOutcome outcome;

  @Size(max = 200)
  @Column(name = "next_action")
  public String nextAction;

  @Column(name = "next_action_date")
  public java.time.LocalDate nextActionDate;

  @Size(max = 50)
  @Column(name = "performed_by")
  public String performedBy;

  @Column(name = "created_at", nullable = false)
  public LocalDateTime createdAt = LocalDateTime.now();

  // Business methods
  public boolean shouldUpdateLeadStatus() {
    return isMeaningfulContact || resetsTimer;
  }

  public static LeadActivity createActivity(
      Lead lead, String userId, ActivityType type, String description) {
    LeadActivity activity = new LeadActivity();
    activity.lead = lead;
    activity.userId = userId;
    activity.activityType = type;
    activity.description = description;
    activity.activityDate = LocalDateTime.now();

    // Determine if this is meaningful contact
    activity.isMeaningfulContact = type.isMeaningfulContact();
    activity.resetsTimer = type.resetsTimer();

    return activity;
  }

  // TODO: Remove after Q1 2026 - Backwards compatibility for old field names
  // These transient fields and methods maintain compatibility with legacy code
  // that still uses 'type' and 'occurredAt' instead of the new field names
  // 'activityType' and 'activityDate'. All new code should use the new names.
  // Migration plan: 1) Update all references in Q4 2025, 2) Remove in Q1 2026

  // Compatibility getters/setters - DEPRECATED
  @Deprecated(forRemoval = true, since = "Sprint 2.1")
  public ActivityType getType() {
    return activityType;
  }

  @Deprecated(forRemoval = true, since = "Sprint 2.1")
  public void setType(ActivityType type) {
    this.activityType = type;
    this.type = type;
  }

  @Deprecated(forRemoval = true, since = "Sprint 2.1")
  public LocalDateTime getOccurredAt() {
    return activityDate;
  }

  @Deprecated(forRemoval = true, since = "Sprint 2.1")
  public void setOccurredAt(LocalDateTime occurredAt) {
    this.activityDate = occurredAt;
    this.occurredAt = occurredAt;
  }

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    if (activityDate == null) {
      activityDate = LocalDateTime.now();
    }
    // Sync alternative field names
    this.type = this.activityType;
    this.occurredAt = this.activityDate;
  }
}

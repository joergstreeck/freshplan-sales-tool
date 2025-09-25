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
  @Transient
  public ActivityType type;

  @Column(name = "activity_date", nullable = false)
  public LocalDateTime activityDate = LocalDateTime.now();

  // Alternative field name for compatibility
  @Transient
  public LocalDateTime occurredAt;

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

  // Compatibility getters/setters
  public ActivityType getType() {
    return activityType;
  }

  public void setType(ActivityType type) {
    this.activityType = type;
    this.type = type;
  }

  public LocalDateTime getOccurredAt() {
    return activityDate;
  }

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

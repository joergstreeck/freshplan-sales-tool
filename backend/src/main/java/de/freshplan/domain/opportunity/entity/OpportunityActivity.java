package de.freshplan.domain.opportunity.entity;

import de.freshplan.domain.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

/**
 * Opportunity Activity - Aktivitäten zu einer Verkaufschance
 *
 * <p>Dokumentiert alle Aktivitäten und Kommunikation im Zusammenhang mit einer Opportunity.
 * Ermöglicht Nachverfolgung und Audit-Trail für Sales-Aktivitäten.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Entity
@Table(name = "opportunity_activities")
public class OpportunityActivity {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "opportunity_id", nullable = false)
  private Opportunity opportunity;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", nullable = false)
  private User createdBy;

  @Enumerated(EnumType.STRING)
  @Column(name = "activity_type", nullable = false)
  private ActivityType activityType;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "scheduled_date")
  private LocalDateTime scheduledDate;

  @Column(name = "completed")
  private boolean completed = false;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  // Default constructor
  public OpportunityActivity() {}

  // Constructor für neue Activities
  public OpportunityActivity(
      Opportunity opportunity,
      User createdBy,
      ActivityType activityType,
      String title,
      String description) {
    this.opportunity = opportunity;
    this.createdBy = createdBy;
    this.activityType = activityType;
    this.title = title;
    this.description = description;
  }

  // Business Method: Activity als abgeschlossen markieren
  public void complete() {
    this.completed = true;
    this.completedAt = LocalDateTime.now();
  }

  // Activity Types Enum
  public enum ActivityType {
    CALL("Telefonat"),
    EMAIL("E-Mail"),
    MEETING("Termin"),
    NOTE("Notiz"),
    TASK("Aufgabe"),
    PROPOSAL_SENT("Angebot gesendet"),
    STAGE_CHANGED("Status geändert"),
    CALCULATOR_USED("Calculator verwendet"),
    DOCUMENT_SENT("Dokument gesendet"),
    FOLLOW_UP("Nachfassung");

    private final String displayName;

    ActivityType(String displayName) {
      this.displayName = displayName;
    }

    public String getDisplayName() {
      return displayName;
    }
  }

  // Getters und Setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Opportunity getOpportunity() {
    return opportunity;
  }

  public void setOpportunity(Opportunity opportunity) {
    this.opportunity = opportunity;
  }

  public User getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(User createdBy) {
    this.createdBy = createdBy;
  }

  public ActivityType getActivityType() {
    return activityType;
  }

  public void setActivityType(ActivityType activityType) {
    this.activityType = activityType;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDateTime getScheduledDate() {
    return scheduledDate;
  }

  public void setScheduledDate(LocalDateTime scheduledDate) {
    this.scheduledDate = scheduledDate;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
    if (completed && completedAt == null) {
      this.completedAt = LocalDateTime.now();
    }
  }

  public LocalDateTime getCompletedAt() {
    return completedAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}

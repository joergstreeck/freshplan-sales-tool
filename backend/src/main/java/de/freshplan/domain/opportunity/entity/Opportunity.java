package de.freshplan.domain.opportunity.entity;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.user.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Opportunity Entity - Verkaufschance im Sales Pipeline
 *
 * <p>Repräsentiert eine Verkaufschance im Sales-Prozess. Jede Opportunity durchläuft verschiedene
 * Stages von NEW_LEAD bis CLOSED_WON/LOST.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Entity
@Table(name = "opportunities")
public class Opportunity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "stage", nullable = false)
  private OpportunityStage stage;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id")
  private Customer customer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assigned_to")
  private User assignedTo;

  @Column(name = "expected_value", precision = 19, scale = 2)
  private BigDecimal expectedValue;

  @Column(name = "expected_close_date")
  private LocalDate expectedCloseDate;

  @Column(name = "probability")
  private Integer probability;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @OneToMany(mappedBy = "opportunity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<OpportunityActivity> activities = new ArrayList<>();

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "stage_changed_at")
  private LocalDateTime stageChangedAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // Default constructor
  public Opportunity() {}

  // Constructor für neue Opportunities
  public Opportunity(String name, OpportunityStage stage, User assignedTo) {
    this.name = name;
    this.stage = stage;
    this.assignedTo = assignedTo;
    this.stageChangedAt = LocalDateTime.now();
    this.probability = getDefaultProbabilityForStage(stage);
  }

  // Business Logic: Standard-Wahrscheinlichkeit je Stage
  private Integer getDefaultProbabilityForStage(OpportunityStage stage) {
    return switch (stage) {
      case NEW_LEAD -> 10;
      case QUALIFICATION -> 25;
      case NEEDS_ANALYSIS -> 40;
      case PROPOSAL -> 60;
      case NEGOTIATION -> 80;
      case CLOSED_WON -> 100;
      case CLOSED_LOST -> 0;
    };
  }

  // Business Method: Stage ändern mit Timestamp
  public void changeStage(OpportunityStage newStage) {
    // Business Rule: Geschlossene Opportunities können nicht mehr geändert werden
    if (this.stage == OpportunityStage.CLOSED_WON || this.stage == OpportunityStage.CLOSED_LOST) {
      return; // Silently ignore stage changes for closed opportunities
    }

    if (this.stage != newStage) {
      this.stage = newStage;
      this.stageChangedAt = LocalDateTime.now();
      this.probability = getDefaultProbabilityForStage(newStage);
      this.updatedAt = LocalDateTime.now();
    }
  }

  // Business Method: Activity hinzufügen
  public void addActivity(OpportunityActivity activity) {
    activities.add(activity);
    activity.setOpportunity(this);
    this.updatedAt = LocalDateTime.now();
  }

  // Getters und Setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    this.updatedAt = LocalDateTime.now();
  }

  public OpportunityStage getStage() {
    return stage;
  }

  public void setStage(OpportunityStage stage) {
    changeStage(stage);
  }

  public Customer getCustomer() {
    return customer;
  }

  public void setCustomer(Customer customer) {
    this.customer = customer;
    this.updatedAt = LocalDateTime.now();
  }

  public User getAssignedTo() {
    return assignedTo;
  }

  public void setAssignedTo(User assignedTo) {
    this.assignedTo = assignedTo;
    this.updatedAt = LocalDateTime.now();
  }

  public BigDecimal getExpectedValue() {
    return expectedValue;
  }

  public void setExpectedValue(BigDecimal expectedValue) {
    this.expectedValue = expectedValue;
    this.updatedAt = LocalDateTime.now();
  }

  public LocalDate getExpectedCloseDate() {
    return expectedCloseDate;
  }

  public void setExpectedCloseDate(LocalDate expectedCloseDate) {
    this.expectedCloseDate = expectedCloseDate;
    this.updatedAt = LocalDateTime.now();
  }

  public Integer getProbability() {
    return probability;
  }

  public void setProbability(Integer probability) {
    this.probability = probability;
    this.updatedAt = LocalDateTime.now();
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
    this.updatedAt = LocalDateTime.now();
  }

  public List<OpportunityActivity> getActivities() {
    return activities;
  }

  public void setActivities(List<OpportunityActivity> activities) {
    this.activities = activities;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getStageChangedAt() {
    return stageChangedAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}

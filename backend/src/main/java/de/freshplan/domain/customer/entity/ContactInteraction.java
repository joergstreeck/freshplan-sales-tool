package de.freshplan.domain.customer.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

/**
 * ContactInteraction entity for tracking all interactions with contacts. Part of the Data Strategy
 * Intelligence feature to enable warmth scoring, timeline visualization, and predictive analytics.
 */
@Entity
@Table(
    name = "contact_interactions",
    indexes = {
      @Index(name = "idx_interaction_contact", columnList = "contact_id"),
      @Index(name = "idx_interaction_timestamp", columnList = "timestamp"),
      @Index(name = "idx_interaction_type", columnList = "type"),
      @Index(name = "idx_interaction_initiated_by", columnList = "initiated_by")
    })
public class ContactInteraction extends PanacheEntityBase {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contact_id", nullable = false)
  private Contact contact;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 50)
  private InteractionType type;

  @Column(name = "timestamp", nullable = false)
  private LocalDateTime timestamp;

  @Column(name = "sentiment_score")
  private Double sentimentScore; // -1.0 to +1.0

  @Column(name = "engagement_score")
  private Integer engagementScore; // 0-100

  // Auto-captured metadata
  @Column(name = "response_time_minutes")
  private Integer responseTimeMinutes;

  @Column(name = "word_count")
  private Integer wordCount;

  @Column(name = "initiated_by", length = 50)
  private String initiatedBy; // CUSTOMER, SALES, SYSTEM

  // Content and details
  @Column(name = "subject", length = 500)
  private String subject;

  @Column(name = "summary", columnDefinition = "TEXT")
  private String summary;

  @Column(name = "full_content", columnDefinition = "TEXT")
  private String fullContent;

  // Channel information
  @Column(name = "channel", length = 50)
  private String channel; // EMAIL, PHONE, MEETING, CHAT, SOCIAL

  @Column(name = "channel_details", length = 255)
  private String channelDetails; // e.g., "LinkedIn", "WhatsApp", "Zoom"

  // Outcome tracking
  @Column(name = "outcome", length = 50)
  private String outcome; // POSITIVE, NEUTRAL, NEGATIVE, PENDING

  @Column(name = "next_action", length = 500)
  private String nextAction;

  @Column(name = "next_action_date")
  private LocalDateTime nextActionDate;

  // References
  @Column(name = "external_ref_id", length = 100)
  private String externalRefId; // ID from email system, CRM, etc.

  @Column(name = "external_ref_type", length = 50)
  private String externalRefType; // OUTLOOK, GMAIL, SALESFORCE, etc.

  // Metadata
  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "created_by", length = 100)
  private String createdBy;

  // Enums
  public enum InteractionType {
    EMAIL,
    CALL,
    MEETING,
    NOTE,
    TASK,
    EVENT,
    DOCUMENT,
    CHAT,
    SOCIAL_MEDIA,
    VISIT,
    PRESENTATION,
    PROPOSAL,
    CONTRACT,
    SUPPORT_TICKET,
    OTHER
  }

  // Constructors
  public ContactInteraction() {
    // Default constructor for JPA
  }

  // Getters and Setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Contact getContact() {
    return contact;
  }

  public void setContact(Contact contact) {
    this.contact = contact;
  }

  public InteractionType getType() {
    return type;
  }

  public void setType(InteractionType type) {
    this.type = type;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public Double getSentimentScore() {
    return sentimentScore;
  }

  public void setSentimentScore(Double sentimentScore) {
    this.sentimentScore = sentimentScore;
  }

  public Integer getEngagementScore() {
    return engagementScore;
  }

  public void setEngagementScore(Integer engagementScore) {
    this.engagementScore = engagementScore;
  }

  public Integer getResponseTimeMinutes() {
    return responseTimeMinutes;
  }

  public void setResponseTimeMinutes(Integer responseTimeMinutes) {
    this.responseTimeMinutes = responseTimeMinutes;
  }

  public Integer getWordCount() {
    return wordCount;
  }

  public void setWordCount(Integer wordCount) {
    this.wordCount = wordCount;
  }

  public String getInitiatedBy() {
    return initiatedBy;
  }

  public void setInitiatedBy(String initiatedBy) {
    this.initiatedBy = initiatedBy;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getFullContent() {
    return fullContent;
  }

  public void setFullContent(String fullContent) {
    this.fullContent = fullContent;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public String getChannelDetails() {
    return channelDetails;
  }

  public void setChannelDetails(String channelDetails) {
    this.channelDetails = channelDetails;
  }

  public String getOutcome() {
    return outcome;
  }

  public void setOutcome(String outcome) {
    this.outcome = outcome;
  }

  public String getNextAction() {
    return nextAction;
  }

  public void setNextAction(String nextAction) {
    this.nextAction = nextAction;
  }

  public LocalDateTime getNextActionDate() {
    return nextActionDate;
  }

  public void setNextActionDate(LocalDateTime nextActionDate) {
    this.nextActionDate = nextActionDate;
  }

  public String getExternalRefId() {
    return externalRefId;
  }

  public void setExternalRefId(String externalRefId) {
    this.externalRefId = externalRefId;
  }

  public String getExternalRefType() {
    return externalRefType;
  }

  public void setExternalRefType(String externalRefType) {
    this.externalRefType = externalRefType;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  // Business Methods
  public boolean isPositive() {
    return sentimentScore != null && sentimentScore > 0.3;
  }

  public boolean isNegative() {
    return sentimentScore != null && sentimentScore < -0.3;
  }

  public boolean hasFollowUp() {
    return nextAction != null && !nextAction.isBlank();
  }

  public boolean isOverdue() {
    return nextActionDate != null && nextActionDate.isBefore(LocalDateTime.now());
  }

  // Builder pattern for easier construction
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final ContactInteraction interaction = new ContactInteraction();

    public Builder contact(Contact contact) {
      interaction.setContact(contact);
      return this;
    }

    public Builder type(InteractionType type) {
      interaction.setType(type);
      return this;
    }

    public Builder timestamp(LocalDateTime timestamp) {
      interaction.setTimestamp(timestamp);
      return this;
    }

    public Builder sentimentScore(Double sentimentScore) {
      interaction.setSentimentScore(sentimentScore);
      return this;
    }

    public Builder engagementScore(Integer engagementScore) {
      interaction.setEngagementScore(engagementScore);
      return this;
    }

    public Builder initiatedBy(String initiatedBy) {
      interaction.setInitiatedBy(initiatedBy);
      return this;
    }

    public Builder subject(String subject) {
      interaction.setSubject(subject);
      return this;
    }

    public Builder summary(String summary) {
      interaction.setSummary(summary);
      return this;
    }

    public Builder channel(String channel) {
      interaction.setChannel(channel);
      return this;
    }

    public Builder outcome(String outcome) {
      interaction.setOutcome(outcome);
      return this;
    }

    public Builder createdBy(String createdBy) {
      interaction.setCreatedBy(createdBy);
      return this;
    }

    public ContactInteraction build() {
      if (interaction.getTimestamp() == null) {
        interaction.setTimestamp(LocalDateTime.now());
      }
      return interaction;
    }
  }
}

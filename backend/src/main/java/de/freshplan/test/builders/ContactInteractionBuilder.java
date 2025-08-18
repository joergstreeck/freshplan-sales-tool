package de.freshplan.test.builders;

import de.freshplan.domain.customer.entity.ContactInteraction;
import de.freshplan.domain.customer.entity.ContactInteraction.InteractionType;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.ContactInteractionRepository;
import de.freshplan.test.utils.TestDataUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

/**
 * Builder for creating test ContactInteraction entities. Provides fluent API for setting
 * interaction properties and tracking metrics.
 *
 * <p>This builder ensures all test interactions are properly marked and can be identified for
 * cleanup.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class ContactInteractionBuilder {

  @Inject ContactInteractionRepository repository;

  // Required fields
  private CustomerContact contact = null;
  private InteractionType type = InteractionType.EMAIL;
  private LocalDateTime timestamp = LocalDateTime.now();

  // Optional fields
  private Double sentimentScore = 0.5; // Neutral by default
  private Integer engagementScore = 50;
  private String initiatedBy = "SALES";
  private String subject = "Test Interaction";
  private String summary = null;
  private String fullContent = null;
  private String channel = "EMAIL";
  private String channelDetails = null;
  private String outcome = "NEUTRAL";
  private String nextAction = null;
  private LocalDateTime nextActionDate = null;
  private Integer responseTimeMinutes = null;
  private Integer wordCount = null;
  private String externalRefId = null;
  private String externalRefType = null;

  // Test marker
  private final String testMarker = "[TEST]";

  /**
   * Resets the builder to default values for creating a new interaction.
   *
   * @return this builder instance for chaining
   */
  public ContactInteractionBuilder reset() {
    this.contact = null;
    this.type = InteractionType.EMAIL;
    this.timestamp = LocalDateTime.now();
    this.sentimentScore = 0.5;
    this.engagementScore = 50;
    this.initiatedBy = "SALES";
    this.subject = "Test Interaction";
    this.summary = null;
    this.fullContent = null;
    this.channel = "EMAIL";
    this.channelDetails = null;
    this.outcome = "NEUTRAL";
    this.nextAction = null;
    this.nextActionDate = null;
    this.responseTimeMinutes = null;
    this.wordCount = null;
    this.externalRefId = null;
    this.externalRefType = null;
    return this;
  }

  // Contact association (REQUIRED)
  public ContactInteractionBuilder forContact(CustomerContact contact) {
    this.contact = contact;
    return this;
  }

  // Type and timing
  public ContactInteractionBuilder ofType(InteractionType type) {
    this.type = type;
    // Set default channel based on type
    this.channel =
        switch (type) {
          case EMAIL -> "EMAIL";
          case CALL -> "PHONE";
          case MEETING -> "MEETING";
          case CHAT -> "CHAT";
          case SOCIAL_MEDIA -> "SOCIAL";
          default -> "OTHER";
        };
    return this;
  }

  public ContactInteractionBuilder at(LocalDateTime timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  public ContactInteractionBuilder daysAgo(int days) {
    this.timestamp = LocalDateTime.now().minusDays(days);
    return this;
  }

  public ContactInteractionBuilder hoursAgo(int hours) {
    this.timestamp = LocalDateTime.now().minusHours(hours);
    return this;
  }

  public ContactInteractionBuilder minutesAgo(int minutes) {
    this.timestamp = LocalDateTime.now().minusMinutes(minutes);
    return this;
  }

  // Sentiment and engagement
  public ContactInteractionBuilder withSentiment(double score) {
    this.sentimentScore = Math.max(-1.0, Math.min(1.0, score));
    return this;
  }

  public ContactInteractionBuilder positive() {
    this.sentimentScore = 0.8;
    this.outcome = "POSITIVE";
    return this;
  }

  public ContactInteractionBuilder negative() {
    this.sentimentScore = -0.8;
    this.outcome = "NEGATIVE";
    return this;
  }

  public ContactInteractionBuilder neutral() {
    this.sentimentScore = 0.0;
    this.outcome = "NEUTRAL";
    return this;
  }

  public ContactInteractionBuilder withEngagement(int score) {
    this.engagementScore = Math.max(0, Math.min(100, score));
    return this;
  }

  // Content
  public ContactInteractionBuilder withSubject(String subject) {
    this.subject = subject;
    return this;
  }

  public ContactInteractionBuilder withSummary(String summary) {
    this.summary = summary;
    return this;
  }

  public ContactInteractionBuilder withFullContent(String content) {
    this.fullContent = content;
    if (content != null) {
      this.wordCount = content.split("\\s+").length;
    }
    return this;
  }

  // Metadata
  public ContactInteractionBuilder initiatedBy(String initiator) {
    this.initiatedBy = initiator;
    return this;
  }

  public ContactInteractionBuilder initiatedByCustomer() {
    this.initiatedBy = "CUSTOMER";
    return this;
  }

  public ContactInteractionBuilder initiatedBySales() {
    this.initiatedBy = "SALES";
    return this;
  }

  public ContactInteractionBuilder initiatedBySystem() {
    this.initiatedBy = "SYSTEM";
    return this;
  }

  public ContactInteractionBuilder withChannel(String channel) {
    this.channel = channel;
    return this;
  }

  public ContactInteractionBuilder withChannelDetails(String details) {
    this.channelDetails = details;
    return this;
  }

  public ContactInteractionBuilder withOutcome(String outcome) {
    this.outcome = outcome;
    return this;
  }

  public ContactInteractionBuilder withNextAction(String action, LocalDateTime date) {
    this.nextAction = action;
    this.nextActionDate = date;
    return this;
  }

  public ContactInteractionBuilder withResponseTime(int minutes) {
    this.responseTimeMinutes = minutes;
    return this;
  }

  public ContactInteractionBuilder withExternalRef(String id, String type) {
    this.externalRefId = id;
    this.externalRefType = type;
    return this;
  }

  // Predefined scenarios
  public ContactInteractionBuilder asEmail() {
    this.type = InteractionType.EMAIL;
    this.channel = "EMAIL";
    this.subject = testMarker + " Test Email " + TestDataUtils.uniqueId();
    this.summary = "Email interaction for testing";
    this.fullContent =
        "This is a test email content with multiple paragraphs.\n\n"
            + "It contains important information about the customer interaction.\n\n"
            + "Best regards,\nSales Team";
    this.wordCount = 20;
    return this;
  }

  public ContactInteractionBuilder asPhoneCall() {
    this.type = InteractionType.CALL;
    this.channel = "PHONE";
    this.subject = testMarker + " Phone Call " + TestDataUtils.uniqueId();
    this.summary = "Phone call with customer";
    this.responseTimeMinutes = 15;
    return this;
  }

  public ContactInteractionBuilder asMeeting() {
    this.type = InteractionType.MEETING;
    this.channel = "MEETING";
    this.channelDetails = "Zoom";
    this.subject = testMarker + " Customer Meeting " + TestDataUtils.uniqueId();
    this.summary = "Quarterly business review meeting";
    this.engagementScore = 85;
    return this;
  }

  public ContactInteractionBuilder asFollowUp() {
    this.type = InteractionType.TASK;
    this.channel = "OTHER";
    this.subject = testMarker + " Follow-up Task " + TestDataUtils.uniqueId();
    this.summary = "Follow up on previous discussion";
    this.nextAction = "Send proposal";
    this.nextActionDate = LocalDateTime.now().plusDays(3);
    return this;
  }

  public ContactInteractionBuilder asProposal() {
    this.type = InteractionType.PROPOSAL;
    this.channel = "EMAIL";
    this.subject = testMarker + " Proposal Sent " + TestDataUtils.uniqueId();
    this.summary = "Annual contract proposal sent";
    this.sentimentScore = 0.7;
    this.engagementScore = 90;
    this.outcome = "PENDING";
    return this;
  }

  /**
   * Builds a ContactInteraction entity WITHOUT persisting to database. Use this for unit tests or
   * when you need an entity without DB interaction.
   *
   * @return a new ContactInteraction entity with test data markers
   */
  public ContactInteraction build() {
    if (contact == null) {
      throw new IllegalStateException(
          "Contact is required for ContactInteractionBuilder. Use forContact() first.");
    }

    ContactInteraction interaction = new ContactInteraction();

    // Set required fields
    interaction.setContact(contact);
    interaction.setType(type);
    interaction.setTimestamp(timestamp);

    // Set optional fields
    interaction.setSentimentScore(sentimentScore);
    interaction.setEngagementScore(engagementScore);
    interaction.setInitiatedBy(initiatedBy);

    // Set content with test marker
    if (!subject.startsWith(testMarker)) {
      subject = testMarker + " " + subject;
    }
    interaction.setSubject(subject);
    interaction.setSummary(
        summary != null ? summary : "Test interaction for " + contact.getFirstName());
    interaction.setFullContent(fullContent);

    // Set channel info
    interaction.setChannel(channel);
    interaction.setChannelDetails(channelDetails);

    // Set outcome and next actions
    interaction.setOutcome(outcome);
    interaction.setNextAction(nextAction);
    interaction.setNextActionDate(nextActionDate);

    // Set metrics
    interaction.setResponseTimeMinutes(responseTimeMinutes);
    interaction.setWordCount(wordCount);

    // Set external references
    interaction.setExternalRefId(externalRefId);
    interaction.setExternalRefType(externalRefType);

    // Set audit fields
    interaction.setCreatedBy("test-system");

    return interaction;
  }

  /**
   * Builds and persists a ContactInteraction entity to the database. Use this for integration tests
   * that need DB interaction.
   *
   * @return the persisted ContactInteraction entity
   */
  @Transactional
  public ContactInteraction persist() {
    ContactInteraction interaction = build();
    repository.persist(interaction);
    repository.flush(); // Force immediate constraint validation
    return interaction;
  }
}

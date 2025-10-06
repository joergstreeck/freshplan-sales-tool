package de.freshplan.test.builders;

import de.freshplan.domain.customer.entity.ContactInteraction;
import de.freshplan.domain.customer.entity.ContactInteraction.InteractionType;
import de.freshplan.domain.customer.entity.CustomerContact;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Test data factory for ContactInteraction entities. Provides builder pattern for creating test
 * interactions without CDI.
 *
 * @author Claude
 * @since Gemini Code Review Fix - ContactInteractionTestDataFactory
 */
public class ContactInteractionTestDataFactory {

  /** Create a new builder instance. */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    // Default values
    private UUID id;
    private CustomerContact contact;
    private InteractionType type = InteractionType.EMAIL;
    private LocalDateTime timestamp = LocalDateTime.now();
    private Double sentimentScore = 0.0;
    private Integer engagementScore = 50;
    private Integer responseTimeMinutes;
    private Integer wordCount;
    private String initiatedBy = "SALES";
    private String subject;
    private String summary = "Test interaction";
    private String fullContent = "This is a test interaction content";
    private String channel = "EMAIL";
    private String channelDetails;
    private String outcome = "NEUTRAL";
    private String nextAction;
    private LocalDateTime nextActionDate;

    /** Builder constructor with default values */
    public Builder() {
      // Auto-generate ID
      this.id = UUID.randomUUID();
    }

    /**
     * Set the contact for this interaction.
     *
     * @param contact The customer contact entity
     * @return This builder
     */
    public Builder withContact(CustomerContact contact) {
      this.contact = contact;
      return this;
    }

    /**
     * Set the interaction type.
     *
     * @param type Interaction type
     * @return This builder
     */
    public Builder withType(InteractionType type) {
      this.type = type;
      return this;
    }

    /**
     * Set the timestamp.
     *
     * @param timestamp Interaction timestamp
     * @return This builder
     */
    public Builder withTimestamp(LocalDateTime timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    /**
     * Set the sentiment score.
     *
     * @param sentimentScore Sentiment score (-1.0 to +1.0)
     * @return This builder
     */
    public Builder withSentimentScore(Double sentimentScore) {
      this.sentimentScore = sentimentScore;
      return this;
    }

    /**
     * Set the engagement score.
     *
     * @param engagementScore Engagement score (0-100)
     * @return This builder
     */
    public Builder withEngagementScore(Integer engagementScore) {
      this.engagementScore = engagementScore;
      return this;
    }

    /**
     * Set the response time in minutes.
     *
     * @param responseTimeMinutes Response time
     * @return This builder
     */
    public Builder withResponseTimeMinutes(Integer responseTimeMinutes) {
      this.responseTimeMinutes = responseTimeMinutes;
      return this;
    }

    /**
     * Set the word count.
     *
     * @param wordCount Word count
     * @return This builder
     */
    public Builder withWordCount(Integer wordCount) {
      this.wordCount = wordCount;
      return this;
    }

    /**
     * Set who initiated the interaction.
     *
     * @param initiatedBy Who initiated (CUSTOMER, SALES, SYSTEM)
     * @return This builder
     */
    public Builder withInitiatedBy(String initiatedBy) {
      this.initiatedBy = initiatedBy;
      return this;
    }

    /**
     * Set the subject.
     *
     * @param subject Interaction subject
     * @return This builder
     */
    public Builder withSubject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Set the summary.
     *
     * @param summary Interaction summary
     * @return This builder
     */
    public Builder withSummary(String summary) {
      this.summary = summary;
      return this;
    }

    /**
     * Set the full content.
     *
     * @param fullContent Full content text
     * @return This builder
     */
    public Builder withFullContent(String fullContent) {
      this.fullContent = fullContent;
      return this;
    }

    /**
     * Set the communication channel.
     *
     * @param channel Channel (EMAIL, PHONE, MEETING, etc.)
     * @return This builder
     */
    public Builder withChannel(String channel) {
      this.channel = channel;
      return this;
    }

    /**
     * Set the channel details.
     *
     * @param channelDetails Specific channel details
     * @return This builder
     */
    public Builder withChannelDetails(String channelDetails) {
      this.channelDetails = channelDetails;
      return this;
    }

    /**
     * Set the outcome.
     *
     * @param outcome Outcome (POSITIVE, NEUTRAL, NEGATIVE, PENDING)
     * @return This builder
     */
    public Builder withOutcome(String outcome) {
      this.outcome = outcome;
      return this;
    }

    /**
     * Set the next action.
     *
     * @param nextAction Next action description
     * @return This builder
     */
    public Builder withNextAction(String nextAction) {
      this.nextAction = nextAction;
      return this;
    }

    /**
     * Set the next action date.
     *
     * @param nextActionDate Next action date
     * @return This builder
     */
    public Builder withNextActionDate(LocalDateTime nextActionDate) {
      this.nextActionDate = nextActionDate;
      return this;
    }

    /**
     * Build the ContactInteraction entity.
     *
     * @return Configured ContactInteraction
     */
    public ContactInteraction build() {
      ContactInteraction interaction = new ContactInteraction();
      interaction.setId(id);
      interaction.setContact(contact);
      interaction.setType(type);
      interaction.setTimestamp(timestamp);
      interaction.setSentimentScore(sentimentScore);
      interaction.setEngagementScore(engagementScore);
      interaction.setResponseTimeMinutes(responseTimeMinutes);
      interaction.setWordCount(wordCount);
      interaction.setInitiatedBy(initiatedBy);
      interaction.setSubject(subject);
      interaction.setSummary(summary);
      interaction.setFullContent(fullContent);
      interaction.setChannel(channel);
      interaction.setChannelDetails(channelDetails);
      interaction.setOutcome(outcome);
      interaction.setNextAction(nextAction);
      interaction.setNextActionDate(nextActionDate);
      return interaction;
    }
  }
}

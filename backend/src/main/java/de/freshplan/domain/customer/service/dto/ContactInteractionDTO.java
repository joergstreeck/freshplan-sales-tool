package de.freshplan.domain.customer.service.dto;

import de.freshplan.domain.customer.entity.ContactInteraction.InteractionType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for ContactInteraction entity
 */
public class ContactInteractionDTO {
    
    private UUID id;
    
    @NotNull
    private UUID contactId;
    
    @NotNull
    private InteractionType type;
    
    @NotNull
    private LocalDateTime timestamp;
    
    private Double sentimentScore; // -1.0 to +1.0
    
    private Integer engagementScore; // 0-100
    
    private Integer responseTimeMinutes;
    
    private Integer wordCount;
    
    private String initiatedBy; // CUSTOMER, SALES, SYSTEM
    
    private String subject;
    
    private String summary;
    
    private String fullContent;
    
    private String channel; // EMAIL, PHONE, MEETING, CHAT, SOCIAL
    
    private String channelDetails;
    
    private String outcome; // POSITIVE, NEUTRAL, NEGATIVE, PENDING
    
    private String nextAction;
    
    private LocalDateTime nextActionDate;
    
    private String externalRefId;
    
    private String externalRefType;
    
    private LocalDateTime createdAt;
    
    private String createdBy;
    
    // Constructors
    public ContactInteractionDTO() {}
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getContactId() {
        return contactId;
    }

    public void setContactId(UUID contactId) {
        this.contactId = contactId;
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
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final ContactInteractionDTO dto = new ContactInteractionDTO();
        
        public Builder id(UUID id) {
            dto.setId(id);
            return this;
        }
        
        public Builder contactId(UUID contactId) {
            dto.setContactId(contactId);
            return this;
        }
        
        public Builder type(InteractionType type) {
            dto.setType(type);
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            dto.setTimestamp(timestamp);
            return this;
        }
        
        public Builder sentimentScore(Double sentimentScore) {
            dto.setSentimentScore(sentimentScore);
            return this;
        }
        
        public Builder engagementScore(Integer engagementScore) {
            dto.setEngagementScore(engagementScore);
            return this;
        }
        
        public Builder initiatedBy(String initiatedBy) {
            dto.setInitiatedBy(initiatedBy);
            return this;
        }
        
        public Builder subject(String subject) {
            dto.setSubject(subject);
            return this;
        }
        
        public Builder summary(String summary) {
            dto.setSummary(summary);
            return this;
        }
        
        public Builder fullContent(String fullContent) {
            dto.setFullContent(fullContent);
            return this;
        }
        
        public Builder channel(String channel) {
            dto.setChannel(channel);
            return this;
        }
        
        public Builder outcome(String outcome) {
            dto.setOutcome(outcome);
            return this;
        }
        
        public Builder nextAction(String nextAction) {
            dto.setNextAction(nextAction);
            return this;
        }
        
        public Builder nextActionDate(LocalDateTime nextActionDate) {
            dto.setNextActionDate(nextActionDate);
            return this;
        }
        
        public Builder createdBy(String createdBy) {
            dto.setCreatedBy(createdBy);
            return this;
        }
        
        public ContactInteractionDTO build() {
            if (dto.getTimestamp() == null) {
                dto.setTimestamp(LocalDateTime.now());
            }
            return dto;
        }
    }
}
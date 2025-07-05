package de.freshplan.domain.customer.service.dto.timeline;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for creating a communication event.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class CreateCommunicationRequest {
    
    @NotBlank(message = "Communication channel is required")
    @Size(max = 30)
    private String channel; // email, phone, teams, meeting, etc.
    
    @NotBlank(message = "Communication direction is required")
    @Pattern(regexp = "inbound|outbound", message = "Direction must be 'inbound' or 'outbound'")
    private String direction;
    
    @NotBlank(message = "Description is required")
    @Size(max = 5000)
    private String description;
    
    @NotBlank(message = "Performed by is required")
    @Size(max = 100)
    private String performedBy;
    
    private Integer duration; // in minutes
    
    private UUID relatedContactId;
    
    private Boolean requiresFollowUp;
    private LocalDateTime followUpDate;
    
    @Size(max = 1000)
    private String followUpNotes;
    
    // Constructors
    public CreateCommunicationRequest() {}
    
    // Builder pattern for convenience
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final CreateCommunicationRequest request = new CreateCommunicationRequest();
        
        public Builder channel(String channel) {
            request.channel = channel;
            return this;
        }
        
        public Builder direction(String direction) {
            request.direction = direction;
            return this;
        }
        
        public Builder description(String description) {
            request.description = description;
            return this;
        }
        
        public Builder performedBy(String performedBy) {
            request.performedBy = performedBy;
            return this;
        }
        
        public Builder duration(Integer duration) {
            request.duration = duration;
            return this;
        }
        
        public Builder relatedContactId(UUID contactId) {
            request.relatedContactId = contactId;
            return this;
        }
        
        public Builder requiresFollowUp(LocalDateTime followUpDate, String notes) {
            request.requiresFollowUp = true;
            request.followUpDate = followUpDate;
            request.followUpNotes = notes;
            return this;
        }
        
        public CreateCommunicationRequest build() {
            return request;
        }
    }
    
    // Getters and Setters
    public String getChannel() {
        return channel;
    }
    
    public void setChannel(String channel) {
        this.channel = channel;
    }
    
    public String getDirection() {
        return direction;
    }
    
    public void setDirection(String direction) {
        this.direction = direction;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getPerformedBy() {
        return performedBy;
    }
    
    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public UUID getRelatedContactId() {
        return relatedContactId;
    }
    
    public void setRelatedContactId(UUID relatedContactId) {
        this.relatedContactId = relatedContactId;
    }
    
    public Boolean getRequiresFollowUp() {
        return requiresFollowUp;
    }
    
    public void setRequiresFollowUp(Boolean requiresFollowUp) {
        this.requiresFollowUp = requiresFollowUp;
    }
    
    public LocalDateTime getFollowUpDate() {
        return followUpDate;
    }
    
    public void setFollowUpDate(LocalDateTime followUpDate) {
        this.followUpDate = followUpDate;
    }
    
    public String getFollowUpNotes() {
        return followUpNotes;
    }
    
    public void setFollowUpNotes(String followUpNotes) {
        this.followUpNotes = followUpNotes;
    }
}
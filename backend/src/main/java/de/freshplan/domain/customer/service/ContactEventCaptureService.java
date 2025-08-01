package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.entity.ContactInteraction.InteractionType;
import de.freshplan.domain.customer.service.dto.ContactInteractionDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for automatically capturing user actions as contact interactions.
 * Part of the Data Strategy Intelligence feature.
 */
@ApplicationScoped
public class ContactEventCaptureService {
    
    private static final Logger LOG = Logger.getLogger(ContactEventCaptureService.class);
    
    @Inject
    ContactInteractionService interactionService;
    
    @Inject
    Event<ContactInteractionCaptured> interactionCapturedEvent;
    
    /**
     * Capture when a contact is viewed
     */
    public void captureContactView(UUID contactId, String userId) {
        LOG.debugf("Capturing contact view for %s by user %s", contactId, userId);
        
        // We don't create an interaction for every view, but track it for analytics
        // This could be aggregated hourly/daily
        interactionCapturedEvent.fire(new ContactInteractionCaptured(
            contactId, 
            "VIEW", 
            userId,
            LocalDateTime.now()
        ));
    }
    
    /**
     * Capture when contact details are updated
     */
    public void captureContactUpdate(UUID contactId, String userId, String fieldUpdated) {
        LOG.infof("Capturing contact update for %s by user %s - field: %s", 
                 contactId, userId, fieldUpdated);
        
        ContactInteractionDTO interaction = ContactInteractionDTO.builder()
                .contactId(contactId)
                .type(InteractionType.NOTE)
                .timestamp(LocalDateTime.now())
                .initiatedBy("SALES")
                .subject("Kontaktdaten aktualisiert")
                .summary("Feld '" + fieldUpdated + "' wurde aktualisiert")
                .engagementScore(20) // Low engagement for data maintenance
                .createdBy(userId)
                .build();
        
        try {
            interactionService.createInteraction(interaction);
        } catch (Exception e) {
            LOG.error("Failed to capture contact update", e);
        }
    }
    
    /**
     * Capture email sent to contact
     */
    public void captureEmailSent(UUID contactId, String userId, String subject, String content) {
        LOG.infof("Capturing email sent to %s by user %s", contactId, userId);
        
        ContactInteractionDTO interaction = ContactInteractionDTO.builder()
                .contactId(contactId)
                .type(InteractionType.EMAIL)
                .timestamp(LocalDateTime.now())
                .initiatedBy("SALES")
                .subject(subject)
                .fullContent(content)
                .summary(truncateSummary(content, 200))
                .channel("EMAIL")
                .engagementScore(50) // Medium engagement
                .createdBy(userId)
                .build();
        
        try {
            interactionService.createInteraction(interaction);
        } catch (Exception e) {
            LOG.error("Failed to capture email sent", e);
        }
    }
    
    /**
     * Capture phone call logged
     */
    public void capturePhoneCall(UUID contactId, String userId, Integer durationMinutes, 
                                String outcome, String notes) {
        LOG.infof("Capturing phone call to %s by user %s - duration: %d min", 
                 contactId, userId, durationMinutes);
        
        // Calculate engagement based on call duration
        Integer engagementScore = calculateCallEngagement(durationMinutes);
        
        ContactInteractionDTO interaction = ContactInteractionDTO.builder()
                .contactId(contactId)
                .type(InteractionType.CALL)
                .timestamp(LocalDateTime.now())
                .initiatedBy("SALES")
                .subject("Telefonat - " + durationMinutes + " Minuten")
                .summary(notes)
                .channel("PHONE")
                .outcome(outcome)
                .engagementScore(engagementScore)
                .createdBy(userId)
                .build();
        
        try {
            interactionService.createInteraction(interaction);
        } catch (Exception e) {
            LOG.error("Failed to capture phone call", e);
        }
    }
    
    /**
     * Capture meeting scheduled
     */
    public void captureMeetingScheduled(UUID contactId, String userId, 
                                       LocalDateTime meetingDate, String agenda) {
        LOG.infof("Capturing meeting scheduled with %s by user %s", contactId, userId);
        
        ContactInteractionDTO interaction = ContactInteractionDTO.builder()
                .contactId(contactId)
                .type(InteractionType.EVENT)
                .timestamp(LocalDateTime.now())
                .initiatedBy("SALES")
                .subject("Termin vereinbart")
                .summary(agenda)
                .nextAction("Termin wahrnehmen")
                .nextActionDate(meetingDate)
                .engagementScore(70) // High engagement for scheduling meeting
                .createdBy(userId)
                .build();
        
        try {
            interactionService.createInteraction(interaction);
        } catch (Exception e) {
            LOG.error("Failed to capture meeting scheduled", e);
        }
    }
    
    /**
     * Capture document shared
     */
    public void captureDocumentShared(UUID contactId, String userId, 
                                     String documentName, String documentType) {
        LOG.infof("Capturing document shared with %s by user %s - doc: %s", 
                 contactId, userId, documentName);
        
        ContactInteractionDTO interaction = ContactInteractionDTO.builder()
                .contactId(contactId)
                .type(InteractionType.DOCUMENT)
                .timestamp(LocalDateTime.now())
                .initiatedBy("SALES")
                .subject("Dokument geteilt: " + documentName)
                .summary("Dokumenttyp: " + documentType)
                .engagementScore(40) // Medium-low engagement
                .createdBy(userId)
                .build();
        
        try {
            interactionService.createInteraction(interaction);
        } catch (Exception e) {
            LOG.error("Failed to capture document shared", e);
        }
    }
    
    /**
     * Capture task created for contact
     */
    public void captureTaskCreated(UUID contactId, String userId, 
                                  String taskDescription, LocalDateTime dueDate) {
        LOG.infof("Capturing task created for %s by user %s", contactId, userId);
        
        ContactInteractionDTO interaction = ContactInteractionDTO.builder()
                .contactId(contactId)
                .type(InteractionType.TASK)
                .timestamp(LocalDateTime.now())
                .initiatedBy("SALES")
                .subject("Aufgabe erstellt")
                .summary(taskDescription)
                .nextActionDate(dueDate)
                .engagementScore(30) // Low-medium engagement
                .createdBy(userId)
                .build();
        
        try {
            interactionService.createInteraction(interaction);
        } catch (Exception e) {
            LOG.error("Failed to capture task created", e);
        }
    }
    
    /**
     * Listen for domain events and capture interactions
     */
    public void onContactEvent(@Observes ContactDomainEvent event) {
        LOG.debugf("Received contact domain event: %s", event.getEventType());
        
        switch (event.getEventType()) {
            case "CONTACT_VIEWED":
                captureContactView(event.getContactId(), event.getUserId());
                break;
            case "CONTACT_UPDATED":
                captureContactUpdate(event.getContactId(), event.getUserId(), 
                                   event.getMetadata().get("field"));
                break;
            case "EMAIL_SENT":
                captureEmailSent(event.getContactId(), event.getUserId(),
                               event.getMetadata().get("subject"),
                               event.getMetadata().get("content"));
                break;
            // Add more event types as needed
        }
    }
    
    // Helper methods
    
    private String truncateSummary(String content, int maxLength) {
        if (content == null) return null;
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength - 3) + "...";
    }
    
    private Integer calculateCallEngagement(Integer durationMinutes) {
        if (durationMinutes == null) return 50;
        if (durationMinutes < 2) return 20;
        if (durationMinutes < 5) return 40;
        if (durationMinutes < 15) return 60;
        if (durationMinutes < 30) return 80;
        return 100; // Very long calls indicate high engagement
    }
    
    // Event classes
    
    /**
     * Event fired when an interaction is captured
     */
    public static class ContactInteractionCaptured {
        private final UUID contactId;
        private final String interactionType;
        private final String userId;
        private final LocalDateTime timestamp;
        
        public ContactInteractionCaptured(UUID contactId, String interactionType, 
                                        String userId, LocalDateTime timestamp) {
            this.contactId = contactId;
            this.interactionType = interactionType;
            this.userId = userId;
            this.timestamp = timestamp;
        }
        
        // Getters
        public UUID getContactId() { return contactId; }
        public String getInteractionType() { return interactionType; }
        public String getUserId() { return userId; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    /**
     * Generic contact domain event
     */
    public static class ContactDomainEvent {
        private final String eventType;
        private final UUID contactId;
        private final String userId;
        private final java.util.Map<String, String> metadata;
        
        public ContactDomainEvent(String eventType, UUID contactId, 
                                 String userId, java.util.Map<String, String> metadata) {
            this.eventType = eventType;
            this.contactId = contactId;
            this.userId = userId;
            this.metadata = metadata;
        }
        
        // Getters
        public String getEventType() { return eventType; }
        public UUID getContactId() { return contactId; }
        public String getUserId() { return userId; }
        public java.util.Map<String, String> getMetadata() { return metadata; }
    }
}
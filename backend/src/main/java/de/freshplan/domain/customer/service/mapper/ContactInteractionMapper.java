package de.freshplan.domain.customer.service.mapper;

import de.freshplan.domain.customer.entity.ContactInteraction;
import de.freshplan.domain.customer.service.dto.ContactInteractionDTO;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapper for ContactInteraction entity and DTO
 */
@ApplicationScoped
public class ContactInteractionMapper {
    
    /**
     * Convert entity to DTO
     */
    public ContactInteractionDTO toDTO(ContactInteraction entity) {
        if (entity == null) {
            return null;
        }
        
        ContactInteractionDTO dto = new ContactInteractionDTO();
        dto.setId(entity.getId());
        dto.setContactId(entity.getContact() != null ? entity.getContact().getId() : null);
        dto.setType(entity.getType());
        dto.setTimestamp(entity.getTimestamp());
        dto.setSentimentScore(entity.getSentimentScore());
        dto.setEngagementScore(entity.getEngagementScore());
        dto.setResponseTimeMinutes(entity.getResponseTimeMinutes());
        dto.setWordCount(entity.getWordCount());
        dto.setInitiatedBy(entity.getInitiatedBy());
        dto.setSubject(entity.getSubject());
        dto.setSummary(entity.getSummary());
        dto.setFullContent(entity.getFullContent());
        dto.setChannel(entity.getChannel());
        dto.setChannelDetails(entity.getChannelDetails());
        dto.setOutcome(entity.getOutcome());
        dto.setNextAction(entity.getNextAction());
        dto.setNextActionDate(entity.getNextActionDate());
        dto.setExternalRefId(entity.getExternalRefId());
        dto.setExternalRefType(entity.getExternalRefType());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        
        return dto;
    }
    
    /**
     * Convert DTO to entity (without Contact reference)
     */
    public ContactInteraction toEntity(ContactInteractionDTO dto) {
        if (dto == null) {
            return null;
        }
        
        ContactInteraction entity = new ContactInteraction();
        entity.setId(dto.getId());
        // Contact must be set separately in service layer
        entity.setType(dto.getType());
        entity.setTimestamp(dto.getTimestamp());
        entity.setSentimentScore(dto.getSentimentScore());
        entity.setEngagementScore(dto.getEngagementScore());
        entity.setResponseTimeMinutes(dto.getResponseTimeMinutes());
        entity.setWordCount(dto.getWordCount());
        entity.setInitiatedBy(dto.getInitiatedBy());
        entity.setSubject(dto.getSubject());
        entity.setSummary(dto.getSummary());
        entity.setFullContent(dto.getFullContent());
        entity.setChannel(dto.getChannel());
        entity.setChannelDetails(dto.getChannelDetails());
        entity.setOutcome(dto.getOutcome());
        entity.setNextAction(dto.getNextAction());
        entity.setNextActionDate(dto.getNextActionDate());
        entity.setExternalRefId(dto.getExternalRefId());
        entity.setExternalRefType(dto.getExternalRefType());
        entity.setCreatedBy(dto.getCreatedBy());
        
        return entity;
    }
    
    /**
     * Update entity from DTO (for updates)
     */
    public void updateEntity(ContactInteraction entity, ContactInteractionDTO dto) {
        if (entity == null || dto == null) {
            return;
        }
        
        // Don't update ID or Contact reference
        entity.setType(dto.getType());
        entity.setTimestamp(dto.getTimestamp());
        entity.setSentimentScore(dto.getSentimentScore());
        entity.setEngagementScore(dto.getEngagementScore());
        entity.setResponseTimeMinutes(dto.getResponseTimeMinutes());
        entity.setWordCount(dto.getWordCount());
        entity.setInitiatedBy(dto.getInitiatedBy());
        entity.setSubject(dto.getSubject());
        entity.setSummary(dto.getSummary());
        entity.setFullContent(dto.getFullContent());
        entity.setChannel(dto.getChannel());
        entity.setChannelDetails(dto.getChannelDetails());
        entity.setOutcome(dto.getOutcome());
        entity.setNextAction(dto.getNextAction());
        entity.setNextActionDate(dto.getNextActionDate());
        entity.setExternalRefId(dto.getExternalRefId());
        entity.setExternalRefType(dto.getExternalRefType());
    }
}
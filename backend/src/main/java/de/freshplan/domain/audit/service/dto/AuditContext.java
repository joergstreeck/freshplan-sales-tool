package de.freshplan.domain.audit.service.dto;

import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.entity.AuditSource;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Context object for audit logging with all relevant information
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Data
@Builder(toBuilder = true)
public class AuditContext {
    
    // Required fields
    private final AuditEventType eventType;
    private final String entityType;
    private final UUID entityId;
    
    // Change details
    private final Object oldValue;
    private final Object newValue;
    private final String changeReason;
    private final String userComment;
    
    // User information (auto-populated if null)
    private final UUID userId;
    private final String userName;
    private final String userRole;
    
    // Context information
    private final String ipAddress;
    private final String userAgent;
    private final UUID sessionId;
    
    // Source information
    private final AuditSource source;
    private final String apiEndpoint;
    private final UUID requestId;
    
    // Additional metadata
    private final String correlationId;
    private final String externalReference;
    
    /**
     * Create a minimal audit context
     */
    public static AuditContext of(AuditEventType eventType, String entityType, UUID entityId) {
        return AuditContext.builder()
                .eventType(eventType)
                .entityType(entityType)
                .entityId(entityId)
                .build();
    }
    
    /**
     * Create audit context with old and new values
     */
    public static AuditContext ofChange(
            AuditEventType eventType, 
            String entityType, 
            UUID entityId,
            Object oldValue,
            Object newValue) {
        return AuditContext.builder()
                .eventType(eventType)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();
    }
}
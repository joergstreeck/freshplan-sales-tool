package de.freshplan.domain.audit.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

/**
 * Enterprise-grade Audit Trail Entity
 * 
 * Immutable audit log entry for compliance and security tracking.
 * Uses append-only pattern with cryptographic integrity verification.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Entity
@Table(name = "audit_trail", indexes = {
    @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id, timestamp DESC"),
    @Index(name = "idx_audit_user", columnList = "user_id, timestamp DESC"),
    @Index(name = "idx_audit_type", columnList = "event_type, timestamp DESC"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp DESC")
})
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class AuditEntry extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;
    
    @Column(name = "event_type", nullable = false, updatable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AuditEventType eventType;
    
    @Column(name = "entity_type", nullable = false, updatable = false, length = 50)
    private String entityType;
    
    @Column(name = "entity_id", nullable = false, updatable = false)
    private UUID entityId;
    
    // User Information
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;
    
    @Column(name = "user_name", nullable = false, updatable = false)
    private String userName;
    
    @Column(name = "user_role", nullable = false, updatable = false, length = 50)
    private String userRole;
    
    // Change Details - Using JSONB for flexibility
    @Column(name = "old_value", columnDefinition = "jsonb", updatable = false)
    private String oldValue;
    
    @Column(name = "new_value", columnDefinition = "jsonb", updatable = false)
    private String newValue;
    
    @Column(name = "change_reason", updatable = false, length = 500)
    private String changeReason;
    
    @Column(name = "user_comment", updatable = false, columnDefinition = "TEXT")
    private String userComment;
    
    // Context Information
    @Column(name = "ip_address", updatable = false)
    private String ipAddress;
    
    @Column(name = "user_agent", updatable = false, columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "session_id", updatable = false)
    private UUID sessionId;
    
    // Source Information
    @Column(name = "source", nullable = false, updatable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AuditSource source;
    
    @Column(name = "api_endpoint", updatable = false)
    private String apiEndpoint;
    
    @Column(name = "request_id", updatable = false)
    private UUID requestId;
    
    // Integrity & Compliance
    @Column(name = "data_hash", nullable = false, updatable = false, length = 64)
    private String dataHash;
    
    @Column(name = "previous_hash", updatable = false, length = 64)
    private String previousHash;
    
    // Version for schema evolution
    @Column(name = "schema_version", nullable = false, updatable = false)
    private Integer schemaVersion = 1;
    
    /**
     * Pre-persist validation to ensure data integrity
     */
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
        
        // Validate required fields
        if (eventType == null || entityType == null || entityId == null) {
            throw new IllegalStateException("Audit entry missing required fields");
        }
        
        if (userId == null || userName == null || userRole == null) {
            throw new IllegalStateException("Audit entry missing user information");
        }
        
        if (source == null) {
            source = AuditSource.SYSTEM;
        }
        
        if (dataHash == null) {
            throw new IllegalStateException("Audit entry missing data hash for integrity");
        }
    }
    
    /**
     * Check if this audit entry represents a failed operation
     */
    public boolean isFailure() {
        return eventType.name().contains("FAILURE") || 
               eventType.name().contains("DENIED") ||
               eventType.name().contains("ERROR");
    }
    
    /**
     * Check if this audit entry is security-relevant
     */
    public boolean isSecurityRelevant() {
        return eventType.name().contains("PERMISSION") ||
               eventType.name().contains("ROLE") ||
               eventType.name().contains("DELEGATION") ||
               eventType.name().contains("LOGIN") ||
               eventType.name().contains("GDPR");
    }
    
    /**
     * Get a redacted version for non-admin users
     */
    public AuditEntry getRedacted() {
        return this.toBuilder()
            .ipAddress("REDACTED")
            .userAgent("REDACTED")
            .sessionId(null)
            .oldValue(null)
            .newValue(null)
            .build();
    }
}
package de.freshplan.shared;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Base entity with common fields.
 */
@MappedSuperclass
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    // Getters
    public UUID getId() {
        return id;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
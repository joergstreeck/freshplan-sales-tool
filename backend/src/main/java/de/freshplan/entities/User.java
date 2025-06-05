package de.freshplan.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * User entity for FreshPlan application.
 * Maps to the app_user table to avoid PostgreSQL keyword conflicts.
 */
@Entity
@Table(name = "app_user")
public class User extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    public UUID id;
    
    @Column(length = 60, unique = true, nullable = false)
    public String username;
    
    @Column(name = "first_name", length = 60, nullable = false)
    public String firstName;
    
    @Column(name = "last_name", length = 60, nullable = false)
    public String lastName;
    
    @Column(length = 120, unique = true, nullable = false)
    public String email;
    
    @Column(nullable = false)
    public boolean enabled = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    // Helper method for full name
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
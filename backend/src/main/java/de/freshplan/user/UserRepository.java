package de.freshplan.user;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity operations.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, UUID> {
    
    /**
     * Find user by username.
     */
    public Optional<User> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }
    
    /**
     * Find user by email.
     */
    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }
    
    /**
     * Check if username exists.
     */
    public boolean existsByUsername(String username) {
        return count("username", username) > 0;
    }
    
    /**
     * Check if email exists.
     */
    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }
}
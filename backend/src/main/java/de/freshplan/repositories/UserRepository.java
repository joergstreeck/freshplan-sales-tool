package de.freshplan.repositories;

import de.freshplan.entities.User;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity operations.
 * Extends PanacheRepositoryBase for UUID-based primary keys.
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
     * Find all enabled users with pagination.
     */
    public List<User> findEnabledUsers(Page page) {
        return find("enabled = true", Sort.by("lastName", "firstName"))
                .page(page)
                .list();
    }
    
    /**
     * Check if username already exists.
     */
    public boolean existsByUsername(String username) {
        return count("username = ?1", username) > 0;
    }
    
    /**
     * Check if email already exists.
     */
    public boolean existsByEmail(String email) {
        return count("email = ?1", email) > 0;
    }
}
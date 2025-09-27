package de.freshplan.leads.repo;

import de.freshplan.leads.domain.LeadEntity;
import de.freshplan.leads.domain.LeadStatus;
import de.freshplan.security.ScopeContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ABAC-secured Lead Repository Implementation
 *
 * Implements territory-scoped data access for Lead Management
 * following Foundation Standards for ABAC security patterns.
 *
 * @see ../../../grundlagen/SQL_STANDARDS.md - Named Parameters, Performance Optimization
 * @see ../../../grundlagen/SECURITY_GUIDELINES.md - ABAC Territory Scoping
 * @see ../../../grundlagen/BACKEND_ARCHITECTURE.md - Repository Pattern
 *
 * @author Lead Management Team
 * @version 2.0
 * @since 2025-09-19
 */
@ApplicationScoped
public class LeadRepositoryImpl implements LeadRepository {

    private static final Logger log = LoggerFactory.getLogger(LeadRepositoryImpl.class);

    @Inject
    EntityManager em;

    @Inject
    ScopeContext scopeContext;

    /**
     * Persist a new Lead with automatic territory assignment
     */
    @Transactional
    @Override
    public void persist(LeadEntity entity) {
        // Ensure territory is set from security context if not provided
        if (entity.getTerritory() == null) {
            List<String> territories = scopeContext.getTerritories();
            if (territories != null && !territories.isEmpty()) {
                entity.setTerritory(territories.get(0));
            }
        }

        // Set audit fields
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        em.persist(entity);
        log.debug("Lead persisted: id={}, territory={}",
                  entity.getId(), entity.getTerritory());
    }

    /**
     * Find Lead by ID with ABAC territory validation
     */
    @Override
    public Optional<LeadEntity> findById(UUID id) {
        String jpql = "SELECT l FROM LeadEntity l " +
                      "WHERE l.id = :id " +
                      "AND l.territory IN :territories";

        TypedQuery<LeadEntity> query = em.createQuery(jpql, LeadEntity.class);
        query.setParameter("id", id);
        query.setParameter("territories", scopeContext.getTerritories());

        List<LeadEntity> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Find paginated Leads with ABAC territory scoping
     * Performance optimized with cursor-based pagination
     */
    @Override
    public List<LeadEntity> findPage(String searchQuery,
                                      LeadStatus status,
                                      List<String> scopedTerritories,
                                      String territoryFilter,
                                      UUID cursor,
                                      int limit) {

        // Build dynamic JPQL with territory scoping
        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT l FROM LeadEntity l WHERE 1=1");

        Map<String, Object> params = new HashMap<>();

        // ABAC: Always apply territory scoping from security context
        List<String> effectiveTerritories = scopedTerritories != null
            ? scopedTerritories
            : scopeContext.getTerritories();

        if (effectiveTerritories != null && !effectiveTerritories.isEmpty()) {
            jpql.append(" AND l.territory IN :territories");
            params.put("territories", effectiveTerritories);
        }

        // Additional filter for specific territory
        if (territoryFilter != null && !territoryFilter.isBlank()) {
            jpql.append(" AND l.territory = :territoryFilter");
            params.put("territoryFilter", territoryFilter);
        }

        // Search query
        if (searchQuery != null && !searchQuery.isBlank()) {
            jpql.append(" AND (LOWER(l.companyName) LIKE :query");
            jpql.append(" OR LOWER(l.contactName) LIKE :query");
            jpql.append(" OR LOWER(l.email) LIKE :query)");
            params.put("query", "%" + searchQuery.toLowerCase() + "%");
        }

        // Status filter
        if (status != null) {
            jpql.append(" AND l.status = :status");
            params.put("status", status);
        }

        // Cursor-based pagination for performance
        if (cursor != null) {
            jpql.append(" AND l.id > :cursor");
            params.put("cursor", cursor);
        }

        // Order by ID for consistent pagination
        jpql.append(" ORDER BY l.id ASC");

        // Create and configure query
        TypedQuery<LeadEntity> query = em.createQuery(jpql.toString(), LeadEntity.class);

        // Set all parameters
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        // Limit results (max 200 to prevent memory issues)
        int effectiveLimit = Math.max(1, Math.min(200, limit));
        query.setMaxResults(effectiveLimit);

        List<LeadEntity> results = query.getResultList();

        log.debug("Lead search: territories={}, results={}, limit={}",
                  effectiveTerritories, results.size(), effectiveLimit);

        return results;
    }

    /**
     * Update existing Lead with ABAC validation
     */
    @Transactional
    @Override
    public LeadEntity update(LeadEntity entity) {
        // Verify territory access
        Optional<LeadEntity> existing = findById(entity.getId());
        if (existing.isEmpty()) {
            throw new SecurityException("Lead not found or access denied: " + entity.getId());
        }

        // Update audit field
        entity.setUpdatedAt(LocalDateTime.now());

        // Merge changes
        LeadEntity merged = em.merge(entity);

        log.debug("Lead updated: id={}, territory={}",
                  merged.getId(), merged.getTerritory());

        return merged;
    }

    /**
     * Delete Lead with ABAC validation
     */
    @Transactional
    @Override
    public void delete(UUID id) {
        Optional<LeadEntity> entity = findById(id);
        if (entity.isPresent()) {
            em.remove(entity.get());
            log.debug("Lead deleted: id={}", id);
        } else {
            throw new SecurityException("Lead not found or access denied: " + id);
        }
    }

    /**
     * Count Leads by status with ABAC scoping
     */
    @Override
    public long countByStatus(LeadStatus status) {
        String jpql = "SELECT COUNT(l) FROM LeadEntity l " +
                      "WHERE l.status = :status " +
                      "AND l.territory IN :territories";

        TypedQuery<Long> query = em.createQuery(jpql, Long.class);
        query.setParameter("status", status);
        query.setParameter("territories", scopeContext.getTerritories());

        return query.getSingleResult();
    }

    /**
     * Find Leads created within date range with ABAC scoping
     */
    @Override
    public List<LeadEntity> findByDateRange(LocalDateTime startDate,
                                             LocalDateTime endDate,
                                             int limit) {
        String jpql = "SELECT l FROM LeadEntity l " +
                      "WHERE l.createdAt BETWEEN :startDate AND :endDate " +
                      "AND l.territory IN :territories " +
                      "ORDER BY l.createdAt DESC";

        TypedQuery<LeadEntity> query = em.createQuery(jpql, LeadEntity.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        query.setParameter("territories", scopeContext.getTerritories());
        query.setMaxResults(Math.min(limit, 1000));

        return query.getResultList();
    }

    /**
     * Get Lead statistics grouped by territory (admin only)
     */
    @Override
    public Map<String, Long> getStatsByTerritory() {
        String jpql = "SELECT l.territory, COUNT(l) FROM LeadEntity l " +
                      "WHERE l.territory IN :territories " +
                      "GROUP BY l.territory";

        TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
        query.setParameter("territories", scopeContext.getTerritories());

        Map<String, Long> stats = new HashMap<>();
        for (Object[] row : query.getResultList()) {
            stats.put((String) row[0], (Long) row[1]);
        }

        return stats;
    }

    /**
     * Bulk update Lead status with ABAC validation
     */
    @Transactional
    @Override
    public int bulkUpdateStatus(List<UUID> ids, LeadStatus newStatus) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        String jpql = "UPDATE LeadEntity l " +
                      "SET l.status = :newStatus, l.updatedAt = :updatedAt " +
                      "WHERE l.id IN :ids " +
                      "AND l.territory IN :territories";

        int updated = em.createQuery(jpql)
                        .setParameter("newStatus", newStatus)
                        .setParameter("updatedAt", LocalDateTime.now())
                        .setParameter("ids", ids)
                        .setParameter("territories", scopeContext.getTerritories())
                        .executeUpdate();

        log.info("Bulk status update: ids={}, newStatus={}, updated={}",
                 ids.size(), newStatus, updated);

        return updated;
    }
}
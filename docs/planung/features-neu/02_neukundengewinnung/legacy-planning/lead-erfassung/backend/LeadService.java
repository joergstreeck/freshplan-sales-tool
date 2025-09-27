package de.freshplan.leads.service;

import de.freshplan.leads.domain.LeadEntity;
import de.freshplan.leads.domain.LeadStatus;
import de.freshplan.leads.dto.LeadDTO;
import de.freshplan.leads.repo.LeadRepository;
import de.freshplan.security.ScopeContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Lead Business Service with Foundation Standards Compliance
 *
 * @see ../../grundlagen/API_STANDARDS.md - Jakarta EE Business Logic Standards
 * @see ../../grundlagen/SECURITY_GUIDELINES.md - ABAC Territory Enforcement
 * @see ../../grundlagen/PERFORMANCE_STANDARDS.md - P95 <200ms SLO Requirements
 * @see ../../grundlagen/BUSINESS_LOGIC_STANDARDS.md - Transaction Management
 *
 * This service provides ABAC-secured business logic for lead management
 * with territory-based access control and Foundation Standards compliance.
 *
 * @author Backend Team
 * @version 1.1
 * @since 2025-09-19
 */
@ApplicationScoped
public class LeadService {

    @Inject LeadRepository leadRepo;
    @Inject ScopeContext scope;

    public LeadDTO.Page search(String q, String status, String territory, String cursor, int limit) {
        LeadStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            statusEnum = LeadStatus.valueOf(status.toUpperCase());
        }

        UUID cursorUuid = null;
        if (cursor != null && !cursor.isBlank()) {
            cursorUuid = UUID.fromString(cursor);
        }

        var entities = leadRepo.findPage(q, statusEnum, scope.getTerritories(), territory, cursorUuid, limit);

        LeadDTO.Page page = new LeadDTO.Page();
        for (LeadEntity entity : entities) {
            page.items.add(LeadDTO.Item.from(entity));
        }

        if (entities.size() == limit) {
            page.nextCursor = entities.get(entities.size() - 1).getId().toString();
        }

        return page;
    }

    @Transactional
    public void create(LeadDTO.CreateRequest req) {
        LeadEntity entity = new LeadEntity(
            UUID.randomUUID(),
            req.name,
            req.territory,
            req.status != null ? req.status : LeadStatus.NEW,
            OffsetDateTime.now()
        );
        leadRepo.persist(entity);
    }
}
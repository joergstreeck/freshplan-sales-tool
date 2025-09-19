package de.freshplan.leads.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Lead Management Entity with Foundation Standards Compliance
 *
 * @see ../../grundlagen/API_STANDARDS.md - Jakarta EE Standards
 * @see ../../grundlagen/SECURITY_GUIDELINES.md - ABAC Implementation
 * @see ../../grundlagen/PERFORMANCE_STANDARDS.md - P95 <200ms SLO Requirements
 * @see ../../grundlagen/DESIGN_SYSTEM.md - FreshFoodz CI Integration
 *
 * This entity provides lead management capabilities with ABAC security
 * and follows OpenAPI 3.1 specifications for B2B food industry workflows.
 *
 * @author Backend Team
 * @version 1.1
 * @since 2025-09-19
 */
@Entity
@Table(name = "lead")
public class LeadEntity {
  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String territory;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private LeadStatus status;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  public LeadEntity() {}

  public LeadEntity(UUID id, String name, String territory, LeadStatus status, OffsetDateTime createdAt) {
    this.id = id; this.name = name; this.territory = territory; this.status = status; this.createdAt = createdAt;
  }

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getTerritory() { return territory; }
  public void setTerritory(String territory) { this.territory = territory; }
  public LeadStatus getStatus() { return status; }
  public void setStatus(LeadStatus status) { this.status = status; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}

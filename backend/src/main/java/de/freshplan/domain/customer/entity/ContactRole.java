package de.freshplan.domain.customer.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/**
 * Contact role entity representing different roles a contact can have. Examples: CEO, CTO,
 * Procurement Manager, Decision Maker, etc.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Entity
@Table(
    name = "contact_roles",
    indexes = {@Index(name = "idx_contact_role_name", columnList = "role_name", unique = true)})
public class ContactRole extends PanacheEntityBase {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "role_name", unique = true, nullable = false, length = 100)
  private String roleName;

  @Column(name = "description", length = 255)
  private String description;

  @Column(name = "is_decision_maker_role", nullable = false)
  private Boolean isDecisionMakerRole = false;

  @Column(name = "hierarchy_level")
  private Integer hierarchyLevel;

  // Default constructor
  public ContactRole() {}

  // Constructor for easy creation
  public ContactRole(String roleName, String description) {
    this.roleName = roleName;
    this.description = description;
  }

  public ContactRole(String roleName, String description, Boolean isDecisionMakerRole) {
    this.roleName = roleName;
    this.description = description;
    this.isDecisionMakerRole = isDecisionMakerRole;
  }

  // Getters and Setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Boolean getIsDecisionMakerRole() {
    return isDecisionMakerRole;
  }

  public void setIsDecisionMakerRole(Boolean isDecisionMakerRole) {
    this.isDecisionMakerRole = isDecisionMakerRole;
  }

  public Integer getHierarchyLevel() {
    return hierarchyLevel;
  }

  public void setHierarchyLevel(Integer hierarchyLevel) {
    this.hierarchyLevel = hierarchyLevel;
  }

  @Override
  public String toString() {
    return "ContactRole{"
        + "id="
        + id
        + ", roleName='"
        + roleName
        + '\''
        + ", description='"
        + description
        + '\''
        + ", isDecisionMakerRole="
        + isDecisionMakerRole
        + ", hierarchyLevel="
        + hierarchyLevel
        + '}';
  }
}

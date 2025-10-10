package de.freshplan.modules.leads.api;

import de.freshplan.modules.leads.domain.LeadStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;

/**
 * Request DTO for updating a lead (PATCH operation). All fields are optional. Supports
 * stop-the-clock feature and collaborator management.
 */
public class LeadUpdateRequest {

  @Size(max = 255)
  public String companyName;

  @Size(max = 255)
  public String contactPerson;

  @Email(message = "Invalid email format")
  @Size(max = 255)
  public String email;

  @Size(max = 50)
  public String phone;

  @Size(max = 255)
  public String website;

  @Size(max = 255)
  public String street;

  @Size(max = 20)
  public String postalCode;

  @Size(max = 100)
  public String city;

  // B2B-specific fields
  @Size(max = 100)
  public String businessType;

  @Size(max = 20)
  public String kitchenSize;

  public Integer employeeCount;

  public BigDecimal estimatedVolume;

  // Sprint 2.1.6+ Lead Scoring - Revenue Dimension
  public Boolean budgetConfirmed;

  @Size(max = 20)
  public String dealSize; // SMALL, MEDIUM, LARGE, ENTERPRISE

  // Status management
  public LeadStatus status;

  // Stop-the-clock feature
  public Boolean stopClock;

  @Size(max = 500, message = "Stop reason must not exceed 500 characters")
  public String stopReason;

  // Collaborator management
  public Set<String> addCollaborators;
  public Set<String> removeCollaborators;

  // Relationship Dimension (Sprint 2.1.6 Phase 5+ - V280)
  @Size(max = 30, message = "relationshipStatus must not exceed 30 characters")
  public String relationshipStatus; // COLD, CONTACTED, ENGAGED_SKEPTICAL, ENGAGED_POSITIVE, TRUSTED, ADVOCATE

  @Size(max = 30, message = "decisionMakerAccess must not exceed 30 characters")
  public String decisionMakerAccess; // UNKNOWN, BLOCKED, INDIRECT, DIRECT, IS_DECISION_MAKER

  @Size(max = 100, message = "competitorInUse must not exceed 100 characters")
  public String competitorInUse;

  @Size(max = 100, message = "internalChampionName must not exceed 100 characters")
  public String internalChampionName;

  // Pain Dimension (Sprint 2.1.6 Phase 5+ - Pain Scoring)
  public Boolean painStaffShortage;
  public Boolean painHighCosts;
  public Boolean painFoodWaste;
  public Boolean painQualityInconsistency;
  public Boolean painUnreliableDelivery;
  public Boolean painPoorService;
  public Boolean painSupplierQuality;
  public Boolean painTimePressure;

  @Size(max = 20, message = "urgencyLevel must not exceed 20 characters")
  public String urgencyLevel; // NORMAL, MEDIUM, HIGH, EMERGENCY

  public Integer multiPainBonus;

  @Size(max = 1000, message = "painNotes must not exceed 1000 characters")
  public String painNotes;
}

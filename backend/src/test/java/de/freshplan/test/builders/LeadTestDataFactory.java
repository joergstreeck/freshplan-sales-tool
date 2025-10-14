package de.freshplan.test.builders;

import de.freshplan.domain.shared.BusinessType;
import de.freshplan.domain.shared.DealSize;
import de.freshplan.domain.shared.KitchenSize;
import de.freshplan.domain.shared.LeadSource;
import de.freshplan.modules.leads.domain.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test data factory for Lead entities. Provides builder pattern for creating test leads without
 * CDI.
 *
 * <p>Track 2C - Advanced Test Infrastructure with RealisticDataGenerator
 *
 * @author Claude
 * @since Track 2C - Integrated with RealisticDataGenerator
 */
public class LeadTestDataFactory {

  // Thread-local RealisticDataGenerator for realistic defaults
  private static final ThreadLocal<RealisticDataGenerator> GENERATOR =
      ThreadLocal.withInitial(() -> new RealisticDataGenerator());

  // Collision-free ID generation
  private static final AtomicLong SEQ = new AtomicLong();

  /**
   * Create a seeded generator for deterministic tests.
   *
   * @param seed Seed value for repeatable randomization
   * @return Builder with seeded generator
   */
  public static Builder builder(long seed) {
    Builder builder = new Builder();
    builder.generator = new RealisticDataGenerator(seed);
    return builder;
  }

  /** Create a new builder instance. */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    // RealisticDataGenerator instance (thread-local or seeded)
    private RealisticDataGenerator generator = GENERATOR.get();

    // Required fields
    private String companyName; // Lazy-initialized
    private String countryCode = "DE";
    private LeadStatus status = LeadStatus.REGISTERED;
    private LocalDateTime registeredAt = LocalDateTime.now().minusDays(1);
    private LocalDateTime protectionStartAt = LocalDateTime.now();
    private Integer protectionMonths = 6;
    private Integer protectionDays60 = 60;
    private Integer protectionDays10 = 10;
    private Boolean isCanonical = true;
    private LeadStage stage = LeadStage.REGISTRIERUNG;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private String createdBy = "test-system";

    // Optional fields
    private String contactPerson;
    private String email;
    private String phone;
    private String website;
    private String street;
    private String postalCode;
    private String city;
    private Territory territory;
    private BusinessType businessType;
    private KitchenSize kitchenSize;
    private Integer employeeCount;
    private BigDecimal estimatedVolume;
    private Integer branchCount = 1;
    private Boolean isChain = false;

    // Lead scoring
    private Boolean budgetConfirmed;
    private DealSize dealSize;
    private Integer painScore;
    private Integer revenueScore;
    private Integer fitScore;
    private Integer engagementScore;
    private Integer leadScore;

    // Ownership
    private String ownerUserId;
    private String updatedBy;

    // Follow-up
    private LocalDateTime lastFollowupAt;
    private Integer followupCount = 0;

    // Source
    private LeadSource source;
    private String sourceCampaign;

    // Progressive Profiling
    private LocalDateTime firstContactDocumentedAt;
    private LocalDateTime lastActivityAt;

    // Pain & Urgency
    private Boolean painStaffShortage = false;
    private Boolean painHighCosts = false;
    private UrgencyLevel urgencyLevel = UrgencyLevel.NORMAL;

    // Relationship
    private RelationshipStatus relationshipStatus = RelationshipStatus.COLD;
    private DecisionMakerAccess decisionMakerAccess = DecisionMakerAccess.UNKNOWN;

    /** Set the company name. */
    public Builder withCompanyName(String companyName) {
      this.companyName = companyName;
      return this;
    }

    /** Set the contact person. */
    public Builder withContactPerson(String contactPerson) {
      this.contactPerson = contactPerson;
      return this;
    }

    /** Set the email. */
    public Builder withEmail(String email) {
      this.email = email;
      return this;
    }

    /** Set the phone. */
    public Builder withPhone(String phone) {
      this.phone = phone;
      return this;
    }

    /** Set the address. */
    public Builder withAddress(String street, String city, String postalCode) {
      this.street = street;
      this.city = city;
      this.postalCode = postalCode;
      return this;
    }

    /** Set the city. */
    public Builder withCity(String city) {
      this.city = city;
      return this;
    }

    /** Set the postal code. */
    public Builder withPostalCode(String postalCode) {
      this.postalCode = postalCode;
      return this;
    }

    /** Set the business type. */
    public Builder withBusinessType(BusinessType businessType) {
      this.businessType = businessType;
      return this;
    }

    /** Set the kitchen size. */
    public Builder withKitchenSize(KitchenSize kitchenSize) {
      this.kitchenSize = kitchenSize;
      return this;
    }

    /** Set the employee count. */
    public Builder withEmployeeCount(Integer employeeCount) {
      this.employeeCount = employeeCount;
      return this;
    }

    /** Set the estimated volume. */
    public Builder withEstimatedVolume(BigDecimal estimatedVolume) {
      this.estimatedVolume = estimatedVolume;
      return this;
    }

    /** Set the lead status. */
    public Builder withStatus(LeadStatus status) {
      this.status = status;
      return this;
    }

    /** Set the lead stage. */
    public Builder withStage(LeadStage stage) {
      this.stage = stage;
      return this;
    }

    /** Set the owner user ID. */
    public Builder withOwnerUserId(String ownerUserId) {
      this.ownerUserId = ownerUserId;
      return this;
    }

    /** Set the lead source. */
    public Builder withSource(LeadSource source) {
      this.source = source;
      return this;
    }

    /** Set the deal size. */
    public Builder withDealSize(DealSize dealSize) {
      this.dealSize = dealSize;
      return this;
    }

    /** Set the budget confirmed flag. */
    public Builder withBudgetConfirmed(Boolean budgetConfirmed) {
      this.budgetConfirmed = budgetConfirmed;
      return this;
    }

    /** Set pain scores. */
    public Builder withPainStaffShortage(Boolean painStaffShortage) {
      this.painStaffShortage = painStaffShortage;
      return this;
    }

    /** Set urgency level. */
    public Builder withUrgencyLevel(UrgencyLevel urgencyLevel) {
      this.urgencyLevel = urgencyLevel;
      return this;
    }

    /** Set relationship status. */
    public Builder withRelationshipStatus(RelationshipStatus relationshipStatus) {
      this.relationshipStatus = relationshipStatus;
      return this;
    }

    /** Set first contact documented at. */
    public Builder withFirstContactDocumentedAt(LocalDateTime firstContactDocumentedAt) {
      this.firstContactDocumentedAt = firstContactDocumentedAt;
      return this;
    }

    /** Set registered at. */
    public Builder withRegisteredAt(LocalDateTime registeredAt) {
      this.registeredAt = registeredAt;
      return this;
    }

    /** Set created by. */
    public Builder createdBy(String createdBy) {
      this.createdBy = createdBy;
      return this;
    }

    /**
     * Build the lead entity without persisting.
     *
     * <p>Track 2C - Enhanced with RealisticDataGenerator defaults
     *
     * @return The built lead entity
     */
    public Lead build() {
      // Realistic defaults from RealisticDataGenerator
      if (companyName == null) {
        companyName = "[TEST] " + generator.germanCateringCompanyName();
      }
      if (city == null) {
        city = generator.germanCity();
      }
      if (postalCode == null) {
        postalCode = generator.germanPostalCode();
      }
      if (contactPerson == null) {
        contactPerson = generator.germanFullName();
      }
      if (email == null) {
        String[] nameParts = contactPerson.split(" ");
        String firstName = nameParts.length > 0 ? nameParts[0] : "test";
        String lastName = nameParts.length > 1 ? nameParts[nameParts.length - 1] : "user";
        email = generator.email(firstName, lastName, "example.com");
      }
      if (phone == null) {
        phone = generator.germanPhoneNumber();
      }
      if (employeeCount == null) {
        employeeCount = generator.realisticEmployeeCount();
      }

      Lead lead = new Lead();

      // Required fields
      lead.companyName = companyName;
      lead.companyNameNormalized = companyName.toLowerCase();
      lead.countryCode = countryCode;
      lead.status = status;
      lead.registeredAt = registeredAt;
      lead.protectionStartAt = protectionStartAt;
      lead.protectionMonths = protectionMonths;
      lead.protectionDays60 = protectionDays60;
      lead.protectionDays10 = protectionDays10;
      lead.isCanonical = isCanonical;
      lead.stage = stage;
      lead.createdAt = createdAt;
      lead.updatedAt = updatedAt;
      lead.createdBy = createdBy;

      // Optional fields
      lead.contactPerson = contactPerson;
      lead.email = email;
      lead.emailNormalized = email != null ? email.toLowerCase() : null;
      lead.phone = phone;
      lead.website = website;
      lead.street = street;
      lead.postalCode = postalCode;
      lead.city = city;
      lead.territory = territory;
      lead.businessType = businessType;
      lead.kitchenSize = kitchenSize;
      lead.employeeCount = employeeCount;
      lead.estimatedVolume = estimatedVolume;
      lead.branchCount = branchCount;
      lead.isChain = isChain;

      // Ownership
      lead.ownerUserId = ownerUserId;
      lead.updatedBy = updatedBy;

      // Follow-up
      lead.lastFollowupAt = lastFollowupAt;
      lead.followupCount = followupCount;

      // Source
      lead.source = source;
      lead.sourceCampaign = sourceCampaign;

      // Progressive Profiling
      lead.firstContactDocumentedAt = firstContactDocumentedAt;
      lead.lastActivityAt = lastActivityAt;

      // Scoring
      lead.budgetConfirmed = budgetConfirmed;
      lead.dealSize = dealSize;
      lead.painScore = painScore;
      lead.revenueScore = revenueScore;
      lead.fitScore = fitScore;
      lead.engagementScore = engagementScore;
      lead.leadScore = leadScore;

      // Pain & Urgency
      lead.painStaffShortage = painStaffShortage;
      lead.painHighCosts = painHighCosts;
      lead.urgencyLevel = urgencyLevel;

      // Relationship
      lead.relationshipStatus = relationshipStatus;
      lead.decisionMakerAccess = decisionMakerAccess;

      return lead;
    }

    /**
     * Build a minimal test lead (Pre-Claim State - Vormerkung).
     *
     * <p>Convenience method for common test case: Lead ohne FirstContact, Status = REGISTERED,
     * Stage = VORMERKUNG
     */
    public Lead buildMinimal() {
      return withStatus(LeadStatus.REGISTERED)
          .withStage(LeadStage.VORMERKUNG)
          .withFirstContactDocumentedAt(null)
          .build();
    }

    /**
     * Build a qualified lead (Registrierung Stage + FirstContact).
     *
     * <p>Convenience method: Lead mit FirstContact dokumentiert, Status = REGISTERED, Stage =
     * REGISTRIERUNG
     */
    public Lead buildQualified() {
      return withStatus(LeadStatus.REGISTERED)
          .withStage(LeadStage.REGISTRIERUNG)
          .withFirstContactDocumentedAt(LocalDateTime.now().minusDays(5))
          .build();
    }

    /**
     * Build and persist to database.
     *
     * @param repository The repository to use for persistence
     * @return The persisted lead entity
     */
    public Lead buildAndPersist(Object repository) {
      Lead lead = build();
      // Note: Actual persistence requires injected repository
      // This is a placeholder for future implementation
      return lead;
    }
  }
}

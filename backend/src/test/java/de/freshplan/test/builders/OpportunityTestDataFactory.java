package de.freshplan.test.builders;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.user.entity.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test data factory for Opportunity entities. Provides builder pattern for creating test
 * opportunities without CDI.
 *
 * @author Claude
 * @since Migration Phase 4
 * @since Phase 2B - Enhanced with unique name generation and audit fields
 */
public class OpportunityTestDataFactory {

  // KOLLISIONSFREIE ID-GENERIERUNG - Thread-Safe & CI-kompatibel
  private static final AtomicLong OPP_SEQ = new AtomicLong();

  /** Create a new builder instance. */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    // Default values
    private String name;
    private String description;
    private OpportunityStage stage = OpportunityStage.NEW_LEAD;
    private Customer customer;
    private User assignedTo;
    private BigDecimal expectedValue;
    private LocalDate expectedCloseDate;
    private Integer probability;
    private String source;
    private String competitorInfo;
    private String nextSteps;
    private LocalDateTime lastActivityDate;
    private String notes;
    private String lossReason;
    private BigDecimal actualValue;
    private LocalDate actualCloseDate;

    /**
     * Generiert eindeutige Opportunity-Namen für Tests.
     * Format: TEST Opportunity {RUN_ID}-{SEQ}
     */
    private static String generateUniqueName() {
      String runId = System.getProperty("test.run.id", 
          System.getenv().getOrDefault("GITHUB_RUN_ID", "LOCAL"));
      long seq = OPP_SEQ.incrementAndGet();
      return "TEST Opportunity " + runId + "-" + String.format("%03d", seq);
    }

    /**
     * Builder-Konstruktor mit intelligenten Defaults
     */
    public Builder() {
      this.name = generateUniqueName();
      this.stage = OpportunityStage.NEW_LEAD;
      this.expectedValue = BigDecimal.valueOf(10000);
      this.probability = 25; // Standard für NEW_LEAD
      this.expectedCloseDate = LocalDate.now().plusDays(30);
      this.source = "TEST";
      this.description = "Test opportunity created for automated testing";
    }

    /** Set the opportunity name. */
    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    /** Set the description. */
    public Builder withDescription(String description) {
      this.description = description;
      return this;
    }

    /** Set the stage. */
    public Builder inStage(OpportunityStage stage) {
      this.stage = stage;
      return this;
    }

    /** Set the customer. */
    public Builder forCustomer(Customer customer) {
      this.customer = customer;
      return this;
    }

    /** Set the assigned user. */
    public Builder assignedTo(User assignedTo) {
      this.assignedTo = assignedTo;
      return this;
    }

    /** Set the expected value. */
    public Builder withExpectedValue(BigDecimal expectedValue) {
      this.expectedValue = expectedValue;
      return this;
    }

    /** Set the expected close date. */
    public Builder withExpectedCloseDate(LocalDate expectedCloseDate) {
      this.expectedCloseDate = expectedCloseDate;
      return this;
    }

    /** Set the probability. */
    public Builder withProbability(Integer probability) {
      this.probability = probability;
      return this;
    }

    /** Set the source. */
    public Builder withSource(String source) {
      this.source = source;
      return this;
    }

    /** Set the competitor info. */
    public Builder withCompetitorInfo(String competitorInfo) {
      this.competitorInfo = competitorInfo;
      return this;
    }

    /** Set the next steps. */
    public Builder withNextSteps(String nextSteps) {
      this.nextSteps = nextSteps;
      return this;
    }

    /** Set the last activity date. */
    public Builder withLastActivityDate(LocalDateTime lastActivityDate) {
      this.lastActivityDate = lastActivityDate;
      return this;
    }

    /** Set the notes. */
    public Builder withNotes(String notes) {
      this.notes = notes;
      return this;
    }

    /** Set the loss reason. */
    public Builder withLossReason(String lossReason) {
      this.lossReason = lossReason;
      return this;
    }

    /** Set the actual value. */
    public Builder withActualValue(BigDecimal actualValue) {
      this.actualValue = actualValue;
      return this;
    }

    /** Set the actual close date. */
    public Builder withActualCloseDate(LocalDate actualCloseDate) {
      this.actualCloseDate = actualCloseDate;
      return this;
    }

    /**
     * Build the opportunity entity without persisting.
     *
     * @return The built opportunity entity
     */
    public Opportunity build() {
      // Create mock assignedTo user if not provided
      if (assignedTo == null) {
        assignedTo = createMockUser();
      }
      
      // Use constructor for required fields
      Opportunity opportunity = new Opportunity(name, stage, assignedTo);

      // Set optional fields
      opportunity.setDescription(description);
      opportunity.setCustomer(customer);
      opportunity.setExpectedValue(expectedValue);
      opportunity.setExpectedCloseDate(expectedCloseDate);

      // Set probability if provided, otherwise use stage default
      if (probability != null) {
        opportunity.setProbability(probability);
      }

      // Test-Data-Flag setzen falls Feld existiert
      setTestDataFlagIfExists(opportunity);

      // Audit-Felder für Tests setzen falls vorhanden
      setAuditFieldsIfExists(opportunity);

      // Fields that don't exist in Opportunity entity - commented out
      // opportunity.setSource(source);
      // opportunity.setCompetitorInfo(competitorInfo);
      // opportunity.setNextSteps(nextSteps);
      // opportunity.setLastActivityDate(lastActivityDate);
      // opportunity.setNotes(notes);
      // opportunity.setLossReason(lossReason);
      // opportunity.setActualValue(actualValue);
      // opportunity.setActualCloseDate(actualCloseDate);

      return opportunity;
    }

    /**
     * Setzt isTestData=true falls das Feld existiert.
     * Failsafe approach für optionales Feld.
     */
    private void setTestDataFlagIfExists(Opportunity opportunity) {
      try {
        java.lang.reflect.Method setter = opportunity.getClass().getMethod("setIsTestData", Boolean.class);
        setter.invoke(opportunity, true);
      } catch (Exception e) {
        // Field doesn't exist - that's ok
        // Opportunity hat wahrscheinlich kein isTestData Feld
      }
    }

    /**
     * Setzt Audit-Felder falls sie existieren.
     * Failsafe approach für optionale Felder.
     */
    private void setAuditFieldsIfExists(Opportunity opportunity) {
      LocalDateTime now = LocalDateTime.now();
      String testSystem = "test-system";
      
      try {
        // createdAt
        java.lang.reflect.Method setCreatedAt = opportunity.getClass().getMethod("setCreatedAt", LocalDateTime.class);
        setCreatedAt.invoke(opportunity, now);
      } catch (Exception e) {
        // Field doesn't exist - ok
      }
      
      try {
        // createdBy
        java.lang.reflect.Method setCreatedBy = opportunity.getClass().getMethod("setCreatedBy", String.class);
        setCreatedBy.invoke(opportunity, testSystem);
      } catch (Exception e) {
        // Field doesn't exist - ok
      }
      
      try {
        // updatedAt
        java.lang.reflect.Method setUpdatedAt = opportunity.getClass().getMethod("setUpdatedAt", LocalDateTime.class);
        setUpdatedAt.invoke(opportunity, now);
      } catch (Exception e) {
        // Field doesn't exist - ok
      }
      
      try {
        // updatedBy
        java.lang.reflect.Method setUpdatedBy = opportunity.getClass().getMethod("setUpdatedBy", String.class);
        setUpdatedBy.invoke(opportunity, testSystem);
      } catch (Exception e) {
        // Field doesn't exist - ok
      }
    }

    /**
     * Create a mock user for opportunities that need an assignedTo user.
     */
    private User createMockUser() {
      // Nutze UserTestDataFactory für konsistente Test-User
      return UserTestDataFactory.builder()
          .withUsername("opp-test-user")
          .withFirstName("Opportunity")
          .withLastName("TestUser")
          .build();
    }

    /** Build a minimal test opportunity. Convenience method for common test case. */
    public Opportunity buildMinimal() {
      return withName("Test Opportunity").inStage(OpportunityStage.NEW_LEAD).build();
    }
  }
}

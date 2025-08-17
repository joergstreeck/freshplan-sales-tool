package de.freshplan.test.builders;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.domain.user.entity.User;
import de.freshplan.test.utils.TestDataUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Builder for creating test Opportunity entities.
 * Provides fluent API for setting opportunity properties and sales stages.
 * 
 * This builder ensures all test opportunities are properly marked
 * and can be identified for cleanup.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class OpportunityBuilder {
    
    @Inject
    OpportunityRepository repository;
    
    @Inject
    CustomerRepository customerRepository;
    
    // Required fields
    private Customer customer = null;
    private String name = "Test Opportunity";
    private OpportunityStage stage = OpportunityStage.NEW_LEAD;
    
    // Optional fields
    private User assignedTo = null;
    private BigDecimal expectedValue = BigDecimal.valueOf(10000);
    private LocalDate expectedCloseDate = LocalDate.now().plusMonths(3);
    private Integer probability = null; // Will be set based on stage if not specified
    private String description = null;
    
    // Activity tracking
    private List<String> activities = new ArrayList<>();
    
    // Test marker - opportunities don't have isTestData field, so we mark via name
    private final String testMarker = "[TEST]";
    
    /**
     * Resets the builder to default values for creating a new opportunity.
     * 
     * @return this builder instance for chaining
     */
    public OpportunityBuilder reset() {
        this.customer = null;
        this.name = "Test Opportunity";
        this.stage = OpportunityStage.NEW_LEAD;
        this.assignedTo = null;
        this.expectedValue = BigDecimal.valueOf(10000);
        this.expectedCloseDate = LocalDate.now().plusMonths(3);
        this.probability = null;
        this.description = null;
        this.activities = new ArrayList<>();
        return this;
    }
    
    // Customer association (REQUIRED)
    public OpportunityBuilder forCustomer(Customer customer) {
        this.customer = customer;
        return this;
    }
    
    public OpportunityBuilder forCustomerId(UUID customerId) {
        this.customer = customerRepository.findById(customerId);
        if (this.customer == null) {
            throw new IllegalArgumentException("Customer not found: " + customerId);
        }
        return this;
    }
    
    // Basic setters
    public OpportunityBuilder withName(String name) {
        this.name = name;
        return this;
    }
    
    public OpportunityBuilder inStage(OpportunityStage stage) {
        this.stage = stage;
        // Auto-set probability based on stage default if not manually set
        if (this.probability == null) {
            this.probability = stage.getDefaultProbability();
        }
        return this;
    }
    
    public OpportunityBuilder assignedTo(User user) {
        this.assignedTo = user;
        return this;
    }
    
    // Value setters
    public OpportunityBuilder withExpectedValue(BigDecimal value) {
        this.expectedValue = value;
        return this;
    }
    
    public OpportunityBuilder withExpectedValue(long value) {
        this.expectedValue = BigDecimal.valueOf(value);
        return this;
    }
    
    public OpportunityBuilder withExpectedCloseDate(LocalDate date) {
        this.expectedCloseDate = date;
        return this;
    }
    
    public OpportunityBuilder closingInDays(int days) {
        this.expectedCloseDate = LocalDate.now().plusDays(days);
        return this;
    }
    
    public OpportunityBuilder closingInMonths(int months) {
        this.expectedCloseDate = LocalDate.now().plusMonths(months);
        return this;
    }
    
    public OpportunityBuilder withProbability(int probability) {
        this.probability = Math.min(100, Math.max(0, probability));
        return this;
    }
    
    public OpportunityBuilder withDescription(String description) {
        this.description = description;
        return this;
    }
    
    // Activity tracking
    public OpportunityBuilder withActivity(String activity) {
        this.activities.add(activity);
        return this;
    }
    
    // Predefined scenarios
    public OpportunityBuilder asNewLead() {
        this.stage = OpportunityStage.NEW_LEAD;
        this.probability = 10;
        this.expectedCloseDate = LocalDate.now().plusMonths(6);
        this.description = "Neuer Lead aus Test-Szenario";
        return this;
    }
    
    public OpportunityBuilder asQualifiedLead() {
        this.stage = OpportunityStage.QUALIFICATION;
        this.probability = 25;
        this.expectedValue = BigDecimal.valueOf(50000);
        this.expectedCloseDate = LocalDate.now().plusMonths(3);
        this.description = "Qualifizierte Opportunity mit gutem Potenzial";
        return this;
    }
    
    public OpportunityBuilder asProposal() {
        this.stage = OpportunityStage.PROPOSAL;
        this.probability = 60;
        this.expectedValue = BigDecimal.valueOf(100000);
        this.expectedCloseDate = LocalDate.now().plusMonths(1);
        this.description = "Angebot wurde versendet";
        this.withActivity("Angebot erstellt");
        this.withActivity("Angebot versendet");
        return this;
    }
    
    public OpportunityBuilder asNegotiation() {
        this.stage = OpportunityStage.NEGOTIATION;
        this.probability = 80;
        this.expectedValue = BigDecimal.valueOf(150000);
        this.expectedCloseDate = LocalDate.now().plusWeeks(2);
        this.description = "In finaler Verhandlungsphase";
        return this;
    }
    
    public OpportunityBuilder asWon() {
        this.stage = OpportunityStage.CLOSED_WON;
        this.probability = 100;
        this.expectedCloseDate = LocalDate.now();
        this.description = "Erfolgreich abgeschlossen!";
        return this;
    }
    
    public OpportunityBuilder asLost() {
        this.stage = OpportunityStage.CLOSED_LOST;
        this.probability = 0;
        this.expectedCloseDate = LocalDate.now();
        this.description = "Verloren an Mitbewerber";
        return this;
    }
    
    public OpportunityBuilder asRenewal() {
        this.stage = OpportunityStage.RENEWAL;
        this.probability = 75;
        this.expectedCloseDate = LocalDate.now().plusMonths(1);
        this.description = "Vertragsverlängerung anstehend";
        return this;
    }
    
    public OpportunityBuilder asHighValue() {
        this.expectedValue = BigDecimal.valueOf(TestDataUtils.randomInt(500_000, 1_000_000));
        this.stage = OpportunityStage.NEEDS_ANALYSIS;
        this.probability = 40;
        return this;
    }
    
    public OpportunityBuilder asUrgent() {
        this.expectedCloseDate = LocalDate.now().plusDays(7);
        if (this.stage.isActive()) {
            this.stage = OpportunityStage.NEGOTIATION;
            this.probability = 80;
        }
        return this;
    }
    
    /**
     * Builds an Opportunity entity WITHOUT persisting to database.
     * Use this for unit tests or when you need an entity without DB interaction.
     * 
     * @return a new Opportunity entity with test data markers
     */
    public Opportunity build() {
        if (customer == null) {
            throw new IllegalStateException("Customer is required for OpportunityBuilder. Use forCustomer() first.");
        }
        
        Opportunity opportunity = new Opportunity();
        String id = TestDataUtils.uniqueId();
        
        // Set required fields with test marker
        opportunity.setName(testMarker + " " + name + " " + id);
        opportunity.setStage(stage);
        opportunity.setCustomer(customer);
        
        // Set optional fields
        opportunity.setAssignedTo(assignedTo);
        opportunity.setExpectedValue(expectedValue);
        opportunity.setExpectedCloseDate(expectedCloseDate);
        
        // Set probability - use stage default if not specified
        if (probability == null) {
            probability = stage.getDefaultProbability();
        }
        opportunity.setProbability(probability);
        
        // Set description
        if (description == null) {
            description = "Test opportunity for " + customer.getCompanyName() + 
                         " in stage " + stage.getDisplayName();
        }
        opportunity.setDescription(description);
        
        // Add activities to description if present
        if (!activities.isEmpty()) {
            opportunity.setDescription(description + "\nAktivitäten: " + String.join(", ", activities));
        }
        
        // Note: Timestamps (createdAt, stageChangedAt, updatedAt) are set automatically by JPA
        
        return opportunity;
    }
    
    /**
     * Builds and persists an Opportunity entity to the database.
     * Use this for integration tests that need DB interaction.
     * 
     * @return the persisted Opportunity entity
     */
    @Transactional
    public Opportunity persist() {
        Opportunity opportunity = build();
        repository.persist(opportunity);
        repository.flush(); // Force immediate constraint validation
        return opportunity;
    }
}
package de.freshplan.test;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.entity.CustomerTimelineEvent;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.test.builders.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

/**
 * Central facade for test data creation in FreshPlan application.
 * 
 * This class serves as the single entry point for all test data builders,
 * providing a clean and consistent API for creating test entities.
 * 
 * <h3>Architecture</h3>
 * The test data architecture consists of:
 * <ul>
 *   <li>This facade class as the central entry point</li>
 *   <li>Individual builder classes for each entity type</li>
 *   <li>Utility classes for common functionality</li>
 *   <li>All test data is marked for easy identification and cleanup</li>
 * </ul>
 * 
 * <h3>Usage Example</h3>
 * <pre>{@code
 * @Inject
 * TestDataBuilder testData;
 * 
 * // Create a premium customer with contacts
 * Customer customer = testData.customer()
 *     .asPremiumCustomer()
 *     .withCompanyName("Test GmbH")
 *     .persist();
 * 
 * CustomerContact ceo = testData.contact()
 *     .forCustomer(customer)
 *     .asCEO()
 *     .persist();
 * 
 * Opportunity opportunity = testData.opportunity()
 *     .forCustomer(customer)
 *     .asProposal()
 *     .withExpectedValue(100000)
 *     .persist();
 * }</pre>
 * 
 * <h3>Key Principles</h3>
 * <ul>
 *   <li>All test data is marked with isTestData=true (where available)</li>
 *   <li>All test data has identifying prefixes ([TEST] or test-)</li>
 *   <li>Builders provide both build() and persist() methods</li>
 *   <li>build() creates objects without DB interaction</li>
 *   <li>persist() creates and saves to DB in a transaction</li>
 * </ul>
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class TestDataBuilder {
    
    @Inject
    private CustomerBuilder customerBuilder;
    
    @Inject
    private ContactBuilder contactBuilder;
    
    @Inject
    private OpportunityBuilder opportunityBuilder;
    
    @Inject
    private TimelineEventBuilder timelineEventBuilder;
    
    @Inject
    private UserBuilder userBuilder;
    
    /**
     * Creates a new CustomerBuilder for building test customers.
     * The builder is reset to default values before being returned.
     * 
     * @return a fresh CustomerBuilder instance
     */
    public CustomerBuilder customer() {
        return customerBuilder.reset();
    }
    
    /**
     * Creates a new ContactBuilder for building test contacts.
     * The builder is reset to default values before being returned.
     * 
     * @return a fresh ContactBuilder instance
     */
    public ContactBuilder contact() {
        return contactBuilder.reset();
    }
    
    /**
     * Creates a new OpportunityBuilder for building test opportunities.
     * The builder is reset to default values before being returned.
     * 
     * @return a fresh OpportunityBuilder instance
     */
    public OpportunityBuilder opportunity() {
        return opportunityBuilder.reset();
    }
    
    /**
     * Creates a new TimelineEventBuilder for building test timeline events.
     * The builder is reset to default values before being returned.
     * 
     * @return a fresh TimelineEventBuilder instance
     */
    public TimelineEventBuilder timelineEvent() {
        return timelineEventBuilder.reset();
    }
    
    /**
     * Creates a new UserBuilder for building test users.
     * The builder is reset to default values before being returned.
     * 
     * @return a fresh UserBuilder instance
     */
    public UserBuilder user() {
        return userBuilder.reset();
    }
    
    /**
     * Creates complex test scenarios with multiple related entities.
     * This method is useful for setting up complete test environments.
     * 
     * @return a new ScenarioBuilder instance
     */
    public ScenarioBuilder scenario() {
        return new ScenarioBuilder(this);
    }
    
    /**
     * Builder for creating complex test scenarios with multiple related entities.
     * Provides convenience methods for setting up complete test environments.
     */
    public static class ScenarioBuilder {
        private final TestDataBuilder testDataBuilder;
        
        ScenarioBuilder(TestDataBuilder testDataBuilder) {
            this.testDataBuilder = testDataBuilder;
        }
        
        /**
         * Creates a complete customer scenario with contacts and opportunities.
         * 
         * @param companyName the name of the company
         * @return a CompleteCustomerScenario with all created entities
         */
        public CompleteCustomerScenario completeCustomerWithContactsAndOpportunities(String companyName) {
            // Create customer
            Customer customer = testDataBuilder.customer()
                .withCompanyName(companyName)
                .asNewLead()
                .persist();
            
            // Create primary contact (CEO)
            CustomerContact ceo = testDataBuilder.contact()
                .forCustomer(customer)
                .asCEO()
                .withFullName("Max", "Mustermann")
                .persist();
            
            // Create purchasing contact
            CustomerContact purchaser = testDataBuilder.contact()
                .forCustomer(customer)
                .asPurchasingManager()
                .withFullName("Erika", "Musterfrau")
                .persist();
            
            // Create opportunity
            Opportunity opportunity = testDataBuilder.opportunity()
                .forCustomer(customer)
                .asQualifiedLead()
                .withExpectedValue(75000)
                .persist();
            
            // Create timeline events
            CustomerTimelineEvent firstContact = testDataBuilder.timelineEvent()
                .forCustomer(customer)
                .asPhoneCall("Erstkontakt hergestellt")
                .happenedDaysAgo(7)
                .persist();
            
            CustomerTimelineEvent meeting = testDataBuilder.timelineEvent()
                .forCustomer(customer)
                .asMeeting("Produktpräsentation")
                .happenedDaysAgo(3)
                .relatedToContact(ceo.getId())
                .persist();
            
            return new CompleteCustomerScenario(
                customer,
                List.of(ceo, purchaser),
                opportunity,
                List.of(firstContact, meeting)
            );
        }
        
        /**
         * Container for a complete customer scenario.
         */
        public static class CompleteCustomerScenario {
            public final Customer customer;
            public final List<CustomerContact> contacts;
            public final Opportunity opportunity;
            public final List<CustomerTimelineEvent> events;
            
            CompleteCustomerScenario(Customer customer, 
                                    List<CustomerContact> contacts,
                                    Opportunity opportunity,
                                    List<CustomerTimelineEvent> events) {
                this.customer = customer;
                this.contacts = contacts;
                this.opportunity = opportunity;
                this.events = events;
            }
        }
    }
}
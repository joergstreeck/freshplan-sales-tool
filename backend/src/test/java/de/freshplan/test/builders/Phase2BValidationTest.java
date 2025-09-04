package de.freshplan.test.builders;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
/**
 * Validation tests for Phase 2B - Builder Advanced Features.
 * Verifies that all builders have unique ID generation and proper features.
 */
@Tag("migrate")class Phase2BValidationTest {

  @Test
  void contactTestDataFactory_generatesUniqueEmails() {
    // Given/When
    CustomerContact contact1 = ContactTestDataFactory.builder().build();
    CustomerContact contact2 = ContactTestDataFactory.builder().build();
    
    // Then
    assertThat(contact1.getEmail()).isNotNull();
    assertThat(contact2.getEmail()).isNotNull();
    assertThat(contact1.getEmail()).isNotEqualTo(contact2.getEmail());
    assertThat(contact1.getEmail()).contains("@test.example.com");
    
    // Mock Customer should have isTestData=true
    assertThat(contact1.getCustomer()).isNotNull();
    assertThat(contact1.getCustomer().getIsTestData()).isTrue();
    assertThat(contact1.getCustomer().getCustomerNumber()).startsWith("TEST-MOCK-");
  }
  
  @Test
  void opportunityTestDataFactory_generatesUniqueNames() {
    // Given/When
    Opportunity opp1 = OpportunityTestDataFactory.builder().build();
    Opportunity opp2 = OpportunityTestDataFactory.builder().build();
    
    // Then
    assertThat(opp1.getName()).isNotNull();
    assertThat(opp2.getName()).isNotNull();
    assertThat(opp1.getName()).isNotEqualTo(opp2.getName());
    assertThat(opp1.getName()).startsWith("TEST Opportunity");
    
    // Should have mock user
    assertThat(opp1.getAssignedTo()).isNotNull();
    assertThat(opp1.getAssignedTo().isTestData()).isTrue();
  }
  
  @Test
  void opportunityTestDataFactory_hasGoodDefaults() {
    // Given/When
    Opportunity opp = OpportunityTestDataFactory.builder().build();
    
    // Then
    assertThat(opp.getStage()).isEqualTo(OpportunityStage.NEW_LEAD);
    assertThat(opp.getExpectedValue()).isNotNull();
    assertThat(opp.getExpectedCloseDate()).isNotNull();
    assertThat(opp.getDescription()).contains("Test opportunity created for automated testing");
  }
  
  @Test
  void contactTestDataFactory_canOverrideDefaults() {
    // Given/When
    CustomerContact contact = ContactTestDataFactory.builder()
        .withFirstName("Max")
        .withLastName("Mustermann")
        .withEmail("max@custom.com")
        .asPrimary()
        .asDecisionMaker()
        .build();
    
    // Then
    assertThat(contact.getFirstName()).isEqualTo("Max");
    assertThat(contact.getLastName()).isEqualTo("Mustermann");
    assertThat(contact.getEmail()).isEqualTo("max@custom.com");
    assertThat(contact.getIsPrimary()).isTrue();
    assertThat(contact.getIsDecisionMaker()).isTrue();
  }
  
  @Test
  void allBuildersHaveBuildMethods() {
    // Test that all builders can build without errors
    Customer customer = CustomerTestDataFactory.builder().build();
    assertThat(customer).isNotNull();
    
    CustomerContact contact = ContactTestDataFactory.builder().build();
    assertThat(contact).isNotNull();
    
    Opportunity opportunity = OpportunityTestDataFactory.builder().build();
    assertThat(opportunity).isNotNull();
  }
}
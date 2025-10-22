package de.freshplan.domain.opportunity.service;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.entity.OpportunityType;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.domain.opportunity.service.exception.OpportunityNotFoundException;
import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.Territory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests für OpportunityService.handleOpportunityWon() - Auto-Conversion bei Opportunity WON.
 *
 * <p>Sprint 2.1.7.4 - Customer Status Architecture
 */
@QuarkusTest
class OpportunityServiceTest {

  @Inject OpportunityService opportunityService;

  @Inject OpportunityRepository opportunityRepository;

  @Inject CustomerRepository customerRepository;

  @Inject UserRepository userRepository;

  @Inject EntityManager em;

  private static final String TEST_USER = "admin-test";
  private User testUser;
  private Territory testTerritory;

  @BeforeEach
  @Transactional
  void setup() {
    // Clean test data - IMPORTANT: Delete in correct order (FK constraints!)
    em.createQuery("DELETE FROM OpportunityActivity").executeUpdate();
    em.createQuery("DELETE FROM Opportunity").executeUpdate();
    em.createQuery("DELETE FROM LeadContact").executeUpdate();
    em.createQuery("DELETE FROM LeadActivity").executeUpdate();
    em.createQuery("DELETE FROM Lead").executeUpdate();
    em.createQuery("DELETE FROM Territory").executeUpdate();

    // Create test territory (always fresh for each test)
    testTerritory = new Territory();
    testTerritory.id = "DE";
    testTerritory.name = "Deutschland";
    testTerritory.countryCode = "DE";
    testTerritory.currencyCode = "EUR";
    testTerritory.languageCode = "de-DE";
    testTerritory.taxRate = new BigDecimal("19.00");
    testTerritory.active = true;
    testTerritory.persist();

    // Ensure test user exists
    testUser =
        userRepository
            .findByUsername(TEST_USER)
            .orElseGet(
                () -> {
                  User user = new User(TEST_USER, "Admin", "Test", "admin-test@freshplan.de");
                  userRepository.persist(user);
                  return user;
                });
  }

  // ========== SPRINT 2.1.7.4: AUTO-CONVERSION TESTS ==========

  @Test
  @Transactional
  void shouldAutoConvertLeadToCustomerWhenOpportunityHasLead() {
    // Given: Opportunity mit Lead
    Lead lead = createTestLead("Test Restaurant GmbH", LeadStatus.QUALIFIED);
    lead.persist();

    Opportunity opportunity = createTestOpportunity("Neugeschäft Test Restaurant");
    opportunity.setLead(lead);
    opportunity.setOpportunityType(OpportunityType.NEUGESCHAEFT);
    opportunityRepository.persist(opportunity);

    // When: handleOpportunityWon() aufrufen
    Customer customer = opportunityService.handleOpportunityWon(opportunity.getId());

    // Then: Customer wurde erstellt
    assertNotNull(customer, "Customer should be created from Lead");
    assertNotNull(customer.getId(), "Customer ID should be set");
    assertEquals(
        "Test Restaurant GmbH", customer.getCompanyName(), "Company name should be copied");

    // Sprint 2.1.7.4: Status should be PROSPECT (not AKTIV!)
    assertEquals(
        CustomerStatus.PROSPECT,
        customer.getStatus(),
        "Customer should be PROSPECT - waiting for first order");

    // Verify Opportunity → Customer link
    Opportunity updatedOpp = opportunityRepository.findById(opportunity.getId());
    assertNotNull(updatedOpp, "Opportunity should still exist");
    assertNotNull(updatedOpp.getCustomer(), "Opportunity should be linked to Customer");
    assertEquals(customer.getId(), updatedOpp.getCustomer().getId(), "Customer FK should match");

    // Verify Lead FK cleared
    assertNull(updatedOpp.getLead(), "Opportunity.lead FK should be cleared after conversion");

    // Verify Lead is marked CONVERTED
    Lead updatedLead = Lead.findById(lead.id);
    assertNotNull(updatedLead, "Lead should still exist (audit trail)");
    assertEquals(LeadStatus.CONVERTED, updatedLead.status, "Lead should be marked CONVERTED");

    // Verify originalLeadId is stored in Customer
    assertEquals(
        lead.id, customer.getOriginalLeadId(), "Customer.originalLeadId should track Lead");
  }

  @Test
  @Transactional
  void shouldReturnNullWhenOpportunityHasNoLead() {
    // Given: Opportunity WITHOUT Lead (created directly for existing customer)
    Customer existingCustomer = new Customer();
    existingCustomer.setCustomerNumber("EXISTING-001");
    existingCustomer.setCompanyName("Existing Corp");
    existingCustomer.setStatus(CustomerStatus.AKTIV);
    existingCustomer.setCreatedBy(TEST_USER);
    existingCustomer.setCreatedAt(java.time.LocalDateTime.now());
    customerRepository.persist(existingCustomer);

    Opportunity opportunity = createTestOpportunity("Direct Customer Opportunity");
    opportunity.setCustomer(existingCustomer); // Has Customer, NO Lead
    opportunity.setOpportunityType(OpportunityType.SORTIMENTSERWEITERUNG);
    opportunityRepository.persist(opportunity);

    // When: handleOpportunityWon() aufrufen
    Customer customer = opportunityService.handleOpportunityWon(opportunity.getId());

    // Then: Kein Customer erstellt (return null)
    assertNull(customer, "Should return null when Opportunity has no Lead");

    // Verify Opportunity unchanged
    Opportunity updatedOpp = opportunityRepository.findById(opportunity.getId());
    assertNotNull(updatedOpp, "Opportunity should still exist");
    assertNotNull(updatedOpp.getCustomer(), "Opportunity.customer should still be set");
    assertEquals(
        existingCustomer.getId(),
        updatedOpp.getCustomer().getId(),
        "Opportunity.customer should be unchanged");
    assertNull(updatedOpp.getLead(), "Opportunity.lead should still be null");
  }

  @Test
  @Transactional
  void shouldThrowOpportunityNotFoundExceptionWhenOpportunityDoesNotExist() {
    // Given: Invalid opportunity ID
    UUID invalidId = UUID.randomUUID();

    // When/Then: Should throw OpportunityNotFoundException
    assertThrows(
        OpportunityNotFoundException.class,
        () -> opportunityService.handleOpportunityWon(invalidId),
        "Should throw OpportunityNotFoundException for invalid ID");
  }

  @Test
  @Transactional
  void shouldCopyAllLeadFieldsToCustomerDuringAutoConversion() {
    // Given: Lead with ALL business fields populated (100% Lead Parity)
    Lead lead = createTestLead("Test Chain Restaurant", LeadStatus.QUALIFIED);
    lead.businessType = de.freshplan.domain.shared.BusinessType.RESTAURANT;
    lead.kitchenSize = de.freshplan.domain.shared.KitchenSize.GROSS;
    lead.employeeCount = 30;
    lead.branchCount = 5;
    lead.isChain = true;
    lead.estimatedVolume = new BigDecimal("75000.00");
    lead.painStaffShortage = true;
    lead.painHighCosts = true;
    lead.painNotes = "Critical: Staff shortage";
    lead.persist();

    Opportunity opportunity = createTestOpportunity("Neugeschäft Test Chain");
    opportunity.setLead(lead);
    opportunity.setOpportunityType(OpportunityType.NEUGESCHAEFT);
    opportunityRepository.persist(opportunity);

    // When: handleOpportunityWon() aufrufen
    Customer customer = opportunityService.handleOpportunityWon(opportunity.getId());

    // Then: ALL fields must be copied (100% Lead Parity)
    assertNotNull(customer, "Customer should be created");

    // Classification fields
    assertEquals(
        de.freshplan.domain.shared.BusinessType.RESTAURANT,
        customer.getBusinessType(),
        "businessType must be copied");
    assertEquals(
        de.freshplan.domain.shared.KitchenSize.GROSS,
        customer.getKitchenSize(),
        "kitchenSize must be copied");
    assertEquals(30, customer.getEmployeeCount(), "employeeCount must be copied");

    // Chain/Branch fields
    assertEquals(5, customer.getBranchCount(), "branchCount must be copied");
    assertEquals(true, customer.getIsChain(), "isChain must be copied");
    assertEquals(5, customer.getTotalLocationsEU(), "totalLocationsEU must sync with branchCount");

    // Volume fields
    assertEquals(
        new BigDecimal("75000.00"),
        customer.getEstimatedVolume(),
        "estimatedVolume must be copied");
    assertEquals(
        new BigDecimal("75000.00"),
        customer.getExpectedAnnualVolume(),
        "expectedAnnualVolume must sync with estimatedVolume");

    // Pain Scoring fields
    assertEquals(true, customer.getPainStaffShortage(), "painStaffShortage must be copied");
    assertEquals(true, customer.getPainHighCosts(), "painHighCosts must be copied");
    assertEquals("Critical: Staff shortage", customer.getPainNotes(), "painNotes must be copied");
  }

  @Test
  @Transactional
  void shouldLinkOpportunityToCustomerAndClearLeadFK() {
    // Given: Opportunity mit Lead
    Lead lead = createTestLead("Test Restaurant Link", LeadStatus.QUALIFIED);
    lead.persist();

    Opportunity opportunity = createTestOpportunity("Link Test Opportunity");
    opportunity.setLead(lead);
    opportunity.setOpportunityType(OpportunityType.NEUGESCHAEFT);
    opportunityRepository.persist(opportunity);

    // Verify precondition: Opportunity has Lead, no Customer
    assertNotNull(opportunity.getLead(), "Opportunity should start with Lead");
    assertNull(opportunity.getCustomer(), "Opportunity should start with no Customer");

    // When: handleOpportunityWon() aufrufen
    Customer customer = opportunityService.handleOpportunityWon(opportunity.getId());

    // Then: Opportunity → Customer link updated
    Opportunity updatedOpp = opportunityRepository.findById(opportunity.getId());

    assertEquals(
        customer.getId(),
        updatedOpp.getCustomer().getId(),
        "Opportunity.customer should be set to new Customer");
    assertNull(
        updatedOpp.getLead(), "Opportunity.lead should be null (FK cleared after conversion)");

    // Verify Customer can be loaded via relationship
    assertNotNull(updatedOpp.getCustomer(), "Customer relationship should be navigable");
    assertEquals(
        "Test Restaurant Link",
        updatedOpp.getCustomer().getCompanyName(),
        "Customer relationship should load correct entity");
  }

  @Test
  @Transactional
  void shouldCreateCustomerWithProspectStatus() {
    // Given: Opportunity mit Lead
    Lead lead = createTestLead("Test Restaurant Status", LeadStatus.QUALIFIED);
    lead.persist();

    Opportunity opportunity = createTestOpportunity("Status Test Opportunity");
    opportunity.setLead(lead);
    opportunity.setOpportunityType(OpportunityType.NEUGESCHAEFT);
    opportunityRepository.persist(opportunity);

    // When: handleOpportunityWon() aufrufen
    Customer customer = opportunityService.handleOpportunityWon(opportunity.getId());

    // Then: Customer Status = PROSPECT (Sprint 2.1.7.4)
    assertNotNull(customer, "Customer should be created");
    assertEquals(
        CustomerStatus.PROSPECT,
        customer.getStatus(),
        "Customer should be PROSPECT - waiting for first order delivery");

    // Customer should NOT be AKTIV yet (requires first order DELIVERED)
    assertNotEquals(
        CustomerStatus.AKTIV,
        customer.getStatus(),
        "Customer should NOT be AKTIV until first order delivered");
  }

  @Test
  @Transactional
  void shouldHandleAutoConversionWithMinimalLeadData() {
    // Given: Lead with minimal data (nur Pflichtfelder)
    Lead lead = createTestLead("Minimal Restaurant", LeadStatus.QUALIFIED);
    // Keine business fields gesetzt (alles NULL)
    lead.persist();

    Opportunity opportunity = createTestOpportunity("Minimal Data Opportunity");
    opportunity.setLead(lead);
    opportunity.setOpportunityType(OpportunityType.NEUGESCHAEFT);
    opportunityRepository.persist(opportunity);

    // When: handleOpportunityWon() aufrufen
    Customer customer = opportunityService.handleOpportunityWon(opportunity.getId());

    // Then: Conversion should succeed (no NPE)
    assertNotNull(customer, "Customer should be created even with minimal Lead data");
    assertEquals("Minimal Restaurant", customer.getCompanyName(), "Company name should be copied");
    assertEquals(
        CustomerStatus.PROSPECT, customer.getStatus(), "Customer should be PROSPECT status");

    // Optional fields should be NULL
    assertNull(customer.getBusinessType(), "NULL businessType should be preserved");
    assertNull(customer.getKitchenSize(), "NULL kitchenSize should be preserved");
    assertNull(customer.getEmployeeCount(), "NULL employeeCount should be preserved");
  }

  // ========== HELPER METHODS ==========

  private Lead createTestLead(String companyName, LeadStatus status) {
    Lead lead = new Lead();
    lead.companyName = companyName;
    lead.city = "München";
    lead.territory = testTerritory;
    lead.ownerUserId = TEST_USER;
    lead.createdBy = TEST_USER;
    lead.updatedBy = TEST_USER;
    lead.status = status;
    lead.registeredAt = LocalDateTime.now();
    return lead;
  }

  private Opportunity createTestOpportunity(String name) {
    Opportunity opportunity = new Opportunity();
    opportunity.setName(name);
    opportunity.setStage(OpportunityStage.CLOSED_WON);
    opportunity.setAssignedTo(testUser);
    opportunity.setExpectedValue(new BigDecimal("50000.00"));
    opportunity.setExpectedCloseDate(LocalDate.now());
    opportunity.setProbability(100);
    return opportunity;
  }
}

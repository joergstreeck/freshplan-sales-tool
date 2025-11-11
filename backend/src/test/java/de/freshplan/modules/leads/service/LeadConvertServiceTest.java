package de.freshplan.modules.leads.service;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.modules.leads.api.admin.dto.LeadConvertRequest;
import de.freshplan.modules.leads.api.admin.dto.LeadConvertResponse;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.Territory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

/**
 * Tests für Lead → Customer Conversion Service.
 *
 * <p>Sprint 2.1.6 - User Story 2
 */
@QuarkusTest
class LeadConvertServiceTest {

  @Inject LeadConvertService convertService;

  @Inject jakarta.persistence.EntityManager em;

  @Inject CustomerRepository customerRepository;

  private static final String TEST_USER = "admin-test";
  private Lead testLead;

  @Inject EntityManager em;

  @BeforeEach
  @Transactional
  void setup() {
    // Clean test data - IMPORTANT: Delete in correct order (FK constraints!)
    // Sprint 2.1.7.4: DELETE Opportunities FIRST (FK to leads + chk_opportunity_has_source)
    em.createQuery("DELETE FROM Opportunity").executeUpdate();
    em.createQuery("DELETE FROM LeadContact").executeUpdate();
    em.createQuery("DELETE FROM LeadActivity").executeUpdate();
    em.createQuery("DELETE FROM Lead").executeUpdate();

    // Ensure territory exists
    Territory territory = Territory.findByCode("DE");
    if (territory == null) {
      territory = Territory.getDefault();
      if (territory.id == null) {
        territory.persist();
      }
    }

    // Create test lead
    testLead = new Lead();
    testLead.companyName = "Test Restaurant GmbH";
    testLead.city = "München";
    testLead.territory = territory;
    testLead.ownerUserId = TEST_USER;
    testLead.createdBy = TEST_USER;
    testLead.updatedBy = TEST_USER;
    testLead.status = LeadStatus.QUALIFIED;
    testLead.registeredAt =
        LocalDateTime.now()
            .minusSeconds(
                1); // Fix: 1s buffer for DB check constraint (chk_leads_registered_at_not_future)
    testLead.persist();
  }

    @AfterEach
  @Transactional
  void cleanup() {
    // Delete test data using pattern matching
    em.createNativeQuery("DELETE FROM opportunity_activities WHERE opportunity_id IN (SELECT id FROM opportunities WHERE test_marker LIKE 'TEST-%')").executeUpdate();
    em.createNativeQuery("DELETE FROM opportunities WHERE test_marker LIKE 'TEST-%'").executeUpdate();
    em.createNativeQuery("DELETE FROM customer_contacts WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')").executeUpdate();
    em.createNativeQuery("DELETE FROM customer_timeline_events WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')").executeUpdate();
    em.createNativeQuery("DELETE FROM customers WHERE customer_number LIKE 'TEST-%'").executeUpdate();
  }

  @Test
  @Transactional
  void shouldConvertLeadToCustomerSuccessfully() {
    // Given
    LeadConvertRequest request = new LeadConvertRequest();
    request.keepLeadRecord = true;

    // When
    LeadConvertResponse response =
        convertService.convertToCustomer(testLead.id, request, TEST_USER);

    // Then
    assertNotNull(response);
    assertEquals(testLead.id, response.leadId);
    assertNotNull(response.customerId);
    assertNotNull(response.customerNumber);
    assertNotNull(response.convertedAt);

    // Verify Customer was created
    Customer customer = customerRepository.findById(response.customerId);
    assertNotNull(customer);
    assertEquals(response.customerNumber, customer.getCustomerNumber());
    assertEquals("Test Restaurant GmbH", customer.getCompanyName());

    // Sprint 2.1.7.4: Status should be PROSPECT (not AKTIV!)
    assertEquals(
        CustomerStatus.PROSPECT,
        customer.getStatus(),
        "Customer should be PROSPECT - waiting for first order");

    assertEquals(testLead.id, customer.getOriginalLeadId()); // V261: Lead → Customer tracking

    // Verify Lead was marked as CONVERTED
    Lead updatedLead = Lead.findById(testLead.id);
    assertNotNull(updatedLead);
    assertEquals(LeadStatus.CONVERTED, updatedLead.status);
  }

  @Test
  @Transactional
  void shouldDeleteLeadWhenKeepLeadRecordIsFalse() {
    // Given
    LeadConvertRequest request = new LeadConvertRequest();
    request.keepLeadRecord = false;

    // When
    LeadConvertResponse response =
        convertService.convertToCustomer(testLead.id, request, TEST_USER);

    // Then
    assertNotNull(response.customerId);

    // Verify Lead was ARCHIVED (not deleted) - Fix #7 Sprint 2.1.6 Phase 2
    // Lead should always be retained for audit trail (status=CONVERTED)
    Lead archivedLead = Lead.findById(testLead.id);
    assertNotNull(archivedLead, "Lead should be archived, not deleted");
    assertEquals(LeadStatus.CONVERTED, archivedLead.status);
  }

  @Test
  @Transactional
  void shouldUseProvidedCustomerNumber() {
    // Given
    String uniqueNumber = "CUSTOM-" + testLead.id; // Unique per test run
    LeadConvertRequest request = new LeadConvertRequest();
    request.customerNumber = uniqueNumber;
    request.keepLeadRecord = true;

    // When
    LeadConvertResponse response =
        convertService.convertToCustomer(testLead.id, request, TEST_USER);

    // Then
    assertEquals(uniqueNumber, response.customerNumber);

    Customer customer = customerRepository.findByCustomerNumber(uniqueNumber).orElseThrow();
    assertEquals(uniqueNumber, customer.getCustomerNumber());
  }

  @Test
  @Transactional
  void shouldRejectAlreadyConvertedLead() {
    // Given
    testLead.status = LeadStatus.CONVERTED;
    Lead.getEntityManager().merge(testLead);

    LeadConvertRequest request = new LeadConvertRequest();

    // When/Then
    assertThrows(
        IllegalStateException.class,
        () -> convertService.convertToCustomer(testLead.id, request, TEST_USER));
  }

  @Test
  @Transactional
  void shouldThrowNotFoundForInvalidLead() {
    // Given
    LeadConvertRequest request = new LeadConvertRequest();

    // When/Then
    assertThrows(
        NotFoundException.class,
        () -> convertService.convertToCustomer(99999L, request, TEST_USER));
  }

  @Test
  @Transactional
  void shouldRejectDuplicateCustomerNumber() {
    // Given: Create existing customer
    Customer existingCustomer = new Customer();
    existingCustomer.setCustomerNumber("EXISTING-123");
    existingCustomer.setCompanyName("Existing Corp");
    existingCustomer.setStatus(CustomerStatus.AKTIV);
    existingCustomer.setCreatedBy(TEST_USER);
    existingCustomer.setCreatedAt(java.time.LocalDateTime.now());
    customerRepository.persist(existingCustomer);

    LeadConvertRequest request = new LeadConvertRequest();
    request.customerNumber = "EXISTING-123";

    // When/Then
    assertThrows(
        IllegalArgumentException.class,
        () -> convertService.convertToCustomer(testLead.id, request, TEST_USER));
  }

  // ========== SPRINT 2.1.7.4: 100% LEAD PARITY TESTS ==========

  @Test
  @Transactional
  void shouldCopyAllBusinessFieldsFromLead() {
    // Given: Lead with ALL business fields populated
    testLead.businessType = de.freshplan.domain.shared.BusinessType.RESTAURANT;
    testLead.kitchenSize = de.freshplan.domain.shared.KitchenSize.GROSS;
    testLead.employeeCount = 25;
    testLead.branchCount = 3;
    testLead.isChain = true;
    testLead.estimatedVolume = new java.math.BigDecimal("50000.00");
    Lead.getEntityManager().merge(testLead);

    LeadConvertRequest request = new LeadConvertRequest();
    request.keepLeadRecord = true;

    // When
    LeadConvertResponse response =
        convertService.convertToCustomer(testLead.id, request, TEST_USER);

    // Then: ALL fields must be copied
    Customer customer = customerRepository.findById(response.customerId);
    assertNotNull(customer);

    // Classification fields (V10032)
    assertEquals(
        de.freshplan.domain.shared.BusinessType.RESTAURANT,
        customer.getBusinessType(),
        "businessType must be copied from Lead");
    assertEquals(
        de.freshplan.domain.shared.KitchenSize.GROSS,
        customer.getKitchenSize(),
        "kitchenSize must be copied from Lead");
    assertEquals(25, customer.getEmployeeCount(), "employeeCount must be copied from Lead");

    // Chain/Branch fields (V10032)
    assertEquals(3, customer.getBranchCount(), "branchCount must be copied from Lead");
    assertEquals(true, customer.getIsChain(), "isChain must be copied from Lead");
    assertEquals(3, customer.getTotalLocationsEU(), "totalLocationsEU must sync with branchCount");

    // Volume fields (V10032)
    assertEquals(
        new java.math.BigDecimal("50000.00"),
        customer.getEstimatedVolume(),
        "estimatedVolume must be copied from Lead");
    assertEquals(
        new java.math.BigDecimal("50000.00"),
        customer.getExpectedAnnualVolume(),
        "expectedAnnualVolume must sync with estimatedVolume");
  }

  @Test
  @Transactional
  void shouldCopyAllPainScoringFieldsFromLead() {
    // Given: Lead with ALL pain scoring fields populated
    testLead.painStaffShortage = true;
    testLead.painHighCosts = true;
    testLead.painFoodWaste = false;
    testLead.painQualityInconsistency = true;
    testLead.painTimePressure = false;
    testLead.painSupplierQuality = true;
    testLead.painUnreliableDelivery = true;
    testLead.painPoorService = false;
    testLead.painNotes = "Critical: Staff shortage + unreliable delivery";
    Lead.getEntityManager().merge(testLead);

    LeadConvertRequest request = new LeadConvertRequest();
    request.keepLeadRecord = true;

    // When
    LeadConvertResponse response =
        convertService.convertToCustomer(testLead.id, request, TEST_USER);

    // Then: ALL pain fields must be copied
    Customer customer = customerRepository.findById(response.customerId);
    assertNotNull(customer);

    // Pain Scoring System V3 (8 Boolean fields + notes)
    assertEquals(true, customer.getPainStaffShortage(), "painStaffShortage must be copied");
    assertEquals(true, customer.getPainHighCosts(), "painHighCosts must be copied");
    assertEquals(false, customer.getPainFoodWaste(), "painFoodWaste must be copied");
    assertEquals(
        true, customer.getPainQualityInconsistency(), "painQualityInconsistency must be copied");
    assertEquals(false, customer.getPainTimePressure(), "painTimePressure must be copied");
    assertEquals(true, customer.getPainSupplierQuality(), "painSupplierQuality must be copied");
    assertEquals(
        true, customer.getPainUnreliableDelivery(), "painUnreliableDelivery must be copied");
    assertEquals(false, customer.getPainPoorService(), "painPoorService must be copied");
    assertEquals(
        "Critical: Staff shortage + unreliable delivery",
        customer.getPainNotes(),
        "painNotes must be copied");
  }

  @Test
  @Transactional
  void shouldHandleNullBusinessFieldsGracefully() {
    // Given: Lead with NULL business fields (minimal Lead)
    testLead.businessType = null;
    testLead.kitchenSize = null;
    testLead.employeeCount = null;
    testLead.estimatedVolume = null;
    Lead.getEntityManager().merge(testLead);

    LeadConvertRequest request = new LeadConvertRequest();
    request.keepLeadRecord = true;

    // When
    LeadConvertResponse response =
        convertService.convertToCustomer(testLead.id, request, TEST_USER);

    // Then: Should succeed (no NPE)
    assertNotNull(response);
    assertNotNull(response.customerId);

    // Verify Customer was created with NULL fields
    Customer customer = customerRepository.findById(response.customerId);
    assertNotNull(customer);
    assertNull(customer.getBusinessType(), "NULL businessType should be preserved");
    assertNull(customer.getKitchenSize(), "NULL kitchenSize should be preserved");
    assertNull(customer.getEmployeeCount(), "NULL employeeCount should be preserved");
    assertNull(customer.getEstimatedVolume(), "NULL estimatedVolume should be preserved");
  }
}

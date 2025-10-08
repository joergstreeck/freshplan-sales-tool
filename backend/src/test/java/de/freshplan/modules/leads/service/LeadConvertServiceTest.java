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
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests für Lead → Customer Conversion Service.
 *
 * <p>Sprint 2.1.6 - User Story 2
 */
@QuarkusTest
class LeadConvertServiceTest {

  @Inject LeadConvertService convertService;

  @Inject CustomerRepository customerRepository;

  private static final String TEST_USER = "admin-test";
  private Lead testLead;

  @BeforeEach
  @Transactional
  void setup() {
    // Clean test data (only Leads - Customers have FK constraints)
    Lead.deleteAll();

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
    testLead.registeredAt = LocalDateTime.now(); // Variante B: IMMER gesetzt
    testLead.persist();
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
    assertEquals(CustomerStatus.AKTIV, customer.getStatus());
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
}

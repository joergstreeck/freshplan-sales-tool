package de.freshplan.modules.leads.domain;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.shared.BusinessType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.*;

/**
 * Integration tests for LeadContact entity - 100% PARITY mit Contact.java tests.
 *
 * <p>Tests database constraints, JPA relationships, business methods.
 *
 * <p><b>Testing Strategy:</b> Integration Tests (Panache entities cannot be mocked)
 *
 * <p><b>Sprint:</b> 2.1.6 Phase 5+ - Lead Contacts Refactoring (ADR-007 Option C)
 */
@QuarkusTest
@Tag("integration")
class LeadContactTest {

  @Inject EntityManager em;

  @BeforeEach
  @Transactional
  void setup() {
    // Cleanup for test isolation (order matters: FK constraints!)
    em.createQuery("DELETE FROM LeadContact").executeUpdate();
    em.createQuery("DELETE FROM LeadActivity").executeUpdate(); // FK to leads
    em.createQuery("DELETE FROM Lead").executeUpdate();
    em.createQuery("DELETE FROM Territory").executeUpdate();
  }

  /** Creates test territory for lead creation. */
  @Transactional
  Territory createTestTerritory() {
    Territory territory = new Territory();
    territory.id = "DE";
    territory.name = "Deutschland";
    territory.countryCode = "DE";
    territory.currencyCode = "EUR";
    territory.languageCode = "de-DE";
    territory.taxRate = new BigDecimal("19.00");
    territory.active = true;
    territory.persist();
    return territory;
  }

  /** Creates minimal test lead for contact tests. */
  @Transactional
  Lead createTestLead(String userId) {
    Territory territory = createTestTerritory();

    Lead lead = new Lead();
    lead.companyName = "Test Restaurant GmbH";
    lead.contactPerson = "Max Mustermann"; // Legacy field (deprecated)
    lead.email = "max@restaurant.de"; // Legacy field (deprecated)
    lead.phone = "+49 123 456789"; // Legacy field (deprecated)
    lead.city = "Berlin";
    lead.postalCode = "10115";
    lead.countryCode = "DE";
    lead.territory = territory;
    lead.status = LeadStatus.REGISTERED;
    lead.ownerUserId = userId;
    lead.createdBy = userId;
    lead.registeredAt = LocalDateTime.now();
    lead.protectionStartAt = LocalDateTime.now();
    lead.persist();

    return lead;
  }

  // ===========================
  // 1. CRUD Operations
  // ===========================

  @Test
  @Transactional
  @DisplayName("Should create lead contact with all required fields")
  void testCreateLeadContact() {
    Lead lead = createTestLead("user1");

    LeadContact contact = new LeadContact();
    contact.setLead(lead);
    contact.setFirstName("Maria");
    contact.setLastName("Schmidt");
    contact.setEmail("maria@restaurant.de");
    contact.setPrimary(true);
    contact.setCreatedBy("user1");
    contact.persist();

    em.flush();
    em.clear();

    LeadContact fetched = LeadContact.findById(contact.getId());
    assertNotNull(fetched);
    assertEquals("Maria", fetched.getFirstName());
    assertEquals("Schmidt", fetched.getLastName());
    assertEquals("maria@restaurant.de", fetched.getEmail());
    assertTrue(fetched.isPrimary());
    assertNotNull(fetched.getCreatedAt());
    assertNotNull(fetched.getUpdatedAt());
  }

  @Test
  @Transactional
  @DisplayName("Should fail when firstName is missing (NOT NULL constraint)")
  void testFirstNameRequired() {
    Lead lead = createTestLead("user1");

    LeadContact contact = new LeadContact();
    contact.setLead(lead);
    // Missing firstName!
    contact.setLastName("Schmidt");
    contact.setEmail("maria@restaurant.de");

    assertThrows(
        Exception.class,
        () -> {
          contact.persist();
          em.flush();
        },
        "Should fail due to NOT NULL constraint on first_name");
  }

  @Test
  @Transactional
  @DisplayName("Should fail when email AND phone AND mobile are all NULL (CHECK constraint)")
  void testAtLeastOneContactMethodRequired() {
    Lead lead = createTestLead("user1");

    LeadContact contact = new LeadContact();
    contact.setLead(lead);
    contact.setFirstName("Maria");
    contact.setLastName("Schmidt");
    // All contact methods NULL!

    assertThrows(
        Exception.class,
        () -> {
          contact.persist();
          em.flush();
        },
        "Should fail due to CHECK constraint (email OR phone OR mobile required)");
  }

  @Test
  @Transactional
  @DisplayName("Should allow phone-only contact (no email)")
  void testPhoneOnlyContact() {
    Lead lead = createTestLead("user1");

    LeadContact contact = new LeadContact();
    contact.setLead(lead);
    contact.setFirstName("Karl");
    contact.setLastName("Meier");
    contact.setPhone("+49 30 12345678");
    // Email is NULL - phone is sufficient
    contact.persist();

    em.flush();
    em.clear();

    LeadContact fetched = LeadContact.findById(contact.getId());
    assertNotNull(fetched);
    assertEquals("+49 30 12345678", fetched.getPhone());
    assertNull(fetched.getEmail());
  }

  // ===========================
  // 2. Primary Contact Logic + Backward Compatibility Trigger
  // ===========================

  @Test
  @Transactional
  @DisplayName(
      "Should sync primary contact to leads.contact_person via trigger (Backward Compatibility)")
  void testPrimaryContactSyncToLead() {
    Lead lead = createTestLead("user1");

    LeadContact contact = new LeadContact();
    contact.setLead(lead);
    contact.setFirstName("Maria");
    contact.setLastName("Schmidt");
    contact.setEmail("maria.schmidt@restaurant.de");
    contact.setPhone("+49 30 98765432");
    contact.setPrimary(true); // Mark as primary
    contact.persist();

    em.flush();
    em.clear();

    // V276 trigger should have updated leads.contact_person
    Lead fetchedLead = Lead.findById(lead.id);
    assertNotNull(fetchedLead);
    assertEquals("Maria Schmidt", fetchedLead.contactPerson, "Trigger should sync full name");
    assertEquals(
        "maria.schmidt@restaurant.de", fetchedLead.email, "Trigger should sync email");
    assertEquals("+49 30 98765432", fetchedLead.phone, "Trigger should sync phone");
  }

  @Test
  @Transactional
  @DisplayName("Should enforce UNIQUE constraint: only 1 primary contact per lead allowed")
  void testOnlyOnePrimaryContactPerLead() {
    Lead lead = createTestLead("user1");

    // Create first primary contact
    LeadContact contact1 = new LeadContact();
    contact1.setLead(lead);
    contact1.setFirstName("Maria");
    contact1.setLastName("Schmidt");
    contact1.setEmail("maria@restaurant.de");
    contact1.setPrimary(true);
    contact1.persist();

    em.flush();

    // Try to create second primary contact for same lead
    LeadContact contact2 = new LeadContact();
    contact2.setLead(lead);
    contact2.setFirstName("Karl");
    contact2.setLastName("Meier");
    contact2.setEmail("karl@restaurant.de");
    contact2.setPrimary(true); // Should fail!

    assertThrows(
        Exception.class,
        () -> {
          contact2.persist();
          em.flush();
        },
        "Should fail due to UNIQUE constraint on (lead_id, is_primary)");
  }

  // ===========================
  // 3. CRM Intelligence Fields (100% Parity with Contact.java)
  // ===========================

  @Test
  @Transactional
  @DisplayName("Should store relationship data (birthday, hobbies, family status)")
  void testRelationshipData() {
    Lead lead = createTestLead("user1");

    LeadContact contact = new LeadContact();
    contact.setLead(lead);
    contact.setFirstName("Thomas");
    contact.setLastName("Becker");
    contact.setEmail("thomas@restaurant.de");

    // Relationship Data
    contact.setBirthday(LocalDate.of(1980, 5, 15));
    contact.setHobbies("Golf, Wandern, Kochen");
    contact.setFamilyStatus("married");
    contact.setChildrenCount(2);
    contact.setPersonalNotes("Spricht fließend Französisch. Liebt Bordeaux-Weine.");
    contact.persist();

    em.flush();
    em.clear();

    LeadContact fetched = LeadContact.findById(contact.getId());
    assertNotNull(fetched);
    assertEquals(LocalDate.of(1980, 5, 15), fetched.getBirthday());
    assertEquals("Golf, Wandern, Kochen", fetched.getHobbies());
    assertEquals("married", fetched.getFamilyStatus());
    assertEquals(2, fetched.getChildrenCount());
    assertTrue(fetched.getPersonalNotes().contains("Bordeaux"));
  }

  @Test
  @Transactional
  @DisplayName("Should store intelligence data (warmth score, confidence, interaction count)")
  void testIntelligenceData() {
    Lead lead = createTestLead("user1");

    LeadContact contact = new LeadContact();
    contact.setLead(lead);
    contact.setFirstName("Anna");
    contact.setLastName("Fischer");
    contact.setEmail("anna@restaurant.de");

    // Intelligence Data
    contact.setWarmthScore(85); // Hot lead!
    contact.setWarmthConfidence(90); // High confidence
    contact.setLastInteractionDate(LocalDateTime.now().minusDays(3));
    contact.setInteractionCount(12);
    contact.persist();

    em.flush();
    em.clear();

    LeadContact fetched = LeadContact.findById(contact.getId());
    assertNotNull(fetched);
    assertEquals(85, fetched.getWarmthScore());
    assertEquals(90, fetched.getWarmthConfidence());
    assertEquals(12, fetched.getInteractionCount());
    assertNotNull(fetched.getLastInteractionDate());
  }

  @Test
  @Transactional
  @DisplayName("Should store data quality score and recommendations")
  void testDataQuality() {
    Lead lead = createTestLead("user1");

    LeadContact contact = new LeadContact();
    contact.setLead(lead);
    contact.setFirstName("Peter");
    contact.setLastName("Müller");
    contact.setEmail("peter@restaurant.de");

    // Data Quality
    contact.setDataQualityScore(75);
    contact.setDataQualityRecommendations(
        "Mobile fehlt;Position nicht angegeben;Geburtsdatum nachtragen");
    contact.persist();

    em.flush();
    em.clear();

    LeadContact fetched = LeadContact.findById(contact.getId());
    assertNotNull(fetched);
    assertEquals(75, fetched.getDataQualityScore());
    assertTrue(fetched.getDataQualityRecommendations().contains("Mobile fehlt"));
  }

  // ===========================
  // 4. Business Methods (getFullName, getDisplayName)
  // ===========================

  @Test
  @DisplayName("getFullName() should return first + last name with title")
  void testGetFullName() {
    LeadContact contact = new LeadContact();
    contact.setTitle("Dr.");
    contact.setFirstName("Maria");
    contact.setLastName("Schmidt");

    assertEquals("Dr. Maria Schmidt", contact.getFullName());
  }

  @Test
  @DisplayName("getFullName() should work without title")
  void testGetFullNameWithoutTitle() {
    LeadContact contact = new LeadContact();
    contact.setFirstName("Karl");
    contact.setLastName("Meier");

    assertEquals("Karl Meier", contact.getFullName());
  }

  @Test
  @DisplayName("getDisplayName() should return salutation + full name")
  void testGetDisplayName() {
    LeadContact contact = new LeadContact();
    contact.setSalutation("herr");
    contact.setTitle("Dr.");
    contact.setFirstName("Thomas");
    contact.setLastName("Becker");

    assertEquals("Herr Dr. Thomas Becker", contact.getDisplayName());
  }

  @Test
  @DisplayName("getDisplayName() should work with frau salutation")
  void testGetDisplayNameFrau() {
    LeadContact contact = new LeadContact();
    contact.setSalutation("frau");
    contact.setFirstName("Maria");
    contact.setLastName("Schmidt");

    assertEquals("Frau Maria Schmidt", contact.getDisplayName());
  }

  @Test
  @DisplayName("getDisplayName() should omit divers salutation")
  void testGetDisplayNameDivers() {
    LeadContact contact = new LeadContact();
    contact.setSalutation("divers");
    contact.setFirstName("Alex");
    contact.setLastName("Müller");

    // "divers" should be omitted in display name
    assertEquals("Alex Müller", contact.getDisplayName());
  }

  // ===========================
  // 5. Builder Pattern
  // ===========================

  @Test
  @DisplayName("Builder pattern should work for LeadContact creation")
  void testBuilder() {
    Lead lead = new Lead();

    LeadContact contact =
        LeadContact.builder()
            .lead(lead)
            .firstName("Anna")
            .lastName("Fischer")
            .email("anna@test.de")
            .position("Geschäftsführerin")
            .isPrimary(true)
            .build();

    assertEquals("Anna", contact.getFirstName());
    assertEquals("Fischer", contact.getLastName());
    assertEquals("anna@test.de", contact.getEmail());
    assertEquals("Geschäftsführerin", contact.getPosition());
    assertTrue(contact.isPrimary());
  }

  // ===========================
  // 6. Cascade DELETE (orphanRemoval)
  // ===========================

  @Test
  @Transactional
  @DisplayName("Should delete all contacts when lead is deleted (CASCADE)")
  void testCascadeDelete() {
    Lead lead = createTestLead("user1");

    LeadContact contact1 = new LeadContact();
    contact1.setLead(lead);
    contact1.setFirstName("Maria");
    contact1.setLastName("Schmidt");
    contact1.setEmail("maria@restaurant.de");
    contact1.setPrimary(true);
    contact1.persist();

    LeadContact contact2 = new LeadContact();
    contact2.setLead(lead);
    contact2.setFirstName("Karl");
    contact2.setLastName("Meier");
    contact2.setEmail("karl@restaurant.de");
    contact2.setPrimary(false);
    contact2.persist();

    em.flush();
    em.clear();

    // Re-load lead to get managed instance
    Lead managedLead = Lead.findById(lead.id);
    assertNotNull(managedLead);
    assertEquals(2, managedLead.contacts.size(), "Lead should have 2 contacts");

    Long leadId = managedLead.id;

    // Delete lead (CASCADE should delete contacts)
    managedLead.delete();
    em.flush();
    em.clear();

    // Contacts should be deleted via CASCADE
    assertEquals(0, LeadContact.count(), "All contacts should be deleted via CASCADE");
    assertNull(Lead.findById(leadId), "Lead should be deleted");
  }
}

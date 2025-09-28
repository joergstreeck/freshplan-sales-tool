package de.freshplan.modules.leads.service;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.Territory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit tests for LeadNormalizationService. Sprint 2.1.4: Lead Deduplication & Data Quality */
@QuarkusTest
class LeadNormalizationServiceTest {

  @Inject LeadNormalizationService normalizationService;

  @Inject EntityManager entityManager;

  private Territory defaultTerritory;

  @BeforeEach
  @Transactional
  void clearDatabase() {
    // Clean up any existing test data
    entityManager.createQuery("DELETE FROM Lead").executeUpdate();

    // Create or find default territory for tests
    defaultTerritory =
        entityManager
            .createQuery("SELECT t FROM Territory t WHERE t.id = :id", Territory.class)
            .setParameter("id", "DE")
            .getResultStream()
            .findFirst()
            .orElseGet(
                () -> {
                  Territory t = new Territory();
                  t.id = "DE";
                  t.name = "Deutschland";
                  t.countryCode = "DE";
                  t.currencyCode = "EUR";
                  t.taxRate = new java.math.BigDecimal("19.0");
                  t.languageCode = "de";
                  entityManager.persist(t);
                  return t;
                });
  }

  // ========== Email Normalization Tests ==========

  @ParameterizedTest
  @CsvSource({
    "' JoHn.Doe@EXAMPLE.COM ', john.doe@example.com",
    "'ADMIN@FRESHFOODZ.DE', admin@freshfoodz.de",
    "'test+tag@gmail.com', test+tag@gmail.com",
    "'  spaces@domain.com  ', spaces@domain.com"
  })
  void shouldNormalizeEmail(String input, String expected) {
    String result = normalizationService.normalizeEmail(input);
    assertEquals(expected, result);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  ", "\t", "\n"})
  void shouldReturnNullForInvalidEmail(String input) {
    assertNull(normalizationService.normalizeEmail(input));
  }

  // ========== Company Name Normalization Tests ==========

  @ParameterizedTest
  @CsvSource({
    "'Hotel Beispiel GmbH', hotel beispiel",
    "'Restaurant Muster AG', restaurant muster",
    "'Café Schmidt e.K.', café schmidt",
    "'Test & Co. KG', test & co.",
    "'  UPPERCASE COMPANY  ', uppercase company"
  })
  void shouldNormalizeCompanyName(String input, String expected) {
    String result = normalizationService.normalizeCompanyName(input);
    assertEquals(expected, result);
  }

  @ParameterizedTest
  @NullAndEmptySource
  void shouldReturnNullForInvalidCompanyName(String input) {
    assertNull(normalizationService.normalizeCompanyName(input));
  }

  // ========== Phone Normalization Tests ==========

  @ParameterizedTest
  @CsvSource({
    "'030 123456', '+4930123456'",
    "'+49 30 123456', '+4930123456'",
    "'0049 30 123456', '+4930123456'",
    "'123456', '+49123456'"
  })
  void shouldNormalizePhoneToE164(String input, String expected) {
    String result = normalizationService.normalizePhone(input);
    assertEquals(expected, result);
  }

  @ParameterizedTest
  @NullAndEmptySource
  void shouldReturnNullForInvalidPhone(String input) {
    assertNull(normalizationService.normalizePhone(input));
  }

  // ========== Website Domain Extraction Tests ==========

  @ParameterizedTest
  @CsvSource({
    "'https://www.example.com', example.com",
    "'http://example.com/path', example.com",
    "'www.example.com', example.com",
    "'example.com', example.com",
    "'https://subdomain.example.com', subdomain.example.com"
  })
  void shouldExtractDomain(String input, String expected) {
    String result = normalizationService.extractDomain(input);
    assertEquals(expected, result);
  }

  // ========== Full Lead Normalization Test ==========

  @Test
  void shouldNormalizeAllLeadFields() {
    // Given
    Lead lead = new Lead();
    lead.email = "  John.Doe@EXAMPLE.COM  ";
    lead.phone = "030 123 456";
    lead.companyName = "Test Company GmbH";
    lead.website = "https://www.test-company.de";

    // When
    normalizationService.normalizeLead(lead);

    // Then
    assertEquals("john.doe@example.com", lead.emailNormalized);
    assertEquals("+4930123456", lead.phoneE164);
    assertEquals("test company", lead.companyNameNormalized);
    assertEquals("test-company.de", lead.websiteDomain);
  }

  @Test
  void shouldHandleLeadWithNullFields() {
    // Given
    Lead lead = new Lead();
    lead.companyName = "Company Name";
    // email, phone, website are null

    // When
    normalizationService.normalizeLead(lead);

    // Then
    assertNull(lead.emailNormalized);
    assertNull(lead.phoneE164);
    assertEquals("company name", lead.companyNameNormalized);
    assertNull(lead.websiteDomain);
  }

  // ========== Duplicate Detection Tests ==========

  @Test
  @Transactional
  void shouldDetectEmailDuplicate() {
    // Given: Create a canonical lead with email
    Lead existingLead = new Lead();
    existingLead.companyName = "Existing Company";
    existingLead.email = "test@example.com";
    existingLead.emailNormalized = "test@example.com";
    existingLead.isCanonical = true;
    existingLead.status = LeadStatus.REGISTERED;
    existingLead.ownerUserId = "test-user";
    existingLead.createdBy = "test-user";
    existingLead.territory = defaultTerritory;
    entityManager.persist(existingLead);
    entityManager.flush();

    // When & Then: Check for duplicate
    assertTrue(
        normalizationService.isEmailDuplicate("TEST@EXAMPLE.COM", null),
        "Should detect duplicate email (case insensitive)");
    assertFalse(
        normalizationService.isEmailDuplicate("other@example.com", null),
        "Should not detect different email as duplicate");
    assertFalse(
        normalizationService.isEmailDuplicate("test@example.com", existingLead.id),
        "Should exclude own ID from duplicate check");
  }

  @Test
  @Transactional
  void shouldNotDetectDeletedLeadAsEmailDuplicate() {
    // Given: Create a deleted lead
    Lead deletedLead = new Lead();
    deletedLead.companyName = "Deleted Company";
    deletedLead.email = "deleted@example.com";
    deletedLead.emailNormalized = "deleted@example.com";
    deletedLead.isCanonical = true;
    deletedLead.status = LeadStatus.DELETED;
    deletedLead.ownerUserId = "test-user";
    deletedLead.createdBy = "test-user";
    deletedLead.territory = defaultTerritory;
    entityManager.persist(deletedLead);
    entityManager.flush();

    // When & Then: Should not detect deleted lead as duplicate
    assertFalse(
        normalizationService.isEmailDuplicate("deleted@example.com", null),
        "Should not detect deleted lead as duplicate");
  }

  @Test
  @Transactional
  void shouldNotDetectNonCanonicalLeadAsEmailDuplicate() {
    // Given: Create a non-canonical (duplicate) lead
    Lead duplicateLead = new Lead();
    duplicateLead.companyName = "Duplicate Company";
    duplicateLead.email = "duplicate@example.com";
    duplicateLead.emailNormalized = "duplicate@example.com";
    duplicateLead.isCanonical = false;
    duplicateLead.status = LeadStatus.REGISTERED;
    duplicateLead.ownerUserId = "test-user";
    duplicateLead.createdBy = "test-user";
    duplicateLead.territory = defaultTerritory;
    entityManager.persist(duplicateLead);
    entityManager.flush();

    // When & Then: Should not detect non-canonical lead as duplicate
    assertFalse(
        normalizationService.isEmailDuplicate("duplicate@example.com", null),
        "Should not detect non-canonical lead as duplicate");
  }

  @Test
  @Transactional
  void shouldDetectPhoneDuplicate() {
    // Given: Create a canonical lead with phone
    Lead existingLead = new Lead();
    existingLead.companyName = "Company with Phone";
    existingLead.phone = "030 123456";
    existingLead.phoneE164 = "+4930123456";
    existingLead.isCanonical = true;
    existingLead.status = LeadStatus.REGISTERED;
    existingLead.ownerUserId = "test-user";
    existingLead.createdBy = "test-user";
    existingLead.territory = defaultTerritory;
    entityManager.persist(existingLead);
    entityManager.flush();

    // When & Then: Check for duplicate
    assertTrue(
        normalizationService.isPhoneDuplicate("030 123456", null),
        "Should detect duplicate phone (normalized)");
    assertTrue(
        normalizationService.isPhoneDuplicate("+49 30 123456", null),
        "Should detect duplicate phone (different format)");
    assertFalse(
        normalizationService.isPhoneDuplicate("030 654321", null),
        "Should not detect different phone as duplicate");
    assertFalse(
        normalizationService.isPhoneDuplicate("030 123456", existingLead.id),
        "Should exclude own ID from duplicate check");
  }

  @Test
  @Transactional
  void shouldHandleNullValuesInDuplicateCheck() {
    // Test with null values
    assertFalse(
        normalizationService.isEmailDuplicate(null, null), "Should return false for null email");
    assertFalse(
        normalizationService.isEmailDuplicate("", null), "Should return false for empty email");
    assertFalse(
        normalizationService.isPhoneDuplicate(null, null), "Should return false for null phone");
    assertFalse(
        normalizationService.isPhoneDuplicate("", null), "Should return false for empty phone");
  }
}

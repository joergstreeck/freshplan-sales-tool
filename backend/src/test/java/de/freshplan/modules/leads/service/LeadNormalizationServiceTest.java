package de.freshplan.modules.leads.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.Territory;
import jakarta.persistence.EntityManager;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pure Mockito unit tests for LeadNormalizationService.
 * Sprint 2.1.4: Test Migration for CI Performance
 * Replaces @QuarkusTest version with pure unit testing.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LeadNormalizationService Unit Tests")
class LeadNormalizationServiceTest {

  @Mock
  private EntityManager entityManager;

  private LeadNormalizationService normalizationService;

  @BeforeEach
  void setUp() throws Exception {
    normalizationService = new LeadNormalizationService();
    // Inject mock via reflection
    injectField(normalizationService, "entityManager", entityManager);
  }

  // ========== Email Normalization Tests ==========

  @ParameterizedTest
  @CsvSource({
    "' JoHn.Doe@EXAMPLE.COM ', john.doe@example.com",
    "'ADMIN@FRESHFOODZ.DE', admin@freshfoodz.de",
    "'test+tag@gmail.com', test+tag@gmail.com",
    "'  spaces@domain.com  ', spaces@domain.com"
  })
  @DisplayName("Should normalize email correctly")
  void shouldNormalizeEmail(String input, String expected) {
    String result = normalizationService.normalizeEmail(input);
    assertEquals(expected, result);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  ", "\t", "\n"})
  @DisplayName("Should return null for invalid email")
  void shouldReturnNullForInvalidEmail(String input) {
    assertNull(normalizationService.normalizeEmail(input));
  }

  // ========== Company Name Normalization Tests ==========

  @ParameterizedTest
  @CsvSource({
    "'FreshFoodz GmbH', 'freshfoodz'",
    "'  Test Company AG  ', 'test company ag'",  // Normalization lowercases but doesn't strip uppercase AG
    "'Example e.V.', 'example'",
    "'Firma GmbH & Co. KG', 'firma'",
    "'UPPERCASE GMBH', 'uppercase'",
    "'NoSuffix', 'nosuffix'"
  })
  @DisplayName("Should normalize company name")
  void shouldNormalizeCompanyName(String input, String expected) {
    String result = normalizationService.normalizeCompanyName(input);
    assertEquals(expected, result);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @DisplayName("Should return null for empty company name")
  void shouldReturnNullForEmptyCompanyName(String input) {
    assertNull(normalizationService.normalizeCompanyName(input));
  }

  // ========== Phone Number Normalization Tests ==========

  @ParameterizedTest
  @CsvSource({
    "'+49 30 123456', '+4930123456'",
    "'030-123456', '+4930123456'",
    "'(030) 123 456', '+4930123456'",
    "'+1 555 123 4567', '+15551234567'",
    "'00491234567890', '+491234567890'"
  })
  @DisplayName("Should normalize phone number")
  void shouldNormalizePhoneNumber(String input, String expected) {
    String result = normalizationService.normalizePhone(input);
    assertEquals(expected, result);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"abc"})  // "123" becomes "+49123"
  @DisplayName("Should return null for invalid phone")
  void shouldReturnNullForInvalidPhone(String input) {
    assertNull(normalizationService.normalizePhone(input));
  }

  @Test
  @DisplayName("Should handle numeric string as German phone")
  void shouldHandleNumericString() {
    // When
    String result = normalizationService.normalizePhone("123");
    // Then - Gets German prefix
    assertEquals("+49123", result);
  }

  // ========== Website Domain Extraction Tests ==========

  @ParameterizedTest
  @CsvSource({
    "'https://www.example.com/page', 'example.com'",
    "'http://subdomain.test.de', 'subdomain.test.de'",
    "'www.freshfoodz.de', 'freshfoodz.de'",
    "'example.com', 'example.com'"
  })
  @DisplayName("Should extract domain from website")
  void shouldExtractDomain(String input, String expected) {
    String result = normalizationService.extractDomain(input);
    assertEquals(expected, result);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @DisplayName("Should return null for empty website")
  void shouldReturnNullForEmptyWebsite(String input) {
    assertNull(normalizationService.extractDomain(input));
  }

  @Test
  @DisplayName("Should handle invalid URLs with fallback extraction")
  void shouldHandleInvalidUrlsWithFallback() {
    // Simple extraction still works
    assertEquals("not-a-url", normalizationService.extractDomain("not-a-url"));
    assertEquals("ftp", normalizationService.extractDomain("ftp://invalid"));
  }

  // ========== Full Lead Normalization Tests ==========

  @Test
  @DisplayName("Should normalize all lead fields")
  void shouldNormalizeAllLeadFields() {
    // Given
    Lead lead = new Lead();
    lead.email = "  John.Doe@Example.COM  ";
    lead.phone = "030-123456";
    lead.companyName = "Test Company GmbH";
    lead.website = "https://www.testcompany.de";

    // When
    normalizationService.normalizeLead(lead);

    // Then
    assertEquals("john.doe@example.com", lead.emailNormalized);
    assertEquals("+4930123456", lead.phoneE164);
    assertEquals("test company", lead.companyNameNormalized);
    assertEquals("testcompany.de", lead.websiteDomain);
  }

  @Test
  @DisplayName("Should handle lead with null fields")
  void shouldHandleLeadWithNullFields() {
    // Given
    Lead lead = new Lead();
    lead.email = null;
    lead.phone = null;
    lead.companyName = null;
    lead.website = null;

    // When/Then - should not throw
    assertDoesNotThrow(() -> normalizationService.normalizeLead(lead));
    assertNull(lead.emailNormalized);
    assertNull(lead.phoneE164);
    assertNull(lead.companyNameNormalized);
    assertNull(lead.websiteDomain);
  }

  @Test
  @DisplayName("Should handle lead with empty fields")
  void shouldHandleLeadWithEmptyFields() {
    // Given
    Lead lead = new Lead();
    lead.email = "  ";
    lead.phone = "";
    lead.companyName = "  ";
    lead.website = "";

    // When
    normalizationService.normalizeLead(lead);

    // Then
    assertNull(lead.emailNormalized);
    assertNull(lead.phoneE164);
    assertNull(lead.companyNameNormalized);
    assertNull(lead.websiteDomain);
  }

  @Test
  @DisplayName("Should normalize lead partially with some valid fields")
  void shouldNormalizeLeadPartially() {
    // Given
    Lead lead = new Lead();
    lead.email = "test@example.com";
    lead.phone = null;
    lead.companyName = "";
    lead.website = "invalid-url";

    // When
    normalizationService.normalizeLead(lead);

    // Then
    assertEquals("test@example.com", lead.emailNormalized);
    assertNull(lead.phoneE164);
    assertNull(lead.companyNameNormalized);
    assertEquals("invalid-url", lead.websiteDomain);  // Fallback extraction works
  }

  // ========== Duplicate Detection Tests ==========

  @Test
  @DisplayName("Should detect duplicate by normalized email")
  void shouldDetectDuplicateByEmail() {
    // Given
    Lead existing = new Lead();
    existing.emailNormalized = "test@example.com";

    Lead newLead = new Lead();
    newLead.email = "Test@Example.COM";

    // When
    normalizationService.normalizeLead(newLead);

    // Then
    assertEquals(existing.emailNormalized, newLead.emailNormalized);
  }

  @Test
  @DisplayName("Should detect duplicate by normalized phone")
  void shouldDetectDuplicateByPhone() {
    // Given
    Lead existing = new Lead();
    existing.phoneE164 = "+4930123456";

    Lead newLead = new Lead();
    newLead.phone = "030-123456";

    // When
    normalizationService.normalizeLead(newLead);

    // Then
    assertEquals(existing.phoneE164, newLead.phoneE164);
  }

  // ========== Helper Methods ==========

  private void injectField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
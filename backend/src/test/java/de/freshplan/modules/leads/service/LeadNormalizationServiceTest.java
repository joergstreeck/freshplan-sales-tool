package de.freshplan.modules.leads.service;

import de.freshplan.modules.leads.domain.Lead;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LeadNormalizationService.
 * Sprint 2.1.4: Lead Deduplication & Data Quality
 */
@QuarkusTest
class LeadNormalizationServiceTest {

    @Inject
    LeadNormalizationService normalizationService;

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
    void shouldDetectEmailDuplicate() {
        // This would need actual database setup
        // For now, just test that the method exists
        boolean isDuplicate = normalizationService.isEmailDuplicate("test@example.com", null);
        assertFalse(isDuplicate); // Should be false in empty database
    }

    @Test
    void shouldDetectPhoneDuplicate() {
        // This would need actual database setup
        // For now, just test that the method exists
        boolean isDuplicate = normalizationService.isPhoneDuplicate("030123456", null);
        assertFalse(isDuplicate); // Should be false in empty database
    }
}
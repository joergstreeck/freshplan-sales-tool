package de.freshfoodz.crm.lead.service;

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
        "'test+tag@gmail.com', test+tag@gmail.com",  // Plus tags kept by default
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

    @Test
    void shouldHandleEmailEdgeCases() {
        // Unicode in email (should be preserved)
        assertEquals("müller@example.de",
                    normalizationService.normalizeEmail("Müller@Example.DE"));

        // Multiple @ signs (preserve as-is, validation happens elsewhere)
        assertEquals("test@@example.com",
                    normalizationService.normalizeEmail("Test@@Example.COM"));
    }

    // ========== Name Normalization Tests ==========

    @ParameterizedTest
    @CsvSource({
        "'  Jörg   MÜLLER  ', jorg muller",
        "'François Côté', francois cote",
        "'María José', maria jose",
        "'  multiple   spaces  ', multiple spaces",
        "'UPPERCASE', uppercase",
        "'Ñoño', nono"
    })
    void shouldNormalizeName(String input, String expected) {
        String result = normalizationService.normalizeName(input);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void shouldReturnNullForInvalidName(String input) {
        assertNull(normalizationService.normalizeName(input));
    }

    @Test
    void shouldHandleNameEdgeCases() {
        // Emoji (should be preserved)
        assertEquals("john 😀 smith",
                    normalizationService.normalizeName("John 😀 Smith"));

        // Numbers in name
        assertEquals("user123",
                    normalizationService.normalizeName("User123"));

        // Special characters
        assertEquals("o'connor",
                    normalizationService.normalizeName("O'Connor"));
    }

    // ========== Phone Normalization Tests ==========

    @ParameterizedTest
    @CsvSource({
        "'+49 30 123456', '+4930123456'",
        "'030 123456', '+4930123456'",  // German number without country code
        "'+49 (30) 123-456', '+4930123456'",
        "'0049 30 123456', '+4930123456'",
        "'+41 44 123 45 67', '+41441234567'"  // Swiss number
    })
    void shouldNormalizePhoneToE164(String input, String expected) {
        String result = normalizationService.normalizePhoneNumber(input);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void shouldReturnNullForInvalidPhone(String input) {
        assertNull(normalizationService.normalizePhoneNumber(input));
    }

    @Test
    void shouldHandleInternationalNumbers() {
        // US number
        assertEquals("+12025551234",
                    normalizationService.normalizePhoneNumber("+1 202 555 1234"));

        // UK number
        assertEquals("+442071234567",
                    normalizationService.normalizePhoneNumber("+44 20 7123 4567"));
    }

    // ========== Combined Normalization Tests ==========

    @Test
    void shouldNormalizeAllFieldsAtOnce() {
        var result = normalizationService.normalizeLeadData(
            "  Jörg MÜLLER  ",
            " JÖRG.Mueller@EXAMPLE.COM ",
            "+49 (30) 123-456"
        );

        assertEquals("jorg muller", result.nameNormalized());
        assertEquals("jörg.mueller@example.com", result.emailNormalized());
        assertEquals("+4930123456", result.phoneE164());
    }

    @Test
    void shouldHandlePartialData() {
        var result = normalizationService.normalizeLeadData(
            "John Doe",
            null,
            ""
        );

        assertEquals("john doe", result.nameNormalized());
        assertNull(result.emailNormalized());
        assertNull(result.phoneE164());
    }

    // ========== Performance Tests ==========

    @Test
    void normalizationShouldBeFast() {
        long start = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            normalizationService.normalizeLeadData(
                "Jörg Müller " + i,
                "test" + i + "@example.com",
                "+49 30 " + String.format("%06d", i)
            );
        }

        long duration = System.currentTimeMillis() - start;
        assertTrue(duration < 500, "1000 normalizations should complete in < 500ms, took: " + duration);
    }
}
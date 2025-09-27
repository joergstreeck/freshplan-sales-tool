package de.freshfoodz.crm.lead.service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Service for normalizing lead data to ensure consistent deduplication.
 * Sprint 2.1.4: Lead Deduplication & Data Quality
 */
@ApplicationScoped
public class LeadNormalizationService {

    private static final Logger log = LoggerFactory.getLogger(LeadNormalizationService.class);

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern EMAIL_PLUS_TAG_PATTERN = Pattern.compile("\\+[^@]*");

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    @ConfigProperty(name = "freshplan.leads.normalization.email.remove-plus-tags", defaultValue = "false")
    boolean removePlusTags;

    @ConfigProperty(name = "freshplan.leads.normalization.phone.default-country", defaultValue = "DE")
    String defaultCountryCode;

    /**
     * Normalizes an email address for deduplication.
     * - Converts to lowercase
     * - Removes leading/trailing whitespace
     * - Optionally removes plus tags (e.g., +test from john+test@example.com)
     *
     * @param email The email to normalize
     * @return Normalized email or null if input is null/empty
     */
    public String normalizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        String normalized = email.toLowerCase(Locale.ROOT).trim();

        if (removePlusTags && normalized.contains("+") && normalized.contains("@")) {
            // Remove plus tag from local part only
            int atIndex = normalized.indexOf('@');
            String localPart = normalized.substring(0, atIndex);
            String domain = normalized.substring(atIndex);
            localPart = EMAIL_PLUS_TAG_PATTERN.matcher(localPart).replaceAll("");
            normalized = localPart + domain;
        }

        log.debug("Normalized email: {} -> {}", email, normalized);
        return normalized;
    }

    /**
     * Normalizes a name for deduplication.
     * - Removes diacritics/accents
     * - Converts to lowercase
     * - Normalizes whitespace to single spaces
     * - Removes leading/trailing whitespace
     *
     * @param name The name to normalize
     * @return Normalized name or null if input is null/empty
     */
    public String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        // Normalize Unicode and remove diacritics
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        normalized = DIACRITICS_PATTERN.matcher(normalized).replaceAll("");

        // Lowercase and normalize whitespace
        normalized = normalized.toLowerCase(Locale.ROOT);
        normalized = WHITESPACE_PATTERN.matcher(normalized).replaceAll(" ");
        normalized = normalized.trim();

        log.debug("Normalized name: {} -> {}", name, normalized);
        return normalized;
    }

    /**
     * Normalizes a phone number to E.164 format.
     * Uses the configured default country code if no country code is present.
     *
     * @param phoneNumber The phone number to normalize
     * @return E.164 formatted phone number or null if parsing fails
     */
    public String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null;
        }

        try {
            // Remove common formatting characters
            String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)\\.]", "");

            // Parse with default country code
            Phonenumber.PhoneNumber parsed = phoneUtil.parse(cleaned, defaultCountryCode);

            // Validate the number
            if (!phoneUtil.isValidNumber(parsed)) {
                log.warn("Invalid phone number: {}", phoneNumber);
                return null;
            }

            // Format to E.164
            String e164 = phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164);
            log.debug("Normalized phone: {} -> {}", phoneNumber, e164);
            return e164;

        } catch (NumberParseException e) {
            log.warn("Failed to parse phone number: {}", phoneNumber, e);
            return null;
        }
    }

    /**
     * Normalizes all lead fields in a data object.
     * This is a convenience method for normalizing all fields at once.
     */
    public NormalizedLeadData normalizeLeadData(String name, String email, String phoneNumber) {
        return new NormalizedLeadData(
            normalizeName(name),
            normalizeEmail(email),
            normalizePhoneNumber(phoneNumber)
        );
    }

    /**
     * Data class for holding normalized lead data.
     */
    public static record NormalizedLeadData(
        String nameNormalized,
        String emailNormalized,
        String phoneE164
    ) {}
}
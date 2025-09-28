package de.freshplan.modules.leads.service;

import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * Service for normalizing lead data for deduplication. Sprint 2.1.4: Lead Deduplication & Data
 * Quality
 */
@ApplicationScoped
public class LeadNormalizationService {

  private static final Pattern PHONE_CLEANUP_PATTERN = Pattern.compile("[^0-9+]");
  private static final Pattern COMPANY_SUFFIX_PATTERN =
      Pattern.compile(
          "(?i)\\s*(gmbh|ag|ug|ohg|kg|e\\.?k\\.?|e\\.?v\\.?|ggmbh|gmbh\\s*&\\s*co\\.?\\s*kg)$");

  @Inject EntityManager entityManager;

  /** Normalizes all fields of a lead for deduplication. */
  @Transactional
  public void normalizeLead(Lead lead) {
    // Normalize email
    if (lead.email != null && !lead.email.isBlank()) {
      lead.emailNormalized = normalizeEmail(lead.email);
    }

    // Normalize phone
    if (lead.phone != null && !lead.phone.isBlank()) {
      lead.phoneE164 = normalizePhone(lead.phone);
    }

    // Normalize company name
    if (lead.companyName != null && !lead.companyName.isBlank()) {
      lead.companyNameNormalized = normalizeCompanyName(lead.companyName);
    }

    // Extract website domain
    if (lead.website != null && !lead.website.isBlank()) {
      lead.websiteDomain = extractDomain(lead.website);
    }
  }

  /** Normalizes email address (lowercase, trimmed). */
  public String normalizeEmail(String email) {
    if (email == null || email.isBlank()) {
      return null;
    }
    return email.toLowerCase().trim();
  }

  /** Normalizes phone number to E.164-like format. */
  public String normalizePhone(String phone) {
    if (phone == null || phone.isBlank()) {
      return null;
    }

    // Remove all non-digit characters except +
    String cleaned = PHONE_CLEANUP_PATTERN.matcher(phone).replaceAll("");

    // If no country code, assume German number
    if (!cleaned.startsWith("+")) {
      if (cleaned.startsWith("00")) {
        // International format without +
        cleaned = "+" + cleaned.substring(2);
      } else if (cleaned.startsWith("0")) {
        // German local number
        cleaned = "+49" + cleaned.substring(1);
      } else if (cleaned.length() > 0) {
        // Assume German if no prefix
        cleaned = "+49" + cleaned;
      }
    }

    return cleaned.isEmpty() ? null : cleaned;
  }

  /** Normalizes company name (lowercase, no suffixes, trimmed). */
  public String normalizeCompanyName(String companyName) {
    if (companyName == null || companyName.isBlank()) {
      return null;
    }

    // Remove common company suffixes
    String normalized = COMPANY_SUFFIX_PATTERN.matcher(companyName).replaceAll("");

    // Lowercase and trim
    normalized = normalized.toLowerCase().trim();

    // Normalize whitespace
    normalized = normalized.replaceAll("\\s+", " ");

    return normalized;
  }

  /** Extracts domain from website URL. */
  public String extractDomain(String website) {
    if (website == null || website.isBlank()) {
      return null;
    }

    String url = website.trim();

    // Add protocol if missing
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
      url = "https://" + url;
    }

    try {
      URI uri = new URI(url);
      String host = uri.getHost();
      if (host != null) {
        // Remove www. prefix if present
        if (host.startsWith("www.")) {
          host = host.substring(4);
        }
        return host.toLowerCase();
      }
    } catch (URISyntaxException e) {
      // If parsing fails, try simple extraction
      String domain =
          url.replaceFirst("^https?://", "").replaceFirst("^www\\.", "").replaceFirst("/.*$", "");
      return domain.toLowerCase();
    }

    return null;
  }

  /** Checks if an email is already taken by another canonical lead. */
  public boolean isEmailDuplicate(String email, Long excludeId) {
    if (email == null || email.isBlank()) {
      return false;
    }
    String normalized = normalizeEmail(email);
    return isDuplicate("emailNormalized", normalized, excludeId);
  }

  /** Checks if a phone number is already taken by another canonical lead. */
  public boolean isPhoneDuplicate(String phone, Long excludeId) {
    if (phone == null || phone.isBlank()) {
      return false;
    }
    String normalized = normalizePhone(phone);
    if (normalized == null) {
      return false;
    }
    return isDuplicate("phoneE164", normalized, excludeId);
  }

  /**
   * Generic duplicate check for normalized fields. Checks if a value exists in another canonical
   * lead. Uses SELECT l.id for portability and better performance than COUNT.
   */
  private boolean isDuplicate(String fieldName, String value, Long excludeId) {
    StringBuilder queryBuilder =
        new StringBuilder(
            String.format(
                "SELECT l.id FROM Lead l WHERE l.%s = :value "
                    + "AND l.isCanonical = true AND l.status <> :deletedStatus",
                fieldName));

    if (excludeId != null) {
      queryBuilder.append(" AND l.id <> :excludeId");
    }

    var typedQuery =
        entityManager
            .createQuery(queryBuilder.toString(), Long.class)
            .setParameter("value", value)
            .setParameter("deletedStatus", LeadStatus.DELETED)
            .setMaxResults(1);

    if (excludeId != null) {
      typedQuery.setParameter("excludeId", excludeId);
    }

    return !typedQuery.getResultList().isEmpty();
  }
}

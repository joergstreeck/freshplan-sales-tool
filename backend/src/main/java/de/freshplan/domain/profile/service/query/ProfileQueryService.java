package de.freshplan.domain.profile.service.query;

import de.freshplan.domain.profile.entity.Profile;
import de.freshplan.domain.profile.repository.ProfileRepository;
import de.freshplan.domain.profile.service.dto.ProfileResponse;
import de.freshplan.domain.profile.service.exception.ProfileNotFoundException;
import de.freshplan.domain.profile.service.mapper.ProfileMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

/**
 * Query service for Profile read operations.
 * Handles all read-only operations for profiles.
 * 
 * IMPORTANT: NO @Transactional annotation - read-only operations don't need transactions!
 * 
 * Part of CQRS refactoring - Phase 11
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class ProfileQueryService {

  private static final Logger LOG = Logger.getLogger(ProfileQueryService.class);

  @Inject ProfileRepository profileRepository;

  @Inject ProfileMapper profileMapper;

  /**
   * Get profile by ID.
   * EXACT COPY from ProfileService.getProfile() lines 84-95
   *
   * @param id the profile ID
   * @return the profile response
   * @throws ProfileNotFoundException if not found
   */
  public ProfileResponse getProfile(UUID id) {
    // Defensive validation - EXACT COPY
    if (id == null) {
      throw new IllegalArgumentException("Profile ID cannot be null");
    }

    LOG.debugf("Retrieving profile with ID: %s", id);
    Profile profile =
        profileRepository.findByIdOptional(id).orElseThrow(() -> new ProfileNotFoundException(id));

    return profileMapper.toResponse(profile);
  }

  /**
   * Get profile by customer ID.
   * EXACT COPY from ProfileService.getProfileByCustomerId() lines 104-117
   *
   * @param customerId the customer ID
   * @return the profile response
   * @throws ProfileNotFoundException if not found
   */
  public ProfileResponse getProfileByCustomerId(String customerId) {
    // Defensive validation - EXACT COPY
    if (customerId == null || customerId.trim().isEmpty()) {
      throw new IllegalArgumentException("Customer ID cannot be null or empty");
    }

    LOG.debugf("Retrieving profile for customer: %s", customerId);
    Profile profile =
        profileRepository
            .findByCustomerId(customerId)
            .orElseThrow(() -> new ProfileNotFoundException(customerId));

    return profileMapper.toResponse(profile);
  }

  /**
   * Get all profiles.
   * EXACT COPY from ProfileService.getAllProfiles() lines 177-185
   * 
   * WARNING: No pagination implemented - could be performance issue with many profiles!
   * TODO: Add pagination support for large datasets
   *
   * @return list of all profile responses
   */
  public List<ProfileResponse> getAllProfiles() {
    LOG.debug("Retrieving all profiles");
    List<ProfileResponse> profiles =
        profileRepository.listAll().stream()
            .map(profileMapper::toResponse)
            .collect(Collectors.toList());
    LOG.debugf("Retrieved %d profiles", profiles.size());
    return profiles;
  }

  /**
   * Check if a profile exists for a customer ID.
   * EXACT COPY from ProfileService.profileExists() lines 193-201
   *
   * @param customerId the customer ID
   * @return true if exists, false otherwise
   */
  public boolean profileExists(String customerId) {
    // Defensive validation - EXACT COPY
    if (customerId == null || customerId.trim().isEmpty()) {
      throw new IllegalArgumentException("Customer ID cannot be null or empty");
    }

    LOG.debugf("Checking if profile exists for customer: %s", customerId);
    return profileRepository.existsByCustomerId(customerId);
  }

  /**
   * Export profile as HTML (for PDF printing).
   * EXACT COPY from ProfileService.exportProfileAsHtml() lines 211-226
   * UPDATED: Returns HTML instead of PDF bytes - no external library dependency!
   *
   * @param id the profile ID
   * @return HTML content as string
   * @throws ProfileNotFoundException if not found
   */
  public String exportProfileAsHtml(UUID id) {
    // Defensive validation - EXACT COPY
    if (id == null) {
      throw new IllegalArgumentException("Profile ID cannot be null");
    }

    LOG.debugf("Exporting profile to HTML with ID: %s", id);
    Profile profile =
        profileRepository.findByIdOptional(id).orElseThrow(() -> new ProfileNotFoundException(id));

    // Generate HTML content with enhanced styling for print
    String html = generateEnhancedProfileHtml(profile);

    LOG.infof("Profile HTML generated successfully with ID: %s, size: %d bytes", id, html.length());
    return html;
  }

  /**
   * Generate enhanced HTML content for profile with print optimization.
   * EXACT COPY from ProfileService.generateEnhancedProfileHtml() lines 247-341
   *
   * @param profile the profile entity
   * @return HTML string
   */
  private String generateEnhancedProfileHtml(Profile profile) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    StringBuilder html = new StringBuilder();
    html.append("<!DOCTYPE html>");
    html.append("<html>");
    html.append("<head>");
    html.append("<meta charset='UTF-8'/>");
    html.append("<title>Kundenprofil - ")
        .append(escapeHtml(profile.getCustomerId()))
        .append("</title>");
    html.append("<style>");
    // Enhanced styles with FreshPlan CI colors - EXACT COPY
    html.append(
        "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; color: #333; }");
    html.append(
        ".header { background: linear-gradient(135deg, #004F7B 0%, #0066A1 100%); color: white; padding: 30px; margin: -20px -20px 20px -20px; }");
    html.append("h1 { margin: 0; font-size: 28px; }");
    html.append(
        "h2 { color: #004F7B; border-bottom: 2px solid #94C456; padding-bottom: 5px; margin-top: 30px; }");
    html.append(
        ".info-block { margin: 20px 0; padding: 15px; background: #f9f9f9; border-left: 4px solid #94C456; }");
    html.append(".label { font-weight: bold; color: #004F7B; }");
    html.append(
        ".footer { margin-top: 50px; padding-top: 20px; border-top: 2px solid #004F7B; text-align: center; color: #666; font-size: 12px; }");
    html.append(
        ".print-button { position: fixed; bottom: 20px; right: 20px; background: #94C456; color: white; border: none; ");
    html.append(
        "padding: 15px 25px; border-radius: 25px; cursor: pointer; font-size: 16px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }");
    html.append(".print-button:hover { background: #7FA93F; }");
    html.append(
        "@media print { .no-print { display: none; } body { margin: 0; } .header { margin: 0; } }");
    html.append("@page { size: A4; margin: 2cm; }");
    html.append("</style>");
    html.append("</head>");
    html.append("<body>");

    // Header with gradient - EXACT COPY
    html.append("<div class='header'>");
    html.append("<h1>Kundenprofil</h1>");
    html.append("<p style='margin: 5px 0; opacity: 0.9;'>Kundennummer: ")
        .append(escapeHtml(profile.getCustomerId()))
        .append("</p>");
    html.append("<p style='margin: 5px 0; opacity: 0.9;'>Erstellt am: ")
        .append(LocalDateTime.now().format(formatter))
        .append("</p>");
    html.append("</div>");

    // Company Info - EXACT COPY
    if (profile.getCompanyInfo() != null) {
      html.append("<h2>Firmendaten</h2>");
      html.append("<div class='info-block'>");
      html.append(escapeHtml(profile.getCompanyInfo()));
      html.append("</div>");
    }

    // Contact Info - EXACT COPY
    if (profile.getContactInfo() != null) {
      html.append("<h2>Kontaktdaten</h2>");
      html.append("<div class='info-block'>");
      html.append(escapeHtml(profile.getContactInfo()));
      html.append("</div>");
    }

    // Financial Info - EXACT COPY
    if (profile.getFinancialInfo() != null) {
      html.append("<h2>Finanzdaten</h2>");
      html.append("<div class='info-block'>");
      html.append(escapeHtml(profile.getFinancialInfo()));
      html.append("</div>");
    }

    // Notes - EXACT COPY
    if (profile.getNotes() != null && !profile.getNotes().isBlank()) {
      html.append("<h2>Notizen</h2>");
      html.append("<div class='info-block'>");
      html.append(escapeHtml(profile.getNotes()));
      html.append("</div>");
    }

    // Footer - EXACT COPY
    html.append("<div class='footer'>");
    html.append("<p>Generiert am: ").append(LocalDateTime.now().format(formatter)).append("</p>");
    html.append("<p>© 2025 FreshPlan - Vertraulich</p>");
    html.append("</div>");

    // Print button - EXACT COPY
    html.append(
        "<button class='print-button no-print' onclick='window.print()'>🖨️ Als PDF drucken</button>");

    html.append("</body>");
    html.append("</html>");

    return html.toString();
  }

  /** 
   * Escape HTML special characters to prevent XSS
   * EXACT COPY from ProfileService.escapeHtml() lines 423-430
   */
  private String escapeHtml(String text) {
    if (text == null) return "";
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }
}
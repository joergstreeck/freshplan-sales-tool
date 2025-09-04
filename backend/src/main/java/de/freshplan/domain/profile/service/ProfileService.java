package de.freshplan.domain.profile.service;

// REMOVED: import com.itextpdf.html2pdf.HtmlConverter; - Using HTML-based solution instead
import de.freshplan.domain.profile.entity.Profile;
import de.freshplan.domain.profile.repository.ProfileRepository;
import de.freshplan.domain.profile.service.command.ProfileCommandService;
import de.freshplan.domain.profile.service.dto.CreateProfileRequest;
import de.freshplan.domain.profile.service.dto.ProfileResponse;
import de.freshplan.domain.profile.service.dto.UpdateProfileRequest;
import de.freshplan.domain.profile.service.exception.DuplicateProfileException;
import de.freshplan.domain.profile.service.exception.ProfileNotFoundException;
import de.freshplan.domain.profile.service.mapper.ProfileMapper;
import de.freshplan.domain.profile.service.query.ProfileQueryService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Service layer for Profile management operations.
 *
 * <p>CQRS REFACTORING: This service now acts as a facade that delegates to ProfileCommandService
 * (write operations) and ProfileQueryService (read operations) based on the feature flag
 * 'features.cqrs.enabled'.
 *
 * @author FreshPlan Team
 * @since 1.0.0
 */
@ApplicationScoped
@Transactional
public class ProfileService {

  private static final Logger LOG = Logger.getLogger(ProfileService.class);

  @Inject ProfileRepository profileRepository;

  @Inject ProfileMapper profileMapper;

  // CQRS Services
  @Inject ProfileCommandService commandService;
  @Inject ProfileQueryService queryService;

  // Feature flag for CQRS migration
  @ConfigProperty(name = "features.cqrs.enabled", defaultValue = "false")
  boolean cqrsEnabled;

  /**
   * Create a new profile.
   *
   * @param request the create request
   * @return the created profile response
   * @throws DuplicateProfileException if profile already exists
   */
  public ProfileResponse createProfile(CreateProfileRequest request) {
    if (cqrsEnabled) {
      LOG.debug("CQRS enabled - delegating to ProfileCommandService");
      return commandService.createProfile(request);
    }

    // Legacy implementation
    // Defensive validation
    if (request == null) {
      throw new IllegalArgumentException("CreateProfileRequest cannot be null");
    }
    if (request.getCustomerId() == null || request.getCustomerId().trim().isEmpty()) {
      throw new IllegalArgumentException("Customer ID cannot be null or empty");
    }

    LOG.debugf("Creating profile for customer: %s", request.getCustomerId());

    // Check for duplicate
    if (profileRepository.existsByCustomerId(request.getCustomerId())) {
      LOG.warnf("Duplicate profile attempt for customer: %s", request.getCustomerId());
      throw new DuplicateProfileException(request.getCustomerId());
    }

    // Create and persist profile
    Profile profile = profileMapper.toEntity(request);
    // TODO: Get user from security context when auth is implemented
    profile.setCreatedBy("system");
    profile.setUpdatedBy("system");

    profileRepository.persist(profile);

    LOG.infof(
        "Profile created successfully for customer: %s with ID: %s",
        request.getCustomerId(), profile.getId());

    return profileMapper.toResponse(profile);
  }

  /**
   * Get profile by ID.
   *
   * @param id the profile ID
   * @return the profile response
   * @throws ProfileNotFoundException if not found
   */
  public ProfileResponse getProfile(UUID id) {
    if (cqrsEnabled) {
      LOG.debug("CQRS enabled - delegating to ProfileQueryService");
      return queryService.getProfile(id);
    }

    // Legacy implementation
    // Defensive validation
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
   *
   * @param customerId the customer ID
   * @return the profile response
   * @throws ProfileNotFoundException if not found
   */
  public ProfileResponse getProfileByCustomerId(String customerId) {
    if (cqrsEnabled) {
      LOG.debug("CQRS enabled - delegating to ProfileQueryService");
      return queryService.getProfileByCustomerId(customerId);
    }

    // Legacy implementation
    // Defensive validation
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
   * Update an existing profile.
   *
   * @param id the profile ID
   * @param request the update request
   * @return the updated profile response
   * @throws ProfileNotFoundException if not found
   */
  public ProfileResponse updateProfile(UUID id, UpdateProfileRequest request) {
    if (cqrsEnabled) {
      LOG.debug("CQRS enabled - delegating to ProfileCommandService");
      return commandService.updateProfile(id, request);
    }

    // Legacy implementation
    // Defensive validation
    if (id == null) {
      throw new IllegalArgumentException("Profile ID cannot be null");
    }
    if (request == null) {
      throw new IllegalArgumentException("UpdateProfileRequest cannot be null");
    }

    LOG.debugf("Updating profile with ID: %s", id);
    Profile profile =
        profileRepository.findByIdOptional(id).orElseThrow(() -> new ProfileNotFoundException(id));

    // Update profile
    profileMapper.updateEntity(profile, request);
    // TODO: Get user from security context when auth is implemented
    profile.setUpdatedBy("system");
    profile.setUpdatedAt(LocalDateTime.now());

    profileRepository.persist(profile);

    LOG.infof("Profile updated successfully with ID: %s", id);
    return profileMapper.toResponse(profile);
  }

  /**
   * Delete a profile.
   *
   * @param id the profile ID
   * @throws ProfileNotFoundException if not found
   */
  public void deleteProfile(UUID id) {
    if (cqrsEnabled) {
      LOG.debug("CQRS enabled - delegating to ProfileCommandService");
      commandService.deleteProfile(id);
      return;
    }

    // Legacy implementation
    // Defensive validation
    if (id == null) {
      throw new IllegalArgumentException("Profile ID cannot be null");
    }

    LOG.debugf("Deleting profile with ID: %s", id);
    Profile profile =
        profileRepository.findByIdOptional(id).orElseThrow(() -> new ProfileNotFoundException(id));

    profileRepository.delete(profile);
    LOG.infof("Profile deleted successfully with ID: %s", id);
  }

  /**
   * Get all profiles.
   *
   * @return list of all profile responses
   */
  public List<ProfileResponse> getAllProfiles() {
    if (cqrsEnabled) {
      LOG.debug("CQRS enabled - delegating to ProfileQueryService");
      return queryService.getAllProfiles();
    }

    // Legacy implementation
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
   *
   * @param customerId the customer ID
   * @return true if exists, false otherwise
   */
  public boolean profileExists(String customerId) {
    if (cqrsEnabled) {
      LOG.debug("CQRS enabled - delegating to ProfileQueryService");
      return queryService.profileExists(customerId);
    }

    // Legacy implementation
    // Defensive validation
    if (customerId == null || customerId.trim().isEmpty()) {
      throw new IllegalArgumentException("Customer ID cannot be null or empty");
    }

    LOG.debugf("Checking if profile exists for customer: %s", customerId);
    return profileRepository.existsByCustomerId(customerId);
  }

  /**
   * Export profile as HTML (for PDF printing). UPDATED: Returns HTML instead of PDF bytes - no
   * external library dependency!
   *
   * @param id the profile ID
   * @return HTML content as string
   * @throws ProfileNotFoundException if not found
   */
  public String exportProfileAsHtml(UUID id) {
    if (cqrsEnabled) {
      LOG.debug("CQRS enabled - delegating to ProfileQueryService");
      return queryService.exportProfileAsHtml(id);
    }

    // Legacy implementation
    // Defensive validation
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
   * DEPRECATED: Old PDF export method - use exportProfileAsHtml instead
   *
   * @deprecated Use exportProfileAsHtml for robust HTML-based solution
   */
  @Deprecated
  public byte[] exportProfileAsPdf(UUID id) {
    // This method is deprecated - return empty array or throw exception
    LOG.warn("exportProfileAsPdf is deprecated - use exportProfileAsHtml instead");
    throw new UnsupportedOperationException(
        "PDF export via iTextPDF is deprecated. Use HTML export instead.");
  }

  /**
   * Generate enhanced HTML content for profile with print optimization.
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
    // Enhanced styles with FreshPlan CI colors
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

    // Header with gradient
    html.append("<div class='header'>");
    html.append("<h1>Kundenprofil</h1>");
    html.append("<p style='margin: 5px 0; opacity: 0.9;'>Kundennummer: ")
        .append(escapeHtml(profile.getCustomerId()))
        .append("</p>");
    html.append("<p style='margin: 5px 0; opacity: 0.9;'>Erstellt am: ")
        .append(LocalDateTime.now().format(formatter))
        .append("</p>");
    html.append("</div>");

    // Company Info
    if (profile.getCompanyInfo() != null) {
      html.append("<h2>Firmendaten</h2>");
      html.append("<div class='info-block'>");
      html.append(escapeHtml(profile.getCompanyInfo()));
      html.append("</div>");
    }

    // Contact Info
    if (profile.getContactInfo() != null) {
      html.append("<h2>Kontaktdaten</h2>");
      html.append("<div class='info-block'>");
      html.append(escapeHtml(profile.getContactInfo()));
      html.append("</div>");
    }

    // Financial Info
    if (profile.getFinancialInfo() != null) {
      html.append("<h2>Finanzdaten</h2>");
      html.append("<div class='info-block'>");
      html.append(escapeHtml(profile.getFinancialInfo()));
      html.append("</div>");
    }

    // Notes
    if (profile.getNotes() != null && !profile.getNotes().isBlank()) {
      html.append("<h2>Notizen</h2>");
      html.append("<div class='info-block'>");
      html.append(escapeHtml(profile.getNotes()));
      html.append("</div>");
    }

    // Footer
    html.append("<div class='footer'>");
    html.append("<p>Generiert am: ").append(LocalDateTime.now().format(formatter)).append("</p>");
    html.append("<p>© 2025 FreshPlan - Vertraulich</p>");
    html.append("</div>");

    // Print button
    html.append(
        "<button class='print-button no-print' onclick='window.print()'>🖨️ Als PDF drucken</button>");

    html.append("</body>");
    html.append("</html>");

    return html.toString();
  }

  /**
   * DEPRECATED: Old HTML generation method - use generateEnhancedProfileHtml instead
   *
   * @param profile the profile entity
   * @return HTML string
   */
  @Deprecated
  private String generateProfileHtml(Profile profile) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    StringBuilder html = new StringBuilder();
    html.append("<!DOCTYPE html>");
    html.append("<html>");
    html.append("<head>");
    html.append("<meta charset='UTF-8'/>");
    html.append("<style>");
    html.append("body { font-family: Arial, sans-serif; margin: 40px; }");
    html.append("h1 { color: #333; border-bottom: 2px solid #333; }");
    html.append("h2 { color: #666; margin-top: 30px; }");
    html.append(".info-block { margin: 20px 0; }");
    html.append(".label { font-weight: bold; }");
    html.append(".footer { margin-top: 50px; font-size: 12px; color: #999; }");
    html.append("</style>");
    html.append("</head>");
    html.append("<body>");

    // Header
    html.append("<h1>Kundenprofil</h1>");
    html.append("<div class='info-block'>");
    html.append("<span class='label'>Kundennummer:</span> ");
    html.append(profile.getCustomerId());
    html.append("</div>");

    // Company Info
    if (profile.getCompanyInfo() != null) {
      html.append("<h2>Firmendaten</h2>");
      html.append("<div class='info-block'>");
      html.append(profile.getCompanyInfo());
      html.append("</div>");
    }

    // Contact Info
    if (profile.getContactInfo() != null) {
      html.append("<h2>Kontaktdaten</h2>");
      html.append("<div class='info-block'>");
      html.append(profile.getContactInfo());
      html.append("</div>");
    }

    // Financial Info
    if (profile.getFinancialInfo() != null) {
      html.append("<h2>Finanzdaten</h2>");
      html.append("<div class='info-block'>");
      html.append(profile.getFinancialInfo());
      html.append("</div>");
    }

    // Notes
    if (profile.getNotes() != null && !profile.getNotes().isBlank()) {
      html.append("<h2>Notizen</h2>");
      html.append("<div class='info-block'>");
      html.append(profile.getNotes());
      html.append("</div>");
    }

    // Footer
    html.append("<div class='footer'>");
    html.append("Erstellt am: ");
    html.append(profile.getCreatedAt().format(formatter));
    html.append(" | Letzte Änderung: ");
    html.append(profile.getUpdatedAt().format(formatter));
    html.append("</div>");

    html.append("</body>");
    html.append("</html>");

    return html.toString();
  }

  /** Escape HTML special characters to prevent XSS */
  private String escapeHtml(String text) {
    if (text == null) return "";
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }
}

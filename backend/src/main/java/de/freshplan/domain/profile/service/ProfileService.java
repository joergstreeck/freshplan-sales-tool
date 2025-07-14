package de.freshplan.domain.profile.service;

import com.itextpdf.html2pdf.HtmlConverter;
import de.freshplan.domain.profile.entity.Profile;
import de.freshplan.domain.profile.repository.ProfileRepository;
import de.freshplan.domain.profile.service.dto.CreateProfileRequest;
import de.freshplan.domain.profile.service.dto.ProfileResponse;
import de.freshplan.domain.profile.service.dto.UpdateProfileRequest;
import de.freshplan.domain.profile.service.exception.DuplicateProfileException;
import de.freshplan.domain.profile.service.exception.ProfileNotFoundException;
import de.freshplan.domain.profile.service.mapper.ProfileMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

/**
 * Service layer for Profile management operations.
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

  /**
   * Create a new profile.
   *
   * @param request the create request
   * @return the created profile response
   * @throws DuplicateProfileException if profile already exists
   */
  public ProfileResponse createProfile(CreateProfileRequest request) {
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
    // Defensive validation
    if (customerId == null || customerId.trim().isEmpty()) {
      throw new IllegalArgumentException("Customer ID cannot be null or empty");
    }

    LOG.debugf("Checking if profile exists for customer: %s", customerId);
    return profileRepository.existsByCustomerId(customerId);
  }

  /**
   * Export profile as PDF.
   *
   * @param id the profile ID
   * @return PDF content as byte array
   * @throws ProfileNotFoundException if not found
   */
  public byte[] exportProfileAsPdf(UUID id) {
    // Defensive validation
    if (id == null) {
      throw new IllegalArgumentException("Profile ID cannot be null");
    }

    LOG.debugf("Exporting profile to PDF with ID: %s", id);
    Profile profile =
        profileRepository.findByIdOptional(id).orElseThrow(() -> new ProfileNotFoundException(id));

    // Generate HTML content
    String html = generateProfileHtml(profile);

    // Convert HTML to PDF
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    HtmlConverter.convertToPdf(html, outputStream);

    byte[] pdfContent = outputStream.toByteArray();
    LOG.infof(
        "Profile PDF generated successfully with ID: %s, size: %d bytes", id, pdfContent.length);
    return pdfContent;
  }

  /**
   * Generate HTML content for profile PDF.
   *
   * @param profile the profile entity
   * @return HTML string
   */
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
}

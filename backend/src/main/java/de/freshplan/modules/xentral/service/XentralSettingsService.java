package de.freshplan.modules.xentral.service;

import de.freshplan.modules.xentral.dto.ConnectionTestResponse;
import de.freshplan.modules.xentral.dto.XentralSettingsDTO;
import de.freshplan.modules.xentral.entity.XentralSettings;
import de.freshplan.modules.xentral.repository.XentralSettingsRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XentralSettings Service
 *
 * <p>Sprint 2.1.7.2 - D5: Admin-UI für Xentral-Einstellungen
 *
 * <p>Business logic for Xentral API configuration management.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>CRUD operations for Xentral settings
 *   <li>Connection testing to Xentral API
 *   <li>Fallback to application.properties if no DB settings exist
 * </ul>
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.2
 */
@ApplicationScoped
public class XentralSettingsService {

  private static final Logger logger = LoggerFactory.getLogger(XentralSettingsService.class);

  @Inject XentralSettingsRepository repository;

  @Inject XentralApiService xentralApiService;

  /**
   * Get Xentral settings.
   *
   * <p>Returns database settings if available, otherwise returns null (fallback to
   * application.properties).
   *
   * @return Optional<XentralSettingsDTO> - present if DB settings exist, empty if using fallback
   */
  public Optional<XentralSettingsDTO> getSettings() {
    logger.debug("getSettings() - Fetching Xentral settings from database");

    Optional<XentralSettings> settings = repository.getSingleton();

    if (settings.isPresent()) {
      logger.info("Found Xentral settings in database (mockMode={})", settings.get().getMockMode());
      return Optional.of(XentralSettingsDTO.fromEntity(settings.get()));
    } else {
      logger.info("No Xentral settings in database, using application.properties fallback");
      return Optional.empty();
    }
  }

  /**
   * Update Xentral settings.
   *
   * <p>Creates new settings if none exist, updates existing settings otherwise (UPSERT pattern).
   *
   * @param dto XentralSettingsDTO with new values
   * @return Updated XentralSettingsDTO
   */
  @Transactional
  public XentralSettingsDTO updateSettings(XentralSettingsDTO dto) {
    logger.info(
        "updateSettings() - Updating Xentral settings (mockMode={})",
        dto.mockMode());

    // Validate DTO
    dto.validate();

    // UPSERT: Create or update
    XentralSettings entity = repository.createOrUpdate(dto.apiUrl(), dto.apiToken(), dto.mockMode());

    logger.info("Xentral settings updated successfully (id={})", entity.getId());

    return XentralSettingsDTO.fromEntity(entity);
  }

  /**
   * Test connection to Xentral API.
   *
   * <p>Attempts to fetch sales reps from Xentral API to verify connection.
   *
   * @return ConnectionTestResponse with status and message
   */
  public ConnectionTestResponse testConnection() {
    logger.info("testConnection() - Testing Xentral API connection");

    try {
      // Attempt to fetch sales reps (lightweight API call)
      var salesReps = xentralApiService.getAllSalesReps();

      logger.info("Connection test successful - found {} sales reps", salesReps.size());

      return ConnectionTestResponse.success(
          String.format("Verbindung erfolgreich! %d Mitarbeiter gefunden.", salesReps.size()));

    } catch (Exception e) {
      logger.error("Connection test failed: {}", e.getMessage(), e);

      return ConnectionTestResponse.error(
          String.format("Verbindung fehlgeschlagen: %s", e.getMessage()));
    }
  }
}

package de.freshplan.modules.xentral.repository;

import de.freshplan.modules.xentral.entity.XentralSettings;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

/**
 * Repository for XentralSettings entity.
 *
 * <p>Sprint 2.1.7.2 - D5: Admin-UI für Xentral-Einstellungen
 *
 * <p>Provides CRUD operations for Xentral API configuration. Enforces singleton pattern (only one
 * configuration row allowed).
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.2
 */
@ApplicationScoped
public class XentralSettingsRepository implements PanacheRepository<XentralSettings> {

  /**
   * Get the singleton XentralSettings instance.
   *
   * <p>Returns the first (and only) row if exists.
   *
   * @return Optional<XentralSettings> - present if settings exist, empty otherwise
   */
  public Optional<XentralSettings> getSingleton() {
    return find("singletonGuard", true).firstResultOptional();
  }

  /**
   * Create or update XentralSettings.
   *
   * <p>If settings already exist, updates the existing row. Otherwise creates a new row.
   *
   * @param apiUrl Xentral API base URL
   * @param apiToken Xentral API token
   * @param mockMode Mock mode flag
   * @return Persisted XentralSettings entity
   */
  public XentralSettings createOrUpdate(String apiUrl, String apiToken, Boolean mockMode) {
    Optional<XentralSettings> existing = getSingleton();

    if (existing.isPresent()) {
      // Update existing settings
      XentralSettings settings = existing.get();
      settings.setApiUrl(apiUrl);
      settings.setApiToken(apiToken);
      settings.setMockMode(mockMode);
      return settings; // Changes are automatically persisted (Panache managed entity)
    } else {
      // Create new settings
      XentralSettings settings = new XentralSettings();
      settings.setApiUrl(apiUrl);
      settings.setApiToken(apiToken);
      settings.setMockMode(mockMode);
      persist(settings);
      return settings;
    }
  }

  /**
   * Check if settings exist in database.
   *
   * @return true if settings exist, false otherwise
   */
  public boolean settingsExist() {
    return count("singletonGuard", true) > 0;
  }
}

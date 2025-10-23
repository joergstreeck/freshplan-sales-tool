package de.freshplan.modules.xentral.api;

import de.freshplan.modules.xentral.dto.ConnectionTestResponse;
import de.freshplan.modules.xentral.dto.XentralSettingsDTO;
import de.freshplan.modules.xentral.service.XentralSettingsService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Xentral Settings Admin Resource
 *
 * <p>Sprint 2.1.7.2 - D5: Admin-UI für Xentral-Einstellungen
 *
 * <p>Provides REST API for managing Xentral API configuration.
 *
 * <p>Endpoints:
 *
 * <ul>
 *   <li>GET /api/admin/xentral/settings - Get current settings
 *   <li>PUT /api/admin/xentral/settings - Update settings
 *   <li>GET /api/admin/xentral/test-connection - Test connection to Xentral API
 * </ul>
 *
 * <p>Security: @RolesAllowed("ADMIN") - Admin-only access
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.2
 */
@Path("/api/admin/xentral")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class XentralSettingsResource {

  private static final Logger logger = LoggerFactory.getLogger(XentralSettingsResource.class);

  @Inject XentralSettingsService service;

  // Xentral Connect API (2024/25) - Token-based authentication
  // Only Base URL and Personal Access Token needed
  @ConfigProperty(name = "xentral.api.base-url")
  String baseUrl;

  @ConfigProperty(name = "xentral.api.token")
  String token;

  @ConfigProperty(name = "xentral.api.mock-mode", defaultValue = "true")
  boolean mockMode;

  /**
   * Get Xentral settings.
   *
   * <p>Returns current database settings if available. Returns application.properties defaults if
   * no settings exist.
   *
   * @return XentralSettingsDTO (200 OK) - DB settings or application.properties defaults
   */
  @GET
  @Path("/settings")
  public Response getSettings() {
    logger.debug("GET /api/admin/xentral/settings");

    Optional<XentralSettingsDTO> settings = service.getSettings();

    if (settings.isPresent()) {
      logger.info("Returning Xentral settings from database");
      return Response.ok(settings.get()).build();
    } else {
      logger.info("No Xentral settings in database - returning application.properties defaults");
      // Return defaults from application.properties
      XentralSettingsDTO defaultDto = new XentralSettingsDTO(baseUrl, token, mockMode);
      return Response.ok(defaultDto).build();
    }
  }

  /**
   * Update Xentral settings.
   *
   * <p>Creates new settings if none exist, updates existing settings otherwise (UPSERT pattern).
   *
   * @param dto XentralSettingsDTO with new values
   * @return Updated XentralSettingsDTO (200 OK) or 400 if validation fails
   */
  @PUT
  @Path("/settings")
  public Response updateSettings(@Valid XentralSettingsDTO dto) {
    logger.info("PUT /api/admin/xentral/settings - mockMode={}", dto.mockMode());

    try {
      XentralSettingsDTO updated = service.updateSettings(dto);

      logger.info("Xentral settings updated successfully");
      return Response.ok(updated).build();

    } catch (IllegalArgumentException e) {
      logger.warn("Invalid Xentral settings: {}", e.getMessage());
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("{\"error\": \"" + e.getMessage() + "\"}")
          .build();
    } catch (Exception e) {
      logger.error("Failed to update Xentral settings", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(
              "{\"error\": \"Fehler beim Speichern der Einstellungen: " + e.getMessage() + "\"}")
          .build();
    }
  }

  /**
   * Test connection to Xentral API.
   *
   * <p>Attempts to fetch sales reps from Xentral API to verify connection.
   *
   * @return ConnectionTestResponse with status and message (200 OK always, check status field)
   */
  @GET
  @Path("/test-connection")
  public Response testConnection() {
    logger.info("GET /api/admin/xentral/test-connection");

    ConnectionTestResponse result = service.testConnection();

    logger.info("Connection test completed - status={}", result.status());

    // Always return 200 OK, check status field in response
    return Response.ok(result).build();
  }
}

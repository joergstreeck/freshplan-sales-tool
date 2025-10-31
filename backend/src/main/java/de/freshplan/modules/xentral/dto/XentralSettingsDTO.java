package de.freshplan.modules.xentral.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * XentralSettings DTO for Admin-UI.
 *
 * <p>Sprint 2.1.7.2 - D5: Admin-UI für Xentral-Einstellungen
 *
 * <p>Used for:
 *
 * <ul>
 *   <li>PUT /api/admin/xentral/settings (Request Body)
 *   <li>GET /api/admin/xentral/settings (Response Body)
 * </ul>
 *
 * @param apiUrl Xentral API base URL (e.g., https://644b6ff97320d.xentral.biz)
 * @param apiToken Xentral API authentication token
 * @param mockMode Mock mode flag (true = use mock, false = use real API)
 * @author FreshPlan Team
 * @since Sprint 2.1.7.2
 */
public record XentralSettingsDTO(
    @NotBlank(message = "API URL darf nicht leer sein")
        @Size(max = 255, message = "API URL darf maximal 255 Zeichen lang sein")
        @Pattern(
            regexp = "^https?://.*",
            message = "API URL muss mit http:// oder https:// beginnen")
        String apiUrl,
    @NotBlank(message = "API Token darf nicht leer sein")
        @Size(max = 500, message = "API Token darf maximal 500 Zeichen lang sein")
        String apiToken,
    @NotNull(message = "Mock-Mode darf nicht null sein") Boolean mockMode) {

  /**
   * Validates the DTO.
   *
   * @throws IllegalArgumentException if required fields are invalid
   */
  public void validate() {
    if (apiUrl == null || apiUrl.isBlank()) {
      throw new IllegalArgumentException("API URL darf nicht leer sein");
    }
    if (!apiUrl.startsWith("http://") && !apiUrl.startsWith("https://")) {
      throw new IllegalArgumentException("API URL muss mit http:// oder https:// beginnen");
    }
    if (apiToken == null || apiToken.isBlank()) {
      throw new IllegalArgumentException("API Token darf nicht leer sein");
    }
    if (mockMode == null) {
      throw new IllegalArgumentException("Mock-Mode darf nicht null sein");
    }
  }

  /**
   * Creates DTO from Entity (for GET response).
   *
   * @param entity XentralSettings entity
   * @return XentralSettingsDTO
   */
  public static XentralSettingsDTO fromEntity(
      de.freshplan.modules.xentral.entity.XentralSettings entity) {
    return new XentralSettingsDTO(entity.getApiUrl(), entity.getApiToken(), entity.getMockMode());
  }
}

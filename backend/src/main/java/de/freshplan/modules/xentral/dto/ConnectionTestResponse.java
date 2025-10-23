package de.freshplan.modules.xentral.dto;

/**
 * Connection Test Response DTO.
 *
 * <p>Sprint 2.1.7.2 - D5: Admin-UI für Xentral-Einstellungen
 *
 * <p>Used for GET /api/admin/xentral/test-connection response.
 *
 * @param status Connection status ("success" or "error")
 * @param message Detailed message (e.g., error description)
 * @author FreshPlan Team
 * @since Sprint 2.1.7.2
 */
public record ConnectionTestResponse(String status, String message) {

  /**
   * Creates a success response.
   *
   * @param message Success message
   * @return ConnectionTestResponse with status="success"
   */
  public static ConnectionTestResponse success(String message) {
    return new ConnectionTestResponse("success", message);
  }

  /**
   * Creates an error response.
   *
   * @param message Error message
   * @return ConnectionTestResponse with status="error"
   */
  public static ConnectionTestResponse error(String message) {
    return new ConnectionTestResponse("error", message);
  }
}

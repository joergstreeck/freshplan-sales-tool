package de.freshplan.modules.xentral.dto.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Xentral v2 API Customer entity (from /api/v2/customers).
 *
 * <p>Structure mirrors Xentral v25.40.2 v2 API response with nested objects:
 *
 * <pre>
 * {
 *   "id": 123,
 *   "general": { "name": "...", "email": "...", "phone": "..." },
 *   "project": { "id": 1, "name": "Hauptprojekt" },
 *   "status": "active",
 *   "salesRep": { "id": 456, "name": "John Doe" }
 * }
 * </pre>
 *
 * <p>Sprint: 2.1.7.2 - D2a Real API DTOs
 *
 * @see XentralV2CustomerResponse
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record XentralV2Customer(
    String id,
    General general,
    Project project,
    String status,
    @JsonProperty("sales_rep") SalesRep salesRep) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record General(String name, String email, String phone, String website) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Project(String id, String name) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record SalesRep(String id, String name, String email) {}

  /**
   * Validates that the customer has required fields.
   *
   * @throws IllegalArgumentException if id or general.name is missing
   */
  public void validate() {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("Customer id cannot be null or blank");
    }
    if (general == null || general.name() == null || general.name().isBlank()) {
      throw new IllegalArgumentException("Customer general.name cannot be null or blank");
    }
  }

  /**
   * Gets the customer's company name (from general.name).
   *
   * @return company name or empty string if not available
   */
  public String getCompanyName() {
    return general != null && general.name() != null ? general.name() : "";
  }

  /**
   * Gets the customer's email (from general.email).
   *
   * @return email or empty string if not available
   */
  public String getEmail() {
    return general != null && general.email() != null ? general.email() : "";
  }

  /**
   * Gets the customer's phone (from general.phone).
   *
   * @return phone or empty string if not available
   */
  public String getPhone() {
    return general != null && general.phone() != null ? general.phone() : "";
  }

  /**
   * Gets the sales rep ID (from salesRep.id).
   *
   * @return sales rep ID or null if not assigned
   */
  public String getSalesRepId() {
    return salesRep != null ? salesRep.id() : null;
  }
}

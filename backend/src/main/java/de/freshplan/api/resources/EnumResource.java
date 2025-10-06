package de.freshplan.api.resources;

import de.freshplan.domain.shared.BusinessType;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST endpoint for enum values (Business Types, etc.).
 *
 * <p>Sprint 2.1.6 Phase 2 Review: Single Source of Truth for dropdown values. No hardcoding in
 * frontend, all enum values served from backend.
 *
 * <p>GET /api/enums/business-types → [{"value": "RESTAURANT", "label": "Restaurant"}, ...]
 */
@Path("/api/enums")
@Produces(MediaType.APPLICATION_JSON)
public class EnumResource {

  /**
   * Get all business types for dropdowns.
   *
   * <p>Used by Lead and Customer forms. Frontend must NOT hardcode these values.
   *
   * @return List of business types with value (enum name) and display name
   */
  @GET
  @Path("/business-types")
  public List<EnumValue> getBusinessTypes() {
    return Arrays.stream(BusinessType.values())
        .map(type -> new EnumValue(type.name(), type.getDisplayName()))
        .collect(Collectors.toList());
  }

  /**
   * Get all lead sources for dropdowns.
   *
   * @return List of lead sources (MESSE, TELEFON, etc.)
   */
  @GET
  @Path("/lead-sources")
  public List<EnumValue> getLeadSources() {
    return List.of(
        new EnumValue("MESSE", "Messe"),
        new EnumValue("EMPFEHLUNG", "Empfehlung"),
        new EnumValue("TELEFON", "Telefon"),
        new EnumValue("WEB_FORMULAR", "Web-Formular"),
        new EnumValue("PARTNER", "Partner"),
        new EnumValue("SONSTIGE", "Sonstige"));
  }

  /**
   * Get all kitchen sizes for dropdowns.
   *
   * @return List of kitchen sizes (SMALL, MEDIUM, LARGE)
   */
  @GET
  @Path("/kitchen-sizes")
  public List<EnumValue> getKitchenSizes() {
    return List.of(
        new EnumValue("small", "Klein"),
        new EnumValue("medium", "Mittel"),
        new EnumValue("large", "Groß"));
  }

  /** Enum value DTO for frontend consumption. */
  public static class EnumValue {
    public String value;
    public String label;

    public EnumValue(String value, String label) {
      this.value = value;
      this.label = label;
    }
  }
}

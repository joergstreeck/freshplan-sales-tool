package de.freshplan.api.resources;

import de.freshplan.domain.shared.BusinessType;
import de.freshplan.domain.shared.KitchenSize;
import de.freshplan.domain.shared.LeadSource;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST API for Enum values (Single Source of Truth).
 *
 * <p>Sprint 2.1.6 Phase 5: Enum-Migration Phase 1 (Lead-Modul)
 *
 * <p>Frontend loads enum values from Backend-API instead of hardcoding. Benefits: - Consistency:
 * Same values in DB, Backend, and Frontend - Maintainability: Add new values in ONE place (Backend
 * Enum) - Type-Safety: Compiler validates all enum usage
 *
 * <p>Cache Strategy: Frontend uses React Query with 5min stale time.
 *
 * <p>Reference: ENUM_MIGRATION_STRATEGY.md - 3-Phase Migration Plan
 */
@Path("/api/enums")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Enums", description = "Enum values Single Source of Truth")
public class EnumResource {

  /**
   * Get all LeadSource enum values.
   *
   * <p>Used for: LeadWizard, LeadFilter, LeadTable
   *
   * <p>Business Rule: MESSE and TELEFON require first contact documentation (see {@link
   * LeadSource#requiresFirstContact()})
   *
   * @return List of LeadSource values with display names
   */
  @GET
  @Path("/lead-sources")
  @PermitAll
  @Operation(summary = "Get all Lead Source enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Lead Source values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getLeadSources() {
    return Arrays.stream(LeadSource.values())
        .map(source -> new EnumValue(source.name(), source.getDisplayName()))
        .toList();
  }

  /**
   * Get all BusinessType enum values.
   *
   * <p>Used for: LeadWizard, LeadFilter, CustomerForm (shared with Customer module)
   *
   * <p>Note: This enum is SHARED between Lead and Customer domains (9 values).
   *
   * @return List of BusinessType values with display names
   */
  @GET
  @Path("/business-types")
  @PermitAll
  @Operation(summary = "Get all Business Type enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Business Type values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getBusinessTypes() {
    return Arrays.stream(BusinessType.values())
        .map(type -> new EnumValue(type.name(), type.getDisplayName()))
        .toList();
  }

  /**
   * Get all KitchenSize enum values.
   *
   * <p>Used for: LeadWizard, LeadFilter, LeadScoring (Business Fit factor)
   *
   * <p>Business Rule: KitchenSize impacts LeadScoring (volume estimate + high-value check)
   *
   * @return List of KitchenSize values with display names
   */
  @GET
  @Path("/kitchen-sizes")
  @PermitAll
  @Operation(summary = "Get all Kitchen Size enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Kitchen Size values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getKitchenSizes() {
    return Arrays.stream(KitchenSize.values())
        .map(size -> new EnumValue(size.name(), size.getDisplayName()))
        .toList();
  }

  /**
   * DTO for Enum values (name + displayName).
   *
   * <p>Used by Frontend for Dropdown rendering.
   */
  public static class EnumValue {
    @Schema(description = "Enum name (e.g., 'MESSE', 'RESTAURANT')", example = "MESSE")
    public String value;

    @Schema(
        description = "Human-readable display name (e.g., 'Messe/Event', 'Restaurant')",
        example = "Messe/Event")
    public String label;

    public EnumValue(String value, String label) {
      this.value = value;
      this.label = label;
    }
  }
}

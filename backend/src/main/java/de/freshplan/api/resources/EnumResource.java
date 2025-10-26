package de.freshplan.api.resources;

import de.freshplan.domain.customer.entity.CustomerType;
import de.freshplan.domain.customer.entity.DeliveryCondition;
import de.freshplan.domain.customer.entity.ExpansionPlan;
import de.freshplan.domain.customer.entity.FinancingType;
import de.freshplan.domain.customer.entity.LegalForm;
import de.freshplan.domain.customer.entity.PaymentTerms;
import de.freshplan.domain.shared.BusinessType;
import de.freshplan.domain.shared.CountryCode;
import de.freshplan.domain.shared.KitchenSize;
import de.freshplan.domain.shared.LeadSource;
import de.freshplan.modules.leads.domain.ActivityOutcome;
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
   * Get all ActivityOutcome enum values.
   *
   * <p>Used for: ActivityDialog, LeadActivityForm
   *
   * <p>Sprint 2.1.7 Issue #126: Standardized outcomes for lead activities
   *
   * @return List of ActivityOutcome values with display names
   */
  @GET
  @Path("/activity-outcomes")
  @PermitAll
  @Operation(summary = "Get all Activity Outcome enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Activity Outcome values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getActivityOutcomes() {
    return Arrays.stream(ActivityOutcome.values())
        .map(outcome -> new EnumValue(outcome.name(), outcome.getDisplayName()))
        .toList();
  }

  /**
   * Get all FinancingType enum values.
   *
   * <p>Used for: CustomerWizard, CustomerForm
   *
   * <p>Sprint 2.1.7.2: Lead→Customer Parity - Enum API consistency
   *
   * @return List of FinancingType values with display names
   */
  @GET
  @Path("/financing-types")
  @PermitAll
  @Operation(summary = "Get all Financing Type enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Financing Type values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getFinancingTypes() {
    return Arrays.stream(FinancingType.values())
        .map(type -> new EnumValue(type.name(), type.getDisplayName()))
        .toList();
  }

  /**
   * Get all CustomerType enum values.
   *
   * <p>Used for: CustomerForm, CustomerCards
   *
   * <p>Sprint 2.1.7.2 D11: Server-Driven Customer Cards
   *
   * @return List of CustomerType values with display names
   */
  @GET
  @Path("/customer-types")
  @PermitAll
  @Operation(summary = "Get all Customer Type enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Customer Type values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getCustomerTypes() {
    return Arrays.stream(CustomerType.values())
        .map(type -> new EnumValue(type.name(), type.name()))
        .toList();
  }

  /**
   * Get all PaymentTerms enum values.
   *
   * <p>Used for: CustomerForm, CustomerCards
   *
   * <p>Sprint 2.1.7.2 D11: Server-Driven Customer Cards
   *
   * @return List of PaymentTerms values with display names
   */
  @GET
  @Path("/payment-terms")
  @PermitAll
  @Operation(summary = "Get all Payment Terms enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Payment Terms values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getPaymentTerms() {
    return Arrays.stream(PaymentTerms.values())
        .map(term -> new EnumValue(term.name(), term.name()))
        .toList();
  }

  /**
   * Get all DeliveryCondition enum values.
   *
   * <p>Used for: CustomerForm, CustomerCards
   *
   * <p>Sprint 2.1.7.2 D11: Server-Driven Customer Cards
   *
   * @return List of DeliveryCondition values with display names
   */
  @GET
  @Path("/delivery-conditions")
  @PermitAll
  @Operation(summary = "Get all Delivery Condition enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Delivery Condition values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getDeliveryConditions() {
    return Arrays.stream(DeliveryCondition.values())
        .map(condition -> new EnumValue(condition.name(), condition.name()))
        .toList();
  }

  /**
   * Get all Legal Form enum values.
   *
   * <p>Used for: CustomerForm, CustomerCards
   *
   * <p>Sprint 2.1.7.2 D11: Server-Driven Customer Cards
   *
   * @return List of Legal Form values with display names
   */
  @GET
  @Path("/legal-forms")
  @PermitAll
  @Operation(summary = "Get all Legal Form enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Legal Form values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getLegalForms() {
    return Arrays.stream(LegalForm.values())
        .map(form -> new EnumValue(form.name(), form.getDisplayName()))
        .toList();
  }

  /**
   * Get all Expansion Plan enum values.
   *
   * <p>Used for: CustomerForm, CustomerCards
   *
   * <p>Sprint 2.1.7.2 D11: Server-Driven Customer Cards
   *
   * @return List of Expansion Plan values with display names
   */
  @GET
  @Path("/expansion-plan")
  @PermitAll
  @Operation(summary = "Get all Expansion Plan enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Expansion Plan values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getExpansionPlan() {
    return Arrays.stream(ExpansionPlan.values())
        .map(plan -> new EnumValue(plan.name(), plan.getDisplayName()))
        .toList();
  }

  /**
   * Get all Country Code enum values.
   *
   * <p>Used for: CustomerForm, CustomerCards (Address Fields)
   *
   * <p>Sprint 2.1.7.2 D11: Structured Address Support (GROUP/ARRAY types)
   *
   * @return List of Country Code values with display names
   */
  @GET
  @Path("/country-codes")
  @PermitAll
  @Operation(summary = "Get all Country Code enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Country Code values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getCountryCodes() {
    return Arrays.stream(CountryCode.values())
        .map(code -> new EnumValue(code.name(), code.getDisplayName()))
        .toList();
  }

  /**
   * Get all Customer Status enum values.
   *
   * <p>Used for: CustomerForm, CustomerCards (Status Field)
   *
   * <p>Sprint 2.1.7.2 D11: Server-Driven Customer Cards
   *
   * @return List of Customer Status values with display names
   */
  @GET
  @Path("/customer-status")
  @PermitAll
  @Operation(summary = "Get all Customer Status enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Customer Status values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getCustomerStatus() {
    return Arrays.stream(de.freshplan.domain.customer.entity.CustomerStatus.values())
        .map(status -> new EnumValue(status.name(), status.name()))
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

package de.freshplan.api.resources;

import de.freshplan.domain.customer.entity.ContactRole;
import de.freshplan.domain.customer.entity.CustomerType;
import de.freshplan.domain.customer.entity.DecisionLevel;
import de.freshplan.domain.customer.entity.DeliveryCondition;
import de.freshplan.domain.customer.entity.ExpansionPlan;
import de.freshplan.domain.customer.entity.FinancingType;
import de.freshplan.domain.customer.entity.LegalForm;
import de.freshplan.domain.customer.entity.PaymentTerms;
import de.freshplan.domain.customer.entity.Salutation;
import de.freshplan.domain.customer.entity.Title;
import de.freshplan.domain.opportunity.entity.OpportunityType;
import de.freshplan.domain.shared.BudgetAvailability;
import de.freshplan.domain.shared.BusinessType;
import de.freshplan.domain.shared.CountryCode;
import de.freshplan.domain.shared.DealSize;
import de.freshplan.domain.shared.KitchenSize;
import de.freshplan.domain.shared.LeadSource;
import de.freshplan.modules.leads.domain.ActivityOutcome;
import de.freshplan.modules.leads.domain.ActivityType;
import de.freshplan.modules.leads.domain.DecisionMakerAccess;
import de.freshplan.modules.leads.domain.RelationshipStatus;
import de.freshplan.modules.leads.domain.UrgencyLevel;
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
   * Get user-selectable ActivityType enum values.
   *
   * <p>Used for: ActivityDialog, LeadActivityForm
   *
   * <p>Sprint 2.1.7.2 D11: Server-Driven UI for Activity Management
   *
   * <p><strong>Filters:</strong> Returns only user-selectable activities (10 types). System
   * activities (FIRST_CONTACT_DOCUMENTED, EMAIL_RECEIVED, LEAD_ASSIGNED) and legacy activities
   * (ORDER, STATUS_CHANGE, etc.) are excluded.
   *
   * @return List of user-selectable ActivityType values with display names (10 types)
   */
  @GET
  @Path("/activity-types")
  @PermitAll
  @Operation(summary = "Get user-selectable Activity Type enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of user-selectable Activity Type values (10 types)",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getActivityTypes() {
    return Arrays.stream(ActivityType.values())
        .filter(ActivityType::isUserSelectable) // Filter: Only user-selectable activities
        .map(type -> new EnumValue(type.name(), type.getDisplayName()))
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
   * Get all Contact Role enum values.
   *
   * <p>Used for: ContactEditDialog (Dropdown suggestions)
   *
   * <p>Sprint 2.1.7.2 D11.1: Contact Management - Backend/Frontend Parity
   *
   * <p>Business Rule: Realistic roles in German gastronomy businesses (Küchenchef, Einkaufsleiter,
   * Betriebsleiter, Geschäftsführer, etc.)
   *
   * @return List of ContactRole values with display names
   */
  @GET
  @Path("/contact-roles")
  @PermitAll
  @Operation(summary = "Get all Contact Role enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Contact Role values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getContactRoles() {
    return Arrays.stream(ContactRole.values())
        .map(role -> new EnumValue(role.name(), role.getDisplayName()))
        .toList();
  }

  /**
   * Get all Salutation enum values.
   *
   * <p>Used for: ContactEditDialog (Dropdown)
   *
   * <p>Sprint 2.1.7.2 D11.1: Contact Management - Backend/Frontend Parity
   *
   * <p>Business Rule: German business etiquette requires proper salutations (Herr, Frau, Divers)
   *
   * @return List of Salutation values with display names
   */
  @GET
  @Path("/salutations")
  @PermitAll
  @Operation(summary = "Get all Salutation enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Salutation values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getSalutations() {
    return Arrays.stream(Salutation.values())
        .map(salutation -> new EnumValue(salutation.name(), salutation.getDisplayName()))
        .toList();
  }

  /**
   * Get all Decision Level enum values.
   *
   * <p>Used for: ContactEditDialog (Dropdown)
   *
   * <p>Sprint 2.1.7.2 D11.1: Contact Management - Backend/Frontend Parity
   *
   * <p>Business Rule: Understanding decision-making level is crucial for B2B sales strategy
   * (Executive, Manager, Operational, Influencer)
   *
   * @return List of DecisionLevel values with display names
   */
  @GET
  @Path("/decision-levels")
  @PermitAll
  @Operation(summary = "Get all Decision Level enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Decision Level values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getDecisionLevels() {
    return Arrays.stream(DecisionLevel.values())
        .map(level -> new EnumValue(level.name(), level.getDisplayName()))
        .toList();
  }

  /**
   * Get all Title enum values.
   *
   * <p>Used for: ContactEditDialog (Dropdown)
   *
   * <p>Sprint 2.1.7.2 D11.1: Contact Management - Backend/Frontend Parity
   *
   * <p>Business Rule: German business etiquette requires proper titles in formal correspondence
   * (letters, emails). Common titles: Dr., Prof., Dipl.-Ing., M.Sc., etc.
   *
   * @return List of Title values with display names
   */
  @GET
  @Path("/titles")
  @PermitAll
  @Operation(summary = "Get all Title enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Title values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getTitles() {
    return Arrays.stream(Title.values())
        .map(title -> new EnumValue(title.name(), title.getDisplayName()))
        .toList();
  }

  /**
   * Get all RelationshipStatus enum values.
   *
   * <p>Used for: LeadWizard Stage 2, EngagementScoreForm, LeadDetailView
   *
   * <p>Sprint 2.1.7.2 D11.2: Server-Driven UI for Lead Scoring (Engagement Score)
   *
   * <p>Business Rule: Relationship status impacts Engagement Score (40% weight). Values range from
   * COLD (0 pts) to ADVOCATE (25 pts).
   *
   * @return List of RelationshipStatus values with display names
   */
  @GET
  @Path("/relationship-status")
  @PermitAll
  @Operation(summary = "Get all Relationship Status enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Relationship Status values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getRelationshipStatus() {
    return Arrays.stream(RelationshipStatus.values())
        .map(status -> new EnumValue(status.name(), status.getLabel()))
        .toList();
  }

  /**
   * Get all DecisionMakerAccess enum values.
   *
   * <p>Used for: LeadWizard Stage 2, EngagementScoreForm, LeadDetailView
   *
   * <p>Sprint 2.1.7.2 D11.2: Server-Driven UI for Lead Scoring (Engagement Score)
   *
   * <p>Business Rule: Decision maker access is THE critical factor for win rate (60% weight).
   * IS_DECISION_MAKER = ~70-80% win rate, BLOCKED/UNKNOWN = ~10-15% win rate.
   *
   * @return List of DecisionMakerAccess values with display names
   */
  @GET
  @Path("/decision-maker-access")
  @PermitAll
  @Operation(summary = "Get all Decision Maker Access enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Decision Maker Access values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getDecisionMakerAccess() {
    return Arrays.stream(DecisionMakerAccess.values())
        .map(access -> new EnumValue(access.name(), access.getLabel()))
        .toList();
  }

  /**
   * Get all UrgencyLevel enum values.
   *
   * <p>Used for: LeadWizard Stage 2, PainScoreForm, LeadDetailView
   *
   * <p>Sprint 2.1.7.2 D11.2: Server-Driven UI for Lead Scoring (Pain Score)
   *
   * <p>Business Rule: Urgency level impacts Dringlichkeit dimension (40% weight). Values range from
   * NORMAL (0 pts) to EMERGENCY (25 pts). High pain + high urgency = hot lead.
   *
   * @return List of UrgencyLevel values with display names
   */
  @GET
  @Path("/urgency-levels")
  @PermitAll
  @Operation(summary = "Get all Urgency Level enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Urgency Level values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getUrgencyLevels() {
    return Arrays.stream(UrgencyLevel.values())
        .map(level -> new EnumValue(level.name(), level.getLabel()))
        .toList();
  }

  /**
   * Get all BudgetAvailability enum values.
   *
   * <p>Used for: LeadWizard Stage 2, RevenueScoreForm, LeadDetailView
   *
   * <p>Sprint 2.1.7.2 D11.2: Server-Driven UI for Lead Scoring (Revenue Score)
   *
   * <p>Business Rule: Budget availability is critical for sales prioritization. YES = high
   * priority, NO = nurturing lead.
   *
   * @return List of BudgetAvailability values with display names
   */
  @GET
  @Path("/budget-availability")
  @PermitAll
  @Operation(summary = "Get all Budget Availability enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Budget Availability values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getBudgetAvailability() {
    return Arrays.stream(BudgetAvailability.values())
        .map(budget -> new EnumValue(budget.name(), budget.getLabel()))
        .toList();
  }

  /**
   * Get all DealSize enum values.
   *
   * <p>Used for: LeadWizard Stage 2, RevenueScoreForm, LeadDetailView
   *
   * <p>Sprint 2.1.7.2 D11.2: Server-Driven UI for Lead Scoring (Revenue Score)
   *
   * <p>Business Rule: Deal size impacts Revenue Score (25% of total lead score). Values range from
   * SMALL (25 pts) to ENTERPRISE (100 pts).
   *
   * @return List of DealSize values with display names
   */
  @GET
  @Path("/deal-sizes")
  @PermitAll
  @Operation(summary = "Get all Deal Size enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Deal Size values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getDealSizes() {
    return Arrays.stream(DealSize.values())
        .map(size -> new EnumValue(size.name(), size.getDisplayName()))
        .toList();
  }

  /**
   * Get all OpportunityType enum values.
   *
   * <p>Used for: CreateOpportunityDialog, OpportunityForm
   *
   * <p>Sprint 2.1.7.7: Schema-Driven Forms Migration
   *
   * <p>Business Rule: Freshfoodz-specific opportunity types (Neugeschäft, Sortimentserweiterung,
   * Neuer Standort, Vertragsverlängerung).
   *
   * @return List of OpportunityType values with display names
   */
  @GET
  @Path("/opportunity-types")
  @PermitAll
  @Operation(summary = "Get all Opportunity Type enum values")
  @APIResponse(
      responseCode = "200",
      description = "List of Opportunity Type values",
      content = @Content(schema = @Schema(implementation = EnumValue.class)))
  public List<EnumValue> getOpportunityTypes() {
    return Arrays.stream(OpportunityType.values())
        .map(type -> new EnumValue(type.name(), type.getLabel()))
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

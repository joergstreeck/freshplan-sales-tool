package de.freshplan.modules.leads.api;

import de.freshplan.domain.customer.dto.*;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Business Potential Schema Resource for Server-Driven UI
 *
 * <p>Sprint 2.1.7.2 D11: Server-Driven UI for Business Potential Assessment
 *
 * <p>Provides schema definition for Business Potential dialog (subset of Lead fields).
 *
 * <p>Frontend fetches this schema from `GET /api/business-potentials/schema` and renders form
 * dynamically.
 *
 * <p><strong>Architecture:</strong> Backend = Single Source of Truth for schema + data Frontend =
 * Rendering Layer (no hardcoded schemas)
 *
 * <p><strong>Business Potential Fields:</strong>
 *
 * <ul>
 *   <li>businessType - Geschäftsart (Restaurant, Hotel, Kantine, etc.)
 *   <li>estimatedBudget - Geschätztes jährliches Budget (maps to Lead.estimatedVolume)
 *   <li>decisionTimeframe - Entscheidungszeitraum (TODO: Add to Lead entity in future migration)
 *   <li>notes - Notizen zum Geschäftspotenzial (TODO: Add to Lead entity or map to existing notes)
 * </ul>
 *
 * <p><strong>Benefits:</strong>
 *
 * <ul>
 *   <li>Backend controls Business Potential form structure
 *   <li>Enum sources from backend (/api/enums/...)
 *   <li>No frontend/backend parity issues
 *   <li>Used by BusinessPotentialDialog component
 * </ul>
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/business-potentials/schema")
@Produces(MediaType.APPLICATION_JSON)
@Tag(
    name = "Business Potential Schema",
    description = "Server-Driven UI Schema for Business Potential Assessment")
public class BusinessPotentialSchemaResource {

  /**
   * Get schema for Business Potential assessment dialog
   *
   * <p>Returns field definitions for dynamic rendering in BusinessPotentialDialog.
   *
   * <p><strong>Section: Potenzial-Bewertung</strong>
   *
   * <p>Fields:
   *
   * <ol>
   *   <li>businessType (ENUM, required) - Geschäftsart
   *   <li>estimatedBudget (CURRENCY) - Geschätztes Budget
   *   <li>decisionTimeframe (ENUM) - Entscheidungszeitraum
   *   <li>notes (TEXTAREA) - Notizen
   * </ol>
   *
   * @return Business Potential Schema with 1 section (potential_assessment)
   */
  @GET
  @PermitAll
  @Operation(summary = "Get Business Potential Schema (Assessment Dialog)")
  @APIResponse(
      responseCode = "200",
      description = "Business Potential Schema",
      content = @Content(schema = @Schema(implementation = CustomerCardSchema.class)))
  public Response getBusinessPotentialSchema() {

    // Single card with 1 section (Potenzial-Bewertung)
    CustomerCardSchema businessPotentialSchema =
        CustomerCardSchema.builder()
            .cardId("business_potential")
            .title("Geschäftspotenzial")
            .subtitle("Bewertung des Geschäftspotenzials für Lead")
            .icon("💼")
            .order(1)
            .sections(List.of(buildPotentialAssessmentSection()))
            .build();

    return Response.ok(List.of(businessPotentialSchema)).build();
  }

  /**
   * Section: Potenzial-Bewertung
   *
   * <p>Business Potential assessment fields for Lead qualification.
   *
   * <p>Fields:
   *
   * <ul>
   *   <li>businessType - Required ENUM field (Restaurant, Hotel, Kantine, etc.)
   *   <li>estimatedBudget - CURRENCY field (Jährliches Budget für Lebensmittel/Getränke)
   *   <li>decisionTimeframe - ENUM field (Entscheidungszeitraum: Sofort, 1-3 Monate, etc.)
   *   <li>notes - TEXTAREA field (Weitere Informationen zum Geschäftspotenzial)
   * </ul>
   */
  private CardSection buildPotentialAssessmentSection() {
    return CardSection.builder()
        .sectionId("potential_assessment")
        .title("Potenzial-Bewertung")
        .subtitle("Geschäftsart, Budget, Entscheidungszeitraum")
        .fields(
            List.of(
                FieldDefinition.builder()
                    .fieldKey("businessType")
                    .label("Geschäftsart")
                    .type(FieldType.ENUM)
                    .enumSource(
                        "/api/enums/business-types") // RESTAURANT, HOTEL, KANTINE, CATERING, etc.
                    .required(true)
                    .gridCols(6)
                    .helpText("Branche des Unternehmens")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("estimatedBudget")
                    .label("Geschätztes Budget")
                    .type(FieldType.CURRENCY)
                    .gridCols(6)
                    .placeholder("z.B. 100000")
                    .helpText("Jährliches Budget für Lebensmittel/Getränke")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("decisionTimeframe")
                    .label("Entscheidungszeitraum")
                    .type(FieldType.ENUM)
                    .enumSource(
                        "/api/enums/decision-timeframes") // SOFORT, 1_3_MONATE, 3_6_MONATE, LAENGER
                    .gridCols(6)
                    .helpText("Wann wird eine Entscheidung erwartet?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("notes")
                    .label("Notizen")
                    .type(FieldType.TEXTAREA)
                    .gridCols(12)
                    .placeholder("Weitere Informationen zum Geschäftspotenzial...")
                    .helpText("Zusätzliche Details zur Potenzial-Bewertung")
                    .build()))
        .collapsible(false)
        .defaultCollapsed(false)
        .build();
  }
}

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
 * Lead Schema Resource for Server-Driven UI
 *
 * <p>Sprint 2.1.7.2 D11.2: Server-Driven Lead Forms (Progressive Profiling)
 *
 * <p>Provides schema definitions for Lead sections (Progressive Wizard + Edit Dialog).
 *
 * <p>Frontend fetches this schema from `GET /api/leads/schema` and renders forms dynamically.
 *
 * <p><strong>Architecture:</strong> Backend = Single Source of Truth for schema + data Frontend =
 * Rendering Layer (no hardcoded schemas, no fieldCatalog.json)
 *
 * <p><strong>Progressive Profiling:</strong>
 *
 * <ul>
 *   <li>Stage 0: Pre-Claim (MESSE, EMPFEHLUNG) - Minimal fields for 10-day protection
 *   <li>Stage 1: Vollschutz (6 Monate) - Extended business information
 *   <li>Stage 2: Nurturing - Pain Points, Relationship, Scores
 * </ul>
 *
 * <p><strong>Benefits:</strong>
 *
 * <ul>
 *   <li>Backend controls Lead form structure
 *   <li>Progressive Profiling stages defined in backend
 *   <li>Enum sources from backend (/api/enums/...)
 *   <li>No frontend/backend parity issues
 *   <li>LeadWizard + LeadEditDialog use SAME schema
 * </ul>
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/leads/schema")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Lead Schema", description = "Server-Driven UI Schema for Lead Forms (Progressive Profiling)")
public class LeadSchemaResource {

  /**
   * Get schema for Lead sections (Progressive Profiling + Edit Dialog)
   *
   * <p>Returns field definitions for dynamic rendering in LeadWizard and LeadEditDialog.
   *
   * <p><strong>Sections (3 stages for Progressive Profiling):</strong>
   *
   * <ol>
   *   <li>🎯 stage_0_pre_claim - Basis-Informationen (Pre-Claim, 10 Tage Schutz)
   *   <li>💼 stage_1_vollschutz - Erweiterte Informationen (Vollschutz, 6 Monate)
   *   <li>📊 stage_2_nurturing - Nurturing & Qualifikation (Pain Points, Relationship)
   * </ol>
   *
   * @return Lead Schema with 3 sections (Progressive Profiling stages)
   */
  @GET
  @PermitAll
  @Operation(summary = "Get Lead Schema (Progressive Profiling: 3 Stages)")
  @APIResponse(
      responseCode = "200",
      description = "Lead Schema (Progressive Profiling)",
      content = @Content(schema = @Schema(implementation = CustomerCardSchema.class)))
  public Response getLeadSchema() {

    // Single card with 3 sections (Progressive Profiling stages)
    CustomerCardSchema leadSchema =
        CustomerCardSchema.builder()
            .cardId("lead_progressive_profiling")
            .title("Lead erfassen")
            .subtitle("Progressive Profiling: Schrittweise Lead-Qualifizierung")
            .icon("🎯")
            .order(1)
            .sections(
                List.of(
                    buildStage0Section(), // Section 1: Pre-Claim (MESSE, EMPFEHLUNG)
                    buildStage1Section(), // Section 2: Vollschutz (6 Monate)
                    buildStage2Section() // Section 3: Nurturing (Pain, Relationship)
                    ))
            .build();

    return Response.ok(List.of(leadSchema)).build();
  }

  // ========== STAGE 0: PRE-CLAIM (MESSE, EMPFEHLUNG) ==========

  /**
   * Stage 0: 🎯 Pre-Claim (MESSE, EMPFEHLUNG)
   *
   * <p>Minimale Felder für 10 Tage Lead-Schutz
   *
   * <p>Source = MESSE | EMPFEHLUNG → Pre-Claim (10 Tage)
   *
   * <p>Fields: companyName, source, website, phone, email, street, postalCode, city
   */
  private CardSection buildStage0Section() {
    return CardSection.builder()
        .sectionId("stage_0_pre_claim")
        .title("Basis-Informationen (Pre-Claim)")
        .subtitle("Pflichtfelder für Lead-Schutz (10 Tage)")
        .fields(
            List.of(
                FieldDefinition.builder()
                    .fieldKey("companyName")
                    .label("Firmenname")
                    .type(FieldType.TEXT)
                    .required(true)
                    .gridCols(12)
                    .placeholder("z.B. Hotel Adlon Kempinski Berlin")
                    .helpText("Der offizielle Firmenname des Leads")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("source")
                    .label("Quelle")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/lead-sources") // MESSE, EMPFEHLUNG, TELEFON, WEB_FORMULAR, PARTNER, SONSTIGE
                    .required(true)
                    .gridCols(6)
                    .helpText("Woher kommt der Lead? (MESSE/EMPFEHLUNG = Pre-Claim 10 Tage)")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("website")
                    .label("Website")
                    .type(FieldType.TEXT)
                    .gridCols(6)
                    .placeholder("https://www.example.com")
                    .helpText("Unternehmens-Website (optional)")
                    .showDividerAfter(true)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("phone")
                    .label("Telefon")
                    .type(FieldType.TEXT)
                    .gridCols(6)
                    .placeholder("+49 30 12345678")
                    .helpText("Haupttelefonnummer (optional)")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("email")
                    .label("E-Mail")
                    .type(FieldType.TEXT)
                    .gridCols(6)
                    .placeholder("info@example.com")
                    .helpText("Haupt-E-Mail-Adresse (optional)")
                    .showDividerAfter(true)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("street")
                    .label("Straße")
                    .type(FieldType.TEXT)
                    .gridCols(12)
                    .placeholder("Unter den Linden 77")
                    .helpText("Straße und Hausnummer (optional)")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("postalCode")
                    .label("PLZ")
                    .type(FieldType.TEXT)
                    .gridCols(4)
                    .placeholder("10117")
                    .helpText("Postleitzahl (optional)")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("city")
                    .label("Stadt")
                    .type(FieldType.TEXT)
                    .gridCols(8)
                    .placeholder("Berlin")
                    .helpText("Stadt (optional)")
                    .build()))
        .collapsible(false)
        .defaultCollapsed(false)
        .build();
  }

  // ========== STAGE 1: VOLLSCHUTZ (6 MONATE) ==========

  /**
   * Stage 1: 💼 Vollschutz (6 Monate)
   *
   * <p>Erweiterte Business-Informationen für 6 Monate Lead-Schutz
   *
   * <p>Stage 1 completed → Vollschutz (6 Monate)
   *
   * <p>Fields: businessType, kitchenSize, employeeCount, estimatedVolume, branchCount, isChain
   */
  private CardSection buildStage1Section() {
    return CardSection.builder()
        .sectionId("stage_1_vollschutz")
        .title("Erweiterte Informationen (Vollschutz)")
        .subtitle("Business Type, Budget, Mitarbeiter, Filialstruktur")
        .fields(
            List.of(
                FieldDefinition.builder()
                    .fieldKey("businessType")
                    .label("Geschäftsart")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/business-types") // RESTAURANT, HOTEL, KANTINE, CATERING, etc.
                    .gridCols(6)
                    .helpText("Branche des Unternehmens")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("kitchenSize")
                    .label("Küchengröße")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/kitchen-sizes") // KLEIN, MITTEL, GROSS, SEHR_GROSS
                    .gridCols(6)
                    .helpText("Größe der Küche / Produktionskapazität")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("employeeCount")
                    .label("Mitarbeiteranzahl")
                    .type(FieldType.NUMBER)
                    .gridCols(6)
                    .placeholder("z.B. 50")
                    .helpText("Anzahl Mitarbeiter gesamt")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("estimatedVolume")
                    .label("Geschätztes Jahresvolumen (€)")
                    .type(FieldType.CURRENCY)
                    .gridCols(6)
                    .placeholder("z.B. 100000")
                    .helpText("Geschätztes jährliches Einkaufsvolumen Lebensmittel/Getränke")
                    .showDividerAfter(true)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("branchCount")
                    .label("Anzahl Filialen/Standorte")
                    .type(FieldType.NUMBER)
                    .gridCols(6)
                    .placeholder("z.B. 1")
                    .helpText("Anzahl Filialen/Standorte (1 = Einzelstandort)")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("isChain")
                    .label("Kettenbetrieb")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Ist dies ein Kettenbetrieb? (mehrere Standorte mit zentraler Verwaltung)")
                    .build()))
        .collapsible(false)
        .defaultCollapsed(false)
        .build();
  }

  // ========== STAGE 2: NURTURING & QUALIFIKATION ==========

  /**
   * Stage 2: 📊 Nurturing & Qualifikation
   *
   * <p>Pain Points, Relationship Status, Decision Maker Access
   *
   * <p>Stage 2: Advanced Lead Nurturing & Qualification
   *
   * <p>Fields: Pain Points (9 booleans), painNotes, relationshipStatus, decisionMakerAccess
   */
  private CardSection buildStage2Section() {
    return CardSection.builder()
        .sectionId("stage_2_nurturing")
        .title("Nurturing & Qualifikation")
        .subtitle("Pain Points, Beziehungsstatus, Entscheider-Zugang")
        .fields(
            List.of(
                // ========== PAIN POINTS ==========
                FieldDefinition.builder()
                    .fieldKey("painStaffShortage")
                    .label("Personalmangel")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Leidet der Betrieb unter Personalmangel?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painHighCosts")
                    .label("Hohe Kosten")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Sind die Einkaufskosten zu hoch?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painFoodWaste")
                    .label("Lebensmittelverschwendung")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Gibt es Probleme mit Lebensmittelverschwendung?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painQualityInconsistency")
                    .label("Qualitätsschwankungen")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Gibt es Qualitätsschwankungen bei Produkten?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painTimePressure")
                    .label("Zeitdruck")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Besteht Zeitdruck in der Küche/Produktion?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painSupplierQuality")
                    .label("Lieferanten-Qualität")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Gibt es Probleme mit der Qualität des aktuellen Lieferanten?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painUnreliableDelivery")
                    .label("Unzuverlässige Lieferung")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Ist die Lieferung des aktuellen Lieferanten unzuverlässig?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painPoorService")
                    .label("Schlechter Service")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Ist der Service des aktuellen Lieferanten schlecht?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painNotes")
                    .label("Notizen zu Schmerzpunkten")
                    .type(FieldType.TEXTAREA)
                    .gridCols(12)
                    .placeholder("Weitere Schmerzpunkte oder Details...")
                    .helpText("Freitext für zusätzliche Pain Points")
                    .showDividerAfter(true)
                    .build(),
                // ========== RELATIONSHIP ==========
                FieldDefinition.builder()
                    .fieldKey("relationshipStatus")
                    .label("Beziehungsstatus")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/relationship-status") // COLD, WARM, HOT, CHAMPION
                    .gridCols(6)
                    .helpText("Wie ist der aktuelle Beziehungsstatus?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("decisionMakerAccess")
                    .label("Entscheider-Zugang")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/decision-maker-access") // UNKNOWN, GATEKEEPER, INFLUENCER, DECISION_MAKER, EXECUTIVE
                    .gridCols(6)
                    .helpText("Haben wir Zugang zum Entscheider?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("competitorInUse")
                    .label("Aktueller Wettbewerber")
                    .type(FieldType.TEXT)
                    .gridCols(6)
                    .placeholder("z.B. Metro, Transgourmet")
                    .helpText("Welcher Wettbewerber wird aktuell genutzt?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("internalChampionName")
                    .label("Interner Champion")
                    .type(FieldType.TEXT)
                    .gridCols(6)
                    .placeholder("z.B. Max Mustermann")
                    .helpText("Name des internen Champions (falls vorhanden)")
                    .build()))
        .collapsible(false)
        .defaultCollapsed(false)
        .build();
  }
}

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
 * Score Schema Resource for Server-Driven UI
 *
 * <p>Sprint 2.1.7.2 D11.2: Server-Driven UI for Lead Scoring (Pain, Revenue, Engagement)
 *
 * <p>Provides schema definitions for 3 separate scoring forms:
 *
 * <ul>
 *   <li>Pain Score - Schmerzpunkte-Bewertung (8 Boolean Pain Points + Notes)
 *   <li>Revenue Score - Umsatzpotenzial-Bewertung
 *   <li>Engagement Score - Engagement-Level-Bewertung
 * </ul>
 *
 * <p>Frontend fetches this schema from `GET /api/scores/schema` and renders 3 separate scoring
 * dialogs.
 *
 * <p><strong>CRITICAL:</strong> Schemas MUST match LeadSchemaResource Stage 2 fields to ensure
 * consistency across Lead Wizard and Score Forms!
 *
 * <p><strong>Architecture:</strong>
 *
 * <ul>
 *   <li>Backend = Single Source of Truth for schema + data
 *   <li>Frontend = Rendering Layer (no hardcoded schemas)
 *   <li>Pain Points use BOOLEAN (not ENUM) to match Lead + Customer entities
 * </ul>
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/scores/schema")
@Produces(MediaType.APPLICATION_JSON)
@Tag(
    name = "Score Schema",
    description = "Server-Driven UI Schema for Lead Scoring (Pain, Revenue, Engagement)")
public class ScoreSchemaResource {

  /**
   * Get schemas for all 3 scoring forms
   *
   * <p>Returns 3 CardSchema objects:
   *
   * <ol>
   *   <li>Pain Score Schema (cardId: pain_score)
   *   <li>Revenue Score Schema (cardId: revenue_score)
   *   <li>Engagement Score Schema (cardId: engagement_score)
   * </ol>
   *
   * @return List of 3 CardSchemas for scoring forms
   */
  @GET
  @PermitAll
  @Operation(summary = "Get Score Schemas (Pain, Revenue, Engagement)")
  @APIResponse(
      responseCode = "200",
      description = "Score Schemas (3 separate forms)",
      content = @Content(schema = @Schema(implementation = CustomerCardSchema.class)))
  public Response getScoreSchemas() {
    return Response.ok(
            List.of(
                buildPainScoreSchema(), buildRevenueScoreSchema(), buildEngagementScoreSchema()))
        .build();
  }

  // ========== PAIN SCORE SCHEMA ==========

  /**
   * Pain Score Schema
   *
   * <p>Schmerzpunkte des Kunden bewerten
   *
   * <p><strong>CRITICAL:</strong> Pain Points use BOOLEAN (not ENUM) to match:
   *
   * <ul>
   *   <li>LeadSchemaResource Stage 2 (Lines 312-367)
   *   <li>CustomerSchemaResource Pain Points Card (Lines 637-692)
   *   <li>Lead.java + Customer.java entities (Boolean fields)
   * </ul>
   *
   * <p>Fields (8 boolean pain points + urgency + notes):
   *
   * <ul>
   *   <li>painStaffShortage - Personalmangel (BOOLEAN)
   *   <li>painHighCosts - Hohe Kosten (BOOLEAN)
   *   <li>painFoodWaste - Lebensmittelverschwendung (BOOLEAN)
   *   <li>painQualityInconsistency - Qualitätsschwankungen (BOOLEAN)
   *   <li>painTimePressure - Zeitdruck (BOOLEAN)
   *   <li>painSupplierQuality - Lieferanten-Qualität (BOOLEAN)
   *   <li>painUnreliableDelivery - Unzuverlässige Lieferung (BOOLEAN)
   *   <li>painPoorService - Schlechter Service (BOOLEAN)
   *   <li>urgencyLevel - Dringlichkeitsstufe (ENUM: NORMAL, MEDIUM, HIGH, EMERGENCY)
   *   <li>painNotes - Notizen zu Schmerzpunkten (TEXTAREA)
   * </ul>
   */
  private CustomerCardSchema buildPainScoreSchema() {
    return CustomerCardSchema.builder()
        .cardId("pain_score")
        .title("Pain Score")
        .subtitle("Schmerzpunkte des Kunden bewerten")
        .icon("⚠️")
        .order(1)
        .sections(List.of(buildPainPointsSection()))
        .build();
  }

  private CardSection buildPainPointsSection() {
    return CardSection.builder()
        .sectionId("pain_points")
        .title("Schmerzpunkte")
        .subtitle("Herausforderungen des Kunden identifizieren")
        .fields(
            List.of(
                // ========== 8 BOOLEAN PAIN POINTS (from LeadSchemaResource Stage 2) ==========
                FieldDefinition.builder()
                    .fieldKey("painStaffShortage")
                    .label("Personalmangel / Fachkräftemangel")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Leidet der Betrieb unter Personalmangel?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painHighCosts")
                    .label("Hoher Kostendruck")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Sind die Einkaufskosten zu hoch?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painFoodWaste")
                    .label("Food Waste / Überproduktion")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Gibt es Probleme mit Lebensmittelverschwendung?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painQualityInconsistency")
                    .label("Interne Qualitätsinkonsistenz")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Gibt es Qualitätsschwankungen bei Produkten?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painTimePressure")
                    .label("Zeitdruck / Effizienz")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Besteht Zeitdruck in der Küche/Produktion?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painSupplierQuality")
                    .label("Qualitätsprobleme beim Lieferanten")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Gibt es Probleme mit der Qualität des aktuellen Lieferanten?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painUnreliableDelivery")
                    .label("Unzuverlässige Lieferzeiten")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Ist die Lieferung des aktuellen Lieferanten unzuverlässig?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painPoorService")
                    .label("Schlechter Service/Support")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Ist der Service des aktuellen Lieferanten schlecht?")
                    .build(),
                // ========== URGENCY LEVEL ==========
                FieldDefinition.builder()
                    .fieldKey("urgencyLevel")
                    .label("Dringlichkeitsstufe")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/urgency-levels") // NORMAL, MEDIUM, HIGH, EMERGENCY
                    .gridCols(6)
                    .helpText("Wie dringlich ist die Lösung dieser Probleme?")
                    .build(),
                // ========== NOTES ==========
                FieldDefinition.builder()
                    .fieldKey("painNotes")
                    .label("Weitere Details zu Pain-Faktoren (optional)")
                    .type(FieldType.TEXTAREA)
                    .gridCols(12)
                    .placeholder(
                        "Beschreiben Sie konkrete Probleme, Auswirkungen oder besondere"
                            + " Umstände...")
                    .helpText("Freitext für zusätzliche Pain Points")
                    .build()))
        .collapsible(false)
        .defaultCollapsed(false)
        .build();
  }

  // ========== REVENUE SCORE SCHEMA ==========

  /**
   * Revenue Score Schema
   *
   * <p>Umsatzpotenzial des Kunden bewerten
   *
   * <p>Fields:
   *
   * <ul>
   *   <li>estimatedAnnualRevenue - Geschätzter Jahresumsatz (CURRENCY)
   *   <li>budgetAvailable - Budget verfügbar (ENUM: YES, NO, UNKNOWN)
   *   <li>contractValue - Vertragswert (CURRENCY)
   *   <li>dealSize - Deal-Größe (ENUM: SMALL, MEDIUM, LARGE, ENTERPRISE)
   *   <li>revenueNotes - Notizen zum Umsatzpotenzial (TEXTAREA)
   * </ul>
   */
  private CustomerCardSchema buildRevenueScoreSchema() {
    return CustomerCardSchema.builder()
        .cardId("revenue_score")
        .title("Revenue Score")
        .subtitle("Umsatzpotenzial des Kunden bewerten")
        .icon("💰")
        .order(2)
        .sections(List.of(buildRevenuePotentialSection()))
        .build();
  }

  private CardSection buildRevenuePotentialSection() {
    return CardSection.builder()
        .sectionId("revenue_potential")
        .title("Umsatzpotenzial")
        .subtitle("Bewerten Sie das geschätzte Umsatzpotenzial")
        .fields(
            List.of(
                // ========== estimatedVolume (jährlich!) ==========
                FieldDefinition.builder()
                    .fieldKey("estimatedVolume")
                    .label("Geschätztes Jahresvolumen (€)")
                    .type(FieldType.CURRENCY)
                    .gridCols(6)
                    .placeholder("z.B. 100000")
                    .helpText("Geschätztes jährliches Einkaufsvolumen Lebensmittel/Getränke")
                    .build(),
                // ========== budgetConfirmed (BOOLEAN!) ==========
                FieldDefinition.builder()
                    .fieldKey("budgetConfirmed")
                    .label("Budget freigegeben / bestätigt")
                    .type(FieldType.BOOLEAN)
                    .gridCols(12)
                    .helpText("Hat der Kunde Budget für Einkäufe bestätigt?")
                    .build()))
        .collapsible(false)
        .defaultCollapsed(false)
        .build();
  }

  // ========== ENGAGEMENT SCORE SCHEMA ==========

  /**
   * Engagement Score Schema
   *
   * <p>Engagement-Level des Kunden bewerten
   *
   * <p><strong>CRITICAL:</strong> Fields match LeadSchemaResource Stage 2 (Lines 377-408)
   *
   * <p>Fields:
   *
   * <ul>
   *   <li>relationshipStatus - Beziehungsstatus (ENUM: COLD, WARM, HOT, CHAMPION)
   *   <li>decisionMakerAccess - Entscheider-Zugang (ENUM: UNKNOWN, GATEKEEPER, INFLUENCER,
   *       DECISION_MAKER, EXECUTIVE)
   *   <li>competitorInUse - Aktueller Wettbewerber (TEXT)
   *   <li>internalChampionName - Interner Champion (TEXT)
   *   <li>engagementNotes - Notizen zum Engagement (TEXTAREA)
   * </ul>
   */
  private CustomerCardSchema buildEngagementScoreSchema() {
    return CustomerCardSchema.builder()
        .cardId("engagement_score")
        .title("Engagement Score")
        .subtitle("Engagement-Level des Kunden bewerten")
        .icon("🤝")
        .order(3)
        .sections(List.of(buildEngagementMetricsSection()))
        .build();
  }

  private CardSection buildEngagementMetricsSection() {
    return CardSection.builder()
        .sectionId("engagement_metrics")
        .title("Beziehungsebene")
        .subtitle("Bewerten Sie die Beziehung zum Kunden")
        .fields(
            List.of(
                FieldDefinition.builder()
                    .fieldKey("relationshipStatus")
                    .label("Beziehungsqualität")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/relationship-status") // COLD, WARM, HOT, CHAMPION
                    .gridCols(6)
                    .helpText("Wie ist der aktuelle Beziehungsstatus?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("decisionMakerAccess")
                    .label("Entscheider-Zugang")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/decision-maker-access") // UNKNOWN, GATEKEEPER,
                    // INFLUENCER, DECISION_MAKER,
                    // EXECUTIVE
                    .gridCols(6)
                    .helpText("Haben wir Zugang zum Entscheider?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("internalChampionName")
                    .label("Fürsprecher im Unternehmen")
                    .type(FieldType.TEXT)
                    .gridCols(6)
                    .placeholder("Name des Fürsprechers")
                    .helpText("+30 Punkte wenn vorhanden")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("competitorInUse")
                    .label("Aktueller Wettbewerber")
                    .type(FieldType.TEXT)
                    .gridCols(6)
                    .placeholder("z.B. Metro, CHEFS CULINAR")
                    .helpText("Welcher Lieferant wird aktuell genutzt?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("engagementNotes")
                    .label("Notizen zum Engagement (optional)")
                    .type(FieldType.TEXTAREA)
                    .gridCols(12)
                    .placeholder("Weitere Informationen zum Engagement...")
                    .helpText("Freitext für zusätzliche Details")
                    .build()))
        .collapsible(false)
        .defaultCollapsed(false)
        .build();
  }
}

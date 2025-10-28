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
 * <p>Sprint 2.1.7.2 D11: Server-Driven UI for Lead Scoring (Pain, Revenue, Engagement)
 *
 * <p>Provides schema definitions for 3 separate scoring forms:
 *
 * <ul>
 *   <li>Pain Score - Schmerzpunkte-Bewertung (OPERATIONAL, DELIVERY, SERVICE)
 *   <li>Revenue Score - Umsatzpotenzial-Bewertung
 *   <li>Engagement Score - Engagement-Level-Bewertung
 * </ul>
 *
 * <p>Frontend fetches this schema from `GET /api/scores/schema` and renders 3 separate scoring
 * dialogs.
 *
 * <p><strong>Architecture:</strong> Backend = Single Source of Truth for schema + data Frontend =
 * Rendering Layer (no hardcoded schemas)
 *
 * <p><strong>Benefits:</strong>
 *
 * <ul>
 *   <li>Backend controls Score form structures
 *   <li>Enum sources from backend (/api/enums/...)
 *   <li>No frontend/backend parity issues
 *   <li>3 separate scoring forms for comprehensive Lead qualification
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
   * <p>Schmerzpunkte des Kunden bewerten (3 Kategorien: OPERATIONAL, DELIVERY, SERVICE)
   *
   * <p>Fields (8 pain points + notes):
   *
   * <ul>
   *   <li>painStaffShortage - Personalmangel (ENUM: NONE, LOW, MEDIUM, HIGH)
   *   <li>painHighCosts - Hohe Kosten (ENUM)
   *   <li>painFoodWaste - Lebensmittelverschwendung (ENUM)
   *   <li>painQualityInconsistency - Qualitätsschwankungen (ENUM)
   *   <li>painTimePressure - Zeitdruck (ENUM)
   *   <li>painSupplierQuality - Lieferanten-Qualität (ENUM)
   *   <li>painUnreliableDelivery - Unzuverlässige Lieferung (ENUM)
   *   <li>painPoorService - Schlechter Service (ENUM)
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
        .subtitle("Bewerten Sie die Intensität jedes Schmerzpunktes")
        .fields(
            List.of(
                // OPERATIONAL PAINS
                FieldDefinition.builder()
                    .fieldKey("painStaffShortage")
                    .label("Personalmangel")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/pain-intensity") // NONE, LOW, MEDIUM, HIGH
                    .gridCols(6)
                    .helpText("Wie stark ist der Personalmangel?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painHighCosts")
                    .label("Hohe Kosten")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/pain-intensity")
                    .gridCols(6)
                    .helpText("Sind die Einkaufskosten zu hoch?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painFoodWaste")
                    .label("Lebensmittelverschwendung")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/pain-intensity")
                    .gridCols(6)
                    .helpText("Gibt es Probleme mit Lebensmittelverschwendung?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painQualityInconsistency")
                    .label("Qualitätsschwankungen")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/pain-intensity")
                    .gridCols(6)
                    .helpText("Gibt es Qualitätsschwankungen bei Produkten?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painTimePressure")
                    .label("Zeitdruck")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/pain-intensity")
                    .gridCols(6)
                    .helpText("Besteht Zeitdruck in der Küche/Produktion?")
                    .build(),
                // DELIVERY PAINS
                FieldDefinition.builder()
                    .fieldKey("painSupplierQuality")
                    .label("Lieferanten-Qualität")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/pain-intensity")
                    .gridCols(6)
                    .helpText("Gibt es Probleme mit der Qualität des aktuellen Lieferanten?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painUnreliableDelivery")
                    .label("Unzuverlässige Lieferung")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/pain-intensity")
                    .gridCols(6)
                    .helpText("Ist die Lieferung des aktuellen Lieferanten unzuverlässig?")
                    .build(),
                // SERVICE PAINS
                FieldDefinition.builder()
                    .fieldKey("painPoorService")
                    .label("Schlechter Service")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/pain-intensity")
                    .gridCols(6)
                    .helpText("Ist der Service des aktuellen Lieferanten schlecht?")
                    .build(),
                // NOTES
                FieldDefinition.builder()
                    .fieldKey("painNotes")
                    .label("Notizen zu Schmerzpunkten")
                    .type(FieldType.TEXTAREA)
                    .gridCols(12)
                    .placeholder("Weitere Schmerzpunkte oder Details...")
                    .helpText("Freitext für zusätzliche Pain Points")
                    .showDividerAfter(false)
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
                FieldDefinition.builder()
                    .fieldKey("estimatedAnnualRevenue")
                    .label("Geschätzter Jahresumsatz")
                    .type(FieldType.CURRENCY)
                    .gridCols(6)
                    .placeholder("z.B. 50000")
                    .helpText("Potentieller Jahresumsatz mit diesem Kunden")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("budgetAvailable")
                    .label("Budget verfügbar")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/budget-availability") // YES, NO, UNKNOWN
                    .gridCols(6)
                    .helpText("Hat der Kunde Budget für Einkäufe?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("contractValue")
                    .label("Vertragswert")
                    .type(FieldType.CURRENCY)
                    .gridCols(6)
                    .placeholder("z.B. 100000")
                    .helpText("Erwarteter Vertragswert über Laufzeit")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("dealSize")
                    .label("Deal-Größe")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/deal-sizes") // SMALL, MEDIUM, LARGE, ENTERPRISE
                    .gridCols(6)
                    .helpText("Größe des Deals")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("revenueNotes")
                    .label("Notizen zum Umsatzpotenzial")
                    .type(FieldType.TEXTAREA)
                    .gridCols(12)
                    .placeholder("Weitere Informationen zum Umsatzpotenzial...")
                    .helpText("Freitext für zusätzliche Details")
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
   * <p>Fields:
   *
   * <ul>
   *   <li>responseRate - Antwortrate (ENUM: FAST, MEDIUM, SLOW, NONE)
   *   <li>meetingFrequency - Meeting-Häufigkeit (ENUM: WEEKLY, BIWEEKLY, MONTHLY, RARELY)
   *   <li>stakeholderEngagement - Stakeholder-Engagement (ENUM: HIGH, MEDIUM, LOW)
   *   <li>decisionMakingSpeed - Entscheidungsgeschwindigkeit (ENUM: FAST, MEDIUM, SLOW)
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
        .title("Engagement-Metriken")
        .subtitle("Bewerten Sie das Engagement des Kunden")
        .fields(
            List.of(
                FieldDefinition.builder()
                    .fieldKey("responseRate")
                    .label("Antwortrate")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/response-rates") // FAST, MEDIUM, SLOW, NONE
                    .gridCols(6)
                    .helpText("Wie schnell reagiert der Kunde auf Anfragen?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("meetingFrequency")
                    .label("Meeting-Häufigkeit")
                    .type(FieldType.ENUM)
                    .enumSource(
                        "/api/enums/meeting-frequency") // WEEKLY, BIWEEKLY, MONTHLY, RARELY
                    .gridCols(6)
                    .helpText("Wie häufig finden Meetings statt?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("stakeholderEngagement")
                    .label("Stakeholder-Engagement")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/engagement-levels") // HIGH, MEDIUM, LOW
                    .gridCols(6)
                    .helpText("Wie stark sind die Stakeholder engagiert?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("decisionMakingSpeed")
                    .label("Entscheidungsgeschwindigkeit")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/decision-speeds") // FAST, MEDIUM, SLOW
                    .gridCols(6)
                    .helpText("Wie schnell werden Entscheidungen getroffen?")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("engagementNotes")
                    .label("Notizen zum Engagement")
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

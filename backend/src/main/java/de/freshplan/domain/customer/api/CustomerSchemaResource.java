package de.freshplan.domain.customer.api;

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
 * Customer Schema Resource for Server-Driven UI
 *
 * <p>Sprint 2.1.7.2 D11: Server-Driven Customer Cards
 *
 * <p>Provides schema definitions for all 7 Customer Cards.
 *
 * <p>Frontend fetches this schema from `GET /api/customers/schema` and renders cards dynamically.
 *
 * <p><strong>Architecture:</strong> Backend = Single Source of Truth for schema + data Frontend =
 * Rendering Layer (no hardcoded schemas)
 *
 * <p><strong>Benefits:</strong> - Lead→Customer automatically consistent (same schema) - No
 * fieldCatalog.json needed - No calculations in Frontend - Future-proof (new fields → only Backend
 * changes)
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/customers/schema")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Customer Schema", description = "Server-Driven UI Schema for Customer Cards")
public class CustomerSchemaResource {

  /**
   * Get schema for all 7 Customer Cards
   *
   * <p>Returns field definitions for dynamic rendering in Frontend.
   *
   * <p><strong>7 Cards:</strong>
   *
   * <ol>
   *   <li>🏢 Unternehmensprofil - Company Profile
   *   <li>💰 Geschäftsdaten & Performance - Business Data
   *   <li>🎯 Bedürfnisse & Lösungen - Needs & Solutions
   *   <li>👥 Kontakte & Stakeholder - Contacts
   *   <li>📦 Produktportfolio & Services - Products
   *   <li>📈 Aktivitäten & Timeline - Activities
   *   <li>⚠️ Health & Risk - Health Score & Alerts
   * </ol>
   *
   * @return List of 7 Customer Card Schemas
   */
  @GET
  @PermitAll
  @Operation(summary = "Get Customer Card Schema (7 Cards)")
  @APIResponse(
      responseCode = "200",
      description = "Customer Card Schema",
      content = @Content(schema = @Schema(implementation = CustomerCardSchema.class)))
  public Response getCustomerSchema() {

    List<CustomerCardSchema> schema =
        List.of(
            buildCompanyProfileCard(),
            buildBusinessDataCard(),
            buildNeedsAndSolutionsCard(),
            buildContactsCard(),
            buildProductPortfolioCard(),
            buildActivitiesCard(),
            buildHealthAndRiskCard());

    return Response.ok(schema).build();
  }

  // ========== CARD 1: UNTERNEHMENSPROFIL ==========

  /**
   * Karte 1: 🏢 Unternehmensprofil
   *
   * <p>Sections: Stammdaten, Standorte, Klassifikation, Hierarchie
   */
  private CustomerCardSchema buildCompanyProfileCard() {
    return CustomerCardSchema.builder()
        .cardId("company-profile")
        .title("Unternehmensprofil")
        .subtitle("Stammdaten, Standorte, Klassifikation, Hierarchie")
        .icon("🏢")
        .order(1)
        .sections(
            List.of(
                buildBasicInfoSection(),
                buildLocationsSection(),
                buildClassificationSection(),
                buildHierarchySection()))
        .build();
  }

  private CardSection buildBasicInfoSection() {
    return CardSection.builder()
        .sectionId("basic-info")
        .title("Stammdaten")
        .subtitle("Grundlegende Unternehmensinformationen")
        .fields(
            List.of(
                FieldDefinition.builder()
                    .fieldKey("customerNumber")
                    .label("Kundennummer")
                    .type(FieldType.TEXT)
                    .readonly(true)
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("companyName")
                    .label("Firmenname")
                    .type(FieldType.TEXT)
                    .required(true)
                    .placeholder("z.B. Großhandel Frische Küche GmbH")
                    .helpText("Offizieller Firmenname laut Handelsregister")
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("tradingName")
                    .label("Handelsname")
                    .type(FieldType.TEXT)
                    .placeholder("z.B. Fresh Kitchen Wholesale")
                    .helpText("Unter diesem Namen ist das Unternehmen im Markt bekannt")
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("legalForm")
                    .label("Rechtsform")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/legal-forms")
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("customerType")
                    .label("Kundentyp")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/customer-types")
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("businessType")
                    .label("Branche")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/business-types")
                    .required(true)
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("status")
                    .label("Status")
                    .type(FieldType.CHIP)
                    .readonly(true)
                    .gridCols(6)
                    .build()))
        .build();
  }

  private CardSection buildLocationsSection() {
    return CardSection.builder()
        .sectionId("locations")
        .title("Standorte")
        .subtitle("Geografische Präsenz und Expansion")
        .fields(
            List.of(
                // Phase 3: Multi-Location Address Support
                FieldDefinition.builder()
                    .fieldKey("billingAddress")
                    .label("Rechnungsadresse")
                    .type(FieldType.TEXTAREA)
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("deliveryAddresses")
                    .label("Lieferadressen (JSON)")
                    .type(FieldType.TEXTAREA)
                    .gridCols(6)
                    .helpText("JSON-Array: [{\"street\":\"...\", \"city\":\"...\", \"zip\":\"...\", \"country\":\"...\"}]")
                    .build(),
                // Location Statistics
                FieldDefinition.builder()
                    .fieldKey("totalLocationsEu")
                    .label("Standorte gesamt (EU)")
                    .type(FieldType.NUMBER)
                    .gridCols(4)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("locationsGermany")
                    .label("Standorte Deutschland")
                    .type(FieldType.NUMBER)
                    .gridCols(4)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("locationsAustria")
                    .label("Standorte Österreich")
                    .type(FieldType.NUMBER)
                    .gridCols(4)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("locationsSwitzerland")
                    .label("Standorte Schweiz")
                    .type(FieldType.NUMBER)
                    .gridCols(4)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("locationsRestEu")
                    .label("Standorte Rest-EU")
                    .type(FieldType.NUMBER)
                    .gridCols(4)
                    .build(),
                // Expansion Planning
                FieldDefinition.builder()
                    .fieldKey("expansionPlanned")
                    .label("Expansion geplant?")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/expansion-plan")
                    .gridCols(4)
                    .build()))
        .collapsible(true)
        .build();
  }

  private CardSection buildClassificationSection() {
    return CardSection.builder()
        .sectionId("classification")
        .title("Klassifikation")
        .subtitle("Größe und Struktur")
        .fields(
            List.of(
                FieldDefinition.builder()
                    .fieldKey("kitchenSize")
                    .label("Küchengröße")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/kitchen-sizes")
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("employeeCount")
                    .label("Anzahl Mitarbeiter")
                    .type(FieldType.NUMBER)
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("branchCount")
                    .label("Anzahl Filialen")
                    .type(FieldType.NUMBER)
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("isChain")
                    .label("Filialunternehmen?")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .build()))
        .collapsible(true)
        .build();
  }

  private CardSection buildHierarchySection() {
    return CardSection.builder()
        .sectionId("hierarchy")
        .title("Hierarchie")
        .subtitle("Unternehmensstruktur und Konzernzugehörigkeit")
        .fields(
            List.of(
                FieldDefinition.builder()
                    .fieldKey("parentCustomer")
                    .label("Muttergesellschaft")
                    .type(FieldType.TEXT)
                    .readonly(true)
                    .gridCols(12)
                    .helpText("Übergeordnetes Unternehmen im Konzern")
                    .build()))
        .collapsible(true)
        .defaultCollapsed(true)
        .build();
  }

  // ========== CARD 2: GESCHÄFTSDATEN & PERFORMANCE ==========

  /**
   * Karte 2: 💰 Geschäftsdaten & Performance
   *
   * <p>Sections: Umsätze (Xentral), Verträge, Konditionen, YoY Growth
   */
  private CustomerCardSchema buildBusinessDataCard() {
    return CustomerCardSchema.builder()
        .cardId("business-data")
        .title("Geschäftsdaten & Performance")
        .subtitle("Umsätze, Verträge, Konditionen, YoY Growth")
        .icon("💰")
        .order(2)
        .sections(List.of(buildRevenueSection(), buildContractTermsSection()))
        .build();
  }

  private CardSection buildRevenueSection() {
    return CardSection.builder()
        .sectionId("revenue")
        .title("Umsätze")
        .subtitle("Geplant, Ist und Xentral-Metriken")
        .fields(
            List.of(
                FieldDefinition.builder()
                    .fieldKey("estimatedVolume")
                    .label("Geschätztes Volumen (Lead-Phase)")
                    .type(FieldType.CURRENCY)
                    .readonly(true)
                    .gridCols(6)
                    .helpText("Ursprüngliche Schätzung aus der Lead-Erfassung")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("expectedAnnualVolume")
                    .label("Erwarteter Jahresumsatz")
                    .type(FieldType.CURRENCY)
                    .gridCols(6)
                    .helpText("Geplanter Umsatz für das laufende Jahr")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("actualAnnualVolume")
                    .label("Tatsächlicher Jahresumsatz")
                    .type(FieldType.CURRENCY)
                    .readonly(true)
                    .gridCols(6)
                    .helpText("Realisierter Umsatz (aus Xentral)")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("revenue30Days")
                    .label("Umsatz letzte 30 Tage")
                    .type(FieldType.CURRENCY)
                    .readonly(true)
                    .gridCols(4)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("revenue90Days")
                    .label("Umsatz letzte 90 Tage")
                    .type(FieldType.CURRENCY)
                    .readonly(true)
                    .gridCols(4)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("revenue365Days")
                    .label("Umsatz letzte 365 Tage")
                    .type(FieldType.CURRENCY)
                    .readonly(true)
                    .gridCols(4)
                    .build()))
        .build();
  }

  private CardSection buildContractTermsSection() {
    return CardSection.builder()
        .sectionId("contract-terms")
        .title("Vertragskonditionen")
        .subtitle("Zahlungsbedingungen und Kreditrahmen")
        .fields(
            List.of(
                FieldDefinition.builder()
                    .fieldKey("paymentTerms")
                    .label("Zahlungsziel")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/payment-terms")
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("creditLimit")
                    .label("Kreditlimit")
                    .type(FieldType.CURRENCY)
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("deliveryCondition")
                    .label("Lieferbedingung")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/delivery-conditions")
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("primaryFinancing")
                    .label("Finanzierungsart")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/financing-types")
                    .gridCols(6)
                    .build()))
        .collapsible(true)
        .build();
  }

  // ========== CARD 3: BEDÜRFNISSE & LÖSUNGEN ==========

  /**
   * Karte 3: 🎯 Bedürfnisse & Lösungen
   *
   * <p>Sections: Pain Points (aus Lead) → Produktempfehlungen
   */
  private CustomerCardSchema buildNeedsAndSolutionsCard() {
    return CustomerCardSchema.builder()
        .cardId("needs-solutions")
        .title("Bedürfnisse & Lösungen")
        .subtitle("Pain Points und Produktempfehlungen")
        .icon("🎯")
        .order(3)
        .sections(List.of(buildPainPointsSection()))
        .build();
  }

  private CardSection buildPainPointsSection() {
    return CardSection.builder()
        .sectionId("pain-points")
        .title("Pain Points")
        .subtitle("Herausforderungen des Kunden (aus Lead-Phase)")
        .fields(
            List.of(
                FieldDefinition.builder()
                    .fieldKey("painStaffShortage")
                    .label("Personalmangel")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painHighCosts")
                    .label("Hohe Kosten")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painFoodWaste")
                    .label("Lebensmittelverschwendung")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painQualityInconsistency")
                    .label("Qualitätsschwankungen")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painTimePressure")
                    .label("Zeitdruck")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painSupplierQuality")
                    .label("Lieferantenqualität")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painUnreliableDelivery")
                    .label("Unzuverlässige Lieferungen")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painPoorService")
                    .label("Schlechter Service")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painNotes")
                    .label("Details zu Pain Points")
                    .type(FieldType.TEXTAREA)
                    .gridCols(12)
                    .placeholder(
                        "z.B. Hauptproblem: Zuverlässige Bio-Lieferanten schwer zu finden...")
                    .build()))
        .build();
  }

  // ========== CARD 4: KONTAKTE & STAKEHOLDER ==========

  /**
   * Karte 4: 👥 Kontakte & Stakeholder
   *
   * <p>Note: Contact data wird per separatem Endpoint geladen: GET /api/customers/{id}/contacts
   *
   * <p>Diese Karte zeigt keine Fields (nur Title + Explanation), Frontend lädt Daten dynamisch.
   */
  private CustomerCardSchema buildContactsCard() {
    return CustomerCardSchema.builder()
        .cardId("contacts")
        .title("Kontakte & Stakeholder")
        .subtitle("Ansprechpartner, Buying Center, Kommunikationshistorie")
        .icon("👥")
        .order(4)
        .sections(
            List.of(
                CardSection.builder()
                    .sectionId("contacts-list")
                    .title("Ansprechpartner")
                    .subtitle("Kontakte werden per separatem Endpoint geladen")
                    .fields(List.of())
                    .build()))
        .build();
  }

  // ========== CARD 5: PRODUKTPORTFOLIO & SERVICES ==========

  /**
   * Karte 5: 📦 Produktportfolio & Services
   *
   * <p>Note: Product data noch nicht implementiert. Platzhalter für zukünftige Features.
   */
  private CustomerCardSchema buildProductPortfolioCard() {
    return CustomerCardSchema.builder()
        .cardId("product-portfolio")
        .title("Produktportfolio & Services")
        .subtitle("Aktive Produkte, Service-Level, Cross-Sell Opportunities")
        .icon("📦")
        .order(5)
        .sections(
            List.of(
                CardSection.builder()
                    .sectionId("products-placeholder")
                    .title("Produktportfolio")
                    .subtitle("Feature kommt in Sprint 2.2.x")
                    .fields(List.of())
                    .build()))
        .defaultCollapsed(true)
        .build();
  }

  // ========== CARD 6: AKTIVITÄTEN & TIMELINE ==========

  /**
   * Karte 6: 📈 Aktivitäten & Timeline
   *
   * <p>Note: Activity data wird per separatem Endpoint geladen: GET
   * /api/activities?entityType=CUSTOMER&entityId={id}
   *
   * <p>Diese Karte zeigt keine Fields (nur Title), Frontend lädt Activity-Timeline dynamisch.
   */
  private CustomerCardSchema buildActivitiesCard() {
    return CustomerCardSchema.builder()
        .cardId("activities")
        .title("Aktivitäten & Timeline")
        .subtitle("Bestellungen, Meetings, Calls, Next Steps")
        .icon("📈")
        .order(6)
        .sections(
            List.of(
                CardSection.builder()
                    .sectionId("activities-timeline")
                    .title("Timeline")
                    .subtitle("Aktivitäten werden per separatem Endpoint geladen")
                    .fields(List.of())
                    .build()))
        .build();
  }

  // ========== CARD 7: HEALTH & RISK ==========

  /**
   * Karte 7: ⚠️ Health & Risk
   *
   * <p>Sections: Health Score (auto), Churn-Alerts, Handlungsempfehlungen
   */
  private CustomerCardSchema buildHealthAndRiskCard() {
    return CustomerCardSchema.builder()
        .cardId("health-risk")
        .title("Health & Risk")
        .subtitle("Health Score, Churn-Alerts, Handlungsempfehlungen")
        .icon("⚠️")
        .order(7)
        .sections(List.of(buildHealthScoreSection(), buildRiskIndicatorsSection()))
        .build();
  }

  private CardSection buildHealthScoreSection() {
    return CardSection.builder()
        .sectionId("health-score")
        .title("Health Score")
        .subtitle("Automatisch berechnet anhand von Bestellungen und Kommunikation")
        .fields(
            List.of(
                FieldDefinition.builder()
                    .fieldKey("healthScore")
                    .label("Health Score")
                    .type(FieldType.LABEL)
                    .readonly(true)
                    .gridCols(12)
                    .helpText("🟢 80-100: Gesund | 🟡 50-79: Watch | 🔴 0-49: Risiko (Handeln!)")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("lastContactDate")
                    .label("Letzter Kontakt")
                    .type(FieldType.DATE)
                    .readonly(true)
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("lastOrderDate")
                    .label("Letzte Bestellung")
                    .type(FieldType.DATE)
                    .readonly(true)
                    .gridCols(6)
                    .build()))
        .build();
  }

  private CardSection buildRiskIndicatorsSection() {
    return CardSection.builder()
        .sectionId("risk-indicators")
        .title("Risiko-Indikatoren")
        .subtitle("Churn-Schwellwerte und Alerts")
        .fields(
            List.of(
                FieldDefinition.builder()
                    .fieldKey("riskScore")
                    .label("Risiko-Score")
                    .type(FieldType.NUMBER)
                    .readonly(true)
                    .gridCols(6)
                    .helpText("0-100: Je höher, desto größer das Churn-Risiko")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("churnThresholdDays")
                    .label("Churn-Schwelle (Tage)")
                    .type(FieldType.NUMBER)
                    .gridCols(6)
                    .helpText("Anzahl Tage ohne Bestellung, bevor Kunde als 'At Risk' gilt")
                    .build()))
        .collapsible(true)
        .build();
  }
}

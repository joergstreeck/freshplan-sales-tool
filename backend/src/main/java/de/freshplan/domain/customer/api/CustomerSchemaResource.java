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
 * <p>Provides schema definitions for 7 Customer Cards (3 in Tab "Firma", 4 in Tab "Geschäft").
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
 * <p><strong>IMPORTANT:</strong> All cardIds use UNDERSCORES (e.g. "company_profile"), NOT hyphens!
 * This matches frontend filter expectations in CustomerDetailTabFirma.tsx and
 * CustomerDetailTabGeschaeft.tsx.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/customers/schema")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Customer Schema", description = "Server-Driven UI Schema for Customer Cards")
public class CustomerSchemaResource {

  /**
   * Get schema for 7 Customer Cards
   *
   * <p>Returns field definitions for dynamic rendering in Frontend.
   *
   * <p><strong>Tab "Firma" (3 Cards):</strong>
   *
   * <ol>
   *   <li>🏢 company_profile - Unternehmensprofil
   *   <li>📍 locations - Standorte
   *   <li>📊 classification - Klassifikation & Größe
   * </ol>
   *
   * <p><strong>Tab "Geschäft" (4 Cards):</strong>
   *
   * <ol>
   *   <li>💰 business_data - Geschäftsdaten & Performance
   *   <li>📄 contracts - Vertragsbedingungen
   *   <li>🎯 pain_points - Bedürfnisse & Pain Points (from Lead conversion)
   *   <li>📦 products - Produktportfolio & Services
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
            // Tab "Firma" (3 Cards)
            buildCompanyProfileCard(), // Order 1
            buildLocationsCard(), // Order 2
            buildClassificationCard(), // Order 3
            // Tab "Geschäft" (4 Cards)
            buildBusinessDataCard(), // Order 4
            buildContractsCard(), // Order 5
            buildPainPointsCard(), // Order 6
            buildProductsCard() // Order 7
            );

    return Response.ok(schema).build();
  }

  // ========== TAB "FIRMA" - CARD 1: UNTERNEHMENSPROFIL ==========

  /**
   * Card 1: 🏢 Unternehmensprofil (Tab "Firma")
   *
   * <p>Basic company information: Name, Legal Form, Customer Type, Status
   */
  private CustomerCardSchema buildCompanyProfileCard() {
    return CustomerCardSchema.builder()
        .cardId("company_profile") // UNDERSCORE (not hyphen!)
        .title("Unternehmensprofil")
        .subtitle("Stammdaten und grundlegende Informationen")
        .icon("🏢")
        .order(1)
        .sections(List.of(buildBasicInfoSection()))
        .build();
  }

  private CardSection buildBasicInfoSection() {
    return CardSection.builder()
        .sectionId("basic_info")
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
                    .helpText("Wird aus Lead-Konversion übernommen (lead.businessType)")
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

  // ========== TAB "FIRMA" - CARD 2: STANDORTE ==========

  /**
   * Card 2: 📍 Standorte (Tab "Firma")
   *
   * <p>Multi-location details: Billing Address, Delivery Addresses, Location Statistics
   *
   * <p>IMPORTANT: Migration V10040 added billingAddress (VARCHAR) and deliveryAddresses (JSONB)
   */
  private CustomerCardSchema buildLocationsCard() {
    return CustomerCardSchema.builder()
        .cardId("locations") // UNDERSCORE (not hyphen!)
        .title("Standorte")
        .subtitle("Geografische Präsenz und Multi-Location Details")
        .icon("📍")
        .order(2)
        .sections(List.of(buildAddressesSection(), buildLocationStatsSection()))
        .build();
  }

  private CardSection buildAddressesSection() {
    return CardSection.builder()
        .sectionId("addresses")
        .title("Adressen")
        .subtitle("Rechnungs- und Lieferadressen (V10040)")
        .fields(
            List.of(
                // Migration V10040: Multi-Location Address Support
                FieldDefinition.builder()
                    .fieldKey("billingAddress")
                    .label("Rechnungsadresse")
                    .type(FieldType.TEXTAREA)
                    .gridCols(6)
                    .helpText("Primäre Rechnungsadresse für Invoicing (VARCHAR 500)")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("deliveryAddresses")
                    .label("Lieferadressen (JSON)")
                    .type(FieldType.TEXTAREA)
                    .gridCols(6)
                    .helpText(
                        "JSON-Array: [{\"street\":\"...\", \"city\":\"...\", \"zip\":\"...\","
                            + " \"country\":\"...\"}]")
                    .build()))
        .build();
  }

  private CardSection buildLocationStatsSection() {
    return CardSection.builder()
        .sectionId("location_stats")
        .title("Standort-Statistik")
        .subtitle("Anzahl Standorte nach Region")
        .fields(
            List.of(
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

  // ========== TAB "FIRMA" - CARD 3: KLASSIFIKATION ==========

  /**
   * Card 3: 📊 Klassifikation (Tab "Firma")
   *
   * <p>Company size and structure: Kitchen Size, Employees, Branches, Chain
   */
  private CustomerCardSchema buildClassificationCard() {
    return CustomerCardSchema.builder()
        .cardId("classification") // UNDERSCORE (not hyphen!)
        .title("Klassifikation")
        .subtitle("Größe und Struktur")
        .icon("📊")
        .order(3)
        .sections(List.of(buildClassificationSection()))
        .build();
  }

  private CardSection buildClassificationSection() {
    return CardSection.builder()
        .sectionId("classification")
        .title("Größe und Struktur")
        .subtitle("Klassifikation des Unternehmens")
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
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("parentCustomer")
                    .label("Muttergesellschaft")
                    .type(FieldType.TEXT)
                    .readonly(true)
                    .gridCols(12)
                    .helpText("Übergeordnetes Unternehmen im Konzern")
                    .build()))
        .build();
  }

  // ========== TAB "GESCHÄFT" - CARD 4: GESCHÄFTSDATEN & PERFORMANCE ==========

  /**
   * Card 4: 💰 Geschäftsdaten & Performance (Tab "Geschäft")
   *
   * <p>Revenue metrics: Expected vs Actual, Xentral Integration (30/90/365 days)
   *
   * <p>IMPORTANT: expectedAnnualVolume comes from Lead conversion (lead.estimatedVolume →
   * customer.expectedAnnualVolume)
   */
  private CustomerCardSchema buildBusinessDataCard() {
    return CustomerCardSchema.builder()
        .cardId("business_data") // UNDERSCORE (not hyphen!)
        .title("Geschäftsdaten & Performance")
        .subtitle("Umsätze, Metriken und YoY Growth")
        .icon("💰")
        .order(4)
        .sections(List.of(buildRevenueSection()))
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
                    .fieldKey("expectedAnnualVolume")
                    .label("Erwarteter Jahresumsatz")
                    .type(FieldType.CURRENCY)
                    .gridCols(6)
                    .helpText(
                        "Geplanter Umsatz (aus Lead-Konversion: lead.estimatedVolume →"
                            + " customer.expectedAnnualVolume)")
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

  // ========== TAB "GESCHÄFT" - CARD 5: VERTRAGSBEDINGUNGEN ==========

  /**
   * Card 5: 📄 Vertragsbedingungen (Tab "Geschäft")
   *
   * <p>Contract terms: Payment Terms, Credit Limit, Delivery Conditions, Financing
   */
  private CustomerCardSchema buildContractsCard() {
    return CustomerCardSchema.builder()
        .cardId("contracts") // UNDERSCORE (not hyphen!)
        .title("Vertragsbedingungen")
        .subtitle("Zahlungsbedingungen und Kreditrahmen")
        .icon("📄")
        .order(5)
        .sections(List.of(buildContractTermsSection()))
        .build();
  }

  private CardSection buildContractTermsSection() {
    return CardSection.builder()
        .sectionId("contract_terms")
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
        .build();
  }

  // ========== TAB "GESCHÄFT" - CARD 6: BEDÜRFNISSE & PAIN POINTS ==========

  /**
   * Card 6: 🎯 Bedürfnisse & Pain Points (Tab "Geschäft")
   *
   * <p>CRITICAL: This card contains ALL 9 fields copied from Lead during conversion
   * (OpportunityService.convertToCustomer() lines 445-584):
   *
   * <ul>
   *   <li>8 boolean pain point flags (painStaffShortage, painHighCosts, etc.)
   *   <li>1 text field (painNotes)
   * </ul>
   *
   * <p>If these fields are missing → data loss during Lead→Customer conversion!
   */
  private CustomerCardSchema buildPainPointsCard() {
    return CustomerCardSchema.builder()
        .cardId("pain_points") // UNDERSCORE (not hyphen!)
        .title("Bedürfnisse & Pain Points")
        .subtitle("Herausforderungen des Kunden (aus Lead-Phase)")
        .icon("🎯")
        .order(6)
        .sections(List.of(buildPainPointsSection()))
        .build();
  }

  private CardSection buildPainPointsSection() {
    return CardSection.builder()
        .sectionId("pain_points")
        .title("Pain Points")
        .subtitle("Herausforderungen (aus Lead-Konversion kopiert)")
        .fields(
            List.of(
                // All 8 boolean pain point flags from Lead conversion
                FieldDefinition.builder()
                    .fieldKey("painStaffShortage")
                    .label("Personalmangel")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Lead-Feld: lead.painStaffShortage → customer.painStaffShortage")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painHighCosts")
                    .label("Hohe Kosten")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Lead-Feld: lead.painHighCosts → customer.painHighCosts")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painFoodWaste")
                    .label("Lebensmittelverschwendung")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Lead-Feld: lead.painFoodWaste → customer.painFoodWaste")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painQualityInconsistency")
                    .label("Qualitätsschwankungen")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText(
                        "Lead-Feld: lead.painQualityInconsistency →"
                            + " customer.painQualityInconsistency")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painTimePressure")
                    .label("Zeitdruck")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Lead-Feld: lead.painTimePressure → customer.painTimePressure")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painSupplierQuality")
                    .label("Lieferantenqualität")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Lead-Feld: lead.painSupplierQuality → customer.painSupplierQuality")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painUnreliableDelivery")
                    .label("Unzuverlässige Lieferungen")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText(
                        "Lead-Feld: lead.painUnreliableDelivery → customer.painUnreliableDelivery")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("painPoorService")
                    .label("Schlechter Service")
                    .type(FieldType.BOOLEAN)
                    .gridCols(6)
                    .helpText("Lead-Feld: lead.painPoorService → customer.painPoorService")
                    .build(),
                // Text field with detailed pain point notes
                FieldDefinition.builder()
                    .fieldKey("painNotes")
                    .label("Details zu Pain Points")
                    .type(FieldType.TEXTAREA)
                    .gridCols(12)
                    .placeholder(
                        "z.B. Hauptproblem: Zuverlässige Bio-Lieferanten schwer zu finden...")
                    .helpText("Lead-Feld: lead.painNotes → customer.painNotes")
                    .build()))
        .build();
  }

  // ========== TAB "GESCHÄFT" - CARD 7: PRODUKTPORTFOLIO ==========

  /**
   * Card 7: 📦 Produktportfolio & Services (Tab "Geschäft")
   *
   * <p>Note: Product data noch nicht implementiert. Platzhalter für zukünftige Features (Sprint
   * 2.2.x).
   */
  private CustomerCardSchema buildProductsCard() {
    return CustomerCardSchema.builder()
        .cardId("products") // UNDERSCORE (not hyphen!)
        .title("Produktportfolio & Services")
        .subtitle("Aktive Produkte, Service-Level, Cross-Sell Opportunities")
        .icon("📦")
        .order(7)
        .sections(
            List.of(
                CardSection.builder()
                    .sectionId("products_placeholder")
                    .title("Produktportfolio")
                    .subtitle("Feature kommt in Sprint 2.2.x")
                    .fields(List.of())
                    .build()))
        .defaultCollapsed(true)
        .build();
  }
}

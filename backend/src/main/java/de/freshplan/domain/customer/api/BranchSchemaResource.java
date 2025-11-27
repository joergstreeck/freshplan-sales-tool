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
 * Branch Schema Resource for Server-Driven UI
 *
 * <p>Sprint 2.1.7.7: Server-Driven Branch/Filiale Creation Dialog
 *
 * <p>Provides schema definitions for CreateBranchDialog.
 *
 * <p>Frontend fetches this schema from `GET /api/branches/schema` and renders fields dynamically.
 *
 * <p><strong>Architecture:</strong> Backend = Single Source of Truth for schema + data Frontend =
 * Rendering Layer (no hardcoded schemas)
 *
 * @author FreshPlan Team
 * @since 2.1.7.7
 */
@Path("/api/branches/schema")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Branch Schema", description = "Server-Driven UI Schema for Branch Creation")
public class BranchSchemaResource {

  /**
   * Get schema for Branch creation dialog
   *
   * <p>Returns field definitions for dynamic rendering in CreateBranchDialog.
   *
   * <p><strong>Steps (2):</strong>
   *
   * <ol>
   *   <li>🏢 basic_info - Basisdaten (Firmenname, Geschäftsart, Status, Umsatz)
   *   <li>📍 address_contact - Adresse & Kontakt (Straße, PLZ, Stadt, Land, Telefon, E-Mail)
   * </ol>
   *
   * @return Branch Schema with 2 sections (steps)
   */
  @GET
  @PermitAll
  @Operation(summary = "Get Branch Schema (2 Steps)")
  @APIResponse(
      responseCode = "200",
      description = "Branch Schema",
      content = @Content(schema = @Schema(implementation = CustomerCardSchema.class)))
  public Response getBranchSchema() {

    CustomerCardSchema branchSchema =
        CustomerCardSchema.builder()
            .cardId("branch_create")
            .title("Neue Filiale anlegen")
            .subtitle("Erfassen Sie die Daten der neuen Filiale")
            .icon("🏢")
            .order(1)
            .sections(
                List.of(
                    buildBasicInfoSection(), // Step 1: Basisdaten
                    buildAddressContactSection() // Step 2: Adresse & Kontakt
                    ))
            .build();

    return Response.ok(List.of(branchSchema)).build();
  }

  // ========== STEP 1: BASISDATEN ==========

  /**
   * Step 1: 🏢 Basisdaten
   *
   * <p>Basic branch information: CompanyName, TradingName, BusinessType, CustomerType, Status,
   * ExpectedAnnualVolume
   */
  private CardSection buildBasicInfoSection() {
    return CardSection.builder()
        .sectionId("basic_info")
        .title("Basisdaten")
        .subtitle("Erfassen Sie die Grunddaten der neuen Filiale")
        .fields(
            List.of(
                FieldDefinition.builder()
                    .fieldKey("companyName")
                    .label("Firmenname der Filiale")
                    .type(FieldType.TEXT)
                    .required(true)
                    .gridCols(12)
                    .placeholder("z.B. Silbertanne Filiale Frankfurt")
                    .helpText("Offizieller Name der Filiale")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("tradingName")
                    .label("Handelsname")
                    .type(FieldType.TEXT)
                    .gridCols(6)
                    .placeholder("Falls abweichend vom Firmennamen")
                    .helpText("Handelsname, falls vom Firmennamen abweichend")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("businessType")
                    .label("Geschäftsart")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/business-types")
                    .required(true)
                    .gridCols(6)
                    .helpText("Art des Geschäftsbetriebs")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("customerType")
                    .label("Kundentyp")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/customer-types")
                    .gridCols(6)
                    .helpText("Rechtsform des Kunden")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("status")
                    .label("Status")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/customer-status")
                    .gridCols(6)
                    .helpText("Aktueller Status der Filiale")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("expectedAnnualVolume")
                    .label("Erwarteter Jahresumsatz")
                    .type(FieldType.CURRENCY)
                    .gridCols(12)
                    .placeholder("z.B. 50000")
                    .helpText("Geschätzter Jahresumsatz dieser Filiale in EUR")
                    .build()))
        .build();
  }

  // ========== STEP 2: ADRESSE & KONTAKT ==========

  /**
   * Step 2: 📍 Adresse & Kontakt
   *
   * <p>Location and contact data: Street, PostalCode, City, Country, Phone, Email
   */
  private CardSection buildAddressContactSection() {
    return CardSection.builder()
        .sectionId("address_contact")
        .title("Adresse & Kontakt")
        .subtitle("Standort und Kontaktdaten der Filiale")
        .fields(
            List.of(
                FieldDefinition.builder()
                    .fieldKey("street")
                    .label("Straße und Hausnummer")
                    .type(FieldType.TEXT)
                    .gridCols(12)
                    .placeholder("z.B. Musterstraße 123")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("postalCode")
                    .label("PLZ")
                    .type(FieldType.TEXT)
                    .gridCols(4)
                    .placeholder("z.B. 80331")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("city")
                    .label("Stadt")
                    .type(FieldType.TEXT)
                    .required(true)
                    .gridCols(8)
                    .placeholder("z.B. München")
                    .helpText("Standort der Filiale")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("country")
                    .label("Land")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/country-codes")
                    .required(true)
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("phone")
                    .label("Telefon")
                    .type(FieldType.TEXT)
                    .gridCols(6)
                    .placeholder("z.B. +49 89 12345678")
                    .showDividerAfter(true)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("email")
                    .label("E-Mail")
                    .type(FieldType.EMAIL)
                    .gridCols(6)
                    .placeholder("z.B. filiale@example.de")
                    .build()))
        .build();
  }
}

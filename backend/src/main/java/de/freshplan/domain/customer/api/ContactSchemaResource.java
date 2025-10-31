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
 * Contact Schema Resource for Server-Driven UI
 *
 * <p>Sprint 2.1.7.2 D11.1: Server-Driven Contact Cards
 *
 * <p>Provides schema definitions for Contact sections (visible in CustomerDetailTabVerlauf).
 *
 * <p>Frontend fetches this schema from `GET /api/contacts/schema` and renders sections dynamically.
 *
 * <p><strong>Architecture:</strong> Backend = Single Source of Truth for schema + data Frontend =
 * Rendering Layer (no hardcoded schemas, no fieldCatalog.json)
 *
 * <p><strong>Benefits:</strong>
 *
 * <ul>
 *   <li>Backend controls Contact form structure
 *   <li>V2 fields (LinkedIn, XING, Notes) automatically included
 *   <li>Enum sources from backend (/api/enums/...)
 *   <li>No frontend/backend parity issues
 * </ul>
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/contacts/schema")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Contact Schema", description = "Server-Driven UI Schema for Contact Forms")
public class ContactSchemaResource {

  /**
   * Get schema for Contact sections
   *
   * <p>Returns field definitions for dynamic rendering in ContactEditDialog.
   *
   * <p><strong>Sections (3):</strong>
   *
   * <ol>
   *   <li>👤 basic_info - Stammdaten (Name, Position, Kontaktdaten)
   *   <li>🤝 relationship - Beziehungsmanagement (Birthday, Hobbies, Familie)
   *   <li>💼 social_business - Professionelle Links & Notizen (LinkedIn, XING, Notes)
   * </ol>
   *
   * @return Contact Schema with 3 sections
   */
  @GET
  @PermitAll
  @Operation(summary = "Get Contact Schema (3 Sections)")
  @APIResponse(
      responseCode = "200",
      description = "Contact Schema",
      content = @Content(schema = @Schema(implementation = CustomerCardSchema.class)))
  public Response getContactSchema() {

    // Single card with 3 sections (Contact details are shown in dialog, not as separate cards)
    CustomerCardSchema contactSchema =
        CustomerCardSchema.builder()
            .cardId("contact_details")
            .title("Kontaktdaten")
            .subtitle("Persönliche und berufliche Informationen")
            .icon("👤")
            .order(1)
            .sections(
                List.of(
                    buildBasicInfoSection(), // Section 1
                    buildRelationshipSection(), // Section 2
                    buildSocialBusinessSection() // Section 3
                    ))
            .build();

    return Response.ok(List.of(contactSchema)).build();
  }

  // ========== SECTION 1: STAMMDATEN (BASIC INFO) ==========

  /**
   * Section 1: 👤 Stammdaten
   *
   * <p>Basic contact information: Salutation, Title, Name, Position, DecisionLevel, Email, Phone,
   * Mobile
   *
   * <p>Migration V10042: Added linkedin, xing, notes (V2 fields)
   */
  private CardSection buildBasicInfoSection() {
    return CardSection.builder()
        .sectionId("basic_info")
        .title("Stammdaten")
        .subtitle("Persönliche und berufliche Informationen")
        .fields(
            List.of(
                FieldDefinition.builder()
                    .fieldKey("salutation")
                    .label("Anrede")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/contact-salutations")
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("title")
                    .label("Titel")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/contact-titles")
                    .gridCols(6)
                    .placeholder("z.B. Dr., Prof.")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("firstName")
                    .label("Vorname")
                    .type(FieldType.TEXT)
                    .required(true)
                    .gridCols(6)
                    .placeholder("z.B. Maria")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("lastName")
                    .label("Nachname")
                    .type(FieldType.TEXT)
                    .required(true)
                    .gridCols(6)
                    .placeholder("z.B. Müller")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("position")
                    .label("Position")
                    .type(FieldType.TEXT)
                    .gridCols(6)
                    .placeholder("z.B. Küchenchef, Einkaufsleiter")
                    .helpText("Rolle im Unternehmen")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("decisionLevel")
                    .label("Entscheidungsebene")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/contact-decision-levels")
                    .gridCols(6)
                    .helpText(
                        "Entscheidungskompetenz (Executive, Manager, Operational, Influencer)")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("email")
                    .label("E-Mail")
                    .type(FieldType.TEXT)
                    .gridCols(12)
                    .placeholder("maria.mueller@example.com")
                    .helpText("Geschäftliche E-Mail-Adresse")
                    .showDividerAfter(true)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("phone")
                    .label("Telefon")
                    .type(FieldType.TEXT)
                    .gridCols(6)
                    .placeholder("+49 123 456789")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("mobile")
                    .label("Mobil")
                    .type(FieldType.TEXT)
                    .gridCols(6)
                    .placeholder("+49 170 1234567")
                    .build()))
        .build();
  }

  // ========== SECTION 2: BEZIEHUNGSMANAGEMENT (RELATIONSHIP) ==========

  /**
   * Section 2: 🤝 Beziehungsmanagement
   *
   * <p>Relationship data for sales excellence: Birthday, Hobbies, Family Status, Children, Personal
   * Notes
   */
  private CardSection buildRelationshipSection() {
    return CardSection.builder()
        .sectionId("relationship")
        .title("Beziehungsmanagement")
        .subtitle("Persönliche Informationen für exzellente Kundenbeziehungen")
        .fields(
            List.of(
                FieldDefinition.builder()
                    .fieldKey("birthday")
                    .label("Geburtstag")
                    .type(FieldType.DATE)
                    .gridCols(6)
                    .helpText("Für Geburtstagsgrüße und Kundenbindung")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("hobbies")
                    .label("Hobbies & Interessen")
                    .type(FieldType.TEXT)
                    .gridCols(6)
                    .placeholder("z.B. Golf, Skifahren, Kochen")
                    .helpText("Small Talk Themen, kommagetrennt")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("familyStatus")
                    .label("Familienstand")
                    .type(FieldType.TEXT)
                    .gridCols(6)
                    .placeholder("z.B. verheiratet, ledig")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("childrenCount")
                    .label("Anzahl Kinder")
                    .type(FieldType.NUMBER)
                    .gridCols(6)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("personalNotes")
                    .label("Persönliche Notizen")
                    .type(FieldType.TEXTAREA)
                    .gridCols(12)
                    .placeholder(
                        "z.B. Liebt italienisches Essen, spricht fließend Englisch, Fußball-Fan...")
                    .helpText(
                        "Beziehungsrelevante Details für persönliche Ansprache (NICHT"
                            + " geschäftliche Notizen - siehe 'Business Notizen')")
                    .showDividerAfter(true)
                    .build()))
        .collapsible(true)
        .defaultCollapsed(false)
        .build();
  }

  // ========== SECTION 3: PROFESSIONELLE LINKS & BUSINESS NOTIZEN ==========

  /**
   * Section 3: 💼 Professionelle Links & Business Notizen
   *
   * <p>Sprint 2.1.7.2 D11.1: V2 Fields - LinkedIn, XING, Business Notes
   *
   * <p>Migration V10042: Added linkedin (VARCHAR 500), xing (VARCHAR 500), notes (TEXT)
   */
  private CardSection buildSocialBusinessSection() {
    return CardSection.builder()
        .sectionId("social_business")
        .title("Professionelle Links & Business Notizen")
        .subtitle("LinkedIn, XING und geschäftliche Notizen")
        .fields(
            List.of(
                FieldDefinition.builder()
                    .fieldKey("linkedin")
                    .label("LinkedIn Profil")
                    .type(FieldType.TEXT)
                    .gridCols(6)
                    .placeholder("https://linkedin.com/in/...")
                    .helpText("LinkedIn Profil-URL für Recherche und Netzwerk")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("xing")
                    .label("XING Profil")
                    .type(FieldType.TEXT)
                    .gridCols(6)
                    .placeholder("https://xing.com/profile/...")
                    .helpText("XING Profil-URL (besonders relevant im DACH-Raum)")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("notes")
                    .label("Business Notizen")
                    .type(FieldType.TEXTAREA)
                    .gridCols(12)
                    .placeholder(
                        "z.B. Entscheidungsträger für Bio-Produkte, bevorzugt kurze"
                            + " Meetings...")
                    .helpText(
                        "Geschäftliche Notizen (Entscheidungskompetenz, Präferenzen,"
                            + " Meeting-Historie)")
                    .build()))
        .collapsible(true)
        .defaultCollapsed(false)
        .build();
  }
}

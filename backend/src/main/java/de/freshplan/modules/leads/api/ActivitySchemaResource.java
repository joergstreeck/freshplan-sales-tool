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
 * Activity Schema Resource for Server-Driven UI
 *
 * <p>Sprint 2.1.7.2 D11: Server-Driven UI for Activity Management
 *
 * <p>Provides schema definition for Activity dialog (Lead activities tracking).
 *
 * <p>Frontend fetches this schema from `GET /api/activities/schema` and renders form dynamically.
 *
 * <p><strong>Architecture:</strong> Backend = Single Source of Truth for schema + data Frontend =
 * Rendering Layer (no hardcoded schemas)
 *
 * <p><strong>Activity Fields:</strong>
 *
 * <ul>
 *   <li>activityType - Aktivitätstyp (CALL, EMAIL, MEETING, NOTE)
 *   <li>title - Titel der Aktivität (required)
 *   <li>description - Beschreibung der Aktivität
 *   <li>scheduledDate - Geplantes Datum
 *   <li>status - Status (PLANNED, COMPLETED, CANCELLED)
 * </ul>
 *
 * <p><strong>Benefits:</strong>
 *
 * <ul>
 *   <li>Backend controls Activity form structure
 *   <li>Enum sources from backend (/api/enums/...)
 *   <li>No frontend/backend parity issues
 *   <li>Used by ActivityDialog component
 * </ul>
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/activities/schema")
@Produces(MediaType.APPLICATION_JSON)
@Tag(
    name = "Activity Schema",
    description = "Server-Driven UI Schema for Activity Management (Lead Activities)")
public class ActivitySchemaResource {

  /**
   * Get schema for Activity dialog
   *
   * <p>Returns field definitions for dynamic rendering in ActivityDialog.
   *
   * <p><strong>Section: Aktivitätsdetails</strong>
   *
   * <p>Fields:
   *
   * <ol>
   *   <li>activityType (ENUM, required) - CALL, EMAIL, MEETING, NOTE, etc.
   *   <li>description (TEXTAREA, required) - Beschreibung der Aktivität
   *   <li>outcome (ENUM, optional) - Ergebnis (SUCCESSFUL, UNSUCCESSFUL, etc.)
   * </ol>
   *
   * @return Activity Schema with 1 section (activity_details)
   */
  @GET
  @PermitAll
  @Operation(summary = "Get Activity Schema (Activity Management Dialog)")
  @APIResponse(
      responseCode = "200",
      description = "Activity Schema",
      content = @Content(schema = @Schema(implementation = CustomerCardSchema.class)))
  public Response getActivitySchema() {

    // Single card with 1 section (Aktivitätsdetails)
    CustomerCardSchema activitySchema =
        CustomerCardSchema.builder()
            .cardId("activity")
            .title("Neue Aktivität erfassen")
            .subtitle("Lead-Aktivität protokollieren")
            .icon("📝")
            .order(1)
            .sections(List.of(buildActivityDetailsSection()))
            .build();

    return Response.ok(List.of(activitySchema)).build();
  }

  /**
   * Section: Aktivitätsdetails
   *
   * <p>Activity logging fields for completed Lead activities.
   *
   * <p>Fields:
   *
   * <ul>
   *   <li>activityType - Required ENUM field (CALL, EMAIL, MEETING, NOTE, etc.)
   *   <li>description - Required TEXTAREA field (Was wurde besprochen/gemacht?)
   *   <li>outcome - Optional ENUM field (SUCCESSFUL, UNSUCCESSFUL, NO_ANSWER, etc.)
   * </ul>
   */
  private CardSection buildActivityDetailsSection() {
    return CardSection.builder()
        .sectionId("activity_details")
        .title("Aktivitätsdetails")
        .subtitle("Typ, Beschreibung, Ergebnis")
        .fields(
            List.of(
                FieldDefinition.builder()
                    .fieldKey("activityType")
                    .label("Aktivitätstyp")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/activity-types") // CALL, EMAIL, MEETING, NOTE, etc.
                    .required(true)
                    .gridCols(12)
                    .helpText("Art der Aktivität (Anruf, E-Mail, Meeting, Notiz)")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("description")
                    .label("Beschreibung")
                    .type(FieldType.TEXTAREA)
                    .required(true)
                    .gridCols(12)
                    .placeholder("Was wurde besprochen? Welche Ergebnisse wurden erzielt?")
                    .helpText("Ausführliche Beschreibung der Aktivität (max. 1000 Zeichen)")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("outcome")
                    .label("Ergebnis (optional)")
                    .type(FieldType.ENUM)
                    .enumSource("/api/enums/activity-outcomes") // SUCCESSFUL, UNSUCCESSFUL, etc.
                    .gridCols(12)
                    .helpText("Hilft beim Tracking von Erfolg und Follow-up-Bedarf")
                    .build()))
        .collapsible(false)
        .defaultCollapsed(false)
        .build();
  }
}

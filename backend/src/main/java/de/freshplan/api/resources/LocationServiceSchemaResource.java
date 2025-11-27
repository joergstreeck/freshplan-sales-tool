package de.freshplan.api.resources;

import de.freshplan.domain.customer.dto.FieldDefinition;
import de.freshplan.domain.customer.dto.FieldType;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST API for Location Service Schema (Server-Driven UI).
 *
 * <p>Sprint 2.1.7.x: fieldCatalog.json Migration - Location Service Fields
 *
 * <p>Replaces hardcoded field definitions in ServiceFieldsContainer.tsx with dynamic Server-Driven
 * UI.
 *
 * <p>Returns service field groups based on industry (hotel, krankenhaus, betriebsrestaurant).
 *
 * @see ServiceFieldsContainer.tsx (frontend/src/features/customers/components/location/)
 */
@Path("/api/locations/service-schema")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Locations", description = "Location service schema operations")
public class LocationServiceSchemaResource {

  /**
   * Get service field groups for a specific industry.
   *
   * <p>Sprint 2.1.7.x: Server-Driven UI for location-specific service fields
   *
   * <p>Industry-specific field groups: - hotel: Frühstücksgeschäft, Mittag- und Abendessen,
   * Zusatzservices, Kapazität - krankenhaus: Patientenverpflegung, Diätformen - betriebsrestaurant:
   * Betriebszeiten
   *
   * @param industry Industry type (hotel, krankenhaus, betriebsrestaurant)
   * @return List of service field groups with field definitions
   */
  @GET
  @PermitAll
  @Operation(summary = "Get location service schema by industry")
  @APIResponse(
      responseCode = "200",
      description = "Service field groups for the specified industry",
      content = @Content(schema = @Schema(implementation = ServiceFieldGroup.class)))
  public List<ServiceFieldGroup> getServiceSchema(
      @Parameter(description = "Industry type", required = true) @QueryParam("industry")
          String industry) {

    if (industry == null || industry.isBlank()) {
      return Collections.emptyList();
    }

    return switch (industry.toLowerCase()) {
      case "hotel" -> getHotelServiceGroups();
      case "krankenhaus" -> getKrankenhausServiceGroups();
      case "betriebsrestaurant" -> getBetriebsrestaurantServiceGroups();
      default -> Collections.emptyList();
    };
  }

  /** Service field groups for Hotel industry */
  private List<ServiceFieldGroup> getHotelServiceGroups() {
    return Arrays.asList(
        new ServiceFieldGroup(
            "breakfast",
            "Frühstücksgeschäft",
            "☕",
            Arrays.asList(
                FieldDefinition.builder()
                    .fieldKey("offersBreakfast")
                    .label("Bietet Frühstück an")
                    .type(FieldType.BOOLEAN)
                    .required(false)
                    .readonly(false)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("breakfastWarm")
                    .label("Warmes Frühstück")
                    .type(FieldType.BOOLEAN)
                    .required(false)
                    .readonly(false)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("breakfastGuestsPerDay")
                    .label("Frühstücksgäste/Tag")
                    .type(FieldType.NUMBER)
                    .required(false)
                    .readonly(false)
                    .placeholder("Anzahl Frühstücksgäste")
                    .build())),
        new ServiceFieldGroup(
            "meals",
            "Mittag- und Abendessen",
            "🍽️",
            Arrays.asList(
                FieldDefinition.builder()
                    .fieldKey("offersLunch")
                    .label("Bietet Mittagessen an")
                    .type(FieldType.BOOLEAN)
                    .required(false)
                    .readonly(false)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("offersDinner")
                    .label("Bietet Abendessen an")
                    .type(FieldType.BOOLEAN)
                    .required(false)
                    .readonly(false)
                    .build())),
        new ServiceFieldGroup(
            "additional",
            "Zusatzservices",
            "🛎️",
            Arrays.asList(
                FieldDefinition.builder()
                    .fieldKey("offersRoomService")
                    .label("Bietet Zimmerservice an")
                    .type(FieldType.BOOLEAN)
                    .required(false)
                    .readonly(false)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("offersEvents")
                    .label("Bietet Events an")
                    .type(FieldType.BOOLEAN)
                    .required(false)
                    .readonly(false)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("eventCapacity")
                    .label("Event-Kapazität")
                    .type(FieldType.NUMBER)
                    .required(false)
                    .readonly(false)
                    .placeholder("Max. Anzahl Gäste")
                    .build())),
        new ServiceFieldGroup(
            "capacity",
            "Kapazität",
            "🏨",
            Arrays.asList(
                FieldDefinition.builder()
                    .fieldKey("roomCount")
                    .label("Anzahl Zimmer")
                    .type(FieldType.NUMBER)
                    .required(false)
                    .readonly(false)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("averageOccupancy")
                    .label("Durchschnittliche Auslastung (%)")
                    .type(FieldType.NUMBER)
                    .required(false)
                    .readonly(false)
                    .placeholder("z.B. 75")
                    .build())));
  }

  /** Service field groups for Krankenhaus industry */
  private List<ServiceFieldGroup> getKrankenhausServiceGroups() {
    return Arrays.asList(
        new ServiceFieldGroup(
            "patientMeals",
            "Patientenverpflegung",
            "🏥",
            Arrays.asList(
                FieldDefinition.builder()
                    .fieldKey("mealSystem")
                    .label("Verpflegungssystem")
                    .type(FieldType.ENUM)
                    .required(false)
                    .readonly(false)
                    .enumSource("/api/enums/meal-systems")
                    .placeholder("Verpflegungssystem wählen...")
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("bedsCount")
                    .label("Anzahl Betten")
                    .type(FieldType.NUMBER)
                    .required(false)
                    .readonly(false)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("mealsPerDay")
                    .label("Mahlzeiten/Tag")
                    .type(FieldType.NUMBER)
                    .required(false)
                    .readonly(false)
                    .build())),
        new ServiceFieldGroup(
            "diets",
            "Diätformen",
            "🥗",
            Arrays.asList(
                FieldDefinition.builder()
                    .fieldKey("offersVegetarian")
                    .label("Vegetarisch")
                    .type(FieldType.BOOLEAN)
                    .required(false)
                    .readonly(false)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("offersVegan")
                    .label("Vegan")
                    .type(FieldType.BOOLEAN)
                    .required(false)
                    .readonly(false)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("offersHalal")
                    .label("Halal")
                    .type(FieldType.BOOLEAN)
                    .required(false)
                    .readonly(false)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("offersKosher")
                    .label("Koscher")
                    .type(FieldType.BOOLEAN)
                    .required(false)
                    .readonly(false)
                    .build())));
  }

  /** Service field groups for Betriebsrestaurant industry */
  private List<ServiceFieldGroup> getBetriebsrestaurantServiceGroups() {
    return List.of(
        new ServiceFieldGroup(
            "operation",
            "Betriebszeiten",
            "🏢",
            Arrays.asList(
                FieldDefinition.builder()
                    .fieldKey("operatingDays")
                    .label("Betriebstage/Woche")
                    .type(FieldType.NUMBER)
                    .required(false)
                    .readonly(false)
                    .helpText("Anzahl Betriebstage pro Woche (1-7)")
                    .validationRules(Arrays.asList("min:1", "max:7"))
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("lunchGuests")
                    .label("Mittagsgäste/Tag")
                    .type(FieldType.NUMBER)
                    .required(false)
                    .readonly(false)
                    .build(),
                FieldDefinition.builder()
                    .fieldKey("subsidized")
                    .label("Subventioniert")
                    .type(FieldType.BOOLEAN)
                    .required(false)
                    .readonly(false)
                    .build())));
  }

  /**
   * DTO for Service Field Group (Server-Driven UI).
   *
   * <p>Groups related service fields together with visual metadata (title, icon).
   *
   * <p>Frontend renders each group as a section with icon + title heading.
   */
  public static class ServiceFieldGroup {
    @Schema(description = "Group ID", example = "breakfast")
    public String id;

    @Schema(description = "Group title (German)", example = "Frühstücksgeschäft")
    public String title;

    @Schema(description = "Group icon (emoji)", example = "☕")
    public String icon;

    @Schema(description = "Field definitions for this group")
    public List<FieldDefinition> fields;

    public ServiceFieldGroup(String id, String title, String icon, List<FieldDefinition> fields) {
      this.id = id;
      this.title = title;
      this.icon = icon;
      this.fields = fields;
    }
  }
}

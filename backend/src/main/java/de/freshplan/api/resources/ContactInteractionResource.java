package de.freshplan.api.resources;

import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.service.ContactInteractionService;
import de.freshplan.domain.customer.service.DataHygieneService;
import de.freshplan.domain.customer.service.dto.ContactDTO;
import de.freshplan.domain.customer.service.dto.ContactInteractionDTO;
import de.freshplan.domain.customer.service.dto.DataFreshnessLevel;
import de.freshplan.domain.customer.service.dto.DataQualityMetricsDTO;
import de.freshplan.domain.customer.service.dto.DataQualityScore;
import de.freshplan.domain.customer.service.dto.WarmthScoreDTO;
import de.freshplan.domain.customer.service.mapper.ContactMapper;
import io.quarkus.panache.common.Page;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/** REST resource for contact interactions and data intelligence features */
@Path("/api/contact-interactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(
    name = "Contact Interactions",
    description = "Manage contact interactions and intelligence metrics")
public class ContactInteractionResource {

  private static final Logger LOG = Logger.getLogger(ContactInteractionResource.class);

  @Inject ContactInteractionService interactionService;

  @Inject DataHygieneService dataHygieneService;

  @Inject ContactRepository contactRepository;

  @Inject ContactMapper contactMapper;

  @POST
  @Operation(summary = "Create a new contact interaction")
  @APIResponses({
    @APIResponse(responseCode = "201", description = "Interaction created successfully"),
    @APIResponse(responseCode = "400", description = "Invalid interaction data"),
    @APIResponse(responseCode = "404", description = "Contact not found")
  })
  public Response createInteraction(@Valid ContactInteractionDTO interaction) {
    LOG.infof("Creating interaction for contact %s", interaction.getContactId());
    ContactInteractionDTO created = interactionService.createInteraction(interaction);
    return Response.status(Response.Status.CREATED).entity(created).build();
  }

  @GET
  @Path("/contact/{contactId}")
  @Operation(summary = "Get interactions for a specific contact")
  @APIResponses({
    @APIResponse(responseCode = "200", description = "List of interactions"),
    @APIResponse(responseCode = "404", description = "Contact not found")
  })
  public List<ContactInteractionDTO> getContactInteractions(
      @Parameter(description = "Contact ID", required = true) @PathParam("contactId")
          UUID contactId,
      @Parameter(description = "Page number (0-based)") @QueryParam("page") @DefaultValue("0")
          int page,
      @Parameter(description = "Page size") @QueryParam("size") @DefaultValue("20") int size) {

    LOG.infof("Getting interactions for contact %s, page %d, size %d", contactId, page, size);
    return interactionService.getInteractionsByContact(contactId, Page.of(page, size));
  }

  @POST
  @Path("/contact/{contactId}/note")
  @Operation(summary = "Quick create a note interaction")
  @APIResponses({
    @APIResponse(responseCode = "201", description = "Note created successfully"),
    @APIResponse(responseCode = "404", description = "Contact not found")
  })
  public Response createNote(
      @Parameter(description = "Contact ID", required = true) @PathParam("contactId")
          UUID contactId,
      @Parameter(description = "Note content", required = true) String note,
      @Parameter(description = "User creating the note") @HeaderParam("X-User-Id") String userId) {

    LOG.infof("Creating note for contact %s by user %s", contactId, userId);
    ContactInteractionDTO created = interactionService.recordNote(contactId, note, userId);
    return Response.status(Response.Status.CREATED).entity(created).build();
  }

  @GET
  @Path("/contact/{contactId}/warmth-score")
  @Operation(summary = "Calculate warmth score for a contact")
  @APIResponses({
    @APIResponse(responseCode = "200", description = "Warmth score calculated"),
    @APIResponse(responseCode = "404", description = "Contact not found")
  })
  public WarmthScoreDTO getWarmthScore(
      @Parameter(description = "Contact ID", required = true) @PathParam("contactId")
          UUID contactId) {

    LOG.infof("Calculating warmth score for contact %s", contactId);
    return interactionService.calculateWarmthScore(contactId);
  }

  @GET
  @Path("/metrics/data-quality")
  @Operation(summary = "Get data quality metrics for intelligence features")
  @APIResponse(responseCode = "200", description = "Data quality metrics")
  public DataQualityMetricsDTO getDataQualityMetrics() {
    LOG.info("Getting data quality metrics");
    return interactionService.getDataQualityMetrics();
  }

  @POST
  @Path("/import/batch")
  @Operation(summary = "Batch import historical interactions")
  @APIResponses({
    @APIResponse(responseCode = "200", description = "Import completed"),
    @APIResponse(responseCode = "400", description = "Invalid import data"),
    @APIResponse(responseCode = "500", description = "Import failed")
  })
  public Response batchImport(List<ContactInteractionDTO> interactions) {
    LOG.infof("Batch importing %d interactions", interactions.size());

    try {
      ContactInteractionService.BatchImportResult result =
          interactionService.batchImportInteractions(interactions);

      if (result.failed > 0) {
        LOG.warnf("Batch import completed with %d failures", result.failed);
      }

      return Response.ok().entity(result).build();
    } catch (Exception e) {
      LOG.error("Batch import failed", e);
      return Response.serverError()
          .entity(new ErrorResponse("Batch import failed: " + e.getMessage()))
          .build();
    }
  }

  /** Get freshness level for a specific contact */
  @GET
  @Path("/freshness/{contactId}")
  @Operation(summary = "Get data freshness level for a contact")
  @APIResponses({
    @APIResponse(responseCode = "200", description = "Freshness level retrieved successfully"),
    @APIResponse(responseCode = "404", description = "Contact not found")
  })
  public Response getContactFreshnessLevel(@PathParam("contactId") UUID contactId) {
    try {
      CustomerContact contact = contactRepository.findByIdOptional(contactId).orElse(null);
      if (contact == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      DataFreshnessLevel freshnessLevel = dataHygieneService.getDataFreshnessLevel(contact);
      return Response.ok(freshnessLevel).build();
    } catch (Exception e) {
      LOG.error("Error getting freshness level for contact: " + contactId, e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /** Get data quality score for a specific contact */
  @GET
  @Path("/quality-score/{contactId}")
  @Operation(summary = "Get comprehensive data quality score for a contact")
  @APIResponses({
    @APIResponse(responseCode = "200", description = "Quality score calculated successfully"),
    @APIResponse(responseCode = "404", description = "Contact not found")
  })
  public Response getContactQualityScore(@PathParam("contactId") UUID contactId) {
    try {
      CustomerContact contact = contactRepository.findByIdOptional(contactId).orElse(null);
      if (contact == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      DataQualityScore qualityScore = dataHygieneService.calculateDataQualityScore(contact);
      return Response.ok(qualityScore).build();
    } catch (Exception e) {
      LOG.error("Error calculating quality score for contact: " + contactId, e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /** Get freshness statistics for all contacts */
  @GET
  @Path("/statistics/freshness")
  @Operation(summary = "Get data freshness statistics")
  @APIResponse(responseCode = "200", description = "Freshness statistics retrieved successfully")
  public Response getFreshnessStatistics() {
    try {
      Map<DataFreshnessLevel, Long> statistics = dataHygieneService.getDataFreshnessStatistics();
      return Response.ok(statistics).build();
    } catch (Exception e) {
      LOG.error("Error getting freshness statistics", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /** Get contacts by freshness level */
  @GET
  @Path("/contacts-by-freshness/{level}")
  @Operation(summary = "Get contacts filtered by freshness level")
  @APIResponses({
    @APIResponse(responseCode = "200", description = "Contacts retrieved successfully"),
    @APIResponse(responseCode = "400", description = "Invalid freshness level")
  })
  public Response getContactsByFreshnessLevel(@PathParam("level") String levelStr) {
    try {
      DataFreshnessLevel level = DataFreshnessLevel.valueOf(levelStr.toUpperCase());
      List<CustomerContact> contacts = dataHygieneService.findContactsByFreshnessLevel(level);

      // Convert entities to DTOs to avoid LazyInitializationException
      List<ContactDTO> contactDTOs = contacts.stream()
          .map(contactMapper::toDTO)
          .toList();

      return Response.ok(contactDTOs).build();
    } catch (IllegalArgumentException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Invalid freshness level: " + levelStr)
          .build();
    } catch (Exception e) {
      LOG.error("Error getting contacts by freshness level: " + levelStr, e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /** Trigger manual data hygiene check */
  @POST
  @Path("/trigger-hygiene-check")
  @Operation(summary = "Manually trigger data hygiene check")
  @APIResponse(responseCode = "200", description = "Hygiene check completed successfully")
  public Response triggerHygieneCheck() {
    try {
      dataHygieneService.checkDataFreshness();
      dataHygieneService.updateDataQualityScores();
      return Response.ok()
          .entity("{\"message\": \"Data hygiene check completed successfully\"}")
          .build();
    } catch (Exception e) {
      LOG.error("Error during manual hygiene check", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  // Inner class for error responses
  public static class ErrorResponse {
    public final String message;

    public ErrorResponse(String message) {
      this.message = message;
    }
  }
}

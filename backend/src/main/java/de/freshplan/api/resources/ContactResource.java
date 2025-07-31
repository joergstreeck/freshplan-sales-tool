package de.freshplan.api.resources;

import de.freshplan.domain.customer.service.ContactService;
import de.freshplan.domain.customer.service.dto.ContactDTO;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST API endpoints for Contact management.
 * Provides CRUD operations for contacts with multi-contact support per customer.
 */
@Path("/api/customers/{customerId}/contacts")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Contacts", description = "Contact management operations")
public class ContactResource {
    
    @Inject
    ContactService contactService;
    
    @GET
    @Operation(summary = "Get all contacts for a customer")
    @APIResponse(responseCode = "200", description = "List of contacts")
    public List<ContactDTO> getContacts(
            @Parameter(description = "Customer ID", required = true)
            @PathParam("customerId") UUID customerId) {
        return contactService.getContactsByCustomerId(customerId);
    }
    
    @POST
    @Operation(summary = "Create a new contact for a customer")
    @APIResponse(responseCode = "201", description = "Contact created successfully")
    @APIResponse(responseCode = "400", description = "Invalid contact data")
    @APIResponse(responseCode = "404", description = "Customer not found")
    public Response createContact(
            @Parameter(description = "Customer ID", required = true)
            @PathParam("customerId") UUID customerId,
            @Valid ContactDTO contactDTO) {
        ContactDTO created = contactService.createContact(customerId, contactDTO);
        return Response.created(
            URI.create("/api/customers/" + customerId + "/contacts/" + created.getId())
        ).entity(created).build();
    }
    
    @GET
    @Path("/{contactId}")
    @Operation(summary = "Get a specific contact")
    @APIResponse(responseCode = "200", description = "Contact found")
    @APIResponse(responseCode = "404", description = "Contact not found")
    public ContactDTO getContact(
            @Parameter(description = "Customer ID", required = true)
            @PathParam("customerId") UUID customerId,
            @Parameter(description = "Contact ID", required = true)
            @PathParam("contactId") UUID contactId) {
        return contactService.getContact(contactId);
    }
    
    @PUT
    @Path("/{contactId}")
    @Operation(summary = "Update a contact")
    @APIResponse(responseCode = "200", description = "Contact updated successfully")
    @APIResponse(responseCode = "400", description = "Invalid contact data")
    @APIResponse(responseCode = "404", description = "Contact not found")
    public ContactDTO updateContact(
            @Parameter(description = "Customer ID", required = true)
            @PathParam("customerId") UUID customerId,
            @Parameter(description = "Contact ID", required = true)
            @PathParam("contactId") UUID contactId,
            @Valid ContactDTO contactDTO) {
        contactDTO.setId(contactId);
        return contactService.updateContact(contactId, contactDTO);
    }
    
    @DELETE
    @Path("/{contactId}")
    @Operation(summary = "Delete a contact (soft delete)")
    @APIResponse(responseCode = "204", description = "Contact deleted successfully")
    @APIResponse(responseCode = "400", description = "Cannot delete primary contact if others exist")
    @APIResponse(responseCode = "404", description = "Contact not found")
    public Response deleteContact(
            @Parameter(description = "Customer ID", required = true)
            @PathParam("customerId") UUID customerId,
            @Parameter(description = "Contact ID", required = true)
            @PathParam("contactId") UUID contactId) {
        contactService.deleteContact(contactId);
        return Response.noContent().build();
    }
    
    @PUT
    @Path("/{contactId}/set-primary")
    @Operation(summary = "Set a contact as primary")
    @APIResponse(responseCode = "204", description = "Primary contact updated")
    @APIResponse(responseCode = "404", description = "Contact not found")
    public Response setPrimaryContact(
            @Parameter(description = "Customer ID", required = true)
            @PathParam("customerId") UUID customerId,
            @Parameter(description = "Contact ID", required = true)
            @PathParam("contactId") UUID contactId) {
        contactService.setPrimaryContact(customerId, contactId);
        return Response.noContent().build();
    }
    
    // Additional endpoints
    
    @GET
    @Path("/birthdays")
    @Operation(summary = "Get contacts with upcoming birthdays")
    @APIResponse(responseCode = "200", description = "List of contacts with birthdays")
    public List<ContactDTO> getUpcomingBirthdays(
            @Parameter(description = "Days ahead to check", example = "7")
            @QueryParam("days") @DefaultValue("7") int daysAhead) {
        return contactService.getUpcomingBirthdays(daysAhead);
    }
    
    @POST
    @Path("/check-email")
    @Operation(summary = "Check if email is already in use")
    @APIResponse(responseCode = "200", description = "Email availability check result")
    public Response checkEmailAvailability(
            @Parameter(description = "Email to check", required = true)
            @QueryParam("email") String email,
            @Parameter(description = "Contact ID to exclude from check")
            @QueryParam("excludeId") UUID excludeId) {
        boolean inUse = contactService.isEmailInUse(email, excludeId);
        return Response.ok()
            .entity(new EmailCheckResult(email, !inUse))
            .build();
    }
    
    // Inner class for email check response
    public static class EmailCheckResult {
        public String email;
        public boolean available;
        
        public EmailCheckResult(String email, boolean available) {
            this.email = email;
            this.available = available;
        }
    }
}

/**
 * Separate resource for location-based contact queries
 */
@Path("/api/locations/{locationId}/contacts")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Contacts", description = "Contact management operations")
class LocationContactResource {
    
    @Inject
    ContactService contactService;
    
    @GET
    @Operation(summary = "Get all contacts assigned to a location")
    @APIResponse(responseCode = "200", description = "List of contacts")
    public List<ContactDTO> getContactsByLocation(
            @Parameter(description = "Location ID", required = true)
            @PathParam("locationId") UUID locationId) {
        return contactService.getContactsByLocationId(locationId);
    }
}
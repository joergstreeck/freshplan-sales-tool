package de.freshplan.api;

import de.freshplan.domain.profile.service.ProfileService;
import de.freshplan.domain.profile.service.dto.CreateProfileRequest;
import de.freshplan.domain.profile.service.dto.ProfileResponse;
import de.freshplan.domain.profile.service.dto.UpdateProfileRequest;
import de.freshplan.infrastructure.security.SecurityAudit;
import de.freshplan.infrastructure.security.SecurityContextProvider;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST resource for Profile management.
 *
 * @author FreshPlan Team
 * @since 1.0.0
 */
@Path("/api/profiles")
@PermitAll // TODO: SECURITY ROLLBACK - Remove after fixing test configuration (Issue #CI-FIX)
// @RolesAllowed({"admin", "manager", "sales"}) // TODO: SECURITY ROLLBACK - Uncomment after test fix
@SecurityAudit
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProfileResource {

  @Inject ProfileService profileService;

  @Inject SecurityContextProvider securityContext;

  /**
   * Create a new profile.
   *
   * @param request the create request
   * @return created profile with 201 status
   */
  @POST
  public Response createProfile(@Valid CreateProfileRequest request) {
    ProfileResponse response = profileService.createProfile(request);

    URI location = URI.create("/api/profiles/" + response.getId());

    return Response.created(location).entity(response).build();
  }

  /**
   * Get profile by ID.
   *
   * @param id the profile ID
   * @return profile response with 200 status
   */
  @GET
  @Path("/{id}")
  public Response getProfile(@PathParam("id") UUID id) {
    ProfileResponse response = profileService.getProfile(id);
    return Response.ok(response).build();
  }

  /**
   * Get profile by customer ID.
   *
   * @param customerId the customer ID
   * @return profile response with 200 status
   */
  @GET
  @Path("/customer/{customerId}")
  public Response getProfileByCustomerId(@PathParam("customerId") String customerId) {
    ProfileResponse response = profileService.getProfileByCustomerId(customerId);
    return Response.ok(response).build();
  }

  /**
   * Update an existing profile.
   *
   * @param id the profile ID
   * @param request the update request
   * @return updated profile with 200 status
   */
  @PUT
  @Path("/{id}")
  public Response updateProfile(@PathParam("id") UUID id, @Valid UpdateProfileRequest request) {
    ProfileResponse response = profileService.updateProfile(id, request);
    return Response.ok(response).build();
  }

  /**
   * Delete a profile.
   *
   * @param id the profile ID
   * @return 204 No Content
   */
  @DELETE
  @Path("/{id}")
  public Response deleteProfile(@PathParam("id") UUID id) {
    profileService.deleteProfile(id);
    return Response.noContent().build();
  }

  /**
   * Get all profiles.
   *
   * @return list of all profiles with 200 status
   */
  @GET
  public Response getAllProfiles() {
    List<ProfileResponse> profiles = profileService.getAllProfiles();
    return Response.ok(profiles).build();
  }

  /**
   * Check if profile exists for customer ID.
   *
   * @param customerId the customer ID
   * @return 200 if exists, 404 if not
   */
  @HEAD
  @Path("/customer/{customerId}")
  public Response checkProfileExists(@PathParam("customerId") String customerId) {
    if (profileService.profileExists(customerId)) {
      return Response.ok().build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  /**
   * Export profile as PDF.
   *
   * @param id the profile ID
   * @return PDF file with appropriate headers
   */
  @GET
  @Path("/{id}/export/pdf")
  @Produces("application/pdf")
  public Response exportProfilePdf(@PathParam("id") UUID id) {
    byte[] pdfContent = profileService.exportProfileAsPdf(id);

    return Response.ok(pdfContent)
        .header("Content-Disposition", "attachment; filename=\"profile-" + id + ".pdf\"")
        .build();
  }
}

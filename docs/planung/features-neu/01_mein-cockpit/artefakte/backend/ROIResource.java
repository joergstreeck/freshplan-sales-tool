package de.freshplan.cockpit.api;

import de.freshplan.cockpit.dto.ROIDTO;
import de.freshplan.cockpit.service.ROIService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/**
 * ROIResource – Foundation Standards
 * - API Standards: /docs/planung/grundlagen/API_STANDARDS.md
 * - Security: ABAC (territories/channels) enforced via ContainerRequestFilter
 * - Validation: Bean Validation on request
 * - Errors: RFC7807 via ProblemExceptionMapper
 */
@Path("/api/cockpit/roi")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ROIResource {

  @Inject ROIService roi;

  @POST
  @Path("/calc")
  @RolesAllowed({"user","manager","admin"})
  public ROIDTO.CalcResponse calc(@Valid ROIDTO.CalcRequest req) {
    return roi.calculate(req);
  }
}

package de.freshplan.communication.api;

import de.freshplan.communication.repo.CommunicationRepository;
import de.freshplan.security.ScopeContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.time.OffsetDateTime;
import java.util.*;

@Path("/api/comm")
@Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
public class CommActivityResource {

  @Inject CommunicationRepository repo;
  @Inject ScopeContext scope;

  public static class CallReq {
    @NotNull public String customerId;
    @NotBlank public String occurredAt;
    @NotBlank public String summary;
    public List<@Email String> participants;
  }
  public static class MeetingReq {
    @NotNull public String customerId;
    @NotBlank public String occurredAt;
    @NotBlank public String summary;
    public List<@Email String> attendees;
  }

  @POST @Path("/calls") @RolesAllowed({"user","manager"})
  public Response logCall(@Valid CallReq req) {
    if (scope.getTerritories().isEmpty()) throw new ForbiddenException("No authorized territories");
    repo.logCall(java.util.UUID.fromString(req.customerId), scope.getTerritories().get(0),
      OffsetDateTime.parse(req.occurredAt), req.summary,
      new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(req.participants==null? java.util.List.of() : req.participants).toString());
    return Response.status(Response.Status.CREATED).build();
  }

  @POST @Path("/meetings") @RolesAllowed({"user","manager"})
  public Response logMeeting(@Valid MeetingReq req) {
    if (scope.getTerritories().isEmpty()) throw new ForbiddenException("No authorized territories");
    repo.logMeeting(java.util.UUID.fromString(req.customerId), scope.getTerritories().get(0),
      OffsetDateTime.parse(req.occurredAt), req.summary,
      new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(req.attendees==null? java.util.List.of() : req.attendees).toString());
    return Response.status(Response.Status.CREATED).build();
  }
}

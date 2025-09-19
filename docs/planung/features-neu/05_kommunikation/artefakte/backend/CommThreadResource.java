package de.freshplan.communication.api;

import de.freshplan.communication.repo.CommunicationRepository;
import de.freshplan.security.ScopeContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;

@Path("/api/comm/threads")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommThreadResource {

  @Inject CommunicationRepository repo;
  @Inject ScopeContext scope;

  @GET @RolesAllowed({"user","manager"})
  public Map<String,Object> list(@QueryParam("q") String q,
                                 @QueryParam("customerId") String customerId,
                                 @QueryParam("channel") String channel,
                                 @QueryParam("unread") Boolean unread,
                                 @QueryParam("from") String from,
                                 @QueryParam("to") String to,
                                 @QueryParam("limit") @DefaultValue("50") int limit,
                                 @QueryParam("cursor") String cursor) {
    if (scope.getTerritories().isEmpty()) throw new ForbiddenException("No authorized territories");
    return repo.pageThreads(q, customerId, channel, unread, from, to, limit, cursor);
  }

  public static class CreateThreadReq {
    @NotNull public String customerId;
    @NotBlank public String subject;
    @NotBlank public String channel; // EMAIL|PHONE|MEETING|NOTE
    public List<String> participants;
  }

  @POST @RolesAllowed({"user","manager"})
  public Response create(CreateThreadReq req) {
    if (scope.getTerritories().isEmpty()) throw new ForbiddenException("No authorized territories");
    var tid = repo.createThread(java.util.UUID.fromString(req.customerId), scope.getTerritories().get(0), req.subject, req.channel, req.participants);
    return Response.status(Response.Status.CREATED).entity(Map.of("id", tid.toString())).build();
  }

  public static class ReplyReq { @NotBlank public String bodyText; public List<String> cc; }
  @POST @Path("/{id}/reply") @RolesAllowed({"user","manager"})
  public Response reply(@PathParam("id") String id, @HeaderParam("If-Match") String ifMatch, ReplyReq req) {
    if (ifMatch == null || ifMatch.isBlank()) throw new BadRequestException("If-Match header required");
    if (scope.getTerritories().isEmpty()) throw new ForbiddenException("No authorized territories");
    repo.replyToThread(java.util.UUID.fromString(id), ifMatch, scope.getTerritories().get(0), req.bodyText, req.cc);
    return Response.status(Response.Status.ACCEPTED).build();
  }
}

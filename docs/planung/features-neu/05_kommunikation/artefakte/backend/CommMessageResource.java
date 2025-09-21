package de.freshplan.communication.api;

import de.freshplan.communication.repo.CommunicationRepository;
import de.freshplan.security.ScopeContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/comm/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CommMessageResource {

  @Inject CommunicationRepository repo;
  @Inject ScopeContext scope;

  public static class StartConversationReq {
    @NotNull public String customerId;
    @NotBlank public String subject;
    @NotNull @Size(min=1) public List<@Email String> to;
    public List<@Email String> cc;
    @NotBlank public String bodyText;
    public String accountId;
  }

  @POST @RolesAllowed({"user","manager"})
  public Response start(@Valid StartConversationReq req) {
    if (scope.getTerritories().isEmpty()) throw new ForbiddenException("No authorized territories");
    var tid = repo.startConversation(UUID.fromString(req.customerId), scope.getTerritories().get(0), req.subject, req.to, req.cc, req.bodyText);
    return Response.status(Response.Status.CREATED).entity(Map.of("threadId", tid.toString())).build();
  }
}

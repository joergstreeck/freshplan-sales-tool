package de.freshplan.api;

import de.freshplan.domain.user.service.UserService;
import de.freshplan.domain.user.service.dto.CreateUserRequest;
import de.freshplan.domain.user.service.dto.UpdateUserRequest;
import de.freshplan.domain.user.service.dto.UpdateUserRolesRequest;
import de.freshplan.domain.user.service.dto.UserResponse;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Development-only version of UserResource without security.
 * This is ONLY active in dev profile for local development.
 */
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@IfBuildProfile("dev")
public class DevUserResource {
    
    private final UserService userService;
    
    @Inject
    public DevUserResource(UserService userService) {
        this.userService = userService;
    }
    
    @GET
    public Response listUsers(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("search") String search,
            @QueryParam("enabledOnly") @DefaultValue("false") boolean enabledOnly) {
        
        List<UserResponse> users;
        long totalCount;
        
        if (search != null && !search.isBlank()) {
            users = userService.searchUsers(search, page, size);
            totalCount = userService.countUsers();
        } else if (enabledOnly) {
            users = userService.listEnabledUsers(page, size);
            totalCount = userService.countEnabledUsers();
        } else {
            users = userService.listUsers(page, size);
            totalCount = userService.countUsers();
        }
        
        return Response.ok(users)
                .header("X-Total-Count", totalCount)
                .header("X-Page-Number", page)
                .header("X-Page-Size", size)
                .build();
    }
    
    @POST
    public Response createUser(CreateUserRequest request, @Context UriInfo uriInfo) {
        UserResponse created = userService.createUser(request);
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(created.getId().toString())
                .build();
        return Response.created(location).entity(created).build();
    }
    
    @GET
    @Path("/{id}")
    public UserResponse getUser(@PathParam("id") UUID id) {
        return userService.getUser(id);
    }
    
    @PUT
    @Path("/{id}")
    public UserResponse updateUser(@PathParam("id") UUID id, UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }
    
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") UUID id) {
        userService.deleteUser(id);
        return Response.noContent().build();
    }
    
    @PUT
    @Path("/{id}/enable")
    public Response enableUser(@PathParam("id") UUID id) {
        userService.enableUser(id);
        return Response.noContent().build();
    }
    
    @PUT
    @Path("/{id}/disable")
    public Response disableUser(@PathParam("id") UUID id) {
        userService.disableUser(id);
        return Response.noContent().build();
    }
    
    @PUT
    @Path("/{id}/roles")
    public UserResponse updateUserRoles(@PathParam("id") UUID id, UpdateUserRolesRequest request) {
        return userService.updateUserRoles(id, request);
    }
}
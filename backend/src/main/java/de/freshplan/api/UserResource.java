package de.freshplan.api;

import de.freshplan.user.UserService;
import de.freshplan.user.dto.CreateUserRequest;
import de.freshplan.user.dto.UpdateUserRequest;
import de.freshplan.user.dto.UserResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

/**
 * REST resource for User management.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {
    
    @Inject
    UserService userService;
    
    /**
     * Get all users.
     */
    @GET
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }
    
    /**
     * Get user by ID.
     */
    @GET
    @Path("/{id}")
    public UserResponse getUserById(@PathParam("id") UUID id) {
        return userService.getUserById(id);
    }
    
    /**
     * Create new user.
     */
    @POST
    public Response createUser(@Valid CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return Response.status(Response.Status.CREATED).entity(user).build();
    }
    
    /**
     * Update existing user.
     */
    @PUT
    @Path("/{id}")
    public UserResponse updateUser(@PathParam("id") UUID id, @Valid UpdateUserRequest request) {
        request.setId(id);
        return userService.updateUser(id, request);
    }
    
    /**
     * Delete user.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") UUID id) {
        userService.deleteUser(id);
        return Response.noContent().build();
    }
    
    /**
     * Enable user.
     */
    @PUT
    @Path("/{id}/enable")
    public Response enableUser(@PathParam("id") UUID id) {
        userService.enableUser(id);
        return Response.noContent().build();
    }
    
    /**
     * Disable user.
     */
    @PUT
    @Path("/{id}/disable")
    public Response disableUser(@PathParam("id") UUID id) {
        userService.disableUser(id);
        return Response.noContent().build();
    }
}
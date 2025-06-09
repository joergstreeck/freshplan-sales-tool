package de.freshplan.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Simple health check endpoint.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/ping")
@Produces(MediaType.APPLICATION_JSON)
public class PingResource {
    
    @GET
    public Response ping() {
        return Response.ok(Map.of(
            "status", "ok",
            "timestamp", LocalDateTime.now().toString(),
            "service", "freshplan-backend",
            "version", "2.0.0"
        )).build();
    }
}
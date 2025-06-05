package de.freshplan.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Path("/api/ping")
public class PingResource {

    // JWT injection removed - OIDC is disabled in tests

    @Inject
    DataSource dataSource;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "pong");
        response.put("timestamp", Instant.now().toString());
        response.put("user", "test-user"); // Hardcoded for tests
        
        // Optional: DB-Check
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT current_timestamp")) {
            if (rs.next()) {
                response.put("dbTime", rs.getTimestamp(1).toString());
            }
        } catch (Exception e) {
            response.put("dbStatus", "error: " + e.getMessage());
        }
        
        return Response.ok(response).build();
    }
}
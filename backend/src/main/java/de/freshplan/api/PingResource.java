package de.freshplan.api;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Path("/api/ping")
@Authenticated
public class PingResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    DataSource dataSource;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "pong");
        response.put("timestamp", Instant.now().toString());
        response.put("user", jwt.getName());
        
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
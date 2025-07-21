package de.freshplan.api.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API für Reality Check Integration
 * Ermöglicht es anderen Tools, Reality Checks durchzuführen
 */
@Path("/api/reality-check")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RealityCheckResource {
    
    @GET
    @Path("/{featureCode}")
    public Response checkFeature(@PathParam("featureCode") String featureCode) {
        RealityCheckResult result = performRealityCheck(featureCode);
        
        if (result.isValid()) {
            return Response.ok(result).build();
        } else {
            return Response.status(Response.Status.CONFLICT)
                .entity(result)
                .build();
        }
    }
    
    @GET
    @Path("/dashboard")
    public Response getDashboard() {
        List<FeatureStatus> features = scanAllFeatures();
        
        DashboardResponse dashboard = new DashboardResponse();
        dashboard.setFeatures(features);
        dashboard.setLastScan(new Date());
        dashboard.setSummary(generateSummary(features));
        
        return Response.ok(dashboard).build();
    }
    
    @POST
    @Path("/{featureCode}/fix")
    public Response autoFix(@PathParam("featureCode") String featureCode,
                           FixRequest fixRequest) {
        // Auto-fix functionality
        // - Create missing files
        // - Update plan references
        // - Sync structure
        
        return Response.ok(new FixResponse("Fixed", List.of())).build();
    }
    
    private RealityCheckResult performRealityCheck(String featureCode) {
        // Implementation der Reality Check Logik
        return new RealityCheckResult();
    }
    
    private List<FeatureStatus> scanAllFeatures() {
        // Scan all features
        return List.of();
    }
    
    private Map<String, Object> generateSummary(List<FeatureStatus> features) {
        long valid = features.stream().filter(FeatureStatus::isValid).count();
        long invalid = features.size() - valid;
        
        return Map.of(
            "total", features.size(),
            "valid", valid,
            "invalid", invalid,
            "percentage", (valid * 100.0) / features.size()
        );
    }
}

// DTOs
class RealityCheckResult {
    private boolean valid;
    private List<String> missingFiles;
    private List<String> outdatedReferences;
    private Map<String, String> suggestions;
    
    // getters/setters
    public boolean isValid() { return valid; }
}

class FeatureStatus {
    private String featureCode;
    private boolean valid;
    private int missingFiles;
    private Date lastCheck;
    
    // getters/setters
    public boolean isValid() { return valid; }
}

class DashboardResponse {
    private List<FeatureStatus> features;
    private Date lastScan;
    private Map<String, Object> summary;
    
    // getters/setters
    public void setFeatures(List<FeatureStatus> features) { this.features = features; }
    public void setLastScan(Date lastScan) { this.lastScan = lastScan; }
    public void setSummary(Map<String, Object> summary) { this.summary = summary; }
}

class FixRequest {
    private String action; // "createFiles", "updatePlan", "syncStructure"
    private Map<String, String> parameters;
}

class FixResponse {
    private String status;
    private List<String> changes;
    
    public FixResponse(String status, List<String> changes) {
        this.status = status;
        this.changes = changes;
    }
}
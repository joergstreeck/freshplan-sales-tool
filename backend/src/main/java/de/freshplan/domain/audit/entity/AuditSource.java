package de.freshplan.domain.audit.entity;

/**
 * Source of audit events for tracking origin
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public enum AuditSource {
    /**
     * User action through the web UI
     */
    UI("User Interface"),
    
    /**
     * Direct API call (REST/GraphQL)
     */
    API("API Call"),
    
    /**
     * System-generated event (scheduled jobs, triggers)
     */
    SYSTEM("System Process"),
    
    /**
     * External webhook or callback
     */
    WEBHOOK("External Webhook"),
    
    /**
     * Batch job or import process
     */
    BATCH("Batch Process"),
    
    /**
     * Mobile application
     */
    MOBILE("Mobile App"),
    
    /**
     * Command line interface
     */
    CLI("Command Line"),
    
    /**
     * Integration with external system
     */
    INTEGRATION("External Integration"),
    
    /**
     * Test environment or automated test
     */
    TEST("Test System");
    
    private final String description;
    
    AuditSource(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this source is external (requires extra validation)
     */
    public boolean isExternal() {
        return this == WEBHOOK || this == API || this == INTEGRATION;
    }
    
    /**
     * Check if this source is automated (no direct user action)
     */
    public boolean isAutomated() {
        return this == SYSTEM || this == BATCH || this == TEST;
    }
}
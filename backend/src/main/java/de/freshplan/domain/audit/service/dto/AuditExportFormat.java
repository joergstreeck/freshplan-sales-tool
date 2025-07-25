package de.freshplan.domain.audit.service.dto;

/**
 * Supported export formats for audit data
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public enum AuditExportFormat {
    CSV("text/csv", ".csv"),
    JSON("application/json", ".json"),
    XML("application/xml", ".xml"),
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx");
    
    private final String mimeType;
    private final String extension;
    
    AuditExportFormat(String mimeType, String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public String getExtension() {
        return extension;
    }
}
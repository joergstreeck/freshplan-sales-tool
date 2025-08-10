package de.freshplan.infrastructure.export;

/**
 * Supported export formats in the Universal Export Framework.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public enum ExportFormat {
    /**
     * Comma-Separated Values - for spreadsheet applications
     */
    CSV("text/csv", ".csv", "CSV Export"),
    
    /**
     * Microsoft Excel format - for advanced spreadsheet features
     */
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
          ".xlsx", "Excel Export"),
    
    /**
     * JavaScript Object Notation - for data interchange
     */
    JSON("application/json", ".json", "JSON Export"),
    
    /**
     * HTML format - for browser display and print-to-PDF
     */
    HTML("text/html", ".html", "HTML Report"),
    
    /**
     * PDF format - native PDF generation using OpenPDF
     */
    PDF("application/pdf", ".pdf", "PDF Report");
    
    private final String contentType;
    private final String extension;
    private final String displayName;
    
    ExportFormat(String contentType, String extension, String displayName) {
        this.contentType = contentType;
        this.extension = extension;
        this.displayName = displayName;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public String getMimeType() {
        return contentType;  // Alias for contentType
    }
    
    public String getExtension() {
        return extension;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get format from string, case-insensitive
     */
    public static ExportFormat fromString(String format) {
        if (format == null) {
            throw new IllegalArgumentException("Format cannot be null");
        }
        
        try {
            return ExportFormat.valueOf(format.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Unknown export format: " + format + ". " +
                "Supported formats: CSV, EXCEL, JSON, HTML, PDF"
            );
        }
    }
    
    /**
     * Check if format supports streaming
     */
    public boolean supportsStreaming() {
        return this == CSV || this == JSON;
    }
    
    /**
     * Check if format is binary
     */
    public boolean isBinary() {
        return this == EXCEL;
    }
}
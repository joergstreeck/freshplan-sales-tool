package de.freshplan.domain.export.service.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO for export options (used in POST requests)
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class ExportOptions {
    
    @NotNull
    private String reportType; // e.g., "GDPR", "AUDIT", "CUSTOM"
    
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private List<String> entityTypes;
    private List<String> eventTypes;
    private boolean includeStatistics = true;
    private boolean includeCharts = false;
    private boolean includeSummary = true;
    private String format = "PDF"; // PDF, EXCEL, CSV
    private Map<String, Object> customOptions;
    
    // Default constructor
    public ExportOptions() {
    }
    
    // Constructor with report type
    public ExportOptions(String reportType) {
        this.reportType = reportType;
    }
    
    // Getters
    public String getReportType() {
        return reportType;
    }
    
    public LocalDateTime getDateFrom() {
        return dateFrom;
    }
    
    public LocalDateTime getDateTo() {
        return dateTo;
    }
    
    public List<String> getEntityTypes() {
        return entityTypes;
    }
    
    public List<String> getEventTypes() {
        return eventTypes;
    }
    
    public boolean isIncludeStatistics() {
        return includeStatistics;
    }
    
    public boolean isIncludeCharts() {
        return includeCharts;
    }
    
    public boolean isIncludeSummary() {
        return includeSummary;
    }
    
    public String getFormat() {
        return format;
    }
    
    public Map<String, Object> getCustomOptions() {
        return customOptions;
    }
    
    // Setters
    public void setReportType(String reportType) {
        this.reportType = reportType;
    }
    
    public void setDateFrom(LocalDateTime dateFrom) {
        this.dateFrom = dateFrom;
    }
    
    public void setDateTo(LocalDateTime dateTo) {
        this.dateTo = dateTo;
    }
    
    public void setEntityTypes(List<String> entityTypes) {
        this.entityTypes = entityTypes;
    }
    
    public void setEventTypes(List<String> eventTypes) {
        this.eventTypes = eventTypes;
    }
    
    public void setIncludeStatistics(boolean includeStatistics) {
        this.includeStatistics = includeStatistics;
    }
    
    public void setIncludeCharts(boolean includeCharts) {
        this.includeCharts = includeCharts;
    }
    
    public void setIncludeSummary(boolean includeSummary) {
        this.includeSummary = includeSummary;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public void setCustomOptions(Map<String, Object> customOptions) {
        this.customOptions = customOptions;
    }
    
    /**
     * Convert to map for logging/auditing
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("reportType", reportType);
        if (dateFrom != null) map.put("dateFrom", dateFrom);
        if (dateTo != null) map.put("dateTo", dateTo);
        if (entityTypes != null && !entityTypes.isEmpty()) map.put("entityTypes", entityTypes);
        if (eventTypes != null && !eventTypes.isEmpty()) map.put("eventTypes", eventTypes);
        map.put("includeStatistics", includeStatistics);
        map.put("includeCharts", includeCharts);
        map.put("includeSummary", includeSummary);
        map.put("format", format);
        if (customOptions != null && !customOptions.isEmpty()) map.put("customOptions", customOptions);
        return map;
    }
    
    @Override
    public String toString() {
        return "ExportOptions{" +
                "reportType='" + reportType + '\'' +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", format='" + format + '\'' +
                '}';
    }
}
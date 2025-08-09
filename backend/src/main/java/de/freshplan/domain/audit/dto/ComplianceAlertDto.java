package de.freshplan.domain.audit.dto;

import java.time.Instant;

/**
 * Data Transfer Object for compliance alerts.
 * Provides type-safe structure for compliance-related warnings and notifications.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class ComplianceAlertDto {
  
  private String id;
  private AlertType type;
  private AlertSeverity severity;
  private String title;
  private String description;
  private Instant createdAt;
  private Long affectedCount;
  private String recommendation;
  
  /**
   * Type of compliance alert
   */
  public enum AlertType {
    RETENTION("Data retention limit exceeded"),
    INTEGRITY("Data integrity issue detected"),
    ACCESS_VIOLATION("Unauthorized access attempt"),
    DATA_EXPORT("Sensitive data export detected"),
    PERMISSION_CHANGE("Permission modification detected"),
    DSGVO_VIOLATION("DSGVO compliance issue");
    
    private final String description;
    
    AlertType(String description) {
      this.description = description;
    }
    
    public String getDescription() {
      return description;
    }
  }
  
  /**
   * Severity level of the alert
   */
  public enum AlertSeverity {
    INFO("Information only"),
    WARNING("Requires attention"),
    CRITICAL("Immediate action required");
    
    private final String description;
    
    AlertSeverity(String description) {
      this.description = description;
    }
    
    public String getDescription() {
      return description;
    }
  }
  
  // Constructors
  public ComplianceAlertDto() {
    this.createdAt = Instant.now();
  }
  
  public ComplianceAlertDto(String id, AlertType type, AlertSeverity severity, 
                            String title, String description) {
    this();
    this.id = id;
    this.type = type;
    this.severity = severity;
    this.title = title;
    this.description = description;
  }
  
  // Builder pattern for fluent API
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private final ComplianceAlertDto alert = new ComplianceAlertDto();
    
    public Builder id(String id) {
      alert.id = id;
      return this;
    }
    
    public Builder type(AlertType type) {
      alert.type = type;
      return this;
    }
    
    public Builder severity(AlertSeverity severity) {
      alert.severity = severity;
      return this;
    }
    
    public Builder title(String title) {
      alert.title = title;
      return this;
    }
    
    public Builder description(String description) {
      alert.description = description;
      return this;
    }
    
    public Builder affectedCount(Long count) {
      alert.affectedCount = count;
      return this;
    }
    
    public Builder recommendation(String recommendation) {
      alert.recommendation = recommendation;
      return this;
    }
    
    public ComplianceAlertDto build() {
      if (alert.id == null) {
        alert.id = "alert-" + alert.type + "-" + System.currentTimeMillis();
      }
      return alert;
    }
  }
  
  // Getters and Setters
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public AlertType getType() {
    return type;
  }
  
  public void setType(AlertType type) {
    this.type = type;
  }
  
  public AlertSeverity getSeverity() {
    return severity;
  }
  
  public void setSeverity(AlertSeverity severity) {
    this.severity = severity;
  }
  
  public String getTitle() {
    return title;
  }
  
  public void setTitle(String title) {
    this.title = title;
  }
  
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public Instant getCreatedAt() {
    return createdAt;
  }
  
  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
  
  public Long getAffectedCount() {
    return affectedCount;
  }
  
  public void setAffectedCount(Long affectedCount) {
    this.affectedCount = affectedCount;
  }
  
  public String getRecommendation() {
    return recommendation;
  }
  
  public void setRecommendation(String recommendation) {
    this.recommendation = recommendation;
  }
}
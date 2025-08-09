package de.freshplan.audit.dto;

/** DTO für Compliance-Warnungen und Alerts im Audit-System. */
public class ComplianceAlertDto {

  public enum AlertType {
    INFO,
    WARNING,
    ERROR
  }

  public enum Severity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
  }

  private AlertType type;
  private String message;
  private Severity severity;
  private long affectedRecords;

  public ComplianceAlertDto() {}

  public ComplianceAlertDto(AlertType type, String message, Severity severity) {
    this.type = type;
    this.message = message;
    this.severity = severity;
  }

  public ComplianceAlertDto(
      AlertType type, String message, Severity severity, long affectedRecords) {
    this.type = type;
    this.message = message;
    this.severity = severity;
    this.affectedRecords = affectedRecords;
  }

  // Getters and Setters
  public AlertType getType() {
    return type;
  }

  public void setType(AlertType type) {
    this.type = type;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Severity getSeverity() {
    return severity;
  }

  public void setSeverity(Severity severity) {
    this.severity = severity;
  }

  public long getAffectedRecords() {
    return affectedRecords;
  }

  public void setAffectedRecords(long affectedRecords) {
    this.affectedRecords = affectedRecords;
  }
}

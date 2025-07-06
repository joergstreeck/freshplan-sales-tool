package de.freshplan.domain.cockpit.service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO für KI-gestützte Alerts im Sales Cockpit Dashboard.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class DashboardAlert {

  private UUID id;
  private String title;
  private String message;
  private AlertType type;
  private AlertSeverity severity;
  private UUID customerId;
  private String customerName;
  private LocalDateTime createdAt;
  private String actionLink;

  public enum AlertType {
    OPPORTUNITY, // Verkaufschance erkannt
    RISK, // Risiko erkannt
    CONTRACT_EXPIRY, // Vertragsablauf
    FOLLOW_UP, // Follow-up erforderlich
    MILESTONE // Wichtiger Meilenstein
  }

  public enum AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
  }

  public DashboardAlert() {}

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
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

  public UUID getCustomerId() {
    return customerId;
  }

  public void setCustomerId(UUID customerId) {
    this.customerId = customerId;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getActionLink() {
    return actionLink;
  }

  public void setActionLink(String actionLink) {
    this.actionLink = actionLink;
  }
}

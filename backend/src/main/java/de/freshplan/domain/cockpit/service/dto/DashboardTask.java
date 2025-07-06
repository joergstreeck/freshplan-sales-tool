package de.freshplan.domain.cockpit.service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO für eine Aufgabe im Sales Cockpit Dashboard.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class DashboardTask {

  private UUID id;
  private String title;
  private String description;
  private TaskType type;
  private TaskPriority priority;
  private UUID customerId;
  private String customerName;
  private LocalDateTime dueDate;
  private boolean completed;

  public enum TaskType {
    CALL,
    EMAIL,
    APPOINTMENT,
    TODO,
    FOLLOW_UP
  }

  public enum TaskPriority {
    HIGH,
    MEDIUM,
    LOW
  }

  public DashboardTask() {}

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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public TaskType getType() {
    return type;
  }

  public void setType(TaskType type) {
    this.type = type;
  }

  public TaskPriority getPriority() {
    return priority;
  }

  public void setPriority(TaskPriority priority) {
    this.priority = priority;
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

  public LocalDateTime getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDateTime dueDate) {
    this.dueDate = dueDate;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }
}

package de.freshplan.domain.opportunity.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO für das Erstellen einer neuen Opportunity
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class CreateOpportunityRequest {

  @NotBlank(message = "Opportunity Name ist erforderlich")
  @Size(max = 255, message = "Name darf maximal 255 Zeichen lang sein")
  private String name;

  @Size(max = 2000, message = "Beschreibung darf maximal 2000 Zeichen lang sein")
  private String description;

  private UUID customerId;

  private UUID assignedTo;

  @PositiveOrZero(message = "Erwarteter Wert muss positiv sein")
  private BigDecimal expectedValue;

  private LocalDate expectedCloseDate;

  // Default constructor
  public CreateOpportunityRequest() {}

  // Constructor
  public CreateOpportunityRequest(
      String name,
      String description,
      UUID customerId,
      UUID assignedTo,
      BigDecimal expectedValue,
      LocalDate expectedCloseDate) {
    this.name = name;
    this.description = description;
    this.customerId = customerId;
    this.assignedTo = assignedTo;
    this.expectedValue = expectedValue;
    this.expectedCloseDate = expectedCloseDate;
  }

  // Private constructor for Builder
  private CreateOpportunityRequest(Builder builder) {
    this.name = builder.name;
    this.description = builder.description;
    this.customerId = builder.customerId;
    this.assignedTo = builder.assignedTo;
    this.expectedValue = builder.expectedValue;
    this.expectedCloseDate = builder.expectedCloseDate;
  }

  // Getters und Setters
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public void setCustomerId(UUID customerId) {
    this.customerId = customerId;
  }

  public BigDecimal getExpectedValue() {
    return expectedValue;
  }

  public void setExpectedValue(BigDecimal expectedValue) {
    this.expectedValue = expectedValue;
  }

  public LocalDate getExpectedCloseDate() {
    return expectedCloseDate;
  }

  public void setExpectedCloseDate(LocalDate expectedCloseDate) {
    this.expectedCloseDate = expectedCloseDate;
  }

  public UUID getAssignedTo() {
    return assignedTo;
  }

  public void setAssignedTo(UUID assignedTo) {
    this.assignedTo = assignedTo;
  }

  // Builder
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String name;
    private String description;
    private UUID customerId;
    private UUID assignedTo;
    private BigDecimal expectedValue;
    private LocalDate expectedCloseDate;

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder customerId(UUID customerId) {
      this.customerId = customerId;
      return this;
    }

    public Builder assignedTo(UUID assignedTo) {
      this.assignedTo = assignedTo;
      return this;
    }

    public Builder expectedValue(BigDecimal expectedValue) {
      this.expectedValue = expectedValue;
      return this;
    }

    public Builder expectedCloseDate(LocalDate expectedCloseDate) {
      this.expectedCloseDate = expectedCloseDate;
      return this;
    }

    public CreateOpportunityRequest build() {
      return new CreateOpportunityRequest(this);
    }
  }

  @Override
  public String toString() {
    return "CreateOpportunityRequest{"
        + "name='"
        + name
        + '\''
        + ", description='"
        + description
        + '\''
        + ", customerId="
        + customerId
        + ", assignedTo="
        + assignedTo
        + ", expectedValue="
        + expectedValue
        + ", expectedCloseDate="
        + expectedCloseDate
        + '}';
  }
}

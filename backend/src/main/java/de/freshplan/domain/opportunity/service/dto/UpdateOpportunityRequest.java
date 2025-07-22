package de.freshplan.domain.opportunity.service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO für das Aktualisieren einer Opportunity
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class UpdateOpportunityRequest {

  @Size(max = 255, message = "Name darf maximal 255 Zeichen lang sein")
  private String name;

  @Size(max = 2000, message = "Beschreibung darf maximal 2000 Zeichen lang sein")
  private String description;

  private UUID customerId;

  @PositiveOrZero(message = "Erwarteter Wert muss positiv sein")
  private BigDecimal expectedValue;

  private LocalDate expectedCloseDate;

  @Min(value = 0, message = "Wahrscheinlichkeit muss zwischen 0 und 100 liegen")
  @Max(value = 100, message = "Wahrscheinlichkeit muss zwischen 0 und 100 liegen")
  private Integer probability;

  // Default constructor
  public UpdateOpportunityRequest() {}

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

  public Integer getProbability() {
    return probability;
  }

  public void setProbability(Integer probability) {
    this.probability = probability;
  }

  // Builder
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String name;
    private String description;
    private UUID customerId;
    private BigDecimal expectedValue;
    private LocalDate expectedCloseDate;
    private Integer probability;

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

    public Builder expectedValue(BigDecimal expectedValue) {
      this.expectedValue = expectedValue;
      return this;
    }

    public Builder expectedCloseDate(LocalDate expectedCloseDate) {
      this.expectedCloseDate = expectedCloseDate;
      return this;
    }

    public Builder probability(Integer probability) {
      this.probability = probability;
      return this;
    }

    public UpdateOpportunityRequest build() {
      UpdateOpportunityRequest request = new UpdateOpportunityRequest();
      request.name = this.name;
      request.description = this.description;
      request.customerId = this.customerId;
      request.expectedValue = this.expectedValue;
      request.expectedCloseDate = this.expectedCloseDate;
      request.probability = this.probability;
      return request;
    }
  }

  @Override
  public String toString() {
    return "UpdateOpportunityRequest{"
        + "name='"
        + name
        + '\''
        + ", description='"
        + description
        + '\''
        + ", customerId="
        + customerId
        + ", expectedValue="
        + expectedValue
        + ", expectedCloseDate="
        + expectedCloseDate
        + ", probability="
        + probability
        + '}';
  }
}

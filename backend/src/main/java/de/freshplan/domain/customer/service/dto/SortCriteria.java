package de.freshplan.domain.customer.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Sort criteria for customer search results. */
public class SortCriteria {

  @NotBlank(message = "Sort field is required")
  private String field;

  @Pattern(regexp = "ASC|DESC", message = "Direction must be ASC or DESC")
  private String direction = "ASC";

  // Constructors
  public SortCriteria() {}

  public SortCriteria(String field) {
    this.field = field;
  }

  public SortCriteria(String field, String direction) {
    this.field = field;
    this.direction = direction;
  }

  // Static factory methods
  public static SortCriteria asc(String field) {
    return new SortCriteria(field, "ASC");
  }

  public static SortCriteria desc(String field) {
    return new SortCriteria(field, "DESC");
  }

  // Getters and Setters
  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public String getDirection() {
    return direction;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }

  public boolean isAscending() {
    return "ASC".equalsIgnoreCase(direction);
  }
}

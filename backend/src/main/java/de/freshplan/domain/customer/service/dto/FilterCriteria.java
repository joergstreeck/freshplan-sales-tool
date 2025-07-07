package de.freshplan.domain.customer.service.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Represents a single filter criterion for dynamic customer search. Supports various operators and
 * logical combinations.
 */
public class FilterCriteria {

  @NotNull(message = "Filter field is required") private String field;

  @NotNull(message = "Filter operator is required") private FilterOperator operator;

  private Object value;

  private LogicalOperator combineWith = LogicalOperator.AND;

  // Constructors
  public FilterCriteria() {}

  public FilterCriteria(String field, FilterOperator operator, Object value) {
    this.field = field;
    this.operator = operator;
    this.value = value;
  }

  public FilterCriteria(
      String field, FilterOperator operator, Object value, LogicalOperator combineWith) {
    this(field, operator, value);
    this.combineWith = combineWith;
  }

  // Static factory methods for common filters
  public static FilterCriteria equals(String field, Object value) {
    return new FilterCriteria(field, FilterOperator.EQUALS, value);
  }

  public static FilterCriteria greaterThan(String field, Object value) {
    return new FilterCriteria(field, FilterOperator.GREATER_THAN, value);
  }

  public static FilterCriteria contains(String field, String value) {
    return new FilterCriteria(field, FilterOperator.CONTAINS, value);
  }

  public static FilterCriteria in(String field, Object value) {
    return new FilterCriteria(field, FilterOperator.IN, value);
  }

  // Getters and Setters
  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public FilterOperator getOperator() {
    return operator;
  }

  public void setOperator(FilterOperator operator) {
    this.operator = operator;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public LogicalOperator getCombineWith() {
    return combineWith;
  }

  public void setCombineWith(LogicalOperator combineWith) {
    this.combineWith = combineWith;
  }
}

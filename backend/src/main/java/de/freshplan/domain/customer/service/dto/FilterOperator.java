package de.freshplan.domain.customer.service.dto;

/** Supported filter operators for dynamic customer search. */
public enum FilterOperator {
  EQUALS,
  NOT_EQUALS,
  GREATER_THAN,
  GREATER_THAN_OR_EQUALS,
  LESS_THAN,
  LESS_THAN_OR_EQUALS,
  CONTAINS,
  STARTS_WITH,
  ENDS_WITH,
  IN,
  NOT_IN,
  IS_NULL,
  IS_NOT_NULL,
  BETWEEN
}

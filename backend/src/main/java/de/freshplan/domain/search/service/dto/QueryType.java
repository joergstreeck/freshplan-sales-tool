package de.freshplan.domain.search.service.dto;

/**
 * Type of search query detected through pattern matching.
 *
 * @since FC-005 PR4
 */
public enum QueryType {
  /** Query is an email address. */
  EMAIL,

  /** Query is a phone number. */
  PHONE,

  /** Query is a customer number. */
  CUSTOMER_NUMBER,

  /** Query is general text. */
  TEXT
}

package de.freshplan.domain.search.service.dto;

/**
 * Search context enum to specify the entity type to search.
 *
 * <p>Replaces string-based context parameter with type-safe enum.
 */
public enum SearchContext {
  /**
   * Search in leads.
   */
  LEADS,

  /**
   * Search in customers (default).
   */
  CUSTOMERS;

  /**
   * Converts a string to SearchContext enum.
   *
   * @param value String value ("leads" or "customers")
   * @return Corresponding SearchContext enum, defaults to CUSTOMERS if unknown
   */
  public static SearchContext fromString(String value) {
    if (value != null && "leads".equalsIgnoreCase(value)) {
      return LEADS;
    }
    return CUSTOMERS; // default
  }
}

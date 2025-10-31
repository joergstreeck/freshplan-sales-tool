package de.freshplan.domain.customer.entity;

/**
 * Industry sectors for customer classification.
 *
 * @deprecated This enum is deprecated since 2.1.6. Use {@link
 *     de.freshplan.domain.shared.BusinessType} instead. This field is kept for backward
 *     compatibility and will be removed in a future migration (V265+).
 */
@Deprecated(since = "2.1.6", forRemoval = true)
public enum Industry {
  /** Hotel and accommodation. */
  HOTEL,

  /** Restaurant and dining. */
  RESTAURANT,

  /** Catering services. */
  CATERING,

  /** Canteen and cafeteria. */
  KANTINE,

  /** Healthcare facilities. */
  GESUNDHEITSWESEN,

  /** Educational institutions. */
  BILDUNG,

  /** Events and entertainment. */
  VERANSTALTUNG,

  /** Retail and shops. */
  EINZELHANDEL,

  /**
   * Food service sector (legacy value from database).
   *
   * @deprecated Use {@link de.freshplan.domain.shared.BusinessType#GASTRONOMIE} instead
   */
  @Deprecated
  FOOD_SERVICE,

  /** Other industries. */
  SONSTIGE
}

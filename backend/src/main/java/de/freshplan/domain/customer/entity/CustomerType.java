package de.freshplan.domain.customer.entity;

/** Type of customer organization. */
public enum CustomerType {
  /** New customer prospect. */
  NEUKUNDE,

  /** Business customer (B2B). */
  UNTERNEHMEN,

  /** Government or public institution. */
  INSTITUTION,

  /** Private individual (B2C). */
  PRIVAT,

  /** Non-profit organization. */
  VEREIN,

  /** Other type not listed. */
  SONSTIGE
}

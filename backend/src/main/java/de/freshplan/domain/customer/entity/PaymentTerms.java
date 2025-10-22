package de.freshplan.domain.customer.entity;

/** Payment terms for customer billing. */
public enum PaymentTerms {
  /** Payment immediately upon delivery. */
  SOFORT,

  /** Payment within 7 days. */
  NETTO_7,

  /** Payment within 14 days. */
  NETTO_14,

  /** Payment within 15 days (English variant). */
  NET_15,

  /** Payment within 30 days. */
  NETTO_30,

  /** Payment within 30 days (English variant). */
  NET_30,

  /** Payment within 60 days. */
  NETTO_60,

  /** Payment within 90 days. */
  NETTO_90,

  /** Payment in advance. */
  VORKASSE,

  /** Direct debit. */
  LASTSCHRIFT
}

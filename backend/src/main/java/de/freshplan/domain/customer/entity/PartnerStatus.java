package de.freshplan.domain.customer.entity;

/** Partnership status of a customer. */
public enum PartnerStatus {
  /** Not a partner. */
  KEIN_PARTNER,

  /** Bronze level partner (English variant). */
  BRONZE,

  /** Bronze level partner (German variant with suffix). */
  BRONZE_PARTNER,

  /** Silver level partner (English variant). */
  SILVER,

  /** Silver level partner (German variant with suffix). */
  SILBER_PARTNER,

  /** Gold level partner (English/German variant). */
  GOLD,

  /** Gold level partner (German variant with suffix). */
  GOLD_PARTNER,

  /** Platinum level partner (English variant). */
  PLATINUM,

  /** Platinum level partner (German variant with suffix). */
  PLATIN_PARTNER
}

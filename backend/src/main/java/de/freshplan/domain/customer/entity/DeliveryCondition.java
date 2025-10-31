package de.freshplan.domain.customer.entity;

/** Delivery conditions for customer orders. */
public enum DeliveryCondition {
  /** Standard delivery terms. */
  STANDARD,

  /** Express delivery required. */
  EXPRESS,

  /** Delivered at Place (Incoterm) - seller delivers goods to buyer's location. */
  DAP,

  /** Customer picks up order. */
  SELBSTABHOLUNG,

  /** Free delivery included. */
  FREI_HAUS,

  /** Special delivery arrangements. */
  SONDERKONDITIONEN
}

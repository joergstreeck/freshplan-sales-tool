package de.freshplan.domain.customer.entity;

/** Customer lifecycle status. Represents the current state of a customer in the sales process. */
public enum CustomerStatus {
  /** Initial contact, not yet qualified. */
  LEAD,

  /** Qualified lead with active interest. */
  PROSPECT,

  /** Active customer with ongoing business. */
  AKTIV,

  /** Customer at risk of churning. */
  RISIKO,

  /** Inactive customer, no recent business. */
  INAKTIV,

  /** Archived customer, soft deleted. */
  ARCHIVIERT
}

package de.freshplan.modules.leads.domain;

/**
 * Lead status enum following the 6/60/10 rule from Handelsvertretervertrag. Sprint 2.1: State
 * machine for lead lifecycle management.
 */
public enum LeadStatus {
  /** Initial state when lead is created */
  REGISTERED,

  /** Lead has meaningful contact and is actively worked on */
  ACTIVE,

  /** 60 days without activity - reminder sent to owner (renamed for consistency) */
  REMINDER,

  /** After reminder period - 10 day grace period */
  GRACE_PERIOD,

  /** Lead has been qualified as a real opportunity */
  QUALIFIED,

  /** Lead has been converted to a customer */
  CONVERTED,

  /** Lead was lost (not converted) */
  LOST,

  /** Lead protection expired - can be reassigned */
  EXPIRED,

  /** Lead protection extended by management */
  EXTENDED,

  /** Soft delete - lead is archived */
  DELETED
}

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

  /** 60 days without activity - reminder sent to owner */
  REMINDER_SENT,

  /** After reminder period - 10 day grace period */
  GRACE_PERIOD,

  /** Lead protection expired - can be reassigned */
  EXPIRED,

  /** Lead protection extended by management */
  EXTENDED,

  /** Clock stopped due to FreshFoodz delays */
  STOP_CLOCK
}

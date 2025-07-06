package de.freshplan.domain.customer.entity;

/** Customer lifecycle stage for targeted marketing and support. */
public enum CustomerLifecycleStage {
  /** New customer acquisition phase. */
  ACQUISITION,

  /** Customer onboarding and integration. */
  ONBOARDING,

  /** Active growth and expansion phase. */
  GROWTH,

  /** Focus on customer retention. */
  RETENTION,

  /** Win-back or recovery efforts. */
  RECOVERY
}

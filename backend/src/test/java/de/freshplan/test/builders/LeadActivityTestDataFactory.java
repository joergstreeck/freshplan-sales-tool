package de.freshplan.test.builders;

import de.freshplan.modules.leads.domain.*;
import io.vertx.core.json.JsonObject;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Test data factory for LeadActivity entities. Provides builder pattern for creating test
 * activities without CDI.
 *
 * <p>Track 2C - Advanced Test Infrastructure with RealisticDataGenerator
 *
 * @author Claude
 * @since Track 2C - Integrated with RealisticDataGenerator
 */
public class LeadActivityTestDataFactory {

  // Thread-local RealisticDataGenerator for realistic defaults
  private static final ThreadLocal<RealisticDataGenerator> GENERATOR =
      ThreadLocal.withInitial(() -> new RealisticDataGenerator());

  /**
   * Create a seeded generator for deterministic tests.
   *
   * @param seed Seed value for repeatable randomization
   * @return Builder with seeded generator
   */
  public static Builder builder(long seed) {
    Builder builder = new Builder();
    builder.generator = new RealisticDataGenerator(seed);
    return builder;
  }

  /** Create a new builder instance. */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    // RealisticDataGenerator instance (thread-local or seeded)
    private RealisticDataGenerator generator = GENERATOR.get();

    // Required fields
    private Lead lead; // Must be set explicitly
    private String userId = "test-user";
    private ActivityType activityType = ActivityType.NOTE;
    private LocalDateTime activityDate = LocalDateTime.now().minusHours(2);
    private LocalDateTime createdAt = LocalDateTime.now();

    // Optional fields
    private String description; // Lazy-initialized
    private JsonObject metadata = new JsonObject();
    private boolean isMeaningfulContact = false;
    private boolean resetsTimer = false;
    private boolean countsAsProgress = false;
    private String summary;

    // Sprint 2.1.7 Issue #126: ActivityOutcome
    private ActivityOutcome outcome;
    private String nextAction;
    private LocalDate nextActionDate;
    private String performedBy;

    /** Set the lead this activity belongs to (REQUIRED). */
    public Builder forLead(Lead lead) {
      this.lead = lead;
      return this;
    }

    /** Set the user ID. */
    public Builder withUserId(String userId) {
      this.userId = userId;
      return this;
    }

    /** Set the activity type. */
    public Builder withActivityType(ActivityType activityType) {
      this.activityType = activityType;
      // Auto-set meaningful contact and timer reset based on type
      this.isMeaningfulContact = activityType.isMeaningfulContact();
      this.resetsTimer = activityType.resetsTimer();
      return this;
    }

    /** Set the activity date. */
    public Builder withActivityDate(LocalDateTime activityDate) {
      this.activityDate = activityDate;
      return this;
    }

    /** Set the description. */
    public Builder withDescription(String description) {
      this.description = description;
      return this;
    }

    /** Set the metadata. */
    public Builder withMetadata(JsonObject metadata) {
      this.metadata = metadata;
      return this;
    }

    /** Set meaningful contact flag. */
    public Builder withMeaningfulContact(boolean isMeaningfulContact) {
      this.isMeaningfulContact = isMeaningfulContact;
      return this;
    }

    /** Set timer reset flag. */
    public Builder withResetsTimer(boolean resetsTimer) {
      this.resetsTimer = resetsTimer;
      return this;
    }

    /** Set progress tracking flag. */
    public Builder withCountsAsProgress(boolean countsAsProgress) {
      this.countsAsProgress = countsAsProgress;
      return this;
    }

    /** Set the summary. */
    public Builder withSummary(String summary) {
      this.summary = summary;
      return this;
    }

    /** Set the outcome (Sprint 2.1.7 Issue #126). */
    public Builder withOutcome(ActivityOutcome outcome) {
      this.outcome = outcome;
      return this;
    }

    /** Set the next action. */
    public Builder withNextAction(String nextAction) {
      this.nextAction = nextAction;
      return this;
    }

    /** Set the next action date. */
    public Builder withNextActionDate(LocalDate nextActionDate) {
      this.nextActionDate = nextActionDate;
      return this;
    }

    /** Set the performed by user. */
    public Builder withPerformedBy(String performedBy) {
      this.performedBy = performedBy;
      return this;
    }

    /** Set the created at timestamp. */
    public Builder withCreatedAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    /**
     * Build the lead activity entity without persisting.
     *
     * <p>Track 2C - Enhanced with RealisticDataGenerator defaults
     *
     * @return The built lead activity entity
     * @throws IllegalStateException if lead is not set
     */
    public LeadActivity build() {
      if (lead == null) {
        throw new IllegalStateException(
            "Lead must be set via forLead() before building LeadActivity");
      }

      // Realistic defaults from RealisticDataGenerator
      if (description == null) {
        description = generator.shortNote();
      }

      LeadActivity activity = new LeadActivity();

      // Required fields
      activity.lead = lead;
      activity.userId = userId;
      activity.activityType = activityType;
      activity.activityDate = activityDate;
      activity.createdAt = createdAt;

      // Optional fields
      activity.description = description;
      activity.metadata = metadata;
      activity.isMeaningfulContact = isMeaningfulContact;
      activity.resetsTimer = resetsTimer;
      activity.countsAsProgress = countsAsProgress;
      activity.summary = summary;

      // Sprint 2.1.7 Issue #126: ActivityOutcome
      activity.outcome = outcome;
      activity.nextAction = nextAction;
      activity.nextActionDate = nextActionDate;
      activity.performedBy = performedBy;

      return activity;
    }

    /**
     * Build a phone call activity (meaningful contact).
     *
     * <p>Convenience method for CALL activity with outcome
     */
    public LeadActivity buildCall() {
      return withActivityType(ActivityType.CALL)
          .withMeaningfulContact(true)
          .withResetsTimer(true)
          .withOutcome(ActivityOutcome.SUCCESSFUL)
          .withDescription("Telefonat mit " + (lead != null ? lead.contactPerson : "Kontakt"))
          .build();
    }

    /**
     * Build an email activity.
     *
     * <p>Convenience method for EMAIL activity with outcome
     */
    public LeadActivity buildEmail() {
      return withActivityType(ActivityType.EMAIL)
          .withOutcome(ActivityOutcome.INFO_SENT)
          .withDescription("E-Mail versendet an " + (lead != null ? lead.email : "Kontakt"))
          .build();
    }

    /**
     * Build a note activity (non-meaningful contact).
     *
     * <p>Convenience method for NOTE activity
     */
    public LeadActivity buildNote() {
      return withActivityType(ActivityType.NOTE)
          .withMeaningfulContact(false)
          .withDescription(generator.shortNote())
          .build();
    }

    /**
     * Build a meeting activity (meaningful contact).
     *
     * <p>Convenience method for MEETING activity with outcome
     */
    public LeadActivity buildMeeting() {
      return withActivityType(ActivityType.MEETING)
          .withMeaningfulContact(true)
          .withResetsTimer(true)
          .withOutcome(ActivityOutcome.SUCCESSFUL)
          .withDescription("Meeting vor Ort")
          .build();
    }

    /**
     * Build a first contact documented activity (meaningful contact, counts as progress).
     *
     * <p>Convenience method for FIRST_CONTACT_DOCUMENTED activity
     */
    public LeadActivity buildFirstContact() {
      return withActivityType(ActivityType.FIRST_CONTACT_DOCUMENTED)
          .withMeaningfulContact(true)
          .withResetsTimer(true)
          .withCountsAsProgress(true)
          .withDescription("Erstkontakt dokumentiert")
          .build();
    }

    /**
     * Build and persist to database.
     *
     * @return The persisted lead activity entity
     */
    public LeadActivity buildAndPersist() {
      LeadActivity activity = build();
      activity.persist();
      return activity;
    }
  }
}

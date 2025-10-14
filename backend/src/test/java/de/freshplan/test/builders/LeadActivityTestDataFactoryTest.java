package de.freshplan.test.builders;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.modules.leads.domain.*;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * LeadActivityTestDataFactoryTest - Unit tests for LeadActivityTestDataFactory
 *
 * <p>Track 2C - Advanced Test Infrastructure
 *
 * <p>Tests: - Activity creation with required Lead - Builder pattern functionality - Convenience
 * methods (buildCall, buildEmail, buildNote, buildMeeting, buildFirstContact) - Realistic
 * description generation from RealisticDataGenerator - Outcome setting (Sprint 2.1.7 Issue #126) -
 * Meaningful contact auto-detection - Seeded builder for deterministic tests
 */
@Tag("unit")
class LeadActivityTestDataFactoryTest {

  private Lead testLead;

  @BeforeEach
  void setUp() {
    // Create a test lead for activity tests
    testLead = LeadTestDataFactory.builder().build();
  }

  @Test
  void testBuilder_withoutLead_shouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> LeadActivityTestDataFactory.builder().build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Lead must be set");
  }

  @Test
  void testBuilder_shouldGenerateRealisticDescription() {
    // When
    LeadActivity activity = LeadActivityTestDataFactory.builder().forLead(testLead).build();

    // Then
    assertThat(activity.description).isNotBlank();
  }

  @Test
  void testBuilder_shouldUseRealisticDefaults() {
    // When
    LeadActivity activity = LeadActivityTestDataFactory.builder().forLead(testLead).build();

    // Then - Required fields
    assertThat(activity.lead).isEqualTo(testLead);
    assertThat(activity.userId).isEqualTo("test-user");
    assertThat(activity.activityType).isEqualTo(ActivityType.NOTE);
    assertThat(activity.activityDate).isNotNull();
    assertThat(activity.createdAt).isNotNull();

    // Optional fields
    assertThat(activity.description).isNotBlank();
    assertThat(activity.metadata).isNotNull();
    assertThat(activity.isMeaningfulContact).isFalse(); // NOTE is not meaningful
    assertThat(activity.resetsTimer).isFalse();
    assertThat(activity.countsAsProgress).isFalse();
  }

  @Test
  void testBuilder_withCustomValues_shouldOverrideDefaults() {
    // When
    LeadActivity activity =
        LeadActivityTestDataFactory.builder()
            .forLead(testLead)
            .withUserId("user-123")
            .withActivityType(ActivityType.CALL)
            .withDescription("Test call description")
            .withMeaningfulContact(true)
            .withOutcome(ActivityOutcome.SUCCESSFUL)
            .build();

    // Then
    assertThat(activity.userId).isEqualTo("user-123");
    assertThat(activity.activityType).isEqualTo(ActivityType.CALL);
    assertThat(activity.description).isEqualTo("Test call description");
    assertThat(activity.isMeaningfulContact).isTrue();
    assertThat(activity.outcome).isEqualTo(ActivityOutcome.SUCCESSFUL);
  }

  @Test
  void testBuilder_withActivityType_shouldAutoSetMeaningfulContact() {
    // When - CALL is meaningful contact
    LeadActivity callActivity =
        LeadActivityTestDataFactory.builder()
            .forLead(testLead)
            .withActivityType(ActivityType.CALL)
            .build();

    // Then
    assertThat(callActivity.activityType).isEqualTo(ActivityType.CALL);
    assertThat(callActivity.isMeaningfulContact).isTrue(); // Auto-set from ActivityType
    assertThat(callActivity.resetsTimer).isTrue(); // Auto-set from ActivityType

    // When - NOTE is not meaningful contact
    LeadActivity noteActivity =
        LeadActivityTestDataFactory.builder()
            .forLead(testLead)
            .withActivityType(ActivityType.NOTE)
            .build();

    // Then
    assertThat(noteActivity.activityType).isEqualTo(ActivityType.NOTE);
    assertThat(noteActivity.isMeaningfulContact).isFalse(); // Auto-set from ActivityType
    assertThat(noteActivity.resetsTimer).isFalse();
  }

  @Test
  void testBuildCall_shouldCreateCallActivity() {
    // When
    LeadActivity activity = LeadActivityTestDataFactory.builder().forLead(testLead).buildCall();

    // Then
    assertThat(activity.activityType).isEqualTo(ActivityType.CALL);
    assertThat(activity.isMeaningfulContact).isTrue();
    assertThat(activity.resetsTimer).isTrue();
    assertThat(activity.outcome).isEqualTo(ActivityOutcome.SUCCESSFUL);
    assertThat(activity.description).contains("Telefonat");
    assertThat(activity.lead).isEqualTo(testLead);
  }

  @Test
  void testBuildEmail_shouldCreateEmailActivity() {
    // When
    LeadActivity activity = LeadActivityTestDataFactory.builder().forLead(testLead).buildEmail();

    // Then
    assertThat(activity.activityType).isEqualTo(ActivityType.EMAIL);
    assertThat(activity.outcome).isEqualTo(ActivityOutcome.INFO_SENT);
    assertThat(activity.description).contains("E-Mail");
    assertThat(activity.lead).isEqualTo(testLead);
  }

  @Test
  void testBuildNote_shouldCreateNoteActivity() {
    // When
    LeadActivity activity = LeadActivityTestDataFactory.builder().forLead(testLead).buildNote();

    // Then
    assertThat(activity.activityType).isEqualTo(ActivityType.NOTE);
    assertThat(activity.isMeaningfulContact).isFalse();
    assertThat(activity.description).isNotBlank(); // Generated by RealisticDataGenerator
    assertThat(activity.lead).isEqualTo(testLead);
  }

  @Test
  void testBuildMeeting_shouldCreateMeetingActivity() {
    // When
    LeadActivity activity = LeadActivityTestDataFactory.builder().forLead(testLead).buildMeeting();

    // Then
    assertThat(activity.activityType).isEqualTo(ActivityType.MEETING);
    assertThat(activity.isMeaningfulContact).isTrue();
    assertThat(activity.resetsTimer).isTrue();
    assertThat(activity.outcome).isEqualTo(ActivityOutcome.SUCCESSFUL);
    assertThat(activity.description).contains("Meeting");
    assertThat(activity.lead).isEqualTo(testLead);
  }

  @Test
  void testBuildFirstContact_shouldCreateFirstContactActivity() {
    // When
    LeadActivity activity =
        LeadActivityTestDataFactory.builder().forLead(testLead).buildFirstContact();

    // Then
    assertThat(activity.activityType).isEqualTo(ActivityType.FIRST_CONTACT_DOCUMENTED);
    assertThat(activity.isMeaningfulContact).isTrue();
    assertThat(activity.resetsTimer).isTrue();
    assertThat(activity.countsAsProgress).isTrue(); // KEY: Counts as progress
    assertThat(activity.description).contains("Erstkontakt");
    assertThat(activity.lead).isEqualTo(testLead);
  }

  @Test
  void testBuilder_withMetadata_shouldSetMetadata() {
    // Given
    JsonObject metadata = new JsonObject();
    metadata.put("key", "value");
    metadata.put("duration", 30);

    // When
    LeadActivity activity =
        LeadActivityTestDataFactory.builder().forLead(testLead).withMetadata(metadata).build();

    // Then
    assertThat(activity.metadata).isNotNull();
    assertThat(activity.metadata.getString("key")).isEqualTo("value");
    assertThat(activity.metadata.getInteger("duration")).isEqualTo(30);
  }

  @Test
  void testBuilder_withSummary_shouldSetSummary() {
    // When
    LeadActivity activity =
        LeadActivityTestDataFactory.builder()
            .forLead(testLead)
            .withSummary("Call successful, follow-up needed")
            .build();

    // Then
    assertThat(activity.summary).isEqualTo("Call successful, follow-up needed");
  }

  @Test
  void testBuilder_withNextAction_shouldSetNextAction() {
    // When
    LeadActivity activity =
        LeadActivityTestDataFactory.builder()
            .forLead(testLead)
            .withNextAction("Send proposal")
            .withNextActionDate(java.time.LocalDate.now().plusDays(7))
            .build();

    // Then
    assertThat(activity.nextAction).isEqualTo("Send proposal");
    assertThat(activity.nextActionDate).isEqualTo(java.time.LocalDate.now().plusDays(7));
  }

  @Test
  void testBuilder_withPerformedBy_shouldSetPerformedBy() {
    // When
    LeadActivity activity =
        LeadActivityTestDataFactory.builder()
            .forLead(testLead)
            .withPerformedBy("sales-rep-123")
            .build();

    // Then
    assertThat(activity.performedBy).isEqualTo("sales-rep-123");
  }

  @Test
  void testBuilder_withOutcome_shouldSetOutcome() {
    // When
    LeadActivity activity =
        LeadActivityTestDataFactory.builder()
            .forLead(testLead)
            .withOutcome(ActivityOutcome.QUALIFIED)
            .build();

    // Then
    assertThat(activity.outcome).isEqualTo(ActivityOutcome.QUALIFIED);
  }

  @Test
  void testBuilder_withCountsAsProgress_shouldSetFlag() {
    // When
    LeadActivity activity =
        LeadActivityTestDataFactory.builder().forLead(testLead).withCountsAsProgress(true).build();

    // Then
    assertThat(activity.countsAsProgress).isTrue();
  }

  @Test
  void testSeededBuilder_shouldBeAvailable() {
    // Given
    long seed = 42L;

    // When
    LeadActivityTestDataFactory.Builder builder = LeadActivityTestDataFactory.builder(seed);
    LeadActivity activity = builder.forLead(testLead).build();

    // Then - Seeded builder works (determinism tested in RealisticDataGeneratorTest)
    assertThat(activity).isNotNull();
    assertThat(activity.description).isNotNull();
  }

  @Test
  void testSeededBuilder_withDifferentSeeds_shouldProduceDifferentResults() {
    // Given
    long seed1 = 42L;
    long seed2 = 99L;

    // When
    LeadActivity activity1 = LeadActivityTestDataFactory.builder(seed1).forLead(testLead).build();
    LeadActivity activity2 = LeadActivityTestDataFactory.builder(seed2).forLead(testLead).build();

    // Then - Different seeds → different descriptions
    assertThat(activity1.description).isNotEqualTo(activity2.description);
  }

  @Test
  void testBuilder_withActivityDate_shouldSetDate() {
    // Given
    java.time.LocalDateTime date = java.time.LocalDateTime.now().minusDays(5);

    // When
    LeadActivity activity =
        LeadActivityTestDataFactory.builder().forLead(testLead).withActivityDate(date).build();

    // Then
    assertThat(activity.activityDate).isEqualTo(date);
  }

  @Test
  void testBuilder_withCreatedAt_shouldSetCreatedAt() {
    // Given
    java.time.LocalDateTime createdAt = java.time.LocalDateTime.now().minusDays(10);

    // When
    LeadActivity activity =
        LeadActivityTestDataFactory.builder().forLead(testLead).withCreatedAt(createdAt).build();

    // Then
    assertThat(activity.createdAt).isEqualTo(createdAt);
  }

  @Test
  void testBuilder_shouldUpdateLeadStatus_whenMeaningfulContact() {
    // When - Meaningful contact activity
    LeadActivity meaningfulActivity =
        LeadActivityTestDataFactory.builder().forLead(testLead).buildCall();

    // Then
    assertThat(meaningfulActivity.shouldUpdateLeadStatus()).isTrue();

    // When - Non-meaningful contact activity
    LeadActivity nonMeaningfulActivity =
        LeadActivityTestDataFactory.builder().forLead(testLead).buildNote();

    // Then
    assertThat(nonMeaningfulActivity.shouldUpdateLeadStatus()).isFalse();
  }

  @Test
  void testBuilder_multipleActivitiesForSameLead_shouldWork() {
    // When - Create multiple activities for same lead
    LeadActivity call = LeadActivityTestDataFactory.builder().forLead(testLead).buildCall();

    LeadActivity email = LeadActivityTestDataFactory.builder().forLead(testLead).buildEmail();

    LeadActivity note = LeadActivityTestDataFactory.builder().forLead(testLead).buildNote();

    // Then - All activities should reference same lead
    assertThat(call.lead).isEqualTo(testLead);
    assertThat(email.lead).isEqualTo(testLead);
    assertThat(note.lead).isEqualTo(testLead);

    // All activities should have different types/descriptions
    assertThat(call.activityType).isEqualTo(ActivityType.CALL);
    assertThat(email.activityType).isEqualTo(ActivityType.EMAIL);
    assertThat(note.activityType).isEqualTo(ActivityType.NOTE);
  }
}

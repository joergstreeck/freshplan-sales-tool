package de.freshplan.modules.leads.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.freshplan.modules.leads.domain.ActivityOutcome;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStage;
import de.freshplan.modules.leads.domain.LeadStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit Tests for LeadProtectionService (Sprint 2.1.5).
 *
 * <p>Tests Progressive Profiling (Stage 0→1→2) and Progress Tracking (60-day deadline).
 *
 * <p>Strategy: Pure Mockito, NO @QuarkusTest, NO DB. Fast, isolated, reliable.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LeadProtectionService Unit Tests - Sprint 2.1.5")
@Tag("unit")
class LeadProtectionServiceTest {

  @Mock private UserLeadSettingsService settingsService;

  @Mock private java.time.Clock clock;

  @InjectMocks private LeadProtectionService protectionService;

  // ========== Sprint 2.1.6: Type-Safe Enum Stage Validation (Issue #125) ==========

  @Nested
  @DisplayName("Type-Safe Enum Stage Transitions (Sprint 2.1.6)")
  class EnumStageTransitionTests {

    @Test
    @DisplayName("Should allow forward transitions (VORMERKUNG → REGISTRIERUNG → QUALIFIZIERT)")
    void shouldAllowForwardTransitions() {
      // VORMERKUNG → REGISTRIERUNG
      assertThat(
              protectionService.canTransitionStage(LeadStage.VORMERKUNG, LeadStage.REGISTRIERUNG))
          .isTrue();

      // REGISTRIERUNG → QUALIFIZIERT
      assertThat(
              protectionService.canTransitionStage(LeadStage.REGISTRIERUNG, LeadStage.QUALIFIZIERT))
          .isTrue();
    }

    @Test
    @DisplayName("Should allow same-stage transitions (idempotent)")
    void shouldAllowSameStageTransitions() {
      assertThat(protectionService.canTransitionStage(LeadStage.VORMERKUNG, LeadStage.VORMERKUNG))
          .isTrue();
      assertThat(
              protectionService.canTransitionStage(
                  LeadStage.REGISTRIERUNG, LeadStage.REGISTRIERUNG))
          .isTrue();
      assertThat(
              protectionService.canTransitionStage(LeadStage.QUALIFIZIERT, LeadStage.QUALIFIZIERT))
          .isTrue();
    }

    @Test
    @DisplayName("Should reject backward transitions (downgrade not allowed)")
    void shouldRejectBackwardTransitions() {
      // REGISTRIERUNG → VORMERKUNG (downgrade)
      assertThat(
              protectionService.canTransitionStage(LeadStage.REGISTRIERUNG, LeadStage.VORMERKUNG))
          .isFalse();

      // QUALIFIZIERT → REGISTRIERUNG (downgrade)
      assertThat(
              protectionService.canTransitionStage(LeadStage.QUALIFIZIERT, LeadStage.REGISTRIERUNG))
          .isFalse();

      // QUALIFIZIERT → VORMERKUNG (downgrade)
      assertThat(protectionService.canTransitionStage(LeadStage.QUALIFIZIERT, LeadStage.VORMERKUNG))
          .isFalse();
    }

    @Test
    @DisplayName("Should reject stage skipping (must go through all stages)")
    void shouldRejectStageSkipping() {
      // VORMERKUNG → QUALIFIZIERT (skip REGISTRIERUNG)
      assertThat(protectionService.canTransitionStage(LeadStage.VORMERKUNG, LeadStage.QUALIFIZIERT))
          .isFalse();
    }

    @Test
    @DisplayName("Should reject null stage values")
    void shouldRejectNullStageValues() {
      assertThat(protectionService.canTransitionStage(null, LeadStage.REGISTRIERUNG)).isFalse();
      assertThat(protectionService.canTransitionStage(LeadStage.VORMERKUNG, null)).isFalse();
      assertThat(protectionService.canTransitionStage(null, null)).isFalse();
    }

    @Test
    @DisplayName("Should enforce sequential progression (VORMERKUNG→REGISTRIERUNG→QUALIFIZIERT)")
    void shouldEnforceSequentialProgression() {
      // VORMERKUNG can go to REGISTRIERUNG, not QUALIFIZIERT
      assertThat(
              protectionService.canTransitionStage(LeadStage.VORMERKUNG, LeadStage.REGISTRIERUNG))
          .isTrue();
      assertThat(protectionService.canTransitionStage(LeadStage.VORMERKUNG, LeadStage.QUALIFIZIERT))
          .isFalse();

      // REGISTRIERUNG can go to QUALIFIZIERT, not back to VORMERKUNG
      assertThat(
              protectionService.canTransitionStage(LeadStage.REGISTRIERUNG, LeadStage.QUALIFIZIERT))
          .isTrue();
      assertThat(
              protectionService.canTransitionStage(LeadStage.REGISTRIERUNG, LeadStage.VORMERKUNG))
          .isFalse();

      // QUALIFIZIERT cannot downgrade
      assertThat(
              protectionService.canTransitionStage(LeadStage.QUALIFIZIERT, LeadStage.REGISTRIERUNG))
          .isFalse();
      assertThat(protectionService.canTransitionStage(LeadStage.QUALIFIZIERT, LeadStage.VORMERKUNG))
          .isFalse();
    }
  }

  // ========== Sprint 2.1.5: Backward Compatibility Tests (Deprecated) ==========

  @Nested
  @DisplayName("Legacy int-based Stage Transitions (Deprecated, Sprint 2.1.5)")
  class LegacyStageTransitionTests {

    @ParameterizedTest
    @CsvSource({
      "0, 1, true", // Vormerkung → Registrierung (valid)
      "1, 2, true", // Registrierung → Qualifiziert (valid)
      "0, 0, true", // Same stage (no-op, valid)
      "1, 1, true", // Same stage (no-op, valid)
      "2, 2, true" // Same stage (no-op, valid)
    })
    @DisplayName("Should allow valid stage transitions (deprecated int API)")
    void shouldAllowValidStageTransitions(int currentStage, int newStage, boolean expected) {
      boolean result = protectionService.canTransitionStage(currentStage, newStage);
      assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
      "0, 2", // Skip stage (0→2 invalid, must go through 1)
      "1, 0", // Downgrade (not allowed)
      "2, 0", // Downgrade (not allowed)
      "2, 1" // Downgrade (not allowed)
    })
    @DisplayName("Should reject invalid stage transitions (deprecated int API)")
    void shouldRejectInvalidStageTransitions(int currentStage, int newStage) {
      boolean result = protectionService.canTransitionStage(currentStage, newStage);
      assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
      "-1, 1", // Invalid current stage
      "0, -1", // Invalid new stage
      "3, 1", // Current stage out of range
      "0, 3" // New stage out of range
    })
    @DisplayName("Should reject out-of-range stages (deprecated int API)")
    void shouldRejectOutOfRangeStages(int currentStage, int newStage) {
      boolean result = protectionService.canTransitionStage(currentStage, newStage);
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should enforce sequential progression (0→1→2) (deprecated int API)")
    void shouldEnforceSequentialProgression() {
      // Stage 0 can only go to 1, not 2
      assertThat(protectionService.canTransitionStage(0, 1)).isTrue();
      assertThat(protectionService.canTransitionStage(0, 2)).isFalse();

      // Stage 1 can only go to 2, not back to 0
      assertThat(protectionService.canTransitionStage(1, 2)).isTrue();
      assertThat(protectionService.canTransitionStage(1, 0)).isFalse();

      // Stage 2 cannot downgrade
      assertThat(protectionService.canTransitionStage(2, 1)).isFalse();
      assertThat(protectionService.canTransitionStage(2, 0)).isFalse();
    }
  }

  // ========== Sprint 2.1.5: Progress Tracking Calculations ==========

  @Nested
  @DisplayName("Protection & Progress Calculations")
  class ProgressCalculationTests {

    @Test
    @DisplayName("Should calculate protection end date (registered_at + 6 months)")
    void shouldCalculateProtectionUntil() {
      // Given
      Lead lead = new Lead();
      lead.registeredAt = LocalDateTime.of(2025, 1, 1, 10, 0);
      lead.protectionMonths = 6;

      // When
      LocalDateTime protectionUntil = protectionService.calculateProtectionUntil(lead);

      // Then
      assertThat(protectionUntil).isEqualTo(LocalDateTime.of(2025, 7, 1, 10, 0));
    }

    @Test
    @DisplayName("Should calculate protection end with custom months")
    void shouldCalculateProtectionUntilWithCustomMonths() {
      // Given
      Lead lead = new Lead();
      lead.registeredAt = LocalDateTime.of(2025, 3, 15, 14, 30);
      lead.protectionMonths = 3;

      // When
      LocalDateTime protectionUntil = protectionService.calculateProtectionUntil(lead);

      // Then
      assertThat(protectionUntil).isEqualTo(LocalDateTime.of(2025, 6, 15, 14, 30));
    }

    @Test
    @DisplayName("Should calculate progress deadline (last_activity + 60 days)")
    void shouldCalculateProgressDeadline() {
      // Given
      LocalDateTime lastActivity = LocalDateTime.of(2025, 1, 1, 12, 0);

      // When
      LocalDateTime progressDeadline = protectionService.calculateProgressDeadline(lastActivity);

      // Then - §3.3: 60 days after last activity
      assertThat(progressDeadline).isEqualTo(LocalDateTime.of(2025, 3, 2, 12, 0));
    }

    @Test
    @DisplayName("Should return null for null lastActivityAt")
    void shouldReturnNullForNullLastActivity() {
      // When
      LocalDateTime progressDeadline = protectionService.calculateProgressDeadline(null);

      // Then
      assertThat(progressDeadline).isNull();
    }

    @Test
    @DisplayName("Should detect progress warning needed (deadline < 7 days)")
    void shouldDetectProgressWarningNeeded() {
      // Given - Warning threshold: 7 days before deadline
      LocalDateTime now = LocalDateTime.of(2025, 10, 13, 12, 0);
      when(clock.instant()).thenReturn(now.toInstant(java.time.ZoneOffset.UTC));
      when(clock.getZone()).thenReturn(java.time.ZoneOffset.UTC);

      Lead lead = new Lead();
      lead.progressDeadline = now.plusDays(5); // 5 days until deadline
      lead.progressWarningSentAt = null; // No warning sent yet

      // When
      boolean needsWarning = protectionService.needsProgressWarning(lead);

      // Then - §3.3: Warning at 53 days (7 days before 60-day deadline)
      assertThat(needsWarning).isTrue();
    }

    @Test
    @DisplayName("Should not send warning if already sent")
    void shouldNotSendWarningIfAlreadySent() {
      // Given - No clock mocking needed (early return path)
      LocalDateTime now = LocalDateTime.of(2025, 10, 13, 12, 0);

      Lead lead = new Lead();
      lead.progressDeadline = now.plusDays(5);
      lead.progressWarningSentAt = now.minusDays(1); // Already sent

      // When
      boolean needsWarning = protectionService.needsProgressWarning(lead);

      // Then
      assertThat(needsWarning).isFalse();
    }

    @Test
    @DisplayName("Should not send warning if deadline far away")
    void shouldNotSendWarningIfDeadlineFarAway() {
      // Given - 10 days until deadline (> 7 days threshold)
      LocalDateTime now = LocalDateTime.of(2025, 10, 13, 12, 0);
      when(clock.instant()).thenReturn(now.toInstant(java.time.ZoneOffset.UTC));
      when(clock.getZone()).thenReturn(java.time.ZoneOffset.UTC);

      Lead lead = new Lead();
      lead.progressDeadline = now.plusDays(10);
      lead.progressWarningSentAt = null;

      // When
      boolean needsWarning = protectionService.needsProgressWarning(lead);

      // Then
      assertThat(needsWarning).isFalse();
    }

    @Test
    @DisplayName("Should not send warning if no deadline set")
    void shouldNotSendWarningIfNoDeadline() {
      // Given
      Lead lead = new Lead();
      lead.progressDeadline = null;

      // When
      boolean needsWarning = protectionService.needsProgressWarning(lead);

      // Then
      assertThat(needsWarning).isFalse();
    }
  }

  // ========== Sprint 2.1.7: ActivityOutcome Enum Tests (Issue #126) ==========

  @Nested
  @DisplayName("ActivityOutcome Enum Business Logic (Sprint 2.1.7)")
  class ActivityOutcomeEnumTests {

    @Test
    @DisplayName("ActivityOutcome.isPositive() should identify positive outcomes")
    void activityOutcomeShouldIdentifyPositiveOutcomes() {
      // Positive outcomes
      assertThat(ActivityOutcome.SUCCESSFUL.isPositive()).isTrue();
      assertThat(ActivityOutcome.QUALIFIED.isPositive()).isTrue();

      // Non-positive outcomes
      assertThat(ActivityOutcome.UNSUCCESSFUL.isPositive()).isFalse();
      assertThat(ActivityOutcome.NO_ANSWER.isPositive()).isFalse();
      assertThat(ActivityOutcome.CALLBACK_REQUESTED.isPositive()).isFalse();
      assertThat(ActivityOutcome.INFO_SENT.isPositive()).isFalse();
      assertThat(ActivityOutcome.DISQUALIFIED.isPositive()).isFalse();
    }

    @Test
    @DisplayName("ActivityOutcome.requiresFollowUp() should identify follow-up needs")
    void activityOutcomeShouldIdentifyFollowUpNeeds() {
      // Requires follow-up
      assertThat(ActivityOutcome.CALLBACK_REQUESTED.requiresFollowUp()).isTrue();
      assertThat(ActivityOutcome.NO_ANSWER.requiresFollowUp()).isTrue();

      // No follow-up needed
      assertThat(ActivityOutcome.SUCCESSFUL.requiresFollowUp()).isFalse();
      assertThat(ActivityOutcome.UNSUCCESSFUL.requiresFollowUp()).isFalse();
      assertThat(ActivityOutcome.INFO_SENT.requiresFollowUp()).isFalse();
      assertThat(ActivityOutcome.QUALIFIED.requiresFollowUp()).isFalse();
      assertThat(ActivityOutcome.DISQUALIFIED.requiresFollowUp()).isFalse();
    }

    @Test
    @DisplayName("ActivityOutcome.isTerminal() should identify terminal outcomes")
    void activityOutcomeShouldIdentifyTerminalOutcomes() {
      // Terminal outcomes
      assertThat(ActivityOutcome.DISQUALIFIED.isTerminal()).isTrue();
      assertThat(ActivityOutcome.UNSUCCESSFUL.isTerminal()).isTrue();

      // Non-terminal outcomes
      assertThat(ActivityOutcome.SUCCESSFUL.isTerminal()).isFalse();
      assertThat(ActivityOutcome.NO_ANSWER.isTerminal()).isFalse();
      assertThat(ActivityOutcome.CALLBACK_REQUESTED.isTerminal()).isFalse();
      assertThat(ActivityOutcome.INFO_SENT.isTerminal()).isFalse();
      assertThat(ActivityOutcome.QUALIFIED.isTerminal()).isFalse();
    }
  }

  // ========== Existing Protection Tests (Regression) ==========

  @Nested
  @DisplayName("Existing Protection Logic (Regression)")
  class ExistingProtectionTests {

    @Test
    @DisplayName("Should calculate remaining protection days correctly")
    void shouldCalculateRemainingProtectionDays() {
      // Given
      LocalDateTime now = LocalDateTime.of(2025, 10, 13, 12, 0);
      when(clock.instant()).thenReturn(now.toInstant(java.time.ZoneOffset.UTC));
      when(clock.getZone()).thenReturn(java.time.ZoneOffset.UTC);

      Lead lead = new Lead();
      lead.registeredAt = now;
      lead.protectionMonths = 6;
      lead.status = LeadStatus.ACTIVE;
      lead.clockStoppedAt = null;

      // When
      int remainingDays = protectionService.getRemainingProtectionDays(lead);

      // Then - ~180 days remaining (6 months = 180-184 days depending on month lengths)
      assertThat(remainingDays).isGreaterThan(165).isLessThan(195);
    }

    @Test
    @DisplayName("Should return infinite protection when clock stopped")
    void shouldReturnInfiniteProtectionWhenClockStopped() {
      // Given - No clock mocking needed (early return path: clockStoppedAt != null)
      LocalDateTime now = LocalDateTime.of(2025, 10, 13, 12, 0);

      Lead lead = new Lead();
      lead.registeredAt = now.minusMonths(7); // Expired
      lead.protectionMonths = 6;
      lead.clockStoppedAt = now; // Clock stopped

      // When
      int remainingDays = protectionService.getRemainingProtectionDays(lead);

      // Then
      assertThat(remainingDays).isEqualTo(Integer.MAX_VALUE);
    }
  }
}

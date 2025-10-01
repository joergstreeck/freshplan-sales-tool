package de.freshplan.modules.leads.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
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

  @InjectMocks private LeadProtectionService protectionService;

  // ========== Sprint 2.1.5: Progressive Profiling Stage Validation ==========

  @Nested
  @DisplayName("Progressive Profiling Stage Transitions")
  class StageTransitionTests {

    @ParameterizedTest
    @CsvSource({
      "0, 1, true",  // Vormerkung → Registrierung (valid)
      "1, 2, true",  // Registrierung → Qualifiziert (valid)
      "0, 0, true",  // Same stage (no-op, valid)
      "1, 1, true",  // Same stage (no-op, valid)
      "2, 2, true"   // Same stage (no-op, valid)
    })
    @DisplayName("Should allow valid stage transitions")
    void shouldAllowValidStageTransitions(int currentStage, int newStage, boolean expected) {
      boolean result = protectionService.canTransitionStage(currentStage, newStage);
      assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
      "0, 2",  // Skip stage (0→2 invalid, must go through 1)
      "1, 0",  // Downgrade (not allowed)
      "2, 0",  // Downgrade (not allowed)
      "2, 1"   // Downgrade (not allowed)
    })
    @DisplayName("Should reject invalid stage transitions")
    void shouldRejectInvalidStageTransitions(int currentStage, int newStage) {
      boolean result = protectionService.canTransitionStage(currentStage, newStage);
      assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
      "-1, 1",  // Invalid current stage
      "0, -1",  // Invalid new stage
      "3, 1",   // Current stage out of range
      "0, 3"    // New stage out of range
    })
    @DisplayName("Should reject out-of-range stages")
    void shouldRejectOutOfRangeStages(int currentStage, int newStage) {
      boolean result = protectionService.canTransitionStage(currentStage, newStage);
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should enforce sequential progression (0→1→2)")
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
      Lead lead = new Lead();
      lead.progressDeadline = LocalDateTime.now().plusDays(5); // 5 days until deadline
      lead.progressWarningSentAt = null; // No warning sent yet

      // When
      boolean needsWarning = protectionService.needsProgressWarning(lead);

      // Then - §3.3: Warning at 53 days (7 days before 60-day deadline)
      assertThat(needsWarning).isTrue();
    }

    @Test
    @DisplayName("Should not send warning if already sent")
    void shouldNotSendWarningIfAlreadySent() {
      // Given
      Lead lead = new Lead();
      lead.progressDeadline = LocalDateTime.now().plusDays(5);
      lead.progressWarningSentAt = LocalDateTime.now().minusDays(1); // Already sent

      // When
      boolean needsWarning = protectionService.needsProgressWarning(lead);

      // Then
      assertThat(needsWarning).isFalse();
    }

    @Test
    @DisplayName("Should not send warning if deadline far away")
    void shouldNotSendWarningIfDeadlineFarAway() {
      // Given - 10 days until deadline (> 7 days threshold)
      Lead lead = new Lead();
      lead.progressDeadline = LocalDateTime.now().plusDays(10);
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

  // ========== Existing Protection Tests (Regression) ==========

  @Nested
  @DisplayName("Existing Protection Logic (Regression)")
  class ExistingProtectionTests {

    @Test
    @DisplayName("Should calculate remaining protection days correctly")
    void shouldCalculateRemainingProtectionDays() {
      // Given
      Lead lead = new Lead();
      lead.registeredAt = LocalDateTime.now();
      lead.protectionMonths = 6;
      lead.status = LeadStatus.ACTIVE;
      lead.clockStoppedAt = null;

      // When
      int remainingDays = protectionService.getRemainingProtectionDays(lead);

      // Then - ~180 days remaining (6 months)
      assertThat(remainingDays).isGreaterThan(170).isLessThan(190);
    }

    @Test
    @DisplayName("Should return infinite protection when clock stopped")
    void shouldReturnInfiniteProtectionWhenClockStopped() {
      // Given
      Lead lead = new Lead();
      lead.registeredAt = LocalDateTime.now().minusMonths(7); // Expired
      lead.protectionMonths = 6;
      lead.clockStoppedAt = LocalDateTime.now(); // Clock stopped

      // When
      int remainingDays = protectionService.getRemainingProtectionDays(lead);

      // Then
      assertThat(remainingDays).isEqualTo(Integer.MAX_VALUE);
    }
  }
}

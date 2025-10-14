package de.freshplan.modules.leads.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit Tests for ActivityOutcome Enum (Sprint 2.1.7 - Issue #126).
 *
 * <p>Tests business logic methods for activity outcome classification.
 *
 * <p>Strategy: Lightweight enum tests, NO @QuarkusTest needed.
 */
@DisplayName("ActivityOutcome Enum Unit Tests - Sprint 2.1.7")
@Tag("unit")
class ActivityOutcomeTest {

  @Nested
  @DisplayName("Positive Outcome Detection")
  class PositiveOutcomeTests {

    @Test
    @DisplayName("SUCCESSFUL should be positive outcome")
    void successfulShouldBePositive() {
      assertThat(ActivityOutcome.SUCCESSFUL.isPositive()).isTrue();
    }

    @Test
    @DisplayName("QUALIFIED should be positive outcome")
    void qualifiedShouldBePositive() {
      assertThat(ActivityOutcome.QUALIFIED.isPositive()).isTrue();
    }

    @Test
    @DisplayName("All other outcomes should NOT be positive")
    void othersShouldNotBePositive() {
      assertThat(ActivityOutcome.UNSUCCESSFUL.isPositive()).isFalse();
      assertThat(ActivityOutcome.NO_ANSWER.isPositive()).isFalse();
      assertThat(ActivityOutcome.CALLBACK_REQUESTED.isPositive()).isFalse();
      assertThat(ActivityOutcome.INFO_SENT.isPositive()).isFalse();
      assertThat(ActivityOutcome.DISQUALIFIED.isPositive()).isFalse();
    }
  }

  @Nested
  @DisplayName("Follow-Up Detection")
  class FollowUpTests {

    @Test
    @DisplayName("CALLBACK_REQUESTED should require follow-up")
    void callbackRequestedShouldRequireFollowUp() {
      assertThat(ActivityOutcome.CALLBACK_REQUESTED.requiresFollowUp()).isTrue();
    }

    @Test
    @DisplayName("NO_ANSWER should require follow-up")
    void noAnswerShouldRequireFollowUp() {
      assertThat(ActivityOutcome.NO_ANSWER.requiresFollowUp()).isTrue();
    }

    @Test
    @DisplayName("All other outcomes should NOT require follow-up")
    void othersShouldNotRequireFollowUp() {
      assertThat(ActivityOutcome.SUCCESSFUL.requiresFollowUp()).isFalse();
      assertThat(ActivityOutcome.UNSUCCESSFUL.requiresFollowUp()).isFalse();
      assertThat(ActivityOutcome.INFO_SENT.requiresFollowUp()).isFalse();
      assertThat(ActivityOutcome.QUALIFIED.requiresFollowUp()).isFalse();
      assertThat(ActivityOutcome.DISQUALIFIED.requiresFollowUp()).isFalse();
    }
  }

  @Nested
  @DisplayName("Terminal Outcome Detection")
  class TerminalOutcomeTests {

    @Test
    @DisplayName("DISQUALIFIED should be terminal outcome")
    void disqualifiedShouldBeTerminal() {
      assertThat(ActivityOutcome.DISQUALIFIED.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("UNSUCCESSFUL should be terminal outcome")
    void unsuccessfulShouldBeTerminal() {
      assertThat(ActivityOutcome.UNSUCCESSFUL.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("All other outcomes should NOT be terminal")
    void othersShouldNotBeTerminal() {
      assertThat(ActivityOutcome.SUCCESSFUL.isTerminal()).isFalse();
      assertThat(ActivityOutcome.NO_ANSWER.isTerminal()).isFalse();
      assertThat(ActivityOutcome.CALLBACK_REQUESTED.isTerminal()).isFalse();
      assertThat(ActivityOutcome.INFO_SENT.isTerminal()).isFalse();
      assertThat(ActivityOutcome.QUALIFIED.isTerminal()).isFalse();
    }
  }

  @Nested
  @DisplayName("Display Names")
  class DisplayNameTests {

    @Test
    @DisplayName("Should return German display names")
    void shouldReturnGermanDisplayNames() {
      assertThat(ActivityOutcome.SUCCESSFUL.getDisplayName()).isEqualTo("Erfolgreich");
      assertThat(ActivityOutcome.UNSUCCESSFUL.getDisplayName()).isEqualTo("Nicht erfolgreich");
      assertThat(ActivityOutcome.NO_ANSWER.getDisplayName()).isEqualTo("Keine Antwort");
      assertThat(ActivityOutcome.CALLBACK_REQUESTED.getDisplayName())
          .isEqualTo("Rückruf gewünscht");
      assertThat(ActivityOutcome.INFO_SENT.getDisplayName()).isEqualTo("Info versendet");
      assertThat(ActivityOutcome.QUALIFIED.getDisplayName()).isEqualTo("Qualifiziert");
      assertThat(ActivityOutcome.DISQUALIFIED.getDisplayName()).isEqualTo("Disqualifiziert");
    }

    @Test
    @DisplayName("Should return English descriptions")
    void shouldReturnEnglishDescriptions() {
      assertThat(ActivityOutcome.SUCCESSFUL.getDescription())
          .isEqualTo("Activity achieved its goal");
      assertThat(ActivityOutcome.UNSUCCESSFUL.getDescription())
          .isEqualTo("Activity did not achieve its goal");
      assertThat(ActivityOutcome.NO_ANSWER.getDescription()).isEqualTo("Contact attempt failed");
      assertThat(ActivityOutcome.CALLBACK_REQUESTED.getDescription()).isEqualTo("Follow-up needed");
      assertThat(ActivityOutcome.INFO_SENT.getDescription())
          .isEqualTo("Information materials provided");
      assertThat(ActivityOutcome.QUALIFIED.getDescription())
          .isEqualTo("Lead meets criteria for next stage");
      assertThat(ActivityOutcome.DISQUALIFIED.getDescription())
          .isEqualTo("Lead does not meet criteria");
    }
  }

  @Nested
  @DisplayName("Enum Completeness")
  class CompletenessTests {

    @Test
    @DisplayName("Should have exactly 7 enum values")
    void shouldHaveSevenValues() {
      assertThat(ActivityOutcome.values()).hasSize(7);
    }

    @Test
    @DisplayName("Should be able to convert from string name")
    void shouldConvertFromStringName() {
      assertThat(ActivityOutcome.valueOf("SUCCESSFUL")).isEqualTo(ActivityOutcome.SUCCESSFUL);
      assertThat(ActivityOutcome.valueOf("QUALIFIED")).isEqualTo(ActivityOutcome.QUALIFIED);
      assertThat(ActivityOutcome.valueOf("DISQUALIFIED")).isEqualTo(ActivityOutcome.DISQUALIFIED);
    }
  }
}

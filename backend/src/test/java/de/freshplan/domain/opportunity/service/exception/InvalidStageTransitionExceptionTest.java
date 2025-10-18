package de.freshplan.domain.opportunity.service.exception;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.opportunity.entity.OpportunityStage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit Tests für InvalidStageTransitionException.
 *
 * <p>Sprint 2.1.4: Neu erstellt als Plain JUnit Test.
 *
 * <p>Testet Exception mit Stage-Information.
 *
 * @see TEST_DEBUGGING_GUIDE.md
 */
@Tag("unit")
@DisplayName("InvalidStageTransitionException Unit Tests")
class InvalidStageTransitionExceptionTest {

  @Test
  @DisplayName("Should create exception with from/to stages")
  void constructor_withStages_shouldSetFieldsAndMessage() {
    // When
    InvalidStageTransitionException exception =
        new InvalidStageTransitionException(OpportunityStage.NEW_LEAD, OpportunityStage.CLOSED_WON);

    // Then
    assertThat(exception.getFromStage()).isEqualTo(OpportunityStage.NEW_LEAD);
    assertThat(exception.getToStage()).isEqualTo(OpportunityStage.CLOSED_WON);
    assertThat(exception.getMessage())
        .contains("Invalid stage transition")
        .contains("NEW_LEAD")
        .contains("CLOSED_WON");
  }

  @Test
  @DisplayName("Should create exception with custom message")
  void constructor_withMessage_shouldSetMessage() {
    // When
    InvalidStageTransitionException exception =
        new InvalidStageTransitionException("Custom error message");

    // Then
    assertThat(exception.getMessage()).isEqualTo("Custom error message");
    assertThat(exception.getFromStage()).isNull();
    assertThat(exception.getToStage()).isNull();
  }

  @Test
  @DisplayName("Should be a RuntimeException")
  void exception_shouldBeRuntimeException() {
    // When
    InvalidStageTransitionException exception =
        new InvalidStageTransitionException(OpportunityStage.PROPOSAL, OpportunityStage.NEW_LEAD);

    // Then
    assertThat(exception).isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("Should format message correctly for all stage combinations")
  void constructor_shouldFormatMessageForAllStageCombinations() {
    // When
    InvalidStageTransitionException exception =
        new InvalidStageTransitionException(
            OpportunityStage.NEGOTIATION, OpportunityStage.QUALIFICATION);

    // Then
    assertThat(exception.getMessage())
        .isEqualTo("Invalid stage transition from NEGOTIATION to QUALIFICATION");
  }

  @Test
  @DisplayName("Should handle null stages in custom message constructor")
  void constructor_withCustomMessage_shouldHaveNullStages() {
    // When
    InvalidStageTransitionException exception = new InvalidStageTransitionException("Test message");

    // Then
    assertThat(exception.getFromStage()).isNull();
    assertThat(exception.getToStage()).isNull();
    assertThat(exception.getMessage()).isEqualTo("Test message");
  }

  @Test
  @DisplayName("Should preserve stage information for getter access")
  void getters_shouldReturnCorrectStages() {
    // Given
    OpportunityStage from = OpportunityStage.QUALIFICATION;
    OpportunityStage to = OpportunityStage.NEEDS_ANALYSIS;

    // When
    InvalidStageTransitionException exception = new InvalidStageTransitionException(from, to);

    // Then
    assertThat(exception.getFromStage()).isSameAs(from);
    assertThat(exception.getToStage()).isSameAs(to);
  }

  @Test
  @DisplayName("Should handle same stage transition")
  void constructor_withSameStage_shouldCreateException() {
    // When
    InvalidStageTransitionException exception =
        new InvalidStageTransitionException(OpportunityStage.PROPOSAL, OpportunityStage.PROPOSAL);

    // Then
    assertThat(exception.getMessage()).contains("from PROPOSAL to PROPOSAL");
    assertThat(exception.getFromStage()).isEqualTo(OpportunityStage.PROPOSAL);
    assertThat(exception.getToStage()).isEqualTo(OpportunityStage.PROPOSAL);
  }

  @Test
  @DisplayName("Should include both stages in toString-like message")
  void message_shouldIncludeBothStages() {
    // When
    InvalidStageTransitionException exception =
        new InvalidStageTransitionException(
            OpportunityStage.CLOSED_LOST, OpportunityStage.NEGOTIATION);

    // Then
    String message = exception.getMessage();
    assertThat(message).contains("CLOSED_LOST");
    assertThat(message).contains("NEGOTIATION");
    assertThat(message).contains("from");
    assertThat(message).contains("to");
  }
}

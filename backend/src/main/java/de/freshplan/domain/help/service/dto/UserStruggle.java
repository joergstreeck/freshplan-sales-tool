package de.freshplan.domain.help.service.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** DTO für User Struggle Detection */
public record UserStruggle(
    String userId,
    String feature,
    StruggleType type,
    int severity,
    boolean detected,
    LocalDateTime detectedAt,
    Map<String, Object> context,
    List<Suggestion> suggestions) {

  public enum StruggleType {
    REPEATED_FAILED_ATTEMPTS,
    RAPID_NAVIGATION_CHANGES,
    LONG_IDLE_AFTER_START,
    ABANDONED_WORKFLOW_PATTERN,
    COMPLEX_FORM_STRUGGLE
  }

  public record Suggestion(String type, String label, int priority) {}

  public static UserStruggle noStruggle(String userId, String feature) {
    return new UserStruggle(userId, feature, null, 0, false, null, Map.of(), List.of());
  }

  public boolean isDetected() {
    return detected;
  }

  public StruggleType getType() {
    return type;
  }

  public int getSeverity() {
    return severity;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String userId;
    private String feature;
    private StruggleType type;
    private int severity = 0;
    private boolean detected = false;
    private LocalDateTime detectedAt;
    private Map<String, Object> context = Map.of();
    private List<Suggestion> suggestions = List.of();

    public Builder userId(String userId) {
      this.userId = userId;
      return this;
    }

    public Builder feature(String feature) {
      this.feature = feature;
      return this;
    }

    public Builder type(StruggleType type) {
      this.type = type;
      return this;
    }

    public Builder severity(int severity) {
      this.severity = severity;
      return this;
    }

    public Builder detected(boolean detected) {
      this.detected = detected;
      return this;
    }

    public Builder detectedAt(LocalDateTime detectedAt) {
      this.detectedAt = detectedAt;
      return this;
    }

    public Builder context(Map<String, Object> context) {
      this.context = context;
      return this;
    }

    public Builder suggestions(List<Suggestion> suggestions) {
      this.suggestions = suggestions;
      return this;
    }

    public UserStruggle build() {
      return new UserStruggle(
          userId, feature, type, severity, detected, detectedAt, context, suggestions);
    }
  }
}

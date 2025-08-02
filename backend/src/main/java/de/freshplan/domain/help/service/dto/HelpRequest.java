package de.freshplan.domain.help.service.dto;

import de.freshplan.domain.help.entity.HelpType;
import java.util.List;
import java.util.Map;

/** Request DTO für Help Content Anfragen */
public record HelpRequest(
    String userId,
    String feature,
    String userLevel,
    List<String> userRoles,
    HelpType preferredType,
    Map<String, Object> context,
    boolean isFirstTime,
    String sessionId) {

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String userId;
    private String feature;
    private String userLevel = "BEGINNER";
    private List<String> userRoles = List.of();
    private HelpType preferredType;
    private Map<String, Object> context = Map.of();
    private boolean isFirstTime = false;
    private String sessionId;

    public Builder userId(String userId) {
      this.userId = userId;
      return this;
    }

    public Builder feature(String feature) {
      this.feature = feature;
      return this;
    }

    public Builder userLevel(String userLevel) {
      this.userLevel = userLevel;
      return this;
    }

    public Builder userRoles(List<String> userRoles) {
      this.userRoles = userRoles;
      return this;
    }

    public Builder preferredType(HelpType preferredType) {
      this.preferredType = preferredType;
      return this;
    }

    public Builder context(Map<String, Object> context) {
      this.context = context;
      return this;
    }

    public Builder isFirstTime(boolean isFirstTime) {
      this.isFirstTime = isFirstTime;
      return this;
    }

    public Builder sessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
    }

    public HelpRequest build() {
      return new HelpRequest(
          userId, feature, userLevel, userRoles, preferredType, context, isFirstTime, sessionId);
    }
  }
}

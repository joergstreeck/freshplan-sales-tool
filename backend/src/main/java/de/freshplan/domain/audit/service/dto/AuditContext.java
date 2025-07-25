package de.freshplan.domain.audit.service.dto;

import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.entity.AuditSource;
import java.util.UUID;

/**
 * Context object for audit logging with all relevant information
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class AuditContext {

  // Required fields
  private final AuditEventType eventType;
  private final String entityType;
  private final UUID entityId;

  // Change details
  private final Object oldValue;
  private final Object newValue;
  private final String changeReason;
  private final String userComment;

  // User information (auto-populated if null)
  private final UUID userId;
  private final String userName;
  private final String userRole;

  // Context information
  private final String ipAddress;
  private final String userAgent;
  private final UUID sessionId;

  // Source information
  private final AuditSource source;
  private final String apiEndpoint;
  private final UUID requestId;

  // Additional metadata
  private final String correlationId;
  private final String externalReference;

  private AuditContext(Builder builder) {
    this.eventType = builder.eventType;
    this.entityType = builder.entityType;
    this.entityId = builder.entityId;
    this.oldValue = builder.oldValue;
    this.newValue = builder.newValue;
    this.changeReason = builder.changeReason;
    this.userComment = builder.userComment;
    this.userId = builder.userId;
    this.userName = builder.userName;
    this.userRole = builder.userRole;
    this.ipAddress = builder.ipAddress;
    this.userAgent = builder.userAgent;
    this.sessionId = builder.sessionId;
    this.source = builder.source;
    this.apiEndpoint = builder.apiEndpoint;
    this.requestId = builder.requestId;
    this.correlationId = builder.correlationId;
    this.externalReference = builder.externalReference;
  }

  // Getters
  public AuditEventType getEventType() {
    return eventType;
  }

  public String getEntityType() {
    return entityType;
  }

  public UUID getEntityId() {
    return entityId;
  }

  public Object getOldValue() {
    return oldValue;
  }

  public Object getNewValue() {
    return newValue;
  }

  public String getChangeReason() {
    return changeReason;
  }

  public String getUserComment() {
    return userComment;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }

  public String getUserRole() {
    return userRole;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public UUID getSessionId() {
    return sessionId;
  }

  public AuditSource getSource() {
    return source;
  }

  public String getApiEndpoint() {
    return apiEndpoint;
  }

  public UUID getRequestId() {
    return requestId;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public String getExternalReference() {
    return externalReference;
  }

  /** Create a minimal audit context */
  public static AuditContext of(AuditEventType eventType, String entityType, UUID entityId) {
    return AuditContext.builder()
        .eventType(eventType)
        .entityType(entityType)
        .entityId(entityId)
        .build();
  }

  /** Create audit context with old and new values */
  public static AuditContext ofChange(
      AuditEventType eventType,
      String entityType,
      UUID entityId,
      Object oldValue,
      Object newValue) {
    return AuditContext.builder()
        .eventType(eventType)
        .entityType(entityType)
        .entityId(entityId)
        .oldValue(oldValue)
        .newValue(newValue)
        .build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return new Builder()
        .eventType(this.eventType)
        .entityType(this.entityType)
        .entityId(this.entityId)
        .oldValue(this.oldValue)
        .newValue(this.newValue)
        .changeReason(this.changeReason)
        .userComment(this.userComment)
        .userId(this.userId)
        .userName(this.userName)
        .userRole(this.userRole)
        .ipAddress(this.ipAddress)
        .userAgent(this.userAgent)
        .sessionId(this.sessionId)
        .source(this.source)
        .apiEndpoint(this.apiEndpoint)
        .requestId(this.requestId)
        .correlationId(this.correlationId)
        .externalReference(this.externalReference);
  }

  public static class Builder {
    private AuditEventType eventType;
    private String entityType;
    private UUID entityId;
    private Object oldValue;
    private Object newValue;
    private String changeReason;
    private String userComment;
    private UUID userId;
    private String userName;
    private String userRole;
    private String ipAddress;
    private String userAgent;
    private UUID sessionId;
    private AuditSource source;
    private String apiEndpoint;
    private UUID requestId;
    private String correlationId;
    private String externalReference;

    public Builder eventType(AuditEventType eventType) {
      this.eventType = eventType;
      return this;
    }

    public Builder entityType(String entityType) {
      this.entityType = entityType;
      return this;
    }

    public Builder entityId(UUID entityId) {
      this.entityId = entityId;
      return this;
    }

    public Builder oldValue(Object oldValue) {
      this.oldValue = oldValue;
      return this;
    }

    public Builder newValue(Object newValue) {
      this.newValue = newValue;
      return this;
    }

    public Builder changeReason(String changeReason) {
      this.changeReason = changeReason;
      return this;
    }

    public Builder userComment(String userComment) {
      this.userComment = userComment;
      return this;
    }

    public Builder userId(UUID userId) {
      this.userId = userId;
      return this;
    }

    public Builder userName(String userName) {
      this.userName = userName;
      return this;
    }

    public Builder userRole(String userRole) {
      this.userRole = userRole;
      return this;
    }

    public Builder ipAddress(String ipAddress) {
      this.ipAddress = ipAddress;
      return this;
    }

    public Builder userAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }

    public Builder sessionId(UUID sessionId) {
      this.sessionId = sessionId;
      return this;
    }

    public Builder source(AuditSource source) {
      this.source = source;
      return this;
    }

    public Builder apiEndpoint(String apiEndpoint) {
      this.apiEndpoint = apiEndpoint;
      return this;
    }

    public Builder requestId(UUID requestId) {
      this.requestId = requestId;
      return this;
    }

    public Builder correlationId(String correlationId) {
      this.correlationId = correlationId;
      return this;
    }

    public Builder externalReference(String externalReference) {
      this.externalReference = externalReference;
      return this;
    }

    public AuditContext build() {
      return new AuditContext(this);
    }
  }
}

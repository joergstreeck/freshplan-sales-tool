package de.freshplan.domain.export.service.dto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for export requests
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class ExportRequest {
  private String entityType;
  private UUID entityId;
  private LocalDateTime dateFrom;
  private LocalDateTime dateTo;
  private String userId;
  private String eventType;
  private boolean includeDetails;
  private boolean includeStats;
  private boolean includeContacts;
  private String groupBy;
  private String format;
  private String businessType;
  private List<String> status;
  private int page;
  private int size;

  // Builder pattern for fluent API
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final ExportRequest request = new ExportRequest();

    public Builder entityType(String entityType) {
      request.entityType = entityType;
      return this;
    }

    public Builder entityId(UUID entityId) {
      request.entityId = entityId;
      return this;
    }

    public Builder dateFrom(LocalDateTime dateFrom) {
      request.dateFrom = dateFrom;
      return this;
    }

    public Builder dateTo(LocalDateTime dateTo) {
      request.dateTo = dateTo;
      return this;
    }

    public Builder userId(String userId) {
      request.userId = userId;
      return this;
    }

    public Builder eventType(String eventType) {
      request.eventType = eventType;
      return this;
    }

    public Builder includeDetails(boolean includeDetails) {
      request.includeDetails = includeDetails;
      return this;
    }

    public Builder includeStats(boolean includeStats) {
      request.includeStats = includeStats;
      return this;
    }

    public Builder includeContacts(boolean includeContacts) {
      request.includeContacts = includeContacts;
      return this;
    }

    public Builder groupBy(String groupBy) {
      request.groupBy = groupBy;
      return this;
    }

    public Builder format(String format) {
      request.format = format;
      return this;
    }

    public Builder businessType(String businessType) {
      request.businessType = businessType;
      return this;
    }

    public Builder status(List<String> status) {
      request.status = status;
      return this;
    }

    public Builder page(int page) {
      request.page = page;
      return this;
    }

    public Builder size(int size) {
      request.size = size;
      return this;
    }

    public ExportRequest build() {
      return request;
    }
  }

  // Getters
  public String getEntityType() {
    return entityType;
  }

  public UUID getEntityId() {
    return entityId;
  }

  public LocalDateTime getDateFrom() {
    return dateFrom;
  }

  public LocalDateTime getDateTo() {
    return dateTo;
  }

  public String getUserId() {
    return userId;
  }

  public String getEventType() {
    return eventType;
  }

  public boolean isIncludeDetails() {
    return includeDetails;
  }

  public boolean isIncludeStats() {
    return includeStats;
  }

  public boolean isIncludeContacts() {
    return includeContacts;
  }

  public String getGroupBy() {
    return groupBy;
  }

  public String getFormat() {
    return format;
  }

  public String getBusinessType() {
    return businessType;
  }

  public List<String> getStatus() {
    return status;
  }

  public int getPage() {
    return page;
  }

  public int getSize() {
    return size;
  }

  // Setters
  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public void setEntityId(UUID entityId) {
    this.entityId = entityId;
  }

  public void setDateFrom(LocalDateTime dateFrom) {
    this.dateFrom = dateFrom;
  }

  public void setDateTo(LocalDateTime dateTo) {
    this.dateTo = dateTo;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public void setIncludeDetails(boolean includeDetails) {
    this.includeDetails = includeDetails;
  }

  public void setIncludeStats(boolean includeStats) {
    this.includeStats = includeStats;
  }

  public void setIncludeContacts(boolean includeContacts) {
    this.includeContacts = includeContacts;
  }

  public void setGroupBy(String groupBy) {
    this.groupBy = groupBy;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public void setBusinessType(String businessType) {
    this.businessType = businessType;
  }

  public void setStatus(List<String> status) {
    this.status = status;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    if (entityType != null) map.put("entityType", entityType);
    if (entityId != null) map.put("entityId", entityId);
    if (dateFrom != null) map.put("dateFrom", dateFrom);
    if (dateTo != null) map.put("dateTo", dateTo);
    if (userId != null) map.put("userId", userId);
    if (eventType != null) map.put("eventType", eventType);
    if (businessType != null) map.put("businessType", businessType);
    if (status != null) map.put("status", status);
    if (format != null) map.put("format", format);
    if (groupBy != null) map.put("groupBy", groupBy);
    map.put("includeDetails", includeDetails);
    map.put("includeStats", includeStats);
    map.put("includeContacts", includeContacts);
    map.put("page", page);
    map.put("size", size);
    return map;
  }
}

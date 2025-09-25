package de.freshplan.modules.leads.api;

import de.freshplan.modules.leads.domain.ActivityType;
import de.freshplan.modules.leads.domain.LeadActivity;
import java.time.LocalDateTime;

/**
 * DTO for LeadActivity to avoid lazy loading issues. Sprint 2.1: Safe serialization without
 * lazy-loaded entities.
 */
public class LeadActivityDTO {
  public Long id;
  public Long leadId;
  public String userId;
  public ActivityType activityType;
  public LocalDateTime activityDate;
  public String description;
  public boolean isMeaningfulContact;
  public boolean resetsTimer;
  public LocalDateTime createdAt;

  public static LeadActivityDTO from(LeadActivity activity) {
    LeadActivityDTO dto = new LeadActivityDTO();
    dto.id = activity.id;
    dto.leadId = activity.lead.id;
    dto.userId = activity.userId;
    dto.activityType = activity.activityType;
    dto.activityDate = activity.activityDate;
    dto.description = activity.description;
    dto.isMeaningfulContact = activity.isMeaningfulContact;
    dto.resetsTimer = activity.resetsTimer;
    dto.createdAt = activity.createdAt;
    return dto;
  }
}

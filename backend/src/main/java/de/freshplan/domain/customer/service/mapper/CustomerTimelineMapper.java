package de.freshplan.domain.customer.service.mapper;

import de.freshplan.domain.customer.entity.CustomerTimelineEvent;
import de.freshplan.domain.customer.service.dto.timeline.TimelineEventResponse;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between CustomerTimelineEvent entities and DTOs.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class CustomerTimelineMapper {

  /** Converts a CustomerTimelineEvent entity to a TimelineEventResponse DTO. */
  public TimelineEventResponse toResponse(CustomerTimelineEvent event) {
    if (event == null) {
      return null;
    }

    TimelineEventResponse response = new TimelineEventResponse();

    // Basic fields
    response.setId(event.getId());
    response.setEventType(event.getEventType());
    response.setEventDate(event.getEventDate());
    response.setTitle(event.getTitle());
    response.setDescription(event.getDescription());
    response.setCategory(event.getCategory());
    response.setImportance(event.getImportance());
    response.setPerformedBy(event.getPerformedBy());
    response.setPerformedByRole(event.getPerformedByRole());

    // Communication fields
    response.setCommunicationChannel(event.getCommunicationChannel());
    response.setCommunicationDirection(event.getCommunicationDirection());
    response.setCommunicationDuration(event.getCommunicationDuration());

    // Follow-up fields
    response.setRequiresFollowUp(event.getRequiresFollowUp());
    response.setFollowUpDate(event.getFollowUpDate());
    response.setFollowUpNotes(event.getFollowUpNotes());
    response.setFollowUpCompleted(event.getFollowUpCompleted());

    // Related entities
    response.setRelatedContactId(event.getRelatedContactId());
    response.setRelatedLocationId(event.getRelatedLocationId());

    // Business impact
    response.setBusinessImpact(event.getBusinessImpact());
    response.setRevenueImpact(event.getRevenueImpact());

    // Tags and labels
    if (event.getTags() != null && !event.getTags().isBlank()) {
      response.setTags(Arrays.asList(event.getTags().split(",")));
    } else {
      response.setTags(Collections.emptyList());
    }

    if (event.getLabels() != null && !event.getLabels().isBlank()) {
      response.setLabels(Arrays.asList(event.getLabels().split(",")));
    } else {
      response.setLabels(Collections.emptyList());
    }

    // Metadata
    response.setIsDeleted(event.getIsDeleted());
    response.setCreatedAt(event.getCreatedAt());
    response.setUpdatedAt(event.getUpdatedAt());

    // Computed fields
    response.setEventAgeInDays(event.getEventAgeInDays());
    response.setIsFollowUpOverdue(event.isFollowUpOverdue());
    response.setSummary(event.getSummary());

    return response;
  }

  /** Converts a list of CustomerTimelineEvent entities to TimelineEventResponse DTOs. */
  public List<TimelineEventResponse> toResponseList(List<CustomerTimelineEvent> events) {
    if (events == null) {
      return Collections.emptyList();
    }

    return events.stream().map(this::toResponse).collect(Collectors.toList());
  }
}

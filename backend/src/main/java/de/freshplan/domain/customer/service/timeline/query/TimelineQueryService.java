package de.freshplan.domain.customer.service.timeline.query;

import de.freshplan.domain.customer.entity.CustomerTimelineEvent;
import de.freshplan.domain.customer.entity.EventCategory;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.repository.CustomerTimelineRepository;
import de.freshplan.domain.customer.service.dto.timeline.*;
import de.freshplan.domain.customer.service.exception.CustomerNotFoundException;
import de.freshplan.domain.customer.service.mapper.CustomerTimelineMapper;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

/**
 * Query service for reading customer timeline events.
 * Handles all read operations for customer interaction history.
 * 
 * This is part of the CQRS refactoring (PR #5) to separate read and write operations.
 * All methods are exact copies from CustomerTimelineService to ensure 100% compatibility.
 * 
 * IMPORTANT: No @Transactional annotation on class or methods as these are read-only operations.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class TimelineQueryService {

  private static final Logger LOG = Logger.getLogger(TimelineQueryService.class);

  private final CustomerTimelineRepository timelineRepository;
  private final CustomerRepository customerRepository;
  private final CustomerTimelineMapper timelineMapper;

  @Inject
  public TimelineQueryService(
      CustomerTimelineRepository timelineRepository,
      CustomerRepository customerRepository,
      CustomerTimelineMapper timelineMapper) {
    this.timelineRepository = timelineRepository;
    this.customerRepository = customerRepository;
    this.timelineMapper = timelineMapper;
  }

  /**
   * Gets paginated timeline events for a customer.
   * EXACT COPY from CustomerTimelineService lines 171-215
   * 
   * NOTE: No @Transactional annotation as this is a read-only operation
   */
  public TimelineListResponse getCustomerTimeline(
      @NotNull UUID customerId, int page, int size, String category, String search) {

    // Enforce maximum page size for performance
    int maxSize = 100;
    int actualSize = Math.min(size, maxSize);

    LOG.debugf("Getting timeline for customer %s, page %d, size %d", customerId, page, actualSize);

    // Verify customer exists
    if (!customerRepository.findByIdOptional(customerId).isPresent()) {
      throw new CustomerNotFoundException(customerId.toString());
    }

    Page pageRequest = Page.of(page, actualSize);
    List<CustomerTimelineEvent> events;
    long totalElements;

    if (search != null && !search.isBlank()) {
      // Search by text
      events = timelineRepository.searchByCustomerIdAndText(customerId, search, pageRequest);
      totalElements =
          timelineRepository.count(
              "customer.id = ?1 and isDeleted = false and "
                  + "(lower(title) like ?2 or lower(description) like ?2)",
              customerId,
              "%" + search.toLowerCase() + "%");
    } else if (category != null && !category.isBlank()) {
      // Filter by category
      EventCategory eventCategory = EventCategory.valueOf(category);
      events =
          timelineRepository.findByCustomerIdAndCategory(customerId, eventCategory, pageRequest);
      totalElements = timelineRepository.countByCustomerIdAndCategory(customerId, eventCategory);
    } else {
      // Get all events
      events = timelineRepository.findByCustomerId(customerId, pageRequest);
      totalElements = timelineRepository.countByCustomerId(customerId);
    }

    List<TimelineEventResponse> content =
        events.stream().map(timelineMapper::toResponse).collect(Collectors.toList());

    return new TimelineListResponse(
        content, page, size, totalElements, (int) Math.ceil((double) totalElements / size));
  }

  /**
   * Gets timeline events requiring follow-up.
   * EXACT COPY from CustomerTimelineService lines 218-224
   * 
   * NOTE: No @Transactional annotation as this is a read-only operation
   */
  public List<TimelineEventResponse> getFollowUpEvents(@NotNull UUID customerId) {
    LOG.debugf("Getting follow-up events for customer %s", customerId);

    List<CustomerTimelineEvent> events = timelineRepository.findRequiringFollowUp(customerId);

    return events.stream().map(timelineMapper::toResponse).collect(Collectors.toList());
  }

  /**
   * Gets overdue follow-up events.
   * EXACT COPY from CustomerTimelineService lines 227-233
   * 
   * NOTE: No @Transactional annotation as this is a read-only operation
   */
  public List<TimelineEventResponse> getOverdueFollowUps(@NotNull UUID customerId) {
    LOG.debugf("Getting overdue follow-ups for customer %s", customerId);

    List<CustomerTimelineEvent> events = timelineRepository.findOverdueFollowUps(customerId);

    return events.stream().map(timelineMapper::toResponse).collect(Collectors.toList());
  }

  /**
   * Gets recent communication history.
   * EXACT COPY from CustomerTimelineService lines 236-244
   * 
   * NOTE: No @Transactional annotation as this is a read-only operation
   */
  public List<TimelineEventResponse> getRecentCommunications(@NotNull UUID customerId, int days) {

    LOG.debugf("Getting recent communications for customer %s (last %d days)", customerId, days);

    List<CustomerTimelineEvent> events =
        timelineRepository.findRecentCommunications(customerId, days);

    return events.stream().map(timelineMapper::toResponse).collect(Collectors.toList());
  }

  /**
   * Gets timeline summary statistics.
   * EXACT COPY from CustomerTimelineService lines 298-310
   * 
   * NOTE: No @Transactional annotation as this is a read-only operation
   */
  public TimelineSummaryResponse getTimelineSummary(@NotNull UUID customerId) {
    LOG.debugf("Getting timeline summary for customer %s", customerId);

    CustomerTimelineRepository.TimelineSummary summary =
        timelineRepository.getTimelineSummary(customerId);

    return new TimelineSummaryResponse(
        summary.totalEvents,
        summary.communicationEvents,
        summary.meetingEvents,
        summary.taskEvents,
        summary.systemEvents);
  }
}
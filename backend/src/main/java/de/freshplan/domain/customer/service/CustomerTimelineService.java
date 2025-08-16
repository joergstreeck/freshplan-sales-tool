package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.repository.CustomerTimelineRepository;
import de.freshplan.domain.customer.service.dto.timeline.*;
import de.freshplan.domain.customer.service.exception.CustomerNotFoundException;
import de.freshplan.domain.customer.service.mapper.CustomerTimelineMapper;
import de.freshplan.domain.customer.service.timeline.command.TimelineCommandService;
import de.freshplan.domain.customer.service.timeline.query.TimelineQueryService;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Service for managing customer timeline events. Handles creation, retrieval, and management of
 * customer interaction history.
 *
 * <p>As part of the CQRS refactoring (PR #5), this service now acts as a Facade that delegates to
 * separate Command and Query services based on a feature flag. This ensures backward compatibility
 * while allowing gradual migration to CQRS pattern.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
@Transactional
public class CustomerTimelineService {

  private static final Logger LOG = Logger.getLogger(CustomerTimelineService.class);

  private final CustomerTimelineRepository timelineRepository;
  private final CustomerRepository customerRepository;
  private final CustomerTimelineMapper timelineMapper;

  // CQRS Services (NEW for PR #5)
  @Inject TimelineCommandService commandService;
  @Inject TimelineQueryService queryService;

  // Feature flag for CQRS pattern
  @ConfigProperty(name = "features.cqrs.enabled", defaultValue = "false")
  boolean cqrsEnabled;

  @Inject
  public CustomerTimelineService(
      CustomerTimelineRepository timelineRepository,
      CustomerRepository customerRepository,
      CustomerTimelineMapper timelineMapper) {
    this.timelineRepository = timelineRepository;
    this.customerRepository = customerRepository;
    this.timelineMapper = timelineMapper;
  }

  /** Creates a new timeline event for a customer. */
  public TimelineEventResponse createEvent(
      @NotNull UUID customerId, @Valid @NotNull CreateTimelineEventRequest request) {

    if (cqrsEnabled) {
      LOG.debugf("Using CQRS CommandService for createEvent");
      return commandService.createEvent(customerId, request);
    }

    // Legacy implementation below
    LOG.infof("Creating timeline event for customer %s: %s", customerId, request.getEventType());

    // Verify customer exists - in same transaction for consistency
    Customer customer =
        customerRepository
            .findByIdOptional(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId.toString()));

    // Create event entity
    CustomerTimelineEvent event = new CustomerTimelineEvent();
    event.setCustomer(customer);
    event.setEventType(request.getEventType());
    event.setTitle(request.getTitle());
    event.setDescription(request.getDescription());
    event.setCategory(request.getCategory());
    event.setImportance(
        request.getImportance() != null ? request.getImportance() : ImportanceLevel.MEDIUM);
    event.setPerformedBy(request.getPerformedBy());
    event.setPerformedByRole(request.getPerformedByRole());

    // Set optional fields
    if (request.getEventDate() != null) {
      event.setEventDate(request.getEventDate());
    }

    if (request.getCommunicationChannel() != null) {
      event.setCommunicationChannel(request.getCommunicationChannel());
      event.setCommunicationDirection(request.getCommunicationDirection());
      event.setCommunicationDuration(request.getCommunicationDuration());
    }

    if (request.getRequiresFollowUp() != null && request.getRequiresFollowUp()) {
      event.setRequiresFollowUp(true);
      event.setFollowUpDate(request.getFollowUpDate());
      event.setFollowUpNotes(request.getFollowUpNotes());
    }

    if (request.getTags() != null && !request.getTags().isEmpty()) {
      event.setTags(String.join(",", request.getTags()));
    }

    if (request.getRelatedContactId() != null) {
      event.setRelatedContactId(request.getRelatedContactId());
    }

    if (request.getBusinessImpact() != null) {
      event.setBusinessImpact(request.getBusinessImpact());
      event.setRevenueImpact(request.getRevenueImpact());
    }

    // Persist event
    timelineRepository.persist(event);

    LOG.infof("Created timeline event %s for customer %s", event.getId(), customerId);

    return timelineMapper.toResponse(event);
  }

  /** Creates a quick note event. */
  public TimelineEventResponse createNote(
      @NotNull UUID customerId, @Valid @NotNull CreateNoteRequest request) {

    if (cqrsEnabled) {
      LOG.debugf("Using CQRS CommandService for createNote");
      return commandService.createNote(customerId, request);
    }

    // Legacy implementation below
    LOG.infof("Creating note for customer %s", customerId);

    Customer customer =
        customerRepository
            .findByIdOptional(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId.toString()));

    CustomerTimelineEvent event = new CustomerTimelineEvent();
    event.setCustomer(customer);
    event.setEventType("NOTE");
    event.setTitle("Notiz");
    event.setDescription(request.getNote());
    event.setCategory(EventCategory.NOTE);
    event.setImportance(ImportanceLevel.MEDIUM);
    event.setPerformedBy(request.getPerformedBy());

    timelineRepository.persist(event);

    return timelineMapper.toResponse(event);
  }

  /** Creates a communication event (call, email, etc.). */
  public TimelineEventResponse createCommunication(
      @NotNull UUID customerId, @Valid @NotNull CreateCommunicationRequest request) {

    if (cqrsEnabled) {
      LOG.debugf("Using CQRS CommandService for createCommunication");
      return commandService.createCommunication(customerId, request);
    }

    // Legacy implementation below
    LOG.infof(
        "Creating communication event for customer %s via %s", customerId, request.getChannel());

    Customer customer =
        customerRepository
            .findByIdOptional(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId.toString()));

    CustomerTimelineEvent event =
        CustomerTimelineEvent.createCommunicationEvent(
            customer,
            request.getChannel(),
            request.getDirection(),
            request.getDescription(),
            request.getPerformedBy());

    // Set additional communication details
    event.setCommunicationDuration(request.getDuration());

    if (request.getRelatedContactId() != null) {
      event.setRelatedContactId(request.getRelatedContactId());
    }

    if (request.getRequiresFollowUp() != null && request.getRequiresFollowUp()) {
      event.setFollowUpRequired(request.getFollowUpDate(), request.getFollowUpNotes());
    }

    timelineRepository.persist(event);

    return timelineMapper.toResponse(event);
  }

  /** Gets paginated timeline events for a customer. */
  public TimelineListResponse getCustomerTimeline(
      @NotNull UUID customerId, int page, int size, String category, String search) {

    if (cqrsEnabled) {
      LOG.debugf("Using CQRS QueryService for getCustomerTimeline");
      return queryService.getCustomerTimeline(customerId, page, size, category, search);
    }

    // Legacy implementation below
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

  /** Gets timeline events requiring follow-up. */
  public List<TimelineEventResponse> getFollowUpEvents(@NotNull UUID customerId) {
    if (cqrsEnabled) {
      LOG.debugf("Using CQRS QueryService for getFollowUpEvents");
      return queryService.getFollowUpEvents(customerId);
    }

    // Legacy implementation below
    LOG.debugf("Getting follow-up events for customer %s", customerId);

    List<CustomerTimelineEvent> events = timelineRepository.findRequiringFollowUp(customerId);

    return events.stream().map(timelineMapper::toResponse).collect(Collectors.toList());
  }

  /** Gets overdue follow-up events. */
  public List<TimelineEventResponse> getOverdueFollowUps(@NotNull UUID customerId) {
    if (cqrsEnabled) {
      LOG.debugf("Using CQRS QueryService for getOverdueFollowUps");
      return queryService.getOverdueFollowUps(customerId);
    }

    // Legacy implementation below
    LOG.debugf("Getting overdue follow-ups for customer %s", customerId);

    List<CustomerTimelineEvent> events = timelineRepository.findOverdueFollowUps(customerId);

    return events.stream().map(timelineMapper::toResponse).collect(Collectors.toList());
  }

  /** Gets recent communication history. */
  public List<TimelineEventResponse> getRecentCommunications(@NotNull UUID customerId, int days) {

    if (cqrsEnabled) {
      LOG.debugf("Using CQRS QueryService for getRecentCommunications");
      return queryService.getRecentCommunications(customerId, days);
    }

    // Legacy implementation below
    LOG.debugf("Getting recent communications for customer %s (last %d days)", customerId, days);

    List<CustomerTimelineEvent> events =
        timelineRepository.findRecentCommunications(customerId, days);

    return events.stream().map(timelineMapper::toResponse).collect(Collectors.toList());
  }

  /** Marks a follow-up as completed. */
  public void completeFollowUp(@NotNull UUID eventId, @NotNull String completedBy) {

    if (cqrsEnabled) {
      LOG.debugf("Using CQRS CommandService for completeFollowUp");
      commandService.completeFollowUp(eventId, completedBy);
      return;
    }

    // Legacy implementation below
    LOG.infof("Completing follow-up for event %s by %s", eventId, completedBy);

    timelineRepository.completeFollowUp(eventId, completedBy);
  }

  /** Updates an existing timeline event. */
  public TimelineEventResponse updateEvent(
      @NotNull UUID eventId, @Valid @NotNull UpdateTimelineEventRequest request) {

    if (cqrsEnabled) {
      LOG.debugf("Using CQRS CommandService for updateEvent");
      return commandService.updateEvent(eventId, request);
    }

    // Legacy implementation below
    LOG.infof("Updating timeline event %s", eventId);

    CustomerTimelineEvent event =
        timelineRepository
            .findByIdOptional(eventId)
            .orElseThrow(
                () -> new IllegalArgumentException("Timeline event not found: " + eventId));

    // Update fields
    if (request.getTitle() != null) {
      event.setTitle(request.getTitle());
    }
    if (request.getDescription() != null) {
      event.setDescription(request.getDescription());
    }
    if (request.getImportance() != null) {
      event.setImportance(request.getImportance());
    }
    if (request.getTags() != null) {
      event.setTags(String.join(",", request.getTags()));
    }
    if (request.getBusinessImpact() != null) {
      event.setBusinessImpact(request.getBusinessImpact());
    }

    event.setUpdatedBy(request.getUpdatedBy());

    timelineRepository.persist(event);

    return timelineMapper.toResponse(event);
  }

  /** Soft deletes a timeline event. */
  public void deleteEvent(@NotNull UUID eventId, @NotNull String deletedBy) {
    if (cqrsEnabled) {
      LOG.debugf("Using CQRS CommandService for deleteEvent");
      commandService.deleteEvent(eventId, deletedBy);
      return;
    }

    // Legacy implementation below
    LOG.infof("Soft deleting timeline event %s by %s", eventId, deletedBy);

    timelineRepository.softDelete(eventId, deletedBy);
  }

  /** Gets timeline summary statistics. */
  public TimelineSummaryResponse getTimelineSummary(@NotNull UUID customerId) {
    if (cqrsEnabled) {
      LOG.debugf("Using CQRS QueryService for getTimelineSummary");
      return queryService.getTimelineSummary(customerId);
    }

    // Legacy implementation below
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

  /** Creates a system event (for audit trail). */
  public void createSystemEvent(
      @NotNull Customer customer,
      @NotNull String eventType,
      @NotNull String description,
      @NotNull String performedBy) {

    if (cqrsEnabled) {
      LOG.debugf("Using CQRS CommandService for createSystemEvent");
      commandService.createSystemEvent(customer, eventType, description, performedBy);
      return;
    }

    // Legacy implementation below
    LOG.debugf("Creating system event for customer %s: %s", customer.getId(), eventType);

    CustomerTimelineEvent event =
        CustomerTimelineEvent.createSystemEvent(customer, eventType, description, performedBy);

    timelineRepository.persist(event);
  }
}

package de.freshplan.domain.customer.service.timeline.command;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.repository.CustomerTimelineRepository;
import de.freshplan.domain.customer.service.dto.timeline.*;
import de.freshplan.domain.customer.service.exception.CustomerNotFoundException;
import de.freshplan.domain.customer.service.mapper.CustomerTimelineMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.jboss.logging.Logger;

/**
 * Command service for managing customer timeline events.
 * Handles all write operations for customer interaction history.
 * 
 * This is part of the CQRS refactoring (PR #5) to separate read and write operations.
 * All methods are exact copies from CustomerTimelineService to ensure 100% compatibility.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class TimelineCommandService {

  private static final Logger LOG = Logger.getLogger(TimelineCommandService.class);

  private final CustomerTimelineRepository timelineRepository;
  private final CustomerRepository customerRepository;
  private final CustomerTimelineMapper timelineMapper;

  @Inject
  public TimelineCommandService(
      CustomerTimelineRepository timelineRepository,
      CustomerRepository customerRepository,
      CustomerTimelineMapper timelineMapper) {
    this.timelineRepository = timelineRepository;
    this.customerRepository = customerRepository;
    this.timelineMapper = timelineMapper;
  }

  /**
   * Creates a new timeline event for a customer.
   * EXACT COPY from CustomerTimelineService lines 48-107
   */
  @Transactional
  public TimelineEventResponse createEvent(
      @NotNull UUID customerId, @Valid @NotNull CreateTimelineEventRequest request) {

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

  /**
   * Creates a quick note event.
   * EXACT COPY from CustomerTimelineService lines 110-132
   */
  @Transactional
  public TimelineEventResponse createNote(
      @NotNull UUID customerId, @Valid @NotNull CreateNoteRequest request) {

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

  /**
   * Creates a communication event (call, email, etc.).
   * EXACT COPY from CustomerTimelineService lines 135-168
   */
  @Transactional
  public TimelineEventResponse createCommunication(
      @NotNull UUID customerId, @Valid @NotNull CreateCommunicationRequest request) {

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

  /**
   * Marks a follow-up as completed.
   * EXACT COPY from CustomerTimelineService lines 247-252
   */
  @Transactional
  public void completeFollowUp(@NotNull UUID eventId, @NotNull String completedBy) {

    LOG.infof("Completing follow-up for event %s by %s", eventId, completedBy);

    timelineRepository.completeFollowUp(eventId, completedBy);
  }

  /**
   * Updates an existing timeline event.
   * EXACT COPY from CustomerTimelineService lines 255-288
   */
  @Transactional
  public TimelineEventResponse updateEvent(
      @NotNull UUID eventId, @Valid @NotNull UpdateTimelineEventRequest request) {

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

  /**
   * Soft deletes a timeline event.
   * EXACT COPY from CustomerTimelineService lines 291-295
   */
  @Transactional
  public void deleteEvent(@NotNull UUID eventId, @NotNull String deletedBy) {
    LOG.infof("Soft deleting timeline event %s by %s", eventId, deletedBy);

    timelineRepository.softDelete(eventId, deletedBy);
  }

  /**
   * Creates a system event (for audit trail).
   * EXACT COPY from CustomerTimelineService lines 313-325
   * 
   * NOTE: This method is package-private in the original service but needs to be public
   * for CQRS access. The original visibility might have been a mistake.
   */
  @Transactional
  public void createSystemEvent(
      @NotNull Customer customer,
      @NotNull String eventType,
      @NotNull String description,
      @NotNull String performedBy) {

    LOG.debugf("Creating system event for customer %s: %s", customer.getId(), eventType);

    CustomerTimelineEvent event =
        CustomerTimelineEvent.createSystemEvent(customer, eventType, description, performedBy);

    timelineRepository.persist(event);
  }
}
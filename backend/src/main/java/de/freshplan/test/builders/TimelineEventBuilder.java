package de.freshplan.test.builders;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.repository.CustomerTimelineEventRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Builder for creating test CustomerTimelineEvent entities. Provides fluent API for creating
 * various timeline events for customer history.
 *
 * <p>This builder ensures all test timeline events are properly marked and can be identified for
 * cleanup.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class TimelineEventBuilder {

  @Inject CustomerTimelineEventRepository repository;

  @Inject CustomerRepository customerRepository;

  // Required fields
  private Customer customer = null;
  private String eventType = "TEST_EVENT";
  private LocalDateTime eventDate = LocalDateTime.now();
  private String title = "Test Event";
  private EventCategory category = EventCategory.NOTE;
  private ImportanceLevel importance = ImportanceLevel.MEDIUM;
  private String performedBy = "test-user";

  // Optional fields
  private String description = null;
  private UUID relatedContactId = null;
  private UUID relatedLocationId = null;
  private UUID relatedDocumentId = null;
  private String tags = null;
  private String labels = null;

  // Flags
  private Boolean requiresFollowUp = false;
  private LocalDateTime followUpDate = null;
  private Boolean followUpCompleted = false;
  private Boolean isPublic = true;
  private Boolean isCustomerVisible = false;

  /**
   * Resets the builder to default values for creating a new event.
   *
   * @return this builder instance for chaining
   */
  public TimelineEventBuilder reset() {
    this.customer = null;
    this.eventType = "TEST_EVENT";
    this.eventDate = LocalDateTime.now();
    this.title = "Test Event";
    this.category = EventCategory.NOTE;
    this.importance = ImportanceLevel.MEDIUM;
    this.performedBy = "test-user";
    this.description = null;
    this.relatedContactId = null;
    this.relatedLocationId = null;
    this.relatedDocumentId = null;
    this.tags = null;
    this.labels = null;
    this.requiresFollowUp = false;
    this.followUpDate = null;
    this.followUpCompleted = false;
    this.isPublic = true;
    this.isCustomerVisible = false;
    return this;
  }

  // Customer association (REQUIRED)
  public TimelineEventBuilder forCustomer(Customer customer) {
    this.customer = customer;
    return this;
  }

  public TimelineEventBuilder forCustomerId(UUID customerId) {
    this.customer = customerRepository.findById(customerId);
    if (this.customer == null) {
      throw new IllegalArgumentException("Customer not found: " + customerId);
    }
    return this;
  }

  // Basic setters
  public TimelineEventBuilder withEventType(String eventType) {
    this.eventType = eventType;
    return this;
  }

  public TimelineEventBuilder withTitle(String title) {
    this.title = title;
    return this;
  }

  public TimelineEventBuilder withDescription(String description) {
    this.description = description;
    return this;
  }

  public TimelineEventBuilder withCategory(EventCategory category) {
    this.category = category;
    return this;
  }

  public TimelineEventBuilder withImportance(ImportanceLevel importance) {
    this.importance = importance;
    return this;
  }

  public TimelineEventBuilder performedBy(String user) {
    this.performedBy = user;
    return this;
  }

  // Date setters
  public TimelineEventBuilder withEventDate(LocalDateTime date) {
    this.eventDate = date;
    return this;
  }

  public TimelineEventBuilder happenedToday() {
    this.eventDate = LocalDateTime.now();
    return this;
  }

  public TimelineEventBuilder happenedDaysAgo(int days) {
    this.eventDate = LocalDateTime.now().minusDays(days);
    return this;
  }

  public TimelineEventBuilder happenedHoursAgo(int hours) {
    this.eventDate = LocalDateTime.now().minusHours(hours);
    return this;
  }

  // Related entity setters
  public TimelineEventBuilder relatedToContact(UUID contactId) {
    this.relatedContactId = contactId;
    return this;
  }

  public TimelineEventBuilder relatedToLocation(UUID locationId) {
    this.relatedLocationId = locationId;
    return this;
  }

  public TimelineEventBuilder relatedToDocument(UUID documentId) {
    this.relatedDocumentId = documentId;
    return this;
  }

  public TimelineEventBuilder withTags(String... tags) {
    this.tags = String.join(",", tags);
    return this;
  }

  public TimelineEventBuilder withLabels(String... labels) {
    this.labels = String.join(",", labels);
    return this;
  }

  // Follow-up setters
  public TimelineEventBuilder requiresFollowUp(LocalDateTime date) {
    this.requiresFollowUp = true;
    this.followUpDate = date;
    this.followUpCompleted = false;
    return this;
  }

  public TimelineEventBuilder requiresFollowUpInDays(int days) {
    return requiresFollowUp(LocalDateTime.now().plusDays(days));
  }

  public TimelineEventBuilder followUpCompleted() {
    this.followUpCompleted = true;
    return this;
  }

  // Visibility setters
  public TimelineEventBuilder asPrivate() {
    this.isPublic = false;
    this.isCustomerVisible = false;
    return this;
  }

  public TimelineEventBuilder asCustomerVisible() {
    this.isCustomerVisible = true;
    this.isPublic = true;
    return this;
  }

  // Predefined event types
  public TimelineEventBuilder asStatusChange(CustomerStatus oldStatus, CustomerStatus newStatus) {
    this.eventType = "STATUS_CHANGE";
    this.category = EventCategory.STATUS_UPDATE;
    this.title = "Status geändert: " + oldStatus + " → " + newStatus;
    this.importance = ImportanceLevel.HIGH;
    this.tags = "status-change," + oldStatus + "," + newStatus;
    return this;
  }

  public TimelineEventBuilder asPhoneCall(String summary) {
    this.eventType = "PHONE_CALL";
    this.category = EventCategory.PHONE_CALL;
    this.title = "Telefonat durchgeführt";
    this.description = summary;
    this.importance = ImportanceLevel.MEDIUM;
    return this;
  }

  public TimelineEventBuilder asEmail(String subject) {
    this.eventType = "EMAIL_SENT";
    this.category = EventCategory.EMAIL;
    this.title = "E-Mail: " + subject;
    this.importance = ImportanceLevel.LOW;
    return this;
  }

  public TimelineEventBuilder asMeeting(String purpose) {
    this.eventType = "MEETING";
    this.category = EventCategory.MEETING;
    this.title = "Meeting: " + purpose;
    this.importance = ImportanceLevel.HIGH;
    this.isCustomerVisible = true;
    return this;
  }

  public TimelineEventBuilder asNote(String note) {
    this.eventType = "NOTE_ADDED";
    this.category = EventCategory.NOTE;
    this.title = "Notiz hinzugefügt";
    this.description = note;
    this.importance = ImportanceLevel.LOW;
    this.isPublic = false;
    return this;
  }

  public TimelineEventBuilder asQuote(BigDecimal amount) {
    this.eventType = "QUOTE_CREATED";
    this.category = EventCategory.QUOTE;
    this.title = "Angebot erstellt: " + amount + " €";
    this.importance = ImportanceLevel.HIGH;
    this.isCustomerVisible = true;
    return this;
  }

  public TimelineEventBuilder asOrder(String orderNumber) {
    this.eventType = "ORDER_PLACED";
    this.category = EventCategory.ORDER;
    this.title = "Bestellung eingegangen: " + orderNumber;
    this.importance = ImportanceLevel.HIGH;
    this.isCustomerVisible = true;
    return this;
  }

  public TimelineEventBuilder asComplaint(String issue) {
    this.eventType = "COMPLAINT_RECEIVED";
    this.category = EventCategory.COMPLAINT;
    this.title = "Beschwerde erhalten";
    this.description = issue;
    this.importance = ImportanceLevel.URGENT;
    this.requiresFollowUp = true;
    this.followUpDate = LocalDateTime.now().plusDays(1);
    return this;
  }

  public TimelineEventBuilder asRiskAssessment(int riskScore) {
    this.eventType = "RISK_ASSESSMENT";
    this.category = EventCategory.RISK_ASSESSMENT;
    this.title = "Risikobewertung: Score " + riskScore;
    this.importance = riskScore > 70 ? ImportanceLevel.HIGH : ImportanceLevel.MEDIUM;
    this.tags = "risk-assessment,risk-score-" + riskScore;
    return this;
  }

  public TimelineEventBuilder asSystemEvent(String event) {
    this.eventType = "SYSTEM_EVENT";
    this.category = EventCategory.SYSTEM;
    this.title = event;
    this.importance = ImportanceLevel.LOW;
    this.performedBy = "system";
    this.isPublic = false;
    return this;
  }

  /**
   * Builds a CustomerTimelineEvent entity WITHOUT persisting to database. Use this for unit tests
   * or when you need an entity without DB interaction.
   *
   * @return a new CustomerTimelineEvent entity with test data markers
   */
  public CustomerTimelineEvent build() {
    if (customer == null) {
      throw new IllegalStateException(
          "Customer is required for TimelineEventBuilder. Use forCustomer() first.");
    }

    CustomerTimelineEvent event = new CustomerTimelineEvent();

    // Set required fields
    event.setCustomer(customer);
    event.setEventType(eventType);
    event.setEventDate(eventDate);
    event.setTitle("[TEST] " + title);
    event.setCategory(category);
    event.setImportance(importance);
    event.setPerformedBy(performedBy);

    // Set optional fields
    event.setDescription(description);
    event.setRelatedContactId(relatedContactId);
    event.setRelatedLocationId(relatedLocationId);
    event.setRelatedDocumentId(relatedDocumentId);
    event.setTags(tags);
    event.setLabels(labels);

    // Set follow-up fields
    event.setRequiresFollowUp(requiresFollowUp);
    event.setFollowUpDate(followUpDate);
    event.setFollowUpCompleted(followUpCompleted);

    // Set visibility flags
    event.setIsPublic(isPublic);
    event.setIsCustomerVisible(isCustomerVisible);

    // Set test data marker
    event.setIsTestData(true);
    event.setIsDeleted(false);

    // Set audit fields
    event.setCreatedAt(LocalDateTime.now());

    return event;
  }

  /**
   * Builds and persists a CustomerTimelineEvent entity to the database. Use this for integration
   * tests that need DB interaction.
   *
   * @return the persisted CustomerTimelineEvent entity
   */
  @Transactional
  public CustomerTimelineEvent persist() {
    CustomerTimelineEvent event = build();
    repository.persist(event);
    repository.flush(); // Force immediate constraint validation
    return event;
  }
}

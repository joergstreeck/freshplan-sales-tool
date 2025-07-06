package de.freshplan.domain.customer.repository;

import de.freshplan.domain.customer.entity.CustomerTimelineEvent;
import de.freshplan.domain.customer.entity.EventCategory;
import de.freshplan.domain.customer.entity.ImportanceLevel;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing customer timeline events. Provides comprehensive methods for querying and
 * managing timeline history.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
@Transactional
public class CustomerTimelineRepository
    implements PanacheRepositoryBase<CustomerTimelineEvent, UUID> {

  /**
   * Finds all timeline events for a specific customer with pagination. Uses explicit JOIN FETCH to
   * avoid N+1 query problem.
   */
  public List<CustomerTimelineEvent> findByCustomerId(UUID customerId, Page page) {
    return find(
            "SELECT e FROM CustomerTimelineEvent e "
                + "LEFT JOIN FETCH e.customer c "
                + "WHERE c.id = :customerId and e.isDeleted = false "
                + "ORDER BY e.eventDate DESC, e.createdAt DESC",
            Parameters.with("customerId", customerId))
        .page(page)
        .list();
  }

  /**
   * Counts all non-deleted timeline events for a customer. Uses customer.id field to maintain
   * consistency with entity mapping.
   */
  public long countByCustomerId(UUID customerId) {
    return count(
        "customer.id = :customerId and isDeleted = false",
        Parameters.with("customerId", customerId));
  }

  /**
   * Finds timeline events by category for a customer. Uses explicit JOIN FETCH to avoid N+1 query
   * problem.
   */
  public List<CustomerTimelineEvent> findByCustomerIdAndCategory(
      UUID customerId, EventCategory category, Page page) {

    return find(
            "SELECT e FROM CustomerTimelineEvent e "
                + "LEFT JOIN FETCH e.customer c "
                + "WHERE c.id = :customerId and e.category = :category "
                + "and e.isDeleted = false "
                + "ORDER BY e.eventDate DESC",
            Parameters.with("customerId", customerId).and("category", category))
        .page(page)
        .list();
  }

  /** Finds timeline events with specific importance levels. */
  public List<CustomerTimelineEvent> findByCustomerIdAndImportance(
      UUID customerId, ImportanceLevel minImportance, Page page) {

    return find(
            "customer.id = :customerId and importance >= :importance and isDeleted = false",
            Sort.by("importance").descending().and("eventDate").descending(),
            Parameters.with("customerId", customerId).and("importance", minImportance))
        .page(page)
        .list();
  }

  /** Finds timeline events within a date range. */
  public List<CustomerTimelineEvent> findByCustomerIdAndDateRange(
      UUID customerId, LocalDateTime startDate, LocalDateTime endDate, Page page) {

    return find(
            "customer.id = :customerId and eventDate between :startDate and :endDate and isDeleted = false",
            Sort.by("eventDate").descending(),
            Parameters.with("customerId", customerId)
                .and("startDate", startDate)
                .and("endDate", endDate))
        .page(page)
        .list();
  }

  /** Finds timeline events requiring follow-up. */
  public List<CustomerTimelineEvent> findRequiringFollowUp(UUID customerId) {
    return find(
            "customer.id = :customerId and requiresFollowUp = true "
                + "and followUpCompleted = false and isDeleted = false",
            Sort.by("followUpDate").ascending(),
            Parameters.with("customerId", customerId))
        .list();
  }

  /** Finds overdue follow-ups for a customer. */
  public List<CustomerTimelineEvent> findOverdueFollowUps(UUID customerId) {
    return find(
            "customer.id = :customerId and requiresFollowUp = true "
                + "and followUpCompleted = false and followUpDate < :now and isDeleted = false",
            Sort.by("followUpDate").ascending(),
            Parameters.with("customerId", customerId).and("now", LocalDateTime.now()))
        .list();
  }

  /** Finds recent communication events for a customer. */
  public List<CustomerTimelineEvent> findRecentCommunications(UUID customerId, int days) {
    LocalDateTime since = LocalDateTime.now().minusDays(days);

    return find(
            "customer.id = :customerId and category in :categories "
                + "and eventDate >= :since and isDeleted = false",
            Sort.by("eventDate").descending(),
            Parameters.with("customerId", customerId)
                .and(
                    "categories",
                    List.of(
                        EventCategory.COMMUNICATION,
                        EventCategory.EMAIL,
                        EventCategory.PHONE_CALL,
                        EventCategory.MEETING))
                .and("since", since))
        .list();
  }

  /** Finds timeline events by event type. */
  public List<CustomerTimelineEvent> findByCustomerIdAndEventType(
      UUID customerId, String eventType, Page page) {

    return find(
            "customer.id = :customerId and eventType = :eventType and isDeleted = false",
            Sort.by("eventDate").descending(),
            Parameters.with("customerId", customerId).and("eventType", eventType))
        .page(page)
        .list();
  }

  /**
   * Searches timeline events by text in title or description. Uses explicit JOIN FETCH to avoid N+1
   * query problem.
   */
  public List<CustomerTimelineEvent> searchByCustomerIdAndText(
      UUID customerId, String searchText, Page page) {

    String likePattern = "%" + searchText.toLowerCase() + "%";

    return find(
            "SELECT e FROM CustomerTimelineEvent e "
                + "LEFT JOIN FETCH e.customer c "
                + "WHERE c.id = :customerId and e.isDeleted = false and "
                + "(lower(e.title) like :search or lower(e.description) like :search) "
                + "ORDER BY e.eventDate DESC",
            Parameters.with("customerId", customerId).and("search", likePattern))
        .page(page)
        .list();
  }

  /** Finds timeline events with specific tags. */
  public List<CustomerTimelineEvent> findByCustomerIdAndTag(
      UUID customerId, String tag, Page page) {

    return find(
            "customer.id = :customerId and tags like :tag and isDeleted = false",
            Sort.by("eventDate").descending(),
            Parameters.with("customerId", customerId).and("tag", "%" + tag + "%"))
        .page(page)
        .list();
  }

  /** Gets the latest event for a customer. */
  public Optional<CustomerTimelineEvent> findLatestByCustomerId(UUID customerId) {
    return find(
            "customer.id = :customerId and isDeleted = false",
            Sort.by("eventDate").descending().and("createdAt").descending(),
            Parameters.with("customerId", customerId))
        .firstResultOptional();
  }

  /** Gets the latest event of a specific category. */
  public Optional<CustomerTimelineEvent> findLatestByCustomerIdAndCategory(
      UUID customerId, EventCategory category) {

    return find(
            "customer.id = :customerId and category = :category and isDeleted = false",
            Sort.by("eventDate").descending(),
            Parameters.with("customerId", customerId).and("category", category))
        .firstResultOptional();
  }

  /** Counts events by category for a customer. */
  public long countByCustomerIdAndCategory(UUID customerId, EventCategory category) {
    return count(
        "customer.id = :customerId and category = :category and isDeleted = false",
        Parameters.with("customerId", customerId).and("category", category));
  }

  /** Marks an event as deleted (soft delete). */
  public void softDelete(UUID eventId, String deletedBy) {
    update(
        "isDeleted = true, deletedAt = :now, deletedBy = :deletedBy where id = :id",
        Parameters.with("now", LocalDateTime.now()).and("deletedBy", deletedBy).and("id", eventId));
  }

  /** Restores a soft-deleted event. */
  public void restore(UUID eventId) {
    update(
        "isDeleted = false, deletedAt = null, deletedBy = null where id = :id",
        Parameters.with("id", eventId));
  }

  /** Updates follow-up status to completed. */
  public void completeFollowUp(UUID eventId, String completedBy) {
    update(
        "followUpCompleted = true, updatedBy = :completedBy, updatedAt = :now "
            + "where id = :id and requiresFollowUp = true",
        Parameters.with("completedBy", completedBy)
            .and("now", LocalDateTime.now())
            .and("id", eventId));
  }

  /** Finds events performed by a specific user. */
  public List<CustomerTimelineEvent> findByPerformedBy(String performedBy, Page page) {
    return find(
            "performedBy = :performedBy and isDeleted = false",
            Sort.by("eventDate").descending(),
            Parameters.with("performedBy", performedBy))
        .page(page)
        .list();
  }

  /** Gets timeline summary statistics for a customer. */
  public TimelineSummary getTimelineSummary(UUID customerId) {
    // This would typically be done with a native query for performance,
    // but for now we'll use multiple queries
    TimelineSummary summary = new TimelineSummary();
    summary.totalEvents = countByCustomerId(customerId);
    summary.communicationEvents =
        countByCustomerIdAndCategory(customerId, EventCategory.COMMUNICATION);
    summary.meetingEvents = countByCustomerIdAndCategory(customerId, EventCategory.MEETING);
    summary.taskEvents = countByCustomerIdAndCategory(customerId, EventCategory.TASK);
    summary.systemEvents = countByCustomerIdAndCategory(customerId, EventCategory.SYSTEM);
    return summary;
  }

  /** Inner class for timeline summary statistics. */
  public static class TimelineSummary {
    public long totalEvents;
    public long communicationEvents;
    public long meetingEvents;
    public long taskEvents;
    public long systemEvents;
  }
}

package de.freshplan.domain.customer.repository;

import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.entity.ContactInteraction;
import de.freshplan.domain.customer.entity.ContactInteraction.InteractionType;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repository for ContactInteraction entities. Provides methods for querying interaction history and
 * analytics.
 */
@ApplicationScoped
public class ContactInteractionRepository
    implements PanacheRepositoryBase<ContactInteraction, UUID> {

  /** Find all interactions for a specific contact, ordered by timestamp descending */
  public List<ContactInteraction> findByContact(CustomerContact contact) {
    return find("contact", Sort.by("timestamp").descending(), contact).list();
  }

  /** Find interactions for a contact with pagination */
  public List<ContactInteraction> findByContactPaginated(CustomerContact contact, Page page) {
    return find("contact", Sort.by("timestamp").descending(), contact).page(page).list();
  }

  /** Find interactions by type for a contact */
  public List<ContactInteraction> findByContactAndType(CustomerContact contact, InteractionType type) {
    return find(
            "contact = :contact and type = :type",
            Sort.by("timestamp").descending(),
            Parameters.with("contact", contact).and("type", type))
        .list();
  }

  /** Find recent interactions for warmth calculation */
  public List<ContactInteraction> findRecentInteractions(CustomerContact contact, int days) {
    LocalDateTime since = LocalDateTime.now().minusDays(days);
    return find(
            "contact = :contact and timestamp >= :since",
            Sort.by("timestamp").descending(),
            Parameters.with("contact", contact).and("since", since))
        .list();
  }

  /** Count interactions by type for a contact */
  public Map<InteractionType, Long> countInteractionsByType(CustomerContact contact) {
    return find(
            "select i.type, count(i) from ContactInteraction i "
                + "where i.contact = :contact group by i.type",
            Parameters.with("contact", contact))
        .project(Map.class)
        .list()
        .stream()
        .collect(
            java.util.stream.Collectors.toMap(
                m -> (InteractionType) m.get("type"), m -> (Long) m.get("count")));
  }

  /** Find interactions with pending follow-ups */
  public List<ContactInteraction> findPendingFollowUps() {
    return find(
            "nextActionDate is not null and nextActionDate <= :now",
            Sort.by("nextActionDate"),
            Parameters.with("now", LocalDateTime.now()))
        .list();
  }

  /** Find interactions by sentiment range */
  public List<ContactInteraction> findBySentiment(
      CustomerContact contact, Double minSentiment, Double maxSentiment) {
    return find(
            "contact = :contact and sentimentScore between :min and :max",
            Sort.by("timestamp").descending(),
            Parameters.with("contact", contact).and("min", minSentiment).and("max", maxSentiment))
        .list();
  }

  /** Calculate average sentiment for a contact */
  public Double calculateAverageSentiment(CustomerContact contact) {
    return find(
            "select avg(i.sentimentScore) from ContactInteraction i "
                + "where i.contact = :contact and i.sentimentScore is not null",
            Parameters.with("contact", contact))
        .project(Double.class)
        .firstResult();
  }

  /** Find last interaction for a contact */
  public ContactInteraction findLastInteraction(CustomerContact contact) {
    return find("contact", Sort.by("timestamp").descending(), contact).firstResult();
  }

  /** Find interactions initiated by customer */
  public List<ContactInteraction> findCustomerInitiated(CustomerContact contact) {
    return find(
            "contact = :contact and initiatedBy = 'CUSTOMER'",
            Sort.by("timestamp").descending(),
            Parameters.with("contact", contact))
        .list();
  }

  /** Calculate response rate (customer-initiated with sales response) */
  public Double calculateResponseRate(CustomerContact contact) {
    Long customerInitiated =
        count(
            "contact = :contact and initiatedBy = 'CUSTOMER'", Parameters.with("contact", contact));

    if (customerInitiated == 0) return 100.0; // No customer inquiries to respond to

    Long salesResponses =
        count(
            "contact = :contact and initiatedBy = 'SALES' " + "and responseTimeMinutes is not null",
            Parameters.with("contact", contact));

    return (salesResponses.doubleValue() / customerInitiated.doubleValue()) * 100;
  }

  /** Find interactions for data freshness check */
  public LocalDateTime findLastUpdateDate(CustomerContact contact) {
    ContactInteraction lastInteraction = findLastInteraction(contact);
    return lastInteraction != null ? lastInteraction.getTimestamp() : contact.getCreatedAt();
  }
}

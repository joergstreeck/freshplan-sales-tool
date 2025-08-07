package de.freshplan.domain.customer.repository;

import de.freshplan.domain.customer.entity.Contact;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Repository for Contact entity providing custom queries for multi-contact management, primary
 * contact handling, and location-based searches.
 */
@ApplicationScoped
public class ContactRepository implements PanacheRepositoryBase<Contact, UUID> {

  /**
   * Find all active contacts for a customer
   *
   * @param customerId the customer ID
   * @return list of active contacts sorted by primary status and name
   */
  public List<Contact> findByCustomerId(UUID customerId) {
    return find(
            "customer.id = ?1 and isActive = true order by isPrimary desc, lastName, firstName",
            customerId)
        .list();
  }

  /**
   * Find primary contact for a customer
   *
   * @param customerId the customer ID
   * @return optional primary contact
   */
  public Optional<Contact> findPrimaryByCustomerId(UUID customerId) {
    return find("customer.id = ?1 and isPrimary = true and isActive = true", customerId)
        .firstResultOptional();
  }

  /**
   * Find contacts assigned to a specific location
   *
   * @param locationId the location ID
   * @return list of contacts assigned to the location
   */
  public List<Contact> findByLocationId(UUID locationId) {
    return find(
            "assignedLocation.id = ?1 and isActive = true order by lastName, firstName", locationId)
        .list();
  }

  /**
   * Set a contact as primary for a customer (unsets all other primary flags)
   *
   * @param customerId the customer ID
   * @param contactId the contact ID to set as primary
   */
  @Transactional
  public void setPrimaryContact(UUID customerId, UUID contactId) {
    // First, unset all primary flags for this customer
    update("isPrimary = false where customer.id = ?1", customerId);

    // Then set the new primary contact
    update("isPrimary = true where id = ?1 and customer.id = ?2", contactId, customerId);
  }

  /**
   * Check if a customer has any contacts
   *
   * @param customerId the customer ID
   * @return true if customer has at least one active contact
   */
  public boolean hasContacts(UUID customerId) {
    return count("customer.id = ?1 and isActive = true", customerId) > 0;
  }

  /**
   * Find contacts by email across all customers
   *
   * @param email the email address
   * @return list of contacts with the given email
   */
  public List<Contact> findByEmail(String email) {
    return find("lower(email) = lower(?1) and isActive = true", email).list();
  }

  /**
   * Soft delete a contact
   *
   * @param contactId the contact ID
   * @return number of updated records
   */
  @Transactional
  public int softDelete(UUID contactId) {
    return update("isActive = false where id = ?1", contactId);
  }

  /**
   * Count active contacts for a customer
   *
   * @param customerId the customer ID
   * @return count of active contacts
   */
  public long countActiveByCustomerId(UUID customerId) {
    return count("customer.id = ?1 and isActive = true", customerId);
  }

  /**
   * Find contacts with upcoming birthdays
   *
   * @param daysAhead number of days to look ahead
   * @return list of contacts with birthdays in the next N days
   */
  public List<Contact> findUpcomingBirthdays(int daysAhead) {
    // Calculate date range in Java to avoid database-specific syntax
    LocalDate today = LocalDate.now();
    LocalDate endDate = today.plusDays(daysAhead);

    // Get all active contacts with birthdays
    List<Contact> contactsWithBirthdays = list("isActive = true AND birthday IS NOT NULL");

    // Filter in Java for upcoming birthdays (handles year boundaries correctly)
    return contactsWithBirthdays.stream()
        .filter(
            contact -> {
              LocalDate birthday = contact.getBirthday();
              if (birthday == null) return false;

              // Create birthday in current/next year
              LocalDate birthdayThisYear = birthday.withYear(today.getYear());
              LocalDate birthdayNextYear = birthday.withYear(today.getYear() + 1);

              // Check if birthday falls within the range
              return (birthdayThisYear.isAfter(today.minusDays(1))
                      && birthdayThisYear.isBefore(endDate.plusDays(1)))
                  || (birthdayNextYear.isAfter(today.minusDays(1))
                      && birthdayNextYear.isBefore(endDate.plusDays(1)));
            })
        .sorted(
            (c1, c2) -> {
              // Sort by month and day
              LocalDate b1 = c1.getBirthday();
              LocalDate b2 = c2.getBirthday();
              int monthCompare = Integer.compare(b1.getMonthValue(), b2.getMonthValue());
              return monthCompare != 0
                  ? monthCompare
                  : Integer.compare(b1.getDayOfMonth(), b2.getDayOfMonth());
            })
        .collect(Collectors.toList());
  }

  /**
   * Update location assignment for multiple contacts
   *
   * @param contactIds list of contact IDs
   * @param locationId new location ID (can be null to unassign)
   * @return number of updated records
   */
  @Transactional
  public int updateLocationAssignment(List<UUID> contactIds, UUID locationId) {
    if (contactIds == null || contactIds.isEmpty()) {
      return 0;
    }

    if (locationId == null) {
      return update("assignedLocation = null where id in ?1", contactIds);
    } else {
      return update("assignedLocation.id = ?1 where id in ?2", locationId, contactIds);
    }
  }
}

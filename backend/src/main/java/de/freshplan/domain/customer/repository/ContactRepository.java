package de.freshplan.domain.customer.repository;

import de.freshplan.domain.customer.entity.CustomerContact;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CustomerContact entity providing custom queries for multi-contact management,
 * primary contact handling, and location-based searches.
 */
@ApplicationScoped
public class ContactRepository implements PanacheRepositoryBase<CustomerContact, UUID> {

  /**
   * Find all active contacts for a customer
   *
   * @param customerId the customer ID
   * @return list of active contacts sorted by primary status and name
   */
  public List<CustomerContact> findByCustomerId(UUID customerId) {
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
  public Optional<CustomerContact> findPrimaryByCustomerId(UUID customerId) {
    return find("customer.id = ?1 and isPrimary = true and isActive = true", customerId)
        .firstResultOptional();
  }

  // ========== SEARCH QUERIES (FC-005 PR4 - Extended for Hybrid Solution) ==========

  /**
   * Search contacts by full text in multiple fields. Basic search for backward compatibility.
   *
   * @param query the search query
   * @param limit maximum number of results
   * @return list of matching contacts
   */
  public List<CustomerContact> searchContacts(String query, int limit) {
    String searchPattern = "%" + query.toLowerCase() + "%";
    return find(
            "isActive = true AND (lower(firstName || ' ' || lastName) like ?1 "
                + "OR lower(email) like ?1 OR lower(position) like ?1)",
            searchPattern)
        .page(0, limit)
        .list();
  }

  /**
   * Extended full-text search across ALL contact fields. Searches in: firstName, lastName, email,
   * phone, mobile, position, department
   *
   * @param query the search query
   * @param limit maximum number of results
   * @return list of matching contacts
   */
  public List<CustomerContact> searchContactsFullText(String query, int limit) {
    String searchPattern = "%" + query.toLowerCase() + "%";
    return find(
            """
        isActive = true AND (
            lower(firstName) like ?1 OR
            lower(lastName) like ?1 OR
            lower(firstName || ' ' || lastName) like ?1 OR
            lower(email) like ?1 OR
            lower(phone) like ?1 OR
            lower(mobile) like ?1 OR
            lower(position) like ?1 OR
            lower(department) like ?1
        )
        """,
            searchPattern)
        .page(0, limit)
        .list();
  }

  /**
   * Find contacts by email address (exact match, case-insensitive).
   *
   * @param email the email address
   * @param limit maximum number of results
   * @return list of matching contacts
   */
  public List<CustomerContact> findByEmail(String email, int limit) {
    return find("isActive = true AND lower(email) = lower(?1)", email).page(0, limit).list();
  }

  /**
   * Find contacts by phone number (partial match).
   *
   * @param phone the phone number pattern
   * @param limit maximum number of results
   * @return list of matching contacts
   */
  public List<CustomerContact> findByPhoneLike(String phone, int limit) {
    return find("isActive = true AND (phone like ?1 OR mobile like ?1)", phone)
        .page(0, limit)
        .list();
  }

  /**
   * Find contacts by phone OR mobile number with normalization. Removes all non-numeric characters
   * except + for comparison.
   *
   * @param phone the phone number to search (will be normalized)
   * @param limit maximum number of results
   * @return list of matching contacts
   */
  public List<CustomerContact> findByPhoneOrMobile(String phone, int limit) {
    // Normalize the search phone number (keep only digits and +)
    String normalizedPhone = phone.replaceAll("[^0-9+]", "");
    String searchPattern = "%" + normalizedPhone + "%";

    // Note: REGEXP_REPLACE is PostgreSQL specific
    // For H2 in tests, we'd need a different approach
    String dbProduct =
        getEntityManager()
            .getEntityManagerFactory()
            .getProperties()
            .get("javax.persistence.database-product-name")
            .toString();

    if (dbProduct != null && dbProduct.toLowerCase().contains("h2")) {
      // H2 Database (for tests) - simple LIKE without normalization
      return find("isActive = true AND (phone like ?1 OR mobile like ?1)", searchPattern)
          .page(0, limit)
          .list();
    } else {
      // PostgreSQL - use REGEXP_REPLACE for normalization
      return getEntityManager()
          .createQuery(
              """
              SELECT c FROM CustomerContact c
              WHERE c.isActive = true
              AND (
                  REGEXP_REPLACE(c.phone, '[^0-9+]', '', 'g') LIKE :phone OR
                  REGEXP_REPLACE(c.mobile, '[^0-9+]', '', 'g') LIKE :phone
              )
              """,
              CustomerContact.class)
          .setParameter("phone", searchPattern)
          .setMaxResults(limit)
          .getResultList();
    }
  }

  // ========== EXISTING METHODS ==========

  /**
   * Find contacts assigned to a specific location (legacy single-location)
   *
   * @param locationId the location ID
   * @return list of contacts assigned to the location
   * @deprecated Use findByLocationIdMulti for multi-location support
   */
  @Deprecated
  public List<CustomerContact> findByLocationId(UUID locationId) {
    return find(
            "assignedLocation.id = ?1 and isActive = true order by lastName, firstName", locationId)
        .list();
  }

  // ========== SPRINT 2.1.7.7: MULTI-LOCATION QUERIES ==========

  /**
   * Find contacts assigned to a specific location (Multi-Location aware). Includes: - Contacts with
   * responsibilityScope = 'ALL' - Contacts with responsibilityScope = 'SPECIFIC' AND location in
   * assignedLocations
   *
   * @param locationId the location ID
   * @return list of contacts responsible for this location
   */
  public List<CustomerContact> findByLocationIdMulti(UUID locationId) {
    return getEntityManager()
        .createQuery(
            """
            SELECT DISTINCT c FROM CustomerContact c
            LEFT JOIN c.assignedLocations al
            WHERE c.isActive = true
            AND c.customer.id = (SELECT loc.customer.id FROM CustomerLocation loc WHERE loc.id = :locationId)
            AND (
                c.responsibilityScope = 'ALL'
                OR (c.responsibilityScope = 'SPECIFIC' AND al.id = :locationId)
            )
            ORDER BY c.lastName, c.firstName
            """,
            CustomerContact.class)
        .setParameter("locationId", locationId)
        .getResultList();
  }

  /**
   * Find contacts by customer that have specific location assignments.
   *
   * @param customerId the customer ID
   * @param responsibilityScope filter by scope ('ALL' or 'SPECIFIC'), null for all
   * @return list of contacts
   */
  public List<CustomerContact> findByCustomerIdAndScope(
      UUID customerId, String responsibilityScope) {
    if (responsibilityScope == null) {
      return findByCustomerId(customerId);
    }
    return find(
            "customer.id = ?1 and isActive = true and responsibilityScope = ?2 order by isPrimary desc, lastName, firstName",
            customerId,
            responsibilityScope)
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

    // Flush to ensure the update is executed
    flush();

    // Then set the new primary contact
    update("isPrimary = true where id = ?1 and customer.id = ?2", contactId, customerId);

    // Flush again to ensure changes are persisted
    flush();
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
  public List<CustomerContact> findByEmail(String email) {
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
   * Find contacts with upcoming birthdays NOTE: Birthday field not yet implemented in
   * CustomerContact entity
   *
   * @param daysAhead number of days to look ahead
   * @return list of contacts with birthdays in the next N days
   */
  public List<CustomerContact> findUpcomingBirthdays(int daysAhead) {
    // TODO: Implement when birthday field is added to CustomerContact entity
    // For now, return empty list
    return new ArrayList<>();

    /* Original implementation for future reference:
    // Calculate date range in Java to avoid database-specific syntax
    LocalDate today = LocalDate.now();
    LocalDate endDate = today.plusDays(daysAhead);

    // Get all active contacts with birthdays
    List<CustomerContact> contactsWithBirthdays = list("isActive = true AND birthday IS NOT NULL");

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
    */
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

package de.freshplan.domain.customer.repository;

import de.freshplan.domain.customer.constants.CustomerConstants;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerLifecycleStage;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.shared.BusinessType;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Customer entity with comprehensive query methods and soft delete support.
 * Implements all query methods required by the CRM Master Plan.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class CustomerRepository implements PanacheRepositoryBase<Customer, UUID> {

  // ========== SOFT DELETE SUPPORT ==========

  /** Find customer by ID, only active (non-deleted) customers. */
  public Optional<Customer> findByIdActive(UUID id) {
    return find("id = ?1 AND isDeleted = false", id).firstResultOptional();
  }

  /** Find all active customers with pagination. */
  public List<Customer> findAllActive(Page page) {
    return find("isDeleted = false", Sort.by("companyName").ascending()).page(page).list();
  }

  /** Count all active customers. */
  public long countActive() {
    return count("isDeleted = false");
  }

  /** Find deleted customers (for admin/audit purposes). */
  public List<Customer> findDeleted(Page page) {
    return find("isDeleted = true", Sort.by("deletedAt").descending()).page(page).list();
  }

  // ========== SEARCH QUERIES (FC-005 PR4) ==========

  /** Full text search across multiple fields. */
  public List<Customer> searchFullText(String query, int limit) {
    String searchPattern = "%" + query.toLowerCase() + "%";
    return find(
            "isDeleted = false AND (lower(companyName) like ?1 OR lower(tradingName) like ?1 "
                + "OR customerNumber like ?1)",
            searchPattern)
        .page(0, limit)
        .list();
  }

  /** Quick search for autocomplete (only company name and customer number). */
  public List<Customer> quickSearch(String query, int limit) {
    String searchPattern = "%" + query.toLowerCase() + "%";
    return find(
            "isDeleted = false AND (lower(companyName) like ?1 OR customerNumber like ?1)",
            searchPattern)
        .page(0, limit)
        .list();
  }

  /** Find customers by contact email - searches in contacts. */
  public List<Customer> findByContactEmail(String email, int limit) {
    // Since Customer doesn't have email field, return empty for now
    // TODO: Join with contacts table to search contact emails
    return new ArrayList<>();
  }

  /** Find customers by phone number - searches in contacts. */
  public List<Customer> findByPhone(String phone, int limit) {
    // Since Customer doesn't have phone field, return empty for now
    // TODO: Join with contacts table to search contact phones
    return new ArrayList<>();
  }

  /** Find customers by customer number (prefix search). */
  public List<Customer> findByCustomerNumberLike(String pattern, int limit) {
    return find("isDeleted = false AND customerNumber LIKE ?1", pattern).page(0, limit).list();
  }

  // ========== EXPORT QUERIES ==========

  /** Find customers by filters for export. */
  public List<Customer> findByFilters(List<String> status, String businessType) {
    StringBuilder query = new StringBuilder("isDeleted = false");

    if (status != null && !status.isEmpty()) {
      query.append(" AND status IN ?1");
      if (businessType != null) {
        query.append(" AND businessType = ?2");
        try {
          BusinessType businessTypeEnum = BusinessType.valueOf(businessType);
          return find(query.toString(), status, businessTypeEnum).list();
        } catch (IllegalArgumentException e) {
          // Invalid businessType - return empty list
          return List.of();
        }
      }
      return find(query.toString(), status).list();
    }

    if (businessType != null) {
      query.append(" AND businessType = ?1");
      try {
        BusinessType businessTypeEnum = BusinessType.valueOf(businessType);
        return find(query.toString(), businessTypeEnum).list();
      } catch (IllegalArgumentException e) {
        // Invalid businessType - return empty list
        return List.of();
      }
    }

    return find(query.toString()).list();
  }

  /** Find customers by filters with pagination. */
  public List<Customer> findByFilters(
      List<String> status, String businessType, int page, int size) {
    StringBuilder query = new StringBuilder("isDeleted = false");

    if (status != null && !status.isEmpty()) {
      query.append(" AND status IN ?1");
      if (businessType != null) {
        query.append(" AND businessType = ?2");
        try {
          BusinessType businessTypeEnum = BusinessType.valueOf(businessType);
          return find(query.toString(), status, businessTypeEnum).page(Page.of(page, size)).list();
        } catch (IllegalArgumentException e) {
          // Invalid businessType - return empty list
          return List.of();
        }
      }
      return find(query.toString(), status).page(Page.of(page, size)).list();
    }

    if (businessType != null) {
      query.append(" AND businessType = ?1");
      try {
        BusinessType businessTypeEnum = BusinessType.valueOf(businessType);
        return find(query.toString(), businessTypeEnum).page(Page.of(page, size)).list();
      } catch (IllegalArgumentException e) {
        // Invalid businessType - return empty list
        return List.of();
      }
    }

    return find(query.toString()).page(Page.of(page, size)).list();
  }

  /** Count customers by filters. */
  public long countByFilters(List<String> status, String businessType) {
    StringBuilder query = new StringBuilder("isDeleted = false");

    if (status != null && !status.isEmpty()) {
      query.append(" AND status IN ?1");
      if (businessType != null) {
        query.append(" AND businessType = ?2");
        return count(query.toString(), status, businessType);
      }
      return count(query.toString(), status);
    }

    if (businessType != null) {
      query.append(" AND businessType = ?1");
      return count(query.toString(), businessType);
    }

    return count(query.toString());
  }

  // ========== RISK CUSTOMER QUERIES ==========

  /**
   * Findet alle aktiven Kunden ohne kürzlichen Kontakt (Risiko-Kunden).
   *
   * @param thresholdDate Der Schwellwert für den letzten Kontakt
   * @return Liste von Kunden ohne Kontakt seit dem Schwellwert
   */
  public List<Customer> findActiveCustomersWithoutRecentContact(LocalDateTime thresholdDate) {
    String query = "status = ?1 AND (lastContactDate IS NULL OR lastContactDate < ?2)";
    return find(query, CustomerStatus.AKTIV, thresholdDate).list();
  }

  /**
   * Zählt alle aktiven Kunden ohne kürzlichen Kontakt (Risiko-Kunden).
   *
   * @param thresholdDate Der Schwellwert für den letzten Kontakt
   * @return Anzahl der Kunden ohne Kontakt seit dem Schwellwert
   */
  public long countActiveCustomersWithoutRecentContact(LocalDateTime thresholdDate) {
    String query = "status = ?1 AND (lastContactDate IS NULL OR lastContactDate < ?2)";
    return count(query, CustomerStatus.AKTIV, thresholdDate);
  }

  // ========== UNIQUE CONSTRAINTS ==========

  /** Find customer by customer number (active only). */
  public Optional<Customer> findByCustomerNumber(String customerNumber) {
    if (customerNumber == null || customerNumber.isBlank()) {
      return Optional.empty();
    }
    return find("customerNumber = ?1 AND isDeleted = false", customerNumber).firstResultOptional();
  }

  /** Check if customer number exists (active only). */
  public boolean existsByCustomerNumber(String customerNumber) {
    if (customerNumber == null || customerNumber.isBlank()) {
      return false;
    }
    return count("customerNumber = ?1 AND isDeleted = false", customerNumber) > 0;
  }

  // ========== SEARCH & FILTERING ==========

  // Note: Search functionality has been moved to CustomerQueryBuilder
  // which provides more flexible and performant search capabilities

  /** Find customers by status. */
  public List<Customer> findByStatus(CustomerStatus status, Page page) {
    return find("status = ?1 AND isDeleted = false", status).page(page).list();
  }

  /** Find customers by multiple statuses. */
  public List<Customer> findByStatusIn(List<CustomerStatus> statuses, Page page) {
    if (statuses == null || statuses.isEmpty()) {
      return List.of();
    }
    return find("status IN ?1 AND isDeleted = false", statuses).page(page).list();
  }

  /** Find customers by lifecycle stage. */
  public List<Customer> findByLifecycleStage(CustomerLifecycleStage stage, Page page) {
    return find("lifecycleStage = ?1 AND isDeleted = false", stage).page(page).list();
  }

  // ========== HIERARCHY SUPPORT ==========

  /** Find all children of a parent customer. */
  public List<Customer> findChildren(UUID parentId) {
    return find("parentCustomer.id = ?1 AND isDeleted = false", parentId).list();
  }

  /** Find root customers (no parent). */
  public List<Customer> findRootCustomers(Page page) {
    return find("parentCustomer IS NULL AND isDeleted = false").page(page).list();
  }

  /** Check if customer has children. */
  public boolean hasChildren(UUID customerId) {
    return count("parentCustomer.id = ?1 AND isDeleted = false", customerId) > 0;
  }

  // ========== RISK MANAGEMENT ==========

  /** Find customers at risk (high risk score). */
  public List<Customer> findAtRisk(int minRiskScore, Page page) {
    return find("riskScore >= ?1 AND isDeleted = false", minRiskScore).page(page).list();
  }

  /** Find customers with overdue follow-ups. */
  public List<Customer> findOverdueFollowUps(Page page) {
    return find("nextFollowUpDate < ?1 AND isDeleted = false", LocalDateTime.now())
        .page(page)
        .list();
  }

  /** Find customers not contacted for specified days. */
  public List<Customer> findNotContactedSince(int days, Page page) {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
    return find(
            """
                (lastContactDate IS NULL OR lastContactDate < ?1)
                AND isDeleted = false
                """,
            cutoffDate)
        .page(page)
        .list();
  }

  // ========== FINANCIAL QUERIES ==========

  /** Find customers by expected annual volume range. */
  public List<Customer> findByExpectedVolumeRange(
      BigDecimal minVolume, BigDecimal maxVolume, Page page) {

    if (minVolume != null && maxVolume != null) {
      return find(
              """
                    expectedAnnualVolume >= ?1
                    AND expectedAnnualVolume <= ?2
                    AND isDeleted = false
                    """,
              minVolume,
              maxVolume)
          .page(page)
          .list();
    } else if (minVolume != null) {
      return find("expectedAnnualVolume >= ?1 AND isDeleted = false", minVolume).page(page).list();
    } else if (maxVolume != null) {
      return find("expectedAnnualVolume <= ?1 AND isDeleted = false", maxVolume).page(page).list();
    }

    return findAllActive(page);
  }

  // ========== DUPLICATE DETECTION ==========

  /**
   * Find potential duplicates by company name similarity. Uses exact match after normalization.
   * Special handling for [TEST] prefix in test data.
   */
  public List<Customer> findPotentialDuplicates(String companyName) {
    if (companyName == null || companyName.isBlank()) {
      return List.of();
    }

    // Only check for exact duplicates (case-insensitive)
    // Don't do fuzzy matching or prefix removal - it causes too many false positives
    return find(
            """
                isDeleted = false
                AND LOWER(companyName) = LOWER(?1)
                """,
            companyName)
        .list();
  }

  // ========== DASHBOARD QUERIES ==========

  /** Count customers by status for dashboard. */
  public long countByStatus(CustomerStatus status) {
    return count("status = ?1 AND isDeleted = false", status);
  }

  /** Count customers by lifecycle stage for dashboard. */
  public long countByLifecycleStage(CustomerLifecycleStage stage) {
    return count("lifecycleStage = ?1 AND isDeleted = false", stage);
  }

  /** Count new customers this month. */
  public long countNewThisMonth() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
    return count("createdAt >= ?1 AND isDeleted = false", monthStart);
  }

  /** Count customers at risk. */
  public long countAtRisk(int minRiskScore) {
    return count("riskScore >= ?1 AND isDeleted = false", minRiskScore);
  }

  /** Count overdue follow-ups. */
  public long countOverdueFollowUps() {
    return count("nextFollowUpDate < ?1 AND isDeleted = false", LocalDateTime.now());
  }

  // ========== RECENT ACTIVITY ==========

  /** Find recently created customers. */
  public List<Customer> findRecentlyCreated(int days, Page page) {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
    return find("createdAt >= ?1 AND isDeleted = false", cutoffDate).page(page).list();
  }

  /** Find recently updated customers. */
  public List<Customer> findRecentlyUpdated(int days, Page page) {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
    return find("updatedAt >= ?1 AND isDeleted = false", cutoffDate).page(page).list();
  }

  // ========== UTILITY METHODS ==========

  /** Get next customer number for CustomerNumberGenerator. Format: KD-YYYY-XXXXX */
  public Integer getMaxCustomerNumberForYear(int year) {
    String yearPrefix =
        CustomerConstants.CUSTOMER_NUMBER_PREFIX
            + CustomerConstants.CUSTOMER_NUMBER_SEPARATOR
            + year
            + CustomerConstants.CUSTOMER_NUMBER_SEPARATOR;

    // Native query to extract number part and find maximum
    String sql =
        """
                SELECT MAX(CAST(SUBSTRING(customer_number, 9) AS INTEGER))
                FROM customers
                WHERE customer_number LIKE ?1
                """;

    Object result =
        getEntityManager()
            .createNativeQuery(sql)
            .setParameter(1, yearPrefix + "%")
            .getSingleResult();

    return result != null ? (Integer) result : null;
  }

  // ========== TEST DATA CLEANUP ==========

  /**
   * Delete all test data. Only deletes: - Customers with is_test_data = true - Customers with
   * customer_number LIKE 'KD-TEST-%' - Customers with company_name LIKE '[TEST%'
   *
   * @return Number of customers deleted
   */
  public long deleteAllTestData() {
    return delete(
        """
        isTestData = true
        OR customerNumber LIKE 'KD-TEST-%'
        OR companyName LIKE '[TEST%'
        """);
  }
}

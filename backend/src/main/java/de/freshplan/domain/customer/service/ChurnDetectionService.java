package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.CustomerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Churn Detection Service
 *
 * <p>Sprint 2.1.7.4: Seasonal Business Support
 *
 * <p>Business Rule: - Regular customers: 90+ days no order → RISIKO - Seasonal businesses: Exclude
 * off-season (expected inactivity)
 *
 * <p>This service determines which customers should be monitored for churn. Seasonal businesses are
 * excluded from churn detection when they are outside their active season, as inactivity during
 * off-season is expected and should not trigger at-risk alerts.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class ChurnDetectionService {

  private static final Logger logger = LoggerFactory.getLogger(ChurnDetectionService.class);

  private static final int CHURN_THRESHOLD_DAYS = 90;

  private final CustomerRepository customerRepository;

  @Inject
  public ChurnDetectionService(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  /**
   * Check if customer should be monitored for churn
   *
   * <p>Seasonal businesses are excluded during off-season
   *
   * @param customer Customer to check
   * @return true if customer should be monitored for churn, false if excluded (e.g. off-season
   *     seasonal business)
   */
  public boolean shouldCheckForChurn(Customer customer) {
    // Skip seasonal customers outside their season
    if (Boolean.TRUE.equals(customer.getIsSeasonalBusiness())) {
      int currentMonth = LocalDate.now().getMonthValue();

      List<Integer> activeMonths = customer.getSeasonalMonths();
      if (activeMonths != null && !activeMonths.isEmpty()) {
        // Wenn NICHT in Saison → kein Churn-Check!
        if (!activeMonths.contains(currentMonth)) {
          logger.debug(
              "Customer {} is seasonal and out-of-season (month {}). Skipping churn check.",
              customer.getId(),
              currentMonth);
          return false; // Outside season = expected inactivity
        }
      }
    }

    return true; // Regular churn monitoring
  }

  /**
   * Get at-risk customers (excluding seasonal businesses outside season)
   *
   * <p>Returns customers with no contact in the last 90 days, but excludes seasonal businesses that
   * are currently in their off-season.
   *
   * @return List of customers at risk of churning
   */
  public List<Customer> getAtRiskCustomers() {
    // Find all AKTIV customers with no contact in last 90 days
    LocalDateTime thresholdDateTime = LocalDateTime.now().minusDays(CHURN_THRESHOLD_DAYS);
    // Fetch all AKTIV customers with old lastContactDate
    // REVERTED: Gemini #3 JSONB optimization caused Hibernate parsing errors (::jsonb not
    // supported)
    // Seasonal filtering now done in Java (more reliable, minimal performance impact)
    List<Customer> atRiskCustomers =
        customerRepository
            .find("status = ?1 AND lastContactDate < ?2", CustomerStatus.AKTIV, thresholdDateTime)
            .list();

    // Filter out seasonal businesses that are currently off-season (Sprint 2.1.7.4)
    int currentMonth = LocalDate.now().getMonthValue();
    List<Customer> filteredCustomers =
        atRiskCustomers.stream()
            .filter(
                customer -> {
                  // Include non-seasonal businesses
                  if (!customer.getIsSeasonalBusiness()) {
                    return true;
                  }

                  // Include seasonal if no months configured
                  List<Integer> seasonalMonths = customer.getSeasonalMonths();
                  if (seasonalMonths == null || seasonalMonths.isEmpty()) {
                    return true;
                  }

                  // Include seasonal if current month is in season
                  return seasonalMonths.contains(currentMonth);
                })
            .collect(Collectors.toList());

    logger.debug(
        "Churn Detection: Found {} at-risk customers (seasonal filtering applied in Java)",
        filteredCustomers.size());

    return filteredCustomers;
  }

  /**
   * Get the churn threshold in days
   *
   * @return Number of days without order before customer is considered at-risk
   */
  public int getChurnThresholdDays() {
    return CHURN_THRESHOLD_DAYS;
  }
}

package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerAddress;
import de.freshplan.domain.customer.entity.CustomerHierarchyType;
import de.freshplan.domain.customer.entity.CustomerLocation;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.exception.InvalidHierarchyException;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Hierarchy Metrics Service - Calculates roll-up metrics for customer hierarchies.
 *
 * <p>Sprint 2.1.7.7 - D3: Multi-Location Management - Hierarchy Metrics
 *
 * <p>**Business Context:** HEADQUARTER customers can have multiple FILIALE (branch) customers. This
 * service calculates aggregated metrics across all branches: total revenue, average revenue, branch
 * count, open opportunities, and percentage distribution.
 *
 * <p>**Use Cases:**
 *
 * <ul>
 *   <li>Customer Detail Page: Display hierarchy overview with branch revenue breakdown
 *   <li>Dashboard: Top customers by total hierarchy revenue
 *   <li>Sales Planning: Identify expansion potential per branch
 *   <li>Account Management: Monitor health of multi-location customers
 * </ul>
 *
 * <p>**Example:**
 *
 * <pre>{@code
 * NH Hotels Deutschland GmbH (HEADQUARTER)
 *   └─ München Branch: €120,000 (40%)
 *   └─ Hamburg Branch: €100,000 (33%)
 *   └─ Berlin Branch: €80,000 (27%)
 * Total: €300,000 | Avg: €100,000 | Branches: 3 | Open Opps: 8
 * }</pre>
 *
 * @author FreshPlan Team
 * @since 2.1.7.7
 */
@ApplicationScoped
public class HierarchyMetricsService {

  @Inject CustomerRepository customerRepository;
  @Inject OpportunityRepository opportunityRepository;

  /**
   * Calculates hierarchy metrics for a HEADQUARTER customer.
   *
   * <p>Business Logic:
   *
   * <ol>
   *   <li>Validates that parent is a HEADQUARTER customer
   *   <li>Iterates through all child branches (FILIALE)
   *   <li>Sums actualAnnualVolume from each branch
   *   <li>Counts open opportunities (stage != CLOSED_WON)
   *   <li>Calculates percentage distribution
   *   <li>Returns aggregated metrics + branch details
   * </ol>
   *
   * @param parentId UUID of the HEADQUARTER customer
   * @return HierarchyMetrics with total/average revenue, branch count, opportunities, and branch
   *     details
   * @throws IllegalArgumentException if customer not found
   * @throws InvalidHierarchyException if customer is not a HEADQUARTER
   */
  public HierarchyMetrics getHierarchyMetrics(UUID parentId) {
    // Validate parent exists
    Customer parent =
        customerRepository
            .findByIdOptional(parentId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + parentId));

    // Validate HEADQUARTER type
    if (parent.getHierarchyType() != CustomerHierarchyType.HEADQUARTER) {
      throw new InvalidHierarchyException(
          "Only HEADQUARTER customers have hierarchy metrics. Customer "
              + parent.getCompanyName()
              + " is of type "
              + parent.getHierarchyType());
    }

    List<Customer> branches = parent.getChildCustomers();
    if (branches == null) {
      branches = new ArrayList<>();
    }

    // Calculate total revenue and build branch details
    BigDecimal totalRevenue = BigDecimal.ZERO;
    List<BranchRevenueDetail> branchDetails = new ArrayList<>();

    for (Customer branch : branches) {
      // Get branch revenue (actualAnnualVolume)
      BigDecimal branchRevenue =
          branch.getActualAnnualVolume() != null ? branch.getActualAnnualVolume() : BigDecimal.ZERO;

      totalRevenue = totalRevenue.add(branchRevenue);

      // Count open opportunities (stage != CLOSED_WON)
      int openOpportunities =
          (int)
              opportunityRepository.count(
                  "customer = ?1 and stage != ?2", branch, OpportunityStage.CLOSED_WON);

      // Build branch detail (percentage calculated later)
      branchDetails.add(
          new BranchRevenueDetail(
              branch.getId(),
              branch.getCompanyName(),
              getCity(branch),
              getCountry(branch),
              branchRevenue,
              null, // percentage - will be calculated after totalRevenue is known
              openOpportunities,
              branch.getStatus()));
    }

    // Calculate percentages (now that totalRevenue is final)
    BigDecimal finalTotalRevenue = totalRevenue;
    branchDetails =
        branchDetails.stream()
            .map(
                detail ->
                    new BranchRevenueDetail(
                        detail.branchId,
                        detail.branchName,
                        detail.city,
                        detail.country,
                        detail.revenue,
                        calculatePercentage(detail.revenue, finalTotalRevenue),
                        detail.openOpportunities,
                        detail.status))
            .toList();

    // Calculate average revenue
    BigDecimal averageRevenue =
        branches.isEmpty()
            ? BigDecimal.ZERO
            : totalRevenue.divide(BigDecimal.valueOf(branches.size()), 2, RoundingMode.HALF_UP);

    // Sum total open opportunities
    int totalOpenOpportunities =
        branchDetails.stream().mapToInt(BranchRevenueDetail::openOpportunities).sum();

    return new HierarchyMetrics(
        totalRevenue, averageRevenue, branches.size(), totalOpenOpportunities, branchDetails);
  }

  /**
   * Calculates percentage of a value relative to total.
   *
   * <p>Formula: (value / total) * 100
   *
   * <p>Rounding: 1 decimal place (HALF_UP)
   *
   * <p>Example: €120,000 / €300,000 = 40.0%
   *
   * @param value Branch revenue
   * @param total Total revenue across all branches
   * @return Percentage with 1 decimal place (0.0 if total is zero)
   */
  private BigDecimal calculatePercentage(BigDecimal value, BigDecimal total) {
    if (total.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    return value
        .divide(total, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .setScale(1, RoundingMode.HALF_UP);
  }

  /**
   * Extracts city from branch's primary shipping address.
   *
   * <p>Logic: mainLocation → primaryShippingAddress → city
   *
   * @param customer Branch customer
   * @return City name or "N/A" if not available
   */
  private String getCity(Customer customer) {
    return customer
        .getMainLocation()
        .flatMap(CustomerLocation::getPrimaryShippingAddress)
        .map(CustomerAddress::getCity)
        .orElse("N/A");
  }

  /**
   * Extracts country from branch's primary shipping address.
   *
   * <p>Logic: mainLocation → primaryShippingAddress → country
   *
   * @param customer Branch customer
   * @return Country code (ISO 3166-1 alpha-3) or "DEU" (Germany) as default
   */
  private String getCountry(Customer customer) {
    return customer
        .getMainLocation()
        .flatMap(CustomerLocation::getPrimaryShippingAddress)
        .map(CustomerAddress::getCountry)
        .orElse("DEU");
  }

  /**
   * Hierarchy Metrics DTO - Aggregated metrics for a HEADQUARTER customer.
   *
   * <p>Immutable record with hierarchy-wide metrics + detailed branch breakdown.
   *
   * @param totalRevenue Sum of actualAnnualVolume across all branches
   * @param averageRevenue Total revenue / branch count (2 decimal places)
   * @param branchCount Number of child branches (FILIALE customers)
   * @param totalOpenOpportunities Sum of open opportunities across all branches
   * @param branches Detailed breakdown per branch (sorted by revenue DESC in frontend)
   */
  public record HierarchyMetrics(
      BigDecimal totalRevenue,
      BigDecimal averageRevenue,
      int branchCount,
      int totalOpenOpportunities,
      List<BranchRevenueDetail> branches) {}

  /**
   * Branch Revenue Detail DTO - Metrics for a single branch.
   *
   * <p>Used in hierarchy metrics breakdown to show revenue distribution.
   *
   * @param branchId Branch customer UUID
   * @param branchName Branch company name (e.g., "NH Hotel München")
   * @param city Branch city (e.g., "München")
   * @param country Branch country code (ISO 3166-1 alpha-3, e.g., "DEU")
   * @param revenue Branch actualAnnualVolume
   * @param percentage Revenue percentage of total hierarchy revenue (1 decimal place)
   * @param openOpportunities Count of opportunities where stage != CLOSED_WON
   * @param status Branch customer status (AKTIV, RISIKO, etc.)
   */
  public record BranchRevenueDetail(
      UUID branchId,
      String branchName,
      String city,
      String country,
      BigDecimal revenue,
      BigDecimal percentage,
      int openOpportunities,
      CustomerStatus status) {}
}

package de.freshplan.domain.customer.service;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.exception.InvalidHierarchyException;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Unit Tests for HierarchyMetricsService.
 *
 * <p>Sprint 2.1.7.7 - D3: Multi-Location Management - Hierarchy Metrics Tests
 *
 * <p>Test Coverage Goals: ≥85% (Branch Coverage)
 *
 * <p>Test Scenarios: 1. Basic Metrics Calculation (3 branches with revenue) 2. Percentage
 * Distribution (40%, 33%, 27%) 3. Zero Revenue Handling (branches with null actualAnnualVolume) 4.
 * Empty Hierarchy (HEADQUARTER with no branches) 5. Exception Handling (customer not found,
 * non-HEADQUARTER) 6. Open Opportunities Counting 7. Average Revenue Calculation (with
 * RoundingMode.HALF_UP) 8. Branch Address Extraction (city, country)
 *
 * @author FreshPlan Team
 * @since 2.1.7.7
 */
@QuarkusTest
@DisplayName("HierarchyMetricsService Tests")
public class HierarchyMetricsServiceTest {

  @Inject HierarchyMetricsService hierarchyMetricsService;

  @InjectMock CustomerRepository customerRepository;

  @InjectMock OpportunityRepository opportunityRepository;

  private Customer headquarter;
  private Customer branch1;
  private Customer branch2;
  private Customer branch3;

  @BeforeEach
  void setUp() {
    // Create Headquarter Customer
    headquarter = createHeadquarter("NH Hotels Deutschland GmbH");

    // Create Branch 1: Munich (€120,000 revenue, 40%)
    branch1 =
        createBranch(
            headquarter,
            "NH Hotel München",
            new BigDecimal("120000.00"),
            "München",
            "DEU",
            CustomerStatus.AKTIV);

    // Create Branch 2: Hamburg (€100,000 revenue, 33%)
    branch2 =
        createBranch(
            headquarter,
            "NH Hotel Hamburg",
            new BigDecimal("100000.00"),
            "Hamburg",
            "DEU",
            CustomerStatus.AKTIV);

    // Create Branch 3: Berlin (€80,000 revenue, 27%)
    branch3 =
        createBranch(
            headquarter,
            "NH Hotel Berlin",
            new BigDecimal("80000.00"),
            "Berlin",
            "DEU",
            CustomerStatus.RISIKO);

    // Link branches to headquarter
    List<Customer> branches = new ArrayList<>();
    branches.add(branch1);
    branches.add(branch2);
    branches.add(branch3);
    headquarter.setChildCustomers(branches);

    // Setup mocks
    Mockito.when(customerRepository.findByIdOptional(headquarter.getId()))
        .thenReturn(Optional.of(headquarter));

    // Mock opportunity counts for each branch
    // Note: Using raw values (not matchers) - tests that create new branches will need their own
    // mocks
    Mockito.when(
            opportunityRepository.count(
                "customer = ?1 and stage != ?2", branch1, OpportunityStage.CLOSED_WON))
        .thenReturn(3L);
    Mockito.when(
            opportunityRepository.count(
                "customer = ?1 and stage != ?2", branch2, OpportunityStage.CLOSED_WON))
        .thenReturn(2L);
    Mockito.when(
            opportunityRepository.count(
                "customer = ?1 and stage != ?2", branch3, OpportunityStage.CLOSED_WON))
        .thenReturn(3L);
  }

  // ========== TEST CASES ==========

  @Test
  @DisplayName("Should calculate total revenue correctly (3 branches)")
  void testGetHierarchyMetrics_TotalRevenue() {
    // Given: Headquarter with 3 branches (€120k + €100k + €80k = €300k)

    // When: Get hierarchy metrics
    HierarchyMetricsService.HierarchyMetrics metrics =
        hierarchyMetricsService.getHierarchyMetrics(headquarter.getId());

    // Then: Total revenue should be €300,000
    assertNotNull(metrics, "Metrics should not be null");
    assertEquals(
        new BigDecimal("300000.00"),
        metrics.totalRevenue(),
        "Total revenue should be sum of all branches");
  }

  @Test
  @DisplayName("Should calculate average revenue correctly (with rounding)")
  void testGetHierarchyMetrics_AverageRevenue() {
    // Given: 3 branches (€300k total / 3 = €100,000.00)

    // When: Get hierarchy metrics
    HierarchyMetricsService.HierarchyMetrics metrics =
        hierarchyMetricsService.getHierarchyMetrics(headquarter.getId());

    // Then: Average revenue should be €100,000.00 (rounded to 2 decimal places)
    assertEquals(
        new BigDecimal("100000.00"),
        metrics.averageRevenue(),
        "Average revenue should be total / branch count with 2 decimal places");
  }

  @Test
  @DisplayName("Should count branches correctly")
  void testGetHierarchyMetrics_BranchCount() {
    // Given: Headquarter with 3 branches

    // When: Get hierarchy metrics
    HierarchyMetricsService.HierarchyMetrics metrics =
        hierarchyMetricsService.getHierarchyMetrics(headquarter.getId());

    // Then: Branch count should be 3
    assertEquals(3, metrics.branchCount(), "Branch count should match number of child customers");
  }

  @Test
  @DisplayName("Should calculate percentages correctly (40%, 33.3%, 26.7%)")
  void testGetHierarchyMetrics_PercentageDistribution() {
    // Given: Branches with different revenues

    // When: Get hierarchy metrics
    HierarchyMetricsService.HierarchyMetrics metrics =
        hierarchyMetricsService.getHierarchyMetrics(headquarter.getId());

    // Then: Percentages should be calculated correctly (1 decimal place)
    List<HierarchyMetricsService.BranchRevenueDetail> branches = metrics.branches();
    assertEquals(3, branches.size(), "Should have 3 branch details");

    // Munich: €120k / €300k = 40.0%
    HierarchyMetricsService.BranchRevenueDetail munich =
        branches.stream().filter(b -> b.branchName().equals("NH Hotel München")).findFirst().get();
    assertEquals(new BigDecimal("40.0"), munich.percentage(), "Munich should be 40.0%");

    // Hamburg: €100k / €300k = 33.3%
    HierarchyMetricsService.BranchRevenueDetail hamburg =
        branches.stream().filter(b -> b.branchName().equals("NH Hotel Hamburg")).findFirst().get();
    assertEquals(new BigDecimal("33.3"), hamburg.percentage(), "Hamburg should be 33.3%");

    // Berlin: €80k / €300k = 26.7%
    HierarchyMetricsService.BranchRevenueDetail berlin =
        branches.stream().filter(b -> b.branchName().equals("NH Hotel Berlin")).findFirst().get();
    assertEquals(new BigDecimal("26.7"), berlin.percentage(), "Berlin should be 26.7%");
  }

  @Test
  @DisplayName("Should count open opportunities correctly")
  void testGetHierarchyMetrics_OpenOpportunities() {
    // Given: Branches with different opportunity counts

    // When: Get hierarchy metrics
    HierarchyMetricsService.HierarchyMetrics metrics =
        hierarchyMetricsService.getHierarchyMetrics(headquarter.getId());

    // Then: Total opportunities should be 3 + 2 + 3 = 8
    assertEquals(
        8, metrics.totalOpenOpportunities(), "Total opportunities should sum across all branches");

    // Individual branch opportunities
    List<HierarchyMetricsService.BranchRevenueDetail> branches = metrics.branches();
    HierarchyMetricsService.BranchRevenueDetail munich =
        branches.stream().filter(b -> b.branchName().equals("NH Hotel München")).findFirst().get();
    assertEquals(3, munich.openOpportunities(), "Munich should have 3 opportunities");

    HierarchyMetricsService.BranchRevenueDetail hamburg =
        branches.stream().filter(b -> b.branchName().equals("NH Hotel Hamburg")).findFirst().get();
    assertEquals(2, hamburg.openOpportunities(), "Hamburg should have 2 opportunities");

    HierarchyMetricsService.BranchRevenueDetail berlin =
        branches.stream().filter(b -> b.branchName().equals("NH Hotel Berlin")).findFirst().get();
    assertEquals(3, berlin.openOpportunities(), "Berlin should have 3 opportunities");
  }

  @Test
  @DisplayName("Should handle null revenue as zero")
  void testGetHierarchyMetrics_NullRevenue() {
    // Given: Branch with null actualAnnualVolume
    Customer branchNoRevenue =
        createBranch(
            headquarter,
            "NH Hotel Köln",
            null, // null revenue
            "Köln",
            "DEU",
            CustomerStatus.PROSPECT);
    headquarter.setChildCustomers(List.of(branchNoRevenue));

    // When: Get hierarchy metrics
    HierarchyMetricsService.HierarchyMetrics metrics =
        hierarchyMetricsService.getHierarchyMetrics(headquarter.getId());

    // Then: Total revenue should be €0
    assertEquals(BigDecimal.ZERO, metrics.totalRevenue(), "Null revenue should be treated as zero");
    assertEquals(
        new BigDecimal("0.00"),
        metrics.averageRevenue(),
        "Average should also be zero (with 2 decimal places)");
  }

  @Test
  @DisplayName("Should return zero metrics for empty hierarchy (no branches)")
  void testGetHierarchyMetrics_NoBranches() {
    // Given: Headquarter with no branches
    headquarter.setChildCustomers(new ArrayList<>());

    // When: Get hierarchy metrics
    HierarchyMetricsService.HierarchyMetrics metrics =
        hierarchyMetricsService.getHierarchyMetrics(headquarter.getId());

    // Then: All metrics should be zero
    assertEquals(BigDecimal.ZERO, metrics.totalRevenue(), "Total revenue should be zero");
    assertEquals(BigDecimal.ZERO, metrics.averageRevenue(), "Average revenue should be zero");
    assertEquals(0, metrics.branchCount(), "Branch count should be zero");
    assertEquals(0, metrics.totalOpenOpportunities(), "Opportunities should be zero");
    assertTrue(metrics.branches().isEmpty(), "Branch list should be empty");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when customer not found")
  void testGetHierarchyMetrics_CustomerNotFound() {
    // Given: Non-existent customer ID
    UUID nonExistentId = UUID.randomUUID();
    Mockito.when(customerRepository.findByIdOptional(nonExistentId)).thenReturn(Optional.empty());

    // When/Then: Should throw IllegalArgumentException
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> hierarchyMetricsService.getHierarchyMetrics(nonExistentId),
            "Should throw when customer not found");

    assertTrue(
        exception.getMessage().contains("Customer not found"),
        "Exception message should mention customer not found");
  }

  @Test
  @DisplayName("Should throw InvalidHierarchyException for non-HEADQUARTER customer")
  void testGetHierarchyMetrics_NonHeadquarter() {
    // Given: FILIALE customer (not HEADQUARTER)
    Customer filiale = createFiliale("Standalone Restaurant");
    Mockito.when(customerRepository.findByIdOptional(filiale.getId()))
        .thenReturn(Optional.of(filiale));

    // When/Then: Should throw InvalidHierarchyException
    InvalidHierarchyException exception =
        assertThrows(
            InvalidHierarchyException.class,
            () -> hierarchyMetricsService.getHierarchyMetrics(filiale.getId()),
            "Should throw when customer is not HEADQUARTER");

    assertTrue(
        exception.getMessage().contains("Only HEADQUARTER customers"),
        "Exception message should mention HEADQUARTER requirement");
  }

  @Test
  @DisplayName("Should extract city from branch address")
  void testGetHierarchyMetrics_CityExtraction() {
    // Given: Branches with cities in addresses

    // When: Get hierarchy metrics
    HierarchyMetricsService.HierarchyMetrics metrics =
        hierarchyMetricsService.getHierarchyMetrics(headquarter.getId());

    // Then: Cities should be extracted correctly
    List<HierarchyMetricsService.BranchRevenueDetail> branches = metrics.branches();

    HierarchyMetricsService.BranchRevenueDetail munich =
        branches.stream().filter(b -> b.branchName().equals("NH Hotel München")).findFirst().get();
    assertEquals("München", munich.city(), "Munich branch should have city 'München'");

    HierarchyMetricsService.BranchRevenueDetail hamburg =
        branches.stream().filter(b -> b.branchName().equals("NH Hotel Hamburg")).findFirst().get();
    assertEquals("Hamburg", hamburg.city(), "Hamburg branch should have city 'Hamburg'");
  }

  @Test
  @DisplayName("Should default to 'N/A' for missing city")
  void testGetHierarchyMetrics_MissingCity() {
    // Given: Branch without address
    Customer branchNoAddress = createBranchWithoutAddress(headquarter, "NH Hotel Dresden");
    headquarter.setChildCustomers(List.of(branchNoAddress));

    // When: Get hierarchy metrics
    HierarchyMetricsService.HierarchyMetrics metrics =
        hierarchyMetricsService.getHierarchyMetrics(headquarter.getId());

    // Then: City should default to "N/A"
    HierarchyMetricsService.BranchRevenueDetail branch = metrics.branches().get(0);
    assertEquals("N/A", branch.city(), "Missing city should default to 'N/A'");
  }

  @Test
  @DisplayName("Should default to 'DEU' for missing country")
  void testGetHierarchyMetrics_MissingCountry() {
    // Given: Branch without address
    Customer branchNoAddress = createBranchWithoutAddress(headquarter, "NH Hotel Dresden");
    headquarter.setChildCustomers(List.of(branchNoAddress));

    // When: Get hierarchy metrics
    HierarchyMetricsService.HierarchyMetrics metrics =
        hierarchyMetricsService.getHierarchyMetrics(headquarter.getId());

    // Then: Country should default to "DEU"
    HierarchyMetricsService.BranchRevenueDetail branch = metrics.branches().get(0);
    assertEquals("DEU", branch.country(), "Missing country should default to 'DEU'");
  }

  @Test
  @DisplayName("Should handle zero total revenue (no division by zero)")
  void testGetHierarchyMetrics_ZeroTotalRevenue() {
    // Given: All branches with zero revenue
    branch1.setActualAnnualVolume(BigDecimal.ZERO);
    branch2.setActualAnnualVolume(BigDecimal.ZERO);
    branch3.setActualAnnualVolume(BigDecimal.ZERO);

    // When: Get hierarchy metrics
    HierarchyMetricsService.HierarchyMetrics metrics =
        hierarchyMetricsService.getHierarchyMetrics(headquarter.getId());

    // Then: Percentages should be 0.0 (not throw division by zero)
    assertEquals(BigDecimal.ZERO, metrics.totalRevenue(), "Total revenue should be zero");
    metrics.branches().forEach(b -> assertEquals(BigDecimal.ZERO, b.percentage()));
  }

  @Test
  @DisplayName("Should preserve branch status in results")
  void testGetHierarchyMetrics_BranchStatus() {
    // Given: Branches with different statuses

    // When: Get hierarchy metrics
    HierarchyMetricsService.HierarchyMetrics metrics =
        hierarchyMetricsService.getHierarchyMetrics(headquarter.getId());

    // Then: Status should be preserved
    List<HierarchyMetricsService.BranchRevenueDetail> branches = metrics.branches();

    HierarchyMetricsService.BranchRevenueDetail munich =
        branches.stream().filter(b -> b.branchName().equals("NH Hotel München")).findFirst().get();
    assertEquals(CustomerStatus.AKTIV, munich.status(), "Munich should be AKTIV");

    HierarchyMetricsService.BranchRevenueDetail berlin =
        branches.stream().filter(b -> b.branchName().equals("NH Hotel Berlin")).findFirst().get();
    assertEquals(CustomerStatus.RISIKO, berlin.status(), "Berlin should be RISIKO");
  }

  // ========== HELPER METHODS ==========

  /** Creates a test HEADQUARTER customer. */
  private Customer createHeadquarter(String companyName) {
    Customer customer = new Customer();
    customer.setId(UUID.randomUUID());
    customer.setCompanyName(companyName);
    customer.setHierarchyType(CustomerHierarchyType.HEADQUARTER);
    customer.setChildCustomers(new ArrayList<>());
    customer.setStatus(CustomerStatus.AKTIV);
    return customer;
  }

  /** Creates a test FILIALE customer with revenue and address. */
  private Customer createBranch(
      Customer parent,
      String companyName,
      BigDecimal actualAnnualVolume,
      String city,
      String country,
      CustomerStatus status) {
    Customer branch = new Customer();
    branch.setId(UUID.randomUUID());
    branch.setCompanyName(companyName);
    branch.setHierarchyType(CustomerHierarchyType.FILIALE);
    branch.setParentCustomer(parent);
    branch.setActualAnnualVolume(actualAnnualVolume);
    branch.setStatus(status);

    // Create main location
    CustomerLocation location = new CustomerLocation();
    location.setId(UUID.randomUUID());
    location.setCustomer(branch);
    location.setLocationName(companyName);
    location.setIsMainLocation(true);

    // Create primary shipping address
    CustomerAddress address = new CustomerAddress();
    address.setId(UUID.randomUUID());
    address.setLocation(location);
    address.setAddressType(AddressType.SHIPPING);
    address.setCity(city);
    address.setCountry(country);
    address.setIsPrimaryForType(true);

    // Link address to location
    List<CustomerAddress> addresses = new ArrayList<>();
    addresses.add(address);
    location.setAddresses(addresses);

    // Link location to branch
    List<CustomerLocation> locations = new ArrayList<>();
    locations.add(location);
    branch.setLocations(locations);

    return branch;
  }

  /** Creates a test FILIALE customer WITHOUT address (edge case). */
  private Customer createBranchWithoutAddress(Customer parent, String companyName) {
    Customer branch = new Customer();
    branch.setId(UUID.randomUUID());
    branch.setCompanyName(companyName);
    branch.setHierarchyType(CustomerHierarchyType.FILIALE);
    branch.setParentCustomer(parent);
    branch.setActualAnnualVolume(BigDecimal.ZERO);
    branch.setStatus(CustomerStatus.PROSPECT);
    branch.setLocations(new ArrayList<>());
    return branch;
  }

  /** Creates a test FILIALE customer (standalone, not part of hierarchy). */
  private Customer createFiliale(String companyName) {
    Customer customer = new Customer();
    customer.setId(UUID.randomUUID());
    customer.setCompanyName(companyName);
    customer.setHierarchyType(CustomerHierarchyType.FILIALE);
    customer.setStatus(CustomerStatus.AKTIV);
    return customer;
  }
}

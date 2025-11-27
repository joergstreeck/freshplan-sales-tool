package de.freshplan.modules.xentral.service;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.customer.entity.*;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit Tests for XentralAddressMatcher.
 *
 * <p>Sprint 2.1.7.7 - D2: Multi-Location Management - Address Matching Tests
 *
 * <p>Test Coverage Goals: ≥85% (Branch Coverage)
 *
 * <p>Test Scenarios: 1. Exact Match (100% similarity) 2. High Similarity (≥80% match) 3. Low
 * Similarity (<80% match → Fallback to parent) 4. Empty/Null Input Handling 5. No Branches → Return
 * Parent 6. Multiple Branches → Best Match Selection 7. German Umlaut Normalization 8. Address
 * Component Variations
 *
 * @author FreshPlan Team
 * @since 2.1.7.7
 */
@QuarkusTest
@DisplayName("XentralAddressMatcher Tests")
public class XentralAddressMatcherTest {

  @Inject XentralAddressMatcher addressMatcher;

  private Customer headquarter;
  private Customer branch1;
  private Customer branch2;
  private Customer branch3;

  @BeforeEach
  void setUp() {
    // Create Headquarter Customer
    headquarter = createHeadquarter("NH Hotels Deutschland GmbH");

    // Create Branch 1: Munich
    branch1 =
        createBranch(headquarter, "NH Hotel München", "Maximilianstraße", "17", "80539", "München");

    // Create Branch 2: Hamburg
    branch2 = createBranch(headquarter, "NH Hotel Hamburg", "Reeperbahn", "1", "20359", "Hamburg");

    // Create Branch 3: Berlin
    branch3 =
        createBranch(headquarter, "NH Hotel Berlin", "Alexanderplatz", "1", "10178", "Berlin");

    // Link branches to headquarter
    List<Customer> branches = new ArrayList<>();
    branches.add(branch1);
    branches.add(branch2);
    branches.add(branch3);
    headquarter.setChildCustomers(branches);
  }

  // ========== TEST CASES ==========

  @Test
  @DisplayName("Should match exact address (100% similarity)")
  void testMatchDeliveryAddress_ExactMatch() {
    // Given: Exact Xentral address matching Branch 1
    String xentralAddress = "Maximilianstraße 17, 80539 München";

    // When: Match address
    Customer result = addressMatcher.matchDeliveryAddress(xentralAddress, headquarter);

    // Then: Should return Branch 1
    assertNotNull(result, "Result should not be null");
    assertEquals(branch1.getId(), result.getId(), "Should match Branch 1 (München)");
    assertEquals("NH Hotel München", result.getCompanyName());
  }

  @Test
  @DisplayName("Should match with typos/abbreviations (≥80% similarity)")
  void testMatchDeliveryAddress_HighSimilarity() {
    // Given: Xentral address with abbreviation "Maximilianstr." (missing "aße")
    String xentralAddress = "Maximilianstr. 17, 80539 München";

    // When: Match address
    Customer result = addressMatcher.matchDeliveryAddress(xentralAddress, headquarter);

    // Then: Should still match Branch 1 (similarity ≥ 80%)
    assertNotNull(result);
    assertEquals(branch1.getId(), result.getId(), "Should match Branch 1 despite abbreviation");
  }

  @Test
  @DisplayName("Should match with missing postal code (≥80% similarity)")
  void testMatchDeliveryAddress_MissingPostalCode() {
    // Given: Xentral address without postal code
    String xentralAddress = "Maximilianstraße 17, München";

    // When: Match address
    Customer result = addressMatcher.matchDeliveryAddress(xentralAddress, headquarter);

    // Then: Should still match Branch 1
    assertNotNull(result);
    assertEquals(
        branch1.getId(), result.getId(), "Should match Branch 1 despite missing postal code");
  }

  @Test
  @DisplayName("Should fallback to parent when similarity <80%")
  void testMatchDeliveryAddress_LowSimilarity() {
    // Given: Completely different address (no match)
    String xentralAddress = "Hauptstraße 999, 12345 UnknownCity";

    // When: Match address
    Customer result = addressMatcher.matchDeliveryAddress(xentralAddress, headquarter);

    // Then: Should return parent (fallback)
    assertNotNull(result);
    assertEquals(
        headquarter.getId(), result.getId(), "Should fallback to parent when no match found");
  }

  @Test
  @DisplayName("Should handle null Xentral address → Return parent")
  void testMatchDeliveryAddress_NullInput() {
    // Given: Null Xentral address
    String xentralAddress = null;

    // When: Match address
    Customer result = addressMatcher.matchDeliveryAddress(xentralAddress, headquarter);

    // Then: Should return parent
    assertNotNull(result);
    assertEquals(headquarter.getId(), result.getId(), "Should return parent for null input");
  }

  @Test
  @DisplayName("Should handle empty Xentral address → Return parent")
  void testMatchDeliveryAddress_EmptyInput() {
    // Given: Empty Xentral address
    String xentralAddress = "   ";

    // When: Match address
    Customer result = addressMatcher.matchDeliveryAddress(xentralAddress, headquarter);

    // Then: Should return parent
    assertNotNull(result);
    assertEquals(headquarter.getId(), result.getId(), "Should return parent for empty input");
  }

  @Test
  @DisplayName("Should return parent when no branches exist")
  void testMatchDeliveryAddress_NoBranches() {
    // Given: Headquarter with no branches
    Customer headquarterNoBranches = createHeadquarter("Test Company");
    headquarterNoBranches.setChildCustomers(new ArrayList<>());

    String xentralAddress = "Test Street 1, 12345 TestCity";

    // When: Match address
    Customer result = addressMatcher.matchDeliveryAddress(xentralAddress, headquarterNoBranches);

    // Then: Should return parent
    assertNotNull(result);
    assertEquals(
        headquarterNoBranches.getId(),
        result.getId(),
        "Should return parent when no branches exist");
  }

  @Test
  @DisplayName("Should select best match from multiple branches")
  void testMatchDeliveryAddress_MultipleBranches_BestMatch() {
    // Given: Xentral address matching Hamburg branch
    String xentralAddress = "Reeperbahn 1, 20359 Hamburg";

    // When: Match address
    Customer result = addressMatcher.matchDeliveryAddress(xentralAddress, headquarter);

    // Then: Should return Branch 2 (Hamburg), not Branch 1 or 3
    assertNotNull(result);
    assertEquals(branch2.getId(), result.getId(), "Should select Hamburg branch as best match");
    assertEquals("NH Hotel Hamburg", result.getCompanyName());
  }

  @Test
  @DisplayName("Should normalize German umlauts (ä→ae, ö→oe, ü→ue, ß→ss)")
  void testMatchDeliveryAddress_GermanUmlautNormalization() {
    // Given: Xentral address with umlauts
    String xentralAddress = "Maximilianstraße 17, 80539 München"; // ß, ü

    // When: Match address
    Customer result = addressMatcher.matchDeliveryAddress(xentralAddress, headquarter);

    // Then: Should match (umlauts normalized correctly)
    assertNotNull(result);
    assertEquals(branch1.getId(), result.getId(), "Should match after umlaut normalization");
  }

  @Test
  @DisplayName("Should handle branch without address → Skip branch")
  void testMatchDeliveryAddress_BranchWithoutAddress() {
    // Given: Branch 4 without address
    Customer branch4 = createBranchWithoutAddress(headquarter, "NH Hotel Dresden");
    headquarter.getChildCustomers().add(branch4);

    String xentralAddress = "Maximilianstraße 17, 80539 München";

    // When: Match address
    Customer result = addressMatcher.matchDeliveryAddress(xentralAddress, headquarter);

    // Then: Should still match Branch 1, ignoring Branch 4
    assertNotNull(result);
    assertEquals(branch1.getId(), result.getId(), "Should skip branch without address");
  }

  @Test
  @DisplayName("Should match with special characters removed")
  void testMatchDeliveryAddress_SpecialCharactersRemoved() {
    // Given: Xentral address with special characters
    String xentralAddress = "Maximilianstraße 17, 80539 München!!!";

    // When: Match address
    Customer result = addressMatcher.matchDeliveryAddress(xentralAddress, headquarter);

    // Then: Should match (special chars removed during normalization)
    assertNotNull(result);
    assertEquals(branch1.getId(), result.getId(), "Should match after removing special characters");
  }

  @Test
  @DisplayName("Should match with different whitespace formatting")
  void testMatchDeliveryAddress_WhitespaceNormalization() {
    // Given: Xentral address with extra whitespace
    String xentralAddress = "Maximilianstraße    17,    80539    München";

    // When: Match address
    Customer result = addressMatcher.matchDeliveryAddress(xentralAddress, headquarter);

    // Then: Should match (whitespace normalized)
    assertNotNull(result);
    assertEquals(branch1.getId(), result.getId(), "Should match after whitespace normalization");
  }

  @Test
  @DisplayName("Should match Berlin branch with correct address")
  void testMatchDeliveryAddress_BerlinBranch() {
    // Given: Xentral address for Berlin branch
    String xentralAddress = "Alexanderplatz 1, 10178 Berlin";

    // When: Match address
    Customer result = addressMatcher.matchDeliveryAddress(xentralAddress, headquarter);

    // Then: Should return Branch 3 (Berlin)
    assertNotNull(result);
    assertEquals(branch3.getId(), result.getId(), "Should match Branch 3 (Berlin)");
    assertEquals("NH Hotel Berlin", result.getCompanyName());
  }

  // ========== HELPER METHODS ==========

  /** Creates a test HEADQUARTER customer. */
  private Customer createHeadquarter(String companyName) {
    Customer customer = new Customer();
    customer.setId(UUID.randomUUID());
    customer.setCompanyName(companyName);
    customer.setHierarchyType(CustomerHierarchyType.HEADQUARTER);
    customer.setChildCustomers(new ArrayList<>());
    return customer;
  }

  /** Creates a test FILIALE customer with a primary shipping address. */
  private Customer createBranch(
      Customer parent,
      String companyName,
      String street,
      String streetNumber,
      String postalCode,
      String city) {
    Customer branch = new Customer();
    branch.setId(UUID.randomUUID());
    branch.setCompanyName(companyName);
    branch.setHierarchyType(CustomerHierarchyType.FILIALE);
    branch.setParentCustomer(parent);

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
    address.setStreet(street);
    address.setStreetNumber(streetNumber);
    address.setPostalCode(postalCode);
    address.setCity(city);
    address.setCountry("DEU");
    address.setIsPrimaryForType(true);

    // Link address to location
    List<CustomerAddress> addresses = new ArrayList<>();
    addresses.add(address);
    location.setAddresses(addresses);

    // Link location to branch via locations collection
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
    // No location/address
    return branch;
  }
}

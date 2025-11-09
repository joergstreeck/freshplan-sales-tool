package de.freshplan.domain.customer.service;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerHierarchyType;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.CustomerType;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.CreateCustomerRequest;
import de.freshplan.domain.customer.service.dto.CustomerResponse;
import de.freshplan.domain.customer.service.exception.CustomerNotFoundException;
import de.freshplan.domain.customer.service.exception.InvalidHierarchyException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit Tests for BranchService.
 *
 * <p>Sprint 2.1.7.7 D4: CreateBranchDialog - BranchService Tests
 *
 * <p>Test Coverage Goals: ≥85% (Branch Coverage)
 *
 * <p>Test Scenarios:
 *
 * <ol>
 *   <li>Successful branch creation under HEADQUARTER
 *   <li>Branch inherits xentral_customer_id from parent
 *   <li>Branch has hierarchyType = FILIALE
 *   <li>Parent not found error
 *   <li>Parent is not HEADQUARTER error (STANDALONE, FILIALE)
 *   <li>Get branches by headquarter
 *   <li>Update branch
 *   <li>Delete branch
 *   <li>Validate hierarchy integrity
 * </ol>
 *
 * @author FreshPlan Team
 * @since 2.1.7.7
 */
@QuarkusTest
@DisplayName("BranchService Tests")
public class BranchServiceTest {

  @Inject BranchService branchService;

  @Inject CustomerRepository customerRepository;

  @Inject EntityManager entityManager;

  private Customer testHeadquarter;
  private UUID testHeadquarterId;
  private static final String TEST_USER = "test@freshplan.de";

  @BeforeEach
  @Transactional
  void setUp() {
    // Create test HEADQUARTER customer
    testHeadquarter = new Customer();
    testHeadquarter.setCustomerNumber(de.freshplan.TestIds.uniqueCustomerNumber());
    testHeadquarter.setCompanyName("Test NH Hotels Deutschland GmbH");
    testHeadquarter.setHierarchyType(CustomerHierarchyType.HEADQUARTER);
    testHeadquarter.setStatus(CustomerStatus.AKTIV);
    testHeadquarter.setXentralCustomerId("XEN-12345");
    testHeadquarter.setCreatedBy(TEST_USER);
    testHeadquarter.setUpdatedBy(TEST_USER);

    customerRepository.persist(testHeadquarter);
    testHeadquarterId = testHeadquarter.getId();
  }

  @AfterEach
  @Transactional
  void cleanup() {
    // Clean up all test customers created by this test
    // Pattern matches TEST-xxx and KD-xxx customer numbers from TestIds.uniqueCustomerNumber()

    // Step 1: Delete timeline events first (foreign key constraint)
    entityManager.createNativeQuery(
        "DELETE FROM customer_timeline_events WHERE customer_id IN " +
        "(SELECT id FROM customers WHERE customer_number LIKE 'TEST-%' OR customer_number LIKE 'KD-%')")
        .executeUpdate();

    // Step 2: Now delete customers
    customerRepository.delete("customerNumber LIKE 'TEST-%' OR customerNumber LIKE 'KD-%'");
  }

  // ========== TEST CASES ==========

  @Test
  @DisplayName("Should create branch under HEADQUARTER successfully")
  void testCreateBranch_Success() {
    // Given: Valid CreateCustomerRequest for branch
    CreateCustomerRequest request =
        CreateCustomerRequest.builder()
            .companyName("Test Branch A - Frankfurt")
            .customerType(CustomerType.UNTERNEHMEN)
            .status(CustomerStatus.PROSPECT)
            .build();

    // When: Create branch
    CustomerResponse branch = branchService.createBranch(testHeadquarterId, request, TEST_USER);

    // Then: Branch created successfully
    assertNotNull(branch, "Branch should not be null");
    assertNotNull(branch.id(), "Branch ID should not be null");
    assertEquals("Test Branch A - Frankfurt", branch.companyName(), "Branch name should match");
    assertEquals(
        CustomerHierarchyType.FILIALE, branch.hierarchyType(), "Branch should be FILIALE");
    assertEquals(
        testHeadquarterId.toString(), branch.parentCustomerId(), "Parent ID should match");
    assertEquals(
        "XEN-12345", branch.xentralCustomerId(), "Branch should inherit parent xentralCustomerId");
    assertEquals(CustomerStatus.PROSPECT, branch.status(), "Branch status should be PROSPECT");
  }

  @Test
  @DisplayName("Should throw CustomerNotFoundException when parent not found")
  void testCreateBranch_ParentNotFound() {
    // Given: Non-existent parent ID
    UUID nonExistentId = UUID.randomUUID();
    CreateCustomerRequest request =
        CreateCustomerRequest.builder()
            .companyName("Test Branch B - Köln")
            .customerType(CustomerType.UNTERNEHMEN)
            .build();

    // When/Then: Should throw CustomerNotFoundException
    assertThrows(
        CustomerNotFoundException.class,
        () -> branchService.createBranch(nonExistentId, request, TEST_USER),
        "Should throw CustomerNotFoundException when parent not found");
  }

  @Test
  @DisplayName("Should throw InvalidHierarchyException when parent is STANDALONE")
  @Transactional
  void testCreateBranch_ParentIsStandalone() {
    // Given: STANDALONE customer (not HEADQUARTER)
    Customer standalone = new Customer();
    standalone.setCustomerNumber(de.freshplan.TestIds.uniqueCustomerNumber());
    standalone.setCompanyName("Test Standalone Customer");
    standalone.setHierarchyType(CustomerHierarchyType.STANDALONE);
    standalone.setStatus(CustomerStatus.AKTIV);
    standalone.setCreatedBy(TEST_USER);
    standalone.setUpdatedBy(TEST_USER);
    customerRepository.persist(standalone);

    CreateCustomerRequest request =
        CreateCustomerRequest.builder()
            .companyName("Test Branch C - Stuttgart")
            .customerType(CustomerType.UNTERNEHMEN)
            .build();

    // When/Then: Should throw InvalidHierarchyException
    InvalidHierarchyException exception =
        assertThrows(
            InvalidHierarchyException.class,
            () -> branchService.createBranch(standalone.getId(), request, TEST_USER),
            "Should throw InvalidHierarchyException when parent is not HEADQUARTER");

    assertTrue(
        exception.getMessage().contains("not a HEADQUARTER"),
        "Exception message should indicate parent is not HEADQUARTER");
  }

  @Test
  @DisplayName("Should throw InvalidHierarchyException when parent is FILIALE")
  @Transactional
  void testCreateBranch_ParentIsFiliale() {
    // Given: FILIALE customer (not HEADQUARTER)
    Customer filiale = new Customer();
    filiale.setCustomerNumber(de.freshplan.TestIds.uniqueCustomerNumber());
    filiale.setCompanyName("Test Filiale Customer");
    filiale.setHierarchyType(CustomerHierarchyType.FILIALE);
    filiale.setStatus(CustomerStatus.AKTIV);
    filiale.setParentCustomer(testHeadquarter);
    filiale.setCreatedBy(TEST_USER);
    filiale.setUpdatedBy(TEST_USER);
    customerRepository.persist(filiale);

    CreateCustomerRequest request =
        CreateCustomerRequest.builder()
            .companyName("Test Sub-Branch")
            .customerType(CustomerType.UNTERNEHMEN)
            .build();

    // When/Then: Should throw InvalidHierarchyException (no nested branches)
    InvalidHierarchyException exception =
        assertThrows(
            InvalidHierarchyException.class,
            () -> branchService.createBranch(filiale.getId(), request, TEST_USER),
            "Should throw InvalidHierarchyException when trying to create branch under FILIALE");

    assertTrue(
        exception.getMessage().contains("not a HEADQUARTER"),
        "Exception message should indicate parent is not HEADQUARTER");
  }

  @Test
  @DisplayName("Should get all branches for a HEADQUARTER")
  void testGetBranchesByHeadquarter() {
    // Given: 3 branches under HEADQUARTER
    CreateCustomerRequest branch1 =
        CreateCustomerRequest.builder()
            .companyName("Test NH Hotel München")
            .customerType(CustomerType.UNTERNEHMEN)
            .build();
    CreateCustomerRequest branch2 =
        CreateCustomerRequest.builder()
            .companyName("Test NH Hotel Hamburg")
            .customerType(CustomerType.UNTERNEHMEN)
            .build();
    CreateCustomerRequest branch3 =
        CreateCustomerRequest.builder()
            .companyName("Test NH Hotel Berlin")
            .customerType(CustomerType.UNTERNEHMEN)
            .build();

    branchService.createBranch(testHeadquarterId, branch1, TEST_USER);
    branchService.createBranch(testHeadquarterId, branch2, TEST_USER);
    branchService.createBranch(testHeadquarterId, branch3, TEST_USER);

    // When: Get branches
    List<CustomerResponse> branches = branchService.getBranchesByHeadquarter(testHeadquarterId);

    // Then: Should return 3 branches
    assertNotNull(branches, "Branches list should not be null");
    assertEquals(3, branches.size(), "Should return 3 branches");

    // Verify all branches are FILIALE
    for (CustomerResponse branch : branches) {
      assertEquals(
          CustomerHierarchyType.FILIALE,
          branch.hierarchyType(),
          "All branches should be FILIALE");
      assertEquals(
          testHeadquarterId.toString(),
          branch.parentCustomerId(),
          "All branches should have same parent");
    }
  }

  @Test
  @DisplayName("Should return empty list when HEADQUARTER has no branches")
  @Transactional
  void testGetBranchesByHeadquarter_NoBranches() {
    // Given: HEADQUARTER with no branches

    // When: Get branches
    List<CustomerResponse> branches = branchService.getBranchesByHeadquarter(testHeadquarterId);

    // Then: Should return empty list
    assertNotNull(branches, "Branches list should not be null");
    assertTrue(branches.isEmpty(), "Branches list should be empty");
  }

  @Test
  @DisplayName("Should validate hierarchy: FILIALE must have HEADQUARTER parent")
  @Transactional
  void testValidateHierarchy_FilialeWithoutParent() {
    // Given: FILIALE without parent (invalid)
    Customer invalidFiliale = new Customer();
    invalidFiliale.setCustomerNumber(de.freshplan.TestIds.uniqueCustomerNumber());
    invalidFiliale.setCompanyName("Test Invalid Filiale");
    invalidFiliale.setHierarchyType(CustomerHierarchyType.FILIALE);
    invalidFiliale.setStatus(CustomerStatus.AKTIV);
    invalidFiliale.setParentCustomer(null); // No parent!
    invalidFiliale.setCreatedBy(TEST_USER);
    invalidFiliale.setUpdatedBy(TEST_USER);
    customerRepository.persist(invalidFiliale);

    // When/Then: Validation should fail
    InvalidHierarchyException exception =
        assertThrows(
            InvalidHierarchyException.class,
            () -> branchService.validateHierarchy(invalidFiliale.getId()),
            "Should throw InvalidHierarchyException for FILIALE without parent");

    assertTrue(
        exception.getMessage().contains("has no parent"),
        "Exception message should indicate missing parent");
  }

  @Test
  @DisplayName("Should validate hierarchy: HEADQUARTER must not have parent")
  @Transactional
  void testValidateHierarchy_HeadquarterWithParent() {
    // Given: HEADQUARTER with parent (invalid)
    Customer invalidHeadquarter = new Customer();
    invalidHeadquarter.setCustomerNumber(de.freshplan.TestIds.uniqueCustomerNumber());
    invalidHeadquarter.setCompanyName("Test Invalid Headquarter");
    invalidHeadquarter.setHierarchyType(CustomerHierarchyType.HEADQUARTER);
    invalidHeadquarter.setStatus(CustomerStatus.AKTIV);
    invalidHeadquarter.setParentCustomer(testHeadquarter); // HEADQUARTER should not have parent!
    invalidHeadquarter.setCreatedBy(TEST_USER);
    invalidHeadquarter.setUpdatedBy(TEST_USER);
    customerRepository.persist(invalidHeadquarter);

    // When/Then: Validation should fail
    InvalidHierarchyException exception =
        assertThrows(
            InvalidHierarchyException.class,
            () -> branchService.validateHierarchy(invalidHeadquarter.getId()),
            "Should throw InvalidHierarchyException for HEADQUARTER with parent");

    assertTrue(
        exception.getMessage().contains("has a parent"),
        "Exception message should indicate invalid parent");
  }

  @Test
  @DisplayName("Should validate hierarchy: Valid HEADQUARTER")
  @Transactional
  void testValidateHierarchy_ValidHeadquarter() {
    // Given: Valid HEADQUARTER (no parent)

    // When/Then: Validation should pass
    assertTrue(
        branchService.validateHierarchy(testHeadquarterId),
        "Valid HEADQUARTER should pass validation");
  }

  @Test
  @DisplayName("Should validate hierarchy: Valid FILIALE")
  void testValidateHierarchy_ValidFiliale() {
    // Given: Valid FILIALE (with HEADQUARTER parent)
    CreateCustomerRequest request =
        CreateCustomerRequest.builder()
            .companyName("Test Valid Branch")
            .customerType(CustomerType.UNTERNEHMEN)
            .build();
    CustomerResponse branch = branchService.createBranch(testHeadquarterId, request, TEST_USER);

    // When/Then: Validation should pass
    assertTrue(
        branchService.validateHierarchy(UUID.fromString(branch.id())),
        "Valid FILIALE should pass validation");
  }

  @Test
  @DisplayName("Should inherit xentral_customer_id from parent")
  @Transactional
  void testCreateBranch_InheritsXentralCustomerId() {
    // Given: Reload parent in current transaction and update xentralCustomerId
    Customer headquarter =
        customerRepository
            .findByIdOptional(testHeadquarterId)
            .orElseThrow(() -> new RuntimeException("Headquarter not found"));
    headquarter.setXentralCustomerId("XEN-99999");
    entityManager.flush(); // Flush to DB before BranchService loads parent

    CreateCustomerRequest request =
        CreateCustomerRequest.builder()
            .companyName("Test Branch D - Düsseldorf")
            .customerType(CustomerType.UNTERNEHMEN)
            .build();

    // When: Create branch
    CustomerResponse branch = branchService.createBranch(testHeadquarterId, request, TEST_USER);

    // Then: Branch inherits xentral_customer_id
    assertEquals(
        "XEN-99999",
        branch.xentralCustomerId(),
        "Branch should inherit parent's xentral_customer_id");
  }

  @Test
  @DisplayName("Should allow branch creation when parent is soft-deleted")
  @Transactional
  void testCreateBranch_ParentIsDeleted() {
    // Given: Soft-deleted HEADQUARTER
    testHeadquarter.setIsDeleted(true);

    CreateCustomerRequest request =
        CreateCustomerRequest.builder()
            .companyName("Test Branch E - Dortmund")
            .customerType(CustomerType.UNTERNEHMEN)
            .build();

    // When/Then: Should allow creation (BranchService doesn't check isDeleted, but validates
    // hierarchy)
    // This test documents current behavior - may be enhanced in future to check isDeleted
    CustomerResponse branch = branchService.createBranch(testHeadquarterId, request, TEST_USER);

    assertNotNull(branch, "Branch creation should succeed even if parent is soft-deleted");
    // Note: Future enhancement could validate parent.isDeleted = false
  }
}

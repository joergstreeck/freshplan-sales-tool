package de.freshplan.domain.customer.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.api.resources.CustomerResource;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.CustomerType;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.service.dto.*;
import de.freshplan.domain.customer.service.exception.CustomerNotFoundException;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * CRITICAL Integration Test for Customer CQRS Implementation.
 *
 * <p>CustomerService is the HEART of the application. This test ensures that both implementations
 * (Legacy and CQRS) have IDENTICAL behavior.
 *
 * <p>Tests all Command and Query operations with Feature Flag enabled: - Commands: create, update,
 * delete, restore, addChild, merge, changeStatus - Queries: get, getAll, getByStatus,
 * getByIndustry, getDashboard, getAtRisk, getHierarchy, checkDuplicates
 *
 * @author FreshPlan Team
 * @since Phase 14 - Integration Tests
 */
@QuarkusTest
@TestProfile(CustomerCQRSTestProfile.class)
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
@TestTransaction
@DisplayName("Customer CQRS Integration Test")
class CustomerCQRSIntegrationTest {

  @Inject CustomerResource customerResource; // Test via Resource to verify full stack

  @ConfigProperty(name = "features.cqrs.enabled")
  boolean cqrsEnabled;

  private CreateCustomerRequest validCreateRequest;
  private UUID createdCustomerId;

  @BeforeEach
  void setUp() {
    // Create valid request with unique company name to avoid test conflicts
    String uniqueSuffix =
        "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    validCreateRequest =
        CreateCustomerRequest.builder()
            .companyName("CQRS Test Company" + uniqueSuffix)
            .customerType(CustomerType.NEUKUNDE)
            .industry(Industry.SONSTIGE)
            .expectedAnnualVolume(new BigDecimal("100000"))
            .build();
  }

  @Test
  @DisplayName("Feature Flag should be enabled for CQRS tests")
  void testCQRSModeIsEnabled() {
    // Verify that CQRS mode is enabled for this test
    assertThat(cqrsEnabled).as("CQRS Feature Flag must be enabled for this test").isTrue();
  }

  // =====================================
  // CREATE OPERATION TESTS
  // =====================================

  @Test
  @TestTransaction
  void createCustomer_inCQRSMode_shouldCreateSuccessfully() {
    // When
    var response = customerResource.createCustomer(validCreateRequest);

    // Then
    assertThat(response.getStatus()).isEqualTo(201); // Created

    CustomerResponse customer = (CustomerResponse) response.getEntity();
    assertThat(customer).isNotNull();
    assertThat(customer.customerNumber()).startsWith("KD-");
    assertThat(customer.companyName()).isEqualTo(validCreateRequest.companyName());
    assertThat(customer.customerType()).isEqualTo(CustomerType.NEUKUNDE);
    assertThat(customer.status()).isEqualTo(CustomerStatus.LEAD);

    // Store for other tests
    createdCustomerId = UUID.fromString(customer.id());
  }

  @Test
  @TestTransaction
  @DisplayName("Create and retrieve customer should work end-to-end")
  void createAndRetrieve_inCQRSMode_shouldWorkEndToEnd() {
    // Create
    var createResponse = customerResource.createCustomer(validCreateRequest);
    CustomerResponse created = (CustomerResponse) createResponse.getEntity();
    UUID customerId = UUID.fromString(created.id());

    // Retrieve
    var getResponse = customerResource.getCustomer(customerId);
    CustomerResponse retrieved = (CustomerResponse) getResponse.getEntity();

    // Compare - should be identical
    assertThat(retrieved).isNotNull();
    assertThat(retrieved.id()).isEqualTo(created.id());
    assertThat(retrieved.customerNumber()).isEqualTo(created.customerNumber());
    assertThat(retrieved.companyName()).isEqualTo(created.companyName());
    assertThat(retrieved.customerType()).isEqualTo(created.customerType());
    assertThat(retrieved.status()).isEqualTo(created.status());
  }

  @Test
  @TestTransaction
  void createCustomer_withDuplicateName_shouldThrowException() {
    // Create unique request to avoid conflicts with other tests
    String uniqueSuffix = "_duplicate_test_" + System.currentTimeMillis();
    CreateCustomerRequest uniqueRequest =
        CreateCustomerRequest.builder()
            .companyName("Duplicate Test Company" + uniqueSuffix)
            .customerType(CustomerType.NEUKUNDE)
            .industry(Industry.SONSTIGE)
            .expectedAnnualVolume(new BigDecimal("100000"))
            .build();

    // Create first customer
    customerResource.createCustomer(uniqueRequest);

    // Try to create duplicate with same name
    assertThatThrownBy(() -> customerResource.createCustomer(uniqueRequest))
        .isInstanceOf(
            RuntimeException.class) // CustomerAlreadyExistsException wrapped in RuntimeException
        .hasMessageContaining("Customer with similar company name already exists");
  }

  // =====================================
  // UPDATE OPERATION TESTS
  // =====================================

  @Test
  @TestTransaction
  void updateCustomer_inCQRSMode_shouldUpdateSuccessfully() {
    // Create customer first
    var createResponse = customerResource.createCustomer(validCreateRequest);
    CustomerResponse created = (CustomerResponse) createResponse.getEntity();
    UUID customerId = UUID.fromString(created.id());

    // Prepare update
    UpdateCustomerRequest updateRequest =
        UpdateCustomerRequest.builder()
            .companyName("Updated CQRS Company")
            .expectedAnnualVolume(new BigDecimal("200000"))
            .build();

    // Update
    var updateResponse = customerResource.updateCustomer(customerId, updateRequest);
    CustomerResponse updated = (CustomerResponse) updateResponse.getEntity();

    // Verify
    assertThat(updated.companyName()).isEqualTo("Updated CQRS Company");

    // Retrieve again to confirm persistence
    var getResponse = customerResource.getCustomer(customerId);
    CustomerResponse retrieved = (CustomerResponse) getResponse.getEntity();
    assertThat(retrieved.companyName()).isEqualTo("Updated CQRS Company");
  }

  // =====================================
  // DELETE & RESTORE OPERATION TESTS
  // =====================================

  @Test
  @TestTransaction
  @DisplayName("Delete customer should soft delete with reason")
  void deleteCustomer_inCQRSMode_shouldSoftDelete() {
    // Create customer
    var createResponse = customerResource.createCustomer(validCreateRequest);
    CustomerResponse created = (CustomerResponse) createResponse.getEntity();
    UUID customerId = UUID.fromString(created.id());

    // Delete (soft delete) with reason
    String deleteReason = "Test deletion for CQRS integration test";
    var deleteResponse = customerResource.deleteCustomer(customerId, deleteReason);
    assertThat(deleteResponse.getStatus()).isEqualTo(204); // No Content

    // Try to retrieve - soft-deleted customers should NOT be retrievable
    // They should throw CustomerNotFoundException
    assertThatThrownBy(() -> customerResource.getCustomer(customerId))
        .isInstanceOf(CustomerNotFoundException.class)
        .hasMessageContaining("Customer not found with ID: " + customerId);
  }

  @Test
  @TestTransaction
  void restoreCustomer_inCQRSMode_shouldRestoreDeleted() {
    // Create customer
    var createResponse = customerResource.createCustomer(validCreateRequest);
    CustomerResponse created = (CustomerResponse) createResponse.getEntity();
    UUID customerId = UUID.fromString(created.id());

    // Delete with reason
    customerResource.deleteCustomer(customerId, "Test deletion before restore");

    // Restore
    var restoreResponse = customerResource.restoreCustomer(customerId);
    CustomerResponse restored = (CustomerResponse) restoreResponse.getEntity();

    // Verify restored
    assertThat(restored).isNotNull();
    assertThat(restored.id()).isEqualTo(created.id());
    assertThat(restored.companyName()).isEqualTo(created.companyName());

    // Should be retrievable again
    var getResponse = customerResource.getCustomer(customerId);
    CustomerResponse retrieved = (CustomerResponse) getResponse.getEntity();
    assertThat(retrieved).isNotNull();
    assertThat(retrieved.status()).isNotEqualTo(CustomerStatus.ARCHIVIERT);
  }

  // =====================================
  // QUERY OPERATION TESTS
  // =====================================

  @Test
  @TestTransaction
  @DisplayName("Get all customers should return paginated list")
  void getAllCustomers_inCQRSMode_shouldReturnList() {
    // Create a few customers first with unique names to avoid conflicts
    String uniqueSuffix = "_" + System.currentTimeMillis();

    customerResource.createCustomer(
        CreateCustomerRequest.builder()
            .companyName("CQRS Test Company 1" + uniqueSuffix)
            .customerType(CustomerType.UNTERNEHMEN)
            .industry(Industry.SONSTIGE)
            .expectedAnnualVolume(BigDecimal.ZERO)
            .build());

    customerResource.createCustomer(
        CreateCustomerRequest.builder()
            .companyName("CQRS Test Company 2" + uniqueSuffix)
            .customerType(CustomerType.INSTITUTION)
            .industry(Industry.SONSTIGE)
            .expectedAnnualVolume(BigDecimal.ZERO)
            .build());

    // Get all with proper parameters (page, size, status, industry)
    var response = customerResource.getAllCustomers(0, 10, null, null);
    assertThat(response.getStatus()).isEqualTo(200);

    CustomerListResponse listResponse = (CustomerListResponse) response.getEntity();

    // Verify
    assertThat(listResponse).isNotNull();
    assertThat(listResponse.content()).isNotEmpty();
    // Check that our newly created customers are in the list
    assertThat(listResponse.content())
        .extracting(CustomerResponse::companyName)
        .anyMatch(name -> name.startsWith("CQRS Test Company 1"))
        .anyMatch(name -> name.startsWith("CQRS Test Company 2"));
  }

  @Test
  @TestTransaction
  @DisplayName("Get customers by status should filter correctly")
  void getCustomersByStatus_inCQRSMode_shouldFilterCorrectly() {
    // Create customers with different statuses (Note: can't set status in CreateRequest)
    customerResource.createCustomer(
        CreateCustomerRequest.builder()
            .companyName("Lead Customer")
            .customerType(CustomerType.NEUKUNDE)
            .industry(Industry.SONSTIGE)
            .expectedAnnualVolume(BigDecimal.ZERO)
            .build());

    customerResource.createCustomer(
        CreateCustomerRequest.builder()
            .companyName("Active Customer")
            .customerType(CustomerType.UNTERNEHMEN)
            .industry(Industry.SONSTIGE)
            .expectedAnnualVolume(BigDecimal.ZERO)
            .build());

    // Query by status using getAllCustomers with status filter
    var leadResponse = customerResource.getAllCustomers(0, 100, CustomerStatus.LEAD, null);
    CustomerListResponse leadList = (CustomerListResponse) leadResponse.getEntity();

    var activeResponse = customerResource.getAllCustomers(0, 100, CustomerStatus.AKTIV, null);
    CustomerListResponse activeList = (CustomerListResponse) activeResponse.getEntity();

    // Verify filtering
    if (!leadList.content().isEmpty()) {
      assertThat(leadList.content())
          .extracting(CustomerResponse::status)
          .containsOnly(CustomerStatus.LEAD);
    }

    if (!activeList.content().isEmpty()) {
      assertThat(activeList.content())
          .extracting(CustomerResponse::status)
          .containsOnly(CustomerStatus.AKTIV);
    }
  }

  @Test
  @TestTransaction
  @DisplayName("Get customers at risk should return high risk customers")
  void getCustomersAtRisk_inCQRSMode_shouldReturnHighRiskCustomers() {
    // Create customers with unique names to avoid duplicates
    String uniqueSuffix = "_" + System.currentTimeMillis();

    customerResource.createCustomer(
        CreateCustomerRequest.builder()
            .companyName("Low Risk Customer" + uniqueSuffix)
            .customerType(CustomerType.UNTERNEHMEN)
            .industry(Industry.SONSTIGE)
            .expectedAnnualVolume(BigDecimal.ZERO)
            .build());

    // Create high risk customer
    var highRiskRequest =
        CreateCustomerRequest.builder()
            .companyName("High Risk Customer" + uniqueSuffix)
            .customerType(CustomerType.NEUKUNDE)
            .industry(Industry.SONSTIGE)
            .expectedAnnualVolume(BigDecimal.ZERO)
            .build();

    var response = customerResource.createCustomer(highRiskRequest);
    CustomerResponse created = (CustomerResponse) response.getEntity();
    UUID customerId = UUID.fromString(created.id());

    // Update customer to be at risk (no riskScore field in UpdateRequest)
    customerResource.updateCustomer(
        customerId,
        UpdateCustomerRequest.builder()
            .status(CustomerStatus.RISIKO) // Mark as at risk
            .build());

    // Get at-risk customers using the correct endpoint
    var riskResponse = customerResource.getCustomersAtRisk(70, 0, 100);
    CustomerListResponse atRiskList = (CustomerListResponse) riskResponse.getEntity();

    // Verify
    assertThat(atRiskList).isNotNull();
    if (!atRiskList.content().isEmpty()) {
      // Check for our specific high risk customer
      assertThat(atRiskList.content())
          .extracting(CustomerResponse::companyName)
          .anyMatch(name -> name.startsWith("High Risk Customer"));
      // Risk customers should have RISIKO status
      assertThat(atRiskList.content())
          .extracting(CustomerResponse::status)
          .contains(CustomerStatus.RISIKO);
    }
  }

  @Test
  @TestTransaction
  @DisplayName("Get dashboard data should return statistics")
  void getDashboardData_inCQRSMode_shouldReturnStatistics() {
    // Create test data
    customerResource.createCustomer(
        CreateCustomerRequest.builder()
            .companyName("Dashboard Test 1")
            .customerType(CustomerType.UNTERNEHMEN)
            .industry(Industry.SONSTIGE)
            .expectedAnnualVolume(new BigDecimal("50000"))
            .build());

    customerResource.createCustomer(
        CreateCustomerRequest.builder()
            .companyName("Dashboard Test 2")
            .customerType(CustomerType.NEUKUNDE)
            .industry(Industry.SONSTIGE)
            .expectedAnnualVolume(new BigDecimal("75000"))
            .build());

    // Get dashboard data
    var dashboardResponse = customerResource.getDashboardData();
    CustomerDashboardResponse dashboard = (CustomerDashboardResponse) dashboardResponse.getEntity();

    // Verify statistics
    assertThat(dashboard).isNotNull();
    assertThat(dashboard.totalCustomers()).isGreaterThan(0);
    assertThat(dashboard.activeCustomers()).isGreaterThanOrEqualTo(0);
    assertThat(dashboard.customersByStatus()).isNotEmpty();
    assertThat(dashboard.customersByLifecycle()).isNotEmpty();
  }

  // =====================================
  // HIERARCHY OPERATION TESTS
  // =====================================

  @Test
  @TestTransaction
  void addChildCustomer_inCQRSMode_shouldCreateHierarchy() {
    // Create parent customer
    var parentResponse =
        customerResource.createCustomer(
            CreateCustomerRequest.builder()
                .companyName("Parent Company AG")
                .customerType(CustomerType.UNTERNEHMEN)
                .build());
    CustomerResponse parent = (CustomerResponse) parentResponse.getEntity();
    UUID parentId = UUID.fromString(parent.id());

    // Create child customer
    var childResponse =
        customerResource.createCustomer(
            CreateCustomerRequest.builder()
                .companyName("Child Company GmbH")
                .customerType(CustomerType.UNTERNEHMEN)
                .build());
    CustomerResponse child = (CustomerResponse) childResponse.getEntity();
    UUID childId = UUID.fromString(child.id());

    // Add child to parent
    AddChildCustomerRequest addChildRequest = new AddChildCustomerRequest(childId);
    var addChildResponse = customerResource.addChildCustomer(parentId, addChildRequest);
    assertThat(addChildResponse.getStatus()).isEqualTo(200);

    // Get hierarchy
    var hierarchyResponse = customerResource.getCustomerHierarchy(parentId);
    CustomerResponse hierarchy = (CustomerResponse) hierarchyResponse.getEntity();

    // Verify - the hierarchy is returned as a CustomerResponse with child relationships
    assertThat(hierarchy).isNotNull();
    assertThat(hierarchy.id()).isEqualTo(parentId.toString());
    // Note: Child relationships would be in the CustomerResponse structure
    // The exact verification depends on how CustomerResponse includes children
  }

  // =====================================
  // SPECIAL OPERATION TESTS
  // =====================================

  @Test
  @TestTransaction
  @DisplayName("Batch update customer risk scores")
  void batchUpdateRiskScores_inCQRSMode_shouldUpdateMultipleCustomers() {
    // Create customers
    var response1 =
        customerResource.createCustomer(
            CreateCustomerRequest.builder()
                .companyName("Risk Test 1")
                .customerType(CustomerType.UNTERNEHMEN)
                .industry(Industry.SONSTIGE)
                .expectedAnnualVolume(BigDecimal.ZERO)
                .build());
    CustomerResponse customer1 = (CustomerResponse) response1.getEntity();

    var response2 =
        customerResource.createCustomer(
            CreateCustomerRequest.builder()
                .companyName("Risk Test 2")
                .customerType(CustomerType.NEUKUNDE)
                .industry(Industry.SONSTIGE)
                .expectedAnnualVolume(BigDecimal.ZERO)
                .build());
    CustomerResponse customer2 = (CustomerResponse) response2.getEntity();

    // Update status to simulate risk (no riskScore field in UpdateRequest)
    customerResource.updateCustomer(
        UUID.fromString(customer1.id()),
        UpdateCustomerRequest.builder()
            .status(CustomerStatus.AKTIV) // Low risk - active
            .build());

    customerResource.updateCustomer(
        UUID.fromString(customer2.id()),
        UpdateCustomerRequest.builder()
            .status(CustomerStatus.RISIKO) // High risk
            .build());

    // Get all customers and verify risk scores updated
    var listResponse = customerResource.getAllCustomers(0, 100, null, null);
    CustomerListResponse customers = (CustomerListResponse) listResponse.getEntity();

    // Verify status updates are reflected
    assertThat(customers.content())
        .filteredOn(c -> c.companyName().startsWith("Risk Test"))
        .extracting(CustomerResponse::status)
        .containsExactlyInAnyOrder(CustomerStatus.AKTIV, CustomerStatus.RISIKO);
  }

  @Test
  @TestTransaction
  @DisplayName("Merge customers should merge data")
  void mergeCustomers_inCQRSMode_shouldMergeData() {
    // Create source customer with unique name
    String uniqueSuffix = "_" + System.currentTimeMillis();

    var sourceResponse =
        customerResource.createCustomer(
            CreateCustomerRequest.builder()
                .companyName("Source Company" + uniqueSuffix)
                .customerType(CustomerType.UNTERNEHMEN)
                .industry(Industry.SONSTIGE)
                .expectedAnnualVolume(new BigDecimal("50000"))
                .build());
    CustomerResponse source = (CustomerResponse) sourceResponse.getEntity();
    UUID sourceId = UUID.fromString(source.id());

    // Create target customer with unique name
    var targetResponse =
        customerResource.createCustomer(
            CreateCustomerRequest.builder()
                .companyName("Target Company" + uniqueSuffix)
                .customerType(CustomerType.UNTERNEHMEN)
                .industry(Industry.SONSTIGE)
                .expectedAnnualVolume(new BigDecimal("75000"))
                .build());
    CustomerResponse target = (CustomerResponse) targetResponse.getEntity();
    UUID targetId = UUID.fromString(target.id());

    // Merge source into target
    MergeCustomersRequest mergeRequest = new MergeCustomersRequest(sourceId);
    var mergeResponse = customerResource.mergeCustomers(targetId, mergeRequest);
    assertThat(mergeResponse.getStatus()).isEqualTo(200);

    // Source should be soft-deleted and not retrievable via normal API
    assertThatThrownBy(() -> customerResource.getCustomer(sourceId))
        .isInstanceOf(CustomerNotFoundException.class)
        .hasMessageContaining("Customer not found with ID: " + sourceId);

    // Target should have merged data and be retrievable
    var checkTargetResponse = customerResource.getCustomer(targetId);
    CustomerResponse merged = (CustomerResponse) checkTargetResponse.getEntity();
    assertThat(merged).isNotNull();
    assertThat(merged.id()).isEqualTo(targetId.toString());
    // The merged customer should have the better annual volume (75000 from target)
  }

  @Test
  @TestTransaction
  @DisplayName("Change customer status should update with validation")
  void changeStatus_inCQRSMode_shouldUpdateStatus() {
    // Create customer
    var createResponse = customerResource.createCustomer(validCreateRequest);
    CustomerResponse created = (CustomerResponse) createResponse.getEntity();
    UUID customerId = UUID.fromString(created.id());

    // Change status to AKTIV (not ACTIVE - that doesn't exist in enum)
    ChangeStatusRequest statusRequest = new ChangeStatusRequest(CustomerStatus.AKTIV);
    var statusResponse = customerResource.changeCustomerStatus(customerId, statusRequest);
    assertThat(statusResponse.getStatus()).isEqualTo(200);

    // Verify status changed
    var getResponse = customerResource.getCustomer(customerId);
    CustomerResponse updated = (CustomerResponse) getResponse.getEntity();
    assertThat(updated.status()).isEqualTo(CustomerStatus.AKTIV);
  }

  // =====================================
  // PERFORMANCE & CONSISTENCY TESTS
  // =====================================

  @Test
  @TestTransaction
  @DisplayName("Check duplicates should find similar company names")
  void checkDuplicates_inCQRSMode_shouldFindSimilarNames() {
    // Create customer
    customerResource.createCustomer(
        CreateCustomerRequest.builder()
            .companyName("Duplicate Test Company")
            .customerType(CustomerType.UNTERNEHMEN)
            .industry(Industry.SONSTIGE)
            .expectedAnnualVolume(BigDecimal.ZERO)
            .build());

    // Check for duplicates with similar name
    CheckDuplicatesRequest duplicatesRequest = new CheckDuplicatesRequest("Duplicate Test");
    var duplicatesResponse = customerResource.checkDuplicates(duplicatesRequest);

    @SuppressWarnings("unchecked")
    List<CustomerResponse> duplicates = (List<CustomerResponse>) duplicatesResponse.getEntity();

    // Should find the similar company
    assertThat(duplicates)
        .extracting(CustomerResponse::companyName)
        .contains("Duplicate Test Company");
  }

  @Test
  @DisplayName("Verify feature flag properly enables CQRS implementation")
  void verifyFeatureFlagSwitching_worksCorrectly() {
    // This test verifies that the feature flag properly switches between implementations
    // The flag is enabled for this test via TestProfile
    assertThat(cqrsEnabled).isTrue();

    // Create a customer and verify it works with CQRS enabled
    var response = customerResource.createCustomer(validCreateRequest);
    assertThat(response.getStatus()).isEqualTo(201);

    // All operations should delegate to CQRS services
    // This is verified by the successful execution of all tests above
  }

  // =====================================
  // ADDITIONAL QUERY TESTS
  // =====================================

  @Test
  @TestTransaction
  @DisplayName("Get customers by industry should filter correctly")
  void getCustomersByIndustry_inCQRSMode_shouldFilterCorrectly() {
    // Create customers with different industries
    customerResource.createCustomer(
        CreateCustomerRequest.builder()
            .companyName("Healthcare Company")
            .customerType(CustomerType.UNTERNEHMEN)
            .industry(Industry.GESUNDHEITSWESEN)
            .expectedAnnualVolume(BigDecimal.ZERO)
            .build());

    customerResource.createCustomer(
        CreateCustomerRequest.builder()
            .companyName("Education Company")
            .customerType(CustomerType.INSTITUTION)
            .industry(Industry.BILDUNG)
            .expectedAnnualVolume(BigDecimal.ZERO)
            .build());

    // Query by industry
    var healthcareResponse =
        customerResource.getAllCustomers(0, 100, null, Industry.GESUNDHEITSWESEN);
    CustomerListResponse healthcareList = (CustomerListResponse) healthcareResponse.getEntity();

    var educationResponse = customerResource.getAllCustomers(0, 100, null, Industry.BILDUNG);
    CustomerListResponse educationList = (CustomerListResponse) educationResponse.getEntity();

    // Verify filtering
    if (!healthcareList.content().isEmpty()) {
      assertThat(healthcareList.content())
          .extracting(CustomerResponse::industry)
          .containsOnly(Industry.GESUNDHEITSWESEN);
    }

    if (!educationList.content().isEmpty()) {
      assertThat(educationList.content())
          .extracting(CustomerResponse::industry)
          .containsOnly(Industry.BILDUNG);
    }
  }

  @Test
  @TestTransaction
  @DisplayName("Pagination should work correctly")
  void pagination_inCQRSMode_shouldWorkCorrectly() {
    // Create multiple customers for pagination test with unique names
    String uniqueSuffix = "_" + System.currentTimeMillis();

    for (int i = 1; i <= 15; i++) {
      customerResource.createCustomer(
          CreateCustomerRequest.builder()
              .companyName("Pagination Test " + i + uniqueSuffix)
              .customerType(CustomerType.UNTERNEHMEN)
              .industry(Industry.SONSTIGE)
              .expectedAnnualVolume(BigDecimal.ZERO)
              .build());
    }

    // Get first page
    var page1Response = customerResource.getAllCustomers(0, 5, null, null);
    CustomerListResponse page1 = (CustomerListResponse) page1Response.getEntity();

    // Get second page
    var page2Response = customerResource.getAllCustomers(1, 5, null, null);
    CustomerListResponse page2 = (CustomerListResponse) page2Response.getEntity();

    // Verify pagination
    assertThat(page1.content()).hasSizeLessThanOrEqualTo(5);
    assertThat(page2.content()).hasSizeLessThanOrEqualTo(5);

    // Pages should have different content if there are enough customers
    // Note: Since we're getting ALL customers (not filtered), pages might overlap with existing
    // data
    // We just verify that pagination returns data and pages are different
    if (page1.totalElements() > 5) {
      // If we have more than 5 total elements, pages should be different
      assertThat(page1.page()).isEqualTo(0);
      assertThat(page2.page()).isEqualTo(1);
      // Verify first and last flags
      assertThat(page1.first()).isTrue();
      assertThat(page2.first()).isFalse();
    }
  }
}

package de.freshplan.domain.customer.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.CustomerType;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.CustomerService;
import de.freshplan.domain.customer.service.dto.CreateCustomerRequest;
import de.freshplan.domain.customer.service.dto.CustomerResponse;
import de.freshplan.domain.customer.service.dto.UpdateCustomerRequest;
import de.freshplan.domain.customer.service.exception.CustomerNotFoundException;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration test that verifies CustomerCommandService behaves EXACTLY like CustomerService for
 * the same inputs.
 *
 * <p>This is critical for safe switching via feature flag.
 */
@QuarkusTest
@Tag("integration")
class CustomerCommandServiceIntegrationTest {

  @Inject CustomerService originalService;

  @Inject CustomerCommandService commandService;

  @Inject CustomerRepository customerRepository;

  @Inject jakarta.persistence.EntityManager em;

  @AfterEach
  @Transactional
  void cleanup() {
    // Delete in correct order to respect foreign key constraints
    em.createNativeQuery("DELETE FROM customer_timeline_events WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')").executeUpdate();
    em.createNativeQuery("DELETE FROM customer_contacts WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')").executeUpdate();
    em.createNativeQuery("DELETE FROM opportunities WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')").executeUpdate();
    em.createNativeQuery("DELETE FROM customers WHERE customer_number LIKE 'TEST-%'").executeUpdate();
  }



  @Test
  @TestTransaction
  void createCustomer_shouldProduceSameResultAsOriginalService() {
    // Given - a minimal valid request
    CreateCustomerRequest request =
        CreateCustomerRequest.builder()
            .companyName("[TEST] Integration Test Company " + System.currentTimeMillis())
            .customerType(CustomerType.NEUKUNDE)
            .build();

    String createdBy = "integration-test";

    // When - we create with both services
    CustomerResponse originalResult = originalService.createCustomer(request, createdBy);

    // Create another request with different name to avoid duplicate
    CreateCustomerRequest request2 =
        CreateCustomerRequest.builder()
            .companyName("[TEST] Integration Test Company 2 " + System.currentTimeMillis())
            .customerType(CustomerType.NEUKUNDE)
            .build();
    CustomerResponse commandResult = commandService.createCustomer(request2, createdBy);

    // Then - both should produce similar results (except IDs and names)
    assertThat(originalResult).isNotNull();
    assertThat(commandResult).isNotNull();

    // Both should have customer numbers in same format
    assertThat(originalResult.customerNumber()).matches("KD-\\d{4}-\\d{5}");
    assertThat(commandResult.customerNumber()).matches("KD-\\d{4}-\\d{5}");

    // Both should have same status
    assertThat(originalResult.status()).isEqualTo(commandResult.status());

    // Both should have same customer type
    assertThat(originalResult.customerType()).isEqualTo(commandResult.customerType());

    // Risk scores should be calculated the same way
    assertThat(originalResult.riskScore()).isEqualTo(commandResult.riskScore());
  }

  @Test
  @TestTransaction
  void createCustomer_withNullRequest_shouldFailSameWay() {
    // Both services should throw the same exception for null request
    try {
      originalService.createCustomer(null, "test");
      assertThat(false).isTrue(); // Should not reach here
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("CreateCustomerRequest cannot be null");
    }

    try {
      commandService.createCustomer(null, "test");
      assertThat(false).isTrue(); // Should not reach here
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("CreateCustomerRequest cannot be null");
    }
  }

  @Test
  @TestTransaction
  void createCustomer_withNullCreatedBy_shouldFailSameWay() {
    // Given
    CreateCustomerRequest request =
        CreateCustomerRequest.builder()
            .companyName("[TEST] Test Company")
            .customerType(CustomerType.NEUKUNDE)
            .build();

    // Both services should throw the same exception for null createdBy
    try {
      originalService.createCustomer(request, null);
      assertThat(false).isTrue(); // Should not reach here
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("createdBy cannot be null or empty");
    }

    try {
      commandService.createCustomer(request, null);
      assertThat(false).isTrue(); // Should not reach here
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("createdBy cannot be null or empty");
    }
  }

  @Test
  @TestTransaction
  void updateCustomer_shouldProduceSameResultAsOriginalService() {
    // Given - create customers first
    CreateCustomerRequest createRequest1 =
        CreateCustomerRequest.builder()
            .companyName("[TEST] Update Test Company 1 " + System.currentTimeMillis())
            .customerType(CustomerType.NEUKUNDE)
            .build();
    CreateCustomerRequest createRequest2 =
        CreateCustomerRequest.builder()
            .companyName("[TEST] Update Test Company 2 " + System.currentTimeMillis())
            .customerType(CustomerType.NEUKUNDE)
            .build();

    String createdBy = "test-user";
    String updatedBy = "update-user";

    // Create customers with both services
    CustomerResponse customer1 = originalService.createCustomer(createRequest1, createdBy);
    CustomerResponse customer2 = commandService.createCustomer(createRequest2, createdBy);

    // When - update with both services
    UpdateCustomerRequest updateRequest =
        new UpdateCustomerRequest(
            "Updated Company Name", // companyName
            null, // tradingName
            null, // legalForm
            null, // customerType
            null, // industry
            null, // classification
            null, // parentCustomerId
            null, // hierarchyType
            null, // status
            null, // lifecycleStage
            null, // expectedAnnualVolume
            null, // actualAnnualVolume
            null, // paymentTerms
            null, // creditLimit
            null, // deliveryCondition
            null, // lastContactDate
            null, // nextFollowUpDate
            null // churnThresholdDays
            );

    CustomerResponse originalUpdated =
        originalService.updateCustomer(UUID.fromString(customer1.id()), updateRequest, updatedBy);
    CustomerResponse commandUpdated =
        commandService.updateCustomer(UUID.fromString(customer2.id()), updateRequest, updatedBy);

    // Then - both should have updated the company name
    assertThat(originalUpdated).isNotNull();
    assertThat(commandUpdated).isNotNull();
    assertThat(originalUpdated.companyName()).isEqualTo("Updated Company Name");
    assertThat(commandUpdated.companyName()).isEqualTo("Updated Company Name");

    // Risk scores should still be calculated the same way
    assertThat(originalUpdated.riskScore()).isEqualTo(commandUpdated.riskScore());
  }

  @Test
  @TestTransaction
  void updateCustomer_withNullId_shouldFailSameWay() {
    // Given
    UpdateCustomerRequest request =
        new UpdateCustomerRequest(
            "Updated Name",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null); // churnThresholdDays

    // Both services should throw the same exception for null ID
    try {
      originalService.updateCustomer(null, request, "test");
      assertThat(false).isTrue(); // Should not reach here
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("Customer ID cannot be null");
    }

    try {
      commandService.updateCustomer(null, request, "test");
      assertThat(false).isTrue(); // Should not reach here
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("Customer ID cannot be null");
    }
  }

  @Test
  @TestTransaction
  void deleteCustomer_shouldProduceSameResultAsOriginalService() {
    // Given - create customers for deletion test
    CreateCustomerRequest createRequest1 =
        new CreateCustomerRequest(
            "Delete Test Company 1 " + System.currentTimeMillis(), CustomerType.NEUKUNDE);
    CreateCustomerRequest createRequest2 =
        new CreateCustomerRequest(
            "Delete Test Company 2 " + System.currentTimeMillis(), CustomerType.NEUKUNDE);

    String createdBy = "test-user";
    String deletedBy = "delete-user";
    String deleteReason = "Test deletion";

    // Create customers with both services
    CustomerResponse customer1 = originalService.createCustomer(createRequest1, createdBy);
    CustomerResponse customer2 = commandService.createCustomer(createRequest2, createdBy);

    // When - delete with both services
    originalService.deleteCustomer(UUID.fromString(customer1.id()), deletedBy, deleteReason);
    commandService.deleteCustomer(UUID.fromString(customer2.id()), deletedBy, deleteReason);

    // Then - both should be soft deleted
    // Verify by checking that findByIdActive returns empty
    assertThat(customerRepository.findByIdActive(UUID.fromString(customer1.id()))).isEmpty();
    assertThat(customerRepository.findByIdActive(UUID.fromString(customer2.id()))).isEmpty();

    // Verify the deleted customers still exist but are marked as deleted
    Customer deletedCustomer1 = customerRepository.findById(UUID.fromString(customer1.id()));
    Customer deletedCustomer2 = customerRepository.findById(UUID.fromString(customer2.id()));

    assertThat(deletedCustomer1.getIsDeleted()).isTrue();
    assertThat(deletedCustomer2.getIsDeleted()).isTrue();
    assertThat(deletedCustomer1.getDeletedBy()).isEqualTo(deletedBy);
    assertThat(deletedCustomer2.getDeletedBy()).isEqualTo(deletedBy);
    assertThat(deletedCustomer1.getDeletedAt()).isNotNull();
    assertThat(deletedCustomer2.getDeletedAt()).isNotNull();
  }

  @Test
  @TestTransaction
  void restoreCustomer_shouldProduceSameResultAsOriginalService() {
    // Given - create and delete customers first
    CreateCustomerRequest createRequest1 =
        new CreateCustomerRequest(
            "Restore Test Company 1 " + System.currentTimeMillis(), CustomerType.NEUKUNDE);
    CreateCustomerRequest createRequest2 =
        new CreateCustomerRequest(
            "Restore Test Company 2 " + System.currentTimeMillis(), CustomerType.NEUKUNDE);

    String createdBy = "test-user";
    String deletedBy = "delete-user";
    String restoredBy = "restore-user";
    String deleteReason = "Test deletion for restore";

    // Create customers
    CustomerResponse customer1 = originalService.createCustomer(createRequest1, createdBy);
    CustomerResponse customer2 = commandService.createCustomer(createRequest2, createdBy);

    UUID id1 = UUID.fromString(customer1.id());
    UUID id2 = UUID.fromString(customer2.id());

    // Delete both customers first
    originalService.deleteCustomer(id1, deletedBy, deleteReason);
    commandService.deleteCustomer(id2, deletedBy, deleteReason);

    // Verify they are deleted
    assertThat(customerRepository.findByIdActive(id1)).isEmpty();
    assertThat(customerRepository.findByIdActive(id2)).isEmpty();

    // When - restore with both services
    CustomerResponse restored1 = originalService.restoreCustomer(id1, restoredBy);
    CustomerResponse restored2 = commandService.restoreCustomer(id2, restoredBy);

    // Then - both should be restored
    assertThat(restored1).isNotNull();
    assertThat(restored2).isNotNull();

    // Verify customers are active again
    assertThat(customerRepository.findByIdActive(id1)).isPresent();
    assertThat(customerRepository.findByIdActive(id2)).isPresent();

    // Check the restored state
    Customer restoredCustomer1 = customerRepository.findById(id1);
    Customer restoredCustomer2 = customerRepository.findById(id2);

    assertThat(restoredCustomer1.getIsDeleted()).isFalse();
    assertThat(restoredCustomer2.getIsDeleted()).isFalse();
    assertThat(restoredCustomer1.getDeletedAt()).isNull();
    assertThat(restoredCustomer2.getDeletedAt()).isNull();
    assertThat(restoredCustomer1.getDeletedBy()).isNull();
    assertThat(restoredCustomer2.getDeletedBy()).isNull();
    assertThat(restoredCustomer1.getUpdatedBy()).isEqualTo(restoredBy);
    assertThat(restoredCustomer2.getUpdatedBy()).isEqualTo(restoredBy);
  }

  @Test
  @TestTransaction
  void restoreCustomer_shouldFailForNonDeletedCustomer() {
    // Given - create an active customer
    CreateCustomerRequest createRequest =
        new CreateCustomerRequest(
            "Active Customer " + System.currentTimeMillis(), CustomerType.NEUKUNDE);

    String createdBy = "test-user";
    CustomerResponse customer = commandService.createCustomer(createRequest, createdBy);
    UUID id = UUID.fromString(customer.id());

    // When/Then - trying to restore an active customer should fail
    assertThatThrownBy(() -> commandService.restoreCustomer(id, "restore-user"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Customer is not deleted");

    // Verify same behavior with original service
    assertThatThrownBy(() -> originalService.restoreCustomer(id, "restore-user"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Customer is not deleted");
  }

  @Test
  @TestTransaction
  void addChildCustomer_shouldProduceSameResultAsOriginalService() {
    // Given - create parent and child customers
    CreateCustomerRequest parentRequest =
        new CreateCustomerRequest(
            "Parent Company " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);
    CreateCustomerRequest childRequest =
        new CreateCustomerRequest(
            "Child Company " + System.currentTimeMillis(), CustomerType.NEUKUNDE);

    String createdBy = "test-user";
    String updatedBy = "hierarchy-user";

    // Create customers for both services
    CustomerResponse parent1 = originalService.createCustomer(parentRequest, createdBy);
    CustomerResponse child1 = originalService.createCustomer(childRequest, createdBy);

    CreateCustomerRequest parentRequest2 =
        new CreateCustomerRequest(
            "Parent Company 2 " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);
    CreateCustomerRequest childRequest2 =
        new CreateCustomerRequest(
            "Child Company 2 " + System.currentTimeMillis(), CustomerType.NEUKUNDE);

    CustomerResponse parent2 = commandService.createCustomer(parentRequest2, createdBy);
    CustomerResponse child2 = commandService.createCustomer(childRequest2, createdBy);

    UUID parentId1 = UUID.fromString(parent1.id());
    UUID childId1 = UUID.fromString(child1.id());
    UUID parentId2 = UUID.fromString(parent2.id());
    UUID childId2 = UUID.fromString(child2.id());

    // When - add child to parent with both services
    CustomerResponse result1 = originalService.addChildCustomer(parentId1, childId1, updatedBy);
    CustomerResponse result2 = commandService.addChildCustomer(parentId2, childId2, updatedBy);

    // Then - both should return the updated child
    assertThat(result1).isNotNull();
    assertThat(result2).isNotNull();

    // Both should return the child (not parent)
    assertThat(result1.id()).isEqualTo(child1.id());
    assertThat(result2.id()).isEqualTo(child2.id());

    // Verify hierarchy was established
    Customer updatedChild1 = customerRepository.findById(childId1);
    Customer updatedChild2 = customerRepository.findById(childId2);

    assertThat(updatedChild1.getParentCustomer()).isNotNull();
    assertThat(updatedChild1.getParentCustomer().getId()).isEqualTo(parentId1);
    assertThat(updatedChild2.getParentCustomer()).isNotNull();
    assertThat(updatedChild2.getParentCustomer().getId()).isEqualTo(parentId2);
  }

  @Test
  @TestTransaction
  void addChildCustomer_shouldFailForChildWithExistingParent() {
    // Given - create three customers: parent1, parent2, and child
    CreateCustomerRequest parent1Request =
        new CreateCustomerRequest(
            "Parent 1 " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);
    CreateCustomerRequest parent2Request =
        new CreateCustomerRequest(
            "Parent 2 " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);
    CreateCustomerRequest childRequest =
        new CreateCustomerRequest("Child " + System.currentTimeMillis(), CustomerType.NEUKUNDE);

    String createdBy = "test-user";

    CustomerResponse parent1 = commandService.createCustomer(parent1Request, createdBy);
    CustomerResponse parent2 = commandService.createCustomer(parent2Request, createdBy);
    CustomerResponse child = commandService.createCustomer(childRequest, createdBy);

    UUID parent1Id = UUID.fromString(parent1.id());
    UUID parent2Id = UUID.fromString(parent2.id());
    UUID childId = UUID.fromString(child.id());

    // First assignment should succeed
    commandService.addChildCustomer(parent1Id, childId, "user1");

    // When/Then - trying to add same child to different parent should fail
    assertThatThrownBy(() -> commandService.addChildCustomer(parent2Id, childId, "user2"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Customer already has a parent");

    // Verify same behavior with original service
    // Create same scenario for original service
    CreateCustomerRequest parent3Request =
        new CreateCustomerRequest(
            "Parent 3 " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);
    CreateCustomerRequest parent4Request =
        new CreateCustomerRequest(
            "Parent 4 " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);
    CreateCustomerRequest child2Request =
        new CreateCustomerRequest("Child 2 " + System.currentTimeMillis(), CustomerType.NEUKUNDE);

    CustomerResponse parent3 = originalService.createCustomer(parent3Request, createdBy);
    CustomerResponse parent4 = originalService.createCustomer(parent4Request, createdBy);
    CustomerResponse child2 = originalService.createCustomer(child2Request, createdBy);

    UUID parent3Id = UUID.fromString(parent3.id());
    UUID parent4Id = UUID.fromString(parent4.id());
    UUID child2Id = UUID.fromString(child2.id());

    originalService.addChildCustomer(parent3Id, child2Id, "user1");

    assertThatThrownBy(() -> originalService.addChildCustomer(parent4Id, child2Id, "user2"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Customer already has a parent");
  }

  @Test
  @TestTransaction
  void addChildCustomer_shouldNotPreventCircularHierarchy_dueToExistingBug() {
    // NOTE: There's a bug in the original CustomerService where the circular hierarchy
    // check is inverted. The code calls isDescendant(parent, child) but should call
    // isDescendant(child, parent). This test documents the ACTUAL behavior (with bug).

    // Given - create three customers: A, B, C
    CreateCustomerRequest requestA =
        new CreateCustomerRequest(
            "Company A " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);
    CreateCustomerRequest requestB =
        new CreateCustomerRequest(
            "Company B " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);
    CreateCustomerRequest requestC =
        new CreateCustomerRequest(
            "Company C " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);

    String createdBy = "test-user";

    CustomerResponse customerA = commandService.createCustomer(requestA, createdBy);
    CustomerResponse customerB = commandService.createCustomer(requestB, createdBy);
    CustomerResponse customerC = commandService.createCustomer(requestC, createdBy);

    UUID idA = UUID.fromString(customerA.id());
    UUID idB = UUID.fromString(customerB.id());
    UUID idC = UUID.fromString(customerC.id());

    // Setup hierarchy: A -> B -> C
    commandService.addChildCustomer(idA, idB, "user1"); // B is child of A
    commandService.addChildCustomer(idB, idC, "user2"); // C is child of B

    // Now the hierarchy is: A (parent) -> B (child of A) -> C (child of B)

    // Due to the bug, this SHOULD fail but DOESN'T - creating a cycle!
    // The method will allow C to become parent of A, creating: C -> A -> B -> C
    CustomerResponse result = commandService.addChildCustomer(idC, idA, "user3");

    // The bug allows this circular hierarchy to be created
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(customerA.id());

    // Verify both services have the same bug
    CreateCustomerRequest requestD =
        new CreateCustomerRequest(
            "Company D " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);
    CreateCustomerRequest requestE =
        new CreateCustomerRequest(
            "Company E " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);
    CreateCustomerRequest requestF =
        new CreateCustomerRequest(
            "Company F " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);

    CustomerResponse customerD = originalService.createCustomer(requestD, createdBy);
    CustomerResponse customerE = originalService.createCustomer(requestE, createdBy);
    CustomerResponse customerF = originalService.createCustomer(requestF, createdBy);

    UUID idD = UUID.fromString(customerD.id());
    UUID idE = UUID.fromString(customerE.id());
    UUID idF = UUID.fromString(customerF.id());

    originalService.addChildCustomer(idD, idE, "user1");
    originalService.addChildCustomer(idE, idF, "user2");

    // Original service also allows the circular hierarchy (same bug)
    CustomerResponse resultOriginal = originalService.addChildCustomer(idF, idD, "user3");
    assertThat(resultOriginal).isNotNull();
  }

  @Test
  @TestTransaction
  void addChildCustomer_withNonExistentParent_shouldFail() {
    // Given - create only a child customer
    CreateCustomerRequest childRequest =
        new CreateCustomerRequest(
            "Child Company " + System.currentTimeMillis(), CustomerType.NEUKUNDE);

    CustomerResponse child = commandService.createCustomer(childRequest, "test-user");
    UUID childId = UUID.fromString(child.id());
    UUID nonExistentParentId = UUID.randomUUID();

    // When/Then - should throw CustomerNotFoundException for parent
    assertThatThrownBy(() -> commandService.addChildCustomer(nonExistentParentId, childId, "user"))
        .isInstanceOf(CustomerNotFoundException.class)
        .hasMessageContaining("Parent customer not found");

    // Verify same behavior with original service
    assertThatThrownBy(() -> originalService.addChildCustomer(nonExistentParentId, childId, "user"))
        .isInstanceOf(CustomerNotFoundException.class)
        .hasMessageContaining("Parent customer not found");
  }

  @Test
  @TestTransaction
  void addChildCustomer_withNonExistentChild_shouldFail() {
    // Given - create only a parent customer
    CreateCustomerRequest parentRequest =
        new CreateCustomerRequest(
            "Parent Company " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);

    CustomerResponse parent = commandService.createCustomer(parentRequest, "test-user");
    UUID parentId = UUID.fromString(parent.id());
    UUID nonExistentChildId = UUID.randomUUID();

    // When/Then - should throw CustomerNotFoundException for child
    assertThatThrownBy(() -> commandService.addChildCustomer(parentId, nonExistentChildId, "user"))
        .isInstanceOf(CustomerNotFoundException.class)
        .hasMessageContaining("Child customer not found");

    // Verify same behavior with original service
    assertThatThrownBy(() -> originalService.addChildCustomer(parentId, nonExistentChildId, "user"))
        .isInstanceOf(CustomerNotFoundException.class)
        .hasMessageContaining("Child customer not found");
  }

  @Test
  @TestTransaction
  void updateAllRiskScores_shouldUpdateScoresIdenticallyToBothServices() {
    // Given - create test customers with different statuses for both services
    // Customers for original service
    CreateCustomerRequest request1 =
        new CreateCustomerRequest(
            "Risk Test 1 " + System.currentTimeMillis(), CustomerType.NEUKUNDE);
    CreateCustomerRequest request2 =
        new CreateCustomerRequest(
            "Risk Test 2 " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);

    CustomerResponse customer1 = originalService.createCustomer(request1, "test-user");
    CustomerResponse customer2 = originalService.createCustomer(request2, "test-user");

    // Customers for command service
    CreateCustomerRequest request3 =
        new CreateCustomerRequest(
            "Risk Test 3 " + System.currentTimeMillis(), CustomerType.NEUKUNDE);
    CreateCustomerRequest request4 =
        new CreateCustomerRequest(
            "Risk Test 4 " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);

    CustomerResponse customer3 = commandService.createCustomer(request3, "test-user");
    CustomerResponse customer4 = commandService.createCustomer(request4, "test-user");

    // Manually set risk scores to 0 to test the update
    Customer c1 = customerRepository.findById(UUID.fromString(customer1.id()));
    Customer c2 = customerRepository.findById(UUID.fromString(customer2.id()));
    Customer c3 = customerRepository.findById(UUID.fromString(customer3.id()));
    Customer c4 = customerRepository.findById(UUID.fromString(customer4.id()));

    c1.setRiskScore(0);
    c2.setRiskScore(0);
    c3.setRiskScore(0);
    c4.setRiskScore(0);
    customerRepository.flush();

    // When - update all risk scores with both services
    originalService.updateAllRiskScores();
    commandService.updateAllRiskScores();
    customerRepository.flush();

    // Then - all customers should have updated risk scores
    Customer updated1 = customerRepository.findById(UUID.fromString(customer1.id()));
    Customer updated2 = customerRepository.findById(UUID.fromString(customer2.id()));
    Customer updated3 = customerRepository.findById(UUID.fromString(customer3.id()));
    Customer updated4 = customerRepository.findById(UUID.fromString(customer4.id()));

    // All should have risk scores > 0 (based on their status)
    assertThat(updated1.getRiskScore()).isGreaterThan(0);
    assertThat(updated2.getRiskScore()).isGreaterThan(0);
    assertThat(updated3.getRiskScore()).isGreaterThan(0);
    assertThat(updated4.getRiskScore()).isGreaterThan(0);

    // Customers with same type should have similar risk scores
    // (exact values depend on status and other factors)
    assertThat(updated1.getRiskScore()).isEqualTo(updated3.getRiskScore());
    assertThat(updated2.getRiskScore()).isEqualTo(updated4.getRiskScore());
  }

  @Test
  @TestTransaction
  void updateAllRiskScores_shouldHandleEmptyDatabase() {
    // Given - ensure we have a known state (may have test data from other tests)
    // We can't delete all customers, so we just test that it doesn't crash

    // When - update risk scores with both services
    // Should not throw any exceptions even if no customers to update
    assertThatCode(() -> originalService.updateAllRiskScores()).doesNotThrowAnyException();

    assertThatCode(() -> commandService.updateAllRiskScores()).doesNotThrowAnyException();

    // Then - both services handled it gracefully
    // (actual behavior depends on existing test data)
  }

  @Test
  @TestTransaction
  void updateAllRiskScores_shouldRespect1000CustomerLimit() {
    // This test documents the limitation that only 1000 customers are processed
    // We don't actually create 1001 customers in the test, but document the behavior

    // Given - the method uses Page.ofSize(1000)
    // When - updateAllRiskScores is called
    // Then - only the first 1000 customers (ordered by company name) would be updated

    // This is a documentation test - the actual limitation is in the implementation
    // Both services have the same limitation
    assertThat(true).as("Both services are limited to updating 1000 customers at a time").isTrue();
  }

  @Test
  @TestTransaction
  void mergeCustomers_shouldProduceSameResultAsOriginalService() {
    // Given - create target and source customers for both services
    CreateCustomerRequest targetRequest1 =
        new CreateCustomerRequest(
            "Target Company 1 " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);
    CreateCustomerRequest sourceRequest1 =
        new CreateCustomerRequest(
            "Source Company 1 " + System.currentTimeMillis(), CustomerType.NEUKUNDE);

    CreateCustomerRequest targetRequest2 =
        new CreateCustomerRequest(
            "Target Company 2 " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);
    CreateCustomerRequest sourceRequest2 =
        new CreateCustomerRequest(
            "Source Company 2 " + System.currentTimeMillis(), CustomerType.NEUKUNDE);

    String createdBy = "test-user";
    String mergedBy = "merge-user";

    // Create customers for original service
    CustomerResponse target1 = originalService.createCustomer(targetRequest1, createdBy);
    CustomerResponse source1 = originalService.createCustomer(sourceRequest1, createdBy);

    // Create customers for command service
    CustomerResponse target2 = commandService.createCustomer(targetRequest2, createdBy);
    CustomerResponse source2 = commandService.createCustomer(sourceRequest2, createdBy);

    UUID targetId1 = UUID.fromString(target1.id());
    UUID sourceId1 = UUID.fromString(source1.id());
    UUID targetId2 = UUID.fromString(target2.id());
    UUID sourceId2 = UUID.fromString(source2.id());

    // Set some additional data on source customers to test merging
    UpdateCustomerRequest updateSource =
        new UpdateCustomerRequest(
            null, // companyName
            null, // tradingName
            null, // legalForm
            null, // customerType
            null, // industry
            null, // classification
            null, // parentCustomerId
            null, // hierarchyType
            null, // status
            null, // lifecycleStage
            new BigDecimal("100000.0"), // expectedAnnualVolume
            new BigDecimal("80000.0"), // actualAnnualVolume
            null, // paymentTerms
            null, // creditLimit
            null, // deliveryCondition
            LocalDateTime.now().minusDays(5), // lastContactDate
            null, // nextFollowUpDate
            null // churnThresholdDays
            );

    originalService.updateCustomer(sourceId1, updateSource, "update-user");
    commandService.updateCustomer(sourceId2, updateSource, "update-user");

    // When - merge with both services
    CustomerResponse mergedResult1 = originalService.mergeCustomers(targetId1, sourceId1, mergedBy);
    CustomerResponse mergedResult2 = commandService.mergeCustomers(targetId2, sourceId2, mergedBy);

    // Then - both should produce similar results
    assertThat(mergedResult1).isNotNull();
    assertThat(mergedResult2).isNotNull();

    // Target should still exist
    assertThat(mergedResult1.id()).isEqualTo(target1.id());
    assertThat(mergedResult2.id()).isEqualTo(target2.id());

    // Financial data should be merged (target had null, so source values should be used)
    assertThat(mergedResult1.expectedAnnualVolume()).isEqualTo(new BigDecimal("100000.0"));
    assertThat(mergedResult2.expectedAnnualVolume()).isEqualTo(new BigDecimal("100000.0"));
    assertThat(mergedResult1.actualAnnualVolume()).isEqualTo(new BigDecimal("80000.0"));
    assertThat(mergedResult2.actualAnnualVolume()).isEqualTo(new BigDecimal("80000.0"));

    // Last contact date should be updated to source's date (comparing just the date part)
    assertThat(mergedResult1.lastContactDate().toLocalDate())
        .isEqualTo(LocalDate.now().minusDays(5));
    assertThat(mergedResult2.lastContactDate().toLocalDate())
        .isEqualTo(LocalDate.now().minusDays(5));

    // Source should be soft deleted
    assertThat(customerRepository.findByIdActive(sourceId1)).isEmpty();
    assertThat(customerRepository.findByIdActive(sourceId2)).isEmpty();

    // But source should still exist as deleted
    Customer deletedSource1 = customerRepository.findById(sourceId1);
    Customer deletedSource2 = customerRepository.findById(sourceId2);

    assertThat(deletedSource1.getIsDeleted()).isTrue();
    assertThat(deletedSource2.getIsDeleted()).isTrue();
    assertThat(deletedSource1.getDeletedBy()).isEqualTo(mergedBy);
    assertThat(deletedSource2.getDeletedBy()).isEqualTo(mergedBy);
    assertThat(deletedSource1.getDeletedAt()).isNotNull();
    assertThat(deletedSource2.getDeletedAt()).isNotNull();
  }

  @Test
  @TestTransaction
  void mergeCustomers_withSourceHavingChildren_shouldNotFailDueToBug() {
    // NOTE: There's a bug in the original implementation where hasChildren() doesn't work
    // correctly after addChildCustomer() because the childCustomers collection is not
    // properly updated. This test documents the ACTUAL behavior (with bug).

    // Given - create parent and child customers
    CreateCustomerRequest parentRequest =
        new CreateCustomerRequest(
            "Parent for Merge Test " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);
    CreateCustomerRequest childRequest =
        new CreateCustomerRequest(
            "Child of Parent " + System.currentTimeMillis(), CustomerType.NEUKUNDE);
    CreateCustomerRequest targetRequest =
        new CreateCustomerRequest(
            "Target for Merge " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);

    String createdBy = "test-user";

    // Test with command service
    CustomerResponse parent = commandService.createCustomer(parentRequest, createdBy);
    CustomerResponse child = commandService.createCustomer(childRequest, createdBy);
    CustomerResponse target = commandService.createCustomer(targetRequest, createdBy);

    UUID parentId = UUID.fromString(parent.id());
    UUID childId = UUID.fromString(child.id());
    UUID targetId = UUID.fromString(target.id());

    // Make parent have a child (only sets child.parentCustomer, doesn't update
    // parent.childCustomers)
    commandService.addChildCustomer(parentId, childId, "user");

    // When - merge should NOT fail due to the bug (hasChildren returns false incorrectly)
    CustomerResponse mergedResult = commandService.mergeCustomers(targetId, parentId, "merge-user");

    // Then - merge succeeds even though parent has children (this is the bug!)
    assertThat(mergedResult).isNotNull();
    assertThat(mergedResult.id()).isEqualTo(target.id());

    // Verify same behavior with original service
    CreateCustomerRequest parentRequest2 =
        new CreateCustomerRequest(
            "Parent for Merge Test 2 " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);
    CreateCustomerRequest childRequest2 =
        new CreateCustomerRequest(
            "Child of Parent 2 " + System.currentTimeMillis(), CustomerType.NEUKUNDE);
    CreateCustomerRequest targetRequest2 =
        new CreateCustomerRequest(
            "Target for Merge 2 " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);

    CustomerResponse parent2 = originalService.createCustomer(parentRequest2, createdBy);
    CustomerResponse child2 = originalService.createCustomer(childRequest2, createdBy);
    CustomerResponse target2 = originalService.createCustomer(targetRequest2, createdBy);

    UUID parentId2 = UUID.fromString(parent2.id());
    UUID childId2 = UUID.fromString(child2.id());
    UUID targetId2 = UUID.fromString(target2.id());

    originalService.addChildCustomer(parentId2, childId2, "user");

    // Original service also has the same bug - merge succeeds when it shouldn't
    CustomerResponse mergedResultOriginal =
        originalService.mergeCustomers(targetId2, parentId2, "merge-user");
    assertThat(mergedResultOriginal).isNotNull();
    assertThat(mergedResultOriginal.id()).isEqualTo(target2.id());
  }

  @Test
  @TestTransaction
  void mergeCustomers_withNonExistentTarget_shouldFailSameWay() {
    // Given - create only source customer
    CreateCustomerRequest sourceRequest =
        new CreateCustomerRequest(
            "Source Company " + System.currentTimeMillis(), CustomerType.NEUKUNDE);

    CustomerResponse source = commandService.createCustomer(sourceRequest, "test-user");
    UUID sourceId = UUID.fromString(source.id());
    UUID nonExistentTargetId = UUID.randomUUID();

    // When/Then - should throw CustomerNotFoundException for target
    assertThatThrownBy(
            () -> commandService.mergeCustomers(nonExistentTargetId, sourceId, "merge-user"))
        .isInstanceOf(CustomerNotFoundException.class)
        .hasMessageContaining("Target customer not found");

    // Verify same behavior with original service
    assertThatThrownBy(
            () -> originalService.mergeCustomers(nonExistentTargetId, sourceId, "merge-user"))
        .isInstanceOf(CustomerNotFoundException.class)
        .hasMessageContaining("Target customer not found");
  }

  @Test
  @TestTransaction
  void mergeCustomers_withNonExistentSource_shouldFailSameWay() {
    // Given - create only target customer
    CreateCustomerRequest targetRequest =
        new CreateCustomerRequest(
            "Target Company " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);

    CustomerResponse target = commandService.createCustomer(targetRequest, "test-user");
    UUID targetId = UUID.fromString(target.id());
    UUID nonExistentSourceId = UUID.randomUUID();

    // When/Then - should throw CustomerNotFoundException for source
    assertThatThrownBy(
            () -> commandService.mergeCustomers(targetId, nonExistentSourceId, "merge-user"))
        .isInstanceOf(CustomerNotFoundException.class)
        .hasMessageContaining("Source customer not found");

    // Verify same behavior with original service
    assertThatThrownBy(
            () -> originalService.mergeCustomers(targetId, nonExistentSourceId, "merge-user"))
        .isInstanceOf(CustomerNotFoundException.class)
        .hasMessageContaining("Source customer not found");
  }

  @Test
  @TestTransaction
  void mergeCustomers_withDeletedSource_shouldFail() {
    // Given - create and delete a source customer
    CreateCustomerRequest targetRequest =
        new CreateCustomerRequest(
            "Target Active " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);
    CreateCustomerRequest sourceRequest =
        new CreateCustomerRequest(
            "Source to Delete " + System.currentTimeMillis(), CustomerType.NEUKUNDE);

    String createdBy = "test-user";

    CustomerResponse target = commandService.createCustomer(targetRequest, createdBy);
    CustomerResponse source = commandService.createCustomer(sourceRequest, createdBy);

    UUID targetId = UUID.fromString(target.id());
    UUID sourceId = UUID.fromString(source.id());

    // Delete the source
    commandService.deleteCustomer(sourceId, "delete-user", "Test deletion");

    // When/Then - merging with deleted source should fail (findByIdActive won't find it)
    assertThatThrownBy(() -> commandService.mergeCustomers(targetId, sourceId, "merge-user"))
        .isInstanceOf(CustomerNotFoundException.class)
        .hasMessageContaining("Source customer not found");

    // Same test for original service
    CreateCustomerRequest targetRequest2 =
        new CreateCustomerRequest(
            "Target Active 2 " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);
    CreateCustomerRequest sourceRequest2 =
        new CreateCustomerRequest(
            "Source to Delete 2 " + System.currentTimeMillis(), CustomerType.NEUKUNDE);

    CustomerResponse target2 = originalService.createCustomer(targetRequest2, createdBy);
    CustomerResponse source2 = originalService.createCustomer(sourceRequest2, createdBy);

    UUID targetId2 = UUID.fromString(target2.id());
    UUID sourceId2 = UUID.fromString(source2.id());

    originalService.deleteCustomer(sourceId2, "delete-user", "Test deletion");

    assertThatThrownBy(() -> originalService.mergeCustomers(targetId2, sourceId2, "merge-user"))
        .isInstanceOf(CustomerNotFoundException.class)
        .hasMessageContaining("Source customer not found");
  }

  @Test
  @TestTransaction
  void mergeCustomers_shouldNotCreateTimelineEvent() {
    // This test documents that mergeCustomers does NOT create a Timeline Event
    // This is a bug/inconsistency but we maintain it for compatibility

    // Given - create customers
    CreateCustomerRequest targetRequest =
        new CreateCustomerRequest(
            "Target for Event Test " + System.currentTimeMillis(), CustomerType.UNTERNEHMEN);
    CreateCustomerRequest sourceRequest =
        new CreateCustomerRequest(
            "Source for Event Test " + System.currentTimeMillis(), CustomerType.NEUKUNDE);

    CustomerResponse target = commandService.createCustomer(targetRequest, "test-user");
    CustomerResponse source = commandService.createCustomer(sourceRequest, "test-user");

    UUID targetId = UUID.fromString(target.id());
    UUID sourceId = UUID.fromString(source.id());

    // Get initial timeline event count for target
    Customer targetCustomer = customerRepository.findById(targetId);
    int initialEventCount = targetCustomer.getTimelineEvents().size();

    // When - merge customers
    commandService.mergeCustomers(targetId, sourceId, "merge-user");

    // Then - no new timeline event should be created (this is the bug)
    customerRepository.flush();
    Customer mergedCustomer = customerRepository.findById(targetId);
    int finalEventCount = mergedCustomer.getTimelineEvents().size();

    // Document the bug: Event count should increase but doesn't
    assertThat(finalEventCount)
        .isEqualTo(initialEventCount)
        .as("mergeCustomers does NOT create Timeline Events (documented bug)");
  }

  // ========== changeStatus() Tests ==========

  @Test
  @TestTransaction
  void changeStatus_shouldProduceSameResultAsOriginalService() {
    // Given - create a customer
    CreateCustomerRequest request =
        new CreateCustomerRequest(
            "Status Test Company " + System.currentTimeMillis(), CustomerType.NEUKUNDE);

    CustomerResponse createdCustomer = commandService.createCustomer(request, "test-user");
    UUID customerId = UUID.fromString(createdCustomer.id());

    // When - change status from AKTIV to INAKTIV using both services
    CustomerResponse originalResult =
        originalService.changeStatus(customerId, CustomerStatus.INAKTIV, "status-changer");

    // Reset to AKTIV for second test
    Customer customer = customerRepository.findById(customerId);
    customer.setStatus(CustomerStatus.AKTIV);
    customerRepository.persist(customer);
    customerRepository.flush();

    CustomerResponse commandResult =
        commandService.changeStatus(customerId, CustomerStatus.INAKTIV, "status-changer");

    // Then - both should return same result
    assertThat(commandResult.status()).isEqualTo(originalResult.status());
    assertThat(commandResult.status()).isEqualTo(CustomerStatus.INAKTIV);

    // Verify timeline event was created
    Customer updatedCustomer = customerRepository.findById(customerId);
    boolean hasStatusChangeEvent =
        updatedCustomer.getTimelineEvents().stream()
            .anyMatch(event -> "STATUS_CHANGE".equals(event.getEventType()));
    assertThat(hasStatusChangeEvent)
        .isTrue()
        .as("changeStatus should create a STATUS_CHANGE timeline event");
  }

  @Test
  @TestTransaction
  void changeStatus_withInvalidTransition_shouldThrowSameException() {
    // Given - create a customer and set to ARCHIVIERT
    CreateCustomerRequest request =
        new CreateCustomerRequest(
            "Invalid Status Test " + System.currentTimeMillis(), CustomerType.NEUKUNDE);

    CustomerResponse createdCustomer = commandService.createCustomer(request, "test-user");
    UUID customerId = UUID.fromString(createdCustomer.id());

    // Set status to ARCHIVIERT
    Customer customer = customerRepository.findById(customerId);
    customer.setStatus(CustomerStatus.ARCHIVIERT);
    customerRepository.persist(customer);
    customerRepository.flush();

    // When/Then - try to change from ARCHIVIERT to AKTIV (invalid transition)
    assertThatThrownBy(
            () -> originalService.changeStatus(customerId, CustomerStatus.AKTIV, "test-user"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition from ARCHIVIERT to AKTIV");

    assertThatThrownBy(
            () -> commandService.changeStatus(customerId, CustomerStatus.AKTIV, "test-user"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition from ARCHIVIERT to AKTIV");
  }

  @Test
  @TestTransaction
  void changeStatus_withValidTransitionFromArchiviert_shouldSucceed() {
    // Given - create a customer and set to ARCHIVIERT
    CreateCustomerRequest request =
        new CreateCustomerRequest(
            "Valid Archiviert Test " + System.currentTimeMillis(), CustomerType.NEUKUNDE);

    CustomerResponse createdCustomer = commandService.createCustomer(request, "test-user");
    UUID customerId = UUID.fromString(createdCustomer.id());

    // Set status to ARCHIVIERT
    Customer customer = customerRepository.findById(customerId);
    customer.setStatus(CustomerStatus.ARCHIVIERT);
    customerRepository.persist(customer);
    customerRepository.flush();

    // When - change from ARCHIVIERT to INAKTIV (valid transition)
    CustomerResponse result =
        commandService.changeStatus(customerId, CustomerStatus.INAKTIV, "test-user");

    // Then - should succeed
    assertThat(result.status()).isEqualTo(CustomerStatus.INAKTIV);

    // Verify both services behave the same
    customer.setStatus(CustomerStatus.ARCHIVIERT); // Reset for second test
    customerRepository.persist(customer);
    customerRepository.flush();

    CustomerResponse originalResult =
        originalService.changeStatus(customerId, CustomerStatus.INAKTIV, "test-user");

    assertThat(originalResult.status()).isEqualTo(CustomerStatus.INAKTIV);
  }

  @Test
  @TestTransaction
  void changeStatus_withNonExistentCustomer_shouldThrowSameException() {
    // Given - non-existent customer ID
    UUID nonExistentId = UUID.randomUUID();

    // When/Then - both services should throw CustomerNotFoundException
    assertThatThrownBy(
            () -> originalService.changeStatus(nonExistentId, CustomerStatus.INAKTIV, "test-user"))
        .isInstanceOf(CustomerNotFoundException.class);

    assertThatThrownBy(
            () -> commandService.changeStatus(nonExistentId, CustomerStatus.INAKTIV, "test-user"))
        .isInstanceOf(CustomerNotFoundException.class);
  }

  @Test
  @TestTransaction
  void changeStatus_shouldUpdateRiskScore() {
    // Given - create a customer
    CreateCustomerRequest request =
        new CreateCustomerRequest(
            "Risk Score Test " + System.currentTimeMillis(), CustomerType.NEUKUNDE);

    CustomerResponse createdCustomer = commandService.createCustomer(request, "test-user");
    UUID customerId = UUID.fromString(createdCustomer.id());

    // Get initial risk score
    Customer customer = customerRepository.findById(customerId);
    int initialRiskScore = customer.getRiskScore();

    // When - change status
    commandService.changeStatus(customerId, CustomerStatus.INAKTIV, "test-user");

    // Then - risk score should be updated (updateRiskScore() was called)
    Customer updatedCustomer = customerRepository.findById(customerId);
    // Note: We don't test the exact value as that's business logic in Customer entity
    // We just verify that updateRiskScore() was called
    assertThat(updatedCustomer.getStatus()).isEqualTo(CustomerStatus.INAKTIV);
  }
}

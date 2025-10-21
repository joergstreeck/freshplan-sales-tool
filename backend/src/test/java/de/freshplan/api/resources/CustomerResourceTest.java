package de.freshplan.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.ActivateCustomerRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests für CustomerResource.activateCustomer() - Manual Activation Button.
 *
 * <p>Sprint 2.1.7.4 - Customer Status Architecture
 */
@QuarkusTest
class CustomerResourceTest {

  @Inject CustomerRepository customerRepository;

  private static final String ACTIVATION_ENDPOINT = "/api/customers/{id}/activate";
  private static final String TEST_USER = "admin-test";

  @BeforeEach
  @Transactional
  void cleanup() {
    // Clean test customers (keep seed data)
    customerRepository
        .find("companyName LIKE ?1", "[TEST-ACTIVATION]%")
        .list()
        .forEach(c -> customerRepository.delete(c));
  }

  // Helper: Create customer in separate transaction (committed before REST call)
  @Transactional
  UUID createCustomerInDb(String companyName, CustomerStatus status) {
    Customer customer = createTestCustomer(companyName, status);
    customerRepository.persist(customer);
    customerRepository.flush();
    return customer.getId();
  }

  // ========== SPRINT 2.1.7.4: MANUAL ACTIVATION TESTS ==========

  @Test
  @TestSecurity(user = "admin-test", roles = {"admin"})
  void shouldActivateProspectCustomerSuccessfully() {
    // Given: PROSPECT Customer (created in committed transaction)
    UUID customerId = createCustomerInDb("Test Corp Activation", CustomerStatus.PROSPECT);

    ActivateCustomerRequest request =
        ActivateCustomerRequest.of("XENTRAL-ORD-001"); // with order number

    // When/Then: PUT /api/customers/{id}/activate returns 200 with AKTIV status
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put(ACTIVATION_ENDPOINT, customerId)
        .then()
        .statusCode(200)
        .body("id", equalTo(customerId.toString()))
        .body("status", equalTo("AKTIV"))
        .body("companyName", equalTo("[TEST-ACTIVATION] Test Corp Activation"));
  }

  @Test
  @TestSecurity(user = "admin-test", roles = {"admin"})
  void shouldActivateWithoutOrderNumber() {
    // Given: PROSPECT Customer
    UUID customerId = createCustomerInDb("Test Corp No Order", CustomerStatus.PROSPECT);

    ActivateCustomerRequest request =
        ActivateCustomerRequest.withoutOrderNumber(); // NO order number

    // When/Then: PUT /api/customers/{id}/activate returns 200 with AKTIV status
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put(ACTIVATION_ENDPOINT, customerId)
        .then()
        .statusCode(200)
        .body("status", equalTo("AKTIV"));
  }

  @Test
  @TestSecurity(user = "admin-test", roles = {"admin"})
  void shouldReturn404WhenCustomerNotFound() {
    // Given: Invalid customer ID
    UUID invalidId = UUID.randomUUID();
    ActivateCustomerRequest request = ActivateCustomerRequest.withoutOrderNumber();

    // When/Then: PUT /api/customers/{id}/activate returns 404
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put(ACTIVATION_ENDPOINT, invalidId)
        .then()
        .statusCode(404);
  }

  @Test
  @TestSecurity(user = "admin-test", roles = {"admin"})
  void shouldReturn400WhenCustomerIsNotProspect() {
    // Given: AKTIV Customer (already activated)
    UUID customerId = createCustomerInDb("Test Corp Already Active", CustomerStatus.AKTIV);

    ActivateCustomerRequest request = ActivateCustomerRequest.withoutOrderNumber();

    // When/Then: PUT /api/customers/{id}/activate returns 400
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put(ACTIVATION_ENDPOINT, customerId)
        .then()
        .statusCode(400)
        .body("message", containsString("Current status: AKTIV"))
        .body("errorCode", equalTo("INVALID_STATUS"));
  }

  @Test
  @TestSecurity(user = "admin-test", roles = {"admin"})
  void shouldReturn400WhenCustomerIsRisiko() {
    // Given: RISIKO Customer
    UUID customerId = createCustomerInDb("Test Corp At Risk", CustomerStatus.RISIKO);

    ActivateCustomerRequest request = ActivateCustomerRequest.withoutOrderNumber();

    // When/Then: PUT /api/customers/{id}/activate returns 400
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put(ACTIVATION_ENDPOINT, customerId)
        .then()
        .statusCode(400)
        .body("message", containsString("Current status: RISIKO"))
        .body("errorCode", equalTo("INVALID_STATUS"));
  }

  @Test
  @TestSecurity(user = "admin-test", roles = {"admin"})
  void shouldReturn400WhenCustomerIsInaktiv() {
    // Given: INAKTIV Customer
    UUID customerId = createCustomerInDb("Test Corp Inactive", CustomerStatus.INAKTIV);

    ActivateCustomerRequest request = ActivateCustomerRequest.withoutOrderNumber();

    // When/Then: PUT /api/customers/{id}/activate returns 400
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put(ACTIVATION_ENDPOINT, customerId)
        .then()
        .statusCode(400)
        .body("message", containsString("Current status: INAKTIV"))
        .body("errorCode", equalTo("INVALID_STATUS"));
  }

  // ========== AUTHORIZATION TESTS ==========

  @Test
  @TestSecurity(user = "manager-test", roles = {"manager"})
  void shouldAllowManagerToActivateCustomer() {
    // Given: PROSPECT Customer
    UUID customerId = createCustomerInDb("Test Corp Manager", CustomerStatus.PROSPECT);

    ActivateCustomerRequest request = ActivateCustomerRequest.withoutOrderNumber();

    // When/Then: Manager can activate
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put(ACTIVATION_ENDPOINT, customerId)
        .then()
        .statusCode(200)
        .body("status", equalTo("AKTIV"));
  }

  @Test
  @TestSecurity(user = "sales-test", roles = {"sales"})
  void shouldAllowSalesToActivateCustomer() {
    // Given: PROSPECT Customer
    UUID customerId = createCustomerInDb("Test Corp Sales", CustomerStatus.PROSPECT);

    ActivateCustomerRequest request = ActivateCustomerRequest.withoutOrderNumber();

    // When/Then: Sales can activate
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put(ACTIVATION_ENDPOINT, customerId)
        .then()
        .statusCode(200)
        .body("status", equalTo("AKTIV"));
  }

  @Test
  @TestSecurity(user = "guest-test", roles = {"guest"})
  void shouldDenyGuestToActivateCustomer() {
    // Given: PROSPECT Customer
    UUID customerId = createCustomerInDb("Test Corp Guest", CustomerStatus.PROSPECT);

    ActivateCustomerRequest request = ActivateCustomerRequest.withoutOrderNumber();

    // When/Then: Guest role denied (403)
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put(ACTIVATION_ENDPOINT, customerId)
        .then()
        .statusCode(403); // Forbidden
  }

  @Test
  void shouldDenyUnauthenticatedRequests() {
    // Given: No authentication
    UUID customerId = UUID.randomUUID();
    ActivateCustomerRequest request = ActivateCustomerRequest.withoutOrderNumber();

    // When/Then: Unauthenticated request denied (401)
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put(ACTIVATION_ENDPOINT, customerId)
        .then()
        .statusCode(401); // Unauthorized
  }

  // ========== EDGE CASES ==========

  @Test
  @TestSecurity(user = "admin-test", roles = {"admin"})
  void shouldValidateOrderNumberLength() {
    // Given: PROSPECT Customer + order number too long
    UUID customerId = createCustomerInDb("Test Corp Long Order", CustomerStatus.PROSPECT);

    // Order number exceeds 50 chars (max length in ActivateCustomerRequest)
    String tooLongOrderNumber = "X".repeat(51);
    ActivateCustomerRequest request = ActivateCustomerRequest.of(tooLongOrderNumber);

    // When/Then: Validation error (400)
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put(ACTIVATION_ENDPOINT, customerId)
        .then()
        .statusCode(400); // Bean Validation should fail
  }

  @Test
  @TestSecurity(user = "admin-test", roles = {"admin"})
  void shouldAcceptMaxLengthOrderNumber() {
    // Given: PROSPECT Customer + order number at max length (50 chars)
    UUID customerId = createCustomerInDb("Test Corp Max Order", CustomerStatus.PROSPECT);

    String maxLengthOrderNumber = "X".repeat(50); // exactly 50 chars
    ActivateCustomerRequest request = ActivateCustomerRequest.of(maxLengthOrderNumber);

    // When/Then: Should succeed
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put(ACTIVATION_ENDPOINT, customerId)
        .then()
        .statusCode(200)
        .body("status", equalTo("AKTIV"));
  }

  @Test
  @TestSecurity(user = "admin-test", roles = {"admin"})
  void shouldHandleEmptyOrderNumber() {
    // Given: PROSPECT Customer + empty order number
    UUID customerId = createCustomerInDb("Test Corp Empty Order", CustomerStatus.PROSPECT);

    ActivateCustomerRequest request = ActivateCustomerRequest.of(""); // empty string

    // When/Then: Should succeed (empty string is valid)
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put(ACTIVATION_ENDPOINT, customerId)
        .then()
        .statusCode(200)
        .body("status", equalTo("AKTIV"));
  }

  // ========== HELPER METHODS ==========

  private Customer createTestCustomer(String companyName, CustomerStatus status) {
    Customer customer = new Customer();
    customer.setCustomerNumber(
        "TEST-ACT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    customer.setCompanyName("[TEST-ACTIVATION] " + companyName);
    customer.setStatus(status);
    customer.setCreatedBy(TEST_USER);
    customer.setCreatedAt(LocalDateTime.now());
    customer.setIsTestData(true);
    return customer;
  }
}

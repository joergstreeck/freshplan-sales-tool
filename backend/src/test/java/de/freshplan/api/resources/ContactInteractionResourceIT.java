package de.freshplan.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.customer.entity.ContactInteraction.InteractionType;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.FinancingType;
import de.freshplan.domain.customer.entity.PartnerStatus;
import de.freshplan.domain.customer.entity.PaymentTerms;
import de.freshplan.domain.customer.repository.ContactInteractionRepository;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.repository.CustomerTimelineEventRepository;
import de.freshplan.domain.customer.service.dto.ContactInteractionDTO;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.test.builders.CustomerTestDataFactory;
import de.freshplan.test.builders.ContactTestDataFactory;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("migrate")
@DisplayName("Contact Interaction Resource Integration Tests")
class ContactInteractionResourceIT {

  @Inject CustomerRepository customerRepository;

  @Inject ContactRepository contactRepository;

  @Inject ContactInteractionRepository interactionRepository;

  @Inject EntityManager entityManager;

  @Inject OpportunityRepository opportunityRepository;

  @Inject CustomerTimelineEventRepository timelineEventRepository;

  private UUID testCustomerId;
  private UUID testContactId;
  private String testMarker;

  private void createTestInteraction(InteractionType type, String summary) {
    ContactInteractionDTO dto =
        ContactInteractionDTO.builder()
            .contactId(testContactId)
            .type(type)
            .summary(summary)
            .sentimentScore(0.7)
            .engagementScore(75)
            .timestamp(LocalDateTime.now())
            .build();

    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .post("/api/contact-interactions")
        .then()
        .statusCode(201);
  }

  @BeforeEach
  @Transactional
  void setUp() {
    // Generate unique test marker for this test run (max 20 chars for customer_number)
    testMarker =
        "T"
            + System.currentTimeMillis() % 1000000
            + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

    // Create test customer using TestDataFactory pattern
    Customer testCustomer = CustomerTestDataFactory.builder()
        .withCompanyName("[" + testMarker + "] Test Company GmbH")
        .withCustomerNumber(testMarker)
        .withStatus(CustomerStatus.LEAD)
        .asTestData(true)
        .buildAndPersist(customerRepository);

    testCustomerId = testCustomer.getId();

    // Create test contact using TestDataFactory pattern
    CustomerContact testContact = ContactTestDataFactory.builder()
        .forCustomer(testCustomer)
        .withFirstName("Max")
        .withLastName("Mustermann")
        .withEmail("max@company.com")
        .withIsPrimary(false)
        // isDecisionMaker defaults to false, no need to set it
        .buildAndPersist(customerRepository, entityManager);

    testContactId = testContact.getId();
  }

  @AfterEach
  @Transactional
  void tearDown() {
    // Clean up test data using unique marker
    if (testMarker != null) {
      contactRepository
          .getEntityManager()
          .createQuery(
              "DELETE FROM ContactInteraction ci WHERE ci.contact.customer.customerNumber = :marker")
          .setParameter("marker", testMarker)
          .executeUpdate();
      contactRepository
          .getEntityManager()
          .createQuery("DELETE FROM CustomerContact cc WHERE cc.customer.customerNumber = :marker")
          .setParameter("marker", testMarker)
          .executeUpdate();
      customerRepository
          .getEntityManager()
          .createQuery("DELETE FROM Customer c WHERE c.customerNumber = :marker")
          .setParameter("marker", testMarker)
          .executeUpdate();
    }
  }

  @Test
  @DisplayName("Should get data quality metrics for empty system")
  void shouldGetDataQualityMetricsEmpty() {
    given()
        .when()
        .get("/api/contact-interactions/metrics/data-quality")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("totalContacts", greaterThanOrEqualTo(1))
        .body("contactsWithInteractions", equalTo(0))
        .body("averageInteractionsPerContact", equalTo(0.0f))
        .body("dataCompletenessScore", greaterThanOrEqualTo(0.0f))
        .body("contactsWithWarmthScore", greaterThanOrEqualTo(0))
        .body("freshContacts", greaterThanOrEqualTo(0))
        .body("agingContacts", greaterThanOrEqualTo(0))
        .body("staleContacts", greaterThanOrEqualTo(0))
        .body("criticalContacts", greaterThanOrEqualTo(0))
        .body("showDataCollectionHints", equalTo(true))
        .body("criticalDataGaps", not(empty()))
        .body("improvementSuggestions", not(empty()))
        .body(
            "overallDataQuality",
            oneOf("CRITICAL", "POOR", "FAIR", "GOOD", "EXCELLENT", "UNKNOWN"));
  }

  @Test
  @DisplayName("Should create contact interaction successfully")
  void shouldCreateContactInteraction() {
    ContactInteractionDTO dto =
        ContactInteractionDTO.builder()
            .contactId(testContactId)
            .type(InteractionType.NOTE)
            .summary("Test note for integration")
            .sentimentScore(0.5)
            .engagementScore(75)
            .timestamp(LocalDateTime.now())
            .build();

    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .post("/api/contact-interactions")
        .then()
        .statusCode(201)
        .contentType(ContentType.JSON)
        .body("id", notNullValue())
        .body("contactId", equalTo(testContactId.toString()))
        .body("type", equalTo("NOTE"))
        .body("summary", equalTo("Test note for integration"))
        .body("sentimentScore", equalTo(0.5f))
        .body("engagementScore", equalTo(75));
  }

  @Test
  @DisplayName("Should get interactions for contact")
  void shouldGetContactInteractions() {
    // First create an interaction
    createTestInteraction(InteractionType.EMAIL, "Test email subject");

    given()
        .when()
        .get("/api/contact-interactions/contact/{contactId}", testContactId)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("size()", equalTo(1))
        .body("[0].contactId", equalTo(testContactId.toString()))
        .body("[0].type", equalTo("EMAIL"))
        .body("[0].summary", equalTo("Test email subject"));
  }

  @Test
  @DisplayName("Should calculate warmth score for contact")
  void shouldCalculateWarmthScore() {
    // Create multiple interactions to have a meaningful score
    createTestInteraction(InteractionType.EMAIL, "First email");
    createTestInteraction(InteractionType.EMAIL, "Response email");
    createTestInteraction(InteractionType.CALL, "Follow-up call");

    given()
        .when()
        .get("/api/contact-interactions/contact/{contactId}/warmth-score", testContactId)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("contactId", equalTo(testContactId.toString()))
        .body("warmthScore", greaterThanOrEqualTo(0))
        .body("confidence", greaterThanOrEqualTo(0));
  }

  @Test
  @DisplayName("Should get warmth score for contact")
  void shouldGetWarmthScore() {
    // First create an interaction
    createTestInteraction(InteractionType.NOTE, "Initial note");

    // Get warmth score
    given()
        .when()
        .get("/api/contact-interactions/contact/{contactId}/warmth-score", testContactId)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("contactId", equalTo(testContactId.toString()))
        .body("warmthScore", greaterThanOrEqualTo(0));
  }

  @Test
  @DisplayName("Should record note as interaction")
  void shouldRecordNote() {
    String noteContent = "This is a test note for integration testing";

    ContactInteractionDTO noteDto =
        ContactInteractionDTO.builder()
            .contactId(testContactId)
            .type(InteractionType.NOTE)
            .summary(noteContent)
            .timestamp(LocalDateTime.now())
            .build();

    given()
        .contentType(ContentType.JSON)
        .body(noteDto)
        .when()
        .post("/api/contact-interactions")
        .then()
        .statusCode(201)
        .contentType(ContentType.JSON)
        .body("contactId", equalTo(testContactId.toString()))
        .body("type", equalTo("NOTE"))
        .body("summary", equalTo(noteContent))
        .body("id", notNullValue())
        .body("timestamp", notNullValue());
  }

  @Test
  @DisplayName("Should record email interaction")
  void shouldRecordEmail() {
    ContactInteractionDTO dto =
        ContactInteractionDTO.builder()
            .contactId(testContactId)
            .type(InteractionType.EMAIL)
            .summary("Test Email Subject")
            .sentimentScore(0.8)
            .timestamp(LocalDateTime.now())
            .build();

    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .post("/api/contact-interactions")
        .then()
        .statusCode(201)
        .contentType(ContentType.JSON)
        .body("contactId", equalTo(testContactId.toString()))
        .body("type", equalTo("EMAIL"))
        .body("summary", equalTo("Test Email Subject"))
        .body("sentimentScore", equalTo(0.8f));
  }

  @Test
  @DisplayName("Should record call interaction")
  void shouldRecordCall() {
    ContactInteractionDTO dto =
        ContactInteractionDTO.builder()
            .contactId(testContactId)
            .type(InteractionType.CALL)
            .summary("Successful discussion")
            .engagementScore(80)
            .timestamp(LocalDateTime.now())
            .build();

    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .post("/api/contact-interactions")
        .then()
        .statusCode(201)
        .contentType(ContentType.JSON)
        .body("contactId", equalTo(testContactId.toString()))
        .body("type", equalTo("CALL"))
        .body("engagementScore", greaterThan(0))
        .body("summary", equalTo("Successful discussion"));
  }

  @Test
  @DisplayName("Should record meeting interaction")
  void shouldRecordMeeting() {
    ContactInteractionDTO dto =
        ContactInteractionDTO.builder()
            .contactId(testContactId)
            .type(InteractionType.MEETING)
            .summary("Productive meeting")
            .engagementScore(90)
            .timestamp(LocalDateTime.now())
            .build();

    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .post("/api/contact-interactions")
        .then()
        .statusCode(201)
        .contentType(ContentType.JSON)
        .body("contactId", equalTo(testContactId.toString()))
        .body("type", equalTo("MEETING"))
        .body("engagementScore", greaterThan(0))
        .body("summary", equalTo("Productive meeting"));
  }

  @Test
  @DisplayName("Should improve data quality metrics after adding interactions")
  void shouldImproveDataQualityAfterInteractions() {
    // Get initial metrics
    Float initialScore =
        given()
            .when()
            .get("/api/contact-interactions/metrics/data-quality")
            .then()
            .statusCode(200)
            .extract()
            .path("dataCompletenessScore");

    // Add several interactions
    createTestInteraction(InteractionType.EMAIL, "First interaction");
    createTestInteraction(InteractionType.EMAIL, "Response");
    createTestInteraction(InteractionType.CALL, "Follow-up");
    createTestInteraction(InteractionType.NOTE, "Meeting notes");

    // Get updated metrics
    given()
        .when()
        .get("/api/contact-interactions/metrics/data-quality")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("contactsWithInteractions", equalTo(1))
        .body("averageInteractionsPerContact", equalTo(4.0f))
        .body("contactsWithInteractions", greaterThan(0))
        .body("dataCompletenessScore", greaterThanOrEqualTo(initialScore));
  }

  @Test
  @DisplayName("Should handle invalid contact ID gracefully")
  void shouldHandleInvalidContactId() {
    UUID invalidContactId = UUID.randomUUID();

    given()
        .contentType(ContentType.JSON)
        .body("{\"note\": \"Test note\"}")
        .when()
        .post("/api/contact-interactions/note/{contactId}", invalidContactId)
        .then()
        .statusCode(404);
  }

  @Test
  @DisplayName("Should validate required fields")
  void shouldValidateRequiredFields() {
    // Test with missing contact ID
    ContactInteractionDTO dto =
        ContactInteractionDTO.builder().type(InteractionType.NOTE).summary("Test note").build();

    given()
        .contentType(ContentType.JSON)
        .body(dto)
        .when()
        .post("/api/contact-interactions")
        .then()
        .statusCode(400);
  }
}

package de.freshplan.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.customer.entity.Contact;
import de.freshplan.domain.customer.entity.ContactInteraction;
import de.freshplan.domain.customer.entity.ContactInteraction.InteractionType;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.ContactInteractionRepository;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.ContactInteractionDTO;
import de.freshplan.domain.customer.service.dto.DataQualityMetricsDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@DisplayName("Contact Interaction Resource Integration Tests")
class ContactInteractionResourceIT {

  @Inject CustomerRepository customerRepository;

  @Inject ContactRepository contactRepository;

  @Inject ContactInteractionRepository interactionRepository;

  private UUID testCustomerId;
  private UUID testContactId;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean slate for each test
    interactionRepository.deleteAll();
    contactRepository.deleteAll();
    customerRepository.deleteAll();

    // Create test customer with all required fields
    Customer testCustomer = new Customer();
    testCustomer.setCompanyName("Test Company GmbH");
    testCustomer.setCustomerNumber("CUST-" + UUID.randomUUID().toString().substring(0, 8));
    testCustomer.setCompanyType("UNTERNEHMEN");
    testCustomer.setCompanyStructure("STANDALONE");
    testCustomer.setLifecyclePhase("ACQUISITION");
    testCustomer.setPartnerStatus("KEIN_PARTNER");
    testCustomer.setPaymentTerms("NETTO_30");
    testCustomer.setStatus("LEAD");
    testCustomer.setDataQualityStatus("STANDARD");
    testCustomer.setPainPoints("[]"); // Empty JSON array
    testCustomer.setCreatedBy("system");
    // Sprint 2 fields
    testCustomer.setLocationsGermany(0);
    testCustomer.setLocationsAustria(0);
    testCustomer.setLocationsRestEu(0);
    testCustomer.setLocationsSwitzerland(0);
    testCustomer.setTotalLocationsEu(0);
    testCustomer.setPrimaryFinancing("EIGENKAPITAL");
    customerRepository.persist(testCustomer);
    testCustomerId = testCustomer.getId();

    // Create test contact
    Contact testContact = new Contact();
    testContact.setFirstName("Max");
    testContact.setLastName("Mustermann");
    testContact.setEmail("max@company.com");
    testContact.setCustomer(testCustomer);
    contactRepository.persist(testContact);
    testContactId = testContact.getId();
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
        .body("totalContacts", equalTo(1))
        .body("contactsWithInteractions", equalTo(0))
        .body("averageInteractionsPerContact", equalTo(0.0f))
        .body("dataCompletenessScore", greaterThanOrEqualTo(0.0f))
        .body("contactsWithWarmthScore", equalTo(0))
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
        .body("sentimentScore", closeTo(0.5f, 0.01f))
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
        .body("[0].subject", equalTo("Test email subject"));
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
        .post("/api/contact-interactions/warmth/{contactId}/calculate", testContactId)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("contactId", equalTo(testContactId.toString()))
        .body("score", allOf(greaterThanOrEqualTo(0), lessThanOrEqualTo(100)))
        .body("confidence", allOf(greaterThanOrEqualTo(0), lessThanOrEqualTo(100)))
        .body("factors", notNullValue())
        .body("factors.frequency", greaterThanOrEqualTo(0.0f))
        .body("factors.sentiment", greaterThanOrEqualTo(0.0f))
        .body("factors.engagement", greaterThanOrEqualTo(0.0f))
        .body("factors.response", greaterThanOrEqualTo(0.0f))
        .body("trend", oneOf("INCREASING", "STABLE", "DECREASING"))
        .body("recommendations", notNullValue());
  }

  @Test
  @DisplayName("Should get warmth score for contact")
  void shouldGetWarmthScore() {
    // First calculate the score
    createTestInteraction(InteractionType.NOTE, "Initial note");
    given()
        .when()
        .post("/api/contact-interactions/warmth/{contactId}/calculate", testContactId)
        .then()
        .statusCode(200);

    // Then retrieve it
    given()
        .when()
        .get("/api/contact-interactions/warmth/{contactId}", testContactId)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("contactId", equalTo(testContactId.toString()))
        .body("score", allOf(greaterThanOrEqualTo(0), lessThanOrEqualTo(100)));
  }

  @Test
  @DisplayName("Should record note as interaction")
  void shouldRecordNote() {
    String noteContent = "This is a test note for integration testing";

    given()
        .contentType(ContentType.JSON)
        .body("{\"note\": \"" + noteContent + "\"}")
        .when()
        .post("/api/contact-interactions/note/{contactId}", testContactId)
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
    given()
        .contentType(ContentType.JSON)
        .body("{\"type\": \"SENT\", \"subject\": \"Test Email Subject\", \"sentiment\": 0.8}")
        .when()
        .post("/api/contact-interactions/email/{contactId}", testContactId)
        .then()
        .statusCode(201)
        .contentType(ContentType.JSON)
        .body("contactId", equalTo(testContactId.toString()))
        .body("type", equalTo("EMAIL"))
        .body("subject", equalTo("Test Email Subject"))
        .body("sentimentScore", closeTo(0.8f, 0.01f));
  }

  @Test
  @DisplayName("Should record call interaction")
  void shouldRecordCall() {
    given()
        .contentType(ContentType.JSON)
        .body(
            "{\"type\": \"OUTBOUND\", \"duration\": 1800, \"outcome\": \"Successful discussion\"}")
        .when()
        .post("/api/contact-interactions/call/{contactId}", testContactId)
        .then()
        .statusCode(201)
        .contentType(ContentType.JSON)
        .body("contactId", equalTo(testContactId.toString()))
        .body("type", equalTo("CALL"))
        .body("duration", equalTo(1800))
        .body("outcome", equalTo("Successful discussion"));
  }

  @Test
  @DisplayName("Should record meeting interaction")
  void shouldRecordMeeting() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"type\": \"COMPLETED\", \"duration\": 3600, \"notes\": \"Productive meeting\"}")
        .when()
        .post("/api/contact-interactions/meeting/{contactId}", testContactId)
        .then()
        .statusCode(201)
        .contentType(ContentType.JSON)
        .body("contactId", equalTo(testContactId.toString()))
        .body("type", equalTo("MEETING_COMPLETED"))
        .body("duration", equalTo(3600))
        .body("summary", equalTo("Productive meeting"));
  }

  @Test
  @DisplayName("Should improve data quality metrics after adding interactions")
  void shouldImproveDataQualityAfterInteractions() {
    // Get initial metrics
    DataQualityMetricsDTO initialMetrics =
        given()
            .when()
            .get("/api/contact-interactions/metrics/data-quality")
            .then()
            .statusCode(200)
            .extract()
            .as(DataQualityMetricsDTO.class);

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
        .body("interactionCoverage", equalTo(100.0f))
        .body("dataCompletenessScore", greaterThan(initialMetrics.getDataCompletenessScore()));
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

  @Transactional
  protected void createTestInteraction(InteractionType type, String content) {
    ContactInteraction interaction =
        ContactInteraction.builder()
            .contact(contactRepository.findById(testContactId))
            .type(type)
            .timestamp(LocalDateTime.now())
            .subject(content)
            .summary(content)
            .sentimentScore(0.7)
            .engagementScore(80)
            .build();

    interactionRepository.persist(interaction);
  }
}

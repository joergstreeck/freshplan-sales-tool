package de.freshplan.api.resources;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.repository.CustomerTimelineRepository;
import de.freshplan.domain.customer.service.dto.timeline.*;
import de.freshplan.test.builders.CustomerBuilder;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
/**
 * Integration tests for CustomerTimelineResource.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@Tag("migrate")@TestHTTPEndpoint(CustomerTimelineResource.class)
class CustomerTimelineResourceIT {

  @Inject CustomerRepository customerRepository;

  @Inject CustomerTimelineRepository timelineRepository;

  @Inject CustomerBuilder customerBuilder;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean only test-specific data to preserve CustomerDataInitializer test customers
    timelineRepository.deleteAll();
    customerRepository.delete("customerNumber LIKE ?1", "IT-TEST-%");
  }

  @Transactional
  UUID createTestCustomerInTransaction() {
    Customer customer =
        customerBuilder
            .withCompanyName("[TEST] Integration Test Company")
            .withStatus(CustomerStatus.AKTIV)
            .withType(CustomerType.UNTERNEHMEN)
            .withIndustry(Industry.SONSTIGE)
            .build();
    customer.setCustomerNumber("IT-TEST-001");
    customer.setCompanyName("[TEST] Integration Test Company"); // Keep [TEST] prefix
    customer.setIsTestData(true); // Mark as test data
    customerRepository.persist(customer);
    return customer.getId();
  }

  @Transactional
  UUID createTestEventInTransaction(UUID customerId) {
    Customer customer = customerRepository.findById(customerId);
    CustomerTimelineEvent event = new CustomerTimelineEvent();
    event.setCustomer(customer);
    event.setEventType("NOTE");
    event.setTitle("Test Note");
    event.setDescription("Test Description");
    event.setCategory(EventCategory.NOTE);
    event.setImportance(ImportanceLevel.MEDIUM);
    event.setPerformedBy("testuser");
    event.setEventDate(LocalDateTime.now());
    timelineRepository.persist(event);
    return event.getId();
  }

  @Transactional
  UUID createCommunicationEventInTransaction(UUID customerId) {
    Customer customer = customerRepository.findById(customerId);
    CustomerTimelineEvent event = new CustomerTimelineEvent();
    event.setCustomer(customer);
    event.setEventType("CALL");
    event.setTitle("Phone Call");
    event.setCategory(EventCategory.PHONE_CALL);
    event.setImportance(ImportanceLevel.HIGH);
    event.setPerformedBy("salesrep");
    event.setEventDate(LocalDateTime.now());
    timelineRepository.persist(event);
    return event.getId();
  }

  @Transactional
  UUID createFollowUpEventInTransaction(UUID customerId) {
    Customer customer = customerRepository.findById(customerId);
    CustomerTimelineEvent followUpEvent = new CustomerTimelineEvent();
    followUpEvent.setCustomer(customer);
    followUpEvent.setEventType("CALL");
    followUpEvent.setTitle("Sales Call");
    followUpEvent.setCategory(EventCategory.PHONE_CALL);
    followUpEvent.setImportance(ImportanceLevel.HIGH);
    followUpEvent.setPerformedBy("salesrep");
    followUpEvent.setRequiresFollowUp(true);
    followUpEvent.setFollowUpDate(LocalDateTime.now().plusDays(2));
    followUpEvent.setFollowUpNotes("Send proposal");
    followUpEvent.setEventDate(LocalDateTime.now());
    timelineRepository.persist(followUpEvent);
    return followUpEvent.getId();
  }

  @Transactional
  UUID createOverdueFollowUpEventInTransaction(UUID customerId) {
    Customer customer = customerRepository.findById(customerId);
    CustomerTimelineEvent overdueEvent = new CustomerTimelineEvent();
    overdueEvent.setCustomer(customer);
    overdueEvent.setEventType("EMAIL");
    overdueEvent.setTitle("Email Communication");
    overdueEvent.setCategory(EventCategory.EMAIL);
    overdueEvent.setImportance(ImportanceLevel.HIGH);
    overdueEvent.setPerformedBy("salesrep");
    overdueEvent.setRequiresFollowUp(true);
    overdueEvent.setFollowUpDate(LocalDateTime.now().minusDays(1)); // Overdue
    overdueEvent.setFollowUpCompleted(false);
    overdueEvent.setEventDate(LocalDateTime.now().minusDays(3));
    timelineRepository.persist(overdueEvent);
    return overdueEvent.getId();
  }

  @Transactional
  UUID createRecentCommunicationEventInTransaction(UUID customerId) {
    Customer customer = customerRepository.findById(customerId);
    CustomerTimelineEvent recentComm =
        CustomerTimelineEvent.createCommunicationEvent(
            customer, "teams", "inbound", "Customer inquiry about new products", "support");
    timelineRepository.persist(recentComm);
    return recentComm.getId();
  }

  @Transactional
  UUID createVariousCommunicationEventInTransaction(UUID customerId, int index) {
    Customer customer = customerRepository.findById(customerId);
    CustomerTimelineEvent event = new CustomerTimelineEvent();
    event.setCustomer(customer);
    event.setEventType("COMM_" + index);
    event.setTitle("Communication " + index);
    event.setCategory(EventCategory.COMMUNICATION);
    event.setImportance(ImportanceLevel.MEDIUM);
    event.setPerformedBy("user" + index);
    event.setEventDate(LocalDateTime.now());
    timelineRepository.persist(event);
    return event.getId();
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  void getTimeline_shouldReturnPaginatedEvents() {
    // Create test data in separate transactions
    UUID customerId = createTestCustomerInTransaction();
    UUID eventId = createTestEventInTransaction(customerId);

    Response response =
        given()
            .pathParam("customerId", customerId)
            .queryParam("page", 0)
            .queryParam("size", 10)
            .when()
            .get()
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .response();

    TimelineListResponse result = response.as(TimelineListResponse.class);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.isFirst()).isTrue();
    assertThat(result.isLast()).isTrue();

    TimelineEventResponse event = result.getContent().get(0);
    assertThat(event.getEventType()).isEqualTo("NOTE");
    assertThat(event.getTitle()).isEqualTo("Test Note");
    assertThat(event.getDescription()).isEqualTo("Test Description");
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  void getTimeline_withCategoryFilter_shouldReturnFilteredEvents() {
    // Create test data in separate transactions
    UUID customerId = createTestCustomerInTransaction();
    UUID eventId = createTestEventInTransaction(customerId);
    UUID communicationEventId = createCommunicationEventInTransaction(customerId);

    Response response =
        given()
            .pathParam("customerId", customerId)
            .queryParam("category", "PHONE_CALL")
            .when()
            .get()
            .then()
            .statusCode(200)
            .extract()
            .response();

    TimelineListResponse result = response.as(TimelineListResponse.class);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getCategory()).isEqualTo(EventCategory.PHONE_CALL);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  void getTimeline_withSearchText_shouldReturnMatchingEvents() {
    // Create test data in separate transactions
    UUID customerId = createTestCustomerInTransaction();
    UUID eventId = createTestEventInTransaction(customerId);

    Response response =
        given()
            .pathParam("customerId", customerId)
            .queryParam("search", "Test")
            .when()
            .get()
            .then()
            .statusCode(200)
            .extract()
            .response();

    TimelineListResponse result = response.as(TimelineListResponse.class);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getTitle()).contains("Test");
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  void getTimeline_withNonExistentCustomer_shouldReturn404() {
    given().pathParam("customerId", UUID.randomUUID()).when().get().then().statusCode(404);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  void createEvent_withValidData_shouldCreateTimelineEvent() {
    // Create test data in separate transaction
    UUID customerId = createTestCustomerInTransaction();

    CreateTimelineEventRequest request =
        CreateTimelineEventRequest.builder()
            .eventType("MEETING")
            .title("Customer Meeting")
            .description("Discussed Q1 plans")
            .category(EventCategory.MEETING)
            .importance(ImportanceLevel.HIGH)
            .performedBy("salesmanager")
            .performedByRole("Sales Manager")
            .build();

    Response response =
        given()
            .pathParam("customerId", customerId)
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post()
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .extract()
            .response();

    TimelineEventResponse result = response.as(TimelineEventResponse.class);

    assertThat(result.getId()).isNotNull();
    assertThat(result.getEventType()).isEqualTo("MEETING");
    assertThat(result.getTitle()).isEqualTo("Customer Meeting");
    assertThat(result.getDescription()).isEqualTo("Discussed Q1 plans");
    assertThat(result.getCategory()).isEqualTo(EventCategory.MEETING);
    assertThat(result.getImportance()).isEqualTo(ImportanceLevel.HIGH);

    // Verify in database
    CustomerTimelineEvent savedEvent = timelineRepository.findById(result.getId());
    assertThat(savedEvent).isNotNull();
    assertThat(savedEvent.getTitle()).isEqualTo("Customer Meeting");
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  void createNote_withValidData_shouldCreateNoteEvent() {
    // Create test data in separate transaction
    UUID customerId = createTestCustomerInTransaction();

    CreateNoteRequest request =
        new CreateNoteRequest("Customer prefers email communication", "salesrep");

    Response response =
        given()
            .pathParam("customerId", customerId)
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/notes")
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .extract()
            .response();

    TimelineEventResponse result = response.as(TimelineEventResponse.class);

    assertThat(result.getEventType()).isEqualTo("NOTE");
    assertThat(result.getTitle()).isEqualTo("Notiz");
    assertThat(result.getDescription()).isEqualTo("Customer prefers email communication");
    assertThat(result.getCategory()).isEqualTo(EventCategory.NOTE);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  void createCommunication_withValidData_shouldCreateCommunicationEvent() {
    // Create test data in separate transaction
    UUID customerId = createTestCustomerInTransaction();

    CreateCommunicationRequest request =
        CreateCommunicationRequest.builder()
            .channel("email")
            .direction("outbound")
            .description("Sent price list and catalog")
            .performedBy("salesrep")
            .duration(10)
            .requiresFollowUp(LocalDateTime.now().plusDays(3), "Follow up on pricing questions")
            .build();

    Response response =
        given()
            .pathParam("customerId", customerId)
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/communications")
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .extract()
            .response();

    TimelineEventResponse result = response.as(TimelineEventResponse.class);

    assertThat(result.getEventType()).isEqualTo("COMMUNICATION");
    assertThat(result.getTitle()).isEqualTo("Kommunikation via email");
    assertThat(result.getCommunicationChannel()).isEqualTo("email");
    assertThat(result.getCommunicationDirection()).isEqualTo("outbound");
    assertThat(result.getCommunicationDuration()).isEqualTo(10);
    assertThat(result.getRequiresFollowUp()).isTrue();
    assertThat(result.getFollowUpNotes()).isEqualTo("Follow up on pricing questions");
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  void createEvent_withInvalidData_shouldReturn400() {
    // Create test data in separate transaction
    UUID customerId = createTestCustomerInTransaction();

    CreateTimelineEventRequest request = new CreateTimelineEventRequest();
    // Missing required fields

    given()
        .pathParam("customerId", customerId)
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post()
        .then()
        .statusCode(400);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  void getFollowUps_shouldReturnEventsRequiringFollowUp() {
    // Create test data in separate transaction
    UUID customerId = createTestCustomerInTransaction();
    UUID followUpEventId = createFollowUpEventInTransaction(customerId);

    given()
        .pathParam("customerId", customerId)
        .when()
        .get("/follow-ups")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("size()", is(1))
        .body("[0].requiresFollowUp", is(true))
        .body("[0].followUpNotes", is("Send proposal"));
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  void getOverdueFollowUps_shouldReturnOverdueEvents() {
    // Create test data in separate transaction
    UUID customerId = createTestCustomerInTransaction();
    UUID overdueEventId = createOverdueFollowUpEventInTransaction(customerId);

    given()
        .pathParam("customerId", customerId)
        .when()
        .get("/follow-ups/overdue")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("size()", is(1))
        .body("[0].isFollowUpOverdue", is(true));
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  void getRecentCommunications_shouldReturnRecentCommunicationEvents() {
    // Create test data in separate transaction
    UUID customerId = createTestCustomerInTransaction();
    UUID recentCommId = createRecentCommunicationEventInTransaction(customerId);

    given()
        .pathParam("customerId", customerId)
        .queryParam("days", 7)
        .when()
        .get("/communications/recent")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("size()", is(1))
        .body("[0].category", is("COMMUNICATION"));
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  void getTimelineSummary_shouldReturnSummaryStatistics() {
    // Create test data in separate transaction
    UUID customerId = createTestCustomerInTransaction();
    UUID eventId = createTestEventInTransaction(customerId);
    // Create 3 communication events
    for (int i = 0; i < 3; i++) {
      createVariousCommunicationEventInTransaction(customerId, i);
    }

    given()
        .pathParam("customerId", customerId)
        .when()
        .get("/summary")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("totalEvents", is(4)) // 1 initial + 3 new
        .body("communicationEvents", is(3))
        .body("systemEvents", is(0));
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  void updateEvent_withValidData_shouldUpdateTimelineEvent() {
    // Create test data in separate transaction
    UUID customerId = createTestCustomerInTransaction();
    UUID eventId = createTestEventInTransaction(customerId);

    UpdateTimelineEventRequest request = new UpdateTimelineEventRequest();
    request.setTitle("Updated Title");
    request.setDescription("Updated Description");
    request.setImportance(ImportanceLevel.CRITICAL);
    request.setUpdatedBy("manager");

    given()
        .pathParam("customerId", customerId)
        .pathParam("eventId", eventId)
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .put("/events/{eventId}")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("title", is("Updated Title"))
        .body("description", is("Updated Description"))
        .body("importance", is("CRITICAL"));

    // Verify in database
    CustomerTimelineEvent updated = timelineRepository.findById(eventId);
    assertThat(updated.getTitle()).isEqualTo("Updated Title");
    assertThat(updated.getImportance()).isEqualTo(ImportanceLevel.CRITICAL);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  void completeFollowUp_shouldMarkAsCompleted() {
    // Create test data in separate transaction
    UUID customerId = createTestCustomerInTransaction();
    UUID eventId = createFollowUpEventInTransaction(customerId);

    given()
        .pathParam("customerId", customerId)
        .pathParam("eventId", eventId)
        .queryParam("completedBy", "manager")
        .contentType(ContentType.JSON)
        .when()
        .post("/events/{eventId}/complete-follow-up")
        .then()
        .statusCode(204);

    // Verify in database
    CustomerTimelineEvent updated = timelineRepository.findById(eventId);
    assertThat(updated.getFollowUpCompleted()).isTrue();
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin", "manager", "sales"})
  void deleteEvent_shouldSoftDeleteEvent() {
    // Create test data in separate transaction
    UUID customerId = createTestCustomerInTransaction();
    UUID eventId = createTestEventInTransaction(customerId);

    given()
        .pathParam("customerId", customerId)
        .pathParam("eventId", eventId)
        .queryParam("deletedBy", "admin")
        .when()
        .delete("/events/{eventId}")
        .then()
        .statusCode(204);

    // Verify soft delete in database
    CustomerTimelineEvent deleted = timelineRepository.findById(eventId);
    assertThat(deleted.getIsDeleted()).isTrue();
    assertThat(deleted.getDeletedBy()).isEqualTo("admin");
    assertThat(deleted.getDeletedAt()).isNotNull();
  }
}

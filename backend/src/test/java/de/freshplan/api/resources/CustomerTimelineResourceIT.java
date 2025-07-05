package de.freshplan.api.resources;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.repository.CustomerTimelineRepository;
import de.freshplan.domain.customer.service.dto.timeline.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for CustomerTimelineResource.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@TestHTTPEndpoint(CustomerTimelineResource.class)
class CustomerTimelineResourceIT {
    
    @Inject
    CustomerRepository customerRepository;
    
    @Inject
    CustomerTimelineRepository timelineRepository;
    
    private Customer testCustomer;
    private CustomerTimelineEvent testEvent;
    
    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up database
        timelineRepository.deleteAll();
        customerRepository.deleteAll();
    }
    
    private void createTestCustomer() {
        testCustomer = new Customer();
        testCustomer.setCustomerNumber("IT-TEST-001");
        testCustomer.setCompanyName("Integration Test Company");
        testCustomer.setStatus(CustomerStatus.AKTIV);
        testCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
        testCustomer.setIndustry(Industry.SONSTIGE);
        testCustomer.setCreatedAt(LocalDateTime.now());
        testCustomer.setCreatedBy("test");
        customerRepository.persist(testCustomer);
    }
    
    private void createTestEvent() {
        testEvent = new CustomerTimelineEvent();
        testEvent.setCustomer(testCustomer);
        testEvent.setEventType("NOTE");
        testEvent.setTitle("Test Note");
        testEvent.setDescription("Test Description");
        testEvent.setCategory(EventCategory.NOTE);
        testEvent.setImportance(ImportanceLevel.MEDIUM);
        testEvent.setPerformedBy("testuser");
        testEvent.setEventDate(LocalDateTime.now());
        timelineRepository.persist(testEvent);
    }
    
    @Transactional
    UUID createTestCustomerInTransaction() {
        Customer customer = new Customer();
        customer.setCustomerNumber("IT-TEST-001");
        customer.setCompanyName("Integration Test Company");
        customer.setStatus(CustomerStatus.AKTIV);
        customer.setCustomerType(CustomerType.UNTERNEHMEN);
        customer.setIndustry(Industry.SONSTIGE);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setCreatedBy("test");
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
    
    @Test
    void getTimeline_shouldReturnPaginatedEvents() {
        // Create test data in separate transactions
        UUID customerId = createTestCustomerInTransaction();
        UUID eventId = createTestEventInTransaction(customerId);
        
        Response response = given()
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
    @Transactional
    void getTimeline_withCategoryFilter_shouldReturnFilteredEvents() {
        createTestCustomer();
        createTestEvent();
        // Create additional event with different category
        CustomerTimelineEvent communicationEvent = new CustomerTimelineEvent();
        communicationEvent.setCustomer(testCustomer);
        communicationEvent.setEventType("CALL");
        communicationEvent.setTitle("Phone Call");
        communicationEvent.setCategory(EventCategory.PHONE_CALL);
        communicationEvent.setImportance(ImportanceLevel.HIGH);
        communicationEvent.setPerformedBy("salesrep");
        communicationEvent.setEventDate(LocalDateTime.now());
        timelineRepository.persist(communicationEvent);
        
        Response response = given()
                .pathParam("customerId", testCustomer.getId())
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
    @Transactional
    void getTimeline_withSearchText_shouldReturnMatchingEvents() {
        createTestCustomer();
        createTestEvent();
        Response response = given()
                .pathParam("customerId", testCustomer.getId())
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
    void getTimeline_withNonExistentCustomer_shouldReturn404() {
        given()
                .pathParam("customerId", UUID.randomUUID())
                .when()
                .get()
                .then()
                .statusCode(404);
    }
    
    @Test
    @Transactional
    void createEvent_withValidData_shouldCreateTimelineEvent() {
        createTestCustomer();
        CreateTimelineEventRequest request = CreateTimelineEventRequest.builder()
                .eventType("MEETING")
                .title("Customer Meeting")
                .description("Discussed Q1 plans")
                .category(EventCategory.MEETING)
                .importance(ImportanceLevel.HIGH)
                .performedBy("salesmanager")
                .performedByRole("Sales Manager")
                .build();
        
        Response response = given()
                .pathParam("customerId", testCustomer.getId())
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
    @Transactional
    void createNote_withValidData_shouldCreateNoteEvent() {
        createTestCustomer();
        CreateNoteRequest request = new CreateNoteRequest(
                "Customer prefers email communication",
                "salesrep"
        );
        
        Response response = given()
                .pathParam("customerId", testCustomer.getId())
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
    @Transactional
    void createCommunication_withValidData_shouldCreateCommunicationEvent() {
        createTestCustomer();
        CreateCommunicationRequest request = CreateCommunicationRequest.builder()
                .channel("email")
                .direction("outbound")
                .description("Sent price list and catalog")
                .performedBy("salesrep")
                .duration(10)
                .requiresFollowUp(
                        LocalDateTime.now().plusDays(3),
                        "Follow up on pricing questions"
                )
                .build();
        
        Response response = given()
                .pathParam("customerId", testCustomer.getId())
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
    void createEvent_withInvalidData_shouldReturn400() {
        CreateTimelineEventRequest request = new CreateTimelineEventRequest();
        // Missing required fields
        
        given()
                .pathParam("customerId", testCustomer.getId())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .statusCode(400);
    }
    
    @Test
    @Transactional
    void getFollowUps_shouldReturnEventsRequiringFollowUp() {
        createTestCustomer();
        // Create event with follow-up
        CustomerTimelineEvent followUpEvent = new CustomerTimelineEvent();
        followUpEvent.setCustomer(testCustomer);
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
        
        given()
                .pathParam("customerId", testCustomer.getId())
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
    @Transactional
    void getOverdueFollowUps_shouldReturnOverdueEvents() {
        createTestCustomer();
        // Create overdue follow-up
        CustomerTimelineEvent overdueEvent = new CustomerTimelineEvent();
        overdueEvent.setCustomer(testCustomer);
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
        
        given()
                .pathParam("customerId", testCustomer.getId())
                .when()
                .get("/follow-ups/overdue")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(1))
                .body("[0].isFollowUpOverdue", is(true));
    }
    
    @Test
    @Transactional
    void getRecentCommunications_shouldReturnRecentCommunicationEvents() {
        createTestCustomer();
        // Create recent communication
        CustomerTimelineEvent recentComm = CustomerTimelineEvent.createCommunicationEvent(
                testCustomer,
                "teams",
                "inbound",
                "Customer inquiry about new products",
                "support"
        );
        timelineRepository.persist(recentComm);
        
        given()
                .pathParam("customerId", testCustomer.getId())
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
    @Transactional
    void getTimelineSummary_shouldReturnSummaryStatistics() {
        createTestCustomer();
        createTestEvent();
        // Create various events
        for (int i = 0; i < 3; i++) {
            CustomerTimelineEvent event = new CustomerTimelineEvent();
            event.setCustomer(testCustomer);
            event.setEventType("COMM_" + i);
            event.setTitle("Communication " + i);
            event.setCategory(EventCategory.COMMUNICATION);
            event.setImportance(ImportanceLevel.MEDIUM);
            event.setPerformedBy("user" + i);
            event.setEventDate(LocalDateTime.now());
            timelineRepository.persist(event);
        }
        
        given()
                .pathParam("customerId", testCustomer.getId())
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
    @Transactional
    void updateEvent_withValidData_shouldUpdateTimelineEvent() {
        createTestCustomer();
        createTestEvent();
        UpdateTimelineEventRequest request = new UpdateTimelineEventRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated Description");
        request.setImportance(ImportanceLevel.CRITICAL);
        request.setUpdatedBy("manager");
        
        given()
                .pathParam("customerId", testCustomer.getId())
                .pathParam("eventId", testEvent.getId())
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
        CustomerTimelineEvent updated = timelineRepository.findById(testEvent.getId());
        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getImportance()).isEqualTo(ImportanceLevel.CRITICAL);
    }
    
    @Test
    @Transactional
    void completeFollowUp_shouldMarkAsCompleted() {
        createTestCustomer();
        createTestEvent();
        // Update event with follow-up
        testEvent.setRequiresFollowUp(true);
        testEvent.setFollowUpDate(LocalDateTime.now().plusDays(1));
        testEvent.setFollowUpCompleted(false);
        // No persist needed - entity is already persisted and managed
        
        given()
                .pathParam("customerId", testCustomer.getId())
                .pathParam("eventId", testEvent.getId())
                .queryParam("completedBy", "manager")
                .contentType(ContentType.JSON)
                .when()
                .post("/events/{eventId}/complete-follow-up")
                .then()
                .statusCode(204);
        
        // Verify in database
        CustomerTimelineEvent updated = timelineRepository.findById(testEvent.getId());
        assertThat(updated.getFollowUpCompleted()).isTrue();
    }
    
    @Test
    @Transactional
    void deleteEvent_shouldSoftDeleteEvent() {
        createTestCustomer();
        createTestEvent();
        given()
                .pathParam("customerId", testCustomer.getId())
                .pathParam("eventId", testEvent.getId())
                .queryParam("deletedBy", "admin")
                .when()
                .delete("/events/{eventId}")
                .then()
                .statusCode(204);
        
        // Verify soft delete in database
        CustomerTimelineEvent deleted = timelineRepository.findById(testEvent.getId());
        assertThat(deleted.getIsDeleted()).isTrue();
        assertThat(deleted.getDeletedBy()).isEqualTo("admin");
        assertThat(deleted.getDeletedAt()).isNotNull();
    }
}
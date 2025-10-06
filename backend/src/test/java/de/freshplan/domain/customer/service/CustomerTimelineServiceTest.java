package de.freshplan.domain.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.repository.CustomerTimelineRepository;
import de.freshplan.domain.customer.service.dto.timeline.*;
import de.freshplan.domain.customer.service.exception.CustomerNotFoundException;
import de.freshplan.domain.customer.service.mapper.CustomerTimelineMapper;
import de.freshplan.test.builders.CustomerTestDataFactory;
import io.quarkus.panache.common.Page;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Unit tests for CustomerTimelineService.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@Tag("integration")
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
class CustomerTimelineServiceTest {

  @Inject CustomerTimelineService timelineService;

  @InjectMock CustomerTimelineRepository timelineRepository;

  @InjectMock CustomerRepository customerRepository;

  @InjectMock CustomerTimelineMapper timelineMapper;

  private UUID customerId;
  private Customer customer;
  private CustomerTimelineEvent timelineEvent;
  private TimelineEventResponse timelineResponse;

  @BeforeEach
  void setUp() {
    customerId = UUID.randomUUID();

    customer = CustomerTestDataFactory.builder().withCompanyName("Test Company").build();
    customer.setId(customerId);
    customer.setCustomerNumber("CUST001");
    customer.setCompanyName("Test Company"); // Override to remove [TEST-xxx] prefix

    timelineEvent = new CustomerTimelineEvent();
    timelineEvent.setId(UUID.randomUUID());
    timelineEvent.setCustomer(customer);
    timelineEvent.setEventType("NOTE");
    timelineEvent.setTitle("Test Note");
    timelineEvent.setDescription("Test Description");
    timelineEvent.setCategory(EventCategory.NOTE);
    timelineEvent.setImportance(ImportanceLevel.MEDIUM);
    timelineEvent.setPerformedBy("testuser");
    timelineEvent.setEventDate(LocalDateTime.now());

    timelineResponse = new TimelineEventResponse();
    timelineResponse.setId(timelineEvent.getId());
    timelineResponse.setEventType(timelineEvent.getEventType());
    timelineResponse.setTitle(timelineEvent.getTitle());
    timelineResponse.setDescription(timelineEvent.getDescription());
    timelineResponse.setCategory(timelineEvent.getCategory());
    timelineResponse.setImportance(timelineEvent.getImportance());
    timelineResponse.setPerformedBy(timelineEvent.getPerformedBy());
  }

  @Test
  void createEvent_withValidData_shouldCreateTimelineEvent() {
    // Given
    CreateTimelineEventRequest request =
        CreateTimelineEventRequest.builder()
            .eventType("CALL")
            .title("Customer Call")
            .description("Discussed new requirements")
            .category(EventCategory.PHONE_CALL)
            .importance(ImportanceLevel.HIGH)
            .performedBy("salesrep")
            .performedByRole("Sales Representative")
            .build();

    when(customerRepository.findByIdOptional(customerId)).thenReturn(Optional.of(customer));
    when(timelineMapper.toResponse(any(CustomerTimelineEvent.class))).thenReturn(timelineResponse);

    // When
    TimelineEventResponse result = timelineService.createEvent(customerId, request);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(timelineResponse.getId());

    ArgumentCaptor<CustomerTimelineEvent> eventCaptor =
        ArgumentCaptor.forClass(CustomerTimelineEvent.class);
    verify(timelineRepository).persist(eventCaptor.capture());

    CustomerTimelineEvent capturedEvent = eventCaptor.getValue();
    assertThat(capturedEvent.getEventType()).isEqualTo("CALL");
    assertThat(capturedEvent.getTitle()).isEqualTo("Customer Call");
    assertThat(capturedEvent.getDescription()).isEqualTo("Discussed new requirements");
    assertThat(capturedEvent.getCategory()).isEqualTo(EventCategory.PHONE_CALL);
    assertThat(capturedEvent.getImportance()).isEqualTo(ImportanceLevel.HIGH);
    assertThat(capturedEvent.getPerformedBy()).isEqualTo("salesrep");
  }

  @Test
  void createEvent_withNonExistentCustomer_shouldThrowException() {
    // Given
    CreateTimelineEventRequest request =
        CreateTimelineEventRequest.builder()
            .eventType("NOTE")
            .title("Test Note")
            .category(EventCategory.NOTE)
            .performedBy("testuser")
            .build();

    when(customerRepository.findByIdOptional(customerId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> timelineService.createEvent(customerId, request))
        .isInstanceOf(CustomerNotFoundException.class)
        .hasMessageContaining(customerId.toString());
  }

  @Test
  void createNote_withValidData_shouldCreateNoteEvent() {
    // Given
    CreateNoteRequest request =
        new CreateNoteRequest("Quick note about customer preferences", "salesrep");

    when(customerRepository.findByIdOptional(customerId)).thenReturn(Optional.of(customer));
    when(timelineMapper.toResponse(any(CustomerTimelineEvent.class))).thenReturn(timelineResponse);

    // When
    TimelineEventResponse result = timelineService.createNote(customerId, request);

    // Then
    assertThat(result).isNotNull();

    ArgumentCaptor<CustomerTimelineEvent> eventCaptor =
        ArgumentCaptor.forClass(CustomerTimelineEvent.class);
    verify(timelineRepository).persist(eventCaptor.capture());

    CustomerTimelineEvent capturedEvent = eventCaptor.getValue();
    assertThat(capturedEvent.getEventType()).isEqualTo("NOTE");
    assertThat(capturedEvent.getTitle()).isEqualTo("Notiz");
    assertThat(capturedEvent.getDescription()).isEqualTo("Quick note about customer preferences");
    assertThat(capturedEvent.getCategory()).isEqualTo(EventCategory.NOTE);
    assertThat(capturedEvent.getPerformedBy()).isEqualTo("salesrep");
  }

  @Test
  void createCommunication_withValidData_shouldCreateCommunicationEvent() {
    // Given
    CreateCommunicationRequest request =
        CreateCommunicationRequest.builder()
            .channel("email")
            .direction("outbound")
            .description("Sent product catalog")
            .performedBy("salesrep")
            .duration(5)
            .build();

    when(customerRepository.findByIdOptional(customerId)).thenReturn(Optional.of(customer));
    when(timelineMapper.toResponse(any(CustomerTimelineEvent.class))).thenReturn(timelineResponse);

    // When
    TimelineEventResponse result = timelineService.createCommunication(customerId, request);

    // Then
    assertThat(result).isNotNull();

    ArgumentCaptor<CustomerTimelineEvent> eventCaptor =
        ArgumentCaptor.forClass(CustomerTimelineEvent.class);
    verify(timelineRepository).persist(eventCaptor.capture());

    CustomerTimelineEvent capturedEvent = eventCaptor.getValue();
    assertThat(capturedEvent.getEventType()).isEqualTo("COMMUNICATION");
    assertThat(capturedEvent.getTitle()).isEqualTo("Kommunikation via email");
    assertThat(capturedEvent.getDescription()).isEqualTo("Sent product catalog");
    assertThat(capturedEvent.getCategory()).isEqualTo(EventCategory.COMMUNICATION);
    assertThat(capturedEvent.getCommunicationChannel()).isEqualTo("email");
    assertThat(capturedEvent.getCommunicationDirection()).isEqualTo("outbound");
    assertThat(capturedEvent.getCommunicationDuration()).isEqualTo(5);
  }

  @Test
  void getCustomerTimeline_withValidCustomer_shouldReturnPaginatedResults() {
    // Given
    List<CustomerTimelineEvent> events = Arrays.asList(timelineEvent);
    when(customerRepository.findByIdOptional(customerId)).thenReturn(Optional.of(customer));
    when(timelineRepository.findByCustomerId(eq(customerId), any(Page.class))).thenReturn(events);
    when(timelineRepository.countByCustomerId(customerId)).thenReturn(1L);
    when(timelineMapper.toResponse(timelineEvent)).thenReturn(timelineResponse);

    // When
    TimelineListResponse result =
        timelineService.getCustomerTimeline(customerId, 0, 20, null, null);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getId()).isEqualTo(timelineResponse.getId());
    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getTotalPages()).isEqualTo(1);
    assertThat(result.isFirst()).isTrue();
    assertThat(result.isLast()).isTrue();
  }

  @Test
  void getCustomerTimeline_withCategoryFilter_shouldReturnFilteredResults() {
    // Given
    List<CustomerTimelineEvent> events = Arrays.asList(timelineEvent);
    when(customerRepository.findByIdOptional(customerId)).thenReturn(Optional.of(customer));
    when(timelineRepository.findByCustomerIdAndCategory(
            eq(customerId), eq(EventCategory.NOTE), any(Page.class)))
        .thenReturn(events);
    when(timelineRepository.countByCustomerIdAndCategory(customerId, EventCategory.NOTE))
        .thenReturn(1L);
    when(timelineMapper.toResponse(timelineEvent)).thenReturn(timelineResponse);

    // When
    TimelineListResponse result =
        timelineService.getCustomerTimeline(customerId, 0, 20, "NOTE", null);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(timelineRepository)
        .findByCustomerIdAndCategory(eq(customerId), eq(EventCategory.NOTE), any(Page.class));
  }

  @Test
  void getCustomerTimeline_withSearchText_shouldReturnSearchResults() {
    // Given
    String searchText = "test";
    List<CustomerTimelineEvent> events = Arrays.asList(timelineEvent);
    when(customerRepository.findByIdOptional(customerId)).thenReturn(Optional.of(customer));
    when(timelineRepository.searchByCustomerIdAndText(
            eq(customerId), eq(searchText), any(Page.class)))
        .thenReturn(events);
    when(timelineRepository.count(anyString(), any(Object[].class))).thenReturn(1L);
    when(timelineMapper.toResponse(timelineEvent)).thenReturn(timelineResponse);

    // When
    TimelineListResponse result =
        timelineService.getCustomerTimeline(customerId, 0, 20, null, searchText);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(timelineRepository)
        .searchByCustomerIdAndText(eq(customerId), eq(searchText), any(Page.class));
  }

  @Test
  void getFollowUpEvents_shouldReturnEventsRequiringFollowUp() {
    // Given
    timelineEvent.setRequiresFollowUp(true);
    timelineEvent.setFollowUpDate(LocalDateTime.now().plusDays(1));
    List<CustomerTimelineEvent> events = Arrays.asList(timelineEvent);

    when(timelineRepository.findRequiringFollowUp(customerId)).thenReturn(events);
    when(timelineMapper.toResponse(timelineEvent)).thenReturn(timelineResponse);

    // When
    List<TimelineEventResponse> result = timelineService.getFollowUpEvents(customerId);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(timelineResponse.getId());
  }

  @Test
  void completeFollowUp_shouldMarkFollowUpAsCompleted() {
    // Given
    UUID eventId = UUID.randomUUID();
    String completedBy = "manager";

    // When
    timelineService.completeFollowUp(eventId, completedBy);

    // Then
    verify(timelineRepository).completeFollowUp(eventId, completedBy);
  }

  @Test
  void updateEvent_withValidData_shouldUpdateTimelineEvent() {
    // Given
    UUID eventId = timelineEvent.getId();
    UpdateTimelineEventRequest request = new UpdateTimelineEventRequest();
    request.setTitle("Updated Title");
    request.setDescription("Updated Description");
    request.setImportance(ImportanceLevel.HIGH);
    request.setUpdatedBy("manager");

    when(timelineRepository.findByIdOptional(eventId)).thenReturn(Optional.of(timelineEvent));
    when(timelineMapper.toResponse(timelineEvent)).thenReturn(timelineResponse);

    // When
    TimelineEventResponse result = timelineService.updateEvent(eventId, request);

    // Then
    assertThat(result).isNotNull();
    assertThat(timelineEvent.getTitle()).isEqualTo("Updated Title");
    assertThat(timelineEvent.getDescription()).isEqualTo("Updated Description");
    assertThat(timelineEvent.getImportance()).isEqualTo(ImportanceLevel.HIGH);
    assertThat(timelineEvent.getUpdatedBy()).isEqualTo("manager");

    verify(timelineRepository).persist(timelineEvent);
  }

  @Test
  void deleteEvent_shouldSoftDeleteEvent() {
    // Given
    UUID eventId = UUID.randomUUID();
    String deletedBy = "admin";

    // When
    timelineService.deleteEvent(eventId, deletedBy);

    // Then
    verify(timelineRepository).softDelete(eventId, deletedBy);
  }

  @Test
  void getTimelineSummary_shouldReturnSummaryStatistics() {
    // Given
    CustomerTimelineRepository.TimelineSummary repoSummary =
        new CustomerTimelineRepository.TimelineSummary();
    repoSummary.totalEvents = 100;
    repoSummary.communicationEvents = 40;
    repoSummary.meetingEvents = 20;
    repoSummary.taskEvents = 15;
    repoSummary.systemEvents = 25;

    when(timelineRepository.getTimelineSummary(customerId)).thenReturn(repoSummary);

    // When
    TimelineSummaryResponse result = timelineService.getTimelineSummary(customerId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getTotalEvents()).isEqualTo(100);
    assertThat(result.getCommunicationEvents()).isEqualTo(40);
    assertThat(result.getMeetingEvents()).isEqualTo(20);
    assertThat(result.getTaskEvents()).isEqualTo(15);
    assertThat(result.getSystemEvents()).isEqualTo(25);
  }

  @Test
  void createSystemEvent_shouldCreateSystemGeneratedEvent() {
    // Given
    String eventType = "STATUS_CHANGE";
    String description = "Customer status changed to ACTIVE";
    String performedBy = "system";

    // When
    timelineService.createSystemEvent(customer, eventType, description, performedBy);

    // Then
    ArgumentCaptor<CustomerTimelineEvent> eventCaptor =
        ArgumentCaptor.forClass(CustomerTimelineEvent.class);
    verify(timelineRepository).persist(eventCaptor.capture());

    CustomerTimelineEvent capturedEvent = eventCaptor.getValue();
    assertThat(capturedEvent.getCustomer()).isEqualTo(customer);
    assertThat(capturedEvent.getEventType()).isEqualTo(eventType);
    assertThat(capturedEvent.getDescription()).isEqualTo(description);
    assertThat(capturedEvent.getCategory()).isEqualTo(EventCategory.SYSTEM);
    assertThat(capturedEvent.getPerformedBy()).isEqualTo(performedBy);
  }
}

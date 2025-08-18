package de.freshplan.domain.opportunity.service.command;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.service.AuditService;
import de.freshplan.domain.audit.service.dto.AuditContext;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityActivity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.domain.opportunity.service.dto.ChangeStageRequest;
import de.freshplan.domain.opportunity.service.dto.CreateOpportunityRequest;
import de.freshplan.domain.opportunity.service.dto.OpportunityResponse;
import de.freshplan.domain.opportunity.service.dto.UpdateOpportunityRequest;
import de.freshplan.domain.opportunity.service.exception.InvalidStageTransitionException;
import de.freshplan.domain.opportunity.service.exception.OpportunityNotFoundException;
import de.freshplan.domain.opportunity.service.mapper.OpportunityMapper;
import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.test.TestDataBuilder;
import de.freshplan.test.builders.OpportunityTestDataFactory;
import de.freshplan.test.builders.UserTestDataFactory;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Unit Tests für OpportunityCommandService
 *
 * <p>Diese Tests stellen sicher, dass der OpportunityCommandService identisches Verhalten zum
 * originalen OpportunityService aufweist.
 */
@QuarkusTest
class OpportunityCommandServiceTest {

  @Inject OpportunityCommandService commandService;

  @InjectMock OpportunityRepository opportunityRepository;

  @InjectMock CustomerRepository customerRepository;

  @InjectMock UserRepository userRepository;

  @InjectMock OpportunityMapper opportunityMapper;

  @InjectMock SecurityIdentity securityIdentity;

  @InjectMock AuditService auditService;

  private User testUser;
  private Customer testCustomer;
  private Opportunity testOpportunity;
  private UUID testOpportunityId;
  private UUID testCustomerId;
  private OpportunityResponse testResponse;

  @BeforeEach
  void setUp() {
    // Test-User Setup
    testUser =
        UserTestDataFactory.builder()
            .withUsername("testuser")
            .withFirstName("Test")
            .withLastName("User")
            .withEmail("test@example.com")
            .withRoles(Arrays.asList("admin", "manager", "sales"))
            .build();

    // Test-Customer Setup
    testCustomerId = UUID.randomUUID();
    testCustomer = TestDataBuilder.createTestCustomer("Test Customer GmbH");
    testCustomer.setId(testCustomerId);

    // Test-Opportunity Setup
    testOpportunityId = UUID.randomUUID();
    testOpportunity =
        OpportunityTestDataFactory.builder()
            .withName("Test Opportunity")
            .inStage(OpportunityStage.NEW_LEAD)
            .assignedTo(testUser)
            .withExpectedValue(new BigDecimal("10000"))
            .withExpectedCloseDate(LocalDate.now().plusDays(30))
            .build();
    testOpportunity.setId(testOpportunityId);

    // Test-Response Setup
    testResponse =
        OpportunityResponse.builder()
            .id(testOpportunityId)
            .name("Test Opportunity")
            .stage(OpportunityStage.NEW_LEAD)
            .expectedValue(new BigDecimal("10000"))
            .expectedCloseDate(LocalDate.now().plusDays(30))
            .build();

    // Security Mock
    Principal principal = mock(Principal.class);
    when(principal.getName()).thenReturn("testuser");
    when(securityIdentity.getPrincipal()).thenReturn(principal);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
  }

  // =====================================
  // CREATE OPERATION TESTS
  // =====================================

  @Test
  void createOpportunity_withValidRequest_shouldCreateSuccessfully() {
    // Given
    CreateOpportunityRequest request =
        CreateOpportunityRequest.builder()
            .name("New Opportunity")
            .customerId(testCustomerId)
            .description("Test description")
            .expectedValue(new BigDecimal("5000"))
            .expectedCloseDate(LocalDate.now().plusDays(60))
            .build();

    when(customerRepository.findByIdOptional(testCustomerId)).thenReturn(Optional.of(testCustomer));
    when(opportunityMapper.toResponse(any(Opportunity.class))).thenReturn(testResponse);

    // When
    OpportunityResponse response = commandService.createOpportunity(request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getName()).isEqualTo("Test Opportunity");

    // Verify Repository persist was called
    ArgumentCaptor<Opportunity> opportunityCaptor = ArgumentCaptor.forClass(Opportunity.class);
    verify(opportunityRepository).persist(opportunityCaptor.capture());

    Opportunity created = opportunityCaptor.getValue();
    assertThat(created.getName()).isEqualTo("New Opportunity");
    assertThat(created.getStage()).isEqualTo(OpportunityStage.NEW_LEAD);
    assertThat(created.getDescription()).isEqualTo("Test description");

    // Verify Audit Log was created
    verify(auditService).logAsync(any(AuditContext.class));
  }

  @Test
  void createOpportunity_withNullCustomerId_shouldThrowException() {
    // Given
    CreateOpportunityRequest request =
        CreateOpportunityRequest.builder().name("New Opportunity").customerId(null).build();

    // When & Then
    assertThatThrownBy(() -> commandService.createOpportunity(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Customer ID cannot be null");
  }

  @Test
  void createOpportunity_withPastCloseDate_shouldThrowException() {
    // Given
    CreateOpportunityRequest request =
        CreateOpportunityRequest.builder()
            .name("New Opportunity")
            .customerId(UUID.randomUUID())
            .expectedCloseDate(LocalDate.now().minusDays(1))
            .build();

    // When & Then
    assertThatThrownBy(() -> commandService.createOpportunity(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Expected close date cannot be in the past");
  }

  // =====================================
  // UPDATE OPERATION TESTS
  // =====================================

  @Test
  void updateOpportunity_withValidRequest_shouldUpdateSuccessfully() {
    // Given
    UpdateOpportunityRequest request =
        UpdateOpportunityRequest.builder()
            .name("Updated Opportunity")
            .description("Updated description")
            .expectedValue(new BigDecimal("15000"))
            .expectedCloseDate(LocalDate.now().plusDays(45))
            .probability(75)
            .build();

    when(opportunityRepository.findByIdOptional(testOpportunityId))
        .thenReturn(Optional.of(testOpportunity));
    when(opportunityMapper.toResponse(testOpportunity)).thenReturn(testResponse);

    // When
    OpportunityResponse response = commandService.updateOpportunity(testOpportunityId, request);

    // Then
    assertThat(response).isNotNull();
    assertThat(testOpportunity.getName()).isEqualTo("Updated Opportunity");
    assertThat(testOpportunity.getDescription()).isEqualTo("Updated description");
    assertThat(testOpportunity.getExpectedValue()).isEqualTo(new BigDecimal("15000"));
    assertThat(testOpportunity.getProbability()).isEqualTo(75);

    verify(opportunityRepository).persist(testOpportunity);
  }

  @Test
  void updateOpportunity_withNonExistentId_shouldThrowException() {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    UpdateOpportunityRequest request =
        UpdateOpportunityRequest.builder().name("Updated Opportunity").build();

    when(opportunityRepository.findByIdOptional(nonExistentId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> commandService.updateOpportunity(nonExistentId, request))
        .isInstanceOf(OpportunityNotFoundException.class);
  }

  // =====================================
  // DELETE OPERATION TESTS
  // =====================================

  @Test
  void deleteOpportunity_withExistingId_shouldDeleteSuccessfully() {
    // Given
    when(opportunityRepository.findByIdOptional(testOpportunityId))
        .thenReturn(Optional.of(testOpportunity));

    // When
    commandService.deleteOpportunity(testOpportunityId);

    // Then
    verify(opportunityRepository).delete(testOpportunity);
  }

  @Test
  void deleteOpportunity_withNonExistentId_shouldThrowException() {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    when(opportunityRepository.findByIdOptional(nonExistentId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> commandService.deleteOpportunity(nonExistentId))
        .isInstanceOf(OpportunityNotFoundException.class);
  }

  // =====================================
  // STAGE MANAGEMENT TESTS
  // =====================================

  @Test
  void changeStage_withValidTransition_shouldChangeSuccessfully() {
    // Given
    testOpportunity.setStage(OpportunityStage.NEW_LEAD);
    when(opportunityRepository.findByIdOptional(testOpportunityId))
        .thenReturn(Optional.of(testOpportunity));
    when(opportunityMapper.toResponse(testOpportunity)).thenReturn(testResponse);

    // When
    OpportunityResponse response =
        commandService.changeStage(
            testOpportunityId, OpportunityStage.QUALIFICATION, "Moving to qualification");

    // Then
    assertThat(response).isNotNull();
    assertThat(testOpportunity.getStage()).isEqualTo(OpportunityStage.QUALIFICATION);

    // Verify Audit Log
    ArgumentCaptor<AuditContext> auditCaptor = ArgumentCaptor.forClass(AuditContext.class);
    verify(auditService).logAsync(auditCaptor.capture());

    AuditContext audit = auditCaptor.getValue();
    assertThat(audit.getEventType()).isEqualTo(AuditEventType.OPPORTUNITY_STAGE_CHANGED);
    assertThat(audit.getChangeReason()).isEqualTo("Moving to qualification");

    verify(opportunityRepository).persist(testOpportunity);
  }

  @Test
  void changeStage_withInvalidTransition_shouldThrowException() {
    // Given
    testOpportunity.setStage(OpportunityStage.NEW_LEAD);
    when(opportunityRepository.findByIdOptional(testOpportunityId))
        .thenReturn(Optional.of(testOpportunity));

    // Mock canTransitionTo to return false for invalid transition
    OpportunityStage invalidStage = OpportunityStage.CLOSED_WON;

    // When & Then
    assertThatThrownBy(
            () -> commandService.changeStage(testOpportunityId, invalidStage, "Invalid transition"))
        .isInstanceOf(InvalidStageTransitionException.class);
  }

  @Test
  void changeStage_withDefaultReason_shouldUseDefaultMessage() {
    // Given
    testOpportunity.setStage(OpportunityStage.NEW_LEAD);
    when(opportunityRepository.findByIdOptional(testOpportunityId))
        .thenReturn(Optional.of(testOpportunity));
    when(opportunityMapper.toResponse(testOpportunity)).thenReturn(testResponse);

    // When
    OpportunityResponse response =
        commandService.changeStage(testOpportunityId, OpportunityStage.QUALIFICATION);

    // Then
    assertThat(response).isNotNull();

    // Verify default reason was used
    ArgumentCaptor<AuditContext> auditCaptor = ArgumentCaptor.forClass(AuditContext.class);
    verify(auditService).logAsync(auditCaptor.capture());
    assertThat(auditCaptor.getValue().getChangeReason()).isEqualTo("Stage change");
  }

  @Test
  void changeStage_withChangeStageRequest_shouldHandleCustomProbability() {
    // Given
    ChangeStageRequest request = new ChangeStageRequest();
    request.setStage(OpportunityStage.PROPOSAL);
    request.setCustomProbability(80);
    request.setReason("Good progress");

    testOpportunity.setStage(OpportunityStage.NEEDS_ANALYSIS);
    when(opportunityRepository.findByIdOptional(testOpportunityId))
        .thenReturn(Optional.of(testOpportunity));
    when(opportunityMapper.toResponse(testOpportunity)).thenReturn(testResponse);

    // When
    OpportunityResponse response = commandService.changeStage(testOpportunityId, request);

    // Then
    assertThat(response).isNotNull();
    assertThat(testOpportunity.getStage()).isEqualTo(OpportunityStage.PROPOSAL);
    assertThat(testOpportunity.getProbability()).isEqualTo(80);
  }

  @Test
  void changeStage_fromClosedState_shouldThrowException() {
    // Given
    ChangeStageRequest request = new ChangeStageRequest();
    request.setStage(OpportunityStage.QUALIFICATION);

    testOpportunity.setStage(OpportunityStage.CLOSED_WON);
    when(opportunityRepository.findByIdOptional(testOpportunityId))
        .thenReturn(Optional.of(testOpportunity));

    // When & Then
    assertThatThrownBy(() -> commandService.changeStage(testOpportunityId, request))
        .isInstanceOf(InvalidStageTransitionException.class)
        .hasMessage("Cannot change stage from closed state");
  }

  // =====================================
  // ACTIVITY MANAGEMENT TESTS
  // =====================================

  @Test
  void addActivity_withValidData_shouldAddSuccessfully() {
    // Given
    when(opportunityRepository.findByIdOptional(testOpportunityId))
        .thenReturn(Optional.of(testOpportunity));
    when(opportunityMapper.toResponse(testOpportunity)).thenReturn(testResponse);

    // When
    OpportunityResponse response =
        commandService.addActivity(
            testOpportunityId,
            OpportunityActivity.ActivityType.NOTE,
            "Test Activity",
            "Test Description");

    // Then
    assertThat(response).isNotNull();
    assertThat(testOpportunity.getActivities()).hasSize(1);

    OpportunityActivity activity = testOpportunity.getActivities().get(0);
    assertThat(activity.getTitle()).isEqualTo("Test Activity");
    assertThat(activity.getDescription()).isEqualTo("Test Description");
    assertThat(activity.getActivityType()).isEqualTo(OpportunityActivity.ActivityType.NOTE);

    verify(opportunityRepository).persist(testOpportunity);
  }

  // =====================================
  // HELPER METHOD TESTS
  // =====================================

  @Test
  void getCurrentUser_withSecurityIdentity_shouldReturnUser() {
    // This is implicitly tested in other tests, but we can add explicit test
    // The getCurrentUser method is private, so it's tested through public methods

    // Given
    CreateOpportunityRequest request =
        CreateOpportunityRequest.builder().name("Test").customerId(testCustomerId).build();

    when(customerRepository.findByIdOptional(testCustomerId)).thenReturn(Optional.of(testCustomer));
    when(opportunityMapper.toResponse(any())).thenReturn(testResponse);

    // When
    commandService.createOpportunity(request);

    // Then
    verify(userRepository).findByUsername("testuser");
  }

  @Test
  void getCurrentUser_withoutSecurityIdentity_shouldUseFallback() {
    // Given
    when(securityIdentity.getPrincipal()).thenThrow(new RuntimeException("No security"));
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

    CreateOpportunityRequest request =
        CreateOpportunityRequest.builder().name("Test").customerId(testCustomerId).build();

    when(customerRepository.findByIdOptional(testCustomerId)).thenReturn(Optional.of(testCustomer));
    when(opportunityMapper.toResponse(any())).thenReturn(testResponse);

    // When
    commandService.createOpportunity(request);

    // Then
    verify(userRepository).findByUsername("testuser");
  }
}

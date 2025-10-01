package de.freshplan.domain.opportunity.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityActivity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.domain.opportunity.service.dto.CreateOpportunityRequest;
import de.freshplan.domain.opportunity.service.dto.UpdateOpportunityRequest;
import de.freshplan.domain.opportunity.service.exception.OpportunityNotFoundException;
import de.freshplan.domain.opportunity.service.mapper.OpportunityMapper;
import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.test.builders.CustomerBuilder;
import de.freshplan.test.builders.OpportunityTestDataFactory;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Mock-basierte Unit Tests für OpportunityService
 *
 * <p>Diese vereinfachte Version testet die Core Business Logic ohne echte Datenbankzugriffe.
 */
@QuarkusTest
@Tag("integration")
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
@DisplayName("OpportunityService Tests (Mock-based)")
public class OpportunityServiceMockTest {

  @Inject OpportunityService opportunityService;

  @InjectMock OpportunityRepository opportunityRepository;

  @InjectMock CustomerRepository customerRepository;

  @Inject CustomerBuilder customerBuilder;

  @InjectMock UserRepository userRepository;

  @InjectMock SecurityIdentity securityIdentity;

  @Inject OpportunityMapper opportunityMapper;

  private UUID customerId;
  private UUID userId;
  private UUID opportunityId;
  private Customer testCustomer;
  private User testUser;

  @BeforeEach
  void setUp() {
    // Reset all mocks
    reset(opportunityRepository, customerRepository, userRepository, securityIdentity);

    // Setup test IDs
    customerId = UUID.randomUUID();
    userId = UUID.randomUUID();
    opportunityId = UUID.randomUUID();

    // Create test customer
    testCustomer = customerBuilder.withCompanyName("[TEST] Test Company GmbH").build();
    testCustomer.setId(customerId);
    testCustomer.setCompanyName("[TEST] Test Company GmbH"); // Keep [TEST] prefix
    testCustomer.setCustomerNumber(de.freshplan.TestIds.uniqueCustomerNumber());
    testCustomer.setIsTestData(true); // Mark as test data

    // Create test user (mocked)
    testUser = mock(User.class);
    when(testUser.getId()).thenReturn(userId);
    when(testUser.getUsername()).thenReturn("testuser");
    when(testUser.getFirstName()).thenReturn("Test");
    when(testUser.getLastName()).thenReturn("User");
    when(testUser.getEmail()).thenReturn("test@example.com");

    // Setup default mock behavior
    when(customerRepository.findByIdOptional(customerId)).thenReturn(Optional.of(testCustomer));
    when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));

    // Mock SecurityIdentity
    Principal principal = mock(Principal.class);
    when(principal.getName()).thenReturn("testuser");
    when(securityIdentity.getPrincipal()).thenReturn(principal);

    // Mock getCurrentUser - findByUsername
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

    // Mock persist to set ID
    doAnswer(
            invocation -> {
              Opportunity opp = invocation.getArgument(0);
              if (opp.getId() == null) {
                opp.setId(UUID.randomUUID());
              }
              return null;
            })
        .when(opportunityRepository)
        .persist(any(Opportunity.class));
  }

  @Test
  @DisplayName("Should create opportunity with valid data")
  void createOpportunity_withValidData_shouldReturnCreatedOpportunity() {
    // Arrange
    var request =
        CreateOpportunityRequest.builder()
            .name("Test Opportunity")
            .description("Test Description")
            .customerId(customerId)
            .assignedTo(userId)
            .expectedValue(BigDecimal.valueOf(25000.00))
            .expectedCloseDate(LocalDate.now().plusDays(30))
            .build();

    // Act
    var result = opportunityService.createOpportunity(request);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Test Opportunity");
    assertThat(result.getDescription()).isEqualTo("Test Description");
    assertThat(result.getStage()).isEqualTo(OpportunityStage.NEW_LEAD);
    assertThat(result.getExpectedValue()).isEqualTo(BigDecimal.valueOf(25000.00));
    assertThat(result.getProbability()).isEqualTo(10); // Default for NEW_LEAD

    // Verify persistence was called
    ArgumentCaptor<Opportunity> oppCaptor = ArgumentCaptor.forClass(Opportunity.class);
    verify(opportunityRepository).persist(oppCaptor.capture());

    Opportunity persistedOpp = oppCaptor.getValue();
    assertThat(persistedOpp.getName()).isEqualTo("Test Opportunity");
    // Customer assignment will be implemented with Customer Repository integration
    // assertThat(persistedOpp.getCustomer()).isEqualTo(testCustomer); // TODO: Enable when Customer
    // integration complete
    assertThat(persistedOpp.getAssignedTo()).isEqualTo(testUser);
  }

  @Test
  @DisplayName("Should throw exception when creating opportunity with null customer ID")
  void createOpportunity_withNullCustomerId_shouldThrowException() {
    // Arrange
    var request =
        CreateOpportunityRequest.builder()
            .name("Test Opportunity")
            .customerId(null) // Invalid
            .build();

    // Act & Assert
    assertThatThrownBy(() -> opportunityService.createOpportunity(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Customer ID");
  }

  @Test
  @DisplayName("Should find opportunity by ID")
  void findById_withExistingId_shouldReturnOpportunity() {
    // Arrange
    Opportunity existingOpp =
        OpportunityTestDataFactory.builder()
            .withName("Existing Opportunity")
            .forCustomer(testCustomer)
            .inStage(OpportunityStage.PROPOSAL)
            .withProbability(60)
            .build();
    existingOpp.setId(opportunityId);

    when(opportunityRepository.findByIdOptional(opportunityId))
        .thenReturn(Optional.of(existingOpp));

    // Act
    var result = opportunityService.findById(opportunityId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(opportunityId);
    assertThat(result.getName()).isEqualTo("Existing Opportunity");
    assertThat(result.getStage()).isEqualTo(OpportunityStage.PROPOSAL);
  }

  @Test
  @DisplayName("Should throw exception for non-existent ID")
  void findById_withNonExistentId_shouldThrowException() {
    // Arrange
    when(opportunityRepository.findByIdOptional(opportunityId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> opportunityService.findById(opportunityId))
        .isInstanceOf(OpportunityNotFoundException.class);
  }

  @Test
  @DisplayName("Should update opportunity with valid data")
  void updateOpportunity_withValidData_shouldReturnUpdatedOpportunity() {
    // Arrange
    Opportunity existingOpp =
        OpportunityTestDataFactory.builder()
            .withName("Original Name")
            .withDescription("Original Description")
            .forCustomer(testCustomer)
            .inStage(OpportunityStage.NEW_LEAD)
            .build();
    existingOpp.setId(opportunityId);

    when(opportunityRepository.findByIdOptional(opportunityId))
        .thenReturn(Optional.of(existingOpp));

    var updateRequest =
        UpdateOpportunityRequest.builder()
            .name("Updated Name")
            .description("Updated Description")
            .expectedValue(BigDecimal.valueOf(35000))
            .build();

    // Act
    var result = opportunityService.updateOpportunity(opportunityId, updateRequest);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Updated Name");
    assertThat(result.getDescription()).isEqualTo("Updated Description");
    assertThat(result.getExpectedValue()).isEqualTo(BigDecimal.valueOf(35000));

    // Verify persist was called
    verify(opportunityRepository).persist(existingOpp);
  }

  @Test
  @DisplayName("Should delete opportunity")
  void deleteOpportunity_withExistingId_shouldDeleteSuccessfully() {
    // Arrange
    Opportunity existingOpp =
        OpportunityTestDataFactory.builder().withName("Test Opportunity").build();
    existingOpp.setId(opportunityId);
    when(opportunityRepository.findByIdOptional(opportunityId))
        .thenReturn(Optional.of(existingOpp));

    // Act
    opportunityService.deleteOpportunity(opportunityId);

    // Assert
    verify(opportunityRepository).delete(existingOpp);
  }

  @Test
  @DisplayName("Should throw exception when deleting non-existent opportunity")
  void deleteOpportunity_withNonExistentId_shouldThrowException() {
    // Arrange
    when(opportunityRepository.findByIdOptional(opportunityId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> opportunityService.deleteOpportunity(opportunityId))
        .isInstanceOf(OpportunityNotFoundException.class)
        .hasMessageContaining(opportunityId.toString());
  }

  @Test
  @DisplayName("Should calculate forecast correctly")
  void calculateForecast_shouldReturnCorrectValue() {
    // Arrange
    when(opportunityRepository.calculateForecast()).thenReturn(BigDecimal.valueOf(25000));

    // Act
    var result = opportunityService.calculateForecast();

    // Assert
    assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(25000));
  }

  @Test
  @DisplayName("Should change opportunity stage")
  void changeStage_withValidTransition_shouldUpdateStage() {
    // Arrange
    Opportunity existingOpp =
        OpportunityTestDataFactory.builder()
            .withName("Test Opportunity")
            .forCustomer(testCustomer)
            .inStage(OpportunityStage.NEW_LEAD)
            .withProbability(10)
            .build();
    existingOpp.setId(opportunityId);

    when(opportunityRepository.findByIdOptional(opportunityId))
        .thenReturn(Optional.of(existingOpp));

    // Act
    var result = opportunityService.changeStage(opportunityId, OpportunityStage.QUALIFICATION);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getStage()).isEqualTo(OpportunityStage.QUALIFICATION);
    assertThat(result.getProbability()).isEqualTo(25); // Default for QUALIFICATION

    // Verify activity was tracked
    assertThat(existingOpp.getActivities()).hasSize(1);
    assertThat(existingOpp.getActivities().get(0).getActivityType())
        .isEqualTo(OpportunityActivity.ActivityType.STAGE_CHANGED);
  }
}

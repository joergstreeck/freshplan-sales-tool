package de.freshplan.domain.cockpit.service.query;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.freshplan.domain.cockpit.service.dto.*;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.user.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.domain.user.service.exception.UserNotFoundException;
import io.quarkus.panache.common.Page;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

/**
 * Unit tests for SalesCockpitQueryService.
 * 
 * Verifies that all query operations work correctly and that
 * NO write operations occur (pure read-only service).
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class SalesCockpitQueryServiceTest {

  @Mock
  private CustomerRepository customerRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private SalesCockpitQueryService queryService;

  private UUID testUserId;
  private UUID testUserId2;
  private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
  private User testUser;
  private List<Customer> mockCustomers;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    testUserId2 = UUID.randomUUID();
    
    // Setup test user - don't stub in setUp to avoid unnecessary stubbing
    testUser = mock(User.class);
    
    // Setup mock customers
    mockCustomers = createMockCustomers();
  }

  @Test
  void testGetDashboardData_withValidUser_shouldReturnDashboard() {
    // Given
    when(userRepository.findById(testUserId)).thenReturn(testUser);
    setupMockRepositoryResponses();
    
    // When
    SalesCockpitDashboard result = queryService.getDashboardData(testUserId);
    
    // Then
    assertNotNull(result);
    assertNotNull(result.getTodaysTasks());
    assertNotNull(result.getRiskCustomers());
    assertNotNull(result.getStatistics());
    assertNotNull(result.getAlerts());
    
    // Verify user validation occurred
    verify(userRepository).findById(testUserId);
    
    // Verify NO write operations
    verifyNoWriteOperations();
  }

  @Test
  void testGetDashboardData_withTestUserId_shouldSkipUserValidation() {
    // Given
    setupMockRepositoryResponses();
    
    // When
    SalesCockpitDashboard result = queryService.getDashboardData(TEST_USER_ID);
    
    // Then
    assertNotNull(result);
    
    // Verify user validation was skipped for TEST_USER_ID
    verify(userRepository, never()).findById(TEST_USER_ID);
    
    // Verify NO write operations
    verifyNoWriteOperations();
  }

  @Test
  void testGetDashboardData_withNullUserId_shouldThrowException() {
    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> queryService.getDashboardData(null)
    );
    
    assertEquals("User ID must not be null", exception.getMessage());
    
    // Verify no repository calls were made
    verifyNoInteractions(customerRepository);
    verifyNoInteractions(userRepository);
  }

  @Test
  void testGetDashboardData_withNonExistentUser_shouldThrowException() {
    // Given
    UUID unknownUserId = UUID.randomUUID();
    when(userRepository.findById(unknownUserId)).thenReturn(null);
    
    // When & Then
    UserNotFoundException exception = assertThrows(
        UserNotFoundException.class,
        () -> queryService.getDashboardData(unknownUserId)
    );
    
    assertTrue(exception.getMessage().contains("User not found"));
    assertTrue(exception.getMessage().contains(unknownUserId.toString()));
  }

  @Test
  void testGetDevDashboardData_shouldReturnConsistentData() {
    // Given
    setupMockRepositoryResponses();
    
    // When
    SalesCockpitDashboard result = queryService.getDevDashboardData();
    
    // Then
    assertNotNull(result);
    assertNotNull(result.getStatistics());
    
    // Verify consistent test data
    assertNotNull(result.getTodaysTasks());
    assertEquals(3, result.getTodaysTasks().size(), "Should have exactly 3 tasks for tests");
    
    assertNotNull(result.getRiskCustomers());
    assertEquals(2, result.getRiskCustomers().size(), "Should have exactly 2 risk customers for tests");
    
    assertNotNull(result.getAlerts());
    assertEquals(1, result.getAlerts().size(), "Should have exactly 1 alert for tests");
    
    // Verify NO write operations
    verifyNoWriteOperations();
  }

  @Test
  void testTodaysTasks_shouldIncludeOverdueFollowUps() {
    // Given
    Customer overdueCustomer = createCustomerWithOverdueFollowUp();
    when(userRepository.findById(testUserId)).thenReturn(testUser);
    when(customerRepository.findOverdueFollowUps(any(Page.class)))
        .thenReturn(List.of(overdueCustomer));
    when(customerRepository.findActiveCustomersWithoutRecentContact(any(LocalDateTime.class)))
        .thenReturn(new ArrayList<>());
    when(customerRepository.findRecentlyCreated(anyInt(), any(Page.class)))
        .thenReturn(new ArrayList<>());
    setupStatisticsResponses();
    setupFindMocks(); // Add find mocks for alerts generation
    
    // When
    SalesCockpitDashboard result = queryService.getDashboardData(testUserId);
    
    // Then
    List<DashboardTask> tasks = result.getTodaysTasks();
    assertFalse(tasks.isEmpty());
    
    DashboardTask overdueTask = tasks.stream()
        .filter(t -> t.getTitle().startsWith("ÜBERFÄLLIG"))
        .findFirst()
        .orElse(null);
    
    assertNotNull(overdueTask);
    assertEquals(DashboardTask.TaskPriority.HIGH, overdueTask.getPriority());
    assertEquals(DashboardTask.TaskType.CALL, overdueTask.getType());
    
    // Verify NO write operations
    verifyNoWriteOperations();
  }

  @Test
  void testRiskCustomers_shouldCalculateRiskLevels() {
    // Given
    Customer highRiskCustomer = createCustomerWithDaysSinceContact(125);
    Customer mediumRiskCustomer = createCustomerWithDaysSinceContact(95);
    Customer lowRiskCustomer = createCustomerWithDaysSinceContact(65);
    
    when(userRepository.findById(testUserId)).thenReturn(testUser);
    when(customerRepository.findActiveCustomersWithoutRecentContact(any(LocalDateTime.class)))
        .thenReturn(List.of(highRiskCustomer, mediumRiskCustomer, lowRiskCustomer));
    
    // Setup other required mocks
    when(customerRepository.findOverdueFollowUps(any(Page.class)))
        .thenReturn(new ArrayList<>());
    when(customerRepository.findRecentlyCreated(anyInt(), any(Page.class)))
        .thenReturn(new ArrayList<>());
    setupStatisticsResponses();
    setupFindMocks();
    
    // When
    SalesCockpitDashboard result = queryService.getDashboardData(testUserId);
    
    // Then
    List<RiskCustomer> riskCustomers = result.getRiskCustomers();
    assertEquals(3, riskCustomers.size());
    
    // Verify risk levels are correctly assigned
    RiskCustomer high = riskCustomers.stream()
        .filter(rc -> rc.getDaysSinceLastContact() > 120)
        .findFirst()
        .orElse(null);
    assertNotNull(high);
    assertEquals(RiskCustomer.RiskLevel.HIGH, high.getRiskLevel());
    
    RiskCustomer medium = riskCustomers.stream()
        .filter(rc -> rc.getDaysSinceLastContact() > 90 && rc.getDaysSinceLastContact() <= 120)
        .findFirst()
        .orElse(null);
    assertNotNull(medium);
    assertEquals(RiskCustomer.RiskLevel.MEDIUM, medium.getRiskLevel());
    
    RiskCustomer low = riskCustomers.stream()
        .filter(rc -> rc.getDaysSinceLastContact() > 60 && rc.getDaysSinceLastContact() <= 90)
        .findFirst()
        .orElse(null);
    assertNotNull(low);
    assertEquals(RiskCustomer.RiskLevel.LOW, low.getRiskLevel());
    
    // Verify NO write operations
    verifyNoWriteOperations();
  }

  @Test
  void testStatistics_shouldAggregateCorrectly() {
    // Given
    when(userRepository.findById(testUserId)).thenReturn(testUser);
    when(customerRepository.count()).thenReturn(150L);
    when(customerRepository.countByStatus(CustomerStatus.AKTIV)).thenReturn(140L);
    when(customerRepository.countActiveCustomersWithoutRecentContact(any(LocalDateTime.class)))
        .thenReturn(10L);
    when(customerRepository.countOverdueFollowUps()).thenReturn(5L);
    setupTaskAndRiskResponses();
    setupFindMocks(); // Add find mocks for alerts generation
    
    // When
    SalesCockpitDashboard result = queryService.getDashboardData(testUserId);
    
    // Then
    DashboardStatistics stats = result.getStatistics();
    assertNotNull(stats);
    assertEquals(150, stats.getTotalCustomers());
    assertEquals(140, stats.getActiveCustomers());
    assertEquals(10, stats.getCustomersAtRisk());
    assertEquals(5, stats.getOverdueItems());
    
    // Verify NO write operations
    verifyNoWriteOperations();
  }

  @Test
  void testAlerts_shouldGenerateOpportunityAlerts() {
    // Given
    Customer customerWithoutRecentContact = createCustomerWithDaysSinceContact(35);
    when(userRepository.findById(testUserId)).thenReturn(testUser);
    
    // Mock the find query specifically for alerts
    PanacheQuery<Customer> mockQuery = mock(PanacheQuery.class);
    when(mockQuery.list()).thenReturn(List.of(customerWithoutRecentContact));
    when(customerRepository.find(anyString(), any(CustomerStatus.class), any(LocalDateTime.class)))
        .thenReturn(mockQuery);
    
    // Setup other required mocks
    setupTaskAndRiskResponses();
    setupStatisticsResponses();
    
    // When
    SalesCockpitDashboard result = queryService.getDashboardData(testUserId);
    
    // Then
    List<DashboardAlert> alerts = result.getAlerts();
    assertNotNull(alerts);
    assertFalse(alerts.isEmpty());
    
    DashboardAlert alert = alerts.get(0);
    assertEquals(DashboardAlert.AlertType.OPPORTUNITY, alert.getType());
    assertEquals(DashboardAlert.AlertSeverity.INFO, alert.getSeverity());
    assertTrue(alert.getTitle().contains("Umsatzchance"));
    assertTrue(alert.getMessage().contains("Cross-Selling"));
    
    // Verify NO write operations
    verifyNoWriteOperations();
  }

  @Test
  void testNoWriteOperations_inAnyMethod() {
    // Given
    setupMockRepositoryResponses();
    
    // When - Execute all public methods
    queryService.getDashboardData(TEST_USER_ID);
    queryService.getDevDashboardData();
    
    // Then - Verify absolutely NO write operations occurred
    verify(customerRepository, never()).persist((Customer) any());
    verify(customerRepository, never()).persistAndFlush(any());
    verify(customerRepository, never()).delete(any());
    verify(customerRepository, never()).deleteById(any());
    verify(customerRepository, never()).flush();
    
    verify(userRepository, never()).persist((User) any());
    verify(userRepository, never()).persistAndFlush(any());
    verify(userRepository, never()).delete(any());
    verify(userRepository, never()).deleteById(any());
    verify(userRepository, never()).flush();
    
    // Verify only read operations occurred
    verify(customerRepository, atLeastOnce()).count();
    verify(customerRepository, atLeastOnce()).countByStatus(any());
  }

  // ==================== Helper Methods ====================

  private void setupMockRepositoryResponses() {
    setupTaskAndRiskResponses();
    setupStatisticsResponses();
    setupFindMocks();
  }
  
  private void setupFindMocks() {
    // Create a mock query that returns empty list for alerts generation
    PanacheQuery<Customer> mockQuery = mock(PanacheQuery.class);
    when(mockQuery.list()).thenReturn(new ArrayList<>());
    
    // Mock all variations of find() method
    when(customerRepository.find(anyString(), any(CustomerStatus.class), any(LocalDateTime.class)))
        .thenReturn(mockQuery);
    when(customerRepository.find(anyString(), any(Object[].class)))
        .thenReturn(mockQuery);
    
    // Use lenient for edge cases
    Mockito.lenient().when(customerRepository.find(anyString(), any(Object.class), any(Object.class)))
        .thenReturn(mockQuery);
  }

  private void setupTaskAndRiskResponses() {
    when(customerRepository.findOverdueFollowUps(any(Page.class)))
        .thenReturn(new ArrayList<>());
    when(customerRepository.findActiveCustomersWithoutRecentContact(any(LocalDateTime.class)))
        .thenReturn(new ArrayList<>());
    when(customerRepository.findRecentlyCreated(anyInt(), any(Page.class)))
        .thenReturn(new ArrayList<>());
  }

  private void setupStatisticsResponses() {
    when(customerRepository.count()).thenReturn(100L);
    when(customerRepository.countByStatus(CustomerStatus.AKTIV)).thenReturn(90L);
    when(customerRepository.countActiveCustomersWithoutRecentContact(any(LocalDateTime.class)))
        .thenReturn(5L);
    when(customerRepository.countOverdueFollowUps()).thenReturn(3L);
  }


  private void verifyNoWriteOperations() {
    verify(customerRepository, never()).persist((Customer) any());
    verify(customerRepository, never()).persistAndFlush(any());
    verify(customerRepository, never()).delete(any());
    verify(userRepository, never()).persist((User) any());
    verify(userRepository, never()).delete(any());
  }

  private List<Customer> createMockCustomers() {
    List<Customer> customers = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      Customer customer = new Customer();
      customer.setId(UUID.randomUUID());
      customer.setCustomerNumber("KD-2025-000" + (i + 1));
      customer.setCompanyName("Test Company " + (i + 1));
      customer.setStatus(CustomerStatus.AKTIV);
      customer.setCreatedAt(LocalDateTime.now().minusDays(30));
      customer.setLastContactDate(LocalDateTime.now().minusDays(i * 30));
      customers.add(customer);
    }
    return customers;
  }

  private Customer createCustomerWithOverdueFollowUp() {
    Customer customer = new Customer();
    customer.setId(UUID.randomUUID());
    customer.setCustomerNumber("KD-2025-0001");
    customer.setCompanyName("Overdue Customer GmbH");
    customer.setStatus(CustomerStatus.AKTIV);
    customer.setNextFollowUpDate(LocalDateTime.now().minusDays(5));
    customer.setCreatedAt(LocalDateTime.now().minusDays(60));
    return customer;
  }

  private Customer createCustomerWithDaysSinceContact(int days) {
    Customer customer = new Customer();
    customer.setId(UUID.randomUUID());
    customer.setCustomerNumber("KD-2025-00" + days);
    customer.setCompanyName("Risk Customer " + days + " GmbH");
    customer.setStatus(CustomerStatus.AKTIV);
    customer.setLastContactDate(LocalDateTime.now().minusDays(days));
    customer.setCreatedAt(LocalDateTime.now().minusDays(days + 30));
    return customer;
  }

}
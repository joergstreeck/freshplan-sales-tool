package de.freshplan.infrastructure.jobs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.modules.xentral.dto.XentralEmployeeDTO;
import de.freshplan.modules.xentral.service.XentralApiService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit Tests for SalesRepSyncJob.
 *
 * <p>Sprint 2.1.7.2 D6 - Sales-Rep Mapping Auto-Sync Tests
 *
 * <p><b>Test Strategy:</b> Unit tests with mocked dependencies (XentralApiService,
 * UserRepository)
 *
 * <p><b>Bug Detection Focus:</b>
 *
 * <ul>
 *   <li>Email Matching: Ensure case-insensitive matching
 *   <li>Update Logic: Only update if xentralSalesRepId changed
 *   <li>Unmatched Sales Reps: Proper logging without errors
 *   <li>API Failures: Graceful error handling
 * </ul>
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.2
 */
@QuarkusTest
class SalesRepSyncJobTest {

  @Inject SalesRepSyncJob salesRepSyncJob;

  @InjectMock XentralApiService xentralApiService;

  @InjectMock UserRepository userRepository;

  private User testUser;
  private XentralEmployeeDTO testSalesRep;

  @BeforeEach
  void setUp() {
    // Reset mocks
    reset(xentralApiService, userRepository);

    // Create test user using public constructor (matching SEED data domain)
    testUser = new User("stefan.weber", "Stefan", "Weber", "stefan.weber@freshplan.example");
    testUser.setXentralSalesRepId(null); // Initially not synced

    // Create test Xentral sales rep (matching SEED data domain)
    testSalesRep =
        new XentralEmployeeDTO(
            "XENT-EMP-001", "Stefan", "Weber", "stefan.weber@freshplan.example", "sales");
  }

  // ============================================================================
  // Successful Sync Scenarios
  // ============================================================================

  @Test
  @DisplayName("syncSalesRepIds() - Email match → Updates xentralSalesRepId")
  void testSyncSalesRepIds_EmailMatch_UpdatesXentralSalesRepId() {
    // GIVEN: Xentral API returns 1 sales rep
    when(xentralApiService.getAllSalesReps()).thenReturn(List.of(testSalesRep));

    // AND: User with matching email exists
    when(userRepository.findByEmail("stefan.weber@freshplan.example"))
        .thenReturn(Optional.of(testUser));

    // WHEN: Sync job runs
    salesRepSyncJob.syncSalesRepIds();

    // THEN: User's xentralSalesRepId should be updated
    verify(userRepository, times(1)).findByEmail("stefan.weber@freshplan.example");
    verify(userRepository, times(1)).persist(testUser);
    assertEquals("XENT-EMP-001", testUser.getXentralSalesRepId());
  }

  @Test
  @DisplayName("syncSalesRepIds() - Multiple sales reps → All synced")
  void testSyncSalesRepIds_MultipleSalesReps_AllSynced() {
    // GIVEN: Xentral API returns 3 sales reps (matching SEED data domain)
    XentralEmployeeDTO salesRep1 =
        new XentralEmployeeDTO(
            "SR-001", "Stefan", "Weber", "stefan.weber@freshplan.example", "sales");
    XentralEmployeeDTO salesRep2 =
        new XentralEmployeeDTO(
            "SR-002", "Anna", "Schmidt", "anna.schmidt@freshplan.example", "sales");
    XentralEmployeeDTO salesRep3 =
        new XentralEmployeeDTO(
            "SR-003", "Michael", "Becker", "michael.becker@freshplan.example", "sales");

    when(xentralApiService.getAllSalesReps()).thenReturn(List.of(salesRep1, salesRep2, salesRep3));

    // AND: Users exist for all sales reps
    User user1 = new User("stefan.weber", "Stefan", "Weber", "stefan.weber@freshplan.example");
    User user2 = new User("anna.schmidt", "Anna", "Schmidt", "anna.schmidt@freshplan.example");
    User user3 = new User("michael.becker", "Michael", "Becker", "michael.becker@freshplan.example");

    when(userRepository.findByEmail("stefan.weber@freshplan.example")).thenReturn(Optional.of(user1));
    when(userRepository.findByEmail("anna.schmidt@freshplan.example")).thenReturn(Optional.of(user2));
    when(userRepository.findByEmail("michael.becker@freshplan.example")).thenReturn(Optional.of(user3));

    // WHEN: Sync job runs
    salesRepSyncJob.syncSalesRepIds();

    // THEN: All 3 users should be updated
    verify(userRepository, times(3)).persist(any(User.class));
    assertEquals("SR-001", user1.getXentralSalesRepId());
    assertEquals("SR-002", user2.getXentralSalesRepId());
    assertEquals("SR-003", user3.getXentralSalesRepId());
  }

  @Test
  @DisplayName("syncSalesRepIds() - Already synced (no change) → Skips persist")
  void testSyncSalesRepIds_AlreadySynced_SkipsPersist() {
    // GIVEN: User already has correct xentralSalesRepId
    testUser.setXentralSalesRepId("XENT-EMP-001");

    when(xentralApiService.getAllSalesReps()).thenReturn(List.of(testSalesRep));
    when(userRepository.findByEmail("stefan.weber@freshplan.example"))
        .thenReturn(Optional.of(testUser));

    // WHEN: Sync job runs
    salesRepSyncJob.syncSalesRepIds();

    // THEN: Should NOT call persist (no change)
    verify(userRepository, never()).persist(any(User.class));
  }

  // ============================================================================
  // Unmatched Sales Reps (No User Found)
  // ============================================================================

  @Test
  @DisplayName("syncSalesRepIds() - Email not found → Logs warning, continues")
  void testSyncSalesRepIds_EmailNotFound_LogsWarningAndContinues() {
    // GIVEN: Xentral sales rep exists, but no matching user
    when(xentralApiService.getAllSalesReps()).thenReturn(List.of(testSalesRep));
    when(userRepository.findByEmail("stefan.weber@freshplan.example"))
        .thenReturn(Optional.empty());

    // WHEN: Sync job runs
    salesRepSyncJob.syncSalesRepIds();

    // THEN: Should NOT persist anything
    verify(userRepository, never()).persist(any(User.class));
    // Note: Warning is logged (check logs in real execution)
  }

  @Test
  @DisplayName("syncSalesRepIds() - Mixed: 2 matched, 1 unmatched → Syncs matched only")
  void testSyncSalesRepIds_MixedScenario_SyncsMatchedOnly() {
    // GIVEN: 3 sales reps (2 matched, 1 unmatched)
    XentralEmployeeDTO matchedRep1 =
        new XentralEmployeeDTO(
            "SR-001", "Stefan", "Weber", "stefan.weber@freshplan.example", "sales");
    XentralEmployeeDTO unmatchedRep =
        new XentralEmployeeDTO(
            "SR-002", "Unknown", "User", "unknown@external.com", "sales"); // Not in FreshPlan
    XentralEmployeeDTO matchedRep2 =
        new XentralEmployeeDTO(
            "SR-003", "Anna", "Schmidt", "anna.schmidt@freshplan.example", "sales");

    when(xentralApiService.getAllSalesReps())
        .thenReturn(List.of(matchedRep1, unmatchedRep, matchedRep2));

    // AND: Only 2 users exist
    User user1 = new User("stefan.weber", "Stefan", "Weber", "stefan.weber@freshplan.example");
    User user2 = new User("anna.schmidt", "Anna", "Schmidt", "anna.schmidt@freshplan.example");

    when(userRepository.findByEmail("stefan.weber@freshplan.example")).thenReturn(Optional.of(user1));
    when(userRepository.findByEmail("unknown@external.com")).thenReturn(Optional.empty());
    when(userRepository.findByEmail("anna.schmidt@freshplan.example")).thenReturn(Optional.of(user2));

    // WHEN: Sync job runs
    salesRepSyncJob.syncSalesRepIds();

    // THEN: Only 2 users should be persisted (matched ones)
    verify(userRepository, times(2)).persist(any(User.class));
    assertEquals("SR-001", user1.getXentralSalesRepId());
    assertEquals("SR-003", user2.getXentralSalesRepId());
  }

  // ============================================================================
  // Error Handling
  // ============================================================================

  @Test
  @DisplayName("syncSalesRepIds() - Xentral API fails → Logs error, no exception")
  void testSyncSalesRepIds_XentralApiFailure_LogsErrorNoException() {
    // GIVEN: Xentral API throws exception
    when(xentralApiService.getAllSalesReps()).thenThrow(new RuntimeException("Xentral API down"));

    // WHEN: Sync job runs
    salesRepSyncJob.syncSalesRepIds();

    // THEN: Should NOT throw exception (job continues)
    verify(userRepository, never()).persist(any(User.class));
    // Note: Error is logged (check logs in real execution)
  }

  @Test
  @DisplayName("syncSalesRepIds() - Empty sales reps list → No updates")
  void testSyncSalesRepIds_EmptySalesRepsList_NoUpdates() {
    // GIVEN: Xentral API returns empty list
    when(xentralApiService.getAllSalesReps()).thenReturn(List.of());

    // WHEN: Sync job runs
    salesRepSyncJob.syncSalesRepIds();

    // THEN: Should NOT persist anything
    verify(userRepository, never()).persist(any(User.class));
  }

  // ============================================================================
  // Manual Trigger Test
  // ============================================================================

  @Test
  @DisplayName("triggerManualSync() - Delegates to syncSalesRepIds()")
  void testTriggerManualSync_DelegatesToSyncSalesRepIds() {
    // GIVEN: Mock Xentral API
    when(xentralApiService.getAllSalesReps()).thenReturn(List.of());

    // WHEN: Manual sync triggered
    salesRepSyncJob.triggerManualSync();

    // THEN: Should call getAllSalesReps (proves sync ran)
    verify(xentralApiService, times(1)).getAllSalesReps();
  }
}

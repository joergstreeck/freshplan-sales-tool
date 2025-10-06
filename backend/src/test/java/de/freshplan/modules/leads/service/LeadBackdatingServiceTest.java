package de.freshplan.modules.leads.service;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.modules.leads.api.admin.dto.BackdatingRequest;
import de.freshplan.modules.leads.api.admin.dto.BackdatingResponse;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.Territory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests für Backdating Service.
 *
 * <p>Sprint 2.1.6 - User Story 4
 */
@QuarkusTest
class LeadBackdatingServiceTest {

  @Inject LeadBackdatingService backdatingService;

  private static final String TEST_USER = "admin-test";
  private Lead testLead;

  @BeforeEach
  @Transactional
  void setup() {
    // Clean test data
    Lead.deleteAll();

    // Ensure territory exists
    Territory territory = Territory.findByCode("DE");
    if (territory == null) {
      territory = Territory.getDefault();
      if (territory.id == null) {
        territory.persist();
      }
    }

    // Create test lead
    testLead = new Lead();
    testLead.companyName = "Test Restaurant";
    testLead.city = "München";
    testLead.territory = territory;
    testLead.ownerUserId = TEST_USER;
    testLead.createdBy = TEST_USER;
    testLead.updatedBy = TEST_USER;
    testLead.registeredAt = LocalDateTime.now().minusMonths(3);
    testLead.protectionStartAt = testLead.registeredAt;
    testLead.progressDeadline = testLead.registeredAt.plusDays(60);
    testLead.persist();
  }

  @Test
  @Transactional
  void shouldUpdateRegisteredAtSuccessfully() {
    // Given
    LocalDateTime newDate = LocalDateTime.now().minusMonths(6);
    BackdatingRequest request = new BackdatingRequest();
    request.registeredAt = newDate;
    request.reason = "Bestandsdaten-Migration - Import Altbestand";

    LocalDateTime oldRegisteredAt = testLead.registeredAt;

    // When
    BackdatingResponse response =
        backdatingService.updateRegisteredAt(testLead.id, request, TEST_USER);

    // Then
    assertNotNull(response);
    assertEquals(testLead.id, response.leadId);
    assertEquals(oldRegisteredAt, response.oldRegisteredAt);
    assertEquals(newDate, response.newRegisteredAt);

    // Verify lead was updated
    Lead updatedLead = Lead.findById(testLead.id);
    assertEquals(newDate, updatedLead.registeredAt);
    assertEquals(
        "Bestandsdaten-Migration - Import Altbestand", updatedLead.registeredAtOverrideReason);
    assertEquals(TEST_USER, updatedLead.registeredAtSetBy);
    assertNotNull(updatedLead.registeredAtSetAt);
    assertEquals("backdated", updatedLead.registeredAtSource);
  }

  @Test
  @Transactional
  void shouldRecalculateProtectionDeadlines() {
    // Given
    LocalDateTime newDate = LocalDateTime.now().minusMonths(8);
    BackdatingRequest request = new BackdatingRequest();
    request.registeredAt = newDate;
    request.reason = "Historical data correction";

    // When
    BackdatingResponse response =
        backdatingService.updateRegisteredAt(testLead.id, request, TEST_USER);

    // Then
    Lead updatedLead = Lead.findById(testLead.id);

    // Protection: newDate + 6 months (calculated from protectionStartAt + protectionMonths)
    LocalDateTime expectedProtectionEnd = newDate.plusMonths(updatedLead.protectionMonths);
    assertEquals(expectedProtectionEnd, response.newProtectionUntil);

    // Progress Deadline: newDate + 60 days
    assertEquals(newDate.plusDays(updatedLead.protectionDays60), updatedLead.progressDeadline);
    assertEquals(newDate.plusDays(updatedLead.protectionDays60), response.newProgressDeadline);

    // Protection Start
    assertEquals(newDate, updatedLead.protectionStartAt);
  }

  @Test
  @Transactional
  void shouldRejectFutureDate() {
    // Given
    BackdatingRequest request = new BackdatingRequest();
    request.registeredAt = LocalDateTime.now().plusDays(1);
    request.reason = "Invalid future date";

    // When/Then
    assertThrows(
        IllegalArgumentException.class,
        () -> backdatingService.updateRegisteredAt(testLead.id, request, TEST_USER));
  }

  @Test
  @Transactional
  void shouldThrowNotFoundForInvalidLead() {
    // Given
    BackdatingRequest request = new BackdatingRequest();
    request.registeredAt = LocalDateTime.now().minusMonths(1);
    request.reason = "Test";

    // When/Then
    assertThrows(
        NotFoundException.class,
        () -> backdatingService.updateRegisteredAt(99999L, request, TEST_USER));
  }

  @Test
  @Transactional
  void shouldHandleStopTheClockCorrectly() {
    // Given: Lead with stopped clock
    testLead.clockStoppedAt = LocalDateTime.now().minusDays(10);
    Lead.getEntityManager().merge(testLead);

    LocalDateTime newDate = LocalDateTime.now().minusMonths(4);
    BackdatingRequest request = new BackdatingRequest();
    request.registeredAt = newDate;
    request.reason = "Backdating with paused clock";

    // When
    BackdatingResponse response =
        backdatingService.updateRegisteredAt(testLead.id, request, TEST_USER);

    // Then
    Lead updatedLead = Lead.findById(testLead.id);

    // Progress deadline should be calculated (baseDeadline + cumulative pause)
    // NOTE: progressPauseTotalSeconds is 0 until Resume is called
    // So progressDeadline = baseDeadline + 0 = baseDeadline
    LocalDateTime baseDeadline = newDate.plusDays(60);
    assertEquals(baseDeadline, updatedLead.progressDeadline);
    assertNotNull(response.newProgressDeadline);
  }

  @Test
  @Transactional
  void shouldSetLastActivityAtIfNull() {
    // Given: Lead without lastActivityAt
    testLead.lastActivityAt = null;
    Lead.getEntityManager().merge(testLead);

    LocalDateTime newDate = LocalDateTime.now().minusMonths(5);
    BackdatingRequest request = new BackdatingRequest();
    request.registeredAt = newDate;
    request.reason = "Set activity date";

    // When
    backdatingService.updateRegisteredAt(testLead.id, request, TEST_USER);

    // Then
    Lead updatedLead = Lead.findById(testLead.id);
    assertEquals(newDate, updatedLead.lastActivityAt);
  }

  @Test
  @Transactional
  void shouldNotOverwriteExistingLastActivityAt() {
    // Given: Lead with existing lastActivityAt
    LocalDateTime existingActivityDate = LocalDateTime.now().minusDays(5);
    testLead.lastActivityAt = existingActivityDate;
    Lead.getEntityManager().merge(testLead);

    LocalDateTime newDate = LocalDateTime.now().minusMonths(6);
    BackdatingRequest request = new BackdatingRequest();
    request.registeredAt = newDate;
    request.reason = "Don't overwrite activity";

    // When
    backdatingService.updateRegisteredAt(testLead.id, request, TEST_USER);

    // Then
    Lead updatedLead = Lead.findById(testLead.id);
    assertEquals(existingActivityDate, updatedLead.lastActivityAt); // Unchanged
  }
}

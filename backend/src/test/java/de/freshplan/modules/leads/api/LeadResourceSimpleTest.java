package de.freshplan.modules.leads.api;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.service.LeadProtectionService;
import de.freshplan.modules.leads.service.LeadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Simple unit tests for Lead API components. */
class LeadResourceSimpleTest {

  @Test
  @DisplayName("Lead status enum should have all required states")
  void testLeadStatusEnum() {
    // Verify all required statuses exist
    assertNotNull(LeadStatus.REGISTERED);
    assertNotNull(LeadStatus.ACTIVE);
    assertNotNull(LeadStatus.REMINDER);
    assertNotNull(LeadStatus.GRACE_PERIOD);
    assertNotNull(LeadStatus.QUALIFIED);
    assertNotNull(LeadStatus.CONVERTED);
    assertNotNull(LeadStatus.LOST);
    assertNotNull(LeadStatus.EXPIRED);
    assertNotNull(LeadStatus.DELETED);
  }

  @Test
  @DisplayName("Lead request DTOs should be instantiable")
  void testRequestDTOs() {
    // Test creation request
    LeadCreateRequest createRequest = new LeadCreateRequest();
    createRequest.companyName = "Test Company";
    createRequest.countryCode = "DE";
    assertEquals("Test Company", createRequest.companyName);
    assertEquals("DE", createRequest.countryCode);

    // Test update request
    LeadUpdateRequest updateRequest = new LeadUpdateRequest();
    updateRequest.status = LeadStatus.ACTIVE;
    updateRequest.stopClock = true;
    updateRequest.stopReason = "Test reason";
    assertEquals(LeadStatus.ACTIVE, updateRequest.status);
    assertTrue(updateRequest.stopClock);
    assertEquals("Test reason", updateRequest.stopReason);

    // Test activity request
    ActivityRequest activityRequest = new ActivityRequest();
    activityRequest.activityType = "CALL";
    activityRequest.description = "Test call";
    assertEquals("CALL", activityRequest.activityType);
    assertEquals("Test call", activityRequest.description);
  }

  @Test
  @DisplayName("Lead statistics DTO should be instantiable")
  void testLeadStatistics() {
    LeadService.LeadStatistics stats = new LeadService.LeadStatistics();
    stats.totalLeads = 100;
    stats.activeLeads = 50;
    stats.reminderLeads = 20;
    stats.gracePeriodLeads = 10;
    stats.expiredLeads = 5;
    stats.clockStoppedLeads = 3;
    stats.expiringSoonLeads = 7;

    assertEquals(100, stats.totalLeads);
    assertEquals(50, stats.activeLeads);
    assertEquals(20, stats.reminderLeads);
    assertEquals(10, stats.gracePeriodLeads);
    assertEquals(5, stats.expiredLeads);
    assertEquals(3, stats.clockStoppedLeads);
    assertEquals(7, stats.expiringSoonLeads);
  }

  @Test
  @DisplayName("Protection status DTO should be instantiable")
  void testProtectionStatus() {
    LeadProtectionService.ProtectionStatus status = new LeadProtectionService.ProtectionStatus();
    status.leadId = 123L;
    status.currentStatus = LeadStatus.ACTIVE;
    status.isProtected = true;
    status.clockStopped = false;
    status.remainingDays = 180;
    status.daysUntilTransition = 60;

    assertEquals(123L, status.leadId);
    assertEquals(LeadStatus.ACTIVE, status.currentStatus);
    assertTrue(status.isProtected);
    assertFalse(status.clockStopped);
    assertEquals(180, status.remainingDays);
    assertEquals(60, status.daysUntilTransition);
  }
}

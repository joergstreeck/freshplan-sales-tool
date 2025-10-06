package de.freshplan.modules.leads.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.events.ImportJobsArchivedEvent;
import de.freshplan.modules.leads.events.LeadProgressWarningIssuedEvent;
import de.freshplan.modules.leads.events.LeadProtectionExpiredEvent;
import de.freshplan.modules.leads.events.LeadsPseudonymizedEvent;
import jakarta.enterprise.event.Event;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit Tests für LeadMaintenanceService (Sprint 2.1.6 Phase 3).
 *
 * <p>Tests für Nightly Jobs: Progress Warning, Protection Expiry, Pseudonymization, Import
 * Archival.
 *
 * <p>Strategy: Pure Mockito, NO @QuarkusTest, NO DB. Fast (~1s), isolated, reliable (siehe
 * ADR-005: Hybrid Test Strategy).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LeadMaintenanceService Unit Tests - Sprint 2.1.6 Phase 3")
@Tag("unit")
class LeadMaintenanceServiceTest {

  @Mock private EntityManager em;

  @Mock private Event<LeadProgressWarningIssuedEvent> progressWarningEvent;

  @Mock private Event<LeadProtectionExpiredEvent> protectionExpiredEvent;

  @Mock private Event<LeadsPseudonymizedEvent> pseudonymizedEvent;

  @Mock private Event<ImportJobsArchivedEvent> importArchivedEvent;

  @Mock private TypedQuery<Lead> query;

  @InjectMocks private LeadMaintenanceService service;

  private Clock fixedClock;
  private LocalDateTime now;

  @BeforeEach
  void setUp() {
    // Fixed Clock für testbare Zeit-Logik (2025-10-15 12:00:00)
    Instant fixedInstant = Instant.parse("2025-10-15T12:00:00Z");
    fixedClock = Clock.fixed(fixedInstant, ZoneId.systemDefault());
    service.setClock(fixedClock);
    now = LocalDateTime.now(fixedClock);
  }

  // ========== Job 1: Progress Warning Check ==========

  @Nested
  @DisplayName("Job 1: Progress Warning Check (60-Day Activity Rule)")
  class ProgressWarningCheckTests {

    @Test
    @DisplayName("Should issue warning for leads with deadline < NOW() + 7 days")
    void shouldIssueWarningForLeadsNearingDeadline() {
      // Arrange: Lead mit Deadline in 6 Tagen (< 7 Tage Schwelle)
      Lead lead = createTestLead();
      lead.progressDeadline = now.plusDays(6);
      lead.progressWarningSentAt = null;
      lead.status = LeadStatus.ACTIVE;

      when(em.createQuery(anyString(), eq(Lead.class))).thenReturn(query);
      when(query.setParameter(anyString(), any())).thenReturn(query);
      when(query.getResultList()).thenReturn(List.of(lead));

      // Mock UPDATE Query für atomares Flag-Setzen
      jakarta.persistence.Query updateQuery = mock(jakarta.persistence.Query.class);
      when(em.createQuery(contains("UPDATE Lead"))).thenReturn(updateQuery);
      when(updateQuery.setParameter(anyString(), any())).thenReturn(updateQuery);
      when(updateQuery.executeUpdate()).thenReturn(1); // 1 row updated

      // Act
      int warned = service.checkProgressWarnings();

      // Assert
      assertThat(warned).isEqualTo(1);
      verify(progressWarningEvent).fire(any(LeadProgressWarningIssuedEvent.class));
    }

    @Test
    @DisplayName("Should skip leads already warned (idempotent)")
    void shouldSkipLeadsAlreadyWarned() {
      // Arrange: Lead mit bereits gesetztem progressWarningSentAt
      Lead lead = createTestLead();
      lead.progressDeadline = now.plusDays(6);
      lead.progressWarningSentAt = now.minusDays(1);

      when(em.createQuery(anyString(), eq(Lead.class))).thenReturn(query);
      when(query.setParameter(anyString(), any())).thenReturn(query);
      when(query.getResultList()).thenReturn(List.of(lead));

      // Mock UPDATE Query - returns 0 (bereits verarbeitet)
      jakarta.persistence.Query updateQuery = mock(jakarta.persistence.Query.class);
      when(em.createQuery(contains("UPDATE Lead"))).thenReturn(updateQuery);
      when(updateQuery.setParameter(anyString(), any())).thenReturn(updateQuery);
      when(updateQuery.executeUpdate()).thenReturn(0); // Already processed

      // Act
      int warned = service.checkProgressWarnings();

      // Assert
      assertThat(warned).isEqualTo(0);
      verify(progressWarningEvent, never()).fire(any());
    }

    @Test
    @DisplayName("Should skip leads with clock stopped (Stop-the-Clock)")
    void shouldSkipLeadsWithClockStopped() {
      // Arrange: Query liefert keine Leads (WHERE clause filtert clock_stopped_at IS NULL)
      when(em.createQuery(anyString(), eq(Lead.class))).thenReturn(query);
      when(query.setParameter(anyString(), any())).thenReturn(query);
      when(query.getResultList()).thenReturn(List.of()); // Empty

      // Act
      int warned = service.checkProgressWarnings();

      // Assert
      assertThat(warned).isEqualTo(0);
      verify(progressWarningEvent, never()).fire(any());
    }

    @Test
    @DisplayName("Should fire event with correct lead data")
    void shouldFireEventWithCorrectLeadData() {
      // Arrange
      Lead lead = createTestLead();
      lead.id = 99999L;
      lead.ownerUserId = "partner123";
      lead.progressDeadline = now.plusDays(5);
      lead.progressWarningSentAt = null;
      lead.status = LeadStatus.ACTIVE;

      when(em.createQuery(anyString(), eq(Lead.class))).thenReturn(query);
      when(query.setParameter(anyString(), any())).thenReturn(query);
      when(query.getResultList()).thenReturn(List.of(lead));

      jakarta.persistence.Query updateQuery = mock(jakarta.persistence.Query.class);
      when(em.createQuery(contains("UPDATE Lead"))).thenReturn(updateQuery);
      when(updateQuery.setParameter(anyString(), any())).thenReturn(updateQuery);
      when(updateQuery.executeUpdate()).thenReturn(1);

      // Act
      service.checkProgressWarnings();

      // Assert
      ArgumentCaptor<LeadProgressWarningIssuedEvent> eventCaptor =
          ArgumentCaptor.forClass(LeadProgressWarningIssuedEvent.class);
      verify(progressWarningEvent).fire(eventCaptor.capture());

      LeadProgressWarningIssuedEvent event = eventCaptor.getValue();
      assertThat(event.leadId()).isEqualTo(lead.id);
      assertThat(event.assignedTo()).isEqualTo("partner123");
      assertThat(event.progressDeadline()).isEqualTo(lead.progressDeadline);
    }
  }

  // ========== Job 2: Protection Expiry Check ==========

  @Nested
  @DisplayName("Job 2: Protection Expiry Check (10-Day Grace Period)")
  class ProtectionExpiryCheckTests {

    @Test
    @DisplayName("Should expire lead after 10-day grace period")
    void shouldExpireLeadAfterGracePeriod() {
      // Arrange: Lead mit Warnung vor 11 Tagen (> 10 Tage Nachfrist)
      Lead lead = createTestLead();
      lead.progressWarningSentAt = now.minusDays(11);
      lead.progressDeadline = now.minusDays(4);
      lead.status = LeadStatus.ACTIVE;

      when(em.createQuery(anyString(), eq(Lead.class))).thenReturn(query);
      when(query.setParameter(anyString(), any())).thenReturn(query);
      when(query.getResultList()).thenReturn(List.of(lead));

      jakarta.persistence.Query updateQuery = mock(jakarta.persistence.Query.class);
      when(em.createQuery(contains("UPDATE Lead"))).thenReturn(updateQuery);
      when(updateQuery.setParameter(anyString(), any())).thenReturn(updateQuery);
      when(updateQuery.executeUpdate()).thenReturn(1);

      // Act
      int expired = service.checkProtectionExpiry();

      // Assert
      assertThat(expired).isEqualTo(1);
      verify(protectionExpiredEvent).fire(any(LeadProtectionExpiredEvent.class));
    }

    @Test
    @DisplayName("Should NOT expire lead still in grace period")
    void shouldNotExpireLeadStillInGracePeriod() {
      // Arrange: Lead mit Warnung vor 5 Tagen (< 10 Tage Nachfrist)
      Lead lead = createTestLead();
      lead.progressWarningSentAt = now.minusDays(5);
      lead.progressDeadline = now.plusDays(2);
      lead.status = LeadStatus.ACTIVE;

      when(em.createQuery(anyString(), eq(Lead.class))).thenReturn(query);
      when(query.setParameter(anyString(), any())).thenReturn(query);
      when(query.getResultList()).thenReturn(List.of(lead));

      // Act
      int expired = service.checkProtectionExpiry();

      // Assert
      assertThat(expired).isEqualTo(0);
      verify(em, never()).createQuery(contains("UPDATE Lead"));
      verify(protectionExpiredEvent, never()).fire(any());
    }

    @Test
    @DisplayName("Should set status to EXPIRED and clear ownerUserId")
    void shouldSetStatusExpiredAndClearOwnerUserId() {
      // Arrange
      Lead lead = createTestLead();
      lead.id = 88888L;
      lead.ownerUserId = "partner456";
      lead.progressWarningSentAt = now.minusDays(11);
      lead.progressDeadline = now.minusDays(1);
      lead.status = LeadStatus.ACTIVE;

      when(em.createQuery(anyString(), eq(Lead.class))).thenReturn(query);
      when(query.setParameter(anyString(), any())).thenReturn(query);
      when(query.getResultList()).thenReturn(List.of(lead));

      jakarta.persistence.Query updateQuery = mock(jakarta.persistence.Query.class);
      when(em.createQuery(contains("UPDATE Lead"))).thenReturn(updateQuery);
      when(updateQuery.setParameter(anyString(), any())).thenReturn(updateQuery);
      when(updateQuery.executeUpdate()).thenReturn(1);

      // Act
      service.checkProtectionExpiry();

      // Assert
      verify(updateQuery).setParameter("expiredStatus", LeadStatus.EXPIRED);
      verify(updateQuery).setParameter("systemUser", "SYSTEM");

      ArgumentCaptor<LeadProtectionExpiredEvent> eventCaptor =
          ArgumentCaptor.forClass(LeadProtectionExpiredEvent.class);
      verify(protectionExpiredEvent).fire(eventCaptor.capture());

      LeadProtectionExpiredEvent event = eventCaptor.getValue();
      assertThat(event.leadId()).isEqualTo(lead.id);
      assertThat(event.previouslyAssignedTo()).isEqualTo("partner456");
    }
  }

  // ========== Job 3: DSGVO B2B Pseudonymization ==========

  @Nested
  @DisplayName("Job 3: DSGVO B2B Pseudonymization (60 days after expiry)")
  class PseudonymizationTests {

    @Test
    @DisplayName("Should pseudonymize leads 60 days after expiry")
    void shouldPseudonymizeLeadsAfter60Days() {
      // Arrange: Lead expired 61 Tage (> 60 Tage Schwelle)
      Lead lead = createTestLead();
      lead.email = "test@hotel.de";
      lead.phone = "+49123456789";
      lead.contactPerson = "Max Mustermann";
      lead.status = LeadStatus.EXPIRED;
      lead.updatedAt = now.minusDays(61);
      lead.pseudonymizedAt = null;

      when(em.createQuery(anyString(), eq(Lead.class))).thenReturn(query);
      when(query.setParameter(anyString(), any())).thenReturn(query);
      when(query.getResultList()).thenReturn(List.of(lead));

      jakarta.persistence.Query updateQuery = mock(jakarta.persistence.Query.class);
      when(em.createQuery(contains("UPDATE Lead"))).thenReturn(updateQuery);
      when(updateQuery.setParameter(anyString(), any())).thenReturn(updateQuery);
      when(updateQuery.executeUpdate()).thenReturn(1);

      // Act
      int pseudonymized = service.pseudonymizeExpiredLeads();

      // Assert
      assertThat(pseudonymized).isEqualTo(1);
      verify(pseudonymizedEvent).fire(any(LeadsPseudonymizedEvent.class));
    }

    @Test
    @DisplayName("Should hash email with SHA-256 (lowercase)")
    void shouldHashEmailWithSha256() {
      // Arrange
      Lead lead = createTestLead();
      lead.email = "Test@Hotel.DE"; // Mixed case
      lead.status = LeadStatus.EXPIRED;
      lead.updatedAt = now.minusDays(61);
      lead.pseudonymizedAt = null;

      when(em.createQuery(anyString(), eq(Lead.class))).thenReturn(query);
      when(query.setParameter(anyString(), any())).thenReturn(query);
      when(query.getResultList()).thenReturn(List.of(lead));

      jakarta.persistence.Query updateQuery = mock(jakarta.persistence.Query.class);
      when(em.createQuery(contains("UPDATE Lead"))).thenReturn(updateQuery);
      when(updateQuery.setParameter(anyString(), any())).thenReturn(updateQuery);
      when(updateQuery.executeUpdate()).thenReturn(1);

      // Act
      service.pseudonymizeExpiredLeads();

      // Assert
      ArgumentCaptor<String> emailHashCaptor = ArgumentCaptor.forClass(String.class);
      verify(updateQuery).setParameter(eq("emailHash"), emailHashCaptor.capture());

      String emailHash = emailHashCaptor.getValue();
      assertThat(emailHash).isNotNull();
      assertThat(emailHash).hasSize(64); // SHA-256 = 64 hex chars
      assertThat(emailHash).matches("^[a-f0-9]{64}$"); // Hex lowercase
    }

    @Test
    @DisplayName("Should skip leads not yet expired 60 days")
    void shouldSkipLeadsNotYetExpired60Days() {
      // Arrange: Lead expired 50 Tage (< 60 Tage Schwelle)
      Lead lead = createTestLead();
      lead.status = LeadStatus.EXPIRED;
      lead.updatedAt = now.minusDays(50);

      when(em.createQuery(anyString(), eq(Lead.class))).thenReturn(query);
      when(query.setParameter(anyString(), any())).thenReturn(query);
      when(query.getResultList()).thenReturn(List.of()); // Query filtert

      // Act
      int pseudonymized = service.pseudonymizeExpiredLeads();

      // Assert
      assertThat(pseudonymized).isEqualTo(0);
      verify(pseudonymizedEvent, never()).fire(any());
    }

    @Test
    @DisplayName("Should fire event with correct batch count")
    void shouldFireEventWithCorrectBatchCount() {
      // Arrange: 3 Leads
      Lead lead1 = createTestLead();
      lead1.email = "test1@example.com";
      lead1.status = LeadStatus.EXPIRED;
      lead1.updatedAt = now.minusDays(61);
      lead1.pseudonymizedAt = null;

      Lead lead2 = createTestLead();
      lead2.email = "test2@example.com";
      lead2.status = LeadStatus.EXPIRED;
      lead2.updatedAt = now.minusDays(61);
      lead2.pseudonymizedAt = null;

      Lead lead3 = createTestLead();
      lead3.email = "test3@example.com";
      lead3.status = LeadStatus.EXPIRED;
      lead3.updatedAt = now.minusDays(61);
      lead3.pseudonymizedAt = null;

      when(em.createQuery(anyString(), eq(Lead.class))).thenReturn(query);
      when(query.setParameter(anyString(), any())).thenReturn(query);
      when(query.getResultList()).thenReturn(List.of(lead1, lead2, lead3));

      jakarta.persistence.Query updateQuery = mock(jakarta.persistence.Query.class);
      when(em.createQuery(contains("UPDATE Lead"))).thenReturn(updateQuery);
      when(updateQuery.setParameter(anyString(), any())).thenReturn(updateQuery);
      when(updateQuery.executeUpdate()).thenReturn(1);

      // Act
      int pseudonymized = service.pseudonymizeExpiredLeads();

      // Assert
      assertThat(pseudonymized).isEqualTo(3);

      ArgumentCaptor<LeadsPseudonymizedEvent> eventCaptor =
          ArgumentCaptor.forClass(LeadsPseudonymizedEvent.class);
      verify(pseudonymizedEvent).fire(eventCaptor.capture());

      LeadsPseudonymizedEvent event = eventCaptor.getValue();
      assertThat(event.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should handle null email gracefully")
    void shouldHandleNullEmailGracefully() {
      // Arrange: Lead ohne Email
      Lead lead = createTestLead();
      lead.email = null;
      lead.status = LeadStatus.EXPIRED;
      lead.updatedAt = now.minusDays(61);
      lead.pseudonymizedAt = null;

      when(em.createQuery(anyString(), eq(Lead.class))).thenReturn(query);
      when(query.setParameter(anyString(), any())).thenReturn(query);
      when(query.getResultList()).thenReturn(List.of(lead));

      jakarta.persistence.Query updateQuery = mock(jakarta.persistence.Query.class);
      when(em.createQuery(contains("UPDATE Lead"))).thenReturn(updateQuery);
      when(updateQuery.setParameter(anyString(), any())).thenReturn(updateQuery);
      when(updateQuery.executeUpdate()).thenReturn(1);

      // Act
      int pseudonymized = service.pseudonymizeExpiredLeads();

      // Assert
      assertThat(pseudonymized).isEqualTo(1);
      verify(updateQuery).setParameter("emailHash", null);
    }
  }

  // ========== Job 4: Import Jobs Archival ==========

  @Nested
  @DisplayName("Job 4: Import Jobs Archival (Sprint 2.1.6 Phase 3 - Issue #134)")
  class ImportJobsArchivalTests {

    @Test
    @org.junit.jupiter.api.Disabled(
        "ImportJob archival requires DB access (Panache Entity). See Integration Test: LeadMaintenanceSchedulerIT.shouldArchiveExpiredImportJobs")
    @DisplayName("Should archive expired import jobs")
    void shouldArchiveExpiredImportJobs() {
      // Note: This test is DISABLED because ImportJob.findReadyForArchival() is a Panache query
      // and cannot be easily mocked in pure Mockito unit tests.
      //
      // Coverage: See integration test LeadMaintenanceSchedulerIT.shouldArchiveExpiredImportJobs
      // which validates the full archival flow with real database.

      // Act
      int archived = service.archiveCompletedImportJobs();

      // Assert: No jobs to archive (empty DB in test)
      assertThat(archived).isEqualTo(0);
      verify(importArchivedEvent, never()).fire(any());
    }

    // Full integration test for ImportJob archival is in LeadMaintenanceSchedulerIT
  }

  // ========== Helper Methods ==========

  private Lead createTestLead() {
    Lead lead = new Lead();
    lead.id = 12345L; // Long ID (auto-generated in real DB)
    lead.companyName = "Test Hotel GmbH";
    lead.city = "Berlin";
    lead.status = LeadStatus.ACTIVE;
    lead.ownerUserId = "partner123";
    lead.createdBy = "system";
    lead.createdAt = now.minusDays(30);
    lead.updatedAt = now.minusDays(1);
    return lead;
  }
}

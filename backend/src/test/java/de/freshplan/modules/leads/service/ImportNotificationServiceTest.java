package de.freshplan.modules.leads.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Unit Tests für ImportNotificationService.
 *
 * <p>Sprint 2.1.8 - Issue #149 Testet:
 *
 * <ul>
 *   <li>notifyApprovalRequired - Admin-Benachrichtigung bei hoher Duplikat-Rate
 *   <li>notifyImportApproved - User-Benachrichtigung bei Genehmigung
 *   <li>notifyImportRejected - User-Benachrichtigung bei Ablehnung
 *   <li>Email-Templates (HTML-Format, FreshFoodz CI)
 *   <li>Config-Properties (enabled, admin-emails)
 * </ul>
 *
 * @since Sprint 2.1.8
 */
@DisplayName("ImportNotificationService")
class ImportNotificationServiceTest {

  private ImportNotificationService service;
  private ReactiveMailer mockMailer;

  @BeforeEach
  void setUp() throws Exception {
    service = new ImportNotificationService();
    mockMailer = mock(ReactiveMailer.class);

    // Inject mock mailer via reflection
    setField(service, "mailer", mockMailer);
    setField(service, "notificationsEnabled", true);
    setField(
        service, "adminEmails", Optional.of(List.of("admin1@freshplan.de", "admin2@freshplan.de")));
    setField(service, "fromAddress", "noreply@freshplan.de");
    setField(service, "appBaseUrl", "https://app.freshplan.de");

    // Mock successful email send
    when(mockMailer.send(any(Mail.class))).thenReturn(Uni.createFrom().voidItem());
  }

  // ============================================================================
  // notifyApprovalRequired Tests
  // ============================================================================

  @Nested
  @DisplayName("notifyApprovalRequired")
  class NotifyApprovalRequiredTests {

    @Test
    @DisplayName("should send email to all admin emails")
    void shouldSendEmailToAllAdmins() {
      // Given
      UUID importId = UUID.randomUUID();

      // When
      service.notifyApprovalRequired(importId, "user-123", "leads.csv", 100, 0.15);

      // Then
      verify(mockMailer, times(2)).send(any(Mail.class));
    }

    @Test
    @DisplayName("should include correct subject with duplicate rate")
    void shouldIncludeCorrectSubject() {
      // Given
      UUID importId = UUID.randomUUID();
      ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);

      // When
      service.notifyApprovalRequired(importId, "user-123", "leads.csv", 100, 0.25);

      // Then
      verify(mockMailer, atLeastOnce()).send(mailCaptor.capture());
      Mail sentMail = mailCaptor.getValue();

      assertTrue(sentMail.getSubject().contains("[FreshPlan]"));
      assertTrue(sentMail.getSubject().contains("Genehmigung"));
      assertTrue(sentMail.getSubject().contains("25%")); // 0.25 * 100
    }

    @Test
    @DisplayName("should include approval URL in body")
    void shouldIncludeApprovalUrlInBody() {
      // Given
      UUID importId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
      ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);

      // When
      service.notifyApprovalRequired(importId, "user-123", "leads.csv", 100, 0.15);

      // Then
      verify(mockMailer, atLeastOnce()).send(mailCaptor.capture());
      Mail sentMail = mailCaptor.getValue();

      String htmlBody = sentMail.getHtml();
      assertTrue(htmlBody.contains("https://app.freshplan.de/admin/imports/" + importId));
    }

    @Test
    @DisplayName("should include import details in body")
    void shouldIncludeImportDetails() {
      // Given
      UUID importId = UUID.randomUUID();
      ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);

      // When
      service.notifyApprovalRequired(importId, "user-123", "test-leads.csv", 150, 0.20);

      // Then
      verify(mockMailer, atLeastOnce()).send(mailCaptor.capture());
      Mail sentMail = mailCaptor.getValue();

      String htmlBody = sentMail.getHtml();
      assertTrue(htmlBody.contains("user-123")); // User ID
      assertTrue(htmlBody.contains("test-leads.csv")); // Filename
      assertTrue(htmlBody.contains("150")); // Total rows
      assertTrue(htmlBody.contains("20")); // Duplicate rate percentage
    }

    @Test
    @DisplayName("should not send when notifications disabled")
    void shouldNotSendWhenDisabled() throws Exception {
      // Given
      setField(service, "notificationsEnabled", false);
      UUID importId = UUID.randomUUID();

      // When
      service.notifyApprovalRequired(importId, "user-123", "leads.csv", 100, 0.15);

      // Then
      verify(mockMailer, never()).send(any(Mail.class));
    }

    @Test
    @DisplayName("should not send when no admin emails configured")
    void shouldNotSendWhenNoAdminEmails() throws Exception {
      // Given
      setField(service, "adminEmails", Optional.empty());
      UUID importId = UUID.randomUUID();

      // When
      service.notifyApprovalRequired(importId, "user-123", "leads.csv", 100, 0.15);

      // Then
      verify(mockMailer, never()).send(any(Mail.class));
    }

    @Test
    @DisplayName("should skip blank admin emails")
    void shouldSkipBlankAdminEmails() throws Exception {
      // Given - List.of() doesn't allow null, use Arrays.asList for test
      setField(
          service,
          "adminEmails",
          Optional.of(java.util.Arrays.asList("admin@freshplan.de", "", "  ", null)));
      UUID importId = UUID.randomUUID();

      // When
      service.notifyApprovalRequired(importId, "user-123", "leads.csv", 100, 0.15);

      // Then
      verify(mockMailer, times(1)).send(any(Mail.class));
    }

    @Test
    @DisplayName("should use correct from address")
    void shouldUseCorrectFromAddress() {
      // Given
      UUID importId = UUID.randomUUID();
      ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);

      // When
      service.notifyApprovalRequired(importId, "user-123", "leads.csv", 100, 0.15);

      // Then
      verify(mockMailer, atLeastOnce()).send(mailCaptor.capture());
      Mail sentMail = mailCaptor.getValue();

      assertEquals("noreply@freshplan.de", sentMail.getFrom());
    }
  }

  // ============================================================================
  // notifyImportApproved Tests
  // ============================================================================

  @Nested
  @DisplayName("notifyImportApproved")
  class NotifyImportApprovedTests {

    @Test
    @DisplayName("should send approval confirmation to user")
    void shouldSendApprovalConfirmation() {
      // Given
      UUID importId = UUID.randomUUID();
      ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);

      // When
      service.notifyImportApproved(importId, "user@example.com", "leads.csv", 50);

      // Then
      verify(mockMailer).send(mailCaptor.capture());
      Mail sentMail = mailCaptor.getValue();

      assertEquals("user@example.com", sentMail.getTo().get(0));
      assertTrue(sentMail.getSubject().contains("genehmigt"));
    }

    @Test
    @DisplayName("should include imported count in body")
    void shouldIncludeImportedCount() {
      // Given
      UUID importId = UUID.randomUUID();
      ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);

      // When
      service.notifyImportApproved(importId, "user@example.com", "test.csv", 42);

      // Then
      verify(mockMailer).send(mailCaptor.capture());
      Mail sentMail = mailCaptor.getValue();

      assertTrue(sentMail.getHtml().contains("42"));
      assertTrue(sentMail.getHtml().contains("test.csv"));
    }

    @Test
    @DisplayName("should not send when user email is blank")
    void shouldNotSendWhenUserEmailBlank() {
      // Given
      UUID importId = UUID.randomUUID();

      // When
      service.notifyImportApproved(importId, "", "leads.csv", 50);
      service.notifyImportApproved(importId, "  ", "leads.csv", 50);
      service.notifyImportApproved(importId, null, "leads.csv", 50);

      // Then
      verify(mockMailer, never()).send(any(Mail.class));
    }

    @Test
    @DisplayName("should not send when notifications disabled")
    void shouldNotSendWhenDisabled() throws Exception {
      // Given
      setField(service, "notificationsEnabled", false);
      UUID importId = UUID.randomUUID();

      // When
      service.notifyImportApproved(importId, "user@example.com", "leads.csv", 50);

      // Then
      verify(mockMailer, never()).send(any(Mail.class));
    }
  }

  // ============================================================================
  // notifyImportRejected Tests
  // ============================================================================

  @Nested
  @DisplayName("notifyImportRejected")
  class NotifyImportRejectedTests {

    @Test
    @DisplayName("should send rejection notification to user")
    void shouldSendRejectionNotification() {
      // Given
      UUID importId = UUID.randomUUID();
      ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);

      // When
      service.notifyImportRejected(importId, "user@example.com", "leads.csv", "Zu viele Duplikate");

      // Then
      verify(mockMailer).send(mailCaptor.capture());
      Mail sentMail = mailCaptor.getValue();

      assertEquals("user@example.com", sentMail.getTo().get(0));
      assertTrue(sentMail.getSubject().contains("abgelehnt"));
    }

    @Test
    @DisplayName("should include rejection reason in body")
    void shouldIncludeRejectionReason() {
      // Given
      UUID importId = UUID.randomUUID();
      ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);

      // When
      service.notifyImportRejected(
          importId, "user@example.com", "data.csv", "Datenqualität nicht ausreichend");

      // Then
      verify(mockMailer).send(mailCaptor.capture());
      Mail sentMail = mailCaptor.getValue();

      assertTrue(sentMail.getHtml().contains("Datenqualität nicht ausreichend"));
      assertTrue(sentMail.getHtml().contains("data.csv"));
    }

    @Test
    @DisplayName("should handle null rejection reason")
    void shouldHandleNullRejectionReason() {
      // Given
      UUID importId = UUID.randomUUID();
      ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);

      // When
      service.notifyImportRejected(importId, "user@example.com", "leads.csv", null);

      // Then
      verify(mockMailer).send(mailCaptor.capture());
      Mail sentMail = mailCaptor.getValue();

      assertTrue(sentMail.getHtml().contains("Keine Begründung angegeben"));
    }

    @Test
    @DisplayName("should not send when user email is blank")
    void shouldNotSendWhenUserEmailBlank() {
      // Given
      UUID importId = UUID.randomUUID();

      // When
      service.notifyImportRejected(importId, "", "leads.csv", "Reason");
      service.notifyImportRejected(importId, null, "leads.csv", "Reason");

      // Then
      verify(mockMailer, never()).send(any(Mail.class));
    }
  }

  // ============================================================================
  // Email Template Tests
  // ============================================================================

  @Nested
  @DisplayName("Email Templates")
  class EmailTemplateTests {

    @Test
    @DisplayName("should use FreshPlan branding colors")
    void shouldUseFreshPlanBrandingColors() {
      // Given
      UUID importId = UUID.randomUUID();
      ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);

      // When
      service.notifyApprovalRequired(importId, "user", "file.csv", 100, 0.15);

      // Then
      verify(mockMailer, atLeastOnce()).send(mailCaptor.capture());
      String html = mailCaptor.getValue().getHtml();

      // FreshFoodz CI colors
      assertTrue(html.contains("#004F7B") || html.contains("#94C456"));
    }

    @Test
    @DisplayName("should use short import ID in template")
    void shouldUseShortImportId() {
      // Given
      UUID importId = UUID.fromString("12345678-1234-1234-1234-123456789012");
      ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);

      // When
      service.notifyApprovalRequired(importId, "user", "file.csv", 100, 0.15);

      // Then
      verify(mockMailer, atLeastOnce()).send(mailCaptor.capture());
      String html = mailCaptor.getValue().getHtml();

      // Short ID = first 8 chars
      assertTrue(html.contains("#12345678"));
    }

    @Test
    @DisplayName("approval email should contain required elements")
    void approvalEmailShouldContainRequiredElements() {
      // Given
      UUID importId = UUID.randomUUID();
      ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);

      // When
      service.notifyApprovalRequired(importId, "test-user", "import.csv", 200, 0.25);

      // Then
      verify(mockMailer, atLeastOnce()).send(mailCaptor.capture());
      String html = mailCaptor.getValue().getHtml();

      // Required elements
      assertTrue(html.contains("FreshPlan"), "Should contain FreshPlan branding");
      assertTrue(html.contains("Genehmigung"), "Should mention approval needed");
      assertTrue(html.contains("Duplikat"), "Should mention duplicates");
      assertTrue(html.contains("prüfen"), "Should contain action button");
      assertTrue(html.contains("href="), "Should contain link");
    }
  }

  // ============================================================================
  // Helper Methods
  // ============================================================================

  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}

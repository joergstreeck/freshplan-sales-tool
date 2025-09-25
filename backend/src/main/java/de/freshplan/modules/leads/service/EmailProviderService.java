package de.freshplan.modules.leads.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Email Provider Service - Abstraction für Email-Versand
 *
 * <p>Sprint 2.1: Mock-Implementation für Follow-up Automation Sprint 3.x: Integration mit
 * SendGrid/AWS SES
 */
@ApplicationScoped
public class EmailProviderService {

  private static final Logger LOG = Logger.getLogger(EmailProviderService.class);

  @ConfigProperty(name = "freshplan.email.mock-mode", defaultValue = "true")
  boolean mockMode;

  @ConfigProperty(name = "freshplan.email.log-emails", defaultValue = "true")
  boolean logEmails;

  /**
   * Sendet Email über konfigurierten Provider
   *
   * @param message Email-Message
   * @return true wenn erfolgreich
   */
  public boolean send(EmailNotificationService.EmailMessage message) {
    if (mockMode) {
      return sendMocked(message);
    }

    // Sprint 3.x: Real Provider Implementation
    // return sendViaSendGrid(message);
    // return sendViaAwsSes(message);

    LOG.warn("No real email provider configured, using mock mode");
    return sendMocked(message);
  }

  /** Mock-Implementation für Development/Testing Loggt Emails statt sie zu senden */
  private boolean sendMocked(EmailNotificationService.EmailMessage message) {
    if (logEmails) {
      LOG.infof(
          """
                ========== MOCK EMAIL ==========
                FROM: %s <%s>
                TO: %s <%s>
                SUBJECT: %s
                LEAD-ID: %s
                TEMPLATE-ID: %s
                --------------------------------
                HTML PREVIEW (first 500 chars):
                %s
                ================================
                """,
          message.getFromName(),
          message.getFrom(),
          message.getToName(),
          message.getTo(),
          message.getSubject(),
          message.getLeadId(),
          message.getTemplateId(),
          message.getHtmlContent() != null
              ? message
                  .getHtmlContent()
                  .substring(0, Math.min(500, message.getHtmlContent().length()))
              : "N/A");
    }

    // Simulate success rate (95% success in mock mode)
    return Math.random() > 0.05;
  }

  /** SendGrid Implementation (Sprint 3.x) */
  /*
  private boolean sendViaSendGrid(EmailNotificationService.EmailMessage message) {
      // TODO: Implement SendGrid integration
      // Use SendGrid Java SDK
      // Handle rate limiting
      // Track delivery status
      return false;
  }
  */

  /** AWS SES Implementation (Sprint 3.x) */
  /*
  private boolean sendViaAwsSes(EmailNotificationService.EmailMessage message) {
      // TODO: Implement AWS SES integration
      // Use AWS SDK for Java
      // Handle sandbox mode
      // Track bounce/complaint rates
      return false;
  }
  */

  /** Prüft Email-Provider Health */
  public boolean isHealthy() {
    if (mockMode) {
      return true;
    }

    // Sprint 3.x: Real health checks
    // Check API connectivity
    // Check rate limits
    // Check credit balance

    return true;
  }

  /** Gibt Provider-Statistiken zurück */
  public ProviderStats getStats() {
    return new ProviderStats(
        mockMode ? "MOCK" : "UNKNOWN",
        0, // sent today
        0, // bounced
        0, // complaints
        95.0 // success rate
        );
  }

  /** Provider Statistics */
  public record ProviderStats(
      String provider, int sentToday, int bounced, int complaints, double successRate) {}
}

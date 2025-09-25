package de.freshplan.modules.leads.service;

import de.freshplan.modules.leads.domain.CampaignTemplate;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.Territory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Map;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Email Notification Service für Lead-Follow-ups
 *
 * <p>Integriert mit externen Email-Providern (SendGrid/AWS SES) und respektiert DSGVO-Compliance
 * und Territory-spezifische Footer
 */
@ApplicationScoped
public class EmailNotificationService {

  private static final Logger LOG = Logger.getLogger(EmailNotificationService.class);

  @ConfigProperty(name = "freshplan.email.enabled", defaultValue = "true")
  boolean emailEnabled;

  @ConfigProperty(name = "freshplan.email.from", defaultValue = "noreply@freshfoodz.de")
  String fromEmail;

  @ConfigProperty(name = "freshplan.email.from-name", defaultValue = "FreshFoodz Cook&Fresh®")
  String fromName;

  @Inject EmailProviderService emailProvider; // Abstraction für SendGrid/SES

  @Inject UnsubscribeSecretGuard secretGuard;

  /**
   * Sendet Campaign-Email mit Template und personalisierten Daten
   *
   * @param lead Lead-Empfänger
   * @param template Campaign-Template
   * @param templateData Personalisierungs-Daten
   * @return true wenn erfolgreich gesendet
   */
  public boolean sendCampaignEmail(
      Lead lead, CampaignTemplate template, Map<String, String> templateData) {
    if (!emailEnabled) {
      LOG.debug("Email disabled in configuration, skipping send");
      return false;
    }

    if (lead.email == null || lead.email.isBlank()) {
      LOG.warnf("No email address for lead %s, skipping email", lead.id);
      return false;
    }

    try {
      // DSGVO-Check
      if (!hasEmailConsent(lead)) {
        LOG.debugf("Lead %s has no email consent, skipping", lead.id);
        return false;
      }

      // Füge Territory-spezifische Footer hinzu
      addTerritoryFooter(templateData, lead.territory);

      // Füge Unsubscribe-Link hinzu (DSGVO-Pflicht)
      templateData.put("unsubscribe.url", generateUnsubscribeUrl(lead));

      // Personalisiere Subject und Content
      String personalizedSubject = template.getPersonalizedSubject(templateData);
      String personalizedContent = template.getPersonalizedContent(templateData);

      // Sende Email über Provider
      EmailMessage message =
          EmailMessage.builder()
              .from(fromEmail, fromName)
              .to(lead.email, lead.contactPerson)
              .subject(personalizedSubject)
              .htmlContent(personalizedContent)
              .textContent(extractTextContent(personalizedContent))
              .leadId(lead.id.toString())
              .templateId(template.id.toString())
              .build();

      boolean sent = emailProvider.send(message);

      if (sent) {
        LOG.infof("Campaign email sent to lead %s using template %s", lead.id, template.name);
      } else {
        LOG.warnf("Failed to send campaign email to lead %s", lead.id);
      }

      return sent;

    } catch (Exception e) {
      LOG.errorf(e, "Error sending campaign email to lead %s", lead.id);
      return false;
    }
  }

  /**
   * Prüft DSGVO-Consent für Email-Kommunikation
   *
   * <p>DSGVO/GDPR-konform: Standardmäßig KEINE Einwilligung annehmen, nur wenn explizit als true in
   * Metadaten gespeichert
   */
  private boolean hasEmailConsent(Lead lead) {
    // Check metadata für explicit consent
    if (lead.metadata != null) {
      Boolean consent = lead.metadata.getBoolean("emailConsent");
      if (consent != null) {
        return consent;
      }
    }

    // Default: NO consent unless explicitly given (DSGVO/GDPR compliance)
    // This is a fail-safe approach - explicit opt-in required
    LOG.debugf("Lead %s has no explicit email consent - email blocked", lead.id);
    return false;
  }

  /** Fügt Territory-spezifische Footer hinzu (Impressum, MwSt-Info) */
  private void addTerritoryFooter(Map<String, String> templateData, Territory territory) {
    if (territory == null) {
      territory = Territory.getDefault(); // Deutschland als Default
    }

    String footer =
        switch (territory.id) {
          case "DE" ->
              """
                FreshFoodz Cook&Fresh® GmbH
                Musterstraße 123, 80333 München
                USt-IdNr: DE123456789
                MwSt: 19% (Deutschland)
                """;
          case "CH" ->
              """
                FreshFoodz Cook&Fresh® AG
                Bahnhofstrasse 10, 8001 Zürich
                CHE-123.456.789 MWST
                MwSt: 7.7% (Schweiz)
                """;
          default ->
              """
                FreshFoodz Cook&Fresh®
                International Sales
                """;
        };

    templateData.put("footer.legal", footer);
    templateData.put("footer.territory", territory.name);
    templateData.put("footer.currency", territory.currencyCode);
  }

  /** Generiert DSGVO-konformen Unsubscribe-URL */
  private String generateUnsubscribeUrl(Lead lead) {
    // In Produktion: Signierter Token mit Lead-ID und Timestamp
    String token = generateSecureToken(lead.id.toString());
    return String.format("https://crm.freshfoodz.de/unsubscribe?token=%s", token);
  }

  /**
   * Generiert kryptographisch sicheren Token für Unsubscribe-Links
   *
   * <p>Nutzt HMAC-SHA256 für Signierung um Manipulation zu verhindern
   */
  private String generateSecureToken(String leadId) {
    try {
      // Payload: LeadID + Timestamp + Random Nonce
      long timestamp = System.currentTimeMillis();
      String nonce = java.util.UUID.randomUUID().toString();
      String payload = String.format("%s:%d:%s", leadId, timestamp, nonce);

      // HMAC-SHA256 Signierung mit Guard-verifiziertem Secret
      String secret = secretGuard.getTokenSecret();

      javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
      javax.crypto.spec.SecretKeySpec secretKey =
          new javax.crypto.spec.SecretKeySpec(
              secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
      mac.init(secretKey);

      byte[] hmacBytes = mac.doFinal(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      String signature =
          java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);

      // Token Format: base64(payload).signature
      String encodedPayload =
          java.util.Base64.getUrlEncoder()
              .withoutPadding()
              .encodeToString(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));

      return encodedPayload + "." + signature;

    } catch (java.security.NoSuchAlgorithmException | java.security.InvalidKeyException e) {
      LOG.error("Failed to generate secure token", e);
      // Re-throw exception to prevent sending email with insecure token
      throw new RuntimeException("Could not generate secure token for unsubscribe link", e);
    }
  }

  /** Extrahiert Text-Content aus HTML für Multipart-Emails */
  private String extractTextContent(String htmlContent) {
    if (htmlContent == null) {
      return "";
    }

    // Simple HTML-Stripping für Sprint 2.1
    // In Produktion: Proper HTML-to-Text Converter
    return htmlContent
        .replaceAll("<[^>]+>", "") // Remove HTML tags
        .replaceAll("\\s+", " ") // Normalize whitespace
        .trim();
  }

  /** Email Message Builder Pattern */
  public static class EmailMessage {
    private String from;
    private String fromName;
    private String to;
    private String toName;
    private String subject;
    private String htmlContent;
    private String textContent;
    private String leadId;
    private String templateId;

    public static EmailMessageBuilder builder() {
      return new EmailMessageBuilder();
    }

    // Getters
    public String getFrom() {
      return from;
    }

    public String getFromName() {
      return fromName;
    }

    public String getTo() {
      return to;
    }

    public String getToName() {
      return toName;
    }

    public String getSubject() {
      return subject;
    }

    public String getHtmlContent() {
      return htmlContent;
    }

    public String getTextContent() {
      return textContent;
    }

    public String getLeadId() {
      return leadId;
    }

    public String getTemplateId() {
      return templateId;
    }

    public static class EmailMessageBuilder {
      private EmailMessage message = new EmailMessage();

      public EmailMessageBuilder from(String email, String name) {
        message.from = email;
        message.fromName = name;
        return this;
      }

      public EmailMessageBuilder to(String email, String name) {
        message.to = email;
        message.toName = name;
        return this;
      }

      public EmailMessageBuilder subject(String subject) {
        message.subject = subject;
        return this;
      }

      public EmailMessageBuilder htmlContent(String content) {
        message.htmlContent = content;
        return this;
      }

      public EmailMessageBuilder textContent(String content) {
        message.textContent = content;
        return this;
      }

      public EmailMessageBuilder leadId(String id) {
        message.leadId = id;
        return this;
      }

      public EmailMessageBuilder templateId(String id) {
        message.templateId = id;
        return this;
      }

      public EmailMessage build() {
        return message;
      }
    }
  }
}

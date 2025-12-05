package de.freshplan.modules.leads.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Email-Notification Service für Lead-Import Approval-Workflow.
 *
 * <p>Sendet Benachrichtigungen an ADMIN/MANAGER wenn ein Import Genehmigung benötigt (Duplikat-Rate
 * > 10%).
 *
 * @since Sprint 2.1.8
 */
@ApplicationScoped
public class ImportNotificationService {

  private static final Logger LOG = Logger.getLogger(ImportNotificationService.class);
  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

  @Inject ReactiveMailer mailer;

  @ConfigProperty(name = "freshplan.import.notification.enabled", defaultValue = "true")
  boolean notificationsEnabled;

  @ConfigProperty(name = "freshplan.import.notification.admin-emails", defaultValue = "")
  List<String> adminEmails;

  @ConfigProperty(
      name = "freshplan.import.notification.from",
      defaultValue = "noreply@freshplan.de")
  String fromAddress;

  @ConfigProperty(name = "freshplan.app.base-url", defaultValue = "http://localhost:5173")
  String appBaseUrl;

  /**
   * Sendet Notification an Admins wenn ein Import Genehmigung benötigt.
   *
   * @param importId Import-Log ID
   * @param userId User der den Import ausgelöst hat
   * @param fileName Name der importierten Datei
   * @param totalRows Gesamtanzahl Zeilen
   * @param duplicateRate Duplikat-Rate (0.0 - 1.0)
   */
  public void notifyApprovalRequired(
      UUID importId, String userId, String fileName, int totalRows, double duplicateRate) {

    if (!notificationsEnabled) {
      LOG.infof("Import notifications disabled, skipping for importId=%d", importId);
      return;
    }

    if (adminEmails.isEmpty()) {
      LOG.warnf(
          "No admin emails configured, cannot send approval notification for importId=%d",
          importId);
      return;
    }

    String subject =
        String.format(
            "[FreshPlan] Import-Genehmigung erforderlich - %.0f%% Duplikate", duplicateRate * 100);

    String approvalUrl = appBaseUrl + "/admin/imports/" + importId;

    String body =
        buildApprovalEmailHtml(importId, userId, fileName, totalRows, duplicateRate, approvalUrl);

    for (String adminEmail : adminEmails) {
      if (adminEmail == null || adminEmail.isBlank()) {
        continue;
      }

      Mail mail = Mail.withHtml(adminEmail.trim(), subject, body).setFrom(fromAddress);

      mailer
          .send(mail)
          .onFailure()
          .invoke(
              error ->
                  LOG.errorf(
                      error,
                      "Failed to send approval notification to %s for importId=%d",
                      adminEmail,
                      importId))
          .subscribe()
          .with(
              success ->
                  LOG.infof(
                      "Approval notification sent to %s for importId=%d", adminEmail, importId),
              failure ->
                  LOG.errorf(
                      "Failed to send notification to %s: %s", adminEmail, failure.getMessage()));
    }
  }

  /**
   * Sendet Notification an User wenn sein Import genehmigt wurde.
   *
   * @param importId Import-Log ID
   * @param userEmail Email des Users
   * @param fileName Name der Datei
   * @param importedCount Anzahl importierter Leads
   */
  public void notifyImportApproved(
      UUID importId, String userEmail, String fileName, int importedCount) {

    if (!notificationsEnabled || userEmail == null || userEmail.isBlank()) {
      return;
    }

    String subject = "[FreshPlan] Ihr Import wurde genehmigt";

    String body = buildApprovedEmailHtml(importId, fileName, importedCount);

    Mail mail = Mail.withHtml(userEmail, subject, body).setFrom(fromAddress);

    mailer
        .send(mail)
        .subscribe()
        .with(
            success ->
                LOG.infof("Approval confirmation sent to %s for importId=%d", userEmail, importId),
            failure ->
                LOG.errorf("Failed to send approval confirmation: %s", failure.getMessage()));
  }

  /**
   * Sendet Notification an User wenn sein Import abgelehnt wurde.
   *
   * @param importId Import-Log ID
   * @param userEmail Email des Users
   * @param fileName Name der Datei
   * @param reason Ablehnungsgrund
   */
  public void notifyImportRejected(
      UUID importId, String userEmail, String fileName, String reason) {

    if (!notificationsEnabled || userEmail == null || userEmail.isBlank()) {
      return;
    }

    String subject = "[FreshPlan] Ihr Import wurde abgelehnt";

    String body = buildRejectedEmailHtml(importId, fileName, reason);

    Mail mail = Mail.withHtml(userEmail, subject, body).setFrom(fromAddress);

    mailer
        .send(mail)
        .subscribe()
        .with(
            success ->
                LOG.infof("Rejection notification sent to %s for importId=%d", userEmail, importId),
            failure ->
                LOG.errorf("Failed to send rejection notification: %s", failure.getMessage()));
  }

  // ============================================================================
  // Email Templates
  // ============================================================================

  private String buildApprovalEmailHtml(
      UUID importId,
      String userId,
      String fileName,
      int totalRows,
      double duplicateRate,
      String approvalUrl) {

    String shortId = importId.toString().substring(0, 8);
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <style>
            body { font-family: 'Poppins', Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }
            .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
            .header { background-color: #004F7B; color: white; padding: 20px; text-align: center; }
            .header h1 { margin: 0; font-family: 'Antonio', Arial, sans-serif; }
            .content { padding: 30px; }
            .alert { background-color: #FFF3CD; border: 1px solid #FFE69C; border-radius: 4px; padding: 15px; margin-bottom: 20px; }
            .alert-icon { color: #856404; font-size: 24px; }
            .details { background-color: #F8F9FA; border-radius: 4px; padding: 15px; margin: 20px 0; }
            .details table { width: 100%%; border-collapse: collapse; }
            .details td { padding: 8px 0; border-bottom: 1px solid #E9ECEF; }
            .details td:first-child { font-weight: bold; width: 40%%; }
            .duplicate-rate { color: #DC3545; font-weight: bold; font-size: 1.2em; }
            .btn { display: inline-block; padding: 12px 24px; background-color: #94C456; color: white; text-decoration: none; border-radius: 4px; font-weight: bold; margin-top: 20px; }
            .btn:hover { background-color: #7FB043; }
            .footer { background-color: #F8F9FA; padding: 15px; text-align: center; font-size: 12px; color: #6C757D; }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="header">
              <h1>🍃 FreshPlan Import</h1>
            </div>
            <div class="content">
              <div class="alert">
                <span class="alert-icon">⚠️</span>
                <strong>Genehmigung erforderlich</strong>
                <p>Ein Lead-Import hat die Duplikat-Schwelle überschritten und benötigt Ihre Freigabe.</p>
              </div>

              <div class="details">
                <table>
                  <tr>
                    <td>Import-ID:</td>
                    <td>#%s</td>
                  </tr>
                  <tr>
                    <td>Benutzer:</td>
                    <td>%s</td>
                  </tr>
                  <tr>
                    <td>Datei:</td>
                    <td>%s</td>
                  </tr>
                  <tr>
                    <td>Zeilen gesamt:</td>
                    <td>%d</td>
                  </tr>
                  <tr>
                    <td>Duplikat-Rate:</td>
                    <td><span class="duplicate-rate">%.1f%%</span> (Schwelle: 10%%)</td>
                  </tr>
                </table>
              </div>

              <p>Bitte prüfen Sie die Duplikate und entscheiden Sie über die Freigabe:</p>

              <center>
                <a href="%s" class="btn">Import prüfen →</a>
              </center>
            </div>
            <div class="footer">
              FreshPlan Sales Tool • Diese E-Mail wurde automatisch generiert
            </div>
          </div>
        </body>
        </html>
        """
        .formatted(shortId, userId, fileName, totalRows, duplicateRate * 100, approvalUrl);
  }

  private String buildApprovedEmailHtml(UUID importId, String fileName, int importedCount) {
    String shortId = importId.toString().substring(0, 8);
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <style>
            body { font-family: 'Poppins', Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }
            .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
            .header { background-color: #94C456; color: white; padding: 20px; text-align: center; }
            .header h1 { margin: 0; font-family: 'Antonio', Arial, sans-serif; }
            .content { padding: 30px; }
            .success { background-color: #D4EDDA; border: 1px solid #C3E6CB; border-radius: 4px; padding: 15px; margin-bottom: 20px; color: #155724; }
            .footer { background-color: #F8F9FA; padding: 15px; text-align: center; font-size: 12px; color: #6C757D; }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="header">
              <h1>✅ Import genehmigt</h1>
            </div>
            <div class="content">
              <div class="success">
                <strong>Ihr Import wurde genehmigt!</strong>
              </div>
              <p>Die Leads aus der Datei <strong>%s</strong> wurden erfolgreich importiert.</p>
              <p><strong>%d Leads</strong> wurden in das System übernommen.</p>
              <p>Import-ID: #%s</p>
            </div>
            <div class="footer">
              FreshPlan Sales Tool • Diese E-Mail wurde automatisch generiert
            </div>
          </div>
        </body>
        </html>
        """
        .formatted(fileName, importedCount, shortId);
  }

  private String buildRejectedEmailHtml(UUID importId, String fileName, String reason) {
    String shortId = importId.toString().substring(0, 8);
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <style>
            body { font-family: 'Poppins', Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }
            .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
            .header { background-color: #DC3545; color: white; padding: 20px; text-align: center; }
            .header h1 { margin: 0; font-family: 'Antonio', Arial, sans-serif; }
            .content { padding: 30px; }
            .error { background-color: #F8D7DA; border: 1px solid #F5C6CB; border-radius: 4px; padding: 15px; margin-bottom: 20px; color: #721C24; }
            .reason { background-color: #F8F9FA; border-radius: 4px; padding: 15px; margin: 20px 0; }
            .footer { background-color: #F8F9FA; padding: 15px; text-align: center; font-size: 12px; color: #6C757D; }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="header">
              <h1>❌ Import abgelehnt</h1>
            </div>
            <div class="content">
              <div class="error">
                <strong>Ihr Import wurde abgelehnt</strong>
              </div>
              <p>Der Import der Datei <strong>%s</strong> wurde von einem Administrator abgelehnt.</p>
              <div class="reason">
                <strong>Begründung:</strong>
                <p>%s</p>
              </div>
              <p>Import-ID: #%s</p>
              <p>Bitte korrigieren Sie die Daten und versuchen Sie es erneut.</p>
            </div>
            <div class="footer">
              FreshPlan Sales Tool • Diese E-Mail wurde automatisch generiert
            </div>
          </div>
        </body>
        </html>
        """
        .formatted(fileName, reason != null ? reason : "Keine Begründung angegeben", shortId);
  }
}

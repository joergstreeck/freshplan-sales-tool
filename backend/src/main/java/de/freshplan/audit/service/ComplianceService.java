package de.freshplan.audit.service;

import de.freshplan.audit.dto.ComplianceAlertDto;
import de.freshplan.audit.dto.ComplianceAlertDto.AlertType;
import de.freshplan.audit.dto.ComplianceAlertDto.Severity;
import de.freshplan.audit.repository.AuditRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service zur Überwachung und Generierung von Compliance-Alerts für das Audit-System gemäß
 * DSGVO-Anforderungen.
 */
@ApplicationScoped
public class ComplianceService {

  @Inject AuditRepository auditRepository;

  /**
   * Generiert Compliance-Alerts basierend auf den aktuellen Audit-Daten.
   *
   * @return Liste von Compliance-Alerts
   */
  public List<ComplianceAlertDto> getComplianceAlerts() {
    List<ComplianceAlertDto> alerts = new ArrayList<>();

    // Check for missing retention policies
    long missingRetention =
        auditRepository.count("isDsgvoRelevant = true AND retentionUntil IS NULL");
    if (missingRetention > 0) {
      alerts.add(
          new ComplianceAlertDto(
              AlertType.WARNING,
              missingRetention + " DSGVO-relevante Einträge ohne Retention-Policy",
              Severity.HIGH,
              missingRetention));
    }

    // Check for expiring entries
    LocalDateTime expiryWarning = LocalDateTime.now().plusDays(30);
    long expiringSoon = auditRepository.count("retentionUntil <= ?1", expiryWarning);
    if (expiringSoon > 0) {
      alerts.add(
          new ComplianceAlertDto(
              AlertType.INFO,
              expiringSoon + " Einträge laufen in 30 Tagen ab",
              Severity.MEDIUM,
              expiringSoon));
    }

    // Check hash chain integrity
    String integrityStatus = auditRepository.getLastIntegrityCheckStatus();
    if (!"VALID".equals(integrityStatus)) {
      alerts.add(
          new ComplianceAlertDto(
              AlertType.ERROR, "Hash-Chain Integrität muss überprüft werden", Severity.CRITICAL));
    }

    // Check for overdue deletions
    LocalDateTime now = LocalDateTime.now();
    long overdueDeletions = auditRepository.count("retentionUntil < ?1 AND deletedAt IS NULL", now);
    if (overdueDeletions > 0) {
      alerts.add(
          new ComplianceAlertDto(
              AlertType.ERROR,
              overdueDeletions + " Einträge überfällig für Löschung",
              Severity.HIGH,
              overdueDeletions));
    }

    // Check for unauthorized access patterns
    long suspiciousAccess = auditRepository.countSuspiciousActivities();
    if (suspiciousAccess > 0) {
      alerts.add(
          new ComplianceAlertDto(
              AlertType.WARNING,
              suspiciousAccess + " verdächtige Zugriffsmuster erkannt",
              Severity.MEDIUM,
              suspiciousAccess));
    }

    return alerts;
  }

  /**
   * Prüft, ob alle Compliance-Anforderungen erfüllt sind.
   *
   * @return true wenn alle Anforderungen erfüllt sind
   */
  public boolean isCompliant() {
    List<ComplianceAlertDto> alerts = getComplianceAlerts();
    return alerts.stream()
        .noneMatch(
            alert ->
                alert.getSeverity() == Severity.CRITICAL || alert.getSeverity() == Severity.HIGH);
  }

  /**
   * Berechnet den Compliance-Score als Prozentsatz.
   *
   * @return Compliance-Score zwischen 0 und 100
   */
  public int calculateComplianceScore() {
    List<ComplianceAlertDto> alerts = getComplianceAlerts();

    // Basis-Score von 100
    int score = 100;

    // Abzüge basierend auf Alert-Severity
    for (ComplianceAlertDto alert : alerts) {
      switch (alert.getSeverity()) {
        case CRITICAL:
          score -= 30;
          break;
        case HIGH:
          score -= 15;
          break;
        case MEDIUM:
          score -= 5;
          break;
        case LOW:
          score -= 2;
          break;
      }
    }

    // Score kann nicht unter 0 fallen
    return Math.max(0, score);
  }
}

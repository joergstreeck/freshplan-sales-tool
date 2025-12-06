package de.freshplan.modules.leads.service;

import de.freshplan.modules.leads.domain.ImportLog;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.jboss.logging.Logger;

/**
 * Import Quota Service - Sprint 2.1.8 Phase 2
 *
 * <p>Prüft Import-Quotas für Self-Service Lead-Import:
 *
 * <ul>
 *   <li>Max. offene Leads pro User (nach Rolle)
 *   <li>Max. Imports pro Tag (nach Rolle)
 *   <li>Max. Leads pro Import (nach Rolle)
 * </ul>
 *
 * <p><strong>Quota-Limits:</strong>
 *
 * <table>
 *   <tr><th>Rolle</th><th>Offene Leads</th><th>Imports/Tag</th><th>Leads/Import</th></tr>
 *   <tr><td>SALES</td><td>100</td><td>3</td><td>100</td></tr>
 *   <tr><td>MANAGER</td><td>200</td><td>5</td><td>200</td></tr>
 *   <tr><td>ADMIN</td><td>∞</td><td>∞</td><td>1000</td></tr>
 * </table>
 *
 * @since Sprint 2.1.8
 */
@ApplicationScoped
public class ImportQuotaService {

  private static final Logger LOG = Logger.getLogger(ImportQuotaService.class);

  @Inject EntityManager em;

  /**
   * Prüft ob ein User einen Import mit der angegebenen Lead-Anzahl durchführen darf.
   *
   * @param userId Keycloak Subject des Users
   * @param role User-Rolle (SALES, MANAGER, ADMIN)
   * @param leadCount Anzahl der zu importierenden Leads
   * @return QuotaCheckResult mit Ergebnis und Details
   */
  public QuotaCheckResult checkQuota(String userId, UserRole role, int leadCount) {
    LOG.infof("Quota check for user %s (role=%s, leadCount=%d)", userId, role, leadCount);

    QuotaLimits limits = getQuotaLimits(role);

    // 1. Prüfe: Max. Leads pro Import
    if (leadCount > limits.maxLeadsPerImport()) {
      return QuotaCheckResult.rejected(
          String.format(
              "Zu viele Leads: %d (max. %d pro Import für Rolle %s)",
              leadCount, limits.maxLeadsPerImport(), role));
    }

    // 2. Prüfe: Imports heute
    long todayImports = ImportLog.countTodayImports(userId);
    if (todayImports >= limits.maxImportsPerDay()) {
      return QuotaCheckResult.rejected(
          String.format(
              "Tageslimit erreicht: %d Imports heute (max. %d für Rolle %s)",
              todayImports, limits.maxImportsPerDay(), role));
    }

    // 3. Prüfe: Offene Leads
    long currentOpenLeads = countOpenLeadsByOwner(userId);
    if (currentOpenLeads + leadCount > limits.maxOpenLeads()) {
      return QuotaCheckResult.rejected(
          String.format(
              "Lead-Limit erreicht: Sie haben bereits %d offene Leads. "
                  + "Mit diesem Import (%d Leads) würden Sie das Limit von %d überschreiten.",
              currentOpenLeads, leadCount, limits.maxOpenLeads()));
    }

    // Alle Checks bestanden
    return QuotaCheckResult.approved(
        currentOpenLeads, limits.maxOpenLeads(), todayImports, limits.maxImportsPerDay());
  }

  /**
   * Gibt die aktuellen Quota-Informationen für einen User zurück (ohne Check).
   *
   * @param userId Keycloak Subject des Users
   * @param role User-Rolle
   * @return QuotaInfo mit aktuellen Werten und Limits
   */
  public QuotaInfo getQuotaInfo(String userId, UserRole role) {
    QuotaLimits limits = getQuotaLimits(role);
    long currentOpenLeads = countOpenLeadsByOwner(userId);
    long todayImports = ImportLog.countTodayImports(userId);

    return new QuotaInfo(
        currentOpenLeads,
        limits.maxOpenLeads(),
        todayImports,
        limits.maxImportsPerDay(),
        limits.maxLeadsPerImport(),
        limits.maxOpenLeads() - currentOpenLeads // remainingCapacity
        );
  }

  /**
   * Zählt offene Leads eines Users (nicht DSGVO-gelöscht, nicht konvertiert).
   *
   * @param userId Owner-User-ID
   * @return Anzahl offener Leads
   */
  private long countOpenLeadsByOwner(String userId) {
    // Offene Leads = nicht gelöscht, nicht zu Kunde konvertiert
    return em.createQuery(
            "SELECT COUNT(l) FROM Lead l WHERE l.ownerUserId = :userId "
                + "AND (l.gdprDeleted = false OR l.gdprDeleted IS NULL) "
                + "AND l.status != 'CONVERTED'",
            Long.class)
        .setParameter("userId", userId)
        .getSingleResult();
  }

  /**
   * Gibt die Quota-Limits für eine Rolle zurück.
   *
   * @param role User-Rolle
   * @return QuotaLimits Record
   */
  private QuotaLimits getQuotaLimits(UserRole role) {
    return switch (role) {
      case SALES -> new QuotaLimits(100, 3, 100);
      case MANAGER -> new QuotaLimits(200, 5, 200);
      case ADMIN -> new QuotaLimits(Integer.MAX_VALUE, Integer.MAX_VALUE, 1000);
      case AUDITOR -> new QuotaLimits(0, 0, 0); // Auditor darf nicht importieren
    };
  }

  // ============================================================================
  // Inner Classes: Records für Type-Safety
  // ============================================================================

  /** Quota-Limits pro Rolle */
  public record QuotaLimits(int maxOpenLeads, int maxImportsPerDay, int maxLeadsPerImport) {}

  /** Ergebnis einer Quota-Prüfung */
  public record QuotaCheckResult(
      boolean approved,
      String message,
      long currentOpenLeads,
      int maxOpenLeads,
      long todayImports,
      int maxImportsPerDay) {

    public static QuotaCheckResult approved(
        long currentOpenLeads, int maxOpenLeads, long todayImports, int maxImportsPerDay) {
      return new QuotaCheckResult(
          true, "Quota OK", currentOpenLeads, maxOpenLeads, todayImports, maxImportsPerDay);
    }

    public static QuotaCheckResult rejected(String message) {
      return new QuotaCheckResult(false, message, 0, 0, 0, 0);
    }
  }

  /** Quota-Informationen für UI */
  public record QuotaInfo(
      long currentOpenLeads,
      int maxOpenLeads,
      long todayImports,
      int maxImportsPerDay,
      int maxLeadsPerImport,
      long remainingCapacity) {}

  /** User-Rollen für Quota-System */
  public enum UserRole {
    SALES,
    MANAGER,
    ADMIN,
    AUDITOR
  }
}

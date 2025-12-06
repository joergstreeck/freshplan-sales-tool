package de.freshplan.modules.leads.service;

import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.service.AuditService;
import de.freshplan.modules.leads.domain.GdprDataRequest;
import de.freshplan.modules.leads.domain.GdprDeletionLog;
import de.freshplan.modules.leads.domain.Lead;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.jboss.logging.Logger;

/**
 * DSGVO Compliance Service - Sprint 2.1.8
 *
 * <p>Implementiert die gesetzlich vorgeschriebenen DSGVO-Features:
 *
 * <ul>
 *   <li><strong>Art. 15 - Auskunftsrecht:</strong> Datenexport auf Anfrage (PDF)
 *   <li><strong>Art. 17 - Löschrecht:</strong> Sofort-Löschung auf Antrag (Soft-Delete +
 *       PII-Anonymisierung)
 *   <li><strong>Art. 7 Abs. 3 - Einwilligungswiderruf:</strong> Consent-Widerruf mit Kontakt-Sperre
 * </ul>
 *
 * <p><strong>Rechtliche Grundlage:</strong> EU-DSGVO mit Bußgeldern bis 20 Mio EUR
 *
 * <p><strong>Architektur:</strong> Soft-Delete + PII-Anonymisierung (kein Hard-Delete), Audit-Trail
 * für Compliance-Nachweise
 *
 * @since Sprint 2.1.8
 */
@ApplicationScoped
public class GdprService {

  private static final Logger LOG = Logger.getLogger(GdprService.class);

  private static final String ENTITY_TYPE_LEAD = "LEAD";
  private static final String ANONYMIZED_COMPANY_PREFIX = "DSGVO-GELÖSCHT-";

  @Inject EntityManager em;

  @Inject GdprPdfGeneratorService pdfService;

  @Inject AuditService auditService;

  // ============================================================================
  // Art. 15 - Auskunftsrecht (Datenexport)
  // ============================================================================

  /**
   * Generiert einen DSGVO-konformen Datenexport (Art. 15).
   *
   * <p>Antwortfrist: 1 Monat ab Anfrage (Art. 12 Abs. 3 DSGVO)
   *
   * @param leadId ID des Leads
   * @param requestedBy User-ID des Anfragenden
   * @return PDF als Byte-Array
   */
  @Transactional
  public byte[] generateDataExport(Long leadId, String requestedBy) {
    LOG.infof("GDPR Data Export requested for Lead %d by %s", leadId, requestedBy);

    Lead lead = em.find(Lead.class, leadId);
    if (lead == null) {
      throw new IllegalArgumentException("Lead not found: " + leadId);
    }

    // 1. Request protokollieren
    GdprDataRequest request = new GdprDataRequest();
    request.entityType = ENTITY_TYPE_LEAD;
    request.entityId = leadId;
    request.requestedBy = requestedBy;
    request.requestedAt = LocalDateTime.now();
    request.persist();

    // 2. PDF generieren
    byte[] pdf = pdfService.generateLeadDataExport(lead);

    // 3. Request als erfolgreich markieren
    request.markPdfGenerated();

    // 4. Audit-Log
    createAuditEntry(
        AuditEventType.GDPR_REQUEST, leadId, requestedBy, "Art. 15 Datenexport für Lead " + leadId);

    LOG.infof("GDPR Data Export generated for Lead %d (Request ID: %d)", leadId, request.id);

    return pdf;
  }

  /**
   * Listet alle Datenexport-Anfragen für einen Lead.
   *
   * @param leadId ID des Leads
   * @return Liste der Anfragen
   */
  public List<GdprDataRequest> getDataRequestsForLead(Long leadId) {
    return GdprDataRequest.findByEntity(ENTITY_TYPE_LEAD, leadId);
  }

  // ============================================================================
  // Art. 17 - Löschrecht (Soft-Delete + PII-Anonymisierung)
  // ============================================================================

  /**
   * DSGVO-konforme Löschung eines Leads (Art. 17).
   *
   * <p><strong>Implementierung:</strong> Soft-Delete + PII-Anonymisierung
   *
   * <ul>
   *   <li>Personenbezogene Daten werden anonymisiert (NULL/Hash)
   *   <li>Audit-Trail bleibt erhalten (für Compliance-Audits)
   *   <li>Referentielle Integrität bleibt intakt
   * </ul>
   *
   * @param leadId ID des Leads
   * @param deletedBy User-ID des Löschenden
   * @param reason DSGVO-konformer Löschgrund
   * @throws GdprDeletionBlockedException wenn Löschung nicht möglich (z.B. offene Opportunities)
   */
  @Transactional
  public void gdprDeleteLead(Long leadId, String deletedBy, String reason) {
    LOG.infof("GDPR Deletion requested for Lead %d by %s. Reason: %s", leadId, deletedBy, reason);

    Lead lead = em.find(Lead.class, leadId);
    if (lead == null) {
      throw new IllegalArgumentException("Lead not found: " + leadId);
    }

    // 1. Prüfe ob bereits gelöscht
    if (Boolean.TRUE.equals(lead.gdprDeleted)) {
      throw new IllegalStateException("Lead " + leadId + " wurde bereits DSGVO-konform gelöscht");
    }

    // 2. Prüfe Abhängigkeiten (z.B. offene Opportunities)
    checkDeletionConstraints(lead);

    // 3. Hash für Audit-Nachweis erstellen
    String dataHash = createDataHash(lead);

    // 4. PII anonymisieren
    anonymizeLeadPii(lead, deletedBy, reason);

    // 5. Deletion Log erstellen
    GdprDeletionLog deletionLog = new GdprDeletionLog();
    deletionLog.entityType = ENTITY_TYPE_LEAD;
    deletionLog.entityId = leadId;
    deletionLog.deletedBy = deletedBy;
    deletionLog.deletedAt = LocalDateTime.now();
    deletionLog.deletionReason = reason;
    deletionLog.originalDataHash = dataHash;
    deletionLog.persist();

    // 6. Audit-Log
    createAuditEntry(
        AuditEventType.DATA_ANONYMIZED,
        leadId,
        deletedBy,
        "Art. 17 DSGVO-Löschung für Lead " + leadId + ". Grund: " + reason);

    LOG.infof("GDPR Deletion completed for Lead %d (Deletion Log ID: %d)", leadId, deletionLog.id);
  }

  /**
   * Prüft ob ein Lead gelöscht werden kann.
   *
   * @param lead Der zu prüfende Lead
   * @throws GdprDeletionBlockedException wenn Löschung blockiert ist
   */
  private void checkDeletionConstraints(Lead lead) {
    // Prüfe auf Opportunities (via JPQL Query)
    Long opportunityCount =
        em.createQuery("SELECT COUNT(o) FROM Opportunity o WHERE o.lead.id = :leadId", Long.class)
            .setParameter("leadId", lead.id)
            .getSingleResult();

    if (opportunityCount > 0) {
      throw new GdprDeletionBlockedException(
          "Lead hat "
              + opportunityCount
              + " Opportunity/Opportunities. Bitte zuerst abschließen oder löschen.");
    }
  }

  /**
   * Anonymisiert personenbezogene Daten eines Leads.
   *
   * <p>Gemäß DSGVO Art. 17 werden folgende Daten anonymisiert:
   *
   * <ul>
   *   <li>companyName → "DSGVO-GELÖSCHT-{shortId}"
   *   <li>contactPerson → NULL
   *   <li>email → NULL
   *   <li>phone → NULL
   *   <li>street → NULL
   *   <li>city → NULL
   *   <li>postalCode → NULL
   *   <li>notes (painNotes etc.) → NULL
   * </ul>
   */
  private void anonymizeLeadPii(Lead lead, String deletedBy, String reason) {
    LocalDateTime now = LocalDateTime.now();

    // Company Name mit Short-ID für Identifizierung
    String shortId = lead.id.toString();
    if (shortId.length() > 8) {
      shortId = shortId.substring(0, 8);
    }
    lead.companyName = ANONYMIZED_COMPANY_PREFIX + shortId;

    // Personenbezogene Daten anonymisieren
    lead.contactPerson = null;
    lead.email = null;
    lead.emailNormalized = null;
    lead.phone = null;
    lead.phoneE164 = null;
    lead.website = null;
    lead.websiteDomain = null;
    lead.street = null;
    lead.city = null;
    lead.postalCode = null;

    // Notizen anonymisieren
    lead.painNotes = null;
    lead.internalChampionName = null;

    // DSGVO-Flags setzen
    lead.gdprDeleted = true;
    lead.gdprDeletedAt = now;
    lead.gdprDeletedBy = deletedBy;
    lead.gdprDeletionReason = reason;

    // Kontakt sperren
    lead.contactBlocked = true;

    // Updated Timestamp
    lead.updatedAt = now;
    lead.updatedBy = deletedBy;
  }

  /**
   * Listet alle Löschprotokolle für einen Lead.
   *
   * @param leadId ID des Leads
   * @return Liste der Löschprotokolle
   */
  public List<GdprDeletionLog> getDeletionLogsForLead(Long leadId) {
    return GdprDeletionLog.findByEntity(ENTITY_TYPE_LEAD, leadId);
  }

  // ============================================================================
  // Art. 7 Abs. 3 - Einwilligungswiderruf
  // ============================================================================

  /**
   * Widerruft die Einwilligung eines Leads (Art. 7 Abs. 3).
   *
   * <p>Nach Widerruf:
   *
   * <ul>
   *   <li>Kontakt wird gesperrt (contactBlocked = true)
   *   <li>Keine Kontaktaufnahme mehr erlaubt
   *   <li>Alle Kontakt-Aktionen werden blockiert
   * </ul>
   *
   * @param leadId ID des Leads
   * @param revokedBy User-ID des Widerrufenden
   */
  @Transactional
  public void revokeConsent(Long leadId, String revokedBy) {
    LOG.infof("Consent revocation requested for Lead %d by %s", leadId, revokedBy);

    Lead lead = em.find(Lead.class, leadId);
    if (lead == null) {
      throw new IllegalArgumentException("Lead not found: " + leadId);
    }

    // Prüfe ob bereits widerrufen
    if (lead.consentRevokedAt != null) {
      throw new IllegalStateException(
          "Einwilligung für Lead "
              + leadId
              + " wurde bereits am "
              + lead.consentRevokedAt
              + " widerrufen");
    }

    LocalDateTime now = LocalDateTime.now();

    // Consent-Widerruf protokollieren
    lead.consentRevokedAt = now;
    lead.consentRevokedBy = revokedBy;
    lead.contactBlocked = true;

    // Updated Timestamp
    lead.updatedAt = now;
    lead.updatedBy = revokedBy;

    // Audit-Log
    createAuditEntry(
        AuditEventType.GDPR_REQUEST,
        leadId,
        revokedBy,
        "Art. 7 Abs. 3 Einwilligungswiderruf für Lead " + leadId);

    LOG.infof("Consent revoked for Lead %d. Contact blocked.", leadId);
  }

  /**
   * Prüft ob ein Lead kontaktiert werden darf.
   *
   * @param leadId ID des Leads
   * @return true wenn Kontakt erlaubt, false wenn gesperrt
   */
  public boolean isContactAllowed(Long leadId) {
    Lead lead = em.find(Lead.class, leadId);
    if (lead == null) {
      return false;
    }
    return !Boolean.TRUE.equals(lead.contactBlocked) && !Boolean.TRUE.equals(lead.gdprDeleted);
  }

  // ============================================================================
  // Helper Methods
  // ============================================================================

  /**
   * Erstellt einen SHA-256 Hash der Lead-Daten für Audit-Nachweise.
   *
   * <p>Der Hash ermöglicht den Nachweis, dass bestimmte Daten existierten und gelöscht wurden, ohne
   * die Daten selbst zu speichern.
   */
  private String createDataHash(Lead lead) {
    String dataToHash =
        String.valueOf(lead.id)
            + "|"
            + (lead.companyName != null ? lead.companyName : "")
            + "|"
            + (lead.email != null ? lead.email : "")
            + "|"
            + (lead.contactPerson != null ? lead.contactPerson : "");

    return sha256Hash(dataToHash);
  }

  private String sha256Hash(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder hexString = new StringBuilder();
      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      LOG.error("SHA-256 algorithm not available", e);
      throw new RuntimeException("Failed to hash data", e);
    }
  }

  private void createAuditEntry(
      AuditEventType eventType, Long entityId, String userId, String details) {
    try {
      // Use the enterprise AuditService for proper audit logging
      UUID entityUuid = UUID.nameUUIDFromBytes(("LEAD-" + entityId).getBytes());
      auditService.logAsync(eventType, ENTITY_TYPE_LEAD, entityUuid, null, null, details);
      LOG.debugf("Audit entry created for Lead %d: %s", entityId, eventType);
    } catch (Exception e) {
      // Audit failures should not block GDPR operations
      LOG.warnf(e, "Failed to create audit entry for Lead %d: %s", entityId, eventType);
    }
  }

  // ============================================================================
  // Exception Classes
  // ============================================================================

  /** Exception wenn DSGVO-Löschung blockiert ist (z.B. wegen Abhängigkeiten) */
  public static class GdprDeletionBlockedException extends RuntimeException {
    public GdprDeletionBlockedException(String message) {
      super(message);
    }
  }
}

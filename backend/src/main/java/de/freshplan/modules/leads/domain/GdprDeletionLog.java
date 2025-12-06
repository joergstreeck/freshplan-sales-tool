package de.freshplan.modules.leads.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DSGVO Art. 17 - Löschrecht: Audit-Trail für DSGVO-konforme Löschungen.
 *
 * <p>Diese Entity protokolliert alle DSGVO-Löschungen. Sie dient als rechtlicher Nachweis, dass
 * personenbezogene Daten gemäß Art. 17 DSGVO gelöscht/anonymisiert wurden.
 *
 * <p><strong>Rechtsgrundlage:</strong> EU-DSGVO Art. 17 - Recht auf Löschung ("Recht auf
 * Vergessenwerden")
 *
 * <p><strong>Wichtig:</strong> Diese Logs dürfen NICHT gelöscht werden, da sie für
 * Compliance-Audits erforderlich sind (Art. 17 Abs. 3e DSGVO).
 *
 * @since Sprint 2.1.8
 */
@Entity
@Table(name = "gdpr_deletion_logs")
public class GdprDeletionLog extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  /** Entitätstyp: LEAD oder CUSTOMER */
  @NotNull @Size(max = 50)
  @Column(name = "entity_type", nullable = false)
  public String entityType;

  /** ID der gelöschten Entität */
  @NotNull @Column(name = "entity_id", nullable = false)
  public Long entityId;

  /** User-ID des Löschenden (Manager/Admin) */
  @NotNull @Size(max = 50)
  @Column(name = "deleted_by", nullable = false)
  public String deletedBy;

  /** Zeitpunkt der Löschung */
  @NotNull @Column(name = "deleted_at", nullable = false)
  public LocalDateTime deletedAt = LocalDateTime.now();

  /**
   * DSGVO-konformer Löschgrund.
   *
   * <p>Mögliche Werte:
   *
   * <ul>
   *   <li>ART_17_REQUEST - Betroffener hat Löschung beantragt
   *   <li>DATA_NOT_REQUIRED - Daten nicht mehr erforderlich (Art. 17 Abs. 1a)
   *   <li>CONSENT_WITHDRAWN - Einwilligung widerrufen (Art. 17 Abs. 1b)
   *   <li>OTHER - Anderer Grund (mit Freitext-Begründung)
   * </ul>
   */
  @NotNull @Size(max = 500)
  @Column(name = "deletion_reason", nullable = false)
  public String deletionReason;

  /**
   * SHA-256 Hash der Original-Daten für Audit-Nachweise.
   *
   * <p>Ermöglicht den Nachweis, dass bestimmte Daten existierten und gelöscht wurden, ohne die
   * Daten selbst zu speichern.
   */
  @Size(max = 64)
  @Column(name = "original_data_hash")
  public String originalDataHash;

  @Column(name = "created_at", nullable = false)
  public LocalDateTime createdAt = LocalDateTime.now();

  // ============================================================================
  // Static Finder Methods
  // ============================================================================

  /** Findet alle Löschungen für eine bestimmte Entität */
  public static List<GdprDeletionLog> findByEntity(String entityType, Long entityId) {
    return list("entityType = ?1 AND entityId = ?2", entityType, entityId);
  }

  /** Findet alle Löschungen eines bestimmten Users */
  public static List<GdprDeletionLog> findByDeletedBy(String userId) {
    return list("deletedBy", userId);
  }

  /** Findet alle Löschungen in einem Zeitraum */
  public static List<GdprDeletionLog> findByDateRange(LocalDateTime from, LocalDateTime to) {
    return list("deletedAt >= ?1 AND deletedAt <= ?2 ORDER BY deletedAt DESC", from, to);
  }

  /** Zählt Löschungen für eine bestimmte Entität */
  public static long countByEntity(String entityType, Long entityId) {
    return count("entityType = ?1 AND entityId = ?2", entityType, entityId);
  }

  /** Findet alle Löschungen eines bestimmten Typs */
  public static List<GdprDeletionLog> findByEntityType(String entityType) {
    return list("entityType = ?1 ORDER BY deletedAt DESC", entityType);
  }

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    if (deletedAt == null) {
      deletedAt = LocalDateTime.now();
    }
  }
}

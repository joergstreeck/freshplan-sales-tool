package de.freshplan.modules.leads.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DSGVO Art. 15 - Auskunftsrecht: Tracking von Datenauskunfts-Anfragen.
 *
 * <p>Diese Entity protokolliert alle Anfragen zur Datenauskunft gemäß DSGVO Art. 15. Sie dient als
 * Audit-Trail für Compliance-Nachweise.
 *
 * <p><strong>Rechtsgrundlage:</strong> EU-DSGVO Art. 15 - Auskunftsrecht der betroffenen Person
 *
 * <p><strong>Antwortfrist:</strong> 1 Monat ab Anfrage (Art. 12 Abs. 3 DSGVO)
 *
 * @since Sprint 2.1.8
 */
@Entity
@Table(name = "gdpr_data_requests")
public class GdprDataRequest extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  /** Entitätstyp: LEAD oder CUSTOMER */
  @NotNull @Size(max = 50)
  @Column(name = "entity_type", nullable = false)
  public String entityType;

  /** ID der betroffenen Entität */
  @NotNull @Column(name = "entity_id", nullable = false)
  public Long entityId;

  /** User-ID des Anfragenden (Manager/Admin) */
  @NotNull @Size(max = 50)
  @Column(name = "requested_by", nullable = false)
  public String requestedBy;

  /** Zeitpunkt der Anfrage */
  @NotNull @Column(name = "requested_at", nullable = false)
  public LocalDateTime requestedAt = LocalDateTime.now();

  /** Wurde PDF erfolgreich generiert? */
  @Column(name = "pdf_generated")
  public Boolean pdfGenerated = false;

  /** Zeitpunkt der PDF-Generierung */
  @Column(name = "pdf_generated_at")
  public LocalDateTime pdfGeneratedAt;

  /** Optionale Notizen zur Anfrage */
  @Size(max = 1000)
  @Column(name = "notes")
  public String notes;

  @Column(name = "created_at", nullable = false)
  public LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at", nullable = false)
  public LocalDateTime updatedAt = LocalDateTime.now();

  // ============================================================================
  // Static Finder Methods
  // ============================================================================

  /** Findet alle Anfragen für eine bestimmte Entität */
  public static List<GdprDataRequest> findByEntity(String entityType, Long entityId) {
    return list("entityType = ?1 AND entityId = ?2", entityType, entityId);
  }

  /** Findet alle Anfragen eines bestimmten Users */
  public static List<GdprDataRequest> findByRequestedBy(String userId) {
    return list("requestedBy", userId);
  }

  /** Findet alle offenen Anfragen (PDF noch nicht generiert) */
  public static List<GdprDataRequest> findPendingRequests() {
    return list("pdfGenerated = false ORDER BY requestedAt ASC");
  }

  /** Zählt Anfragen für eine bestimmte Entität */
  public static long countByEntity(String entityType, Long entityId) {
    return count("entityType = ?1 AND entityId = ?2", entityType, entityId);
  }

  // ============================================================================
  // Business Methods
  // ============================================================================

  /** Markiert die Anfrage als PDF generiert */
  public void markPdfGenerated() {
    this.pdfGenerated = true;
    this.pdfGeneratedAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (requestedAt == null) {
      requestedAt = LocalDateTime.now();
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}

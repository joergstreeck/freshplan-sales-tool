package de.freshplan.audit.repository;

import de.freshplan.audit.entity.AuditLog;
import de.freshplan.audit.entity.AuditLog.*;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Repository für AuditLog Entity. Bietet spezialisierte Queries für Compliance, Forensik und
 * Admin-Dashboard.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
@Transactional
public class AuditRepository implements PanacheRepositoryBase<AuditLog, UUID> {

  /** Findet Audit-Einträge für eine Entität. */
  public List<AuditLog> findByEntity(EntityType entityType, UUID entityId) {
    return find(
            "entityType = ?1 AND entityId = ?2",
            Sort.by("occurredAt").descending(),
            entityType,
            entityId)
        .list();
  }

  /** Findet Audit-Einträge eines Users. */
  public List<AuditLog> findByUser(UUID userId) {
    String userIdStr = userId != null ? userId.toString() : null;
    return find("userId", Sort.by("occurredAt").descending(), userIdStr).list();
  }

  /** Findet Audit-Einträge in einem Zeitraum. */
  public List<AuditLog> findInPeriod(LocalDateTime from, LocalDateTime to) {
    return find("occurredAt BETWEEN ?1 AND ?2", Sort.by("occurredAt").descending(), from, to)
        .list();
  }

  /** Findet kritische Aktionen. */
  public List<AuditLog> findCriticalActions(LocalDateTime since) {
    return find(
            "occurredAt >= ?1 AND action IN ?2",
            Sort.by("occurredAt").descending(),
            since,
            Arrays.asList(
                AuditAction.DELETE,
                AuditAction.BULK_DELETE,
                AuditAction.PERMISSION_CHANGE,
                AuditAction.DATA_DELETION))
        .list();
  }

  /** Findet DSGVO-relevante Einträge. */
  public List<AuditLog> findDsgvoRelevant(LocalDateTime from, LocalDateTime to) {
    return find(
            "isDsgvoRelevant = true AND occurredAt BETWEEN ?1 AND ?2",
            Sort.by("occurredAt").descending(),
            from,
            to)
        .list();
  }

  /** Findet Einträge für eine Transaktion. */
  public List<AuditLog> findByTransactionId(String transactionId) {
    return find("transactionId", Sort.by("occurredAt"), transactionId).list();
  }

  /** Findet fehlgeschlagene Login-Versuche. */
  public List<AuditLog> findFailedLogins(UUID userId, LocalDateTime since) {
    String userIdStr = userId != null ? userId.toString() : null;
    return find(
            "userId = ?1 AND action = ?2 AND occurredAt >= ?3",
            Sort.by("occurredAt").descending(),
            userIdStr,
            AuditAction.FAILED_LOGIN,
            since)
        .list();
  }

  /** Zählt Aktionen eines Users - optimiert mit GROUP BY Query. */
  public Map<AuditAction, Long> countUserActions(UUID userId, LocalDateTime since) {
    // userId in der Entity ist ein String, also konvertieren
    String userIdStr = userId != null ? userId.toString() : null;
    return getEntityManager()
        .createQuery(
            "SELECT a.action, COUNT(a) FROM AuditLog a " +
            "WHERE a.userId = :userId AND a.occurredAt >= :since " +
            "GROUP BY a.action", Object[].class)
        .setParameter("userId", userIdStr)
        .setParameter("since", since)
        .getResultStream()
        .collect(Collectors.toMap(
            row -> (AuditAction) row[0],
            row -> (Long) row[1]
        ));
  }

  /** Findet verdächtige Aktivitäten. */
  public List<AuditLog> findSuspiciousActivities(LocalDateTime from, LocalDateTime to) {
    // Verdächtig sind:
    // - Massenaktionen außerhalb Geschäftszeiten
    // - Viele Exporte in kurzer Zeit
    // - Zugriff auf gelöschte Daten

    return getEntityManager()
        .createQuery(
            "SELECT a FROM AuditLog a "
                + "WHERE a.occurredAt BETWEEN :from AND :to "
                + "AND ("
                + "  (a.action IN ('BULK_DELETE', 'BULK_UPDATE') AND "
                + "   (EXTRACT(HOUR FROM a.occurredAt) < 6 OR EXTRACT(HOUR FROM a.occurredAt) > 22)) "
                + "  OR "
                + "  (a.action = 'EXPORT' AND EXISTS ("
                + "     SELECT 1 FROM AuditLog a2 "
                + "     WHERE a2.userId = a.userId "
                + "     AND a2.action = 'EXPORT' "
                + "     AND a2.occurredAt BETWEEN a.occurredAt - INTERVAL '1 HOUR' AND a.occurredAt"
                + "     GROUP BY a2.userId HAVING COUNT(*) > 5"
                + "  ))"
                + ") "
                + "ORDER BY a.occurredAt DESC",
            AuditLog.class)
        .setParameter("from", from)
        .setParameter("to", to)
        .getResultList();
  }

  /** Prüft die Hash-Chain Integrität - optimiert mit Stream-Processing. */
  public boolean verifyHashChain(LocalDateTime from, LocalDateTime to) {
    // Verwende Stream statt List für bessere Memory-Effizienz
    try (Stream<AuditLog> logStream = 
        find("occurredAt BETWEEN ?1 AND ?2", Sort.by("occurredAt"), from, to).stream()) {
      
      // Container für previousHash (mutable reference für Lambda)
      String[] previousHashHolder = new String[1];
      
      return logStream.allMatch(log -> {
        // Erster Eintrag
        if (previousHashHolder[0] == null) {
          if (log.getPreviousHash() != null && !log.getPreviousHash().equals("GENESIS")) {
            return false; // Erster Eintrag sollte GENESIS oder null haben
          }
        } else {
          // Prüfe ob previousHash mit dem Hash des vorherigen Eintrags übereinstimmt
          if (!previousHashHolder[0].equals(log.getPreviousHash())) {
            return false; // Hash-Chain unterbrochen
          }
        }
        previousHashHolder[0] = log.getCurrentHash();
        return true;
      });
    }
  }

  /** Findet Einträge die bald gelöscht werden müssen (DSGVO). */
  public List<AuditLog> findExpiringEntries(int daysAhead) {
    LocalDateTime expiryThreshold = LocalDateTime.now().plusDays(daysAhead);
    return find(
            "retentionUntil IS NOT NULL AND retentionUntil <= ?1",
            Sort.by("retentionUntil"),
            expiryThreshold)
        .list();
  }

  /** Statistiken für Dashboard. */
  public Map<String, Object> getDashboardStatistics(LocalDateTime from, LocalDateTime to) {
    Map<String, Object> stats = new HashMap<>();

    // Total Events
    stats.put("totalEvents", count("occurredAt BETWEEN ?1 AND ?2", from, to));

    // Critical Events
    stats.put(
        "criticalEvents",
        count(
            "occurredAt BETWEEN ?1 AND ?2 AND action IN ?3",
            from,
            to,
            Arrays.asList(
                AuditAction.DELETE,
                AuditAction.BULK_DELETE,
                AuditAction.PERMISSION_CHANGE,
                AuditAction.DATA_DELETION)));

    // DSGVO Relevant
    stats.put(
        "dsgvoRelevant",
        count("occurredAt BETWEEN ?1 AND ?2 AND isDsgvoRelevant = true", from, to));

    // Unique Users (userId is now String)
    List<String> uniqueUsers =
        getEntityManager()
            .createQuery(
                "SELECT DISTINCT a.userId FROM AuditLog a "
                    + "WHERE a.occurredAt BETWEEN :from AND :to",
                String.class)
            .setParameter("from", from)
            .setParameter("to", to)
            .getResultList();
    stats.put("activeUsers", uniqueUsers.size());

    // Actions by Type
    List<Object[]> actionCounts =
        getEntityManager()
            .createQuery(
                "SELECT a.action, COUNT(a) FROM AuditLog a "
                    + "WHERE a.occurredAt BETWEEN :from AND :to "
                    + "GROUP BY a.action",
                Object[].class)
            .setParameter("from", from)
            .setParameter("to", to)
            .getResultList();

    Map<AuditAction, Long> actionMap = new HashMap<>();
    for (Object[] row : actionCounts) {
      actionMap.put((AuditAction) row[0], (Long) row[1]);
    }
    stats.put("actionsByType", actionMap);

    return stats;
  }

  /** Findet den letzten Audit-Eintrag (für Hash-Chain). */
  public Optional<AuditLog> findLastEntry() {
    return find("", Sort.by("occurredAt", "id").descending()).firstResultOptional();
  }

  /** Paginierte Suche mit Filtern. */
  public List<AuditLog> search(
      EntityType entityType,
      AuditAction action,
      UUID userId,
      LocalDateTime from,
      LocalDateTime to,
      int page,
      int size) {
    StringBuilder query = new StringBuilder("1=1");
    Map<String, Object> params = new HashMap<>();

    if (entityType != null) {
      query.append(" AND entityType = :entityType");
      params.put("entityType", entityType);
    }

    if (action != null) {
      query.append(" AND action = :action");
      params.put("action", action);
    }

    if (userId != null) {
      query.append(" AND userId = :userId");
      params.put("userId", userId.toString());
    }

    if (from != null) {
      query.append(" AND occurredAt >= :from");
      params.put("from", from);
    }

    if (to != null) {
      query.append(" AND occurredAt <= :to");
      params.put("to", to);
    }

    return find(query.toString(), Sort.by("occurredAt").descending(), params)
        .page(Page.of(page, size))
        .list();
  }
}

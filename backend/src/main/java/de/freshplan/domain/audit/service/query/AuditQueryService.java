package de.freshplan.domain.audit.service.query;

import de.freshplan.domain.audit.dto.ComplianceAlertDto;
import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.repository.AuditRepository;
import de.freshplan.domain.export.service.dto.ExportRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.jboss.logging.Logger;

/**
 * Audit Query Service - CQRS Read Side
 *
 * <p>Behandelt alle lesenden Operationen für Audit-Einträge: - Find operations - Statistics and
 * metrics - Dashboard data - Compliance checks - Export operations
 *
 * <p>WICHTIG: Dieser Service hat KEINE @Transactional Annotation, da nur lesende Operationen
 * durchgeführt werden. Alle Query-Methoden delegieren an das AuditRepository.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class AuditQueryService {

  private static final Logger log = Logger.getLogger(AuditQueryService.class);

  @Inject AuditRepository auditRepository;

  // =====================================
  // BASIC QUERY OPERATIONS
  // =====================================

  /** Find audit entries by entity Delegiert an AuditRepository.findByEntity() */
  public List<AuditEntry> findByEntity(String entityType, UUID entityId) {
    log.debugf("Finding audit entries for entity: %s/%s", entityType, entityId);
    return auditRepository.findByEntity(entityType, entityId);
  }

  /**
   * Find audit entries by entity with pagination Delegiert an AuditRepository.findByEntity() mit
   * Pagination
   */
  public List<AuditEntry> findByEntity(String entityType, UUID entityId, int page, int size) {
    log.debugf(
        "Finding audit entries for entity: %s/%s (page %d, size %d)",
        entityType, entityId, page, size);
    return auditRepository.findByEntity(entityType, entityId, page, size);
  }

  /** Find audit entries by user Delegiert an AuditRepository.findByUser() */
  public List<AuditEntry> findByUser(UUID userId, Instant from, Instant to) {
    log.debugf("Finding audit entries for user: %s from %s to %s", userId, from, to);
    return auditRepository.findByUser(userId, from, to);
  }

  /** Find audit entries by event type Delegiert an AuditRepository.findByEventType() */
  public List<AuditEntry> findByEventType(AuditEventType eventType, Instant from, Instant to) {
    log.debugf("Finding audit entries for event type: %s from %s to %s", eventType, from, to);
    return auditRepository.findByEventType(eventType, from, to);
  }

  /** Find security-relevant audit entries Delegiert an AuditRepository.findSecurityEvents() */
  public List<AuditEntry> findSecurityEvents(Instant from, Instant to) {
    log.debugf("Finding security events from %s to %s", from, to);
    return auditRepository.findSecurityEvents(from, to);
  }

  /** Find failed operations Delegiert an AuditRepository.findFailures() */
  public List<AuditEntry> findFailures(Instant from, Instant to) {
    log.debugf("Finding failures from %s to %s", from, to);
    return auditRepository.findFailures(from, to);
  }

  // =====================================
  // ADVANCED SEARCH
  // =====================================

  /** Advanced search with multiple filters Delegiert an AuditRepository.search() */
  public List<AuditEntry> search(AuditRepository.AuditSearchCriteria criteria) {
    log.debugf("Performing advanced search with criteria");
    return auditRepository.search(criteria);
  }

  /**
   * Stream audit entries for export (memory-efficient) Delegiert an
   * AuditRepository.streamForExport()
   */
  public Stream<AuditEntry> streamForExport(AuditRepository.AuditSearchCriteria criteria) {
    log.debugf("Streaming audit entries for export");
    return auditRepository.streamForExport(criteria);
  }

  // =====================================
  // STATISTICS AND METRICS
  // =====================================

  /** Get audit statistics for a time period Delegiert an AuditRepository.getStatistics() */
  public AuditRepository.AuditStatistics getStatistics(Instant from, Instant to) {
    log.debugf("Getting audit statistics from %s to %s", from, to);
    return auditRepository.getStatistics(from, to);
  }

  /** Get dashboard metrics for Admin UI Delegiert an AuditRepository.getDashboardMetrics() */
  public AuditRepository.DashboardMetrics getDashboardMetrics() {
    log.debugf("Getting dashboard metrics");
    return auditRepository.getDashboardMetrics();
  }

  /**
   * Get activity chart data for visualization Delegiert an AuditRepository.getActivityChartData()
   */
  public List<Map<String, Object>> getActivityChartData(int days, String groupBy) {
    log.debugf("Getting activity chart data for %d days grouped by %s", days, groupBy);
    return auditRepository.getActivityChartData(days, groupBy);
  }

  /** Get critical events for dashboard Delegiert an AuditRepository.getCriticalEvents() */
  public List<AuditEntry> getCriticalEvents(int limit) {
    log.debugf("Getting %d critical events", limit);
    return auditRepository.getCriticalEvents(limit);
  }

  // =====================================
  // COMPLIANCE AND INTEGRITY
  // =====================================

  /**
   * Find entries that require notification Delegiert an AuditRepository.findRequiringNotification()
   */
  public List<AuditEntry> findRequiringNotification() {
    log.debugf("Finding entries requiring notification");
    return auditRepository.findRequiringNotification();
  }

  /** Verify audit trail integrity Delegiert an AuditRepository.verifyIntegrity() */
  public List<AuditRepository.AuditIntegrityIssue> verifyIntegrity(Instant from, Instant to) {
    log.warnf("Verifying audit trail integrity from %s to %s", from, to);
    return auditRepository.verifyIntegrity(from, to);
  }

  /** Get compliance alerts Delegiert an AuditRepository.getComplianceAlerts() */
  public List<ComplianceAlertDto> getComplianceAlerts() {
    log.debugf("Getting compliance alerts");
    return auditRepository.getComplianceAlerts();
  }

  /**
   * Get last audit entry hash for blockchain-style chaining Delegiert an
   * AuditRepository.getLastHash()
   */
  public String getLastHash() {
    return auditRepository.getLastHash().orElse(null);
  }

  // =====================================
  // EXPORT OPERATIONS
  // =====================================

  /** Find audit entries by filters for export Delegiert an AuditRepository.findByFilters() */
  public List<AuditEntry> findByFilters(ExportRequest request) {
    log.debugf("Finding audit entries for export with filters");
    return auditRepository.findByFilters(request);
  }

  /** Count audit entries by filters for export Delegiert an AuditRepository.countByFilters() */
  public long countByFilters(ExportRequest request) {
    log.debugf("Counting audit entries for export");
    return auditRepository.countByFilters(request);
  }

  // =====================================
  // DATA RETENTION
  // =====================================

  /**
   * Count old audit entries for retention check Delegiert an AuditRepository.deleteOlderThan() mit
   * dryRun=true
   */
  public long countOlderThan(Instant cutoffDate) {
    log.debugf("Counting audit entries older than %s", cutoffDate);
    return auditRepository.deleteOlderThan(cutoffDate, true);
  }

  /**
   * Clean up old audit entries (for data retention) HINWEIS: Diese Methode führt einen DELETE durch
   * und sollte eigentlich im CommandService sein, aber da sie im Repository ist, delegieren wir sie
   * hier nur für die Kompatibilität.
   *
   * <p>TODO: In späteren Refactorings sollte diese Methode in den CommandService verschoben werden.
   */
  public long deleteOlderThan(Instant cutoffDate) {
    log.warnf("Deleting audit entries older than %s", cutoffDate);
    // Diese Operation ist eigentlich ein Command, aber für Kompatibilität
    // delegieren wir sie hier nur ans Repository
    return auditRepository.deleteOlderThan(cutoffDate, false);
  }
}

package de.freshplan.domain.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.freshplan.domain.audit.dto.ComplianceAlertDto;
import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.entity.AuditSource;
import de.freshplan.domain.audit.events.AuditableApplicationEvent;
import de.freshplan.domain.audit.repository.AuditRepository;
import de.freshplan.domain.audit.service.command.AuditCommandService;
import de.freshplan.domain.audit.service.dto.AuditContext;
import de.freshplan.domain.audit.service.provider.AuditConfiguration;
import de.freshplan.domain.audit.service.provider.AuditEvent;
import de.freshplan.domain.audit.service.provider.AuditException;
import de.freshplan.domain.audit.service.query.AuditQueryService;
import de.freshplan.domain.export.service.dto.ExportRequest;
import de.freshplan.shared.util.SecurityUtils;
import io.quarkus.runtime.Startup;
import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Enterprise-grade Audit Service with async processing and integrity verification
 *
 * <p>FACADE PATTERN: Dieser Service fungiert als Facade für die CQRS-aufgeteilten Services. Mit
 * Feature Flag kann zwischen Legacy-Implementierung und CQRS umgeschaltet werden.
 *
 * <p>Features: - Async audit logging to prevent performance impact - Cryptographic hash chaining
 * for tamper detection - Automatic context enrichment - Event-driven architecture support -
 * Configurable retention policies
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
@Startup
public class AuditService {

  private static final Logger log = Logger.getLogger(AuditService.class);

  @Inject AuditRepository auditRepository;

  @Inject ObjectMapper objectMapper;

  @Inject SecurityUtils securityUtils;

  @Inject Event<AuditEvent> auditEventBus;

  @Inject AuditConfiguration configuration;

  @Inject Instance<HttpServerRequest> httpRequestInstance;

  // CQRS Services (NEU)
  @Inject AuditCommandService commandService;

  @Inject AuditQueryService queryService;

  // Feature Flag für CQRS
  @ConfigProperty(name = "features.cqrs.enabled", defaultValue = "false")
  boolean cqrsEnabled;

  private ExecutorService auditExecutor;
  private volatile String lastGlobalHash = null;

  @PostConstruct
  void init() {
    // Initialize async executor for audit logging
    this.auditExecutor =
        Executors.newFixedThreadPool(
            configuration.getAsyncThreadPoolSize(),
            r -> {
              Thread thread = new Thread(r);
              thread.setName("audit-executor-" + thread.getId());
              thread.setDaemon(true);
              return thread;
            });

    log.infof(
        "Audit Service initialized with %d async threads", configuration.getAsyncThreadPoolSize());
  }

  @PreDestroy
  void cleanup() {
    if (auditExecutor != null) {
      log.info("Shutting down audit executor service...");
      auditExecutor.shutdown();
      try {
        // Wait for existing tasks to complete
        if (!auditExecutor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)) {
          log.warn("Audit executor did not terminate in time, forcing shutdown");
          auditExecutor.shutdownNow();
          // Wait a bit for tasks to respond to cancellation
          if (!auditExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
            log.error("Audit executor did not terminate after forced shutdown");
          }
        }
        log.info("Audit executor service shut down successfully");
      } catch (InterruptedException e) {
        log.error("Interrupted while shutting down audit executor", e);
        auditExecutor.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
  }

  /** Log an audit event asynchronously */
  public CompletableFuture<UUID> logAsync(
      AuditEventType eventType,
      String entityType,
      UUID entityId,
      Object oldValue,
      Object newValue,
      String reason) {

    if (cqrsEnabled) {
      log.debugf("CQRS mode: delegating to AuditCommandService");
      return commandService.logAsync(eventType, entityType, entityId, oldValue, newValue, reason);
    }

    return logAsync(
        AuditContext.builder()
            .eventType(eventType)
            .entityType(entityType)
            .entityId(entityId)
            .oldValue(oldValue)
            .newValue(newValue)
            .changeReason(reason)
            .build());
  }

  /** Log an audit event with full context asynchronously */
  public CompletableFuture<UUID> logAsync(AuditContext context) {
    if (cqrsEnabled) {
      log.debugf("CQRS mode: delegating to AuditCommandService");
      return commandService.logAsync(context);
    }
    // Capture request context before async execution
    final var capturedContext = captureCurrentContext(context);

    return CompletableFuture.supplyAsync(
        () -> {
          try {
            return logSync(capturedContext);
          } catch (Exception e) {
            log.errorf(e, "Failed to log audit event: %s", context.getEventType());
            // Re-throw to propagate through CompletableFuture
            throw new AuditException("Failed to log audit event", e);
          }
        },
        auditExecutor);
  }

  /** Log an audit event synchronously (use sparingly) */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  @jakarta.enterprise.context.control.ActivateRequestContext
  public UUID logSync(AuditContext context) {
    if (cqrsEnabled) {
      log.debugf("CQRS mode: delegating to AuditCommandService");
      return commandService.logSync(context);
    }
    try {
      // Build audit entry
      AuditEntry entry = buildAuditEntry(context);

      // Persist
      auditRepository.persist(entry);

      // Update global hash cache
      lastGlobalHash = entry.getDataHash();

      // Fire event for real-time monitoring
      if (configuration.isEventBusEnabled()) {
        auditEventBus.fireAsync(new AuditEvent(entry));
      }

      // Check if notification required
      if (context.getEventType().requiresNotification()) {
        notifySecurityTeam(entry);
      }

      log.debugf(
          "Audit event logged: %s for %s/%s",
          context.getEventType(), context.getEntityType(), context.getEntityId());

      return entry.getId();

    } catch (Exception e) {
      log.error("Critical: Failed to log audit event", e);
      // Log to fallback mechanism (file, external service)
      logToFallback(context, e);
      throw new AuditException("Failed to log audit event", e);
    }
  }

  /** Log a security event (always synchronous for immediate recording) */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public UUID logSecurityEvent(AuditEventType eventType, String details) {
    if (cqrsEnabled) {
      log.debugf("CQRS mode: delegating to AuditCommandService");
      return commandService.logSecurityEvent(eventType, details);
    }
    return logSync(
        AuditContext.builder()
            .eventType(eventType)
            .entityType("SECURITY")
            .entityId(UUID.randomUUID()) // Generate unique ID for security events
            .newValue(Map.of("details", details))
            .source(AuditSource.SYSTEM)
            .build());
  }

  /** Log an export event for compliance tracking */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public UUID logExport(String exportType, Map<String, Object> parameters) {
    if (cqrsEnabled) {
      log.debugf("CQRS mode: delegating to AuditCommandService");
      return commandService.logExport(exportType, parameters);
    }
    return logSync(
        AuditContext.builder()
            .eventType(AuditEventType.DATA_EXPORT_STARTED)
            .entityType("EXPORT")
            .entityId(UUID.randomUUID())
            .newValue(
                Map.of(
                    "exportType", exportType,
                    "parameters", parameters,
                    "timestamp", Instant.now()))
            .changeReason("User initiated export: " + exportType)
            .source(AuditSource.API)
            .build());
  }

  /** Build audit entry with all required fields */
  private AuditEntry buildAuditEntry(AuditContext context) {
    Instant now = Instant.now();
    String previousHash = getPreviousHash(context.getEntityType());

    AuditEntry.Builder builder =
        AuditEntry.builder()
            .timestamp(now)
            .eventType(context.getEventType())
            .entityType(context.getEntityType())
            .entityId(context.getEntityId())
            .changeReason(context.getChangeReason())
            .userComment(context.getUserComment())
            .apiEndpoint(context.getApiEndpoint())
            .requestId(context.getRequestId())
            .previousHash(previousHash);

    // PMD Complexity Refactoring (Issue #146) - Extracted helper methods
    applyUserContext(builder, context);
    applySourceAndSession(builder, context);
    applyValues(builder, context);
    applyRequestContext(builder, context);

    // Build entry without hash
    AuditEntry entry = builder.build();

    // Calculate and set hash
    String dataHash = calculateHash(entry, previousHash);
    return entry.toBuilder().dataHash(dataHash).build();
  }

  // ============================================================================
  // PMD Complexity Refactoring (Issue #146) - Helper methods for buildAuditEntry()
  // ============================================================================

  private void applyUserContext(AuditEntry.Builder builder, AuditContext context) {
    builder.userId(
        context.getUserId() != null ? context.getUserId() : securityUtils.getCurrentUserId());
    builder.userName(
        context.getUserName() != null ? context.getUserName() : securityUtils.getCurrentUserName());
    builder.userRole(
        context.getUserRole() != null ? context.getUserRole() : securityUtils.getCurrentUserRole());
  }

  private void applySourceAndSession(AuditEntry.Builder builder, AuditContext context) {
    builder.source(context.getSource() != null ? context.getSource() : determineSource());
    builder.sessionId(
        context.getSessionId() != null
            ? context.getSessionId()
            : securityUtils.getCurrentSessionId());
  }

  private void applyValues(AuditEntry.Builder builder, AuditContext context) {
    if (context.getOldValue() != null) {
      builder.oldValue(toJson(context.getOldValue()));
    }
    if (context.getNewValue() != null) {
      builder.newValue(toJson(context.getNewValue()));
    }
  }

  private void applyRequestContext(AuditEntry.Builder builder, AuditContext context) {
    if (context.getIpAddress() == null) {
      builder.ipAddress(getClientIpAddress());
      builder.userAgent(getUserAgent());
    } else {
      builder.ipAddress(context.getIpAddress());
      builder.userAgent(context.getUserAgent());
    }
  }

  /** Capture current request/security context */
  private AuditContext captureCurrentContext(AuditContext context) {
    return context.toBuilder()
        .userId(
            context.getUserId() != null ? context.getUserId() : securityUtils.getCurrentUserId())
        .userName(
            context.getUserName() != null
                ? context.getUserName()
                : securityUtils.getCurrentUserName())
        .userRole(
            context.getUserRole() != null
                ? context.getUserRole()
                : securityUtils.getCurrentUserRole())
        .sessionId(
            context.getSessionId() != null
                ? context.getSessionId()
                : securityUtils.getCurrentSessionId())
        .ipAddress(context.getIpAddress() != null ? context.getIpAddress() : getClientIpAddress())
        .userAgent(context.getUserAgent() != null ? context.getUserAgent() : getUserAgent())
        .apiEndpoint(context.getApiEndpoint() != null ? context.getApiEndpoint() : getApiEndpoint())
        .build();
  }

  /** Determine audit source based on context */
  private AuditSource determineSource() {
    if (!httpRequestInstance.isResolvable()) {
      return AuditSource.SYSTEM;
    }

    try {
      HttpServerRequest httpRequest = httpRequestInstance.get();
      String path = httpRequest.path();
      if (path.startsWith("/api/")) {
        return AuditSource.API;
      } else if (path.startsWith("/webhook/")) {
        return AuditSource.WEBHOOK;
      } else if (path.startsWith("/admin/")) {
        return AuditSource.UI;
      }

      return AuditSource.UI;
    } catch (Exception e) {
      // HTTP context not available (e.g., in tests or async threads)
      log.debugf("HTTP context not available for source determination: %s", e.getMessage());
      return AuditSource.SYSTEM;
    }
  }

  /** Get client IP address with proxy support */
  private String getClientIpAddress() {
    if (!httpRequestInstance.isResolvable()) {
      return "SYSTEM";
    }

    try {
      HttpServerRequest httpRequest = httpRequestInstance.get();

      // Check for proxy headers
      String xForwardedFor = httpRequest.getHeader("X-Forwarded-For");
      if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
        return xForwardedFor.split(",")[0].trim();
      }

      String xRealIp = httpRequest.getHeader("X-Real-IP");
      if (xRealIp != null && !xRealIp.isEmpty()) {
        return xRealIp;
      }

      return httpRequest.remoteAddress().host();
    } catch (Exception e) {
      // HTTP context not available (e.g., in tests or async threads)
      log.debugf("HTTP context not available for IP determination: %s", e.getMessage());
      return "SYSTEM";
    }
  }

  /** Get user agent */
  private String getUserAgent() {
    if (!httpRequestInstance.isResolvable()) {
      return "SYSTEM";
    }

    try {
      return httpRequestInstance.get().getHeader("User-Agent");
    } catch (Exception e) {
      // HTTP context not available (e.g., in tests or async threads)
      log.debugf("HTTP context not available for user agent: %s", e.getMessage());
      return "SYSTEM";
    }
  }

  /** Get API endpoint */
  private String getApiEndpoint() {
    if (!httpRequestInstance.isResolvable()) {
      return null;
    }

    try {
      HttpServerRequest httpRequest = httpRequestInstance.get();
      return httpRequest.method().name() + " " + httpRequest.path();
    } catch (Exception e) {
      // HTTP context not available (e.g., in tests or async threads)
      log.debugf("HTTP context not available for API endpoint: %s", e.getMessage());
      return null;
    }
  }

  /** Convert object to JSON string */
  private String toJson(Object value) {
    if (value == null) {
      return null;
    }

    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      log.warnf(e, "Failed to serialize audit value to JSON");
      return value.toString();
    }
  }

  /** Calculate SHA-256 hash for integrity */
  private String calculateHash(AuditEntry entry, String previousHash) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      // Build hash input
      StringBuilder hashInput = new StringBuilder();
      hashInput.append(entry.getTimestamp().toEpochMilli());
      hashInput.append(entry.getEventType());
      hashInput.append(entry.getEntityType());
      hashInput.append(entry.getEntityId());
      hashInput.append(entry.getUserId());
      hashInput.append(Objects.toString(entry.getOldValue(), ""));
      hashInput.append(Objects.toString(entry.getNewValue(), ""));
      hashInput.append(Objects.toString(previousHash, ""));

      byte[] hash = digest.digest(hashInput.toString().getBytes(StandardCharsets.UTF_8));

      // Convert to hex string
      StringBuilder hexString = new StringBuilder();
      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
      }

      return hexString.toString();

    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  /** Get previous hash for global chaining */
  private String getPreviousHash(String entityType) {
    // Check global cache first
    if (lastGlobalHash != null) {
      return lastGlobalHash;
    }

    // Load from database (global chain)
    return auditRepository.getLastHash().orElse(null);
  }

  /** Notify security team for critical events */
  private void notifySecurityTeam(AuditEntry entry) {
    // Implementation depends on notification service
    log.warnf(
        "Security notification required for event: %s - %s", entry.getEventType(), entry.getId());
  }

  /** Fallback logging mechanism */
  private void logToFallback(AuditContext context, Exception error) {
    // Log to file or external service as fallback
    log.errorf(
        "AUDIT_FALLBACK: %s %s %s - Error: %s",
        context.getEventType(), context.getEntityType(), context.getEntityId(), error.getMessage());
  }

  /** Handle audit events from CDI event bus */
  public void onApplicationEvent(@Observes AuditableApplicationEvent event) {
    if (cqrsEnabled) {
      commandService.onApplicationEvent(event);
      return;
    }
    logAsync(event.toAuditContext());
  }

  // =====================================
  // QUERY OPERATIONS (NEU für CQRS)
  // =====================================

  /** Find audit entries by entity Delegiert an AuditQueryService */
  public List<AuditEntry> findByEntity(String entityType, UUID entityId) {
    if (cqrsEnabled) {
      return queryService.findByEntity(entityType, entityId);
    }
    // Legacy: direkt vom Repository
    return auditRepository.findByEntity(entityType, entityId);
  }

  /** Find audit entries by entity with pagination Delegiert an AuditQueryService */
  public List<AuditEntry> findByEntity(String entityType, UUID entityId, int page, int size) {
    if (cqrsEnabled) {
      return queryService.findByEntity(entityType, entityId, page, size);
    }
    // Legacy: direkt vom Repository
    return auditRepository.findByEntity(entityType, entityId, page, size);
  }

  /** Get dashboard metrics for Admin UI Delegiert an AuditQueryService */
  public AuditRepository.DashboardMetrics getDashboardMetrics() {
    if (cqrsEnabled) {
      return queryService.getDashboardMetrics();
    }
    // Legacy: direkt vom Repository
    return auditRepository.getDashboardMetrics();
  }

  /** Get compliance alerts Delegiert an AuditQueryService */
  public List<ComplianceAlertDto> getComplianceAlerts() {
    if (cqrsEnabled) {
      return queryService.getComplianceAlerts();
    }
    // Legacy: direkt vom Repository
    return auditRepository.getComplianceAlerts();
  }

  /** Find audit entries by filters for export Delegiert an AuditQueryService */
  public List<AuditEntry> findByFilters(ExportRequest request) {
    if (cqrsEnabled) {
      return queryService.findByFilters(request);
    }
    // Legacy: direkt vom Repository
    return auditRepository.findByFilters(request);
  }

  /** Stream audit entries for export (memory-efficient) Delegiert an AuditQueryService */
  public Stream<AuditEntry> streamForExport(AuditRepository.AuditSearchCriteria criteria) {
    if (cqrsEnabled) {
      return queryService.streamForExport(criteria);
    }
    // Legacy: direkt vom Repository
    return auditRepository.streamForExport(criteria);
  }
}

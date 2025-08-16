package de.freshplan.domain.audit.service.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.entity.AuditSource;
import de.freshplan.domain.audit.repository.AuditRepository;
import de.freshplan.domain.audit.service.AuditService;
import de.freshplan.domain.audit.service.AuditableApplicationEvent;
import de.freshplan.domain.audit.service.dto.AuditContext;
import de.freshplan.shared.util.SecurityUtils;
import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.PostConstruct;
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
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jboss.logging.Logger;

/**
 * Audit Command Service - CQRS Write Side
 *
 * <p>Behandelt alle schreibenden Operationen für Audit-Einträge: - Async und Sync Logging -
 * Security Events - Export Tracking - Hash-Chain Management - Event Processing
 *
 * <p>WICHTIG: Dieser Service ist eine EXAKTE KOPIE der Command-Methoden aus AuditService für 100%
 * Kompatibilität.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class AuditCommandService {

  private static final Logger log = Logger.getLogger(AuditCommandService.class);

  @Inject AuditRepository auditRepository;

  @Inject ObjectMapper objectMapper;

  @Inject SecurityUtils securityUtils;

  @Inject Event<AuditService.AuditEvent> auditEventBus;

  @Inject AuditService.AuditConfiguration configuration;

  @Inject Instance<HttpServerRequest> httpRequestInstance;

  private ExecutorService auditExecutor;
  private volatile String lastGlobalHash = null;

  @PostConstruct
  void init() {
    // Initialize async executor for audit logging (EXAKTE KOPIE Zeile 62-76)
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
        "Audit Command Service initialized with %d async threads",
        configuration.getAsyncThreadPoolSize());
  }

  /** Log an audit event asynchronously EXAKTE KOPIE von AuditService.logAsync() Zeile 79-96 */
  public CompletableFuture<UUID> logAsync(
      AuditEventType eventType,
      String entityType,
      UUID entityId,
      Object oldValue,
      Object newValue,
      String reason) {

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

  /**
   * Log an audit event with full context asynchronously EXAKTE KOPIE von
   * AuditService.logAsync(AuditContext) Zeile 99-114
   */
  public CompletableFuture<UUID> logAsync(AuditContext context) {
    // Capture request context before async execution
    final var capturedContext = captureCurrentContext(context);

    return CompletableFuture.supplyAsync(
        () -> {
          try {
            return logSync(capturedContext);
          } catch (Exception e) {
            log.errorf(e, "Failed to log audit event: %s", context.getEventType());
            // Re-throw to propagate through CompletableFuture
            throw new AuditService.AuditException("Failed to log audit event", e);
          }
        },
        auditExecutor);
  }

  /**
   * Log an audit event synchronously (use sparingly) EXAKTE KOPIE von AuditService.logSync() Zeile
   * 117-152
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  @jakarta.enterprise.context.control.ActivateRequestContext
  public UUID logSync(AuditContext context) {
    try {
      // Build audit entry
      AuditEntry entry = buildAuditEntry(context);

      // Persist
      auditRepository.persist(entry);

      // Update global hash cache
      lastGlobalHash = entry.getDataHash();

      // Fire event for real-time monitoring
      if (configuration.isEventBusEnabled()) {
        auditEventBus.fireAsync(new AuditService.AuditEvent(entry));
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
      throw new AuditService.AuditException("Failed to log audit event", e);
    }
  }

  /**
   * Log a security event (always synchronous for immediate recording) EXAKTE KOPIE von
   * AuditService.logSecurityEvent() Zeile 155-165
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public UUID logSecurityEvent(AuditEventType eventType, String details) {
    return logSync(
        AuditContext.builder()
            .eventType(eventType)
            .entityType("SECURITY")
            .entityId(UUID.randomUUID()) // Generate unique ID for security events
            .newValue(Map.of("details", details))
            .source(AuditSource.SYSTEM)
            .build());
  }

  /**
   * Log an export event for compliance tracking EXAKTE KOPIE von AuditService.logExport() Zeile
   * 168-183
   */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public UUID logExport(String exportType, Map<String, Object> parameters) {
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

  /**
   * Handle audit events from CDI event bus EXAKTE KOPIE von AuditService.onApplicationEvent() Zeile
   * 425-427
   */
  public void onApplicationEvent(@Observes AuditableApplicationEvent event) {
    logAsync(event.toAuditContext());
  }

  // =====================================
  // PRIVATE HELPER METHODS (EXAKTE KOPIEN)
  // =====================================

  /**
   * Build audit entry with all required fields EXAKTE KOPIE von AuditService.buildAuditEntry()
   * Zeile 186-242
   */
  private AuditEntry buildAuditEntry(AuditContext context) {
    Instant now = Instant.now();
    String previousHash = getPreviousHash(context.getEntityType());

    AuditEntry.Builder builder =
        AuditEntry.builder()
            .timestamp(now)
            .eventType(context.getEventType())
            .entityType(context.getEntityType())
            .entityId(context.getEntityId())
            .userId(
                context.getUserId() != null
                    ? context.getUserId()
                    : securityUtils.getCurrentUserId())
            .userName(
                context.getUserName() != null
                    ? context.getUserName()
                    : securityUtils.getCurrentUserName())
            .userRole(
                context.getUserRole() != null
                    ? context.getUserRole()
                    : securityUtils.getCurrentUserRole())
            .changeReason(context.getChangeReason())
            .userComment(context.getUserComment())
            .source(context.getSource() != null ? context.getSource() : determineSource())
            .apiEndpoint(context.getApiEndpoint())
            .requestId(context.getRequestId())
            .sessionId(
                context.getSessionId() != null
                    ? context.getSessionId()
                    : securityUtils.getCurrentSessionId())
            .previousHash(previousHash);

    // Serialize values to JSON
    if (context.getOldValue() != null) {
      builder.oldValue(toJson(context.getOldValue()));
    }
    if (context.getNewValue() != null) {
      builder.newValue(toJson(context.getNewValue()));
    }

    // Add request context if available
    if (context.getIpAddress() == null) {
      builder.ipAddress(getClientIpAddress());
      builder.userAgent(getUserAgent());
    } else {
      builder.ipAddress(context.getIpAddress());
      builder.userAgent(context.getUserAgent());
    }

    // Build entry without hash
    AuditEntry entry = builder.build();

    // Calculate and set hash
    String dataHash = calculateHash(entry, previousHash);
    return entry.toBuilder().dataHash(dataHash).build();
  }

  /**
   * Capture current request/security context EXAKTE KOPIE von AuditService.captureCurrentContext()
   * Zeile 245-265
   */
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

  /**
   * Determine audit source based on context EXAKTE KOPIE von AuditService.determineSource() Zeile
   * 268-290
   */
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

  /**
   * Get client IP address with proxy support EXAKTE KOPIE von AuditService.getClientIpAddress()
   * Zeile 293-318
   */
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

  /** Get user agent EXAKTE KOPIE von AuditService.getUserAgent() Zeile 321-333 */
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

  /** Get API endpoint EXAKTE KOPIE von AuditService.getApiEndpoint() Zeile 336-349 */
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

  /** Convert object to JSON string EXAKTE KOPIE von AuditService.toJson() Zeile 352-363 */
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

  /**
   * Calculate SHA-256 hash for integrity EXAKTE KOPIE von AuditService.calculateHash() Zeile
   * 366-396
   */
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

  /**
   * Get previous hash for global chaining EXAKTE KOPIE von AuditService.getPreviousHash() Zeile
   * 399-407
   */
  private String getPreviousHash(String entityType) {
    // Check global cache first
    if (lastGlobalHash != null) {
      return lastGlobalHash;
    }

    // Load from database (global chain)
    return auditRepository.getLastHash().orElse(null);
  }

  /**
   * Notify security team for critical events EXAKTE KOPIE von AuditService.notifySecurityTeam()
   * Zeile 410-414
   */
  private void notifySecurityTeam(AuditEntry entry) {
    // Implementation depends on notification service
    log.warnf(
        "Security notification required for event: %s - %s", entry.getEventType(), entry.getId());
  }

  /** Fallback logging mechanism EXAKTE KOPIE von AuditService.logToFallback() Zeile 417-422 */
  private void logToFallback(AuditContext context, Exception error) {
    // Log to file or external service as fallback
    log.errorf(
        "AUDIT_FALLBACK: %s %s %s - Error: %s",
        context.getEventType(), context.getEntityType(), context.getEntityId(), error.getMessage());
  }
}

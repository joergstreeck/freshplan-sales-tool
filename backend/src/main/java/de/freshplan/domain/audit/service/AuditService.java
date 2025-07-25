package de.freshplan.domain.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.entity.AuditSource;
import de.freshplan.domain.audit.repository.AuditRepository;
import de.freshplan.domain.audit.service.dto.AuditContext;
import de.freshplan.shared.util.SecurityUtils;
import io.quarkus.runtime.Startup;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jboss.logging.Logger;

/**
 * Enterprise-grade Audit Service with async processing and integrity verification
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

  private ExecutorService auditExecutor;
  private final Map<String, String> lastHashCache = new ConcurrentHashMap<>();

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

  /** Log an audit event asynchronously */
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

  /** Log an audit event with full context asynchronously */
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
            throw new AuditException("Failed to log audit event", e);
          }
        },
        auditExecutor);
  }

  /** Log an audit event synchronously (use sparingly) */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public UUID logSync(AuditContext context) {
    try {
      // Build audit entry
      AuditEntry entry = buildAuditEntry(context);

      // Persist
      auditRepository.persist(entry);

      // Update hash cache
      lastHashCache.put(context.getEntityType(), entry.getDataHash());

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
    return logSync(
        AuditContext.builder()
            .eventType(eventType)
            .entityType("SECURITY")
            .entityId(UUID.randomUUID()) // Generate unique ID for security events
            .newValue(Map.of("details", details))
            .source(AuditSource.SYSTEM)
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

  /** Get previous hash for chaining */
  private String getPreviousHash(String entityType) {
    // Check cache first
    String cached = lastHashCache.get(entityType);
    if (cached != null) {
      return cached;
    }

    // Load from database
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
    logAsync(event.toAuditContext());
  }

  /** Audit service configuration */
  @ApplicationScoped
  public static class AuditConfiguration {
    // These would typically come from application.properties
    public int getAsyncThreadPoolSize() {
      return 5;
    }

    public boolean isEventBusEnabled() {
      return true;
    }
  }

  /** Audit event for CDI event bus */
  public static class AuditEvent {
    private final AuditEntry entry;

    public AuditEvent(AuditEntry entry) {
      this.entry = entry;
    }

    public AuditEntry getEntry() {
      return entry;
    }
  }

  /** Exception for audit failures */
  public static class AuditException extends RuntimeException {
    public AuditException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}

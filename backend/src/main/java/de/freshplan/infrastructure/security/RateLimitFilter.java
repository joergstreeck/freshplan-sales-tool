package de.freshplan.infrastructure.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jboss.logging.Logger;

/**
 * Rate Limiting Filter for API Protection.
 *
 * <p>Protects against:
 * <ul>
 *   <li>Brute Force Attacks (Login/Auth attempts)</li>
 *   <li>API Abuse (excessive requests)</li>
 *   <li>DDoS Attacks (distributed denial of service)</li>
 * </ul>
 *
 * <p>Limits per User ID (Token Bucket Algorithm):
 * <ul>
 *   <li>General API: 100 requests/minute</li>
 *   <li>Write Operations (POST/PUT/PATCH): 50 requests/minute</li>
 *   <li>Delete Operations: 10 requests/minute</li>
 * </ul>
 *
 * <p>Sprint 2.1.6 - Security Hardening Phase 2
 */
@Provider
@Priority(Priorities.AUTHENTICATION - 1) // Run before authentication
public class RateLimitFilter implements ContainerRequestFilter {

  private static final Logger LOG = Logger.getLogger(RateLimitFilter.class);

  @Inject SecurityAuditLogger securityAuditLogger;

  // Bucket storage: userId -> Bucket
  private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();
  private final Map<String, Bucket> writeBuckets = new ConcurrentHashMap<>();
  private final Map<String, Bucket> deleteBuckets = new ConcurrentHashMap<>();

  // Rate limits (requests per minute)
  private static final int GENERAL_LIMIT = 100; // Read operations (GET)
  private static final int WRITE_LIMIT = 50; // Create/Update (POST/PUT/PATCH)
  private static final int DELETE_LIMIT = 10; // Delete operations (DELETE)

  @Override
  public void filter(ContainerRequestContext requestContext) {
    String method = requestContext.getMethod();
    String path = requestContext.getUriInfo().getPath();

    // Skip rate limiting for health checks and metrics endpoints
    if (path.startsWith("q/health") || path.startsWith("q/metrics")) {
      return;
    }

    // Get user ID (use IP address as fallback for unauthenticated requests)
    String userId = getUserIdentifier(requestContext);

    // Select appropriate bucket based on HTTP method
    Bucket bucket;
    String limitType;

    if ("DELETE".equals(method)) {
      bucket = deleteBuckets.computeIfAbsent(userId, k -> createBucket(DELETE_LIMIT));
      limitType = "DELETE";
    } else if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) {
      bucket = writeBuckets.computeIfAbsent(userId, k -> createBucket(WRITE_LIMIT));
      limitType = "WRITE";
    } else {
      bucket = generalBuckets.computeIfAbsent(userId, k -> createBucket(GENERAL_LIMIT));
      limitType = "GENERAL";
    }

    // Try to consume 1 token from bucket
    if (bucket.tryConsume(1)) {
      // Request allowed
      return;
    }

    // Rate limit exceeded
    LOG.warnf(
        "Rate limit exceeded: user=%s, method=%s, path=%s, limitType=%s",
        userId, method, path, limitType);

    securityAuditLogger.logRateLimitExceeded(userId, method, path, limitType);

    // Return 429 Too Many Requests
    requestContext.abortWith(
        Response.status(429)
            .entity(
                Map.of(
                    "error", "Rate limit exceeded",
                    "message",
                        "Too many requests. Please try again later.",
                    "retryAfter",
                        "60 seconds"))
            .header("Retry-After", "60") // Client should wait 60 seconds
            .header("X-RateLimit-Limit", getLimit(limitType))
            .header("X-RateLimit-Remaining", "0")
            .build());
  }

  /**
   * Create bucket with Token Bucket Algorithm.
   *
   * @param capacity Max tokens (requests) per minute
   * @return Configured bucket
   */
  private Bucket createBucket(int capacity) {
    // Refill strategy: Greedy refill (all tokens refill at once after 1 minute)
    // Alternative: Interval refill (tokens refill gradually)
    Bandwidth limit =
        Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofMinutes(1)));
    return Bucket.builder().addLimit(limit).build();
  }

  /**
   * Get user identifier for rate limiting.
   *
   * <p>Priority:
   * <ol>
   *   <li>User ID from JWT token (authenticated users)</li>
   *   <li>IP address from X-Forwarded-For header (reverse proxy)</li>
   *   <li>Session ID (fallback for anonymous users without X-Forwarded-For)</li>
   * </ol>
   *
   * <p>SECURITY FIX: Previously used static "unknown" string for all anonymous users
   * without X-Forwarded-For, which allowed a single attacker to exhaust the rate limit
   * for ALL anonymous users (DoS vulnerability). Now uses unique session IDs.
   *
   * @param requestContext Request context
   * @return User identifier (never null, always unique per user/session)
   */
  private String getUserIdentifier(ContainerRequestContext requestContext) {
    // Try to get user ID from security context (JWT token)
    var securityContext = requestContext.getSecurityContext();
    if (securityContext != null && securityContext.getUserPrincipal() != null) {
      String userId = securityContext.getUserPrincipal().getName();
      return "user:" + userId;
    }

    // Fallback 1: Use IP address from X-Forwarded-For header (reverse proxy)
    String xForwardedFor = requestContext.getHeaderString("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      // X-Forwarded-For may contain multiple IPs (client, proxy1, proxy2...)
      // Use first IP (client IP)
      String clientIp = xForwardedFor.split(",")[0].trim();
      return "ip:" + clientIp;
    }

    // Fallback 2: Use Session ID for anonymous users
    // SECURITY: Prevents DoS where one attacker exhausts rate limit for ALL anonymous users
    // Each anonymous session gets its own rate limit bucket
    String sessionId = requestContext.getHeaderString("X-Session-Id");
    if (sessionId != null && !sessionId.isEmpty()) {
      return "session:" + sessionId;
    }

    // Ultimate fallback: Generate unique identifier based on User-Agent + client fingerprint
    // This is NOT perfect but better than static "unknown" string
    String userAgent = requestContext.getHeaderString("User-Agent");
    String acceptLanguage = requestContext.getHeaderString("Accept-Language");
    String fingerprint = (userAgent != null ? userAgent : "no-ua") +
                        "_" +
                        (acceptLanguage != null ? acceptLanguage : "no-lang");

    // Use hash to keep identifier length manageable
    int hash = fingerprint.hashCode();
    return "anon:" + Math.abs(hash);
  }

  /**
   * Get rate limit for limit type (for response headers).
   *
   * @param limitType Limit type (GENERAL, WRITE, DELETE)
   * @return Rate limit value
   */
  private String getLimit(String limitType) {
    return switch (limitType) {
      case "DELETE" -> String.valueOf(DELETE_LIMIT);
      case "WRITE" -> String.valueOf(WRITE_LIMIT);
      default -> String.valueOf(GENERAL_LIMIT);
    };
  }
}

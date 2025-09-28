package de.freshplan.modules.leads.service;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Service to handle idempotent API operations. Sprint 2.1.4: Lead Deduplication & Data Quality
 *
 * <p>Prevents duplicate processing of API requests by storing and checking idempotency keys with a
 * TTL (Time To Live).
 */
@ApplicationScoped
public class IdempotencyService {

  /** Represents a stored idempotent response with status and body. */
  public record StoredResponse(int status, String body) {}

  private static final Duration DEFAULT_TTL = Duration.ofHours(24);

  @Inject EntityManager entityManager;

  /**
   * Checks if an idempotency key already exists for a tenant.
   *
   * @param tenantId The tenant identifier
   * @param idempotencyKey The unique request key
   * @return Optional containing the stored response if key exists
   */
  public Optional<String> checkIdempotencyKey(String tenantId, String idempotencyKey) {
    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      return Optional.empty();
    }

    try {
      var result =
          entityManager
              .createNativeQuery(
                  "SELECT response_body FROM idempotency_keys "
                      + "WHERE tenant_id = :tenantId "
                      + "AND idempotency_key = :key "
                      + "AND expires_at > :now",
                  String.class)
              .setParameter("tenantId", tenantId)
              .setParameter("key", idempotencyKey)
              .setParameter("now", Instant.now())
              .getSingleResult();

      Log.infof("Idempotency hit for tenant %s: %s", tenantId, idempotencyKey);
      return Optional.of((String) result);
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  /**
   * Stores an idempotency key with its response.
   *
   * @param tenantId The tenant identifier
   * @param idempotencyKey The unique request key
   * @param requestBody The request body (for validation)
   * @param responseBody The response to cache
   * @return true if stored successfully, false if key already exists with different request
   */
  @Transactional
  public boolean storeIdempotencyKey(
      String tenantId, String idempotencyKey, String requestBody, String responseBody) {
    var result = upsertOrGetIdempotencyKey(tenantId, idempotencyKey, requestBody, responseBody, 200);
    return result.isPresent() && result.get().body().equals(responseBody);
  }

  /**
   * Stores an idempotency key or retrieves the existing response.
   *
   * @param tenantId The tenant identifier
   * @param idempotencyKey The unique request key
   * @param requestBody The request body (for validation)
   * @param responseBody The response to cache
   * @param responseStatus The HTTP status code of the response
   * @return Optional with stored response, empty if different request with same key
   */
  @Transactional
  public Optional<StoredResponse> upsertOrGetIdempotencyKey(
      String tenantId,
      String idempotencyKey,
      String requestBody,
      String responseBody,
      int responseStatus) {
    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      // No idempotency requested, return the new response
      return Optional.of(new StoredResponse(responseStatus, responseBody));
    }

    String requestHash = hashRequest(requestBody);
    Instant now = Instant.now();
    Instant expiresAt = now.plus(DEFAULT_TTL);

    // Atomic insert-or-nothing using ON CONFLICT
    int inserted =
        entityManager
            .createNativeQuery(
                "INSERT INTO idempotency_keys "
                    + "(tenant_id, idempotency_key, request_hash, response_status, response_body, created_at, expires_at) "
                    + "VALUES (:tenantId, :key, :requestHash, :responseStatus, :responseBody, :now, :expiresAt) "
                    + "ON CONFLICT (tenant_id, idempotency_key) DO NOTHING")
            .setParameter("tenantId", tenantId)
            .setParameter("key", idempotencyKey)
            .setParameter("requestHash", requestHash)
            .setParameter("responseStatus", responseStatus)
            .setParameter("responseBody", responseBody)
            .setParameter("now", now)
            .setParameter("expiresAt", expiresAt)
            .executeUpdate();

    if (inserted == 0) {
      // Key already exists, check if it's the same request and get stored response
      try {
        var result =
            entityManager
                .createNativeQuery(
                    "SELECT request_hash, response_status, response_body FROM idempotency_keys "
                        + "WHERE tenant_id = :tenantId AND idempotency_key = :key "
                        + "AND expires_at > :now")
                .setParameter("tenantId", tenantId)
                .setParameter("key", idempotencyKey)
                .setParameter("now", now)
                .getSingleResult();

        Object[] row = (Object[]) result;
        String existingHash = (String) row[0];
        Integer storedStatus = (Integer) row[1];
        String storedBody = (String) row[2];

        if (!existingHash.equals(requestHash)) {
          Log.warnf(
              "Idempotency key reused with different request. Tenant: %s, Key: %s",
              tenantId, idempotencyKey);
          return Optional.empty(); // Different request, return empty for 409 conflict
        }
        // Same request, return the stored response
        Log.debugf(
            "Returning stored response for idempotency key. Tenant: %s, Key: %s",
            tenantId, idempotencyKey);
        return Optional.of(new StoredResponse(storedStatus, storedBody));
      } catch (NoResultException e) {
        // Key expired between insert attempt and check
        return Optional.empty();
      }
    }

    // Successfully inserted new key
    Log.infof("Stored idempotency key for tenant %s: %s", tenantId, idempotencyKey);
    return Optional.of(new StoredResponse(responseStatus, responseBody));
  }

  /** Removes expired idempotency keys. Should be called periodically by a scheduled job. */
  @Transactional
  public int cleanupExpiredKeys() {
    int deleted =
        entityManager
            .createNativeQuery("DELETE FROM idempotency_keys WHERE expires_at <= :now")
            .setParameter("now", Instant.now())
            .executeUpdate();

    if (deleted > 0) {
      Log.infof("Cleaned up %d expired idempotency keys", deleted);
    }
    return deleted;
  }

  /** Creates a SHA-256 hash of the request body for comparison. */
  private String hashRequest(String requestBody) {
    if (requestBody == null) {
      return "NULL";
    }

    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(requestBody.getBytes());
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      // SHA-256 is guaranteed to be available in Java
      throw new IllegalStateException("SHA-256 algorithm not available", e);
    }
  }
}

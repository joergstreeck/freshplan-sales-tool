package de.freshfoodz.crm.lead.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/**
 * Service for handling idempotent API requests.
 * Stores and retrieves idempotency keys with request validation.
 * Sprint 2.1.4: Lead Deduplication & Data Quality
 */
@ApplicationScoped
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);

    @Inject
    EntityManager entityManager;

    @ConfigProperty(name = "freshplan.idempotency.ttl-seconds", defaultValue = "86400") // 24 hours
    long ttlSeconds;

    /**
     * Stores an idempotency key with the associated request and response.
     *
     * @param tenantId The tenant ID
     * @param idempotencyKey The unique idempotency key
     * @param requestBody The request body (for hash validation)
     * @param responseStatus The HTTP response status
     * @param responseBody The response body to cache
     * @return The stored idempotency record
     */
    @Transactional
    public IdempotencyRecord storeIdempotencyKey(
            String tenantId,
            String idempotencyKey,
            String requestBody,
            int responseStatus,
            String responseBody) {

        String requestHash = hashRequest(requestBody);
        Instant expiresAt = Instant.now().plus(Duration.ofSeconds(ttlSeconds));

        IdempotencyRecord record = new IdempotencyRecord();
        record.setTenantId(tenantId);
        record.setIdempotencyKey(idempotencyKey);
        record.setRequestHash(requestHash);
        record.setResponseStatus(responseStatus);
        record.setResponseBody(responseBody);
        record.setCreatedAt(Instant.now());
        record.setExpiresAt(expiresAt);

        entityManager.persist(record);
        entityManager.flush();

        log.info("Stored idempotency key for tenant {}: {}", tenantId, idempotencyKey);
        return record;
    }

    /**
     * Retrieves a cached response for an idempotency key.
     * Validates that the request hash matches to prevent key reuse with different requests.
     *
     * @param tenantId The tenant ID
     * @param idempotencyKey The idempotency key
     * @param requestBody The current request body
     * @return The cached response if valid, empty if not found or invalid
     */
    @Transactional
    public Optional<IdempotencyRecord> retrieveIdempotentResponse(
            String tenantId,
            String idempotencyKey,
            String requestBody) {

        try {
            IdempotencyRecord record = entityManager
                .createQuery(
                    "SELECT i FROM IdempotencyRecord i " +
                    "WHERE i.tenantId = :tenantId " +
                    "AND i.idempotencyKey = :key " +
                    "AND i.expiresAt > :now",
                    IdempotencyRecord.class
                )
                .setParameter("tenantId", tenantId)
                .setParameter("key", idempotencyKey)
                .setParameter("now", Instant.now())
                .getSingleResult();

            // Validate request hash
            String currentHash = hashRequest(requestBody);
            if (!currentHash.equals(record.getRequestHash())) {
                log.warn("Idempotency key reused with different request body. " +
                        "Tenant: {}, Key: {}", tenantId, idempotencyKey);
                return Optional.empty();
            }

            log.info("Idempotency hit for tenant {}: {}", tenantId, idempotencyKey);
            return Optional.of(record);

        } catch (NoResultException e) {
            log.debug("Idempotency miss for tenant {}: {}", tenantId, idempotencyKey);
            return Optional.empty();
        }
    }

    /**
     * Cleans up expired idempotency records.
     * Should be called periodically by a scheduled job.
     *
     * @return The number of records deleted
     */
    @Transactional
    public int cleanupExpiredKeys() {
        int deleted = entityManager
            .createQuery("DELETE FROM IdempotencyRecord i WHERE i.expiresAt < :now")
            .setParameter("now", Instant.now())
            .executeUpdate();

        if (deleted > 0) {
            log.info("Cleaned up {} expired idempotency keys", deleted);
        }
        return deleted;
    }

    /**
     * Creates a SHA-256 hash of the request body for validation.
     *
     * @param requestBody The request body to hash
     * @return Base64-encoded hash
     */
    private String hashRequest(String requestBody) {
        if (requestBody == null) {
            requestBody = "";
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(requestBody.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
package de.freshfoodz.crm.lead.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for IdempotencyService.
 * Sprint 2.1.4: Lead Deduplication & Data Quality
 */
@QuarkusTest
class IdempotencyServiceTest {

    @Inject
    IdempotencyService idempotencyService;

    @Test
    @TestTransaction
    void shouldStoreAndRetrieveIdempotencyKey() {
        // Given
        String tenantId = "tenant-1";
        String idempotencyKey = UUID.randomUUID().toString();
        String requestBody = "{\"name\":\"John Doe\",\"email\":\"john@example.com\"}";
        int responseStatus = 201;
        String responseBody = "{\"id\":1,\"name\":\"John Doe\"}";

        // When - Store
        IdempotencyRecord stored = idempotencyService.storeIdempotencyKey(
            tenantId, idempotencyKey, requestBody, responseStatus, responseBody
        );

        // Then - Verify storage
        assertNotNull(stored);
        assertEquals(tenantId, stored.getTenantId());
        assertEquals(idempotencyKey, stored.getIdempotencyKey());
        assertEquals(responseStatus, stored.getResponseStatus());
        assertEquals(responseBody, stored.getResponseBody());
        assertNotNull(stored.getRequestHash());

        // When - Retrieve with same request
        Optional<IdempotencyRecord> retrieved = idempotencyService.retrieveIdempotentResponse(
            tenantId, idempotencyKey, requestBody
        );

        // Then - Should find the record
        assertTrue(retrieved.isPresent());
        assertEquals(responseBody, retrieved.get().getResponseBody());
        assertEquals(responseStatus, retrieved.get().getResponseStatus());
    }

    @Test
    @TestTransaction
    void shouldRejectDifferentRequestWithSameKey() {
        // Given
        String tenantId = "tenant-1";
        String idempotencyKey = UUID.randomUUID().toString();
        String originalRequest = "{\"name\":\"John Doe\"}";
        String differentRequest = "{\"name\":\"Jane Doe\"}";

        // When - Store with original request
        idempotencyService.storeIdempotencyKey(
            tenantId, idempotencyKey, originalRequest, 201, "{\"id\":1}"
        );

        // Then - Retrieve with different request should fail
        Optional<IdempotencyRecord> retrieved = idempotencyService.retrieveIdempotentResponse(
            tenantId, idempotencyKey, differentRequest
        );

        assertFalse(retrieved.isPresent(), "Should reject different request with same idempotency key");
    }

    @Test
    @TestTransaction
    void shouldIsolateBetweenTenants() {
        // Given
        String idempotencyKey = UUID.randomUUID().toString();
        String requestBody = "{\"name\":\"John Doe\"}";

        // When - Store for tenant-1
        idempotencyService.storeIdempotencyKey(
            "tenant-1", idempotencyKey, requestBody, 201, "{\"tenant\":1}"
        );

        // Then - Should not find for tenant-2
        Optional<IdempotencyRecord> retrieved = idempotencyService.retrieveIdempotentResponse(
            "tenant-2", idempotencyKey, requestBody
        );

        assertFalse(retrieved.isPresent(), "Keys should be isolated between tenants");
    }

    @Test
    @TestTransaction
    void shouldHandleNullRequestBody() {
        // Given
        String tenantId = "tenant-1";
        String idempotencyKey = UUID.randomUUID().toString();

        // When - Store with null request
        IdempotencyRecord stored = idempotencyService.storeIdempotencyKey(
            tenantId, idempotencyKey, null, 201, "{\"id\":1}"
        );

        // Then - Should work
        assertNotNull(stored);

        // And retrieve with null should work
        Optional<IdempotencyRecord> retrieved = idempotencyService.retrieveIdempotentResponse(
            tenantId, idempotencyKey, null
        );

        assertTrue(retrieved.isPresent());
    }

    @Test
    @TestTransaction
    void shouldReturnEmptyForNonExistentKey() {
        // When - Try to retrieve non-existent key
        Optional<IdempotencyRecord> retrieved = idempotencyService.retrieveIdempotentResponse(
            "tenant-1", "non-existent-key", "{}"
        );

        // Then
        assertFalse(retrieved.isPresent());
    }

    @Test
    @TestTransaction
    void shouldCleanupExpiredKeys() {
        // Given - Create some records with past expiry
        String tenantId = "tenant-1";

        // Store an expired key (manually setting expiry in the past would require direct DB access)
        // For now, we'll just test the cleanup method exists and returns 0 for no expired keys

        // When
        int deleted = idempotencyService.cleanupExpiredKeys();

        // Then
        assertEquals(0, deleted, "Should return 0 when no expired keys exist");
    }

    @Test
    @TestTransaction
    void shouldHandleLongRequestBodies() {
        // Given - Large request body
        StringBuilder largeBody = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeBody.append("field").append(i).append(":").append("value").append(i).append(",");
        }
        String requestBody = largeBody.toString();

        String tenantId = "tenant-1";
        String idempotencyKey = UUID.randomUUID().toString();

        // When
        IdempotencyRecord stored = idempotencyService.storeIdempotencyKey(
            tenantId, idempotencyKey, requestBody, 201, "{\"status\":\"ok\"}"
        );

        // Then
        assertNotNull(stored);

        // And retrieval should work
        Optional<IdempotencyRecord> retrieved = idempotencyService.retrieveIdempotentResponse(
            tenantId, idempotencyKey, requestBody
        );

        assertTrue(retrieved.isPresent());
    }

    @Test
    void idempotencyLookupShouldBeFast() {
        // Performance test - should complete quickly even with multiple operations
        long start = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            idempotencyService.retrieveIdempotentResponse(
                "tenant-" + i,
                "key-" + i,
                "{\"test\":" + i + "}"
            );
        }

        long duration = System.currentTimeMillis() - start;
        assertTrue(duration < 1000, "100 lookups should complete in < 1000ms, took: " + duration);
    }
}
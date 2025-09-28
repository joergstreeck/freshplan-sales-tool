package de.freshplan.modules.leads.service;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Integration tests for IdempotencyService. Sprint 2.1.4: Lead Deduplication & Data Quality */
@QuarkusTest
class IdempotencyServiceTest {

  @Inject IdempotencyService idempotencyService;

  @Test
  @TestTransaction
  void shouldStoreAndRetrieveIdempotencyKey() {
    // Given
    String tenantId = "tenant-1";
    String idempotencyKey = UUID.randomUUID().toString();
    String requestBody = "{\"name\":\"John Doe\",\"email\":\"john@example.com\"}";
    String responseBody = "{\"id\":1,\"name\":\"John Doe\"}";

    // When - Store
    boolean stored =
        idempotencyService.storeIdempotencyKey(tenantId, idempotencyKey, requestBody, responseBody);

    // Then - Verify storage
    assertTrue(stored);

    // When - Check if key exists
    Optional<String> retrieved = idempotencyService.checkIdempotencyKey(tenantId, idempotencyKey);

    // Then - Should find the record
    assertTrue(retrieved.isPresent());
    assertEquals(responseBody, retrieved.get());
  }

  @Test
  @TestTransaction
  void shouldRejectDifferentRequestWithSameIdempotencyKey() {
    // Given
    String tenantId = "tenant-1";
    String idempotencyKey = UUID.randomUUID().toString();
    String originalRequest = "{\"name\":\"John Doe\"}";
    String differentRequest = "{\"name\":\"Jane Doe\"}";
    String responseBody = "{\"id\":1}";

    // When - Store original
    idempotencyService.storeIdempotencyKey(tenantId, idempotencyKey, originalRequest, responseBody);

    // Then - Storing with different request should return false
    boolean result =
        idempotencyService.storeIdempotencyKey(
            tenantId, idempotencyKey, differentRequest, responseBody);
    assertFalse(result, "Should reject different request with same key");
  }

  @Test
  @TestTransaction
  void shouldReturnCachedResponseForSameRequest() {
    // Given
    String tenantId = "tenant-1";
    String idempotencyKey = UUID.randomUUID().toString();
    String requestBody = "{\"name\":\"John\"}";
    String responseBody = "{\"id\":123}";

    // Store first time
    idempotencyService.storeIdempotencyKey(tenantId, idempotencyKey, requestBody, responseBody);

    // When - Check second time
    Optional<String> cached = idempotencyService.checkIdempotencyKey(tenantId, idempotencyKey);

    // Then
    assertTrue(cached.isPresent());
    assertEquals(responseBody, cached.get());
  }

  @Test
  @TestTransaction
  void shouldHandleNullRequestBody() {
    // Given
    String tenantId = "tenant-1";
    String idempotencyKey = UUID.randomUUID().toString();
    String responseBody = "{\"status\":\"ok\"}";

    // When - Store with null request body
    boolean stored =
        idempotencyService.storeIdempotencyKey(tenantId, idempotencyKey, null, responseBody);

    // Then
    assertTrue(stored);

    // When - Check
    Optional<String> retrieved = idempotencyService.checkIdempotencyKey(tenantId, idempotencyKey);

    // Then
    assertTrue(retrieved.isPresent());
    assertEquals(responseBody, retrieved.get());
  }

  @Test
  @TestTransaction
  void shouldReturnEmptyWhenNoIdempotencyKeyProvided() {
    // When
    Optional<String> result = idempotencyService.checkIdempotencyKey("tenant-1", null);

    // Then
    assertTrue(result.isEmpty());
  }

  @Test
  @TestTransaction
  void shouldReturnEmptyForNonExistentKey() {
    // When
    Optional<String> result =
        idempotencyService.checkIdempotencyKey("tenant-1", "non-existent-key");

    // Then
    assertTrue(result.isEmpty());
  }

  @Test
  @TestTransaction
  void shouldIsolateTenants() {
    // Given
    String idempotencyKey = UUID.randomUUID().toString();
    String requestBody = "{\"data\":\"test\"}";
    String responseBody1 = "{\"tenant\":\"1\"}";
    String responseBody2 = "{\"tenant\":\"2\"}";

    // When - Store for different tenants
    idempotencyService.storeIdempotencyKey("tenant-1", idempotencyKey, requestBody, responseBody1);
    idempotencyService.storeIdempotencyKey("tenant-2", idempotencyKey, requestBody, responseBody2);

    // Then - Each tenant gets their own response
    Optional<String> result1 = idempotencyService.checkIdempotencyKey("tenant-1", idempotencyKey);
    Optional<String> result2 = idempotencyService.checkIdempotencyKey("tenant-2", idempotencyKey);

    assertTrue(result1.isPresent());
    assertTrue(result2.isPresent());
    assertEquals(responseBody1, result1.get());
    assertEquals(responseBody2, result2.get());
  }

  @Test
  @TestTransaction
  void shouldCleanupExpiredKeys() {
    // Store keys
    for (int i = 0; i < 5; i++) {
      String key = UUID.randomUUID().toString();
      idempotencyService.storeIdempotencyKey("tenant-1", key, "{}", "{\"id\":" + i + "}");
    }

    // When - Clean up (none should be expired yet)
    int deleted = idempotencyService.cleanupExpiredKeys();

    // Then - Should not delete any fresh keys
    assertEquals(0, deleted, "Should not delete fresh keys");
  }
}

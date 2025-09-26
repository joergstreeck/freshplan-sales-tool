package de.freshplan.domain.cockpit;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.cockpit.service.DashboardEventListener;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test für Idempotenz der Event-Verarbeitung. Stellt sicher, dass doppelte Events nur einmal
 * verarbeitet werden.
 *
 * <p>Sprint 2.1.1 P0 HOTFIX - Deduplizierung via Caffeine Cache
 */
@QuarkusTest
class DashboardEventIdempotencyTest {

  @Inject DashboardEventListener listener;

  private UUID testLeadId;
  private String testUserId;

  @BeforeEach
  void setUp() {
    testLeadId = UUID.randomUUID();
    testUserId = "test-user-" + UUID.randomUUID();
  }

  @Test
  @TestTransaction
  void shouldProcessIdenticalEventOnlyOnce() {
    // Given: Identisches Event (gleicher IdempotencyKey)
    String idempotencyKey = UUID.randomUUID().toString();
    JsonObject envelope =
        new JsonObject()
            .put("id", UUID.randomUUID().toString())
            .put("type", "dashboard.followup_completed")
            .put("idempotencyKey", idempotencyKey)
            .put("source", "system")
            .put(
                "data",
                new JsonObject()
                    .put("leadId", testLeadId.toString())
                    .put("followUpType", "T3")
                    .put("userId", testUserId));

    // Capture initial metrics
    long initialProcessed = listener.getMetrics().processed();
    long initialDuplicated = listener.getMetrics().duplicated();

    // When: Dasselbe Event zweimal verarbeiten
    listener.handleEnvelopeForTest("dashboard_updates", envelope);
    listener.handleEnvelopeForTest("dashboard_updates", envelope);

    // Then: Nur 1 Event sollte verarbeitet werden
    var metrics = listener.getMetrics();
    assertThat(metrics.processed() - initialProcessed)
        .as("Nur ein Event sollte verarbeitet werden")
        .isEqualTo(1);

    assertThat(metrics.duplicated() - initialDuplicated)
        .as("Ein Event sollte als Duplikat erkannt werden")
        .isEqualTo(1);
  }

  @Test
  @TestTransaction
  void shouldProcessDifferentEventsIndependently() {
    // Given: Zwei verschiedene Events (unterschiedliche IdempotencyKeys)
    JsonObject envelope1 =
        new JsonObject()
            .put("id", UUID.randomUUID().toString())
            .put("type", "dashboard.followup_completed")
            .put("idempotencyKey", UUID.randomUUID().toString())
            .put("source", "system")
            .put(
                "data",
                new JsonObject()
                    .put("leadId", testLeadId.toString())
                    .put("followUpType", "T3")
                    .put("userId", testUserId));

    JsonObject envelope2 =
        new JsonObject()
            .put("id", UUID.randomUUID().toString())
            .put("type", "dashboard.followup_completed")
            .put("idempotencyKey", UUID.randomUUID().toString()) // Anderer Key
            .put("source", "system")
            .put(
                "data",
                new JsonObject()
                    .put("leadId", testLeadId.toString())
                    .put("followUpType", "T7")
                    .put("userId", testUserId));

    // Capture initial metrics
    long initialProcessed = listener.getMetrics().processed();

    // When: Beide Events verarbeiten
    listener.handleEnvelopeForTest("dashboard_updates", envelope1);
    listener.handleEnvelopeForTest("dashboard_updates", envelope2);

    // Then: Beide sollten verarbeitet werden
    var metrics = listener.getMetrics();
    assertThat(metrics.processed() - initialProcessed)
        .as("Beide unterschiedlichen Events sollten verarbeitet werden")
        .isEqualTo(2);
  }

  @Test
  @TestTransaction
  void shouldMaintainCacheStatistics() {
    // Given: Mehrere Events
    for (int i = 0; i < 5; i++) {
      JsonObject envelope =
          new JsonObject()
              .put("id", UUID.randomUUID().toString())
              .put("type", "dashboard.followup_completed")
              .put("idempotencyKey", UUID.randomUUID().toString())
              .put("source", "system")
              .put(
                  "data",
                  new JsonObject()
                      .put("leadId", UUID.randomUUID().toString())
                      .put("followUpType", "T3")
                      .put("userId", testUserId));

      listener.handleEnvelopeForTest("dashboard_updates", envelope);
    }

    // Then: Cache sollte Einträge haben
    var metrics = listener.getMetrics();
    assertThat(metrics.cacheSize()).as("Cache sollte Einträge enthalten").isGreaterThan(0);

    assertThat(metrics.cacheHitRate())
        .as("Cache Hit Rate sollte verfügbar sein")
        .isGreaterThanOrEqualTo(0.0);
  }
}

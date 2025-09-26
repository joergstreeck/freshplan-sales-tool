package de.freshplan.domain.cockpit;

import de.freshplan.domain.cockpit.service.DashboardEventListener;
import de.freshplan.domain.cockpit.service.DashboardEventPublisher;
import de.freshplan.domain.cockpit.service.Metrics;
import de.freshplan.modules.leads.events.FollowUpProcessedEvent;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test für Idempotenz der Event-Verarbeitung.
 * Stellt sicher, dass doppelte Events nur einmal verarbeitet werden.
 *
 * Sprint 2.1.1 P0 HOTFIX - Deduplizierung via Caffeine Cache
 */
@QuarkusTest
class DashboardEventIdempotencyTest {

    @Inject
    DashboardEventPublisher publisher;

    @Inject
    DashboardEventListener listener;

    @Inject
    Metrics metrics;

    private UUID testLeadId;
    private String testUserId;
    private LocalDateTime fixedTime;

    @BeforeEach
    void setUp() {
        testLeadId = UUID.randomUUID();
        testUserId = "test-user-" + UUID.randomUUID();
        fixedTime = LocalDateTime.of(2025, 9, 26, 12, 0, 0);
    }

    @Test
    void shouldProcessIdenticalEventOnlyOnce() {
        // Given: Identisches Event (gleicher IdempotencyKey)
        FollowUpProcessedEvent event1 = new FollowUpProcessedEvent(
            testLeadId,
            "T3",
            1,
            0,
            true,
            testUserId,
            "2s",
            fixedTime  // Feste Zeit für deterministischen Key
        );

        // Zweites Event mit exakt gleichen Daten
        FollowUpProcessedEvent event2 = new FollowUpProcessedEvent(
            testLeadId,
            "T3",
            1,
            0,
            true,
            testUserId,
            "2s",
            fixedTime  // Gleiche Zeit = gleicher IdempotencyKey
        );

        // Capture initial metrics
        long initialProcessed = listener.getMetrics().processed();
        long initialDuplicated = listener.getMetrics().duplicated();

        // When: Beide Events publizieren
        publisher.onFollowUpProcessed(event1);
        publisher.onFollowUpProcessed(event2);

        // Then: Warte auf Verarbeitung (mit Awaitility)
        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                var metrics = listener.getMetrics();
                // Nur 1 Event sollte verarbeitet werden
                assertThat(metrics.processed() - initialProcessed)
                    .as("Nur ein Event sollte verarbeitet werden")
                    .isEqualTo(1);

                // 1 Event sollte als Duplikat erkannt werden
                assertThat(metrics.duplicated() - initialDuplicated)
                    .as("Ein Event sollte als Duplikat erkannt werden")
                    .isEqualTo(1);
            });
    }

    @Test
    void shouldProcessDifferentEventsIndependently() {
        // Given: Zwei verschiedene Events (unterschiedliche Zeiten)
        FollowUpProcessedEvent event1 = new FollowUpProcessedEvent(
            testLeadId,
            "T3",
            1,
            0,
            true,
            testUserId,
            "2s",
            LocalDateTime.of(2025, 9, 26, 12, 0, 0)
        );

        FollowUpProcessedEvent event2 = new FollowUpProcessedEvent(
            testLeadId,
            "T3",
            1,
            0,
            true,
            testUserId,
            "3s",
            LocalDateTime.of(2025, 9, 26, 12, 0, 1)  // Andere Zeit = anderer Key
        );

        // Capture initial metrics
        long initialProcessed = listener.getMetrics().processed();

        // When: Beide Events publizieren
        publisher.onFollowUpProcessed(event1);
        publisher.onFollowUpProcessed(event2);

        // Then: Beide sollten verarbeitet werden
        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                var metrics = listener.getMetrics();
                // Beide Events sollten verarbeitet werden
                assertThat(metrics.processed() - initialProcessed)
                    .as("Beide unterschiedlichen Events sollten verarbeitet werden")
                    .isEqualTo(2);
            });
    }

    @Test
    void shouldMaintainCacheStatistics() {
        // Given: Mehrere Events
        for (int i = 0; i < 5; i++) {
            FollowUpProcessedEvent event = new FollowUpProcessedEvent(
                UUID.randomUUID(),
                "T3",
                1,
                0,
                true,
                testUserId,
                "1s",
                LocalDateTime.now().plusSeconds(i)
            );
            publisher.onFollowUpProcessed(event);
        }

        // Then: Cache sollte Einträge haben
        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                var metrics = listener.getMetrics();
                assertThat(metrics.cacheSize())
                    .as("Cache sollte Einträge enthalten")
                    .isGreaterThan(0);

                assertThat(metrics.cacheHitRate())
                    .as("Cache Hit Rate sollte verfügbar sein")
                    .isGreaterThanOrEqualTo(0.0);
            });
    }
}
package de.freshplan.domain.cockpit;

import de.freshplan.domain.cockpit.service.DashboardEventPublisher;
import de.freshplan.infrastructure.pg.TestPgNotifySender;
import de.freshplan.modules.leads.events.FollowUpProcessedEvent;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test für BATCH-Event Idempotency-Key Stabilität.
 * Verifiziert Minute-Boundary Handling und deterministische Keys.
 *
 * Sprint 2.1.1 P1 - BATCH Event Key Robustheit
 */
@QuarkusTest
class DashboardBatchIdempotencyTest {

    @Inject
    DashboardEventPublisher publisher;

    @Inject
    UserTransaction userTransaction;

    @Inject
    TestPgNotifySender testSender;

    private String testUserId;

    @BeforeEach
    void setUp() {
        testUserId = "batch-user-" + UUID.randomUUID();
        testSender.clear();
    }

    @Test
    void shouldGenerateDifferentKeysForBatchEventsAcrossMinuteBoundary() throws Exception {
        // Given: Zwei BATCH-Events knapp über Minutengrenze
        LocalDateTime beforeMinuteBoundary = LocalDateTime.of(2025, 9, 26, 14, 59, 59, 900_000_000);
        LocalDateTime afterMinuteBoundary = LocalDateTime.of(2025, 9, 26, 15, 0, 0, 50_000_000);

        FollowUpProcessedEvent batchEvent1 = new FollowUpProcessedEvent(
            null, // kein Lead bei BATCH
            "BATCH",
            5,
            3,
            true,
            testUserId,
            "100ms",
            beforeMinuteBoundary
        );

        FollowUpProcessedEvent batchEvent2 = new FollowUpProcessedEvent(
            null,
            "BATCH",
            5,
            3,
            true,
            testUserId,
            "100ms",
            afterMinuteBoundary
        );

        // When: Beide Events publizieren
        userTransaction.begin();
        try {
            publisher.onFollowUpProcessed(batchEvent1);
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            throw e;
        }

        Thread.sleep(100);

        userTransaction.begin();
        try {
            publisher.onFollowUpProcessed(batchEvent2);
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            throw e;
        }

        Thread.sleep(500);

        // Then: Zwei verschiedene Events sollten gesendet worden sein
        assertThat(testSender.count())
            .as("Zwei BATCH-Events über Minutengrenze sollten verschiedene Keys haben")
            .isEqualTo(2);

        // Verify different idempotency keys
        var sent1 = testSender.sent().get(0);
        var sent2 = testSender.sent().get(1);

        JsonObject envelope1 = new JsonObject(sent1.payload());
        JsonObject envelope2 = new JsonObject(sent2.payload());

        String key1 = envelope1.getString("idempotencyKey");
        String key2 = envelope2.getString("idempotencyKey");

        assertThat(key1)
            .as("IdempotencyKey sollte nicht null sein")
            .isNotNull();

        assertThat(key2)
            .as("IdempotencyKey sollte nicht null sein")
            .isNotNull();

        assertThat(key1)
            .as("Keys sollten sich unterscheiden bei verschiedenen Minuten")
            .isNotEqualTo(key2);

        // Verify timestamps in data
        JsonObject data1 = envelope1.getJsonObject("data");
        JsonObject data2 = envelope2.getJsonObject("data");

        assertThat(data1.getString("processedAt")).contains("14:59");
        assertThat(data2.getString("processedAt")).contains("15:00");
    }

    @Test
    void shouldGenerateSameKeyForBatchEventsWithinSameMinute() throws Exception {
        // Given: Zwei BATCH-Events innerhalb derselben Minute
        LocalDateTime time1 = LocalDateTime.of(2025, 9, 26, 14, 30, 10);
        LocalDateTime time2 = LocalDateTime.of(2025, 9, 26, 14, 30, 45);

        FollowUpProcessedEvent batchEvent1 = new FollowUpProcessedEvent(
            null,
            "BATCH",
            5,
            3,
            true,
            testUserId,
            "100ms",
            time1
        );

        FollowUpProcessedEvent batchEvent2 = new FollowUpProcessedEvent(
            null,
            "BATCH",
            5,
            3,
            true,
            testUserId,
            "100ms",
            time2
        );

        // When: Beide Events publizieren
        userTransaction.begin();
        try {
            publisher.onFollowUpProcessed(batchEvent1);
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            throw e;
        }

        Thread.sleep(100);

        userTransaction.begin();
        try {
            publisher.onFollowUpProcessed(batchEvent2);
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            throw e;
        }

        Thread.sleep(500);

        // Then: Sollten gleiche Keys haben (da gleiche Minute + gleiche Counts)
        assertThat(testSender.count()).isEqualTo(2);

        var sent1 = testSender.sent().get(0);
        var sent2 = testSender.sent().get(1);

        JsonObject envelope1 = new JsonObject(sent1.payload());
        JsonObject envelope2 = new JsonObject(sent2.payload());

        String key1 = envelope1.getString("idempotencyKey");
        String key2 = envelope2.getString("idempotencyKey");

        assertThat(key1)
            .as("Keys sollten gleich sein innerhalb derselben Minute mit gleichen Counts")
            .isEqualTo(key2);
    }

    @Test
    void shouldGenerateDifferentKeysForBatchEventsWithDifferentCounts() throws Exception {
        // Given: Zwei BATCH-Events mit unterschiedlichen Counts
        LocalDateTime sameTime = LocalDateTime.of(2025, 9, 26, 14, 30, 0);

        FollowUpProcessedEvent batchEvent1 = new FollowUpProcessedEvent(
            null,
            "BATCH",
            5,
            3,
            true,
            testUserId,
            "100ms",
            sameTime
        );

        FollowUpProcessedEvent batchEvent2 = new FollowUpProcessedEvent(
            null,
            "BATCH",
            10, // andere T3 Count
            7,  // andere T7 Count
            true,
            testUserId,
            "100ms",
            sameTime
        );

        // When: Beide Events publizieren
        userTransaction.begin();
        try {
            publisher.onFollowUpProcessed(batchEvent1);
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            throw e;
        }

        Thread.sleep(100);

        userTransaction.begin();
        try {
            publisher.onFollowUpProcessed(batchEvent2);
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            throw e;
        }

        Thread.sleep(500);

        // Then: Verschiedene Keys wegen verschiedener Counts
        assertThat(testSender.count()).isEqualTo(2);

        var sent1 = testSender.sent().get(0);
        var sent2 = testSender.sent().get(1);

        String key1 = new JsonObject(sent1.payload()).getString("idempotencyKey");
        String key2 = new JsonObject(sent2.payload()).getString("idempotencyKey");

        assertThat(key1)
            .as("Keys sollten sich unterscheiden bei verschiedenen Counts")
            .isNotEqualTo(key2);
    }
}
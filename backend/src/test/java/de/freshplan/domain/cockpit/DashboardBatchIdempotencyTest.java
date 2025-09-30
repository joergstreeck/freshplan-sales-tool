package de.freshplan.domain.cockpit;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.cockpit.service.DashboardEventPublisher;
import de.freshplan.infrastructure.pg.TestPgNotifySender;
import de.freshplan.modules.leads.events.FollowUpProcessedEvent;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test für BATCH-Event Idempotency-Key Stabilität. Verifiziert Minute-Boundary Handling und
 * deterministische Keys.
 *
 * <p>Sprint 2.1.1 P1 - BATCH Event Key Robustheit
 */
@QuarkusTest
@TestTransaction  // Sprint 2.1.4: Fix ContextNotActiveException
class DashboardBatchIdempotencyTest {

  @Inject DashboardEventPublisher publisher;

  @Inject UserTransaction userTransaction;

  @Inject TestPgNotifySender testSender;

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

    FollowUpProcessedEvent batchEvent1 =
        new FollowUpProcessedEvent(
            null, // kein Lead bei BATCH
            "BATCH",
            5,
            3,
            true,
            testUserId,
            "100ms",
            beforeMinuteBoundary);

    FollowUpProcessedEvent batchEvent2 =
        new FollowUpProcessedEvent(
            null, "BATCH", 5, 3, true, testUserId, "100ms", afterMinuteBoundary);

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

    // Then: Zwei verschiedene Follow-up Events sollten gesendet worden sein (ohne Metrics-Events)
    List<TestPgNotifySender.Send> dashboardEvents =
        filterDashboardFollowupEvents(testSender.sent());
    assertThat(dashboardEvents)
        .as("Zwei BATCH-Events über Minutengrenze sollten verschiedene Keys haben")
        .hasSize(2);

    // Verify different idempotency keys
    var sent1 = dashboardEvents.get(0);
    var sent2 = dashboardEvents.get(1);

    JsonObject envelope1 = new JsonObject(sent1.payload());
    JsonObject envelope2 = new JsonObject(sent2.payload());

    String key1 = envelope1.getString("idempotencyKey");
    String key2 = envelope2.getString("idempotencyKey");

    assertThat(key1).as("IdempotencyKey sollte nicht null sein").isNotNull();

    assertThat(key2).as("IdempotencyKey sollte nicht null sein").isNotNull();

    assertThat(key1)
        .as("Keys sollten sich unterscheiden bei verschiedenen Minuten")
        .isNotEqualTo(key2);

    // Verify timestamps in data
    JsonObject data1 = envelope1.getJsonObject("data");
    JsonObject data2 = envelope2.getJsonObject("data");

    // Check that timestamps correspond to the times we set
    LocalDateTime processed1 = LocalDateTime.parse(data1.getString("processedAt"));
    LocalDateTime processed2 = LocalDateTime.parse(data2.getString("processedAt"));

    assertThat(processed1.getMinute()).isEqualTo(59);
    assertThat(processed2.getMinute()).isEqualTo(0);
  }

  @Test
  void shouldGenerateSameKeyForBatchEventsWithinSameMinute() throws Exception {
    // Given: Zwei BATCH-Events innerhalb derselben Minute
    LocalDateTime time1 = LocalDateTime.of(2025, 9, 26, 14, 30, 10);
    LocalDateTime time2 = LocalDateTime.of(2025, 9, 26, 14, 30, 45);

    FollowUpProcessedEvent batchEvent1 =
        new FollowUpProcessedEvent(null, "BATCH", 5, 3, true, testUserId, "100ms", time1);

    FollowUpProcessedEvent batchEvent2 =
        new FollowUpProcessedEvent(null, "BATCH", 5, 3, true, testUserId, "100ms", time2);

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
    List<TestPgNotifySender.Send> dashboardEvents =
        filterDashboardFollowupEvents(testSender.sent());
    assertThat(dashboardEvents).hasSize(2);

    var sent1 = dashboardEvents.get(0);
    var sent2 = dashboardEvents.get(1);

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

    FollowUpProcessedEvent batchEvent1 =
        new FollowUpProcessedEvent(null, "BATCH", 5, 3, true, testUserId, "100ms", sameTime);

    FollowUpProcessedEvent batchEvent2 =
        new FollowUpProcessedEvent(
            null,
            "BATCH",
            10, // andere T3 Count
            7, // andere T7 Count
            true,
            testUserId,
            "100ms",
            sameTime);

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
    List<TestPgNotifySender.Send> dashboardEvents =
        filterDashboardFollowupEvents(testSender.sent());
    assertThat(dashboardEvents).hasSize(2);

    var sent1 = dashboardEvents.get(0);
    var sent2 = dashboardEvents.get(1);

    String key1 = new JsonObject(sent1.payload()).getString("idempotencyKey");
    String key2 = new JsonObject(sent2.payload()).getString("idempotencyKey");

    assertThat(key1)
        .as("Keys sollten sich unterscheiden bei verschiedenen Counts")
        .isNotEqualTo(key2);
  }

  /** Helper: Filtert nur dashboard.followup_completed Events (ohne metrics.followup_tracked). */
  private List<TestPgNotifySender.Send> filterDashboardFollowupEvents(
      List<TestPgNotifySender.Send> allEvents) {
    return allEvents.stream()
        .filter(
            send -> {
              try {
                JsonObject envelope = new JsonObject(send.payload());
                String type = envelope.getString("type", "");
                return "dashboard.followup_completed".equals(type);
              } catch (Exception e) {
                return false;
              }
            })
        .collect(Collectors.toList());
  }
}

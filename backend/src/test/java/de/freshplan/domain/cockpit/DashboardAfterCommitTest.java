package de.freshplan.domain.cockpit;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.cockpit.service.DashboardEventPublisher;
import de.freshplan.infrastructure.pg.TestPgNotifySender;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.events.LeadStatusChangeEvent;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test für AFTER_COMMIT Pattern. Stellt sicher, dass Events nur nach erfolgreicher Transaktion
 * publiziert werden.
 *
 * <p>Sprint 2.1.1 P0 HOTFIX - Ghost-Event Prevention
 */
@QuarkusTest
@TestTransaction  // Sprint 2.1.4: Fix ContextNotActiveException
class DashboardAfterCommitTest {

  @Inject DashboardEventPublisher publisher;

  @Inject UserTransaction userTransaction;

  @Inject TestPgNotifySender testSender;

  private UUID testLeadId;
  private String testUserId;

  @BeforeEach
  void setUp() {
    testLeadId = UUID.randomUUID();
    testUserId = "test-user-" + UUID.randomUUID();
    testSender.clear();
  }

  @Test
  void shouldNotPublishEventOnRollback() throws Exception {
    // Given: Initial state
    int initialSentCount = testSender.count();

    // When: Transaktion starten, Event publizieren, dann rollback
    userTransaction.begin();
    try {
      LeadStatusChangeEvent event =
          LeadStatusChangeEvent.of(
              testLeadId,
              "Test Company",
              LeadStatus.REGISTERED,
              LeadStatus.QUALIFIED,
              testUserId,
              "Test rollback scenario");

      // Event wird vorbereitet aber sollte nicht publiziert werden
      publisher.onLeadStatusChange(event);

      // Expliziter Rollback
      userTransaction.rollback();

    } catch (Exception e) {
      userTransaction.rollback();
      throw e;
    }

    // Then: Kein Event sollte publiziert worden sein
    Thread.sleep(500); // Kurz warten falls async

    assertThat(testSender.count())
        .as("Kein Event sollte nach Rollback publiziert werden")
        .isEqualTo(initialSentCount);
  }

  @Test
  void shouldPublishEventOnCommit() throws Exception {
    // Given: Initial state
    int initialSentCount = testSender.count();

    // When: Transaktion starten, Event publizieren, dann commit
    userTransaction.begin();
    try {
      LeadStatusChangeEvent event =
          LeadStatusChangeEvent.of(
              testLeadId,
              "Test Company",
              LeadStatus.REGISTERED,
              LeadStatus.QUALIFIED,
              testUserId,
              "Test commit scenario");

      publisher.onLeadStatusChange(event);

      // Expliziter Commit
      userTransaction.commit();

    } catch (Exception e) {
      userTransaction.rollback();
      throw e;
    }

    // Then: Event sollte publiziert worden sein
    Awaitility.await()
        .atMost(2, TimeUnit.SECONDS)
        .pollInterval(100, TimeUnit.MILLISECONDS)
        .untilAsserted(
            () -> {
              assertThat(testSender.count())
                  .as("Ein Event sollte nach Commit publiziert werden")
                  .isEqualTo(initialSentCount + 1);

              // Verify the event content
              var sent = testSender.sent().get(0);
              assertThat(sent.channel()).isEqualTo("dashboard_updates");

              JsonObject envelope = new JsonObject(sent.payload());
              assertThat(envelope.getString("type")).isEqualTo("dashboard.lead_status_changed");
              assertThat(envelope.getString("idempotencyKey")).isNotNull();
            });
  }

  @Test
  void shouldHandleNoTransactionGracefully() {
    // Given: Initial state
    int initialSentCount = testSender.count();

    // When: Event außerhalb einer Transaktion publizieren
    LeadStatusChangeEvent event =
        LeadStatusChangeEvent.of(
            testLeadId,
            "Test Company",
            LeadStatus.REGISTERED,
            LeadStatus.QUALIFIED,
            testUserId,
            "Test no-tx scenario");

    publisher.onLeadStatusChange(event);

    // Then: Event sollte sofort publiziert werden (no_tx Pfad)
    Awaitility.await()
        .atMost(1, TimeUnit.SECONDS)
        .pollInterval(100, TimeUnit.MILLISECONDS)
        .untilAsserted(
            () -> {
              assertThat(testSender.count())
                  .as("Event sollte ohne Transaktion sofort publiziert werden")
                  .isEqualTo(initialSentCount + 1);

              // Event wurde synchron publiziert
              var sent = testSender.sent().get(0);
              assertThat(sent.channel()).isEqualTo("dashboard_updates");
            });
  }
}

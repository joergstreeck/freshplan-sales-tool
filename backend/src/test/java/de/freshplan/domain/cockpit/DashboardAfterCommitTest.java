package de.freshplan.domain.cockpit;

import de.freshplan.domain.cockpit.service.DashboardEventPublisher;
import de.freshplan.domain.cockpit.service.Metrics;
import de.freshplan.modules.leads.events.LeadStatusChangeEvent;
import de.freshplan.modules.leads.domain.LeadStatus;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test für AFTER_COMMIT Pattern.
 * Stellt sicher, dass Events nur nach erfolgreicher Transaktion publiziert werden.
 *
 * Sprint 2.1.1 P0 HOTFIX - Ghost-Event Prevention
 */
@QuarkusTest
class DashboardAfterCommitTest {

    @Inject
    DashboardEventPublisher publisher;

    @Inject
    UserTransaction userTransaction;

    @Inject
    MeterRegistry meterRegistry;

    private UUID testLeadId;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testLeadId = UUID.randomUUID();
        testUserId = "test-user-" + UUID.randomUUID();
    }

    @Test
    void shouldNotPublishEventOnRollback() throws Exception {
        // Given: Counter vor dem Test
        double initialPublished = meterRegistry.counter("freshplan_events_published",
            "event_type", "lead_status_changed",
            "module", "leads",
            "result", "success").count();

        // When: Transaktion starten, Event publizieren, dann rollback
        userTransaction.begin();
        try {
            LeadStatusChangeEvent event = LeadStatusChangeEvent.of(
                testLeadId,
                "Test Company",
                LeadStatus.REGISTERED,
                LeadStatus.QUALIFIED,
                testUserId,
                "Test rollback scenario"
            );

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

        double afterRollback = meterRegistry.counter("freshplan_events_published",
            "event_type", "lead_status_changed",
            "module", "leads",
            "result", "success").count();

        assertThat(afterRollback)
            .as("Kein Event sollte nach Rollback publiziert werden")
            .isEqualTo(initialPublished);
    }

    @Test
    void shouldPublishEventOnCommit() throws Exception {
        // Given: Counter vor dem Test
        double initialPublished = meterRegistry.counter("freshplan_events_published",
            "event_type", "lead_status_changed",
            "module", "leads",
            "result", "success").count();

        // When: Transaktion starten, Event publizieren, dann commit
        userTransaction.begin();
        try {
            LeadStatusChangeEvent event = LeadStatusChangeEvent.of(
                testLeadId,
                "Test Company",
                LeadStatus.REGISTERED,
                LeadStatus.QUALIFIED,
                testUserId,
                "Test commit scenario"
            );

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
            .untilAsserted(() -> {
                double afterCommit = meterRegistry.counter("freshplan_events_published",
                    "event_type", "lead_status_changed",
                    "module", "leads",
                    "result", "success").count();

                assertThat(afterCommit)
                    .as("Ein Event sollte nach Commit publiziert werden")
                    .isEqualTo(initialPublished + 1);
            });
    }

    @Test
    void shouldHandleNoTransactionGracefully() {
        // Given: Counter vor dem Test
        double initialNoTx = meterRegistry.counter("freshplan_events_published",
            "event_type", "lead_status_changed",
            "module", "leads",
            "result", "no_tx").count();

        // When: Event außerhalb einer Transaktion publizieren
        LeadStatusChangeEvent event = LeadStatusChangeEvent.of(
            testLeadId,
            "Test Company",
            LeadStatus.REGISTERED,
            LeadStatus.QUALIFIED,
            testUserId,
            "Test no-tx scenario"
        );

        publisher.onLeadStatusChange(event);

        // Then: Event sollte mit no_tx markiert worden sein
        Awaitility.await()
            .atMost(1, TimeUnit.SECONDS)
            .pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                double afterNoTx = meterRegistry.counter("freshplan_events_published",
                    "event_type", "lead_status_changed",
                    "module", "leads",
                    "result", "no_tx").count();

                assertThat(afterNoTx)
                    .as("Event sollte als no_tx markiert werden wenn keine Transaktion")
                    .isEqualTo(initialNoTx + 1);
            });
    }
}
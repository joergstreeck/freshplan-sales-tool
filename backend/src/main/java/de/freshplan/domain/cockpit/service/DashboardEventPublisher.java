package de.freshplan.domain.cockpit.service;

import de.freshplan.infrastructure.security.RlsContext;
import de.freshplan.infrastructure.security.SecurityContextProvider;
import de.freshplan.modules.leads.events.FollowUpProcessedEvent;
import de.freshplan.modules.leads.events.LeadStatusChangeEvent;
import io.quarkus.logging.Log;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Status;
import java.sql.PreparedStatement;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Publisher für Dashboard-Events mit AFTER_COMMIT Pattern.
 * Stellt sicher, dass Events nur nach erfolgreicher Transaktion publiziert werden.
 *
 * Sprint 2.1.1 P0 HOTFIX - AFTER_COMMIT nur in Publishern, nicht in Listenern!
 */
@ApplicationScoped
@RlsContext
public class DashboardEventPublisher {

    @Inject
    TransactionSynchronizationRegistry txRegistry;

    @Inject
    EntityManager entityManager;

    @Inject
    SecurityContextProvider securityContext;

    @Inject
    Metrics metrics;

    private static final String DASHBOARD_CHANNEL = "dashboard_updates";
    private static final String METRICS_CHANNEL = "metrics_events";

    /**
     * Verarbeitet Lead-Status-Änderungen mit AFTER_COMMIT.
     */
    public void onLeadStatusChange(@Observes LeadStatusChangeEvent event) {
        Log.debugf("Preparing dashboard event for lead status change: %s", event.leadId());

        // RBAC Check: Darf User dieses Event erzeugen?
        if (!canPublishEvent(event.userId())) {
            Log.warnf("User %s not authorized to publish lead events", event.userId());
            return;
        }

        JsonObject payload = buildLeadStatusPayload(event);
        String idempotencyKey = event.getIdempotencyKey();

        // AFTER_COMMIT Pattern - Event wird nur nach erfolgreicher Transaktion publiziert
        txRegistry.registerInterposedSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() {
                // Nothing to do before completion
            }

            @Override
            public void afterCompletion(int status) {
                if (status == Status.STATUS_COMMITTED) {
                    publishToDashboard(payload, idempotencyKey);
                } else {
                    Log.debugf("Transaction rolled back, not publishing event for lead: %s", event.leadId());
                }
            }
        });
    }

    /**
     * Verarbeitet Follow-up Events mit AFTER_COMMIT.
     */
    public void onFollowUpProcessed(@Observes FollowUpProcessedEvent event) {
        Log.debugf("Preparing dashboard event for follow-up: %s", event.getLeadId());

        // RBAC Check
        if (!canPublishEvent(event.getUserId())) {
            Log.warnf("User %s not authorized to publish follow-up events", event.getUserId());
            return;
        }

        JsonObject payload = buildFollowUpPayload(event);
        String idempotencyKey = generateIdempotencyKey(event);

        // AFTER_COMMIT für Ghost-Event Prevention
        txRegistry.registerInterposedSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() {
                // Nothing to do before completion
            }

            @Override
            public void afterCompletion(int status) {
                if (status == Status.STATUS_COMMITTED) {
                    publishCrossModuleEvent(payload, idempotencyKey);
                } else {
                    Log.debugf("Transaction rolled back, not publishing follow-up event");
                }
            }
        });
    }

    /**
     * Publiziert Event via PostgreSQL NOTIFY (PreparedStatement gegen SQL-Injection).
     */
    private void publishToDashboard(JsonObject payload, String idempotencyKey) {
        try {
            JsonObject envelope = new JsonObject()
                .put("id", UUID.randomUUID().toString())
                .put("source", "lead-management")
                .put("type", "dashboard.lead_status_changed")
                .put("time", LocalDateTime.now().toString())
                .put("idempotencyKey", idempotencyKey)
                .put("data", payload);

            notifyPg(DASHBOARD_CHANNEL, envelope.encode());
            Log.debugf("Published dashboard event with key: %s", idempotencyKey);
            metrics.incPublished("lead_status_changed", "leads");

        } catch (Exception e) {
            Log.errorf(e, "Failed to publish dashboard event");
            // Fehler wird geloggt aber nicht propagiert (fail-safe)
        }
    }

    /**
     * Publiziert Cross-Module Event für Follow-ups.
     */
    private void publishCrossModuleEvent(JsonObject payload, String idempotencyKey) {
        try {
            JsonObject envelope = new JsonObject()
                .put("id", UUID.randomUUID().toString())
                .put("source", "lead-management")
                .put("type", "dashboard.followup_completed")
                .put("time", LocalDateTime.now().toString())
                .put("idempotencyKey", idempotencyKey)
                .put("data", payload);

            notifyPg(DASHBOARD_CHANNEL, envelope.encode());
            Log.debugf("Published follow-up event with key: %s", idempotencyKey);
            metrics.incPublished("followup_processed", "leads");

            // Zusätzlich Metrics-Event publizieren
            publishMetricsEvent(payload, idempotencyKey);

        } catch (Exception e) {
            Log.errorf(e, "Failed to publish cross-module event");
        }
    }

    /**
     * Publiziert Metrics-Event für Monitoring.
     */
    private void publishMetricsEvent(JsonObject payload, String idempotencyKey) {
        try {
            JsonObject metricsPayload = new JsonObject()
                .put("id", UUID.randomUUID().toString())
                .put("source", "lead-management")
                .put("type", "metrics.followup_tracked")
                .put("time", LocalDateTime.now().toString())
                .put("idempotencyKey", idempotencyKey + "_metrics")
                .put("data", payload);

            notifyPg(METRICS_CHANNEL, metricsPayload.encode());

        } catch (Exception e) {
            Log.debugf("Failed to publish metrics event: %s", e.getMessage());
        }
    }

    /**
     * Robuste pg_notify Implementation via Hibernate Session.
     * Einfacher und sicherer als komplexe Unwrap-Ketten.
     */
    private void notifyPg(String channel, String payload) {
        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            try (PreparedStatement ps = connection.prepareStatement("SELECT pg_notify(?, ?)")) {
                ps.setString(1, channel);
                ps.setString(2, payload);
                ps.execute();
            }
        });
    }

    /**
     * Baut Payload für Lead-Status-Event.
     */
    private JsonObject buildLeadStatusPayload(LeadStatusChangeEvent event) {
        return new JsonObject()
            .put("leadId", event.leadId().toString())
            .put("companyName", event.companyName())
            .put("oldStatus", event.oldStatus().name())
            .put("newStatus", event.newStatus().name())
            .put("userId", event.userId())
            .put("changedAt", event.changedAt().toString())
            .put("reason", event.reason());
    }

    /**
     * Baut Payload für Follow-up-Event.
     */
    private JsonObject buildFollowUpPayload(FollowUpProcessedEvent event) {
        return new JsonObject()
            .put("leadId", event.getLeadId() != null ? event.getLeadId().toString() : "")
            .put("followUpType", event.getFollowUpType())
            .put("t3Count", event.getT3Count())
            .put("t7Count", event.getT7Count())
            .put("success", event.isSuccess())
            .put("userId", event.getUserId())
            .put("processedAt", LocalDateTime.now().toString())
            .put("responseTime", event.getResponseTime() != null ? event.getResponseTime() : "unknown");
    }

    /**
     * Generiert deterministischen Idempotency-Key für Follow-ups.
     * WICHTIG: Keine aktuelle Zeit verwenden - muss deterministisch sein!
     */
    private String generateIdempotencyKey(FollowUpProcessedEvent event) {
        String leadId = event.leadId() != null ? event.leadId().toString() : "BATCH";
        String followUpType = event.followUpType() != null ? event.followUpType() : "UNKNOWN";
        // Verwende processedAt aus Event (ist im record fest gesetzt)
        LocalDateTime when = event.processedAt() != null ? event.processedAt() : LocalDateTime.MIN;

        String composite = leadId + "|" + followUpType + "|" + when;
        return UUID.nameUUIDFromBytes(composite.getBytes(StandardCharsets.UTF_8)).toString();
    }

    /**
     * RBAC Check: Prüft ob User Events publizieren darf.
     */
    private boolean canPublishEvent(String userId) {
        // Manager und Sales-Rollen dürfen publizieren
        return securityContext.hasRole("MANAGER") ||
               securityContext.hasRole("SALES") ||
               securityContext.hasRole("ADMIN");
    }
}
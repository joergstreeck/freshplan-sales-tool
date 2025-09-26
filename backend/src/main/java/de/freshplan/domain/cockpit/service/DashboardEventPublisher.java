package de.freshplan.domain.cockpit.service;

import de.freshplan.infrastructure.pg.PgNotifySender;
import de.freshplan.infrastructure.security.SecurityContextProvider;
import de.freshplan.modules.leads.events.FollowUpProcessedEvent;
import de.freshplan.modules.leads.events.LeadStatusChangeEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.logging.Log;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Status;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Publisher für Dashboard-Events mit AFTER_COMMIT Pattern.
 * Stellt sicher, dass Events nur nach erfolgreicher Transaktion publiziert werden.
 *
 * Sprint 2.1.1 P0 HOTFIX - AFTER_COMMIT nur in Publishern, nicht in Listenern!
 */
@ApplicationScoped
@de.freshplan.infrastructure.security.RlsContext
public class DashboardEventPublisher {

    @Inject
    TransactionSynchronizationRegistry txRegistry;

    @Inject
    PgNotifySender pgNotifySender;

    @Inject
    SecurityContextProvider securityContext;

    @Inject
    Metrics metrics;

    @ConfigProperty(name = "freshplan.security.allow-unauthenticated-publisher", defaultValue = "false")
    boolean allowUnauthenticatedPublisher;

    private static final String DASHBOARD_CHANNEL = "dashboard_updates";
    private static final String METRICS_CHANNEL = "metrics_events";
    private static final int MAX_NOTIFY_PAYLOAD_SIZE = 7680; // 7.5 KB Safety Limit (Postgres max ~8KB)

    /**
     * Warnung bei aktivem Security-Bypass beim Startup.
     */
    @PostConstruct
    void logSecurityBypass() {
        if (allowUnauthenticatedPublisher) {
            Log.warn("⚠️ freshplan.security.allow-unauthenticated-publisher=true (intended only for tests!)");
        }
    }

    /**
     * Verarbeitet Lead-Status-Änderungen mit AFTER_COMMIT.
     */
    @Transactional
    public void onLeadStatusChange(@Observes LeadStatusChangeEvent event) {
        Log.debugf("Preparing dashboard event for lead status change: %s", event.leadId());

        // RBAC Check: Darf User dieses Event erzeugen?
        if (!canPublishEvent(event.userId(), "lead_status_changed")) {
            Log.warnf("User %s not authorized to publish lead events", event.userId());
            return;
        }

        JsonObject payload = buildLeadStatusPayload(event);
        String idempotencyKey = event.getIdempotencyKey();

        // TX-Guard: Prüfe ob aktive Transaktion vorhanden
        if (!isInActiveTransaction()) {
            Log.warnf("No active transaction for lead status change event - publishing synchronously");
            metrics.incPublishedWithResult("lead_status_changed", "leads", "no_tx");
            // Fail-safe: Synchron publizieren wenn keine TX
            publishToDashboard(payload, idempotencyKey);
            return;
        }

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
    @Transactional
    public void onFollowUpProcessed(@Observes FollowUpProcessedEvent event) {
        Log.debugf("Preparing dashboard event for follow-up: %s", event.leadId());

        // RBAC Check
        if (!canPublishEvent(event.userId(), "followup_processed")) {
            Log.warnf("User %s not authorized to publish follow-up events", event.userId());
            return;
        }

        JsonObject payload = buildFollowUpPayload(event);
        String idempotencyKey = generateIdempotencyKey(event);

        // TX-Guard: Prüfe ob aktive Transaktion vorhanden
        if (!isInActiveTransaction()) {
            Log.warnf("No active transaction for follow-up event - publishing synchronously");
            metrics.incPublishedWithResult("followup_processed", "leads", "no_tx");
            // Fail-safe: Synchron publizieren wenn keine TX
            publishCrossModuleEvent(payload, idempotencyKey);
            return;
        }

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
                .put("time", Instant.now().toString())
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
                .put("time", Instant.now().toString())
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
                .put("time", Instant.now().toString())
                .put("idempotencyKey", idempotencyKey + "_metrics")
                .put("data", payload);

            notifyPg(METRICS_CHANNEL, metricsPayload.encode());

        } catch (Exception e) {
            Log.debugf("Failed to publish metrics event: %s", e.getMessage());
        }
    }

    /**
     * Robuste pg_notify Implementation via Hibernate Session.
     * Prüft Payload-Größe gegen Postgres-Limit (~8KB).
     */
    private void notifyPg(String channel, String payload) {
        // Event-Typ extrahieren für Metriken
        String eventType = "unknown";
        try {
            eventType = new JsonObject(payload).getString("type", "unknown");
        } catch (Exception ignore) {
            // Fallback to unknown
        }

        // Payload-Guard: Postgres NOTIFY hat ~8KB Limit
        String effectivePayload = payload;
        int byteLen = payload.getBytes(StandardCharsets.UTF_8).length;
        if (byteLen > MAX_NOTIFY_PAYLOAD_SIZE) {
            Log.warnf("NOTIFY payload too large (%d bytes) for channel %s - truncating data field",
                byteLen, channel);

            // Envelope beibehalten, nur data kürzen
            JsonObject envelope = new JsonObject(payload);
            JsonObject truncatedData = new JsonObject()
                .put("truncated", true)
                .put("reference", envelope.getString("idempotencyKey"))
                .put("original_size_bytes", byteLen)
                .put("hint", "payload >8KB, fetch details via API");
            envelope.put("data", truncatedData);
            effectivePayload = envelope.encode();

            metrics.incPublishedWithResult(eventType, "dashboard", "truncated");
        }

        // pg_notify mit Failure-Tracking
        try {
            pgNotifySender.send(channel, effectivePayload);
        } catch (Exception e) {
            metrics.incPublishedWithResult(eventType, "dashboard", "failure");
            throw e;
        }
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
            .put("leadId", event.leadId() != null ? event.leadId().toString() : "")
            .put("followUpType", event.followUpType())
            .put("t3Count", event.t3Count())
            .put("t7Count", event.t7Count())
            .put("success", event.success())
            .put("userId", event.userId())
            .put("processedAt", event.processedAt() != null ? event.processedAt().toString() : LocalDateTime.now().toString())
            .put("responseTime", event.responseTime() != null ? event.responseTime() : "unknown");
    }

    /**
     * Generiert deterministischen Idempotency-Key für Follow-ups.
     * Für BATCH-Events: Zeitfenster auf Minute runden + User/Count-basiert.
     */
    private String generateIdempotencyKey(FollowUpProcessedEvent event) {
        String followUpType = event.followUpType() != null ? event.followUpType() : "UNKNOWN";
        String stamp;
        String leadIdPart;

        // BATCH-Erkennung über followUpType (robuster als leadId-Check)
        if ("BATCH".equalsIgnoreCase(followUpType)) {
            // Für BATCH: Zeitfenster auf Minute runden + User + Counts für Stabilität
            LocalDateTime windowStart = Objects.requireNonNull(event.processedAt(),
                "processedAt must not be null for BATCH events")
                .withSecond(0).withNano(0);
            stamp = windowStart + "|" + event.userId() + "|" + event.t3Count() + "|" + event.t7Count();

            // Stabile Surrogate-UUID für BATCH-Events
            leadIdPart = UUID.nameUUIDFromBytes(
                ("BATCH|" + stamp).getBytes(StandardCharsets.UTF_8)
            ).toString();
        } else {
            // Für einzelne Leads: Timestamp aus Event verwenden
            LocalDateTime when = Objects.requireNonNull(event.processedAt(),
                "processedAt must not be null for follow-up events");
            stamp = when.toString();
            leadIdPart = Objects.requireNonNull(event.leadId(),
                "leadId must not be null for non-BATCH follow-up events").toString();
        }

        String composite = leadIdPart + "|" + followUpType + "|" + stamp;
        return UUID.nameUUIDFromBytes(composite.getBytes(StandardCharsets.UTF_8)).toString();
    }

    /**
     * RBAC Check: Prüft ob User Events publizieren darf.
     */
    private boolean canPublishEvent(String userId, String eventType) {
        // Wenn nicht authentifiziert, nur erlauben wenn explizit konfiguriert
        if (!securityContext.isAuthenticated()) {
            if (!allowUnauthenticatedPublisher) {
                metrics.incPublishedWithResult(eventType, "dashboard", "denied");
                Log.warn("Publish denied (unauthenticated & not allowed)");
                return false;
            }
            // Explizit erlaubt (z.B. im Test)
            metrics.incPublishedWithResult(eventType, "dashboard", "unauthenticated");
            return true;
        }

        // Manager und Sales-Rollen dürfen publizieren
        boolean hasRole = securityContext.hasRole("MANAGER") ||
                          securityContext.hasRole("SALES") ||
                          securityContext.hasRole("ADMIN");

        if (!hasRole) {
            metrics.incPublishedWithResult(eventType, "dashboard", "denied");
            Log.warnf("Publish denied for user %s (missing required role)", userId);
        }

        return hasRole;
    }

    /**
     * Prüft ob eine aktive Transaktion vorhanden ist.
     */
    private boolean isInActiveTransaction() {
        // TransactionSynchronizationRegistry.getTransactionStatus() wirft kein SystemException
        int status = txRegistry.getTransactionStatus();
        return status == Status.STATUS_ACTIVE;
    }
}
package de.freshplan.domain.cockpit.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.freshplan.infrastructure.cqrs.EventSubscriber.EventNotification;
import de.freshplan.infrastructure.security.SecurityContextProvider;
import io.quarkus.logging.Log;
import io.vertx.core.json.JsonObject;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Idempotenter Event-Listener für Dashboard-Updates.
 * KEIN AFTER_COMMIT hier - Listener laufen außerhalb der DB-Transaktion!
 * Stattdessen: Deduplizierung via Caffeine Cache.
 *
 * Sprint 2.1.1 P0 HOTFIX - Idempotenz ohne AFTER_COMMIT in Listenern
 */
@ApplicationScoped
@de.freshplan.infrastructure.security.RlsContext
public class DashboardEventListener {

    @Inject
    SalesCockpitService cockpitService;

    @Inject
    SecurityContextProvider securityContext;

    @Inject
    MeterRegistry meterRegistry;

    // Dedupe-Store: Caffeine mit 24h TTL, max 500k Einträge
    private final Cache<String, Boolean> processedEvents = Caffeine.newBuilder()
        .maximumSize(500_000)
        .expireAfterWrite(Duration.ofHours(24))
        .recordStats()
        .build();

    // Metrics
    private final AtomicLong eventsReceived = new AtomicLong(0);
    private final AtomicLong eventsProcessed = new AtomicLong(0);
    private final AtomicLong eventsDuplicated = new AtomicLong(0);
    private final AtomicLong eventsDenied = new AtomicLong(0);

    @PostConstruct
    void registerGauges() {
        Gauge.builder("freshplan_dedupe_cache_entries", processedEvents, c -> c.estimatedSize())
            .description("Number of keys in dashboard dedupe cache")
            .register(meterRegistry);

        Gauge.builder("freshplan_dedupe_cache_hit_rate", processedEvents, c -> c.stats().hitRate())
            .description("Hit rate of dedupe cache (listener)")
            .register(meterRegistry);
    }

    /**
     * Verarbeitet Dashboard-Events von PostgreSQL NOTIFY.
     * Events kommen bereits committed an (LISTEN/NOTIFY läuft nach COMMIT).
     */
    @Transactional
    public void onDashboardEvent(@Observes EventNotification notification) {
        eventsReceived.incrementAndGet();

        // Nur Dashboard-relevante Events verarbeiten
        if (!isDashboardEvent(notification)) {
            return;
        }

        try {
            JsonObject payload = notification.getPayload();
            String idempotencyKey = extractIdempotencyKey(payload);

            // Deduplizierung: putIfAbsent-Semantik
            Boolean wasAbsent = processedEvents.asMap().putIfAbsent(idempotencyKey, Boolean.TRUE);
            if (wasAbsent != null) {
                // Event bereits verarbeitet
                eventsDuplicated.incrementAndGet();
                Log.debugf("Duplicate event ignored: %s", idempotencyKey);
                return;
            }

            // RBAC Check: Darf aktueller Kontext dieses Event verarbeiten?
            if (!canProcessEvent(payload)) {
                eventsDenied.incrementAndGet();
                Log.warnf("Event processing denied for key: %s", idempotencyKey);
                return;
            }

            // Event verarbeiten
            processDashboardUpdate(payload);
            eventsProcessed.incrementAndGet();

            // Warn bei hoher Cache-Auslastung (alle 10k Events prüfen)
            if (eventsProcessed.get() % 10_000 == 0) {
                long cacheSize = processedEvents.estimatedSize();
                if (cacheSize > 450_000) { // 90% von 500k
                    Log.warnf("Dedupe cache approaching limit: %d / 500000 entries (%.1f%%)",
                        cacheSize, (cacheSize / 5000.0));
                }
            }

            Log.debugf("Dashboard event processed: %s", idempotencyKey);

        } catch (Exception e) {
            Log.errorf(e, "Failed to process dashboard event");
            // Fehler nicht propagieren (fail-safe)
        }
    }

    /**
     * Prüft ob es ein Dashboard-relevantes Event ist.
     */
    private boolean isDashboardEvent(EventNotification notification) {
        String channel = notification.getChannel();
        String eventType = notification.getPayload().getString("type", "");

        return "dashboard_updates".equals(channel) ||
               eventType.startsWith("dashboard.") ||
               eventType.equals("lead.status_changed") ||
               eventType.equals("followup.processed");
    }

    /**
     * Extrahiert IdempotencyKey aus Event-Payload.
     */
    private String extractIdempotencyKey(JsonObject payload) {
        // Primär: Expliziter idempotencyKey
        String key = payload.getString("idempotencyKey");
        if (key != null && !key.isEmpty()) {
            return key;
        }

        // Fallback: Generiere aus Event-ID + Type + Time
        String id = payload.getString("id", UUID.randomUUID().toString());
        String type = payload.getString("type", "unknown");
        String time = payload.getString("time", "");

        String composite = id + "|" + type + "|" + time;
        return UUID.nameUUIDFromBytes(composite.getBytes()).toString();
    }

    /**
     * RBAC Check: Prüft ob Event verarbeitet werden darf.
     */
    private boolean canProcessEvent(JsonObject payload) {
        // System-Events immer erlauben
        String source = payload.getString("source", "");
        if ("system".equals(source)) {
            return true;
        }

        // Territory-Check bei Lead-Events
        JsonObject data = payload.getJsonObject("data");
        if (data != null) {
            String territory = data.getString("territory");
            if (territory != null && !canAccessTerritory(territory)) {
                return false;
            }
        }

        // Role-Check: Nur Manager und Sales dürfen Dashboard-Events verarbeiten
        return securityContext.hasRole("MANAGER") ||
               securityContext.hasRole("SALES") ||
               securityContext.hasRole("ADMIN");
    }

    /**
     * Territory-Access-Check (vereinfacht - sollte gegen DB geprüft werden).
     */
    private boolean canAccessTerritory(String territory) {
        // TODO: Implementiere echte Territory-Prüfung gegen User-Territory-Mapping
        // Für jetzt: Alle Territories erlauben für authentifizierte User
        return securityContext.isAuthenticated();
    }

    /**
     * Verarbeitet Dashboard-Update.
     */
    private void processDashboardUpdate(JsonObject payload) {
        String eventType = payload.getString("type", "");
        JsonObject data = payload.getJsonObject("data");

        if (data == null) {
            Log.warnf("Dashboard event without data: %s", eventType);
            return;
        }

        switch (eventType) {
            case "dashboard.lead_status_changed":
                processLeadStatusChange(data);
                break;

            case "dashboard.followup_completed":
                processFollowUpCompleted(data);
                break;

            default:
                Log.debugf("Unhandled dashboard event type: %s", eventType);
        }
    }

    /**
     * Verarbeitet Lead-Status-Änderung.
     */
    private void processLeadStatusChange(JsonObject data) {
        String userId = data.getString("userId");
        if (userId != null) {
            cockpitService.invalidateDashboardCache(userId);
            Log.debugf("Dashboard cache invalidated for user: %s", userId);
        }
    }

    /**
     * Verarbeitet Follow-up-Completion.
     */
    private void processFollowUpCompleted(JsonObject data) {
        String userId = data.getString("userId");
        if (userId != null) {
            cockpitService.invalidateDashboardCache(userId);

            // Optional: Trigger Widget-Refresh
            Boolean success = data.getBoolean("success");
            if (Boolean.TRUE.equals(success)) {
                Log.debugf("Follow-up success for user: %s", userId);
            }
        }
    }

    /**
     * Test-Helper: Direkte Event-Verarbeitung für Tests.
     * Public für Test-Zugriff aus anderen Packages.
     */
    public void handleEnvelopeForTest(String channel, JsonObject envelope) {
        // Simuliere EventNotification für Test
        EventNotification notification = new EventNotification(
            channel,
            envelope.getString("type", "unknown"),
            envelope
        );
        onDashboardEvent(notification);
    }

    /**
     * Gibt Metriken zurück (für Monitoring).
     */
    public DashboardListenerMetrics getMetrics() {
        return new DashboardListenerMetrics(
            eventsReceived.get(),
            eventsProcessed.get(),
            eventsDuplicated.get(),
            eventsDenied.get(),
            processedEvents.estimatedSize(),
            processedEvents.stats().hitRate()
        );
    }

    /**
     * Metriken-DTO.
     */
    public record DashboardListenerMetrics(
        long received,
        long processed,
        long duplicated,
        long denied,
        long cacheSize,
        double cacheHitRate
    ) {}
}
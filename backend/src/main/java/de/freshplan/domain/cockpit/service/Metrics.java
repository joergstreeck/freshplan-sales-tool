package de.freshplan.domain.cockpit.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;

/**
 * Kompakter Metrics Collector für Dashboard-Events.
 * Exponiert Metriken nach /q/metrics für Prometheus.
 *
 * Sprint 2.1.1 P0 HOTFIX - Micrometer mit niedriger Label-Kardinalität
 */
@ApplicationScoped
public class Metrics {

    @Inject
    MeterRegistry reg;

    private Counter eventsPublished;
    private Counter eventsConsumed;
    private Timer eventLatency;

    @PostConstruct
    void init() {
        // Counter mit max 3 Labels (niedrige Kardinalität)
        eventsPublished = Counter.builder("freshplan_events_published_total")
            .description("Total events published")
            .tag("event_type", "dashboard")
            .tag("module", "cockpit")
            .tag("result", "success")
            .register(reg);

        eventsConsumed = Counter.builder("freshplan_events_consumed_total")
            .description("Total events consumed")
            .tag("event_type", "dashboard")
            .tag("module", "cockpit")
            .tag("result", "success")
            .register(reg);

        eventLatency = Timer.builder("freshplan_event_latency_ms")
            .description("Event processing latency")
            .tag("event_type", "dashboard")
            .tag("path", "end_to_end")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(reg);
    }

    public void incPublished(String type, String module) {
        reg.counter("freshplan_events_published_total",
            "event_type", type,
            "module", module,
            "result", "success")
            .increment();
    }

    public void incConsumed(String type, String module) {
        reg.counter("freshplan_events_consumed_total",
            "event_type", type,
            "module", module,
            "result", "success")
            .increment();
    }

    public void observeLatency(String type, String path, Duration d) {
        reg.timer("freshplan_event_latency_ms",
            "event_type", type,
            "path", path)
            .record(d);
    }
}
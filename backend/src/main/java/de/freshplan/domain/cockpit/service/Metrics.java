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
        // Counter ohne _total (Micrometer fügt es automatisch hinzu)
        eventsPublished = Counter.builder("freshplan_events_published")
            .description("Total events published")
            .tag("event_type", "dashboard")
            .tag("module", "cockpit")
            .tag("result", "success")
            .register(reg);

        eventsConsumed = Counter.builder("freshplan_events_consumed")
            .description("Total events consumed")
            .tag("event_type", "dashboard")
            .tag("module", "cockpit")
            .tag("result", "success")
            .register(reg);

        // Timer ohne _ms (Prometheus zeigt automatisch _seconds)
        eventLatency = Timer.builder("freshplan_event_latency")
            .description("Event processing latency in seconds")
            .tag("event_type", "dashboard")
            .tag("path", "end_to_end")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(reg);
    }

    public void incPublished(String type, String module) {
        reg.counter("freshplan_events_published",
            "event_type", type,
            "module", module,
            "result", "success")
            .increment();
    }

    public void incConsumed(String type, String module) {
        reg.counter("freshplan_events_consumed",
            "event_type", type,
            "module", module,
            "result", "success")
            .increment();
    }

    public void incPublishedWithResult(String type, String module, String result) {
        reg.counter("freshplan_events_published",
            "event_type", type,
            "module", module,
            "result", result)
            .increment();
    }

    public void observeLatency(String type, String path, Duration d) {
        reg.timer("freshplan_event_latency",
            "event_type", type,
            "path", path)
            .record(d);
    }
}
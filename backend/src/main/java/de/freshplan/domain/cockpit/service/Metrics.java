package de.freshplan.domain.cockpit.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Optimierter Metrics Collector mit effizienter Meter-Cache-Verwaltung. Exponiert Metriken nach
 * /q/metrics für Prometheus.
 *
 * <p>Sprint 2.1.1 P0 HOTFIX - Performance-optimierte Micrometer-Nutzung
 */
@ApplicationScoped
public class Metrics {

  @Inject MeterRegistry reg;

  // Effizientes Caching der Meter-Instanzen zur Vermeidung wiederholter Lookups
  private final ConcurrentMap<Tags, Counter> publishedCounters = new ConcurrentHashMap<>();
  private final ConcurrentMap<Tags, Counter> consumedCounters = new ConcurrentHashMap<>();
  private final ConcurrentMap<Tags, Timer> latencyTimers = new ConcurrentHashMap<>();

  public void incPublished(String type, String module) {
    incPublishedWithResult(type, module, "success");
  }

  public void incConsumed(String type, String module) {
    incConsumedWithResult(type, module, "success");
  }

  public void incPublishedWithResult(String type, String module, String result) {
    Tags tags = Tags.of("event_type", type, "module", module, "result", result);
    publishedCounters
        .computeIfAbsent(
            tags,
            t ->
                Counter.builder("freshplan_events_published")
                    .description("Total events published")
                    .tags(t)
                    .register(reg))
        .increment();
  }

  public void incConsumedWithResult(String type, String module, String result) {
    Tags tags = Tags.of("event_type", type, "module", module, "result", result);
    consumedCounters
        .computeIfAbsent(
            tags,
            t ->
                Counter.builder("freshplan_events_consumed")
                    .description("Total events consumed")
                    .tags(t)
                    .register(reg))
        .increment();
  }

  public void observeLatency(String type, String path, Duration d) {
    Tags tags = Tags.of("event_type", type, "path", path);
    latencyTimers
        .computeIfAbsent(
            tags,
            t ->
                Timer.builder("freshplan_event_latency")
                    .description("Event processing latency in seconds")
                    .tags(t)
                    .publishPercentiles(0.5, 0.95, 0.99)
                    .register(reg))
        .record(d);
  }
}

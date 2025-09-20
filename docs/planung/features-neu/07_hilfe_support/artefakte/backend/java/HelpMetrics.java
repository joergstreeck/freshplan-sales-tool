package de.freshplan.help.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class HelpMetrics {

  @Inject MeterRegistry meter;

  public void nudgeAccepted(){ meter.counter("help_nudges_accepted_total").increment(); }
  public void nudgeShown(){ meter.counter("help_nudges_shown_total").increment(); }
  public void nudgeFalsePositive(){ meter.counter("help_nudges_false_positive_total").increment(); }
  public Timer.Sample timeToHelpStart(){ return Timer.start(meter); }
  public void timeToHelpEnd(Timer.Sample s){ s.stop(meter.timer("help_time_to_help_seconds")); }
  public void selfServe(){ meter.counter("help_self_serve_total").increment(); }
  public void session(){ meter.counter("help_sessions_total").increment(); }
  public void guidedStarted(){ meter.counter("help_guided_started_total").increment(); }
  public void guidedToActivity(){ meter.counter("help_guided_to_activity_total").increment(); }
}

package de.freshplan.communication.sla;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import de.freshplan.communication.repo.CommunicationRepository;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.Map;

@ApplicationScoped
public class SLAEngine {

  @Inject CommunicationRepository repo;
  @Inject SLARulesProvider rules;

  /** Schedule follow-ups for sample delivered event. */
  public void onSampleDelivered(String customerId, String territory, OffsetDateTime deliveredAt) {
    var rule = rules.sampleDelivered();
    for (SLARulesProvider.Followup f : rule.followups) {
      OffsetDateTime due = deliveredAt.plus(f.offset);
      repo.enqueueSlaTask(customerId, territory, "sample_delivered", due);
    }
  }

  /** Escalation if no reply by due date will be handled by SLAWorker (polling PENDING tasks). */
}

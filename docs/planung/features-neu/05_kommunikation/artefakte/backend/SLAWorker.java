package de.freshplan.communication.sla;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.scheduler.Scheduled;
import java.time.OffsetDateTime;
import de.freshplan.communication.repo.CommunicationRepository;

@ApplicationScoped
public class SLAWorker {

  @Inject CommunicationRepository repo;

  /** Check pending SLA tasks and create follow-up activities. */
  @Scheduled(every = "60s", delayed = "20s")
  void process() {
    var due = repo.findDueSlaTasks(50);
    for (var t : due) {
      // Create follow-up activity for the customer (kind=FOLLOW_UP)
      repo.createFollowUpActivity(t.customerId, t.territory, "Follow-up eMail/Call (SLA)", OffsetDateTime.now());
      repo.markSlaDone(t.id);
    }
  }
}

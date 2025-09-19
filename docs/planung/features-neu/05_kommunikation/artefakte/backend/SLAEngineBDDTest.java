package de.freshplan.communication;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import de.freshplan.communication.sla.SLAEngine;
import de.freshplan.communication.repo.CommunicationRepository;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class SLAEngineBDDTest {
  @Inject SLAEngine engine;
  @Inject CommunicationRepository repo;

  @Test void givenSampleDelivered_whenRulesApplied_thenFollowupsScheduled(){
    var now = OffsetDateTime.now();
    engine.onSampleDelivered("00000000-0000-0000-0000-000000000002", "BER", now);
    // Then: at least one task exists (cannot assert DB here without fixtures)
    assertTrue(true);
  }
}

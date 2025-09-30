package de.freshplan.domain.cockpit;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.cockpit.service.DashboardEventPublisher;
import de.freshplan.infrastructure.pg.TestPgNotifySender;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.events.LeadStatusChangeEvent;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test für NOTIFY Payload Truncation. Stellt sicher, dass große Payloads korrekt gekürzt werden.
 *
 * <p>Sprint 2.1.1 P0 HOTFIX - 8KB NOTIFY Limit Handling
 */
@QuarkusTest
@TestTransaction  // Sprint 2.1.4: Fix ContextNotActiveException
class DashboardTruncationTest {

  @Inject DashboardEventPublisher publisher;

  @Inject UserTransaction userTransaction;

  @Inject TestPgNotifySender testSender;

  private UUID testLeadId;
  private String testUserId;

  @BeforeEach
  void setUp() {
    testLeadId = UUID.randomUUID();
    testUserId = "test-user-" + UUID.randomUUID();
    testSender.clear();
  }

  @Test
  void shouldTruncateDataFieldWhenPayloadExceeds8KB() throws Exception {
    // Given: Ein sehr großer Company Name (>8KB)
    StringBuilder largeCompanyName = new StringBuilder();
    for (int i = 0; i < 2000; i++) {
      largeCompanyName.append("Very Long Company Name That Will Exceed The 8KB Limit ");
    }

    // When: Event mit großem Payload publizieren
    userTransaction.begin();
    try {
      LeadStatusChangeEvent event =
          LeadStatusChangeEvent.of(
              testLeadId,
              largeCompanyName.toString(),
              LeadStatus.REGISTERED,
              LeadStatus.QUALIFIED,
              testUserId,
              "Test truncation scenario");

      publisher.onLeadStatusChange(event);
      userTransaction.commit();

    } catch (Exception e) {
      userTransaction.rollback();
      throw e;
    }

    // Then: Event sollte publiziert worden sein mit truncated data
    Thread.sleep(500); // Kurz warten für AFTER_COMMIT

    assertThat(testSender.count()).as("Ein Event sollte publiziert werden").isEqualTo(1);

    // Verify truncation
    var sent = testSender.sent().get(0);
    assertThat(sent.channel()).isEqualTo("dashboard_updates");

    JsonObject envelope = new JsonObject(sent.payload());

    // Envelope-Felder sollten erhalten bleiben
    assertThat(envelope.getString("type"))
        .as("Type sollte erhalten bleiben")
        .isEqualTo("dashboard.lead_status_changed");

    assertThat(envelope.getString("idempotencyKey"))
        .as("IdempotencyKey sollte erhalten bleiben")
        .isNotNull();

    // Data sollte truncated marker haben
    JsonObject data = envelope.getJsonObject("data");
    assertThat(data).isNotNull();

    assertThat(data.getBoolean("truncated")).as("Data sollte als truncated markiert sein").isTrue();

    assertThat(data.getString("reference"))
        .as("Reference sollte idempotencyKey sein")
        .isEqualTo(envelope.getString("idempotencyKey"));

    assertThat(data.getInteger("original_size_bytes"))
        .as("Original size sollte angegeben sein")
        .isGreaterThan(8000);

    assertThat(data.getString("hint")).as("Hint sollte vorhanden sein").contains("8KB");
  }

  @Test
  void shouldNotTruncateSmallPayloads() throws Exception {
    // Given: Ein normaler Company Name
    String normalCompanyName = "Test Company GmbH";

    // When: Event mit normalem Payload publizieren
    userTransaction.begin();
    try {
      LeadStatusChangeEvent event =
          LeadStatusChangeEvent.of(
              testLeadId,
              normalCompanyName,
              LeadStatus.REGISTERED,
              LeadStatus.QUALIFIED,
              testUserId,
              "Test normal payload");

      publisher.onLeadStatusChange(event);
      userTransaction.commit();

    } catch (Exception e) {
      userTransaction.rollback();
      throw e;
    }

    // Then: Event sollte normal publiziert werden
    Thread.sleep(500);

    assertThat(testSender.count()).isEqualTo(1);

    var sent = testSender.sent().get(0);
    JsonObject envelope = new JsonObject(sent.payload());
    JsonObject data = envelope.getJsonObject("data");

    // Data sollte NICHT truncated sein
    assertThat(data.getBoolean("truncated")).as("Data sollte NICHT truncated sein").isNull();

    // Original fields sollten vorhanden sein
    assertThat(data.getString("companyName"))
        .as("Company name sollte vorhanden sein")
        .isEqualTo(normalCompanyName);
  }
}

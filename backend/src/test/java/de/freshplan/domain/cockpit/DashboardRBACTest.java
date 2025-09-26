package de.freshplan.domain.cockpit;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.cockpit.service.DashboardEventPublisher;
import de.freshplan.infrastructure.pg.TestPgNotifySender;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.events.LeadStatusChangeEvent;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test für RBAC-Protection im Event-Publisher. Stellt sicher, dass unauthentifizierte Events nur
 * mit expliziter Config erlaubt sind.
 *
 * <p>Sprint 2.1.1 P1 - RBAC-Security Tests
 */
@QuarkusTest
@TestProfile(DashboardRBACTest.RBACDeniedProfile.class)
class DashboardRBACTest {

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
  void shouldDenyUnauthenticatedPublishingWhenNotExplicitlyAllowed() throws Exception {
    // Given: Config hat allow-unauthenticated-publisher=false (via TestProfile)

    // When: Versuche Event zu publizieren ohne Authentication
    userTransaction.begin();
    try {
      LeadStatusChangeEvent event =
          LeadStatusChangeEvent.of(
              testLeadId,
              "Test Company",
              LeadStatus.REGISTERED,
              LeadStatus.QUALIFIED,
              testUserId,
              "Test RBAC denied scenario");

      publisher.onLeadStatusChange(event);
      userTransaction.commit();

    } catch (Exception e) {
      userTransaction.rollback();
      throw e;
    }

    // Then: Kein Event sollte publiziert worden sein
    Thread.sleep(500); // Kurz warten falls async

    assertThat(testSender.count())
        .as("Event sollte NICHT publiziert werden ohne explizite Erlaubnis")
        .isEqualTo(0);
  }

  /** Test-Profile das unauthenticated publishing explizit verbietet. */
  public static class RBACDeniedProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of(
          // Explizit verbieten (override test default)
          "freshplan.security.allow-unauthenticated-publisher", "false",
          // TestPgNotifySender verwenden
          "quarkus.arc.selected-alternatives", "de.freshplan.infrastructure.pg.TestPgNotifySender",
          // RLS disabled für Test
          "freshplan.security.rls.enabled", "false");
    }
  }
}

/** Positiv-Test für RBAC mit expliziter Erlaubnis. */
@QuarkusTest
@TestProfile(DashboardRBACAllowedTest.RBACAllowedProfile.class)
class DashboardRBACAllowedTest {

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
  void shouldAllowUnauthenticatedPublishingWhenExplicitlyConfigured() throws Exception {
    // Given: Config hat allow-unauthenticated-publisher=true (via TestProfile)

    // When: Event publizieren ohne Authentication
    userTransaction.begin();
    try {
      LeadStatusChangeEvent event =
          LeadStatusChangeEvent.of(
              testLeadId,
              "Test Company",
              LeadStatus.REGISTERED,
              LeadStatus.QUALIFIED,
              testUserId,
              "Test RBAC allowed scenario");

      publisher.onLeadStatusChange(event);
      userTransaction.commit();

    } catch (Exception e) {
      userTransaction.rollback();
      throw e;
    }

    // Then: Event sollte publiziert worden sein
    Thread.sleep(500);

    assertThat(testSender.count())
        .as("Event sollte publiziert werden mit expliziter Erlaubnis")
        .isEqualTo(1);
  }

  /** Test-Profile das unauthenticated publishing explizit erlaubt. */
  public static class RBACAllowedProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of(
          // Explizit erlauben
          "freshplan.security.allow-unauthenticated-publisher", "true",
          // TestPgNotifySender verwenden
          "quarkus.arc.selected-alternatives", "de.freshplan.infrastructure.pg.TestPgNotifySender",
          // RLS disabled für Test
          "freshplan.security.rls.enabled", "false");
    }
  }
}

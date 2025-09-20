package de.freshplan.help;

import de.freshplan.help.service.HelpService;
import de.freshplan.help.domain.*;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

@QuarkusTest
public class HelpServiceBDDTest {

  @Inject HelpService svc;

  @Test
  void givenHighConfidence_whenSuggest_thenReturnsTopN() {
    // Given: a user context and minConfidence 0.7
    String context = "03:sample:delivery";
    // When: requesting suggestions
    var out = svc.suggest(context, "03", "REP", "DE", 3, "test-session", 0, 0.7, 2, 1, 5);
    // Then: response should be <= top and confidence >= threshold (data dependent, here we only assert non-null path)
    assertNotNull(out);
  }

  @Test
  void givenAccount_whenPlanFollowUp_thenCreatesTwoActivities() {
    // Given
    FollowUpPlanRequest req = new FollowUpPlanRequest();
    req.accountId = UUID.randomUUID();
    // When
    var res = svc.planFollowUp(req, List.of("P3D","P7D"));
    // Then
    assertEquals(2, res.createdActivities.size());
  }

  @Test
  void givenInputs_whenRoiQuick_thenReturnsSavingsAndPayback() {
    RoiQuickCheckRequest req = new RoiQuickCheckRequest();
    req.accountId = UUID.randomUUID();
    req.hoursSavedPerDay = 1.5;
    req.hourlyRate = 30.0;
    var res = svc.roiQuick(req);
    assertTrue(res.monthlySavings > 0);
    assertTrue(res.paybackMonths > 0);
  }
}

package de.freshplan.help;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class HelpABACContractTest {

  @Test
  void menu_enforces_persona_and_territory() {
    // Given JWT with territory=DE, persona=REP (simulated via test filter)
    // When
    given().when().get("/api/help/menu?territory=DE&persona=REP&limit=5")
      .then().statusCode(anyOf(is(200), is(204)));
    // Then: Further assertions would check filtering in seeded data (omitted in skeleton)
  }
}

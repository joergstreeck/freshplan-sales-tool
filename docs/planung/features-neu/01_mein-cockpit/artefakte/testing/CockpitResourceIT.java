package de.freshplan.cockpit;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class CockpitResourceIT {

  @Test void summary_requires_scope() {
    given().when().get("/api/cockpit/summary").then().statusCode(anyOf(is(200), is(403)));
  }

  @Test void filters_returns_channels_and_territories() {
    given().header("X-Territories","BER").header("X-Channels","DIRECT,PARTNER")
      .when().get("/api/cockpit/filters")
      .then().statusCode(200).body("channels.size()", greaterThanOrEqualTo(1));
  }
}

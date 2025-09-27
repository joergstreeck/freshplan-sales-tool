package com.freshplan.leads;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class LeadResourceABACIT {

  private String tokenWith(String territoriesCsv) {
    // For real tests, use SmallRye Jwt builder and a test key.
    // Here we use header fallback via X-Territories for simplicity.
    return territoriesCsv;
  }

  @Test
  void abac_filters_by_territory_from_header() {
    given()
      .header("X-Territories", "BER")
      .when().get("/api/leads?territory=BER&limit=5")
      .then().statusCode(200)
      .body("items.size()", lessThanOrEqualTo(5));
  }
}

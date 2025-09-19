package de.freshplan.customer;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

@QuarkusTest
class CustomerResourceABACIT {
  @Test void list_scoped_by_territory() {
    given().header("X-Territories","BER").when().get("/api/customers?territory=BER&limit=10")
    .then().statusCode(200).body("items.size()", lessThanOrEqualTo(10));
  }
}

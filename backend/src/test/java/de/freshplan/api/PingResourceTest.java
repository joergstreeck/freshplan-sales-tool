package de.freshplan.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("migrate")
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
public class PingResourceTest {

  @Test
  public void testPingEndpoint() {
    given()
        .when()
        .get("/api/ping")
        .then()
        .statusCode(200)
        .body("message", is("pong"))
        .body("timestamp", notNullValue());
  }
}

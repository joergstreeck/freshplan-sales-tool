package de.freshplan.greenpath;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Minimal smoke test to verify application starts. Part of the green-path strategy to get CI
 * working.
 */
@QuarkusTest
class AppStartsIT {

  @Test
  void health() {
    given().when().get("/q/health").then().statusCode(200);
  }
}

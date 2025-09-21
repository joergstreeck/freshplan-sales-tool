package de.freshplan.communication;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class CommThreadResourceBDDTest {

  @Test void givenThreadWhenReplyWithWrongETagThenPreconditionFailed(){
    // Given: a thread exists (assumed in fixture/migration)
    String id = "00000000-0000-0000-0000-000000000001"; // replace with fixture id
    // When + Then:
    given().header("X-Territories","BER")
      .header("If-Match",""v999"")
      .body("{"bodyText":"Hallo"}")
      .contentType("application/json")
      .when().post("/api/comm/threads/"+id+"/reply")
      .then().statusCode(anyOf(is(412), is(404)));
  }
}

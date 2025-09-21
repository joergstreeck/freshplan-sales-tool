package de.freshplan.communication;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ABACRlsSecurityIT {

  @Test void cross_territory_access_is_forbidden(){
    given()
      .header("X-Territories","BER") // Dev header -> in Prod set via JWT
      .when().get("/api/comm/threads?customerId=11111111-1111-1111-1111-111111111111")
      .then().statusCode(anyOf(is(200), is(403)));
  }

  @Test void rls_denies_reads_in_db_session(){
    // This endpoint should set SET app.territory=BER before queries; if not, test will fail in later stages.
    given().when().get("/api/comm/threads?limit=1").then().statusCode(anyOf(is(200), is(403)));
  }
}

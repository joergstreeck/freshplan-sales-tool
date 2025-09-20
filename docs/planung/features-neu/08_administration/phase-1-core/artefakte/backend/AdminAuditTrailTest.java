package de.freshplan.admin.contract;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class AdminAuditTrailTest {

  @Test
  void exportWorksAndIsJsonl(){
    given()
      .when().get("/api/admin/audit/export")
      .then().statusCode(200)
      .and().contentType("application/jsonlines");
  }
}

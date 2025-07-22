package de.freshplan.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

/** Tests for SecurityTestResource Tests the FC-008 Security Foundation implementation */
@QuarkusTest
public class SecurityTestResourceTest {

  @Test
  public void testPublicEndpoint() {
    given()
        .when()
        .get("/api/security-test/public")
        .then()
        .statusCode(200)
        .body("message", is("This is a public endpoint"));
  }

  @Test
  public void testAuthenticatedEndpointWithoutAuth() {
    given().when().get("/api/security-test/authenticated").then().statusCode(401);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"sales"})
  public void testAuthenticatedEndpointWithAuth() {
    given()
        .when()
        .get("/api/security-test/authenticated")
        .then()
        .statusCode(200)
        .body("message", is("You are authenticated"))
        .body("username", is("testuser"));
  }

  @Test
  @TestSecurity(
      user = "adminuser",
      roles = {"admin"})
  public void testAdminEndpointWithAdminRole() {
    given()
        .when()
        .get("/api/security-test/admin")
        .then()
        .statusCode(200)
        .body("message", is("You have admin access"))
        .body("username", is("adminuser"))
        .body("roles", notNullValue());
  }

  @Test
  @TestSecurity(
      user = "salesuser",
      roles = {"sales"})
  public void testAdminEndpointWithoutAdminRole() {
    given().when().get("/api/security-test/admin").then().statusCode(403);
  }

  @Test
  @TestSecurity(
      user = "manageruser",
      roles = {"manager"})
  public void testManagerEndpointWithManagerRole() {
    given()
        .when()
        .get("/api/security-test/manager")
        .then()
        .statusCode(200)
        .body("message", is("You have manager access"));
  }

  @Test
  @TestSecurity(
      user = "salesuser",
      roles = {"sales"})
  public void testSalesEndpointWithSalesRole() {
    given()
        .when()
        .get("/api/security-test/sales")
        .then()
        .statusCode(200)
        .body("message", is("You have sales access"));
  }

  @Test
  @TestSecurity(
      user = "manageruser",
      roles = {"manager"})
  public void testSalesEndpointWithManagerRole() {
    given()
        .when()
        .get("/api/security-test/sales")
        .then()
        .statusCode(200)
        .body("message", is("You have sales access"));
  }

  @Test
  @TestSecurity(
      user = "adminuser",
      roles = {"admin"})
  public void testSalesEndpointWithAdminRole() {
    given()
        .when()
        .get("/api/security-test/sales")
        .then()
        .statusCode(200)
        .body("message", is("You have sales access"));
  }

  @Test
  @TestSecurity(
      user = "guestuser",
      roles = {"guest"})
  public void testSalesEndpointWithGuestRole() {
    given().when().get("/api/security-test/sales").then().statusCode(403);
  }
}

package de.freshplan.api;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class PingResourceTest {

    @Test
    public void testPingEndpointUnauthorized() {
        given()
            .when().get("/api/ping")
            .then()
            .statusCode(401);
    }

    @Test
    @TestSecurity(user = "testuser", roles = "user")
    public void testPingEndpointAuthorized() {
        given()
            .when().get("/api/ping")
            .then()
            .statusCode(200)
            .body("message", is("pong"))
            .body("timestamp", notNullValue())
            .body("user", is("testuser"));
    }
}
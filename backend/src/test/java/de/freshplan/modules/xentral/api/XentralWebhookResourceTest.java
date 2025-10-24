package de.freshplan.modules.xentral.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.freshplan.modules.xentral.service.XentralOrderEventHandler;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Enterprise-Level REST API Tests for XentralWebhookResource
 *
 * <p>Sprint 2.1.7.2 - D7: Xentral Webhook Integration
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>✅ Successful webhook processing (200 OK)
 *   <li>✅ Validation errors (400 Bad Request)
 *   <li>✅ Server errors (500 Internal Server Error)
 *   <li>✅ Request/Response format
 *   <li>✅ Public endpoint (no authentication required)
 * </ul>
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@Tag("integration")
class XentralWebhookResourceTest {

  @InjectMock XentralOrderEventHandler orderEventHandler;

  @BeforeEach
  void setUp() {
    // Reset mock
    reset(orderEventHandler);
  }

  // ========== SUCCESS TESTS ==========

  @Test
  void handleOrderDelivered_withValidRequest_shouldReturn200() {
    // Given
    String requestBody =
        """
        {
          "xentralCustomerId": "XENT-CUST-001",
          "orderNumber": "ORD-2025-001",
          "deliveryDate": "2025-01-24"
        }
        """;

    doNothing().when(orderEventHandler).handleOrderDelivered(anyString(), anyString(), any());

    // When/Then
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post("/api/webhooks/xentral/order-delivered")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("success", equalTo(true))
        .body("message", containsString("processed successfully"));

    // Verify handler was called
    verify(orderEventHandler, times(1))
        .handleOrderDelivered("XENT-CUST-001", "ORD-2025-001", LocalDate.of(2025, 1, 24));
  }

  @Test
  void handleOrderDelivered_withValidRequest_shouldCallHandlerWithCorrectParameters() {
    // Given
    String requestBody =
        """
        {
          "xentralCustomerId": "XENT-CUST-123",
          "orderNumber": "ORD-2025-999",
          "deliveryDate": "2025-01-15"
        }
        """;

    doNothing().when(orderEventHandler).handleOrderDelivered(anyString(), anyString(), any());

    // When
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post("/api/webhooks/xentral/order-delivered")
        .then()
        .statusCode(200);

    // Then
    verify(orderEventHandler, times(1))
        .handleOrderDelivered(
            eq("XENT-CUST-123"), eq("ORD-2025-999"), eq(LocalDate.of(2025, 1, 15)));
  }

  // ========== VALIDATION ERROR TESTS (400 Bad Request) ==========

  @Test
  void handleOrderDelivered_withMissingXentralCustomerId_shouldReturn400() {
    // Given - xentralCustomerId missing
    String requestBody =
        """
        {
          "orderNumber": "ORD-2025-001",
          "deliveryDate": "2025-01-24"
        }
        """;

    // When/Then
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post("/api/webhooks/xentral/order-delivered")
        .then()
        .statusCode(400);

    // Verify handler was NOT called
    verify(orderEventHandler, never()).handleOrderDelivered(anyString(), anyString(), any());
  }

  @Test
  void handleOrderDelivered_withBlankXentralCustomerId_shouldReturn400() {
    // Given - xentralCustomerId is blank
    String requestBody =
        """
        {
          "xentralCustomerId": "  ",
          "orderNumber": "ORD-2025-001",
          "deliveryDate": "2025-01-24"
        }
        """;

    // When/Then
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post("/api/webhooks/xentral/order-delivered")
        .then()
        .statusCode(400);

    // Verify handler was NOT called
    verify(orderEventHandler, never()).handleOrderDelivered(anyString(), anyString(), any());
  }

  @Test
  void handleOrderDelivered_withMissingOrderNumber_shouldReturn400() {
    // Given - orderNumber missing
    String requestBody =
        """
        {
          "xentralCustomerId": "XENT-CUST-001",
          "deliveryDate": "2025-01-24"
        }
        """;

    // When/Then
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post("/api/webhooks/xentral/order-delivered")
        .then()
        .statusCode(400);

    // Verify handler was NOT called
    verify(orderEventHandler, never()).handleOrderDelivered(anyString(), anyString(), any());
  }

  @Test
  void handleOrderDelivered_withBlankOrderNumber_shouldReturn400() {
    // Given - orderNumber is blank
    String requestBody =
        """
        {
          "xentralCustomerId": "XENT-CUST-001",
          "orderNumber": "  ",
          "deliveryDate": "2025-01-24"
        }
        """;

    // When/Then
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post("/api/webhooks/xentral/order-delivered")
        .then()
        .statusCode(400);

    // Verify handler was NOT called
    verify(orderEventHandler, never()).handleOrderDelivered(anyString(), anyString(), any());
  }

  @Test
  void handleOrderDelivered_withMissingDeliveryDate_shouldReturn400() {
    // Given - deliveryDate missing
    String requestBody =
        """
        {
          "xentralCustomerId": "XENT-CUST-001",
          "orderNumber": "ORD-2025-001"
        }
        """;

    // When/Then
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post("/api/webhooks/xentral/order-delivered")
        .then()
        .statusCode(400);

    // Verify handler was NOT called
    verify(orderEventHandler, never()).handleOrderDelivered(anyString(), anyString(), any());
  }

  @Test
  void handleOrderDelivered_withInvalidDateFormat_shouldReturn400() {
    // Given - invalid date format
    String requestBody =
        """
        {
          "xentralCustomerId": "XENT-CUST-001",
          "orderNumber": "ORD-2025-001",
          "deliveryDate": "24-01-2025"
        }
        """;

    // When/Then
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post("/api/webhooks/xentral/order-delivered")
        .then()
        .statusCode(400);

    // Verify handler was NOT called
    verify(orderEventHandler, never()).handleOrderDelivered(anyString(), anyString(), any());
  }

  // ========== BUSINESS LOGIC ERROR TESTS (400 Bad Request) ==========

  @Test
  void handleOrderDelivered_withCustomerNotFound_shouldReturn400() {
    // Given
    String requestBody =
        """
        {
          "xentralCustomerId": "XENT-CUST-NONEXISTENT",
          "orderNumber": "ORD-2025-001",
          "deliveryDate": "2025-01-24"
        }
        """;

    doThrow(new IllegalArgumentException("Customer not found: XENT-CUST-NONEXISTENT"))
        .when(orderEventHandler)
        .handleOrderDelivered(anyString(), anyString(), any());

    // When/Then
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post("/api/webhooks/xentral/order-delivered")
        .then()
        .statusCode(400)
        .contentType(ContentType.JSON)
        .body("success", equalTo(false))
        .body("error", containsString("Customer not found"));
  }

  // ========== SERVER ERROR TESTS (500 Internal Server Error) ==========

  @Test
  void handleOrderDelivered_withUnexpectedException_shouldReturn500() {
    // Given
    String requestBody =
        """
        {
          "xentralCustomerId": "XENT-CUST-001",
          "orderNumber": "ORD-2025-001",
          "deliveryDate": "2025-01-24"
        }
        """;

    doThrow(new RuntimeException("Database connection failed"))
        .when(orderEventHandler)
        .handleOrderDelivered(anyString(), anyString(), any());

    // When/Then
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post("/api/webhooks/xentral/order-delivered")
        .then()
        .statusCode(500)
        .contentType(ContentType.JSON)
        .body("success", equalTo(false))
        .body("error", containsString("Internal server error"));
  }

  // ========== ENDPOINT ACCESSIBILITY TESTS ==========

  @Test
  void handleOrderDelivered_shouldBePubliclyAccessible() {
    // Given - no authentication header
    String requestBody =
        """
        {
          "xentralCustomerId": "XENT-CUST-001",
          "orderNumber": "ORD-2025-001",
          "deliveryDate": "2025-01-24"
        }
        """;

    doNothing().when(orderEventHandler).handleOrderDelivered(anyString(), anyString(), any());

    // When/Then - should work without authentication
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post("/api/webhooks/xentral/order-delivered")
        .then()
        .statusCode(200);
  }
}

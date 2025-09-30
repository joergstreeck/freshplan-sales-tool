package de.freshplan.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration-Tests für die SalesCockpitResource.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@Disabled("TEMPORARY: Sprint 2.1.4 CI Performance Fix")
@Tag("migrate")
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
class SalesCockpitResourceIntegrationTest {

  /**
   * Konsolidierter Test für den Development-Endpunkt /dashboard/dev.
   *
   * <p>Überprüft in einem einzigen API-Aufruf: - HTTP Status und Content-Type - Vollständige
   * Mock-Daten (Anzahl und Werte) - Strukturelle Integrität aller Datentypen (Tasks, Risk
   * Customers, Statistics, Alerts)
   */
  @Test
  void testDevDashboardEndpointComplete() {
    given()
        .when()
        .get("/api/sales-cockpit/dashboard/dev")
        .then()
        .statusCode(200)
        .contentType(MediaType.APPLICATION_JSON)
        // Dashboard-Struktur und Anzahlen
        .body("todaysTasks", notNullValue())
        .body("todaysTasks.size()", equalTo(3))
        .body("riskCustomers", notNullValue())
        .body("riskCustomers.size()", equalTo(2))
        .body("statistics", notNullValue())
        .body("alerts", notNullValue())
        .body("alerts.size()", equalTo(1))
        // Statistics-Werte (mindestens die Mock-Werte oder echte Daten)
        .body("statistics.totalCustomers", greaterThanOrEqualTo(0))
        .body("statistics.activeCustomers", greaterThanOrEqualTo(0))
        .body("statistics.customersAtRisk", greaterThanOrEqualTo(0))
        .body("statistics.overdueItems", greaterThanOrEqualTo(0))
        .body("statistics.openTasks", greaterThanOrEqualTo(0))
        // Task-Struktur (erstes Element)
        .body("todaysTasks[0].id", notNullValue())
        .body("todaysTasks[0].title", notNullValue())
        .body("todaysTasks[0].description", notNullValue())
        .body("todaysTasks[0].type", notNullValue())
        .body("todaysTasks[0].priority", notNullValue())
        .body("todaysTasks[0].customerId", notNullValue())
        .body("todaysTasks[0].customerName", notNullValue())
        .body("todaysTasks[0].dueDate", notNullValue())
        .body("todaysTasks[0].completed", equalTo(false))
        // Risk Customer-Struktur (erstes Element)
        .body("riskCustomers[0].id", notNullValue())
        .body("riskCustomers[0].customerNumber", notNullValue())
        .body("riskCustomers[0].companyName", notNullValue())
        // lastContactDate can be null if customer never had contact
        // .body("riskCustomers[0].lastContactDate", notNullValue())
        .body("riskCustomers[0].daysSinceLastContact", greaterThan(0))
        .body("riskCustomers[0].riskReason", notNullValue())
        .body("riskCustomers[0].riskLevel", notNullValue())
        .body("riskCustomers[0].recommendedAction", notNullValue());
  }

  /** Testet den bestehenden Health-Check Endpunkt. */
  @Test
  void testHealthEndpoint() {
    given()
        .when()
        .get("/api/sales-cockpit/health")
        .then()
        .statusCode(200)
        .body("status", equalTo("UP"))
        .body("service", equalTo("sales-cockpit"));
  }
}

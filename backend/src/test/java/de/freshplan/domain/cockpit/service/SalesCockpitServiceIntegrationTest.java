package de.freshplan.domain.cockpit.service;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.cockpit.service.dto.*;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.CustomerType;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.domain.user.service.exception.UserNotFoundException;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration-Tests für den SalesCockpitService.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales", "viewer"})
@TestTransaction
class SalesCockpitServiceIntegrationTest {

  @Inject SalesCockpitService salesCockpitService;

  @Inject CustomerRepository customerRepository;

  @Inject UserRepository userRepository;

  private User testUser;

  @BeforeEach
  void setUp() {
    // Test-User wird in jeder Test-Methode erstellt
  }

  @Test
  void testGetDashboardData_Success() {
    // Setup test data
    testUser =
        new User(
            "test.user." + System.currentTimeMillis(),
            "Test",
            "User",
            "test" + System.currentTimeMillis() + "@example.com");
    userRepository.persist(testUser);
    userRepository.flush();
    createTestCustomers();

    // When
    SalesCockpitDashboard dashboard = salesCockpitService.getDashboardData(testUser.getId());

    // Then
    assertNotNull(dashboard);

    // Prüfe Tasks (Mock-Daten basierend auf aktiven Kunden)
    assertNotNull(dashboard.getTodaysTasks());
    assertTrue(dashboard.getTodaysTasks().size() >= 2);

    // Prüfe Risiko-Kunden
    assertNotNull(dashboard.getRiskCustomers());
    assertTrue(dashboard.getRiskCustomers().size() >= 1);

    // Prüfe mindestens ein Risiko-Kunde mit hohem Risiko
    assertTrue(
        dashboard.getRiskCustomers().stream()
            .anyMatch(risk -> risk.getRiskLevel() == RiskCustomer.RiskLevel.HIGH));

    // Prüfe Statistiken
    DashboardStatistics stats = dashboard.getStatistics();
    assertNotNull(stats);
    assertTrue(stats.getTotalCustomers() >= 4);
    assertTrue(stats.getActiveCustomers() >= 2);
    assertTrue(stats.getCustomersAtRisk() >= 1);

    // Prüfe Alerts
    assertNotNull(dashboard.getAlerts());
  }

  @Test
  void testGetDashboardData_UserNotFound() {
    // Given
    UUID unknownUserId = UUID.randomUUID();

    // When & Then
    assertThrows(
        UserNotFoundException.class,
        () -> {
          salesCockpitService.getDashboardData(unknownUserId);
        });
  }

  @Test
  void testRiskCustomerMapping() {
    // Setup test data
    testUser =
        new User(
            "test.user." + System.currentTimeMillis(),
            "Test",
            "User",
            "test" + System.currentTimeMillis() + "@example.com");
    userRepository.persist(testUser);
    userRepository.flush();
    createTestCustomers();

    // When
    SalesCockpitDashboard dashboard = salesCockpitService.getDashboardData(testUser.getId());

    // Then
    assertNotNull(dashboard.getRiskCustomers());

    // Finde den Hoch-Risiko-Kunden
    RiskCustomer highRiskCustomer =
        dashboard.getRiskCustomers().stream()
            .filter(r -> "Lange Nicht Kontaktiert GmbH".equals(r.getCompanyName()))
            .findFirst()
            .orElse(null);

    assertNotNull(highRiskCustomer);
    assertEquals(RiskCustomer.RiskLevel.HIGH, highRiskCustomer.getRiskLevel());
    assertEquals("Kein Kontakt seit über 120 Tagen", highRiskCustomer.getRiskReason());
    assertTrue(highRiskCustomer.getDaysSinceLastContact() >= 150);
  }

  @Test
  void testNeverContactedCustomer() {
    // Setup test data
    testUser =
        new User(
            "test.user." + System.currentTimeMillis(),
            "Test",
            "User",
            "test" + System.currentTimeMillis() + "@example.com");
    userRepository.persist(testUser);
    userRepository.flush();
    createTestCustomers();

    // When
    SalesCockpitDashboard dashboard = salesCockpitService.getDashboardData(testUser.getId());

    // Then
    // Finde den nie kontaktierten Kunden
    RiskCustomer neverContacted =
        dashboard.getRiskCustomers().stream()
            .filter(r -> "Nie Kontaktiert AG".equals(r.getCompanyName()))
            .findFirst()
            .orElse(null);

    assertNotNull(neverContacted);
    assertEquals(RiskCustomer.RiskLevel.LOW, neverContacted.getRiskLevel());
    assertNull(neverContacted.getLastContactDate());
  }

  private void createTestCustomers() {
    String suffix = String.valueOf(System.currentTimeMillis() % 10000);

    // Aktiver Kunde mit kürzlichem Kontakt
    Customer activeCustomer1 = new Customer();
    activeCustomer1.setCustomerNumber("C001_" + suffix);
    activeCustomer1.setCompanyName("Aktiv GmbH");
    activeCustomer1.setCustomerType(CustomerType.UNTERNEHMEN);
    activeCustomer1.setStatus(CustomerStatus.AKTIV);
    activeCustomer1.setLastContactDate(LocalDateTime.now().minusDays(5));
    activeCustomer1.setCreatedBy("test");
    customerRepository.persist(activeCustomer1);

    // Aktiver Kunde ohne kürzlichen Kontakt (mittleres Risiko)
    Customer activeCustomer2 = new Customer();
    activeCustomer2.setCustomerNumber("C002_" + suffix);
    activeCustomer2.setCompanyName("Mittel Risiko AG");
    activeCustomer2.setCustomerType(CustomerType.UNTERNEHMEN);
    activeCustomer2.setStatus(CustomerStatus.AKTIV);
    activeCustomer2.setLastContactDate(LocalDateTime.now().minusDays(100));
    activeCustomer2.setCreatedBy("test");
    customerRepository.persist(activeCustomer2);

    // Aktiver Kunde mit sehr langem Kontaktausfall (hohes Risiko)
    Customer highRiskCustomer = new Customer();
    highRiskCustomer.setCustomerNumber("C003_" + suffix);
    highRiskCustomer.setCompanyName("Lange Nicht Kontaktiert GmbH");
    highRiskCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
    highRiskCustomer.setStatus(CustomerStatus.AKTIV);
    highRiskCustomer.setLastContactDate(LocalDateTime.now().minusDays(150));
    highRiskCustomer.setCreatedBy("test");
    customerRepository.persist(highRiskCustomer);

    // Kunde der noch nie kontaktiert wurde
    Customer neverContactedCustomer = new Customer();
    neverContactedCustomer.setCustomerNumber("C004_" + suffix);
    neverContactedCustomer.setCompanyName("Nie Kontaktiert AG");
    neverContactedCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
    neverContactedCustomer.setStatus(CustomerStatus.AKTIV);
    neverContactedCustomer.setLastContactDate(null);
    neverContactedCustomer.setCreatedBy("test");
    customerRepository.persist(neverContactedCustomer);

    // Inaktiver Kunde (sollte nicht in Risiko-Liste erscheinen)
    Customer inactiveCustomer = new Customer();
    inactiveCustomer.setCustomerNumber("C005_" + suffix);
    inactiveCustomer.setCompanyName("Inaktiv GmbH");
    inactiveCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
    inactiveCustomer.setStatus(CustomerStatus.INAKTIV);
    inactiveCustomer.setLastContactDate(LocalDateTime.now().minusDays(200));
    inactiveCustomer.setCreatedBy("test");
    customerRepository.persist(inactiveCustomer);

    // Flush to ensure all data is persisted
    customerRepository.flush();
  }
}

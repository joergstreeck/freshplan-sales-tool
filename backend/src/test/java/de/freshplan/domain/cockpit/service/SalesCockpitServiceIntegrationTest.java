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
import de.freshplan.test.builders.CustomerBuilder;
import de.freshplan.test.builders.UserTestDataFactory;
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
    roles = {"admin", "manager", "sales"})
@TestTransaction
class SalesCockpitServiceIntegrationTest {

  @Inject SalesCockpitService salesCockpitService;

  @Inject CustomerRepository customerRepository;

  @Inject UserRepository userRepository;

  @Inject CustomerBuilder customerBuilder;

  private User testUser;

  @BeforeEach
  void setUp() {
    // Test-User wird in jeder Test-Methode erstellt
  }

  @Test
  void testGetDashboardData_Success() {
    // Setup test data
    String timestamp = String.valueOf(System.currentTimeMillis());
    testUser =
        UserTestDataFactory.builder()
            .withUsername("test.user." + timestamp)
            .withFirstName("Test")
            .withLastName("User")
            .withEmail("test" + timestamp + "@example.com")
            .build();
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
    String timestamp = String.valueOf(System.currentTimeMillis());
    testUser =
        UserTestDataFactory.builder()
            .withUsername("test.user." + timestamp)
            .withFirstName("Test")
            .withLastName("User")
            .withEmail("test" + timestamp + "@example.com")
            .build();
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
            .filter(r -> "[TEST] Lange Nicht Kontaktiert GmbH".equals(r.getCompanyName()))
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
    String timestamp = String.valueOf(System.currentTimeMillis());
    testUser =
        UserTestDataFactory.builder()
            .withUsername("test.user." + timestamp)
            .withFirstName("Test")
            .withLastName("User")
            .withEmail("test" + timestamp + "@example.com")
            .build();
    userRepository.persist(testUser);
    userRepository.flush();
    createTestCustomers();

    // When
    SalesCockpitDashboard dashboard = salesCockpitService.getDashboardData(testUser.getId());

    // Then
    // Finde den nie kontaktierten Kunden
    RiskCustomer neverContacted =
        dashboard.getRiskCustomers().stream()
            .filter(r -> "[TEST] Nie Kontaktiert AG".equals(r.getCompanyName()))
            .findFirst()
            .orElse(null);

    assertNotNull(neverContacted);
    assertEquals(RiskCustomer.RiskLevel.LOW, neverContacted.getRiskLevel());
    assertNull(neverContacted.getLastContactDate());
  }

  private void createTestCustomers() {
    String suffix = String.valueOf(System.currentTimeMillis() % 10000);

    // Aktiver Kunde mit kürzlichem Kontakt
    Customer activeCustomer1 =
        customerBuilder
            .withCompanyName("[TEST] Aktiv GmbH")
            .withType(CustomerType.UNTERNEHMEN)
            .withStatus(CustomerStatus.AKTIV)
            .build();
    activeCustomer1.setCustomerNumber("C001_" + suffix);
    activeCustomer1.setCompanyName("[TEST] Aktiv GmbH"); // Keep [TEST] prefix
    activeCustomer1.setIsTestData(true);
    activeCustomer1.setLastContactDate(LocalDateTime.now().minusDays(5));
    customerRepository.persist(activeCustomer1);

    // Aktiver Kunde ohne kürzlichen Kontakt (mittleres Risiko)
    Customer activeCustomer2 =
        customerBuilder
            .withCompanyName("[TEST] Mittel Risiko AG")
            .withType(CustomerType.UNTERNEHMEN)
            .withStatus(CustomerStatus.AKTIV)
            .build();
    activeCustomer2.setCustomerNumber("C002_" + suffix);
    activeCustomer2.setCompanyName("[TEST] Mittel Risiko AG"); // Keep [TEST] prefix
    activeCustomer2.setIsTestData(true);
    activeCustomer2.setLastContactDate(LocalDateTime.now().minusDays(100));
    customerRepository.persist(activeCustomer2);

    // Aktiver Kunde mit sehr langem Kontaktausfall (hohes Risiko)
    Customer highRiskCustomer =
        customerBuilder
            .withCompanyName("[TEST] Lange Nicht Kontaktiert GmbH")
            .withType(CustomerType.UNTERNEHMEN)
            .withStatus(CustomerStatus.AKTIV)
            .build();
    highRiskCustomer.setCustomerNumber("C003_" + suffix);
    highRiskCustomer.setCompanyName("[TEST] Lange Nicht Kontaktiert GmbH"); // Keep [TEST] prefix
    highRiskCustomer.setIsTestData(true);
    highRiskCustomer.setLastContactDate(LocalDateTime.now().minusDays(150));
    customerRepository.persist(highRiskCustomer);

    // Kunde der noch nie kontaktiert wurde
    Customer neverContactedCustomer =
        customerBuilder
            .withCompanyName("[TEST] Nie Kontaktiert AG")
            .withType(CustomerType.UNTERNEHMEN)
            .withStatus(CustomerStatus.AKTIV)
            .build();
    neverContactedCustomer.setCustomerNumber("C004_" + suffix);
    neverContactedCustomer.setCompanyName("[TEST] Nie Kontaktiert AG"); // Keep [TEST] prefix
    neverContactedCustomer.setIsTestData(true);
    neverContactedCustomer.setLastContactDate(null);
    customerRepository.persist(neverContactedCustomer);

    // Inaktiver Kunde (sollte nicht in Risiko-Liste erscheinen)
    Customer inactiveCustomer =
        customerBuilder
            .withCompanyName("[TEST] Inaktiv GmbH")
            .withType(CustomerType.UNTERNEHMEN)
            .withStatus(CustomerStatus.INAKTIV)
            .build();
    inactiveCustomer.setCustomerNumber("C005_" + suffix);
    inactiveCustomer.setCompanyName("[TEST] Inaktiv GmbH"); // Keep [TEST] prefix
    inactiveCustomer.setIsTestData(true);
    inactiveCustomer.setLastContactDate(LocalDateTime.now().minusDays(200));
    customerRepository.persist(inactiveCustomer);

    // Flush to ensure all data is persisted
    customerRepository.flush();
  }
}

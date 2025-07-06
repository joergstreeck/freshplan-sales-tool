package de.freshplan.domain.cockpit.service;

import de.freshplan.domain.cockpit.service.dto.*;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer für das Sales Cockpit Dashboard.
 *
 * <p>Aggregiert Daten aus verschiedenen Services und bereitet sie optimiert für die Cockpit-Ansicht
 * auf.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
@Transactional
public class SalesCockpitService {

  /** Schwellwert für niedriges Risiko: 60 Tage ohne Kontakt */
  private static final int RISK_THRESHOLD_LOW_DAYS = 60;

  /** Schwellwert für mittleres Risiko: 90 Tage ohne Kontakt */
  private static final int RISK_THRESHOLD_MEDIUM_DAYS = 90;

  /** Schwellwert für hohes Risiko: 120 Tage ohne Kontakt */
  private static final int RISK_THRESHOLD_HIGH_DAYS = 120;

  private final CustomerRepository customerRepository;
  private final UserRepository userRepository;

  @Inject
  public SalesCockpitService(CustomerRepository customerRepository, UserRepository userRepository) {
    this.customerRepository = customerRepository;
    this.userRepository = userRepository;
  }

  /**
   * Lädt alle Dashboard-Daten für einen bestimmten Benutzer.
   *
   * @param userId Die ID des Benutzers
   * @return Aggregierte Dashboard-Daten
   * @throws IllegalArgumentException wenn userId null ist oder User nicht gefunden wird
   */
  public SalesCockpitDashboard getDashboardData(UUID userId) {
    // Input validation
    if (userId == null) {
      throw new IllegalArgumentException("User ID must not be null");
    }

    // Benutzer validieren
    User user = userRepository.findById(userId);
    if (user == null) {
      throw new IllegalArgumentException("User not found: " + userId);
    }

    SalesCockpitDashboard dashboard = new SalesCockpitDashboard();

    // Lade verschiedene Datenkomponenten
    dashboard.setTodaysTasks(loadTodaysTasks(userId));
    dashboard.setRiskCustomers(loadRiskCustomers());
    dashboard.setStatistics(calculateStatistics());
    dashboard.setAlerts(generateAlerts());

    return dashboard;
  }

  /**
   * Lädt die heutigen Aufgaben für einen Benutzer.
   *
   * <p>Hinweis: Diese Implementierung verwendet Mock-Daten, da das Task-Modul noch nicht
   * implementiert ist. In der finalen Version werden echte Aufgaben aus dem Task-Service geladen.
   */
  private List<DashboardTask> loadTodaysTasks(UUID userId) {
    List<DashboardTask> tasks = new ArrayList<>();

    // Mock-Implementierung bis Task-Modul verfügbar ist
    // Feature-Flag: ff_FRESH-001_task_module_integration
    boolean useRealTasks = false; // Wird später durch Feature-Flag ersetzt

    if (useRealTasks) {
      // Zukünftige Integration mit Task-Service
      return tasks;
    }

    // Mock-Tasks basierend auf Customer-Daten
    // Hinweis: In Production sollte hier Pagination verwendet werden
    List<Customer> activeCustomers =
        customerRepository
            .find("status", CustomerStatus.AKTIV)
            .page(0, 3) // Nur die ersten 3 laden für Performance
            .list();

    // Erstelle Mock-Tasks für aktive Kunden
    activeCustomers.forEach(
        customer -> {
          DashboardTask task = new DashboardTask();
          task.setId(UUID.randomUUID());
          task.setTitle("Follow-up mit " + customer.getCompanyName());
          task.setDescription("Quartalsmeeting besprechen");
          task.setType(DashboardTask.TaskType.CALL);
          task.setPriority(DashboardTask.TaskPriority.MEDIUM);
          task.setCustomerId(customer.getId());
          task.setCustomerName(customer.getCompanyName());
          task.setDueDate(LocalDateTime.now().plusHours(2));
          task.setCompleted(false);
          tasks.add(task);
        });

    return tasks;
  }

  /**
   * Lädt Kunden, die als Risiko eingestuft werden.
   *
   * <p>Ein Kunde gilt als Risiko, wenn seit mehr als 60 Tagen kein Kontakt stattgefunden hat.
   */
  private List<RiskCustomer> loadRiskCustomers() {
    LocalDateTime riskThreshold = LocalDateTime.now().minusDays(RISK_THRESHOLD_LOW_DAYS);

    // Finde Kunden ohne kürzlichen Kontakt
    String query = "status = ?1 AND (lastContactDate IS NULL " + "OR lastContactDate < ?2)";
    List<Customer> customersAtRisk =
        customerRepository.find(query, CustomerStatus.AKTIV, riskThreshold).list();

    return customersAtRisk.stream().map(this::mapToRiskCustomer).collect(Collectors.toList());
  }

  /** Mappt einen Customer zu einem RiskCustomer DTO. */
  private RiskCustomer mapToRiskCustomer(Customer customer) {
    RiskCustomer riskCustomer = new RiskCustomer();
    riskCustomer.setId(customer.getId());
    riskCustomer.setCustomerNumber(customer.getCustomerNumber());
    riskCustomer.setCompanyName(customer.getCompanyName());
    riskCustomer.setLastContactDate(customer.getLastContactDate());

    // Berechne Tage seit letztem Kontakt
    int daysSinceContact = 0;
    if (customer.getLastContactDate() != null) {
      daysSinceContact =
          (int)
              ChronoUnit.DAYS.between(customer.getLastContactDate().toLocalDate(), LocalDate.now());
    } else {
      // Wenn noch nie Kontakt war, verwende Erstellungsdatum
      daysSinceContact =
          (int) ChronoUnit.DAYS.between(customer.getCreatedAt().toLocalDate(), LocalDate.now());
    }
    riskCustomer.setDaysSinceLastContact(daysSinceContact);

    // Bestimme Risiko-Level
    if (daysSinceContact > RISK_THRESHOLD_HIGH_DAYS) {
      riskCustomer.setRiskLevel(RiskCustomer.RiskLevel.HIGH);
      riskCustomer.setRiskReason("Kein Kontakt seit über 120 Tagen");
      riskCustomer.setRecommendedAction("Dringend kontaktieren - Kundenverlust droht!");
    } else if (daysSinceContact > RISK_THRESHOLD_MEDIUM_DAYS) {
      riskCustomer.setRiskLevel(RiskCustomer.RiskLevel.MEDIUM);
      riskCustomer.setRiskReason("Kein Kontakt seit über 90 Tagen");
      riskCustomer.setRecommendedAction("Zeitnah kontaktieren zur Beziehungspflege");
    } else {
      riskCustomer.setRiskLevel(RiskCustomer.RiskLevel.LOW);
      riskCustomer.setRiskReason("Kein Kontakt seit über 60 Tagen");
      riskCustomer.setRecommendedAction("Bei nächster Gelegenheit kontaktieren");
    }

    return riskCustomer;
  }

  /** Berechnet Dashboard-Statistiken. */
  private DashboardStatistics calculateStatistics() {
    DashboardStatistics stats = new DashboardStatistics();

    // Zähle Kunden
    stats.setTotalCustomers((int) customerRepository.count());
    stats.setActiveCustomers((int) customerRepository.count("status", CustomerStatus.AKTIV));

    // Risiko-Kunden
    LocalDateTime riskThreshold = LocalDateTime.now().minusDays(RISK_THRESHOLD_LOW_DAYS);
    String riskQuery = "status = ?1 AND (lastContactDate IS NULL " + "OR lastContactDate < ?2)";
    stats.setCustomersAtRisk(
        (int) customerRepository.count(riskQuery, CustomerStatus.AKTIV, riskThreshold));

    // Temporäre Mock-Werte bis Task-Modul implementiert ist
    // Feature-Flag: ff_FRESH-001_task_module_integration
    stats.setOpenTasks(3);
    stats.setOverdueItems(1);

    return stats;
  }

  /**
   * Generiert KI-gestützte Alerts.
   *
   * <p>Hinweis: Diese Implementierung generiert einfache regelbasierte Alerts. In einer späteren
   * Version können hier ML-Modelle integriert werden.
   */
  private List<DashboardAlert> generateAlerts() {
    List<DashboardAlert> alerts = new ArrayList<>();

    // Alert für Kunden mit hohem Umsatzpotential ohne kürzlichen Kontakt
    List<Customer> highValueCustomers =
        customerRepository
            .find(
                "status = ?1 AND lastContactDate < ?2",
                CustomerStatus.AKTIV,
                LocalDateTime.now().minusDays(30))
            .list();

    highValueCustomers.stream()
        .limit(2)
        .forEach(
            customer -> {
              DashboardAlert alert = new DashboardAlert();
              alert.setId(UUID.randomUUID());
              alert.setTitle("Umsatzchance bei " + customer.getCompanyName());
              alert.setMessage(
                  "Kunde hatte lange keinen Kontakt - idealer Zeitpunkt für Cross-Selling");
              alert.setType(DashboardAlert.AlertType.OPPORTUNITY);
              alert.setSeverity(DashboardAlert.AlertSeverity.INFO);
              alert.setCustomerId(customer.getId());
              alert.setCustomerName(customer.getCompanyName());
              alert.setCreatedAt(LocalDateTime.now());
              alert.setActionLink("/customers/" + customer.getId());
              alerts.add(alert);
            });

    return alerts;
  }
}

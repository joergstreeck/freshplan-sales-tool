package de.freshplan.domain.cockpit.service;

import de.freshplan.domain.cockpit.service.dto.*;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.domain.user.service.exception.UserNotFoundException;
import io.quarkus.panache.common.Page;
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

  /** Test-User-ID für Entwicklung - umgeht User-Validierung */
  private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

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
    // TODO: User-Validierung aktivieren, sobald User-Modul implementiert ist
    // Temporär: Akzeptiere Test-User-ID für Entwicklung
    if (!userId.equals(TEST_USER_ID)) {
      User user = userRepository.findById(userId);
      if (user == null) {
        throw new UserNotFoundException("User not found: " + userId);
      }
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
   * <p>Diese Implementierung generiert intelligente Tasks basierend auf echten Customer-Daten
   * und deren Status. In der finalen Version werden echte Aufgaben aus dem Task-Service geladen.
   */
  private List<DashboardTask> loadTodaysTasks(UUID userId) {
    List<DashboardTask> tasks = new ArrayList<>();

    // Feature-Flag: ff_FRESH-001_task_module_integration
    boolean useRealTasks = false; // Wird später durch Feature-Flag ersetzt

    if (useRealTasks) {
      // Zukünftige Integration mit Task-Service
      return tasks;
    }

    // Intelligente Task-Generierung basierend auf echten Customer-Daten
    
    // 1. Überfällige Follow-ups (höchste Priorität)
    List<Customer> overdueCustomers = customerRepository.findOverdueFollowUps(Page.of(0, 2));
    overdueCustomers.forEach(customer -> {
      DashboardTask task = new DashboardTask();
      task.setId(UUID.randomUUID());
      task.setTitle("ÜBERFÄLLIG: Follow-up mit " + customer.getCompanyName());
      task.setDescription("Geplanter Follow-up seit " + 
          (customer.getNextFollowUpDate() != null ? 
           customer.getNextFollowUpDate().toLocalDate() : "unbekannt"));
      task.setType(DashboardTask.TaskType.CALL);
      task.setPriority(DashboardTask.TaskPriority.HIGH);
      task.setCustomerId(customer.getId());
      task.setCustomerName(customer.getCompanyName());
      task.setDueDate(LocalDateTime.now().minusHours(1)); // Bereits überfällig
      task.setCompleted(false);
      tasks.add(task);
    });

    // 2. Risiko-Kunden kontaktieren (mittlere Priorität)
    LocalDateTime riskThreshold = LocalDateTime.now().minusDays(RISK_THRESHOLD_LOW_DAYS);
    List<Customer> riskCustomers = customerRepository
        .findActiveCustomersWithoutRecentContact(riskThreshold)
        .stream()
        .limit(3)
        .toList();
    
    riskCustomers.forEach(customer -> {
      DashboardTask task = new DashboardTask();
      task.setId(UUID.randomUUID());
      task.setTitle("Risiko-Kunde kontaktieren: " + customer.getCompanyName());
      task.setDescription("Kein Kontakt seit " + 
          (customer.getLastContactDate() != null ? 
           customer.getLastContactDate().toLocalDate() : "Beginn der Geschäftsbeziehung"));
      task.setType(DashboardTask.TaskType.EMAIL);
      task.setPriority(DashboardTask.TaskPriority.MEDIUM);
      task.setCustomerId(customer.getId());
      task.setCustomerName(customer.getCompanyName());
      task.setDueDate(LocalDateTime.now().plusHours(4));
      task.setCompleted(false);
      tasks.add(task);
    });

    // 3. Neue Kunden begrüßen (niedrige Priorität)
    List<Customer> newCustomers = customerRepository.findRecentlyCreated(7, Page.of(0, 2));
    newCustomers.forEach(customer -> {
      DashboardTask task = new DashboardTask();
      task.setId(UUID.randomUUID());
      task.setTitle("Willkommen-Anruf: " + customer.getCompanyName());
      task.setDescription("Neuer Kunde seit " + customer.getCreatedAt().toLocalDate());
      task.setType(DashboardTask.TaskType.CALL);
      task.setPriority(DashboardTask.TaskPriority.LOW);
      task.setCustomerId(customer.getId());
      task.setCustomerName(customer.getCompanyName());
      task.setDueDate(LocalDateTime.now().plusHours(8));
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
    List<Customer> customersAtRisk =
        customerRepository.findActiveCustomersWithoutRecentContact(riskThreshold);

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

    // Echte Kunden-Statistiken
    stats.setTotalCustomers((int) customerRepository.countActive());
    stats.setActiveCustomers((int) customerRepository.countByStatus(CustomerStatus.AKTIV));

    // Risiko-Kunden basierend auf verschiedenen Schwellwerten
    LocalDateTime riskThreshold = LocalDateTime.now().minusDays(RISK_THRESHOLD_LOW_DAYS);
    stats.setCustomersAtRisk(
        (int) customerRepository.countActiveCustomersWithoutRecentContact(riskThreshold));

    // Task-basierte Statistiken: Berechnung basierend auf echten Customer-Daten
    // bis Task-Modul implementiert ist
    // Feature-Flag: ff_FRESH-001_task_module_integration
    
    // Überfällige Follow-ups als Proxy für überfällige Aufgaben
    long overdueFollowUps = customerRepository.countOverdueFollowUps();
    stats.setOverdueItems((int) overdueFollowUps);
    
    // Offene Tasks basierend auf Kunden ohne kürzlichen Kontakt (letzte 7 Tage)
    LocalDateTime recentContactThreshold = LocalDateTime.now().minusDays(7);
    long customersNeedingContact = customerRepository.countActiveCustomersWithoutRecentContact(recentContactThreshold);
    stats.setOpenTasks((int) customersNeedingContact);

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
    // TODO: Sobald Umsatzfelder verfügbar sind, nach actualRevenue oder potentialRevenue filtern
    // Aktuell: Alle aktiven Kunden ohne Kontakt in den letzten 30 Tagen
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

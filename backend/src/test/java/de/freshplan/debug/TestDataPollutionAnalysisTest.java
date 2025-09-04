package de.freshplan.debug;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.domain.user.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
/**
 * Detaillierte Analyse der Test-Daten-Pollution in der Datenbank.
 *
 * <p>WICHTIG: Dieser Test führt NUR Analysen durch und ändert NICHTS! Sicherheit geht vor
 * Schnelligkeit.
 */
@QuarkusTest
@Tag("quarantine")public class TestDataPollutionAnalysisTest {

  @Inject CustomerRepository customerRepository;
  @Inject OpportunityRepository opportunityRepository;
  @Inject UserRepository userRepository;
  @Inject EntityManager entityManager;

  @Test
  @Transactional
  void analyzeTestDataPollution() {
    System.out.println("\n" + "=".repeat(80));
    System.out.println("🔍 TEST-DATEN-POLLUTION ANALYSE - " + LocalDateTime.now());
    System.out.println("=".repeat(80));

    // 1. Gesamtübersicht Customers
    long totalCustomers = customerRepository.count();
    System.out.println("\n📊 CUSTOMERS ÜBERSICHT:");
    System.out.println("├─ Gesamt: " + totalCustomers);

    // 2. Test-Daten Patterns analysieren
    List<Customer> allCustomers = customerRepository.findAll().list();

    long testPrefixCount =
        allCustomers.stream()
            .filter(c -> c.getCompanyName() != null && c.getCompanyName().startsWith("[TEST]"))
            .count();

    long testInNameCount =
        allCustomers.stream()
            .filter(
                c ->
                    c.getCompanyName() != null
                        && (c.getCompanyName().toUpperCase().contains("TEST")
                            || c.getCompanyName().contains("[TEST-")))
            .count();

    long markedAsTestCount =
        allCustomers.stream().filter(c -> Boolean.TRUE.equals(c.getIsTestData())).count();

    long testCustomerNumberCount =
        allCustomers.stream()
            .filter(
                c ->
                    c.getCustomerNumber() != null
                        && (c.getCustomerNumber().startsWith("TEST-")
                            || c.getCustomerNumber().startsWith("TCT-")
                            || c.getCustomerNumber().startsWith("TCI-")
                            || c.getCustomerNumber().startsWith("TCD-")))
            .count();

    System.out.println("├─ Mit [TEST] Prefix: " + testPrefixCount);
    System.out.println("├─ Mit TEST im Namen: " + testInNameCount);
    System.out.println("├─ Als is_test_data markiert: " + markedAsTestCount);
    System.out.println("└─ Mit Test-CustomerNumber: " + testCustomerNumberCount);

    // 3. Zeitliche Analyse
    LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
    LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
    LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

    long createdLastDay =
        allCustomers.stream()
            .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(oneDayAgo))
            .count();

    long createdLastWeek =
        allCustomers.stream()
            .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(oneWeekAgo))
            .count();

    long createdLastMonth =
        allCustomers.stream()
            .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(oneMonthAgo))
            .count();

    System.out.println("\n⏰ ZEITLICHE VERTEILUNG:");
    System.out.println("├─ Letzter Tag: " + createdLastDay);
    System.out.println("├─ Letzte Woche: " + createdLastWeek);
    System.out.println("└─ Letzter Monat: " + createdLastMonth);

    // 4. Pattern-Analyse
    System.out.println("\n🔍 PATTERN-ANALYSE (Top 10 häufigste Präfixe):");
    Map<String, Long> prefixCounts =
        allCustomers.stream()
            .filter(c -> c.getCompanyName() != null)
            .collect(
                Collectors.groupingBy(
                    c -> {
                      String name = c.getCompanyName();
                      if (name.startsWith("[")) {
                        int endIndex = name.indexOf("]");
                        if (endIndex > 0) {
                          return name.substring(0, endIndex + 1);
                        }
                      }
                      // Erste 10 Zeichen als Fallback
                      return name.length() > 10 ? name.substring(0, 10) : name;
                    },
                    Collectors.counting()));

    prefixCounts.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .limit(10)
        .forEach(e -> System.out.println("├─ \"" + e.getKey() + "\": " + e.getValue()));

    // 5. Duplikate analysieren
    System.out.println("\n🔍 DUPLIKAT-ANALYSE:");
    Map<String, Long> nameCounts =
        allCustomers.stream()
            .filter(c -> c.getCompanyName() != null)
            .collect(Collectors.groupingBy(Customer::getCompanyName, Collectors.counting()));

    long duplicateCount = nameCounts.entrySet().stream().filter(e -> e.getValue() > 1).count();

    System.out.println("├─ Anzahl doppelter Namen: " + duplicateCount);

    // Top 5 häufigste Namen
    System.out.println("└─ Top 5 häufigste Namen:");
    nameCounts.entrySet().stream()
        .filter(e -> e.getValue() > 1)
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .limit(5)
        .forEach(e -> System.out.println("   ├─ \"" + e.getKey() + "\": " + e.getValue() + "x"));

    // 6. Created_by Analyse
    System.out.println("\n👤 CREATED_BY ANALYSE:");
    Map<String, Long> createdByCounts =
        allCustomers.stream()
            .filter(c -> c.getCreatedBy() != null)
            .collect(Collectors.groupingBy(Customer::getCreatedBy, Collectors.counting()));

    createdByCounts.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .limit(10)
        .forEach(e -> System.out.println("├─ \"" + e.getKey() + "\": " + e.getValue()));

    // 7. Opportunities Analyse
    long totalOpportunities = opportunityRepository.count();
    System.out.println("\n📊 OPPORTUNITIES:");
    System.out.println("└─ Gesamt: " + totalOpportunities);

    // 8. Native Query für zusätzliche Insights
    System.out.println("\n🔍 NATIVE QUERY ANALYSE:");

    // Customers ohne Kontakte
    Long customersWithoutContacts =
        (Long)
            entityManager
                .createNativeQuery(
                    "SELECT COUNT(*) FROM customers c "
                        + "WHERE NOT EXISTS (SELECT 1 FROM customer_contacts cc "
                        + "WHERE cc.customer_id = c.id AND cc.is_deleted = false)")
                .getSingleResult();
    System.out.println("├─ Customers ohne Kontakte: " + customersWithoutContacts);

    // Customers ohne Opportunities
    Long customersWithoutOpportunities =
        (Long)
            entityManager
                .createNativeQuery(
                    "SELECT COUNT(*) FROM customers c "
                        + "WHERE NOT EXISTS (SELECT 1 FROM opportunities o "
                        + "WHERE o.customer_id = c.id)")
                .getSingleResult();
    System.out.println("├─ Customers ohne Opportunities: " + customersWithoutOpportunities);

    // Test-Kunden die älter als 1 Tag sind (sollten eigentlich gelöscht sein)
    Long oldTestCustomers =
        (Long)
            entityManager
                .createNativeQuery(
                    "SELECT COUNT(*) FROM customers "
                        + "WHERE (company_name LIKE '%TEST%' OR is_test_data = true) "
                        + "AND created_at < NOW() - INTERVAL '1 day'")
                .getSingleResult();
    System.out.println("└─ Alte Test-Kunden (>1 Tag): " + oldTestCustomers);

    // 9. Empfehlungen
    System.out.println("\n💡 EMPFEHLUNGEN:");
    System.out.println("├─ " + oldTestCustomers + " alte Test-Kunden sollten gelöscht werden");
    System.out.println("├─ " + duplicateCount + " duplizierte Namen sollten bereinigt werden");

    long cleanupCandidates =
        testInNameCount + markedAsTestCount - 58; // 58 sind gewollte Test-Daten
    if (cleanupCandidates > 0) {
      System.out.println("├─ ~" + cleanupCandidates + " Kunden könnten Test-Daten sein");
    }

    System.out.println("└─ @TestTransaction sollte für alle Tests aktiviert werden");

    System.out.println("\n" + "=".repeat(80));
    System.out.println("✅ Analyse abgeschlossen - KEINE Daten wurden geändert!");
    System.out.println("=".repeat(80) + "\n");
  }

  @Test
  @Transactional
  void analyzeTestIsolationProblems() {
    System.out.println("\n" + "=".repeat(80));
    System.out.println("🔍 TEST-ISOLATION PROBLEM ANALYSE");
    System.out.println("=".repeat(80));

    // Suche nach Tests die KEINE @TestTransaction haben
    System.out.println("\n⚠️  POTENTIELLE PROBLEME:");
    System.out.println("├─ Tests ohne @TestTransaction persistieren Daten dauerhaft");
    System.out.println("├─ Tests mit @Transactional statt @TestTransaction");
    System.out.println("├─ Tests die EntityManager.flush() aufrufen");
    System.out.println("└─ Tests die manuell Transaktionen committen");

    // Analysiere spezifische Problem-Patterns
    List<Customer> problemCustomers =
        customerRepository.findAll().stream()
            .filter(
                c -> {
                  String name = c.getCompanyName();
                  String createdBy = c.getCreatedBy();
                  // Typische Test-Pattern ohne proper cleanup
                  return (name != null && name.contains("Test Company"))
                      || (name != null && name.contains("Minimal"))
                      || (name != null && name.contains("Updated"))
                      || (createdBy != null && createdBy.equals("test-system"));
                })
            .collect(Collectors.toList());

    System.out.println("\n🔍 VERDÄCHTIGE EINTRÄGE:");
    System.out.println("├─ Gefunden: " + problemCustomers.size());

    if (problemCustomers.size() > 0 && problemCustomers.size() <= 10) {
      problemCustomers.forEach(
          c ->
              System.out.println(
                  "├─ \""
                      + c.getCompanyName()
                      + "\" (created: "
                      + c.getCreatedAt()
                      + ", by: "
                      + c.getCreatedBy()
                      + ")"));
    } else if (problemCustomers.size() > 10) {
      problemCustomers.stream()
          .limit(10)
          .forEach(
              c ->
                  System.out.println(
                      "├─ \""
                          + c.getCompanyName()
                          + "\" (created: "
                          + c.getCreatedAt()
                          + ", by: "
                          + c.getCreatedBy()
                          + ")"));
      System.out.println("└─ ... und " + (problemCustomers.size() - 10) + " weitere");
    }

    // Wichtige Tests die überprüft werden sollten
    System.out.println("\n📝 ZU ÜBERPRÜFENDE TEST-KLASSEN:");
    System.out.println("├─ OpportunityResourceIntegrationTest");
    System.out.println("├─ CustomerResourceIntegrationTest");
    System.out.println("├─ ContactRepositoryTest");
    System.out.println("├─ CustomerServiceIntegrationTest");
    System.out.println("└─ Alle *IntegrationTest Klassen");

    System.out.println("\n" + "=".repeat(80));
    System.out.println("✅ Isolation-Analyse abgeschlossen!");
    System.out.println("=".repeat(80) + "\n");
  }
}

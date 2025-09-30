package de.freshplan.test;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.ContactInteractionRepository;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * SEHR DETAILLIERTE Analyse der Datenbank-Situation Bevor wir irgendetwas löschen, verstehen wir
 * GENAU was los ist! Sprint 2.1.4 Fix: Added @TestTransaction to fix ContextNotActiveException
 */
@QuarkusTest
@TestTransaction
@Tag("quarantine")
public class DetailedDatabaseAnalysisTest {

  @Inject EntityManager em;

  @Inject CustomerRepository customerRepository;

  @Inject ContactRepository contactRepository;

  @Inject ContactInteractionRepository interactionRepository;

  @Inject OpportunityRepository opportunityRepository;

  @Test
  public void performDetailedAnalysis() {
    System.out.println("\n" + "=".repeat(80));
    System.out.println("=== DETAILLIERTE DATENBANK-ANALYSE ===");
    System.out.println("=".repeat(80));
    System.out.println(
        "Analyse-Zeitpunkt: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

    // 1. GESAMT-ÜBERSICHT
    System.out.println("\n=== 1. GESAMT-ÜBERSICHT ===");
    System.out.println("Total Customers: " + customerRepository.count());
    System.out.println("Total Contacts: " + contactRepository.count());
    System.out.println("Total Interactions: " + interactionRepository.count());
    System.out.println("Total Opportunities: " + opportunityRepository.count());

    // 2. KUNDEN-MUSTER ANALYSE
    System.out.println("\n=== 2. KUNDEN-MUSTER ANALYSE ===");
    analyzeCustomerPatterns();

    // 3. ZEITLICHE VERTEILUNG
    System.out.println("\n=== 3. ZEITLICHE VERTEILUNG ===");
    analyzeTemporalDistribution();

    // 4. FOREIGN KEY DEPENDENCIES
    System.out.println("\n=== 4. FOREIGN KEY DEPENDENCIES ===");
    analyzeForeignKeyDependencies();

    // 5. DUPLIKATE ERKENNEN
    System.out.println("\n=== 5. DUPLIKATE-ANALYSE ===");
    analyzeDuplicates();

    // 6. ORIGINAL vs TEST-DATEN
    System.out.println("\n=== 6. ORIGINAL vs TEST-DATEN ===");
    distinguishOriginalFromTestData();

    // 7. PROBLEMATISCHE KUNDEN
    System.out.println("\n=== 7. PROBLEMATISCHE KUNDEN (mit vielen FK-Referenzen) ===");
    findProblematicCustomers();

    System.out.println("\n" + "=".repeat(80));
    System.out.println("=== ANALYSE ABGESCHLOSSEN ===");
    System.out.println("=".repeat(80));
  }

  private void analyzeCustomerPatterns() {
    // Verschiedene Muster zählen
    Map<String, Long> patterns = new LinkedHashMap<>();
    patterns.put("[TEST] prefix", customerRepository.count("companyName LIKE ?1", "[TEST]%"));
    patterns.put("Test im Namen", customerRepository.count("companyName LIKE ?1", "%Test%"));
    patterns.put("KD-S* (Search)", customerRepository.count("customerNumber LIKE ?1", "KD-S%"));
    patterns.put("KD-E* (Export)", customerRepository.count("customerNumber LIKE ?1", "KD-E%"));
    patterns.put("KD-EVT* (Event)", customerRepository.count("customerNumber LIKE ?1", "KD-EVT%"));
    patterns.put("KD-CQRS*", customerRepository.count("customerNumber LIKE ?1", "KD-CQRS%"));
    patterns.put(
        "KD-PG* (Pagination)", customerRepository.count("customerNumber LIKE ?1", "KD-PG%"));
    patterns.put(
        "KD-E1*/KD-E2*",
        customerRepository.count(
            "customerNumber LIKE ?1 OR customerNumber LIKE ?2", "KD-E1%", "KD-E2%"));
    patterns.put(
        "Search Solutions", customerRepository.count("companyName LIKE ?1", "%Search Solutions%"));
    patterns.put(
        "Export Hotel/Restaurant",
        customerRepository.count(
            "companyName LIKE ?1 OR companyName LIKE ?2", "%Export Hotel%", "%Export Restaurant%"));
    patterns.put(
        "Priority Customer",
        customerRepository.count("companyName LIKE ?1", "%Priority Customer%"));
    patterns.put(
        "Relevance Test", customerRepository.count("companyName LIKE ?1", "%Relevance Test%"));

    patterns.forEach(
        (pattern, count) -> {
          System.out.printf("  %-30s: %4d\n", pattern, count);
        });
  }

  private void analyzeTemporalDistribution() {
    // Wann wurden die Kunden erstellt?
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime oneHourAgo = now.minusHours(1);
    LocalDateTime oneDayAgo = now.minusDays(1);
    LocalDateTime oneWeekAgo = now.minusWeeks(1);
    LocalDateTime oneMonthAgo = now.minusMonths(1);

    Query query = em.createQuery("SELECT COUNT(c) FROM Customer c WHERE c.createdAt > :time");

    query.setParameter("time", oneHourAgo);
    System.out.println("Erstellt in letzter Stunde: " + query.getSingleResult());

    query.setParameter("time", oneDayAgo);
    System.out.println("Erstellt in letzten 24h: " + query.getSingleResult());

    query.setParameter("time", oneWeekAgo);
    System.out.println("Erstellt in letzter Woche: " + query.getSingleResult());

    query.setParameter("time", oneMonthAgo);
    System.out.println("Erstellt im letzten Monat: " + query.getSingleResult());

    // Ältester und neuester Kunde
    Query oldestQuery =
        em.createQuery("SELECT c FROM Customer c ORDER BY c.createdAt ASC", Customer.class)
            .setMaxResults(1);

    Query newestQuery =
        em.createQuery("SELECT c FROM Customer c ORDER BY c.createdAt DESC", Customer.class)
            .setMaxResults(1);

    List<Customer> oldest = oldestQuery.getResultList();
    List<Customer> newest = newestQuery.getResultList();

    if (!oldest.isEmpty()) {
      Customer c = oldest.get(0);
      System.out.println(
          "\nÄltester Kunde: "
              + c.getCustomerNumber()
              + " - "
              + c.getCompanyName()
              + " (erstellt: "
              + c.getCreatedAt()
              + ")");
    }

    if (!newest.isEmpty()) {
      Customer c = newest.get(0);
      System.out.println(
          "Neuester Kunde: "
              + c.getCustomerNumber()
              + " - "
              + c.getCompanyName()
              + " (erstellt: "
              + c.getCreatedAt()
              + ")");
    }
  }

  private void analyzeForeignKeyDependencies() {
    // Welche Kunden haben abhängige Daten?
    Query customersWithContactsQuery =
        em.createQuery("SELECT COUNT(DISTINCT c.customer.id) FROM CustomerContact c");
    System.out.println("Kunden mit Kontakten: " + customersWithContactsQuery.getSingleResult());

    Query customersWithOpportunitiesQuery =
        em.createQuery("SELECT COUNT(DISTINCT o.customer.id) FROM Opportunity o");
    System.out.println(
        "Kunden mit Opportunities: " + customersWithOpportunitiesQuery.getSingleResult());

    // Kunden mit Interactions (indirekt über Contacts)
    Query customersWithInteractionsQuery =
        em.createQuery(
            "SELECT COUNT(DISTINCT cc.customer.id) FROM ContactInteraction ci "
                + "JOIN ci.contact cc");
    System.out.println(
        "Kunden mit Interactions (indirekt): " + customersWithInteractionsQuery.getSingleResult());

    // Test-Kunden mit Dependencies
    Query testCustomersWithDeps =
        em.createQuery(
            "SELECT COUNT(DISTINCT c.id) FROM Customer c "
                + "WHERE (c.companyName LIKE '%Test%' OR c.customerNumber LIKE 'KD-S%' "
                + "OR c.customerNumber LIKE 'KD-E%' OR c.customerNumber LIKE 'KD-EVT%') "
                + "AND (EXISTS (SELECT 1 FROM CustomerContact cc WHERE cc.customer = c) "
                + "OR EXISTS (SELECT 1 FROM Opportunity o WHERE o.customer = c))");
    System.out.println(
        "\nTest-Kunden MIT Dependencies: " + testCustomersWithDeps.getSingleResult());

    Query testCustomersWithoutDeps =
        em.createQuery(
            "SELECT COUNT(c) FROM Customer c "
                + "WHERE (c.companyName LIKE '%Test%' OR c.customerNumber LIKE 'KD-S%' "
                + "OR c.customerNumber LIKE 'KD-E%' OR c.customerNumber LIKE 'KD-EVT%') "
                + "AND NOT EXISTS (SELECT 1 FROM CustomerContact cc WHERE cc.customer = c) "
                + "AND NOT EXISTS (SELECT 1 FROM Opportunity o WHERE o.customer = c)");
    System.out.println(
        "Test-Kunden OHNE Dependencies (löschbar): " + testCustomersWithoutDeps.getSingleResult());
  }

  private void analyzeDuplicates() {
    // Gibt es mehrfache Test-Kunden mit ähnlichen Namen?
    Query duplicateQuery =
        em.createQuery(
            "SELECT c.companyName, COUNT(c) as cnt FROM Customer c "
                + "WHERE c.companyName LIKE '%Test%' "
                + "GROUP BY c.companyName "
                + "HAVING COUNT(c) > 1 "
                + "ORDER BY COUNT(c) DESC");

    List<Object[]> duplicates = duplicateQuery.getResultList();
    if (!duplicates.isEmpty()) {
      System.out.println("Gefundene Duplikate:");
      for (Object[] row : duplicates) {
        System.out.printf("  '%s': %d mal\n", row[0], row[1]);
      }
    } else {
      System.out.println("Keine exakten Duplikate gefunden.");
    }

    // Ähnliche Namen (gleicher Prefix)
    Query similarQuery =
        em.createQuery(
            "SELECT SUBSTRING(c.companyName, 1, 20) as prefix, COUNT(c) as cnt "
                + "FROM Customer c "
                + "WHERE c.companyName LIKE '%Test%' "
                + "GROUP BY SUBSTRING(c.companyName, 1, 20) "
                + "HAVING COUNT(c) > 5 "
                + "ORDER BY COUNT(c) DESC");

    List<Object[]> similar = similarQuery.getResultList();
    if (!similar.isEmpty()) {
      System.out.println("\nÄhnliche Namen (gleicher Prefix, >5 Vorkommen):");
      for (Object[] row : similar) {
        System.out.printf("  '%s...': %d mal\n", row[0], row[1]);
      }
    }
  }

  private void distinguishOriginalFromTestData() {
    // Original-Testkunden haben [TEST] Prefix und bestimmte Nummern
    Query originalTestData =
        em.createQuery(
            "SELECT c FROM Customer c WHERE c.companyName LIKE '[TEST]%' ORDER BY c.customerNumber");
    List<Customer> originals = originalTestData.getResultList();

    System.out.println("Original-Testkunden mit [TEST] Prefix: " + originals.size());
    if (originals.size() <= 10) {
      for (Customer c : originals) {
        System.out.printf(
            "  - %s: %s (erstellt: %s)\n",
            c.getCustomerNumber(), c.getCompanyName(), c.getCreatedAt());
      }
    } else {
      System.out.println("  (Zeige erste 5)");
      for (int i = 0; i < 5; i++) {
        Customer c = originals.get(i);
        System.out.printf("  - %s: %s\n", c.getCustomerNumber(), c.getCompanyName());
      }
    }

    // Kunden die NICHT Test sind
    long nonTestCustomers =
        customerRepository.count(
            "companyName NOT LIKE ?1 AND companyName NOT LIKE ?2 "
                + "AND customerNumber NOT LIKE ?3 AND customerNumber NOT LIKE ?4 "
                + "AND customerNumber NOT LIKE ?5",
            "%Test%",
            "%test%",
            "KD-S%",
            "KD-E%",
            "KD-EVT%");
    System.out.println("\nEchte (Nicht-Test) Kunden: " + nonTestCustomers);
  }

  private void findProblematicCustomers() {
    // Kunden mit den meisten Dependencies
    Query problematicQuery =
        em.createQuery(
                "SELECT c.id, c.customerNumber, c.companyName, "
                    + "(SELECT COUNT(cc) FROM CustomerContact cc WHERE cc.customer = c) as contacts, "
                    + "(SELECT COUNT(o) FROM Opportunity o WHERE o.customer = c) as opportunities "
                    + "FROM Customer c "
                    + "WHERE c.companyName LIKE '%Test%' "
                    + "ORDER BY "
                    + "(SELECT COUNT(cc) FROM CustomerContact cc WHERE cc.customer = c) + "
                    + "(SELECT COUNT(o) FROM Opportunity o WHERE o.customer = c) DESC")
            .setMaxResults(10);

    List<Object[]> problematic = problematicQuery.getResultList();
    for (Object[] row : problematic) {
      System.out.printf(
          "  %s - %s: %d Kontakte, %d Opportunities\n", row[1], row[2], row[3], row[4]);
    }
  }
}

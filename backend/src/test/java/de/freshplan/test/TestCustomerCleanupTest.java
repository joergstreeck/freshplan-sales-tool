package de.freshplan.test;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.ContactInteractionRepository;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Bereinigt die Test-Kunden sicher von 1090 auf 74
 *
 * <p>WICHTIG: Nur Test-Kunden löschen, Original-Daten behalten!
 */
@QuarkusTest
public class TestCustomerCleanupTest {

  @Inject EntityManager em;

  @Inject CustomerRepository customerRepository;

  @Inject ContactRepository contactRepository;

  @Inject ContactInteractionRepository interactionRepository;

  @Inject OpportunityRepository opportunityRepository;

  @Test
  @Transactional
  public void cleanupTestCustomersToOriginal74() {
    System.out.println("\n" + "=".repeat(80));
    System.out.println("=== SICHERE BEREINIGUNG DER TEST-KUNDEN ===");
    System.out.println("=".repeat(80));

    // Status vorher
    long totalBefore = customerRepository.count();
    System.out.println("\nStatus VORHER:");
    System.out.println("Total Customers: " + totalBefore);

    // Identifiziere Original-Kunden (behalten)
    List<Customer> originalCustomers = customerRepository.list("companyName LIKE ?1", "[TEST]%");
    System.out.println("Original [TEST] Kunden (behalten): " + originalCustomers.size());

    // Schritt 1: Lösche Interactions von Test-Kunden
    System.out.println("\nSchritt 1: Lösche Contact Interactions...");
    int deletedInteractions =
        em.createQuery(
                "DELETE FROM ContactInteraction ci WHERE ci.contact.id IN "
                    + "(SELECT c.id FROM CustomerContact c WHERE "
                    + "c.customer.customerNumber LIKE 'KD-S%' OR "
                    + "c.customer.customerNumber LIKE 'KD-E%' OR "
                    + "c.customer.customerNumber LIKE 'KD-EVT%' OR "
                    + "c.customer.customerNumber LIKE 'KD-PG%' OR "
                    + "(c.customer.companyName LIKE '%Test%' AND c.customer.companyName NOT LIKE '[TEST]%'))")
            .executeUpdate();
    System.out.println("  Gelöscht: " + deletedInteractions + " Interactions");

    // Schritt 2: Lösche Contacts von Test-Kunden
    System.out.println("\nSchritt 2: Lösche Customer Contacts...");
    int deletedContacts =
        em.createQuery(
                "DELETE FROM CustomerContact c WHERE "
                    + "c.customer.customerNumber LIKE 'KD-S%' OR "
                    + "c.customer.customerNumber LIKE 'KD-E%' OR "
                    + "c.customer.customerNumber LIKE 'KD-EVT%' OR "
                    + "c.customer.customerNumber LIKE 'KD-PG%' OR "
                    + "(c.customer.companyName LIKE '%Test%' AND c.customer.companyName NOT LIKE '[TEST]%')")
            .executeUpdate();
    System.out.println("  Gelöscht: " + deletedContacts + " Contacts");

    // Schritt 3: Lösche Timeline Events von Test-Kunden
    System.out.println("\nSchritt 3: Lösche Timeline Events...");
    int deletedTimelineEvents =
        em.createQuery(
                "DELETE FROM CustomerTimelineEvent e WHERE "
                    + "e.customer.customerNumber LIKE 'KD-S%' OR "
                    + "e.customer.customerNumber LIKE 'KD-E%' OR "
                    + "e.customer.customerNumber LIKE 'KD-EVT%' OR "
                    + "e.customer.customerNumber LIKE 'KD-PG%' OR "
                    + "(e.customer.companyName LIKE '%Test%' AND e.customer.companyName NOT LIKE '[TEST]%')")
            .executeUpdate();
    System.out.println("  Gelöscht: " + deletedTimelineEvents + " Timeline Events");

    // Schritt 4: Lösche Opportunities von Test-Kunden
    System.out.println("\nSchritt 4: Lösche Opportunities...");
    int deletedOpportunities =
        em.createQuery(
                "DELETE FROM Opportunity o WHERE "
                    + "o.customer.customerNumber LIKE 'KD-S%' OR "
                    + "o.customer.customerNumber LIKE 'KD-E%' OR "
                    + "o.customer.customerNumber LIKE 'KD-EVT%' OR "
                    + "o.customer.customerNumber LIKE 'KD-PG%' OR "
                    + "(o.customer.companyName LIKE '%Test%' AND o.customer.companyName NOT LIKE '[TEST]%')")
            .executeUpdate();
    System.out.println("  Gelöscht: " + deletedOpportunities + " Opportunities");

    // Schritt 5: Lösche Test-Kunden (NICHT die Original [TEST] Kunden!)
    System.out.println("\nSchritt 5: Lösche Test-Kunden...");

    // Pattern-basiertes Löschen
    int deleted1 =
        em.createQuery("DELETE FROM Customer c WHERE c.customerNumber LIKE 'KD-S%'")
            .executeUpdate();
    System.out.println("  KD-S* gelöscht: " + deleted1);

    int deleted2 =
        em.createQuery("DELETE FROM Customer c WHERE c.customerNumber LIKE 'KD-E%'")
            .executeUpdate();
    System.out.println("  KD-E* gelöscht: " + deleted2);

    int deleted3 =
        em.createQuery("DELETE FROM Customer c WHERE c.customerNumber LIKE 'KD-EVT%'")
            .executeUpdate();
    System.out.println("  KD-EVT* gelöscht: " + deleted3);

    int deleted4 =
        em.createQuery("DELETE FROM Customer c WHERE c.customerNumber LIKE 'KD-PG%'")
            .executeUpdate();
    System.out.println("  KD-PG* gelöscht: " + deleted4);

    int deleted5 =
        em.createQuery("DELETE FROM Customer c WHERE c.customerNumber LIKE 'KD-CQRS%'")
            .executeUpdate();
    System.out.println("  KD-CQRS* gelöscht: " + deleted5);

    // Lösche Test-Kunden mit "Test" im Namen (aber NICHT [TEST] Prefix!)
    int deleted6 =
        em.createQuery(
                "DELETE FROM Customer c WHERE "
                    + "c.companyName LIKE '%Test%' AND "
                    + "c.companyName NOT LIKE '[TEST]%'")
            .executeUpdate();
    System.out.println("  Andere Test-Kunden gelöscht: " + deleted6);

    // Lösche spezifische Test-Patterns
    int deleted7 =
        em.createQuery(
                "DELETE FROM Customer c WHERE "
                    + "c.companyName LIKE '%Search Solutions%' OR "
                    + "c.companyName LIKE '%Export Hotel%' OR "
                    + "c.companyName LIKE '%Export Restaurant%' OR "
                    + "c.companyName LIKE '%Performance Test%' OR "
                    + "c.companyName LIKE '%Timeline Test%' OR "
                    + "c.companyName LIKE '%Pagination Test%' OR "
                    + "c.companyName LIKE '%CQRS Test%' OR "
                    + "c.companyName LIKE '%Quick Test%' OR "
                    + "c.companyName LIKE '%Dashboard Test%' OR "
                    + "c.companyName LIKE '%Relevance Test%'")
            .executeUpdate();
    System.out.println("  Spezifische Test-Pattern gelöscht: " + deleted7);

    int totalDeleted = deleted1 + deleted2 + deleted3 + deleted4 + deleted5 + deleted6 + deleted7;

    // Status nachher
    System.out.println("\n" + "=".repeat(80));
    System.out.println("=== ERGEBNIS ===");
    System.out.println("=".repeat(80));

    long totalAfter = customerRepository.count();
    System.out.println("Total Customers VORHER: " + totalBefore);
    System.out.println("Total Customers NACHHER: " + totalAfter);
    System.out.println("Gelöschte Kunden: " + totalDeleted);
    System.out.println("Verbleibende Kunden: " + totalAfter);

    // Zeige verbleibende Kunden
    System.out.println("\n=== VERBLEIBENDE KUNDEN (erste 10) ===");
    customerRepository
        .findAll()
        .range(0, 10)
        .list()
        .forEach(
            c ->
                System.out.println(
                    String.format("  - %s: %s", c.getCustomerNumber(), c.getCompanyName())));

    // Validierung
    if (totalAfter > 100) {
      System.out.println("\n⚠️ WARNUNG: Immer noch mehr als 100 Kunden!");
      System.out.println("Erwartete ~74 Original-Kunden");
    } else {
      System.out.println("\n✅ ERFOLG: Datenbank bereinigt!");
    }
  }
}

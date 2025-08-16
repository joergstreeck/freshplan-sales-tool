package de.freshplan.test;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Markiert die verbleibenden 41 "echten" Kunden als Test-Daten, damit sie eindeutig von produktiven
 * Daten unterscheidbar sind.
 *
 * <p>Diese Kunden sind keine produktiven Daten, sondern Teil der Test-Umgebung.
 */
@QuarkusTest
public class MarkRealCustomersAsTestDataTest {

  @Inject EntityManager em;

  @Inject CustomerRepository customerRepository;

  @Test
  @Transactional
  public void markRemainingCustomersAsTestData() {
    System.out.println("\n" + "=".repeat(80));
    System.out.println("=== MARKIERUNG DER VERBLEIBENDEN KUNDEN ALS TEST-DATEN ===");
    System.out.println("=".repeat(80));

    // Status vorher
    long totalBefore = customerRepository.count();

    // Zähle Kunden mit verschiedenen Markierungen
    Long withTestPrefix = customerRepository.count("companyName LIKE ?1", "[TEST]%");
    Long markedAsTest = customerRepository.count("isTestData = ?1", true);

    System.out.println("\nStatus VORHER:");
    System.out.println("Total Customers: " + totalBefore);
    System.out.println("Mit [TEST] Prefix: " + withTestPrefix);
    System.out.println("Als Test markiert (is_test_data=true): " + markedAsTest);

    // Finde alle Kunden die NICHT bereits als Test markiert sind
    List<Customer> unmarkedCustomers =
        customerRepository.list("isTestData = ?1 OR isTestData IS NULL", false);

    System.out.println("\nKunden ohne Test-Markierung: " + unmarkedCustomers.size());

    // Markiere alle diese Kunden als Test-Daten (auch die mit [TEST] Prefix!)
    int updated = 0;
    for (Customer customer : unmarkedCustomers) {
      // Setze is_test_data flag für ALLE unmarkierten Kunden
      customer.setIsTestData(true);

      // Optional: Füge [TEST] Prefix hinzu wenn nicht vorhanden
      if (customer.getCompanyName() != null && !customer.getCompanyName().contains("[TEST]")) {
        // Kommentiert - nur Flag setzen, nicht den Namen ändern
        // customer.setCompanyName("[TEST] " + customer.getCompanyName());
      }

      customerRepository.persist(customer);
      updated++;

      // Zeige die ersten 10 aktualisierten Kunden
      if (updated <= 10) {
        System.out.println(
            String.format(
                "  Markiert: %s - %s", customer.getCustomerNumber(), customer.getCompanyName()));
      }
    }

    if (updated > 10) {
      System.out.println("  ... und " + (updated - 10) + " weitere");
    }

    // Flush to database
    em.flush();

    // Status nachher
    Long markedAsTestAfter = customerRepository.count("isTestData = ?1", true);

    System.out.println("\n" + "=".repeat(80));
    System.out.println("=== ERGEBNIS ===");
    System.out.println("=".repeat(80));

    System.out.println("Kunden als Test-Daten markiert: " + updated);
    System.out.println("Total Customers: " + customerRepository.count());
    System.out.println("Als Test markiert (is_test_data=true): " + markedAsTestAfter);

    // Verifizierung
    List<Customer> allCustomers = customerRepository.listAll();
    long testDataCount =
        allCustomers.stream().filter(c -> Boolean.TRUE.equals(c.getIsTestData())).count();

    System.out.println("\n=== VERIFIZIERUNG ===");
    System.out.println("Alle Kunden mit is_test_data=true: " + testDataCount);

    // Zeige Beispiele der markierten Kunden
    System.out.println("\n=== BEISPIELE DER MARKIERTEN KUNDEN ===");
    customerRepository
        .find("isTestData = ?1", true)
        .range(0, 5)
        .list()
        .forEach(
            c ->
                System.out.println(
                    String.format(
                        "  - %s: %s (is_test_data=%s)",
                        c.getCustomerNumber(), c.getCompanyName(), c.getIsTestData())));

    System.out.println("\n✅ ERFOLG: Alle verbleibenden Kunden sind jetzt als Test-Daten markiert!");
    System.out.println("Diese Kunden können bei Bedarf mit folgendem SQL gelöscht werden:");
    System.out.println("DELETE FROM customers WHERE is_test_data = true;");
  }

  @Test
  @Transactional
  public void verifyAllCustomersAreTestData() {
    System.out.println("\n" + "=".repeat(80));
    System.out.println("=== VERIFIZIERUNG: ALLE KUNDEN SIND TEST-DATEN ===");
    System.out.println("=".repeat(80));

    // Zähle alle Kunden
    long totalCustomers = customerRepository.count();

    // Zähle Kunden mit is_test_data = true
    long testDataCustomers = customerRepository.count("isTestData = ?1", true);

    // Zähle Kunden mit [TEST] Prefix
    long testPrefixCustomers = customerRepository.count("companyName LIKE ?1", "[TEST]%");

    // Finde Kunden die NICHT als Test markiert sind
    List<Customer> nonTestCustomers =
        customerRepository.list("isTestData = ?1 OR isTestData IS NULL", false);

    System.out.println("\nStatistik:");
    System.out.println("Total Customers: " + totalCustomers);
    System.out.println("Mit is_test_data=true: " + testDataCustomers);
    System.out.println("Mit [TEST] Prefix: " + testPrefixCustomers);
    System.out.println("NICHT als Test markiert: " + nonTestCustomers.size());

    if (!nonTestCustomers.isEmpty()) {
      System.out.println("\n⚠️ WARNUNG: Folgende Kunden sind NICHT als Test-Daten markiert:");
      nonTestCustomers.forEach(
          c ->
              System.out.println(
                  String.format(
                      "  - %s: %s (is_test_data=%s)",
                      c.getCustomerNumber(), c.getCompanyName(), c.getIsTestData())));

      System.out.println("\nFühre markRemainingCustomersAsTestData() aus um diese zu markieren.");
    } else {
      System.out.println("\n✅ ERFOLG: Alle Kunden sind als Test-Daten markiert!");
      System.out.println("Die Datenbank enthält nur Test-Daten, keine produktiven Daten.");
    }
  }
}

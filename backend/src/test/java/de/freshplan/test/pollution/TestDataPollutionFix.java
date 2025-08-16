package de.freshplan.test.pollution;

import de.freshplan.domain.customer.repository.CustomerRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

/**
 * SICHERER Cleanup der Test-Daten-Pollution.
 *
 * <p>Dieser Test entfernt NUR die problematischen Test-Daten, die von fehlerhaften Tests mit
 * QuarkusTransaction.requiringNew() erstellt wurden.
 *
 * <p>Erwartung: 293 Kunden total - 50 [SEED] Kunden bleiben erhalten - 58 [TEST] Kunden bleiben
 * erhalten - 185 problematische Kunden werden gelöscht
 *
 * <p>Die gewollten [SEED] und [TEST] Kunden bleiben erhalten!
 *
 * <p>Sicherheit geht vor Schnelligkeit!
 */
@QuarkusTest
public class TestDataPollutionFix {

  @Inject CustomerRepository customerRepository;
  @Inject EntityManager entityManager;

  @Test
  @Transactional
  void safeCleanupOfTestPollution() {
    System.out.println("\n" + "=".repeat(80));
    System.out.println("🧹 SICHERE BEREINIGUNG DER TEST-DATEN-POLLUTION");
    System.out.println("=".repeat(80));

    // 1. Zähle vorher
    Long totalBefore = customerRepository.count();
    Long seedCount =
        (Long)
            entityManager
                .createNativeQuery(
                    "SELECT COUNT(*) FROM customers WHERE company_name LIKE '[SEED]%'")
                .getSingleResult();
    Long testCount =
        (Long)
            entityManager
                .createNativeQuery(
                    "SELECT COUNT(*) FROM customers WHERE company_name LIKE '[TEST]%'")
                .getSingleResult();

    System.out.println("\n📊 VORHER:");
    System.out.println("├─ Gesamt: " + totalBefore);
    System.out.println("├─ [SEED] Kunden (behalten): " + seedCount);
    System.out.println("└─ [TEST] Kunden (behalten): " + testCount);

    // 2. Lösche NUR die problematischen Test-Daten
    // WICHTIG: Zuerst abhängige Daten löschen wegen Foreign Key Constraints!

    System.out.println("\n🔧 BEREINIGUNG:");

    // WICHTIG: Definiere alle problematischen Kunden-Patterns in einer CTE
    String problematicCustomersQuery =
        "WITH problematic_customers AS ("
            + "  SELECT id FROM customers WHERE "
            + "    (company_name LIKE '[TEST-________]%' OR "
            + "     customer_number LIKE 'PF%' OR "
            + "     customer_number LIKE 'S1%' OR "
            + "     customer_number LIKE 'S2%' OR "
            + "     customer_number LIKE 'E1%' OR "
            + "     customer_number LIKE 'E2%' OR "
            + "     customer_number LIKE 'PG%' OR "
            + "     customer_number LIKE 'QK%' OR "
            + "     customer_number LIKE 'PA%' OR "
            + "     customer_number LIKE 'PI%' OR "
            + "     customer_number LIKE 'ACT%' OR "
            + "     customer_number LIKE 'INA%' OR "
            + "     customer_number LIKE 'EX%' OR "
            + "     customer_number LIKE 'PT%' OR "
            + "     customer_number LIKE 'REL%' OR "
            + "     created_by = 'test-system' OR "
            + "     (company_name LIKE '%Performance Test%' OR "
            + "      company_name LIKE '%Pagination Test%' OR "
            + "      company_name LIKE '%Export Hotel%' OR "
            + "      company_name LIKE '%Export Restaurant%' OR "
            + "      company_name LIKE '%Hotel Search%' OR "
            + "      company_name LIKE '%Restaurant Search%' OR "
            + "      company_name LIKE '%Quick Test%' OR "
            + "      company_name LIKE '%Relevance Test%' OR "
            + "      company_name LIKE '%Priority%' OR "
            + "      company_name LIKE 'CQRS Test%')) "
            + "    AND company_name NOT LIKE '[SEED]%' "
            + "    AND company_name NOT LIKE '[TEST]%' "
            + ") ";

    // Zuerst: Lösche ALLE abhängigen Daten
    System.out.println("├─ Lösche abhängige Timeline-Events...");
    int deletedTimeline =
        entityManager
            .createNativeQuery(
                problematicCustomersQuery
                    + "DELETE FROM customer_timeline_events WHERE customer_id IN (SELECT id FROM problematic_customers)")
            .executeUpdate();
    System.out.println("│  └─ " + deletedTimeline + " Timeline-Events gelöscht");

    System.out.println("├─ Lösche abhängige Opportunities...");
    int deletedOpportunities =
        entityManager
            .createNativeQuery(
                problematicCustomersQuery
                    + "DELETE FROM opportunities WHERE customer_id IN (SELECT id FROM problematic_customers)")
            .executeUpdate();
    System.out.println("│  └─ " + deletedOpportunities + " Opportunities gelöscht");

    System.out.println("├─ Lösche abhängige Kontakte...");
    int deletedContacts =
        entityManager
            .createNativeQuery(
                problematicCustomersQuery
                    + "DELETE FROM customer_contacts WHERE customer_id IN (SELECT id FROM problematic_customers)")
            .executeUpdate();
    System.out.println("│  └─ " + deletedContacts + " Kontakte gelöscht");

    // Jetzt: Lösche die Kunden selbst (mit der gleichen Bedingung)
    System.out.println("└─ Lösche problematische Kunden...");

    int totalDeleted =
        entityManager
            .createNativeQuery(
                "DELETE FROM customers WHERE "
                    + "  (company_name LIKE '[TEST-________]%' OR "
                    + "   customer_number LIKE 'PF%' OR "
                    + "   customer_number LIKE 'S1%' OR "
                    + "   customer_number LIKE 'S2%' OR "
                    + "   customer_number LIKE 'E1%' OR "
                    + "   customer_number LIKE 'E2%' OR "
                    + "   customer_number LIKE 'PG%' OR "
                    + "   customer_number LIKE 'QK%' OR "
                    + "   customer_number LIKE 'PA%' OR "
                    + "   customer_number LIKE 'PI%' OR "
                    + "   customer_number LIKE 'ACT%' OR "
                    + "   customer_number LIKE 'INA%' OR "
                    + "   customer_number LIKE 'EX%' OR "
                    + "   customer_number LIKE 'PT%' OR "
                    + "   customer_number LIKE 'REL%' OR "
                    + "   created_by = 'test-system' OR "
                    + "   (company_name LIKE '%Performance Test%' OR "
                    + "    company_name LIKE '%Pagination Test%' OR "
                    + "    company_name LIKE '%Export Hotel%' OR "
                    + "    company_name LIKE '%Export Restaurant%' OR "
                    + "    company_name LIKE '%Hotel Search%' OR "
                    + "    company_name LIKE '%Restaurant Search%' OR "
                    + "    company_name LIKE '%Quick Test%' OR "
                    + "    company_name LIKE '%Relevance Test%' OR "
                    + "    company_name LIKE '%Priority%' OR "
                    + "    company_name LIKE 'CQRS Test%')) "
                    + "  AND company_name NOT LIKE '[SEED]%' "
                    + "  AND company_name NOT LIKE '[TEST]%'")
            .executeUpdate();

    // 3. Zähle nachher
    Long totalAfter = customerRepository.count();
    Long seedCountAfter =
        (Long)
            entityManager
                .createNativeQuery(
                    "SELECT COUNT(*) FROM customers WHERE company_name LIKE '[SEED]%'")
                .getSingleResult();
    Long testCountAfter =
        (Long)
            entityManager
                .createNativeQuery(
                    "SELECT COUNT(*) FROM customers WHERE company_name LIKE '[TEST]%'")
                .getSingleResult();

    System.out.println("\n🗑️  GELÖSCHT:");
    System.out.println("├─ Timeline-Events: " + deletedTimeline);
    System.out.println("├─ Opportunities: " + deletedOpportunities);
    System.out.println("├─ Kontakte: " + deletedContacts);
    System.out.println("└─ Kunden: " + totalDeleted);

    System.out.println("\n📊 NACHHER:");
    System.out.println("├─ Gesamt: " + totalAfter);
    System.out.println("├─ [SEED] Kunden (erhalten): " + seedCountAfter);
    System.out.println("└─ [TEST] Kunden (erhalten): " + testCountAfter);

    System.out.println("\n✅ ERGEBNIS:");
    System.out.println("├─ Reduzierung: " + totalBefore + " → " + totalAfter);
    System.out.println("├─ Bereinigt: " + totalDeleted + " problematische Test-Kunden");
    System.out.println("└─ Gewollte Test-Daten bleiben erhalten!");

    // 4. Empfehlungen
    System.out.println("\n💡 NÄCHSTE SCHRITTE:");
    System.out.println("├─ SearchCQRSIntegrationTest mit @TestTransaction fixen");
    System.out.println("├─ HtmlExportCQRSIntegrationTest mit @TestTransaction fixen");
    System.out.println("├─ Alle Tests auf QuarkusTransaction.requiringNew() prüfen");
    System.out.println("└─ V9999 Migration erweitern um neue Patterns");

    System.out.println("\n" + "=".repeat(80));
    System.out.println("✅ Bereinigung abgeschlossen!");
    System.out.println("=".repeat(80) + "\n");
  }
}

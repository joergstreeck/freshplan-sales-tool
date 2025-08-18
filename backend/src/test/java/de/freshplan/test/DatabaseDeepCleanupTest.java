package de.freshplan.test;

import de.freshplan.domain.customer.repository.ContactInteractionRepository;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

/** KRITISCH: Bereinigung der 1090 Test-Kunden mit Foreign Key Beachtung */
@QuarkusTest
@TestTransaction // CI-Fix: Rollback nach Test für Database Growth Check
public class DatabaseDeepCleanupTest {

  @Inject EntityManager em;

  @Inject CustomerRepository customerRepository;

  @Inject ContactRepository contactRepository;

  @Inject ContactInteractionRepository interactionRepository;

  @Inject OpportunityRepository opportunityRepository;

  @Test
  @Transactional
  public void performDeepCleanup() {
    System.out.println("=== DEEP DATABASE CLEANUP ===");
    System.out.println("Starting with " + customerRepository.count() + " customers");

    // Schritt 1: Lösche alle Interactions (keine FK zu Customers direkt)
    System.out.println("\n1. Cleaning up Contact Interactions...");
    long interactions = interactionRepository.count();
    int deletedInteractions =
        em.createQuery(
                "DELETE FROM ContactInteraction ci WHERE ci.contact.id IN "
                    + "(SELECT c.id FROM CustomerContact c WHERE c.customer.customerNumber LIKE 'KD-%')")
            .executeUpdate();
    System.out.println(
        "   Deleted " + deletedInteractions + " of " + interactions + " interactions");

    // Schritt 2: Lösche alle Contacts
    System.out.println("\n2. Cleaning up Customer Contacts...");
    long contacts = contactRepository.count();
    int deletedContacts =
        em.createQuery(
                "DELETE FROM CustomerContact c WHERE c.customer.customerNumber LIKE 'KD-S%' "
                    + "OR c.customer.customerNumber LIKE 'KD-E%' "
                    + "OR c.customer.customerNumber LIKE 'KD-EVT%' "
                    + "OR c.customer.customerNumber LIKE 'KD-CQRS%'")
            .executeUpdate();
    System.out.println("   Deleted " + deletedContacts + " of " + contacts + " contacts");

    // Schritt 3: Lösche alle Opportunities
    System.out.println("\n3. Cleaning up Opportunities...");
    long opportunities = opportunityRepository.count();
    int deletedOpportunities =
        em.createQuery(
                "DELETE FROM Opportunity o WHERE o.customer.customerNumber LIKE 'KD-S%' "
                    + "OR o.customer.customerNumber LIKE 'KD-E%' "
                    + "OR o.customer.customerNumber LIKE 'KD-EVT%' "
                    + "OR o.customer.customerNumber LIKE 'KD-CQRS%'")
            .executeUpdate();
    System.out.println(
        "   Deleted " + deletedOpportunities + " of " + opportunities + " opportunities");

    // Schritt 4: Jetzt können wir Customers löschen
    System.out.println("\n4. Cleaning up Test Customers...");

    // Pattern-basiertes Löschen
    int deleted1 =
        em.createQuery("DELETE FROM Customer c WHERE c.customerNumber LIKE 'KD-S%'")
            .executeUpdate();
    System.out.println("   Deleted " + deleted1 + " KD-S* customers");

    int deleted2 =
        em.createQuery("DELETE FROM Customer c WHERE c.customerNumber LIKE 'KD-E%'")
            .executeUpdate();
    System.out.println("   Deleted " + deleted2 + " KD-E* customers");

    int deleted3 =
        em.createQuery("DELETE FROM Customer c WHERE c.customerNumber LIKE 'KD-EVT%'")
            .executeUpdate();
    System.out.println("   Deleted " + deleted3 + " KD-EVT* customers");

    // Name-basiertes Löschen
    int deleted4 =
        em.createQuery("DELETE FROM Customer c WHERE c.companyName LIKE '%Test%'").executeUpdate();
    System.out.println("   Deleted " + deleted4 + " customers with 'Test' in name");

    int deleted5 =
        em.createQuery("DELETE FROM Customer c WHERE c.companyName LIKE '%Search Solutions%'")
            .executeUpdate();
    System.out.println("   Deleted " + deleted5 + " 'Search Solutions' customers");

    int deleted6 =
        em.createQuery("DELETE FROM Customer c WHERE c.companyName LIKE '%Export%'")
            .executeUpdate();
    System.out.println("   Deleted " + deleted6 + " 'Export' customers");

    // Behalte nur die Original-Testkunden mit [TEST] Prefix
    System.out.println("\n=== CLEANUP COMPLETE ===");
    System.out.println("Remaining customers: " + customerRepository.count());
    System.out.println("Remaining contacts: " + contactRepository.count());
    System.out.println("Remaining interactions: " + interactionRepository.count());
    System.out.println("Remaining opportunities: " + opportunityRepository.count());

    // Zeige verbleibende Kunden
    System.out.println("\n=== REMAINING CUSTOMERS (First 10) ===");
    customerRepository
        .findAll()
        .range(0, 10)
        .list()
        .forEach(
            c ->
                System.out.println(
                    String.format("  - %s: %s", c.getCustomerNumber(), c.getCompanyName())));
  }

  @Test
  public void analyzeOnly() {
    System.out.println("=== DATABASE ANALYSIS (NO CHANGES) ===");
    System.out.println("Total Customers: " + customerRepository.count());
    System.out.println("Total Contacts: " + contactRepository.count());
    System.out.println("Total Interactions: " + interactionRepository.count());
    System.out.println("Total Opportunities: " + opportunityRepository.count());

    System.out.println("\nTest Customer Patterns:");
    System.out.println("  KD-S*: " + customerRepository.count("customerNumber LIKE ?1", "KD-S%"));
    System.out.println("  KD-E*: " + customerRepository.count("customerNumber LIKE ?1", "KD-E%"));
    System.out.println(
        "  KD-EVT*: " + customerRepository.count("customerNumber LIKE ?1", "KD-EVT%"));
    System.out.println("  *Test*: " + customerRepository.count("companyName LIKE ?1", "%Test%"));
    System.out.println("  [TEST]*: " + customerRepository.count("companyName LIKE ?1", "[TEST]%"));
  }
}

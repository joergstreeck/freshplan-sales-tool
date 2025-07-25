package de.freshplan.api;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.annotation.Priority;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.jboss.logging.Logger;

/**
 * Opportunity Test Data Initializer
 *
 * <p>Erstellt systematische Testdaten für das Opportunity Pipeline System. Generiert realistische
 * Verkaufschancen in verschiedenen Stages mit unterschiedlichen Werten und Wahrscheinlichkeiten.
 *
 * <p>Nur aktiv im dev Profile.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
@IfBuildProfile("dev")
public class OpportunityDataInitializer {

  private static final Logger LOG = Logger.getLogger(OpportunityDataInitializer.class);

  @Inject OpportunityRepository opportunityRepository;
  @Inject CustomerRepository customerRepository;
  @Inject UserRepository userRepository;

  @Transactional
  void onStart(@Observes @Priority(1000) StartupEvent ev) {
    LOG.info("🎯 Initializing Opportunity test data for Pipeline testing...");

    // Prüfe ob bereits Opportunities existieren
    long existingCount = opportunityRepository.count();
    if (existingCount > 0) {
      LOG.info("Found " + existingCount + " existing opportunities, skipping initialization");
      return;
    }

    // Prüfe ob Customers und Users existieren
    long customerCount = customerRepository.count();
    long userCount = userRepository.count();
    
    if (customerCount == 0) {
      LOG.warn("⚠️ No customers found! CustomerDataInitializer should run first.");
      return;
    }
    
    if (userCount == 0) {
      LOG.warn("⚠️ No users found! Cannot create opportunities without users.");
      return;
    }

    // Lade verfügbare Customers und Users
    List<Customer> customers = customerRepository.listAll();
    List<User> users = userRepository.listAll();
    
    LOG.info("Creating opportunities with " + customers.size() + " customers and " + users.size() + " users");

    // 1. REALISTISCHE PIPELINE-VERTEILUNG
    LOG.info("Creating realistic pipeline distribution...");
    createRealisticPipeline(customers, users);

    // 2. HIGH-VALUE OPPORTUNITIES
    LOG.info("Creating high-value opportunities...");
    createHighValueOpportunities(customers, users);

    // 3. EDGE CASES UND TESTSZENARIEN
    LOG.info("Creating edge cases and test scenarios...");
    createEdgeCaseOpportunities(customers, users);

    // 4. VERSCHIEDENE ZEITRAHMEN
    LOG.info("Creating opportunities with different timeframes...");
    createTimeframeVariations(customers, users);

    long totalOpportunities = opportunityRepository.count();
    LOG.info("🎯 Opportunity test data initialized successfully! Total opportunities: " + totalOpportunities);
    LOG.info("💡 Pipeline contains opportunities in all stages for comprehensive testing");
  }

  /**
   * Erstellt eine realistische Verteilung von Opportunities über alle Pipeline-Stages
   * Folgt der typischen Sales-Funnel-Verteilung: Viele Leads, weniger in späteren Stages
   */
  private void createRealisticPipeline(List<Customer> customers, List<User> users) {
    // NEW_LEAD: 40% der Pipeline (8 Opportunities)
    for (int i = 1; i <= 8; i++) {
      createOpportunity(
          "Lead " + i + ": " + getOpportunityName(i, "Lead"),
          OpportunityStage.NEW_LEAD,
          getRandomCustomer(customers, i),
          getRandomUser(users, i),
          new BigDecimal(5000 + (i * 2000)), // 5k - 21k
          LocalDate.now().plusDays(30 + i * 5),
          "Neuer Kontakt über " + getContactSource(i)
      );
    }

    // QUALIFICATION: 25% der Pipeline (5 Opportunities)
    for (int i = 1; i <= 5; i++) {
      createOpportunity(
          "Qualifizierung " + i + ": " + getOpportunityName(i, "Qualified"),
          OpportunityStage.QUALIFICATION,
          getRandomCustomer(customers, i + 8),
          getRandomUser(users, i),
          new BigDecimal(8000 + (i * 3000)), // 8k - 20k
          LocalDate.now().plusDays(20 + i * 3),
          "Lead qualifiziert, Interesse an " + getProductType(i)
      );
    }

    // NEEDS_ANALYSIS: 15% der Pipeline (3 Opportunities)
    for (int i = 1; i <= 3; i++) {
      createOpportunity(
          "Bedarfsanalyse " + i + ": " + getOpportunityName(i, "Analysis"),
          OpportunityStage.NEEDS_ANALYSIS,
          getRandomCustomer(customers, i + 13),
          getRandomUser(users, i),
          new BigDecimal(12000 + (i * 5000)), // 12k - 22k
          LocalDate.now().plusDays(15 + i * 2),
          "Detaillierte Anforderungen ermittelt für " + getServiceType(i)
      );
    }

    // PROPOSAL: 10% der Pipeline (2 Opportunities)
    for (int i = 1; i <= 2; i++) {
      createOpportunity(
          "Angebot " + i + ": " + getOpportunityName(i, "Proposal"),
          OpportunityStage.PROPOSAL,
          getRandomCustomer(customers, i + 16),
          getRandomUser(users, i),
          new BigDecimal(18000 + (i * 7000)), // 18k - 25k
          LocalDate.now().plusDays(10 + i),
          "Angebot erstellt und versendet am " + LocalDate.now().minusDays(i * 2)
      );
    }

    // NEGOTIATION: 7% der Pipeline (1 Opportunity)
    createOpportunity(
        "Verhandlung 1: Großauftrag Hotel-Kette",
        OpportunityStage.NEGOTIATION,
        getRandomCustomer(customers, 18),
        getRandomUser(users, 1),
        new BigDecimal("35000"),
        LocalDate.now().plusDays(7),
        "Finale Verhandlungsrunde, Preisverhandlung läuft"
    );

    // CLOSED_WON: 2% der Pipeline (1 Opportunity - Erfolgsbeispiel)
    createOpportunity(
        "Gewonnen: Restaurant-Kette Vollservice",
        OpportunityStage.CLOSED_WON,
        getRandomCustomer(customers, 19),
        getRandomUser(users, 2),
        new BigDecimal("28000"),
        LocalDate.now().minusDays(3),
        "Vertragsabschluss erfolgreich, Onboarding startet nächste Woche"
    );

    // CLOSED_LOST: 1% der Pipeline (1 Opportunity - Lernbeispiel)
    createOpportunity(
        "Verloren: Budget-Engpass Schulcatering",
        OpportunityStage.CLOSED_LOST,
        getRandomCustomer(customers, 20),
        getRandomUser(users, 3),
        new BigDecimal("15000"),
        LocalDate.now().minusDays(10),
        "Verlustgrund: Budget nicht ausreichend, Wiedervorlage Q2/2026"
    );
  }

  /**
   * Erstellt High-Value Opportunities für Executive Dashboard Testing
   */
  private void createHighValueOpportunities(List<Customer> customers, List<User> users) {
    // Mega-Deal in Verhandlung
    createOpportunity(
        "MEGA-DEAL: Internationale Hotel-Gruppe",
        OpportunityStage.NEGOTIATION,
        getRandomCustomer(customers, 0),
        getRandomUser(users, 0),
        new BigDecimal("150000"),
        LocalDate.now().plusDays(14),
        "Strategisches Partnership für 50+ Hotels europaweit"
    );

    // Premium Catering Proposal
    createOpportunity(
        "Premium Event-Catering Jahresvertrag",
        OpportunityStage.PROPOSAL,
        getRandomCustomer(customers, 1),
        getRandomUser(users, 1),
        new BigDecimal("85000"),
        LocalDate.now().plusDays(8),
        "Exklusiver Catering-Partner für alle Firmenereignisse"
    );

    // Franchise-System Lead
    createOpportunity(
        "Franchise-System: 20 Standorte",
        OpportunityStage.QUALIFICATION,
        getRandomCustomer(customers, 2),
        getRandomUser(users, 0),
        new BigDecimal("200000"),
        LocalDate.now().plusDays(45),
        "Franchise-Kette möchte alle Standorte umstellen"
    );
  }

  /**
   * Erstellt Edge Cases und spezielle Testszenarien
   */
  private void createEdgeCaseOpportunities(List<Customer> customers, List<User> users) {
    // Null/Empty Values Test
    Opportunity minimalOpp = new Opportunity();
    minimalOpp.setName("Minimal Opportunity Test");
    minimalOpp.setStage(OpportunityStage.NEW_LEAD);
    minimalOpp.setAssignedTo(getRandomUser(users, 0));
    // Bewusst keine Customer, expectedValue, etc. setzen
    opportunityRepository.persist(minimalOpp);

    // Maximum Values Test
    createOpportunity(
        "Maximum Test: " + "X".repeat(200), // Lange Namen testen
        OpportunityStage.PROPOSAL,
        getRandomCustomer(customers, 0),
        getRandomUser(users, 1),
        new BigDecimal("999999.99"), // Maximum Wert
        LocalDate.now().plusYears(1), // Fernes Datum
        "Test für maximale Feldlängen und Werte" + "X".repeat(1000) // Lange Beschreibung
    );

    // Zero Values Test
    createOpportunity(
        "Zero Value Test",
        OpportunityStage.NEW_LEAD,
        getRandomCustomer(customers, 1),
        getRandomUser(users, 2),
        BigDecimal.ZERO,
        LocalDate.now().plusDays(1),
        "Test mit Nullwerten"
    );

    // Past Date Test (überfällig)
    createOpportunity(
        "Überfällige Opportunity",
        OpportunityStage.NEGOTIATION,
        getRandomCustomer(customers, 2),
        getRandomUser(users, 0),
        new BigDecimal("45000"),
        LocalDate.now().minusDays(15), // Überfällig
        "Test für überfällige Opportunities"
    );
  }

  /**
   * Erstellt Opportunities mit verschiedenen Zeitrahmen
   */
  private void createTimeframeVariations(List<Customer> customers, List<User> users) {
    // Sehr kurzfristig (diese Woche)
    createOpportunity(
        "Eilauftrag: Kurzfristige Veranstaltung",
        OpportunityStage.PROPOSAL,
        getRandomCustomer(customers, 0),
        getRandomUser(users, 1),
        new BigDecimal("12000"),
        LocalDate.now().plusDays(3),
        "Kurzfristige Anfrage für Veranstaltung nächste Woche"
    );

    // Langfristig (nächstes Jahr)
    createOpportunity(
        "Strategisches Projekt 2026",
        OpportunityStage.QUALIFICATION,
        getRandomCustomer(customers, 1),
        getRandomUser(users, 2),
        new BigDecimal("75000"),
        LocalDate.now().plusMonths(8),
        "Langfristige Planung für Expansion 2026"
    );

    // Quartalsziel
    createOpportunity(
        "Q1 Zielauftrag: Restaurant-Modernisierung",
        OpportunityStage.NEEDS_ANALYSIS,
        getRandomCustomer(customers, 2),
        getRandomUser(users, 0),
        new BigDecimal("32000"),
        LocalDate.now().plusDays(25), // Ende Q1
        "Wichtig für Q1 Zielerreichung"
    );
  }

  /**
   * Hilfsmethode zum Erstellen einer Opportunity
   */
  private void createOpportunity(
      String name,
      OpportunityStage stage,
      Customer customer,
      User assignedTo,
      BigDecimal expectedValue,
      LocalDate expectedCloseDate,
      String description) {
    
    Opportunity opportunity = new Opportunity();
    opportunity.setName(name);
    opportunity.setStage(stage);
    opportunity.setCustomer(customer);
    opportunity.setAssignedTo(assignedTo);
    opportunity.setExpectedValue(expectedValue);
    opportunity.setExpectedCloseDate(expectedCloseDate);
    opportunity.setDescription(description);
    
    // Stage-spezifische Wahrscheinlichkeiten
    opportunity.setProbability(stage.getDefaultProbability());
    
    opportunityRepository.persist(opportunity);
    LOG.debug("Created opportunity: " + name + " (Stage: " + stage + ", Value: " + expectedValue + "€)");
  }

  // Hilfsmethoden für realistische Testdaten
  private Customer getRandomCustomer(List<Customer> customers, int index) {
    if (customers.isEmpty()) return null;
    return customers.get(index % customers.size());
  }

  private User getRandomUser(List<User> users, int index) {
    if (users.isEmpty()) return null;
    return users.get(index % users.size());
  }

  private String getOpportunityName(int index, String type) {
    String[] businessTypes = {
        "Wochenmenü-Service", "Event-Catering", "Betriebskantine", 
        "Hotel-Vollpension", "Schulverpflegung", "Seniorenheim-Catering",
        "Conference-Catering", "Hochzeits-Service", "Food-Truck-Belieferung"
    };
    return businessTypes[index % businessTypes.length];
  }

  private String getContactSource(int index) {
    String[] sources = {
        "Website-Anfrage", "Empfehlung", "Messe-Kontakt", 
        "Kaltakquise", "LinkedIn", "Google Ads", "Bestehender Kunde"
    };
    return sources[index % sources.length];
  }

  private String getProductType(int index) {
    String[] products = {
        "Bio-Menüs", "Vegetarisch/Vegan", "Internationale Küche",
        "Diätmenüs", "Fingerfood", "Buffet-Service", "À-la-carte"
    };
    return products[index % products.length];
  }

  private String getServiceType(int index) {
    String[] services = {
        "Vollservice mit Personal", "Lieferservice", "Selbstabholung",
        "Hybrid-Modell", "Nur Wochenenden", "Täglich", "Events only"
    };
    return services[index % services.length];
  }
}
package de.freshplan.api;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.infrastructure.util.InitializerUtils;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.jboss.logging.Logger;

/**
 * Realistic test data initializer for Customer Contacts following FC-005 specification. Creates
 * comprehensive test scenarios with German business context.
 *
 * <p>All test data is marked with isTestData flag for easy removal.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
@IfBuildProfile("dev")
public class TestDataContactInitializer {

  private static final Logger LOG = Logger.getLogger(TestDataContactInitializer.class);
  private static final String TEST_PREFIX = "TEST_CUST_";
  private static final String TEST_USER = "test-system";

  @Inject CustomerRepository customerRepository;
  @Inject CustomerContactRepository contactRepository;

  private final Random random = new Random(42); // Fixed seed for reproducibility

  @Transactional
  void onStart(@Observes @Priority(600) StartupEvent ev) {
    // Skip initialization if we're running in test mode
    if (InitializerUtils.isTestMode()) {
      LOG.debug("Skipping contact test data initialization in test mode");
      return;
    }

    LOG.info("🎯 Initializing realistic test contacts for FC-005 Contact Management...");

    // Check if we already have test contacts
    long existingContacts = contactRepository.count();
    if (existingContacts > 100) {
      LOG.info(
          "Test contacts already exist ("
              + existingContacts
              + " contacts). Skipping initialization.");
      return;
    }

    try {
      // Create contacts for existing customers
      createPremiumEnterpriseContacts();
      createMittelstandContacts();
      createSmallBusinessContacts();
      createProblemCustomerContacts();
      createOpportunityContacts();

      long totalContacts = contactRepository.count();
      LOG.info(
          "✅ Successfully created "
              + totalContacts
              + " test contacts across all customer segments");

    } catch (Exception e) {
      LOG.error("Failed to initialize test contacts", e);
    }
  }

  /** Creates contacts for premium enterprise customers with complex hierarchies */
  private void createPremiumEnterpriseContacts() {
    LOG.info("Creating premium enterprise contacts...");

    // Find or create Müller Gastro Gruppe
    Customer muellerGastro =
        findOrCreateCustomer(
            "TEST_CUST_001",
            "Müller Gastro Gruppe GmbH",
            CustomerType.UNTERNEHMEN,
            Industry.RESTAURANT,
            2500,
            45);

    if (muellerGastro != null) {
      // CEO - Hauptentscheider
      CustomerContact ceo =
          createContact(
              muellerGastro,
              "Dr. Thomas",
              "Müller",
              "CEO",
              "Geschäftsführung",
              "thomas.mueller@mueller-gastro.de",
              "+49 89 123456-100",
              "+49 170 1234567",
              true,
              true, // isPrimary, isDecisionMaker
              "Golf-Enthusiast, Familie mit 2 Kindern. Bevorzugt persönliche Treffen. "
                  + "Sehr qualitätsbewusst, legt Wert auf langfristige Partnerschaften.",
              LocalDateTime.now().minusDays(3) // Last contact 3 days ago - HOT
              );

      // CFO - Mitentscheiderin
      CustomerContact cfo =
          createContact(
              muellerGastro,
              "Sandra",
              "Bergmann",
              "CFO",
              "Finanzen",
              "sandra.bergmann@mueller-gastro.de",
              "+49 89 123456-200",
              "+49 171 2345678",
              false,
              true, // not primary, but decision maker
              "Sehr zahlenorientiert. Benötigt immer detaillierte Kostenaufstellungen. "
                  + "Trifft Budget-Entscheidungen über 50k EUR.",
              LocalDateTime.now().minusWeeks(2) // WARM
              );

      // Einkaufsleiter - Operative Ebene
      CustomerContact einkauf =
          createContact(
              muellerGastro,
              "Michael",
              "Weber",
              "Einkaufsleiter",
              "Einkauf",
              "michael.weber@mueller-gastro.de",
              "+49 89 123456-150",
              "+49 172 3456789",
              false,
              false,
              "Hauptkontakt für operative Bestellungen. Bevorzugt WhatsApp für schnelle Abstimmungen. "
                  + "Sehr detailorientiert bei Lieferterminen.",
              LocalDateTime.now().minusDays(5) // HOT
              );
      einkauf.setPreferredCommunicationMethod(CommunicationMethod.WHATSAPP);
      contactRepository.persist(einkauf);

      // Regional Manager Nord - Standortverantwortung
      CustomerContact regionalNord =
          createContact(
              muellerGastro,
              "Lisa",
              "Schmidt",
              "Regional Manager",
              "Operations Nord",
              "lisa.schmidt@mueller-gastro.de",
              "+49 40 234567-100",
              "+49 173 4567890",
              false,
              false,
              "Verantwortlich für 15 Filialen in Norddeutschland. "
                  + "ACHTUNG: Lange kein Kontakt, Beziehung kühlt ab!",
              LocalDateTime.now().minusDays(45) // COOLING - needs attention!
              );
      regionalNord.setNotes(regionalNord.getNotes() + " [WARMTH: COOLING - Action required!]");
      contactRepository.persist(regionalNord);

      // Regional Manager Süd
      CustomerContact regionalSued =
          createContact(
              muellerGastro,
              "Frank",
              "Zimmermann",
              "Regional Manager",
              "Operations Süd",
              "frank.zimmermann@mueller-gastro.de",
              "+49 89 123456-180",
              "+49 174 5678901",
              false,
              false,
              "20 Filialen in Bayern und Baden-Württemberg. Sehr technikaffin, nutzt gerne digitale Tools.",
              LocalDateTime.now().minusDays(20) // WARM
              );

      // Regional Manager West - Problem Contact
      CustomerContact regionalWest =
          createContact(
              muellerGastro,
              "Julia",
              "Hoffmann",
              "Regional Manager",
              "Operations West",
              "julia.hoffmann@mueller-gastro.de",
              "+49 221 345678-100",
              "+49 175 6789012",
              false,
              false,
              "10 Filialen im Rheinland. KRITISCH: Sehr unzufrieden mit letzter Lieferung! "
                  + "Benötigt dringend persönliches Gespräch!",
              LocalDateTime.now().minusDays(75) // COLD - critical!
              );
      regionalWest.setNotes(regionalWest.getNotes() + " [WARMTH: COLD - URGENT ACTION NEEDED!]");
      contactRepository.persist(regionalWest);

      // IT-Leiter - Technischer Kontakt
      CustomerContact itLeiter =
          createContact(
              muellerGastro,
              "Peter",
              "Klein",
              "IT-Leiter",
              "IT & Digitalisierung",
              "peter.klein@mueller-gastro.de",
              "+49 89 123456-300",
              "+49 176 7890123",
              false,
              false,
              "Wichtig für alle System-Integrationen. Interessiert an API-Schnittstellen.",
              LocalDateTime.now().minusDays(30) // WARM
              );

      // Assistenz - Gatekeeper
      CustomerContact assistenz =
          createContact(
              muellerGastro,
              "Anna",
              "Bauer",
              "Assistenz der Geschäftsführung",
              "Geschäftsführung",
              "anna.bauer@mueller-gastro.de",
              "+49 89 123456-101",
              "+49 177 8901234",
              false,
              false,
              "Kontrolliert alle Termine mit Dr. Müller. Sehr hilfsbereit, kennt alle internen Abläufe. "
                  + "Beste Zeiten für Anrufe: Di-Do 9-11 Uhr.",
              LocalDateTime.now().minusDays(1) // HOT - frequent contact
              );

      // Set reporting hierarchy
      einkauf.setReportsTo(cfo);
      regionalNord.setReportsTo(ceo);
      regionalSued.setReportsTo(ceo);
      regionalWest.setReportsTo(ceo);
      assistenz.setReportsTo(ceo);
      
      // Note: createContact() already persists the contacts, so no additional persist() calls needed
      // The setReportsTo() changes will be persisted automatically when the transaction commits

      LOG.info("Created 8 contacts for Müller Gastro Gruppe with complex hierarchy");
    }

    // Create BioFresh Märkte AG
    Customer bioFresh =
        findOrCreateCustomer(
            "TEST_CUST_002",
            "BioFresh Märkte AG",
            CustomerType.UNTERNEHMEN,
            Industry.EINZELHANDEL,
            850,
            28);

    if (bioFresh != null) {
      // Vorstand mit Geburtstag in 5 Tagen
      CustomerContact vorstand =
          createContact(
              bioFresh,
              "Markus",
              "Grün",
              "Vorstand",
              "Vorstand",
              "markus.gruen@biofresh.de",
              "+49 711 234567-100",
              "+49 170 2345678",
              true,
              true,
              "Nachhaltigkeits-Fokus, vegetarisch, fährt Tesla. "
                  + "GEBURTSTAG am "
                  + LocalDate.now().plusDays(5).toString()
                  + "!",
              LocalDateTime.now().minusDays(10) // WARM
              );

      // Einkauf Bio-Produkte
      CustomerContact einkaufBio =
          createContact(
              bioFresh,
              "Claudia",
              "Weiss",
              "Einkaufsleiterin Bio",
              "Einkauf",
              "claudia.weiss@biofresh.de",
              "+49 711 234567-150",
              "+49 171 3456789",
              false,
              true,
              "Sehr detailorientiert, prüft alle Zertifikate genau. Mag keine Überraschungen.",
              LocalDateTime.now().minusDays(2) // HOT
              );

      // Filialkoordinator
      CustomerContact filialKoord =
          createContact(
              bioFresh,
              "Stefan",
              "Rot",
              "Filialkoordinator",
              "Operations",
              "stefan.rot@biofresh.de",
              "+49 711 234567-200",
              "+49 172 4567890",
              false,
              false,
              "Mobile-First Nutzer, immer unterwegs. Am besten per WhatsApp erreichbar.",
              LocalDateTime.now().minusDays(15) // WARM
              );
      filialKoord.setPreferredCommunicationMethod(CommunicationMethod.WHATSAPP);
      contactRepository.persist(filialKoord);

      LOG.info("Created 3 contacts for BioFresh Märkte");
    }
  }

  /** Creates contacts for Mittelstand customers */
  private void createMittelstandContacts() {
    LOG.info("Creating Mittelstand contacts...");

    // Hotel Zur Post - Family business with generation change
    Customer hotelZurPost =
        findOrCreateCustomer(
            "TEST_CUST_011",
            "Hotel Zur Post GmbH & Co. KG",
            CustomerType.UNTERNEHMEN,
            Industry.HOTEL,
            120,
            3);

    if (hotelZurPost != null) {
      // Senior Inhaber
      CustomerContact seniorInhaber =
          createContact(
              hotelZurPost,
              "Robert",
              "Huber",
              "Geschäftsführer",
              "Geschäftsführung",
              "robert.huber@hotel-zur-post.de",
              "+49 8031 234567",
              "+49 170 3456789",
              false,
              true,
              "Traditionsbewusst, 40 Jahre Erfahrung. Übergibt langsam an Sohn Martin. "
                  + "Bevorzugt persönliche Treffen und Handschlag-Qualität.",
              LocalDateTime.now().minusDays(30) // WARM
              );

      // Junior Inhaber - Nachfolger
      CustomerContact juniorInhaber =
          createContact(
              hotelZurPost,
              "Martin",
              "Huber",
              "Geschäftsführer",
              "Geschäftsführung",
              "martin.huber@hotel-zur-post.de",
              "+49 8031 234568",
              "+49 171 4567890",
              true,
              true,
              "Übernimmt schrittweise von Vater. Sehr digital-affin, will modernisieren. "
                  + "Interessiert an neuen Konzepten und Nachhaltigkeit.",
              LocalDateTime.now().minusDays(4) // HOT
              );

      // Hoteldirektorin - Operative Leitung
      CustomerContact direktorin =
          createContact(
              hotelZurPost,
              "Maria",
              "Schneider",
              "Hoteldirektorin",
              "Operations",
              "maria.schneider@hotel-zur-post.de",
              "+49 8031 234569",
              "+49 172 5678901",
              false,
              false,
              "15 Jahre im Haus, kennt alle Abläufe. Beste Ansprechpartnerin für Tagesgeschäft. "
                  + "Sehr zuverlässig und qualitätsbewusst.",
              LocalDateTime.now().minusDays(2) // HOT
              );

      LOG.info("Created 3 contacts for Hotel Zur Post (generation change scenario)");
    }

    // Bäckerei Goldkorn - Traditional family business
    Customer baeckerei =
        findOrCreateCustomer(
            "TEST_CUST_012",
            "Bäckerei Goldkorn GmbH",
            CustomerType.UNTERNEHMEN,
            Industry.SONSTIGE, // Bakery
            85,
            12);

    if (baeckerei != null) {
      // Geschäftsführer - schwer erreichbar
      CustomerContact geschaeftsfuehrer =
          createContact(
              baeckerei,
              "Hans",
              "Meier",
              "Geschäftsführer",
              "Geschäftsführung",
              "hans.meier@goldkorn.de",
              "+49 7531 345678",
              null, // no mobile
              true,
              true,
              "Traditionalist, mag keine E-Mails. Bevorzugt persönliche Treffen. "
                  + "Beste Zeit: Dienstag nachmittags. SCHWER ERREICHBAR!",
              LocalDateTime.now().minusDays(40) // COOLING
              );
      geschaeftsfuehrer.setPreferredCommunicationMethod(CommunicationMethod.PHONE);
      contactRepository.persist(geschaeftsfuehrer);

      // Ehefrau - faktische CFO
      CustomerContact ehefrau =
          createContact(
              baeckerei,
              "Sabine",
              "Meier",
              "Verwaltung",
              "Verwaltung",
              "sabine.meier@goldkorn.de",
              "+49 7531 345679",
              "+49 173 6789012",
              false,
              true,
              "Ehefrau, führt die Bücher. Trifft alle finanziellen Entscheidungen. "
                  + "Deutlich besser erreichbar als Ehemann.",
              LocalDateTime.now().minusDays(8) // WARM
              );

      LOG.info("Created 2 contacts for Bäckerei Goldkorn");
    }
  }

  /** Creates contacts for small business customers */
  private void createSmallBusinessContacts() {
    LOG.info("Creating small business contacts...");

    // Café Einstein - Single owner, very loyal
    Customer cafeEinstein =
        findOrCreateCustomer(
            "TEST_CUST_021",
            "Café Einstein",
            CustomerType.PRIVAT, // Small business owner
            Industry.RESTAURANT,
            8,
            1);

    if (cafeEinstein != null) {
      CustomerContact inhaber =
          createContact(
              cafeEinstein,
              "Tom",
              "Fischer",
              "Inhaber",
              null,
              "tom@cafe-einstein.de",
              null,
              "+49 175 7890123",
              true,
              true,
              "Sehr loyal, Kunde seit 8 Jahren. Kommuniziert nur per WhatsApp. "
                  + "Bestellt immer spontan, braucht schnelle Lieferung.",
              LocalDateTime.now().minusDays(1) // HOT - daily contact
              );
      inhaber.setPreferredCommunicationMethod(CommunicationMethod.WHATSAPP);
      contactRepository.persist(inhaber);

      LOG.info("Created contact for Café Einstein");
    }

    // Create more small businesses with varied contact patterns
    for (int i = 22; i <= 25; i++) {
      String businessName = generateSmallBusinessName(i);
      Customer smallBusiness =
          findOrCreateCustomer(
              "TEST_CUST_0" + i,
              businessName,
              CustomerType.PRIVAT, // Small business owner
              Industry.RESTAURANT,
              random.nextInt(15) + 3,
              1);

      if (smallBusiness != null) {
        String firstName = getRandomFirstName();
        String lastName = getRandomLastName();

        CustomerContact owner =
            createContact(
                smallBusiness,
                firstName,
                lastName,
                "Inhaber",
                null,
                firstName.toLowerCase()
                    + "@"
                    + businessName.toLowerCase().replace(" ", "-")
                    + ".de",
                "+49 " + generatePhoneNumber(),
                "+49 " + generateMobileNumber(),
                true,
                true,
                generateContactNotes(i),
                generateLastContactDate(i));

        LOG.debug("Created contact for " + businessName);
      }
    }
  }

  /** Creates contacts for problem customers (at risk, payment issues, etc.) */
  private void createProblemCustomerContacts() {
    LOG.info("Creating problem customer contacts...");

    // Schwierig & Partner - Payment issues
    Customer schwierig =
        findOrCreateCustomer(
            "TEST_CUST_031",
            "Schwierig & Partner GbR",
            CustomerType.UNTERNEHMEN,
            Industry.CATERING,
            25,
            1);

    if (schwierig != null) {
      // Geschäftsführer - reagiert nicht
      CustomerContact gf =
          createContact(
              schwierig,
              "Klaus",
              "Schwierig",
              "Geschäftsführer",
              "Geschäftsführung",
              "klaus.schwierig@schwierig-partner.de",
              "+49 30 456789",
              "+49 176 8901234",
              true,
              true,
              "KRITISCH: Reagiert nicht auf Kontaktversuche! Zahlungsverzug 90 Tage. "
                  + "Letzte Mahnung versendet. KUNDE AT RISK!",
              LocalDateTime.now().minusDays(90) // COLD - no response
              );
      gf.setNotes(gf.getNotes() + " [STATUS: AT_RISK] [PAYMENT: OVERDUE]");
      contactRepository.persist(gf);

      // Mitinhaberin - versucht zu vermitteln
      CustomerContact partner =
          createContact(
              schwierig,
              "Petra",
              "Partner",
              "Mitinhaberin",
              "Geschäftsführung",
              "petra.partner@schwierig-partner.de",
              "+49 30 456790",
              "+49 177 9012345",
              false,
              true,
              "Versucht zu vermitteln. Sagt Zahlung kommt 'bald'. "
                  + "Etwas zugänglicher als Herr Schwierig.",
              LocalDateTime.now().minusDays(35) // COOLING
              );

      LOG.info("Created 2 contacts for problem customer Schwierig & Partner");
    }
  }

  /** Creates contacts for opportunity customers */
  private void createOpportunityContacts() {
    LOG.info("Creating opportunity contacts...");

    // Expansion Restaurants - High opportunity
    Customer expansion =
        findOrCreateCustomer(
            "TEST_CUST_041",
            "Expansion Restaurants AG",
            CustomerType.UNTERNEHMEN,
            Industry.RESTAURANT,
            450,
            8);

    if (expansion != null) {
      // CEO - New contact, high priority
      CustomerContact ceo =
          createContact(
              expansion,
              "Alexander",
              "Gross",
              "CEO",
              "Vorstand",
              "alexander.gross@expansion-restaurants.de",
              "+49 69 567890",
              "+49 170 0123456",
              true,
              true,
              "OPPORTUNITY: Plant 20 neue Standorte in 2025! Sehr interessiert an Partnerschaft. "
                  + "Erstkontakt war sehr positiv. HOHE PRIORITÄT!",
              null // New contact, no history yet
              );
      ceo.setNotes(ceo.getNotes() + " [OPPORTUNITY: HIGH] [POTENTIAL: 20 LOCATIONS]");
      contactRepository.persist(ceo);

      // Expansion Manager - Main contact
      CustomerContact expansionMgr =
          createContact(
              expansion,
              "Nina",
              "Klein",
              "Expansion Manager",
              "Business Development",
              "nina.klein@expansion-restaurants.de",
              "+49 69 567891",
              "+49 171 1234567",
              false,
              true,
              "Hauptkontakt für Expansion. Sehr professionell, antwortet schnell. "
                  + "Benötigt detaillierte Standort-Analysen.",
              LocalDateTime.now().minusDays(7) // WARM - building relationship
              );

      LOG.info("Created 2 contacts for high-opportunity customer Expansion Restaurants");
    }
  }

  // Helper methods

  private Customer findOrCreateCustomer(
      String testId,
      String name,
      CustomerType type,
      Industry industry,
      int employees,
      int locations) {
    Customer customer = customerRepository.find("companyName", name).firstResult();

    if (customer == null) {
      customer = new Customer();
      customer.setCustomerNumber(testId);
      customer.setCompanyName(name);
      customer.setCustomerType(type);
      customer.setIndustry(industry);
      customer.setStatus(CustomerStatus.AKTIV);
      customer.setCreatedBy(TEST_USER);
      customer.setUpdatedBy(TEST_USER);

      // Mark as test data - note: Customer entity doesn't have notes field
      // Test data marking will be done via customer number prefix

      // Set additional fields for realistic data
      if (locations > 1) {
        customer.setLocationsGermany(locations);
        customer.setTotalLocationsEU(locations);
      }

      customerRepository.persist(customer);
      LOG.debug("Created test customer: " + name);
    }

    return customer;
  }

  private CustomerContact createContact(
      Customer customer,
      String firstName,
      String lastName,
      String position,
      String department,
      String email,
      String phone,
      String mobile,
      boolean isPrimary,
      boolean isDecisionMaker,
      String notes,
      LocalDateTime lastContact) {
    CustomerContact contact = new CustomerContact();
    contact.setCustomer(customer);
    contact.setFirstName(firstName);
    contact.setLastName(lastName);
    contact.setPosition(position);
    contact.setDepartment(department);
    contact.setEmail(email);
    contact.setPhone(phone);
    contact.setMobile(mobile);
    contact.setIsPrimary(isPrimary);
    contact.setIsDecisionMaker(isDecisionMaker);
    contact.setIsActive(true);
    contact.setNotes(notes);
    contact.setLastContactDate(lastContact);
    contact.setCreatedBy(TEST_USER);
    contact.setUpdatedBy(TEST_USER);
    contact.setPreferredCommunicationMethod(CommunicationMethod.EMAIL);
    contact.setLanguagePreference("DE");

    contactRepository.persist(contact);
    return contact;
  }

  private String generateSmallBusinessName(int index) {
    String[] prefixes = {"Restaurant", "Bistro", "Café", "Gasthof", "Pizzeria"};
    String[] names = {"Goldener Löwe", "Alte Post", "Zur Linde", "Adler", "Krone"};
    return prefixes[index % prefixes.length] + " " + names[index % names.length];
  }

  private String getRandomFirstName() {
    String[] names = {
      "Andreas",
      "Barbara",
      "Christian",
      "Diana",
      "Erik",
      "Franziska",
      "Georg",
      "Helena",
      "Ingo",
      "Julia",
      "Karl",
      "Laura",
      "Marco",
      "Nicole"
    };
    return names[random.nextInt(names.length)];
  }

  private String getRandomLastName() {
    String[] names = {
      "Müller",
      "Schmidt",
      "Schneider",
      "Fischer",
      "Weber",
      "Meyer",
      "Wagner",
      "Becker",
      "Schulz",
      "Hoffmann",
      "Schäfer",
      "Koch"
    };
    return names[random.nextInt(names.length)];
  }

  private String generatePhoneNumber() {
    return String.format("%d %d", 30 + random.nextInt(60), 1000000 + random.nextInt(8999999));
  }

  private String generateMobileNumber() {
    return String.format("1%d %d", 50 + random.nextInt(30), 1000000 + random.nextInt(8999999));
  }

  private String generateContactNotes(int index) {
    String[] patterns = {
      "Langjähriger Kunde, sehr zuverlässig. Bestellt regelmäßig.",
      "Neukunde seit 3 Monaten. Noch in Aufbauphase.",
      "Saisonales Geschäft, Hauptsaison Sommer.",
      "Familienbetrieb in 2. Generation. Traditionsbewusst.",
      "Expandiert gerade, plant neue Filiale.",
      "Preissensitiv, vergleicht viel. Qualität trotzdem wichtig."
    };
    return patterns[index % patterns.length];
  }

  private LocalDateTime generateLastContactDate(int index) {
    // Distribute contacts across warmth levels
    int warmthCategory = index % 4;
    switch (warmthCategory) {
      case 0: // HOT
        return LocalDateTime.now().minusDays(random.nextInt(7));
      case 1: // WARM
        return LocalDateTime.now().minusDays(7 + random.nextInt(23));
      case 2: // COOLING
        return LocalDateTime.now().minusDays(30 + random.nextInt(30));
      default: // COLD
        return LocalDateTime.now().minusDays(60 + random.nextInt(30));
    }
  }
}

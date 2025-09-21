# 📊 TEST DATA SPECIFICATION - FC-005 Contact Management

**Status:** 📋 SPEZIFIKATION  
**Erstellt:** 09.08.2025  
**Autor:** Claude  
**Zweck:** Realistische Testdaten für alle Contact Management Szenarien

## 🎯 Zielsetzung

Diese Spezifikation definiert realistische Testdaten für das FreshPlan Sales Tool, die:
- Alle Features des Contact Management Systems testen
- Realistische Geschäftsszenarien abbilden
- Edge Cases und Grenzfälle abdecken
- Als "Testdaten" markiert sind für einfache Entfernung
- Deutsche Geschäftskontexte authentisch darstellen

## 🏢 Testkunden-Kategorien

### 1. Premium Enterprise Kunden (5 Kunden)

#### TEST_CUST_001: "Müller Gastro Gruppe GmbH" 
**Markierung:** `isTestData: true, testDataPrefix: "TEST_CUST_"`
- **Branche:** Gastronomie / Systemgastronomie
- **Mitarbeiter:** 2500+
- **Standorte:** 45 Filialen deutschlandweit
- **Jahresumsatz:** 120 Mio EUR
- **Status:** AKTIV, Premium Partner
- **Besonderheiten:**
  - Multi-Location Management
  - Hierarchische Kontaktstruktur
  - Komplexe Entscheidungswege

**Kontakte (8):**
1. **Dr. Thomas Müller** (CEO) - Hauptentscheider
   - Warmth: HOT (90%) - regelmäßiger Kontakt
   - Letzte Interaktion: vor 3 Tagen
   - Geburtstag: 15.03.1975
   - Notizen: "Golf-Enthusiast, Familie mit 2 Kindern"

2. **Sandra Bergmann** (CFO) - Mitentscheiderin
   - Warmth: WARM (70%) 
   - Letzte Interaktion: vor 2 Wochen
   - Spezialisierung: Budget-Entscheidungen

3. **Michael Weber** (Einkaufsleiter) - Operative Ebene
   - Warmth: HOT (85%)
   - Primärer Kontakt für Bestellungen
   - WhatsApp-Präferenz

4. **Lisa Schmidt** (Regional Manager Nord) - Standort-Verantwortliche
   - Warmth: COOLING (45%)
   - Zuständig: 15 Filialen
   - Anmerkung: "Benötigt mehr Aufmerksamkeit"

5. **Frank Zimmermann** (Regional Manager Süd) - Standort-Verantwortlicher
   - Warmth: WARM (65%)
   - Zuständig: 20 Filialen

6. **Julia Hoffmann** (Regional Manager West) - Standort-Verantwortliche
   - Warmth: COLD (25%)
   - Zuständig: 10 Filialen
   - Alert: "Lange kein Kontakt!"

7. **Peter Klein** (IT-Leiter) - Technischer Ansprechpartner
   - Warmth: WARM (60%)
   - Wichtig für Integrationen

8. **Anna Bauer** (Assistenz Geschäftsführung) - Gatekeeper
   - Warmth: HOT (95%)
   - Kontrolliert Termine mit CEO

#### TEST_CUST_002: "BioFresh Märkte AG"
**Markierung:** `isTestData: true`
- **Branche:** Bio-Lebensmittelhandel
- **Mitarbeiter:** 850
- **Standorte:** 28 Märkte (Süddeutschland)
- **Jahresumsatz:** 45 Mio EUR
- **Status:** AKTIV, Wachsend

**Kontakte (5):**
1. **Markus Grün** (Vorstand) - Entscheider
   - Warmth: WARM (75%)
   - Nachhaltigkeits-Fokus
   - Geburtstag kommt in 5 Tagen

2. **Claudia Weiss** (Einkauf Bio-Produkte) - Mitentscheiderin
   - Warmth: HOT (88%)
   - Sehr detailorientiert

3. **Stefan Rot** (Filialkoordinator) - Operativ
   - Warmth: WARM (70%)
   - Mobile-First Nutzer

### 2. Mittelstand Kunden (10 Kunden)

#### TEST_CUST_011: "Hotel Zur Post GmbH & Co. KG"
**Markierung:** `isTestData: true`
- **Branche:** Hotellerie
- **Mitarbeiter:** 120
- **Standorte:** 3 Hotels (Bayern)
- **Jahresumsatz:** 8 Mio EUR
- **Status:** AKTIV

**Kontakte (3):**
1. **Familie Huber** (Inhaber) - Entscheider
   - Robert Huber (Senior) - WARM (60%)
   - Martin Huber (Junior) - HOT (80%)
   - Generationswechsel im Gange

2. **Maria Schneider** (Hoteldirektorin) - Operative Leitung
   - Warmth: HOT (85%)
   - Beste Ansprechpartnerin

#### TEST_CUST_012: "Bäckerei Goldkorn GmbH"
**Markierung:** `isTestData: true`
- **Branche:** Bäckerei/Konditorei
- **Mitarbeiter:** 85
- **Standorte:** 12 Filialen (Regional)
- **Jahresumsatz:** 5 Mio EUR
- **Status:** AKTIV, Traditionsbetrieb

**Kontakte (2):**
1. **Hans Meier** (Geschäftsführer) - Alleinentscheider
   - Warmth: COOLING (40%)
   - Schwer erreichbar
   - Bevorzugt persönliche Treffen

2. **Sabine Meier** (Verwaltung) - Mitentscheiderin
   - Warmth: WARM (72%)
   - Ehefrau, faktische CFO

### 3. Kleine Kunden (20 Kunden)

#### TEST_CUST_021: "Café Einstein"
**Markierung:** `isTestData: true`
- **Branche:** Café/Bar
- **Mitarbeiter:** 8
- **Standorte:** 1 (Innenstadtlage)
- **Jahresumsatz:** 450k EUR
- **Status:** AKTIV

**Kontakte (1):**
1. **Tom Fischer** (Inhaber)
   - Warmth: HOT (92%)
   - Sehr loyal
   - WhatsApp-only Kommunikation

### 4. Problem-Kunden (5 Kunden) - Edge Cases

#### TEST_CUST_031: "Schwierig & Partner GbR"
**Markierung:** `isTestData: true, testScenario: "PROBLEM_CUSTOMER"`
- **Branche:** Event-Catering
- **Status:** AT_RISK
- **Problem:** Zahlungsverzug

**Kontakte (2):**
1. **Klaus Schwierig** (Geschäftsführer)
   - Warmth: COLD (15%)
   - Reagiert nicht auf Kontaktversuche
   - Letzte Interaktion: vor 3 Monaten

2. **Petra Partner** (Mitinhaberin)
   - Warmth: COOLING (35%)
   - Versucht zu vermitteln

#### TEST_CUST_032: "Insolvenz GmbH"
**Markierung:** `isTestData: true, testScenario: "INACTIVE"`
- **Status:** INAKTIV
- **Problem:** Insolvenz angemeldet
- **Kontakte:** Alle auf COLD (0%)

### 5. Opportunity-Kunden (8 Kunden)

#### TEST_CUST_041: "Expansion Restaurants AG"
**Markierung:** `isTestData: true, testScenario: "HIGH_OPPORTUNITY"`
- **Branche:** Restaurantkette
- **Status:** PROSPECT
- **Opportunity:** Plant 20 neue Standorte

**Kontakte (4):**
1. **Alexander Gross** (CEO)
   - Warmth: Noch kein Score (Neukontakt)
   - Hohe Priorität

2. **Nina Klein** (Expansion Manager)
   - Warmth: WARM (68%)
   - Hauptkontakt für Expansion

## 🌡️ Warmth-Szenarien Testdaten

### HOT Contacts (25% der Kontakte)
- Letzte Interaktion: < 7 Tage
- Interaktions-Frequenz: > 3x pro Monat
- Response-Rate: > 80%
- Sentiment: Überwiegend positiv

### WARM Contacts (35% der Kontakte)
- Letzte Interaktion: 7-30 Tage
- Interaktions-Frequenz: 1-3x pro Monat
- Response-Rate: 50-80%
- Sentiment: Neutral bis positiv

### COOLING Contacts (25% der Kontakte)
- Letzte Interaktion: 30-60 Tage
- Interaktions-Frequenz: < 1x pro Monat
- Response-Rate: 20-50%
- Alert: "Attention needed!"

### COLD Contacts (15% der Kontakte)
- Letzte Interaktion: > 60 Tage
- Interaktions-Frequenz: Keine
- Response-Rate: < 20%
- Alert: "Risk of losing contact!"

## 📅 Zeitbasierte Szenarien

### Geburtstage
- 3 Kontakte: Geburtstag heute
- 5 Kontakte: Geburtstag diese Woche
- 10 Kontakte: Geburtstag diesen Monat
- Rest: Gleichmäßig über Jahr verteilt

### Termine/Follow-ups
- 5 Kontakte: Überfällige Follow-ups (rot)
- 8 Kontakte: Follow-ups diese Woche (gelb)
- 12 Kontakte: Follow-ups nächste Woche (grün)

### Jahrestage
- 3 Kunden: Vertrags-Jubiläum diesen Monat
- 2 Kunden: 10-jähriges Jubiläum dieses Jahr

## 🎭 Rollen-Verteilung

### Entscheidungsebenen
- **Entscheider** (20%): CEO, Inhaber, Geschäftsführer
- **Mitentscheider** (25%): CFO, Einkaufsleiter, Prokuristen
- **Einflussnehmer** (30%): Abteilungsleiter, Berater
- **Nutzer** (20%): Operative Mitarbeiter
- **Gatekeeper** (5%): Assistenz, Sekretariat

### Kommunikationspräferenzen
- **Email-First** (40%)
- **Telefon-Präferenz** (25%)
- **WhatsApp/SMS** (20%)
- **Persönliche Treffen** (10%)
- **Video-Calls** (5%)

## 🏷️ Test-Daten Markierung

### Pflichtfelder für alle Testdaten:
```json
{
  "isTestData": true,
  "testDataPrefix": "TEST_CUST_",
  "testDataCreated": "2025-08-09",
  "testDataVersion": "1.0",
  "testScenario": "STANDARD|PROBLEM|OPPORTUNITY|EDGE_CASE"
}
```

### Lösch-Strategie:
```sql
-- Alle Testdaten löschen
DELETE FROM customers WHERE is_test_data = true;
DELETE FROM customer_contacts WHERE customer_id IN (
  SELECT id FROM customers WHERE is_test_data = true
);
```

## 📊 Metriken-Verteilung

### Kunden-Größen
- Enterprise (>1000 MA): 5 Kunden (8%)
- Mittelstand (50-1000 MA): 15 Kunden (25%)
- Kleinunternehmen (10-50 MA): 25 Kunden (42%)
- Kleinstunternehmen (<10 MA): 15 Kunden (25%)

### Branchen-Mix
- Gastronomie/Hotels: 40%
- Einzelhandel: 25%
- Lebensmittelproduktion: 15%
- Catering/Events: 10%
- Sonstige: 10%

### Geografische Verteilung
- Süddeutschland: 40%
- Norddeutschland: 20%
- Westdeutschland: 20%
- Ostdeutschland: 10%
- Österreich/Schweiz: 10%

### Interaktions-Historie
- Jeder HOT Contact: 50+ Interaktionen
- Jeder WARM Contact: 20-50 Interaktionen
- Jeder COOLING Contact: 5-20 Interaktionen
- Jeder COLD Contact: 0-5 Interaktionen

## 🚀 Implementierungs-Hinweise

### DataInitializer Erweiterung
```java
@Component
public class TestDataInitializer {
    
    private static final String TEST_PREFIX = "TEST_CUST_";
    private static final boolean IS_TEST_DATA = true;
    
    @PostConstruct
    public void initializeTestData() {
        if (shouldCreateTestData()) {
            createPremiumCustomers();
            createMittelstandCustomers();
            createSmallCustomers();
            createProblemCustomers();
            createOpportunityCustomers();
            createInteractionHistory();
        }
    }
    
    private void markAsTestData(Customer customer) {
        customer.setIsTestData(true);
        customer.setTestDataPrefix(TEST_PREFIX);
        customer.setTestDataCreated(LocalDateTime.now());
        customer.setTestDataVersion("1.0");
    }
}
```

### Realistische Namen (Deutsch)
- Nutze häufige deutsche Nachnamen: Müller, Schmidt, Weber, Meyer, Wagner
- Realistische Firmennamen mit Rechtsformen: GmbH, AG, KG, GbR, e.K.
- Echte deutsche Städte und PLZ
- Branchen-typische Bezeichnungen

### Keine Platzhalter
- ❌ NICHT: "Test GmbH", "XXX AG", "Kunde 123"
- ✅ SONDERN: "Müller Gastro GmbH", "BioFresh Märkte AG", "Hotel Zur Post"

## ✅ Validierungs-Checkliste

- [ ] 60 Kunden mit realistischen deutschen Namen
- [ ] 200+ Kontakte mit vollständigen Profilen
- [ ] Alle Warmth-Stufen abgedeckt
- [ ] Geburtstagsverteilung realistisch
- [ ] Multi-Location Szenarien vorhanden
- [ ] Hierarchische Strukturen implementiert
- [ ] Problem-Kunden für Edge-Cases
- [ ] Opportunity-Pipeline gefüllt
- [ ] Alle Kommunikationskanäle genutzt
- [ ] DSGVO-konforme Testdaten
- [ ] Lösch-Mechanismus implementiert
- [ ] Performance bei 1000+ Interaktionen getestet

## 📝 Notizen

Diese Testdaten ermöglichen:
1. **Funktionstest:** Alle Features können getestet werden
2. **Performance-Test:** Realistische Datenmengen
3. **UX-Test:** Authentische Szenarien für Nutzer
4. **Demo-Fähigkeit:** Vorzeigbare Daten für Präsentationen
5. **Trainings-Umgebung:** Schulung neuer Mitarbeiter

**WICHTIG:** Alle Testdaten sind als solche markiert und können jederzeit vollständig entfernt werden ohne Produktivdaten zu beeinflussen.
# 🎯 Test-Daten Qualitätskonzept

**Version:** 1.0
**Datum:** 2025-10-24
**Sprint:** 2.1.7.2 (Xentral Integration + Unified Communication)

---

## 📋 INHALT

### Übersicht
1. [🚨 Problemstellung](#-problemstellung) - Was lief falsch?
2. [🎯 Ziele](#-ziele) - Was wollen wir erreichen?

### Datenmodell
3. [🏗️ Datenmodell-Anforderungen](#-datenmodell-anforderungen) - Pflichtfelder pro Entity
   - [User](#-user)
   - [Lead](#-lead)
   - [Customer](#-customer)
   - [CustomerAddress](#-customeraddress)
   - [CustomerLocation](#-customerlocation)
   - [CustomerContact](#-customercontact-alias-contact)
   - [Opportunity](#-opportunity)
   - [Activity](#-activity-unified-communication)

### Test-Szenarien
4. [🎲 Szenario-Matrix](#-szenario-matrix) - Minimum Non-Overlapping Scenarios
   - [User-Szenarien (U1-U3)](#user-szenarien-minimum-3)
   - [Lead-Szenarien (L1-L5)](#lead-szenarien-minimum-5)
   - [Customer-Szenarien (C1-C8)](#customer-szenarien-minimum-8)

### Qualitätskontrolle
5. [🔍 Qualitätskontroll-Levels](#-qualitätskontroll-levels)
   - [Level 1: Existence Check](#level-1-existence-check--bereits-implementiert)
   - [Level 2: Completeness Check](#level-2-completeness-check--neu)
   - [Level 3: Consistency Check](#level-3-consistency-check--neu)
   - [Level 4: Scenario Coverage Check](#level-4-scenario-coverage-check--neu)

### Wartung & Implementierung
6. [🔄 Erweiterungsstrategie](#-erweiterungsstrategie) - Neue Features hinzufügen
7. [📅 Implementierungsplan](#-implementierungsplan) - Phase 1-4

---

**Quick Links:**
- [Zusammenfassung](#-zusammenfassung)
- [Nächste Schritte](#-nächste-schritte)

---

## 🚨 PROBLEMSTELLUNG

### Was lief falsch?

**Bisheriger Ansatz (FEHLERHAFT):**
- ✅ Coverage-Script zeigt 100% (17/17 Entities)
- ❌ **ABER:** Nur Entity-Namen dokumentiert, keine echten Daten
- ❌ Kunden haben keine vollständigen Adressen
- ❌ Kunden haben keine CustomerLocations
- ❌ Kunden haben keine CustomerContacts
- ❌ Keine Opportunities vorhanden
- ❌ Inkonsistenzen (z.B. ACTIVE Customer ohne Activities)

### User-Feedback (Kritisch):

> "Das macht doch so gar keinen Sinn. Da ist dein ganzes neues System ein einziger Beschiss."

> "Wir wollen doch keinen Türsteher, der schläft und keine Ahnung davon hat was er kontrollieren soll."

### Root Cause:

**Technische Metrik** (100% Coverage) **≠** **Datenqualität**

Das Coverage-Script prüfte nur ob Entity-Namen im Code vorkommen, nicht ob tatsächlich vollständige, realistische Daten existieren.

---

## 🎯 ZIELE

### 1. Realistische Datengrundlage

**Prinzip:** Jeder Datensatz muss so vollständig sein, wie ein echter Verkäufer ihn anlegen würde.

- ✅ Vollständige Pflichtfelder (Name, Adresse, Kontakte, etc.)
- ✅ Logisch konsistente Daten (ACTIVE → hat Activities)
- ✅ Realistische Szenarien (Lead→Customer Conversion, Opportunities in verschiedenen Phasen)

### 2. Nicht-überlappende Szenarien

**Prinzip:** Nicht die Masse zählt, sondern klare, trennbare Test-Fälle.

- ✅ Jedes Szenario ist eindeutig identifizierbar
- ✅ Szenarien überlappen sich nicht (z.B. ACTIVE ≠ ARCHIVED)
- ✅ Minimale Anzahl von Datensätzen für vollständige Coverage

### 3. Effektive Qualitätskontrollen

**Prinzip:** 4 Levels statt oberflächlicher Name-Checks.

- ✅ **Level 1:** Existence (Gibt es Daten?)
- ✅ **Level 2:** Completeness (Sind alle Pflichtfelder gefüllt?)
- ✅ **Level 3:** Consistency (Sind Daten logisch konsistent?)
- ✅ **Level 4:** Scenario-Coverage (Sind alle Szenarien abgedeckt?)

### 4. Nachhaltige Erweiterbarkeit

**Prinzip:** Bei jedem neuen Feature müssen Test-Daten systematisch erweitert werden.

---

## 🔄 TEST-DATEN-STRATEGIE

### Übersicht: 2 getrennte Daten-Typen

**WICHTIG:** Wir verwenden **zwei verschiedene Arten von Test-Daten** mit unterschiedlichen Zwecken und Lifecycles!

| Typ | Zweck | Lifecycle | Verwaltung | Profile |
|-----|-------|-----------|------------|---------|
| **SEED-Daten** | Manuelle UI-Tests & Entwicklung | **Persistent** (bleiben in DB) | Flyway Migrations (`db/dev-seed/`) | `%dev` only |
| **Test-Daten** | Automatisierte Unit/Integration-Tests | **Transient** (erstellen/löschen pro Test) | Java-Code (`@BeforeEach`/`@AfterEach`) | `%test` only |

### 1. SEED-Daten (Persistent)

**Zweck:** Realistische Daten für manuelle Frontend-Tests während der Entwicklung

**Eigenschaften:**
- ✅ Bleiben **dauerhaft** in der Datenbank
- ✅ Werden via **Flyway DEV-SEED Migrations** erstellt (Sequenz V90001+)
- ✅ Vollständig & realistisch (wie echte Produktionsdaten)
- ✅ Nur in `%dev` Profile geladen
- ✅ **NICHT** in automatisierten Tests verwendet

**Beispiele:**
- `Customer 90101` - "[TEST] Großhandel Frisch AG - Super Customer"
- `Lead L3` - "L3 Großhandel - Super Customer"
- `User partner-a` - Test-Vertriebspartner

**Verwaltung:**
```sql
-- Erstellt via: db/dev-seed/V90011__fix_customer_90101_business_type_grosshandel.sql
UPDATE customers
SET business_type = 'GROSSHANDEL'
WHERE customer_number = '90101';
```

**Lifecycle:**
```
Dev Start → Flyway lädt SEED → Daten bleiben in DB → Dev nutzt Frontend → Daten bleiben
                                                    ↑
                                            Manueller Test
```

### 2. Test-Daten (Transient)

**Zweck:** Isolierte Daten für automatisierte Unit- und Integrationstests

**Eigenschaften:**
- ✅ Werden **pro Test** erstellt und gelöscht
- ✅ Verwaltet via **Java-Code** (`TestDataService`, `@BeforeEach`)
- ✅ Minimalistisch (nur was der Test braucht)
- ✅ Nur in `%test` Profile aktiv
- ✅ **NICHT** in Entwicklungs-Datenbank

**Beispiele:**
```java
@BeforeEach
void setUp() {
    // Test erstellt eigene Customer
    customer = createTestCustomer("Test GmbH", CustomerStatus.AKTIV);
}

@AfterEach
void tearDown() {
    // Test löscht eigene Daten
    Customer.deleteById(customer.id);
}
```

**Lifecycle:**
```
Test Start → Setup erstellt Daten → Test läuft → Teardown löscht Daten → Test Ende
            ↑                                     ↑
        @BeforeEach                          @AfterEach
```

### Wann welche Daten?

| Szenario | Datentyp | Begründung |
|----------|----------|------------|
| Entwickler öffnet Frontend, will Customer 90101 anschauen | **SEED** | Persistent, immer verfügbar |
| Entwickler testet neue UI-Komponente manuell | **SEED** | Braucht realistische, vollständige Daten |
| CI führt CustomerServiceTest aus | **Test** | Isoliert, unabhängig, sauber |
| Integration-Test prüft Lead→Customer Conversion | **Test** | Kontrollierte Testbedingungen |

### Wichtige Regeln

**DO:**
- ✅ SEED-Daten via Flyway DEV-SEED Migrations (`V90001+`)
- ✅ Test-Daten via Java-Code selbst verwalten
- ✅ SEED-Daten vollständig & realistisch halten
- ✅ Test-Daten minimalistisch halten (nur Testbedarf)

**DON'T:**
- ❌ SEED-Daten in automatisierten Tests verwenden
- ❌ Test-Daten als SEED-Daten speichern
- ❌ SEED-Daten in Teardown löschen (`cleanTestData()` löscht KEINE SEED-Daten!)
- ❌ Hardcoded Migration-Nummern (immer `./scripts/get-next-migration.sh` nutzen)

- ✅ Klare Regeln, welche Entities bei welchem Feature erweitert werden
- ✅ Pre-Commit Hook erzwingt Dokumentation
- ✅ CI-Pipeline validiert Datenqualität

---

## 🏗️ DATENMODELL-ANFORDERUNGEN

### Obligatorische Felder pro Entity-Typ

Jede Entity muss **mindestens** folgende Felder haben:

#### 👤 User

| Feld | Pflicht | Beispiel | Validierung |
|------|---------|----------|-------------|
| `firstName` | ✅ | "Max" | NOT NULL, LENGTH > 0 |
| `lastName` | ✅ | "Mustermann" | NOT NULL, LENGTH > 0 |
| `email` | ✅ | "max.mustermann@freshplan.de" | NOT NULL, UNIQUE, VALID EMAIL |
| `role` | ✅ | "SALES_REP" / "SALES_MANAGER" | NOT NULL, ENUM |
| `status` | ✅ | "ACTIVE" / "INACTIVE" | NOT NULL, ENUM |
| `profile` | ⚠️ Optional | Profile{…} | FK, kann NULL sein |

**Konsistenz-Regeln:**
- ACTIVE User → muss mindestens 1 Activity haben (Lead oder Customer)
- SALES_MANAGER → kann assignedUsers haben
- SALES_REP → muss assignedTo (Manager) haben

---

#### 🎯 Lead

| Feld | Pflicht | Beispiel | Validierung |
|------|---------|----------|-------------|
| `companyName` | ✅ | "Bäckerei Schmidt GmbH" | NOT NULL, LENGTH > 0 |
| `status` | ✅ | "NEW" / "QUALIFIED" / "CONVERTED" / "LOST" | NOT NULL, ENUM |
| `source` | ✅ | "WEBSITE" / "REFERRAL" / "TRADE_SHOW" | NOT NULL, ENUM |
| `assignedTo` | ✅ | User{id} | FK NOT NULL |
| `createdDate` | ✅ | "2024-10-15T10:30:00Z" | NOT NULL, TIMESTAMP |
| `leadContacts` | ✅ | [LeadContact{…}, …] | SIZE >= 1 |
| `activities` | ⚠️ Optional | [Activity{…}, …] | Für QUALIFIED/CONVERTED empfohlen |

**Konsistenz-Regeln:**
- CONVERTED Lead → muss `convertedToCustomerId` haben
- CONVERTED Lead → muss mindestens 1 Activity haben
- NEW Lead → kann ohne Activities sein (frisch angelegt)
- QUALIFIED Lead → sollte mindestens 2 Activities haben (Kontaktaufnahme + Follow-Up)

---

#### 🏢 Customer

| Feld | Pflicht | Beispiel | Validierung |
|------|---------|----------|-------------|
| `companyName` | ✅ | "Bäckerei Schmidt GmbH" | NOT NULL, LENGTH > 0 |
| `customerNumber` | ✅ | "K-90001" | NOT NULL, UNIQUE |
| `status` | ✅ | "ACTIVE" / "INACTIVE" / "ARCHIVED" | NOT NULL, ENUM |
| `assignedTo` | ✅ | User{id} | FK NOT NULL |
| `createdDate` | ✅ | "2024-06-01T08:00:00Z" | NOT NULL, TIMESTAMP |
| `originalLeadId` | ⚠️ Optional | Lead{id} | FK, NULL wenn direkt angelegt |
| `customerAddresses` | ✅ | [CustomerAddress{…}, …] | SIZE >= 1 (mindestens Hauptadresse) |
| `customerLocations` | ✅ | [CustomerLocation{…}, …] | SIZE >= 1 (mindestens Hauptstandort) |
| `contacts` | ✅ | [CustomerContact{…}, …] | SIZE >= 1 (mindestens 1 Ansprechpartner) |
| `activities` | ⚠️ Optional | [Activity{…}, …] | Für ACTIVE empfohlen |
| `xentralIntegration` | ⚠️ Optional | XentralIntegration{…} | Nur wenn Xentral aktiviert |

**Konsistenz-Regeln:**
- ACTIVE Customer → muss mindestens 1 Activity in letzten 90 Tagen haben
- INACTIVE Customer → keine Activities in letzten 180 Tagen
- ARCHIVED Customer → Status-Change-Datum muss existieren
- Wenn `originalLeadId` gesetzt → Lead muss CONVERTED sein
- Wenn Xentral aktiviert → `xentralCustomerId` muss gesetzt sein

---

#### 📍 CustomerAddress

| Feld | Pflicht | Beispiel | Validierung |
|------|---------|----------|-------------|
| `customerId` | ✅ | Customer{id} | FK NOT NULL |
| `addressType` | ✅ | "BILLING" / "DELIVERY" / "HEADQUARTERS" | NOT NULL, ENUM |
| `street` | ✅ | "Hauptstraße 42" | NOT NULL, LENGTH > 0 |
| `zipCode` | ✅ | "12345" | NOT NULL, PATTERN: ^\d{5}$ |
| `city` | ✅ | "Berlin" | NOT NULL, LENGTH > 0 |
| `country` | ✅ | "Deutschland" / "DE" | NOT NULL, LENGTH > 0 |
| `isPrimary` | ✅ | true / false | NOT NULL, BOOLEAN |

**Konsistenz-Regeln:**
- Jeder Customer muss **genau 1** `isPrimary=true` Adresse haben
- BILLING Adresse ist Pflicht
- DELIVERY Adresse kann identisch mit BILLING sein

---

#### 📦 CustomerLocation

| Feld | Pflicht | Beispiel | Validierung |
|------|---------|----------|-------------|
| `customerId` | ✅ | Customer{id} | FK NOT NULL |
| `locationType` | ✅ | "HEADQUARTERS" / "BRANCH" / "WAREHOUSE" | NOT NULL, ENUM |
| `name` | ✅ | "Hauptfiliale Berlin" | NOT NULL, LENGTH > 0 |
| `street` | ✅ | "Hauptstraße 42" | NOT NULL, LENGTH > 0 |
| `zipCode` | ✅ | "12345" | NOT NULL, PATTERN: ^\d{5}$ |
| `city` | ✅ | "Berlin" | NOT NULL, LENGTH > 0 |
| `country` | ✅ | "Deutschland" / "DE" | NOT NULL, LENGTH > 0 |
| `isPrimary` | ✅ | true / false | NOT NULL, BOOLEAN |

**Konsistenz-Regeln:**
- Jeder Customer muss **genau 1** `isPrimary=true` Location haben
- Primary Location sollte mit Primary Address übereinstimmen (gleiche Stadt/PLZ)

---

#### 👥 CustomerContact (alias: Contact)

| Feld | Pflicht | Beispiel | Validierung |
|------|---------|----------|-------------|
| `customerId` | ✅ | Customer{id} | FK NOT NULL |
| `firstName` | ✅ | "Anna" | NOT NULL, LENGTH > 0 |
| `lastName` | ✅ | "Schmidt" | NOT NULL, LENGTH > 0 |
| `email` | ✅ | "anna.schmidt@kunde.de" | NOT NULL, VALID EMAIL |
| `phone` | ⚠️ Optional | "+49 30 12345678" | PATTERN: ^\+?\d+ |
| `position` | ⚠️ Optional | "Geschäftsführerin" | LENGTH <= 100 |
| `isPrimary` | ✅ | true / false | NOT NULL, BOOLEAN |

**Konsistenz-Regeln:**
- Jeder Customer muss **genau 1** `isPrimary=true` Contact haben
- Primary Contact sollte Email + Phone haben (optional, aber empfohlen)

---

#### 💼 Opportunity

| Feld | Pflicht | Beispiel | Validierung |
|------|---------|----------|-------------|
| `customerId` | ✅ | Customer{id} | FK NOT NULL |
| `title` | ✅ | "Q3 Großbestellung Backwaren" | NOT NULL, LENGTH > 0 |
| `stage` | ✅ | "QUALIFICATION" / "PROPOSAL" / "NEGOTIATION" / "WON" / "LOST" | NOT NULL, ENUM |
| `probability` | ✅ | 50 | NOT NULL, RANGE: 0-100 |
| `estimatedValue` | ✅ | 150000.00 | NOT NULL, >= 0 |
| `expectedCloseDate` | ✅ | "2024-12-31" | NOT NULL, DATE |
| `assignedTo` | ✅ | User{id} | FK NOT NULL |
| `createdDate` | ✅ | "2024-08-01T10:00:00Z" | NOT NULL, TIMESTAMP |

**Konsistenz-Regeln:**
- WON Opportunity → `probability = 100`
- LOST Opportunity → `probability = 0`
- NEGOTIATION Opportunity → `probability >= 60`
- Jede Opportunity sollte mindestens 1 Activity haben (Follow-Up)

---

#### 📞 Activity (Unified Communication)

| Feld | Pflicht | Beispiel | Validierung |
|------|---------|----------|-------------|
| `entityType` | ✅ | "LEAD" / "CUSTOMER" | NOT NULL, ENUM |
| `entityId` | ✅ | Lead{id} oder Customer{id} | FK NOT NULL |
| `activityType` | ✅ | "CALL" / "EMAIL" / "MEETING" / "NOTE" | NOT NULL, ENUM |
| `subject` | ✅ | "Erstgespräch Xentral-Integration" | NOT NULL, LENGTH > 0 |
| `description` | ⚠️ Optional | "Kunde interessiert an Xentral…" | TEXT |
| `activityDate` | ✅ | "2024-10-20T14:30:00Z" | NOT NULL, TIMESTAMP |
| `userId` | ✅ | User{id} | FK NOT NULL |
| `originalLeadId` | ⚠️ Optional | Lead{id} | Nur für CUSTOMER-Activities mit Lead-Historie |

**Konsistenz-Regeln:**
- LEAD Activity → `entityType = LEAD`, `entityId` muss Lead sein
- CUSTOMER Activity → `entityType = CUSTOMER`, `entityId` muss Customer sein
- Wenn Customer aus Lead konvertiert → `originalLeadId` sollte gesetzt sein

---

## 🎲 SZENARIO-MATRIX

### Minimum Non-Overlapping Scenarios

**Prinzip:** Nicht die Masse, sondern klare Trennung der Test-Fälle.

#### User-Szenarien (Minimum: 3)

| Szenario-ID | Role | Status | Beschreibung | Assigned Entities |
|-------------|------|--------|--------------|-------------------|
| **U1** | SALES_REP | ACTIVE | Aktiver Verkäufer mit Leads + Customers | 2 Leads, 3 Customers, 5 Activities |
| **U2** | SALES_MANAGER | ACTIVE | Manager mit Team (assigned Users) | 1 Team-Member (U1), Übersicht aller Leads |
| **U3** | SALES_REP | INACTIVE | Inaktiver User (Urlaub/gekündigt) | 1 Customer (Übergabe an U1) |

---

#### Lead-Szenarien (Minimum: 5)

| Szenario-ID | Status | Source | Assigned To | LeadContacts | Activities | Conversion |
|-------------|--------|--------|-------------|--------------|------------|------------|
| **L1** | NEW | WEBSITE | U1 | 1 Contact | 0 | - |
| **L2** | QUALIFIED | REFERRAL | U1 | 1 Contact | 2 (Call, Meeting) | - |
| **L3** | CONVERTED | TRADE_SHOW | U1 | 2 Contacts | 3 (Call, Meeting, Email) | → C1 (Conversion mit Historie) |
| **L4** | LOST | COLD_CALL | U1 | 1 Contact | 1 (Call) | - |
| **L5** | NEW | WEBSITE | U2 (Manager) | 1 Contact | 0 | Nur für Manager-Übersicht |

---

#### Customer-Szenarien (Minimum: 8)

| Szenario-ID | Status | Original Lead | Assigned To | Addresses | Locations | Contacts | Opportunities | Activities | Xentral | Beschreibung |
|-------------|--------|---------------|-------------|-----------|-----------|----------|---------------|------------|---------|--------------|
| **C1** | ACTIVE | L3 (✅) | U1 | 2 (BILLING+DELIVERY) | 2 (HQ+BRANCH) | 2 | 1 (QUALIFICATION) | 5 (Lead-Phase + Customer-Phase) | ✅ | **SUPER-Customer** (konvertiert, alles vorhanden) |
| **C2** | ACTIVE | - | U1 | 1 (BILLING=DELIVERY) | 1 (HQ) | 1 | 1 (PROPOSAL) | 3 (nur Customer-Phase) | ❌ | Direkt angelegter Customer (kein Lead) |
| **C3** | ACTIVE | - | U1 | 1 (BILLING) | 1 (HQ) | 1 | 2 (NEGOTIATION, WON) | 4 | ✅ | Erfolgreich abgeschlossene Opportunities |
| **C4** | ACTIVE | - | U1 | 1 (BILLING) | 1 (HQ) | 2 | 0 | 2 | ❌ | Mehrere Kontakte, keine Opportunities |
| **C5** | INACTIVE | - | U1 | 1 (BILLING) | 1 (HQ) | 1 | 1 (LOST) | 1 (vor 200 Tagen) | ❌ | Inaktiver Customer (lange keine Aktivität) |
| **C6** | ARCHIVED | - | U3 (INACTIVE) | 1 (BILLING) | 1 (HQ) | 1 | 0 | 0 | ❌ | Archivierter Customer (gekündigt) |
| **C7** | ACTIVE | - | U2 (Manager) | 1 (BILLING) | 1 (HQ) | 1 | 0 | 1 | ❌ | Manager-Customer (Übersicht) |
| **C8** | ACTIVE | - | U1 | 2 (BILLING+DELIVERY unterschiedlich) | 3 (HQ+2 BRANCHES) | 3 | 0 | 2 | ✅ | Multi-Location Customer (Filial-Struktur) |

**Total:**
- **3 Users** (2 ACTIVE, 1 INACTIVE)
- **5 Leads** (1 NEW, 1 QUALIFIED, 1 CONVERTED, 1 LOST, 1 Manager-Lead)
- **8 Customers** (6 ACTIVE, 1 INACTIVE, 1 ARCHIVED)
- **≥15 Addresses** (mindestens 1 pro Customer, manche 2)
- **≥18 Locations** (mindestens 1 pro Customer, C8 hat 3)
- **≥13 Contacts** (mindestens 1 pro Customer, manche mehr)
- **≥5 Opportunities** (verschiedene Stages: QUALIFICATION, PROPOSAL, NEGOTIATION, WON, LOST)
- **≥25 Activities** (Lead-Phase + Customer-Phase, Unified Timeline)

---

## 🔍 QUALITÄTSKONTROLL-LEVELS

### Level 1: Existence Check ✅ (bereits implementiert)

**Frage:** Existieren Test-Daten für alle relevanten Entities?

**Implementierung:**
- Python-Script `scripts/check-test-data-coverage.py`
- Prüft ob Entity-Namen im `TestDataService.java` vorkommen
- CI-Pipeline blockiert bei fehlenden Entities

**Status:** ✅ Funktioniert (zeigt 100%)

**Problem:** Prüft nur Namen, nicht Daten-Qualität!

---

### Level 2: Completeness Check ⚠️ (NEU)

**Frage:** Sind alle Pflichtfelder gefüllt?

**Implementierung (geplant):**
- SQL-Queries gegen DEV-Datenbank
- Prüfung von `NOT NULL` Constraints
- Validierung von obligatorischen Beziehungen (z.B. Customer muss mindestens 1 Address haben)

**Beispiel-Queries:**

```sql
-- ❌ Customers ohne Adressen
SELECT c.id, c.company_name
FROM customer c
WHERE NOT EXISTS (
  SELECT 1 FROM customer_address ca WHERE ca.customer_id = c.id
);

-- ❌ Customers ohne Primary-Adresse
SELECT c.id, c.company_name
FROM customer c
WHERE NOT EXISTS (
  SELECT 1 FROM customer_address ca
  WHERE ca.customer_id = c.id AND ca.is_primary = true
);

-- ❌ Customers ohne Kontakte
SELECT c.id, c.company_name
FROM customer c
WHERE NOT EXISTS (
  SELECT 1 FROM customer_contact cc WHERE cc.customer_id = c.id
);

-- ❌ Leads ohne LeadContacts
SELECT l.id, l.company_name
FROM lead l
WHERE NOT EXISTS (
  SELECT 1 FROM lead_contact lc WHERE lc.lead_id = l.id
);

-- ❌ ACTIVE Customers ohne Activities (letzte 90 Tage)
SELECT c.id, c.company_name
FROM customer c
WHERE c.status = 'ACTIVE'
AND NOT EXISTS (
  SELECT 1 FROM activity a
  WHERE a.entity_type = 'CUSTOMER'
  AND a.entity_id = c.id
  AND a.activity_date >= NOW() - INTERVAL '90 days'
);
```

**Exit-Code:**
- `0` = Alle Pflichtfelder gefüllt ✅
- `1` = Mindestens 1 Completeness-Fehler gefunden ❌

---

### Level 3: Consistency Check ⚠️ (NEU)

**Frage:** Sind Daten logisch konsistent?

**Beispiel-Prüfungen:**

```sql
-- ❌ CONVERTED Lead ohne convertedToCustomerId
SELECT l.id, l.company_name
FROM lead l
WHERE l.status = 'CONVERTED'
AND l.converted_to_customer_id IS NULL;

-- ❌ Customer mit originalLeadId, aber Lead ist nicht CONVERTED
SELECT c.id, c.company_name, l.status
FROM customer c
JOIN lead l ON c.original_lead_id = l.id
WHERE l.status != 'CONVERTED';

-- ❌ WON Opportunity mit probability < 100
SELECT o.id, o.title, o.probability
FROM opportunity o
WHERE o.stage = 'WON'
AND o.probability != 100;

-- ❌ LOST Opportunity mit probability > 0
SELECT o.id, o.title, o.probability
FROM opportunity o
WHERE o.stage = 'LOST'
AND o.probability != 0;

-- ❌ INACTIVE Customer mit Activities in letzten 180 Tagen
SELECT c.id, c.company_name, MAX(a.activity_date)
FROM customer c
JOIN activity a ON a.entity_type = 'CUSTOMER' AND a.entity_id = c.id
WHERE c.status = 'INACTIVE'
GROUP BY c.id, c.company_name
HAVING MAX(a.activity_date) >= NOW() - INTERVAL '180 days';

-- ❌ Customer mit Xentral Integration, aber keine xentralCustomerId
SELECT c.id, c.company_name
FROM customer c
JOIN xentral_integration xi ON xi.customer_id = c.id
WHERE xi.xentral_customer_id IS NULL;
```

**Exit-Code:**
- `0` = Alle Daten konsistent ✅
- `1` = Mindestens 1 Consistency-Fehler gefunden ❌

---

### Level 4: Scenario Coverage Check ⚠️ (NEU)

**Frage:** Sind alle definierten Szenarien abgedeckt?

**Implementierung:**
- Prüfung gegen Szenario-Matrix (siehe oben)
- Vergleich IST-Daten vs. SOLL-Szenarien

**Beispiel-Prüfungen:**

```sql
-- ✅ Szenario U1: SALES_REP mit mindestens 2 Leads + 3 Customers
SELECT
  u.id,
  u.email,
  COUNT(DISTINCT l.id) AS lead_count,
  COUNT(DISTINCT c.id) AS customer_count
FROM "user" u
LEFT JOIN lead l ON l.assigned_to = u.id
LEFT JOIN customer c ON c.assigned_to = u.id
WHERE u.role = 'SALES_REP' AND u.status = 'ACTIVE'
GROUP BY u.id, u.email
HAVING COUNT(DISTINCT l.id) >= 2 AND COUNT(DISTINCT c.id) >= 3;

-- ✅ Szenario C1: ACTIVE Customer mit Lead-Historie + Xentral + Opportunities
SELECT c.id, c.company_name
FROM customer c
WHERE c.status = 'ACTIVE'
AND c.original_lead_id IS NOT NULL
AND EXISTS (SELECT 1 FROM xentral_integration xi WHERE xi.customer_id = c.id)
AND EXISTS (SELECT 1 FROM opportunity o WHERE o.customer_id = c.id);

-- ✅ Szenario C5: INACTIVE Customer mit LOST Opportunity
SELECT c.id, c.company_name
FROM customer c
JOIN opportunity o ON o.customer_id = c.id
WHERE c.status = 'INACTIVE'
AND o.stage = 'LOST';

-- ✅ Szenario C8: Multi-Location Customer (≥3 Locations)
SELECT c.id, c.company_name, COUNT(cl.id) AS location_count
FROM customer c
JOIN customer_location cl ON cl.customer_id = c.id
GROUP BY c.id, c.company_name
HAVING COUNT(cl.id) >= 3;
```

**Exit-Code:**
- `0` = Alle Szenarien abgedeckt ✅
- `1` = Mindestens 1 Szenario fehlt ❌

---

## 🔄 ERWEITERUNGSSTRATEGIE

### Regel: Bei jedem neuen Feature

**1. Feature-Analyse**
- Welche Entities sind betroffen?
- Welche neuen Felder kommen hinzu?
- Welche neuen Szenarien müssen getestet werden?

**2. Szenario-Matrix erweitern**
- Neue Szenarien hinzufügen (z.B. "Customer mit Feature X")
- Bestehende Szenarien anpassen (z.B. C1 bekommt neues Feld)

**3. Test-Daten erweitern**
- Mindestens 1 Super-Customer bekommt das Feature
- Mindestens 1 Szenario ohne das Feature (für Negativ-Tests)

**4. Dokumentation aktualisieren**
- `TEST_DATA_QUALITY_CONCEPT.md` (dieses Dokument)
- `TEST_DATA_SCENARIOS.md` (Quick Reference)
- `TEST_DATA_GUIDE.md` (Developer Guide)

**5. Pre-Commit Hook erzwingt Synchronisation**
- Wenn `TestDataService.java` geändert → BEIDE Docs müssen aktualisiert werden
- Commit wird BLOCKIERT wenn Doku fehlt

---

### Beispiel: Neues Feature "Xentral Integration"

**Feature-Analyse:**
- ✅ Betrifft: `Customer`, `XentralSettings`, `XentralIntegration`
- ✅ Neue Felder: `customer.xentralCustomerId`, `customer.xentralSyncEnabled`
- ✅ Neue Entity: `XentralIntegration` (1:1 mit Customer)

**Szenario-Matrix erweitern:**
- ✅ **C1** (Super-Customer) → Xentral aktiviert
- ✅ **C3** → Xentral aktiviert
- ✅ **C8** (Multi-Location) → Xentral aktiviert
- ✅ **C2, C4, C5, C6, C7** → Xentral NICHT aktiviert (Negativ-Tests)

**Test-Daten erweitern:**
```java
// C1, C3, C8 bekommen Xentral Integration
XentralIntegration integration = new XentralIntegration();
integration.setCustomerId(customer.getId());
integration.setXentralCustomerId("XENTRAL-12345");
integration.setSyncEnabled(true);
integration.setLastSyncDate(Instant.now().minus(1, ChronoUnit.DAYS));
xentralIntegrationRepository.persist(integration);
```

**Dokumentation aktualisieren:**
- ✅ `TEST_DATA_QUALITY_CONCEPT.md` → Szenario-Matrix ergänzt
- ✅ `TEST_DATA_SCENARIOS.md` → Xentral-Spalte hinzugefügt
- ✅ `TEST_DATA_GUIDE.md` → Quick Reference aktualisiert

**Pre-Commit Hook prüft:**
```bash
if [ "$TEST_DATA_SERVICE_CHANGED" = true ]; then
  if [ "$SCENARIOS_DOC_CHANGED" = false ] || [ "$GUIDE_DOC_CHANGED" = false ]; then
    echo "❌ Commit BLOCKIERT. Bitte aktualisiere BEIDE Docs."
    exit 1
  fi
fi
```

---

## 📅 IMPLEMENTIERUNGSPLAN

### Phase 1: Dokumentation ✅ (DONE)

**Ziel:** Konzept schreiben, bevor Code angefasst wird.

- [x] Datenmodell-Anforderungen definieren
- [x] Szenario-Matrix erstellen
- [x] Qualitätskontroll-Levels dokumentieren
- [x] Erweiterungsstrategie beschreiben

**Datei:** `/docs/testing/TEST_DATA_QUALITY_CONCEPT.md` (dieses Dokument)

---

### Phase 2: Test-Daten implementieren ⚠️ (TODO)

**Ziel:** Vollständige, realistische Test-Daten für alle Szenarien.

#### 2.1 User-Szenarien (U1-U3)

```java
// U1: SALES_REP (ACTIVE)
User salesRep = createUser("Max", "Mustermann", "max.mustermann@freshplan.de", UserRole.SALES_REP, UserStatus.ACTIVE);

// U2: SALES_MANAGER (ACTIVE)
User manager = createUser("Anna", "Schmidt", "anna.schmidt@freshplan.de", UserRole.SALES_MANAGER, UserStatus.ACTIVE);

// U3: SALES_REP (INACTIVE)
User inactiveRep = createUser("Peter", "Müller", "peter.mueller@freshplan.de", UserRole.SALES_REP, UserStatus.INACTIVE);
```

#### 2.2 Lead-Szenarien (L1-L5)

```java
// L1: NEW Lead (frisch, keine Activities)
Lead l1 = createLead("Bäckerei Meier GmbH", LeadStatus.NEW, LeadSource.WEBSITE, salesRep);
createLeadContact(l1, "Thomas", "Meier", "thomas.meier@baeckerei-meier.de");

// L2: QUALIFIED Lead (2 Activities)
Lead l2 = createLead("Café Schwarz KG", LeadStatus.QUALIFIED, LeadSource.REFERRAL, salesRep);
createLeadContact(l2, "Sandra", "Schwarz", "sandra.schwarz@cafe-schwarz.de");
createActivity(l2, ActivityType.CALL, "Erstgespräch", salesRep, Instant.now().minus(5, ChronoUnit.DAYS));
createActivity(l2, ActivityType.MEETING, "Bedarfsanalyse", salesRep, Instant.now().minus(2, ChronoUnit.DAYS));

// L3: CONVERTED Lead (3 Activities, wird zu C1)
Lead l3 = createLead("Großhandel Frisch AG", LeadStatus.CONVERTED, LeadSource.TRADE_SHOW, salesRep);
createLeadContact(l3, "Michael", "Frisch", "michael.frisch@frisch-ag.de");
createLeadContact(l3, "Julia", "Neumann", "julia.neumann@frisch-ag.de");
createActivity(l3, ActivityType.CALL, "Messefollow-up", salesRep, Instant.now().minus(30, ChronoUnit.DAYS));
createActivity(l3, ActivityType.MEETING, "Vor-Ort-Termin", salesRep, Instant.now().minus(20, ChronoUnit.DAYS));
createActivity(l3, ActivityType.EMAIL, "Angebot versendet", salesRep, Instant.now().minus(15, ChronoUnit.DAYS));

// → L3 wird in Schritt 2.3 zu C1 konvertiert

// L4: LOST Lead (1 Activity)
Lead l4 = createLead("Imbiss Schnell", LeadStatus.LOST, LeadSource.COLD_CALL, salesRep);
createLeadContact(l4, "Klaus", "Schnell", "klaus@imbiss-schnell.de");
createActivity(l4, ActivityType.CALL, "Kein Interesse", salesRep, Instant.now().minus(10, ChronoUnit.DAYS));

// L5: NEW Lead (Manager-Übersicht)
Lead l5 = createLead("Hotel Adler GmbH", LeadStatus.NEW, LeadSource.WEBSITE, manager);
createLeadContact(l5, "Sabine", "Adler", "sabine.adler@hotel-adler.de");
```

#### 2.3 Customer-Szenarien (C1-C8)

**C1: SUPER-Customer (alles vorhanden)**

```java
// Conversion: L3 → C1
Customer c1 = convertLeadToCustomer(l3, "K-90001");
c1.setOriginalLeadId(l3.getId());

// 2 Adressen (BILLING + DELIVERY)
createCustomerAddress(c1, AddressType.BILLING, "Industriestraße 10", "12345", "Berlin", "Deutschland", true);
createCustomerAddress(c1, AddressType.DELIVERY, "Lagerstraße 5", "12347", "Berlin", "Deutschland", false);

// 2 Locations (HQ + BRANCH)
createCustomerLocation(c1, LocationType.HEADQUARTERS, "Hauptsitz Berlin", "Industriestraße 10", "12345", "Berlin", "Deutschland", true);
createCustomerLocation(c1, LocationType.BRANCH, "Filiale Potsdam", "Potsdamer Straße 20", "14467", "Potsdam", "Deutschland", false);

// 2 Kontakte
createCustomerContact(c1, "Michael", "Frisch", "michael.frisch@frisch-ag.de", "+49 30 123456", "Geschäftsführer", true);
createCustomerContact(c1, "Julia", "Neumann", "julia.neumann@frisch-ag.de", "+49 30 123457", "Einkaufsleiterin", false);

// 1 Opportunity (QUALIFICATION)
Opportunity o1 = createOpportunity(c1, "Q4 Großbestellung Backwaren", OpportunityStage.QUALIFICATION, 50, 150000.00, LocalDate.of(2024, 12, 31), salesRep);

// 5 Activities (Lead-Phase + Customer-Phase)
// (Lead-Activities L3 bleiben erhalten)
createActivity(c1, ActivityType.MEETING, "Vertragsunterzeichnung", salesRep, Instant.now().minus(10, ChronoUnit.DAYS), l3.getId());
createActivity(c1, ActivityType.EMAIL, "Willkommensmail Xentral", salesRep, Instant.now().minus(8, ChronoUnit.DAYS), l3.getId());

// Xentral Integration
createXentralIntegration(c1, "XENTRAL-12345", true);
```

**C2: Direkt angelegter Customer (kein Lead)**

```java
Customer c2 = createCustomer("Bio-Markt Grün GmbH", "K-90002", CustomerStatus.ACTIVE, salesRep);

// 1 Adresse (BILLING = DELIVERY)
createCustomerAddress(c2, AddressType.BILLING, "Ökoweg 3", "10115", "Berlin", "Deutschland", true);

// 1 Location (HQ)
createCustomerLocation(c2, LocationType.HEADQUARTERS, "Hauptgeschäft", "Ökoweg 3", "10115", "Berlin", "Deutschland", true);

// 1 Kontakt
createCustomerContact(c2, "Lisa", "Grün", "lisa.gruen@bio-markt.de", "+49 30 987654", "Inhaberin", true);

// 1 Opportunity (PROPOSAL)
Opportunity o2 = createOpportunity(c2, "Umstellung auf Bio-Lieferung", OpportunityStage.PROPOSAL, 70, 80000.00, LocalDate.of(2024, 11, 15), salesRep);

// 3 Activities (nur Customer-Phase)
createActivity(c2, ActivityType.CALL, "Bedarfsermittlung", salesRep, Instant.now().minus(20, ChronoUnit.DAYS), null);
createActivity(c2, ActivityType.MEETING, "Produktpräsentation", salesRep, Instant.now().minus(15, ChronoUnit.DAYS), null);
createActivity(c2, ActivityType.EMAIL, "Angebot Bio-Sortiment", salesRep, Instant.now().minus(10, ChronoUnit.DAYS), null);
```

**C3: Customer mit erfolgreich abgeschlossenen Opportunities**

```java
Customer c3 = createCustomer("Restaurant Bella Italia", "K-90003", CustomerStatus.ACTIVE, salesRep);

// 1 Adresse
createCustomerAddress(c3, AddressType.BILLING, "Marktplatz 7", "60311", "Frankfurt", "Deutschland", true);

// 1 Location
createCustomerLocation(c3, LocationType.HEADQUARTERS, "Restaurant", "Marktplatz 7", "60311", "Frankfurt", "Deutschland", true);

// 1 Kontakt
createCustomerContact(c3, "Giovanni", "Rossi", "giovanni.rossi@bella-italia.de", "+49 69 123456", "Geschäftsführer", true);

// 2 Opportunities (NEGOTIATION + WON)
Opportunity o3 = createOpportunity(c3, "Liefervertrag Q3", OpportunityStage.NEGOTIATION, 80, 50000.00, LocalDate.of(2024, 9, 30), salesRep);
Opportunity o4 = createOpportunity(c3, "Liefervertrag Q4", OpportunityStage.WON, 100, 55000.00, LocalDate.of(2024, 10, 1), salesRep);

// 4 Activities
createActivity(c3, ActivityType.CALL, "Vertragsverhandlung Q3", salesRep, Instant.now().minus(60, ChronoUnit.DAYS), null);
createActivity(c3, ActivityType.MEETING, "Vertragsabschluss Q3", salesRep, Instant.now().minus(45, ChronoUnit.DAYS), null);
createActivity(c3, ActivityType.CALL, "Vertragsverhandlung Q4", salesRep, Instant.now().minus(30, ChronoUnit.DAYS), null);
createActivity(c3, ActivityType.MEETING, "Vertragsabschluss Q4", salesRep, Instant.now().minus(1, ChronoUnit.DAYS), null);

// Xentral Integration
createXentralIntegration(c3, "XENTRAL-67890", true);
```

**C4: Customer mit mehreren Kontakten, keine Opportunities**

```java
Customer c4 = createCustomer("Kantine Müller & Co", "K-90004", CustomerStatus.ACTIVE, salesRep);

// 1 Adresse
createCustomerAddress(c4, AddressType.BILLING, "Gewerbepark 15", "80331", "München", "Deutschland", true);

// 1 Location
createCustomerLocation(c4, LocationType.HEADQUARTERS, "Kantine", "Gewerbepark 15", "80331", "München", "Deutschland", true);

// 2 Kontakte
createCustomerContact(c4, "Hans", "Müller", "hans.mueller@kantine-muenchen.de", "+49 89 111111", "Betriebsleiter", true);
createCustomerContact(c4, "Petra", "Schmidt", "petra.schmidt@kantine-muenchen.de", "+49 89 111112", "Köchin", false);

// 2 Activities
createActivity(c4, ActivityType.CALL, "Routineanruf", salesRep, Instant.now().minus(30, ChronoUnit.DAYS), null);
createActivity(c4, ActivityType.EMAIL, "Produktupdate Q4", salesRep, Instant.now().minus(15, ChronoUnit.DAYS), null);
```

**C5: INACTIVE Customer mit LOST Opportunity**

```java
Customer c5 = createCustomer("Imbiss City", "K-90005", CustomerStatus.INACTIVE, salesRep);

// 1 Adresse
createCustomerAddress(c5, AddressType.BILLING, "Bahnhofsplatz 1", "50667", "Köln", "Deutschland", true);

// 1 Location
createCustomerLocation(c5, LocationType.HEADQUARTERS, "Imbiss", "Bahnhofsplatz 1", "50667", "Köln", "Deutschland", true);

// 1 Kontakt
createCustomerContact(c5, "Markus", "Klein", "markus.klein@imbiss-city.de", "+49 221 333333", "Inhaber", true);

// 1 Opportunity (LOST)
Opportunity o5 = createOpportunity(c5, "Belieferung Q3", OpportunityStage.LOST, 0, 20000.00, LocalDate.of(2024, 7, 1), salesRep);

// 1 Activity (vor 200 Tagen)
createActivity(c5, ActivityType.CALL, "Letzter Kontakt", salesRep, Instant.now().minus(200, ChronoUnit.DAYS), null);
```

**C6: ARCHIVED Customer (gekündigt)**

```java
Customer c6 = createCustomer("Bäckerei Alt GmbH", "K-90006", CustomerStatus.ARCHIVED, inactiveRep);

// 1 Adresse
createCustomerAddress(c6, AddressType.BILLING, "Alte Straße 99", "70173", "Stuttgart", "Deutschland", true);

// 1 Location
createCustomerLocation(c6, LocationType.HEADQUARTERS, "Bäckerei", "Alte Straße 99", "70173", "Stuttgart", "Deutschland", true);

// 1 Kontakt
createCustomerContact(c6, "Franz", "Alt", "franz.alt@alt-baeckerei.de", "+49 711 444444", "Geschäftsführer", true);

// Keine Opportunities
// Keine Activities
```

**C7: Manager-Customer (Übersicht)**

```java
Customer c7 = createCustomer("Hotel Imperial GmbH", "K-90007", CustomerStatus.ACTIVE, manager);

// 1 Adresse
createCustomerAddress(c7, AddressType.BILLING, "Kaiserplatz 1", "20095", "Hamburg", "Deutschland", true);

// 1 Location
createCustomerLocation(c7, LocationType.HEADQUARTERS, "Hotel", "Kaiserplatz 1", "20095", "Hamburg", "Deutschland", true);

// 1 Kontakt
createCustomerContact(c7, "Wolfgang", "Kaiser", "wolfgang.kaiser@hotel-imperial.de", "+49 40 555555", "Hoteldirektor", true);

// 1 Activity
createActivity(c7, ActivityType.MEETING, "Jahresgespräch", manager, Instant.now().minus(10, ChronoUnit.DAYS), null);
```

**C8: Multi-Location Customer (Filial-Struktur)**

```java
Customer c8 = createCustomer("Supermarkt-Kette Nord GmbH", "K-90008", CustomerStatus.ACTIVE, salesRep);

// 2 Adressen (BILLING != DELIVERY)
createCustomerAddress(c8, AddressType.BILLING, "Verwaltung 1", "24103", "Kiel", "Deutschland", true);
createCustomerAddress(c8, AddressType.DELIVERY, "Zentrallager", "24109", "Kiel", "Deutschland", false);

// 3 Locations (HQ + 2 BRANCHES)
createCustomerLocation(c8, LocationType.HEADQUARTERS, "Zentrale Kiel", "Verwaltung 1", "24103", "Kiel", "Deutschland", true);
createCustomerLocation(c8, LocationType.BRANCH, "Filiale Lübeck", "Marktstraße 10", "23552", "Lübeck", "Deutschland", false);
createCustomerLocation(c8, LocationType.BRANCH, "Filiale Flensburg", "Hafenweg 5", "24937", "Flensburg", "Deutschland", false);

// 3 Kontakte (1 Zentrale, 2 Filialleiter)
createCustomerContact(c8, "Sven", "Hansen", "sven.hansen@supermarkt-nord.de", "+49 431 666666", "Einkaufsleiter", true);
createCustomerContact(c8, "Laura", "Petersen", "laura.petersen@supermarkt-nord.de", "+49 451 777777", "Filialleiterin Lübeck", false);
createCustomerContact(c8, "Finn", "Jansen", "finn.jansen@supermarkt-nord.de", "+49 461 888888", "Filialleiter Flensburg", false);

// 2 Activities
createActivity(c8, ActivityType.MEETING, "Quartalsabstimmung", salesRep, Instant.now().minus(20, ChronoUnit.DAYS), null);
createActivity(c8, ActivityType.EMAIL, "Angebotsupdate Herbst", salesRep, Instant.now().minus(5, ChronoUnit.DAYS), null);

// Xentral Integration
createXentralIntegration(c8, "XENTRAL-99999", true);
```

**Status nach Phase 2:**
- ✅ 3 Users (U1-U3)
- ✅ 5 Leads (L1-L5)
- ✅ 8 Customers (C1-C8)
- ✅ ≥15 Addresses
- ✅ ≥18 Locations
- ✅ ≥13 Contacts
- ✅ ≥5 Opportunities
- ✅ ≥25 Activities

---

### Phase 3: Coverage-Script upgraden ⚠️ (TODO)

**Ziel:** Datenbank-basierte Qualitätsprüfung statt Name-Checking.

#### 3.1 Script-Struktur

**Neuer Name:** `scripts/check-test-data-quality.py`

**4 Level-Checks:**
1. **Existence:** Gibt es überhaupt Daten?
2. **Completeness:** Sind Pflichtfelder gefüllt?
3. **Consistency:** Sind Daten logisch konsistent?
4. **Scenario-Coverage:** Sind alle Szenarien abgedeckt?

#### 3.2 Datenbank-Connection

**Voraussetzung:** DEV-Datenbank muss laufen (`./mvnw quarkus:dev`)

```python
import psycopg2
import os

DB_CONFIG = {
    "host": os.getenv("DB_HOST", "localhost"),
    "port": os.getenv("DB_PORT", "5432"),
    "database": os.getenv("DB_NAME", "freshplan_sales_dev"),
    "user": os.getenv("DB_USER", "freshplan"),
    "password": os.getenv("DB_PASSWORD", "freshplan"),
}

def get_db_connection():
    return psycopg2.connect(**DB_CONFIG)
```

#### 3.3 Level 2: Completeness Check

```python
def check_completeness() -> List[str]:
    """Prüft ob Pflichtfelder gefüllt sind."""
    errors = []
    conn = get_db_connection()
    cursor = conn.cursor()

    # ❌ Customers ohne Adressen
    cursor.execute("""
        SELECT c.id, c.company_name
        FROM customer c
        WHERE NOT EXISTS (
            SELECT 1 FROM customer_address ca WHERE ca.customer_id = c.id
        )
    """)
    missing_addresses = cursor.fetchall()
    if missing_addresses:
        errors.append(f"❌ {len(missing_addresses)} Customers ohne Adressen: {[c[1] for c in missing_addresses]}")

    # ❌ Customers ohne Primary-Adresse
    cursor.execute("""
        SELECT c.id, c.company_name
        FROM customer c
        WHERE NOT EXISTS (
            SELECT 1 FROM customer_address ca
            WHERE ca.customer_id = c.id AND ca.is_primary = true
        )
    """)
    missing_primary_addresses = cursor.fetchall()
    if missing_primary_addresses:
        errors.append(f"❌ {len(missing_primary_addresses)} Customers ohne Primary-Adresse: {[c[1] for c in missing_primary_addresses]}")

    # ❌ Customers ohne Kontakte
    cursor.execute("""
        SELECT c.id, c.company_name
        FROM customer c
        WHERE NOT EXISTS (
            SELECT 1 FROM customer_contact cc WHERE cc.customer_id = c.id
        )
    """)
    missing_contacts = cursor.fetchall()
    if missing_contacts:
        errors.append(f"❌ {len(missing_contacts)} Customers ohne Kontakte: {[c[1] for c in missing_contacts]}")

    # (weitere Checks analog…)

    cursor.close()
    conn.close()
    return errors
```

#### 3.4 Level 3: Consistency Check

```python
def check_consistency() -> List[str]:
    """Prüft ob Daten logisch konsistent sind."""
    errors = []
    conn = get_db_connection()
    cursor = conn.cursor()

    # ❌ CONVERTED Lead ohne convertedToCustomerId
    cursor.execute("""
        SELECT l.id, l.company_name
        FROM lead l
        WHERE l.status = 'CONVERTED'
        AND l.converted_to_customer_id IS NULL
    """)
    converted_leads_without_customer = cursor.fetchall()
    if converted_leads_without_customer:
        errors.append(f"❌ {len(converted_leads_without_customer)} CONVERTED Leads ohne Customer: {[l[1] for l in converted_leads_without_customer]}")

    # ❌ WON Opportunity mit probability < 100
    cursor.execute("""
        SELECT o.id, o.title, o.probability
        FROM opportunity o
        WHERE o.stage = 'WON'
        AND o.probability != 100
    """)
    won_opportunities_wrong_probability = cursor.fetchall()
    if won_opportunities_wrong_probability:
        errors.append(f"❌ {len(won_opportunities_wrong_probability)} WON Opportunities mit falscher Probability: {[o[1] for o in won_opportunities_wrong_probability]}")

    # (weitere Checks analog…)

    cursor.close()
    conn.close()
    return errors
```

#### 3.5 Level 4: Scenario-Coverage Check

```python
def check_scenario_coverage() -> List[str]:
    """Prüft ob alle definierten Szenarien abgedeckt sind."""
    errors = []
    conn = get_db_connection()
    cursor = conn.cursor()

    # ✅ Szenario U1: SALES_REP mit mindestens 2 Leads + 3 Customers
    cursor.execute("""
        SELECT
            u.id,
            u.email,
            COUNT(DISTINCT l.id) AS lead_count,
            COUNT(DISTINCT c.id) AS customer_count
        FROM "user" u
        LEFT JOIN lead l ON l.assigned_to = u.id
        LEFT JOIN customer c ON c.assigned_to = u.id
        WHERE u.role = 'SALES_REP' AND u.status = 'ACTIVE'
        GROUP BY u.id, u.email
        HAVING COUNT(DISTINCT l.id) >= 2 AND COUNT(DISTINCT c.id) >= 3
    """)
    u1_scenarios = cursor.fetchall()
    if not u1_scenarios:
        errors.append("❌ Szenario U1 fehlt: SALES_REP mit ≥2 Leads + ≥3 Customers")

    # ✅ Szenario C1: ACTIVE Customer mit Lead-Historie + Xentral + Opportunities
    cursor.execute("""
        SELECT c.id, c.company_name
        FROM customer c
        WHERE c.status = 'ACTIVE'
        AND c.original_lead_id IS NOT NULL
        AND EXISTS (SELECT 1 FROM xentral_integration xi WHERE xi.customer_id = c.id)
        AND EXISTS (SELECT 1 FROM opportunity o WHERE o.customer_id = c.id)
    """)
    c1_scenarios = cursor.fetchall()
    if not c1_scenarios:
        errors.append("❌ Szenario C1 fehlt: ACTIVE Customer mit Lead + Xentral + Opportunities")

    # (weitere Checks analog…)

    cursor.close()
    conn.close()
    return errors
```

#### 3.6 Main Entry Point

```python
def main():
    print("\n" + "="*80)
    print("🔍 TEST-DATEN QUALITÄTSPRÜFUNG")
    print("="*80)
    print()

    # Level 1: Existence (alter Check, bleibt)
    print("📊 Level 1: Existence Check...")
    # (bestehender Code)

    # Level 2: Completeness
    print("\n📊 Level 2: Completeness Check...")
    completeness_errors = check_completeness()
    if completeness_errors:
        for error in completeness_errors:
            print(f"   {error}")
    else:
        print("   ✅ Alle Pflichtfelder gefüllt")

    # Level 3: Consistency
    print("\n📊 Level 3: Consistency Check...")
    consistency_errors = check_consistency()
    if consistency_errors:
        for error in consistency_errors:
            print(f"   {error}")
    else:
        print("   ✅ Alle Daten konsistent")

    # Level 4: Scenario-Coverage
    print("\n📊 Level 4: Scenario-Coverage Check...")
    scenario_errors = check_scenario_coverage()
    if scenario_errors:
        for error in scenario_errors:
            print(f"   {error}")
    else:
        print("   ✅ Alle Szenarien abgedeckt")

    # Exit Code
    all_errors = completeness_errors + consistency_errors + scenario_errors
    if all_errors:
        print("\n" + "="*80)
        print("❌ TEST-DATEN QUALITÄT: UNVOLLSTÄNDIG")
        print("="*80)
        sys.exit(1)
    else:
        print("\n" + "="*80)
        print("✅ TEST-DATEN QUALITÄT: 100%")
        print("="*80)
        sys.exit(0)

if __name__ == "__main__":
    main()
```

---

### Phase 4: CI-Integration ⚠️ (TODO)

**Ziel:** Automatische Qualitätsprüfung bei jedem Push.

#### 4.1 GitHub Actions Workflow

```yaml
# .github/workflows/test-data-quality.yml
name: Test Data Quality Check

on:
  push:
    branches: [ main, develop, feature/**, bugfix/** ]
  pull_request:
    branches: [ main, develop ]

jobs:
  check-test-data-quality:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_DB: freshplan_sales_dev
          POSTGRES_USER: freshplan
          POSTGRES_PASSWORD: freshplan
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Set up Python
        uses: actions/setup-python@v4
        with:
          python-version: '3.11'

      - name: Install Python dependencies
        run: |
          pip install psycopg2-binary

      - name: Run Flyway Migrations
        run: |
          ./mvnw flyway:migrate -Dquarkus.profile=dev

      - name: Run Quarkus Dev Mode (background)
        run: |
          ./mvnw quarkus:dev &
          sleep 30

      - name: Check Test Data Quality (4 Levels)
        run: |
          echo "🔍 Checking Test Data Quality..."
          python3 scripts/check-test-data-quality.py

      - name: Upload Quality Report (on failure)
        if: failure()
        uses: actions/upload-artifact@v3
        with:
          name: test-data-quality-report
          path: |
            docs/testing/TEST_DATA_QUALITY_CONCEPT.md
            docs/testing/TEST_DATA_SCENARIOS.md
            docs/testing/TEST_DATA_GUIDE.md
          retention-days: 30
```

---

## 📊 ZUSAMMENFASSUNG

### Was ist neu?

**Vorher (FEHLERHAFT):**
- ❌ Coverage-Script prüft nur Entity-Namen
- ❌ Customers haben keine vollständigen Daten
- ❌ Keine Qualitätskontrollen

**Nachher (ZIEL):**
- ✅ 4-Level Qualitätsprüfung (Existence, Completeness, Consistency, Scenario-Coverage)
- ✅ Vollständige, realistische Test-Daten für alle Entities
- ✅ Szenario-Matrix mit minimaler, nicht-überlappender Coverage
- ✅ Systematische Erweiterungsstrategie für neue Features
- ✅ Pre-Commit Hook + CI-Pipeline erzwingen Dokumentation

### User-Feedback integriert

> "Wir wollen doch keinen Türsteher, der schläft und keine Ahnung davon hat was er kontrollieren soll."

✅ **Lösung:** 4-Level Qualitätsprüfung mit echten Datenbank-Checks.

> "Jeder User, Lead und Kunde mit oder ohne Opportunity muss doch vernüftig angelegt sein. So würde das doch ein Verkäufer auch tun."

✅ **Lösung:** Szenario-Matrix mit vollständigen, realistischen Daten.

> "Nicht die pure Masse zählt, sondern nicht-überlappende Szenarien."

✅ **Lösung:** Minimum 3 Users, 5 Leads, 8 Customers (klare Trennung).

> "Erst Doku, dann Daten, dann Script."

✅ **Lösung:** Phase 1 (Doku) → Phase 2 (Daten) → Phase 3 (Script).

---

## 🎯 NÄCHSTE SCHRITTE

1. **✅ Phase 1 (Doku):** Dieses Dokument schreiben → **DONE**
2. **⚠️ Phase 2 (Daten):** Test-Daten in `TestDataService.java` implementieren → **TODO**
3. **⚠️ Phase 3 (Script):** Coverage-Script upgraden auf 4-Level-Checks → **TODO**
4. **⚠️ Phase 4 (CI):** GitHub Actions Workflow anpassen → **TODO**

---

**🤖 Ende des Konzepts | Bereit für Implementierung | Version 1.0 | 2025-10-24**

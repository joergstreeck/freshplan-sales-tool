# FreshPlan Test-Daten Guide

**Letzte Aktualisierung:** 2025-10-24 (Sprint 2.1.7.2)
**Für:** Entwickler, QA, Product Owner
**Referenz:** `backend/src/main/java/de/freshplan/domain/testdata/service/TestDataService.java`

---

## 🚀 QUICK START

### Wo finde ich Test-Daten?

| Entität | Range | Anzahl | Status |
|---------|-------|--------|--------|
| **Users** | `admin`, `anna.schmidt`, `max.mueller`, `tom.weber` | 4 | ✅ |
| **Leads** | `10001` - `10012` | 12 | ✅ |
| **Customers** | `90001` - `90015` | 15 | ✅ |
| **Opportunities** | TBD | 7 | ⚠️ |
| **Activities** | Various IDs | ~30 | ✅ |
| **Contacts** | Embedded in Leads/Customers | ~20 | ✅ |

### Test-Daten aktivieren

```bash
# Dev Mode (automatisch geladen)
./mvnw quarkus:dev

# %dev Profile lädt:
# - db/migration (Production migrations)
# - db/dev-migration (Dev-only migrations)
# - db/dev-seed (DEV-SEED data via TestDataService)
```

---

## 👤 TEST-USERS

| Username | Passwort | Rolle | Xentral Sales Rep ID | Beschreibung |
|----------|----------|-------|----------------------|--------------|
| `admin` | - | admin | ❌ Nein | Voller Zugriff, keine RLS-Filterung |
| `anna.schmidt` | - | partner_manager | ✅ EMP001 | Partner Manager, RLS Security aktiv |
| `max.mueller` | - | partner/USER | ✅ EMP002 | Sales User, normale Vertriebsmitarbeiter |
| `tom.weber` | - | partner/USER | ❌ Nein | Legacy User ohne Xentral-Integration |

**Verwendung:**
- **RLS Security testen:** Login als `anna.schmidt` oder `max.mueller` (sehen nur eigene Kunden via Xentral Sales Rep ID)
- **Admin-Features testen:** Login als `admin` (sieht alle Kunden)
- **Legacy-Szenario testen:** Login als `tom.weber` (keine Xentral-Integration)

---

## 🎯 TEST-LEADS

### Lead-Nummern 10001-10012

| Lead ID | Company Name | Stage | Status | Features | Verwendung |
|---------|-------------|-------|--------|----------|------------|
| `10001` | [TEST] Gastro Schmidt - Vormerkung | VORMERKUNG | REGISTERED | Minimal data, kein Schutz | Pre-Claim testen |
| `10002` | [TEST] Restaurant Müller - Registrierung | REGISTRIERUNG | ACTIVE | 1 Primary Contact | Contact Parity testen |
| `10003` | [TEST] Café Weber - First Contact | REGISTRIERUNG | ACTIVE | Activity statt Contact | Activity-basierte Registrierung |
| `10004` | [TEST] Hotel Becker - Qualifiziert | QUALIFIZIERT | QUALIFIED | Full business data | Ready for Conversion |
| `10005` | [TEST] Bistro Klein - Progress Activities | REGISTRIERUNG | ACTIVE | QUALIFIED_CALL, MEETING, DEMO | Progress Activities testen |
| `10006` | [TEST] Kantine Fischer - Non-Progress | REGISTRIERUNG | ACTIVE | NOTE, FOLLOW_UP, EMAIL | Non-Progress Activities |
| `10007` | [TEST] Mensa Hoffmann - Reminder | REGISTRIERUNG | REMINDER | 60 days inactive | Reminder-Status testen |
| `10008` | [TEST] Cafeteria Richter - Grace Period | REGISTRIERUNG | GRACE_PERIOD | 10-day grace period | Grace Period testen |
| `10009` | [TEST] Restaurant Wagner - Expired | REGISTRIERUNG | EXPIRED | Protection expired | Reassignment testen |
| `10010` | [TEST] Gastro Koch - Lost | REGISTRIERUNG | LOST | Disqualified | Lost-Status testen |
| `10011` | [TEST] Bio-Großhandel Meyer - Converted ⭐ | QUALIFIZIERT | CONVERTED | → Customer 90001 | Lead → Customer Conversion |
| `10012` | [TEST] Fast Food Peters - Deleted | VORMERKUNG | DELETED | Soft deleted | Deletion testen |

**🌟 Killer-Lead für D8 Testing: `10011` (Converted → Customer 90001)**

---

## 🏢 TEST-CUSTOMERS

### Customer-Nummern 90001-90015

| Customer # | Company Name | Status | originalLeadId | Xentral ID | Features | Verwendung |
|------------|-------------|--------|----------------|------------|----------|------------|
| `90001` | 🌟 [TEST] Bio-Großhandel Meyer - Konvertiert (Ex-Lead 10011) | AKTIV | ✅ 10011 | ✅ XENT001 | Lead+Customer Activities, Unified Timeline | **D8 Killer Feature testen!** |
| `90002` | [TEST] Restaurant am See - Direkt angelegt | AKTIV | ❌ Nein | ✅ XENT002 | Nur Customer Activities | Direkt angelegter Kunde |
| `90003` | [TEST] Café Central - Ohne Xentral | AKTIV | ❌ Nein | ❌ Nein | Keine Xentral-Integration | Kunde ohne ERP |
| `90004` | [TEST] Hotel Grandhotel - GOLD Partner | AKTIV | ❌ Nein | ✅ XENT004 | GOLD Status, 3 Contacts, High Revenue | Premium-Kunde |
| `90005` | [TEST] Kantine Zentrale - Hierarchie | AKTIV | ❌ Nein | ✅ XENT005 | ZENTRALE, 2 Filialen | Hierarchie testen |
| `90006` | [TEST] Mensa Campus - Multi-Location | AKTIV | ❌ Nein | ✅ XENT006 | 2 Locations (HQ + Branch) | Multiple Standorte |
| `90007` | [TEST] Restaurant Neu - Prospect | PROSPECT | ❌ Nein | ❌ Nein | In Verhandlung | Prospect-Status |
| `90008` | [TEST] Gastro Riskant - Churn Risk | RISIKO | ❌ Nein | ✅ XENT008 | Keine Activities 90+ Tage | Churn Prevention |
| `90009` | [TEST] Café Inaktiv - Inactive | INAKTIV | ❌ Nein | ✅ XENT009 | Keine Activities 180+ Tage | Reactivation Campaign |
| `90010` | [TEST] Hotel Archiviert - Archived | ARCHIVIERT | ❌ Nein | ❌ Nein | isDeleted: true | Soft Delete |
| `90011` | [TEST] Restaurant A-Kunde - Strategic | AKTIV | ❌ Nein | ✅ XENT011 | A_KUNDE, hoher Umsatz | Strategischer Kunde |
| `90012` | [TEST] Bistro B-Kunde - Medium | AKTIV | ❌ Nein | ✅ XENT012 | B_KUNDE, mittlerer Umsatz | B-Kunde |
| `90013` | [TEST] Kantine C-Kunde - Low | AKTIV | ❌ Nein | ✅ XENT013 | C_KUNDE, niedriger Umsatz | C-Kunde |
| `90014` | [TEST] Café Neukunde - Onboarding | AKTIV | ❌ Nein | ❌ Nein | NEUKUNDE, Onboarding Phase | Neukunden-Onboarding |
| `90015` | [TEST] Mensa Saisonal - Seasonal | AKTIV | ❌ Nein | ✅ XENT015 | seasonal_customer: true | Saisonkunde |

**🌟 Killer-Kunde für D8 Testing: `90001` (Ex-Lead 10011, Unified Timeline mit "Als Lead erfasst" Badge)**

---

## 🎯 FEATURE-TESTING MATRIX

### Welcher Kunde/Lead testet welches Feature?

#### D8 Unified Communication System
- **Unified Timeline (Lead + Customer History):** Customer `90001` (Ex-Lead 10011)
- **Lead-Phase Activities:** Lead `10005`, `10006`
- **Customer-Phase Activities:** Customer `90002`, `90004`
- **Progress Activities:** Lead `10005` (QUALIFIED_CALL, MEETING, DEMO)
- **Non-Progress Activities:** Lead `10006` (NOTE, FOLLOW_UP, EMAIL)

#### Lead → Customer Conversion
- **Erfolgreiche Conversion:** Lead `10011` → Customer `90001`
- **originalLeadId Tracking:** Customer `90001`
- **Xentral Customer Selection während Conversion:** ConvertToCustomerDialog mit Dropdown

#### Xentral Integration
- **MIT Xentral-ID:** Customer `90001`, `90002`, `90004`, `90005`, `90006`, `90008`, `90009`, `90011`, `90012`, `90013`, `90015`
- **OHNE Xentral-ID:** Customer `90003`, `90007`, `90010`, `90014`
- **RLS Security (User mit Xentral Sales Rep ID):** `anna.schmidt` (EMP001), `max.mueller` (EMP002)
- **Kein RLS (User ohne Xentral Sales Rep ID):** `admin`, `tom.weber`

#### Contacts (Lead/Customer Parity)
- **LeadContact - 1 Primary:** Lead `10002`
- **LeadContact - 2+ Contacts:** Lead `10004`
- **CustomerContact - 1 Primary:** Customer `90002`
- **CustomerContact - 3+ Contacts:** Customer `90004` (1 Primary + 2 Secondary)
- **Contact mit Location Assignment:** Customer `90006`
- **Inactive Contact:** Customer `90008`

#### Customer Hierarchie
- **Zentrale mit Filialen:** Customer `90005` (ZENTRALE + 2 Child Customers)
- **Multi-Location:** Customer `90006` (Headquarters + Branch)

#### Customer Lifecycle
- **AKTIV:** Customer `90001`-`90006`, `90011`-`90015`
- **PROSPECT:** Customer `90007`
- **RISIKO:** Customer `90008`
- **INAKTIV:** Customer `90009`
- **ARCHIVIERT:** Customer `90010`

#### Customer Classification
- **A_KUNDE:** Customer `90011`
- **B_KUNDE:** Customer `90012`
- **C_KUNDE:** Customer `90013`
- **NEUKUNDE:** Customer `90014`

#### Lead Protection Timer
- **Active Protection:** Lead `10002`, `10003`, `10004`, `10005`, `10006`
- **Reminder (60 days):** Lead `10007`
- **Grace Period (10 days):** Lead `10008`
- **Expired:** Lead `10009`

#### Lead Stages
- **VORMERKUNG:** Lead `10001`, `10012`
- **REGISTRIERUNG:** Lead `10002`, `10003`, `10005`, `10006`, `10007`, `10008`, `10009`, `10010`
- **QUALIFIZIERT:** Lead `10004`, `10011`

---

## 🔧 NEUE FEATURES HINZUFÜGEN

### Workflow für Entwickler

**Wenn du ein neues Feature entwickelst:**

1. **Feature entwickeln** (Entity/Enum/Migration)
2. **Szenario definieren:**
   - Öffne `docs/testing/TEST_DATA_SCENARIOS.md`
   - Szenario-ID vergeben (z.B. `FEAT-01`)
   - Details dokumentieren (Status, Test-Daten, Features)
   - Coverage-Tabelle aktualisieren
3. **TestDataService erweitern:**
   ```java
   /**
    * NEW_FEATURE-01: Beschreibung des Szenarios.
    *
    * <p>Abgedeckte Szenarien:
    * <ul>
    *   <li>FEAT-01: Feature XYZ mit Variante A</li>
    *   <li>FEAT-02: Feature XYZ mit Variante B</li>
    * </ul>
    *
    * @return List of test entities for NEW_FEATURE
    */
   private List<NewFeature> seedNewFeature() {
     LOG.info("Seeding NEW_FEATURE test data (2 scenarios)...");

     // FEAT-01: Feature XYZ mit Variante A
     NewFeature feat1 = new NewFeature();
     feat1.setName("[TEST] Feature XYZ - Variante A");
     // ... configure feat1

     // FEAT-02: Feature XYZ mit Variante B
     NewFeature feat2 = new NewFeature();
     feat2.setName("[TEST] Feature XYZ - Variante B");
     // ... configure feat2

     newFeatureRepository.persist(feat1, feat2);
     return List.of(feat1, feat2);
   }
   ```
4. **TEST_DATA_GUIDE.md aktualisieren:**
   - Welcher Test-Kunde hat was? (Quick Reference erweitern)
   - Feature-Testing Matrix erweitern
5. **Commit:**
   - Pre-Commit Hook prüft automatisch, ob:
     - `TestDataService.java` geändert wurde
     - `TEST_DATA_SCENARIOS.md` UND `TEST_DATA_GUIDE.md` aktualisiert wurden
   - Commit wird blockiert, wenn Docs fehlen

---

## 🚨 PRE-COMMIT HOOK (AUTOMATISCH)

### Wie funktioniert der Hook?

**Szenario 1: Änderung an Entity/Enum/Migration**
```bash
git add backend/src/main/java/de/freshplan/domain/customer/entity/NewFeature.java
git commit -m "feat: Add NewFeature entity"

⚠️  TESTDATEN-REMINDER:

   Du hast Entities/Enums/Migrations geändert.
   Bitte prüfe, ob TEST-DATEN aktualisiert werden müssen:

   1. Szenarien-Matrix: docs/testing/TEST_DATA_SCENARIOS.md
   2. TestDataService: Neue Szenarien hinzufügen?
   3. Test-Daten-Guide: docs/testing/TEST_DATA_GUIDE.md

   Zum Überspringen: git commit --no-verify

   Testdaten geprüft? (y/n) y
✅ Commit erfolgreich
```

**Szenario 2: Änderung an TestDataService (SCHARFER HOOK)**
```bash
git add backend/src/main/java/de/freshplan/domain/testdata/service/TestDataService.java
git commit -m "feat: Add test data for NewFeature"

🚨 KRITISCH: TestDataService wurde geändert!

   Du MUSST die Dokumentationen aktualisieren:

   ❌ TEST_DATA_SCENARIOS.md nicht geändert
   ❌ TEST_DATA_GUIDE.md nicht geändert

   Commit BLOCKIERT. Bitte aktualisiere BEIDE Docs.

   Zum Überspringen (NOT RECOMMENDED): git commit --no-verify

❌ Commit abgebrochen.
```

**Szenario 3: TestDataService + Docs geändert (KORREKT)**
```bash
git add backend/src/main/java/de/freshplan/domain/testdata/service/TestDataService.java
git add docs/testing/TEST_DATA_SCENARIOS.md
git add docs/testing/TEST_DATA_GUIDE.md
git commit -m "feat: Add test data for NewFeature with docs"

✅ TestDataService + Docs aktualisiert
✅ Commit erfolgreich
```

---

## 📊 BEST PRACTICES

### 1. Selbst-dokumentierende Test-Daten

**✅ GOOD:**
```java
// Customer-Nummer: 90016
// Company Name: [TEST] Restaurant Neu - Feature XYZ
// Features: Feature XYZ aktiv, Variante A
Customer customer = new Customer();
customer.setCustomerNumber("90016");
customer.setCompanyName("[TEST] Restaurant Neu - Feature XYZ");
```

**❌ BAD:**
```java
// Customer-Nummer: 90016
Customer customer = new Customer();
customer.setCustomerNumber("90016");
customer.setCompanyName("Test Restaurant");
// Welches Feature wird hier getestet? Unklar!
```

### 2. Vollständige Features (nicht fragmentiert)

**✅ GOOD:**
- Customer `90001`: Hat ALLES (originalLeadId, Xentral ID, Contacts, Activities, Timeline)
- Customer `90004`: Hat ALLES (GOLD Status, 3 Contacts, High Revenue, Xentral ID)

**❌ BAD:**
- Customer A: Hat Xentral, aber keine Contacts
- Customer B: Hat Contacts, aber keine Activities
- Customer C: Hat Activities, aber keine Timeline
- **Problem:** Du suchst dich tot beim Testen!

### 3. Klare Namenskonventionen

**Format:** `[TEST] <Company Type> <Name> - <Feature Marker>`

**Beispiele:**
- `[TEST] Bio-Großhandel Meyer - Konvertiert (Ex-Lead 10011)`
- `[TEST] Hotel Grandhotel - GOLD Partner`
- `[TEST] Kantine Zentrale - Hierarchie`

### 4. Dokumentation synchron halten

- ✅ Bei jeder Änderung an TestDataService auch Docs aktualisieren
- ✅ Pre-Commit Hook hilft dabei (blockiert Commits wenn Docs fehlen)
- ✅ Szenarien-Matrix ist Single Source of Truth

---

## 🎯 HÄUFIGE FRAGEN

### Q: Wie finde ich den richtigen Test-Kunden für mein Feature?

**A:** Nutze die Feature-Testing Matrix oben! Sie zeigt, welcher Kunde welches Feature testet.

### Q: Warum wird mein Commit blockiert?

**A:** Der Pre-Commit Hook hat erkannt, dass du TestDataService geändert hast, aber die Docs nicht aktualisiert hast. Bitte aktualisiere `TEST_DATA_SCENARIOS.md` UND `TEST_DATA_GUIDE.md`.

### Q: Kann ich den Pre-Commit Hook überspringen?

**A:** Ja, mit `git commit --no-verify`. **ABER:** Das ist NOT RECOMMENDED! Die Docs müssen synchron bleiben, sonst wird das System unwartbar.

### Q: Wie viele Test-Kunden sollte ich für mein neues Feature erstellen?

**A:** Minimum 2, Maximum 4 pro Feature. Folge dem Prinzip: "Vollständige Features, nicht fragmentiert".

### Q: Was ist der Unterschied zwischen TEST_DATA_SCENARIOS.md und TEST_DATA_GUIDE.md?

**A:**
- **TEST_DATA_SCENARIOS.md:** Vollständige Szenarien-Matrix, alle 69 Szenarien, Coverage-Tracking (für Architekten/Tech Leads)
- **TEST_DATA_GUIDE.md:** Developer-freundlicher Quick Reference Guide, "Welcher Kunde hat was?" (für Entwickler/QA)

---

**Verantwortlich:** FreshPlan Development Team
**Letzte Prüfung:** 2025-10-24
**Nächste Prüfung:** 2025-11-24

# FreshPlan Test-Daten Szenarien-Matrix

**Letzte Aktualisierung:** 2025-10-24 (Sprint 2.1.7.2 - D8 Unified Communication)
**Verantwortlich:** FreshPlan Development Team
**Referenz:** `backend/src/main/java/de/freshplan/domain/testdata/service/TestDataService.java`

---

## 🎯 COVERAGE ÜBERSICHT

| Modul | Szenarien Total | Abgedeckt | Status | Priorität |
|-------|----------------|-----------|--------|-----------|
| User | 4 | ✅ 4 | 100% | KRITISCH |
| Lead | 12 | ✅ 12 | 100% | HOCH |
| Customer | 15 | ✅ 15 | 100% | KRITISCH |
| Opportunity | 10 | ⚠️ 7 | 70% | MITTEL |
| Activity | 8 | ✅ 8 | 100% | HOCH |
| Contact | 6 | ✅ 6 | 100% | HOCH |
| Xentral | 6 | ✅ 6 | 100% | HOCH |
| **TOTAL** | **69** | **58** | **84%** | - |

---

## 📋 SZENARIO-DETAILS

### 1️⃣ USER-SZENARIEN (4/4) ✅

| ID | Szenario | Status | Test-User | Details |
|----|----------|--------|-----------|---------|
| USER-01 | Admin ohne Xentral-ID | ✅ | `admin` | Voller Zugriff, keine RLS-Filterung |
| USER-02 | Partner Manager mit Xentral-ID | ✅ | `anna.schmidt` | Xentral Sales Rep ID: `EMP001`, RLS Security aktiv |
| USER-03 | Partner/Sales mit Xentral-ID | ✅ | `max.mueller` | Xentral Sales Rep ID: `EMP002`, normale Vertriebsmitarbeiter |
| USER-04 | Partner ohne Xentral-ID | ✅ | `tom.weber` | Legacy-Szenario, keine Xentral-Integration |

**Features abgedeckt:**
- ✅ Rollen: admin, partner_manager, partner/USER
- ✅ Xentral Sales Rep ID (RLS Security)
- ✅ isTestData Flag
- ✅ Aktiv/Deaktiviert Status

---

### 2️⃣ LEAD-SZENARIEN (12/12) ✅

| ID | Szenario | Status | Lead ID | Stage | Status | Details |
|----|----------|--------|---------|-------|--------|---------|
| LEAD-01 | Vormerkung - Pre-Claim | ✅ | `10001` | VORMERKUNG | REGISTERED | Minimal data, kein Schutz aktiv |
| LEAD-02 | Registrierung - Mit Contact | ✅ | `10002` | REGISTRIERUNG | ACTIVE | 1 Primary Contact, Schutz 6 Monate |
| LEAD-03 | Registrierung - Mit Activity | ✅ | `10003` | REGISTRIERUNG | ACTIVE | First Contact documented |
| LEAD-04 | Qualifiziert - Ready for Conversion | ✅ | `10004` | QUALIFIZIERT | QUALIFIED | Full business data |
| LEAD-05 | Active - Mit Progress Activities | ✅ | `10005` | REGISTRIERUNG | ACTIVE | QUALIFIED_CALL, MEETING, DEMO |
| LEAD-06 | Active - Mit Non-Progress Activities | ✅ | `10006` | REGISTRIERUNG | ACTIVE | NOTE, FOLLOW_UP, EMAIL |
| LEAD-07 | Reminder - 60 Tage inaktiv | ✅ | `10007` | REGISTRIERUNG | REMINDER | Timer abgelaufen, Reminder sent |
| LEAD-08 | Grace Period - 10 Tage Frist | ✅ | `10008` | REGISTRIERUNG | GRACE_PERIOD | Nach Reminder, letzte Chance |
| LEAD-09 | Expired - Schutz abgelaufen | ✅ | `10009` | REGISTRIERUNG | EXPIRED | Kann reassigned werden |
| LEAD-10 | Lost - Disqualifiziert | ✅ | `10010` | REGISTRIERUNG | LOST | Kein Interesse |
| LEAD-11 | Converted - Erfolgreich konvertiert | ✅ | `10011` | QUALIFIZIERT | CONVERTED | → Customer `90001` (originalLeadId) |
| LEAD-12 | Deleted - Soft deleted | ✅ | `10012` | VORMERKUNG | DELETED | Archiviert |

**Features abgedeckt:**
- ✅ 3 Lead Stages (VORMERKUNG, REGISTRIERUNG, QUALIFIZIERT)
- ✅ 10 Lead Status (alle Lifecycle-States)
- ✅ Protection Timer (active, expired, grace period)
- ✅ Lead Contacts (100% Parity mit Customer)
- ✅ Activities (Progress vs. Non-Progress)
- ✅ Territory Assignment
- ✅ DecisionMakerAccess, UrgencyLevel, RelationshipStatus

---

### 3️⃣ CUSTOMER-SZENARIEN (15/15) ✅

| ID | Szenario | Status | Customer # | Status | Details |
|----|----------|--------|------------|--------|---------|
| CUST-01 | 🌟 Konvertiert von Lead + Xentral + Full History | ✅ | `90001` | AKTIV | originalLeadId: 10011, Xentral ID, Lead+Customer Activities |
| CUST-02 | Direkt angelegt + Xentral + Activities | ✅ | `90002` | AKTIV | Kein originalLeadId, Xentral ID, nur Customer Activities |
| CUST-03 | Ohne Xentral (noch nicht in ERP) | ✅ | `90003` | AKTIV | Kein Xentral Customer ID |
| CUST-04 | GOLD Partner + High Revenue + Multiple Contacts | ✅ | `90004` | AKTIV | PartnerStatus: GOLD, 3 Contacts (1 Primary, 2 Secondary) |
| CUST-05 | Hierarchie - Zentrale mit 2 Filialen | ✅ | `90005` | AKTIV | CustomerHierarchyType: ZENTRALE, 2 Child Customers |
| CUST-06 | Mit Locations (Headquarters + Branch) | ✅ | `90006` | AKTIV | 2 CustomerLocations |
| CUST-07 | PROSPECT - Qualifiziert, noch kein Vertrag | ✅ | `90007` | PROSPECT | In Verhandlung |
| CUST-08 | RISIKO - Churn Risk | ✅ | `90008` | RISIKO | Keine Activities seit 90+ Tagen |
| CUST-09 | INAKTIV - Keine Activities 180+ Tage | ✅ | `90009` | INAKTIV | Reactivation Campaign nötig |
| CUST-10 | ARCHIVIERT - Soft deleted | ✅ | `90010` | ARCHIVIERT | isDeleted: true |
| CUST-11 | A_KUNDE - Strategisch wichtig | ✅ | `90011` | AKTIV | Classification: A_KUNDE, hoher Umsatz |
| CUST-12 | B_KUNDE - Mittlerer Umsatz | ✅ | `90012` | AKTIV | Classification: B_KUNDE |
| CUST-13 | C_KUNDE - Niedriger Umsatz | ✅ | `90013` | AKTIV | Classification: C_KUNDE |
| CUST-14 | NEUKUNDE - Onboarding Phase | ✅ | `90014` | AKTIV | Classification: NEUKUNDE, CustomerLifecycleStage: ONBOARDING |
| CUST-15 | Saisonkunde vs. Ganzjahres-Kunde | ✅ | `90015` | AKTIV | seasonal_customer: true |

**Features abgedeckt:**
- ✅ 6 Customer Status (LEAD, PROSPECT, AKTIV, RISIKO, INAKTIV, ARCHIVIERT)
- ✅ originalLeadId (Lead → Customer Conversion)
- ✅ Xentral Customer ID (Integration)
- ✅ Unified Activities (D8 - Lead-Phase + Customer-Phase)
- ✅ Contacts (Primary + Secondary)
- ✅ Timeline Events
- ✅ Locations (Multiple Standorte)
- ✅ Hierarchie (Zentrale + Filialen)
- ✅ PartnerStatus (STANDARD, SILBER, GOLD, PLATIN)
- ✅ CustomerType (UNTERNEHMEN, EINZELSTANDORT, ZENTRALE, FILIALE)
- ✅ Classification (A_KUNDE, B_KUNDE, C_KUNDE, NEUKUNDE)
- ✅ CustomerLifecycleStage (alle Stages)
- ✅ Seasonal vs. Yearlong

---

### 4️⃣ OPPORTUNITY-SZENARIEN (7/10) ⚠️

| ID | Szenario | Status | Opportunity ID | Stage | Type | Details |
|----|----------|--------|----------------|-------|------|---------|
| OPP-01 | NEW_LEAD - From Lead | ✅ | TBD | NEW_LEAD | NEUGESCHAEFT | Lead-Referenz vorhanden |
| OPP-02 | QUALIFICATION - Mit Activities | ✅ | TBD | QUALIFICATION | NEUGESCHAEFT | Lead-based |
| OPP-03 | NEEDS_ANALYSIS - Customer-based | ✅ | TBD | NEEDS_ANALYSIS | SORTIMENTSERWEITERUNG | Customer-Referenz |
| OPP-04 | PROPOSAL - Mit ROI-Calculator | ✅ | TBD | PROPOSAL | NEUGESCHAEFT | expectedValue hoch |
| OPP-05 | NEGOTIATION - Kurz vor Abschluss | ✅ | TBD | NEGOTIATION | NEUGESCHAEFT | probability: 80% |
| OPP-06 | CLOSED_WON - Gewonnen | ✅ | TBD | CLOSED_WON | NEUGESCHAEFT | Customer angelegt |
| OPP-07 | CLOSED_LOST - Verloren | ✅ | TBD | CLOSED_LOST | NEUGESCHAEFT | Verlustgrund dokumentiert |
| OPP-08 | NEUER_STANDORT - Expansion | ❌ | - | - | NEUER_STANDORT | **FEHLT** |
| OPP-09 | VERLAENGERUNG - Contract Renewal | ❌ | - | - | VERLAENGERUNG | **FEHLT** |
| OPP-10 | Mit OpportunityMultiplier | ❌ | - | - | - | **FEHLT** |

**Features abgedeckt:**
- ✅ 7/7 Opportunity Stages
- ⚠️ 2/4 Opportunity Types (NEUER_STANDORT, VERLAENGERUNG fehlen)
- ✅ Lead-Referenz
- ✅ Customer-Referenz
- ✅ Activities
- ❌ OpportunityMultiplier (TODO)

**TODO:**
- [ ] OPP-08: NEUER_STANDORT hinzufügen
- [ ] OPP-09: VERLAENGERUNG hinzufügen
- [ ] OPP-10: OpportunityMultiplier verwenden

---

### 5️⃣ ACTIVITY-SZENARIEN (8/8) ✅ (D8 Unified Communication System)

| ID | Szenario | Status | Details |
|----|----------|--------|---------|
| ACT-01 | Lead-Phase Activities (LEAD entity_type) | ✅ | Für konvertierten Customer sichtbar mit "Als Lead erfasst" Badge |
| ACT-02 | Customer-Phase Activities (CUSTOMER entity_type) | ✅ | Normale Customer Activities |
| ACT-03 | Progress Activities (Reset Timer) | ✅ | QUALIFIED_CALL, MEETING, DEMO, ROI_PRESENTATION, SAMPLE_SENT |
| ACT-04 | Non-Progress Activities | ✅ | NOTE, FOLLOW_UP, EMAIL, CALL, SAMPLE_FEEDBACK |
| ACT-05 | System Activities (Auto-generated) | ✅ | FIRST_CONTACT_DOCUMENTED, EMAIL_RECEIVED, LEAD_ASSIGNED |
| ACT-06 | Mit Outcome - SUCCESSFUL/QUALIFIED | ✅ | ActivityOutcome gesetzt |
| ACT-07 | Ohne Outcome - Neutral | ✅ | ActivityOutcome: NULL |
| ACT-08 | 🌟 Unified Timeline (Lead + Customer History) | ✅ | Customer 90001 zeigt Lead-Activities + Customer-Activities |

**Features abgedeckt:**
- ✅ 18 Activity Types (5 Progress, 5 Non-Progress, 3 System, 5 Legacy)
- ✅ 8 Activity Outcomes
- ✅ 2 Entity Types (LEAD, CUSTOMER)
- ✅ Polymorphic entity_id (BIGINT als TEXT für Lead, UUID als TEXT für Customer)
- ✅ Unified Timeline (D8 Killer Feature!)

---

### 6️⃣ CONTACT-SZENARIEN (6/6) ✅

| ID | Szenario | Status | Details |
|----|----------|--------|---------|
| CONT-01 | LeadContact - 1 Primary | ✅ | Lead 10002 mit 1 Primary Contact |
| CONT-02 | LeadContact - 2+ Contacts | ✅ | Lead 10004 mit 1 Primary + 1 Secondary |
| CONT-03 | CustomerContact - 1 Primary | ✅ | Customer 90002 mit 1 Primary Contact |
| CONT-04 | CustomerContact - 3+ Contacts | ✅ | Customer 90004 mit 1 Primary + 2 Secondary |
| CONT-05 | Contact mit Location Assignment | ✅ | Customer 90006 mit Location-zugeordneten Contacts |
| CONT-06 | Inactive Contact (historisch) | ✅ | Customer 90008 mit inaktivem Contact |

**Features abgedeckt:**
- ✅ LeadContact (100% Parity mit CustomerContact)
- ✅ CustomerContact
- ✅ isPrimary (Hauptansprechpartner)
- ✅ isActive (Aktiv/Inaktiv)
- ✅ decisionLevel (executive, manager, operational, influencer)
- ✅ assignedLocation (Standort-Zuordnung)

---

### 7️⃣ XENTRAL-INTEGRATION-SZENARIEN (6/6) ✅

| ID | Szenario | Status | Details |
|----|----------|--------|---------|
| XENT-01 | Customer MIT Xentral-ID | ✅ | Customer 90001, 90002: Revenue-Daten verfügbar |
| XENT-02 | Customer OHNE Xentral-ID | ✅ | Customer 90003: Noch nicht in ERP |
| XENT-03 | User MIT Xentral Sales Rep ID | ✅ | anna.schmidt (EMP001), max.mueller (EMP002): RLS Security aktiv |
| XENT-04 | User OHNE Xentral Sales Rep ID | ✅ | admin, tom.weber: Sieht alle Kunden |
| XENT-05 | XentralSettings vorhanden | ✅ | Database Config hat Priorität |
| XENT-06 | XentralSettings fehlt | ✅ | Fallback auf application.properties |

**Features abgedeckt:**
- ✅ XentralSettings (Singleton Config)
- ✅ Customer.xentralCustomerId (Customer-Mapping)
- ✅ User.xentralSalesRepId (Sales-Rep-Mapping, RLS Security)
- ✅ Mock Mode (xentral.api.mock-mode=true)
- ✅ v1/v2 Hybrid API (Customers v2, Invoices v1, Employees v1)

---

## 🔧 NEUE FEATURES HINZUFÜGEN

### Workflow

1. **Feature entwickeln** (Entity/Enum/Migration)
2. **Szenario in dieser Matrix hinzufügen:**
   - Szenario-ID vergeben (z.B. `FEAT-01`)
   - Details dokumentieren (Status, Test-Daten, Features)
   - Coverage-Tabelle aktualisieren
3. **TestDataService erweitern:**
   - Methode mit JavaDoc-Referenz auf Szenario-ID
   - Test-Daten generieren
4. **TEST_DATA_GUIDE.md aktualisieren:**
   - Welcher Test-Kunde hat was?
   - Quick Reference erweitern
5. **Commit mit Pre-Commit Hook:**
   - Hook prüft automatisch, ob Docs aktualisiert wurden
   - Commit wird blockiert, wenn TestDataService geändert aber Docs nicht aktualisiert

### Beispiel

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
  // Implementation
}
```

---

## 📊 CHANGELOG

### Sprint 2.1.7.2 (2025-10-24)
- ✅ Initial Szenarien-Matrix erstellt (69 Szenarien identifiziert)
- ✅ D8 Unified Communication System: Activity-Szenarien hinzugefügt (8 Szenarien)
- ✅ Xentral-Integration-Szenarien hinzugefügt (6 Szenarien)
- ⚠️ Opportunity-Szenarien: 7/10 abgedeckt (3 fehlen noch)

### Geplant für Sprint 2.1.8
- [ ] Opportunity-Szenarien vervollständigen (NEUER_STANDORT, VERLAENGERUNG, OpportunityMultiplier)
- [ ] CustomerLocation-Szenarien erweitern (Multi-Location)
- [ ] CustomerHierarchy-Szenarien erweitern (3-Level Hierarchie)

---

## 🎯 BEST PRACTICES

1. **Szenarien-IDs:**
   - Format: `<MODUL>-<NUMMER>` (z.B. `LEAD-01`, `CUST-01`)
   - Fortlaufende Nummerierung pro Modul
   - IDs niemals wiederverwenden (auch nach Löschen)

2. **Status-Tracking:**
   - ✅ Vollständig implementiert und getestet
   - ⚠️ Teilweise implementiert (mit TODO)
   - ❌ Nicht implementiert (geplant)
   - 🌟 Killer Feature (besonders wichtig für Testing)

3. **Coverage-Metrik:**
   - Target: >90% Coverage für kritische Module (User, Customer, Lead, Activity)
   - Target: >80% Coverage für alle anderen Module
   - Monatliches Review der Coverage

4. **Dokumentation synchron halten:**
   - **KRITISCH:** Bei Änderungen an TestDataService MÜSSEN auch die Docs aktualisiert werden
   - Pre-Commit Hook blockiert Commits, wenn Docs fehlen
   - Szenarien-Matrix ist Single Source of Truth

---

**Verantwortlich:** FreshPlan Development Team
**Letzte Prüfung:** 2025-10-24
**Nächste Prüfung:** 2025-11-24

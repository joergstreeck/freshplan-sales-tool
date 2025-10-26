# 📋 TRIGGER: Sprint 2.1.7.2 - D11 Server-Driven Customer Cards

**📅 Erstellt:** 2025-10-25
**🎯 Sprint:** 2.1.7.2 - Customer-Management COMPLETE
**⚡ Deliverable:** D11 - Server-Driven Customer Cards (Refactoring)
**⏱️ Aufwand:** 8-12h (2 Arbeitstage)
**🔄 Status:** 🔧 IN PROGRESS

---

## 🎯 PROBLEM: "Zwei Systeme"

### Root Cause Analysis
```
❌ Frontend: Wizard nutzt `expectedAnnualRevenue` (Lead-Feld, existiert nicht!)
❌ Backend: Customer hat `expectedAnnualVolume` + `estimatedVolume`
❌ fieldCatalog.json: Unvollständig, keine Lead-Parity
❌ Lead→Customer Konvertierung: Felder gehen verloren
❌ Berechnungen im Frontend: Redundant und fehleranfällig
```

### Beweis
```typescript
// customerOnboardingStore.ts:472 (AKTUELL)
const revenueValue = state.customerData.expectedAnnualRevenue; // ❌ Feld existiert nicht!

// Backend Customer.java
private BigDecimal expectedAnnualVolume;  // ✅ Tatsächliches Feld
private BigDecimal estimatedVolume;       // ✅ Lead-Parity
```

**Konsequenz:**
- API liefert `expectedAnnualVolume: 250.000€`
- Frontend zeigt `100.000€` (falsche Quelle/Default)
- Lead-Daten gehen bei Konvertierung verloren

---

## ✅ LÖSUNG: Server-Driven Customer Cards

### Architektur-Prinzip
```
Backend = Single Source of Truth (Schema + Daten)
Frontend = Rendering Layer (kein eigenes Schema)
```

### Vorteile
1. ✅ **Lead→Customer automatisch konsistent** (Backend-Schema gilt für beide)
2. ✅ **Keine feldCatalog.json mehr nötig** (Backend definiert Schema)
3. ✅ **Keine Berechnungen im Frontend** (Backend liefert fertige Werte)
4. ✅ **Zukunftssicher** (neue Felder → nur Backend ändern)
5. ✅ **60% weniger Frontend-Code** (kein Katalog, keine Validierung, keine Logik)

---

## 📦 DELIVERABLE D11: 7 Customer Cards

### Karten-Struktur (Finale Version)
```
1. 🏢 Unternehmensprofil
   └─ Stammdaten, Standorte, Klassifikation, Hierarchie

2. 💰 Geschäftsdaten & Performance
   └─ Umsätze (Xentral), Verträge, Konditionen, YoY Growth

3. 🎯 Bedürfnisse & Lösungen
   └─ Pain Points (aus Lead) → Produktempfehlungen

4. 👥 Kontakte & Stakeholder
   └─ Buying Center, Ansprechpartner, Kommunikationshistorie

5. 📦 Produktportfolio & Services
   └─ Aktive Produkte, Service-Level, Cross-Sell Opportunities

6. 📈 Aktivitäten & Timeline
   └─ Bestellungen (Xentral), Meetings, Calls, Next Steps

7. ⚠️ Health & Risk
   └─ Health Score (auto), Churn-Alerts, Handlungsempfehlungen
```

### Health Score Formula (Advanced mit Xentral)
```java
healthScore = (
  orderRecencyScore(lastOrderDays) * 0.30 +        // 30%: Wie lange her?
  orderFrequencyScore(ordersLast90Days) * 0.25 +   // 25%: Wie oft?
  revenueGrowthScore(yoyGrowth) * 0.20 +           // 20%: Wachstum?
  communicationScore(activities) * 0.15 +          // 15%: Gepflegt?
  paymentBehaviorScore(avgPaymentDelay) * 0.10     // 10%: Zahlung?
) * 100;

// Schwellwerte:
// 🟢 80-100: Gesund
// 🟡 50-79:  Watch
// 🔴 0-49:   Risiko (sofortiges Handeln!)
```

---

## 🏗️ IMPLEMENTIERUNGSPLAN: BAUEN → AUFRÄUMEN → TESTEN

**⚠️ WICHTIG:** Implementierung basiert auf **Zwei-View Architektur** (siehe Architektur-Entscheidung unten)

### Phase 1: CustomerCompactView (Kompakte Übersicht) - 3h

#### Backend (1h)
```
⏳ Optionaler Endpoint (Alternative: Frontend filtert existierende Daten)
   └─ GET /api/customers/{id}/compact-view
   └─ Liefert: Name, Status, Umsatz, Health Score, Locations Summary, Next Steps, Primary Contact

⏳ Falls nicht implementiert:
   └─ Frontend nutzt existierenden GET /api/customers/{id} und filtert relevante Daten
```

#### Frontend (2h)
```
⏳ CustomerCompactView.tsx (NEU)
   ├─ Zeigt: Name, Status, Jahresumsatz
   ├─ Multi-Location Summary ("3 Standorte: München, Berlin, Hamburg")
   ├─ Health Score & Risiko
   ├─ Letzter Kontakt & Letzte Bestellung
   ├─ Nächste Schritte (aus Activities)
   ├─ Haupt-Ansprechpartner
   └─ Button [🔍 Alle Details anzeigen] → Navigate to /customers/:id/details

⏳ Update CustomerDetailPage.tsx
   └─ Default: Zeige CustomerCompactView statt CustomerProfileTab
```

**Acceptance Criteria:**
- [ ] CustomerCompactView zeigt alle Kern-Infos
- [ ] Multi-Location Summary funktioniert
- [ ] Button navigiert zu `/customers/:id/details`

---

### Phase 2: CustomerDetailView mit Tabs - 4h

#### Frontend (4h)
```
⏳ CustomerDetailView.tsx (NEU - Tab-Container)
   ├─ MUI Tabs Component
   ├─ 3 Tabs: Firma, Geschäft, Verlauf
   └─ Tab-State Management (URL-basiert: ?tab=firma)

⏳ CustomerDetailTabFirma.tsx (NEU)
   ├─ Verwendet ServerDrivenCustomerCards
   ├─ Zeigt Cards: company_profile, locations, classification
   └─ Grid Layout: size={{ xs: 12, md: 6 }} (2 Spalten Desktop, 1 Spalte Mobile)

⏳ CustomerDetailTabGeschaeft.tsx (NEU)
   ├─ Verwendet ServerDrivenCustomerCards
   ├─ Zeigt Cards: business_data, contracts, pain_points, products
   └─ Grid Layout: size={{ xs: 12, md: 6 }}

⏳ Routing Update
   ├─ Neue Route: /customers/:id/details
   └─ Component: CustomerDetailViewPage.tsx
```

**Acceptance Criteria:**
- [ ] Tab-Navigation funktioniert
- [ ] Cards werden korrekt in Tabs gruppiert
- [ ] Grid-Layout responsive (MUI v7 API)
- [ ] URL-Navigation funktioniert (`?tab=firma`)

---

### Phase 3: Multi-Location Details + Backend-Erweiterungen - 3h

#### Backend (1.5h)
```
⏳ ExpansionPlan.java (ENUM)
   ├─ YES, NO, UNSURE
   └─ Labels: "Ja, Expansion geplant", "Nein", "Unklar"

⏳ EnumResource.java erweitern
   └─ GET /api/enums/expansion-plan

⏳ Schema Update (CustomerSchemaResource)
   └─ locations Card erweitern mit: billingAddress, deliveryAddresses, locationsDE/CH/AT, expansionPlanned
```

#### Frontend (1.5h)
```
⏳ Tab "Firma" → Card "Standorte" erweitern
   ├─ Rechnungsadresse (Text)
   ├─ Lieferadressen (Liste)
   ├─ Standorte DE/CH/AT (Read-only)
   └─ Expansion geplant (Dropdown mit ExpansionPlan ENUM)

⏳ DynamicField.tsx erweitern (falls nötig)
   └─ Support für ADDRESS, ADDRESS_LIST Typen
```

**Acceptance Criteria:**
- [ ] Standorte-Card zeigt alle Multi-Location Details
- [ ] Expansion-Dropdown funktioniert (ExpansionPlan ENUM)
- [ ] Standort-Statistik (DE/CH/AT) read-only angezeigt

---

### Phase 4: AUFRÄUMEN (1-2h)

#### Alte Wizard-Komponenten LÖSCHEN
```bash
# Frontend: Alte Customer-Wizard Files entfernen
rm -rf frontend/src/features/customers/components/wizard/CustomerOnboardingWizard.tsx
rm -rf frontend/src/features/customers/components/wizard/CustomerOnboardingWizardModal.tsx
rm -rf frontend/src/features/customers/components/steps/Step*.tsx
rm -rf frontend/src/features/customers/stores/customerOnboardingStore.ts
rm -rf frontend/src/features/customers/data/fieldCatalog.json

# Alte CustomerProfileTab.tsx LÖSCHEN (wurde durch CustomerDetailView ersetzt)
rm -rf frontend/src/features/customers/components/CustomerProfileTab.tsx

# Tests entfernen
rm -rf frontend/src/features/customers/tests/integration/wizardFlowIntegration.test.tsx
rm -rf frontend/src/features/customers/components/wizard/__tests__/*.test.tsx
```

#### Alte Logik entfernen
```typescript
// customerOnboardingStore.ts (LÖSCHEN)
// fieldCatalog.json (LÖSCHEN)
// Alle Step*.tsx Komponenten (LÖSCHEN)
// RevenueExpectationSectionV2.tsx (LÖSCHEN - hatte Berechnungen!)
// CustomerProfileTab.tsx (LÖSCHEN - alte Architektur!)
```

**Aufräum-Checkliste:**
- [ ] Wizard-Komponenten gelöscht (8-10 Files)
- [ ] Store gelöscht
- [ ] fieldCatalog.json gelöscht
- [ ] CustomerProfileTab.tsx gelöscht
- [ ] Alte Tests gelöscht (15-20 Test-Files)
- [ ] Imports in CustomerDetailPage.tsx aktualisiert
- [ ] Git Status prüfen (nur neue Files + gelöschte Alte)

---

### Phase 5: TESTEN + DEPENDENCIES PRÜFEN (3-4h)

#### Backend Tests (1h)
```java
// CustomerSchemaResourceTest.java (BEREITS VORHANDEN - erweitern)
@Test void testGetCustomerSchema_Returns7Cards()
@Test void testGetCustomerSchema_ContainsAllFields()
@Test void testFieldDefinitions_HaveCorrectTypes()

// EnumResourceTest.java erweitern
@Test void testGetExpansionPlan_ReturnsAllValues()

// CustomerHealthScoreServiceTest.java (BEREITS VORHANDEN)
@Test void testHealthScore_WithXentralData()
@Test void testHealthScore_Thresholds_Green_Yellow_Red()
```

#### Frontend Tests (1h)
```typescript
// CustomerCompactView.test.tsx (NEU)
describe('CustomerCompactView', () => {
  it('renders customer summary correctly');
  it('shows multi-location summary');
  it('button navigates to detail view');
});

// CustomerDetailView.test.tsx (NEU)
describe('CustomerDetailView', () => {
  it('renders tab navigation');
  it('switches tabs correctly');
  it('updates URL on tab change');
});

// DynamicCustomerCard.test.tsx (BEREITS VORHANDEN)
describe('DynamicCustomerCard', () => {
  it('renders schema-driven fields');
  it('maps backend fields correctly');
  it('handles missing xentralCustomerId');
});
```

#### Integration Tests (1-2h) - KRITISCH!
```typescript
// Lead→Customer Conversion (D1-D10 Dependencies!)
describe('Lead to Customer Conversion', () => {
  it('preserves all lead fields in customer');
  it('maps estimatedVolume correctly');
  it('preserves pain points');
  it('customer shows correctly in CompactView after conversion');
  it('customer shows correctly in DetailView after conversion');
});

// CustomerProfileTab → CustomerDetailView Migration
describe('Profile Tab Migration', () => {
  it('existing customers render in CompactView');
  it('existing customers render in DetailView');
  it('all 7 cards still show data');
});
```

#### D1-D10 Dependencies Check (1h)
```bash
# PFLICHT: Nach Implementation prüfen!

1. Lead→Customer Konvertierung testen (D1-D3)
   └─ Wurden alle Felder korrekt übernommen?

2. Customer-Erstellung ohne Lead testen (D4)
   └─ Funktioniert direktes Anlegen?

3. Xentral-Integration prüfen (D4-D6)
   └─ Werden xentralCustomerId Felder angezeigt?

4. Sales-Rep Mapping prüfen (D6)
   └─ Wird Sales-Rep korrekt zugeordnet?

5. Multi-Location Felder prüfen (D7-D10)
   └─ Werden Standort-Daten korrekt angezeigt?
```

**WICHTIG:** Alle D1-D10 Deliverables MÜSSEN nach D11 Implementation weiterhin funktionieren!

#### Coverage-Ziel
```
Backend:  ≥80% (neue Services + Resources)
Frontend: ≥80% (neue Components)
Integration: 100% (Lead→Customer + Profile Migration!)
```

---

## 📊 AUFWANDS-BREAKDOWN (Aktualisiert)

| Phase | Task | Zeit | Status |
|-------|------|------|--------|
| **Phase 1** | Backend Compact-View Endpoint (optional) | 0.5h | ⏳ |
| | Frontend CustomerCompactView | 2h | ⏳ |
| | Routing Update (CustomerDetailPage) | 0.5h | ⏳ |
| **Phase 2** | CustomerDetailView (Tab-Container) | 1h | ⏳ |
| | CustomerDetailTabFirma | 1.5h | ⏳ |
| | CustomerDetailTabGeschaeft | 1.5h | ⏳ |
| **Phase 3** | Backend ExpansionPlan ENUM + Endpoint | 1h | ⏳ |
| | Frontend Multi-Location Details | 1.5h | ⏳ |
| | Schema Update (locations Card) | 0.5h | ⏳ |
| **Phase 4** | Alte Wizard-Files löschen | 0.5h | ⏳ |
| | CustomerProfileTab.tsx löschen | 0.5h | ⏳ |
| | Alte Tests entfernen | 0.5h | ⏳ |
| | Import-Cleanup | 0.5h | ⏳ |
| **Phase 5** | Backend Tests | 1h | ⏳ |
| | Frontend Tests (CompactView + DetailView) | 1h | ⏳ |
| | Integration Tests (Lead→Customer) | 1h | ⏳ |
| | D1-D10 Dependencies Check | 1h | ⏳ |
| **GESAMT** | | **16h** | ⏳ |

---

## 🎯 ACCEPTANCE CRITERIA

### Funktional
- [ ] Alle 7 Karten werden angezeigt
- [ ] Daten werden korrekt vom Backend geladen
- [ ] Health Score wird automatisch berechnet
- [ ] Lead→Customer Konvertierung behält alle Felder
- [ ] Xentral-Daten werden angezeigt (wenn `xentralCustomerId` gesetzt)
- [ ] Fehlende Endpoints funktionieren: `/api/customers/{id}/locations`, `/api/enums/business-type`

### Technisch
- [ ] Keine `fieldCatalog.json` mehr
- [ ] Keine Berechnungen im Frontend
- [ ] Backend ist Single Source of Truth
- [ ] Test Coverage ≥80%
- [ ] Alte Wizard-Files gelöscht
- [ ] Keine ESLint/TS Errors

### Performance
- [ ] Schema-Endpoint < 50ms
- [ ] Card-Rendering < 100ms
- [ ] Health Score Berechnung < 200ms

---

## 📁 ARTEFAKTE

### Neue Files (Backend)
```
# ENUMs (NEU für Multi-Location)
src/main/java/de/freshplan/domain/customer/enums/ExpansionPlan.java

# Resources (BEREITS VORHANDEN - erweitern)
src/main/java/de/freshplan/domain/customer/api/CustomerSchemaResource.java
src/main/java/de/freshplan/domain/enums/EnumResource.java

# DTOs (BEREITS VORHANDEN)
src/main/java/de/freshplan/domain/customer/dto/CustomerCardSchema.java
src/main/java/de/freshplan/domain/customer/dto/CardSection.java
src/main/java/de/freshplan/domain/customer/dto/FieldDefinition.java

# Services (BEREITS VORHANDEN)
src/main/java/de/freshplan/domain/customer/service/CustomerHealthScoreService.java

# Tests (BEREITS VORHANDEN - erweitern)
src/test/java/de/freshplan/domain/customer/api/CustomerSchemaResourceTest.java
src/test/java/de/freshplan/domain/customer/service/CustomerHealthScoreServiceTest.java
src/test/java/de/freshplan/domain/enums/EnumResourceTest.java
```

### Neue Files (Frontend)
```
# Zwei-View Architektur (NEU)
frontend/src/features/customers/components/detail/CustomerCompactView.tsx
frontend/src/features/customers/components/detail/CustomerDetailView.tsx
frontend/src/features/customers/components/detail/tabs/CustomerDetailTabFirma.tsx
frontend/src/features/customers/components/detail/tabs/CustomerDetailTabGeschaeft.tsx
frontend/src/features/customers/components/detail/tabs/CustomerDetailTabVerlauf.tsx (SPÄTER)
frontend/src/pages/customers/CustomerDetailViewPage.tsx

# Server-Driven Cards (BEREITS VORHANDEN)
frontend/src/components/ServerDrivenCustomerCards.tsx
frontend/src/components/DynamicCustomerCard.tsx
frontend/src/components/DynamicField.tsx
frontend/src/hooks/useCustomerSchema.ts

# Tests (NEU)
frontend/src/features/customers/components/detail/__tests__/CustomerCompactView.test.tsx
frontend/src/features/customers/components/detail/__tests__/CustomerDetailView.test.tsx
frontend/src/features/customers/components/detail/__tests__/CustomerDetailTabFirma.test.tsx
frontend/src/features/customers/components/detail/__tests__/CustomerDetailTabGeschaeft.test.tsx
```

### Gelöschte Files (Cleanup)
```
# Alte Wizard-Komponenten
frontend/src/features/customers/components/wizard/* (ALLE)
frontend/src/features/customers/components/steps/* (ALLE)
frontend/src/features/customers/stores/customerOnboardingStore.ts
frontend/src/features/customers/data/fieldCatalog.json

# Alte Profile Tab (ERSETZT durch CustomerDetailView)
frontend/src/features/customers/components/CustomerProfileTab.tsx

# Alte Tests
frontend/src/features/customers/tests/integration/wizardFlowIntegration.test.tsx
frontend/src/features/customers/components/wizard/__tests__/* (ALLE)
```

---

## 🔄 GIT WORKFLOW

### Branch Strategy
```bash
git checkout -b feature/sprint-2.1.7.2-d11-server-driven-cards
```

### Commit Structure
```bash
# Phase 1: Bauen
git commit -m "feat(backend): Add CustomerSchemaResource + DTOs"
git commit -m "feat(backend): Add CustomerHealthScoreService with Xentral integration"
git commit -m "feat(frontend): Add DynamicCustomerCard framework"
git commit -m "feat(frontend): Implement 7 Customer Cards"

# Phase 2: Aufräumen
git commit -m "refactor(frontend): Remove old CustomerWizard components"
git commit -m "refactor(frontend): Remove old wizard tests"
git commit -m "refactor(frontend): Remove fieldCatalog.json"

# Phase 3: Testen
git commit -m "test(backend): Add CustomerSchemaResource tests"
git commit -m "test(frontend): Add DynamicCard tests + Lead→Customer integration"
```

### PR Erstellung
**⚠️ NUR NACH USER-FREIGABE!**
```bash
# WARTEN auf User: "Soll ich pushen und PR erstellen?"

# Dann erst:
git push -u origin feature/sprint-2.1.7.2-d11-server-driven-cards
gh pr create --title "Sprint 2.1.7.2 D11: Server-Driven Customer Cards" \
  --body "$(cat <<'EOF'
## Summary
- Server-Driven Customer Cards (7 Karten) ersetzen alten Wizard
- Backend Schema-Endpoint als Single Source of Truth
- Health Score mit Xentral-Integration
- Lead→Customer Konvertierung jetzt 100% konsistent

## Changes
- ✅ Backend: CustomerSchemaResource + Health Score Service
- ✅ Frontend: DynamicCard Framework + 7 Karten
- ✅ Cleanup: Alte Wizard-Komponenten entfernt (~15 Files)
- ✅ Tests: ≥80% Coverage Backend + Frontend

## Test Plan
- [ ] Lead→Customer Conversion behält alle Felder
- [ ] Health Score wird korrekt berechnet
- [ ] Alle 7 Karten zeigen Daten
- [ ] Xentral-Daten sichtbar (wenn vorhanden)

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```

---

## ✅ DONE CRITERIA

Sprint 2.1.7.2 D11 ist **COMPLETE** wenn:

1. ✅ Alle 7 Karten implementiert und funktionsfähig
2. ✅ Backend Schema-Endpoint deployed
3. ✅ Health Score Service funktioniert
4. ✅ Alte Wizard-Files gelöscht (Git zeigt Delete)
5. ✅ Tests ≥80% Coverage
6. ✅ Lead→Customer Integration Test grün
7. ✅ PR merged to main
8. ✅ Customer KD-DEV-123 (Super-Customer C1) zeigt alle Daten in neuen Karten

---

## 🎯 FRONTEND ARCHITECTURE DECISION: Zwei-View Customer Detail Design

**📅 Entschieden:** 2025-10-26
**👤 Entscheider:** Jörg Streeck + Claude Code
**📄 Artefakt:** [SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md](artefakte/SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md)

### Problem: Vertikale Kartenstapelung ist schlechtes UX

**User Feedback:**
> "jetzt hast du in Phase 1 einen neuen Tab Profil (Server-Driven) eingerichtet. Und dann hast du alle 7 neuen Karten dort untereinander geballert. Das kann doch aber nicht best Practice sein bei UI Design"

**Root Cause:**
- Phase 1 Implementation: Alle 7 Customer Cards untereinander gestapelt
- Komponente: `ServerDrivenCustomerCards.tsx`
- Problem: Zu viel Information auf einmal → Overwhelm → schlechte UX

### Lösung: Zwei-View Architektur mit Tabs

```
80% Use Case: Kompakte Übersicht (View A)
20% Use Case: Deep Dive mit Tabs (View B)
```

#### View A: Kompakte Kunden-Übersicht (Standard)
- **Route:** `/customers/:id`
- **Component:** `CustomerCompactView.tsx` (NEU)
- **Zeigt:** Name, Status, Umsatz, Risiko, Multi-Location Summary, Hauptansprechpartner, Nächste Schritte
- **Multi-Location:** Nur Summary ("3 Standorte: München, Berlin, Hamburg")
- **Button:** [🔍 Alle Details anzeigen] → Navigiert zu View B

#### View B: Detail-Ansicht mit Tabs (Deep Dive)
- **Route:** `/customers/:id/details`
- **Component:** `CustomerDetailView.tsx` (NEU)
- **Navigation:** Neue Seite (Option A - Browser-Back funktioniert, URLs teilbar)
- **Tabs:**
  1. **"Firma"** → 3 Cards in 2-Spalten Grid (Unternehmensprofil, Standorte, Klassifikation)
  2. **"Geschäft"** → 4 Cards in 2-Spalten Grid (Business Data, Verträge, Pain Points, Produkte)
  3. **"Verlauf"** → 2 Sections in 1-Spalte (Kontakte, Timeline) - SPÄTER

#### Multi-Location Architektur
- **Compact View:** Summary nur ("3 Standorte")
- **Tab "Firma" → Card "Standorte":** Vollständige Details
  - Rechnungsadresse
  - Lieferadressen (Liste)
  - Standorte DE/CH/AT (Read-only)
  - Expansion geplant (Dropdown)

### Implementierungsplan

**Phase 1: Kompakte View (3h)**
- Erstelle `CustomerCompactView.tsx`
- Multi-Location Summary
- Button [Alle Details anzeigen]
- Navigation zu `/customers/:id/details`

**Phase 2: Tab-Structure (4h)**
- Erstelle `CustomerDetailView.tsx` (Tab-Container)
- Tab "Firma" (3 Cards)
- Tab "Geschäft" (4 Cards)
- MUI Grid v7 Layout (2 Spalten Desktop, 1 Spalte Mobile)

**Phase 3: Multi-Location Details (3h)**
- Standorte-Card erweitern
- Backend ENUM: `ExpansionPlan`
- Endpoint: `GET /api/enums/expansion-plan`

**Phase 4: Tab "Verlauf" (SPÄTER - 4h)**
- Kontakte & Timeline
- Endpoints: `/api/customers/{id}/contacts`, `/api/customers/{id}/timeline`

### Neue Backend-Komponenten

**Neue ENUMs:**
- `ExpansionPlan.java` (YES, NO, UNSURE)
- `ContactRole.java` (CHEF, BUYER, DECISION_MAKER)
- `ActivityType.java` (ORDER, MEETING, CALL, EMAIL, NOTE)

**Neue Endpoints:**
- `GET /api/customers/{id}/compact-view` (optional)
- `GET /api/enums/expansion-plan`
- `GET /api/customers/{id}/contacts` (Phase 4)
- `GET /api/customers/{id}/timeline` (Phase 4)

### Acceptance Criteria

**Funktional:**
- [ ] Kompakte Kunden-Übersicht als Default-View
- [ ] Button [Alle Details anzeigen] öffnet Tab-View
- [ ] Tab-View mit 3 Tabs (Firma, Geschäft, Verlauf)
- [ ] Multi-Location: Summary in Compact View, Details in Tab "Firma"
- [ ] Navigation `/customers/:id` → `/customers/:id/details` funktioniert
- [ ] Browser-Back funktioniert korrekt

**Technisch:**
- [ ] MUI Grid v7 API (`size={{ xs: 12, md: 6 }}`)
- [ ] Design System eingehalten (Pre-Commit Hook grün)
- [ ] Server-Driven Architektur beibehalten
- [ ] Test Coverage ≥80%

**Performance:**
- [ ] Compact View lädt < 200ms
- [ ] Tab-Wechsel < 100ms
- [ ] Lazy Loading für Tab-Content

### Artefakt-Link

**📄 Vollständige Spezifikation:** [SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md](artefakte/SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md)

Enthält:
- Detaillierte UI-Mockups
- Component-Struktur
- Backend-Schema
- Code-Beispiele
- Implementierungsplan mit Zeitschätzungen

---

## 📝 NEXT STEPS

Nach D11 COMPLETE:
- Sprint 2.1.7.2 Status → ✅ COMPLETE
- Sprint 2.1.7.7 (Multi-Location) kann starten
- Master Plan V5 Update mit Session Log

---

**🤖 Erstellt mit Claude Code - Trigger-Format V3.2**

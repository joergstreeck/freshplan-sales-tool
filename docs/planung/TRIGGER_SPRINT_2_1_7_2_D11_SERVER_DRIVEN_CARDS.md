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

### Phase 1: BAUEN (6-8h)

#### Backend (3-4h)
```
✅ CustomerSchemaResource.java
   └─ GET /api/customers/schema
   └─ Liefert: Field-Definitionen für alle 7 Karten

✅ DTOs erstellen
   ├─ CustomerCardSchema
   ├─ CardSection
   ├─ FieldDefinition
   └─ FieldType (ENUM: TEXT, CURRENCY, ENUM, BOOLEAN, etc.)

✅ CustomerHealthScoreService.java
   └─ calculateHealthScore() mit Xentral-Daten

✅ Endpoints erweitern
   ├─ GET /api/customers/{id}/locations (FEHLTE!)
   └─ GET /api/enums/business-type (FEHLTE!)
```

#### Frontend (3-4h)
```
✅ DynamicCustomerCard.tsx
   └─ Server-Driven Rendering (Schema → UI)

✅ useCustomerSchema() Hook
   └─ Fetch /api/customers/schema

✅ DynamicField.tsx
   └─ Render basierend auf FieldType

✅ 7 Card-Komponenten
   ├─ CompanyProfileCard.tsx
   ├─ BusinessDataCard.tsx
   ├─ NeedsAndSolutionsCard.tsx
   ├─ ContactsCard.tsx
   ├─ ProductPortfolioCard.tsx
   ├─ ActivitiesCard.tsx
   └─ HealthAndRiskCard.tsx
```

---

### Phase 2: AUFRÄUMEN (1-2h)

#### Alte Wizard-Komponenten LÖSCHEN
```bash
# Frontend: Alte Customer-Wizard Files entfernen
rm -rf frontend/src/features/customers/components/wizard/CustomerOnboardingWizard.tsx
rm -rf frontend/src/features/customers/components/wizard/CustomerOnboardingWizardModal.tsx
rm -rf frontend/src/features/customers/components/steps/Step*.tsx
rm -rf frontend/src/features/customers/stores/customerOnboardingStore.ts
rm -rf frontend/src/features/customers/data/fieldCatalog.json

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
```

**Aufräum-Checkliste:**
- [ ] Wizard-Komponenten gelöscht (8-10 Files)
- [ ] Store gelöscht
- [ ] fieldCatalog.json gelöscht
- [ ] Alte Tests gelöscht (15-20 Test-Files)
- [ ] Imports in CustomerDetailPage.tsx aktualisiert
- [ ] Git Status prüfen (nur neue Files + gelöschte Alte)

---

### Phase 3: TESTEN (2-3h)

#### Backend Tests
```java
// CustomerSchemaResourceTest.java (NEU)
@Test void testGetCustomerSchema_Returns7Cards()
@Test void testGetCustomerSchema_ContainsAllFields()
@Test void testFieldDefinitions_HaveCorrectTypes()

// CustomerHealthScoreServiceTest.java (NEU)
@Test void testHealthScore_WithXentralData()
@Test void testHealthScore_Thresholds_Green_Yellow_Red()
@Test void testHealthScore_WithoutOrders_IsLow()

// Endpoint-Tests
@Test void testGetCustomerLocations_Returns2Locations()
@Test void testGetBusinessTypeEnum_ReturnsAllValues()
```

#### Frontend Tests
```typescript
// DynamicCustomerCard.test.tsx (NEU)
describe('DynamicCustomerCard', () => {
  it('renders schema-driven fields');
  it('maps backend fields correctly');
  it('handles missing xentralCustomerId');
});

// useCustomerSchema.test.tsx (NEU)
describe('useCustomerSchema Hook', () => {
  it('fetches schema from API');
  it('caches schema');
  it('handles API errors');
});

// Integration: Lead→Customer Conversion (KRITISCH!)
describe('Lead to Customer Conversion', () => {
  it('preserves all lead fields in customer');
  it('maps estimatedVolume correctly');
  it('preserves pain points');
});
```

#### Coverage-Ziel
```
Backend:  ≥80% (neue Services + Resources)
Frontend: ≥80% (neue Card-Komponenten)
Integration: 100% (Lead→Customer!)
```

---

## 📊 AUFWANDS-BREAKDOWN

| Phase | Task | Zeit | Status |
|-------|------|------|--------|
| **Bauen** | Backend Schema-Endpoint + DTOs | 2h | ⏳ |
| | Backend Health Score Service | 1h | ⏳ |
| | Frontend DynamicCard Framework | 2h | ⏳ |
| | 7 Card-Komponenten | 3h | ⏳ |
| **Aufräumen** | Alte Wizard-Files löschen | 0.5h | ⏳ |
| | Alte Tests entfernen | 0.5h | ⏳ |
| | Import-Cleanup | 0.5h | ⏳ |
| **Testen** | Backend Tests (Schema + Health) | 1h | ⏳ |
| | Frontend Card Tests | 1h | ⏳ |
| | Lead→Customer Integration Test | 1h | ⏳ |
| **GESAMT** | | **12h** | ⏳ |

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
src/main/java/de/freshplan/domain/customer/api/CustomerSchemaResource.java
src/main/java/de/freshplan/domain/customer/dto/CustomerCardSchema.java
src/main/java/de/freshplan/domain/customer/dto/CardSection.java
src/main/java/de/freshplan/domain/customer/dto/FieldDefinition.java
src/main/java/de/freshplan/domain/customer/service/CustomerHealthScoreService.java
src/test/java/de/freshplan/domain/customer/api/CustomerSchemaResourceTest.java
src/test/java/de/freshplan/domain/customer/service/CustomerHealthScoreServiceTest.java
```

### Neue Files (Frontend)
```
frontend/src/features/customers/components/cards/DynamicCustomerCard.tsx
frontend/src/features/customers/components/cards/DynamicField.tsx
frontend/src/features/customers/components/cards/CompanyProfileCard.tsx
frontend/src/features/customers/components/cards/BusinessDataCard.tsx
frontend/src/features/customers/components/cards/NeedsAndSolutionsCard.tsx
frontend/src/features/customers/components/cards/ContactsCard.tsx
frontend/src/features/customers/components/cards/ProductPortfolioCard.tsx
frontend/src/features/customers/components/cards/ActivitiesCard.tsx
frontend/src/features/customers/components/cards/HealthAndRiskCard.tsx
frontend/src/features/customers/hooks/useCustomerSchema.ts
frontend/src/features/customers/components/cards/__tests__/*.test.tsx
```

### Gelöschte Files (Cleanup)
```
frontend/src/features/customers/components/wizard/* (ALLE)
frontend/src/features/customers/components/steps/* (ALLE)
frontend/src/features/customers/stores/customerOnboardingStore.ts
frontend/src/features/customers/data/fieldCatalog.json
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

## 📝 NEXT STEPS

Nach D11 COMPLETE:
- Sprint 2.1.7.2 Status → ✅ COMPLETE
- Sprint 2.1.7.7 (Multi-Location) kann starten
- Master Plan V5 Update mit Session Log

---

**🤖 Erstellt mit Claude Code - Trigger-Format V3.2**

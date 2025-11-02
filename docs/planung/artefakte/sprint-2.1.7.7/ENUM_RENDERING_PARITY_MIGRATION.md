# 🔍 Sprint 2.1.7.7 Phase 1 - Enum-Rendering-Parity Migration

**Artefakt-ID:** ENUM_RENDERING_PARITY_MIGRATION
**Status:** 🟡 IN PROGRESS (1/20 Files gefixt)
**Priority:** P0 (Critical - Architectural Consistency)
**Estimated Effort:** 4h 15min
**Owner:** Claude Code
**Created:** 2025-11-02
**Dependencies:** Pre-Commit Hook Infrastructure

---

## 🎯 EXECUTIVE SUMMARY

**Problem:** Lead Contact Card zeigt RAW Enum-Werte (z.B. "EXECUTIVE") statt deutsche Labels (z.B. "Geschäftsführer/Inhaber"). **Server-Driven Architecture gilt nur für Forms, NICHT für Read-Views** → Architektur-Inkonsistenz.

**Solution:** Implementiere **Enum-Rendering-Parity** mit Pre-Commit Hook + Context-Aware Detection. Backend = Single Source of Truth für **ALLE** Enum-Werte (Forms + Read-Views).

**Impact:**
- ✅ **Architektur-Konsistenz:** 100% Server-Driven (Forms + Read-Views)
- ✅ **UX:** Deutsche Labels statt englische Enum-Konstanten
- ✅ **Zero Tolerance:** Pre-Commit Hook blockiert neue Violations
- ✅ **Performance:** O(1) Label-Lookup mit useMemo

---

## 📊 PROBLEM ANALYSIS

### **Symptom (Screenshot-Fund vom 2025-11-02)**

Lead Contact Card zeigt:
```typescript
{contact.decisionLevel}  // Rendert: "EXECUTIVE" ❌
```

**Erwartet:**
```
Geschäftsführer/Inhaber ✅
```

### **Root Cause**

**Server-Driven Architecture wurde nur HALB implementiert:**

| Context | Status | Seit Sprint |
|---------|--------|-------------|
| **Forms (CREATE/EDIT)** | ✅ Server-Driven | 2.1.7.2 |
| **Read-Views (CARDS/TABLES)** | ❌ Hardcoded JSX | Legacy |

**Technische Details:**
1. **Forms verwenden:** `useEnumOptions()` + `fieldCatalog.json` → Deutsche Labels ✅
2. **Read-Views verwenden:** Direkte Enum-Werte `{contact.decisionLevel}` → RAW Enums ❌
3. **Backend:** 22 Enum-Endpoints verfügbar (`/api/enums/*`) seit Sprint 2.1.7.2
4. **Frontend:** Nur Forms nutzen diese Endpoints

### **Architectural Drift**

```
Sprint 2.1.7.2 (Xentral Integration):
├── Backend: EnumResource.java ✅ (22 Endpoints)
├── Frontend Forms: useEnumOptions() ✅ (Server-Driven)
└── Frontend Read-Views: ❌ VERGESSEN!
```

**Konsequenz:**
- Forms zeigen "Geschäftsführer/Inhaber" ✅
- Cards zeigen "EXECUTIVE" ❌
- **User Confusion:** Inkonsistente Terminologie

---

## 🔍 SCOPE ANALYSIS

### **Violations Found (2025-11-02)**

**Total:** 20 Files mit 49 Violations

**Breakdown by Enum Field:**

| Enum Field | Violations | Endpoint | Files |
|------------|-----------|----------|-------|
| **decisionLevel** | 10x | `/api/enums/decision-levels` | ContactCard, SmartContactCard, Step3Ansprechpartner, Step3MultiContact, customerOnboardingStore, ContactDialog, LeadContactsCard ✅ |
| **activityType** | 10x | `/api/enums/activity-types` | ActivityDialog, ActivityTimeline (3x), CustomerActivityTimeline (3x), LeadActivityTimeline, LeadActivityTimelineGrouped |
| **businessType** | 6x | `/api/enums/business-types` | BusinessPotentialCard, BusinessPotentialDialog, FitScoreDisplay, leadColumns |
| **salutation** | 3x | `/api/enums/salutations` | ContactEditDialog, Step3Ansprechpartner, customerOnboardingStore, ContactDialog |
| **paymentTerms** | 3x | `/api/enums/payment-terms` | ActionCenterColumnMUI |
| **kitchenSize** | 2x | `/api/enums/kitchen-sizes` | BusinessPotentialCard, BusinessPotentialDialog |
| **legalForm** | 2x | `/api/enums/legal-forms` | CustomerOnboardingWizard |
| **customerType** | 1x | `/api/enums/customer-types` | ActionCenterColumnMUI |

### **File Categories**

1. **Customer Contact Components (5 Files, 10 Violations):**
   - `ContactCard.tsx` - decisionLevel (2x)
   - `SmartContactCard.tsx` - decisionLevel (1x)
   - `ContactEditDialog.tsx` - salutation (1x)
   - `Step3AnsprechpartnerV2.tsx` - salutation, decisionLevel (2x)
   - `Step3MultiContactManagement.tsx` - decisionLevel (2x)

2. **Customer Wizard/Store (3 Files, 6 Violations):**
   - `CustomerOnboardingWizard.tsx` - legalForm (2x)
   - `customerOnboardingStore.ts` - salutation, decisionLevel (2x)
   - `ActionCenterColumnMUI.tsx` - customerType, paymentTerms (3x)

3. **Lead Components (5 Files, 8 Violations):**
   - `LeadContactsCard.tsx` - decisionLevel ✅ **FIXED**
   - `BusinessPotentialCard.tsx` - businessType, kitchenSize
   - `BusinessPotentialDialog.tsx` - businessType, kitchenSize
   - `ContactDialog.tsx` - decisionLevel, salutation
   - `FitScoreDisplay.tsx` - businessType
   - `leadColumns.tsx` - businessType

4. **Activity Components (5 Files, 10 Violations):**
   - `ActivityDialog.tsx` - activityType (1x)
   - `ActivityTimeline.tsx` - activityType (3x)
   - `CustomerActivityTimeline.tsx` - activityType (3x)
   - `LeadActivityTimelineGrouped.tsx` - activityType
   - `LeadActivityTimeline.tsx` - activityType

---

## 🛠️ SOLUTION ARCHITECTURE

### **Option A: Context-Aware Enum-Rendering-Parity (CHOSEN)**

**Why Chosen:**
- ✅ **Zero False Positives:** 3-Layer Filtering (Domain Context + JSX Attribute + Comparison)
- ✅ **ZERO TOLERANCE:** Alle 20 Files sofort fixen (keine Whitelist)
- ✅ **Langfristig:** Hook ohne Legacy-Ballast
- ✅ **Clean Codebase:** 100% Server-Driven ab jetzt

**Rejected Alternatives:**
- ❌ **Option B: Legacy Whitelist** - Tech Debt, nur neue Violations blockieren
- ❌ **Option C: Nur kritische Felder** - Inkonsistent, später mehr Arbeit

### **Implementation Pattern (Referenz: LeadContactsCard.tsx)**

```typescript
// 1. Import Hook
import { useMemo } from 'react';
import { useEnumOptions } from '../../../hooks/useEnumOptions';

// 2. Hook + Label-Lookup (im Component)
const { data: decisionLevelOptions } = useEnumOptions('/api/enums/decision-levels');

const decisionLevelLabels = useMemo(() => {
  if (!decisionLevelOptions) return {};
  return decisionLevelOptions.reduce((acc, item) => {
    acc[item.value] = item.label;
    return acc;
  }, {} as Record<string, string>);
}, [decisionLevelOptions]);

// 3. Replace RAW Enum mit Label-Lookup
// VORHER: {contact.decisionLevel}
// NACHHER: {decisionLevelLabels[contact.decisionLevel] || contact.decisionLevel}
```

**Performance:**
- ✅ **1x Hook Call** pro Component (NICHT pro Row!)
- ✅ **O(1) Map-Lookup** statt O(n) `array.find()`
- ✅ **useMemo** für Lookup-Map (keine re-computation)
- ✅ **React Query Caching** (10min staleTime)

---

## 🔒 PRE-COMMIT HOOK ARCHITECTURE

### **Hook: `check-enum-rendering-parity.py`**

**Location:** `/scripts/check-enum-rendering-parity.py`
**Integration:** `.husky/pre-commit` - PRÜFUNG 2.5
**Language:** Python 3
**Exit Code:** 0 = PASS, 1 = BLOCK

### **3-Layer Context-Aware Filtering**

**Problem:** Naive Regex flaggt False Positives (z.B. `props.title`, `if (status === 'active')`)

**Solution:** 3 Filter-Stufen:

```python
# FILTER 1: Domain Context
if not is_domain_context(object_name):
    continue  # Skip props.title, theme.palette

# FILTER 2: JSX Attribute
if is_jsx_attribute(line, field_name):
    continue  # Skip <Component title="..."/>

# FILTER 3: Comparison/Assignment
if is_comparison_or_assignment(line, field_name):
    continue  # Skip if (status === 'active')
```

**Domain Object Patterns:**
```python
DOMAIN_OBJECT_PATTERNS = [
    'contact', 'lead', 'customer', 'activity', 'opportunity',
    'location', 'branch', 'company', 'person', 'data', 'item',
    'row', 'entity', 'record', 'obj'
]
```

**Enum Field Mapping (22 Fields):**
```python
ENUM_FIELD_MAPPING = {
    'leadSource': 'lead-sources',
    'businessType': 'business-types',
    'kitchenSize': 'kitchen-sizes',
    'activityType': 'activity-types',
    'activityOutcome': 'activity-outcomes',
    'financingType': 'financing-types',
    'customerType': 'customer-types',
    'paymentTerms': 'payment-terms',
    'deliveryCondition': 'delivery-conditions',
    'legalForm': 'legal-forms',
    'expansionPlan': 'expansion-plan',
    'countryCode': 'country-codes',
    'customerStatus': 'customer-status',
    'contactRole': 'contact-roles',
    'salutation': 'salutations',
    'decisionLevel': 'decision-levels',
    'relationshipStatus': 'relationship-status',
    'decisionMakerAccess': 'decision-maker-access',
    'urgencyLevel': 'urgency-levels',
    'budgetAvailability': 'budget-availability',
    'dealSize': 'deal-sizes',
}
```

### **False Positive Reduction**

**Before Context-Awareness:** 68 Files (viele False Positives)
**After 3-Layer Filtering:** 20 Files (nur TRUE Violations)
**Reduction:** 71% False Positive Elimination

---

## 📦 DELIVERABLES

### **E1: Pre-Commit Hook ✅ COMPLETE**
- ✅ `/scripts/check-enum-rendering-parity.py` erstellt
- ✅ `.husky/pre-commit` PRÜFUNG 2.5 integriert
- ✅ 3-Layer Context-Aware Filtering
- ✅ 22 Enum-Felder unterstützt

### **E2: Referenz-Implementation ✅ COMPLETE**
- ✅ `LeadContactsCard.tsx` gefixt (decisionLevel → Label-Lookup)
- ✅ Pattern dokumentiert (useEnumOptions + useMemo)
- ✅ TypeScript Type-Check: PASSED
- ✅ Pre-Commit Hook Test: 0 Violations für diese Datei

### **E3: BATCH 1 - Customer Contact Components (1h)**
**Files:** 5
**Violations:** 10
- [ ] `ContactCard.tsx` - decisionLevel (2x)
- [ ] `SmartContactCard.tsx` - decisionLevel (1x)
- [ ] `ContactEditDialog.tsx` - salutation (1x)
- [ ] `Step3AnsprechpartnerV2.tsx` - salutation, decisionLevel (2x)
- [ ] `Step3MultiContactManagement.tsx` - decisionLevel (2x)

### **E4: BATCH 2 - Customer Wizard/Store (45min)**
**Files:** 3
**Violations:** 6
- [ ] `CustomerOnboardingWizard.tsx` - legalForm (2x)
- [ ] `customerOnboardingStore.ts` - salutation, decisionLevel (2x)
- [ ] `ActionCenterColumnMUI.tsx` - customerType, paymentTerms (3x)

### **E5: BATCH 3 - Lead Components (1h)**
**Files:** 5
**Violations:** 8
- [ ] `BusinessPotentialCard.tsx` - businessType, kitchenSize
- [ ] `BusinessPotentialDialog.tsx` - businessType, kitchenSize
- [ ] `ContactDialog.tsx` - decisionLevel, salutation
- [ ] `FitScoreDisplay.tsx` - businessType
- [ ] `leadColumns.tsx` - businessType

### **E6: BATCH 4 - Activity Components (1h)**
**Files:** 5
**Violations:** 10
- [ ] `ActivityDialog.tsx` - activityType (1x)
- [ ] `ActivityTimeline.tsx` - activityType (3x)
- [ ] `CustomerActivityTimeline.tsx` - activityType (3x)
- [ ] `LeadActivityTimelineGrouped.tsx` - activityType
- [ ] `LeadActivityTimeline.tsx` - activityType

### **E7: Verification + Commit (30min)**
- [ ] False Positive Check (useEnumOptions.ts prüfen)
- [ ] TypeScript Type-Check (alle geänderten Files)
- [ ] Pre-Commit Hook testen (MUSS 0 Violations zeigen)
- [ ] Git Commit mit ausführlicher Message

---

## ✅ SUCCESS CRITERIA

### **Functional**
- [ ] Pre-Commit Hook: `python3 ./scripts/check-enum-rendering-parity.py` → **EXIT 0**
- [ ] Alle 20 Files zeigen deutsche Labels (keine RAW Enums mehr)
- [ ] Backend Enum-Endpoints erreichbar (`/api/enums/*`)

### **Technical**
- [ ] TypeScript: `npm run type-check` → **0 Errors**
- [ ] Hook integriert in `.husky/pre-commit` (PRÜFUNG 2.5)
- [ ] 0 False Positives (Context-Aware Filtering funktioniert)

### **Quality**
- [ ] Code Review: Self-reviewed (Pattern konsistent)
- [ ] Performance: useMemo + O(1) Lookup in allen Files
- [ ] Documentation: MP5 SESSION_LOG + NEXT_STEPS aktualisiert ✅

---

## 📅 TIMELINE

**Total Effort:** 4h 15min

| Phase | Effort | Status |
|-------|--------|--------|
| **E1** Pre-Commit Hook | 1h | ✅ COMPLETE |
| **E2** Referenz-Implementation | 15min | ✅ COMPLETE |
| **E3** BATCH 1 (Customer Contacts) | 1h | ⏳ PENDING |
| **E4** BATCH 2 (Wizard/Store) | 45min | ⏳ PENDING |
| **E5** BATCH 3 (Lead Components) | 1h | ⏳ PENDING |
| **E6** BATCH 4 (Activity Components) | 1h | ⏳ PENDING |
| **E7** Verification + Commit | 30min | ⏳ PENDING |

---

## 🚨 RISKS & MITIGATION

### **Risk 1: Store-File (`customerOnboardingStore.ts`)**
**Problem:** Kein React Component → kann `useEnumOptions` nicht nutzen
**Mitigation:**
- Option A: Store-Refactoring (Enum-Logik in Component verschieben)
- Option B: Manuelles Label-Mapping aus Backend-Response
**Decision:** TBD (bei BATCH 2)

### **Risk 2: False Positives**
**Problem:** `useEnumOptions.ts` selbst könnte als Violation erkannt werden
**Mitigation:** BATCH 5 prüft explizit False Positives
**Status:** Low Risk (Context-Aware Filtering sollte catchen)

### **Risk 3: Performance (19 Files × useEnumOptions)**
**Problem:** Viele Hook-Calls könnten Performance beeinflussen
**Mitigation:**
- ✅ React Query caching (10min staleTime)
- ✅ useMemo für Label-Maps
- ✅ O(1) Lookup statt O(n) `.find()`
**Status:** Low Risk (bereits in LeadContactsCard.tsx getestet)

---

## 📊 METRICS

### **Code Changes**
- **Files Changed:** 21 (20 Violations + 1 Hook)
- **Lines Changed:** ~200 LOC (Hook) + ~10 LOC pro File = ~400 LOC total
- **Migrations:** 0 (nur Frontend Label-Rendering)

### **Test Coverage**
- **Pre-Commit Hook Test:** Manual (python3 ./scripts/check-enum-rendering-parity.py)
- **TypeScript Type-Check:** Automated (npm run type-check)
- **Integration Test:** Frontend Enum-Rendering (visuell)

### **Business Impact**
- ✅ **UX:** Deutsche Labels statt englische Enum-Konstanten
- ✅ **Architektur-Konsistenz:** 100% Server-Driven
- ✅ **Wartbarkeit:** Zentrale Enum-Verwaltung (Backend = SoT)
- ✅ **Quality Gate:** Pre-Commit Hook verhindert neue Violations

---

## 🎯 NEXT STEPS

**Immediate (BATCH 1):**
1. Fix `ContactCard.tsx` (decisionLevel → Label-Lookup)
2. Fix `SmartContactCard.tsx` (decisionLevel → Label-Lookup)
3. Fix `ContactEditDialog.tsx` (salutation → Label-Lookup)
4. Fix `Step3AnsprechpartnerV2.tsx` (salutation + decisionLevel)
5. Fix `Step3MultiContactManagement.tsx` (decisionLevel → Label-Lookup)

**After BATCH 1:**
- BATCH 2: Customer Wizard/Store (45min)
- BATCH 3-4: Lead + Activity Components (2h)
- Verification + Commit (30min)

**After Phase 1 Complete:**
- **Start Phase 2:** Multi-Location Management (D0-D7, 30h)

---

## 📚 REFERENCES

**Related Documents:**
- [TRIGGER_SPRINT_2_1_7_7.md](../../TRIGGER_SPRINT_2_1_7_7.md) - Main Sprint Trigger
- [CRM_COMPLETE_MASTER_PLAN_V5.md](../../CRM_COMPLETE_MASTER_PLAN_V5.md) - Session Log Eintrag
- [SERVER_DRIVEN_ENUM_MAPPING.md](../../grundlagen/SERVER_DRIVEN_ENUM_MAPPING.md) - 22 verfügbare Endpoints

**Backend Enum-Endpoints:**
- GET `/api/enums/decision-levels`
- GET `/api/enums/salutations`
- GET `/api/enums/activity-types`
- GET `/api/enums/business-types`
- GET `/api/enums/kitchen-sizes`
- GET `/api/enums/legal-forms`
- GET `/api/enums/customer-types`
- GET `/api/enums/payment-terms`
- ... (22 total)

**Pre-Commit Hook Location:**
- Script: `/scripts/check-enum-rendering-parity.py`
- Integration: `.husky/pre-commit` (PRÜFUNG 2.5)

---

**✅ PHASE 1 STATUS: 🟡 IN PROGRESS - 2/7 Deliverables COMPLETE (Hook + Referenz)**

**Letzte Aktualisierung:** 2025-11-02 17:45 (Artefakt erstellt nach MP5 Update)

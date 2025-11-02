# Server-Driven Enum Mapping

**Sprint 2.1.7.7 - Migration M3+**
**Status:** Backend-Analyse abgeschlossen
**Datum:** 2025-11-02

## Übersicht

Dieses Dokument dokumentiert die vollständige Mapping zwischen Frontend-Feldern und Backend-Enum-Endpoints für die Server-Driven UI Migration.

## Architektur-Prinzipien

1. **Single Source of Truth:** Backend definiert ALLE Enum-Werte
2. **Backend/Frontend Parity:** ZERO TOLERANCE für Frontend-only Enum-Werte
3. **Keine Hardcoded Labels:** Alle Labels kommen vom Backend via `/api/enums/*`
4. **React Query Caching:** 10min staleTime für optimale Performance

## Verfügbare Backend Enum-Endpoints

### ✅ Vollständig implementiert (22 Endpoints)

| Endpoint | Backend Enum | Verwendung | Status |
|----------|-------------|------------|--------|
| `/api/enums/business-types` | `BusinessType` | Lead + Customer Branche | ✅ |
| `/api/enums/kitchen-sizes` | `KitchenSize` | Lead + Customer Küchengröße | ✅ |
| `/api/enums/lead-sources` | `LeadSource` | Lead Herkunft | ✅ |
| `/api/enums/activity-types` | `ActivityType` | Lead Activities | ✅ |
| `/api/enums/activity-outcomes` | `ActivityOutcome` | Activity Ergebnis | ✅ |
| `/api/enums/customer-status` | `CustomerStatus` | Customer Status | ✅ |
| `/api/enums/customer-types` | `CustomerType` | Customer Typ | ✅ |
| `/api/enums/financing-types` | `FinancingType` | Customer Finanzierung | ✅ |
| `/api/enums/payment-terms` | `PaymentTerms` | Customer Zahlungsbedingungen | ✅ |
| `/api/enums/delivery-conditions` | `DeliveryCondition` | Customer Lieferbedingungen | ✅ |
| `/api/enums/legal-forms` | `LegalForm` | Customer Rechtsform | ✅ |
| `/api/enums/expansion-plan` | `ExpansionPlan` | Customer Expansion | ✅ |
| `/api/enums/country-codes` | `CountryCode` | Adressen | ✅ |
| `/api/enums/contact-roles` | `ContactRole` | Kontakte | ✅ |
| `/api/enums/salutations` | `Salutation` | Kontakte | ✅ |
| `/api/enums/decision-levels` | `DecisionLevel` | Kontakte | ✅ |
| `/api/enums/titles` | `Title` | Kontakte | ✅ |
| `/api/enums/relationship-status` | `RelationshipStatus` | Lead Scoring | ✅ |
| `/api/enums/decision-maker-access` | `DecisionMakerAccess` | Lead Scoring | ✅ |
| `/api/enums/urgency-levels` | `UrgencyLevel` | Lead Scoring | ✅ |
| `/api/enums/budget-availability` | `BudgetAvailability` | Lead Scoring | ✅ |
| `/api/enums/deal-sizes` | `DealSize` | Lead Scoring | ✅ |

### ❌ Fehlende Endpoints (KRITISCH)

| Missing Endpoint | Betroffenes Feld | Status | Action |
|------------------|------------------|--------|--------|
| `/api/enums/lead-stages` | `lead.stage` | ❌ FEHLT | Backend Endpoint erstellen |
| `/api/enums/lead-status` | `lead.status` | ❌ FEHLT | Backend Endpoint erstellen |

## Table Column Enum-Nutzung

### LeadsPage (leadColumns.tsx)

| Column | Field | Aktueller Zustand | Backend Endpoint | Migration Status |
|--------|-------|-------------------|------------------|------------------|
| Branche | `lead.businessType` | ❌ RAW VALUE `"BILDUNG"` | `/api/enums/business-types` | 🔴 BROKEN |
| Status | `lead.stage` | ❌ 9 HARDCODED LABELS (6 falsch!) | ❌ FEHLT | 🔴 BROKEN |
| Score | `lead.leadScore` | ✅ Numeric (keine Enum) | N/A | ✅ OK |

**KRITISCHES PROBLEM - Lead Stage:**
```typescript
// leadColumns.tsx Lines 18-28 - FALSCH!
const leadStageLabels: Record<string, string> = {
  VORMERKUNG: 'Vormerkung',       // ✅ Backend existiert
  REGISTRIERUNG: 'Registrierung', // ✅ Backend existiert
  KONTAKTIERT: 'Kontaktiert',     // ❌ Backend existiert NICHT
  QUALIFIZIERT: 'Qualifiziert',   // ✅ Backend existiert
  ANGEBOT: 'Angebot',             // ❌ Backend existiert NICHT
  VERHANDLUNG: 'Verhandlung',     // ❌ Backend existiert NICHT
  GEWONNEN: 'Gewonnen',           // ❌ Backend existiert NICHT
  VERLOREN: 'Verloren',           // ❌ Backend existiert NICHT
  INAKTIV: 'Inaktiv',             // ❌ Backend existiert NICHT
};
```

**Backend Reality:**
```java
// LeadStage.java - NUR 3 Werte!
public enum LeadStage {
  VORMERKUNG("Vormerkung"),     // Ordinal: 0
  REGISTRIERUNG("Registrierung"), // Ordinal: 1
  QUALIFIZIERT("Qualifiziert")    // Ordinal: 2
}
```

### CustomersPage (customerColumns.tsx)

| Column | Field | Aktueller Zustand | Backend Endpoint | Migration Status |
|--------|-------|-------------------|------------------|------------------|
| Branche | `customer.industry` | ❌ HARDCODED `industryLabels` | `/api/enums/business-types` | 🔴 BROKEN |
| Status | `customer.status` | ✅ Custom Component | `/api/enums/customer-status` | 🟡 PARTIAL |
| Typ | `customer.customerType` | ❌ HARDCODED `customerTypeLabels` | `/api/enums/customer-types` | 🔴 BROKEN |

**DEPRECATED FIELD:**
```typescript
// customer.industry → DEPRECATED seit 2.1.6
// Migration: customer.industry → customer.businessType
// Endpoint: /api/enums/business-types (9 Werte statt 5)
```

## Response Format

Alle Enum-Endpoints liefern folgendes Format:

```json
[
  { "value": "RESTAURANT", "label": "Restaurant" },
  { "value": "HOTEL", "label": "Hotel" },
  { "value": "CATERING", "label": "Catering" }
]
```

- `value`: Enum-Name (UPPERCASE, wie in Backend Enum)
- `label`: Display-Name (User-friendly, German)

## Migration-Plan

### Phase 1: Enum-Helper erstellen ✅ NEXT

Erstelle Hook für Table Column Rendering:

```typescript
// /Users/joergstreeck/freshplan-sales-tool/frontend/src/hooks/useEnumLabelMap.ts

import { useMemo } from 'react';
import { useEnumOptions } from './useEnumOptions';

/**
 * Hook für Table Column Rendering - Konvertiert Enum Options zu Value→Label Map
 *
 * @param enumSource Backend Enum-Endpoint (z.B. "/api/enums/business-types")
 * @returns Record<string, string> für schnelles Label-Lookup
 *
 * @example
 * const businessTypeLabels = useEnumLabelMap('/api/enums/business-types');
 * return businessTypeLabels[lead.businessType] || lead.businessType;
 */
export function useEnumLabelMap(enumSource: string): Record<string, string> {
  const { data, isLoading } = useEnumOptions(enumSource);

  return useMemo(() => {
    if (!data || isLoading) return {};

    return data.reduce((acc, item) => {
      acc[item.value] = item.label;
      return acc;
    }, {} as Record<string, string>);
  }, [data, isLoading]);
}
```

### Phase 2: LeadsPage migrieren

**leadColumns.tsx Changes:**

```typescript
// ❌ REMOVE hardcoded leadStageLabels (Lines 18-28)
// ❌ REMOVE raw enum display (Line 188)

// ✅ ADD at component level (NOT in column config):
export function LeadTableColumnsProvider({ children }: { children: React.ReactNode }) {
  const businessTypeLabels = useEnumLabelMap('/api/enums/business-types');

  // Provide labels via Context or pass as prop
  return <>{children}</>;
}

// ✅ UPDATE column render function:
{
  id: 'businessType',
  label: 'Branche',
  field: 'businessType',
  render: (lead: Lead) =>
    lead.businessType ? businessTypeLabels[lead.businessType] : '-'
}
```

### Phase 3: CustomersPage migrieren

**customerColumns.tsx Changes:**

```typescript
// ❌ REMOVE hardcoded industryLabels
// ❌ REMOVE hardcoded customerTypeLabels

// ✅ ADD:
const businessTypeLabels = useEnumLabelMap('/api/enums/business-types');
const customerTypeLabels = useEnumLabelMap('/api/enums/customer-types');

// ✅ UPDATE column render:
{
  id: 'industry',
  label: 'Branche',
  field: 'businessType', // ⚠️ Field rename: industry → businessType
  render: (customer: CustomerResponse) =>
    customer.businessType ? businessTypeLabels[customer.businessType] : '-'
}
```

### Phase 4: Hardcoded Labels entfernen

**Zu löschen:**
- `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customer/types/customer.types.ts` Lines 153-164 (`industryLabels`)
- `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/leads/config/leadColumns.tsx` Lines 18-28 (`leadStageLabels`)

### Phase 5: Backend Enum-Endpoints ergänzen (OPTIONAL)

Falls Backend-Endpoints fehlen:

```java
// EnumResource.java - ADD:
@GET
@Path("/lead-stages")
@PermitAll
public List<EnumValue> getLeadStages() {
  return Arrays.stream(LeadStage.values())
    .map(stage -> new EnumValue(stage.name(), stage.getDisplayName()))
    .toList();
}

@GET
@Path("/lead-status")
@PermitAll
public List<EnumValue> getLeadStatus() {
  return Arrays.stream(LeadStatus.values())
    .map(status -> new EnumValue(status.name(), status.getDisplayName()))
    .toList();
}
```

## Guards & Validation

### Pre-Commit Hook

`scripts/check-field-parity.py` prüft:
- ✅ Keine hardcoded Enum-Labels in Column Config
- ✅ Alle Enum-Felder haben Backend-Endpoint
- ✅ fieldCatalog.json hat `enumSource` für alle Enums

### CI Check

GitHub Actions prüft:
- ✅ Keine `export const [xyz]Labels = { ... }` Pattern
- ✅ Keine RAW Enum-Values in render Functions

## Testing

### Test-Cases

1. **Enum-Helper Hook:**
   - ✅ Konvertiert Options korrekt zu Map
   - ✅ Gibt leeres Object bei isLoading=true
   - ✅ Cached Results via useMemo

2. **Table Column Rendering:**
   - ✅ Zeigt Labels statt Raw Values
   - ✅ Fallback "-" bei null/undefined
   - ✅ Performance: Keine unnecessary re-renders

3. **Backend Parity:**
   - ✅ Alle Frontend Enums haben Backend Endpoint
   - ✅ Enum-Werte matchen 1:1

## Migration-Checklist

- [x] Backend Enum-Endpoints identifiziert (22 vorhanden, 2 fehlen)
- [ ] Enum-Helper Hook erstellt (`useEnumLabelMap`)
- [ ] leadColumns.tsx migriert (businessType)
- [ ] leadColumns.tsx FIXED (lead.stage - 9→3 Werte)
- [ ] customerColumns.tsx migriert (businessType, customerType)
- [ ] Hardcoded Labels entfernt (`industryLabels`, `leadStageLabels`)
- [ ] Tests geschrieben
- [ ] CI Guards aktiviert
- [ ] Backend Endpoints ergänzt (lead-stages, lead-status) - OPTIONAL

## Referenzen

- **Backend Enum Resource:** `/backend/src/main/java/de/freshplan/api/resources/EnumResource.java`
- **Frontend Enum Hook:** `/frontend/src/hooks/useEnumOptions.ts`
- **Field Catalog:** `/frontend/src/features/customers/data/fieldCatalog.json`
- **CLAUDE.md Guardrails:** `/CLAUDE.md` Lines 47-53

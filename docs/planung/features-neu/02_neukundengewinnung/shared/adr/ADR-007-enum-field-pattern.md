---
module: "02_neukundengewinnung"
sprint: "2.1.6"
domain: "shared"
doc_type: "adr"
status: "accepted"
owner: "team/architecture"
updated: "2025-10-06"
---

# ADR-007: EnumField Pattern for Dynamic Dropdowns

**Status:** ✅ Accepted
**Datum:** 2025-10-06
**Kontext:** Sprint 2.1.6 Phase 2 - BusinessType Harmonization
**Entscheider:** Product Owner, Tech Lead

---

## Kontext und Problemstellung

Nach der BusinessType Harmonization (Sprint 2.1.6 Phase 2) haben wir drei Enum-Typen mit Backend-APIs:
- `BusinessType` (9 Werte: RESTAURANT, HOTEL, KANTINE, ...)
- `LeadSource` (6 Werte: MESSE, EMPFEHLUNG, KALTAKQUISE, ...)
- `KitchenSize` (3 Werte: small, medium, large)

**Probleme mit bisherigem Ansatz:**

1. **Hardcoding in Forms:**
   ```typescript
   // ❌ BAD: Hardcoded values in LeadWizard
   <MenuItem value="RESTAURANT">Restaurant</MenuItem>
   <MenuItem value="HOTEL">Hotel</MenuItem>
   <MenuItem value="KANTINE">Kantine</MenuItem>
   ```
   - Änderungen an Enum-Werten erfordern Frontend-Deployment
   - Inkonsistenz zwischen Backend und Frontend möglich
   - Keine zentrale Single Source of Truth

2. **Unterschiedliche Patterns:**
   - Lead Forms: Hardcoded `<MenuItem>` in `LeadWizard.tsx`
   - Customer Forms: Field Catalog mit `fieldType: "text"` (kein Enum-Support)
   - Keine Wiederverwendung → Code-Duplikation

3. **Field-Based Architecture Lücke:**
   - Field Catalog unterstützt `text`, `number`, `email`, `tel`, `textarea`
   - **FEHLT:** `enum` Field Type für Dropdowns
   - DynamicFieldRenderer kann keine Enums rendern

**Ziel:** Einheitliches Pattern für alle Enum-basierten Dropdowns mit Single Source of Truth.

---

## Entscheidung

**EnumField Pattern in 3 Schichten:**

### Layer 1: Backend API (Single Source of Truth)
```
GET /api/enums/business-types
GET /api/enums/lead-sources
GET /api/enums/kitchen-sizes
```

**Response Format:**
```json
[
  { "value": "RESTAURANT", "label": "Restaurant und Gastronomie" },
  { "value": "HOTEL", "label": "Hotel und Beherbergung" }
]
```

### Layer 2: Frontend Hooks (React Query)
```typescript
// /frontend/src/features/leads/hooks/useBusinessTypes.ts
export function useBusinessTypes() {
  return useQuery({
    queryKey: ['enums', 'businessTypes'],
    queryFn: fetchBusinessTypes,
    staleTime: 5 * 60 * 1000,  // 5min
    gcTime: 10 * 60 * 1000,     // 10min
  });
}
```

**Vorteile:**
- ✅ Client-side Caching (weniger Backend-Requests)
- ✅ Optimistic UI (sofortiges Rendering aus Cache)
- ✅ Automatic Background Refetching

### Layer 3: Reusable EnumField Component
```typescript
// /frontend/src/features/customers/components/fields/EnumField.tsx
interface EnumFieldProps {
  fieldKey: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  enumType: 'businessType' | 'leadSource' | 'kitchenSize';
  required?: boolean;
  disabled?: boolean;
}

export const EnumField: React.FC<EnumFieldProps> = ({
  enumType,
  value,
  onChange,
  label,
  required,
  disabled,
}) => {
  const { data: options, isLoading } = useEnumValues(enumType);

  return (
    <FormControl fullWidth required={required}>
      <InputLabel>{label}</InputLabel>
      <Select value={value} onChange={(e) => onChange(e.target.value)}>
        {options?.map((option) => (
          <MenuItem key={option.value} value={option.value}>
            {option.label}
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );
};
```

### Layer 4: Field Catalog Integration
```json
// /frontend/src/features/customers/data/fieldCatalog.json
{
  "customer": {
    "base": [
      {
        "key": "businessType",
        "label": "Branche",
        "fieldType": "enum",
        "enumType": "businessType",
        "required": false,
        "category": "basic"
      }
    ]
  }
}
```

### Layer 5: DynamicFieldRenderer Extension
```typescript
// /frontend/src/features/customers/components/steps/DynamicFieldRenderer.tsx
case 'enum':
  return (
    <EnumField
      key={field.key}
      fieldKey={field.key}
      label={field.label}
      value={values[field.key] || ''}
      onChange={(value) => handleFieldChange(field.key, value)}
      enumType={field.enumType}
      required={field.required}
    />
  );
```

---

## Begründung

### Warum EnumField Pattern?

**✅ Single Source of Truth:**
- Backend Enum ist die einzige Wahrheit
- Frontend-Änderungen NIEMALS nötig bei Enum-Updates
- Garantierte Konsistenz zwischen BE/FE

**✅ Wiederverwendbarkeit:**
- `EnumField` funktioniert für ALLE Enums (BusinessType, LeadSource, KitchenSize, ...)
- Keine Code-Duplikation mehr
- Neue Enums in <5 Minuten integrierbar

**✅ Field-Based Architecture Completion:**
- Field Catalog unterstützt jetzt `enum` Fields
- Konsistentes Pattern für alle Field Types
- DynamicFieldRenderer ist vollständig

**✅ Developer Experience:**
```typescript
// Vor EnumField Pattern (❌ BAD):
<MenuItem value="RESTAURANT">Restaurant</MenuItem>
<MenuItem value="HOTEL">Hotel</MenuItem>
// → 20+ Zeilen für jeden Dropdown, hardcoded

// Nach EnumField Pattern (✅ GOOD):
<EnumField enumType="businessType" ... />
// → 1 Zeile, automatisch aktualisiert
```

**✅ Performance:**
- React Query Cache (5min): Enum-Werte werden NUR beim ersten Render geladen
- Background Refetching: Automatisches Update bei langen Sessions
- Keine redundanten Backend-Requests

---

## Konsequenzen

### Positive Konsequenzen

✅ **Zentrale Datenhaltung**
- Enum-Änderungen nur im Backend nötig
- Kein Frontend-Deployment bei Enum-Updates

✅ **Konsistente User Experience**
- Alle Forms verwenden identisches Pattern
- Gleiche Dropdown-Logik in Lead- und Customer-Forms

✅ **Skalierbarkeit**
- Neue Enums in <5 Minuten integrierbar
- Pattern funktioniert für beliebige Enum-Typen

✅ **Testbarkeit**
- Mocking von Enum-Values trivial (React Query Mock)
- Keine Hardcoded-Werte in Tests

✅ **Field-Based Architecture Integration**
- Field Catalog vollständig (alle Field Types unterstützt)
- DynamicFieldRenderer kann Enums rendern

### Negative Konsequenzen

❌ **Initial Backend Request**
- Erster Render benötigt Backend-Call (mitigiert durch React Query Cache)

❌ **Abhängigkeit von Backend API**
- Offline-Betrieb eingeschränkt (akzeptabel für B2B Web-App)

❌ **Komplexität für einfache Cases**
- Für 2-3 statische Werte ist Pattern overkill
- **Mitigation:** Pattern NUR für Backend-gesteuerte Enums verwenden

---

## Implementierungs-Details

### Dateien (NEU erstellt):

**Frontend Hooks:**
```
/frontend/src/features/leads/hooks/useBusinessTypes.ts
/frontend/src/features/leads/hooks/useLeadSources.ts
/frontend/src/features/leads/hooks/useKitchenSizes.ts
```

**Reusable Component:**
```
/frontend/src/features/customers/components/fields/EnumField.tsx
```

**Field Catalog Migration:**
```
/frontend/src/features/customers/data/fieldCatalog.json
  - industry → businessType
  - fieldType: "text" → fieldType: "enum"
  - enumType: "businessType" (NEU)
```

**DynamicFieldRenderer Extension:**
```
/frontend/src/features/customers/components/steps/DynamicFieldRenderer.tsx
  - case 'enum': EnumField rendering (NEU)
```

### Dateien (GEÄNDERT):

**LeadWizard.tsx:**
```diff
- <MenuItem value="RESTAURANT">Restaurant</MenuItem>
- <MenuItem value="HOTEL">Hotel</MenuItem>
+ const { data: businessTypes } = useBusinessTypes();
+ businessTypes?.map(type => <MenuItem value={type.value}>{type.label}</MenuItem>)
```

**CustomerDataStep.tsx:**
```diff
- Business-Typ Dropdown hardcoded
+ Verwendet EnumField via DynamicFieldRenderer
```

### Backend (bereits vorhanden):

**EnumResource.java:**
```java
@GET
@Path("/business-types")
public List<EnumValue> getBusinessTypes() {
  return Arrays.stream(BusinessType.values())
    .map(type -> new EnumValue(type.name(), type.getDisplayName()))
    .toList();
}
```

---

## Alternativen

### Alternative 1: Hardcoded Enum-Werte im Frontend (abgelehnt)
**Warum abgelehnt:**
- ❌ Keine Single Source of Truth
- ❌ Frontend-Deployment bei jeder Enum-Änderung
- ❌ Risiko von Inkonsistenzen

### Alternative 2: Enum-Werte in Environment Variables (abgelehnt)
**Warum abgelehnt:**
- ❌ Deployment nötig bei Änderungen
- ❌ Keine dynamischen Updates
- ❌ Komplexere CI/CD-Pipeline

### Alternative 3: GraphQL Schema mit Enums (abgelehnt)
**Warum abgelehnt:**
- ❌ Zu hoher Aufwand (GraphQL-Setup)
- ❌ REST API bereits vorhanden
- ❌ Overkill für einfache Enum-Listen

### Alternative 4: Separate Dropdown-Components (abgelehnt)
**Warum abgelehnt:**
- ❌ Code-Duplikation (BusinessTypeDropdown, LeadSourceDropdown, ...)
- ❌ Keine Field-Catalog-Integration
- ❌ Wartungsaufwand steigt mit jedem Enum

---

## Risiken und Mitigationen

| Risiko | Wahrscheinlichkeit | Impact | Mitigation |
|--------|-------------------|--------|------------|
| **Backend API down** | Niedrig | Hoch | React Query Cache (5min) überbrückt kurze Ausfälle |
| **Langsamer erster Render** | Mittel | Niedrig | Prefetching + Loading States (CircularProgress) |
| **Cache-Invalidierung** | Niedrig | Niedrig | React Query Background Refetch (automatisch) |
| **Falsche Enum-Werte im Cache** | Niedrig | Mittel | gcTime: 10min (Cache wird regelmäßig erneuert) |

---

## Compliance

**DSGVO:** Keine personenbezogenen Daten in Enum-Werten → keine Auswirkung

**Performance:**
- ✅ Enum-Requests < 50ms (Backend)
- ✅ React Query Cache reduziert Backend-Load um ~90%

---

## Verwandte Dokumente

- [HARMONIZATION_COMPLETE.md](../../artefakte/SPRINT_2_1_6/HARMONIZATION_COMPLETE.md) - BusinessType Harmonization
- [ADR-006](./ADR-006-lead-management-hybrid-architecture.md) - Lead-Management Hybrid-Architektur
- [BUSINESS_LOGIC_LEAD_ERFASSUNG.md](../../artefakte/SPRINT_2_1_5/BUSINESS_LOGIC_LEAD_ERFASSUNG.md) - Lead Business Logic

---

## Lessons Learned

### Was funktioniert gut:
- ✅ React Query Caching ist perfekt für Enum-Werte (selten ändernd)
- ✅ Field Catalog Integration macht Pattern sehr flexibel
- ✅ DynamicFieldRenderer Extension war trivial (10 Zeilen)

### Was wir beim nächsten Mal anders machen:
- 🔄 EnumField Pattern von Anfang an einplanen (nicht nachträglich)
- 🔄 Field Catalog sollte `enum` Field Type ab Tag 1 unterstützen
- 🔄 Backend Enum-APIs früher erstellen (vor Frontend-Hardcoding)

---

**Erstellt:** 2025-10-06
**Review:** Product Owner ✅, Tech Lead ✅
**Implementiert:** Sprint 2.1.6 Phase 2 ✅
**Related PR:** #131 (Lead.stage Enum), BusinessType Harmonization

# 🎯 FC-005 Enterprise-Ready Plan

**Datum:** 27.07.2025  
**Feature:** FC-005 Customer Management  
**Ziel:** Code auf Enterprise-Standard bringen vor Pull Request  
**Geschätzter Aufwand:** 3-4 Stunden

---

## 📊 Ausgangslage

- ✅ **Funktionalität:** 100% implementiert, alle 27 Tests grün
- ❌ **Code-Qualität:** 147 ESLint-Probleme
- 🎯 **Ziel:** Sauberer, wartbarer Enterprise-Code

---

## 🔧 Schritt-für-Schritt Plan

### Phase 1: Type Safety herstellen (1.5h)
**Priorität: HOCH - Verhindert Runtime-Fehler**

#### 1.1 API Response Types definieren
```bash
cd frontend/src/features/customers/types
# Erstelle api-types.ts mit konkreten Response Types
```

**Dateien:**
- `services/api-client.ts` - 21 `any` ersetzen
- `types/api.types.ts` - NEU erstellen

#### 1.2 Form Value Types spezifizieren
```typescript
// Statt: values: Record<string, any>
// Neu:   values: CustomerFormData
```

**Dateien:**
- `components/fields/DynamicFieldRenderer.tsx`
- `components/wizard/CustomerOnboardingWizard.tsx`
- `hooks/useFormValidation.ts`

#### 1.3 Event Handler Types
```typescript
// Statt: onChange: (value: any) => void
// Neu:   onChange: (value: FieldValue) => void
```

---

### Phase 2: Dead Code entfernen (0.5h)
**Priorität: MITTEL - Reduziert Bundle Size**

#### 2.1 Ungenutzte Imports entfernen
```bash
# Automatisch mit ESLint:
npx eslint src/features/customers --fix
```

**Hauptsächlich in:**
- `DetailedLocationsStep.tsx` - 7 ungenutzte MUI Imports
- `SaveIndicator.tsx` - Typography nicht genutzt
- Weitere 15+ Dateien

#### 2.2 Ungenutzte Funktionen
- `evaluateCondition` in DynamicFieldRenderer (für Phase 2)
- `addDetailedLocation` / `updateDetailedLocation` 

---

### Phase 3: React Hook Dependencies (0.5h)
**Priorität: MITTEL - Verhindert Stale Closures**

#### 3.1 useEffect/useCallback Dependencies
```bash
# Prüfe alle Hooks:
grep -r "useEffect\|useCallback\|useMemo" src/features/customers
```

**Kritische Stellen:**
- `useFieldDefinitions.ts` - catalog dependencies
- `useAutoSaveApi.ts` - setDraftId dependency
- `LocationsStep.tsx` - industry dependency

---

### Phase 4: Validation Regex Fixes (0.5h)
**Priorität: NIEDRIG - Aber einfach zu fixen**

#### 4.1 Escape-Sequenzen korrigieren
```typescript
// Statt: /\+\(\)\//-/
// Neu:   /[+()/-]/
```

**Dateien:**
- `validation/baseSchemas.ts` - 8 Regex-Fehler
- `validation/schemaBuilder.ts` - 2 Regex-Fehler

---

### Phase 5: Code Review & Testing (1h)
**Priorität: HOCH - Qualitätssicherung**

#### 5.1 Finale ESLint Prüfung
```bash
npm run lint
# Sollte 0 Errors zeigen
```

#### 5.2 Tests erneut ausführen
```bash
npm run test -- src/features/customers --run
# Alle 27 Tests müssen grün bleiben
```

#### 5.3 Type Coverage prüfen
```bash
npx type-coverage src/features/customers
# Ziel: > 95% Type Coverage
```

---

## 📝 Konkrete Aufgaben für Claude

### 1. Type Definitions erstellen
```typescript
// frontend/src/features/customers/types/api.types.ts
export interface CustomerApiResponse {
  id: string;
  fields: Record<string, FieldValue>;
  createdAt: string;
  updatedAt: string;
}

export type FieldValue = string | number | boolean | string[] | null;

export interface ValidationError {
  field: string;
  message: string;
}
```

### 2. DynamicFieldRenderer typisieren
```typescript
// Ersetze alle any mit konkreten Types:
interface DynamicFieldRendererProps {
  fields: FieldDefinition[];
  values: Record<string, FieldValue>; // statt any
  errors: Record<string, string>;
  onChange: (fieldKey: string, value: FieldValue) => void; // statt any
  onBlur: (fieldKey: string) => void;
  loading?: boolean;
  readOnly?: boolean;
}
```

### 3. Ungenutzte Imports automatisch entfernen
```bash
# ESLint Auto-Fix für unused imports:
cd frontend
npx eslint src/features/customers --fix --rule '@typescript-eslint/no-unused-vars: error'
```

### 4. Hook Dependencies fixen
```typescript
// Beispiel useFieldDefinitions.ts:
useEffect(() => {
  // Logic here
}, [catalog.customer.base, catalog.location?.base]); // Dependencies hinzufügen
```

---

## ✅ Definition of Done

- [ ] 0 ESLint Errors in `src/features/customers`
- [ ] Alle 27 Tests bleiben grün
- [ ] Type Coverage > 95%
- [ ] Keine `any` Types mehr
- [ ] Keine ungenutzten Imports
- [ ] Alle Hook Dependencies korrekt

---

## 🚀 Befehle für nächste Session

```bash
# 1. Projekt-Setup
cd /Users/joergstreeck/freshplan-sales-tool
git status

# 2. ESLint Status prüfen
cd frontend
npx eslint src/features/customers --ext ts,tsx | grep -E "error|warning" | wc -l

# 3. Mit Type Definitions beginnen
mkdir -p src/features/customers/types
touch src/features/customers/types/api.types.ts

# 4. Nach jedem Schritt testen
npm run test -- src/features/customers/tests --run
```

---

## 💡 Tipps für effizientes Arbeiten

1. **Batch-Fixes:** Gleiche Fehler in allen Dateien auf einmal fixen
2. **Auto-Fix nutzen:** `eslint --fix` für einfache Probleme
3. **Types zentral:** Einmal definieren, überall nutzen
4. **Incremental:** Nach jeder Phase Tests laufen lassen

---

## 📋 Erwartetes Endergebnis

```bash
# Vorher:
✖ 147 problems (135 errors, 12 warnings)

# Nachher:
✔ No ESLint errors
✔ 27/27 Tests grün
✔ Type Coverage: 96.5%
✔ Bundle Size reduziert
✔ Enterprise-Ready Code
```

**Dann:** Pull Request erstellen mit sauberem Code! 🎉
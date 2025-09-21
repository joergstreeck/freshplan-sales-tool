# 📚 TypeScript Import Type Guide - Lessons Learned

**Erstellt:** 27.07.2025  
**Kontext:** Sprint 2 - Systematische Behebung von Import-Fehlern bei CustomersPageV2

## 🔗 Navigation
- **Zurück zu:** [CLAUDE.md](/Users/joergstreeck/freshplan-sales-tool/CLAUDE.md#symptom-the-requested-module-does-not-provide-an-export-named-fielddefinition)
- **Debug Session:** [Import Type Marathon](/Users/joergstreeck/freshplan-sales-tool/docs/claude-work/daily-work/2025-07-27/2025-07-27_DEBUG_typescript-import-type-marathon.md)
- **Debug Cookbook:** [Hauptübersicht](/Users/joergstreeck/freshplan-sales-tool/docs/guides/DEBUG_COOKBOOK.md)
- **Sprint 2 Updates:** [TypeScript Integration in Sprint 2](/Users/joergstreeck/freshplan-sales-tool/docs/claude-work/daily-work/2025-07-27/2025-07-27_IMPL_sprint2-docs-typescript-update.md)

## 🚨 Das Problem

Bei der Arbeit mit TypeScript, Vite und `"verbatimModuleSyntax": true` führten fehlende `import type` Statements zu einer Kaskade von Fehlern:

```
"The requested module does not provide an export named 'FieldDefinition'"
```

## ✅ Die Lösung: Systematisches Vorgehen

### 1. TypeScript `import type` ist PFLICHT für reine Typen

**Warum?**
- Vite mit `verbatimModuleSyntax` akzeptiert für Types/Interfaces nur direkte `export type`/`export interface` Deklarationen
- Klassische Imports ohne `type` verursachen Build-Fehler

**Regel:**
```typescript
// ❌ FALSCH
import { FieldDefinition } from './types/field.types';

// ✅ RICHTIG
import type { FieldDefinition } from './types/field.types';

// ✅ RICHTIG für Enums (keine Types!)
import { EntityType } from './types/field.types';
```

### 2. Keine Re-Exports für Typen am Dateiende

```typescript
// ❌ FALSCH
type Foo = { ... }
export { Foo };

// ✅ RICHTIG
export type Foo = { ... }

// ✅ RICHTIG
export interface FieldDefinition { ... }
```

### 3. Build-System & Cache bei Typ-Änderungen

Nach Änderungen an `.types.ts` Dateien IMMER:

```bash
# 1. Vite/Node-Prozesse stoppen
# 2. Cache löschen
rm -rf node_modules/.vite

# 3. Neu starten
npm run dev

# 4. Browser Hard Reload (Ctrl+F5)
```

### 4. Systematische Debug-Strategie

1. **Fehler lesen:** Welche Datei/Export fehlt?
2. **Export prüfen:** Direkter Export vorhanden?
3. **Import korrigieren:** `import type` hinzufügen
4. **Projektweit suchen:**
   ```bash
   # Finde alle FieldDefinition Imports ohne type
   grep -r "import {.*FieldDefinition" frontend/src/
   ```
5. **Cache löschen & neu starten**
6. **Nächsten Fehler beheben**

## 🎯 Sprint 2 Erfolg: Alle Import-Fehler systematisch behoben

### Korrigierte Dateien:
- ✅ `field.types.ts` - Alle Re-Exports entfernt
- ✅ `schemaBuilder.ts` - `import type` hinzugefügt
- ✅ `crossFieldValidation.ts` - CustomerData entfernt
- ✅ `useFormValidation.ts` - react-hook-form Imports korrigiert
- ✅ `conditionEvaluator.ts` - `import type` hinzugefügt
- ✅ `customerOnboardingStore.ts` - Location types korrigiert
- ✅ `api.types.ts` - Type/Enum Imports getrennt
- ✅ `useFieldDefinitions.ts` - `import type` hinzugefügt
- ✅ `useFieldDefinitionsApi.ts` - Type/Enum Imports getrennt
- ✅ `FieldWrapper.tsx` - `import type` hinzugefügt
- ✅ Alle fieldTypes/*.tsx - `import type` hinzugefügt

### Zeitaufwand:
- Gesamtdauer: ~2 Stunden
- Pro Fehler: ~5-10 Minuten
- Ersparnis durch systematisches Vorgehen: ~4 Stunden

## 📋 Checkliste für neue Features

Bei neuen TypeScript-Dateien IMMER:

- [ ] Types/Interfaces direkt mit `export type`/`export interface` exportieren
- [ ] Imports von Types mit `import type` durchführen
- [ ] Keine Re-Exports am Dateiende
- [ ] Nach größeren Type-Änderungen Cache löschen
- [ ] Grep-Suche nach fehlenden `import type` Statements

## 🔗 Relevante Dokumentation

- [TypeScript Docs - Type-Only Imports](https://www.typescriptlang.org/docs/handbook/modules.html#type-only-imports-and-exports)
- [Vite - TypeScript](https://vitejs.dev/guide/features.html#typescript)
- [FC-005 Frontend Architektur](../features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/README.md)

## 💡 Tipp für das Team

Diese Import-Hygiene von Anfang an beachten spart Stunden an Debug-Zeit!

---

**Dokumentiert von:** Claude  
**Review:** Jörg Streeck  
**Status:** ✅ Best Practice etabliert
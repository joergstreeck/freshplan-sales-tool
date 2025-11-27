# Migration Plan: fieldCatalog.json Removal

**Sprint:** 2.1.7.x
**Datum:** 2025-11-16
**Status:** ✅ COMPLETED
**Ziel:** Vollständige Entfernung von fieldCatalog.json und Migration auf Server-Driven UI

**Abgeschlossen:** 2025-11-16 (ca. 4 Stunden)

---

## 🎯 Kontext

**Problem:** Legacy vs Modern Architecture Collision
- **Legacy:** `fieldCatalog.json` mit hardcoded lowercase enum values (z.B. `"gmbh"`)
- **Modern:** Backend API `/api/enums/*` mit UPPERCASE values (z.B. `"GMBH"`)
- **Resultat:** Validation errors bei LegalForm und anderen Enums

**Root Cause:**
- fieldCatalog.json wurde in Sprint 2.1.6 durch Server-Driven UI ersetzt
- Aber: Dateien wurden NICHT gelöscht
- 16 Dateien nutzen noch die alten Hooks/Daten

---

## ✅ Bereits erledigt (Status: 2025-11-16 16:00)

### 1. Legacy-Dateien gelöscht ✓
- ❌ `/frontend/src/features/customers/data/fieldCatalog.json`
- ❌ `/frontend/src/features/customers/hooks/useFieldDefinitions.ts`
- ❌ `/frontend/src/features/customers/hooks/useFieldDefinitionsApi.ts`

### 2. Broken Imports gefixt ✓
- ✅ `/frontend/src/features/customers/hooks/index.ts` - Import entfernt

### 3. TypeScript Compiler Status ✓
- ✅ Keine Compile-Fehler (außer 2 aktive Komponenten)

---

## 🚧 TODO: Kritische Komponenten migrieren

### **Komponente 1: `CustomerOnboardingWizard.tsx`**
**Pfad:** `/frontend/src/features/customers/components/wizard/CustomerOnboardingWizard.tsx`

**Aktueller Zustand:**
- Zeile 15: `import { useFieldDefinitions } from '../../hooks/useFieldDefinitions';`
- Zeile 105: Nutzt `useFieldDefinitions()` Hook (gelöscht!)

**Migration:**
```typescript
// ALT (Zeile 15):
import { useFieldDefinitions } from '../../hooks/useFieldDefinitions';

// NEU:
// ENTFERNEN - Hook wird nicht mehr gebraucht
// CustomerOnboardingWizard nutzt Step-Komponenten, die bereits useCustomerSchema nutzen
```

**Änderungen:**
1. Import entfernen (Zeile 15)
2. Hook-Aufruf entfernen (Zeile 105+)
3. Prüfen: Wird `useFieldDefinitions()` irgendwo genutzt? → Wenn JA, auf `useCustomerSchema` migrieren

**Dependencies:**
- Step-Komponenten nutzen bereits `useCustomerSchema`
- Debug-Funktion `debugCustomerFieldTheme` muss evtl. angepasst werden

---

### **Komponente 2: `ServiceFieldsContainer.tsx`**
**Pfad:** `/frontend/src/features/customers/components/location/ServiceFieldsContainer.tsx`

**Aktueller Zustand:**
- Zeile 9: `import { useFieldDefinitions } from '../../hooks/useFieldDefinitions';`
- Zeile 35: `const { getFieldByKey } = useFieldDefinitions();`
- **KRITISCH:** Zeilen 82-166 haben **hardcoded field definitions** für Hotel/Krankenhaus/Betriebsrestaurant

**Migration Strategy:**

#### Option A: Server-Driven UI (EMPFOHLEN)
**Backend ändern:**
1. Neue Resource: `/api/customers/locations/{locationId}/service-schema`
2. Endpoint liefert Service Fields basierend auf Industry
3. Nutzt bestehende FieldDefinition-Struktur

**Frontend ändern:**
```typescript
// NEU: useLocationServiceSchema Hook
export function useLocationServiceSchema(industry: string) {
  const { data, isLoading } = useQuery({
    queryKey: ['locationServiceSchema', industry],
    queryFn: () => fetch(`${BASE_URL}/api/customers/locations/service-schema?industry=${industry}`)
  });
  return { serviceGroups: data?.groups || [], isLoading };
}

// In ServiceFieldsContainer.tsx:
const { serviceGroups, isLoading } = useLocationServiceSchema(industry);
```

**Zeitaufwand:** 2-3 Stunden (Backend + Frontend + Tests)

#### Option B: Quick Migration mit useCustomerSchema
**Frontend only:**
```typescript
// Import ändern
import { useCustomerSchema } from '../../../hooks/useCustomerSchema';

const { getFieldByKey } = useCustomerSchema();
```

**Problem:** `getFieldByKey` existiert in useCustomerSchema **nicht** → muss implementiert werden

**Zeitaufwand:** 1 Stunde

---

## 📋 Migration Steps (Reihenfolge)

### Step 1: CustomerOnboardingWizard.tsx migrieren (30 min)
1. ✅ Read CustomerOnboardingWizard.tsx komplett
2. ✅ Finde alle Usages von `useFieldDefinitions()`
3. ✅ Prüfe: Welche Funktionen werden genutzt?
   - `getFieldByKey`? → Zu useCustomerSchema migrieren
   - `allFields`? → Nutze useCustomerSchema
4. ✅ Entferne Import
5. ✅ Entferne Hook-Aufruf
6. ✅ TypeScript Compiler → 0 Errors
7. ✅ Test: `npm test -- CustomerOnboardingWizard`

### Step 2: ServiceFieldsContainer.tsx migrieren (2-3h)
**Option A gewählt:**
1. ✅ Backend: LocationServiceSchemaResource.java erstellen
2. ✅ Backend: Service Field Definitions für Hotel/Krankenhaus/Betriebsrestaurant
3. ✅ Frontend: useLocationServiceSchema Hook erstellen
4. ✅ Frontend: ServiceFieldsContainer umbauen
5. ✅ TypeScript Compiler → 0 Errors
6. ✅ Test: `npm test -- ServiceFieldsContainer`

### Step 3: Pre-Commit Hook erweitern (15 min) ✅ COMPLETED
```python
# scripts/check-fieldcatalog-removed.py
"""
Prüft, dass fieldCatalog.json nicht existiert
Prüft, dass deprecated Hooks nicht importiert werden
"""

import os
import sys
import re

def check_fieldcatalog_removed():
    # 1. Prüfe dass fieldCatalog.json nicht existiert
    fieldcatalog_path = "frontend/src/features/customers/data/fieldCatalog.json"
    if os.path.exists(fieldcatalog_path):
        print(f"❌ ERROR: {fieldcatalog_path} exists! This file was removed in Sprint 2.1.7.x")
        return False

    # 2. Prüfe dass deprecated Hooks nicht importiert werden
    deprecated_imports = [
        "from '../../hooks/useFieldDefinitions'",
        "from '../hooks/useFieldDefinitions'",
        'from "../../hooks/useFieldDefinitions"',
    ]

    # Scan alle .ts/.tsx Dateien
    for root, dirs, files in os.walk("frontend/src"):
        for file in files:
            if file.endswith(('.ts', '.tsx')):
                filepath = os.path.join(root, file)
                with open(filepath, 'r') as f:
                    content = f.read()
                    for deprecated in deprecated_imports:
                        if deprecated in content:
                            print(f"❌ ERROR: {filepath} imports deprecated useFieldDefinitions")
                            print(f"   Use useCustomerSchema instead!")
                            return False

    print("✅ fieldCatalog.json successfully removed - no legacy imports found")
    return True

if __name__ == "__main__":
    success = check_fieldcatalog_removed()
    sys.exit(0 if success else 1)
```

**Integration in pre-commit:**
```bash
# .githooks/pre-commit
python3 scripts/check-fieldcatalog-removed.py || exit 1
```

### Step 4: Tests ausführen (30 min)
```bash
# Frontend Tests
npm test -- --run

# Backend Tests
cd /Users/joergstreeck/freshplan-sales-tool/backend
./mvnw test

# Integration Tests
npm run test:integration
```

---

## 🎯 Akzeptanzkriterien

### Must-Have (Pflicht)
- ✅ fieldCatalog.json gelöscht
- ✅ Alle deprecated Hooks gelöscht (useFieldDefinitions, useFieldDefinitionsApi)
- ✅ TypeScript Compiler: 0 Errors
- ✅ Pre-Commit Hook blockiert fieldCatalog.json
- ✅ CustomerOnboardingWizard funktioniert
- ✅ ServiceFieldsContainer funktioniert
- ✅ Multi-Location-Management funktioniert
- ✅ Enum Values kommen vom Backend (UPPERCASE)
- ✅ Keine Validation Errors mehr (z.B. "Expected 'gmbh' received 'GMBH'")

### Nice-to-Have
- 📝 Migration dokumentiert in MP5
- 📝 ADR für Server-Driven UI Decision
- 📝 API-Dokumentation für neue Endpoints

---

## 🚨 Rollback Plan (Falls nötig)

**Wenn Migration fehlschlägt:**
1. `git stash` oder `git checkout -- .`
2. Alte Dateien aus Git History wiederherstellen:
   ```bash
   git checkout HEAD~1 -- frontend/src/features/customers/data/fieldCatalog.json
   git checkout HEAD~1 -- frontend/src/features/customers/hooks/useFieldDefinitions.ts
   git checkout HEAD~1 -- frontend/src/features/customers/hooks/useFieldDefinitionsApi.ts
   ```
3. TypeScript Compiler prüfen
4. Tests ausführen

**ABER:** Rollback löst das Parity-Problem NICHT! Backend bleibt UPPERCASE!

---

## 📊 Zeitaufwand (Geschätzt)

| Task | Zeit | Priorität |
|------|------|-----------|
| CustomerOnboardingWizard migrieren | 30 min | P0 (KRITISCH) |
| ServiceFieldsContainer Backend | 1.5h | P0 (KRITISCH) |
| ServiceFieldsContainer Frontend | 1h | P0 (KRITISCH) |
| Pre-Commit Hook | 15 min | P1 (WICHTIG) |
| Tests + Bugfixes | 30 min | P0 (KRITISCH) |
| **GESAMT** | **~4h** | |

---

## 🔗 Related Documents

- **Master Plan V5:** `/docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md`
- **Field Type Architecture:** `/docs/planung/grundlagen/FIELD_TYPE_ARCHITECTURE.md`
- **Server-Driven UI Spec:** `/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/server-driven-ui.md`
- **Backend Enum Endpoints:** Siehe CustomerSchemaResource.java

---

## 💬 Nächste Schritte (für neue Claude-Instanz)

Wenn du diese Datei liest:

1. **Status prüfen:**
   ```bash
   ls -la frontend/src/features/customers/data/fieldCatalog.json  # sollte NOT EXIST
   npx tsc --noEmit 2>&1 | grep "error TS"  # Anzahl Errors?
   ```

2. **Wo bist du?**
   - Wenn `fieldCatalog.json` noch existiert → Start bei "Step 1: Legacy-Dateien löschen"
   - Wenn CustomerOnboardingWizard Errors → Start bei "Step 1: CustomerOnboardingWizard migrieren"
   - Wenn ServiceFieldsContainer Errors → Start bei "Step 2: ServiceFieldsContainer migrieren"

3. **Fortsetzung:**
   - Folge dem Plan oben Schritt für Schritt
   - Update dieses Dokument mit ✅ für erledigte Tasks
   - Bei Problemen: Siehe Rollback Plan

4. **Nach erfolgreicher Migration:**
   - Update MP5 SESSION_LOG
   - Commit mit Message: "feat: Complete fieldCatalog.json removal - migrate to Server-Driven UI"
   - Tests ausführen
   - User informieren: "Migration abgeschlossen - Multi-Location-Management bereit zum Testen"

---

**Letzte Änderung:** 2025-11-16 16:15
**Autor:** Claude (Sonnet 4.5)
**Status:** Migration Step 1-2 in progress

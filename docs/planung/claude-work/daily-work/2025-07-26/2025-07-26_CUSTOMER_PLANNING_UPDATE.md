# 📋 CUSTOMER MANAGEMENT - PLANUNGSANPASSUNG

**Datum:** 26.07.2025 03:28  
**Autor:** Claude  
**Zweck:** Dokumentation der neuen Field-basierten Architektur  

## 🔄 Was wurde angepasst?

### 1. Neues Technisches Konzept erstellt
**Datei:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/2025-07-26_TECH_CONCEPT_customer-field-based-architecture.md`

**Kernänderungen:**
- ✅ Field-basierte Architektur statt starrer Entitäten
- ✅ Hybrid Field System (Global Catalog + Custom Fields später)
- ✅ Keine Legacy-Migration notwendig (Greenfield Ansatz)
- ✅ Branchenspezifische Field Sets
- ✅ Wizard-Flow mit 3 Steps beibehalten

### 2. NEXT_STEP.md aktualisiert
- Verweis auf neues technisches Konzept
- Field Catalog als ersten Implementierungsschritt
- Legacy-Analyse bleibt als Referenz

### 3. Master Plan V5 aktualisiert
- Arbeits-Dokument zeigt jetzt auf FC-005 Field-Based Architecture

### 4. TODO-System aktualisiert
- Alten TODO "Customer UI implementieren" ersetzt durch spezifische Field-basierte TODOs
- Neue TODOs:
  - FC-005 Customer Management Field-Based Architecture (in_progress)
  - Field Catalog JSON erstellen
  - CustomerOnboardingWizard implementieren

## 🏗️ Neue Architektur-Highlights

### Field Definition System
```typescript
interface FieldDefinition {
  key: string              // Eindeutiger Schlüssel
  label: string            // Anzeigename
  entityType: string       // customer|location|detailedLocation
  industry?: string[]      // Branchenfilter
  fieldType: string        // text|number|select|...
  validation?: string      // Zod Schema
  isCustom: boolean        // Catalog vs Custom
}
```

### MVP Field Catalog (10 Felder)
- Firmenname, Rechtsform, Branche
- Kettenkunde (Trigger!)
- Adresse (Straße, PLZ, Ort)
- Ansprechpartner (Name, E-Mail, Telefon)

### Implementierungsplan
1. **Phase 1:** Foundation (2 Tage)
   - Field System
   - Customer Step
   - Auto-Save

2. **Phase 2:** Advanced (3 Tage)
   - Location Step (branchenspezifisch)
   - Detailed Locations
   - Backend Integration

## 🔗 Wichtige Referenzen für neuen Claude

1. **Technisches Konzept (NEU):**  
   `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/2025-07-26_TECH_CONCEPT_customer-field-based-architecture.md`

2. **Legacy-Analyse (Referenz):**  
   `/Users/joergstreeck/freshplan-sales-tool/docs/claude-work/daily-work/2025-07-26/2025-07-26_CUSTOMER_COMPLETE_FIELD_ANALYSIS.md`

3. **Code Location:**  
   `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/`

## ✅ Konsistenz-Check

- [x] NEXT_STEP.md angepasst
- [x] Master Plan V5 angepasst
- [x] TODOs aktualisiert
- [x] Feature Branch erstellt: `feature/customer-field-based-ui`
- [x] Absolute Pfade überall verwendet
- [x] Kontext-optimiert für neuen Claude

## 🎯 Nächster konkreter Schritt

```bash
# 1. Field Catalog erstellen
cd /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/data
# fieldCatalog.json mit 10 MVP Feldern anlegen

# 2. Types definieren
cd ../types
# customer.types.ts mit FieldDefinition Interface
```

---

**WICHTIG:** Das neue technische Konzept ist die SINGLE SOURCE OF TRUTH für Customer Management!
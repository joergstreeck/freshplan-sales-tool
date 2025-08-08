# 🔧 Adaptive Theme Fix Plan - Sprint 2 Korrektur

**Datum:** 30.07.2025  
**Sprint:** 2 - Customer UI Integration  
**Status:** 🚨 Dringend - Behebung erforderlich  

---

## 📍 Navigation
**← Zurück:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  
**⬆️ Master Plan:** [V5 Complete Master Plan](/Users/joergstreeck/freshplan-sales-tool/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)  
**→ Implementierung:** [Adaptive Theme Implementation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/ADAPTIVE_THEME_IMPLEMENTATION.md)

---

## 🎯 Problem-Übersicht

### Identifizierte Probleme:
1. **UI-Elemente fehlen:** Labels, Pflichtfeld-Markierungen (*), Info-Icons
2. **Dynamisches Wachstum:** Felder passen sich nicht an Inhalt an
3. **Größen-Mappings:** Werden nicht korrekt angewendet
4. **Info-Boxen:** Filialunternehmen-Hinweis fehlt

### Screenshot-Analyse:
- Alle Felder sind volle Breite statt adaptiv
- Keine visuellen Hinweise (Pflichtfelder, Help-Texte)
- Layout nutzt nicht das Flexbox-System

---

## 📋 Lösungsplan in 4 Schritten

### Schritt 1: FieldWrapper Integration (30 Min)
**Datei:** `/frontend/src/features/customers/components/adaptive/AdaptiveField.tsx`

**Problem:** AdaptiveField zeigt keine Labels, Pflichtfeld-Markierungen oder Info-Icons

**Lösung:**
1. AdaptiveField soll NUR das Input-Feld rendern
2. FieldWrapper übernimmt Label, Pflichtfeld (*), Info-Icons
3. DynamicFieldRenderer wrapped AdaptiveField mit FieldWrapper

**Code-Änderung:** Siehe [Implementierungs-Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/ADAPTIVE_THEME_IMPLEMENTATION.md#step1)

---

### Schritt 2: Dynamisches Wachstum aktivieren (45 Min)
**Dateien:** 
- `/frontend/src/features/customers/components/adaptive/AdaptiveField.tsx`
- `/frontend/src/features/customers/utils/adaptiveFieldCalculator.ts`

**Problem:** Felder messen Text, aber Width wird nicht angewendet

**Lösung:**
1. AdaptiveField nutzt `adaptiveFieldCalculator` richtig
2. CSS-Width wird korrekt auf TextField angewendet
3. Min/Max-Breiten aus Theme werden respektiert

**Details:** [Implementierungs-Guide Schritt 2](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/ADAPTIVE_THEME_IMPLEMENTATION.md#step2)

---

### Schritt 3: Größen-System korrigieren (30 Min)
**Dateien:**
- `/frontend/src/features/customers/components/fields/DynamicFieldRenderer.tsx`
- `/frontend/src/features/customers/utils/fieldSizeCalculator.ts`

**Problem:** sizeHint existiert nicht, fieldSizeCalculator wird ignoriert

**Lösung:**
1. DynamicFieldRenderer nutzt `getFieldSize()` für ALLE Felder
2. Größen-Klassen werden korrekt auf Container angewendet
3. Mapping von fieldSizeCalculator wird verwendet

**Details:** [Implementierungs-Guide Schritt 3](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/ADAPTIVE_THEME_IMPLEMENTATION.md#step3)

---

### Schritt 4: Info-Boxen & Wizard-Konsistenz (15 Min)
**Datei:** `/frontend/src/features/customers/components/steps/CustomerDataStep.tsx`

**Problem:** Filialunternehmen-Info fehlt, Wizard-Schritte inkonsistent

**Lösung:**
1. Info-Box für Filialunternehmen wieder einbauen
2. Wizard-Schritte bleiben bei 3: Kundendaten → Standorte → Details
3. Konsistente Verwendung des Adaptive Theme

**Details:** [Implementierungs-Guide Schritt 4](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/ADAPTIVE_THEME_IMPLEMENTATION.md#step4)

---

## ✅ Erwartetes Ergebnis

Nach Implementierung:
1. **Felder mit korrekten Größen:** PLZ klein, Firmenname groß
2. **Alle UI-Elemente sichtbar:** Labels, *, Info-Icons
3. **Dynamisches Wachstum:** Felder passen sich an Inhalt an
4. **Intelligenter Umbruch:** Felder ordnen sich in Zeilen an
5. **Info-Boxen:** Hinweise für Filialunternehmen

---

## 🚀 Quick Start für Claude

```bash
# 1. Öffne Implementierungs-Guide
cat /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/ADAPTIVE_THEME_IMPLEMENTATION.md

# 2. Führe Schritte 1-4 aus
# 3. Teste im Browser: http://localhost:5173/customers
```

---

## 📚 Referenzen

- **Field Theme Konzept:** [FIELD_THEME_SYSTEM_PROTOTYPE.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/FIELD_THEME_SYSTEM_PROTOTYPE.md)
- **Sprint 2 Overview:** [README.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)
- **Field Catalog:** [fieldCatalog.json](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/data/fieldCatalog.json)
# 📝 Wizard-Struktur Final - Sprint 2

**Datum:** 30.07.2025  
**Sprint:** 2 - Customer UI Integration  
**Status:** ✅ Finale Definition  

---

## 📍 Navigation
**← Zurück:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  
**⬆️ Master Plan:** [V5 Complete Master Plan](/Users/joergstreeck/freshplan-sales-tool/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)

---

## 🎯 Finale Wizard-Struktur

### Überblick: 3-Schritte-Prozess

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│ 1. Kundendaten  │ --> │  2. Standorte   │ --> │   3. Details    │
│                 │     │                 │     │ (je Standort)   │
└─────────────────┘     └─────────────────┘     └─────────────────┘
     PFLICHT              BEDINGT/OPTIONAL         OPTIONAL
```

---

## 📋 Schritt 1: Kundenstammdaten

**Zweck:** Erfassung der Hauptdaten des Unternehmens

### Felder mit Adaptive Theme:
```
[Kundennummer:klein]    [Firmenname:groß]               
[Rechtsform:klein]      [Branche:klein]      [Filial:klein]
[PLZ:kompakt]          [Ort:groß]
[Straße:groß]          [Hausnummer:kompakt]
[E-Mail:groß]
[Telefon:mittel]       [Ansprechpartner:mittel]
```

### Wichtige Elemente:
- ✅ Pflichtfeld-Markierungen (*)
- ✅ Info-Icons mit helpText
- ✅ Adaptive Feldgrößen
- ✅ Dynamisches Wachstum

### Conditional Info:
```tsx
{customerData.chainCustomer === 'ja' && (
  <Alert severity="info">
    Sie haben angegeben, dass es sich um ein Filialunternehmen handelt. 
    Im nächsten Schritt können Sie die einzelnen Standorte erfassen.
  </Alert>
)}
```

---

## 📋 Schritt 2: Standorte

**Zweck:** Erfassung von Filialen/Standorten

### Anzeige-Logik:
- **Einzelunternehmen:** Schritt wird übersprungen
- **Filialunternehmen:** Pflicht (mind. Hauptstandort)

### UI-Konzept:
```
┌─────────────────────────────────────────┐
│ Standorte erfassen                      │
├─────────────────────────────────────────┤
│ ☑ Hauptstandort                         │
│   Musterfirma GmbH, Berlin              │
│                                         │
│ ☐ Filiale Hamburg                       │
│   Musterstraße 1, 20095 Hamburg        │
│                                         │
│ [+ Standort hinzufügen]                 │
└─────────────────────────────────────────┘
```

### Features:
- Übersichtliche Liste aller Standorte
- Hauptstandort immer zuerst
- Unbegrenzte Anzahl von Filialen
- Quick-Add für neue Standorte

---

## 📋 Schritt 3: Details je Standort

**Zweck:** Optionale Vertiefung pro Standort

### Anzeige-Logik:
- Optional für alle Standorte
- Kann später nachgepflegt werden
- Nicht blockierend für Wizard-Abschluss

### Mögliche Detail-Felder:
- Öffnungszeiten
- Ansprechpartner vor Ort
- Spezielle Lieferbedingungen
- Notizen zum Standort

---

## 🎨 Theme-Integration

### Adaptive Layout überall:
1. **Alle Steps nutzen:** `CustomerFieldThemeProvider`
2. **Felder wachsen:** Basierend auf Inhalt
3. **Intelligenter Umbruch:** Bei Platzmangel
4. **Mobile-First:** Alles stapelt sich auf Smartphones

### Konsistenz:
- Gleiche Feldgrößen-Logik in allen Steps
- Einheitliche Fehlerbehandlung
- Durchgängige UI-Sprache (Deutsch)

---

## ✅ Zusammenfassung

**Der Wizard bleibt bei 3 Schritten:**
1. **Kundendaten** - Immer erforderlich
2. **Standorte** - Bei Filialunternehmen
3. **Details** - Optional für Vertiefung

**Mit Adaptive Theme:**
- Felder passen sich intelligent an
- Professionelle Mindestgrößen
- Optimales Touch-Target (44px)
- Freshfoodz CI konform

---

## 🔗 Weiterführende Dokumente

- **Theme Fix:** [ADAPTIVE_THEME_FIX_PLAN.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/ADAPTIVE_THEME_FIX_PLAN.md)
- **Implementation:** [ADAPTIVE_THEME_IMPLEMENTATION.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/ADAPTIVE_THEME_IMPLEMENTATION.md)
- **Field Catalog:** [fieldCatalog.json](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/data/fieldCatalog.json)
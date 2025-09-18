# 📋 Sprint 2 V3 - Finale Best Practice Struktur

**Status:** 🆕 FINALE VERSION  
**Datum:** 31.07.2025  
**Basis:** Enterprise CRM Best Practices (Salesforce, SAP, HubSpot)

---

## 📍 Navigation
**← Zurück:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  
**→ Wizard V3:** [Wizard Structure V3](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/WIZARD_STRUCTURE_V3.md)  
**⭐ Master Plan:** [V5 Complete](/Users/joergstreeck/freshplan-sales-tool/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)

---

## 🎯 Die finale 4-Step Struktur

### Kernprinzipien:
1. **Global vor Lokal** - Erst Unternehmensstrategie, dann Standort-Details
2. **Eine Ebene pro Step** - Keine mentalen Kontextwechsel
3. **Zukunftssicher** - Einzelbetrieb = Kette mit 1 Standort
4. **Best Practice** - Bewährte Enterprise CRM Patterns

---

## 📊 Wizard Steps im Detail

### Step 1: Basis & Filialstruktur ✅
**Unverändert** - Funktioniert bereits optimal
- Firmenname, Branche, Kundentyp
- Bei Ketten: Standorte erfassen
- Bei Einzelbetrieb: Automatisch 1 Standort

### Step 2: Herausforderungen & Potenzial 🔄
**NEU: NUR globale Themen**
- Pain Points (unternehmensweite Herausforderungen)
- Geplantes Jahresvolumen (EUR-formatiert, großes Feld)
- Zusatzgeschäft (Vending/Automaten)
- **ENTFERNT:** LocationSelector & LocationServices

### Step 3: Ansprechpartner 🔄
**ERWEITERT um Standort-Zuordnung**
- Hauptansprechpartner
- Weitere Kontakte
- **NEU:** Zuständigkeitsbereich (alle/bestimmte Standorte)

### Step 4: Angebot & Leistungen je Filiale 🆕
**KOMPLETT NEU**
- Standort-Navigation mit Progress
- Service-Details pro Standort
- "Kopieren" und "Für alle übernehmen" Features
- Einzelbetrieb: Zeigt direkt Services (ohne Navigation)

---

## 🔧 Technische Umsetzung

### Datenmodell-Anpassungen:

```typescript
// Customer bleibt gleich, aber:
interface Customer {
  expectedAnnualRevenue: number; // NEU: EUR-formatiert
  // LocationServices ENTFERNT aus Customer
}

// Neue Struktur für Step 4:
interface LocationServiceData {
  locationId: string;
  // Hotel-spezifisch
  offersBreakfast?: boolean;
  breakfastWarm?: boolean;
  breakfastGuestsPerDay?: number;
  // ... weitere Services
}

// Store-Erweiterung:
interface CustomerOnboardingStore {
  // Step 4 State
  locationServices: Record<string, LocationServiceData>;
  currentLocationIndex: number;
  completedLocationIds: string[];
}
```

### Backend-Anpassungen:

```java
// CustomerLocation Entity erweitern
@Entity
public class CustomerLocation {
    // Bestehende Felder...
    
    // NEU: Service-Felder
    @Column(name = "offers_breakfast")
    private Boolean offersBreakfast;
    
    @Column(name = "breakfast_warm")
    private Boolean breakfastWarm;
    
    @Column(name = "breakfast_guests_per_day")
    private Integer breakfastGuestsPerDay;
    
    // ... weitere Service-Felder
}
```

**Migration:** V7__add_location_services.sql

---

## 💰 EUR-Feld Spezifikation

```json
{
  "key": "expectedAnnualRevenue",
  "label": "Geplantes Jahresvolumen Freshfoodz (EUR)",
  "fieldType": "number",
  "format": "currency",
  "currency": "EUR",
  "thousandSeparator": true,
  "width": "large",
  "fontSize": "18px",
  "textAlign": "right",
  "min": 0,
  "validationWarning": 1000000,
  "tooltip": "Brutto-Jahresvolumen an Freshfoodz-Produkten, das gemeinsam geplant wird. Dieser Wert erscheint 1:1 im Partnerschaftsvertrag.",
  "showCalculator": true,
  "calculatorHint": "100 Betten × 10€/Monat × 12 = 120.000€"
}
```

### Live-Formatierung:
- Input: `95000` → Display: `95.000 €`
- Während der Eingabe formatieren
- Kopieren: Nur Zahl ohne Formatierung

---

## 🎨 UI/UX mit AdaptiveFormContainer

### Alle Steps nutzen das bewährte Theme-System:
- Automatische Dropdown-Breiten
- Responsive Flexbox Layouts
- Field Size Hints (klein/mittel/groß)
- Info-Tooltips bei Bedarf

### Step 4 Spezial-Features:
```
┌─────────────────────────────────────────────┐
│ Schritt 4: Angebot & Leistungen je Filiale   │
├─────────────────────────────────────────────┤
│                                              │
│ 📍 Standort-Fortschritt                      │
│ ████████░░░░░░░ 3 von 8 erfasst            │
│                                              │
│ Aktueller Standort:                          │
│ [Berlin Hauptsitz ▼] [Kopieren von ▼]       │
│                                              │
│ ☑ Für alle restlichen Standorte übernehmen  │
└─────────────────────────────────────────────┘
```

---

## ✅ Vorteile der finalen Struktur

1. **Kognitiv optimal**: Ein Thema pro Step
2. **Vertriebsgerecht**: Nach Step 3 kann Vertrieb arbeiten
3. **Datenqualität**: Höhere Completion-Rate
4. **Zukunftssicher**: Einzelbetrieb → Kette ohne Änderung
5. **Best Practice**: Bewährt in Enterprise CRMs

---

## 🚀 Implementierungsreihenfolge

1. **EUR-Feld** (TODO-14): Quick Win mit sofortigem Mehrwert
2. **Step 2 bereinigen** (TODO-15): Location-Komponenten entfernen
3. **Step 3 erweitern** (TODO-16): Standort-Zuordnung hinzufügen
4. **Step 4 neu** (TODO-17): Kompletter neuer Step
5. **Store anpassen** (TODO-18): Location-Services verschieben

---

## 📋 Migration von V2 zu V3

### Was ändert sich:
1. Step 2 wird schlanker (nur globale Themen)
2. Step 3 bekommt Standort-Zuordnung
3. Step 4 ist komplett neu

### Was bleibt:
1. Step 1 unverändert
2. AdaptiveFormContainer überall
3. Store-Struktur (nur erweitert)
4. Backend-Entities (nur erweitert)

---

## 🔗 Weiterführende Dokumente

**Implementierung:**
- [Step 2 V3 Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP2_HERAUSFORDERUNGEN_POTENZIAL_V3.md)
- [Step 3 V2 Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP3_ANSPRECHPARTNER_V2.md)
- [Step 4 Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP4_ANGEBOT_SERVICES.md)

**Backend:**
- [Entity Extensions V2](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/backend/ENTITY_EXTENSIONS_V2.md)
- [API Endpoints V2](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/backend/API_ENDPOINTS_V2.md)

**Theme System:**
- [Adaptive Layout Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/ADAPTIVE_LAYOUT_IMPLEMENTATION_GUIDE.md)
# FC-002-M8: Rechner (Calculator) - ⚠️ VERALTET

**Modul:** M8
**Feature:** FC-002
**Status:** ⚠️ **OBSOLET - Neue Rabattlogik ab 01.10.2025**
**Geschätzter Aufwand:** ~~Backend: 0.5 Tage | Frontend: 1.5 Tage~~ **NICHT MEHR RELEVANT**
**Abhängigkeit:** Keine
**Letztes Update:** 17.09.2025 - Als VERALTET markiert

## ⚠️ WICHTIGER HINWEIS

**Dieses Modul beschreibt den ALTEN Calculator, der nicht mehr verwendet wird!**

Die Rabattlogik wurde komplett überarbeitet:
- **ALT (bis 30.09.2025):** Bestellwert-basierte Rabatte mit Vorlaufzeit
- **NEU (ab 01.10.2025):** Jahresumsatz-basierte Rabattstufen

**Aktuelle Rabattlogik siehe:** `/docs/business/rabattlogik_2025_NEU.md`

---

**Der folgende Inhalt ist nur noch zur Dokumentation des alten Systems:**

## 📊 Kompakte Modul-Analyse

### Backend-Analyse

**Was ist vorhanden?**
```
backend/src/main/java/de/freshplan/
├── api/
│   └── CalculatorResource.java      # REST Endpoint
└── domain/calculator/
    └── service/
        ├── CalculatorService.java   # Business Logic
        └── dto/
            ├── CalculatorRequest.java
            └── CalculatorResponse.java
```

**Code-Qualität:** ⭐⭐⭐⭐ (Sehr gut)
- Saubere Architektur mit klarer Trennung
- Gut dokumentierte Business-Regeln (Rabatte, Frühbucher)
- Keine Abhängigkeiten zu anderen Modulen

**Wiederverwendbarkeit:** 95%
- API kann 1:1 übernommen werden
- Evtl. kleine Anpassungen für neue UI-Features

### Frontend-Analyse

**Was ist vorhanden?**
```
frontend/src/features/calculator/
├── components/
│   ├── CalculatorForm.tsx          # Input-Formular
│   ├── CalculatorResults.tsx       # Ergebnis-Anzeige
│   └── OriginalCalculator.tsx      # Legacy-Version
├── api/
│   ├── calculatorApi.ts           # API-Client
│   └── calculatorQueries.ts       # React Query Hooks
├── store/
│   └── calculatorStore.ts         # Zustand-Management
└── pages/
    └── CalculatorPage.tsx         # Hauptseite
```

**Code-Qualität:** ⭐⭐⭐ (Gut mit Verbesserungspotential)
- Mischung aus altem und neuem Code
- React Query bereits implementiert
- Store vorhanden und funktional
- CSS muss auf MUI migriert werden

**Wiederverwendbarkeit:** 70%
- API-Layer und Store können übernommen werden
- UI-Komponenten müssen auf MUI migriert werden
- Form-Handling ist bereits gut strukturiert

## 🎯 Anpassungen für FC-002

### Backend (0.5 Tage)
1. **API-Erweiterungen:**
   - [ ] Batch-Calculation Endpoint für mehrere Szenarien
   - [ ] Calculation-History speichern (optional)
   - [ ] PDF-Export direkt aus Backend

### Frontend (1.5 Tage)
1. **UI-Migration:**
   - [ ] CalculatorForm.tsx → MUI-Version
   - [ ] CalculatorResults.tsx → MUI DataGrid
   - [ ] Responsive Layout für Cockpit-Integration

2. **Neue Features:**
   - [ ] Szenario-Vergleich (Side-by-Side)
   - [ ] Quick-Calculator als Dialog/Drawer
   - [ ] Integration in Aktions-Center (Spalte 3)

3. **Code-Cleanup:**
   - [ ] OriginalCalculator.tsx entfernen
   - [ ] CSS → MUI sx-Props
   - [ ] TypeScript strict mode

## 🚀 Empfehlung

**✅ WIEDERVERWENDEN mit moderaten Anpassungen**

**Begründung:**
- Solide Backend-Implementierung
- Frontend hat gute Basis-Struktur
- Hauptaufwand liegt in UI-Migration zu MUI

**Priorität:** MITTEL - Kann nach M3/M5 angegangen werden

## 📋 Migrations-Strategie

### Phase 1: Backend-Erweiterungen (0.5 Tage)
- Batch-Endpoint implementieren
- Bestehende API testen und dokumentieren

### Phase 2: Frontend-Migration (1 Tag)
- Form und Results auf MUI migrieren
- In Cockpit-Architektur integrieren
- Store-Integration prüfen

### Phase 3: Neue Features (0.5 Tage)
- Szenario-Vergleich
- Quick-Calculator
- Polish & Tests

**Gesamtaufwand:** 2 Personentage

## 🎨 Visueller Migrationsplan (NEU - 09.07.2025)

### 🚨 KRITISCHES UPDATE: CSS-Konflikt-Analyse

**Risiko-Stufe:** 🔴 HOCH

Nach eingehender Analyse wurden **675 Zeilen Legacy-CSS** identifiziert:
- `calculator-layout.css` (376 Zeilen) - Eigenes Grid-System
- `calculator-components.css` (238 Zeilen) - Custom Komponenten
- `calculator.css` (61 Zeilen) - Globale Overrides

**Hauptprobleme:**
1. **Grid-System kollidiert** mit MainLayoutV2
2. **Position: fixed** Elemente brechen aus Layout aus
3. **Custom Scrollbars** konfligieren mit MUI
4. **Z-Index Chaos** mit globalen Werten

### 📐 Migration-Strategie: Clean Slate

**Neuer Aufwand:** 3-4 Tage (statt 1.5 Tage)

#### Phase 1: Parallel-Route (Tag 1)
```typescript
// Neue Route: /calculator-v2
<Route path="/calculator-v2" element={<CalculatorPageV2 />} />

// CalculatorPageV2.tsx
export function CalculatorPageV2() {
  return (
    <MainLayoutV2>
      <CalculatorViewV2 />
    </MainLayoutV2>
  );
}
```

#### Phase 2: MUI-Komponenten (Tag 2-3)
```typescript
// Alte CSS-basierte Komponente
<div className="calculator-form">
  <div className="form-group">...</div>
</div>

// Neue MUI-basierte Komponente
<Paper sx={{ p: 3 }}>
  <Grid2 container spacing={3}>
    <Grid2 size={12}>
      <TextField fullWidth label="..." />
    </Grid2>
  </Grid2>
</Paper>
```

#### Phase 3: Layout-Integration (Tag 4)
- Entfernung aller CSS-Imports
- Integration in MainLayoutV2
- Responsive Design mit MUI Breakpoints

### 🖼️ Visuelle Referenzen

**Aktueller Zustand:** [Screenshot erforderlich]
- Grid-basiertes Layout mit fixen Breiten
- Custom-styled Inputs und Buttons
- Eigene Tabellen-Komponente

**Ziel-Design mit MUI:**
- Material Design Paper für Sections
- MUI DataGrid für Ergebnistabelle
- Consistent Spacing mit theme.spacing()
- Mobile-First Responsive Design

### ⚡ Performance-Überlegungen

**Problem:** CSS-in-JS bei vielen Berechnungen
**Lösung:** 
- Memoization für berechnete Styles
- Static styled-components wo möglich
- Debouncing für Eingabefelder
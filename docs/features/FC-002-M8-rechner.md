# FC-002-M8: Rechner (Calculator)

**Modul:** M8  
**Feature:** FC-002  
**Status:** 🔍 Analyse durchgeführt (NEU)  
**Geschätzter Aufwand:** Backend: 0.5 Tage | Frontend: 1.5 Tage  
**Abhängigkeit:** Keine  
**Letztes Update:** 09.07.2025 - Kompakte Analyse durchgeführt

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
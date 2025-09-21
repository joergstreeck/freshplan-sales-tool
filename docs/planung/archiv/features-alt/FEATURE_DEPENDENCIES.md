# 🚨 Feature Dependencies & Layout-Migrations-Risikoanalyse

**Dokument:** Feature Dependencies & Risikoanalyse  
**Datum:** 09.07.2025  
**Autor:** Claude  
**Status:** KRITISCH - Vor Implementierung beachten!

## 🎯 Executive Summary

Die Analyse zeigt **kritische CSS-Konflikte** in mehreren Modulen, die eine sorgfältige Migrationsstrategie erfordern:

1. **Calculator (M8)**: 675 Zeilen Legacy-CSS - **HÖCHSTES RISIKO**
2. **Customer Management (M5)**: Gemischte CSS/MUI-Nutzung - **MITTLERES RISIKO**
3. **Cockpit (M3)**: Bereits migriert, aber Abhängigkeiten zu anderen Modulen
4. **Settings (M7)**: Minimal betroffen - **GERINGES RISIKO**

## 📊 Risiko-Matrix nach Modulen

### 🔴 HOCH - Calculator (M8)
**CSS-Komplexität:** 675 Zeilen über 3 Dateien
- `calculator-layout.css` (376 Zeilen) - Grid-basiertes Layout
- `calculator-components.css` (238 Zeilen) - Komponenten-Styles
- `calculator.css` (61 Zeilen) - Globale Styles

**Kritische Konflikte:**
- Verwendet eigenes Grid-System (kollidiert mit MainLayoutV2)
- Globale CSS-Variablen überschreiben Theme
- Position: fixed Elemente brechen aus Layout aus
- Custom Scrollbar-Styles

**Migrations-Aufwand:** 3-4 Tage (statt geplante 1.5 Tage)

### 🟡 MITTEL - Customer Management (M5)
**CSS-Komplexität:** Gemischt (CSS + CSS Modules)
- `CustomerList.css` - Direkte CSS-Imports
- `CustomerList.module.css` - CSS Modules (besser isoliert)

**Kritische Konflikte:**
- Tabellen-Layout mit custom Styles
- Eigene Scrollbar-Implementation
- Z-Index Konflikte mit Modals

**Migrations-Aufwand:** 2-3 Tage für Frontend

### 🟢 GERING - Settings (M7)
**CSS-Komplexität:** Minimal
- Nutzt hauptsächlich MUI-Komponenten
- Kaum eigene CSS-Styles

**Migrations-Aufwand:** 0.5-1 Tag

### ✅ ABGESCHLOSSEN - Cockpit (M3)
**Status:** Phase 1 der Migration abgeschlossen
- Clean-Slate Ansatz mit MainLayoutV2
- Parallele Route `/cockpit-v2` implementiert

## 🔗 Modul-Abhängigkeiten

### Direkte Layout-Abhängigkeiten:
```
MainLayoutV2
├── Cockpit (M3)
│   ├── FocusListColumn → Customer Management (M5)
│   └── AktionsCenter → Calculator (M8)
├── Customer Management (M5)
│   └── CustomerDetail → Calculator (M8)
└── Settings (M7) - unabhängig
```

### Kritische Abhängigkeiten:
1. **Calculator ↔ Customer**: Der Calculator wird oft im Customer-Detail eingebettet
2. **Cockpit → Alle**: Als Hauptarbeitsbereich muss das Cockpit alle Module integrieren können
3. **MainLayoutV2 → Alle**: Jedes Modul muss mit dem neuen Layout kompatibel sein

## 🚀 Empfohlene Migrations-Reihenfolge

### Phase 1: Foundation (✅ Abgeschlossen)
- MainLayoutV2 implementiert
- Cockpit-v2 als Proof of Concept

### Phase 2: Isolierte Module (Nächster Schritt)
1. **Settings (M7)** - Einfachster Fall, gutes Übungsmodul
   - Zeitaufwand: 1 Tag
   - Risiko: Gering
   
### Phase 3: Core Business Module
2. **Calculator (M8)** - Höchste Priorität wegen Komplexität
   - Zeitaufwand: 4 Tage
   - Risiko: Hoch
   - Strategie: Komplett-Neubau mit MUI

### Phase 4: Integration
3. **Customer Management (M5)** - Nach Calculator, da Abhängigkeiten
   - Zeitaufwand: 3 Tage
   - Risiko: Mittel

### Phase 5: Finalisierung
4. **Cockpit (M3)** - Integration aller migrierten Module
   - Zeitaufwand: 2 Tage
   - Risiko: Gering (da schrittweise)

## 🛡️ Migrations-Strategien nach Risiko

### Für High-Risk Module (Calculator):
1. **Parallel-Entwicklung** wie bei Cockpit
   - Neue Route `/calculator-v2`
   - Schrittweise Feature-Migration
   - A/B Testing möglich

2. **Component-by-Component Migration**
   - Erst Form-Komponenten
   - Dann Results-Komponenten
   - Zuletzt Layout-Integration

### Für Medium-Risk Module (Customer):
1. **Hybrid-Ansatz**
   - CSS Modules beibehalten wo möglich
   - Nur globale CSS entfernen
   - Schrittweise MUI-Migration

### Für Low-Risk Module (Settings):
1. **Direct Migration**
   - Direkt auf MUI umstellen
   - Keine Parallel-Route nötig

## 📋 Konkrete nächste Schritte

1. **Visueller Migrationsplan** für Calculator erstellen
2. **Screenshot-Analyse** der aktuellen Calculator-UI
3. **MUI-Komponenten-Mapping** erstellen
4. **Proof of Concept** für Calculator-v2 Route

## ⚠️ Kritische Erkenntnisse

1. **CSS-in-JS Performance**: Bei Calculator mit vielen dynamischen Berechnungen kritisch
2. **Bundle Size**: Calculator + Customer + alte CSS = +200KB über Budget
3. **Migration Fatigue**: 4 Module parallel zu pflegen ist riskant
4. **Testing Overhead**: Jede Parallel-Route braucht eigene Tests

## 📊 Gesamt-Aufwandsschätzung (Neu)

| Modul | Original | Neu | Differenz | Grund |
|-------|----------|-----|-----------|--------|
| M1 Layout | 2-3 Tage | ✅ Done | - | Abgeschlossen |
| M3 Cockpit | 2-3 Tage | 2 Tage | -1 Tag | Grundarbeit erledigt |
| M5 Customer | 2 Tage | 3 Tage | +1 Tag | CSS-Komplexität |
| M7 Settings | 3 Tage | 1 Tag | -2 Tage | Bereits MUI-ready |
| M8 Calculator | 2 Tage | 4 Tage | +2 Tage | Massive CSS-Migration |
| **GESAMT** | **11-13 Tage** | **10 Tage** | -1 bis -3 Tage | Optimierte Reihenfolge |

## 🎯 Empfehlung

**STOPP der parallelen Entwicklung!** 

Fokussierung auf sequenzielle Migration in der empfohlenen Reihenfolge. Dies reduziert:
- Komplexität der Parallel-Pflege
- Testing-Overhead
- Bundle Size während Migration
- Team-Verwirrung

**Nächster konkreter Schritt:** Settings (M7) als "Quick Win" migrieren, um Momentum zu gewinnen.
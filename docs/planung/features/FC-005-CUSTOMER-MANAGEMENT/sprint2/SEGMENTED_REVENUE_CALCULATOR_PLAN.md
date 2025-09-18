# 📊 Segmentierte Kalkulationshilfe - Implementierungsplan

**Status:** 🆕 Neu zu implementieren  
**Datum:** 31.07.2025  
**Sprint:** Sprint 2 - Customer UI Integration

---

## 📍 Navigation
**← Sprint 2 Übersicht:** [README](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  
**→ Step 2 Struktur:** [STEP2_HERAUSFORDERUNGEN_POTENZIAL_V3](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP2_HERAUSFORDERUNGEN_POTENZIAL_V3.md)

---

## 🎯 Übersicht

### Ziel
Ersetzen der aktuellen einfachen Kalkulationshilfe durch eine segmentierte, transparente Berechnung basierend auf Hotelkategorien und Service-Bereichen.

### Kernprinzipien
1. **Pauschale Erfassung in Step 2** - Grobe Struktur der Hotelkette
2. **Transparente Berechnung** - Nachvollziehbare Segmente
3. **Überschreibbar** - Nutzer kann finalen Wert anpassen
4. **Verfeinerung in Step 4** - Details pro Standort später

---

## 💡 Geschäftslogik

### Kalkulationsparameter (festgelegt)
```
- Frühstück: 1,50 € pro Gast
- À la carte: 6,00 € pro Essen  
- Bankett: 7,00 € pro Person
- Roomservice: 6,00 € pro Bestellung
```

### Pauschale Jahreswerte nach Hotelgröße

| Hotelgröße | Frühstück | À la carte | Bankett | Roomservice | Gesamt |
|------------|-----------|------------|---------|-------------|---------|
| Klein (<50 Zimmer) | 15.000 € | 25.000 € | 5.000 € | 2.000 € | 47.000 € |
| Mittel (50-120) | 40.000 € | 80.000 € | 10.000 € | 5.000 € | 135.000 € |
| Groß (>120) | 80.000 € | 180.000 € | 25.000 € | 10.000 € | 295.000 € |

---

## 🔧 Technische Umsetzung

### 1. Neue Komponente erstellen

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/calculator/SegmentedRevenueCalculator.tsx`

```typescript
interface SegmentedRevenueCalculatorProps {
  currentValue: number;
  onApplyCalculation: (value: number) => void;
}

interface HotelSegment {
  size: 'small' | 'medium' | 'large';
  count: number;
  services: {
    breakfast: boolean;
    alacarte: boolean;
    banquet: boolean;
    roomservice: boolean;
  };
}
```

### 2. UI-Struktur

```
┌─────────────────────────────────────────────┐
│ 💡 Intelligente Potenzialberechnung         │
├─────────────────────────────────────────────┤
│ ℹ️ Erfassen Sie grob Ihre Hotelstruktur.   │
│ Die genaue Aufschlüsselung erfolgt später  │
│ pro Standort.                               │
├─────────────────────────────────────────────┤
│ Klein (<50 Zimmer)                          │
│ Anzahl: [___] ☐ Frühstück ☐ À la carte    │
│               ☐ Bankett   ☐ Roomservice    │
│ → Potenzial: 47.000 €                       │
├─────────────────────────────────────────────┤
│ Mittel (50-120 Zimmer)                      │
│ Anzahl: [___] ☐ Frühstück ☐ À la carte    │
│               ☐ Bankett   ☐ Roomservice    │
│ → Potenzial: 270.000 €                      │
├─────────────────────────────────────────────┤
│ Groß (>120 Zimmer)                          │
│ Anzahl: [___] ☐ Frühstück ☐ À la carte    │
│               ☐ Bankett   ☐ Roomservice    │
│ → Potenzial: 0 €                            │
├─────────────────────────────────────────────┤
│ Geschätztes Jahrespotenzial: 317.000 €      │
│ → für Partnerschaft: 320.000 €              │
│                          [Übernehmen]        │
└─────────────────────────────────────────────┘
```

### 3. Integration in Step 2

**Änderung in:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/steps/Step2HerausforderungenPotenzialV3.tsx`

```typescript
// Alte RevenueCalculator importieren entfernen
// import { RevenueCalculator } from '../fields/RevenueCalculator';

// Neue importieren
import { SegmentedRevenueCalculator } from '../calculator/SegmentedRevenueCalculator';
```

**Änderung in:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/fields/EURInput.tsx`

```typescript
// Kalkulationshilfe-Anzeige anpassen
{showCalculator && isFocused && (
  <Box onMouseDown={(e) => e.preventDefault()}>
    <SegmentedRevenueCalculator 
      currentValue={value || 0}
      onApplyCalculation={handleApplyCalculation}
    />
  </Box>
)}
```

### 4. Alte Kalkulationshilfe deaktivieren

Die bisherige `RevenueCalculator.tsx` wird nicht gelöscht, aber nicht mehr verwendet. Sie bleibt als Referenz erhalten.

---

## 📋 Implementierungs-Checkliste

- [ ] SegmentedRevenueCalculator.tsx erstellen
- [ ] Berechnungslogik mit festen Werten implementieren
- [ ] UI mit Material-UI Komponenten bauen
- [ ] Transparente Berechnung anzeigen
- [ ] Rundung auf 10.000 € implementieren
- [ ] Integration in EURInput.tsx
- [ ] Alte RevenueCalculator-Imports entfernen
- [ ] Testing der neuen Komponente
- [ ] Validierung der Berechnungen

---

## 🎨 UI/UX Details

### Interaktionslogik
1. **Anzahl Hotels eingeben** → Services werden aktiviert
2. **Services auswählen** → Live-Berechnung erfolgt
3. **Transparente Anzeige** → Nutzer sieht Teilsummen
4. **"Übernehmen" klicken** → Wert wird ins EUR-Feld übertragen
5. **EUR-Feld bleibt editierbar** → Manuelle Anpassung möglich

### Visuelles Feedback
- Inaktive Checkboxen wenn Anzahl = 0
- Live-Update der Berechnungen
- Hervorhebung der Gesamtsumme
- Klarer Hinweis auf Verfeinerung in Step 4

---

## 🔗 Verknüpfung mit Step 4

In Step 4 werden diese pauschalen Werte verfeinert:
- Exakte Zimmeranzahl pro Hotel
- Tatsächliche Gästezahlen
- Spezifische Service-Angebote
- Individuelle Preispunkte

Die Summe aus Step 4 kann dann die pauschale Schätzung aus Step 2 ersetzen.

---

## 📝 Dokumentation für Nutzer

**Hilfetext in der UI:**
> "Diese Kalkulation basiert auf Branchenerfahrungswerten und dient als Ausgangspunkt für Ihre Partnerschaftsvereinbarung. Sie können den Wert jederzeit manuell anpassen. In Schritt 4 erfassen Sie die genauen Details pro Standort."

**Tooltip bei "Übernehmen":**
> "Übernimmt den kalkulierten Wert. Sie können ihn anschließend noch manuell ändern."

---

## ✅ Definition of Done

- [ ] Komponente zeigt korrekte Berechnungen
- [ ] Werte werden korrekt übernommen
- [ ] EUR-Feld bleibt manuell editierbar
- [ ] UI ist selbsterklärend
- [ ] Performance ist optimal (keine Lags)
- [ ] TypeScript kompiliert ohne Fehler
- [ ] Dokumentation ist vollständig
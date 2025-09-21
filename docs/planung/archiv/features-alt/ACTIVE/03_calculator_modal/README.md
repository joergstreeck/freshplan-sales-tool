# 🧮 M8 - Calculator Modal Integration (Tag 6)

**Status:** 📋 Geplant  
**Geschätzter Aufwand:** 1 Tag  
**Priorität:** HOCH - Kern-Tool für Angebotserstellung  
**Abhängigkeit:** M4 Opportunity Pipeline ✅

## 🎯 Ziel

Den bestehenden Calculator als Modal in die Opportunity Pipeline integrieren, sodass er kontextabhängig bei Stage "Angebotserstellung" aufgerufen wird.

## 📋 Implementation Checklist

### Morning (4h): Modal Wrapper
- [ ] CalculatorModal.tsx erstellen
- [ ] MUI Dialog Integration
- [ ] Context-Props definieren
- [ ] Styling von ShadCN → MUI migrieren

### Afternoon (4h): Pipeline Integration
- [ ] Trigger in Stage "PROPOSAL" einbauen
- [ ] Datenübergabe von Opportunity
- [ ] Ergebnis-Rückfluss implementieren
- [ ] Tests für Integration

## 🚨 OFFENE FRAGEN (VOR START KLÄREN!)

### 🔴 Integration Flow:

1. **Datenübergabe an Calculator:**
   - Welche Opportunity-Daten vorbefüllen?
     - Kundenname? ✅
     - Ansprechpartner?
     - Erwartete Personenzahl?
     - Event-Datum von expectedCloseDate?
   - Format der Übergabe?

2. **Ergebnis-Verarbeitung:**
   ```typescript
   // Was passiert mit dem Calculator-Ergebnis?
   onCalculatorComplete(result) {
     // a) Nur expectedValue updaten?
     // b) PDF generieren?
     // c) Angebots-Dokument erstellen?
     // d) E-Mail-Template vorbereiten?
   }
   ```

3. **User Flow:**
   - Calculator Modal = Pflicht oder optional?
   - Was wenn User abbricht?
   - Kann man später nochmal rechnen?
   - Wird alte Kalkulation überschrieben?

### 🟡 Technische Fragen:

1. **State Management:**
   - Calculator State lokal im Modal?
   - Oder globaler State für Wiederverwendung?
   - Zwischenspeicherung bei Abbruch?

2. **Backend Integration:**
   - Kalkulation speichern? Wo?
   - Neue Tabelle `opportunity_calculations`?
   - Oder JSON in Opportunity?
   - Versionierung von Kalkulationen?

3. **PDF Generation:**
   - Im Frontend oder Backend?
   - Template-Engine?
   - Speicherort (S3)?

### 🟢 UX Fragen:

1. **Modal Behavior:**
   - Fullscreen oder Standard Dialog?
   - Mobile: Andere Darstellung?
   - Escape = Abbrechen erlaubt?
   - Backdrop Click = Schließen?

2. **Visual Feedback:**
   - Loading State während Berechnung?
   - Success Animation nach Speichern?
   - Error States klar?

## 🏗️ Technische Architektur

### Modal Wrapper Design:
```typescript
interface CalculatorModalProps {
  open: boolean;
  onClose: () => void;
  opportunity: {
    id: string;
    name: string;
    customer?: {
      id: string;
      name: string;
      defaultDiscount?: number;
    };
    expectedPersons?: number;
    expectedDate?: Date;
  };
  onComplete: (result: CalculationResult) => void;
}

interface CalculationResult {
  totalAmount: number;
  calculation: {
    items: CalculationItem[];
    discounts: AppliedDiscount[];
    summary: CalculationSummary;
  };
  pdfUrl?: string;
}
```

### Integration Points:
```typescript
// In OpportunityPipeline.tsx
const handleStageChange = (oppId: string, newStage: Stage) => {
  if (newStage === 'PROPOSAL') {
    setCalculatorOpen(true);
    setCalculatorContext(opportunities[oppId]);
  }
  // Update stage via API
};

// In CalculatorModal.tsx
const handleCalculationComplete = (result: CalculationResult) => {
  // Update Opportunity
  updateOpportunity({
    expectedValue: result.totalAmount,
    calculationId: result.id, // ???
  });
  
  // Close Modal
  onClose();
  
  // Show Success
  showNotification('Angebot erstellt!');
  
  // Next Action?
  // - Open E-Mail composer?
  // - Generate PDF?
};
```

## 🧪 Test-Szenarien

```typescript
describe('Calculator Modal Integration', () => {
  it('should open when opportunity moved to PROPOSAL stage');
  it('should prefill customer data from opportunity');
  it('should update opportunity value on completion');
  it('should handle modal close without saving');
  it('should show error if calculation fails');
  
  // Edge Cases
  it('should handle missing customer data');
  it('should prevent closing during save');
  it('should work on mobile devices');
});
```

## 📍 Code Locations

### Frontend:
```bash
# Modal Wrapper:
vim frontend/src/features/calculator/components/CalculatorModal.tsx

# Integration in Pipeline:
vim frontend/src/features/opportunity/components/OpportunityPipeline.tsx
# Zeile ~156: Stage change handler

# Context Hook:
vim frontend/src/features/calculator/hooks/useCalculatorContext.ts
```

### Backend:
```bash
# Calculation Storage (wenn nötig):
vim backend/src/main/java/de/freshplan/domain/opportunity/entity/OpportunityCalculation.java

# Service Update:
vim backend/src/main/java/de/freshplan/domain/opportunity/service/OpportunityService.java
# Method: linkCalculation()
```

## ⚠️ Migration Notes

### Von ShadCN zu MUI:
```typescript
// ALT (ShadCN)
import { Dialog, DialogContent } from "@/components/ui/dialog";

// NEU (MUI)
import { Dialog, DialogContent, DialogTitle } from '@mui/material';

// Styling anpassen:
// - Tailwind classes → MUI sx prop
// - shadcn Varianten → MUI variants
```

### Bestehende Calculator Logic:
- Backend unter `/api/calculator/calculate` ✅
- Business Logic in `CalculatorService.java` ✅
- Nur UI-Wrapper nötig!

## 🔗 Abhängige Dokumente

- [Calculator Backend Analyse](../../docs/claude-work/daily-work/2025-07-11/calculator-backend-correction.md)
- [Opportunity Integration Flow](./integration_flow.md) - MUSS ERSTELLT WERDEN!
- [PDF Generation Strategy](./pdf_generation.md) - MUSS ERSTELLT WERDEN!

## ✅ Definition of Done

- [ ] Calculator öffnet bei Stage "PROPOSAL"
- [ ] Kundendaten werden übergeben
- [ ] Berechnung funktioniert wie bisher
- [ ] Ergebnis updated Opportunity.expectedValue
- [ ] Modal schließt sauber
- [ ] Mobile-optimiert
- [ ] Tests grün
- [ ] Performance < 200ms für Modal-Open

## 🚀 Nächster Schritt

Nach Completion → [FC-004 Verkäuferschutz Basis](../04_verkaeufer_schutz/README.md)
# 🎮 M4 Implementation Simulation - Schwachstellen-Analyse

**Datum:** 12.07.2025  
**Modul:** M4 - Opportunity Pipeline  
**Zweck:** Simulation einer kompletten Implementierung zur Identifikation von Planungslücken

## 📋 Simulations-Durchlauf

### 🚨 Problem 1: Unklare Datenmodell-Migration
```java
// ❓ Wo kommen die initialen Opportunities her?
// Haben wir schon Leads im System?
// Wie migrieren wir bestehende "Interessenten"?

@Entity
public class Opportunity {
    // Fehlt: Mapping zu Legacy-Daten
    // Fehlt: Import-Strategie
}
```

**Lösung:** Migration-Strategy-Dokument erstellen

### 🚨 Problem 2: Stage-Übergänge nicht definiert
```typescript
// ❓ Welche Übergänge sind erlaubt?
// Kann man von "Qualifizierung" direkt zu "Gewonnen"?
// Wer darf Stages ändern?

const allowedTransitions = {
    NEW_LEAD: ['QUALIFICATION', 'CLOSED_LOST'],
    QUALIFICATION: ['???'], // Unklar!
}
```

**Lösung:** Business Rules Workshop mit Product Owner

### 🚨 Problem 3: Calculator-Integration unklar
```typescript
// ❓ Wie übergeben wir Daten an den Calculator?
// Was passiert mit dem Ergebnis?
// Wird automatisch ein Angebot erstellt?

const handleCalculatorResult = (result) => {
    // TODO: Was genau?
    // Opportunity updaten?
    // Dokument generieren?
    // E-Mail Template?
}
```

**Lösung:** Integration Flow dokumentieren

### 🚨 Problem 4: Permissions & Verkäuferschutz
```java
// ❓ Wer darf welche Opportunity sehen/editieren?
// Wie integriert sich das mit FC-004 (Verkäuferschutz)?

@PreAuthorize("???") // Völlig unklar!
public OpportunityDTO updateStage(UUID id, OpportunityStage newStage) {
    // Verkäuferschutz-Logik fehlt
}
```

**Lösung:** Security-Konzept VOR Implementation

### 🚨 Problem 5: Performance bei Drag & Drop
```typescript
// ❓ Was passiert bei 500+ Opportunities?
// Optimistic Updates?
// Conflict Resolution?

const handleDrop = async (opportunityId, newStage) => {
    // Naiv: API Call und warten
    await api.updateStage(opportunityId, newStage);
    
    // Problem: Was wenn parallel jemand anders updated?
    // Problem: UI friert ein bei langsamer Verbindung
}
```

**Lösung:** Optimistic UI Pattern definieren

### 🚨 Problem 6: Fehlende Test-Szenarien
```typescript
// ❓ Wie testen wir Drag & Drop?
// ❓ Wie testen wir Stage-Validierung?
// ❓ Wie testen wir Permissions?

describe('OpportunityPipeline', () => {
    it('should ??? when ???', () => {
        // Keine klaren Akzeptanzkriterien!
    });
});
```

**Lösung:** BDD-Szenarien VOR Implementierung

## 📊 Identifizierte Planungslücken

### 1. **Business Logic Gaps**
- [ ] Stage-Übergangsmatrix fehlt
- [ ] Automatische Aktionen bei Stage-Wechsel unklar
- [ ] Verkäuferschutz-Integration nicht definiert
- [ ] Provisions-Zuordnung bei Opportunities unklar

### 2. **Technical Gaps**
- [ ] API-Versionierung nicht geklärt
- [ ] WebSocket-Updates für Team-Arbeit fehlen
- [ ] Caching-Strategie für Pipeline-Ansicht
- [ ] Fehlerbehandlung bei Drag & Drop

### 3. **UX Gaps**
- [ ] Mobile Drag & Drop Alternative?
- [ ] Keyboard Navigation für Accessibility?
- [ ] Bulk-Aktionen (mehrere Opportunities verschieben)?
- [ ] Undo-Funktion nach Stage-Wechsel?

### 4. **Integration Gaps**
- [ ] E-Mail-Zuordnung zu Opportunities
- [ ] Kalender-Events aus Opportunities
- [ ] Dokumenten-Anhänge wo speichern?
- [ ] Activity-Timeline Integration

## 🛠️ Verbesserungsvorschläge für Planungsprozess

### 1. **API-First Development**
```yaml
# Erst OpenAPI Spec schreiben
/api/opportunities:
  post:
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/CreateOpportunityRequest'
```

### 2. **Business Rules Document**
```markdown
## Opportunity Stage Rules

### Erlaubte Übergänge
| Von | Nach | Bedingung | Automatische Aktionen |
|-----|------|-----------|----------------------|
| NEW_LEAD | QUALIFICATION | - | E-Mail an Sales |
| QUALIFICATION | NEEDS_ANALYSIS | Kunde zugeordnet | - |
| NEEDS_ANALYSIS | PROPOSAL | Min. 1 Kontakt | Calculator öffnet |
```

### 3. **Test Scenarios First**
```gherkin
Feature: Opportunity Pipeline Management

Scenario: Sales Rep moves qualified lead to needs analysis
  Given a qualified opportunity "Hotel Adler"
  And I am assigned as the sales rep
  When I drag the opportunity to "Needs Analysis" stage
  Then the opportunity should be in "Needs Analysis"
  And an activity should be logged
  And the customer should receive a notification
```

### 4. **Performance Budget**
```typescript
const PERFORMANCE_REQUIREMENTS = {
  pipelineLoad: 1000,      // ms
  dragDropResponse: 100,   // ms
  stageUpdate: 200,        // ms
  maxOpportunities: 1000   // per view
};
```

## 🎯 Empfohlene Sofortmaßnahmen

### Vor M4-Start (2-3 Stunden):
1. **Business Rules Workshop**
   - Stage-Übergänge klären
   - Automatisierungen definieren
   - Verkäuferschutz-Integration

2. **API Design Session**
   - OpenAPI Spec erstellen
   - Versionierung klären
   - Error Codes definieren

3. **Test-Szenario-Workshop**
   - Akzeptanzkriterien
   - Edge Cases
   - Performance-Tests

### Dokumentations-Updates:
1. **Security-Konzept** für Opportunities
2. **Integration-Flow** Calculator ↔ Opportunity
3. **Migration-Guide** für bestehende Daten

## 💡 Erkenntnisse

Die technische Architektur ist solide, aber es fehlen:
- **Business-Entscheidungen** (was darf wer wann)
- **Integrations-Details** (wie spielen Module zusammen)
- **Non-Functional Requirements** (Performance, Security)

Diese sollten VOR der Implementierung geklärt werden!
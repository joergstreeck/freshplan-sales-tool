# Frontend Bugs Analyse - Kontakte und Opportunities

## 🐛 Bug 1: Nur 2 Kontakte werden angezeigt statt 25

### Problem:
- **Backend:** 25 Kontakte in der Datenbank (verifiziert)
- **Frontend:** Zeigt nur 2 hartcodierte Mock-Kontakte

### Ursache gefunden:
**Datei:** `/frontend/src/pages/CustomerDetailPage.tsx`
**Zeilen:** 354-397

```typescript
// TODO: Replace with actual API call when backend endpoint is ready
// return customerApi.getCustomerContacts(customerId);

// Mock data for development
return [
  {
    id: 'contact-1',
    firstName: 'Max',
    lastName: 'Mustermann',
    // ... hartcodierte Daten
  },
  {
    id: 'contact-2', 
    firstName: 'Anna',
    lastName: 'Schmidt',
    // ... hartcodierte Daten
  }
];
```

### Lösung:
```typescript
// CustomerDetailPage.tsx, Zeile 354-356 ersetzen durch:
return customerApi.getCustomerContacts(customerId);
```

Der API-Call muss implementiert werden in `customerApi`:
```typescript
async getCustomerContacts(customerId: string) {
  const response = await fetch(`/api/customers/${customerId}/contacts`);
  if (!response.ok) throw new Error('Failed to fetch contacts');
  return response.json();
}
```

---

## 🐛 Bug 2: Nur 16 Opportunities im Header statt 20

### Problem:
- **Backend:** 20 aktive Opportunities (alle außer CLOSED_WON/CLOSED_LOST)
- **Frontend:** Zeigt möglicherweise nur 16 an

### Ursache gefunden:
**Datei:** `/frontend/src/features/opportunity/components/KanbanBoard.tsx`
**Zeilen:** 33-37

```typescript
const ACTIVE_STAGES = [
  OpportunityStage.LEAD,        // ❌ GIBT ES NICHT!
  OpportunityStage.QUALIFIED,   // ❌ GIBT ES NICHT!
  OpportunityStage.PROPOSAL,    // ✅ OK
  OpportunityStage.NEGOTIATION, // ✅ OK
];
```

**Tatsächliche Stages im Frontend Enum:**
- `NEW_LEAD` (nicht LEAD)
- `QUALIFICATION` (nicht QUALIFIED)
- `NEEDS_ANALYSIS` (fehlt in ACTIVE_STAGES!)
- `PROPOSAL` ✅
- `NEGOTIATION` ✅

### Erklärung der Diskrepanz:
Die Backend-Daten haben folgende Verteilung:
- NEW_LEAD: 2
- QUALIFICATION: 6
- NEEDS_ANALYSIS: 4  **← Werden nicht gezählt!**
- PROPOSAL: 5
- NEGOTIATION: 3
**Total: 20**

Wenn NEEDS_ANALYSIS (4 Opportunities) nicht in ACTIVE_STAGES ist, werden nur 16 angezeigt!

### Lösung:
```typescript
// KanbanBoard.tsx, Zeile 33-37 ersetzen durch:
const ACTIVE_STAGES = [
  OpportunityStage.NEW_LEAD,       // Korrekter Name
  OpportunityStage.QUALIFICATION,  // Korrekter Name
  OpportunityStage.NEEDS_ANALYSIS, // Fehlte!
  OpportunityStage.PROPOSAL,
  OpportunityStage.NEGOTIATION,
];
```

---

## 📊 Zusammenfassung

### Kontakte-Bug:
- **Status:** Hartcodierte Mock-Daten in CustomerDetailPage.tsx
- **Fix:** API-Call aktivieren, Mock-Daten entfernen
- **Aufwand:** 5 Minuten

### Opportunities-Bug:
- **Status:** Falsche Stage-Namen und fehlende NEEDS_ANALYSIS Stage
- **Fix:** ACTIVE_STAGES Array korrigieren
- **Aufwand:** 2 Minuten

### Root Cause:
Beide Bugs entstanden durch:
1. **Unvollständige Integration** - Mock-Daten wurden nicht durch echte API-Calls ersetzt
2. **Inkonsistente Enums** - Frontend und Backend verwenden unterschiedliche Stage-Namen
3. **Fehlende Stages** - NEEDS_ANALYSIS wurde vergessen

## 🎯 Empfohlene Sofortmaßnahmen

1. **CustomerDetailPage.tsx** - Mock-Daten entfernen, echte API nutzen
2. **KanbanBoard.tsx** - ACTIVE_STAGES korrigieren
3. **Type-Safety** - Enum-Synchronisation zwischen Frontend und Backend sicherstellen

## 📝 Testing nach Fix

```javascript
// Browser Console Tests:

// 1. Kontakte testen
fetch('/api/customers/dd08c720-9478-4e08-a3fb-4cd7e27418b6/contacts')
  .then(r => r.json())
  .then(console.log);
// Sollte 3 Kontakte für BioFresh zeigen

// 2. Opportunities testen  
fetch('/api/opportunities')
  .then(r => r.json())
  .then(data => {
    const byStage = {};
    data.forEach(opp => {
      byStage[opp.stage] = (byStage[opp.stage] || 0) + 1;
    });
    console.log('Opportunities by Stage:', byStage);
    console.log('Total Active:', data.filter(o => 
      !['CLOSED_WON', 'CLOSED_LOST'].includes(o.stage)
    ).length);
  });
// Sollte 20 aktive zeigen
```

---

**Analysiert von:** Claude
**Datum:** 09.08.2025 18:10
**Priorität:** HOCH - Produktionsdaten werden nicht angezeigt!
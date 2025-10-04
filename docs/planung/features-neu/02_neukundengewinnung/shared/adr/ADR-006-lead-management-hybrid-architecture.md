# ADR-006: Lead-Management Hybrid-Architektur

**Status:** ✅ Accepted
**Datum:** 2025-10-04
**Kontext:** Sprint 2.1.5 - Lead-Management UI
**Entscheider:** Product Owner, Tech Lead

---

## Kontext und Problemstellung

Nach Implementierung des Progressive Profiling (Sprint 2.1.5) benötigen wir eine Lead-Management-Oberfläche. Es existieren bereits zwei UI-Pattern:

1. **LeadListEnhanced.tsx** (Card-basiert)
   - Material-UI Cards
   - Basic Features (Stage-Badge, Protection-Badge, Source-Icon)
   - Limitierte Funktionalität (keine Filter, kein Sort, keine Spalten-Konfiguration)

2. **CustomersPageV2.tsx** (Tabellen-basiert)
   - Virtualisierte Tabelle (react-window)
   - Umfangreiche Features:
     - Intelligent Filter Bar + Quick Filters
     - Smart Sort (verkaufsorientiert)
     - Column Manager (Drag & Drop)
     - Universal Search mit Caching
     - Zustand Store (persistent)
   - Kunde-Status-System: `LEAD → PROSPECT → AKTIV`

**Problem:** Inkonsistente UX - Leads haben weniger Features als Kunden, obwohl sie den gleichen Workflow haben.

---

## Entscheidung

**Hybrid-Ansatz in 2 Phasen:**

### Phase 1: Sprint 2.1.5 (JETZT)
**"Leads als Customer-Status"**

- ✅ **Wiederverwendung** der Customer-Architektur
- ✅ **LeadsPage.tsx** wird Wrapper um `CustomersPageV2` mit `defaultFilter={{ status: ['LEAD'] }}`
- ✅ **Alle Customer-Features** sofort verfügbar (Filter, Sort, Columns)
- ✅ **Konsistente UX** - User kennt die Logik bereits
- ✅ **LeadListEnhanced.tsx** wird obsolet → kann gelöscht werden

### Phase 2: Sprint 2.1.6 (SPÄTER)
**"Lead-spezifische Erweiterungen"**

- 🔄 **Lead-Scoring-System** (0-100 Punkte)
  - Umsatzpotenzial (25%)
  - Engagement (25%)
  - Fit (25%)
  - Dringlichkeit (25%)
- 🔄 **Lead-Status-Workflows** (UI für Übergänge)
  - LEAD → PROSPECT (Qualifizierung)
  - PROSPECT → AKTIV (Konversion)
- 🔄 **Lead-Activity-Timeline** (Interaktions-Historie)
- 🔄 **Lead-Protection aktivieren** (assignedTo Filter im Backend)

---

## Begründung

### Warum Phase 1 (Customer-Pattern)?

**✅ Vorteile:**
1. **Minimal Effort** - 1-2 Stunden Aufwand (statt Tage)
2. **Sofortige Features** - Filter, Sort, Column Settings, Search
3. **Konsistente UX** - Kein Umlernen für User
4. **Backend-Ready** - Lead-Status existiert bereits im Customer-Entity
5. **Performance** - Virtualisierung für große Datenmengen
6. **State Management** - Zustand Store mit Persistence

**❌ Nachteile:**
1. Keine Lead-spezifische Validierung (akzeptabel für jetzt)
2. Lead-Scoring fehlt (kommt in Phase 2)
3. Gemischte Business-Logik (akzeptabel, da Leads → Kunden werden)

### Warum Phase 2 (Lead-Erweiterungen)?

**Schrittweise Evolution statt Big Bang:**
- MVP (Phase 1) liefert 80% der Funktionalität in 20% der Zeit
- Lead-spezifische Features (Scoring, Workflows) können iterativ ergänzt werden
- Keine Code-Duplikation - Erweiterung der bestehenden Architektur

---

## Konsequenzen

### Positive Konsequenzen

✅ **Schnelle Time-to-Market**
- Lead-Management produktiv in <2 Stunden

✅ **Konsistente User Experience**
- Gleiche Bedienung für Leads und Kunden

✅ **Code-Wiederverwendung**
- Keine Duplikation von Filter/Sort/Table-Logik

✅ **Skalierbarkeit**
- Virtualisierung für >1000 Leads

✅ **Erweiterbarkeit**
- Phase 2 kann Lead-spezifische Features nahtlos ergänzen

### Negative Konsequenzen

❌ **Technische Schulden (temporär)**
- Lead-Protection noch nicht aktiviert (Quick Win für Sprint 2.1.6)
- Lead-Scoring fehlt (geplant für Sprint 2.1.6)

❌ **Business-Logik-Mischung**
- Lead-Validierung vs. Customer-Validierung in gleicher Komponente
- Akzeptabel, da Leads zu Kunden werden (gleicher Lifecycle)

---

## Implementierungs-Details

### Phase 1: Datei-Änderungen

**NEU:**
```typescript
// /pages/LeadsPage.tsx
import { CustomersPageV2 } from './CustomersPageV2';

export default function LeadsPage() {
  return (
    <CustomersPageV2
      defaultFilter={{ status: ['LEAD'] }}
      title="Lead-Management"
      createButtonLabel="Lead erfassen"
      context="leads"  // ← Lifecycle Context für FilterBar
    />
  );
}
```

**LIFECYCLE-KONTEXT-ARCHITEKTUR:**

Die IntelligentFilterBar verwendet einen `context`-Prop, um kontext-sensitive Filter anzuzeigen:

```typescript
// IntelligentFilterBar.tsx
interface IntelligentFilterBarProps {
  context?: 'customers' | 'leads';  // Lifecycle-Context
  // ...
}

// FilterDrawer.tsx - Status-Filter Logic
{Object.values(CustomerStatus).filter(status => {
  if (context === 'leads') {
    // Lead Lifecycle Phase: Zeige nur Baby-Status
    return status === 'LEAD' || status === 'PROSPECT';
  }
  // Customer Lifecycle Phase: Zeige nur Erwachsenen-Status
  return status !== 'LEAD' && status !== 'PROSPECT' && status !== 'RISIKO';
}).map(status => ...)}
```

**Rationale:**
- ✅ **Lifecycle-basiertes Denken:** Lead ist eine Phase, kein separates Entity
- ✅ **Konsistente Filter:** Risiko, Umsatz, Kontakte gelten für BEIDE Phasen
- ✅ **Einfache Implementierung:** Ein Prop statt zwei FilterBars
- ✅ **Best Practice:** Analog zu Salesforce/HubSpot (Lead → Customer Lifecycle)

**Verwendung:**
```typescript
// CustomersPageV2
<IntelligentFilterBar context="customers" ... />
// → Zeigt Status: AKTIV, INAKTIV, ARCHIVIERT

// LeadsPage
<IntelligentFilterBar context="leads" ... />
// → Zeigt Status: LEAD, PROSPECT
```

**LÖSCHEN:**
```
/features/leads/LeadListEnhanced.tsx
/features/leads/LeadStageBadge.tsx (duplikat zu CustomerStatusBadge)
```

**BEHALTEN:**
```
/features/leads/LeadWizard.tsx (Progressive Profiling)
/features/leads/LeadProtectionBadge.tsx (Lead-spezifisch)
/features/leads/LeadSourceIcon.tsx (Lead-spezifisch)
/features/leads/api.ts
/features/leads/types.ts
```

### Phase 2: Geplante Erweiterungen

**NEU (Sprint 2.1.6):**
```
/features/leads/LeadScoreIndicator.tsx
/features/leads/LeadStatusWorkflow.tsx
/features/leads/LeadActivityTimeline.tsx
/features/leads/hooks/useLeadScore.ts
/features/leads/hooks/useLeadConversion.ts
```

**Backend-Erweiterungen (Sprint 2.1.6):**
```sql
-- Lead-Scoring-Felder
ALTER TABLE leads ADD COLUMN lead_score INTEGER;
ALTER TABLE leads ADD COLUMN lead_score_factors JSONB;

-- Lead-Protection aktivieren
CREATE INDEX idx_leads_assigned_to ON leads(assigned_to)
  WHERE status IN ('LEAD', 'PROSPECT');
```

---

## Alternativen

### Alternative 1: Separates Lead-Feature (abgelehnt)
**Warum abgelehnt:**
- 3-5 Tage Aufwand
- Code-Duplikation (Filter, Sort, Table)
- Inkonsistente UX (zwei verschiedene Patterns)
- Leads werden zu Kunden → gleiche Logik sinnvoll

### Alternative 2: LeadListEnhanced verbessern (abgelehnt)
**Warum abgelehnt:**
- Card-View weniger effizient für große Datenmengen
- Filter/Sort/Column-Logic müsste dupliziert werden
- Keine Virtualisierung → Performance-Problem bei >100 Leads

### Alternative 3: Big Bang (alle Features sofort) (abgelehnt)
**Warum abgelehnt:**
- Zu hoher Aufwand (1-2 Wochen)
- Sprint 2.1.5 würde sich verzögern
- Hybrid-Ansatz liefert 80% der Funktionalität in 20% der Zeit

---

## Risiken und Mitigationen

| Risiko | Wahrscheinlichkeit | Impact | Mitigation |
|--------|-------------------|--------|------------|
| **Lead-Protection nicht aktiv** | Hoch | Mittel | Sprint 2.1.6: `assignedTo` Filter im Backend aktivieren |
| **Lead-Scoring fehlt** | Hoch | Niedrig | Sprint 2.1.6: Lead-Score-Algorithmus implementieren |
| **Business-Logik-Mischung** | Mittel | Niedrig | Klare Trennung via TypeScript Guards + Feature Flags |
| **Performance bei >1000 Leads** | Niedrig | Mittel | Virtualisierung bereits vorhanden |

---

## Compliance

**DSGVO Art. 5 (Datenminimierung):**
- ✅ Progressive Profiling bleibt unberührt
- ✅ Lead-Daten werden nur bei Bedarf erfasst

**Handelsvertretervertrag §3.2 (Lead-Schutz):**
- ⚠️ Lead-Protection noch nicht aktiviert (Sprint 2.1.6)
- ✅ Protection-Badge zeigt Status korrekt an

---

## Verwandte Dokumente

- [FRONTEND_DELTA.md](../SPRINT_2_1_5/FRONTEND_DELTA.md) - Section 8: Lead-Management UI
- [BUSINESS_LOGIC_LEAD_ERFASSUNG.md](../SPRINT_2_1_5/BUSINESS_LOGIC_LEAD_ERFASSUNG.md) - Section 11
- [ADR-004](./ADR-004-lead-protection-inline-first.md) - Inline-First Architecture
- [Sprint 2.1.6 TRIGGER](../SPRINT_2_1_6/TRIGGER.md) - Lead-Erweiterungen

---

**Erstellt:** 2025-10-04
**Review:** Product Owner ✅, Tech Lead ✅
**Implementiert:** Sprint 2.1.5 (Phase 1)
**Geplant:** Sprint 2.1.6 (Phase 2)

# FC-011: Pipeline-Cockpit Integration - Technisches Konzept

**Feature Code:** FC-011  
**Status:** 🟡 KONZEPT  
**Priorität:** HIGH (direkt nach M4 Backend-Integration)  
**Geschätzter Aufwand:** 5-7 Tage  

## 📋 Übersicht

Die Pipeline-Cockpit Integration transformiert das FreshPlan CRM in ein echtes "Sales Command Center". Durch einen einfachen Klick auf eine Opportunity-Karte werden alle relevanten Kundendaten direkt im Cockpit-Arbeitsbereich geladen.

## 🎯 Kernfunktionalität

### Haupt-Features
1. **Click-to-Load:** Ein Klick auf Pipeline-Karte lädt Kundendaten
2. **Multi-Gesten Support:** Single/Double/Right-Click Aktionen
3. **Split-View Layouts:** Flexible Aufteilung Pipeline/Cockpit
4. **Context Preservation:** Breadcrumbs & History
5. **Performance Optimierung:** Preloading & Caching

## 🔗 Abhängigkeiten & Auswirkungen

### Direkte Abhängigkeiten
- **M4 Opportunity Pipeline:** Basis für Klick-Events
- **M5 Customer Cockpit:** Ziel der Datenladung
- **FC-010 Pipeline Scalability:** UI-Komponenten teilen

### Betroffene Features
- **M1 Dashboard:** Neue Quick-Navigation Widgets
- **FC-003 Email Integration:** Kontext-basierte Mail-Actions
- **FC-009 Contract Renewal:** Renewal-Button Integration
- **FC-013 Activity & Notes:** Activity-Timeline im Cockpit-Kontext

## 📐 Architektur

### Frontend State Management
```typescript
interface CockpitState {
  activeOpportunityId: string | null;
  activeCustomerId: string | null;
  loadingState: 'idle' | 'loading' | 'loaded' | 'error';
  viewMode: 'standard' | 'split' | 'compact';
  history: OpportunityHistory[];
}
```

### Event Flow
```
PipelineCard.onClick 
  → dispatch(loadCustomerIntoCockpit)
  → API.fetchCustomerWithContext
  → updateCockpitState
  → renderCockpitContent
```

## 📚 Detail-Dokumente

1. **Frontend Architektur:** [./FC-011/frontend-architecture.md](./FC-011/frontend-architecture.md)
2. **Interaction Design:** [./FC-011/interaction-patterns.md](./FC-011/interaction-patterns.md)
3. **Performance Strategy:** [./FC-011/performance-optimization.md](./FC-011/performance-optimization.md)
4. **API Extensions:** [./FC-011/api-requirements.md](./FC-011/api-requirements.md)

## 🚀 Implementierungs-Phasen

### Phase 1: Basis Click-to-Load (2 Tage)
- Klick-Event Handler in Pipeline-Karten
- Cockpit-Datenladung implementieren
- Visual Feedback (Active States)

### Phase 2: Multi-Gesten & Actions (2 Tage)
- Double-Click für Detail-Modal
- Right-Click Kontextmenü
- Keyboard Shortcuts

### Phase 3: Split-View & Layouts (2 Tage)
- Resizable Split-View
- Layout-Presets (70/30, 50/50)
- Mobile Touch-Gesten

### Phase 4: Performance & Polish (1 Tag)
- Hover-Preloading
- LRU Cache Implementation
- Smooth Transitions

## ⚠️ Technische Herausforderungen

1. **State Synchronisation:** Pipeline ↔ Cockpit
2. **Performance bei großen Datenmengen**
3. **Mobile Touch-Gesten vs. Desktop-Interaktionen**
4. **Unsaved Changes Handling beim Kontext-Wechsel**

## 📊 Messbare Erfolge

- **Time-to-Context:** < 500ms von Klick bis Datenladung
- **User Satisfaction:** Reduktion der Klicks um 60%
- **Adoption Rate:** 90% nutzen Click-to-Load täglich

## 🔄 Updates & Status

**24.07.2025:** Initiales Konzept erstellt nach Business-Diskussion
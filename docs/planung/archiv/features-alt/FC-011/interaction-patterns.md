# FC-011: Interaction Patterns & UX Design

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-011-pipeline-cockpit-integration.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-011-pipeline-cockpit-integration.md)  
**Fokus:** User Interaction Design und UX-Flows

## 🖱️ Klick-Verhalten Matrix

| Aktion | Desktop | Mobile/Touch | Resultat | Visual Feedback |
|--------|---------|--------------|----------|-----------------|
| **Einzelklick** | Left Click | Single Tap | Lädt Kunde ins Cockpit | Karte wird aktiv (Border + Highlight) |
| **Doppelklick** | Double Click | Double Tap | Öffnet Detail-Modal | Modal Overlay mit Fade-In |
| **Rechtsklick** | Right Click | Long Press | Kontextmenü | Menü an Cursor-Position |
| **Hover** | Mouse Over | - | Preload + Preview | Subtle Shadow + Scale(1.02) |
| **Drag** | Click & Drag | Touch & Drag | Stage-Wechsel | Ghost Card + Drop Zones |

## 🎯 Quick Actions im Karten-Footer

```typescript
interface QuickAction {
  icon: string;
  tooltip: string;
  action: ActionType;
  visibility: 'always' | 'hover' | 'contextual';
}

const QUICK_ACTIONS: Record<OpportunityStage, QuickAction[]> = {
  NEGOTIATION: [
    { icon: '✅', tooltip: 'Deal als gewonnen markieren', action: 'WIN', visibility: 'hover' },
    { icon: '❌', tooltip: 'Deal als verloren markieren', action: 'LOSE', visibility: 'hover' },
    { icon: '📧', tooltip: 'Kunde kontaktieren', action: 'EMAIL', visibility: 'always' }
  ],
  RENEWAL: [
    { icon: '📝', tooltip: 'Renewal-Prozess starten', action: 'RENEW', visibility: 'always' },
    { icon: '📧', tooltip: 'Renewal-Erinnerung senden', action: 'REMIND', visibility: 'hover' }
  ],
  CLOSED_LOST: [
    { icon: '🔄', tooltip: 'Erneut ins Rennen schicken', action: 'REACTIVATE', visibility: 'always' }
  ]
};
```

## 📱 Touch-Gesten für Mobile

### Swipe-Aktionen
```typescript
const SWIPE_ACTIONS = {
  LEFT: {
    action: 'QUICK_LOSE',
    color: '#dc2626',
    icon: '❌',
    label: 'Verloren',
    confirmRequired: true
  },
  RIGHT: {
    action: 'QUICK_WIN',
    color: '#16a34a',
    icon: '✅',
    label: 'Gewonnen',
    confirmRequired: true
  },
  UP: {
    action: 'OPEN_DETAILS',
    color: '#3b82f6',
    icon: '📋',
    label: 'Details'
  },
  DOWN: {
    action: 'QUICK_NOTE',
    color: '#8b5cf6',
    icon: '📝',
    label: 'Notiz'
  }
};
```

## 🎨 Visual States & Transitions

### Karten-Zustände
```scss
.opportunity-card {
  // Default State
  &--default {
    border: 1px solid var(--border-default);
    background: var(--bg-card);
    transform: scale(1);
  }
  
  // Hover State
  &--hover {
    border-color: var(--border-hover);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    transform: scale(1.02);
    
    .quick-actions {
      opacity: 1;
      transform: translateY(0);
    }
  }
  
  // Active State (im Cockpit geladen)
  &--active {
    border: 2px solid var(--primary);
    background: var(--bg-active);
    
    &::before {
      content: '👁️';
      position: absolute;
      top: 8px;
      right: 8px;
      font-size: 16px;
    }
  }
  
  // Loading State
  &--loading {
    opacity: 0.7;
    pointer-events: none;
    
    &::after {
      content: '';
      position: absolute;
      inset: 0;
      background: linear-gradient(
        90deg,
        transparent 0%,
        rgba(255, 255, 255, 0.3) 50%,
        transparent 100%
      );
      animation: shimmer 1.5s infinite;
    }
  }
}
```

## 🔄 Kontext-Bewahrung

### Breadcrumb Navigation
```
🏠 Dashboard > 📊 Pipeline > 👤 Müller GmbH > 💼 50k Deal
```

### History Stack
```typescript
interface HistoryEntry {
  opportunityId: string;
  customerId: string;
  timestamp: Date;
  source: 'pipeline' | 'search' | 'dashboard' | 'direct';
}

// Navigation Controls
const NavigationBar = () => (
  <div className="navigation-bar">
    <Button onClick={navigateBack} disabled={!canGoBack}>
      ← Zurück
    </Button>
    <Breadcrumbs items={breadcrumbItems} />
    <Button onClick={navigateForward} disabled={!canGoForward}>
      Vor →
    </Button>
  </div>
);
```

## 💬 Feedback & Notifications

### Toast Messages
```typescript
const TOAST_MESSAGES = {
  CUSTOMER_LOADED: {
    type: 'success',
    message: 'Kundendaten geladen',
    duration: 2000
  },
  STAGE_CHANGED: {
    type: 'info',
    message: 'Opportunity verschoben nach {stage}',
    duration: 3000
  },
  ACTION_CONFIRMED: {
    type: 'success',
    message: '{action} erfolgreich durchgeführt',
    duration: 2500
  },
  ERROR_LOADING: {
    type: 'error',
    message: 'Fehler beim Laden der Kundendaten',
    duration: 5000,
    action: { label: 'Erneut versuchen', onClick: retry }
  }
};
```

### Inline Warnings
```typescript
const WIP_WARNING = () => (
  <Alert severity="warning" icon={<LightbulbIcon />}>
    💡 Sie haben aktuell {count} von {limit} möglichen Opportunities 
    in dieser Phase. Ein effektiver Vertrieb arbeitet am besten, 
    wenn der Fokus erhalten bleibt!
  </Alert>
);
```

## ⌨️ Keyboard Shortcuts

### Global Shortcuts
| Shortcut | Action | Context |
|----------|--------|---------|
| `Space` | Lädt ausgewählte Opportunity ins Cockpit | Pipeline |
| `Enter` | Öffnet Detail-Modal | Pipeline |
| `G` | Markiert als gewonnen | Opportunity Selected |
| `L` | Markiert als verloren | Opportunity Selected |
| `R` | Startet Renewal-Prozess | Renewal Stage |
| `E` | Öffnet E-Mail Dialog | Any |
| `N` | Neue Notiz hinzufügen | Any |
| `Esc` | Schließt Modal/Dialog | Modal Open |
| `←/→` | Navigate History | Cockpit |
| `Ctrl+F` | Fokus auf Suche | Any |

### Implementation
```typescript
export const useKeyboardShortcuts = () => {
  const { selectedOpportunity, loadCustomerIntoCockpit } = usePipeline();
  
  useEffect(() => {
    const handleKeyPress = (e: KeyboardEvent) => {
      // Ignore if typing in input
      if (e.target instanceof HTMLInputElement) return;
      
      switch(e.key) {
        case ' ':
          e.preventDefault();
          if (selectedOpportunity) {
            loadCustomerIntoCockpit(selectedOpportunity.id);
          }
          break;
        case 'g':
          if (selectedOpportunity && !e.ctrlKey) {
            markAsWon(selectedOpportunity.id);
          }
          break;
        // ... more shortcuts
      }
    };
    
    window.addEventListener('keydown', handleKeyPress);
    return () => window.removeEventListener('keydown', handleKeyPress);
  }, [selectedOpportunity]);
};
```

## 🎯 Smart Presets & Quick Filters

### Morning Focus Preset
```typescript
const MORNING_FOCUS = {
  name: "Mein Tagesstart",
  icon: "☀️",
  filters: {
    assignedToMe: true,
    needsAction: true,
    dueToday: true
  },
  sort: 'priority_desc',
  message: "Diese Opportunities brauchen heute Ihre Aufmerksamkeit"
};
```

### Context-Aware Actions
```typescript
// Zeige relevante Actions basierend auf Stage & Zeit
const getContextualActions = (opportunity: Opportunity) => {
  const daysSinceLastActivity = getDaysSince(opportunity.lastActivityDate);
  const daysUntilClose = getDaysUntil(opportunity.expectedCloseDate);
  
  if (daysSinceLastActivity > 7) {
    return ['FOLLOW_UP_REQUIRED'];
  }
  
  if (daysUntilClose < 3 && opportunity.stage === 'NEGOTIATION') {
    return ['URGENT_CLOSE', 'SCHEDULE_MEETING'];
  }
  
  if (opportunity.contractExpiryDays < 30) {
    return ['START_RENEWAL', 'SEND_REMINDER'];
  }
  
  return ['STANDARD_ACTIONS'];
};
```

## 🔔 Activity Indicators

### Visual Indicators
```typescript
const ActivityIndicator = ({ opportunity }: { opportunity: Opportunity }) => {
  const status = getActivityStatus(opportunity);
  
  return (
    <div className={`activity-indicator activity-indicator--${status.level}`}>
      <span className="indicator-dot" />
      <span className="indicator-text">{status.message}</span>
    </div>
  );
};

// Status Levels
const ACTIVITY_LEVELS = {
  URGENT: {
    color: '#dc2626',
    animation: 'pulse',
    message: 'Kunde wartet auf Antwort'
  },
  WARNING: {
    color: '#f59e0b',
    animation: 'none',
    message: 'Follow-up überfällig'
  },
  GOOD: {
    color: '#10b981',
    animation: 'none',
    message: 'Alles im grünen Bereich'
  }
};
```
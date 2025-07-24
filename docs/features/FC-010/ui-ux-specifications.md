# FC-010: UI/UX Spezifikationen für Pipeline-Optimierung

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-010-pipeline-scalability-ux.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-010-pipeline-scalability-ux.md)  
**Fokus:** Detaillierte UI/UX Texte und Interaktionen

## 📝 Quick Actions im Karten-Footer

### Icon-Set mit Tooltips

```typescript
interface QuickActionConfig {
  icon: ReactNode;
  tooltip: string;
  action: string;
  visibleFor?: OpportunityStage[];
}

const QUICK_ACTIONS: QuickActionConfig[] = [
  {
    icon: "✅",
    tooltip: "Deal als gewonnen markieren – Auftrag bestätigt und abgeschlossen.",
    action: "mark_won",
    visibleFor: ['PROPOSAL', 'NEGOTIATION']
  },
  {
    icon: "❌",
    tooltip: "Deal als verloren markieren – Opportunity wurde abgelehnt oder zurückgezogen.",
    action: "mark_lost",
    visibleFor: ['LEAD', 'QUALIFIED', 'PROPOSAL', 'NEGOTIATION']
  },
  {
    icon: "🔄",
    tooltip: "Deal wieder in die aktive Pipeline aufnehmen.",
    action: "reactivate",
    visibleFor: ['CLOSED_LOST']
  },
  {
    icon: "📝",
    tooltip: "Partnerschaftsvereinbarung erneuern – Erinnerung oder Prozess starten.",
    action: "start_renewal",
    visibleFor: ['RENEWAL']
  },
  {
    icon: "📧",
    tooltip: "Kunden direkt kontaktieren – vorgefertigte Vorlage verfügbar.",
    action: "send_email",
    visibleFor: ['ALL']
  }
];
```

## ⚠️ WIP-Limit Warnungen

### Alert-Design

```typescript
const WIPLimitAlert: React.FC<{stage: string, current: number, limit: number}> = ({
  stage, current, limit
}) => (
  <Alert 
    severity="warning" 
    sx={{ 
      mb: 2,
      backgroundColor: '#fff3cd',
      border: '1px solid #ffeaa7'
    }}
  >
    <AlertTitle>⚠️ Achtung: Zu viele Deals in dieser Phase!</AlertTitle>
    Mehr als {limit} Opportunities können die Übersicht und 
    Bearbeitungsqualität beeinträchtigen. Aktuell: {current}/{limit}
  </Alert>
);
```

## 🔍 Filter-Bar Spezifikation

### Quick Filter Buttons

```typescript
const QUICK_FILTERS = [
  {
    label: "Meine Opportunities",
    icon: "👤",
    key: "myOpportunities",
    tooltip: "Zeige nur dir zugewiesene Opportunities"
  },
  {
    label: "Große Deals",
    icon: "💶",
    key: "highValue",
    tooltip: "Opportunities über 50.000€"
  },
  {
    label: "Schließen diese Woche",
    icon: "📅",
    key: "closingSoon",
    tooltip: "Expected Close Date innerhalb 7 Tagen"
  },
  {
    label: "Erneuerung fällig",
    icon: "⏳",
    key: "renewalDue",
    tooltip: "Verträge laufen in < 90 Tagen aus"
  },
  {
    label: "Aktion erforderlich",
    icon: "⚡",
    key: "needsAction",
    tooltip: "Opportunities die deine Aufmerksamkeit brauchen"
  }
];
```

### Advanced Filter Dropdown

```typescript
const ADVANCED_FILTER_LABEL = "Weitere Filter";
const ADVANCED_FILTERS = {
  salesRep: "Mitarbeiter",
  dateRange: "Zeitraum",
  customerType: "Kundenart",
  contractStatus: "Vertragsstatus",
  productGroup: "Produktgruppe"
};
```

## 👁️ Display Mode Switcher

```typescript
const DISPLAY_MODES = [
  {
    mode: "compact",
    label: "Kompakt",
    icon: "☰",
    tooltip: "Reduzierte Ansicht - nur essenzielle Informationen"
  },
  {
    mode: "standard",
    label: "Standard",
    icon: "🗂️",
    tooltip: "Normale Kartenansicht mit allen wichtigen Details"
  },
  {
    mode: "detailed",
    label: "Detailliert",
    icon: "🔍",
    tooltip: "Erweiterte Ansicht mit allen verfügbaren Informationen"
  }
];
```

## 🎯 Bulk Action Bar

```typescript
const BulkActionBar = ({ selectedCount }: { selectedCount: number }) => (
  <AppBar 
    position="fixed" 
    sx={{ 
      bottom: 0, 
      top: 'auto',
      backgroundColor: FRESHFOODZ_DARKBLUE
    }}
  >
    <Toolbar>
      <Typography sx={{ flexGrow: 1 }}>
        {selectedCount} Opportunities ausgewählt
      </Typography>
      <Button color="inherit" startIcon={<AssignIcon />}>
        Zuweisen
      </Button>
      <Button color="inherit" startIcon={<MoveIcon />}>
        Bewegen
      </Button>
      <Button color="inherit" startIcon={<TagIcon />}>
        Taggen
      </Button>
      <Button color="inherit" startIcon={<EmailIcon />}>
        E-Mail senden
      </Button>
      <Button color="inherit" startIcon={<CheckIcon />}>
        Als erledigt markieren
      </Button>
    </Toolbar>
  </AppBar>
);
```

## 🔄 Progressive Disclosure Toggle

```typescript
const ClosedStagesToggle = ({ showClosed, onToggle }) => (
  <Button
    variant="outlined"
    onClick={onToggle}
    startIcon={showClosed ? <VisibilityOffIcon /> : <VisibilityIcon />}
    sx={{ ml: 'auto' }}
  >
    {showClosed ? 'Geschlossene ausblenden' : 'Alle anzeigen'}
  </Button>
);

// Tooltip
const TOGGLE_TOOLTIP = "Zeigt oder verbirgt abgeschlossene und verlorene Opportunities.";
```

## 🏷️ Status-Badges auf Karten

```typescript
const STATUS_BADGES = {
  expiringContract: {
    icon: "⏳",
    getText: (days: number) => `Vereinbarung läuft in ${days} Tagen ab`,
    color: days => days < 30 ? 'error' : 'warning'
  },
  overdueRenewal: {
    icon: "🔴",
    text: "Achtung: Erneuerung überfällig",
    color: 'error'
  },
  readyToClose: {
    icon: "🟢",
    text: "Alle Voraussetzungen erfüllt",
    color: 'success'
  }
};
```

## 💬 Dialog-Texte

### Gewonnen/Verloren Bestätigung

```typescript
const WIN_LOSS_DIALOG = {
  won: {
    title: "Opportunity als gewonnen markieren",
    content: "Möchten Sie diese Opportunity wirklich als gewonnen markieren?",
    subtitle: "Diese Aktion kann nicht rückgängig gemacht werden.",
    confirmButton: "Als gewonnen markieren",
    cancelButton: "Abbrechen"
  },
  lost: {
    title: "Opportunity als verloren markieren",
    content: "Möchten Sie diese Opportunity wirklich als verloren markieren?",
    subtitle: "Sie können die Opportunity später wieder reaktivieren.",
    confirmButton: "Als verloren markieren",
    cancelButton: "Abbrechen"
  }
};
```

### Renewal Dialog

```typescript
const RENEWAL_DIALOG = {
  title: "Vertragsverlängerung starten",
  content: (date: string) => 
    `Die Partnerschaftsvereinbarung läuft am ${date} ab. Wie möchten Sie fortfahren?`,
  options: [
    {
      label: "Erinnerung an Kunde senden",
      icon: <EmailIcon />,
      action: "send_reminder"
    },
    {
      label: "Selbst erledigen",
      icon: <PersonIcon />,
      action: "handle_manually"
    },
    {
      label: "Später",
      icon: <ScheduleIcon />,
      action: "postpone"
    }
  ]
};
```

## 📊 Statistik-Box Texte

```typescript
const STATS_LABELS = {
  activeOpportunities: "Aktive Opportunities",
  openProposals: "Offene Angebote", 
  renewalsDue: "Erneuerungen fällig",
  successRate: "Erfolgsquote",
  
  // Tooltips
  tooltips: {
    activeOpportunities: "Alle Opportunities in aktiven Stages",
    openProposals: "Opportunities in Proposal-Stage",
    renewalsDue: "Verträge die in < 90 Tagen auslaufen",
    successRate: "Verhältnis gewonnen zu verloren (letzte 90 Tage)"
  }
};
```

## 💡 Motivierende Stage-Beschreibungen

```typescript
const STAGE_MOTIVATIONS = {
  LEAD: "Neue Chancen warten! Zeit für die Qualifizierung.",
  QUALIFIED: "Vielversprechende Kontakte - jetzt überzeugen!",
  PROPOSAL: "Angebote unterwegs - bleib am Ball!",
  NEGOTIATION: "Hier laufen die wichtigsten Gespräche. Konzentriere dich auf Deals mit hoher Abschlusswahrscheinlichkeit.",
  RENEWAL: "Die Partnerschaftsvereinbarung läuft ab. Jetzt Renewal-Prozess starten.",
  CLOSED_WON: "🎉 Erfolge feiern!",
  CLOSED_LOST: "Aus Erfahrung lernen - nächstes Mal klappt's!"
};
```

## 🔗 Verwandte Dokumente

- **Technische Implementierung:** [./performance-optimization.md](./performance-optimization.md)
- **Phasen-Plan:** [./implementation-phases.md](./implementation-phases.md)
- **Freshfoodz CI:** [/Users/joergstreeck/freshplan-sales-tool/docs/FRESH-FOODZ_CI.md](/Users/joergstreeck/freshplan-sales-tool/docs/FRESH-FOODZ_CI.md)
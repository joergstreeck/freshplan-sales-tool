# ⚡ FC-030 ONE-TAP ACTIONS (KOMPAKT)

**Erstellt:** 19.07.2025  
**Status:** 📋 READY TO START  
**Feature-Typ:** 🎨 FRONTEND  
**Priorität:** HIGH - Außendienst Essential  
**Geschätzt:** 0.5 Tag (in Quick Wins integriert)  

---

## 🧠 WAS WIR BAUEN

**Problem:** Außendienst tippt zu viel  
**Lösung:** Ein Tap für häufige Aktionen  
**Value:** 80% weniger Taps = 1h/Tag gespart  

> **Business Case:** Jede gesparte Sekunde = mehr Zeit beim Kunden

### 🎯 Core Actions:
- **Quick Note:** "War da" mit Timestamp
- **Follow-up:** Wiedervorlage morgen/nächste Woche
- **Status Update:** Erfolgreich/Verschoben/Abgesagt
- **Quick Call:** Direktwahl mit Log

---

## 🚀 SOFORT LOSLEGEN (15 MIN)

### 1. **Context Detection Service:**
```typescript
// Erkenne wo User gerade ist
const contextService = {
  getCurrentContext: () => ({
    view: 'customer_detail',
    customerId: '123',
    lastAction: 'view_contact',
    time: new Date()
  }),
  
  getSuggestedActions: (context) => {
    // Smart: Nach Meeting → "Follow-up"
    // Smart: Vor Ort → "Check-in"
    // Smart: Nach Anruf → "Notiz"
  }
}
```

### 2. **Action Button Component:**
```typescript
const OneTapButton = ({ action, context }) => (
  <Fab
    color="primary"
    onClick={() => executeAction(action, context)}
    sx={{ 
      position: 'fixed',
      bottom: 16,
      right: 16 
    }}
  >
    <ActionIcon type={action.icon} />
  </Fab>
);
```

### 3. **Integration Points:**
- Customer Detail Page
- Opportunity Cards  
- Calendar Events
- Map View

---

## 💡 SMART CONTEXT ACTIONS

### Nach Kundenbesuch:
```typescript
// Automatisch erkannt: GPS + Kalender
actions: [
  { id: 'check_in', label: 'War da ✓', icon: '📍' },
  { id: 'quick_note', label: 'Notiz', icon: '📝' },
  { id: 'follow_up', label: 'Wiedervorlage', icon: '📅' }
]
```

### In Pipeline View:
```typescript
// Pro Opportunity-Karte
actions: [
  { id: 'won', label: 'Gewonnen!', icon: '🎉' },
  { id: 'move_stage', label: 'Nächste Phase', icon: '➡️' },
  { id: 'schedule', label: 'Termin', icon: '📅' }
]
```

### Floating Action Menu:
```typescript
// Material-UI SpeedDial
<SpeedDial
  ariaLabel="Quick Actions"
  icon={<SpeedDialIcon />}
  direction="up"
>
  {actions.map(action => (
    <SpeedDialAction
      key={action.id}
      icon={action.icon}
      tooltipTitle={action.label}
      onClick={() => execute(action)}
    />
  ))}
</SpeedDial>
```

---

## 🔧 TECHNICAL IMPLEMENTATION

### Action Registry:
```typescript
const actionRegistry = new Map([
  ['quick_note', {
    execute: async (context) => {
      const note = await quickNoteDialog();
      await api.createNote(context.customerId, note);
      showSuccess('Notiz gespeichert!');
    }
  }],
  ['follow_up', {
    execute: async (context) => {
      const date = await selectDate(['morgen', 'nächste Woche']);
      await api.createTask(context.customerId, date);
    }
  }]
]);
```

### Smart Shortcuts:
```typescript
// Keyboard Support für Power User
useHotkeys('ctrl+1', () => executeQuickAction(1));
useHotkeys('ctrl+2', () => executeQuickAction(2));
useHotkeys('ctrl+3', () => executeQuickAction(3));
```

---

## 📱 MOBILE FIRST DESIGN

```typescript
// Touch-optimiert
const TouchAction = styled(Fab)`
  min-width: 56px;
  min-height: 56px;
  
  // Haptic Feedback
  &:active {
    transform: scale(0.95);
    vibrate(10);
  }
`;

// Swipe Actions
<SwipeableListItem
  onSwipeRight={() => executeAction('complete')}
  onSwipeLeft={() => executeAction('postpone')}
>
  {customerCard}
</SwipeableListItem>
```

---

## 🎯 SUCCESS METRICS

- **Ziel:** 80% weniger Taps für Top-5 Aktionen
- **Messung:** Action-Execution-Time < 2 Sekunden
- **Adoption:** 90% der Außendienst nutzt täglich

**Integration:** Tag 6-7 mit FC-020 Quick Wins!

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[⚡ FC-020 Quick Wins](/docs/features/PLANNED/20_quick_wins/FC-020_KOMPAKT.md)** - Gemeinsame Implementation
- **[📱 FC-022 Mobile Light](/docs/features/PLANNED/22_mobile_light/FC-022_KOMPAKT.md)** - Mobile UI Base
- **[👥 M5 Customer Refactor](/docs/features/PLANNED/12_customer_refactor_m5/M5_KOMPAKT.md)** - Customer Context

### ⚡ Context & Datenquellen:
- **[📈 FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_KOMPAKT.md)** - Recent Activities
- **[📊 M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_KOMPAKT.md)** - Pipeline Actions
- **[🎤 FC-029 Voice-First](/docs/features/PLANNED/29_voice_first/FC-029_KOMPAKT.md)** - Voice Shortcuts

### 🚀 Ermöglicht folgende Features:
- **[📱 FC-031 Smart Templates](/docs/features/PLANNED/31_smart_templates/FC-031_KOMPAKT.md)** - Template Quick Actions
- **[🏆 FC-017 Sales Gamification](/docs/features/PLANNED/99_sales_gamification/FC-017_KOMPAKT.md)** - Achievement Actions
- **[🎯 FC-027 Magic Moments](/docs/features/PLANNED/27_magic_moments/FC-027_KOMPAKT.md)** - Smart Suggestions

### 🎨 UI Integration:
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)** - Action Buttons
- **[⚡ M2 Quick Create](/docs/features/ACTIVE/05_ui_foundation/M2_QUICK_CREATE_KOMPAKT.md)** - Quick Action Dialog
- **[📱 FC-018 Mobile PWA](/docs/features/PLANNED/18_mobile_pwa/FC-018_KOMPAKT.md)** - Offline Actions

### 🔧 Technische Details:
- [FC-030_IMPLEMENTATION_GUIDE.md](./FC-030_IMPLEMENTATION_GUIDE.md) *(geplant)* - Context Detection Setup
- [FC-030_DECISION_LOG.md](./FC-030_DECISION_LOG.md) *(geplant)* - FAB vs. Speed Dial
- [ACTION_CATALOG.md](./ACTION_CATALOG.md) *(geplant)* - Alle Quick Actions
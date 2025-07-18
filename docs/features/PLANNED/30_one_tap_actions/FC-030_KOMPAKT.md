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
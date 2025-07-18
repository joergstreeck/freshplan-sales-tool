# 💬 FC-012 TEAM COMMUNICATION & NOTIFICATIONS ⚡

**Feature Code:** FC-012  
**Feature-Typ:** 🔀 FULLSTACK  
**Geschätzter Aufwand:** 6-8 Tage  
**Priorität:** MEDIUM - Team Collaboration  
**ROI:** 50% schnellere Reaktionszeiten, bessere Team-Abstimmung  

---

## 🎯 PROBLEM & LÖSUNG IN 30 SEKUNDEN

**Problem:** Info-Silos - Verkäufer wissen nicht was Kollegen beim Kunden machen  
**Lösung:** Kunden-zentrierte Team-Kommunikation + Smart Notifications  
**Impact:** Keine doppelten Anrufe, koordinierte Kundenbetreuung  

---

## 🧭 KOMMUNIKATIONS-HUB PRO KUNDE

```
KUNDE: Müller GmbH
├── 💬 Team Chat (nur für diesen Kunden)
├── 📋 Aktivitäts-Stream (wer macht was)
├── 🔔 Smart Notifications (wichtige Events)
└── 📎 Geteilte Notizen & Dateien
```

---

## 📋 FEATURES IM DETAIL

### 1. Kunden-Chat
```typescript
// Direkt in der CustomerDetail View
<CustomerTeamChat customerId={customer.id}>
  <ChatMessage>
    @thomas: Kunde hat Interesse an Sonderkonditionen
  </ChatMessage>
  <ChatMessage>
    @sarah: Habe gestern mit Einkauf gesprochen, Budget: 50k
  </ChatMessage>
  <ChatMessage type="system">
    📞 Michael hat angerufen (vor 10 Min)
  </ChatMessage>
</CustomerTeamChat>
```

### 2. Aktivitäts-Stream
```typescript
interface CustomerActivity {
  type: 'call' | 'email' | 'meeting' | 'note' | 'opportunity';
  user: User;
  timestamp: Date;
  summary: string;
  details?: any;
}

// Automatisch aus allen Modulen aggregiert
<ActivityTimeline customerId={customer.id}>
  <TimelineItem icon={<Phone />}>
    Thomas: Anruf - Bedarfsanalyse (vor 2h)
  </TimelineItem>
  <TimelineItem icon={<Email />}>
    Sarah: Angebot versendet (gestern)
  </TimelineItem>
  <TimelineItem icon={<TrendingUp />}>
    System: Opportunity auf "Proposal" (vor 3 Tagen)
  </TimelineItem>
</ActivityTimeline>
```

### 3. Smart Notifications
```java
@ApplicationScoped
public class NotificationEngine {
    
    // Regel-basierte Notifications
    public void processCustomerEvent(CustomerEvent event) {
        List<NotificationRule> rules = getRulesForEvent(event);
        
        for (NotificationRule rule : rules) {
            if (rule.matches(event)) {
                notifyRelevantUsers(rule, event);
            }
        }
    }
    
    // Beispiel-Regeln:
    // - "Kunde wurde 3x diese Woche kontaktiert" → Team Lead
    // - "Opportunity > 50k erstellt" → Sales Manager
    // - "Kunde war 30 Tage inaktiv" → Account Owner
    // - "Kollege erwähnt dich im Chat" → Direkt-Notification
}
```

### 4. @Mentions & Collaboration
```typescript
// In jedem Textfeld
<MentionInput
  placeholder="Schreibe eine Notiz... @kollege"
  onMention={(user) => notifyUser(user, 'mentioned')}
  suggestions={teamMembers}
/>

// Rich-Text Notizen
<CustomerNote>
  Wichtig: @thomas bitte vor nächstem Termin mit 
  @sarah abstimmen wegen Rabattkonditionen.
  
  Der Kunde erwartet eine Rückmeldung bis Freitag!
</CustomerNote>
```

---

## 🔔 NOTIFICATION CHANNELS

### In-App Notifications
```
┌─────────────────────────────────────┐
│ 🔔 3 neue Benachrichtigungen        │
├─────────────────────────────────────┤
│ 💬 @thomas hat dich erwähnt         │
│    "Müller GmbH: Kannst du..."      │
│                          vor 5 Min   │
├─────────────────────────────────────┤
│ 📞 Verpasst: Müller GmbH           │
│    Kollege Michael im Gespräch      │
│                          vor 20 Min  │
└─────────────────────────────────────┘
```

### Push-Notifications (optional)
- Browser Push API
- Mobile App später
- Email-Digest täglich/wöchentlich

### Notification Settings
```typescript
interface NotificationPreferences {
  mentions: 'instant' | 'digest' | 'off';
  customerUpdates: 'all' | 'important' | 'off';
  teamActivity: 'realtime' | 'summary' | 'off';
  channels: {
    inApp: boolean;
    email: boolean;
    push: boolean;
  };
}
```

---

## 🏗️ TECHNISCHE ARCHITEKTUR

### Backend Events
```java
// Domain Events für alle Aktionen
@ApplicationScoped
public class CustomerEventPublisher {
    
    @Inject
    Event<CustomerActivityEvent> activityEvents;
    
    public void publishActivity(
        UUID customerId,
        UUID userId,
        ActivityType type,
        String summary
    ) {
        var event = new CustomerActivityEvent(
            customerId,
            userId,
            type,
            summary,
            Instant.now()
        );
        
        // Async fire für Performance
        activityEvents.fireAsync(event);
    }
}
```

### WebSocket für Real-Time
```typescript
// Real-time Updates
const useCustomerRealtimeUpdates = (customerId: string) => {
  useEffect(() => {
    const ws = new WebSocket(`/api/ws/customer/${customerId}`);
    
    ws.onmessage = (event) => {
      const update = JSON.parse(event.data);
      
      switch (update.type) {
        case 'chat_message':
          addChatMessage(update.payload);
          break;
        case 'activity':
          prependActivity(update.payload);
          break;
        case 'user_typing':
          showTypingIndicator(update.userId);
          break;
      }
    };
    
    return () => ws.close();
  }, [customerId]);
};
```

---

## 📊 SUCCESS METRICS

- **Response Time:** -50% durch Team-Awareness
- **Doppel-Kontakte:** -80% weniger
- **Team Satisfaction:** Bessere Zusammenarbeit
- **Customer Experience:** Koordinierte Betreuung

---

## 🚀 IMPLEMENTATION ROADMAP

**Phase 1: Activity Stream (2 Tage)**
- Event System aufbauen
- Timeline Component
- Aktivitäten aggregieren

**Phase 2: Team Chat (2 Tage)**
- Chat Backend
- Real-time WebSocket
- Message Persistence

**Phase 3: Notifications (2 Tage)**
- Notification Engine
- Rule Builder
- User Preferences

**Phase 4: Integration (2 Tage)**
- In alle Module einbauen
- @Mentions überall
- Testing & Polish

---

**Implementation Guide:** [FC-012_IMPLEMENTATION_GUIDE.md](./FC-012_IMPLEMENTATION_GUIDE.md)  
**Decision Log:** [FC-012_DECISION_LOG.md](./FC-012_DECISION_LOG.md)
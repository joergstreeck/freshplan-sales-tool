# 📅 FC-014 ACTIVITY TIMELINE (KOMPAKT)

**Feature Code:** FC-014  
**Feature-Typ:** 🔀 FULLSTACK  
**Geschätzter Aufwand:** 5-6 Tage  
**Priorität:** HIGH - Zentraler Kontext für Sales  
**ROI:** 360° Kundensicht = bessere Abschlussrate  

---

## 🎯 PROBLEM & LÖSUNG IN 30 SEKUNDEN

**Problem:** Kunde anruft - "Wir hatten doch letzte Woche..." - Keine Ahnung wovon er spricht  
**Lösung:** Chronologische Timeline ALLER Interaktionen pro Kunde  
**Impact:** Immer im Kontext, professioneller Auftritt, keine verlorenen Infos  

---

## 🧠 ACTIVITY TIMELINE KONZEPT

```
CUSTOMER DETAIL VIEW
┌─────────────────────────────────────┐
│ FreshFoodz GmbH                     │
├─────────────────────────────────────┤
│ 📅 ACTIVITY TIMELINE                │
│                                     │
│ HEUTE                               │
│ 09:15 📧 Email erhalten             │
│       "Interesse an Sonderkondit..."│
│       → [Antworten] [Als Task]      │
│                                     │
│ GESTERN                             │
│ 14:30 📞 Anruf (5:23 Min)          │
│       "Besprach Q1 Bestellung"      │
│       Von: Max Müller               │
│                                     │
│ 15.07.2025                          │
│ 10:00 🤝 Meeting bei Kunde          │
│       "Jahresgespräch 2025"         │
│       Teilnehmer: TH, MM, Kunde: JS│
│       → [Protokoll ansehen]         │
│                                     │
│ 10.07.2025                          │
│ 🎯 Opportunity erstellt             │
│       "Q3 Großbestellung"           │
│       Wert: 45.000€                 │
│                                     │
│ [Mehr laden...]                     │
└─────────────────────────────────────┘
```

---

## 📋 FEATURES IM DETAIL

### 1. Aktivitätstypen & Icons

```typescript
enum ActivityType {
  EMAIL_SENT = '📤',
  EMAIL_RECEIVED = '📧',
  PHONE_INBOUND = '📞',
  PHONE_OUTBOUND = '☎️',
  MEETING = '🤝',
  NOTE = '📝',
  OPPORTUNITY = '🎯',
  CONTRACT = '📄',
  TASK = '✅',
  SYSTEM = '🤖'
}

interface Activity {
  id: string;
  type: ActivityType;
  timestamp: Date;
  title: string;
  description?: string;
  participants: User[];
  relatedEntities: {
    customerId: string;
    opportunityId?: string;
    contactId?: string;
  };
  metadata: Record<string, any>; // Email-ID, Call Duration, etc.
  attachments?: Attachment[];
}
```

### 2. Smart Grouping & Filtering

```typescript
// Intelligente Gruppierung
const TimelineView = () => {
  const groups = [
    { label: 'Heute', activities: todayActivities },
    { label: 'Gestern', activities: yesterdayActivities },
    { label: 'Diese Woche', activities: thisWeekActivities },
    { label: 'Letzter Monat', activities: lastMonthActivities }
  ];
  
  // Quick Filters
  const filters = [
    { icon: '📧', label: 'Emails', count: 23 },
    { icon: '📞', label: 'Anrufe', count: 15 },
    { icon: '🤝', label: 'Meetings', count: 4 },
    { icon: '🎯', label: 'Opportunities', count: 7 }
  ];
};
```

### 3. Inline Actions pro Aktivität

```typescript
const ActivityCard: React.FC<{activity}> = ({ activity }) => (
  <Card sx={{ mb: 2 }}>
    <CardContent>
      <Box display="flex" justifyContent="space-between">
        <Typography variant="subtitle2">
          {activity.type} {activity.title}
        </Typography>
        <Typography variant="caption" color="text.secondary">
          {formatRelativeTime(activity.timestamp)}
        </Typography>
      </Box>
      
      {activity.description && (
        <Typography variant="body2" sx={{ mt: 1 }}>
          {activity.description}
        </Typography>
      )}
      
      {/* Kontextuelle Aktionen */}
      <Box mt={2}>
        {activity.type === 'EMAIL_RECEIVED' && (
          <>
            <Button size="small" startIcon={<Reply />}>
              Antworten
            </Button>
            <Button size="small" startIcon={<Task />}>
              Als Task
            </Button>
          </>
        )}
        
        {activity.type === 'PHONE_INBOUND' && (
          <Button size="small" startIcon={<Phone />}>
            Zurückrufen
          </Button>
        )}
        
        {activity.type === 'MEETING' && (
          <Button size="small" startIcon={<Description />}>
            Protokoll
          </Button>
        )}
      </Box>
    </CardContent>
  </Card>
);
```

### 4. Real-time Updates

```typescript
// Mit Polling (gemäß D2 Entscheidung)
const useActivityTimeline = (customerId: string) => {
  const { data, refetch } = useQuery(
    ['activities', customerId],
    () => fetchActivities(customerId),
    {
      refetchInterval: 30000, // 30s Polling
      refetchIntervalInBackground: false
    }
  );
  
  // Notification bei neuen Aktivitäten
  useEffect(() => {
    if (data?.hasNewActivities) {
      showNotification('Neue Aktivität bei diesem Kunden!');
    }
  }, [data]);
};
```

### 5. Backend Activity Service

```java
@ApplicationScoped
public class ActivityService {
    
    @Inject
    ActivityRepository repository;
    
    @Inject
    Event<ActivityCreatedEvent> activityEvents;
    
    public List<ActivityDTO> getCustomerTimeline(UUID customerId, 
                                                  TimelineFilter filter) {
        return repository
            .findByCustomerId(customerId)
            .stream()
            .filter(filter::matches)
            .sorted(Comparator.comparing(Activity::getTimestamp).reversed())
            .map(this::enrichActivity)
            .collect(Collectors.toList());
    }
    
    private ActivityDTO enrichActivity(Activity activity) {
        // Zusätzliche Daten laden (Participants, Related Entities)
        ActivityDTO dto = mapper.toDTO(activity);
        
        // Email-Preview
        if (activity.getType() == ActivityType.EMAIL) {
            dto.setPreview(emailService.getPreview(activity.getMetadata()));
        }
        
        // Call Duration formatieren
        if (activity.getType() == ActivityType.PHONE) {
            dto.setDuration(formatDuration(activity.getMetadata()));
        }
        
        return dto;
    }
}
```

### 6. Activity Sources Integration

```typescript
// Automatisches Activity Tracking
const activitySources = {
  // Email Integration
  email: {
    onReceived: (email) => createActivity({
      type: 'EMAIL_RECEIVED',
      title: email.subject,
      description: email.preview
    }),
    
    onSent: (email) => createActivity({
      type: 'EMAIL_SENT',
      title: email.subject
    })
  },
  
  // Phone Integration
  phone: {
    onCall: (call) => createActivity({
      type: call.direction === 'IN' ? 'PHONE_INBOUND' : 'PHONE_OUTBOUND',
      title: `Anruf ${call.direction === 'IN' ? 'von' : 'an'} ${call.number}`,
      metadata: { duration: call.duration }
    })
  },
  
  // System Events
  system: {
    onOpportunityCreated: (opp) => createActivity({
      type: 'OPPORTUNITY',
      title: `Opportunity erstellt: ${opp.title}`,
      metadata: { value: opp.value }
    })
  }
};
```

---

## 🎯 BUSINESS VALUE

- **Kontext-Switch Zeit:** Von 5 Min auf 10 Sek
- **Verpasste Follow-ups:** -80%
- **Kundenzufriedenheit:** +25% durch bessere Vorbereitung
- **Team-Transparenz:** Jeder weiß was läuft

---

## 🚀 IMPLEMENTIERUNGS-PHASEN

1. **Phase 1:** Basis Timeline mit manuellen Einträgen
2. **Phase 2:** Email-Integration (BCC-to-CRM)
3. **Phase 3:** Phone + Calendar Integration
4. **Phase 4:** AI-Summary pro Kunde

---

## 🔗 ABHÄNGIGKEITEN

- **Integration mit:** FC-003 Email, FC-012 Team Communication
- **Benötigt:** Activity Entity + Service
- **Nice-to-have:** Elasticsearch für Volltext-Suche

---

## 📊 SUCCESS METRICS

- **Activities tracked:** > 90% aller Interaktionen
- **Timeline Load Time:** < 500ms
- **User Adoption:** > 80% schauen Timeline vor Kontakt
- **Data Completeness:** > 85%

---

**Nächster Schritt:** Activity Entity + Repository implementieren

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[👥 M5 Customer Refactor](/docs/features/PLANNED/12_customer_refactor_m5/M5_KOMPAKT.md)** - Customer-Context für Timeline
- **[🔒 FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_KOMPAKT.md)** - Activity-Berechtigungen
- **[👥 FC-009 Permissions System](/docs/features/ACTIVE/04_permissions_system/FC-009_KOMPAKT.md)** - Team-basierte Sichtbarkeit

### ⚡ Activity-Quellen:
- **[📧 FC-003 E-Mail Integration](/docs/features/PLANNED/06_email_integration/FC-003_KOMPAKT.md)** - Email Activities
- **[📊 M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_KOMPAKT.md)** - Opportunity Events
- **[💬 FC-012 Team Communication](/docs/features/PLANNED/14_team_communication/FC-012_KOMPAKT.md)** - Team Activities

### 🚀 Ermöglicht folgende Features:
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)** - Recent Activities Widget
- **[📊 FC-007 Chef-Dashboard](/docs/features/PLANNED/10_chef_dashboard/FC-007_KOMPAKT.md)** - Activity Analytics
- **[🎯 FC-027 Magic Moments](/docs/features/PLANNED/27_magic_moments/FC-027_KOMPAKT.md)** - Activity-basierte Insights

### 🎨 UI Integration:
- **[🧭 M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_NAVIGATION_KOMPAKT.md)** - Activity-Menüpunkt
- **[➕ M2 Quick Create](/docs/features/ACTIVE/05_ui_foundation/M2_QUICK_CREATE_KOMPAKT.md)** - Quick Activity Entry
- **[📊 M6 Analytics Module](/docs/features/PLANNED/13_analytics_m6/M6_KOMPAKT.md)** - Activity Reports

### 🔧 Technische Details:
- [FC-014_IMPLEMENTATION_GUIDE.md](./FC-014_IMPLEMENTATION_GUIDE.md) *(geplant)* - Event Sourcing Pattern
- [FC-014_DECISION_LOG.md](./FC-014_DECISION_LOG.md) *(geplant)* - Polling vs. WebSocket
- [ACTIVITY_SCHEMA.md](./ACTIVITY_SCHEMA.md) *(geplant)* - Activity Data Model
# FC-013: Activity & Notes System - Technisches Konzept

**Feature Code:** FC-013  
**Status:** 🟡 KONZEPT  
**Priorität:** HIGH (Core CRM Funktionalität)  
**Geschätzter Aufwand:** 4-5 Tage  

## 📋 Übersicht

Ein umfassendes System für Notizen, Kommentare, Activity-Tracking und Reminder direkt an Opportunities und Kunden. Ermöglicht lückenlose Dokumentation aller Kundeninteraktionen und automatische Erinnerungen bei Inaktivität.

## 🎯 Kernfunktionalität

### Feature-Set
1. **Notizen & Kommentare** an jeder Opportunity-Karte
2. **Activity Checkboxes** (z.B. "Kunde informiert", "Vertrag versendet")
3. **Automatische Inaktivitäts-Reminder** (14 Tage ohne Aktion)
4. **Aufgaben & Wiedervorlagen** für Team-Mitglieder
5. **Erweiterte Filter** und Volltextsuche

## 🔗 Abhängigkeiten & Integration

### Direkte Abhängigkeiten
- **M4 Opportunity Pipeline:** Notizen an Karten
- **FC-012 Audit Trail:** Alle Activities werden auditiert
- **FC-003 Email Integration:** Email-Activities tracken
- **FC-010 Pipeline Scalability:** Erweiterte Filter-Integration

### Betroffene Features
- **M1 Dashboard:** Activity-Widgets und Reminder
- **FC-011 Pipeline-Cockpit:** Activity-Timeline im Cockpit
- **FC-009 Contract Renewal:** Renewal-Activities

## 📐 Architektur

### Activity Types
```typescript
enum ActivityType {
  // Kommunikation
  NOTE = 'NOTE',
  COMMENT = 'COMMENT',
  EMAIL_SENT = 'EMAIL_SENT',
  EMAIL_RECEIVED = 'EMAIL_RECEIVED',
  PHONE_CALL = 'PHONE_CALL',
  MEETING = 'MEETING',
  
  // Checkboxes/Milestones
  CUSTOMER_INFORMED = 'CUSTOMER_INFORMED',
  CONTRACT_SENT = 'CONTRACT_SENT',
  OFFER_SENT = 'OFFER_SENT',
  FOLLOW_UP_SCHEDULED = 'FOLLOW_UP_SCHEDULED',
  
  // System-Events
  STAGE_CHANGED = 'STAGE_CHANGED',
  VALUE_UPDATED = 'VALUE_UPDATED',
  OWNER_CHANGED = 'OWNER_CHANGED',
  
  // Reminders
  TASK_CREATED = 'TASK_CREATED',
  TASK_COMPLETED = 'TASK_COMPLETED',
  REMINDER_SET = 'REMINDER_SET'
}
```

### Data Model
```typescript
interface Activity {
  id: string;
  type: ActivityType;
  entityType: 'opportunity' | 'customer' | 'contract';
  entityId: string;
  
  // Content
  title: string;
  content?: string;
  metadata?: Record<string, any>;
  
  // User & Time
  createdBy: string;
  createdAt: DateTime;
  completedAt?: DateTime;
  
  // Reminders
  dueDate?: DateTime;
  assignedTo?: string;
  reminderSent?: boolean;
  
  // Relations
  parentActivityId?: string; // Für Antworten/Follow-ups
  attachments?: Attachment[];
}
```

## 📚 Detail-Dokumente

1. **Backend Architecture:** [./FC-013/backend-architecture.md](./FC-013/backend-architecture.md)
2. **Frontend Components:** [./FC-013/frontend-components.md](./FC-013/frontend-components.md)
3. **Reminder Engine:** [./FC-013/reminder-engine.md](./FC-013/reminder-engine.md)
4. **Search Implementation:** [./FC-013/search-implementation.md](./FC-013/search-implementation.md)

## 🚀 Implementierungs-Phasen

### Phase 1: Basis Activity System (2 Tage)
- Activity Entity & Repository
- REST API für CRUD
- Integration mit Audit Trail
- Basis UI Components

### Phase 2: Checkboxes & Quick Actions (1 Tag)
- Vordefinierte Activity-Types
- Quick-Action Buttons
- Timestamp-Tracking
- Visual Status Indicators

### Phase 3: Reminder Engine (1 Tag)
- Scheduled Jobs für Inaktivität
- Task Assignment System
- Email/Push Notifications
- Escalation Logic

### Phase 4: Search & Filter (1 Tag)
- Elasticsearch Integration
- Volltext-Suche
- Advanced Filter UI
- Saved Searches

## ⚠️ Technische Herausforderungen

1. **Performance bei vielen Activities**
2. **Real-time Updates über WebSocket**
3. **Reminder-Zuverlässigkeit**
4. **Such-Performance bei großen Datenmengen**

## 📊 Messbare Erfolge

- **Activity Coverage:** > 95% aller Interaktionen erfasst
- **Reminder Effectiveness:** 80% reagieren auf erste Erinnerung
- **Search Speed:** < 100ms für Volltext-Suche
- **User Adoption:** 90% nutzen Quick Actions täglich

## 🔄 Updates & Status

**24.07.2025:** Initiales Konzept erstellt basierend auf CRM Best Practices
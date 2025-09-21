# FC-013 Activity & Notes System - Integrations-Planung

**Datum:** 24.07.2025  
**Feature:** FC-013 Activity & Notes System  
**Status:** Technisches Konzept fertiggestellt  

## 📋 Zusammenfassung

FC-013 fügt ein umfassendes Activity & Notes System zum FreshPlan CRM hinzu. Dies hat weitreichende Auswirkungen auf andere Features.

## 🔗 Integration Points & Änderungen

### 1. M4 Opportunity Pipeline (FC-002-M4)
**Änderungen:**
- Alle Stage-Wechsel werden als Activities geloggt
- Quick-Action Checkboxes direkt auf Opportunity-Karten
- Automatische Inaktivitäts-Reminder nach 14 Tagen
- Activity-Timeline in der Detail-Ansicht

**Technisch:**
- Neue UI-Komponenten auf den Opportunity-Karten
- WebSocket für Real-time Activity Updates
- Activity-Count Badge auf jeder Karte

### 2. FC-012 Audit Trail System
**Änderungen:**
- Neue Audit Event Types für Activities
  - ACTIVITY_CREATED
  - TASK_COMPLETED
  - REMINDER_SENT
  - NOTE_ADDED
- Alle Activities werden automatisch auditiert
- @Auditable Annotation auf Activity-Service-Methoden

**Technisch:**
- AuditService wird in ActivityService injected
- Hash-Chain Integrity auch für Activities

### 3. FC-010 Pipeline Scalability & UX
**Änderungen:**
- Erweiterte Filter-Optionen
  - Nach letzter Aktivität filtern
  - Opportunities mit offenen Tasks
  - Inaktive Opportunities (> 14 Tage)
- Performance-Optimierung für Activity-Queries

**Technisch:**
- Filter-Criteria erweitert um Activity-basierte Filter
- Index-Optimierung für Activity-Joins

### 4. FC-011 Pipeline-Cockpit Integration
**Änderungen:**
- Activity-Timeline wird im Cockpit-Kontext angezeigt
- Quick-Actions aus dem Cockpit heraus
- Activity-basierte Navigation

**Technisch:**
- CockpitState erweitert um activityContext
- Shared Components zwischen Pipeline und Cockpit

### 5. FC-009 Contract Renewal Management
**Änderungen:**
- Contract-Expiry erzeugt automatisch Task-Activities
- Renewal-Activities mit speziellen Templates
- Eskalations-Activities bei kritischen Fristen

**Technisch:**
- ContractMonitoringService triggert ActivityService
- Spezielle Activity-Types für Contract-Events

### 6. FC-003 Email Integration (zukünftig)
**Änderungen:**
- Emails werden als Activities geloggt
- Email-Templates aus Activity-Context
- Activity-Timeline zeigt Email-Kommunikation

### 7. M1 Dashboard
**Änderungen:**
- Task-Widget für offene Aufgaben
- Activity-Feed Widget
- Inaktivitäts-Warnungen

### 8. M5 Customer Management
**Änderungen:**
- Customer-Activities im Kunden-Cockpit
- Activity-Timeline pro Kunde
- Aggregierte Activities über alle Opportunities

## 📊 Datenbank-Schema Erweiterungen

### Neue Tabellen:
- `activities` - Haupt-Activity-Tabelle
- `activity_attachments` - Dateianhänge
- `activity_templates` - Quick-Action Templates
- `user_reminder_settings` - User-spezifische Settings
- `saved_searches` - Gespeicherte Filter

### Neue Indizes:
- `idx_activity_entity` - für Entity-basierte Queries
- `idx_activity_due` - für Reminder-Engine
- `idx_activity_search` - Full-Text Search
- `idx_activity_assigned` - für User-Tasks

## 🚨 Kritische Punkte

1. **Performance:** Bei vielen Activities pro Entity
   - Lösung: Pagination & Virtual Scrolling
   
2. **Real-time Updates:** WebSocket-Management
   - Lösung: Selective Updates nur für sichtbare Entities
   
3. **Reminder-Zuverlässigkeit:** Scheduled Jobs
   - Lösung: Redundante Checks & Monitoring

## ✅ Nächste Schritte

1. **Backend-Implementation beginnen**
   - Activity Entity & Repository
   - REST API Endpoints
   - Integration mit Audit Trail

2. **Frontend-Components**
   - Activity Timeline Component
   - Quick Action Bar
   - Filter UI

3. **Testing Strategy**
   - Unit Tests für Service Layer
   - Integration Tests für Reminder Engine
   - E2E Tests für User Workflows

## 📝 Offene Fragen

1. **Email-Delivery für Reminder:** Welcher Email-Service?
2. **Push-Notifications:** Mobile App geplant?
3. **Archivierung:** Alte Activities nach X Monaten?
4. **GDPR:** Löschung von Activities bei Kunden-Löschung?

Diese Fragen sollten vor der Implementation geklärt werden.
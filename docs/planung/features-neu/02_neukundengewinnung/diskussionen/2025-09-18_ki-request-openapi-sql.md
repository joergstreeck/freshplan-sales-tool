# 📋 KI-Request: OpenAPI-Specs + SQL-Migrations für Complete Modules

**📊 Request-Typ:** Technical Implementation Documentation
**🎯 Zweck:** Vollständige API-Contracts + Database-Schema für alle 3 Module
**📅 Datum:** 2025-09-18
**🔗 Status:** Ready to Send

---

## 🚀 **REQUEST AN KI: Finale Technical Specs**

**Liebe KI,**

Perfekt! Wir haben alle Entscheidungen getroffen und sind ready für die finalen Technical Specs.

### **✅ FINALE ENTSCHEIDUNGEN:**

**1. Complete Module Development bestätigt**
- Alle 3 Module komplett entwickeln
- Keine V1/V2/V3-Phasen
- 20-24 Wochen für vollständige Implementation

**2. Jörgs Zusatzanforderungen integriert:**
- **Benutzerspezifische Provisionen/Lead-Zeiträume:** UserLeadSettings-Entity
- **all.inkl Mail-Provider:** Detaillierte Integration spezifiziert

**3. Antworten auf deine 5 Fragen:**
- **all.inkl Limits:** Server w1234567.kasserver.com, IMAP/SMTP über SSL, Rate-Limits zu testen
- **Provisionsregeln:** Start einfach mit UserLeadSettings, Rules-Engine später
- **Attributionsmodell:** Last-Touch Default, Time-Decay für Advanced
- **AI-Scoring:** Regelbasiert starten, AI optional
- **E-Mail-Retention:** 7 Jahre Aufbewahrung, 2 Jahre Maskierung

**4. Optimierungen basierend auf Würdigung:**
- **Parallelisierung:** E-Mail + Lead gleichzeitig (Woche 1-12)
- **Provisions vereinfacht:** UserLeadSettings statt complex Rules-Engine
- **all.inkl spezifiziert:** IMAP/SMTP + KAS API Details

---

## 📋 **BITTE LIEFERE JETZT:**

### **1. Vollständige OpenAPI-Spezifikationen (YAML)**

**Für alle 3 Module mit allen Advanced Features:**

**A) E-Mail-Posteingang APIs:**
```yaml
Benötigt:
- POST /email/accounts (all.inkl Setup)
- GET /email/threads (Triage-Inbox)
- POST /email/threads/{id}/classify
- POST /email/threads/{id}/to-lead
- WebSocket-Events für Cockpit-Integration
```

**B) Lead-Erfassung APIs:**
```yaml
Benötigt:
- POST /leads (mit UserLeadSettings)
- GET /leads (Filter/Search)
- POST /leads/{id}/activities
- POST /leads/{id}/stop-clock
- POST /leads/{id}/extend
- POST /leads/{id}/convert
- GET/PUT /users/{userId}/lead-settings (NEU)
```

**C) Kampagnen APIs:**
```yaml
Benötigt:
- POST /campaigns (A/B-Testing Support)
- POST /audiences (dynamische Segmente)
- POST /campaigns/{id}/schedule
- GET /attribution/report (Multi-Touch)
- POST /scoring/score (AI-Ready)
```

### **2. Vollständige SQL-Migrationsskripte**

**Alle Tabellen für PostgreSQL:**

**Core-Tables basierend auf deinem Schema:**
- `email_account`, `email_thread`, `email_message`
- `lead` (mit UserLeadSettings-Support), `lead_activity`, `stop_clock_period`
- `user_lead_settings` (NEU für benutzerspezifische Config)
- `campaign`, `audience`, `delivery`, `touch_event`
- `commission_*` Tables (vereinfacht für UserLeadSettings)

### **3. Event-Schema für Cockpit-Integration**

**WebSocket/SSE-Topics mit Payload-Beispielen:**
```yaml
Cockpit Smart-Updates:
- lead.created
- lead.reminder_due
- lead.protection_expired
- email.received
- mention.created
- campaign.delivery.sent
```

### **4. UserLeadSettings-Integration**

**Erweiterte APIs für benutzerspezifische Konfiguration:**
- User-Settings-Management
- Lead-Creation mit User-Defaults
- Provisions-Berechnung mit User-Rates
- Activity-Check-Zyklen pro User

### **5. all.inkl-spezifische Endpoints**

**Integration mit bestehender `/08_administration/integration/email-service/`:**
- all.inkl IMAP/SMTP-Konfiguration
- KAS API-Wrapper für Account-Management
- Health-Checks + Monitoring

---

## 🎯 **SPEZIFISCHE ANFORDERUNGEN:**

### **Copy & Paste Ready:**
- Vollständige OpenAPI YAML-Files
- Komplette SQL CREATE-Statements
- Beispiel-Requests/Responses
- Event-Payload-Schemas

### **UserLeadSettings-Focus:**
```typescript
// Diese Struktur MUSS in allen APIs unterstützt werden
interface UserLeadSettings {
  userId: string;
  defaultProtectionMonths: number;      // Default: 6
  defaultActivityCheckDays: number;     // Default: 60
  defaultGracePeriodDays: number;       // Default: 10
  firstYearCommissionRate: number;      // Default: 0.07
  followupYearCommissionRate: number;   // Default: 0.02
  monthlyAdvanceAmount: number;         // Default: 2000.00
}
```

### **all.inkl-Integration:**
```yaml
Server-Konfiguration:
  Host: "w1234567.kasserver.com" (dynamisch)
  IMAP: Port 993, SSL required
  SMTP: Port 465, SSL required
  Auth: E-Mail + Password (aus KAS)

KAS API:
  SOAP-Endpoint für Account-Management
  Separate KAS-Login-Credentials
  Account-Creation + Quota-Monitoring
```

### **Complete Features (nicht MVP):**
- A/B-Testing für Kampagnen
- Multi-Touch-Attribution
- Thread-Erkennung für E-Mails
- AI-Scoring-Hook (regelbasiert)
- Stop-the-Clock für Lead-Management
- DSGVO-Compliance (Retention/Maskierung)

---

## 📋 **ERWARTETE DELIVERABLES:**

1. **`neukundengewinnung-openapi.yaml`** - Vollständige API-Specs
2. **`neukundengewinnung-schema.sql`** - Complete Database-Migration
3. **`websocket-events.yaml`** - Event-Schema für Cockpit
4. **`all-inkl-integration.yaml`** - Provider-spezifische APIs
5. **Beispiel-Fixtures** für alle Entities (TestDataBuilder-kompatibel)

---

**🚀 Ready to implement! Alle Entscheidungen sind getroffen, alle Requirements dokumentiert. Jetzt brauchen wir die Copy&Paste-fähigen Technical Specs für die Complete Module Development! 💻**

---

## 📎 **ANHANG: Alle finale Dokumentation**

**Für deine Referenz - alle bisherigen Entscheidungen:**
- ✅ [System-Entscheidungen](./2025-09-18_system-entscheidungen-ki-empfehlungen.md)
- ✅ [KI-Würdigung](./2025-09-18_ki-complete-module-wuerdigung.md)
- ✅ [all.inkl-Integration](./2025-09-18_all-inkl-integration-spezifikation.md)
- ✅ [Handelsvertretervertrag-Requirements](./2025-09-18_handelsvertretervertrag-lead-requirements.md)
- ✅ [Finale Roadmap](./2025-09-18_finale-entwicklungsroadmap.md)
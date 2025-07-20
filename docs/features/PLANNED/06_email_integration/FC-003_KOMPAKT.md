# FC-003: E-Mail Integration - BCC-to-CRM ⚡

**Feature Code:** FC-003  
**Feature-Typ:** 🔀 FULLSTACK  
**Geschätzter Aufwand:** 5-7 Tage  
**Priorität:** HOCH - Direktes Nutzerfeedback  
**ROI:** 30-45 Min/Tag Zeitersparnis pro Verkäufer  

---

## 🎯 PROBLEM & LÖSUNG IN 30 SEKUNDEN

**Problem:** Verkäufer kopieren E-Mails manuell ins CRM = Zeitverschwendung  
**Lösung:** BCC an `kunde-123@crm.freshplan.de` → Automatisch zugeordnet  
**Impact:** 95%+ Automatisierung, <2 Sek Verarbeitung  

---

## 📐 ARCHITEKTUR-ÜBERSICHT

```
E-Mail → SMTP Receiver → Parser → Customer Matcher → Timeline
                                        ↓
                                  Triage Inbox (wenn unbekannt)
```

**Backend-Stack:** Java Mail API, Apache James/SMTP, S3 für Attachments  
**Frontend-Touch:** EmailTimeline.tsx, TriageInbox.tsx (vorhanden!)  

---

## 🏃 QUICK-START IMPLEMENTIERUNG

### Phase 1: Backend (2 Tage)
```java
@ApplicationScoped
public class EmailIngestionService {
    public void processIncomingEmail(IncomingEmail email) {
        // 1. Parse Customer-ID aus To-Address
        Optional<UUID> customerId = extractCustomerId(email.getTo());
        
        // 2. Zuordnen oder Triage
        if (customerId.isPresent()) {
            createCustomerActivity(customerId.get(), email);
        } else {
            addToTriageInbox(email);
        }
    }
}
```

### Phase 2: API & Model (1 Tag)
- Email Entity mit Thread-Support (Message-ID)
- REST: GET /api/customers/{id}/emails
- Integration in Timeline Events

### Phase 3: Frontend (2 Tage)
- EmailTimeline in Customer Detail
- Triage Inbox erweitern
- Thread-Gruppierung UI

---

## 🔗 DEPENDENCIES & INTEGRATION

**Abhängig von:**
- M5 Customer Timeline (als Event)
- M4 Activity System (E-Mail = Activity Type)
- M3 Cockpit (Triage Inbox bereits da!)

**Ermöglicht:**
- FC-004 Verkäuferschutz (Kontakt-Nachweis)
- FC-006 Mobile App (Push für neue E-Mails)

---

## ⚡ KRITISCHE ENTSCHEIDUNGEN

1. **SMTP-Server:** Eigener (Apache James) vs. Service (SendGrid)?
2. **Speicher-Dauer:** 2 Jahre Standard? DSGVO-konform?
3. **Verschlüsselung:** At-rest encryption für E-Mail-Body?

---

## 📊 SUCCESS METRICS

- **Adoption:** 80%+ Verkäufer nutzen BCC aktiv
- **Accuracy:** 95%+ korrekte Zuordnung
- **Performance:** <2s Verarbeitungszeit
- **ROI:** Break-even nach 2 Wochen (Zeitersparnis)

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[🔒 FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_KOMPAKT.md)** - User Authentication für Email-Zuordnung
- **[📈 FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_KOMPAKT.md)** - Email-Events in Customer Timeline

### 🚀 Ermöglicht folgende Features:
- **[📱 FC-012 Team Communication](/docs/features/PLANNED/14_team_communication/FC-012_KOMPAKT.md)** - Interne Email-Benachrichtigungen
- **[📞 FC-028 WhatsApp Business](/docs/features/PLANNED/28_whatsapp_integration/FC-028_KOMPAKT.md)** - Unified Communication Hub

### 🎨 UI Integration:
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)** - Email-Timeline in Customer View
- **[📊 M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_KOMPAKT.md)** - Email-History bei Opportunities

### 🔧 Technische Details:
- [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md) *(geplant)* - Vollständige technische Umsetzung

---

## 🚀 NÄCHSTER SCHRITT

1. SMTP-Server Entscheidung treffen
2. Email Entity modellieren
3. Triage Inbox UI erweitern

**Detaillierte Implementierung:** [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md)  
**Architektur-Details:** [Original FC-003](../../FC-003-email-integration.md)
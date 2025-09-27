---
module: "02_neukundengewinnung"
doc_type: "analyse"
status: "archived"
owner: "team/architecture"
updated: "2025-09-27"
---

# 🔍 Code-Analyse: Neukundengewinnung - Status vs. Planung

> **⚠️ Status:** OBSOLET seit 2025-09-27
>
> **Ersetzt durch:**
> - [`INVENTORY.md`](./INVENTORY.md) - Aktuelle Stack-Analyse & Gaps
> - [`API_CONTRACT.md`](./API_CONTRACT.md) - Frontend-Backend Contract
> - [`RESEARCH_ANSWERS.md`](./RESEARCH_ANSWERS.md) - Implementation-Fragen
>
> Dieses Dokument dokumentiert den Code-Stand vom 18.09.2025 (hauptsächlich Mocks).

**📊 Analyse-Typ:** Code vs. Dokumentation Verification
**🎯 Zweck:** Diskussionsgrundlage für weitere Planung
**📅 Datum:** 2025-09-18
**🔧 Analysierte Module:** Email-Posteingang, Lead-Erfassung, Kampagnen

---

## 🎯 Executive Summary

**Kernbefund:** Neukundengewinnung ist **hauptsächlich Frontend-Mock** mit minimaler Backend-Integration. Große Lücke zwischen FC-003 E-Mail-Integration Planung und aktueller Implementation.

**Status-Überblick:**
- **Email-Posteingang:** 🔴 **70% Mock-Implementation** - Nur UI vorhanden, keine Backend-Integration
- **Lead-Erfassung:** 🔴 **90% fehlend** - Nur Routing und UI-Mock
- **Kampagnen:** 🔴 **95% fehlend** - Nur Dashboard-Verlinkung

**Kritische Erkenntnis:** FC-003 E-Mail-Integration ist detailliert geplant aber **nicht implementiert**. Aktueller Code zeigt nur UI-Mockups ohne funktionierende Backend-Services.

---

## 📧 **Email-Posteingang - Detailanalyse**

### **Frontend-Status:**

**✅ Vorhanden:**
- `NeukundengewinnungDashboard.tsx` - Professionelle Dashboard-UI
- Navigation zu `/neukundengewinnung/posteingang` konfiguriert
- Triage-Inbox Concept in `MyDayColumnMUI.tsx` implementiert
- Mock-KPIs und UI-Layout vollständig

**🔴 Fehlend (kritisch):**
```typescript
// NICHT IMPLEMENTIERT:
- E-Mail-Empfang via SMTP
- Customer-Matching-Logik
- Email-Parser für Metadaten
- Attachment-Handler für S3
- Email-Entity im Backend
- BCC-to-CRM Automatisierung
```

### **Backend-Status:**

**🔍 Backend-Befund:**
- **Keine** Email-Entities gefunden
- **Keine** EmailService-Implementation
- **Keine** SMTP-Integration
- **Keine** E-Mail-REST-Endpoints

**📋 FC-003 Planung vs. Reality:**
```yaml
FC-003 Plan:
  ✅ Detailliert geplant (148 Zeilen, 4 Phasen)
  ✅ Tech-Stack definiert (Java Mail API, Apache James)
  ✅ Implementierungsphasen spezifiziert

Aktuelle Implementation:
  🔴 0% Backend-Integration
  🔴 Nur Frontend-Mock
  🔴 Triage-Inbox zeigt Mock-Daten
```

### **Triage-Inbox Analysis:**

**Code-Location:** `frontend/src/features/cockpit/components/MyDayColumnMUI.tsx:37`

```typescript
// AKTUELL: Mock-Integration
import { mockTriageItems } from '../data/mockData';

// GEPLANT aber nicht implementiert:
interface TriageEmailItem {
  id: string;
  from: string;
  subject: string;
  receivedAt: Date;
  customerMatch?: Customer;
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
}
```

**Gefundene Mock-Struktur:**
- Triage-Inbox Toggle funktional
- UI-Components professional umgesetzt
- **Aber:** Keine echten E-Mail-Daten

---

## 🎯 **Lead-Erfassung - Detailanalyse**

### **Frontend-Status:**

**✅ Minimal vorhanden:**
- Navigation zu `/neukundengewinnung/leads` konfiguriert
- Dashboard-Card mit Mock-KPIs
- Grundlegendes UI-Layout

**🔴 Fehlend (kritisch):**
```typescript
// NICHT GEFUNDEN:
- Lead-Erfassung-Komponenten
- Lead-Qualifizierung-UI
- BANT-Kriterien-Formulare
- Lead-zu-Customer-Konvertierung
- Lead-Pipeline-Management
```

### **Backend-Status:**

**🔍 Backend-Befund:**
- **Keine** Lead-Entity gefunden
- **Keine** Lead-Service-Implementation
- **Keine** Lead-Repository
- **Keine** Lead-REST-Endpoints

**🔍 Customer-Entity-Analyse:**
```java
// VORHANDEN: Customer.java
@Entity
public class Customer {
    // Vollständige Customer-Implementation
    // Aber: Keine Lead-Integration
}
```

**📋 Gap-Analyse:**
```yaml
Benötigt für Lead-Management:
  🔴 Lead-Entity (Status, Source, Qualification)
  🔴 Lead-to-Customer-Migration
  🔴 BANT-Scoring-System
  🔴 Lead-Assignment-Logic
  🔴 Lead-Pipeline-Stages
```

---

## 📈 **Kampagnen - Detailanalyse**

### **Frontend-Status:**

**✅ Minimal vorhanden:**
- Navigation zu `/neukundengewinnung/kampagnen`
- Dashboard-Card mit Mock-Stats (5 aktiv, 12% Conv. Rate)

**🔴 Fehlend (praktisch alles):**
```typescript
// NICHT GEFUNDEN:
- Campaign-Management-UI
- E-Mail-Template-System
- Campaign-Analytics
- Automation-Workflows
- A/B-Testing-Framework
```

### **Backend-Status:**

**🔍 Backend-Befund:**
- **Keine** Campaign-Entities
- **Keine** Template-System
- **Keine** E-Mail-Automation
- **Keine** Campaign-Analytics

**📋 Gap-Analyse:**
```yaml
Für Kampagnen-Management benötigt:
  🔴 Campaign-Entity + Repository
  🔴 Email-Template-System
  🔴 Contact-List-Management
  🔴 Send-Schedule-Engine
  🔴 Analytics-Tracking
  🔴 A/B-Testing-Infrastructure
```

---

## 🗺️ **Navigation & Routing-Analyse**

### **Aktuelle Navigation:**

**Code-Location:** `frontend/src/config/navigation.config.ts`

```typescript
{
  path: '/neukundengewinnung',
  label: 'Neukundengewinnung',
  icon: PersonAddIcon,
  subItems: [
    { path: '/neukundengewinnung/posteingang', label: 'E-Mail Posteingang' },
    { path: '/neukundengewinnung/leads', label: 'Lead-Erfassung' },
    { path: '/neukundengewinnung/kampagnen', label: 'Kampagnen' }
  ]
}
```

**✅ Navigation funktional:**
- Alle Routen konfiguriert
- Dashboard-Integration professionell
- Sidebar-Navigation vollständig

**🔴 Endpunkte führen zu 404:**
- Nur Dashboard (`/neukundengewinnung`) implementiert
- Alle Sub-Routes fehlen

---

## 📊 **Dashboard-Analyse**

### **NeukundengewinnungDashboard.tsx - Detailbefund:**

**✅ Professionelle UI-Implementation:**
```typescript
// 372 Zeilen hochqualitative MUI-Implementation
- FreshFoodz CI-compliant (#94C456, #004F7B)
- KPI-Overview mit Mock-Daten
- Lead-Funnel-Visualization
- Recent-Leads-Timeline
- Upcoming-Actions-Liste
```

**📊 Mock-KPIs analysiert:**
```typescript
const leadTools: LeadCard[] = [
  {
    title: 'E-Mail Posteingang',
    stats: { primary: '23 neue', secondary: '142 gesamt' }
  },
  {
    title: 'Lead-Erfassung',
    stats: { primary: '67 Leads', secondary: 'Diese Woche' }
  },
  {
    title: 'Kampagnen',
    stats: { primary: '5 aktiv', secondary: '12% Conv. Rate' }
  }
];
```

**🎯 Business-Impact:**
- Dashboard suggeriert funktionale Features
- Mock-Daten täuschen Vollständigkeit vor
- **Risiko:** Business erwartet mehr als implementiert

---

## 🔗 **Integration-Punkte-Analyse**

### **Bestehende Integrationen:**

**✅ Cockpit-Integration (teilweise):**
```typescript
// MyDayColumnMUI.tsx - Triage-Inbox-Integration vorhanden
const { showTriageInbox, toggleTriageInbox } = useCockpitStore();

// Aber: Nur Mock-Daten
import { mockTriageItems } from '../data/mockData';
```

**🔍 Customer-Integration:**
```typescript
// Customer-Entities vollständig vorhanden
// Aber: Keine Lead-to-Customer-Pipeline
```

**🔴 Fehlende Integrationen:**
- E-Mail-Provider (Gmail/Outlook)
- Customer-Creation aus Leads
- Activity-Tracking für Lead-Actions
- Opportunity-Creation aus qualifizierten Leads

---

## 📋 **Gap-Analyse: Planung vs. Reality**

### **FC-003 E-Mail-Integration:**

**📋 Plan-Status:** ✅ Vollständig spezifiziert (148 Zeilen)
**🔧 Implementation-Status:** 🔴 0% Backend, 30% Frontend-Mock

```yaml
Geplant aber nicht implementiert:
  Backend:
    - SMTP-Receiver Service
    - E-Mail-Parser
    - Customer-Matcher
    - Attachment-Handler
    - Email-Entity + Repository
    - REST-Endpoints

  Frontend:
    - Email-Timeline-Komponente
    - E-Mail-Composer
    - Thread-Gruppierung
    - Attachment-Handling
```

### **FC-002-M4 Neukundengewinnung:**

**📋 Plan-Status:** 🔴 Nur Platzhalter (37 Zeilen)
**🔧 Implementation-Status:** 🔴 5% (nur Dashboard)

```yaml
Plan vs. Reality:
  Plan: "Wird bei Implementierung ausgearbeitet"
  Reality: Dashboard professional, aber Features fehlen komplett
```

---

## 🏗️ **Architektur-Befunde**

### **Frontend-Architektur:**

**✅ Solide Foundation:**
```
frontend/src/
├── pages/NeukundengewinnungDashboard.tsx ✅ Professional
├── config/navigation.config.ts ✅ Vollständig konfiguriert
├── features/cockpit/components/MyDayColumnMUI.tsx ✅ Triage-Mock
└── [features/neukundengewinnung/] 🔴 Fehlt komplett
```

**🔴 Fehlende Struktur:**
```
// BENÖTIGT aber nicht vorhanden:
frontend/src/features/neukundengewinnung/
├── components/
│   ├── EmailInbox/
│   ├── LeadCapture/
│   └── CampaignManagement/
├── services/
│   ├── emailApi.ts
│   ├── leadApi.ts
│   └── campaignApi.ts
└── stores/
    ├── emailStore.ts
    ├── leadStore.ts
    └── campaignStore.ts
```

### **Backend-Architektur:**

**✅ Solide Customer-Foundation:**
```java
// VORHANDEN:
de.freshplan.domain.customer.entity.Customer ✅
de.freshplan.domain.customer.service.CustomerService ✅
de.freshplan.domain.customer.repository.CustomerRepository ✅
```

**🔴 Fehlende Domain-Module:**
```java
// BENÖTIGT aber nicht vorhanden:
de.freshplan.domain.email.*     // E-Mail-Integration
de.freshplan.domain.lead.*      // Lead-Management
de.freshplan.domain.campaign.*  // Kampagnen-System
```

---

## 🚨 **Kritische Erkenntnisse**

### **1. Große Implementation-Lücke:**
- **FC-003** detailliert geplant, aber **0% implementiert**
- Dashboard suggeriert Funktionalität die nicht existiert
- Triage-Inbox zeigt Mock-Daten statt echter E-Mails

### **2. Architektur-Konsistenz:**
- Frontend-Navigation professional umgesetzt
- Backend-Foundation (Customer) solide
- **Aber:** Verbindung fehlt komplett

### **3. Business-Risiko:**
- Mock-KPIs können Business-Expectations verzerren
- "23 neue E-Mails" im Dashboard sind nicht echt
- Funktions-Gap zwischen UI und Backend kritisch

### **4. Positive Foundation:**
- Excellent MUI-Implementation
- FreshFoodz CI-compliant
- Navigation-Struktur durchdacht
- Customer-Entity als Integration-Basis vorhanden

---

## 🎯 **Empfehlungen für weitere Planung**

### **Priorität 1: E-Mail-Integration (FC-003)**
```yaml
Status: Vollständig geplant, 0% implementiert
Aufwand: 5-7 Tage laut Plan
Impact: Basis für Lead-Generierung
Infrastructure: TestDataBuilder ready für E-Mail-Testing
```

### **Priorität 2: Lead-Entity + Basic Management**
```yaml
Status: Nicht geplant, nicht implementiert
Aufwand: 3-4 Tage (geschätzt)
Impact: Lead-to-Customer-Pipeline
Infrastructure: CustomerBuilder kann Lead-Scenarios generieren
```

### **Priorität 3: Campaign-Grundlagen**
```yaml
Status: Nicht geplant, nicht implementiert
Aufwand: 8-10 Tage (geschätzt)
Impact: Marketing-Automation
Infrastructure: TestDataBuilder für Campaign-Testing ready
```

### **🔗 Infrastructure-Integration:**
```yaml
Mock-Replacement-Koordination:
  - Frontend-Mocks durch TestDataBuilder-APIs ersetzen
  - Dashboard-KPIs: Echte Customer/Contact-Data via TestDataBuilder
  - Triage-Inbox: TestDataBuilder.customer().withEmailHistory()
  - Lead-Pipeline: CustomerBuilder für Lead-to-Customer-Simulation

Immediate verfügbar:
  - CustomerBuilder.java (12.5KB, vollständig implementiert)
  - ContactBuilder.java (8.3KB, E-Mail-Integration ready)
  - TestDataBuilder.java (zentrale Facade)
```

### **Mock-to-Real Migration mit TestDataBuilder:**
```yaml
Sofort (Infrastructure vorhanden):
  - Dashboard-KPIs mit TestDataBuilder-generierten Daten
  - Triage-Inbox mit CustomerBuilder.withEmailHistory()
  - Lead-Mock-Daten durch Customer-Entity-basierte Simulation

Mittelfristig:
  - FC-003 E-Mail-Integration mit TestDataBuilder-Testing
  - Lead-Management-UI mit echten Customer-Entities
  - Campaign-Basic-Features mit TestDataBuilder-Target-Groups
```

---

## 📊 **Zusammenfassung für Diskussion**

**Kern-Situation:**
- Neukundengewinnung ist **UI-komplett, Backend-leer**
- FC-003 E-Mail-Integration ist **ready-to-implement**
- Lead & Campaign-Module benötigen **komplette Planung**

**Nächste Schritte:**
1. **Mock-Replacement:** Dashboard mit TestDataBuilder-APIs verknüpfen (Infrastructure ready!)
2. **E-Mail-Integration:** FC-003 Plan aktivieren und mit TestDataBuilder implementieren
3. **Lead-Management:** Technical Concept erstellen (Customer-Entity-basiert)
4. **Campaign-Basics:** Minimale Kampagnen-Funktionalität mit TestDataBuilder-Integration planen

**Diskussionsfragen:**
- Soll Mock-Replacement parallel zu Neukundengewinnung-Implementation laufen?
- FC-003 E-Mail-Integration sofort starten (Infrastructure ready mit TestDataBuilder)?
- Welche Lead-Management-Features sind business-kritisch?
- Campaign-Management kurzfristig benötigt oder kann es warten?

---

**📋 Diese Analyse bildet die Grundlage für strategische Entscheidungen zur Neukundengewinnung-Implementation.**
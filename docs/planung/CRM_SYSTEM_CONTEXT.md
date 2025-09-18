# 🚀 CRM System Context - Umfangreicher Diskussions-Kontext

**📊 Dokument-Typ:** Context & Background für Feature-Diskussionen mit anderen KIs
**🎯 Zielgruppe:** KIs für qualitative Feature-Diskussionen und Architektur-Entscheidungen
**🔗 Planungs-Übersicht:** → [CRM Master Plan V5](./CRM_COMPLETE_MASTER_PLAN_V5.md) (kompakte Planung)

## 🎯 Executive Summary

**Mission:** Entwicklung eines intelligenten Sales Command Centers für B2B-Gastronomy-Vertrieb mit Cook&Fresh® Convenience-Food-Produkten
**Problem:** Fragmentierte Vertriebsprozesse, manuelle Workflows, fehlende Insights für komplexe B2B-Beratungsverkäufe in der Gastronomie-Branche
**Solution:** Integrierte CRM-Plattform mit Field-Based Architecture und Event-Driven Communication speziell für FreshFoodz Gastronomy-Sales
**Impact:** 3x schnellere Lead-Qualifizierung, 2x höhere Conversion durch ROI-basierte Beratung, vollständige Sales-Process-Automation

## 🍽️ FreshFoodz Business-Kontext (B2B-Gastronomy)

**Unser Geschäftsmodell:**
- **Produkt:** Cook&Fresh® Convenience-Food mit patentierter Konservierungstechnologie
- **Haltbarkeit:** Bis 40 Tage ohne künstliche Konservierungsstoffe
- **Zielgruppe:** Hotels, Restaurants, Betriebsgastronomie, Vending-Konzepte
- **Verkaufsansatz:** "Genussberater" - individuelle ROI-basierte Beratung statt Standard-Produktverkauf

**Sales-Prozess-Besonderheiten:**
```yaml
Klassischer B2B-Produktvertrieb (nicht SaaS!):
1. Lead-Qualifizierung → Restaurant-Typ, Größe, Küchen-Konzept, aktuelle Herausforderungen
2. Bedarf-Analyse → Personal-Situation, Food-Waste-Probleme, Qualitäts-Anforderungen
3. ROI-Demonstration → Kosteneinsparungs-Kalkulation (Personal, Waste, Zeit, Qualität)
4. Produkt-Sampling → Gratis Produktkatalog + individuell zusammengestellte Sample-Boxes
5. Test-Phase → Kunde testet Cook&Fresh® Produkte im echten Gastronomy-Betrieb (2-4 Wochen)
6. Feedback-Analyse → Welche Produkte funktionieren? Anpassungen nötig?
7. Individuelles Angebot → Basierend auf erfolgreich getesteten Produkten + Volumen-Kalkulation
8. Verhandlung → Mengenrabatte, Lieferkonditionen, Zahlungsbedingungen
9. Abschluss → Langfristige Lieferverträge (oft 12-24 Monate)
10. Account-Management → Upselling neuer Produkte, Cross-Selling an weitere Standorte
```

**Verkaufs-Zyklen sind LANG (3-6 Monate):**
- Gastronomie-Entscheidungen sind risikobehaftet (Menu-Qualität beeinflusst Gast-Zufriedenheit)
- Test-Phasen erforderlich bevor Commitment
- Oft Multiple Decision-Maker (Küchenchef, Geschäftsführer, Einkauf)
- Saisonale Faktoren (Weihnachts-Menu vs. Sommer-Terrasse)

**CRM-Anforderungen für Gastronomy-Sales:**
- **ROI-Kalkulation:** Tool für Kosteneinsparungs-Analyse (Personal/Waste/Zeit) statt einfache Preislisten
- **Produkt-Matching:** Intelligentes Matching - welche Cook&Fresh® Produkte passen zum Restaurant-Konzept?
- **Sample-Management:** Tracking von versendeten Produktkatalogen und individuellen Test-Boxen
- **Follow-up-Zyklen:** Systematische Verfolgung langer Entscheidungsprozesse mit Multiple Touchpoints
- **Account-Management:** Bestehende Kunden für Upselling (neue Produkte) und Cross-Selling (weitere Standorte) betreuen
- **Saisonales CRM:** Berücksichtigung saisonaler Menu-Zyklen und Gastronomy-Besonderheiten

## 🏗️ System-Philosophie: Die 3 Kernprinzipien

### 1. Geführte Freiheit (Guided Freedom)
Das System bietet klare Standard-Workflows, erlaubt aber Abweichungen. 80/20-Ansatz für tägliche Aufgaben.

**Konkret bedeutet das:**
- **Standard-Workflows:** 80% der täglichen Verkäufer-Aktivitäten werden durch optimierte Standard-Prozesse abgebildet
- **Flexible Abweichungen:** Bei besonderen Kundensituationen kann vom Standard abgewichen werden
- **Geführte Navigation:** Das System schlägt den nächsten logischen Schritt vor, zwingt aber nicht dazu
- **Lernende Optimierung:** Häufige Abweichungen werden analysiert und fließen in neue Standards ein

### 2. Alles ist miteinander verbunden
Keine Information ist eine Sackgasse - alles führt zur nächsten Aktion. Kontextbezogene Navigation zwischen allen Features.

**Konkret bedeutet das:**
- **Kontextuelle Navigation:** Klick auf Kunde → zeigt relevante Verkaufschancen, Aktivitäten, E-Mails
- **Cross-Feature-Integration:** E-Mail → Lead → Kunde → Verkaufschance → Angebot (nahtlose Übergänge)
- **Aktions-Orientierung:** Jede Informations-Ansicht bietet direkte Handlungsoptionen
- **360°-Kundensicht:** Alle relevanten Informationen sind in 1-2 Klicks erreichbar

### 3. Skalierbare Exzellenz
Von Tag 1 auf Wachstum, Performance und Qualität ausgelegt. API-First, <200ms Response-Zeit, proaktive Datenqualität.

**Konkret bedeutet das:**
- **Performance by Design:** Alle Features für 1000+ gleichzeitige Nutzer ausgelegt
- **API-First-Architektur:** Jede Funktion über REST/GraphQL-APIs zugänglich
- **Proaktive Datenqualität:** Duplikat-Erkennung, Vollständigkeits-Checks, automatische Bereinigung
- **Monitoring & Alerting:** Real-time Überwachung aller kritischen Business-Metriken

## 🎨 Corporate Identity & Design System

**Freshfoodz CI (VERBINDLICH):**
- **Primärgrün:** `#94C456` (Buttons, Actions, Links)
- **Dunkelblau:** `#004F7B` (Headlines, Navigation)
- **Schriften:** Antonio Bold (Headlines), Poppins (Text)

**Design-Standards:** [FRESH-FOODZ_CI.md](../FRESH-FOODZ_CI.md)

**UI/UX-Philosophie:**
- **Minimal Cognitive Load:** Maximal 7 Elemente pro Bildschirm-Bereich
- **Consistent Interactions:** Gleiche Gesten führen zu gleichen Ergebnissen
- **Progressive Disclosure:** Komplexe Features werden schrittweise enthüllt
- **Mobile-First:** Alle Features funktionieren auf Smartphone (responsive)

## 🗺️ Sidebar-Navigation & Feature-Struktur

**Navigation-Architektur:**
```
├── 🏠 Mein Cockpit                # Dashboard & Insights
│   ├── dashboard-core/            # 3-Spalten-Layout + Koordination
│   ├── kpi-widgets/              # Dashboard-Statistiken
│   ├── recent-activities/         # Activity Timeline
│   └── quick-actions/            # Schnellaktionen + Context Menu
├── 👤 Neukundengewinnung          # Lead Generation & Campaigns
│   ├── email-posteingang/        # E-Mail-Triage & Lead-Erkennung
│   ├── lead-erfassung/           # Neue Leads manuell/automatisch
│   └── kampagnen/                # E-Mail-Marketing & Follow-ups
├── 👥 Kundenmanagement            # CRM Core (M4 Pipeline)
│   ├── alle-kunden/              # Kundenliste mit Filter/Suche
│   ├── neuer-kunde/              # Kunden-Erfassung & Onboarding
│   ├── verkaufschancen/          # Pipeline-Management (M4)
│   └── aktivitaeten/             # Termine, Notizen, Follow-ups
├── 📊 Auswertungen               # Analytics & Reports
│   ├── umsatzuebersicht/         # Revenue Tracking & Forecasting
│   ├── kundenanalyse/            # Customer Analytics & Segmentation
│   └── aktivitaetsbericht/       # Sales Activity Reports
├── 💬 Kommunikation              # Team Communication
│   ├── team-chat/                # Internal Team Communication
│   ├── ankuendigungen/           # Company-wide Announcements
│   ├── notizen/                  # Shared Notes & Knowledge Base
│   └── interne-nachrichten/      # Direct Messages & Notifications
├── ⚙️ Einstellungen              # User Configuration
│   ├── mein-profil/              # User Profile & Preferences
│   ├── benachrichtigungen/       # Notification Settings
│   ├── darstellung/              # UI Customization & Themes
│   └── sicherheit/               # Security & Privacy Settings
├── 🆘 Hilfe & Support            # Help System
│   ├── erste-schritte/           # Onboarding & Getting Started
│   ├── handbuecher/              # User Manuals & Documentation
│   ├── video-tutorials/          # Video Learning Content
│   ├── haeufige-fragen/          # FAQ System
│   └── support-kontaktieren/     # Ticketing & Direct Support
└── 🔐 Administration             # Admin Functions
    ├── audit-dashboard/          # System Audit Trail
    ├── benutzerverwaltung/       # User & Role Management
    ├── system/                   # System Management
    │   ├── api-status/           # API Health Monitoring
    │   ├── system-logs/          # Log Management
    │   ├── performance/          # Performance Dashboard
    │   └── backup-recovery/      # Backup Management
    ├── integration/              # External System Integration
    │   ├── ki-anbindungen/       # AI/ML Service Connections
    │   ├── xentral/              # Xentral ERP Integration
    │   ├── email-service/        # E-Mail Provider Management
    │   ├── payment-provider/     # Payment Gateway Configuration
    │   ├── webhooks/             # Webhook Management
    │   └── neue-integration/     # Integration Framework
    ├── hilfe-konfiguration/      # Help System Configuration
    │   ├── hilfe-system-demo/    # Help Demo Interface
    │   ├── tooltips-verwalten/   # Tooltip Management
    │   ├── touren-erstellen/     # Onboarding Tour Builder
    │   └── analytics/            # Help System Analytics
    └── compliance-reports/       # DSGVO & Compliance Reports
```

## 👥 Zielgruppen & User Journeys

### **Primary User: Sales Representative ("Genussberater")**
**Tägliche Hauptaufgaben für Gastronomy-Sales:**
1. **Lead-Qualifizierung:** E-Mails triagieren → Restaurant-Profile analysieren → Erstkontakt mit BANT-Kriterien
2. **ROI-Beratung:** Kosteneinsparungs-Potentiale kalkulieren → Sample-Boxen konfigurieren → individuelle Lösungen entwickeln
3. **Test-Phase-Management:** Sample-Feedback einholen → Produkt-Anpassungen → Follow-up-Zyklen systematisch verfolgen
4. **Account-Development:** Bestehende Kunden für neue Produkte/Standorte entwickeln → saisonale Menu-Zyklen nutzen

**Typischer Gastronomy-Sales Workflow:**
```
09:00 Cockpit öffnen → Sample-Feedback auswerten → Test-Phasen-Status prüfen
10:00 Restaurant-Entscheider kontaktieren → ROI-Kalkulationen erstellen
11:00 Produkt-Matching für Interessenten → Sample-Boxen zusammenstellen
14:00 Langfristige Follow-ups (3-6 Monate Zyklen) → Saisonale Opportunities identifizieren
16:00 Account-Pipeline reviewen → Multiple Decision-Maker koordinieren
```

**Gastronomy-Sales Besonderheiten:**
- **Beratungsintensiv:** Jeder Kunde braucht individuelle ROI-Analyse statt Standard-Pitch
- **Produktexperte:** Muss 200+ Cook&Fresh® Produkte kennen und Restaurant-Konzepten zuordnen können
- **Langfristige Beziehungen:** 3-6 Monate Sales-Cycle, dann oft mehrjährige Lieferverträge
- **Saisonale Expertise:** Weihnachts-Menu, Sommer-Terrasse, Event-Catering berücksichtigen

### **Secondary User: Sales Manager**
**Wöchentliche Hauptaufgaben:**
1. **Team-Performance:** KPIs überwachen → Bottlenecks identifizieren → Coaching-Bedarf erkennen
2. **Pipeline-Review:** Forecast-Accuracy → Deal-Risiken bewerten → Ressourcen-Allokation
3. **Strategische Planung:** Market-Trends analysieren → Ziele adjustieren → Prozesse optimieren

### **Admin User: System Administrator**
**Monatliche Hauptaufgaben:**
1. **System-Health:** Performance überwachen → Kapazitätsplanung → Backup-Validierung
2. **User-Management:** Benutzer administrieren → Rollen anpassen → Security-Compliance
3. **Integration-Management:** APIs überwachen → Webhooks verwalten → Datenqualität sicherstellen

## 🏗️ Technische Architektur

### **Frontend (React + TypeScript)**
```
frontend/
├── features/                    # Feature-basierte Organisation
│   ├── cockpit/                # Dashboard-Components
│   ├── customer/               # Customer-Management
│   ├── leads/                  # Lead-Management
│   ├── sales/                  # Sales-Pipeline
│   ├── communication/          # Team-Communication
│   ├── settings/               # User-Settings
│   ├── help/                   # Help-System
│   └── admin/                  # Administration
├── components/                 # Reusable UI Components
│   ├── common/                 # Generic Components
│   └── domain/                 # Business-specific Components
├── services/                   # API Services
├── hooks/                      # Custom React Hooks
└── utils/                      # Shared Utilities
```

### **Backend (Quarkus + Java)**
```
backend/
├── domain/                     # Business Domain
│   ├── customer/              # Customer Aggregate
│   ├── lead/                  # Lead Management
│   ├── sales/                 # Sales Pipeline
│   ├── communication/         # Team Communication
│   └── admin/                 # System Administration
├── api/                       # REST Endpoints
├── infrastructure/            # Technical Infrastructure
│   ├── persistence/           # Database Layer
│   ├── messaging/             # Event Bus
│   ├── security/              # Authentication & Authorization
│   └── integration/           # External System Integration
└── shared/                    # Cross-cutting Concerns
```

### **Datenbank-Design (PostgreSQL)**
```sql
-- Core Entities
customers (id, name, email, status, created_at, updated_at)
leads (id, source, status, customer_id, assigned_to, created_at)
sales_opportunities (id, customer_id, value, stage, probability, close_date)
activities (id, type, description, related_to_type, related_to_id, created_by, created_at)
users (id, username, email, role, preferences, created_at)

-- Communication & Collaboration
messages (id, sender_id, recipient_id, content, thread_id, created_at)
notifications (id, user_id, type, content, read_at, created_at)
shared_notes (id, title, content, created_by, visibility, created_at)

-- System & Administration
audit_logs (id, user_id, action, entity_type, entity_id, changes, created_at)
system_settings (key, value, description, updated_by, updated_at)
integrations (id, name, type, config, status, created_at)
```

## 🔄 Business Process Flows

### **Gastronomy Lead-to-Customer Journey**
```
1. Lead-Eingang (E-Mail/Web/Messe/Referral)
   ↓
2. Restaurant-Profiling (Typ, Größe, Konzept, aktuelle Herausforderungen)
   ↓
3. BANT-Qualifizierung + Entscheider-Mapping (Küchenchef/GF/Einkauf)
   ↓
4. ROI-Beratungsgespräch (Kosteneinsparungs-Potentiale analysieren)
   ↓
5. Produkt-Matching (Cook&Fresh® Portfolio → Restaurant-Bedarf)
   ↓
6. Sample-Box-Konfiguration (Individuelle Test-Produkte zusammenstellen)
   ↓
7. Test-Phase-Begleitung (2-4 Wochen, Feedback sammeln)
   ↓
8. Feedback-Analyse + Produkt-Anpassung
   ↓
9. Individuelles Angebot (Basierend auf erfolgreichen Tests)
   ↓
10. Verhandlung (Mengen, Konditionen, Liefertermine)
    ↓
11. Vertrags-Abschluss (Langfristige Liefervereinbarung)
    ↓
12. Onboarding + Account-Management (Upselling/Cross-Selling)
```

### **Daily Sales Activities**
```
Cockpit-Check → E-Mail-Triage → Lead-Bearbeitung
     ↑                                    ↓
Pipeline-Update ← Aktivitäten-Dokumentation ← Kunde-Kontaktierung
     ↑                                    ↓
Forecast-Update ← Angebot-Nachfassung ← Termin-Durchführung
```

## 🚀 Integration-Philosophie

### **Bestehende Integrationen**
- **Keycloak:** Single Sign-On & User Management
- **E-Mail-Provider:** Gmail/Outlook-Integration für Lead-Import
- **Calculator (M8):** Angebot-Erstellung & Preiskalkulation

### **Geplante Integrationen**
- **Xentral ERP:** Auftrags-Übergabe & Stammdaten-Sync
- **Monday.com:** Projekt-Management für After-Sales
- **Klenty:** Sales Automation & E-Mail-Sequences
- **Payment-Provider:** Online-Payment für Angebote
- **WhatsApp Business:** Customer Communication

### **Integration-Patterns**
- **Real-time:** WebSockets für Live-Updates (Team-Chat, Notifications)
- **Near-real-time:** Event-driven Architecture für Business-Events
- **Batch:** Nächtliche Sync-Jobs für Stammdaten
- **On-demand:** API-Calls für User-initiated Actions

## 📊 Performance & Scalability Requirements

### **Performance Targets**
- **Page Load Time:** < 2 Sekunden (P95)
- **API Response Time:** < 200ms (P95)
- **Database Query Time:** < 50ms (P95)
- **Concurrent Users:** 1000+ ohne Performance-Degradation

### **Scalability Design**
- **Horizontal Scaling:** Microservice-Architecture
- **Database Scaling:** Read-Replicas + Connection Pooling
- **Caching Strategy:** Redis für Session-Data & Frequent Queries
- **CDN:** Static Assets via CloudFront

## 🔒 Security & Compliance

### **Security Architecture**
- **Authentication:** Keycloak OIDC with MFA
- **Authorization:** Role-based Access Control (RBAC)
- **Data Encryption:** TLS 1.3 in transit, AES-256 at rest
- **API Security:** OAuth 2.0 + Rate Limiting

### **DSGVO Compliance**
- **Data Minimization:** Nur notwendige Daten sammeln
- **Right to Erasure:** Automatisierte Daten-Löschung
- **Data Portability:** Export-Funktion für Kundendaten
- **Audit Trail:** Vollständige Nachverfolgung aller Daten-Zugriffe

## 🎯 Success Metrics & KPIs

### **Business Metrics**
- **Lead Conversion Rate:** Von Lead zu Opportunity
- **Sales Cycle Length:** Durchschnittliche Zeit bis Abschluss
- **Deal Size:** Durchschnittlicher Auftragswert
- **Customer Lifetime Value:** Langfristige Kundenwerte

### **User Experience Metrics**
- **Time to First Value:** Wie schnell ist ein neuer User produktiv?
- **Daily Active Users:** Tägliche Nutzung der Plattform
- **Feature Adoption Rate:** Welche Features werden genutzt?
- **User Satisfaction Score:** Regelmäßige Zufriedenheits-Umfragen

### **System Metrics**
- **System Availability:** 99.9% Uptime
- **Error Rate:** < 0.1% für kritische Operationen
- **Response Time:** P95 < 200ms für alle APIs
- **Data Quality Score:** Vollständigkeit & Konsistenz der CRM-Daten

## 🔄 Continuous Improvement Philosophy

### **Data-Driven Decisions**
- **A/B Testing:** Für alle UI-Änderungen
- **User Behavior Analytics:** Heatmaps & Click-Tracking
- **Performance Monitoring:** Real-time System Metrics
- **Business Intelligence:** Automated Reports & Dashboards

### **Feedback Loops**
- **User Interviews:** Monatliche Gespräche mit Power Users
- **Support Ticket Analysis:** Häufige Probleme identifizieren
- **Sales Team Feedback:** Wöchentliche Reviews mit Vertriebs-Team
- **Customer Success Metrics:** Tracking der Business-Outcomes

---

**📋 Dokument-Zweck:** Umfangreicher Kontext für qualitative Feature-Diskussionen mit KIs
**🔗 Planungs-Übersicht:** → [CRM Master Plan V5](./CRM_COMPLETE_MASTER_PLAN_V5.md)
**🔄 Letzte Aktualisierung:** 2025-09-18 - Initial Creation für Duale Dokumentations-Strategie
# 📊 M4 OPPORTUNITY PIPELINE (KOMPAKT)

**Erstellt:** 17.07.2025 14:15  
**Status:** 📋 READY TO START  
**Feature-Typ:** 🔀 FULLSTACK  
**Priorität:** HIGH - Core Sales Feature  

---

## 🧠 WAS WIR BAUEN

**Problem:** Vertrieb trackt Deals in Excel = Chaos  
**Lösung:** Kanban Board mit "Geführter Freiheit"  
**Value:** Echtzeit-Überblick + intuitive Prozessführung  

> **Business Case:** 50 Deals × 5 Stages = 250 Status-Updates mit klaren nächsten Schritten

### 🎯 Geführte Freiheit Prinzipien:
- **Menüführung:** Strukturierte Navigation zu Opportunities
- **Action Buttons:** Kontextuelle Aktionen direkt im Board
- **Guided Process:** System schlägt nächste Schritte vor
- **Flexible Nutzung:** Power-User können Führung überspringen

---

## 🚀 SOFORT LOSLEGEN (15 MIN)

### 1. **Backend Entity erstellen:**
```bash
cd backend/src/main/java/de/freshplan/domain
mkdir -p opportunity/entity
touch opportunity/entity/Opportunity.java
# → Template: [Entity Code](#entity-code)
```

### 2. **Frontend Board-Komponente:**
```bash
cd frontend/src/features
mkdir -p opportunity/components
touch opportunity/components/OpportunityBoard.tsx
# → Kanban mit react-beautiful-dnd
```

### 3. **API Endpoints planen:**
```bash
# CRUD + Special Operations
POST   /api/opportunities          # Create
GET    /api/opportunities          # List with filters
PATCH  /api/opportunities/{id}     # Update (inkl. Stage-Change)
POST   /api/opportunities/{id}/move # Stage verschieben
```

**Geschätzt: 7-10 Tage**

---

## 📋 DIE 5 SALES STAGES + INTEGRATIONEN

1. **Lead** → Erster Kontakt
2. **Qualified** → Budget & Need confirmed
   - 🔍 **NEU: Bonitätsprüfung** (FC-011) als Stage-Gate
   - ✅ Nur mit positiver Bonität → Proposal
3. **Proposal** → Angebot erstellt  
   - 📄 **Vertragsunterlagen** automatisch generiert
   - 🧮 **Calculator Modal** (M8) für Angebote
4. **Negotiation** → Verhandlung läuft
5. **Closed** → Won/Lost

**Drag & Drop zwischen Stages mit Validierung!**

### 🔗 Integrierte Features:
- **FC-011 Bonitätsprüfung & Verträge** → [Details](./integrations/FC-011_KOMPAKT.md)
- **M8 Calculator Modal** → [Details](../03_calculator_modal/M8_KOMPAKT.md)

---

## 🔗 VOLLSTÄNDIGE DETAILS

**Implementation Guide:** [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md)
- [Entity Code](#entity-code) - JPA Entities
- [Kanban UI](#kanban-ui) - React DnD Setup
- [Stage Rules](#stage-rules) - Validierungslogik
- [Team Features](#team-features) - Ownership & Sharing
- [Forecast Logic](#forecast-logic) - Probability Calculation

**Entscheidungen:** [DECISION_LOG.md](./DECISION_LOG.md)
- Soft Delete vs. Hard Delete?
- Stage-History tracken?
- Attachment-Handling?

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[🔒 FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_KOMPAKT.md)** - User Authentication & Authorization
- **[👥 FC-009 Permissions System](/docs/features/ACTIVE/04_permissions_system/FC-009_KOMPAKT.md)** - Role-based Access Control

### ⚡ Integrierte Sub-Features:
- **[💰 FC-011 Bonitätsprüfung](/docs/features/ACTIVE/02_opportunity_pipeline/integrations/FC-011_KOMPAKT.md)** - Automatische Kreditprüfung bei Qualified → Proposal
- **[🧮 M8 Calculator Modal](/docs/features/ACTIVE/03_calculator_modal/M8_KOMPAKT.md)** - Angebotserstellung in Proposal Stage

### 🚀 Nachgelagerte Features (Dependent):
- **[👨‍💼 FC-007 Chef-Dashboard](/docs/features/PLANNED/10_chef_dashboard/FC-007_KOMPAKT.md)** - Management KPIs basierend auf Pipeline
- **[🔍 FC-013 Duplicate Detection](/docs/features/PLANNED/15_duplicate_detection/FC-013_KOMPAKT.md)** - Verhindert doppelte Opportunities
- **[📈 FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_KOMPAKT.md)** - Opportunity-History & Customer Journey
- **[📊 FC-015 Deal Loss Analysis](/docs/features/PLANNED/17_deal_loss_analysis/FC-015_KOMPAKT.md)** - Analyse verlorener Deals
- **[📋 FC-016 Opportunity Cloning](/docs/features/PLANNED/18_opportunity_cloning/FC-016_KOMPAKT.md)** - Efficient Deal-Duplikation
- **[📈 FC-019 Advanced Sales Metrics](/docs/features/PLANNED/19_advanced_metrics/FC-019_KOMPAKT.md)** - Sales Performance Analytics
- **[🎯 FC-027 Magic Moments](/docs/features/PLANNED/27_magic_moments/FC-027_KOMPAKT.md)** - Verkaufschancen-Identifikation
- **[🤝 FC-036 Beziehungsmanagement](/docs/features/PLANNED/36_relationship_mgmt/FC-036_KOMPAKT.md)** - Customer Relationship Tracking

### 🎨 UI Integration:
- **[🧭 M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_NAVIGATION_KOMPAKT.md)** - Hauptnavigation zu Pipeline
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)** - Integration in 3-Spalten-Layout

---

## 📞 NÄCHSTE SCHRITTE

1. **Opportunity Entity** - Mit allen Feldern
2. **Repository + Service** - CRUD Operations  
3. **REST Endpoints** - Mit Swagger Docs
4. **Kanban Board UI** - Desktop-first
5. **Stage Transitions** - Mit Animation
6. **Team Features** - Sharing & Comments
7. **Integration Tests** - E2E Workflow

**WICHTIG:** Calculator Modal (M8) wird später integriert!
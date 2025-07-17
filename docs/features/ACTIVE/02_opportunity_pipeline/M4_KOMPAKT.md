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

## 📋 DIE 5 SALES STAGES

1. **Lead** → Erster Kontakt
2. **Qualified** → Budget & Need confirmed  
3. **Proposal** → Angebot erstellt
4. **Negotiation** → Verhandlung läuft
5. **Closed** → Won/Lost

**Drag & Drop zwischen Stages mit Validierung!**

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

## 📞 NÄCHSTE SCHRITTE

1. **Opportunity Entity** - Mit allen Feldern
2. **Repository + Service** - CRUD Operations  
3. **REST Endpoints** - Mit Swagger Docs
4. **Kanban Board UI** - Desktop-first
5. **Stage Transitions** - Mit Animation
6. **Team Features** - Sharing & Comments
7. **Integration Tests** - E2E Workflow

**WICHTIG:** Calculator Modal (M8) wird später integriert!
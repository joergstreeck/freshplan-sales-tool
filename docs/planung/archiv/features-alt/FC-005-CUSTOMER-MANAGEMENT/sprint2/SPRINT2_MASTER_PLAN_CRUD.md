# 🎯 Sprint 2 Master Plan - Contact Management CRUD

**Feature:** FC-005 Customer Management  
**Sprint:** Sprint 2 - Contact Management CRUD Implementation  
**Datum:** 31.07.2025  
**Status:** 🚀 BEREIT FÜR IMPLEMENTATION  
**Dauer:** 1-2 Wochen  

## 📌 Executive Summary

Sprint 2 implementiert **Multi-Contact Support** mit einer **pragmatischen CRUD-Architektur**. Das Backend ist bereits vollständig implementiert - wir fokussieren uns auf das Frontend.

**Kernziele:**
- ✅ Backend Contact CRUD bereits komplett ✅
- 🔄 Multi-Contact Frontend UI  
- 🔄 Contact Store & API Integration
- 🔄 Location Assignment für Filialkunden
- 🔄 Responsive Contact Cards

## 🏗️ Bewährte CRUD-Architektur

```
React Frontend → REST API → JPA Service → PostgreSQL
      ↓              ↓           ↓
 Contact Store → ContactResource → Contact Entity
      ↓              ↓           ↓  
   Multi-UI    → CRUD Endpoints → Database Table
```

**Bereits implementiert (Backend):**
- ✅ Contact Entity mit allen Features
- ✅ ContactService mit Business Logic  
- ✅ ContactResource mit REST API
- ✅ V113 Migration mit vollständigem Schema
- ✅ Multi-Contact, Primary Contact, Location Assignment

## 🎯 Sprint 2 Fokus: Frontend Implementation

### Woche 1: Core Frontend (5 Tage)

| Tag | Aufgabe | Deliverable |
|-----|---------|-------------|
| **Tag 1** | Contact Store + Types erweitern | Multi-Contact Store ✅ |
| **Tag 2** | Contact API Service implementieren | REST Client Integration ✅ |
| **Tag 3** | Contact Cards UI komponente | Responsive Card Layout ✅ |
| **Tag 4** | Multi-Contact Manager integrieren | Add/Edit/Delete funktional ✅ |
| **Tag 5** | Step3 Component refactoring | Vollständige Integration ✅ |

### Optional Woche 2: Polish & Features

| Tag | Aufgabe | Deliverable |
|-----|---------|-------------|
| **Tag 1-2** | Location Assignment UI | Dropdown & Assignment ✅ |
| **Tag 3-4** | Relationship Fields erweitern | Birthday, Hobbys, etc. ✅ |
| **Tag 5** | Testing & Polish | Production Ready ✅ |

## 📋 Implementation Checklist

### Must-Have (Sprint 2 Core):
- [ ] **Contact Store** - Multi-Contact Array Management
- [ ] **Contact API Service** - REST Client für Backend
- [ ] **ContactCard Component** - Einzelne Contact-Karte
- [ ] **ContactForm Dialog** - Add/Edit Modal
- [ ] **Multi-Contact Manager** - Hauptkomponente
- [ ] **Step3 Integration** - Wizard-Integration
- [ ] **Primary Contact Logic** - Toggle & Validation
- [ ] **Basic CRUD** - Create, Read, Update, Delete

### Nice-to-Have (Optional):
- [ ] **Location Assignment** - Dropdown für Filialkunden
- [ ] **Relationship Fields** - Birthday, Hobbys UI
- [ ] **Contact Validation** - Email uniqueness check
- [ ] **Responsive Design** - Mobile-optimiert
- [ ] **Empty States** - "Noch keine Kontakte" UI
- [ ] **Confirmation Dialogs** - Delete-Bestätigung

## 🔗 Backend API (bereits verfügbar!)

**Base URL:** `/api/customers/{customerId}/contacts`

```bash
GET    /api/customers/{customerId}/contacts        # Liste alle Kontakte
POST   /api/customers/{customerId}/contacts        # Neuer Kontakt
GET    /api/customers/{customerId}/contacts/{id}   # Einzelner Kontakt
PUT    /api/customers/{customerId}/contacts/{id}   # Update Kontakt
DELETE /api/customers/{customerId}/contacts/{id}   # Soft Delete
PUT    /api/customers/{customerId}/contacts/{id}/set-primary # Set Primary
```

**Zusätzliche Endpoints:**
```bash
GET /api/customers/{customerId}/contacts/birthdays?days=7  # Upcoming Birthdays
POST /api/customers/{customerId}/contacts/check-email     # Email Validation
```

## 🎨 UI/UX Design Vision

### Contact Cards Layout:
```
┌─────────────────────────────────────────┐
│ 👤 Dr. Hans Müller          [Primary]   │
│ Geschäftsführer | Entscheider           │
│ 📧 h.mueller@hotel.de                   │
│ 📍 Zuständig für: Berlin, Hamburg       │
│                                         │
│ [Bearbeiten] [Primary] [Löschen]        │
└─────────────────────────────────────────┘
```

### Multi-Contact Layout:
- **Grid System** - 1-3 Cards per Row (responsive)
- **Empty State** - "Noch keine Kontakte" mit Call-to-Action
- **Add Button** - Prominent platziert
- **Primary Highlighting** - Visuell hervorgehoben

## 📊 Erfolgsmessung

### Sprint 2 Ziele:
- **Funktional:** Multi-Contact CRUD vollständig
- **Performance:** < 200ms API Response
- **UX:** Intuitive Bedienung ohne Schulung
- **Code Quality:** > 80% Test Coverage
- **Integration:** Nahtlos in Customer Wizard

### Business Value:
- **Verkäufer können mehrere Kontakte erfassen**
- **Primary Contact wird automatisch gesetzt**
- **Location-Zuordnung für Filialkunden möglich**
- **Beziehungsebene-Daten erfassbar**

## 🚀 Quick Start für Implementation

```bash
# 1. Contact Store erweitern
# File: frontend/src/features/customers/stores/contactStore.ts

# 2. API Service implementieren  
# File: frontend/src/features/customers/services/contactApi.ts

# 3. Contact Card Component
# File: frontend/src/features/customers/components/ContactCard.tsx

# 4. Multi-Contact Manager
# File: frontend/src/features/customers/components/MultiContactManager.tsx
```

## 🔮 Zukunfts-Features (archiviert)

**Siehe:** `/archive/event-sourcing-ideas/` für:
- Relationship Warmth Indicator
- Advanced Analytics  
- Mobile Quick Actions
- DSGVO Compliance Features

---

## 🎯 Nächste Schritte

1. **Contact Store** implementieren (Multi-Contact Array)
2. **Contact API Service** erstellen 
3. **Contact UI Components** bauen
4. **Step3 Integration** vollenden

**Status:** Backend ✅ FERTIG | Frontend 🔄 BEREIT

**Let's build! 🚀**
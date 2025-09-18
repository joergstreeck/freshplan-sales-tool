# 📁 Neue Dokumentations-Struktur - Foundation-First

**Erstellt:** 2025-09-17
**Strategie:** Sidebar-driven, Development-focused
**Status:** Implementiert

## 🏗️ **NEUE STRUKTUR OVERVIEW:**

```
docs/
├── 📋 FOUNDATION_FIRST_DEVELOPMENT_PLAN.md     # Master-Plan
├── 📋 STRATEGIC_RESTRUCTURING_MASTER_PLAN.md   # Übergeordnete Strategie
├── 📋 NEW_DOCUMENTATION_STRUCTURE.md           # Diese Datei
│
├── grundlagen/                                  # 🏗️ DEVELOPMENT FOUNDATION
│   ├── COMPONENT_LIBRARY.md                   # Design System & Components
│   ├── API_STANDARDS.md                       # Backend Standards & Patterns
│   ├── TESTING_GUIDE.md                       # Testing Framework & Patterns
│   ├── DEVELOPMENT_WORKFLOW.md                # Development Process
│   └── PERFORMANCE_STANDARDS.md               # Performance Guidelines
│
├── features/                                   # 🎯 SIDEBAR-DRIVEN FEATURES
│   ├── 01-mein-cockpit/
│   │   ├── FC-001-OVERVIEW.md                 # Master-Dokument
│   │   ├── FC-001-dashboard-widgets.md        # Widget-System
│   │   └── FC-001-cockpit-analytics.md        # Analytics Integration
│   │
│   ├── 02-neukundengewinnung/
│   │   ├── FC-010-OVERVIEW.md                 # Master-Dokument
│   │   ├── FC-011-lead-erfassung.md          # 🚀 HIGH PRIORITY
│   │   ├── FC-012-email-posteingang.md       # E-Mail Integration
│   │   └── FC-013-kampagnen-management.md    # Campaign System
│   │
│   ├── 03-kundenmanagement/
│   │   ├── FC-020-OVERVIEW.md                 # Master-Dokument
│   │   ├── FC-021-alle-kunden.md             # ✅ ENHANCE EXISTING
│   │   ├── FC-022-neuer-kunde.md             # Customer Creation
│   │   ├── FC-023-verkaufschancen.md         # ✅ ENHANCE EXISTING
│   │   └── FC-024-aktivitaeten.md            # Activity Tracking
│   │
│   ├── 04-auswertungen/
│   │   ├── FC-030-OVERVIEW.md                 # Master-Dokument
│   │   ├── FC-031-umsatzuebersicht.md        # Revenue Analytics
│   │   ├── FC-032-kundenanalyse.md           # Customer Analytics
│   │   └── FC-033-aktivitaetsbericht.md      # Activity Reports
│   │
│   ├── 05-kommunikation/
│   │   ├── FC-040-OVERVIEW.md                 # Master-Dokument
│   │   ├── FC-041-team-chat.md               # Internal Chat
│   │   ├── FC-042-ankuendigungen.md          # Announcements
│   │   ├── FC-043-notizen.md                 # Notes System
│   │   └── FC-044-interne-nachrichten.md     # Internal Messages
│   │
│   ├── 06-einstellungen/
│   │   ├── FC-050-OVERVIEW.md                 # Master-Dokument
│   │   ├── FC-051-mein-profil.md             # User Profile
│   │   ├── FC-052-benachrichtigungen.md      # Notifications
│   │   ├── FC-053-darstellung.md             # UI Preferences
│   │   └── FC-054-sicherheit.md              # Security Settings
│   │
│   ├── 07-hilfe-support/
│   │   ├── FC-060-OVERVIEW.md                 # Master-Dokument
│   │   ├── FC-061-erste-schritte.md          # Onboarding
│   │   ├── FC-062-handbuecher.md             # Documentation
│   │   ├── FC-063-video-tutorials.md         # Video System
│   │   ├── FC-064-haeufige-fragen.md         # FAQ System
│   │   └── FC-065-support-kontakt.md         # Support Integration
│   │
│   └── 08-administration/
│       ├── FC-070-OVERVIEW.md                 # Master-Dokument
│       ├── FC-071-audit-dashboard.md         # ✅ ENHANCE EXISTING
│       ├── FC-072-benutzerverwaltung.md      # ✅ ENHANCE EXISTING
│       ├── FC-073-system-management.md       # System Tools
│       ├── FC-074-api-status.md              # ✅ ENHANCE EXISTING
│       ├── FC-075-integration-management.md  # External Integrations
│       ├── FC-076-hilfe-konfiguration.md     # ✅ ENHANCE EXISTING
│       └── FC-077-compliance-reports.md      # Compliance System
│
├── bereitstellung/                             # 🚀 DEPLOYMENT & OPERATIONS
│   ├── DEVELOPMENT_SETUP.md                   # Local Development
│   ├── STAGING_DEPLOYMENT.md                  # Pre-Production
│   ├── PRODUCTION_DEPLOYMENT.md               # Go-Live Strategy
│   └── MONITORING_ALERTS.md                   # Observability
│
├── vorlagen/                                   # 📝 REUSABLE TEMPLATES
│   ├── FEATURE_TEMPLATE.md                    # Standard Feature Template
│   ├── API_SPEC_TEMPLATE.md                   # API Documentation Template
│   ├── COMPONENT_SPEC_TEMPLATE.md             # Component Documentation
│   └── TEST_PLAN_TEMPLATE.md                  # Testing Documentation
│
└── archiv/                                     # 📦 LEGACY ARCHIVE
    ├── MIGRATION_LOG.md                       # Was migriert wurde
    ├── alte-features/                         # Alte Feature-Docs (archiviert)
    └── backup-dateien/                        # Backup-Chaos (archiviert)
```

## 🎯 **NAMING CONVENTIONS:**

### **Feature-Codes:**
```
FC-XXX: Feature Code (fortlaufend)
FC-001-009: Mein Cockpit
FC-010-019: Neukundengewinnung
FC-020-029: Kundenmanagement
FC-030-039: Auswertungen
FC-040-049: Kommunikation
FC-050-059: Einstellungen
FC-060-069: Hilfe & Support
FC-070-079: Administration
FC-080+: Zukünftige Features
```

### **File-Naming:**
```
OVERVIEW.md         # Master-Dokument pro Feature-Bereich
[feature-name].md   # Spezifische Sub-Features
TEMPLATE.md         # Wiederverwendbare Templates
```

### **Directory-Naming:**
```
XX-sidebar-name/    # Nummeriert nach Sidebar-Reihenfolge
grundlagen/         # Entwickler-Foundation
bereitstellung/     # Operations
vorlagen/           # Wiederverwendbare Docs
archiv/             # Archiv
```

## 📋 **MASTER-DOKUMENT STRUCTURE:**

Jedes OVERVIEW.md enthält:
```markdown
# FC-XXX: [Feature-Bereich] - Master-Dokument

## 🎯 Business Context
- Problem Statement
- User Stories
- Success Criteria

## 🏗️ Technical Foundation
- Required Foundation Components
- API Dependencies
- Database Requirements

## 📋 Sub-Features
- [ ] FC-XXX-1: [Sub-Feature 1]
- [ ] FC-XXX-2: [Sub-Feature 2]
- [ ] FC-XXX-3: [Sub-Feature 3]

## 🔗 Code References
- Frontend: /frontend/src/features/[feature]/
- Backend: /backend/src/main/java/de/freshplan/[feature]/
- Tests: [locations]

## 📊 Implementation Status
- Foundation: 🟡/🟢
- Backend: 🟡/🟢
- Frontend: 🟡/🟢
- Tests: 🟡/🟢
```

## 🚀 **MIGRATION-STRATEGY:**

### **Phase 1: Foundation Setup (DIESE SESSION)**
- [x] Neue Struktur erstellen
- [x] Templates definieren
- [ ] Foundation-Docs beginnen
- [ ] Legacy-Mapping

### **Phase 2: Feature-Migration (NÄCHSTE SESSIONS)**
- [ ] Bestehende Docs in neue Struktur migrieren
- [ ] OVERVIEW-Dokumente erstellen
- [ ] Cross-References aufbauen
- [ ] Legacy archivieren

### **Phase 3: Enhancement (FORTLAUFEND)**
- [ ] Neue Features in Struktur dokumentieren
- [ ] Templates verfeinern
- [ ] Entwickler-Feedback einarbeiten

## ✅ **VORTEILE DER NEUEN STRUKTUR:**

### **Für Entwickler:**
- 🎯 **Sidebar-Correlation**: Sofort klar, wo was liegt
- 🏗️ **Foundation-First**: Alle Standards an einem Ort
- 📋 **Template-Driven**: Konsistente Dokumentation
- 🔗 **Code-Correlation**: Direkte Links zu Implementation

### **Für Claude-Sessions:**
- ⚡ **Quick-Navigation**: Feature-Codes für schnelle Referenz
- 📊 **Status-Overview**: Sofort sichtbar was implementiert ist
- 🎯 **Focus-Driven**: Jedes Feature hat klaren Scope
- 🔄 **Maintainable**: Template-based für Konsistenz

### **Für Project-Management:**
- 📈 **Progress-Tracking**: Status pro Feature sichtbar
- 🎯 **Priority-Clear**: OVERVIEW zeigt Business-Context
- 🔗 **Dependencies**: Foundation-Requirements klar
- 📊 **Roadmap-Ready**: Feature-Codes für Planning

---

**🎯 Struktur steht! Ready für Foundation-First Development! 🏗️**
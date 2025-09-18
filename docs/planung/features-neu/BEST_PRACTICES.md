# 📖 Best Practice Guidelines - Sidebar-orientierte Feature-Module

**📊 Plan Status:** 🔵 Draft
**🎯 Owner:** Development Team + Claude
**⏱️ Timeline:** Permanent Guidelines
**🔧 Effort:** XS (Dokumentation)

## 🎯 Executive Summary (für Claude)

**Mission:** Einheitliche Standards für die neue sidebar-basierte Feature-Modul-Entwicklung
**Problem:** Ohne Guidelines entstehen inkonsistente Strukturen und Claude-Confusion
**Solution:** Klare Templates, Naming-Conventions und Entwicklungsrichtlinien
**Impact:** Konsistente Feature-Entwicklung, bessere Claude-Produktivität, maintainable Code

## 📋 Modul-Struktur Template

### Standard-Modul-Layout
```
XX_modul-name/
├── README.md                    # PFLICHT: Modul-Übersicht
├── submodul-1/
│   ├── README.md               # Submodul-Dokumentation
│   ├── technical-concept.md    # Technisches Konzept
│   ├── business-rules.md       # Business-Logik (wenn relevant)
│   └── api-design.md          # API-Spezifikation (wenn relevant)
├── submodul-2/
└── shared/                     # Modul-interne gemeinsame Komponenten
    ├── types.md               # TypeScript/Java Types
    ├── constants.md           # Konstanten und Konfiguration
    └── utilities.md           # Hilfsfunktionen
```

### Modul-README Template
```markdown
# 🏷️ [Modul-Name] - [Kurzbeschreibung]

**📊 Modul Status:** [🟢 Produktiv / 🔄 In Entwicklung / 📋 Geplant]
**🎯 Owner:** [Team/Person]
**📱 Sidebar Position:** [Navigation-Pfad]
**🔗 Related Modules:** [Cross-References]

## 🎯 Modul-Übersicht
[2-3 Sätze was das Modul macht]

## 🗂️ Submodule
- **submodul-1/**: [Beschreibung]
- **submodul-2/**: [Beschreibung]

## 🔗 Dependencies
### Frontend Components
- [Liste der React Components]

### Backend Services
- [Liste der Java Services/Repositories]

### External APIs
- [Externe Integrationen]

## 🚀 Quick Start für Entwickler
[3-5 Schritte um mit dem Modul zu arbeiten]

## 🤖 Claude Notes
[Wichtige Hinweise für Claude bei der Arbeit an diesem Modul]
```

## 🏗️ Naming Conventions

### Verzeichnis-Namen
- **Format:** `XX_kebab-case-name`
- **Nummerierung:** 01-99 nach Sidebar-Reihenfolge
- **Sprache:** Deutsch (entspricht Sidebar)
- **Beispiele:**
  ```
  ✅ 03_kundenmanagement/alle-kunden/
  ✅ 08_administration/audit-dashboard/
  ❌ 03_CustomerManagement/AllCustomers/
  ❌ customer-management/
  ```

### Datei-Namen
- **README.md:** Immer groß geschrieben
- **Technische Docs:** `kebab-case.md`
- **Datum-Präfix:** `YYYY-MM-DD_` nur bei temporären Docs
- **Beispiele:**
  ```
  ✅ technical-concept.md
  ✅ business-rules.md
  ✅ 2025-09-18_migration-notes.md (temporär)
  ❌ TechnicalConcept.md
  ❌ Business_Rules.md
  ```

### Modul-Codes (Legacy Mapping)
- **Neue Module:** Verwende Sidebar-Pfad als Referenz
- **Legacy FC-XXX:** Symlinks für Backward Compatibility
- **Format:** `SidebarPosition_ModulName`
- **Beispiele:**
  ```
  ✅ 03_kundenmanagement/alle-kunden/    (statt FC-005)
  ✅ 02_neukundengewinnung/email-posteingang/ (statt FC-003)
  ```

## 🔧 Entwicklungs-Workflow

### 1. Neues Feature planen
```bash
# 1. Sidebar-Position identifizieren
# 2. Passenden Modul-Ordner auswählen
cd docs/planung/features-neu/03_kundenmanagement/

# 3. Submodul erstellen
mkdir neues-feature
cd neues-feature

# 4. Template-Dateien erstellen
touch README.md technical-concept.md

# 5. Templates ausfüllen (siehe oben)
```

### 2. Bestehende Features erweitern
```bash
# 1. Bestehendes Modul finden
cd docs/planung/features-neu/03_kundenmanagement/alle-kunden/

# 2. Dokumentation lesen
cat README.md

# 3. Technisches Konzept erweitern
# 4. Cross-References aktualisieren
```

### 3. Cross-Module Integration
```bash
# Bei Features die mehrere Module betreffen:
# 1. Hauptmodul identifizieren (wo UI primär ist)
# 2. Secondary Modules in shared/ referenzieren
# 3. 00_shared/ für echte Cross-Cutting Concerns

# Beispiel: E-Mail Notifications
# Hauptmodul: 02_neukundengewinnung/email-posteingang/
# Referenz: 05_kommunikation/ankuendigungen/
# Shared: 00_shared/notification-system/
```

## 📝 Dokumentations-Standards

### PLANUNGSMETHODIK-Compliance
- **Länge:** 300-400 Zeilen max pro Dokument
- **Executive Summary:** Bei komplexen Konzepten
- **4-Phasen-Struktur:** Bei Implementierungs-Plänen
- **Claude Handover:** Bei wichtigen Dokumenten

### Technical Concepts
```markdown
# 🔧 [Feature] - Technical Concept

## 🎯 Overview
[Problem & Solution in 2-3 Sätzen]

## 🏗️ Architecture
### Frontend Components
[React Component Hierarchie]

### Backend Services
[Java Service Layer Design]

### Database Schema
[Entity/Table Changes]

### API Design
[REST Endpoint Specification]

## 🔄 Implementation Phases
[Wenn komplex, sonst weglassen]

## 🧪 Testing Strategy
[Unit/Integration/E2E Tests]

## 🤖 Claude Implementation Notes
[Wichtige Hinweise für die Implementierung]
```

### Business Rules
```markdown
# 📋 [Feature] - Business Rules

## 🎯 Business Context
[Fachlicher Hintergrund]

## 📜 Rules & Constraints
### Rule 1: [Name]
- **Condition:** [Wann gilt die Regel]
- **Action:** [Was passiert]
- **Exception:** [Ausnahmen]

## 🔀 Workflows
[User Journeys und Prozesse]

## 🧮 Calculations
[Formeln und Berechnungslogik]
```

## 🔗 Cross-Reference Management

### Interne Links
```markdown
# Zwischen Modulen
[Customer Management](../03_kundenmanagement/alle-kunden/)

# Innerhalb eines Moduls
[Technical Concept](./technical-concept.md)

# Zu Infrastructure
[CQRS Migration](../../infrastruktur/CQRS_MIGRATION_PLAN.md)

# Zu Legacy (während Migration)
[Legacy FC-005](../../features/FC-005-CUSTOMER-MANAGEMENT/)
```

### External References
```markdown
# Code Repository
[User Repository](../../../../backend/src/main/java/de/freshplan/user/)

# Master Plan
[CRM Master Plan](../../CRM_COMPLETE_MASTER_PLAN_V5.md)

# ADRs
[Database Design](../../adr/ADR-003-database-architecture.md)
```

## 🚀 Performance Guidelines

### Documentation Performance
- **Load Time:** README.md < 2 Sekunden
- **Size Limit:** Einzeldokumente < 50KB
- **Link Validation:** Alle Links funktional
- **Search Optimization:** Klare Headings für Quick Navigation

### Development Performance
- **Discovery Time:** Feature in < 30 Sekunden findbar
- **Context Switch:** Zwischen Modulen < 10 Sekunden
- **Cross-Reference:** Related Features sofort sichtbar

## 🎯 Quality Assurance

### Module-Review Checklist
- [ ] README.md vollständig ausgefüllt
- [ ] Naming Conventions eingehalten
- [ ] Cross-References validiert
- [ ] Claude-Notes für zukünftige Sessions
- [ ] PLANUNGSMETHODIK befolgt (wenn relevant)
- [ ] Integration mit Sidebar-Navigation klar
- [ ] Backend/Frontend Mapping dokumentiert

### Continuous Improvement
- **Quarterly Review:** Module-Struktur optimieren
- **Feedback Integration:** Claude-Experience verbessern
- **Performance Monitoring:** Discovery-Time messen
- **Template Updates:** Best Practices weiterentwickeln

## 🤖 Claude Handover Section

**Context:** Comprehensive Best Practice Guidelines für sidebar-orientierte Feature-Module erstellt. Templates, Naming Conventions und Workflows für konsistente Entwicklung etabliert.

**Nächste Anwendung:**
1. Templates bei FC-003 E-Mail Migration anwenden
2. README.md für bestehende Module nachrüsten
3. Cross-References systematisch validieren

**Qualitäts-Standards:**
- Module-Discovery in < 30 Sekunden
- Einheitliche Dokumentationsstruktur
- Claude-optimierte Navigation zwischen Features
- PLANUNGSMETHODIK-Integration gewährleistet

**Guidelines-Integration:**
Diese Best Practices sind jetzt der Standard für alle neuen Feature-Module und aktive Migration-Referenz für bestehende FC-XXX Features.
# 🚀 FC-005 QUICK REFERENCE FÜR NEUE CLAUDE-INSTANZEN

**Stand:** 26.07.2025  
**Status:** Dokumentation zu 100% umstrukturiert für Claude-Kompatibilität ✅

## ⚡ SCHNELLSTART

### Was ist FC-005?
**FC-005 Customer Management** ist das neue Field-Based Architecture System für dynamisches Kundendatenmanagement. Es ersetzt das alte M5 Modul.

### Warum wurde die Dokumentation umstrukturiert?
Die ursprünglichen Dokumente waren 700-1000+ Zeilen lang - zu groß für Claude's Kontext-Fenster. Jetzt sind alle Dokumente <500 Zeilen und haben vollständige Navigation.

## 📁 NEUE STRUKTUR - WO FINDE ICH WAS?

```
FC-005-CUSTOMER-MANAGEMENT/
├── README.md ⭐                    # START HIER! Hauptübersicht
├── RESTRUCTURING_PLAN.md          # Fortschritt der Umstrukturierung (95%)
├── CLAUDE_QUICK_REFERENCE.md      # Dieses Dokument
│
├── 01-TECH-CONCEPT/               # Architektur & Konzept
│   └── README.md                  # Executive Summary, Decisions, Data Model
│
├── 02-BACKEND/                    # Java/Quarkus Implementation
│   ├── 01-entities.md            # JPA Entities
│   ├── 02-services.md            # Business Logic
│   ├── 03-rest-api.md            # REST Endpoints
│   └── 04-database.md            # Schema & Migrations
│
├── 03-FRONTEND/                   # React/TypeScript
│   ├── 01-components.md          # UI Components
│   ├── 02-state-management.md   # State & Hooks
│   ├── 03-field-rendering.md    # Dynamic Fields
│   └── 04-validation.md         # Validation Logic
│
├── 04-INTEGRATION/                # System-Integration
│   ├── 01-module-dependencies.md # Abhängigkeiten
│   ├── 02-event-system.md       # Events
│   └── 03-api-gateway.md        # Gateway Pattern
│
├── 05-TESTING/                    # Test-Strategie
│   └── [Unit, Integration, E2E, Performance Tests]
│
├── 06-SECURITY/                   # Security & DSGVO
│   └── [DSGVO, Encryption, Permissions]
│
├── 07-PERFORMANCE/                # Performance & Scaling
│   ├── 01-performance-goals.md  # Ziele & DB
│   ├── 02-caching-api.md        # Caching
│   └── 03-scaling-monitoring.md # Scaling
│
└── 08-IMPLEMENTATION/             # ✅ FERTIG (6 Dokumente)
    └── [5-Tage Implementation Plan mit täglichen Checklisten]
```

## 🎯 WICHTIGSTE KONZEPTE

### 1. Field-Based Architecture
- **Dynamische Felder** statt feste Datenbank-Spalten
- **Field Catalog** definiert verfügbare Felder
- **Industry-spezifische** Feldkonfigurationen

### 2. Entities
- `Customer` - Basis-Entity
- `FieldDefinition` - Feld-Definitionen
- `FieldValue` - Gespeicherte Werte (JSONB)
- `Location` - Standorte für Kettenkunden

### 3. 3-Step Wizard Flow
1. **Kunde anlegen** (Basis-Daten)
2. **Standorte** (wenn chainCustomer='ja')
3. **Details** (branchenspezifische Felder)

## 📌 NAVIGATION TIPS

### Wenn du suchst nach...
- **Überblick:** Start bei `/README.md`
- **API Endpoints:** `/02-BACKEND/03-rest-api.md`
- **React Components:** `/03-FRONTEND/01-components.md`
- **Performance:** `/07-PERFORMANCE/README.md`
- **Security/DSGVO:** `/06-SECURITY/01-dsgvo-compliance.md`

### Jedes Dokument hat:
```markdown
**Navigation:**
- **Parent:** [Link zum übergeordneten Dokument]
- **Previous/Next:** [Navigation in der Sektion]
- **Related:** [Verwandte Dokumente]
```

## ⚠️ WICHTIGE HINWEISE

### Alte Dokumente
Die Original-Dokumente (2025-07-26_*.md) existieren noch im Hauptverzeichnis, werden aber nach Validierung archiviert. Nutze IMMER die neue Struktur!

### TODOs
- ✅ 07-PERFORMANCE/ fertiggestellt (26.07.2025 17:00)
- ✅ 08-IMPLEMENTATION/ fertiggestellt (26.07.2025 17:10)
- ✅ Alle 33 Dokumente erstellt und navigierbar
- ⏳ Cross-References validieren
- ⏳ Alte Dokumente archivieren

### Master Plan V5
Der Master Plan wurde vollständig aktualisiert:
- FC-005 Status: "🔄 In Progress" mit "Docs 100% ✅"
- Direkter Link zur neuen Struktur
- Warnung für Claude über komplette Umstrukturierung (33 Dokumente)

## 🚀 NÄCHSTE SCHRITTE

1. **Umstrukturierung ist FERTIG! ✅**
   - Alle 33 Dokumente erstellt
   - RESTRUCTURING_PLAN.md zeigt 100% Fortschritt

2. **Bei Implementation:**
   - Start mit Field Catalog JSON
   - Dann CustomerOnboardingWizard.tsx
   - Siehe `/01-TECH-CONCEPT/04-implementation-plan.md`

---

**Tipp:** Nutze die Glob/Grep Tools mit dem Pfad `/docs/features/FC-005-CUSTOMER-MANAGEMENT/` für effiziente Suche in der neuen Struktur!
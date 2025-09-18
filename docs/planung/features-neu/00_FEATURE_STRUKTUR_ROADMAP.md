# 🗂️ Neue Feature-Struktur basierend auf Sidebar-Navigation

**📊 Plan Status:** 🔵 Draft
**🎯 Owner:** Development Team + Product Team
**⏱️ Timeline:** Q4 2025 → Q1 2026
**🔧 Effort:** M (Medium - Strukturelle Reorganisation)

## 🎯 Executive Summary (für Claude)

**Mission:** Reorganisation der Feature-Planung nach der finalen Sidebar-Navigation-Struktur
**Problem:** Aktuelle Feature-Codes (FC-XXX) entsprechen nicht der User-Journey-basierten Navigation
**Solution:** Neue modulare Struktur mit 7 Hauptbereichen + Untermodulen
**Impact:** Bessere Übersicht, intuitive Entwicklung, User-Journey-orientierte Planung

## 🗂️ Neue Verzeichnisstruktur

### Basis-Struktur
```
docs/planung/features-neu/
├── 01_mein-cockpit/                    # 🏠 Dashboard & Insights
├── 02_neukundengewinnung/              # 👤 Lead Management
├── 03_kundenmanagement/                # 👥 CRM Core
├── 04_auswertungen/                    # 📊 Analytics & Reports
├── 05_kommunikation/                   # 💬 Team Communication
├── 06_einstellungen/                   # ⚙️ User Configuration
├── 07_hilfe-support/                   # 🆘 Help System
├── 08_administration/                  # 🔐 Admin Functions
└── 00_shared/                          # 🔗 Cross-Cutting Concerns
```

### Detaillierte Struktur mit aktuellen Features

#### 01_mein-cockpit/
```
01_mein-cockpit/
├── README.md                           # Modul-Übersicht
├── dashboard-core/                     # Basis Dashboard
├── kpi-widgets/                        # KPI-Anzeigen
├── recent-activities/                  # Letzte Aktivitäten
└── quick-actions/                      # Schnellaktionen
```
**Status:** ✅ Funktional (bereits implementiert)

#### 02_neukundengewinnung/
```
02_neukundengewinnung/
├── README.md
├── email-posteingang/                  # E-Mail Management
│   ├── email-sync-engine.md           # ← FC-003 Migration
│   ├── provider-integration.md        # ← FC-003 Migration
│   └── email-tracking.md              # ← FC-003 Migration
├── lead-erfassung/                     # Lead Capture
└── kampagnen/                          # Campaign Management
```
**Status:** 📋 Geplant (FC-003 E-Mail Integration migrieren)

#### 03_kundenmanagement/
```
03_kundenmanagement/
├── README.md
├── alle-kunden/                        # Customer List
│   └── field-based-architecture.md    # ← FC-005 Migration
├── neuer-kunde/                        # Customer Creation
├── verkaufschancen/                    # Sales Pipeline
│   └── opportunity-pipeline.md         # ← M4 Implementation
└── aktivitaeten/                       # Activity Timeline
    └── activity-notes-system.md       # ← FC-013 Migration
```
**Status:** ✅ Teilweise funktional (M4 Pipeline, FC-005 in Arbeit)

#### 04_auswertungen/
```
04_auswertungen/
├── README.md
├── umsatzuebersicht/                   # Revenue Analytics
├── kundenanalyse/                      # Customer Analytics
└── aktivitaetsbericht/                 # Activity Reports
    └── kpi-tracking-reporting.md       # ← FC-016 Migration
```
**Status:** 📋 Geplant

#### 05_kommunikation/
```
05_kommunikation/
├── README.md
├── team-chat/                          # Internal Chat
├── ankuendigungen/                     # Announcements
├── notizen/                            # Notes System
└── interne-nachrichten/                # Internal Messages
```
**Status:** 📋 Geplant

#### 06_einstellungen/
```
06_einstellungen/
├── README.md
├── mein-profil/                        # User Profile
├── benachrichtigungen/                 # Notifications
├── darstellung/                        # UI Preferences
└── sicherheit/                         # Security Settings
    └── rights-roles-concept.md         # ← FC-015 Migration
```
**Status:** 📋 Geplant

#### 07_hilfe-support/
```
07_hilfe-support/
├── README.md
├── erste-schritte/                     # Onboarding
├── handbuecher/                        # Documentation
├── video-tutorials/                    # Video Content
├── haeufige-fragen/                    # FAQ System
└── support-kontaktieren/               # Support Contact
```
**Status:** 📋 Geplant

#### 08_administration/
```
08_administration/
├── README.md
├── audit-dashboard/                    # Audit Trail
│   └── audit-trail-system.md          # ← FC-012 Migration
├── benutzerverwaltung/                 # User Management
├── system/                             # System Management
│   ├── api-status/                     # API Monitoring
│   ├── system-logs/                    # Log Management
│   ├── performance/                    # Performance Monitoring
│   └── backup-recovery/                # Backup System
├── integration/                        # External Integrations
│   ├── ki-anbindungen/                 # AI Integrations
│   ├── xentral/                        # Xentral Integration
│   ├── email-service/                  # E-Mail Service
│   ├── payment-provider/               # Payment Integration
│   ├── webhooks/                       # Webhook Management
│   └── neue-integration/               # Integration Framework
├── hilfe-konfiguration/                # Help System Config
│   ├── hilfe-system-demo/              # Help Demo
│   ├── tooltips-verwalten/             # Tooltip Management
│   ├── touren-erstellen/               # Tour Creation
│   └── analytics/                      # Help Analytics
└── compliance-reports/                 # Compliance Reporting
    └── datenschutz-dsgvo-compliance.md # ← FC-018 Migration
```
**Status:** ✅ Teilweise funktional (Audit Dashboard, Benutzerverwaltung, API Status, Hilfe-System Demo)

## 🔄 Migration-Mapping von bestehenden Features

### Aktuelle FC-XXX → Neue Struktur
| Aktueller Code | Neuer Pfad | Status |
|----------------|-------------|--------|
| FC-003 E-Mail Integration | `02_neukundengewinnung/email-posteingang/` | 🔄 Migration |
| FC-005 Customer Management | `03_kundenmanagement/alle-kunden/` | ✅ In Arbeit |
| M4 Pipeline | `03_kundenmanagement/verkaufschancen/` | ✅ Implementiert |
| FC-012 Audit Trail | `08_administration/audit-dashboard/` | ✅ Funktional |
| FC-013 Activity Notes | `03_kundenmanagement/aktivitaeten/` | 🔄 Migration |
| FC-015 Rights & Roles | `06_einstellungen/sicherheit/` | 🔄 Migration |
| FC-016 KPI Tracking | `04_auswertungen/aktivitaetsbericht/` | 🔄 Migration |
| FC-018 DSGVO Compliance | `08_administration/compliance-reports/` | 🔄 Migration |

## 🤖 Claude Handover Section

**Context:** Neue Feature-Struktur basierend auf finaler Sidebar-Navigation (Stand 17.09.2025) erstellt. 8 Hauptbereiche + Untermodule entsprechen der User-Journey.

**Nächste Schritte:**
1. Migration-Plan für bestehende FC-XXX Features entwickeln
2. Best Practice Guidelines für neue Struktur definieren
3. Schrittweise Migration der dokumentierten Features

**Migration-Priorität:**
- **Hoch:** FC-005 (Customer Management) - bereits in Arbeit
- **Medium:** FC-003 (E-Mail), FC-013 (Activities), FC-016 (KPI)
- **Low:** FC-015 (Rights), FC-018 (DSGVO) - weniger dringend
# 🔄 Migration Plan: FC-XXX → Sidebar-basierte Struktur

**📊 Plan Status:** 🔵 Draft
**🎯 Owner:** Development Team + Claude
**⏱️ Timeline:** Q4 2025 (4 Wochen)
**🔧 Effort:** M (Medium - Dokumentations-Reorganisation)

## 🎯 Executive Summary (für Claude)

**Mission:** Schrittweise Migration bestehender FC-XXX Features in die neue sidebar-orientierte Struktur
**Problem:** 20+ FC-Features sind nicht nach User-Journey organisiert
**Solution:** Systematische Migration mit Prioritätsstufen und Backward-Compatibility
**Impact:** Intuitive Feature-Navigation, bessere Entwickler-Produktivität, User-Journey-Alignment

## 📋 Migration-Matrix

### Priorität 1: Aktive Features (Woche 1-2)
| Aktueller Code | Neuer Pfad | Status | Migration-Aufwand |
|----------------|-------------|--------|-------------------|
| **FC-005 Customer Mgmt** | `03_kundenmanagement/alle-kunden/` | 🔄 In Arbeit | S (bereits strukturiert) |
| **M4 Pipeline** | `03_kundenmanagement/verkaufschancen/` | ✅ Implementiert | XS (nur Referenz) |
| **FC-012 Audit Trail** | `08_administration/audit-dashboard/` | ✅ Funktional | XS (nur Referenz) |

### Priorität 2: Core Business Features (Woche 2-3)
| Aktueller Code | Neuer Pfad | Status | Migration-Aufwand |
|----------------|-------------|--------|-------------------|
| **FC-003 E-Mail Integration** | `02_neukundengewinnung/email-posteingang/` | 📋 Geplant | M (Multi-Dokument) |
| **FC-013 Activity Notes** | `03_kundenmanagement/aktivitaeten/` | 📋 Geplant | S (Einzeldokument) |
| **FC-016 KPI Tracking** | `04_auswertungen/aktivitaetsbericht/` | 📋 Geplant | M (Umfangreich) |
| **FC-009 Contract Renewal** | `03_kundenmanagement/verkaufschancen/` | 📋 Geplant | S (Business Logic) |

### Priorität 3: System & Admin Features (Woche 3-4)
| Aktueller Code | Neuer Pfad | Status | Migration-Aufwand |
|----------------|-------------|--------|-------------------|
| **FC-015 Rights & Roles** | `06_einstellungen/sicherheit/` | 📋 Geplant | S (Security) |
| **FC-018 DSGVO Compliance** | `08_administration/compliance-reports/` | 📋 Geplant | L (Komplex) |
| **FC-014 Mobile/Tablet** | `00_shared/mobile-optimization/` | 📋 Geplant | M (Cross-Cutting) |
| **FC-017 Error Handling** | `00_shared/error-handling/` | 📋 Geplant | L (System-wide) |

## 🔧 Migration-Prozess pro Feature

### Phase 1: Dokumentations-Migration
1. **Analyse:** Bestehende FC-XXX Dokumentation erfassen
2. **Mapping:** Zuordnung zu neuem Modul-Pfad
3. **Reorganisation:** Inhalte in neue Struktur übertragen
4. **Cross-References:** Links und Abhängigkeiten aktualisieren

### Phase 2: Code-Integration
1. **Backend-Mapping:** Java-Packages zu Modulen zuordnen
2. **Frontend-Mapping:** React-Components zu Sidebar-Navigation
3. **API-Consistency:** REST-Endpoints nach Modulen organisieren
4. **Test-Migration:** Tests in neue Struktur einordnen

### Phase 3: Qualitätssicherung
1. **PLANUNGSMETHODIK-Check:** Alle Module nach Standards
2. **Navigation-Test:** Sidebar-Logik mit Features validieren
3. **Documentation-Update:** Master Plan und Cross-References
4. **Claude-Handover:** Kontinuität für zukünftige Sessions

## 📁 Detaillierte Migration-Aktionen

### FC-005 Customer Management → `03_kundenmanagement/alle-kunden/`
**Status:** 🔄 In Arbeit
**Aufwand:** S (bereits gut strukturiert)

**Migrationsschritte:**
1. ✅ Field-Based Architecture bereits dokumentiert
2. 📋 README.md für Modul erstellen
3. 📋 Integration mit "Neuer Kunde" und "Verkaufschancen"
4. 📋 Cross-References zu FC-013 Activities

### FC-003 E-Mail Integration → `02_neukundengewinnung/email-posteingang/`
**Status:** 📋 Geplant
**Aufwand:** M (Multi-Dokument-Migration)

**Aktuell in `/features/FC-003/`:**
- `email-sync-engine.md`
- `template-system.md`
- `provider-integration.md`
- `email-tracking.md`

**Neue Struktur:**
```
02_neukundengewinnung/email-posteingang/
├── README.md                    # Modul-Übersicht
├── sync-engine.md              # ← email-sync-engine.md
├── template-system.md          # ← template-system.md
├── provider-integration.md     # ← provider-integration.md
└── tracking-analytics.md       # ← email-tracking.md
```

### FC-016 KPI Tracking → `04_auswertungen/aktivitaetsbericht/`
**Status:** 📋 Geplant
**Aufwand:** M (Umfangreiches Dokument)

**Migrationsschritte:**
1. 📋 Kern-KPI-System in `aktivitaetsbericht/` verschieben
2. 📋 Renewal-Metriken zu `03_kundenmanagement/verkaufschancen/`
3. 📋 Dashboard-Widgets zu `01_mein-cockpit/kpi-widgets/`
4. 📋 Cross-Module Analytics Framework

## 🔗 Backward Compatibility

### Legacy-Pfad-Mapping
```bash
# Alte FC-XXX Referenzen automatisch umleiten
FC-003 → 02_neukundengewinnung/email-posteingang/
FC-005 → 03_kundenmanagement/alle-kunden/
FC-012 → 08_administration/audit-dashboard/
FC-013 → 03_kundenmanagement/aktivitaeten/
FC-015 → 06_einstellungen/sicherheit/
FC-016 → 04_auswertungen/aktivitaetsbericht/
FC-018 → 08_administration/compliance-reports/
```

### Script für automatische Migration
```bash
#!/bin/bash
# Erstelle Symlinks für Backward Compatibility
ln -s ../../features-neu/03_kundenmanagement/alle-kunden/ FC-005-CUSTOMER-MANAGEMENT
ln -s ../../features-neu/08_administration/audit-dashboard/ FC-012
# ... weitere Mappings
```

## 📊 Success Metrics

### Migration-KPIs (4 Wochen)
- **Woche 1:** Priorität 1 Features migriert (3/3)
- **Woche 2:** Priorität 2 Features migriert (4/4)
- **Woche 3:** Priorität 3 Features migriert (4/4)
- **Woche 4:** Qualitätssicherung + Documentation-Update

### Qualitäts-Ziele
- **PLANUNGSMETHODIK-Compliance:** 100% aller neuen Module
- **Cross-References:** Alle Links funktional
- **Navigation-Logic:** Sidebar → Features vollständig mapped
- **Claude-Readiness:** Alle Module Claude-optimiert

## 🤖 Claude Handover Section

**Context:** Detaillierter Migration-Plan von FC-XXX zu sidebar-basierter Feature-Struktur erstellt. 11 Features identifiziert mit 3 Prioritätsstufen über 4 Wochen.

**Nächste konkrete Aktionen:**
1. FC-005 Customer Management finalisieren (bereits in Arbeit)
2. FC-003 E-Mail Integration migrieren (4 Dokumente)
3. Best Practice Guidelines für neue Modulstruktur definieren

**Migration-Koordination:**
- Priorität 1 Features laufen parallel zu aktueller Entwicklung
- Infrastructure-Pläne sind unabhängig von Feature-Migration
- Master Plan koordiniert beide Reorganisations-Tracks

**Qualitäts-Check:**
✅ 11 Features systematisch erfasst und priorisiert
✅ Backward Compatibility durch Symlinks gewährleistet
✅ 4-Wochen-Timeline mit konkreten Wochenmeilensteinen
✅ Integration mit bestehender PLANUNGSMETHODIK
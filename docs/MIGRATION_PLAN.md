# 📋 MIGRATION PLAN - Systematische Bereinigung

**Erstellt:** 21.07.2025 00:05  
**Status:** IN ARBEIT  
**Ziel:** Konsistente Planungsstruktur für zuverlässige Entwicklung

## 📊 AUSGANGSLAGE (Phase 1 Analyse)

### Identifizierte Probleme:
1. **35 Feature-Codes mit Duplikaten** (FC-001 bis M8)
2. **28 Platzhalter-Dokumente** ohne Inhalt
3. **13 Ordner-Nummern** doppelt vergeben
4. **Registry nicht synchron** mit Dateisystem

### Besonders kritisch:
- FC-002: 45 Duplikate! (Legacy UI System)
- FC-033: 12 Duplikate
- FC-034/035/036: je 10 Duplikate
- Module M1-M8: zwischen ACTIVE und LEGACY verstreut

## 🎯 MIGRATIONS-STRATEGIE

### Grundprinzipien:
1. **Keine Daten verlieren** - Alles wird verschoben, nicht gelöscht
2. **Git Historie erhalten** - Nur `git mv` verwenden
3. **Schrittweise vorgehen** - Ein Problem nach dem anderen
4. **Dokumentiert** - Jede Änderung nachvollziehbar

## 📝 PHASE 2: BEREINIGUNG MIT PROTOKOLL

### 2.1 Platzhalter-Bereinigung

| Datei | Aktion | Grund |
|-------|--------|-------|
| LEGACY/pdf_generator/PDF_GENERATOR_TECH_CONCEPT.md | Löschen | Placeholder ohne Inhalt |
| LEGACY/02_navigation_legacy/FC-002_TECH_CONCEPT.md | Löschen | Placeholder ohne Inhalt |
| PLANNED/36_commission_engine/FC-036_TECH_CONCEPT.md | Löschen | Placeholder ohne Inhalt |
| ... 25 weitere ... | Löschen | Alle Placeholder |

**Backup-Ordner:** `docs/features.backup.placeholders.20250721/`

### 2.2 Feature-Code Duplikate Auflösung

#### FC-002 (45 Duplikate) → ARCHIVIEREN
```
ACTIVE/01_security/FC-002*.md → LEGACY/FC-002-ARCHIVE/security/
LEGACY/FC-002/* → LEGACY/FC-002-ARCHIVE/original/
PLANNED/02_smart_insights/FC-002* → behalten (neues FC-002)
```

#### FC-010 (8 Duplikate) → KONSOLIDIEREN
```
ACTIVE/13_import_management/FC-010* → behalten (primär)
PLANNED/11_customer_import/FC-010* → FC-050 (umbenennen)
ACTIVE/01_security/FC-010* → LEGACY/FC-010-ARCHIVE/
```

#### FC-016 (3 Duplikate) → EINDEUTIG MACHEN
```
PLANNED/18_opportunity_cloning/FC-016* → behalten (primär)
PLANNED/02_opportunity_pipeline/FC-016* → FC-051 (umbenennen)
```

#### FC-017 (3 Duplikate) → EINDEUTIG MACHEN
```
PLANNED/99_sales_gamification/FC-017* → behalten (primär)
PLANNED/18_sales_gamification/FC-017* → löschen (Placeholder)
```

#### FC-018 (5 Duplikate) → AUFTEILEN
```
PLANNED/09_mobile_app/FC-018* → behalten (Mobile Core)
PLANNED/19_mobile_pwa/FC-018* → FC-053 (PWA spezifisch)
PLANNED/22_mobile_light/FC-018* → FC-054 (Light Version)
```

#### FC-033-036 (je 10-12 Duplikate) → KONSOLIDIEREN
```
PLANNED/33_visual_cards/FC-033* → behalten
ACTIVE/01_security/FC-033* → LEGACY/security-archive/
Andere FC-033 → individuelle neue Nummern

Gleiches Schema für FC-034, FC-035, FC-036
```

#### Module M1-M8 → NUR IN UI_FOUNDATION
```
ACTIVE/05_ui_foundation/M* → behalten (primär)
ACTIVE/01_security/M* → löschen (Duplikate)
LEGACY/*/M* → LEGACY/module-archive/
```

### 2.3 Ordner-Nummerierung bereinigen

| Alt | Neu | Feature |
|-----|-----|---------|
| 01_security | ARCHIVE/01_security_old | Alte Security Docs |
| 01_customer_management | 50_customer_management | Verschoben |
| 18_sales_gamification | ARCHIVE/18_sales_gamification | Placeholder |
| 19_mobile_pwa | 53_mobile_pwa | Neue Nummer |
| 31_dynamic_documents | 55_dynamic_documents | Neue Nummer |
| 32_predictive_analytics | 56_predictive_analytics | Neue Nummer |
| 32_field_service_time | 57_field_service_time | Neue Nummer |
| 33_customer_360 | 58_customer_360 | Neue Nummer |
| 33_sla_management | 59_sla_management | Neue Nummer |
| ... weitere Konflikte ... | 60+ | Fortlaufend |

## 📂 PHASE 3: NEUE STRUKTUR

### Finale Ordnerstruktur:
```
docs/features/
├── ACTIVE/
│   ├── 01_security_foundation/     # FC-008
│   ├── 02_opportunity_pipeline/    # M4, FC-011
│   ├── 03_calculator_modal/        # M8
│   ├── 04_permissions_system/      # FC-009
│   ├── 05_ui_foundation/           # M1-M3, M7
│   └── 13_import_management/       # FC-010
├── PLANNED/
│   ├── 01_customer_acquisition/    # FC-001
│   ├── 02_smart_insights/          # FC-002 (NEU!)
│   ├── 06_email_integration/       # FC-003
│   ├── ... (eindeutige Nummern) ...
│   └── 64_api_gateway_v2/          # FC-064
├── LEGACY/
│   ├── FC-002-ARCHIVE/             # Alte UI Docs
│   ├── module-archive/             # Alte Module
│   └── placeholders-backup/        # Gelöschte Placeholders
└── ARCHIVE/
    └── migration-2025-07-21/       # Alte Strukturen
```

### Feature Registry Update:
- Alle neuen Codes (FC-050 bis FC-064) eingetragen
- LEGACY Bereich dokumentiert
- Klare Status-Zuordnung

## 🔧 PHASE 4: VALIDIERUNG

### Erfolgs-Kriterien:
- [ ] Keine Feature-Code Duplikate
- [ ] Keine Ordner-Nummern Duplikate
- [ ] Keine Placeholder Dokumente
- [ ] Registry 100% synchron
- [ ] Alle Tests grün

### Validierungs-Scripts:
```bash
./scripts/validate-structure.sh     # Muss 100% grün sein
./tests/test-unique-codes.sh       # Keine Duplikate
./tests/test-structure-integrity.sh # Konsistenz
```

## 📅 ZEITPLAN

1. **Platzhalter löschen:** 30 Min (JETZT)
2. **FC-002 aufräumen:** 45 Min
3. **Andere Duplikate:** 1 Std
4. **Ordner umbenennen:** 45 Min
5. **Validierung:** 30 Min
6. **Git Commit:** 15 Min

**Gesamt:** ~3-4 Stunden für saubere Struktur

## ✅ DANACH

Mit konsistenter Struktur können wir:
- FC-008 Security implementieren
- Reality Check nutzen
- Ohne Chaos entwickeln
- Zuverlässig planen

---

**Status:** Bereit für Ausführung nach Freigabe
# 🗂️ CHANGE LOG: Alte Ordner archiviert

**Datum:** 22.07.2025 13:18
**Bearbeiter:** Claude
**Aktion:** Archivierung alter ACTIVE Ordner

## 📋 Was wurde geändert?

### Archivierte Ordner (6):
1. `01_security` → `LEGACY_PRE_V5/01_security`
2. `12_permission_system` → `LEGACY_PRE_V5/12_permission_system`
3. `13_import_management` → `LEGACY_PRE_V5/13_import_management`
4. `01_customer_management` → `LEGACY_PRE_V5/01_customer_management`
5. `03_customer_management` → `LEGACY_PRE_V5/03_customer_management`
6. `28_pdf_export` → `LEGACY_PRE_V5/28_pdf_export`

### Gelöschte Ordner (1):
1. `00_basic_setup` - Leerer Platzhalter ohne Funktion

### Feature Registry korrigiert:
- FC-010 von ACTIVE → PLANNED verschoben
- Korrekter Name: "Customer Import" (nicht "Import Management")
- Korrekter Pfad: `PLANNED/11_customer_import`

## 🎯 Warum?

### 01_security:
- Dublette zu `01_security_foundation` (FC-008)
- Keine CLAUDE_TECH Datei
- Nur Placeholder und alte Dateien

### 12_permission_system:
- Dublette zu `04_permissions_system` (FC-009)
- Keine CLAUDE_TECH Datei
- Nur Placeholder README

### 13_import_management:
- FC-010 ist laut V5 PLANNED, nicht ACTIVE
- Keine CLAUDE_TECH Datei im Ordner
- Echte CLAUDE_TECH ist in `PLANNED/11_customer_import`

## 📊 Status nach Bereinigung:

### ✅ V5-konforme ACTIVE Ordner (5):
- 01_security_foundation (FC-008) ✅
- 02_opportunity_pipeline (M4, FC-011) ✅
- 03_calculator_modal (M8) ✅
- 04_permissions_system (FC-009) ✅
- 05_ui_foundation (M1, M2, M3, M7) ✅

### ⚠️ Verbleibende unsichere Ordner (7):
- 00_basic_setup
- 01_customer_management (könnte zu M5 gehören)
- 03_customer_management
- 12_file_management
- 28_pdf_export
- 30_smart_search
- pdf-generator

Diese wurden NICHT archiviert, da unklar ist ob sie zu geplanten Features gehören.

## ✅ Verifizierung:

```bash
# Archivierte Ordner:
ls docs/features/LEGACY_PRE_V5/
# ✅ 01_security, 12_permission_system, 13_import_management

# Feature Registry:
grep FC-010 docs/FEATURE_REGISTRY.md
# ✅ Jetzt in PLANNED Sektion
```
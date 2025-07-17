# 🧭 V5 NAVIGATION GUIDE - Für Standardübergabe

**Zweck:** Quick Reference für neue Dokumentenstruktur  
**Gilt ab:** 17.07.2025  

## 📂 WO FINDE ICH WAS?

### Bei Feature-Arbeit:
```
/docs/features/ACTIVE/[XX_feature_name]/
├── QUICKSTART.md      # ⚡ Start hier (15 Min)
├── KOMPAKT.md         # 📄 Übersicht (wenn vorhanden)
├── README.md          # 📚 Vollständige Doku
├── WORK_STATUS.md     # 🚨 Was ist zu tun?
├── DECISION_LOG.md    # ❓ Offene Fragen
└── TECHNICAL_*.md     # 🔧 Implementierungs-Details
```

### Priorität beim Lesen:
1. **QUICKSTART.md** - Wenn in Eile
2. **KOMPAKT.md** - Für Überblick
3. **WORK_STATUS.md** - Für konkreten Stand
4. **README.md** - Für Kontext
5. **Rest** - Bei Bedarf

## 🎯 NEUE REGELN

1. **COMPLETED/** enthält NUR 100% fertige Features
2. **WORK_STATUS.md** ist die Wahrheit über den Stand
3. **DECISION_LOG.md** zeigt was offen ist
4. **QUICKSTART.md** reicht für produktiven Start

## 🔄 IN DIE ÜBERGABE SCHREIBEN:

```markdown
## 📂 AKTIVES MODUL
**Feature:** FC-009 Permissions System
**Status:** Planning / Implementation / Testing
**Quick Start:** /docs/features/ACTIVE/04_permissions_system/QUICKSTART.md ⚡
**Arbeitsstand:** /docs/features/ACTIVE/04_permissions_system/WORK_STATUS.md
**Offene Fragen:** 3 - siehe DECISION_LOG.md
**Nächster Schritt:** [aus WORK_STATUS.md kopieren]
```

## ⚠️ WICHTIG

- Nicht mehr "Modul-Dokument" sondern "Modul-Ordner"
- Master Feature Overview zeigt ALLE Features
- Bei Unklarheit: QUICKSTART.md hat Vorrang!
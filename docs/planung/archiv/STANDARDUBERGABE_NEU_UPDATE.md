# 📋 STANDARDÜBERGABE NEU - Update Vorschlag

**Basierend auf:** Neue Struktur mit Master Plan V5 und Quality Standards

## 🔄 Empfohlene Ergänzungen zu STANDARDÜBERGABE_NEU.md

### Nach Zeile 36 (Quick-Start) ergänzen:

```bash
# Quality Check vor Commit:
./scripts/quality-check.sh

# PR vorbereiten:
./scripts/prepare-pr.sh
```

### Nach Zeile 83 (Pflicht-Lektüre) ergänzen:

**Neue Pflicht-Lektüre Reihenfolge:**
1. `/docs/CLAUDE.md` - Arbeitsrichtlinien und Standards
2. Letzte Übergabe in `/docs/claude-work/daily-work/YYYY-MM-DD/`
3. `/docs/CRM_COMPLETE_MASTER_PLAN_V5.md` → **Claude Working Section**
4. `/docs/features/ACTIVE/*/README.md` - Aktuelles Modul
5. `/docs/features/OPEN_QUESTIONS_TRACKER.md` - Offene Fragen

### Neuer Abschnitt nach "Orientierung":

## 🏆 Quality Standards Integration

**Bei JEDER Arbeitssession gilt:**
- Frontend MUSS MainLayoutV2 verwenden
- Tests MÜSSEN geschrieben werden (≥80% Coverage)
- Code MUSS formatiert sein vor Commit
- Two-Pass Review MUSS durchgeführt werden

**Referenz:** `/docs/features/QUALITY_STANDARDS.md`
**Workflow:** `/docs/features/DAILY_WORKFLOW.md`

### Update für "Arbeiten" Abschnitt:

#### 📝 Neue Arbeitsstruktur:
- **Aktives Modul** findest du in `/docs/features/ACTIVE/`
- **Fortschritt** trackst du in der README.md Checkliste
- **Commits** alle 2-4 Stunden (siehe Daily Workflow)
- **Quality Checks** vor jedem Commit mit `./scripts/quality-check.sh`

### Update für Scripts:

```bash
# ALTE Scripts (funktionieren noch, aber veraltet):
./scripts/get-active-module.sh  # Nutzt .current-focus

# NEUE Navigation:
ls -la docs/features/ACTIVE/     # Zeigt aktuelle Module
cat docs/CRM_COMPLETE_MASTER_PLAN_V5.md | grep -A 20 "Claude Working Section"
```

## 🎯 Kernpunkte die bleiben MÜSSEN:

1. ✅ 5-Schritte-System
2. ✅ Service-Checks
3. ✅ Git-Workflow
4. ✅ Problemlösung-Prozess
5. ✅ Übergabe-Erstellung

## 🆕 Was NEU dazukommt:

1. **Master Plan V5** als zentrale Navigation
2. **ACTIVE/ Ordner** für laufende Arbeit
3. **Quality Standards** als Pflicht
4. **Daily Workflow** als Guide
5. **Automatisierte Scripts** für Quality

## 💡 Empfehlung:

STANDARDÜBERGABE_NEU.md sollte ein **kurzes Update** bekommen, das auf die neuen Dokumente verweist, aber die bewährte Struktur beibehält. Die Details sind in den spezialisierten Dokumenten (Quality Standards, Daily Workflow) gut aufgehoben.
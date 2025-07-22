# 📅 Daily Workflow & Quality Checkpoints

**Zweck:** Strukturierter Tagesablauf mit integrierten Qualitäts-Checks  
**Ziel:** Enterprise-Code ohne Kontext-Überlastung

## 🌅 Morgen-Routine (30 Min)

```bash
# 1. Session Start
./scripts/session-start.sh

# 2. Git Sync
git pull origin main
git checkout feature/current-module

# 3. Quick Context
cat docs/CRM_COMPLETE_MASTER_PLAN_V5.md | grep -A 20 "Claude Working Section"
cat docs/features/ACTIVE/*/README.md | grep -A 10 "Implementation Checklist"

# 4. Tests vom Vortag
npm run test
```

## 💻 Entwicklungs-Blöcke (2-3 Std)

### Block-Struktur:
```
09:00-11:00  Feature Development
11:00-11:15  Commit + Tests
11:15-13:00  Feature Development  
13:00-13:30  Mittagscommit + Review
```

### Nach jedem Block:
```bash
# Quick Quality Check
npm run lint
npm run test:unit -- --watch=false

# Commit
git add -p
git commit -m "feat(MX): [was wurde gemacht]"
```

## 🏆 Modul-Abschluss Routine

### 1️⃣ Test-Suite (1 Std)
```bash
# Vollständige Test-Suite
npm run test:all

# Test-Report generieren
npm run test:report

# Coverage prüfen
npm run test:coverage
```

### 2️⃣ Code-Hygiene (30 Min)
```bash
# Automatische Formatierung
npm run format:fix
cd backend && ./mvnw spotless:apply

# Security Check
npm audit fix
cd backend && ./mvnw dependency-check

# Bundle-Size Check
npm run build && npm run analyze
```

### 3️⃣ Two-Pass Review (45 Min)

**Pass 1: Automatisch (15 Min)**
```bash
./scripts/code-review-pass1.sh
# Output: PASS ✅ oder Fixes needed ❌
```

**Pass 2: Manuell (30 Min)**
- [ ] Architektur-Check (Schichten eingehalten?)
- [ ] Business Logic korrekt?
- [ ] Error Handling vollständig?
- [ ] Performance innerhalb Budget?
- [ ] Security Best Practices?

### 4️⃣ PR-Vorbereitung (30 Min)
```bash
# Wenn alle Checks ✅
./scripts/prepare-pr.sh

# Erstellt:
# - PR-Branch
# - Changelog
# - PR-Template
# - Screenshots (bei UI)
```

## 📊 Wochen-Rhythmus

### Montag
- Planning für Woche
- Module Setup
- Dependencies prüfen

### Dienstag-Donnerstag  
- Feature Development
- Tägliche Commits
- Incremental Tests

### Freitag
- Modul-Abschluss
- Full Test Suite
- PR erstellen
- Dokumentation

## ⚡ Quick-Reference Commands

```bash
# Kontext nicht verlieren
alias ctx='cat docs/CRM_COMPLETE_MASTER_PLAN_V5.md | grep -A 20 "Claude Working Section"'
alias todo='cat docs/features/ACTIVE/*/README.md | grep "\[ \]"'

# Quality Shortcuts  
alias qc='npm run lint && npm run test:unit'
alias review='./scripts/code-review-pass1.sh'
alias pr='./scripts/prepare-pr.sh'

# Git Shortcuts
alias wip='git add . && git commit -m "wip: $(date +%H:%M) - checkpoint"'
alias pushpr='git push origin HEAD'
```

## 🎯 Kontext-Management

### Was NICHT in jedem Commit dokumentieren:
- MainLayoutV2 Nutzung (ist Standard)
- Test-Pflicht (ist Standard)
- Review-Process (ist Standard)

### Was IMMER dokumentieren:
- Abweichungen vom Standard
- Offene Fragen
- Performance-Probleme
- Security-Bedenken

## 📋 Tägliche Checkliste

```markdown
## Tag X - [Modul]

### ✅ Morgen
- [ ] Git Pull & Sync
- [ ] Context Check
- [ ] Tests grün?

### ✅ Entwicklung  
- [ ] Feature 1 implementiert
- [ ] Tests geschrieben
- [ ] Commit 11:00
- [ ] Feature 2 implementiert
- [ ] Tests geschrieben  
- [ ] Commit 15:00

### ✅ Abend
- [ ] Format & Lint
- [ ] WIP Commit
- [ ] Handover Update

### 📝 Notizen
- [Offene Fragen]
- [Probleme]
- [Morgen fortsetzen bei...]
```

Dieser Workflow ist optimiert für:
- **Qualität** ohne Overhead
- **Kontext** ohne Überlastung
- **Progress** ohne Chaos
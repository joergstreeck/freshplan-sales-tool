# 🚦 FreshPlan Prozess-Checkpoints

**Sicherheit durch standardisierte Abläufe**

## 📍 Kritische Checkpoints (IMMER Briefing erforderlich)

### 1. **VOR Git Push** 🔴
```bash
# STOP! Checkpoint-Briefing:
./scripts/pre-push-check.sh

# Prüft automatisch:
- [ ] Repository sauber? (quick-cleanup.sh)
- [ ] Tests grün?
- [ ] Linter zufrieden?
- [ ] Security-Check passed?
- [ ] Dokumentation aktuell?
```

### 2. **Feature-Implementierung** 🟡
```bash
# VOR dem Coden:
./scripts/feature-briefing.sh <feature-name>

# Erstellt automatisch:
- Feature-Checkliste
- Test-Anforderungen
- Security-Überlegungen
- Dokumentations-Bedarf
```

### 3. **Meilenstein erreicht** 🟢
```bash
# Nach Fertigstellung:
./scripts/milestone-review.sh

# Führt durch:
- Code-Review Checkliste
- Test-Coverage Report
- Performance-Check
- Dokumentations-Update
```

## 📋 Standard-Briefings für wichtige Tätigkeiten

### A. **Security-First Briefing** (Bei JEDEM neuen Feature)

```markdown
## Security Checkpoint
- [ ] Keine hardcoded Secrets?
- [ ] Input-Validierung implementiert?
- [ ] SQL-Injection verhindert?
- [ ] XSS-Protection aktiv?
- [ ] CORS korrekt konfiguriert?
- [ ] Authentifizierung geprüft?
- [ ] Autorisierung implementiert?
- [ ] Logging ohne sensitive Daten?
```

### B. **Test-First Briefing** (BEVOR Code geschrieben wird)

```markdown
## Test Planning
- [ ] Unit-Tests geplant? (min. 80%)
- [ ] Integration-Tests definiert?
- [ ] Edge-Cases identifiziert?
- [ ] Error-Cases bedacht?
- [ ] Performance-Tests nötig?
- [ ] Security-Tests eingeplant?
```

### C. **Git-Workflow Briefing** (Bei JEDEM Commit)

```markdown
## Git Checkpoint
- [ ] Branch aktuell mit main?
- [ ] Commit-Message aussagekräftig?
- [ ] Alle Tests grün?
- [ ] Code-Review erhalten?
- [ ] CI/CD wird grün sein?
```

## 🔄 Automatisierte Prozess-Sicherheit

### 1. **Pre-Commit Hooks**
```bash
# .git/hooks/pre-commit
#!/bin/bash
echo "🔍 Pre-Commit Security Check..."

# 1. Keine Secrets
if grep -r "password\|secret\|key" --include="*.js" --include="*.java" .; then
  echo "❌ Mögliche Secrets gefunden!"
  exit 1
fi

# 2. Tests laufen
npm test || exit 1

# 3. Linter check
npm run lint || exit 1

echo "✅ Pre-Commit Check passed!"
```

### 2. **Feature-Template** (Automatisch generiert)
```bash
# scripts/new-feature.sh <name>
#!/bin/bash
FEATURE=$1

mkdir -p docs/features/$FEATURE
cat > docs/features/$FEATURE/briefing.md << EOF
# Feature: $FEATURE
**Erstellt:** $(date +%Y-%m-%d)
**Status:** Planning

## Checkliste

### Planning
- [ ] Requirements klar?
- [ ] API-Design dokumentiert?
- [ ] Test-Strategie definiert?
- [ ] Security-Aspekte bedacht?

### Implementation  
- [ ] TDD: Tests zuerst
- [ ] Code-Review geplant
- [ ] Dokumentation vorbereitet

### Definition of Done
- [ ] Alle Tests grün
- [ ] Code-Review bestanden
- [ ] Dokumentation aktuell
- [ ] Security-Check passed
- [ ] Performance akzeptabel
EOF

echo "✅ Feature-Briefing erstellt: docs/features/$FEATURE/briefing.md"
```

### 3. **Tägliche Checkpoints**

```bash
# scripts/daily-checkpoint.sh
#!/bin/bash
echo "📊 Täglicher Checkpoint $(date +%Y-%m-%d)"
echo "================================"

# 1. Git Status
echo "📍 Git Status:"
git status --short

# 2. Test Status  
echo "🧪 Test Status:"
cd frontend && npm test -- --run 2>/dev/null | grep -E "(PASS|FAIL|Test Suites)"
cd ../backend && ./mvnw test -q | grep -E "(Tests run|FAILURES)"

# 3. Security Check
echo "🔒 Security Status:"
npm audit 2>/dev/null | grep -E "(found|vulnerabilities)"

# 4. Dokumentation
echo "📚 Dokumentations-Status:"
find docs/claude-daily -name "*.md" -mtime +7 | wc -l | xargs echo "Alte Docs (>7 Tage):"

# 5. Empfehlungen
echo ""
echo "💡 Empfehlungen:"
[ -n "$(git status --porcelain)" ] && echo "- Uncommitted changes vorhanden!"
echo "- Denke an: Write Less, Update More"
echo "- Nutze bestehende Docs statt neue zu erstellen"
```

## 🎯 Die 5 Goldenen Prozess-Regeln

### 1. **"Stop & Think" Regel**
Bei JEDEM kritischen Punkt: 5 Sekunden innehalten und Checkliste prüfen

### 2. **"Four Eyes" Prinzip**  
Keine kritische Änderung ohne Review (Mensch oder AI)

### 3. **"Test First" Dogma**
KEIN Code ohne vorherige Test-Definition

### 4. **"Document Once" Policy**
Einmal richtig dokumentieren statt 10x korrigieren

### 5. **"Fail Fast" Mentalität**
Lieber früh einen Fehler finden als spät eine Katastrophe

## 🚨 Kritische Momente für Briefings

### IMMER Briefing bei:
1. **Architektur-Änderungen**
2. **Security-relevanten Features**
3. **API-Änderungen**
4. **Datenbank-Migrationen**
5. **Externe Integrationen**
6. **Performance-kritischen Stellen**
7. **Vor jedem Release**

### Briefing-Arten:
- **Mini** (< 2 Min): Checkliste durchgehen
- **Standard** (5-10 Min): Template ausfüllen
- **Deep** (30+ Min): Team-Review

---
*Sicherheit entsteht durch Wiederholung der richtigen Prozesse!*
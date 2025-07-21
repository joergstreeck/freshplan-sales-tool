# 🎯 Reality Check Etablierung - Alternative Ansätze

## Problem
CLAUDE.md soll kompakt bleiben, aber Reality Check muss trotzdem zur Routine werden.

## 🚀 Lösungsvorschläge

### 1. **In NEXT_STEP.md als Standard-Erstes**
```markdown
## 🎯 JETZT GERADE:

**⚠️ VOR JEDER IMPLEMENTATION:**
```bash
./scripts/reality-check.sh FC-XXX  # PFLICHT! Plan vs. Code abgleichen
```

**Aktuelle Aufgabe:** FC-008 Security Foundation
```

**Vorteil:** Immer sichtbar beim Start
**Nachteil:** Könnte übersehen werden

### 2. **Automatischer Prompt in session-start.sh**
```bash
# Am Ende von session-start.sh hinzufügen:
echo -e "${YELLOW}🔍 Reality Check Erinnerung:${NC}"
echo "Vor jeder Feature-Implementation MUSS durchgeführt werden:"
echo "  ./scripts/reality-check.sh FC-XXX"
echo ""
echo "Letzte Reality Checks:"
tail -5 .reality-check-log 2>/dev/null || echo "Noch keine Checks durchgeführt"
```

**Vorteil:** Bei jedem Session-Start sichtbar
**Nachteil:** Nur einmal pro Session

### 3. **Git Branch Protection mit Hook**
```bash
#!/bin/bash
# .git/hooks/post-checkout
BRANCH=$(git symbolic-ref --short HEAD 2>/dev/null)
if [[ $BRANCH =~ (FC-[0-9]+|M[0-9]+) ]]; then
    echo ""
    echo "🔍 REALITY CHECK REMINDER"
    echo "========================"
    echo "Du arbeitest an: $BRANCH"
    echo "Hast du Reality Check durchgeführt?"
    echo ""
    echo "Falls nicht: ./scripts/reality-check.sh ${BASH_REMATCH[1]}"
    echo ""
fi
```

**Vorteil:** Automatisch bei Branch-Wechsel
**Nachteil:** Nur lokal, muss installiert werden

### 4. **In Übergabe-Template als Pflichtfeld**
```markdown
## 🔍 REALITY CHECK STATUS
- [ ] FC-008: Reality Check durchgeführt? ⚠️ PFLICHT vor Implementation!
- [ ] Alle Code-Dateien gelesen?
- [ ] Plan/Code-Diskrepanzen geklärt?
```

**Vorteil:** Erzwingt Dokumentation
**Nachteil:** Nachträglich, nicht präventiv

### 5. **VS Code Tasks Integration**
`.vscode/tasks.json`:
```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Reality Check",
      "type": "shell",
      "command": "./scripts/reality-check.sh ${input:featureCode}",
      "problemMatcher": [],
      "presentation": {
        "reveal": "always",
        "panel": "new"
      }
    }
  ],
  "inputs": [
    {
      "id": "featureCode",
      "type": "promptString",
      "description": "Feature Code (z.B. FC-008)"
    }
  ]
}
```

**Vorteil:** In IDE integriert
**Nachteil:** Nur für VS Code Nutzer

### 6. **Makefile mit Reality Check als Dependency**
```makefile
# Makefile
.PHONY: implement-feature
implement-feature: reality-check
	@echo "✅ Reality Check passed, starting implementation..."

.PHONY: reality-check
reality-check:
	@echo "Running Reality Check..."
	@./scripts/reality-check.sh $(FEATURE)

# Usage: make implement-feature FEATURE=FC-008
```

**Vorteil:** Erzwingt Check vor Implementation
**Nachteil:** Neues Tool (make) erforderlich

### 7. **CI Pipeline mit PR-Block**
```yaml
name: PR Reality Check
on:
  pull_request:
    types: [opened, synchronize]

jobs:
  check:
    runs-on: ubuntu-latest
    steps:
      - name: Check Reality Check Log
        run: |
          FEATURE=$(echo "${{ github.head_ref }}" | grep -oE "FC-[0-9]+|M[0-9]+")
          if [ -n "$FEATURE" ]; then
            if ! grep -q "✅.*$FEATURE.*passed" .reality-check-log; then
              echo "::error::Reality Check für $FEATURE nicht durchgeführt!"
              exit 1
            fi
          fi
```

**Vorteil:** Zentral erzwungen
**Nachteil:** Erst bei PR, nicht während Entwicklung

### 8. **Bash Alias für häufige Befehle**
In `.bashrc` oder `.zshrc`:
```bash
alias fc='echo "🔍 Reality Check first!" && read -p "Feature (FC-XXX): " f && ./scripts/reality-check.sh $f'
alias implement='fc && echo "✅ Now implement!"'
```

**Vorteil:** Kurze Befehle
**Nachteil:** Muss lokal eingerichtet werden

## 🎯 Empfohlene Kombination

### Minimaler Ansatz (sofort umsetzbar):
1. **NEXT_STEP.md** als primäre Erinnerung ✅
2. **session-start.sh** zeigt letzte Checks ✅
3. **Übergabe-Template** mit Pflichtfeld ✅

### Mittelfristiger Ansatz:
1. Obiges PLUS
2. **Git Hooks** lokal installieren
3. **VS Code Tasks** einrichten
4. **CI Pipeline** aktivieren

### Langfristiger Ansatz:
1. Alles obige PLUS
2. **Web Dashboard** mit Team-Übersicht
3. **Metriken** in Monday.com
4. **Automatische Synchronisation** Plan ↔ Code

## 📝 Nächste Schritte

1. **Sofort:** NEXT_STEP.md anpassen
2. **Heute:** session-start.sh erweitern
3. **Diese Woche:** Git Hooks dokumentieren
4. **Nächste Woche:** Team-Feedback einholen

Der Reality Check wird durch mehrere kleine Touchpoints zur Gewohnheit, statt durch eine große Regel!
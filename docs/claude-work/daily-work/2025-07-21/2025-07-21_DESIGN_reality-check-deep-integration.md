# 🚀 Reality Check - Deep System Integration Design

## Vision
Der Reality Check wird zum zentralen Qualitätssicherungs-Tool, das Plan und Code-Realität kontinuierlich synchron hält.

## 🎯 Der 3-Stufen Reality Check Prozess

### 1️⃣ Plan lesen (wie bisher)
- Entwickler liest CLAUDE_TECH/TECH_CONCEPT Dokument
- Versteht die geplante Architektur und Struktur

### 2️⃣ Code lesen (NEU - VERPFLICHTEND!)
- Entwickler MUSS die betroffenen Code-Dateien lesen
- Script zeigt welche Dateien relevant sind
- Entwickler bestätigt explizit das Lesen

### 3️⃣ Abgleich und Bestätigung
- Interaktive Checkliste im Script
- Bei Diskrepanzen: STOPP der Implementation
- Erst Plan/Code synchronisieren, dann fortfahren

## 🔧 Technische Umsetzung

### A) Erweitertes Reality Check Script ✅
```bash
./scripts/reality-check.sh FC-XXX
```
- Interaktive Führung durch alle 3 Schritte
- Erzwingt Bestätigung nach jedem Schritt
- Detaillierte Checkliste für Abgleich
- Logging für Übergaben

### B) Git Pre-Commit Hook
```bash
#!/bin/bash
# .git/hooks/pre-commit
FEATURE=$(git branch --show-current | grep -oE "FC-[0-9]+|M[0-9]+")
if [ -n "$FEATURE" ]; then
    if ! grep -q "✅ $FEATURE: Reality Check passed" .reality-check-log; then
        echo "❌ Reality Check für $FEATURE nicht durchgeführt!"
        echo "Bitte erst: ./scripts/reality-check.sh $FEATURE"
        exit 1
    fi
fi
```

### C) VS Code Integration (Konzept)
```json
{
  "freshplan.realityCheck": {
    "enabled": true,
    "autoPrompt": true,
    "showInStatusBar": true
  }
}
```
- Status Bar: `[Reality Check: FC-008 ✅]`
- Command Palette: "Run Reality Check"
- Problem Panel: Zeigt Diskrepanzen
- CodeLens: Inline-Hinweise bei Abweichungen

### D) CI/CD Pipeline Integration
```yaml
reality-check:
  runs-on: ubuntu-latest
  steps:
    - name: Extract Feature Code
      run: |
        FEATURE=$(echo ${{ github.head_ref }} | grep -oE "FC-[0-9]+|M[0-9]+")
        echo "FEATURE=$FEATURE" >> $GITHUB_ENV
    
    - name: Run Reality Check
      run: |
        ./scripts/reality-check.sh $FEATURE --ci-mode
    
    - name: Comment on PR
      if: failure()
      uses: actions/github-script@v6
      with:
        script: |
          github.rest.issues.createComment({
            issue_number: context.issue.number,
            body: '❌ Reality Check failed! Plan und Code sind nicht synchron.'
          })
```

### E) Web Dashboard
```javascript
// backend/src/main/java/de/freshplan/api/resources/RealityCheckResource.java
@Path("/api/reality-check")
public class RealityCheckResource {
    @GET
    @Path("/status")
    public Response getAllFeatureStatus() {
        // Zeigt Reality Check Status aller Features
    }
    
    @POST
    @Path("/run/{feature}")
    public Response runCheck(@PathParam("feature") String feature) {
        // Führt Reality Check aus und gibt Bericht zurück
    }
}
```

## 📊 Dashboard Konzept

### Terminal Dashboard (bereits implementiert)
```bash
./scripts/reality-check-dashboard.sh
```
Zeigt:
- Status aller Features
- Letzte Check-Zeitpunkte
- Anzahl fehlender Dateien
- Trend (besser/schlechter)

### Web Dashboard (Zukunft)
```
┌─────────────────────────────────────────┐
│ Reality Check Dashboard                 │
├─────────────────────────────────────────┤
│ Feature │ Status │ Last Check │ Action │
├─────────┼────────┼────────────┼────────┤
│ FC-008  │   ✅   │ 2h ago     │ [Run]  │
│ FC-011  │   ❌   │ 1d ago     │ [Fix]  │
│ M4      │   ⏸️   │ never      │ [Run]  │
└─────────────────────────────────────────┘
```

## 🔄 Automatisierungen

### 1. Auto-Fix für häufige Probleme
```bash
./scripts/reality-check.sh FC-XXX --auto-fix
```
- Erstellt fehlende Verzeichnisse
- Generiert Basis-Dateien aus Templates
- Updated Pfade bei Umbenennungen

### 2. Bidirektionale Synchronisation
- **Code → Plan**: Bei Datei-Umbenennungen Plan updaten
- **Plan → Code**: Bei Plan-Änderungen PR erstellen

### 3. Metriken & Reports
- Reality Check Erfolgsrate pro Team
- Durchschnittliche Zeit bis zur Synchronisation
- Häufigste Diskrepanz-Typen

## 🎓 Team-Adoption

### Schulung
1. Reality Check als Teil des Onboardings
2. Best Practice Videos
3. Pair Programming mit Reality Check

### Gamification
- 🏆 "Reality Master" Badge: 10 erfolgreiche Checks in Folge
- 📊 Team-Scoreboard: Wer hat die beste Plan/Code-Synchronität?
- 🎯 Sprint-Ziel: 100% Reality Check Compliance

## 📈 Erwartete Vorteile

### Kurzfristig (1-2 Wochen)
- 50% weniger "Warum funktioniert das nicht?" Momente
- 80% weniger Zeit mit veralteten Plänen verschwendet
- Bessere Übergaben zwischen Sessions

### Mittelfristig (1-3 Monate)
- Pläne bleiben automatisch aktuell
- Neue Entwickler verstehen Codebase schneller
- Weniger Merge-Konflikte durch klare Strukturen

### Langfristig (6+ Monate)
- Living Documentation die immer stimmt
- Architektur-Drift wird verhindert
- Onboarding-Zeit halbiert

## 🚀 Rollout-Plan

### Phase 1: Script Enhancement ✅
- Erweitertes interaktives Script
- Integration in CLAUDE.md
- Dokumentation

### Phase 2: Git Integration (Woche 1)
- Pre-commit Hook
- Branch Protection Rules
- CI/CD Checks

### Phase 3: IDE Integration (Woche 2-3)
- VS Code Extension Prototype
- IntelliJ Plugin Konzept

### Phase 4: Web Dashboard (Monat 2)
- REST API
- React Dashboard
- Team-Metriken

## 🔑 Erfolgsfaktoren

1. **Einfachheit**: Muss in < 5 Minuten durchführbar sein
2. **Nutzen**: Sofort spürbarer Mehrwert
3. **Integration**: Nahtlos in bestehende Workflows
4. **Feedback**: Klare, hilfreiche Meldungen

## 💡 Nächste Schritte

1. **Sofort**: Git Hook testen
2. **Diese Woche**: CI/CD Integration
3. **Nächste Woche**: VS Code Extension Prototype
4. **In 2 Wochen**: Team-Review und Feedback

Der Reality Check wird vom "Nice to have" zum unverzichtbaren Qualitäts-Tool!
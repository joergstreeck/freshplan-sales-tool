# Codecov Setup Anleitung

## 📊 Was ist Codecov?

Codecov ist ein Service, der Code-Coverage-Daten analysiert und visualisiert. Es zeigt:
- Coverage-Trends über Zeit
- Automatische PR-Kommentare mit Coverage-Diff
- Coverage-Badge für README
- Welche Zeilen/Branches nicht getestet sind

## 🚀 Setup-Schritte (5 Minuten)

### 1. Codecov Account erstellen

1. Gehe zu https://codecov.io
2. Klicke auf **"Sign up with GitHub"**
3. Autorisiere Codecov für dein GitHub-Konto

### 2. Repository aktivieren

1. Nach Login: Klicke auf **"+ Add new repository"**
2. Suche nach `freshplan-sales-tool`
3. Klicke auf **"Setup repo"**
4. Codecov generiert einen **Upload Token**

### 3. GitHub Secret hinzufügen

1. Gehe zu: https://github.com/joergstreeck/freshplan-sales-tool/settings/secrets/actions
2. Klicke auf **"New repository secret"**
3. Name: `CODECOV_TOKEN`
4. Value: [Der Token von Codecov]
5. Klicke **"Add secret"**

### 4. Workflow aktivieren

Der Workflow `.github/workflows/frontend-tests-coverage.yml` ist bereits erstellt!

Er wird automatisch ausgeführt bei:
- Push zu `main`, `develop`, `feature/*` branches
- Pull Requests die `frontend/` Dateien ändern

### 5. README Badge hinzufügen

Nach dem ersten erfolgreichen Upload kannst du den Badge hinzufügen:

```markdown
[![codecov](https://codecov.io/gh/joergstreeck/freshplan-sales-tool/branch/main/graph/badge.svg?token=DEIN_TOKEN)](https://codecov.io/gh/joergstreeck/freshplan-sales-tool)
```

Den Badge-Link findest du in Codecov unter: **Settings → Badge**

## 🎯 Features nach Setup

### Automatische PR-Kommentare

Bei jedem PR siehst du:
```
Coverage Δ: +2.5%
Files changed: 5
Lines added: +150
Coverage: 87.3% → 89.8%
```

### Coverage-Trends

Dashboard zeigt:
- Sunburst-Diagramm (welche Module am wenigsten getestet)
- Zeitverlauf (Coverage steigt/fällt?)
- Commit-by-commit Coverage-Änderungen

### Branch-Protection

Optional in GitHub Settings:
- Require: "codecov/project" Check muss grün sein
- Blockiert Merge wenn Coverage sinkt

## 📱 Lokale Entwicklung

**Neue NPM Scripts:**

```bash
# Quick Coverage-Check (generiert HTML-Report)
npm run test:coverage

# Interaktive UI mit Live-Coverage
npm run test:ui

# Watch-Mode mit Coverage
npm run test:watch
```

**HTML-Report öffnen:**
```bash
cd frontend
npm run test:coverage
open coverage/index.html  # macOS
# oder: xdg-open coverage/index.html  # Linux
```

## ⚙️ Konfiguration

Die Coverage-Config ist in `frontend/vite.config.ts`:

```typescript
coverage: {
  provider: 'v8',
  reporter: ['text', 'json', 'html'],
  exclude: [
    'node_modules/',
    'src/test/',
    '**/*.d.ts',
    '**/*.config.*',
    // ... weitere excludes
  ],
}
```

## 🔍 Troubleshooting

**Workflow schlägt fehl: "Missing CODECOV_TOKEN"**
→ GitHub Secret `CODECOV_TOKEN` fehlt (siehe Schritt 3)

**Coverage wird nicht hochgeladen**
→ Prüfe ob `coverage/coverage-final.json` existiert nach Tests

**Badge zeigt "unknown"**
→ Erster Upload muss erfolgreich sein, dann Badge URL aktualisieren

## 📚 Weitere Infos

- **Codecov Docs:** https://docs.codecov.com
- **GitHub Integration:** https://docs.codecov.com/docs/github-integration
- **Badge Setup:** https://docs.codecov.com/docs/status-badges

---

**Setup erledigt?** ✅
Nächster Commit wird automatisch Coverage nach Codecov uploaden!

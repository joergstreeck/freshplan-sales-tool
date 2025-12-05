# AI Code Review Workflows

## 📁 Diese Datei

- **`ai-code-review.yml.disabled`** - Workflow für automatische AI Code Reviews (aktuell deaktiviert)

## ❓ Warum bekomme ich keine Gemini Reviews?

**Antwort:** Es gibt keine automatische Gemini-Review-Integration im Repository konfiguriert.

### Was in der Vergangenheit passiert ist

Die erwähnten "Gemini Reviews" in älteren PRs (#133, #135, #139) waren **manuelle Reviews**, die vom Entwickler mit Gemini Code Assist oder GitHub Copilot durchgeführt wurden - nicht automatisiert.

## ✅ Lösungen

### Option 1: GitHub Copilot (EMPFOHLEN - 5 Minuten Setup)

**Einfachste und beste Integration:**

1. Gehe zu Repository **Settings**
2. Navigiere zu **Code & automation → Copilot**
3. Aktiviere **"Pull request summaries"**
4. Aktiviere **"Code review"** (falls verfügbar)

**Fertig!** GitHub Copilot wird jetzt automatisch:
- PR-Zusammenfassungen generieren
- Code-Review-Kommentare hinterlassen
- Verbesserungsvorschläge machen

**Kosten:** Teil der GitHub Copilot Business/Enterprise Lizenz

### Option 2: CodeRabbit (15 Minuten Setup)

**Feature-reichste Option:**

1. Installiere die App: https://github.com/apps/coderabbitai
2. Autorisiere für dieses Repository
3. Optional: Erstelle `.coderabbit.yaml` für Custom-Konfiguration

**Features:**
- Automatische Line-by-Line Reviews
- Konversation mit dem Bot
- Multi-Modell Support (GPT-4, Claude, Gemini)

**Kosten:** $15-49/User/Monat

### Option 3: Custom Gemini Workflow (30 Minuten Setup)

**Für vollständige Kontrolle:**

1. Erstelle Google Cloud Service Account
2. Aktiviere Gemini Code Assist API
3. Füge API Key als GitHub Secret hinzu: `GEMINI_API_KEY`
4. Aktiviere Workflow: Benenne `ai-code-review.yml.disabled` um zu `ai-code-review.yml`

**Features:**
- Custom-Implementierung
- Volle Kontrolle über Review-Logik
- Pay-per-use Preismodell

**Kosten:** ~$0.002-0.004 pro 1k Tokens

## 🚀 Schnellstart

Für **sofortige AI-Reviews**:

```bash
# Repository Settings öffnen
# → Code & automation
# → Copilot
# → Enable "Pull request summaries" & "Code review"
```

## 📚 Detaillierte Dokumentation

Siehe: [`/docs/planung/AI_CODE_REVIEW_SETUP.md`](../../docs/planung/AI_CODE_REVIEW_SETUP.md)

## 🔄 Aktueller Status

- ❌ Keine automatischen AI-Reviews aktiv
- ✅ Workflows und Dokumentation vorhanden
- 🎯 Bereit für Aktivierung

## ❓ Häufige Fragen

### "Warum sehe ich keine Gemini-Kommentare auf meiner PR?"

Weil keine automatische Integration konfiguriert ist. Aktiviere eine der oben genannten Optionen.

### "Brauche ich eine API-Key für GitHub Copilot?"

Nein! GitHub Copilot ist vollständig in GitHub integriert und benötigt nur die Aktivierung in den Settings.

### "Welche Option ist am besten?"

**Für die meisten Teams: GitHub Copilot** - native Integration, keine zusätzliche Konfiguration, Teil der Enterprise-Lizenz.

### "Kann ich mehrere Optionen gleichzeitig nutzen?"

Ja! Du kannst GitHub Copilot UND CodeRabbit UND den Custom Gemini Workflow parallel nutzen.

---

**Nächster Schritt:** Wähle eine Option und aktiviere sie (Empfehlung: GitHub Copilot in Settings).

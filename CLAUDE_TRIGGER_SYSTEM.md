# 🎯 Claude Trigger System

## Das Problem
Claude liest lange Dokumente nicht vollständig und vergisst Regeln.

## Die Lösung: Context-Injection Points

### 🔴 Trigger-Momente für User

#### 1. **Bei Session-Start**
```
User: "Lies TRIGGER_START.md"
```
Enthält: Datum, Projekt-Status, aktuelle Aufgabe

#### 2. **Vor Code-Änderungen**
```
User: "Lies TRIGGER_CODE.md"
```
Enthält: Code-Standards, Keine neuen Files, Test-First

#### 3. **Vor Git-Operationen**
```
User: "Lies TRIGGER_GIT.md"
```
Enthält: Cleanup-Script, Commit-Format, Security-Check

#### 4. **Bei Regel-Verstößen**
```
User: "STOP! Lies TRIGGER_RULES.md"
```
Enthält: Die 5 wichtigsten Regeln kompakt

#### 5. **Bei Dokumentation**
```
User: "Lies TRIGGER_DOCS.md"
```
Enthält: Write Less Update More, Keine neuen Docs

### 🟡 Automatische Trigger (wenn möglich)

Claude.code kann KEINE Scripts automatisch ausführen, ABER:

1. **Pre-Prompts**: Nutze Markdown-Kommentare als Trigger
2. **Inline-Reminders**: In Code-Dateien einbetten
3. **Conditional Responses**: "Wenn X dann erinnere mich an Y"

### 🟢 User-Checkliste

| Situation | Trigger-Kommando | Zweck |
|-----------|------------------|-------|
| Neuer Chat | "Lies TRIGGER_START.md" | Kontext etablieren |
| Nach 10 Nachrichten | "Lies TRIGGER_REFRESH.md" | Kontext erneuern |
| Vor großer Änderung | "Lies TRIGGER_CODE.md" | Standards sichern |
| Claude macht Fehler | "Lies TRIGGER_RULES.md" | Korrektur |
| Neue Aufgabe | "Lies TRIGGER_TASK.md" | Fokus setzen |

### 📝 Trigger-Dokumente erstellen

Jedes Trigger-Doc sollte:
- **MAX 20 Zeilen** lang sein
- **Die 3-5 wichtigsten Punkte** enthalten
- **Konkrete Anweisungen** geben
- **Auf Details verweisen** (aber nicht einbinden)

### 💡 Beispiel: TRIGGER_CODE.md

```markdown
# 🔴 CODE TRIGGER - STOP UND LIES!

## Die 5 ABSOLUTEN Regeln:
1. **KEINE neuen Dateien** - NUR bestehende editieren!
2. **Test FIRST** - Schreibe den Test VOR dem Code!
3. **Max 80 Zeichen** pro Zeile - Nutze Umbrüche!
4. **Update Docs** - In BESTEHENDE Docs!
5. **Run Tests** - BEVOR du fertig bist!

## Vergiss nicht:
- Backend = Flache Struktur
- 3 Rollen: admin, manager, sales
- Cleanup vor Commit

JETZT BESTÄTIGE DIESE REGELN!
```

### 🎯 Erfolgs-Metriken

Claude befolgt Regeln besser wenn:
1. **Kurze, klare Trigger** (< 20 Zeilen)
2. **Häufige Erinnerungen** (alle 5-10 Nachrichten)
3. **Explizite Bestätigung** ("Bestätige die Regeln")
4. **Kontext-Fragen** ("Welches Datum haben wir?")

---
*Merke: Claude ist ein Werkzeug, kein Mitarbeiter. Du musst ihn führen!*
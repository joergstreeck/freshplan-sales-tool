# 🎮 Claude Control Guide für User

## 🎯 Die Wahrheit über Claude

### Was Claude KANN:
✅ Code schreiben und analysieren
✅ Bugs finden und fixen  
✅ Dokumentation erstellen
✅ Komplexe Probleme lösen
✅ Mehrere Dateien gleichzeitig bearbeiten

### Was Claude NICHT kann:
❌ Sich an alles erinnern
❌ Lange Dokumente vollständig lesen
❌ Automatisch Scripts ausführen
❌ Konsistent Regeln befolgen ohne Erinnerung
❌ Wie ein menschlicher Mitarbeiter agieren

## 🎪 Claude richtig führen

### 1. **Session-Management**

```markdown
# Neuer Chat
User: "Heutiges Datum ist 09.06.2025. Lies TRIGGER_START.md"
Claude: [Bestätigt Datum und Regeln]

# Nach 10 Nachrichten  
User: "Kontext-Check: Was ist das Datum? Was sind die 5 Hauptregeln?"
Claude: [Sollte korrekt antworten]

# Bei falscher Antwort
User: "STOP! Lies TRIGGER_RULES.md"
```

### 2. **Regel-Durchsetzung**

| Claude macht... | Du sagst... |
|----------------|-------------|
| Englische Antwort | "DEUTSCH! Lies TRIGGER_RULES.md" |
| Neue Datei | "STOP! Keine neuen Dateien! Lösche sie!" |
| Zu viel Doku | "Write Less, Update More! Lies TRIGGER_DOCS.md" |
| Vergisst Tests | "Test First! Schreibe erst den Test!" |
| Falsches Datum | "Datum ist 09.06.2025, nicht Januar!" |

### 3. **Projekt-Kontrolle behalten**

#### Tägliche Routine:
1. **Morgens**: Master-Update laufen lassen
2. **Neuer Chat**: TRIGGER_START.md
3. **Alle 10 Msgs**: Kontext-Refresh
4. **Bei Fehlern**: Sofort korrigieren
5. **Abends**: Cleanup-Scripts

#### Kritische Momente:
- **Vor Features**: Feature-Briefing generieren
- **Vor Push**: Pre-Push-Check
- **Nach Sprint**: Two-Pass Review

### 4. **Effektive Prompts**

#### ❌ Schlecht:
"Implementiere User-Export Feature"

#### ✅ Gut:
"Lies TRIGGER_CODE.md. Dann implementiere User-Export:
1. Erst Tests in UserService.test.ts
2. Dann Code in UserService.ts  
3. Max 80 Zeichen/Zeile
4. Keine neuen Dateien"

### 5. **Notfall-Kommandos**

```markdown
# Claude macht Chaos
"STOP! ALLE ARBEIT ANHALTEN! Lies TRIGGER_RULES.md"

# Claude vergisst Kontext  
"Kontext verloren? Lies MASTER_BRIEFING.md Abschnitt 'Aktuelle Architektur'"

# Claude wird englisch
"DEUTSCH! Immer Deutsch! Auch kurze Antworten!"

# Claude erstellt neue Files
"KEINE neuen Dateien! Zeige mir welche du erstellt hast und lösche sie!"
```

## 📊 Erfolgs-Metriken

### Gute Session:
- Claude bestätigt Regeln
- Hält sich an Struktur
- Fragt bei Unsicherheit
- Schreibt Tests zuerst
- Bleibt bei Deutsch

### Schlechte Session:
- Ignoriert Trigger
- Erstellt wilde Dateien
- Vergisst Tests
- Wird englisch
- Macht Annahmen

## 🎯 Realistische Erwartungen

### Claude als Werkzeug:
- **Junior Dev**: Braucht klare Anweisungen
- **Pair Programmer**: Du führst, er coded
- **Assistent**: Nicht selbstständig

### Claude ist KEIN:
- Senior Developer
- Projektmanager
- Selbstständiger Mitarbeiter
- Perfektes Gedächtnis

## 💡 Pro-Tipps

1. **Kurze Sessions** (max 20-30 Nachrichten)
2. **Klare Einzelaufgaben** statt Mammut-Projekte
3. **Häufige Checkpoints** und Korrekturen
4. **Explizite Anweisungen** statt Annahmen
5. **Geduld und Führung** statt Frustration

## 🚀 Beispiel-Workflow

```markdown
1. User: "Datum ist 09.06.2025. Lies TRIGGER_START.md"
2. Claude: [Bestätigt]
3. User: "Implementiere getUserById Endpoint. Lies TRIGGER_CODE.md"
4. Claude: [Bestätigt Regeln]
5. User: "Zeige mir zuerst den Test"
6. Claude: [Zeigt Test]
7. User: "Gut, jetzt die Implementation"
8. Claude: [Implementiert]
9. User: "Update die API_CONTRACT.md"
10. Claude: [Updated bestehende Datei]
```

---

## 🎮 Zusammenfassung

**Claude ist ein mächtiges Werkzeug, aber:**
- Er braucht ständige Führung
- Er vergisst schnell
- Er macht Fehler

**Du bleibst der Pilot:**
- Nutze Trigger-Dokumente
- Korrigiere sofort
- Halte Sessions kurz
- Sei explizit

**Mit der richtigen Führung** kann Claude ein ganzes Projekt bearbeiten - aber nur mit dir als aktivem Steuermann!

*Frage: Soll ich dir noch spezifischere Trigger-Docs erstellen?*
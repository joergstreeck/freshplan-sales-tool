# 🎯 FreshPlan Quick Reference

## 🚨 Die wichtigsten Befehle

### Vor JEDEM Push
```bash
./scripts/pre-push-check.sh
```

### Neues Feature starten
```bash
./scripts/feature-briefing.sh <feature-name>
```

### Dokumentation aufräumen
```bash
./scripts/cleanup-old-docs.sh
```

### Master Update (täglich)
```bash
./scripts/master-update.sh
```

## 📋 Checkpoints - Wann was tun?

| Situation | Befehl/Aktion |
|-----------|---------------|
| Neuer Tag | `./scripts/master-update.sh` |
| Vor Commit | Tests laufen lassen |
| Vor Push | `./scripts/pre-push-check.sh` |
| Neues Feature | `./scripts/feature-briefing.sh` |
| Code fertig | Two-Pass Review |
| Woche vorbei | `./scripts/cleanup-old-docs.sh` |

## 🔒 Security-Basics

### NIEMALS committen:
- Passwörter
- API-Keys  
- Private Keys
- Connection Strings mit Credentials

### IMMER prüfen:
- Input Validation
- SQL Injection
- XSS Prevention
- Auth/Authorization

## 📚 Dokumentations-Regeln

### Write Less, Update More!
1. Existiert ein Doc? → UPDATE
2. Ist es temporär? → `claude-daily/`
3. Ist es permanent? → Master-Docs

### KEINE neuen Docs für:
- Bugfixes → Git Commit
- Kleine Updates → CHANGELOG
- Code-Erklärungen → Code-Kommentare

## 🎨 Code-Standards

### Naming
- Klassen: `PascalCase`
- Methoden: `camelCase`
- Konstanten: `UPPER_SNAKE_CASE`
- Dateien: Wie Klasse

### Struktur
- Backend: Flache Struktur unter `/backend`
- Frontend: Feature-basiert unter `/frontend`
- Max 80-100 Zeichen pro Zeile

## ⚡ Quick Commands

```bash
# Frontend
cd frontend && npm run dev    # Starten
cd frontend && npm test        # Tests

# Backend  
cd backend && ./mvnw quarkus:dev  # Starten
cd backend && ./mvnw test         # Tests

# Git
git status                     # Status
git add -A                     # Alle Änderungen
git commit -m "type: message"  # Commit
./scripts/pre-push-check.sh    # Vor Push!
git push                       # Push

# Wartung
./scripts/quick-cleanup.sh     # Repository säubern
node update-docs-simple.js     # Datum aktualisieren
```

## 🚦 Prozess-Flow

```
Idee → Feature-Briefing → Tests schreiben → Code → Review → Push
```

## 💡 Goldene Regeln

1. **Security First** - Keine Kompromisse
2. **Test First** - Kein Code ohne Tests  
3. **Document Once** - Richtig statt oft
4. **Review Always** - Four Eyes Prinzip
5. **Clean Always** - Sauberer Code & Repo

---
*Bei Fragen: MASTER_BRIEFING.md lesen!*
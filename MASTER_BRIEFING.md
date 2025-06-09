# 🎯 FreshPlan Sales Tool - Master Briefing

**⚠️ WICHTIG: Dies ist DAS zentrale Dokument für Claude. IMMER ZUERST LESEN!**

## 📅 Aktuelles Datum
**Heute ist: Montag, 9. Juni 2025**
*Hinweis: Wenn das Datum fehlt, frage den User nach dem aktuellen Datum!*

## 🚀 Projekt-Schnellübersicht

### Was ist FreshPlan?
Ein **Enterprise Sales Tool** für die Lebensmittelbranche (speziell Catering/Großküchen).

### Tech-Stack
- **Frontend**: React + TypeScript + Vite
- **Backend**: Quarkus (Java) 
- **Auth**: Keycloak
- **DB**: PostgreSQL
- **Deployment**: AWS (geplant)

### Projekt-Status
- **Gestartet**: Mai 2025
- **Aktueller Sprint**: Sprint 2 (nach Backend-Reorganisation)
- **Letzte große Änderung**: 08.06.2025 - Backend komplett neu strukturiert

## 🏗️ Aktuelle Architektur

### Monorepo-Struktur
```
freshplan-sales-tool/
├── frontend/      # React SPA
├── backend/       # Quarkus API (NEUE flache Struktur!)
├── infrastructure/# Docker, K8s configs
├── docs/          # Dokumentation
├── scripts/       # Automatisierung
└── archive/       # Alte Backend-Struktur (vor 08.06.2025)
```

### ⚠️ WICHTIG: Backend wurde am 08.06.2025 reorganisiert!
- **ALT**: `/backend/src/main/java/de/freshplan/domain/user/service/dto/...` (tief verschachtelt)
- **NEU**: `/backend/src/main/java/de/freshplan/user/...` (flache Struktur)

## 👥 Rollen im System (NUR 3!)
1. **admin** - Software-Administration, User-Verwaltung
2. **manager** - Geschäftsleitung, alle Berichte, Credit Checks
3. **sales** - Verkäufer, Kunden anlegen, Kalkulationen

*Hinweis: "viewer" Rolle wurde am 08.06.2025 entfernt*

## 📍 Wo finde ich was?

### Für Claude's Arbeitsregeln
→ Lies: **CLAUDE.md**

### Für API-Informationen
→ Lies: **API_CONTRACT.md** (im Root!)

### Für Backend-Details
→ Lies: **backend/DATABASE_GUIDE.md**

### Für bekannte Probleme
→ Lies: **docs/development/KNOWN_ISSUES.md**

### Für Team-Workflows
→ Lies: **WAY_OF_WORKING.md**

## 🔧 Aktuelle Arbeitsweise

### Git-Status prüfen
```bash
git status  # Zeigt viele geänderte/gelöschte Dateien wegen Backend-Reorg
```

### Vor jedem Commit
```bash
./scripts/quick-cleanup.sh  # IMMER ausführen!
```

### Dokumentation aktualisieren
```bash
node update-docs-simple.js  # Aktualisiert AUTO_DATE Marker
```

## ⚡ Quick Commands

### Frontend starten
```bash
cd frontend && npm run dev
```

### Backend starten  
```bash
cd backend && ./mvnw quarkus:dev
```

### Tests ausführen
```bash
# Frontend
cd frontend && npm test

# Backend
cd backend && ./mvnw test
```

## 🚨 Kritische Hinweise

1. **Datum-Problem**: Claude's System zeigt oft falsches Datum. IMMER vom User bestätigen lassen!
2. **Pfad-Änderungen**: Viele Dokumente wurden verschoben. Bei 404-Fehlern: Suche mit `find` oder `grep`
3. **Sprint-Status**: Wir sind NICHT mehr in Sprint 0, sondern mindestens in Sprint 2
4. **Keine neuen Dateien**: IMMER bestehende Dateien editieren statt neue erstellen

## 📊 Projekt-Metriken (Stand: Montag, 9. Juni 2025)

- **Code Coverage**: Frontend ~45% / Backend ~78%
- **Offene Issues**: ~12 (3 kritisch)
- **Tech Debt**: ~23 Stunden
- **CI-Status**: Prüfe mit `gh run list --branch main --limit 1`

## 🔗 Weiterführende Dokumentation

Nach diesem Briefing lies in DIESER Reihenfolge:
1. **CLAUDE.md** - Deine spezifischen Arbeitsregeln
2. **API_CONTRACT.md** - Aktuelle API-Spezifikation
3. **KNOWN_ISSUES.md** - Bekannte Probleme und Workarounds

---
*Dieses Dokument ist die Single Source of Truth. Bei Widersprüchen hat DIESES Dokument Vorrang!*
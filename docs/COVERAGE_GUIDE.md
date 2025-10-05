# 📊 Coverage Guide - Einfach und Praktisch

**Aktualisiert:** 2025-10-05

## 🎯 Warum Coverage wichtig ist

Coverage zeigt dir **welche Zeilen deines Codes getestet sind**:
- ✅ Grün: Zeilen werden getestet
- 🟡 Gelb: Branches teilweise getestet
- 🔴 Rot: Zeilen/Branches nicht getestet

**Ziel:** Mindestens 80% Coverage für kritische Business-Logik

---

## 🔥 Lokale Tools (am einfachsten!)

### Frontend (Vitest)

**Option 1: Interaktive UI (Empfohlen!)**
```bash
cd frontend
npm run test:ui
```
- Browser öffnet sich automatisch
- Live-Updates beim Code-Ändern
- Coverage-Heatmaps direkt im Code
- Filtere Tests nach Name/Status
- Super intuitiv!

**Option 2: HTML-Report**
```bash
cd frontend
npm run test:coverage
open coverage/index.html  # macOS
# oder: xdg-open coverage/index.html  # Linux
```
- Zeigt Coverage-Prozentsätze pro Datei
- Klick auf Dateien → sehe exakte Zeilen
- Ideal für Quick-Checks

**Option 3: Watch-Mode**
```bash
cd frontend
npm run test:watch
```
- Tests laufen automatisch bei Änderungen
- Coverage wird live aktualisiert

### Backend (JaCoCo)

**HTML-Report generieren:**
```bash
cd backend
./mvnw verify
open target/site/jacoco/index.html
```

**Nur Coverage (ohne Tests nochmal laufen):**
```bash
./mvnw jacoco:report
open target/site/jacoco/index.html
```

**Was zeigt der Report?**
- Package/Class/Method Coverage
- Line Coverage (grün/gelb/rot)
- Branch Coverage (if/else Zweige)
- Complexity Metrics

---

## 🤖 Coverage in CI/CD

### Automatische Tests bei jedem Push/PR

**Workflows laufen automatisch:**
- `.github/workflows/frontend-tests-coverage.yml`
- `.github/workflows/backend-tests-coverage.yml`

**Was passiert?**
1. Tests laufen mit Coverage
2. HTML-Reports werden generiert
3. Reports werden als **Artifacts gespeichert (30 Tage)**

### Coverage-Reports aus GitHub Actions downloaden

1. Gehe zu: https://github.com/joergstreeck/freshplan-sales-tool/actions
2. Klicke auf einen Workflow-Run (z.B. "Frontend Tests & Coverage")
3. Scrolle runter zu **Artifacts**
4. Download:
   - `frontend-coverage-report` (HTML)
   - `backend-coverage-report` (HTML)
5. Entpacke und öffne `index.html`

**Artifacts bleiben 30 Tage erhalten!**

---

## 📏 Coverage-Ziele

```yaml
Frontend:
├── Utilities (pure functions): 100%
├── Business Logic: ≥85%
├── Components: ≥70%
└── Integration Tests: Critical flows

Backend:
├── Core Business Logic: ≥80%
├── Domain Services: ≥85%
├── API Endpoints: ≥75%
└── Integration Tests: Happy + Error paths
```

**Wichtig:** PRs sollten Coverage nicht senken!

---

## 🎨 Coverage verstehen

### Beispiel: HTML-Report

```
frontend/src/utils/validation.ts

  1  export const isValidEmail = (email: string): boolean => {  ✅
  2    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;          ✅
  3    return emailRegex.test(email);                           ✅
  4  };                                                          ✅

Coverage: 100% (4/4 lines)
```

```
frontend/src/utils/format.ts

  1  export const formatPrice = (price: number): string => {    ✅
  2    if (price < 0) {                                         🔴
  3      return 'Invalid';                                      🔴
  4    }                                                         🔴
  5    return `${price.toFixed(2)} €`;                          ✅
  6  };                                                          ✅

Coverage: 50% (3/6 lines)
Missing: Negative price test!
```

### Was bedeuten die Farben?

- **Grün:** Diese Zeile wurde ausgeführt
- **Gelb:** if/else nur teilweise getestet (z.B. nur if, nicht else)
- **Rot:** Diese Zeile wurde nie ausgeführt

---

## 🚀 Workflow-Tipps

### Während der Entwicklung

1. **Starte Vitest UI:**
   ```bash
   npm run test:ui
   ```

2. **Schreibe Code + Tests gleichzeitig**
   - Siehst sofort ob Tests grün sind
   - Coverage aktualisiert sich live

3. **Vor Commit:**
   ```bash
   npm run test:coverage
   # Prüfe: Ist Coverage ≥80% für deine Änderungen?
   ```

### Vor einem PR

1. **Lokaler Coverage-Check:**
   ```bash
   npm run test:coverage
   open coverage/index.html
   ```

2. **Stelle sicher:**
   - Neue Dateien haben ≥80% Coverage
   - Gesamt-Coverage ist nicht gesunken
   - Kritische Business-Logik ist vollständig getestet

3. **Push → CI läuft automatisch**

---

## 🐛 Troubleshooting

**Coverage zu niedrig?**
- Öffne HTML-Report
- Finde rote/gelbe Zeilen
- Schreibe Tests für diese Cases

**Tests laufen, aber keine Coverage?**
```bash
# Frontend: Prüfe ob coverage/ existiert
ls -la frontend/coverage/

# Backend: Prüfe ob jacoco/ existiert
ls -la backend/target/site/jacoco/
```

**Vitest UI lädt nicht?**
```bash
# Port belegt? Versuche anderen Port:
npx vitest --ui --port 51205
```

---

## 📚 Weitere Ressourcen

- **Frontend Testing:** [frontend/TESTING.md](../frontend/TESTING.md)
- **Testing Guide:** [TESTING_GUIDE.md](./planung/grundlagen/TESTING_GUIDE.md)
- **Vitest Docs:** https://vitest.dev
- **JaCoCo Docs:** https://www.jacoco.org

---

## ✅ Quick Start

**Frontend:**
```bash
cd frontend
npm run test:ui  # Interaktiv, beste Experience!
```

**Backend:**
```bash
cd backend
./mvnw verify && open target/site/jacoco/index.html
```

**Das war's!** 🎉

Keine komplexen Tools, keine Cloud-Services - nur lokale Reports die du sofort nutzen kannst!

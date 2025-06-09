# Code Review Standard - FreshPlan Sales Tool 2.0

**📅 Aktuelles Datum: 09.06.2025 (System: 09.06.2025)**

**Gültig ab:** 07.06.2025  
**Status:** VERPFLICHTEND  
**Gilt für:** Alle Entwickler und AI-Assistenten (Claude, ChatGPT, etc.)

## 🔍 Die Goldene Regel

> **"Bei jedem bedeutenden Abschnitt gilt: Prüfe noch einmal sehr gründlich den Code auf Einhaltung unserer Programmierregeln und Logik"**

Diese Regel wurde von Jörg am 07.06.2025 eingeführt, nachdem ein gründlicher Code Review kritische Probleme aufgedeckt hat, die durch regelmäßige Reviews hätten vermieden werden können.

## Wann ist ein Code Review durchzuführen?

### Pflicht-Reviews (MUSS):
- ✅ **Ende jedes Sprints**
- ✅ **Vor jedem Merge in main**
- ✅ **Nach Abschluss eines Features**
- ✅ **Nach größeren Refactorings**
- ✅ **Bei Architektur-Änderungen**
- ✅ **Nach Integration externer Services**

### Empfohlene Reviews (SOLLTE):
- 📝 Nach jedem größeren Commit
- 📝 Bei Unsicherheit über Code-Qualität
- 📝 Vor kritischen Deployments
- 📝 Nach Pair Programming Sessions

## 🔒 NEU: Two-Pass Review System (Doppelte Sicherheit)

**Ab 07.06.2025 gilt:** Jeder Code Review besteht aus ZWEI Durchgängen!

### Warum Two-Pass Review?
Nach der Erfahrung vom heutigen Frontend-Review ist klar: **Fixes können neue Probleme einführen!**

### Der Two-Pass Prozess:

#### 🔍 Pass 1: Initial Review
1. Vollständige Prüfung aller Kriterien
2. Dokumentation aller Findings
3. Priorisierung der Issues
4. **Fixes implementieren**

#### 🔍 Pass 2: Verification Review
1. **Komplette Wiederholung** der Prüfung
2. Verifizierung dass alle Fixes korrekt sind
3. Prüfung auf **neue Issues durch die Fixes**
4. Erst wenn Pass 2 grün → Freigabe

### Two-Pass Template:
```markdown
# Two-Pass Review - [Feature Name]
**Datum Pass 1:** [YYYY-MM-DD]
**Datum Pass 2:** [YYYY-MM-DD]

## 🔍 Pass 1: Initial Review
### Findings:
- Kritisch: X
- Wichtig: Y
- Minor: Z

### Top Issues:
1. [Issue + Fix]
2. [Issue + Fix]

## 🛠️ Fixes Applied
- [Commit Hash] - [Beschreibung]
- [Commit Hash] - [Beschreibung]

## 🔍 Pass 2: Verification Review
### Neue Issues gefunden:
- [ ] Keine neuen Issues ✅
- [ ] X neue Issues gefunden ❌

### Fix-Verification:
- [ ] Alle Fixes korrekt implementiert
- [ ] Keine Regression eingeführt
- [ ] Tests laufen grün

## ✅ Finale Freigabe
- [ ] Pass 1: Complete
- [ ] Pass 2: Complete
- [ ] Ready to Merge: JA/NEIN
```

## Review-Checkliste

### 1. 📏 Programmierregeln-Compliance
- [ ] **Zeilenlänge**: 80-100 Zeichen eingehalten
- [ ] **Naming**: PascalCase für Klassen, camelCase für Methoden/Variablen
- [ ] **Error Handling**: Try-Catch mit spezifischen Exceptions
- [ ] **Dokumentation**: JSDoc/JavaDoc für alle public APIs
- [ ] **DRY**: Keine Code-Duplikation
- [ ] **SOLID**: Single Responsibility eingehalten

### 2. 🔒 Security-Check
- [ ] **Keine hardcoded Credentials** (auch nicht in Tests!)
- [ ] **Input Validation** auf allen Ebenen
- [ ] **SQL Injection** Prevention durch Prepared Statements
- [ ] **XSS Protection** in Frontend-Code
- [ ] **CORS** korrekt konfiguriert
- [ ] **Environment Variables** für Secrets

### 3. 🧪 Test-Coverage
- [ ] **Unit Tests**: ≥ 80% Coverage
- [ ] **Integration Tests**: Alle APIs getestet
- [ ] **Edge Cases**: Null, Empty, Boundary Values
- [ ] **Error Cases**: Exception Handling getestet
- [ ] **Performance Tests**: Bei kritischen Komponenten

### 4. 🧠 Logik-Überprüfung
- [ ] **Business Logic**: Korrekt implementiert
- [ ] **Race Conditions**: Thread-Safety gewährleistet
- [ ] **Transaktionen**: Richtige Grenzen gesetzt
- [ ] **State Management**: Konsistent und vorhersehbar
- [ ] **Memory Management**: Keine Leaks

### 5. ⚡ Performance
- [ ] **Database**: Keine N+1 Queries
- [ ] **Lazy Loading**: Wo sinnvoll implementiert
- [ ] **Caching**: Strategie definiert
- [ ] **Bundle Size**: Im Budget (< 200KB)
- [ ] **Async Operations**: Keine Blockierungen

### 6. 🏗️ Architektur
- [ ] **Feature-basierte Struktur** (Frontend)
- [ ] **Domain-Driven Design** (Backend)
- [ ] **Clean Architecture**: Schichten getrennt
- [ ] **Dependency Injection**: Verwendet
- [ ] **Repository Pattern**: Für Datenzugriff

## Review-Prozess

### 1. Automatisierte Checks
```bash
# Frontend
cd frontend
npm run lint
npm run test:coverage
npm run security:audit
npm run build

# Backend
cd backend
./mvnw clean verify
./mvnw test
./mvnw spotbugs:check
```

### 2. Manuelle Inspektion
1. Code Zeile für Zeile durchgehen
2. Checkliste oben abarbeiten
3. Logik mental durchspielen
4. Edge Cases identifizieren

### 3. Dokumentation
Erstelle einen Review-Report nach folgendem Template:

```markdown
# Code Review Report - [Feature/Sprint Name]
**Datum:** [YYYY-MM-DD]
**Reviewer:** [Name/Claude/ChatGPT]
**Scope:** [Welcher Code wurde reviewed]
**Commit/PR:** [Hash/Number]

## Executive Summary
- Kritische Issues: [Anzahl]
- Wichtige Issues: [Anzahl]
- Kleinere Issues: [Anzahl]
- Verbesserungsvorschläge: [Anzahl]

## Kritische Findings (MUSS sofort behoben werden)
### 1. [Titel des Problems]
**Datei:** `path/to/file.ts:line`
**Problem:** [Beschreibung]
**Code:**
```typescript
// Problematischer Code
```
**Fix:**
```typescript
// Korrigierter Code
```

## Wichtige Findings (SOLLTE vor Release behoben werden)
...

## Compliance-Status
- [X] Programmierregeln: 95%
- [ ] Security: ❌ Hardcoded Credentials gefunden
- [X] Test Coverage: 82%
- [X] Performance: ✅
- [ ] Architektur: ⚠️ Teilweise Feature-basiert

## Positive Aspekte
- [Was war besonders gut]

## Empfehlungen
1. [Konkrete nächste Schritte]
2. [Priorisierung der Fixes]

## Metriken
- Lines of Code reviewed: [Anzahl]
- Cyclomatic Complexity: [Durchschnitt]
- Test Coverage: [Prozent]
- Bundle Size: [KB]
```

### 4. Follow-Up
1. **Kritische Issues**: Sofort beheben, neuer Review
2. **Wichtige Issues**: In aktuellen Sprint einplanen
3. **Kleinere Issues**: In Backlog aufnehmen
4. **Tech Debt**: Dokumentieren und priorisieren

## Tools und Automatisierung

### Empfohlene Tools:
- **ESLint**: JavaScript/TypeScript Linting
- **Prettier**: Code Formatting
- **SonarQube**: Code Quality Analysis
- **SpotBugs**: Java Bug Detection
- **Jest**: Test Coverage Reports
- **Bundle Analyzer**: Bundle Size Analysis

### Git Hooks (Husky):
```json
{
  "husky": {
    "hooks": {
      "pre-commit": "lint-staged",
      "pre-push": "npm test && npm run lint"
    }
  }
}
```

## Konsequenzen bei Nicht-Einhaltung

1. **Code wird nicht gemerged**
2. **Sprint gilt als nicht abgeschlossen**
3. **Technische Schulden werden dokumentiert**
4. **Review-Training für Team**

## Beispiel eines realen Problems (07.01.2025)

Im Frontend-Code wurden folgende kritische Probleme gefunden:

```typescript
// ❌ SCHLECHT: Hardcoded Credentials
login('e2e@test.de', 'test-password');

// ❌ SCHLECHT: Unsicherer Type Cast
catch (error) {
  setPingResult(`Error: ${error}`);
}

// ❌ SCHLECHT: Non-null Assertion ohne Check
createRoot(document.getElementById('root')!).render(
```

Diese hätten durch regelmäßige Reviews vermieden werden können!

## Zusammenfassung

Code Reviews sind KEINE lästige Pflicht, sondern ein essentieller Bestandteil unserer Qualitätssicherung. Sie:
- ✅ Verhindern Bugs in Production
- ✅ Verbessern die Code-Qualität kontinuierlich
- ✅ Fördern Wissenstransfer im Team
- ✅ Reduzieren technische Schulden
- ✅ Erhöhen die Sicherheit

**Remember: "Code, den jeder Entwickler sofort versteht - KEINE KOMPROMISSE!"**
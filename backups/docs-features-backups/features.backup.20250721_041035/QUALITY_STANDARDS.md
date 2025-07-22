# 🏆 Quality Standards & Process Guidelines

**Erstellt:** 12.07.2025  
**Status:** VERBINDLICH für alle Module  
**Zweck:** Enterprise-Grade Code-Qualität sicherstellen

## 🎯 Frontend-Standards

### Layout-Regel: IMMER MainLayoutV2 verwenden!
```typescript
// ✅ RICHTIG - Jede neue Seite/Feature
import { MainLayoutV2 } from '@/layouts/MainLayoutV2';

export const NewFeaturePage = () => {
  return (
    <MainLayoutV2>
      {/* Dein Feature-Content */}
    </MainLayoutV2>
  );
};

// ❌ FALSCH - Kein eigenes Layout erstellen!
```

### Verfügbare Referenz-Implementierungen:
- **M1 (Navigation):** Sidebar in MainLayoutV2
- **M3 (Cockpit):** 3-Spalten-Layout Beispiel
- **M7 (Settings):** Tab-Navigation Beispiel

## 📋 Modul-Abschluss Checkliste

### Bei JEDEM Modul-Abschluss:

#### 1. 🧪 Umfangreiche Tests
```bash
# Unit Tests (Coverage ≥ 80%)
npm run test:unit -- --coverage

# Integration Tests
npm run test:integration

# E2E Tests für User Flows
npm run test:e2e

# Browser-Tests (Chrome, Firefox, Safari)
npm run test:browser

# Performance Tests
npm run test:performance
```

#### 2. 🎨 Automatische Formatierung
```bash
# Frontend
npm run format:check   # Prüfen
npm run format:fix     # Automatisch fixen

# Backend
cd backend && ./mvnw spotless:check
cd backend && ./mvnw spotless:apply
```

#### 3. 🔍 Two-Pass Review
**Pass 1: Automatische Code-Hygiene**
```bash
./scripts/code-review-pass1.sh
# - Formatierung
# - Linting
# - Import-Optimierung
# - Keine TODOs
```

**Pass 2: Strategische Code-Qualität**
- 🏛️ **Architektur:** Folgt Schichtenmodell?
- 🧠 **Logik:** Geschäftslogik korrekt?
- 📖 **Wartbarkeit:** Selbsterklärender Code?
- 🔒 **Security:** Keine Vulnerabilities?
- ⚡ **Performance:** Innerhalb der Budgets?

## 🔄 Git-Workflow

### Commit-Strategie
```bash
# Nach jedem logischen Schritt (2-4 Std)
git add -p  # Selective staging
git commit -m "feat(M4): implement opportunity entity and repository"

# Vor Feierabend IMMER
git status
git commit -m "wip(M4): save work in progress - [was wurde gemacht]"
```

### PR-Trigger-Punkte
Claude schlägt PR vor bei:
- ✅ Modul-Feature komplett
- ✅ Alle Tests grün
- ✅ Code-Review Pass 1+2 bestanden
- ✅ Dokumentation aktualisiert

### PR-Template
```markdown
## 📋 Summary
[Feature/Modul] ist fertig implementiert

## ✅ Checklist
- [ ] Tests: Unit (X%), Integration ✓, E2E ✓
- [ ] Code-Review: Pass 1 ✓, Pass 2 ✓
- [ ] Docs: README ✓, API ✓, Changes ✓
- [ ] Performance: Load < 1s, Bundle < 200KB

## 📸 Screenshots
[Bei UI-Änderungen]

## 🔗 Related
Closes #[Issue-Number]
```

## 🏗️ Enterprise Code-Kriterien

### Wartbarkeit
- **Naming:** Sprechende Namen (min. 3 Wörter für Funktionen)
- **Funktionslänge:** Max. 20 Zeilen
- **Komplexität:** Cyclomatic Complexity < 10
- **Dokumentation:** JSDoc/JavaDoc für alle public APIs

### Sicherheit
- **Input Validation:** Auf allen Ebenen
- **XSS Prevention:** Sanitize alle User-Inputs
- **CORS:** Nur erlaubte Origins
- **Dependencies:** Keine bekannten CVEs

### Performance
- **Bundle Size:** < 200KB per Route
- **API Response:** < 200ms P95
- **Rendering:** < 16ms per Frame
- **Memory:** Keine Leaks (Profiler-Check)

### Best Practices
- **SOLID Principles:** Durchgängig
- **DRY:** Aber nicht auf Kosten der Klarheit
- **Error Handling:** Niemals silent fail
- **Logging:** Structured Logging überall

## 📊 Qualitäts-Gates

```yaml
# In CI/CD Pipeline
quality-gates:
  coverage:
    threshold: 80%
    
  performance:
    bundle-size: 200KB
    lighthouse-score: 90
    
  security:
    vulnerabilities: 0
    
  code-quality:
    duplications: < 3%
    complexity: < 10
```

## 🎯 Integration in Arbeitsablauf

### Täglicher Rhythmus
```
09:00 - Start & Git Pull
09:15 - Feature Development
11:00 - Zwischencommit
12:00 - Tests schreiben
14:00 - Code Review Pass 1
15:00 - Feature Development
16:30 - Tests & Formatierung
17:00 - Commit & Push
17:15 - Dokumentation
17:30 - Handover erstellen
```

### Wöchentlicher Rhythmus
- **Montag:** Planning & Setup
- **Di-Do:** Feature Development
- **Freitag:** Reviews, Tests, PRs

## 💡 Quick Commands für Claude

```bash
# Qualitäts-Check vor Commit
./scripts/quality-check.sh

# Erstellt automatisch:
# - Test-Report
# - Coverage-Report  
# - Performance-Metrics
# - Security-Scan

# PR vorbereiten
./scripts/prepare-pr.sh [branch-name]
```

Diese Standards sind VERBINDLICH und werden bei JEDEM Modul angewendet!
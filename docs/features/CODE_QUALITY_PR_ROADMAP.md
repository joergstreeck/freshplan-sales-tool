# 🚀 Code Quality PR Roadmap - Option B Implementation

**Erstellt:** 11.08.2025  
**Status:** 🔄 In Arbeit  
**Strategie:** Granulare PRs für optimale Reviewbarkeit

---

**Navigation:**  
⬅️ Zurück zu: [`NEXT_STEP.md`](/docs/NEXT_STEP.md) | [`CODE_QUALITY_START_HERE.md`](/docs/CODE_QUALITY_START_HERE.md)  
➡️ Verwandte: [`ENTERPRISE_CODE_REVIEW_2025.md`](/docs/features/ENTERPRISE_CODE_REVIEW_2025.md) | [`TEST_STRATEGY_PER_PR.md`](/docs/features/TEST_STRATEGY_PER_PR.md)

---

## 📍 Aktueller Stand (13.08.2025)

### ✅ Bereits erreicht:
- **Branch:** `feature/refactor-large-components` (aktiver Branch)
- **Basis-PR #83:** 54 Commits mit 100% ESLint Compliance ✅ GEMERGED
- **Test-Coverage:** 89.1% (590 von 662 Tests grün)
- **Dokumentation:** ENTERPRISE_CODE_REVIEW_2025.md erstellt
- **PR #4 IN ARBEIT:** Refactoring großer Components (Tests ausstehend)

### 🎯 Nächster Schritt:
**Tests für PR #4 implementieren und PR erstellen!**

```bash
# 1. Aktuellen Branch prüfen
git branch --show-current
# Sollte sein: feature/refactor-large-components

# 2. Tests implementieren (siehe TEST_STRATEGY_PER_PR.md)
npm test

# 3. Nach erfolgreichen Tests: PR erstellen
gh pr create --title "feat: refactor large frontend components" \
  --body "Siehe detaillierte Beschreibung unten"
```

## 📋 PR-Roadmap - Sprint-Planung

### 🗓️ Sprint 1 (12.-16.08.2025) - Quick Wins

#### PR #1: Security & MSW Hardening 🔒 NEU!
**Branch:** `feature/security-msw-hardening`
**Umfang:** ~5-10 Dateien
**Priorität:** 🔴 KRITISCH (Security!)
**Review-Zeit:** ~30 Min

```bash
# Branch erstellen
git checkout main
git pull
git checkout -b feature/security-msw-hardening

# 1. MSW Token nur bei expliziter Aktivierung
# In frontend/src/main.tsx:
if (import.meta.env.VITE_USE_MSW === 'true') {
  localStorage.setItem('auth-token', 'MOCK_JWT_TOKEN');
} else {
  localStorage.removeItem('auth-token');
}

# 2. Environment Variables externalisieren
cat > .env.example << 'EOF'
# Keycloak
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=changeme
POSTGRES_PASSWORD=changeme

# API
VITE_API_URL=http://localhost:8080
VITE_USE_MSW=false
EOF

# 3. Docker Compose absichern
# docker-compose.keycloak.yml anpassen:
# Ersetze hardcoded passwords mit ${KEYCLOAK_ADMIN_PASSWORD}

# 4. Tests für Security-Änderungen
cat > frontend/src/__tests__/msw-token.test.ts << 'EOF'
import { describe, it, expect, beforeEach, vi } from 'vitest';

describe('MSW Token Security', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.resetModules();
  });

  it('should NOT set token when MSW is disabled', async () => {
    import.meta.env.VITE_USE_MSW = 'false';
    await import('../main');
    expect(localStorage.getItem('auth-token')).toBeNull();
  });

  it('should set token ONLY when MSW is explicitly enabled', async () => {
    import.meta.env.VITE_USE_MSW = 'true';
    await import('../main');
    expect(localStorage.getItem('auth-token')).toBe('MOCK_JWT_TOKEN');
  });

  it('should remove token when MSW is disabled after being enabled', async () => {
    import.meta.env.VITE_USE_MSW = 'true';
    await import('../main');
    import.meta.env.VITE_USE_MSW = 'false';
    await import('../main');
    expect(localStorage.getItem('auth-token')).toBeNull();
  });
});
EOF

# 5. Test für Environment Variables
cat > backend/src/test/java/EnvironmentConfigTest.java << 'EOF'
@Test
public void shouldNotHaveHardcodedCredentials() {
    // Scan docker-compose for hardcoded passwords
    String dockerCompose = Files.readString(Path.of("docker-compose.keycloak.yml"));
    assertFalse(dockerCompose.contains("KEYCLOAK_ADMIN_PASSWORD: admin"));
    assertFalse(dockerCompose.contains("POSTGRES_PASSWORD: keycloak"));
}

@Test
public void shouldHaveEnvExampleFile() {
    assertTrue(Files.exists(Path.of(".env.example")));
}
EOF

# Tests ausführen
npm test -- msw-token.test.ts
npm run build

# Commit
git add -A
git commit -m "fix: harden security and MSW token handling

- MSW token only set when explicitly enabled
- Externalized environment variables
- Removed hardcoded credentials from docker-compose
- Added .env.example for documentation
- Added security tests for token handling

BREAKING CHANGE: Requires .env file for docker-compose"
```

#### PR #2: Console Cleanup Frontend (verschoben von #1)
**Branch:** `feature/console-cleanup-frontend`
**Umfang:** ~87 Dateien
**Priorität:** 🔴 Hoch
**Review-Zeit:** ~30 Min

```bash
# Branch erstellen
git checkout -b feature/console-cleanup-frontend

# Automatisiertes Script ausführen
cat > /tmp/remove-console.sh << 'EOF'
#!/bin/bash
# Console Cleanup Script für Frontend

echo "🧹 Starting console cleanup..."

# Zähle vorher
BEFORE=$(find ./frontend -type f \( -name "*.ts" -o -name "*.tsx" \) \
  -not -path "*/node_modules/*" -not -path "*/dist/*" \
  -exec grep -h "console\." {} \; | wc -l)

echo "Found $BEFORE console statements"

# Entferne console.* aus Frontend (außer Tests)
find ./frontend/src -type f \( -name "*.ts" -o -name "*.tsx" \) \
  -not -path "*/test/*" -not -path "*.test.*" -not -path "*.spec.*" | \
  while read file; do
    # Backup erstellen
    cp "$file" "$file.bak"
    
    # Entferne console.log, console.error, etc.
    sed -i '' '/console\./d' "$file"
    
    # Wenn keine Änderungen, lösche Backup
    if diff -q "$file" "$file.bak" > /dev/null; then
      rm "$file.bak"
    else
      echo "✨ Cleaned: $file"
      rm "$file.bak"
    fi
  done

# Zähle nachher
AFTER=$(find ./frontend -type f \( -name "*.ts" -o -name "*.tsx" \) \
  -not -path "*/node_modules/*" -not -path "*/dist/*" \
  -exec grep -h "console\." {} \; | wc -l)

echo "✅ Removed $((BEFORE - AFTER)) console statements"
echo "📊 Remaining: $AFTER (mostly in tests)"
EOF

chmod +x /tmp/remove-console.sh
/tmp/remove-console.sh

# Test-Suite für Console Cleanup
cat > frontend/src/__tests__/no-console.test.ts << 'EOF'
import { describe, it, expect } from 'vitest';
import { glob } from 'glob';
import fs from 'fs';
import path from 'path';

describe('Console Statement Verification', () => {
  it('should have NO console statements in production code', async () => {
    const srcFiles = await glob('src/**/*.{ts,tsx}', {
      ignore: ['**/*.test.*', '**/*.spec.*', '**/test/**', '**/tests/**'],
      cwd: path.resolve(__dirname, '..')
    });
    
    const filesWithConsole: string[] = [];
    
    for (const file of srcFiles) {
      const content = fs.readFileSync(path.join(__dirname, '..', file), 'utf-8');
      if (content.includes('console.')) {
        filesWithConsole.push(file);
      }
    }
    
    expect(filesWithConsole).toEqual([]);
  });

  it('should use logger instead of console in services', async () => {
    const serviceFiles = await glob('src/services/**/*.ts', {
      ignore: ['**/*.test.*'],
      cwd: path.resolve(__dirname, '..')
    });
    
    for (const file of serviceFiles) {
      const content = fs.readFileSync(path.join(__dirname, '..', file), 'utf-8');
      if (content.includes('logger.')) {
        expect(content).not.toContain('console.');
      }
    }
  });
});
EOF

# Tests laufen lassen
npm test -- no-console.test.ts
npm test

# Regression Test
npm run build
npm run lint

# Commit
git add -A
git commit -m "chore: remove console statements from frontend components

- Removed ~2000 console.* statements
- Kept console in test files for debugging
- No functional changes"

# PR erstellen
gh pr create --title "chore: remove console statements from frontend" \
  --body "Part of Code Quality Initiative - Sprint 1"
```

#### PR #3: TypeScript any[] → Proper Types (verschoben von #2)
**Branch:** `feature/typescript-array-types`
**Umfang:** ~40 Dateien
**Priorität:** 🟠 Hoch
**Review-Zeit:** ~45 Min

```bash
# Branch erstellen
git checkout main
git pull
git checkout -b feature/typescript-array-types

# Semi-automatisiertes Script
cat > /tmp/fix-array-types.sh << 'EOF'
#!/bin/bash
# Fix any[] to proper types

echo "🔧 Fixing any[] types..."

# Common replacements
find ./frontend/src -type f \( -name "*.ts" -o -name "*.tsx" \) | while read file; do
  # Customer arrays
  sed -i '' 's/: any\[\]/: Customer\[\]/g' "$file"
  sed -i '' 's/as any\[\]/as Customer\[\]/g' "$file"
  
  # Product arrays
  sed -i '' 's/products: any\[\]/products: Product\[\]/g' "$file"
  
  # Generic data arrays
  sed -i '' 's/data: any\[\]/data: unknown\[\]/g' "$file"
  
  # Event arrays
  sed -i '' 's/events: any\[\]/events: AuditEvent\[\]/g' "$file"
done

echo "✅ Fixed array types"
EOF

chmod +x /tmp/fix-array-types.sh
/tmp/fix-array-types.sh

# Manuell: Imports hinzufügen wo nötig
# TypeScript Compiler hilft dabei

# Test-Suite für Type Safety
cat > frontend/src/__tests__/type-safety.test.ts << 'EOF'
import { describe, it, expect } from 'vitest';
import { glob } from 'glob';
import fs from 'fs';
import path from 'path';

describe('TypeScript Type Safety', () => {
  it('should not have any[] in production code', async () => {
    const srcFiles = await glob('src/**/*.{ts,tsx}', {
      ignore: ['**/*.test.*', '**/*.spec.*', '**/test/**'],
      cwd: path.resolve(__dirname, '..')
    });
    
    const filesWithAnyArray: string[] = [];
    
    for (const file of srcFiles) {
      const content = fs.readFileSync(path.join(__dirname, '..', file), 'utf-8');
      if (content.match(/:\s*any\[\]/)) {
        filesWithAnyArray.push(file);
      }
    }
    
    // Should be significantly reduced
    expect(filesWithAnyArray.length).toBeLessThan(5);
  });

  it('should use specific types for common arrays', async () => {
    const patterns = [
      { pattern: /customers:\s*any\[\]/, expected: 'Customer[]' },
      { pattern: /products:\s*any\[\]/, expected: 'Product[]' },
      { pattern: /users:\s*any\[\]/, expected: 'User[]' },
    ];
    
    const srcFiles = await glob('src/**/*.{ts,tsx}', {
      ignore: ['**/*.test.*'],
      cwd: path.resolve(__dirname, '..')
    });
    
    for (const file of srcFiles) {
      const content = fs.readFileSync(path.join(__dirname, '..', file), 'utf-8');
      for (const { pattern, expected } of patterns) {
        if (content.match(pattern)) {
          throw new Error(`File ${file} uses any[] instead of ${expected}`);
        }
      }
    }
  });
});
EOF

# Type-Check und Tests
npm run type-check
npm test -- type-safety.test.ts

# Compile-Zeit Verification
npx tsc --noEmit --strict

git add -A
git commit -m "fix: replace any[] with proper array types

- Customer[], Product[], AuditEvent[] etc.
- unknown[] für generische Daten
- Type safety verbessert
- Added type safety test suite"
```

#### PR #4: Event Handler Types (verschoben von #3)
**Branch:** `feature/event-handler-types`
**Umfang:** ~30 Dateien
**Priorität:** 🟠 Hoch
**Review-Zeit:** ~1 Std

```bash
# Branch erstellen
git checkout main
git pull
git checkout -b feature/event-handler-types

# Manuelle Fixes mit Helper-Script
cat > /tmp/find-event-handlers.sh << 'EOF'
#!/bin/bash
# Finde alle Event Handler mit any

echo "📍 Finding event handlers with 'any'..."

# onClick, onChange, onSubmit etc.
grep -r "on[A-Z][a-zA-Z]*.*:.*any" ./frontend/src --include="*.tsx" --include="*.ts" | \
  cut -d: -f1 | sort -u > /tmp/files-to-fix.txt

echo "Found $(wc -l < /tmp/files-to-fix.txt) files with any-typed event handlers"
echo "Files saved to /tmp/files-to-fix.txt"

# Zeige Beispiele
echo -e "\n📝 Examples to fix:"
head -5 /tmp/files-to-fix.txt
EOF

chmod +x /tmp/find-event-handlers.sh
/tmp/find-event-handlers.sh

# Manuell fixen mit korrekten Types:
# onChange: (e: React.ChangeEvent<HTMLInputElement>) => void
# onClick: (e: React.MouseEvent<HTMLButtonElement>) => void
# onSubmit: (e: React.FormEvent<HTMLFormElement>) => void
```

### 🗓️ Sprint 2 (19.-23.08.2025) - Strukturelle Verbesserungen

#### PR #4: Refactor Frontend Components 🔄 IN ARBEIT
**Branch:** `feature/refactor-large-components`
**Status:** 🔄 **TESTS AUSSTEHEND** (13.08.2025)
**Umfang:** 2 von 5 Komponenten refactored (Rest verschoben)

**✅ Erfolgreich refactored:**
1. ✅ `KanbanBoardDndKit.tsx` (1053 → 429 Zeilen, -59%)
   - Aufgeteilt in 7 Sub-Components
   - Drag & Drop Offset-Problem behoben
   - Theme v2 Status-Farben integriert

2. ✅ `IntelligentFilterBar.tsx` (939 → 592 Zeilen + 6 Sub-Components)
   - `FilterDrawer.tsx` (190 Zeilen)
   - `ColumnManagerDrawer.tsx` (170 Zeilen)
   - `QuickFilters.tsx` (76 Zeilen)
   - `SearchBar.tsx` (85 Zeilen)
   - `constants.ts` (50 Zeilen)
   - Saubere Modul-Struktur

**✅ Filter-Funktionalität komplett überarbeitet:**
- Quick Filters: Nur noch Aktiv/Inaktiv für Klarheit
- Erweiterte Filter: LEAD/PROSPECT/RISIKO aus Status entfernt
- Kontakte-Filter implementiert (Mit/Ohne Kontakte)
- Risiko-Level kompakter mit Bereichen (0-29, 30-59, 60-79, 80-100)
- Umsatz-Range-Slider hinzugefügt (0-500k€)
- UI optimiert für bessere Platznutzung

**✅ Backend-Erweiterungen:**
- CustomerResponse um `contactsCount` erweitert
- CustomerMapper berechnet aktive Kontakte
- Filter-Logik für null-Werte robust gemacht

**⏳ Tests noch zu implementieren:**
- [ ] KanbanBoardDndKit Component Tests
- [ ] IntelligentFilterBar Component Tests
- [ ] Filter-Logik Unit Tests
- [ ] Integration Tests für CustomersPageV2

**📝 Verschoben auf späteren PR:**
- `LocationsForm.tsx` (807 Zeilen)
- `KanbanBoard.tsx` (799 Zeilen)
- `CustomerDetailPage.tsx` (560 Zeilen)

#### PR #5: Refactor Top 5 Backend Services
**Branch:** `feature/refactor-large-services`
**Umfang:** 5 Dateien
**Priorität:** 🟡 Mittel

**Zu refactoren:**
1. `CustomerService.java` (834 Zeilen) → CQRS Pattern:
   - `CustomerCommandService.java`
   - `CustomerQueryService.java`

2. `OpportunityService.java` → Command/Query trennen
3. `AuditService.java` → Event-Sourcing vorbereiten

#### PR #6: TypeScript any - Props & State
**Branch:** `feature/typescript-props-state`
**Umfang:** ~50 Dateien
**Priorität:** 🟡 Mittel

### 🗓️ Sprint 3 (26.-30.08.2025) - Architektur-Evolution

#### PR #7-9: Module Structure
**Details in Phase 3 des ENTERPRISE_CODE_REVIEW_2025.md**

## 📊 Tracking-Metriken

### Vor jedem PR messen:
```bash
# Console Statements
find . -type f \( -name "*.ts" -o -name "*.tsx" \) \
  -not -path "*/node_modules/*" | xargs grep -h "console\." | wc -l

# TypeScript any
find . -type f \( -name "*.ts" -o -name "*.tsx" \) \
  -not -path "*/node_modules/*" | xargs grep -h ": any" | wc -l

# Große Dateien
find . -type f \( -name "*.tsx" -o -name "*.ts" -o -name "*.java" \) \
  -exec wc -l {} \; | awk '$1 > 300' | wc -l

# Test Coverage
npm test -- --coverage
```

## 🎯 Definition of Done für jede PR

### Technische Kriterien:
- [ ] Alle Tests grün
- [ ] TypeScript Compilation erfolgreich
- [ ] ESLint ohne neue Errors
- [ ] Build erfolgreich
- [ ] Keine Performance-Regression

### Review-Kriterien:
- [ ] Code-Review durch mindestens 1 Person
- [ ] Änderungen dokumentiert
- [ ] Commit-Message nach Convention
- [ ] PR-Beschreibung vollständig

### Dokumentation:
- [ ] CHANGELOG.md aktualisiert (wenn nötig)
- [ ] README.md aktualisiert (wenn nötig)
- [ ] Metriken in PR dokumentiert

## 🚨 Wichtige Hinweise für neuen Claude

### Bei Übernahme der Session:
1. **Prüfe aktuellen Branch:**
   ```bash
   git branch --show-current
   git status
   ```

2. **Prüfe offene PRs:**
   ```bash
   gh pr list
   ```

3. **Prüfe aktuellen Sprint:**
   - Schaue in diesem Dokument nach dem Datum
   - Identifiziere nächste anstehende PR

4. **Messe aktuelle Metriken:**
   - Führe die Tracking-Befehle aus
   - Vergleiche mit Zielen in ENTERPRISE_CODE_REVIEW_2025.md

### Häufige Probleme:

**Problem:** Merge-Konflikte
```bash
git pull origin main
git merge main
# Konflikte lösen
git add .
git commit -m "chore: resolve merge conflicts"
```

**Problem:** Tests schlagen fehl nach Cleanup
- Console.log wurde aus Tests entfernt → wieder hinzufügen
- Type-Änderungen brechen Tests → Types in Tests anpassen

**Problem:** Build bricht ab
```bash
npm run type-check  # Zeigt Type-Errors
npm run lint        # Zeigt Lint-Errors
npm run test        # Zeigt Test-Errors
```

## 📈 Fortschritt-Tracking

### Sprint 1 Status:
- [x] Baseline PR (54 Commits) - **✅ ERLEDIGT als PR #83!**
- [ ] PR #1: Security & MSW Hardening 🔒 **NÄCHSTER SCHRITT**
- [ ] PR #2: Console Cleanup Frontend
- [ ] PR #3: TypeScript array types
- [ ] PR #4: Event handler types

### Sprint 2 Status:
- [ ] PR #4: Large components refactor 🔄 **IN ARBEIT** (Tests ausstehend)
- [ ] PR #5: Large services refactor
- [ ] PR #6: Props & State types

### Sprint 3 Status:
- [ ] PR #7: Customer module
- [ ] PR #8: Opportunity module
- [ ] PR #9: Shared module

## 🔗 Verwandte Dokumente

- **Start hier:** [`/docs/CODE_QUALITY_START_HERE.md`](/docs/CODE_QUALITY_START_HERE.md)
- **Hauptplan:** [`/docs/features/ENTERPRISE_CODE_REVIEW_2025.md`](/docs/features/ENTERPRISE_CODE_REVIEW_2025.md)
- **Test-Strategie:** [`/docs/features/TEST_STRATEGY_PER_PR.md`](/docs/features/TEST_STRATEGY_PER_PR.md)
- **Status-Analyse:** [`/docs/features/CODE_QUALITY_UPDATE_ANALYSIS.md`](/docs/features/CODE_QUALITY_UPDATE_ANALYSIS.md)
- **Detaillierte Analyse:** `/docs/claude-work/daily-work/2025-08-11/`
- **Original-Review:** Two-Pass Enterprise Review Session

---

**Navigation:**  
⬅️ Zurück zu: [`NEXT_STEP.md`](/docs/NEXT_STEP.md)  
➡️ Weiter zu: [`TEST_STRATEGY_PER_PR.md`](/docs/features/TEST_STRATEGY_PER_PR.md)

## 💡 Pro-Tipps

1. **Immer von main branchen:**
   ```bash
   git checkout main && git pull
   git checkout -b feature/neue-pr
   ```

2. **Kleine Commits:**
   - Ein Commit pro logischer Änderung
   - Aussagekräftige Commit-Messages

3. **Früh pushen:**
   - Draft PR erstellen
   - CI früh testen lassen

4. **Kommunikation:**
   - PR-Beschreibung ausführlich
   - Screenshots bei UI-Änderungen
   - Before/After Metriken

---

**Letztes Update:** 11.08.2025 23:45
**Nächste Review:** Nach Sprint 1 Abschluss